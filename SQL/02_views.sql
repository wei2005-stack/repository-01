-- =============================================
-- 学籍管理系统 - 视图
-- =============================================
USE student_manage;
GO

-- =============================================
-- 视图1：学生基本信息视图
-- 用途：快速查询学生的学号、姓名、性别、院系、专业、班级
-- =============================================
CREATE OR ALTER VIEW v_StudentInfo
AS
SELECT
    s.stu_id      AS 学号,
    s.name        AS 姓名,
    s.gender      AS 性别,
    s.birth_date  AS 出生日期,
    s.phone       AS 联系电话,
    d.dept_name   AS 院系,
    m.major_name  AS 专业,
    c.class_name  AS 班级,
    s.enroll_date AS 入学时间
FROM Student s
JOIN Class c      ON s.class_id  = c.class_id
JOIN Major m      ON c.major_id  = m.major_id
JOIN Department d ON m.dept_id   = d.dept_id;
GO

-- =============================================
-- 视图2：学生成绩综合视图
-- 用途：查询学生成绩详情，包含课程名、学分
-- =============================================
CREATE OR ALTER VIEW v_StudentScore
AS
SELECT
    s.stu_id        AS 学号,
    s.name          AS 姓名,
    d.dept_name     AS 院系,
    m.major_name    AS 专业,
    c.class_name    AS 班级,
    co.course_name  AS 课程,
    co.credit       AS 学分,
    sc.score        AS 成绩,
    sc.semester     AS 学期,
    sc.exam_type    AS 考试类型,
    CASE
        WHEN sc.score >= 90 THEN N'优秀'
        WHEN sc.score >= 80 THEN N'良好'
        WHEN sc.score >= 70 THEN N'中等'
        WHEN sc.score >= 60 THEN N'及格'
        ELSE N'不及格'
    END AS 等级
FROM Score sc
JOIN Student s   ON sc.stu_id    = s.stu_id
JOIN Course co   ON sc.course_id = co.course_id
JOIN Class c     ON s.class_id   = c.class_id
JOIN Major m     ON c.major_id   = m.major_id
JOIN Department d ON m.dept_id   = d.dept_id;
GO

-- =============================================
-- 视图3：班级学生统计视图
-- 用途：查看各班级实际学生人数
-- =============================================
CREATE OR ALTER VIEW v_ClassStatistics
AS
SELECT
    c.class_id      AS 班级编号,
    c.class_name    AS 班级名称,
    m.major_name    AS 专业,
    d.dept_name     AS 院系,
    c.enroll_year   AS 入学年份,
    c.student_count AS 系统人数,
    COUNT(s.stu_id) AS 实际人数
FROM Class c
JOIN Major m       ON c.major_id  = m.major_id
JOIN Department d  ON m.dept_id   = d.dept_id
LEFT JOIN Student s ON c.class_id = s.class_id
GROUP BY c.class_id, c.class_name, m.major_name, d.dept_name,
         c.enroll_year, c.student_count;
GO

-- =============================================
-- 视图4：不及格学生视图
-- 用途：快速定位不及格学生，便于教务管理
-- =============================================
CREATE OR ALTER VIEW v_FailedStudents
AS
SELECT
    s.stu_id        AS 学号,
    s.name          AS 姓名,
    d.dept_name     AS 院系,
    m.major_name    AS 专业,
    c.class_name    AS 班级,
    co.course_name  AS 课程,
    sc.score        AS 成绩,
    sc.semester     AS 学期
FROM Score sc
JOIN Student s   ON sc.stu_id    = s.stu_id
JOIN Course co   ON sc.course_id = co.course_id
JOIN Class c     ON s.class_id   = c.class_id
JOIN Major m     ON c.major_id   = m.major_id
JOIN Department d ON m.dept_id   = d.dept_id
WHERE sc.score < 60;
GO

PRINT '视图创建完成！';
GO
