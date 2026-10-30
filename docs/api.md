# API guide

Base URL: `http://localhost:8080/api`. JSON success responses contain
`success`, `message` and `data`. Failures use a non-2xx status and a readable
message. Send `Authorization: Bearer <token>` for authenticated requests.

| Area                | Endpoints                                                                                                            |
| ------------------- | -------------------------------------------------------------------------------------------------------------------- |
| Health              | `GET /health`                                                                                                        |
| Authentication      | `POST /auth/signup`, `POST /auth/signin`, `GET /auth/validate`                                                       |
| Catalog             | `GET /products`, `GET /products/{id}`, `GET /categories`                                                             |
| Seller listings     | `GET /products/mine`, `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}`                                |
| Product images      | `POST /file-upload/product-image`, `GET /images/{name}`                                                              |
| Auctions            | `GET /auctions`, `GET /auctions/{id}`, `POST /auctions/{id}/register`                                                |
| Bidding             | `POST /bids`, `GET /bids/mine`, `GET /bids/product/{id}`, `PUT /bids/{id}`                                           |
| Invoices            | `POST /invoices?productID={id}`, `GET /invoices/mine`, `GET /invoices/{id}/download`                                 |
| Admin overview      | `GET /admins/dashboard`                                                                                              |
| User moderation     | `GET /admins/users`, `PUT /admins/users/{id}/status`                                                                 |
| Product moderation  | `GET /admins/products`, `PUT /admins/products/{id}/status`                                                           |
| Category management | `GET /admins/categories`, `POST /admins/categories`, `PUT /admins/categories/{id}`, `DELETE /admins/categories/{id}` |
| Auction management  | `GET /admins/auctions`, `POST /auctions/{id}/close`                                                                  |

Catalog queries accept `keyword` and `categoryID`. Auction dates use local ISO
date-time strings such as `2026-10-24T15:30:00`; browser and API should use the same
local timezone. Money uses decimal values with at most two fractional digits.

Example listing input (replace both dates with your intended schedule and keep the end time in the future):

```json
{
  "title": "Film camera",
  "description": "35mm camera with lens and carrying case.",
  "categoryID": "category-id-from-api",
  "auctionStartDate": "2026-10-24T15:30:00",
  "auctionEndDate": "2026-10-26T15:30:00",
  "startingPrice": 2400,
  "incrementGap": 100,
  "productCondition": "USED"
}
```

Upload PNG/JPEG images as multipart form fields `file` and `productID` before
approval. Maximum image size is 5 MB. Create a bid with
`{"productID":"product-id","amount":2500}`; the server supplies bidder identity
and timestamp. Moderation requests use `{"status":"APPROVED"}` or
`{"status":"REJECTED"}`. Invoice downloads return `application/pdf` bytes.

OpenAPI documentation is available at `/swagger-ui` on the API server.

## Recovery, communication and payment endpoints

| Endpoint                                                      | Purpose                                                           |
| ------------------------------------------------------------- | ----------------------------------------------------------------- |
| `POST /auth/forgot-password`                                  | Public, generic response; body `{"email":"..."}`                  |
| `POST /auth/reset-password`                                   | Public, single-use reset; body `{"token":"...","password":"..."}` |
| `GET /notifications`                                          | Current account notifications                                     |
| `PUT /notifications/{id}/read`, `PUT /notifications/read-all` | Owned read state                                                  |
| `GET /conversations`, `POST /conversations`                   | Private support inbox and new thread                              |
| `GET /conversations/{id}`                                     | Authorized participant or administrator; marks read               |
| `POST /conversations/{id}/messages`                           | Reply with `{"content":"..."}`                                    |
| `PUT /conversations/{id}/status`                              | Authorized close/reopen with `OPEN` or `CLOSED`                   |
| `GET /admins/email-outbox`                                    | Administrator-only delivery status and local preview              |
| `POST /admins/email-outbox/{id}/retry`                        | Explicit SMTP retry                                               |
| `GET /payments/config`                                        | Checkout availability/provider/mode; no secrets                   |
| `POST /payments/orders`                                       | Buyer creates/reuses a server-priced order                        |
| `POST /payments/local-test`                                   | Local profile only; simulate success/failure, no real money       |
| `POST /payments/verify`                                       | Buyer submits signed Razorpay checkout response for verification  |
| `POST /payments/webhooks/razorpay`                            | Signed raw provider webhook, public transport endpoint            |

See [payment configuration](payments.md), [email and recovery](email-and-recovery.md)
and [MySQL verification](mysql.md) for operational details.
