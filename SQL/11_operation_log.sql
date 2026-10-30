-- =============================================
-- 学籍管理系统 - 操作日志表及触发器
-- =============================================
USE student_manage;
GO

-- 创建操作日志表
IF OBJECT_ID('OperationLog', 'U') IS NOT NULL
    DROP TABLE OperationLog;
GO

CREATE TABLE OperationLog (
    log_id      INT IDENTITY(1,1) PRIMARY KEY,
    username    VARCHAR(50),                            -- 操作用户
    operation   NVARCHAR(10)    NOT NULL,               -- 操作类型：INSERT/UPDATE/DELETE
    table_name  NVARCHAR(50)    NOT NULL,               -- 操作表名
    record_id   NVARCHAR(100),                          -- 操作记录ID
    old_value   NVARCHAR(1000),                         -- 旧值（JSON格式）
    new_value   NVARCHAR(1000),                         -- 新值（JSON格式）
    op_time     DATETIME        DEFAULT GETDATE()       -- 操作时间
);
GO

-- 创建索引
CREATE INDEX IX_OperationLog_op_time ON OperationLog(op_time);
CREATE INDEX IX_OperationLog_table_name ON OperationLog(table_name);
CREATE INDEX IX_OperationLog_username ON OperationLog(username);
GO

-- =============================================
-- 学生表操作日志触发器
-- =============================================
CREATE TRIGGER trg_Student_Log ON Student
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- INSERT
    IF EXISTS (SELECT * FROM inserted) AND NOT EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, new_value)
        SELECT
            SYSTEM_USER,
            N'INSERT',
            N'Student',
            i.stu_id,
            CONCAT(N'姓名:', i.name, N', 性别:', i.gender, N', 班级:', i.class_id)
        FROM inserted i;
    END

    -- DELETE
    IF EXISTS (SELECT * FROM deleted) AND NOT EXISTS (SELECT * FROM inserted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, old_value)
        SELECT
            SYSTEM_USER,
            N'DELETE',
            N'Student',
            d.stu_id,
            CONCAT(N'姓名:', d.name, N', 性别:', d.gender, N', 班级:', d.class_id)
        FROM deleted d;
    END

    -- UPDATE
    IF EXISTS (SELECT * FROM inserted) AND EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, old_value, new_value)
        SELECT
            SYSTEM_USER,
            N'UPDATE',
            N'Student',
            i.stu_id,
            CONCAT(N'姓名:', d.name, N', 性别:', d.gender, N', 班级:', d.class_id),
            CONCAT(N'姓名:', i.name, N', 性别:', i.gender, N', 班级:', i.class_id)
        FROM inserted i
        JOIN deleted d ON i.stu_id = d.stu_id;
    END
END;
GO

-- =============================================
-- 成绩表操作日志触发器
-- =============================================
CREATE TRIGGER trg_Score_Log ON Score
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- INSERT
    IF EXISTS (SELECT * FROM inserted) AND NOT EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, new_value)
        SELECT
            SYSTEM_USER,
            N'INSERT',
            N'Score',
            CONCAT(i.stu_id, '-', i.course_id, '-', i.semester),
            CONCAT(N'学号:', i.stu_id, N', 课程:', i.course_id, N', 成绩:', i.score, N', 学期:', i.semester)
        FROM inserted i;
    END

    -- DELETE
    IF EXISTS (SELECT * FROM deleted) AND NOT EXISTS (SELECT * FROM inserted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, old_value)
        SELECT
            SYSTEM_USER,
            N'DELETE',
            N'Score',
            CONCAT(d.stu_id, '-', d.course_id, '-', d.semester),
            CONCAT(N'学号:', d.stu_id, N', 课程:', d.course_id, N', 成绩:', d.score, N', 学期:', d.semester)
        FROM deleted d;
    END

    -- UPDATE
    IF EXISTS (SELECT * FROM inserted) AND EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, old_value, new_value)
        SELECT
            SYSTEM_USER,
            N'UPDATE',
            N'Score',
            CONCAT(i.stu_id, '-', i.course_id, '-', i.semester),
            CONCAT(N'成绩:', d.score),
            CONCAT(N'成绩:', i.score)
        FROM inserted i
        JOIN deleted d ON i.stu_id = d.stu_id AND i.course_id = d.course_id AND i.semester = d.semester;
    END
END;
GO

-- =============================================
-- 奖惩表操作日志触发器
-- =============================================
CREATE TRIGGER trg_RewardPunishment_Log ON RewardPunishment
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- INSERT
    IF EXISTS (SELECT * FROM inserted) AND NOT EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, new_value)
        SELECT
            SYSTEM_USER,
            N'INSERT',
            N'RewardPunishment',
            CAST(i.rp_id AS VARCHAR),
            CONCAT(N'学号:', i.stu_id, N', 类型:', i.rp_type, N', 等级:', i.level)
        FROM inserted i;
    END

    -- DELETE
    IF EXISTS (SELECT * FROM deleted) AND NOT EXISTS (SELECT * FROM inserted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, old_value)
        SELECT
            SYSTEM_USER,
            N'DELETE',
            N'RewardPunishment',
            CAST(d.rp_id AS VARCHAR),
            CONCAT(N'学号:', d.stu_id, N', 类型:', d.rp_type, N', 等级:', d.level)
        FROM deleted d;
    END

    -- UPDATE
    IF EXISTS (SELECT * FROM inserted) AND EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO OperationLog (username, operation, table_name, record_id, old_value, new_value)
        SELECT
            SYSTEM_USER,
            N'UPDATE',
            N'RewardPunishment',
            CAST(i.rp_id AS VARCHAR),
            CONCAT(N'类型:', d.rp_type, N', 等级:', d.level),
            CONCAT(N'类型:', i.rp_type, N', 等级:', i.level)
        FROM inserted i
        JOIN deleted d ON i.rp_id = d.rp_id;
    END
END;
GO

PRINT '操作日志表及触发器创建完成！';
GO
