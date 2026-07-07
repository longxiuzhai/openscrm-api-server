# OpenSCRM Java API Server

This directory is the Spring Boot rewrite track for the original Go API server.

## Stack

- Java 11
- Spring Boot 2.7
- MyBatis Plus
- MySQL
- Redis
- RabbitMQ
- Spring Session Redis
- JWT

## Mapping From The Go Project

- `app/controller` -> `controller`
- `app/services` -> `service`
- `app/models` -> `entity` + `mapper`
- `common/app` -> `common/api`
- `common/delay_queue` -> RabbitMQ delayed/retry queues
- `common/redis` and `common/session` -> Spring Data Redis + Spring Session Redis + JWT
- `pkg/easywework` -> Java WeWork client module to be implemented

## Current State

The Spring Boot service now contains migrated APIs for the main staff-admin and staff-frontend flows documented in `MIGRATION.md`, including:

- auth/session, roles, permissions, staff, departments
- customers, tags, customer info display rules, remarks
- welcome messages, materials, quick replies
- contact-way groups/contact ways with WeWork contact-way sync
- group chat sync/query/export/tagging
- customer and group-chat mass messages
- group-chat auto-join
- conversation archive local query/search and optional external msg-archive sync proxy

Schema management uses Flyway:

- `src/main/resources/db/migration/V1__initial_schema.sql`
- later migrations in `src/main/resources/db/migration`

## Runtime Configuration

Important environment variables:

- `ALIYUN_OSS_ENDPOINT`
- `ALIYUN_OSS_BUCKET`
- `ALIYUN_OSS_ACCESS_KEY_ID`
- `ALIYUN_OSS_ACCESS_KEY_SECRET`
- `ALIYUN_OSS_CDN_URL`
- `OPENSCRM_MSG_ARCH_SERVER_URL`
- `OPENSCRM_MSG_ARCH_APP_CODE`

File storage is expected to use Aliyun OSS. The msg-archive service variables are optional; when absent, `/api/v1/staff-admin/chat-msg/sync` reports the local latest seq instead of proxying to the external msg-archive service.

## Verification

Use the project Maven setup and central-only settings file used during migration:

```bash
/Users/jlgl/Documents/develop/apache-maven-3.6.3/bin/mvn -s /private/tmp/openscrm-maven-central-settings.xml -q test
```

Current automated coverage includes compile/test verification and focused unit tests for the msg-archive sync proxy success, fallback, and upstream failure paths. Real Redis/RabbitMQ/MySQL/WeWork integration tests still need an environment-specific test setup.
