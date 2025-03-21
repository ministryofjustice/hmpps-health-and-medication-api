-- Create a READ-ONLY group and add the digital prison reporting user.
CREATE ROLE read_only_group;

-- Grant access to existing tables for the read-only group
GRANT USAGE ON SCHEMA public TO read_only_group;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO read_only_group;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO read_only_group;

-- Grant access to future tables to the read-only group
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO read_only_group;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO read_only_group;

CREATE ROLE ${dpr_user} WITH LOGIN PASSWORD '${dpr_password}' IN GROUP read_only_group;