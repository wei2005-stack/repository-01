-- =============================================
-- 学籍管理系统 - 建库建表
-- 数据库：MS SQL Server
-- =============================================

-- 创建数据库
IF DB_ID('student_manage') IS NOT NULL
BEGIN
    ALTER DATABASE student_manage SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE student_manage;
END
GO

CREATE DATABASE student_manage;
GO
USE student_manage;
GO

-- =============================================
-- 1. 院系表
-- =============================================
CREATE TABLE Department (
    dept_id     VARCHAR(10)     PRIMARY KEY,          -- 院系编号
    dept_name   NVARCHAR(50)    NOT NULL UNIQUE,       -- 院系名称（唯一）
    phone       VARCHAR(20),                           -- 联系电话
    address     NVARCHAR(100)                          -- 办公地址
);
GO

-- =============================================
-- 2. 专业表
-- =============================================
CREATE TABLE Major (
    major_id    VARCHAR(10)     PRIMARY KEY,          -- 专业编号
    major_name  NVARCHAR(50)    NOT NULL,              -- 专业名称
    dept_id     VARCHAR(10)     NOT NULL,              -- 所属院系（外键）
    FOREIGN KEY (dept_id) REFERENCES Department(dept_id)
);
GO

-- =============================================
-- 3. 班级表
-- =============================================
CREATE TABLE Class (
    class_id      VARCHAR(15)   PRIMARY KEY,          -- 班级编号
    class_name    NVARCHAR(50)  NOT NULL,              -- 班级名称
    major_id      VARCHAR(10)   NOT NULL,              -- 所属专业（外键）
    enroll_year   INT           NOT NULL,              -- 入学年份
    student_count INT           DEFAULT 0,             -- 学生人数（由触发器维护）
    FOREIGN KEY (major_id) REFERENCES Major(major_id)
);
GO

-- =============================================
-- 4. 学生表（含CHECK约束）
-- =============================================
CREATE TABLE Student (
    stu_id      VARCHAR(20)     PRIMARY KEY,          -- 学号
    name        NVARCHAR(20)    NOT NULL,              -- 姓名
    gender      NCHAR(1)        NOT NULL               -- 性别：只能男或女
                CHECK (gender IN (N'男', N'女')),
    birth_date  DATE,                                 -- 出生日期
    id_card     VARCHAR(18)     UNIQUE,               -- 身份证号（唯一）
    phone       VARCHAR(15),                          -- 联系电话
    address     NVARCHAR(200),                        -- 家庭住址
    enroll_date DATE           NOT NULL,              -- 入学时间
    class_id    VARCHAR(15)    NOT NULL,              -- 所属班级（外键）
    FOREIGN KEY (class_id) REFERENCES Class(class_id)
);
GO

-- =============================================
-- 5. 课程表
-- =============================================
CREATE TABLE Course (
    course_id   VARCHAR(10)     PRIMARY KEY,          -- 课程编号
    course_name NVARCHAR(50)    NOT NULL,              -- 课程名称
    credit      DECIMAL(3,1)    NOT NULL               -- 学分
                CHECK (credit > 0),
    hours       INT             NOT NULL               -- 学时
                CHECK (hours > 0),
    course_type NVARCHAR(10)    NOT NULL               -- 课程类型：必修/选修
                CHECK (course_type IN (N'必修', N'选修'))
);
GO

-- =============================================
-- 6. 成绩表（M:N关系转换）
-- =============================================
CREATE TABLE Score (
    stu_id      VARCHAR(20)     NOT NULL,              -- 学号（外键）
    course_id   VARCHAR(10)     NOT NULL,              -- 课程编号（外键）
    score       DECIMAL(5,2)                           -- 成绩：0~100
                CHECK (score >= 0 AND score <= 100),
    semester    VARCHAR(15)     NOT NULL,              -- 学期（如 2023-2024-1）
    exam_type   NVARCHAR(10)    NOT NULL               -- 考试类型
                DEFAULT N'期末',
    PRIMARY KEY (stu_id, course_id, semester, exam_type),
    FOREIGN KEY (stu_id)    REFERENCES Student(stu_id),
    FOREIGN KEY (course_id) REFERENCES Course(course_id)
);
GO

-- =============================================
-- 7. 奖惩记录表
-- =============================================
CREATE TABLE RewardPunishment (
    rp_id       INT IDENTITY(1,1) PRIMARY KEY,        -- 记录编号（自增）
    stu_id      VARCHAR(20)     NOT NULL,              -- 学号（外键）
    rp_type     NVARCHAR(10)    NOT NULL               -- 类型：奖励/惩罚
                CHECK (rp_type IN (N'奖励', N'惩罚')),
    description NVARCHAR(500),                        -- 描述
    rp_date     DATE           NOT NULL,              -- 日期
    level       NVARCHAR(20),                         -- 等级（如：一等奖、警告等）
    FOREIGN KEY (stu_id) REFERENCES Student(stu_id)
);
GO

PRINT '建库建表完成！';
GO
