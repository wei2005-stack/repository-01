-- =============================================
-- 学籍管理系统 - 安全性脚本
-- 用户角色与权限控制
-- =============================================

-- 注意：先在 master 库中创建登录名
USE master;
GO

-- =============================================
-- 1. 创建登录名
-- =============================================
-- 管理员登录名
IF EXISTS (SELECT * FROM sys.server_principals WHERE name = 'admin_user')
    DROP LOGIN admin_user;
CREATE LOGIN admin_user WITH PASSWORD = 'Admin@123', DEFAULT_DATABASE = student_manage;

-- 教务员登录名（读写成绩和学生信息）
IF EXISTS (SELECT * FROM sys.server_principals WHERE name = 'academic_user')
    DROP LOGIN academic_user;
CREATE LOGIN academic_user WITH PASSWORD = 'Academic@123', DEFAULT_DATABASE = student_manage;

-- 只读用户登录名（仅查询）
IF EXISTS (SELECT * FROM sys.server_principals WHERE name = 'readonly_user')
    DROP LOGIN readonly_user;
CREATE LOGIN readonly_user WITH PASSWORD = 'Read@123', DEFAULT_DATABASE = student_manage;
GO

-- =============================================
-- 2. 在 student_manage 库中创建用户
-- =============================================
USE student_manage;
GO

IF EXISTS (SELECT * FROM sys.database_principals WHERE name = 'admin_user')
    DROP USER admin_user;
CREATE USER admin_user FOR LOGIN admin_user;

IF EXISTS (SELECT * FROM sys.database_principals WHERE name = 'academic_user')
    DROP USER academic_user;
CREATE USER academic_user FOR LOGIN academic_user;

IF EXISTS (SELECT * FROM sys.database_principals WHERE name = 'readonly_user')
    DROP USER readonly_user;
CREATE USER readonly_user FOR LOGIN readonly_user;
GO

-- =============================================
-- 3. 创建角色并分配权限
-- =============================================

-- 管理员角色：全部权限
ALTER ROLE db_owner ADD MEMBER admin_user;

-- 教务员角色：学生、成绩、奖惩的读写权限
GRANT SELECT, INSERT, UPDATE, DELETE ON Student            TO academic_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON Score              TO academic_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON RewardPunishment   TO academic_user;
GRANT SELECT ON Course                                   TO academic_user;
GRANT SELECT ON Class                                    TO academic_user;
GRANT SELECT ON Major                                    TO academic_user;
GRANT SELECT ON Department                               TO academic_user;
GRANT SELECT ON v_StudentInfo                            TO academic_user;
GRANT SELECT ON v_StudentScore                           TO academic_user;
GRANT EXECUTE ON sp_GenderStatsByDept                    TO academic_user;
GRANT EXECUTE ON sp_GenderStatsByMajor                   TO academic_user;
GRANT EXECUTE ON sp_ClassScoreStats                      TO academic_user;
GRANT EXECUTE ON sp_CourseScoreDistribution              TO academic_user;

-- 只读用户角色：仅查询视图和基础表
GRANT SELECT ON v_StudentInfo      TO readonly_user;
GRANT SELECT ON v_StudentScore     TO readonly_user;
GRANT SELECT ON v_ClassStatistics  TO readonly_user;
GRANT SELECT ON v_FailedStudents   TO readonly_user;
GRANT SELECT ON Department         TO readonly_user;
GRANT SELECT ON Major              TO readonly_user;
GRANT SELECT ON Class              TO readonly_user;
GRANT EXECUTE ON sp_GenderStatsByDept  TO readonly_user;
GRANT EXECUTE ON sp_GenderStatsByMajor TO readonly_user;

-- =============================================
-- 4. 拒绝危险操作（只读用户不能修改表结构）
-- =============================================
DENY ALTER, DELETE, INSERT, UPDATE ON SCHEMA::dbo TO readonly_user;

PRINT '安全性设置完成！';
PRINT '   管理员：admin_user / Admin@123（全部权限）';
PRINT '   教务员：academic_user / Academic@123（读写学生/成绩）';
PRINT '   只读用户：readonly_user / Read@123（仅查询）';
GO
