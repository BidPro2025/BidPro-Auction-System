# Razorpay payments

The default `local` profile provides an explicitly labeled local test checkout.
Open an unpaid buyer invoice, choose **Open local test checkout**, and select a
successful or failed test outcome. The flow records a `LOCAL_TEST` payment source
and `local-test` mode; the screen and PDF say that no money was transferred.
Failures can retry the same order, successful retries are idempotent, and later
failure requests cannot downgrade a completed test payment. No card or bank
details are requested.

The sandbox requires both the active `local` profile and `PAYMENTS_LOCAL_TEST=true`
(the local default). Other profiles cannot enable it using this flag alone. Set
`PAYMENTS_LOCAL_TEST=false` to disable it. Setting `PAYMENTS_ENABLED=true` selects
Razorpay and prevents fallback to simulated checkout even if the keys are
incomplete.

The invoice screen supports Razorpay Standard Checkout in INR. The winning buyer
can open checkout for an unpaid auction invoice. BidPro creates the provider order
on the server using the invoice amount, reuses that order on retries, verifies the
checkout signature, then fetches the payment from Razorpay before recording it.
Only a captured payment with the matching order, amount and currency marks the
invoice paid. An authorization remains pending until capture.

## Configuration

Put these values in the repository's local `.env` file or environment settings.
Never commit real account secrets.

```dotenv
PAYMENTS_ENABLED=true
RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_test_key_secret
RAZORPAY_WEBHOOK_SECRET=your_own_webhook_secret
RAZORPAY_ALLOW_LIVE=false
```

The frontend receives only the public key ID and provider order details. It loads
the official hosted checkout script when the buyer chooses to pay. BidPro does
not store card numbers, CVVs, bank credentials or other payment instruments.

Configure automatic capture in the Razorpay dashboard. Register a webhook at
`https://your-api-host/api/payments/webhooks/razorpay`, using the same webhook
secret and the `payment.captured`, `payment.authorized` and `payment.failed`
events. The endpoint validates the HMAC signature against the raw request body
and records the provider event ID to make retries idempotent. Older failure events
cannot downgrade a verified captured payment.

Test mode is displayed in the invoice screen and PDF and transfers no real money.
For live operation, use an activated Razorpay account, live keys and
`RAZORPAY_ALLOW_LIVE=true` after completing provider-side testing and configuring
the live webhook. No real payment is attempted by the automated tests.

When Razorpay is selected without complete account keys, the invoice screen shows
that provider checkout is unavailable. Local development checkout remains usable
without keys when its separate local test mode is selected. Administrators can still record an external payment manually if no
online payment order exists. Manual records are explicitly labeled and cannot
overwrite provider-verified payments. Once checkout creates an online order, its
status is controlled by provider verification to avoid manual and online payment
races.

## Endpoints

| Endpoint                               | Access and purpose                                                                |
| -------------------------------------- | --------------------------------------------------------------------------------- |
| `GET /api/payments/config`             | Authenticated; availability and test/live mode, no secrets                        |
| `POST /api/payments/orders`            | Approved invoice buyer; body `{"invoiceID":"..."}`                                |
| `POST /api/payments/verify`            | Approved invoice buyer; invoice ID and Razorpay checkout response fields          |
| `POST /api/payments/webhooks/razorpay` | Provider; requires valid `X-Razorpay-Signature` and `X-Razorpay-Event-Id` headers |

`PaymentIntegrationTest` uses a real local HTTP server with provider-compatible
responses to test the adapter, ownership, signatures, server-side amounts,
authorization versus capture, failed-payment retry, duplicate webhooks, concurrent
order creation and manual-status protection. It uses fake keys and never contacts
Razorpay.

The implementation follows the official [Standard Checkout integration](https://razorpay.com/docs/payments/payment-gateway/web-integration/standard/integration-steps/),
[Orders API](https://razorpay.com/docs/api/orders/create/),
[payment fetch API](https://razorpay.com/docs/api/payments/fetch-with-id/), and
[webhook validation guide](https://razorpay.com/docs/webhooks/validate-test/).

The local-only endpoint `POST /api/payments/local-test` accepts
`{"invoiceID":"...","outcome":"SUCCESS"}` or outcome `FAILURE`, requires the
approved winning buyer, and reuses the locked payment ledger. Local simulation
cannot be used on Razorpay orders or outside the local development profile.
`LocalPaymentIntegrationTest`, `ProductionPaymentGuardTest` and
`PaymentConfigurationTest` cover these boundaries.
