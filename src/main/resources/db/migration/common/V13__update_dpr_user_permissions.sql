-- Grant additional permissions for DPR user to setup replication
GRANT rds_superuser to ${dpr_user};
GRANT rds_replication to ${dpr_user};