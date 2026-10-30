-- =============================================
-- 学籍管理系统 - 数据完整性验证脚本
-- 在 SSMS 中执行，逐项检查数据正确性
-- =============================================
USE student_manage;
GO

PRINT '============================================';
PRINT '        数据完整性验证报告';
PRINT '============================================';
PRINT '';

-- =============================================
-- 1. 各表记录数统计
-- =============================================
PRINT '【1】各表记录数统计';
PRINT '--------------------------------------------';
SELECT N'院系表 Department' AS 表名, COUNT(*) AS 记录数 FROM Department
UNION ALL SELECT N'专业表 Major',             COUNT(*) FROM Major
UNION ALL SELECT N'班级表 Class',             COUNT(*) FROM Class
UNION ALL SELECT N'学生表 Student',           COUNT(*) FROM Student
UNION ALL SELECT N'课程表 Course',            COUNT(*) FROM Course
UNION ALL SELECT N'成绩表 Score',             COUNT(*) FROM Score
UNION ALL SELECT N'奖惩表 RewardPunishment',  COUNT(*) FROM RewardPunishment;
GO

PRINT '';

-- =============================================
-- 2. 外键完整性检查（是否存在孤儿记录）
-- =============================================
PRINT '【2】外键完整性检查';
PRINT '--------------------------------------------';

-- 检查：专业是否有不存在的院系
IF EXISTS (SELECT * FROM Major WHERE dept_id NOT IN (SELECT dept_id FROM Department))
    PRINT '❌ 发现专业表中存在无效的院系编号！';
ELSE
    PRINT '✅ 专业表：所有院系编号均有效';

-- 检查：班级是否有不存在的专业
IF EXISTS (SELECT * FROM Class WHERE major_id NOT IN (SELECT major_id FROM Major))
    PRINT '❌ 发现班级表中存在无效的专业编号！';
ELSE
    PRINT '✅ 班级表：所有专业编号均有效';

-- 检查：学生是否有不存在的班级
IF EXISTS (SELECT * FROM Student WHERE class_id NOT IN (SELECT class_id FROM Class))
    PRINT '❌ 发现学生表中存在无效的班级编号！';
ELSE
    PRINT '✅ 学生表：所有班级编号均有效';

-- 检查：成绩是否有不存在的学生
IF EXISTS (SELECT * FROM Score WHERE stu_id NOT IN (SELECT stu_id FROM Student))
    PRINT '❌ 发现成绩表中存在无效的学号！';
ELSE
    PRINT '✅ 成绩表：所有学号均有效';

-- 检查：成绩是否有不存在的课程
IF EXISTS (SELECT * FROM Score WHERE course_id NOT IN (SELECT course_id FROM Course))
    PRINT '❌ 发现成绩表中存在无效的课程编号！';
ELSE
    PRINT '✅ 成绩表：所有课程编号均有效';

-- 检查：奖惩记录是否有不存在的学生
IF EXISTS (SELECT * FROM RewardPunishment WHERE stu_id NOT IN (SELECT stu_id FROM Student))
    PRINT '❌ 发现奖惩表中存在无效的学号！';
ELSE
    PRINT '✅ 奖惩表：所有学号均有效';

GO

PRINT '';

-- =============================================
-- 3. CHECK 约束验证
-- =============================================
PRINT '【3】CHECK约束验证';
PRINT '--------------------------------------------';

-- 检查：性别是否只有"男"或"女"
IF EXISTS (SELECT * FROM Student WHERE gender NOT IN (N'男', N'女'))
    PRINT '❌ 学生表中存在非法性别值！';
ELSE
    PRINT '✅ 性别约束：所有值均为"男"或"女"';

-- 检查：成绩是否在0-100之间
IF EXISTS (SELECT * FROM Score WHERE score < 0 OR score > 100)
    PRINT '❌ 成绩表中存在超出0-100范围的成绩！';
ELSE
    PRINT '✅ 成绩范围：所有成绩均在0-100之间';

-- 检查：课程类型是否只有"必修"或"选修"
IF EXISTS (SELECT * FROM Course WHERE course_type NOT IN (N'必修', N'选修'))
    PRINT '❌ 课程表中存在非法课程类型！';
ELSE
    PRINT '✅ 课程类型：所有值均为"必修"或"选修"';

-- 检查：奖惩类型是否只有"奖励"或"惩罚"
IF EXISTS (SELECT * FROM RewardPunishment WHERE rp_type NOT IN (N'奖励', N'惩罚'))
    PRINT '❌ 奖惩表中存在非法类型值！';
ELSE
    PRINT '✅ 奖惩类型：所有值均为"奖励"或"惩罚"';

-- 检查：学分是否大于0
IF EXISTS (SELECT * FROM Course WHERE credit <= 0)
    PRINT '❌ 课程表中存在学分<=0的记录！';
ELSE
    PRINT '✅ 学分约束：所有学分均大于0';

-- 检查：学时是否大于0
IF EXISTS (SELECT * FROM Course WHERE hours <= 0)
    PRINT '❌ 课程表中存在学时<=0的记录！';
ELSE
    PRINT '✅ 学时约束：所有学时均大于0';

GO

PRINT '';

-- =============================================
-- 4. 唯一性约束验证
-- =============================================
PRINT '【4】唯一性约束验证';
PRINT '--------------------------------------------';

-- 检查：院系名称是否重复
IF EXISTS (SELECT dept_name, COUNT(*) FROM Department GROUP BY dept_name HAVING COUNT(*) > 1)
    PRINT '❌ 院系表中存在重复的院系名称！';
ELSE
    PRINT '✅ 院系名称：无重复';

-- 检查：身份证号是否重复（排除NULL）
IF EXISTS (SELECT id_card, COUNT(*) FROM Student WHERE id_card IS NOT NULL GROUP BY id_card HAVING COUNT(*) > 1)
    PRINT '❌ 学生表中存在重复的身份证号！';
ELSE
    PRINT '✅ 身份证号：无重复';

-- 检查：学号是否重复
IF EXISTS (SELECT stu_id, COUNT(*) FROM Student GROUP BY stu_id HAVING COUNT(*) > 1)
    PRINT '❌ 学生表中存在重复的学号！';
ELSE
    PRINT '✅ 学号：无重复';

GO

PRINT '';

-- =============================================
-- 5. 非空约束验证
-- =============================================
PRINT '【5】非空约束验证';
PRINT '--------------------------------------------';

IF EXISTS (SELECT * FROM Student WHERE name IS NULL OR name = '')
    PRINT '❌ 学生表中存在姓名为空的记录！';
ELSE
    PRINT '✅ 学生姓名：无空值';

IF EXISTS (SELECT * FROM Student WHERE class_id IS NULL)
    PRINT '❌ 学生表中存在班级为空的记录！';
ELSE
    PRINT '✅ 学生班级：无空值';

IF EXISTS (SELECT * FROM Score WHERE score IS NULL)
    PRINT '⚠️ 成绩表中存在成绩为NULL的记录（可能是未录入）';
ELSE
    PRINT '✅ 成绩值：无NULL值';

GO

PRINT '';

-- =============================================
-- 6. 触发器验证（班级人数是否正确）
-- =============================================
PRINT '【6】触发器验证（班级人数）';
PRINT '--------------------------------------------';

SELECT
    c.class_id      AS 班级编号,
    c.class_name    AS 班级名称,
    c.student_count AS 系统记录人数,
    COUNT(s.stu_id) AS 实际人数,
    CASE
        WHEN c.student_count = COUNT(s.stu_id) THEN N'✅ 一致'
        ELSE N'❌ 不一致'
    END AS 状态
FROM Class c
LEFT JOIN Student s ON c.class_id = s.class_id
GROUP BY c.class_id, c.class_name, c.student_count
ORDER BY c.class_id;
GO

PRINT '';

-- =============================================
-- 7. 数据一致性检查
-- =============================================
PRINT '【7】数据一致性检查';
PRINT '--------------------------------------------';

-- 各院系学生人数
PRINT '各院系学生人数：';
SELECT
    d.dept_name AS 院系,
    COUNT(s.stu_id) AS 学生人数
FROM Department d
LEFT JOIN Major m ON d.dept_id = m.dept_id
LEFT JOIN Class c ON m.major_id = c.major_id
LEFT JOIN Student s ON c.class_id = s.class_id
GROUP BY d.dept_name
ORDER BY d.dept_name;
GO

-- 各专业学生人数
PRINT '各专业学生人数：';
SELECT
    d.dept_name   AS 院系,
    m.major_name  AS 专业,
    COUNT(s.stu_id) AS 学生人数
FROM Department d
JOIN Major m ON d.dept_id = m.dept_id
LEFT JOIN Class c ON m.major_id = c.major_id
LEFT JOIN Student s ON c.class_id = s.class_id
GROUP BY d.dept_name, m.major_name
ORDER BY d.dept_name, m.major_name;
GO

-- 性别统计
PRINT '全校性别统计：';
SELECT
    gender AS 性别,
    COUNT(*) AS 人数
FROM Student
GROUP BY gender;
GO

PRINT '';

-- =============================================
-- 8. 成绩数据合理性检查
-- =============================================
PRINT '【8】成绩数据合理性检查';
PRINT '--------------------------------------------';

-- 不及格成绩
PRINT '不及格成绩列表（<60分）：';
SELECT
    s.stu_id    AS 学号,
    s.name      AS 姓名,
    co.course_name AS 课程,
    sc.score    AS 成绩,
    sc.semester AS 学期
FROM Score sc
JOIN Student s ON sc.stu_id = s.stu_id
JOIN Course co ON sc.course_id = co.course_id
WHERE sc.score < 60
ORDER BY sc.score;
GO

-- 成绩统计
PRINT '各课程成绩统计：';
SELECT
    co.course_name AS 课程,
    COUNT(*)       AS 参考人数,
    CAST(AVG(sc.score) AS DECIMAL(5,2)) AS 平均分,
    MAX(sc.score)  AS 最高分,
    MIN(sc.score)  AS 最低分,
    SUM(CASE WHEN sc.score < 60 THEN 1 ELSE 0 END) AS 不及格人数
FROM Score sc
JOIN Course co ON sc.course_id = co.course_id
GROUP BY co.course_name
ORDER BY co.course_name;
GO

PRINT '';

-- =============================================
-- 9. 视图验证
-- =============================================
PRINT '【9】视图验证';
PRINT '--------------------------------------------';

PRINT 'v_StudentInfo 前5条记录：';
SELECT TOP 5 * FROM v_StudentInfo;
GO

PRINT 'v_StudentScore 前5条记录：';
SELECT TOP 5 * FROM v_StudentScore;
GO

PRINT '';

-- =============================================
-- 10. 存储过程验证
-- =============================================
PRINT '【10】存储过程验证';
PRINT '--------------------------------------------';

PRINT '执行 sp_GenderStatsByDept：';
EXEC sp_GenderStatsByDept;
GO

PRINT '执行 sp_GenderStatsByMajor：';
EXEC sp_GenderStatsByMajor;
GO

PRINT '';

-- =============================================
-- 验证总结
-- =============================================
PRINT '============================================';
PRINT '        验证完成！请检查上述各项结果';
PRINT '============================================';
GO
