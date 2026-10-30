# Verification

Verified on 26-27 October 2026 using Node.js 25, Java 21, Maven 3.9,
Chromium, H2 and a real workspace MySQL 8.4.13 server.

| Check                                        | Result                                |
| -------------------------------------------- | ------------------------------------- |
| Frontend lint                                | Passed with zero errors or warnings   |
| Frontend regression tests                    | 6 passed                              |
| Production frontend build                    | Passed                                |
| Backend integration tests on H2              | 30 passed, zero failures/errors/skips |
| Backend integration tests on MySQL           | 30 passed, zero failures/errors/skips |
| API smoke test                               | 58 checks passed                      |
| Core Chromium workflow                       | 10 stages passed                      |
| Communication and recovery Chromium workflow | 7 stages passed                       |
| Local checkout Chromium workflow             | 4 stages passed                       |
| Formatting                                   | Passed                                |
| Combined development launcher                | Frontend HTTP 200; API/database UP    |

`npm.cmd test` runs lint, frontend tests, the frontend production build and backend
tests from the repository root. `npm.cmd run test:mysql` runs the backend suite
against an isolated schema on a real MySQL server. It caught and verified the fix
for a MySQL reserved column name in the email outbox.

The core browser check covers public navigation, signup, account restrictions,
administrator approval, categories, seller listing creation/editing/image upload,
listing approval, increasing bids, session restore, auction closing, winning-price
settlement, manual payment records, PDF downloads, buyer/seller invoice visibility,
network-error retry and mobile layout.

The communications browser check verifies private conversation access, forbidden
outsider reads/replies, administrator replies, close/reopen controls, persistent
notification read state, admin-only local email preview, password reset through a
signed-out browser, old-session revocation, old-password rejection, reset-token
replay rejection and authenticated mobile layout.

The checkout browser check verifies a real auction-to-invoice fixture, the clearly
labeled local payment dialog, failure/retry/success, one reused order, idempotent
settlement, protection against stale failure downgrades, buyer-only payment access
and a valid invoice PDF. Local test payments transfer no money.

The backend tests include concurrent bidding and settlement, authorization,
validation, image handling, recovery expiry/single use/password limits, actual SMTP
protocol delivery through a loopback listener, notification ownership, private
messaging, Razorpay-compatible HTTP contracts, signatures, amount/currency/order
matching, webhook retries, captured-payment checks and production guards that
prevent local checkout outside the local profile.

Browser checks report no browser exceptions or backend 5xx errors. Smoke and
browser checks create uniquely named local fixtures and preserve their records.
Screenshots and PDFs are written to ignored `test-results/`.

The user selected local test integrations for this handoff. Email is captured in
an administrator-only local outbox; SMTP delivery is implemented and tested using
a real local SMTP listener. Razorpay is implemented and tested against a local
HTTP contract server. Real SMTP account delivery and live Razorpay charges were
not attempted. Configuring your provider credentials enables those account-backed
integrations later; see [email and recovery](email-and-recovery.md) and
[payment setup](payments.md).

## Glass interface verification

The 27 October 2026 design update passed 17 desktop/mobile visual checks,
including actual glass blur, reduced-motion and reduced-transparency fallbacks,
and checks for frontend exceptions, API failures and horizontal overflow.
Additional home-layout checks passed at 320, 390, 640, 768, 850, 1024, 1200 and
1280 pixels. All three browser workflows passed again after the design changes:
core auctions (10 stages), local checkout (4), and communications/recovery (7).
Frontend lint, six logic tests and the production build also passed.

See [interface design](ui.md) for materials and visual-review instructions.

## Collector edition redesign

The follow-up design replaced the light icon-led homepage with a dark editorial
gallery, a generated decorative still life, real collection filters and a featured
auction dock. Authenticated dashboards now use role sidebars, actual status
breakdowns and live-auction panels. Verification passed all three browser flows
(10 core, 4 checkout, 7 communications/recovery stages), 17 desktop/mobile visual
views, six frontend tests, lint, formatting and the production build. Public home
and administrator overview also fit at 320, 390, 640, 768, 1024 and 1280 pixels.

## Pearl collection theme

The dark palette was replaced with pearl and ivory surfaces, luminous white glass,
sage details and emerald actions. The editorial photograph now sits in a framed
panel beside the bright hero copy and stacks beneath it on mobile. Workspaces,
native form controls, messages, status badges, tables and checkout dialogs all use
the light palette, including reduced-transparency and increased-contrast modes.
The mobile recent-registration table scrolls within its panel to preserve readable
member details. Visual capture waits for decoded editorial imagery to paint before
taking screenshots.

Verification passed 17 desktop/mobile visual views, the six-width home and admin
overflow checks (320, 390, 640, 768, 1024 and 1280 pixels), all three browser flows
(10 core auction, 4 local checkout and 7 communications/recovery stages), six
frontend tests, lint, formatting and the production build. Backend logic and local
integration settings were unchanged by this theme update.

## Interface refinement

The follow-up polish enlarged navigation, form labels and product/workspace
details, shortened the editorial introduction, added icon-led collection cards,
clarified auction card actions and bid counts, and introduced a structured footer.
Catalog sorting and filter reset are functional and preserve the selected sort in
the URL. Mobile inputs use 16px text and readable touch targets.

All three browser workflows passed again (10 core auction, 4 local checkout and
7 communications/recovery stages), along with 17 visual views, six frontend
tests, lint, formatting and the production build. Home, catalog and administrator
overview fit at 320, 390, 640, 768, 1024 and 1280 pixels. The visual script also
verifies price ordering, empty search results, filter reset preserving sort, and
mobile sign-in font/control sizes. Role names stay on one line in the internally
scrollable registration table.
