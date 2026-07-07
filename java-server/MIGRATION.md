# Go To Java Migration Plan

## Principles

The original Go service should stay available while the Java service is built module by module. Each migrated module should preserve the HTTP path, request shape, response envelope, database table, and permission behavior unless a change is explicitly accepted.

## Technology Replacement

| Original | Java Replacement |
| --- | --- |
| Gin | Spring MVC |
| GORM | MyBatis Plus |
| Viper YAML config | Spring Boot `application.yml` |
| Go Redis client | Spring Data Redis |
| Redis delay queue | RabbitMQ queue + retry/dead-letter design |
| Gin sessions in Redis | Spring Session Redis + JWT |
| Zap logging | Logback through Spring Boot |
| Go validator | Jakarta Validation |
| `pkg/easywework` | Java WeWork client module |
| OSS storage wrappers | Spring service adapter for Aliyun OSS |

## Migration Order

1. Infrastructure - done
   - Application bootstrap
   - Unified response envelope
   - Global exception handling
   - MyBatis Plus configuration
   - Redis/RabbitMQ configuration
   - Database schema import
   - Full table PO and Mapper generation

2. Authentication And Permission - in progress
   - Staff frontend session
   - Staff admin session
   - JWT issue/parse layer
   - Debug force-login only outside production
   - RBAC guard equivalent

3. Core Enterprise Data - next
   - Department
   - Staff
   - Role
   - Permission

4. Customer Domain
   - Customer
   - Customer info
   - Customer staff relation
   - Customer tags
   - Customer events

5. Marketing And Operations
   - Contact way
   - Welcome message
   - Quick reply
   - Material library
   - Mass message
   - Group chat modules

6. Callback, Async Jobs, And Tasks
   - WeWork callback verification/decryption
   - Event dispatch
   - RabbitMQ consumers with TTL + dead-letter delayed delivery
   - Scheduled jobs

7. Conversation Archive
   - Message archive API client
   - Message parsing
   - Media download
   - Search/query behavior

## Items Needing Confirmation

- File storage: Aliyun OSS is the target adapter.

## Confirmed Decisions

- Session strategy: Spring Session Redis + JWT.
- RabbitMQ delayed message style: TTL + dead-letter queues.
- Java WeWork client: port the current in-repo Go SDK behavior into Java.
- Database migration tool: Flyway with `classpath:db/migration` migrations and `baseline-on-migrate`.

## Completed Java Artifacts

- Spring Boot Java 11 project.
- Full schema at `src/main/resources/db/schema.sql`.
- Flyway initial migration at `src/main/resources/db/migration/V1__initial_schema.sql`.
- 48 generated `*Po` entities under `cn.openscrm.api.persistence.entity`.
- 48 generated `*PoMapper` interfaces under `cn.openscrm.api.persistence.mapper`.
- Common and business error codes.
- Business exception and global exception handling.
- JWT token service.
- Spring Session Redis setup.
- RabbitMQ TTL + dead-letter delayed queue setup.
- WeWork client and callback migration placeholders.
- Staff admin/staff frontend force-login session entry points.
- RBAC annotation and Spring MVC permission interceptor.
- Permission and default role seed service.
- WeWork access token client with Redis cache.
- WeWork department list client.
- Department sync/query service and staff-admin department endpoints.
- WeWork staff ID list client.
- Staff sync service for staff and staff-department relations.
- Staff staff-admin sync/query/get/current endpoints.
- WeWork user detail client.
- Staff sync enriched with user detail fields.
- Staff enable/disable endpoint.
- Department tree get endpoint.
- WeWork OAuth `getuserinfo` client.
- Staff admin QR login URL/callback endpoints.
- Staff frontend H5 OAuth login URL/callback endpoints.
- Login callbacks write Spring Session and issue JWT responses.
- Role query/get/create/update/assign-staff/query-staffs APIs.
- Permission query/get APIs.
- Customer H5 login callback writes Spring Session and issues JWT response.
- Welcome message create/query/get/update/delete APIs.
- Department/staff welcome-message assignment helpers.
- WeWork callback URL verification, AES-CBC decryption, recursive XML field parsing, and event dispatch registry.
- WeWork department create/update/delete callback handlers.
- WeWork staff create/update/delete callback handlers with single-staff sync, relation cleanup, and delete soft-marking.
- WeWork customer relation callback handlers for add/edit/delete/del-follow events.
- Customer callback event records, customer statistics, relation soft-delete/history, and sync-job publish.
- WeWork external contact corp tag list client.
- WeWork tag create/update/delete callback handlers and local tag sync/delete cleanup.
- WeWork external group chat detail client.
- WeWork group-chat create/update/dismiss callback handlers and local group/member sync.
- Contact way group query/get/create/update/delete staff-admin APIs.
- WeWork contact-way add/get/update/delete client.
- Contact way query/get/create/update/delete/batch-update staff-admin APIs with count-preserving staff, backup-staff, and schedule association sync.
- Delayed job topic constants and RabbitMQ ready-queue dispatcher.
- Contact way delayed refresh job publishing/consumer with Redis dedupe keys.
- Contact way daily staff counter reset scheduled task.
- SyncCustomerDataTopic consumer for customer, customer-info, customer-staff relation, and customer-staff tags.
- WeWork external contact detail client.
- WeWork external contact remark, mark-tag, and send-welcome-message clients.
- SyncCustomerDataTopic welcome message flow.
- Contact way add-customer side effects for auto tag, customer remark/description, nickname block, welcome strategy, and normal/schedule/backup staff daily count refresh.
- Staff frontend customer full-info endpoint.
- Customer frontend current-session endpoint.
- WeWork permanent image upload client for welcome-message images.
- Welcome message image upload-url helper endpoint.
- Welcome message create/update image attachment URL conversion.
- Group chat query/detail/get-all/owners/update-tags/sync-one staff-admin APIs.
- Group chat tag relation table and mapper.
- WeWork group-chat list client.
- Group chat full enterprise sync staff-admin API.
- Group chat XLSX export staff-admin API.
- Material library create/query/update/delete staff-admin APIs.
- Material library tag hydration in query responses.
- Material library `material_tag_list` query supports repeated params and comma-separated params with OR matching.
- Material library sidebar-status get/update staff-admin APIs.
- Quick reply group staff-admin create/query/update/delete APIs.
- Quick reply staff-admin create/query/update/delete APIs with reply-detail persistence.
- Quick reply staff-frontend query/group/search/create/update/delete APIs.
- Quick reply staff-frontend routes use staff session while staff-admin routes keep RBAC permission checks.
- Quick reply upload/download signed URL endpoint backed by Aliyun OSS adapter.
- Aliyun OSS and optional msg-archive proxy runtime settings are wired to environment variables.
- File-storage service unit coverage for safe filenames, TTL fallback, adapter routing, and unknown storage type errors.
- Contact way and contact-way group responses flatten primary model fields and hydrate `customer_tags` for Go response parity.
- Contact way response serialization unit coverage for flattened primary fields and `customer_tags`.
- Go-compatible route aliases for `staff_admin`/`staff_frontend`/`customer_frontend` prefixes, login callback dash/underscore variants, contact-way hyphen paths, conversation-archive singular paths, group auto-join paths, group-chat paths, material-library plural path, welcome-message paths, quick-reply paths, and mass-message notify action path.
- Customer mass-message create/query/detail/result/update/delete/customer-filter APIs.
- Customer mass-message filters for staff, add-time, gender, group-chat membership, include-tag AND/OR/NONE, and exclude tags.
- Customer mass-message receiver snapshot persistence in `mass_msg_staff`.
- Group-chat mass-message create/query/detail/delete APIs.
- WeWork add-msg-template client and delayed consumer dispatch for customer/group-chat mass messages.
- WeWork customer mass-message send-result client and local result refresh API.
- WeWork group-chat mass-message task-level send-result refresh API.
- Group-chat mass-message per-chat result table, per-owner `msgid` tracking, local counter recompute, and detail result hydration.
- WeWork main-app text-message client and customer mass-message staff notify API.
- Group-chat auto-join group create/query/update/delete APIs.
- Group-chat auto-join code create/query/update/delete/batch-regroup APIs.
- WeWork contact-way integration for group-chat auto-join code QR generation.
- Group-chat auto-join add-customer callback side effects for auto tag, scan/staff counters, and daily-limit contact-way refresh.
- Group-chat auto-join backup-staff association table, response hydration, callback counters, and daily-limit contact-way refresh fallback.
- Conversation archive session query/message query/search staff-admin APIs backed by local `chat_msg` tables.
- Conversation archive sync endpoint with optional external msg-archive service proxy and local latest-seq fallback.
- Conversation archive msg-archive sync proxy unit coverage for HMAC signature, request payload, local fallback, and non-OK upstream status handling.
- WeWork msg-audit permit-user-list client.
- Staff message-archive enabled-status refresh endpoint.
- Callback registry unit coverage for staff create/update/delete event registration.
- Delete-customer callback risk notifications: staff-delete-customer notifies configured admins in real time or via the delayed queue for the next 8:00 run; customer-delete-staff notifies the affected staff member via WeWork text message.
- Delete-customer notification service unit coverage for enabled/disabled admin notification paths, timed queue payload, and next-8:00 delay calculation.
- `msg_audit_approved` callback registration refreshes staff message-archive enabled status; unit coverage verifies registration and handler dispatch.
- `add_half_external_contact` callback reuses customer sync flow, and `transfer_fail` records a customer event without changing customer-staff relations; unit coverage verifies registration and transfer-fail event creation.
- Customer reminder create/update/delete APIs for staff-admin and staff-frontend, backed by `customer_event` and RabbitMQ delayed reminder delivery.
- Customer reminder unit coverage for event creation, delayed job payload, current-content delivery, and deleted-event skip behavior.
- Customer follow-up/clue-manual create/update/delete APIs for staff-admin and staff-frontend, backed by `customer_event`.
- Customer follow-up/clue-manual unit coverage for create/update/delete service behavior.
- Internal customer tag create/query/delete staff-admin APIs backed by `internal_tag`, with `staff_admin` route aliases.
- Internal customer tag unit coverage for batch create, paging response, and soft delete.
- Customer tag-group query/create/update/delete/exchange-order staff-admin APIs, with `staff_admin` route aliases and Go-compatible request fields.
- WeWork enterprise customer tag add/edit/delete client methods used by customer tag-group management.
- Customer tag-group unit coverage for remote create/update/delete behavior, local persistence, sync cleanup, and local order exchange.
- Group-chat tag create/update/delete staff-admin APIs and group-chat tag-group create/update/query/delete APIs, with `/group-chat` and `/customer-group` path aliases plus `staff_admin` prefix aliases.
- Group-chat tag/tag-group unit coverage for batch tag create, update/delete, inline group tag create/update/delete, and query tag hydration.
- Group-chat welcome-message create/query/update/delete staff-admin APIs, with `/group-chat` and `/customer-group` path aliases plus `staff_admin` prefix aliases.
- Group-chat welcome-message attachment storage corrected from generated `tinyint` to JSON in schema/Flyway/entity, matching the Go `GroupChatWelcomeMsgField`.
- Group-chat welcome-message unit coverage for JSON attachment persistence, corp-scoped update, paging query, and batch delete.
- Customer statistic staff-admin API (`/customers/statistic`) with Go-compatible query fields, statistic type support for total/increase/decrease/net_increase, date-range filling, and multi-staff same-day aggregation.
- Customer statistic unit coverage for total carry-forward behavior, non-total zero-fill behavior, and same-day aggregation.
- Customer loss list staff-admin API (`/customer/losses`) with Go-compatible filters for staff IDs, loss date range, connection-create date range, relationship duration limits, sorting, paging, and customer-staff tag hydration.
- Customer relation-history delete-time columns corrected from generated JSON to `datetime(3)` in schema/Flyway/entity, matching callback write/query semantics.
- Customer loss list unit coverage for empty pages, tag hydration, paging offsets, and sort whitelist behavior.
- Customer loss XLSX export endpoint (`/customer/action/customers-losses-data-export`) with Go-compatible filters and sheet columns; unit coverage verifies workbook generation.
- Customer list XLSX export endpoint (`/customers/action/export`) with Go-compatible export filters, customer/staff/customer-info joins, customer-staff tag hydration, add-way display mapping, and workbook-generation unit coverage.
- Material library tag create/query/delete staff-admin APIs and query staff-frontend API, with `staff_admin`/`staff_frontend` route aliases.
- Material library staff-frontend query API backed by existing material query and tag hydration.
- Common helper APIs for parse-link metadata extraction and public signed upload/download URL generation, including `/common/action/get-signed-url` and `/storage/action/get-signed-url` route compatibility.
- Material tag and common parse-link unit coverage for batch create/query/delete and HTML metadata extraction.
- Staff-delete-customer risk-management APIs for notify-rule get/update, delete-customer record query, and XLSX export, with `/staff-admin` and `/staff_admin` route compatibility.
- Staff-delete-customer notify-rule persistence keeps Go-compatible `is_notify_staff` input behavior while also writing `is_notify_admins` for callback notification compatibility.
- Staff-delete-customer risk-management unit coverage for default notify rule, rule upsert, empty pages, sort whitelist behavior, and workbook generation.
- Customer information configuration APIs for `/customer/info`, `/customer/info/displays`, `/customer/events`, `/customer/remark`, remark order exchange, and remark option CRUD.
- Customer information configuration preserves Go-compatible display-rule update semantics, remark `info_option` hydration, customer-event filtering, and route aliases for `staff_admin`.
- Customer information configuration unit coverage for display-rule defaults/duplicate detection, event-list paging, remark option hydration, option creation, order exchange, and soft-delete style updates.
- Staff-frontend utility APIs for current staff, upload-media, JS config, and JS agent config, with `staff_frontend` route aliases and GET/POST compatibility for JS config routes.
- WeWork JS API ticket, agent-config ticket, and temporary media upload client methods.
- Staff-frontend utility unit coverage for JS config signing, URL fragment stripping, upload-media cache hit, upload-media download/upload path, and Go-compatible temp-material image upload behavior.
- Homepage dashboard APIs for `/action/get-summary` and `/action/get-trend`, with `staff_admin` route alias compatibility.
- Homepage summary preserves Go-compatible counts for total staff, total customers, today's customer increase/decrease, total groups, and today's group join/quit totals.
- Homepage summary unit coverage for count and group join/quit aggregation.
- Staff-admin temp-material upload API (`/api/v1/staff-admin/material/temp`, `/api/v1/staff_admin/material/temp`) reuses the existing temporary media upload flow and preserves the Go service behavior of uploading as image media.
- Root storage compatibility routes for `/storage/action/get-signed-url`, `/storage/action/get_signed_url`, and `/storage/public/**` GET/PUT are present; configured file storage remains OSS by decision, with local root serving kept for legacy/local development compatibility.
- Fresh Go router-to-Java route audit completed; low-risk compatibility aliases were added for `/api/v1/callback`, customer tag sync, staff-admin role/permission routes, group-chat auto-join group list plural route, staff-frontend force-login GET, delete-customer loss notify-rule legacy routes, and customer mass-message upload URL generation.

## Next Manual Migration Batch

1. Implement the remaining staff/customer/tag route gaps from the audit:
   - `GET /api/v1/staff-admin/staff/action/get-all`
   - `GET /api/v1/staff-admin/statistics/:ext-staff-id`
   - `POST /api/v1/staff-admin/customer/action/update-tags`
   - `POST /api/v1/staff-admin/customer/action/update-internal-tags`
   - `POST /api/v1/staff-admin/customer/action/sync`
   - `POST /api/v1/staff-admin/customer/tag`
2. Add focused service tests while porting those endpoints, especially for customer sync and customer tag mutation side effects.
