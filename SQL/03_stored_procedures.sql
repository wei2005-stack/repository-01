-- =============================================
-- 学籍管理系统 - 存储过程脚本
-- =============================================
USE student_manage;
GO

-- =============================================
-- 存储过程1：统计各院系男女人数（题目要求）
-- =============================================
CREATE OR ALTER PROCEDURE sp_GenderStatsByDept
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        d.dept_name                                         AS 院系,
        SUM(CASE WHEN s.gender = N'男' THEN 1 ELSE 0 END)  AS 男生人数,
        SUM(CASE WHEN s.gender = N'女' THEN 1 ELSE 0 END)  AS 女生人数,
        COUNT(*)                                            AS 总人数
    FROM Student s
    JOIN Class c      ON s.class_id = c.class_id
    JOIN Major m      ON c.major_id = m.major_id
    JOIN Department d ON m.dept_id  = d.dept_id
    GROUP BY d.dept_name
    ORDER BY d.dept_name;
END;
GO

-- =============================================
-- 存储过程2：统计各专业男女人数（扩展）
-- =============================================
CREATE OR ALTER PROCEDURE sp_GenderStatsByMajor
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        d.dept_name                                         AS 院系,
        m.major_name                                        AS 专业,
        SUM(CASE WHEN s.gender = N'男' THEN 1 ELSE 0 END)  AS 男生人数,
        SUM(CASE WHEN s.gender = N'女' THEN 1 ELSE 0 END)  AS 女生人数,
        COUNT(*)                                            AS 总人数
    FROM Student s
    JOIN Class c      ON s.class_id = c.class_id
    JOIN Major m      ON c.major_id = m.major_id
    JOIN Department d ON m.dept_id  = d.dept_id
    GROUP BY d.dept_name, m.major_name
    ORDER BY d.dept_name, m.major_name;
END;
GO

-- =============================================
-- 存储过程3：按班级统计成绩
-- =============================================
CREATE OR ALTER PROCEDURE sp_ClassScoreStats
    @class_id VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        co.course_name                                  AS 课程,
        COUNT(*)                                        AS 参考人数,
        CAST(AVG(sc.score) AS DECIMAL(5,2))             AS 平均分,
        MAX(sc.score)                                   AS 最高分,
        MIN(sc.score)                                   AS 最低分,
        SUM(CASE WHEN sc.score < 60 THEN 1 ELSE 0 END) AS 不及格人数
    FROM Score sc
    JOIN Student s ON sc.stu_id    = s.stu_id
    JOIN Course co ON sc.course_id = co.course_id
    WHERE s.class_id = @class_id
    GROUP BY co.course_name
    ORDER BY co.course_name;
END;
GO

-- =============================================
-- 存储过程4：按课程统计成绩分布
-- =============================================
CREATE OR ALTER PROCEDURE sp_CourseScoreDistribution
    @course_id VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        CASE
            WHEN score >= 90 THEN N'优秀(90-100)'
            WHEN score >= 80 THEN N'良好(80-89)'
            WHEN score >= 70 THEN N'中等(70-79)'
            WHEN score >= 60 THEN N'及格(60-69)'
            ELSE N'不及格(<60)'
        END AS 分数段,
        COUNT(*) AS 人数
    FROM Score
    WHERE course_id = @course_id
    GROUP BY
        CASE
            WHEN score >= 90 THEN N'优秀(90-100)'
            WHEN score >= 80 THEN N'良好(80-89)'
            WHEN score >= 70 THEN N'中等(70-79)'
            WHEN score >= 60 THEN N'及格(60-69)'
            ELSE N'不及格(<60)'
        END
    ORDER BY MIN(score) DESC;
END;
GO

-- =============================================
-- 存储过程5：新增学生（含班级人数自动更新）
-- =============================================
CREATE OR ALTER PROCEDURE sp_AddStudent
    @stu_id      VARCHAR(20),
    @name        NVARCHAR(20),
    @gender      NCHAR(1),
    @birth_date  DATE = NULL,
    @id_card     VARCHAR(18) = NULL,
    @phone       VARCHAR(15) = NULL,
    @address     NVARCHAR(200) = NULL,
    @enroll_date DATE,
    @class_id    VARCHAR(15)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        INSERT INTO Student (stu_id, name, gender, birth_date, id_card, phone, address, enroll_date, class_id)
        VALUES (@stu_id, @name, @gender, @birth_date, @id_card, @phone, @address, @enroll_date, @class_id);
        PRINT N'学生添加成功：' + @stu_id;
    END TRY
    BEGIN CATCH
        PRINT N'添加失败：' + ERROR_MESSAGE();
        THROW;
    END CATCH
END;
GO

PRINT '存储过程创建完成！';
GO
