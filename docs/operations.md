# BidPro operations runbook

This runbook describes controls implemented in this repository and the operator
work still required before a public release. It is not a production-readiness or
payment-compliance certification. No containers are required.

## Environments and startup

Run commands from the repository root. Use `npm.cmd` on Windows PowerShell and
`npm` on other platforms. The launcher loads the ignored root `.env`; keep secrets
out of source control, command-line arguments and support messages.

| Profile           | Database and purpose                                               | Schema behavior                                              |
| ----------------- | ------------------------------------------------------------------ | ------------------------------------------------------------ |
| `local` (default) | Persistent H2, demo users, local checkout and email preview        | Hibernate `update`, development only                         |
| `mysql`           | Optional MySQL development/integration environment                 | Hibernate `update`; this is not the production profile       |
| `production`      | Includes the `mysql` profile; dedicated deployment configuration   | Hibernate `validate`; separately provisioned schema required |
| `test`            | Isolated test configuration; no real provider credentials required | Test schema creation and cleanup                             |

Start local development with `npm.cmd run dev`. The frontend listens on
`127.0.0.1:5173` and the API on port 8080 unless configured otherwise. Wait for
readiness before signing in. Stop both processes before an offline H2 backup.

The Maven launcher uses `apps/api/target-dev` for `spring-boot:run`. Tests and
packaging use `apps/api/target`. This keeps test compilation from replacing classes
loaded by the running development API. Avoid simultaneous Maven commands writing
the same output directory. Maven dependencies are cached under ignored `.cache/`.

New JWTs require issuer `bidpro`, audience `bidpro-web` and a token identifier.
Sessions created by older application versions must sign in again once after this
upgrade. Signing-key rotation also invalidates existing sessions. Keep server
clocks synchronized; auction dates currently use local date/time values, so agree
on the deployment timezone before loading or migrating auction schedules.

## Production preconditions

The production profile fails startup if safety settings are missing or conflict.
Do not bypass this validation to make a deployment start.

1. Provision a reviewed schema for the exact release in a staging database, then
   validate it against the application. There is no Flyway/Liquibase migration
   history yet. New security tables, monetary precision and indexes must be
   included in the reviewed provisioning process. Do not use automatic Hibernate
   `update` in production or create a production schema by pointing a local/demo
   instance at production data.
2. Set `SPRING_PROFILES_ACTIVE=production`; do not combine it with `local` or `test`.
   Set a private `JWT_SECRET` of at least 32 bytes through a secret manager or the
   deployment environment. Demo seeding and local checkout must remain disabled.
3. Set an HTTPS `APP_PUBLIC_URL` without credentials, query parameters or fragments.
   Set `CORS_ORIGINS` to the explicit HTTPS frontend origins, without paths or
   wildcards. Terminate HTTPS through a configured application server or trusted
   reverse proxy. The repository does not provision DNS, certificates or hosting.
4. Set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` for MySQL. The URL must explicitly
   include `sslMode=VERIFY_IDENTITY`; configure a trust chain and hostname that
   actually validate. Remove development options such as disabled SSL. Use a
   database principal with only the privileges needed by the application.
5. Configure real SMTP host, sender and credentials. Production requires implicit
   TLS or required STARTTLS, with server identity verification. Do not disable
   certificate verification. Follow [email setup](email-and-recovery.md) and test
   delivery using the intended business account in staging.
6. Set an absolute `UPLOAD_DIR` on durable storage with restricted access. Maven
   development uses `apps/api` as its working directory; service/JAR launch paths
   may differ. Relative upload paths must not silently create another empty store.
7. Configure Razorpay using [payment setup](payments.md). Test credentials and live
   credentials are separate. Enabling live charges requires the provider account,
   webhook registration, explicit live-mode setting and verified business policy.
   Current local integrations transfer no money.

If initial administrator bootstrap is needed, configure its environment variables
for the first controlled startup and remove them afterwards. Never reuse demo
passwords. Restrict access to deployment configuration, database backups, email
outbox data and uploads.

## Health, tracing and capacity

| Check                   | Expected behavior                                     | Operational use                              |
| ----------------------- | ----------------------------------------------------- | -------------------------------------------- |
| `GET /api/health/live`  | 200 while the API can serve the endpoint              | Process liveness; does not test dependencies |
| `GET /api/health/ready` | 200 after a database `SELECT 1`; 503 when unavailable | Readiness before routing traffic             |
| `GET /api/health`       | Alias of readiness                                    | Compatibility with local/browser checks      |

Health responses use `Cache-Control: no-store`. Readiness does not verify SMTP,
Razorpay, upload storage, background-job progress or a completed restore. Add
external alerts and probes for those dependencies before release.

API responses expose `X-Request-ID`, and response envelopes include `requestId`.
Safe caller IDs are accepted; invalid values are replaced. Logs record method,
path, status and duration with the request ID. Preserve the ID, timestamp and
operation when investigating failures; do not collect passwords, bearer tokens,
reset links, provider secrets or full request bodies.

Successful bids, bid increases, early auction closes, settlement and payment state
transitions write transactional audit records. They can be inspected through the
administrator-only `/api/logs/entity/{entityType}/{entityID}` and date-range API.
System settlement uses actor `SYSTEM_SETTLEMENT`; provider callbacks use
`RAZORPAY_WEBHOOK`. Database audit rows are not an external tamper-resistant archive.
Define access, retention and export ownership separately.

Current limits include a 5 MB upload, 6 MB multipart request, a 10-second connection
timeout, `HTTP_THREADS_MAX` default 100 and `HTTP_ACCEPT_QUEUE` default 100.
Production `DB_POOL_MAX` defaults to 10; connection acquisition times out after
10 seconds. Graceful shutdown allows 30 seconds. Size these together with database
capacity and measured traffic, rather than increasing thread limits alone.

Authentication/recovery/bid rate limits are bounded but process-local. They are
not a shared limit across replicas. Forwarded headers are untrusted by default;
configure a trusted proxy boundary before relying on client IP forwarding. Add
shared abuse controls and load testing before scaling the deployment.

## Background jobs and payment safety

Automatic settlement polls every 30 seconds after an initial 10-second delay by
default. It selects up to 50 ended, approved auctions with bids and no invoice.
Each product settles in its own transaction under the same lock used for bidding;
one failure does not stop the other candidates. `app.auctions.settlement-enabled`,
`app.auctions.settlement-poll-ms` and
`app.auctions.settlement-initial-delay-ms` control this worker.

Manual settlement remains an idempotent fallback for an administrator, seller or
winning buyer through `POST /api/invoices?productID=...`. It returns the existing
invoice on an authorized repeat request. Upcoming/open auctions and auctions
without bids cannot produce a winning invoice. Settlement creates a pending
invoice; it does not charge a card or initiate a seller payout.

Notification processing also isolates per-auction failures. SMTP delivery selects
up to 25 queued messages per poll, retries with a delay, and stops automatic
attempts after five failures. An administrator can inspect delivery status and
request a retry after fixing the underlying cause. Delivery and job failure logs
need external monitoring; there is no deployed alerting or dead-letter service.

An accepted bid increase appends a new record. It must not rewrite the original
accepted amount or timestamp. A paid invoice cannot downgrade to failed/pending.
Invoices with online payment orders cannot use the manual payment-state override.
Refunds, disputes and chargebacks require a separately reviewed workflow; changing
an invoice field is not a refund.

## Local backup and restore boundaries

The backup utility supports the offline local H2 database and files under
`apps/api/data`, including default uploads. It is not a MySQL or hosted storage
backup system.

1. Stop the API and other processes using the H2 files. The tool checks the local
   API port and H2 lock files; operators must also stop nonstandard instances or
   processes using another port.
2. Keep the local profile active. Run `npm.cmd run backup:local` to create a new
   ignored `.cache/backups/<timestamp>-<id>/` directory. A custom new destination
   can be supplied with `-- --output <new-directory>`; existing backups are not
   overwritten.
3. Verify a backup explicitly with:

   ```powershell
   npm.cmd run backup:local -- --verify ".cache/backups/<timestamp>-<id>"
   ```

4. Keep the manifest and `data/` together. Verification checks the full file
   inventory, byte lengths and SHA-256 checksums. It detects modification; it does
   not prove that every application workflow can restore successfully.

Backups may contain personal information, password hashes and local reset emails.
They are sensitive and untracked; `.cache/` is not offsite protection. Protect
Windows ACLs, encrypt retained copies and assign a retention owner. If `UPLOAD_DIR`
is outside `apps/api/data`, back up that store separately and keep its version
consistent with the database snapshot.

There is no automatic restore command. Do not overwrite the live database, delete
records or restore over an active instance. A recovery owner should verify a copy
in a separate directory/environment, test login/listings/invoices/uploads, approve
the recovery point and then plan a controlled cutover. Production MySQL backups,
point-in-time recovery, encrypted offsite copies, recovery objectives and a tested
restore drill remain operator work.

## Release and rollback

The new [GitHub Actions workflow](../.github/workflows/verify.yml) configures
frontend/tool tests, a production frontend build, npm vulnerability checks, Java
verification and local browser workflows. [Dependabot](../.github/dependabot.yml)
configures weekly dependency proposals. Configuration in the repository does not
mean these jobs have run successfully on hosted GitHub runners; check actual CI
results and branch protection before merging a release.

Before go-live, require passing checks for the release commit, a reviewed schema
change and rollback plan, verified backups and a restore drill, valid TLS, approved
provider account configuration, role-access tests and a monitored staging checkout.
Resolve known launch blockers in the engineering audit. Do not infer payment,
privacy or security certification from passing application tests.

Keep the previous application artifact and configuration reference, but treat
rollback as schema-sensitive. Older application versions may not understand new
security tables, fields, audit behavior or payment state rules. Prefer a validated
forward fix when rollback compatibility is unknown. Do not automatically reverse
schema changes or restore a database merely to undo an application release.

## Incident procedure

1. Assign an incident owner, note the start time and affected operations, and
   preserve request IDs and relevant audit references. Communicate customer impact
   through the organization's approved channel.
2. Check liveness and readiness. For database/readiness failure, stop routing new
   work to that instance and investigate connectivity, verified TLS, pool capacity
   and database health. Repeated restarts do not repair a database outage.
3. For email failures, inspect queue age, failed attempts and SMTP/TLS availability.
   Correct configuration, then retry selected failed messages. Avoid uncontrolled
   repeated password-reset requests or copying reset content into incident logs.
4. For a pending payment, compare the stored invoice/order/reference with the
   provider dashboard and verified callback/webhook records. Retry signed webhook
   delivery through the provider's supported tooling after restoring availability.
   Do not create unrelated replacement orders, trust a browser success message or
   manually mark an online-order invoice paid.
5. Provider success followed by local commit failure can leave an unknown outcome.
   Preserve the invoice receipt/order details and involve the payment/reconciliation
   owner. A complete distributed reconciliation workflow is still required for
   public operation; avoid speculative charges or destructive record corrections.
6. For stuck settlement, inspect the product/invoice audit and worker error,
   correct the cause and use the authorized idempotent settlement fallback when
   appropriate. For suspected compromise, restrict access and involve the security
   owner before rotating secrets; JWT rotation signs everyone out.
7. Record recovery evidence, verify affected workflows, reconcile financial
   outcomes and complete a follow-up review with owners and dates.
