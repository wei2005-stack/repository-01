package com.tqh.studentmanage.controller;

import com.tqh.studentmanage.service.BackupService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Controller
@RequestMapping("/backup")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping({"", "/"})
    public String page(Model model) {
        model.addAttribute("files", backupService.listFiles());
        model.addAttribute("message", null);
        return "backup";
    }

    @PostMapping("/full")
    public String fullBackup(Model model) {
        String message = backupService.fullBackup();
        model.addAttribute("files", backupService.listFiles());
        model.addAttribute("message", message);
        return "backup";
    }

    @PostMapping("/diff")
    public String diffBackup(Model model) {
        String message = backupService.diffBackup();
        model.addAttribute("files", backupService.listFiles());
        model.addAttribute("message", message);
        return "backup";
    }

    @PostMapping("/restore")
    public String restore(@RequestParam String filename, Model model) {
        String message = backupService.restore(filename);
        model.addAttribute("files", backupService.listFiles());
        model.addAttribute("message", message);
        return "backup";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam String filename, Model model) {
        String message = backupService.delete(filename);
        model.addAttribute("files", backupService.listFiles());
        model.addAttribute("message", message);
        return "backup";
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Path file = backupService.resolveFile(filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
