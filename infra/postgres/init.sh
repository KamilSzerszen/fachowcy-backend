#!/bin/sh
# DEV ONLY: one database and one user per service (database-per-service rule).
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<SQL
CREATE USER jobs WITH PASSWORD '${JOBS_DB_PASSWORD}';
CREATE DATABASE jobs OWNER jobs;
SQL
