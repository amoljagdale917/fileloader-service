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
- Lombok
- Oracle Database (SQL Developer compatible)

## Fixed-width Mapping
- `BNK_NO` (3)
- `CUST_ACCT_NO` (12)
- `SYS_COD` (3)
- `REC_TYPE` (1)
- `CUST_GP` (1)
- `ITL_CUST_NO` (11)
- `FILLER` (11)
- `LMT_ID` (5)
- `CUST_ID` (11)
- `FILLER1` (7)
- `MAINT_ACT` (1)

Each field is parsed from fixed-width positions, then trimmed at start/end before insert.
Internal spaces between characters are preserved.

## Configuration
Profile-based config files:
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`

Important properties:
- `loader.incoming-path`: folder where input files are dropped.
- `loader.success-path`: folder where successfully processed files are moved.
- `loader.failed-path`: folder where failed files are moved.
- Processed files are renamed while moving: `<name>_ddMMyyyy_HHmmssSSS.<ext>`
- `loader.file-names`: includes `CLFACV.txt`, `CLFACVHASE.txt`.
- `loader.batch-size`: batch insert size (default `1000`).
- `loader.schedule-cron`: scheduler cron (default end-of-day `0 59 23 * * *`).
- `loader.schedule-zone`: scheduler timezone.

Profiles:
- `dev`: Oracle SQL Developer local configuration (`application-dev.yml`)
- `prod`: Oracle production configuration (`application-prod.yml`)

Local folder structure example:
- `hub/var/incoming`
- `hub/var/success`
- `hub/var/failed`

Environment configuration (no export needed):
- `application-dev.yml`:
  - `${user.home}/Documents/hub/var/incoming`
  - `${user.home}/Documents/hub/var/success`
  - `${user.home}/Documents/hub/var/failed`
- `application-prod.yml`:
  - `/hub/var/incoming`
  - `/hub/var/success`
  - `/hub/var/failed`

## Run
```bash
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

IntelliJ Run Configuration:
- Main class: `com.loader.facv.FacvLoaderApplication`
- JDK: `1.8`
- Active profile: `dev` or `prod`

## DDL
- Manual script: `sql/create_stg_hk_obs_facv.sql`

## Runtime Trigger
- Loader runs only via scheduler (`loader.schedule-cron`).
- No REST controller is exposed.

## Postman
- Not required because this service has no API endpoints.

## Exception Handling
- File-level processing errors are caught per file; failed files are moved to the failed folder.
- Scheduler and startup triggers catch/log runtime exceptions to keep service alive.
