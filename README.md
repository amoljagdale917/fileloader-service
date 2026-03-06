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

Note: For production DBs (for example Oracle), include your JDBC driver in `pom.xml` based on your environment policy.

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

Each line is parsed as fixed-width and preserved as-is (including spaces).

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
- `loader.run-on-startup`: set `true` to load immediately on app start.

Profiles:
- `dev`: H2 in-memory DB (`application-dev.yml`)
- `prod`: sample external DB configuration (`application-prod.yml`)

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
- H2 dev schema: `src/main/resources/schema-h2.sql`
- Oracle-style sample: `sql/create_stg_hk_obs_facv.sql`

## Controller Endpoints
- `POST /api/facv/load`: manually trigger loading of configured files.
- `GET /api/facv/health`: service health response.

## Postman
- Collection: `postman/FACV-Loader-Service.postman_collection.json`
- Environment (local): `postman/FACV-Loader-Local.postman_environment.json`
- Environment (prod template): `postman/FACV-Loader-Prod-Template.postman_environment.json`

## Global Exception Handler
- `@RestControllerAdvice` is added for consistent API error response.
- Error payload fields: `timestamp`, `status`, `error`, `message`, `path`.
