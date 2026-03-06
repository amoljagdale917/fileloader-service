# FACV Loader Service

Spring Boot microservice (Java 8 + Spring Boot 2.7.x) that:
- Reads fixed-width text files from a configured folder.
- Supports both `CLFACV.txt` and `CLFACVHASE.txt`.
- Maps each line into table `STG_HK_OBS_FACV`.
- Runs automatically via daily scheduler (end of day).

## Tech Stack
- JDK 8
- Spring Boot 2.7.18 (below 3.x)
- Spring JDBC

Note: For production DBs (for example Oracle), include your JDBC driver in `pom.xml` based on your environment policy.

## Fixed-width Mapping
- `BNK_NO` (3)
- `CUST_ACCT_NO` (12)
- `SYS_CODE` (3)
- `REC_TYPE` (1)
- `CUST_GP` (1)
- `ITL_CUST_NO` (11)
- `FILLER` (11)
- `LMT_ID` (5)
- `CUST_ID` (11)
- `FILLER1` (7)
- `MAINT_ACCT` (1)

All columns are inserted as nullable (`blank -> null`).

## Configuration
Main config: `src/main/resources/application.yml`

Important properties:
- `loader.input-directory`: folder containing files.
- `loader.file-names`: includes `CLFACV.txt`, `CLFACVHASE.txt`.
- `loader.batch-size`: batch insert size (default `1000`).
- `loader.schedule-cron`: daily schedule (default `23:59`).
- `loader.schedule-zone`: scheduler timezone.
- `loader.run-on-startup`: set `true` to load immediately on app start.

Profiles:
- `dev`: H2 in-memory DB (`application-dev.yml`)
- `prod`: sample external DB configuration (`application-prod.yml`)

## Run
```bash
mvn clean spring-boot:run
```

Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## DDL
- H2 dev schema: `src/main/resources/schema-h2.sql`
- Oracle-style sample: `sql/create_stg_hk_obs_facv.sql`
