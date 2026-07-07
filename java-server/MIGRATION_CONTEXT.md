# OpenSCRM Go To Java Migration Context

This file is the handoff context for continuing the Go-to-Java migration. Load this file together with `MIGRATION.md` before resuming work.

## Workspace And Runtime

- Repository root: `/Users/jlgl/IdeaProjects/szccw/openscrm-api-server`
- Java project root: `/Users/jlgl/IdeaProjects/szccw/openscrm-api-server/java-server`
- Original Go code remains under the repository root, mainly `app/`, `pkg/easywework/`, `common/`, `conf/`.
- Maven command: `/Users/jlgl/Documents/develop/apache-maven-3.6.3/bin/mvn`
- Verification command used after every batch:

```bash
/Users/jlgl/Documents/develop/apache-maven-3.6.3/bin/mvn -s /private/tmp/openscrm-maven-central-settings.xml -q test
```

- The machine has Java 8 as default, but this project targets Java 11. The Java 11 path was available in previous runs; keep using the existing project Maven setup and verify with the command above.
- The default Maven mirror has returned 502 in this workspace; use the temporary central-only settings file above unless the mirror is fixed.
- `java-server/` is currently untracked as a whole in git, so `git diff` from repo root may not show useful Java changes. Use `find`, `rg`, and direct file reads.

## Confirmed Technical Choices

- Framework: Spring Boot 2.7.x
- Java version: 11
- Database: MySQL
- ORM: MyBatis Plus
- Cache/session: Redis + Spring Session
- Auth token: JWT
- MQ: RabbitMQ
- Delayed message pattern: TTL wait queue + dead-letter ready queue
- WeWork SDK: port current in-repo Go logic into Java, not a third-party SDK.
- Schema: source snapshot remains at `src/main/resources/db/schema.sql`; Flyway is enabled with initial migration `src/main/resources/db/migration/V1__initial_schema.sql`.

## Migration Principles

- Preserve Go HTTP paths, request fields, response envelope, DB table names, and permission behavior unless explicitly changed.
- Prefer existing Java patterns already created in `java-server`.
- Keep original Go service available while Java is built module by module.
- Add small, focused batches and run Maven tests after each batch.
- Use generated `*Po` entities and `*PoMapper` interfaces under `cn.openscrm.api.persistence`.
- Use `ApiResponse.ok(...)` and `PageResponse<T>` for controller responses.
- Use `CurrentStaffService.requireStaffAdmin(session)` for staff-admin routes.
- Use `@RequirePermission` with `BizIdentity` and `OperationType`.
- Keep compatibility aliases for routes when needed, especially `/staff_admin/...` and `/staff-admin/...`.

## Java Project Structure

- Main application: `cn.openscrm.api.OpenScrmApplication`
- Config: `cn.openscrm.api.config`
- Common response/errors/id/security/mq: `cn.openscrm.api.common`
- Auth/session/RBAC: `cn.openscrm.api.auth`
- WeWork client DTOs and API methods: `cn.openscrm.api.wework`
- WeWork callback decrypt/dispatch/registrars: `cn.openscrm.api.wework.callback`
- Persistence entities: `cn.openscrm.api.persistence.entity`
- Persistence mappers: `cn.openscrm.api.persistence.mapper`
- Migrated business modules:
  - `department`
  - `staff`
  - `role`
  - `permission`
  - `welcome`
- `tag`
- `taggroup`
- `groupchat`
- `groupchattag`
- `groupchatwelcome`
- `material`
- `materialtag`
- `quickreply`
- `massmsg`
- `groupchatautojoin`
- `msgarch`
- `contactway`
- `commonutil`
- `storage`
- `deletenotify`
- `customerconfig`
- `stafffrontend`
- `homepage`
- partial `customer`
- `customer/service/CustomerStatisticService.java`
- `customerexport`
- `customerloss`
- partial customer frontend

## Important Existing Java Files

- `src/main/java/cn/openscrm/api/OpenScrmApplication.java`
- `src/main/java/cn/openscrm/api/config/OpenScrmProperties.java`
- `src/main/java/cn/openscrm/api/config/RabbitMqConfig.java`
- `src/main/java/cn/openscrm/api/common/api/ApiResponse.java`
- `src/main/java/cn/openscrm/api/common/api/PageResponse.java`
- `src/main/java/cn/openscrm/api/common/exception/ErrorCode.java`
- `src/main/java/cn/openscrm/api/common/id/SnowflakeIdGenerator.java`
- `src/main/java/cn/openscrm/api/common/mq/DelayedJobPublisher.java`
- `src/main/java/cn/openscrm/api/common/mq/DelayedJobConsumer.java`
- `src/main/java/cn/openscrm/api/common/mq/DelayedJobTopics.java`
- `src/main/java/cn/openscrm/api/auth/service/CurrentStaffService.java`
- `src/main/java/cn/openscrm/api/auth/service/LoginService.java`
- `src/main/java/cn/openscrm/api/wework/WeWorkClient.java`
- `src/main/java/cn/openscrm/api/wework/DefaultWeWorkClient.java`
- `src/main/java/cn/openscrm/api/wework/callback/WeWorkCallbackService.java`
- `src/main/java/cn/openscrm/api/contactway/service/ContactWayService.java`
- `src/main/java/cn/openscrm/api/contactway/service/ContactWayScheduledTasks.java`
- `src/main/java/cn/openscrm/api/groupchat/service/GroupChatSyncService.java`
- `MIGRATION.md`

## Completed Capabilities

See `MIGRATION.md` for the canonical checklist. As of this handoff, the following broad areas are in place:

- Spring Boot Java project scaffold.
- Full schema SQL and generated 48 persistence entities/mappers.
- Unified API response, errors, global exception handling.
- JWT and Spring Session Redis.
- RabbitMQ TTL + dead-letter delayed queue.
- Staff admin/staff login URL/callback/force-login and session writing.
- RBAC annotation/interceptor.
- Permission and role APIs.
- Department sync/query/tree.
- Staff sync/query/detail/current/enable.
- Welcome message CRUD and department/staff assignment helpers.
- WeWork callback verification/decryption/recursive XML field parsing/event registry.
- Callback handlers for department, staff, customer relation, tag, group chat.
- WeWork client methods for access token, departments, staff list/detail/oauth userinfo, external tags, group chat detail, contact way add/get/update/delete.
- Tag sync and delete cleanup.
- Group chat create/update/dismiss callbacks sync local group/member tables.
- Contact way group CRUD.
- Contact way CRUD/batch update, count-preserving staff/backup/schedule association sync, WeWork contact-way sync.
- Contact way delayed refresh publishing/consumer with Redis dedupe keys and daily counter reset scheduled task.
- Staff frontend customer full-info endpoint.
- Customer frontend current-session endpoint.
- WeWork permanent image upload client for welcome-message image attachments.
- Welcome message upload-url helper endpoint and create/update image URL conversion.
- Group chat query/detail/get-all/owners/update-tags/sync-one staff-admin APIs.
- Group chat tag relation table and mapper.
- WeWork group-chat list client.
- Group chat full enterprise sync staff-admin API.
- Group chat XLSX export staff-admin API.
- Material library create/query/update/delete staff-admin APIs.
- Material library `material_tag_list` query supports repeated params and comma-separated params with OR matching.
- Material library sidebar-status get/update staff-admin APIs.
- Quick reply group staff-admin and staff-frontend APIs.
- Quick reply staff-admin CRUD/query APIs.
- Quick reply staff-frontend query/search APIs.
- Quick reply staff-frontend routes use staff session while staff-admin routes keep RBAC permission checks.
- Quick reply upload/download signed URL endpoint backed by Aliyun OSS adapter.
- Aliyun OSS and optional msg-archive proxy runtime settings are wired to environment variables.
- File-storage service unit coverage for safe filenames, TTL fallback, adapter routing, and unknown storage type errors.
- Contact way and contact-way group responses flatten primary model fields and hydrate `customer_tags` for Go response parity.
- Contact way response serialization unit coverage for flattened primary fields and `customer_tags`.
- Go-compatible route aliases for `staff_admin`/`staff_frontend`/`customer_frontend` prefixes, login callback dash/underscore variants, contact-way hyphen paths, conversation-archive singular paths, group auto-join paths, group-chat paths, material-library plural path, welcome-message paths, quick-reply paths, and mass-message notify action path.
- Customer mass-message local task APIs, receiver snapshot persistence, and delayed WeWork dispatch.
- Customer mass-message filters for staff, add-time, gender, group-chat membership, include-tag AND/OR/NONE, and exclude tags.
- Group-chat mass-message local task APIs and delayed WeWork dispatch.
- Customer mass-message WeWork send-result polling and local counter refresh.
- Group-chat mass-message task-level WeWork send-result polling and local counter refresh.
- Group-chat mass-message per-chat result table, per-owner `msgid` tracking, local counter recompute, and detail result hydration.
- Customer mass-message staff notify endpoint via WeWork main-app text message.
- Group-chat auto-join groups and auto-join code CRUD/query/batch-regroup APIs.
- Group-chat auto-join WeWork contact-way add/update/delete integration.
- Group-chat auto-join add-customer callback side effects for auto tag, scan/staff counters, and daily-limit contact-way refresh.
- Group-chat auto-join backup-staff association table, response hydration, callback counters, and daily-limit contact-way refresh fallback.
- Conversation archive local query/search APIs over `chat_msg` and `chat_msg_content`.
- Conversation archive sync endpoint with optional external msg-archive service proxy and local latest-seq fallback.
- Conversation archive msg-archive sync proxy unit coverage for HMAC signature, request payload, local fallback, and non-OK upstream status handling.
- WeWork msg-audit permit-user-list client and staff archive-status refresh endpoint.
- WeWork staff create/update/delete callback handlers with single-staff sync, stale staff-department cleanup, delete soft-marking, and callback registry unit coverage.
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
- Customer tag-group query/create/update/delete/exchange-order staff-admin APIs, backed by `tag_group`/`tag`, WeWork enterprise customer tag add/edit/delete APIs, and `staff_admin` route aliases.
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
- Staff-delete-customer risk-management APIs for notify-rule get/update, record query, and XLSX export, with route compatibility for `/notify/delete-customer/status`, `/notify/delete_customer/status`, `/notify/delete-customers`, `/action/notify/delete-customer`, and `/staff/action/delete-customers-data-export`.
- Staff-delete-customer notify-rule persistence updates both `is_notify_staff` and `is_notify_admins` from the Go-compatible `is_notify_staff` request field so callback admin notification continues to work.
- Staff-delete-customer risk-management unit coverage for default notify rule, rule upsert, empty pages, sort whitelist behavior, and workbook generation.
- Customer information configuration APIs for `/customer/info`, `/customer/info/displays`, `/customer/events`, `/customer/remark`, remark order exchange, and remark option CRUD, with `staff_admin` route aliases.
- Customer information configuration preserves Go-compatible display-rule update semantics, customer-event filters, remark `info_option` hydration, and soft-delete style deletes.
- Customer information configuration unit coverage for display-rule defaults/duplicate detection, event-list paging, remark option hydration, option creation, order exchange, and soft-delete style updates.
- Staff-frontend utility APIs for current staff, upload-media, JS config, and JS agent config, with `staff_frontend` route aliases and GET/POST compatibility for JS config routes.
- WeWork JS API ticket, agent-config ticket, and temporary media upload client methods.
- Staff-frontend utility unit coverage for JS config signing, URL fragment stripping, upload-media cache hit, upload-media download/upload path, and Go-compatible temp-material image upload behavior.
- Homepage dashboard APIs for `/action/get-summary` and `/action/get-trend`, with `staff_admin` route alias compatibility.
- Homepage summary preserves Go-compatible counts for total staff, total customers, today's customer increase/decrease, total groups, and today's group join/quit totals.
- Homepage summary unit coverage for count and group join/quit aggregation.
- Staff-admin temp-material upload API (`/api/v1/staff-admin/material/temp`, `/api/v1/staff_admin/material/temp`) reuses the existing temporary media upload flow and preserves the Go service behavior of uploading as image media.
- Root storage compatibility routes for `/storage/action/get-signed-url`, `/storage/action/get_signed_url`, and `/storage/public/**` GET/PUT are present. File storage target is confirmed as OSS; local root serving remains only for legacy/local development compatibility.
- Fresh Go router-to-Java route audit completed; low-risk compatibility aliases were added for `/api/v1/callback`, customer tag sync, staff-admin role/permission routes, group-chat auto-join group list plural route, staff-frontend force-login GET, delete-customer loss notify-rule legacy routes, and customer mass-message upload URL generation.

## Current MQ/Async State

Rabbit config:

- Exchange: `openscrm.exchange`
- Dead-letter exchange: `openscrm.dead-letter.exchange`
- Wait queue: `openscrm.delayed-job.wait.queue`
- Ready queue: `openscrm.delayed-job.ready.queue`

Topic constants:

- `topic:RefreshContactWayTopic`
- `topic:SyncCustomerDataTopic`
- `topic:MassMsgTopic`
- `topic:GroupChatMassMsgTopic`
- `topic:StaffDeleteCustomerTopic`
- `topic:RemainderTopic`

`DelayedJobConsumer` currently:

- Handles `RefreshContactWayTopic` by calling `ContactWayService.refresh(id, extCorpId)`.
- Handles `SyncCustomerDataTopic` by calling `CustomerSyncService.syncSingle(extStaffId, extCustomerId, welcomeCode, state)`.
- Handles `MassMsgTopic` by calling `MassMsgService.sendMassMsgToWeWork(id, extCorpId)`.
- Handles `GroupChatMassMsgTopic` by calling `MassMsgService.sendGroupChatMassMsgToWeWork(id, extCorpId)`.
- Handles `StaffDeleteCustomerTopic` by calling `DeleteCustomerNotificationService.sendTimedAdminNotification(payload)`.
- Handles `RemainderTopic` by calling `RemainderService.sendRemainder(payload)`.

`CustomerCallbackRegistrar` publishes `SyncCustomerDataTopic` when customers are added/edited.

## Contact Way Notes

Relevant Go source:

- `app/controller/contact_way.go`
- `app/controller/contact_way_group.go`
- `app/models/contact_way.go`
- `app/models/contact_way_staff.go`
- `app/models/contact_way_shedule_staff.go`
- `app/services/contact_way.go`
- `app/consumers/contact_way.go`
- `app/tasks/contact_way.go`
- `pkg/easywework/contact_way_api.go`
- `pkg/easywework/contact_way_model.go`

Current Java implementation:

- `contactway/controller/ContactWayController.java`
- `contactway/controller/ContactWayGroupController.java`
- `contactway/service/ContactWayService.java`
- `contactway/service/ContactWayGroupService.java`
- `contactway/service/ContactWayScheduledTasks.java`
- `contactway/dto/*`

Implemented:

- Contact way group query/get/create/update/delete.
- Contact way query/get/create/update/delete/batch-update.
- Local association persistence and update sync for normal staff, backup staff, schedules, schedule staff.
- Current effective staff calculation based on:
  - schedule enable
  - staff control online flag
  - daily add customer limit
  - backup staff fallback
  - auto skip verify time window
- WeWork add/update/delete/get contact way.
- Delayed refresh jobs for skip-verify time windows and schedule boundaries.
- Daily reset of `daily_add_customer_count` for normal, backup, and schedule staff.
- Add-customer side effects from `ContactWay.DealAddCustomerEvent`:
  - auto tag via WeWork mark tag API
  - customer remark/description update
  - nickname block welcome-message logic
  - channel welcome/default welcome/disable welcome behavior
  - increment normal/schedule/backup staff daily/total counts
  - trigger refresh when staff reaches daily limit

## Callback Notes

Callback entry:

- `WeWorkCallbackController`
- `WeWorkCallbackService`
- `WeWorkCallbackCrypto`

Registrars currently overwrite default log-only handlers:

- `DepartmentCallbackRegistrar`
- `StaffCallbackRegistrar`
- `CustomerCallbackRegistrar`
- `TagCallbackRegistrar`
- `GroupChatCallbackRegistrar`
- `MsgAuditCallbackRegistrar`

Staff callback implementation:

- `change_contact/create_user`: syncs one WeWork staff into local staff and staff-department tables.
- `change_contact/update_user`: syncs one staff and removes stale staff-department rows no longer returned by WeWork.
- `change_contact/delete_user`: soft-marks staff as deleted/disabled and removes staff-department rows.

Group chat callback implementation:

- `change_external_chat/create`: fetches group detail and syncs group/member list.
- `change_external_chat/update`: fetches group detail and refreshes group/member list; increments daily join/quit count when applicable.
- `change_external_chat/dismiss`: marks group as dismissed.

Customer relation callback implementation currently:

- add/edit/add-half publishes `SyncCustomerDataTopic`.
- delete/del-follow creates events/history, soft-deletes relation, and sends delete-customer notifications when `event_notify` enables them. Staff-delete-customer supports real-time delivery and delayed delivery at the next 8:00 run.
- The matching staff-delete-customer admin APIs now live in `deletenotify`: rule get/update, record query, and XLSX export.
- transfer-fail records a customer event with the WeWork fail reason and does not change customer-staff relations.
- `SyncCustomerDataTopic` consumer fetches WeWork external contact detail, upserts customer, customer_staff, customer_staff_tag, and customer_info, then runs contact-way add-customer side effects and default welcome-message fallback.

Message-archive callback implementation:

- `change_external_contact/msg_audit_approved`: refreshes staff `enable_msg_arch` status through the existing permit-user-list client. It does not write Go `chat_session` consent fields because the current Java schema only has local `chat_msg`/`chat_msg_content` tables.

Known callback gaps:

- No remaining callback events are known from the Go dispatcher comparison. Further callback work should be driven by new WeWork requirements or callbacks observed in logs.

## WeWork Client Notes

Java interface: `WeWorkClient`

Current implemented methods include:

- `getAccessToken`
- `listDepartments`
- `listUserIds`
- `getUser`
- `getUserInfo`
- `listExternalContactCorpTags`
- `getGroupChat`
- `getExternalContact`
- `remarkExternalContact`
- `markExternalContactTag`
- `sendWelcomeMessage`
- `uploadPermanentImage`
- `getJsApiTicket`
- `getJsApiAgentTicket`
- `uploadTemporaryMedia`
- `addMsgTemplate`
- `addContactWay`
- `getContactWay`
- `updateContactWay`
- `deleteContactWay`
- `listGroupChats`
- `updateUserEnable`

Next likely WeWork methods needed:

- external customer batch detail

## Known Route Gaps From Latest Router Audit

- Material/temp-material and root storage route aliases have been handled.
- Fresh audit has 6 remaining normalized route gaps, all in staff/customer/tag business logic:
  - `GET /api/v1/staff-admin/staff/action/get-all`
  - `GET /api/v1/staff-admin/statistics/:ext-staff-id`
  - `POST /api/v1/staff-admin/customer/action/update-tags`
  - `POST /api/v1/staff-admin/customer/action/update-internal-tags`
  - `POST /api/v1/staff-admin/customer/action/sync`
  - `POST /api/v1/staff-admin/customer/tag`

## High Priority Next Work

Follow this order unless the user redirects:

1. Port the remaining staff/customer/tag route gaps listed above.
2. Start with the lower-blast-radius staff query/statistics endpoints, then customer tag mutation, then full customer sync.

## Known Gaps And Caveats

- Mass-message customer send-result polling is ported for `mass_msg_staff`; group-chat mass-message send-result polling now persists per-chat rows in `group_chat_mass_msg_result`.
- Conversation-archive Java query/search APIs currently read local `chat_msg` tables. The `sync` endpoint can proxy to an external msg-archive service when `openscrm.msg-arch.server-url` and `openscrm.msg-arch.app-code` are configured; Java still does not embed the WeWork msg-audit C SDK pull/decrypt/media-download pipeline.
- Conversation-archive `sync` falls back to local latest-seq reporting when no external msg-archive service is configured.
- Aliyun OSS signed URL adapter is implemented and wired to environment variables; it still awaits real environment credentials.
- Root local-storage compatibility uses the Java local signed URL signature scheme. The confirmed deployment target is OSS, so legacy Go local `App.Key` SHA1 signatures are not required unless local-storage cutover support is later requested explicitly.
- Integration tests with real Redis/RabbitMQ/MySQL/WeWork are not present; current verification is Maven test/compile plus focused unit coverage for msg-archive sync proxy, file-storage service paths, and ContactWay response serialization.

## Commands And Search Patterns

Useful commands:

```bash
rg -n "SyncCustomerDataTopic|DealAddCustomerEvent|ExternalContact|WelcomeCode" app pkg java-server/src/main/java
rg -n "ContactWay|contact_way|RefreshContactWayTopic" app pkg java-server/src/main/java
rg --files java-server/src/main/java | sort
/Users/jlgl/Documents/develop/apache-maven-3.6.3/bin/mvn -q test
/Users/jlgl/Documents/develop/apache-maven-3.6.3/bin/mvn -s /private/tmp/openscrm-maven-central-settings.xml -q test
```

Prefer `rg`/`rg --files` over slower search tools.

## Editing Rules For Continuation

- Use `apply_patch` for manual edits.
- Do not revert user/unrelated changes.
- Keep edits scoped to the requested migration batch.
- Run Maven tests after each batch.
- Update `MIGRATION.md` when completing a meaningful migration batch.
- Keep this file updated only when it helps future handoff context.
