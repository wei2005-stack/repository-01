package com.tqh.studentmanage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 通过 sqlcmd 执行 BACKUP / RESTORE，保持与原课程设计一致的备份恢复能力。
 */
@Service
public class BackupService {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path backupDir;
    private final String server;
    private final String username;
    private final String password;

    public BackupService(@Value("${app.backup-dir}") String backupDir,
                         @Value("${app.sqlcmd.server}") String server,
                         @Value("${app.sqlcmd.username}") String username,
                         @Value("${app.sqlcmd.password}") String password) {
        this.backupDir = Paths.get(backupDir);
        this.server = server;
        this.username = username;
        this.password = password;
    }

    public List<Map<String, Object>> listFiles() {
        List<Map<String, Object>> files = new ArrayList<>();
        try {
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
                return files;
            }
            try (Stream<Path> stream = Files.list(backupDir)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".bak"))
                        .sorted(Comparator.comparing((Path path) -> path.toFile().lastModified()).reversed())
                        .forEach(path -> {
                            File file = path.toFile();
                            String name = file.getName();
                            files.add(Map.of(
                                    "name", name,
                                    "size", String.format("%.2f MB", file.length() / 1024.0 / 1024.0),
                                    "time", LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()),
                                            ZoneId.systemDefault()).format(FILE_TIME),
                                    "type", name.contains("full") ? "完整备份"
                                            : name.contains("diff") ? "差异备份" : "日志备份"));
                        });
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取备份目录失败：" + e.getMessage(), e);
        }
        return files;
    }

    public String fullBackup() {
        String fileName = "student_manage_full_" + timestamp() + ".bak";
        return backup(fileName, "BACKUP DATABASE student_manage TO DISK = '" + resolve(fileName)
                + "' WITH FORMAT, NAME = N'学籍管理系统-完整备份'", "完整备份");
    }

    public String diffBackup() {
        String fileName = "student_manage_diff_" + timestamp() + ".bak";
        return backup(fileName, "BACKUP DATABASE student_manage TO DISK = '" + resolve(fileName)
                + "' WITH DIFFERENTIAL, NAME = N'学籍管理系统-差异备份'", "差异备份");
    }

    private String backup(String fileName, String sql, String type) {
        try {
            execute(sql);
            return "✅ " + type + "成功：" + fileName;
        } catch (Exception e) {
            return "❌ 备份失败：" + e.getMessage();
        }
    }

    public String restore(String fileName) {
        Path file = backupDir.resolve(fileName).normalize();
        if (!file.startsWith(backupDir) || !Files.exists(file)) {
            return "❌ 备份文件不存在";
        }
        try {
            execute("ALTER DATABASE student_manage SET SINGLE_USER WITH ROLLBACK IMMEDIATE");
            execute("RESTORE DATABASE student_manage FROM DISK = '" + file.toAbsolutePath()
                    + "' WITH REPLACE, RECOVERY");
            execute("ALTER DATABASE student_manage SET MULTI_USER");
            return "✅ 数据库恢复成功：" + fileName;
        } catch (Exception e) {
            try {
                execute("ALTER DATABASE student_manage SET MULTI_USER");
            } catch (Exception ignored) {
                // 忽略恢复现场失败
            }
            return "❌ 恢复失败：" + e.getMessage();
        }
    }

    public String delete(String fileName) {
        try {
            Path file = backupDir.resolve(fileName).normalize();
            if (!file.startsWith(backupDir) || !Files.exists(file)) {
                return "❌ 文件不存在";
            }
            Files.delete(file);
            return "✅ 已删除：" + fileName;
        } catch (IOException e) {
            return "❌ 删除失败：" + e.getMessage();
        }
    }

    public Path resolveFile(String fileName) {
        Path file = backupDir.resolve(fileName).normalize();
        return file.startsWith(backupDir) && Files.exists(file) ? file : null;
    }

    private String resolve(String fileName) {
        return backupDir.resolve(fileName).toAbsolutePath().toString();
    }

    private void execute(String sql) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "sqlcmd", "-S", server, "-U", username, "-P", password,
                "-d", "student_manage", "-Q", sql, "-C");
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("sqlcmd 执行超时");
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException(output.trim().isEmpty() ? "sqlcmd 执行失败" : output.trim());
        }
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
