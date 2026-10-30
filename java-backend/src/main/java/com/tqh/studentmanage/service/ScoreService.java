package com.tqh.studentmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.dto.ImportResult;
import com.tqh.studentmanage.entity.Course;
import com.tqh.studentmanage.entity.Department;
import com.tqh.studentmanage.entity.Major;
import com.tqh.studentmanage.entity.Score;
import com.tqh.studentmanage.entity.Student;
import com.tqh.studentmanage.mapper.ClassInfoMapper;
import com.tqh.studentmanage.mapper.CourseMapper;
import com.tqh.studentmanage.mapper.DepartmentMapper;
import com.tqh.studentmanage.mapper.MajorMapper;
import com.tqh.studentmanage.mapper.ScoreMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScoreService {

    public static final int PAGE_SIZE = 10;

    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final ClassInfoMapper classInfoMapper;
    private final MajorMapper majorMapper;
    private final DepartmentMapper departmentMapper;

    public ScoreService(ScoreMapper scoreMapper, StudentMapper studentMapper, CourseMapper courseMapper,
                        ClassInfoMapper classInfoMapper, MajorMapper majorMapper,
                        DepartmentMapper departmentMapper) {
        this.scoreMapper = scoreMapper;
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.classInfoMapper = classInfoMapper;
        this.majorMapper = majorMapper;
        this.departmentMapper = departmentMapper;
    }

    public long count(String stuId, String stuName, String courseName, String semester) {
        return scoreMapper.countScores(like(stuId), like(stuName), like(courseName), like(semester));
    }

    public List<Score> page(String stuId, String stuName, String courseName, String semester,
                            int page, long total) {
        int current = resolvePage(page, total);
        return scoreMapper.selectScorePage(like(stuId), like(stuName), like(courseName), like(semester),
                (current - 1) * PAGE_SIZE, PAGE_SIZE);
    }

    public int resolvePage(int page, long total) {
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        return Math.max(1, Math.min(page, totalPages));
    }

    public int totalPages(long total) {
        return Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
    }

    public String exportCsv(String stuId, String stuName, String courseName, String semester,
                            String deptId, String majorId, String classId) {
        List<Score> scores = scoreMapper.selectForExport(like(stuId), like(stuName), like(courseName),
                like(semester), deptId, majorId, classId);
        StringWriter writer = new StringWriter();
        writer.write('\uFEFF');
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord("学号", "姓名", "院系", "专业", "班级", "课程", "学分", "成绩", "学期", "考试类型");
            for (Score score : scores) {
                printer.printRecord(score.getStuId(), score.getStudentName(), score.getDeptName(),
                        score.getMajorName(), score.getClassName(), score.getCourseName(),
                        score.getCredit(), score.getScore(), score.getSemester(), score.getExamType());
            }
        } catch (IOException e) {
            throw new IllegalStateException("生成成绩 CSV 失败", e);
        }
        return writer.toString();
    }

    public String exportFileName(String stuId, String stuName, String courseName, String semester,
                                 String deptId, String majorId, String classId) {
        StringBuilder name = new StringBuilder("成绩信息");
        boolean hasAny = false;
        if (StringUtils.hasText(stuId)) {
            name.append("_学号").append(stuId);
            hasAny = true;
        } else if (StringUtils.hasText(stuName)) {
            name.append("_姓名").append(stuName);
            hasAny = true;
        } else if (StringUtils.hasText(courseName)) {
            name.append('_').append(courseName);
            hasAny = true;
        } else if (StringUtils.hasText(classId)) {
            var classInfo = classInfoMapper.selectById(classId);
            if (classInfo != null) {
                name.append('_').append(classInfo.getClassName());
            }
            hasAny = true;
        } else if (StringUtils.hasText(majorId)) {
            Major major = majorMapper.selectById(majorId);
            if (major != null) {
                name.append('_').append(major.getMajorName());
            }
            hasAny = true;
        } else if (StringUtils.hasText(deptId)) {
            Department department = departmentMapper.selectById(deptId);
            if (department != null) {
                name.append('_').append(department.getDeptName());
            }
            hasAny = true;
        }
        if (StringUtils.hasText(semester)) {
            name.append('_').append(semester);
            hasAny = true;
        }
        if (!hasAny) {
            name.append("_全部");
        }
        return name.append('_').append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .append(".csv").toString();
    }

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
                .setHeader().setSkipHeaderRecord(true).setTrim(true)
                .build().parse(new StringReader(content))) {

            Set<String> required = Set.of("学号", "课程编号", "成绩", "学期");
            if (!parser.getHeaderMap().keySet().containsAll(required)) {
                result.addError("表头缺少必要列，需要：学号、课程编号、成绩、学期");
                return result;
            }

            Set<String> existingStudents = new HashSet<>(studentMapper.selectList(
                            new LambdaQueryWrapper<Student>().select(Student::getStuId))
                    .stream().map(Student::getStuId).toList());
            Set<String> existingCourses = new HashSet<>(courseMapper.selectList(
                            new LambdaQueryWrapper<Course>().select(Course::getCourseId))
                    .stream().map(Course::getCourseId).toList());

            int lineNumber = 2;
            for (CSVRecord row : parser) {
                result.setTotal(result.getTotal() + 1);
                try {
                    String stuId = value(row, "学号");
                    String courseId = value(row, "课程编号");
                    String scoreValue = value(row, "成绩");
                    String semester = value(row, "学期");
                    String examType = value(row, "考试类型");
                    if (!StringUtils.hasText(examType)) {
                        examType = "期末";
                    }

                    if (!StringUtils.hasText(stuId) || !StringUtils.hasText(courseId)
                            || !StringUtils.hasText(scoreValue) || !StringUtils.hasText(semester)) {
                        throw new IllegalArgumentException("必填字段不能为空");
                    }
                    if (!existingStudents.contains(stuId)) {
                        throw new IllegalArgumentException("学号 " + stuId + " 不存在");
                    }
                    if (!existingCourses.contains(courseId)) {
                        throw new IllegalArgumentException("课程编号 " + courseId + " 不存在");
                    }
                    BigDecimal scoreValueNumber = new BigDecimal(scoreValue);
                    if (scoreValueNumber.compareTo(BigDecimal.ZERO) < 0
                            || scoreValueNumber.compareTo(new BigDecimal(100)) > 0) {
                        throw new IllegalArgumentException("成绩 " + scoreValue + " 超出0-100范围");
                    }

                    Score score = new Score();
                    score.setStuId(stuId);
                    score.setCourseId(courseId);
                    score.setScore(scoreValueNumber);
                    score.setSemester(semester);
                    score.setExamType(examType);
                    scoreMapper.insert(score);

                    result.setSuccess(result.getSuccess() + 1);
                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    result.addError("第" + lineNumber + "行：" + e.getMessage());
                    if (!skipErrors) {
                        break;
                    }
                }
                lineNumber++;
            }
        } catch (IOException e) {
            result.addError("文件解析失败：" + e.getMessage());
        }
        return result;
    }

    private static String value(CSVRecord row, String column) {
        String value = row.isMapped(column) ? row.get(column) : "";
        return value == null ? "" : value.trim();
    }

    private static String like(String keyword) {
        return StringUtils.hasText(keyword) ? "%" + keyword + "%" : keyword;
    }
}
