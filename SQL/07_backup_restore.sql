-- =============================================
-- 学籍管理系统 - 备份与恢复
-- =============================================
USE master;
GO

-- =============================================
-- 1. 创建备份目录（需提前在文件系统中创建）
-- =============================================
-- 备份文件路径：F:\DatabaseCourseDesign\backup\

-- =============================================
-- 2. 完整备份（推荐每天执行一次）
-- =============================================
BACKUP DATABASE student_manage
TO DISK = 'E:\DatabaseCourseDesign\backup\student_manage_full.bak'
WITH
    FORMAT,
    NAME = N'学籍管理系统-完整备份',
    DESCRIPTION = N'完整备份 student_manage 数据库';
GO

PRINT '完整备份完成！';
GO

-- =============================================
-- 3. 差异备份（推荐每4-6小时执行一次）
-- =============================================
BACKUP DATABASE student_manage
TO DISK = 'E:\DatabaseCourseDesign\backup\student_manage_diff.bak'
WITH
    DIFFERENTIAL,
    NAME = N'学籍管理系统-差异备份',
    DESCRIPTION = N'差异备份 student_manage 数据库';
GO

PRINT '差异备份完成！';
GO

-- =============================================
-- 4. 事务日志备份（推荐每1-2小时执行一次）
-- =============================================
BACKUP LOG student_manage
TO DISK = 'E:\DatabaseCourseDesign\backup\student_manage_log.trn'
WITH
    NAME = N'学籍管理系统-日志备份',
    DESCRIPTION = N'事务日志备份 student_manage 数据库';
GO

PRINT '✅ 事务日志备份完成！';
GO

-- =============================================
-- 5. 恢复数据库（从完整备份恢复）
-- =============================================
-- 取消注释以下代码执行恢复
/*
USE master;
GO
-- 强制断开所有连接
ALTER DATABASE student_manage SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO
-- 恢复数据库
RESTORE DATABASE student_manage
FROM DISK = 'F:\DatabaseCourseDesign\backup\student_manage_full.bak'
WITH REPLACE, RECOVERY;
GO
-- 恢复多用户模式
ALTER DATABASE student_manage SET MULTI_USER;
GO
PRINT '✅ 数据库恢复完成！';
*/

-- =============================================
-- 6. 恢复数据库（从完整备份 + 差异备份恢复）
-- =============================================
-- 取消注释以下代码执行恢复
/*
USE master;
GO
ALTER DATABASE student_manage SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO
-- 先恢复完整备份（NORECOVERY 表示继续恢复后续备份）
RESTORE DATABASE student_manage
FROM DISK = 'F:\DatabaseCourseDesign\backup\student_manage_full.bak'
WITH NORECOVERY, REPLACE;
GO
-- 再恢复差异备份
RESTORE DATABASE student_manage
FROM DISK = 'F:\DatabaseCourseDesign\backup\student_manage_diff.bak'
WITH RECOVERY;
GO
ALTER DATABASE student_manage SET MULTI_USER;
GO
PRINT '✅ 数据库从完整+差异备份恢复完成！';
*/

PRINT '✅ 备份恢复脚本执行完毕！';
GO
