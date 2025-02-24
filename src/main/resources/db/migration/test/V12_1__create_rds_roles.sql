CREATE ROLE rds_superuser WITH LOGIN PASSWORD '${rds_default_credentials}';
CREATE ROLE rds_replication WITH LOGIN PASSWORD '${rds_default_credentials}';