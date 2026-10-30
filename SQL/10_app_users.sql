-- =============================================
-- 学籍管理系统 - 应用层用户表
-- =============================================
USE student_manage;
GO

-- 创建应用用户表
IF OBJECT_ID('AppUser', 'U') IS NOT NULL
    DROP TABLE AppUser;
GO

CREATE TABLE AppUser (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL UNIQUE,      -- 登录用户名
    password    VARCHAR(100)    NOT NULL,              -- 密码哈希值
    real_name   NVARCHAR(50),                         -- 真实姓名
    role        VARCHAR(20)     NOT NULL               -- 角色
                CHECK (role IN ('admin', 'academic', 'readonly')),
    student_id  VARCHAR(20)     NULL,                  -- 关联学号（仅学生角色）
    is_active   BIT             DEFAULT 1,             -- 是否启用
    created_at  DATETIME        DEFAULT GETDATE(),     -- 创建时间
    FOREIGN KEY (student_id) REFERENCES Student(stu_id)
);
GO

-- 插入预置用户（密码哈希由 Python hashlib.sha256 生成）
INSERT INTO AppUser (username, password, real_name, role, student_id) VALUES
('admin',   '0374c780983923ead6ba8404181e398db259e6dbeb291813afece285e493b314', N'系统管理员', 'admin',    NULL),
('teacher', 'babd29f333975ecf118cf00b12a4f8f260f177e7ef889ffdbcb6f7084cb81981', N'教务老师',   'academic', NULL),
('student', '95e9c724a2202759358ebe1f4876c40a9748fa51d4b44eecaa45e6f3fbe8bb4f', N'学生用户',   'readonly', '2023010101');
GO

PRINT '✅ 应用用户表创建完成！';
PRINT '   admin / admin123 (管理员)';
PRINT '   teacher / teacher123 (教务老师)';
PRINT '   student / student123 (学生)';
GO
