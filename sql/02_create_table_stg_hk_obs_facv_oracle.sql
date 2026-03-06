-- Run this script as the application schema user (for example LOADER_APP),
-- or run as DBA after: ALTER SESSION SET CURRENT_SCHEMA = LOADER_APP;

CREATE TABLE STG_HK_OBS_FACV (
    BNK_NO CHAR(3),
    CUST_ACCT_NO CHAR(12),
    SYS_COD CHAR(3),
    REC_TYPE CHAR(1),
    CUST_GP CHAR(1),
    ITL_CUST_NO CHAR(11),
    FILLER CHAR(11),
    LMT_ID CHAR(5),
    CUST_ID CHAR(11),
    FILLER1 CHAR(7),
    MAINT_ACT CHAR(1)
);

-- Optional indexes (enable if query performance needs it)
-- CREATE INDEX IX_STG_HK_OBS_FACV_CUST_ID ON STG_HK_OBS_FACV (CUST_ID);
-- CREATE INDEX IX_STG_HK_OBS_FACV_ITL_CUST_NO ON STG_HK_OBS_FACV (ITL_CUST_NO);
