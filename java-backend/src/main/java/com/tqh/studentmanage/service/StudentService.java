package com.tqh.studentmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.dto.ImportResult;
import com.tqh.studentmanage.entity.ClassInfo;
import com.tqh.studentmanage.entity.Department;
import com.tqh.studentmanage.entity.Major;
import com.tqh.studentmanage.entity.Student;
import com.tqh.studentmanage.mapper.ClassInfoMapper;
import com.tqh.studentmanage.mapper.DepartmentMapper;
import com.tqh.studentmanage.mapper.MajorMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StudentService {

    public static final int PAGE_SIZE = 10;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int BATCH_SIZE = 500;
    private static final long COUNT_CACHE_TTL_MS = 10_000L;
    private static final int COUNT_CACHE_MAX_SIZE = 200;

    private static final String INSERT_STUDENT_SQL =
            "INSERT INTO Student (stu_id, name, gender, birth_date, id_card, phone, address, enroll_date, class_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final MajorMapper majorMapper;
    private final DepartmentMapper departmentMapper;
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, CachedCount> countCache = new ConcurrentHashMap<>();

    public StudentService(StudentMapper studentMapper, ClassInfoMapper classInfoMapper,
                          MajorMapper majorMapper, DepartmentMapper departmentMapper,
                          JdbcTemplate jdbcTemplate) {
        this.studentMapper = studentMapper;
        this.classInfoMapper = classInfoMapper;
        this.majorMapper = majorMapper;
        this.departmentMapper = departmentMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 分页总数带 10 秒本地缓存：翻页与重复筛选不再反复执行 COUNT(*)。
     */
    public long count(String deptId, String majorId, String classId, Integer enrollYear, String keyword) {
        String key = String.join("|",
                deptId == null ? "" : deptId,
                majorId == null ? "" : majorId,
                classId == null ? "" : classId,
                enrollYear == null ? "" : enrollYear.toString(),
                keyword == null ? "" : keyword);
        long now = System.currentTimeMillis();
        CachedCount cached = countCache.get(key);
        if (cached != null && now - cached.timestamp <= COUNT_CACHE_TTL_MS) {
            return cached.total;
        }
        long total = studentMapper.countStudents(deptId, majorId, classId, enrollYear, like(keyword));
        if (countCache.size() > COUNT_CACHE_MAX_SIZE) {
            countCache.clear();
        }
        countCache.put(key, new CachedCount(total, now));
        return total;
    }

    public void invalidateCountCache() {
        countCache.clear();
    }

    public List<Student> page(String deptId, String majorId, String classId, Integer enrollYear,
                              String keyword, int page, long total) {
        return studentMapper.selectStudentPage(deptId, majorId, classId, enrollYear, like(keyword),
                (page - 1) * PAGE_SIZE, PAGE_SIZE);
    }

    public int resolvePage(int page, long total) {
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        return Math.max(1, Math.min(page, totalPages));
    }

    public int totalPages(long total) {
        return Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
    }

    public String exportCsv(String deptId, String majorId, String classId, Integer enrollYear) {
        List<Student> students = studentMapper.selectForExport(deptId, majorId, classId, enrollYear, null);
        StringWriter writer = new StringWriter();
        writer.write('\uFEFF');
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord("学号", "姓名", "性别", "出生日期", "身份证号", "联系电话",
                    "家庭住址", "入学时间", "班级", "专业", "院系");
            for (Student student : students) {
                printer.printRecord(
                        student.getStuId(),
                        student.getName(),
                        student.getGender(),
                        format(student.getBirthDate()),
                        student.getIdCard(),
                        student.getPhone(),
                        student.getAddress(),
                        format(student.getEnrollDate()),
                        student.getClassName(),
                        student.getMajorName(),
                        student.getDeptName());
            }
        } catch (IOException e) {
            throw new IllegalStateException("生成学生 CSV 失败", e);
        }
        return writer.toString();
    }

    public String exportFileName(String deptId, String majorId, String classId, String enrollYear) {
        StringBuilder name = new StringBuilder("学生信息");
        if (StringUtils.hasText(classId)) {
            ClassInfo classInfo = classInfoMapper.selectById(classId);
            if (classInfo != null) {
                name.append('_').append(classInfo.getClassName());
            }
        } else if (StringUtils.hasText(majorId)) {
            Major major = majorMapper.selectById(majorId);
            if (major != null) {
                name.append('_').append(major.getMajorName());
            }
        } else if (StringUtils.hasText(deptId)) {
            Department department = departmentMapper.selectById(deptId);
            if (department != null) {
                name.append('_').append(department.getDeptName());
            }
        } else if (StringUtils.hasText(enrollYear)) {
            name.append('_').append(enrollYear).append("级");
        } else {
            name.append("_全部");
        }
        return name.append('_').append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .append(".csv").toString();
    }

    /**
     * 批量导入学生：先逐行校验，再按 500 条一批写入，避免逐条 insert 的网络往返。
     */
    public ImportResult importCsv(MultipartFile file, boolean skipErrors) {
        ImportResult result = new ImportResult();
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            result.addError("文件解析失败：" + e.getMessage());
            return result;
        }
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()
                .parse(new StringReader(content))) {

            Set<String> required = Set.of("学号", "姓名", "性别", "入学时间", "班级编号");
            if (!parser.getHeaderMap().keySet().containsAll(required)) {
                result.addError("表头缺少必要列，需要：学号、姓名、性别、入学时间、班级编号");
                return result;
            }

            Set<String> existingClasses = classInfoMapper.selectList(
                            new LambdaQueryWrapper<ClassInfo>().select(ClassInfo::getClassId))
                    .stream().map(ClassInfo::getClassId).collect(Collectors.toSet());

            List<PendingRow> pending = new ArrayList<>();
            Set<String> seenStuIds = new HashSet<>();
            Set<String> seenIdCards = new HashSet<>();
            int lineNumber = 2;
            boolean stopped = false;

            for (CSVRecord row : parser) {
                result.setTotal(result.getTotal() + 1);
                try {
                    Student student = buildStudent(row);
                    if (seenStuIds.contains(student.getStuId())) {
                        throw new IllegalArgumentException("学号 " + student.getStuId() + " 在文件中重复");
                    }
                    if (student.getIdCard() != null && seenIdCards.contains(student.getIdCard())) {
                        throw new IllegalArgumentException("身份证号 " + student.getIdCard() + " 在文件中重复");
                    }
                    if (!existingClasses.contains(student.getClassId())) {
                        throw new IllegalArgumentException("班级编号 " + student.getClassId() + " 不存在");
                    }
                    if (!skipErrors) {
                        checkExistsInDatabase(student);
                    }
                    pending.add(new PendingRow(lineNumber, student));
                    seenStuIds.add(student.getStuId());
                    if (student.getIdCard() != null) {
                        seenIdCards.add(student.getIdCard());
                    }
                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    result.addError("第" + lineNumber + "行：" + e.getMessage());
                    if (!skipErrors) {
                        stopped = true;
                    }
                }
                lineNumber++;
                if (stopped) {
                    break;
                }
            }

            if (skipErrors && !pending.isEmpty()) {
                pending = removeExistingRows(pending, result);
            }
            insertBatch(pending, result);
        } catch (IOException e) {
            result.addError("文件解析失败：" + e.getMessage());
        }
        if (result.getSuccess() > 0) {
            invalidateCountCache();
        }
        return result;
    }

    private Student buildStudent(CSVRecord row) {
        String stuId = value(row, "学号");
        String name = value(row, "姓名");
        String gender = value(row, "性别");
        String birthDate = value(row, "出生日期");
        String idCard = value(row, "身份证号");
        String phone = value(row, "联系电话");
        String address = value(row, "家庭住址");
        String enrollDate = value(row, "入学时间");
        String classId = value(row, "班级编号");

        if (!StringUtils.hasText(stuId) || !StringUtils.hasText(name)
                || !StringUtils.hasText(gender) || !StringUtils.hasText(enrollDate)
                || !StringUtils.hasText(classId)) {
            throw new IllegalArgumentException("必填字段不能为空（学号、姓名、性别、入学时间、班级编号）");
        }
        if (!"男".equals(gender) && !"女".equals(gender)) {
            throw new IllegalArgumentException("性别 '" + gender + "' 不合法，只能是'男'或'女'");
        }

        Student student = new Student();
        student.setStuId(stuId);
        student.setName(name);
        student.setGender(gender);
        student.setBirthDate(parseDate(birthDate));
        student.setIdCard(emptyToNull(idCard));
        student.setPhone(emptyToNull(phone));
        student.setAddress(emptyToNull(address));
        student.setEnrollDate(parseDate(enrollDate));
        student.setClassId(classId);
        return student;
    }

    private void checkExistsInDatabase(Student student) {
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getStuId, student.getStuId())) > 0) {
            throw new IllegalArgumentException("学号 " + student.getStuId() + " 已存在");
        }
        if (student.getIdCard() != null
                && studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getIdCard, student.getIdCard())) > 0) {
            throw new IllegalArgumentException("身份证号 " + student.getIdCard() + " 已存在");
        }
    }

    private List<PendingRow> removeExistingRows(List<PendingRow> pending, ImportResult result) {
        Set<String> stuIds = pending.stream()
                .map(row -> row.student.getStuId()).collect(Collectors.toSet());
        Set<String> idCards = pending.stream()
                .map(row -> row.student.getIdCard()).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> existingStuIds = selectExisting("stu_id", stuIds);
        Set<String> existingIdCards = selectExisting("id_card", idCards);

        List<PendingRow> kept = new ArrayList<>();
        for (PendingRow row : pending) {
            Student student = row.student;
            if (existingStuIds.contains(student.getStuId())) {
                result.setFailed(result.getFailed() + 1);
                result.addError("第" + row.lineNumber + "行：学号 " + student.getStuId() + " 已存在");
                continue;
            }
            if (student.getIdCard() != null && existingIdCards.contains(student.getIdCard())) {
                result.setFailed(result.getFailed() + 1);
                result.addError("第" + row.lineNumber + "行：身份证号 " + student.getIdCard() + " 已存在");
                continue;
            }
            kept.add(row);
        }
        return kept;
    }

    private Set<String> selectExisting(String column, Collection<String> values) {
        Set<String> found = new HashSet<>();
        List<String> list = new ArrayList<>(values);
        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            List<String> chunk = list.subList(i, Math.min(i + BATCH_SIZE, list.size()));
            String placeholders = String.join(",", Collections.nCopies(chunk.size(), "?"));
            String sql = "SELECT " + column + " FROM Student WHERE " + column + " IN (" + placeholders + ")";
            found.addAll(jdbcTemplate.queryForList(sql, String.class, chunk.toArray()));
        }
        return found;
    }

    private void insertBatch(List<PendingRow> rows, ImportResult result) {
        if (rows.isEmpty()) {
            return;
        }
        try {
            jdbcTemplate.batchUpdate(INSERT_STUDENT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Student student = rows.get(i).student;
                    ps.setString(1, student.getStuId());
                    ps.setString(2, student.getName());
                    ps.setString(3, student.getGender());
                    ps.setDate(4, student.getBirthDate() == null ? null : Date.valueOf(student.getBirthDate()));
                    ps.setString(5, student.getIdCard());
                    ps.setString(6, student.getPhone());
                    ps.setString(7, student.getAddress());
                    ps.setDate(8, Date.valueOf(student.getEnrollDate()));
                    ps.setString(9, student.getClassId());
                }

                @Override
                public int getBatchSize() {
                    return rows.size();
                }
            });
            result.setSuccess(result.getSuccess() + rows.size());
        } catch (Exception e) {
            // 批量失败时回退逐条，定位具体失败行
            for (PendingRow row : rows) {
                try {
                    studentMapper.insert(row.student);
                    result.setSuccess(result.getSuccess() + 1);
                } catch (Exception ex) {
                    result.setFailed(result.getFailed() + 1);
                    result.addError("第" + row.lineNumber + "行：" + ex.getMessage());
                }
            }
        }
    }

    private static String value(CSVRecord row, String column) {
        String value = row.isMapped(column) ? row.get(column) : "";
        return value == null ? "" : value.trim();
    }

    private static String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private static LocalDate parseDate(String value) {
        return StringUtils.hasText(value) ? LocalDate.parse(value, DATE_FORMAT) : null;
    }

    private static String format(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    private static String like(String keyword) {
        return StringUtils.hasText(keyword) ? "%" + keyword + "%" : keyword;
    }

    private record PendingRow(int lineNumber, Student student) {
    }

    private record CachedCount(long total, long timestamp) {
    }
}
