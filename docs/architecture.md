# Architecture

BidPro is a React application and Spring Boot API maintained in one repository.

```text
apps/
  web/
    src/
      app/                 Router, application shell and layout
      features/            Authentication, catalog and role workspaces
      shared/              API client, hooks, UI and formatting
    tests/                 Frontend logic regression tests
    legacy/                Original prototype retained for reference
  api/
    src/main/java/com/bidpro/
      config/              Security, CORS, local data initialization
      controller/          HTTP contracts and validated inputs
      service/             Business rules and transaction boundaries
      dao/                 JPA persistence queries
      entities/            Database mappings
      dto/                 API response and request models
      payment/             Checkout, payment ledger and provider verification
      custom_exceptions/   Domain exceptions
      util/                Shared backend utilities
    src/main/resources/    Local and MySQL profile configuration
    src/test/              Backend regression tests
scripts/                   Local development and end-to-end checks
docs/                      Architecture and API guide
```

The browser uses one API client and relative `/api` URLs. Vite forwards API and
image requests to Spring Boot during development and preview. A production web
server must also forward `/api` to the backend and serve `index.html` for browser
routes, or set `VITE_API_BASE_URL` to an explicitly configured backend origin.

Authentication returns a signed JWT and a user record. Browser route guards
improve navigation; Spring Security and service checks enforce authorization.
The server resolves user IDs from the authenticated account rather than trusting
IDs supplied in a request. Public registration cannot create administrators.

A seller submits a pending listing. An administrator approves it, which creates
the associated standard auction. Only approved listings accept bids during their
start/end window. Each bid must meet the starting price or the highest bid plus
the configured increment. Product row locks serialize bid and settlement checks.

Once an auction closes, settlement uses its highest bid and produces one invoice
for the winner. The seller, winner and administrator can retrieve the invoice;
administrator-entered payment records are explicitly marked manual. Buyer checkout
uses a server-created order and a payment ledger. Razorpay payments are recorded
only after signature validation and provider confirmation of capture, amount,
currency and order. Webhooks are signed and idempotent. The local profile also
supports an explicitly labeled test checkout that cannot transfer real money.

The `local` profile uses a persistent H2 database and demonstration accounts. The
`mysql` profile requires environment configuration and does not initialize demo
accounts. Uploaded images live on the local filesystem; database rows retain
their generated URLs. Back up the database and upload directory together.

The original frontend prototype remains under `apps/web/legacy`; the running
application imports only `apps/web/src`. Legacy files are excluded from build and
lint because they describe the previous, incompatible API contracts.

Email uses a transactional database outbox. Local mode captures messages for an
administrator preview; SMTP mode delivers with timeouts and bounded retries.
Password recovery stores token hashes, enforces expiry and single use, and revokes
previous sessions through a per-account password version. Notifications belong to
their recipient. Support conversations are private to the account owner and
administrators and use separate tables from the incompatible legacy prototype.
