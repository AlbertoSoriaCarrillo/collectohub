# Local database setup

CollectoHub uses PostgreSQL managed by Spring Boot through `spring.datasource`.
Do not add manual JDBC connectors, `DriverManager` usage, or custom connection classes.

## Expected local connection

The `local` profile points to:

```text
jdbc:postgresql://127.0.0.1:5432/collectohub
```

Default credentials are compatible with `infra/.env.example`:

```text
POSTGRES_HOST=127.0.0.1
POSTGRES_PORT=5432
POSTGRES_DB=collectohub
POSTGRES_USER=collectohub
POSTGRES_PASSWORD=collectohub_local_password
```

## Create database and user

Open `psql` as a PostgreSQL administrator:

```sql
CREATE USER collectohub WITH PASSWORD 'collectohub_local_password';
CREATE DATABASE collectohub OWNER collectohub;
\c collectohub
GRANT ALL ON SCHEMA public TO collectohub;
ALTER SCHEMA public OWNER TO collectohub;
```

If the user or database already exists, adjust with:

```sql
ALTER USER collectohub WITH PASSWORD 'collectohub_local_password';
ALTER DATABASE collectohub OWNER TO collectohub;
```

## Run the backend locally

From `backend`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows PowerShell, environment variables can be set for the current shell:

```powershell
$env:POSTGRES_HOST="127.0.0.1"
$env:POSTGRES_PORT="5432"
$env:POSTGRES_DB="collectohub"
$env:POSTGRES_USER="collectohub"
$env:POSTGRES_PASSWORD="collectohub_local_password"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Liquibase runs automatically at application startup using:

```text
classpath:/db/changelog/db.changelog-master.yaml
```

## Run database integration tests

The Liquibase integration test uses Testcontainers and requires Docker.

From `backend`:

```bash
./mvnw clean verify
```

If Docker is unavailable, Testcontainers tests are skipped by design. Keep them in the codebase; they will run automatically once Docker is installed and reachable by the test process.
