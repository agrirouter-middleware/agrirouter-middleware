-- Remove legacy DB-backed business log tables
-- These tables are no longer used; logging moved to BusinessOperationLogService (application logs)

-- Drop tables if they still exist (handles all previous schema variants)
DROP TABLE IF EXISTS `error`;
DROP TABLE IF EXISTS `warning`;
-- Keep business_log_event as-is (still used for separate concerns)
