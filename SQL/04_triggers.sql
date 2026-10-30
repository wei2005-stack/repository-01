-- =============================================
-- 学籍管理系统 - 触发器
-- =============================================
USE student_manage;
GO

-- =============================================
-- 触发器1：新增学生时自动更新班级人数
-- =============================================
CREATE OR ALTER TRIGGER trg_Student_Insert
ON Student
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Class
    SET student_count = student_count + sub.cnt
    FROM Class c
    JOIN (
        SELECT class_id, COUNT(*) AS cnt
        FROM inserted
        GROUP BY class_id
    ) sub ON c.class_id = sub.class_id;
END;
GO

-- =============================================
-- 触发器2：删除学生时自动更新班级人数
-- =============================================
CREATE OR ALTER TRIGGER trg_Student_Delete
ON Student
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE Class
    SET student_count = student_count - sub.cnt
    FROM Class c
    JOIN (
        SELECT class_id, COUNT(*) AS cnt
        FROM deleted
        GROUP BY class_id
    ) sub ON c.class_id = sub.class_id;
END;
GO

-- =============================================
-- 触发器3：修改学生班级时自动更新新旧班级人数
-- =============================================
CREATE OR ALTER TRIGGER trg_Student_Update_Class
ON Student
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    -- 仅在 class_id 发生变化时执行
    IF UPDATE(class_id)
    BEGIN
        -- 旧班级人数减少
        UPDATE Class
        SET student_count = student_count - sub.cnt
        FROM Class c
        JOIN (
            SELECT class_id, COUNT(*) AS cnt
            FROM deleted
            GROUP BY class_id
        ) sub ON c.class_id = sub.class_id;

        -- 新班级人数增加
        UPDATE Class
        SET student_count = student_count + sub.cnt
        FROM Class c
        JOIN (
            SELECT class_id, COUNT(*) AS cnt
            FROM inserted
            GROUP BY class_id
        ) sub ON c.class_id = sub.class_id;
    END;
END;
GO

PRINT '触发器创建完成！';
GO
