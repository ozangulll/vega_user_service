-- Initialize database (first MySQL container start only).
-- Application users are NOT inserted here — they are created by
-- com.vega.userservice.config.DataInitializer from com.vega.userservice.config.SeedUsers
-- (single source of truth). After changing SeedUsers.java, sync: test-docker.sh,
-- start-all-services.sh hints, README-Docker.md.

USE vega_user_db;
