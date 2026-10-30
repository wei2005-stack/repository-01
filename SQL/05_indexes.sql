-- =============================================
-- 学籍管理系统 - 索引
-- 设计依据：高频查询场景分析
-- =============================================
USE student_manage;
GO

-- =============================================
-- 学生表索引
-- =============================================
-- 外键索引：按班级查询学生（JOIN频繁）
CREATE INDEX IX_Student_class_id ON Student(class_id);

-- 姓名索引：按姓名搜索学生（查询频繁）
CREATE INDEX IX_Student_name ON Student(name);

-- 入学日期索引：按入学年份/日期筛选
CREATE INDEX IX_Student_enroll_date ON Student(enroll_date);

-- 性别索引：统计男女人数时使用
CREATE INDEX IX_Student_gender ON Student(gender);

-- =============================================
-- 成绩表索引
-- =============================================
-- 课程索引：按课程查询成绩（外键，JOIN频繁）
CREATE INDEX IX_Score_course_id ON Score(course_id);

-- 学期索引：按学期查询/统计成绩
CREATE INDEX IX_Score_semester ON Score(semester);

-- 成绩索引：查询不及格/优秀学生
CREATE INDEX IX_Score_score ON Score(score);

-- =============================================
-- 奖惩记录表索引
-- =============================================
-- 学生索引：按学生查询奖惩记录（外键）
CREATE INDEX IX_RP_stu_id ON RewardPunishment(stu_id);

-- 日期索引：按日期范围查询
CREATE INDEX IX_RP_date ON RewardPunishment(rp_date);

-- 类型索引：按奖励/惩罚分类查询
CREATE INDEX IX_RP_type ON RewardPunishment(rp_type);

-- =============================================
-- 专业表索引
-- =============================================
-- 院系索引：按院系查询专业（外键）
CREATE INDEX IX_Major_dept_id ON Major(dept_id);

-- =============================================
-- 班级表索引
-- =============================================
-- 专业索引：按专业查询班级（外键）
CREATE INDEX IX_Class_major_id ON Class(major_id);

-- 入学年份索引：按年级查询班级
CREATE INDEX IX_Class_enroll_year ON Class(enroll_year);

PRINT '索引创建完成！共创建 14 个索引。';
GO
