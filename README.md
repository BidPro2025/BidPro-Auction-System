# BidPro Auction System

React + Spring Boot auction application with separate seller, bidder and administrator workspaces. No containers are required.

## Requirements

- Node.js 20.19+ (or a newer supported release) and npm
- JDK 17+ and Maven 3.9+
- MySQL only when using the optional `mysql` profile

## Run locally on Windows

Open PowerShell in `BidPro-Auction-System`, then run:

```powershell
npm.cmd install
npm.cmd run dev
```

Open **http://127.0.0.1:5173**. The backend uses port **8080**. Wait for the Spring Boot startup message before signing in. Stop both processes with **Ctrl+C**.

`npm.cmd` avoids the PowerShell script execution restriction that can block `npm.ps1`. On macOS/Linux use `npm` instead. If Maven is not on PATH, use the checked-in wrapper by setting `$env:BIDPRO_USE_MAVEN_WRAPPER = "1"` before running the commands. The wrapper needs a network connection on its first run.

Local development uses a persistent H2 database and creates these demo accounts:

| Role          | Email               | Password   |
| ------------- | ------------------- | ---------- |
| Administrator | admin@bidpro.local  | BidPro123! |
| Seller        | seller@bidpro.local | BidPro123! |
| Bidder        | bidder@bidpro.local | BidPro123! |

Demo accounts and sample auctions are initialized only in the `local` profile. Public signup creates a pending seller or bidder account; sign in as the administrator to approve it before listing or bidding.

## Working auction flow

1. Sign in as a seller and submit a listing with its category, prices and auction dates. Upload optional images before approval.
2. Sign in as an administrator and approve the listing. Approval creates its auction.
3. Sign in as a bidder and place bids while the auction is active. The API validates the minimum price, increment, account status and ownership.
4. After the end time, the winner, seller or administrator can settle the auction to create an invoice. Administrators can close an active auction early.
5. Retrieve the invoice from the seller or bidder workspace and download its PDF.
6. As the buyer, use **Open local test checkout** to simulate a failed or successful payment. The invoice and PDF clearly identify the result as a local test with no money transferred.
7. Use **Messages** for private support conversations and **Notifications** for account and auction updates.
8. Use **Forgot password?** to request a reset. In local mode, an administrator can preview its email under **Email outbox** and open the single-use reset link in a signed-out browser.

## Commands

```powershell
npm.cmd run dev:web       # Frontend only
npm.cmd run dev:api       # Backend only
npm.cmd test              # Lint, web tests, production build, backend tests
npm.cmd run test:smoke    # End-to-end API checks against the running local app
npm.cmd run test:browser  # Core browser checks against the running local app
npm.cmd run test:communications # Messaging, notifications and recovery browser checks
npm.cmd run test:checkout # Local payment checkout browser checks
npm.cmd run test:tools    # Offline backup safety/integrity checks
npm.cmd run backup:local  # Offline H2 + uploads backup; stop API first
npm.cmd run test:mysql   # Full backend suite against isolated real MySQL (Windows)
npm.cmd run build        # Frontend production output: apps/web/dist
npm.cmd run build:api    # Backend JAR: apps/api/target
```

Browser verification requires Chromium once:

```powershell
$env:PLAYWRIGHT_BROWSERS_PATH = "$PWD\.cache\ms-playwright"
npx.cmd playwright install chromium
npm.cmd run test:browser
```

The smoke check creates uniquely named test records in the local database and includes a short auction that closes during the check. Browser tests also create uniquely named local records. Run these checks against a development instance.

## Configuration

The frontend defaults to `/api` and uses the Vite development proxy. Copy `apps/web/.env.example` to `apps/web/.env` only if custom settings are needed. `API_PROXY_TARGET` changes the proxy destination; `VITE_API_BASE_URL` changes the browser API URL.

The API defaults to `local`. For MySQL, configure `SPRING_PROFILES_ACTIVE=mysql`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` and `JWT_SECRET` using your environment. See `apps/api/src/main/resources/application-mysql.properties` for exact variables. Use a strong private JWT secret, at least 32 bytes. MySQL does not seed demonstration accounts. Data and uploads are local runtime files and are excluded from Git.

For the first MySQL administrator, set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` before starting the API. The bootstrap creates a missing administrator and preserves existing accounts. Remove these variables after the first successful startup.

## Code organization

- `apps/web/src`: active React application, grouped into app, features and shared modules
- `apps/api`: Spring Boot controllers, transactional services, DTOs, persistence and tests
- `scripts`: development launcher and full-stack checks
- `docs`: [architecture](docs/architecture.md) and [API guide](docs/api.md)
- `apps/web/legacy`: preserved original prototype for reference

Local development includes a clearly labeled test checkout and administrator email preview, so the complete flow can be exercised without provider credentials. Local test payments transfer no money and are disabled outside the local profile.

The Razorpay integration supports real hosted checkout, signed callbacks and webhooks. SMTP delivery includes queued messages and retries. To enable your accounts later, configure the root `.env` using [payment setup](docs/payments.md) and [email and recovery setup](docs/email-and-recovery.md). The launcher loads this file. Real provider credentials and real transactions are not used by automated tests.

For workspace-only MySQL setup and database verification, see [MySQL instructions](docs/mysql.md).

See [verification results](docs/verification.md) for the tested workflows and remaining integration limits.

## Engineering review and operations

The [enterprise gap register](docs/enterprise-gap-audit.md) records the prioritized
findings, fixes, and release work still required. The [operations runbook](docs/operations.md)
covers production configuration, health checks, backups, incidents and rollback.
The hardened `production` profile includes MySQL, validates the existing schema,
requires verified TLS settings and rejects local/demo configuration. The `mysql`
profile alone remains a development/integration option that updates its schema.

Catalog browsing now uses database pagination, filtering and sorting. Expired
auctions with winning bids settle automatically in bounded batches; manual
settlement remains idempotent. Successful account, bidding, settlement and payment
changes write transactional audit records. Sign-out revokes the presented token;
the profile also offers sign-out from all devices. Existing sessions require one
new sign-in after the JWT hardening update.

GitHub Actions and Dependabot configurations are included; hosted CI runs and
branch protection take effect after the repository is pushed and configured.
Development builds use `apps/api/target-dev`, separate from test/package output
under `apps/api/target`, so running backend tests cannot rewrite the dev classes.
