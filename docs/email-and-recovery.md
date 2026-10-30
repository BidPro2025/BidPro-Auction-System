# Email, recovery and notifications

The local profile stores emails in an administrator-only outbox. Sign in as an
administrator and open **Email outbox** to preview account updates, auction
notifications and password reset instructions. These messages are explicitly
marked **Local**; no external delivery is claimed.

## Password reset

Use **Forgot password?** on the sign-in page and submit an account email. The
response is deliberately the same for known and unknown accounts. In local
testing, open the captured reset email from the admin outbox and use its link in
a signed-out browser. With SMTP configured, the email arrives in the account's
mailbox instead.

Reset tokens expire after 30 minutes, are stored as hashes, and can be used once.
Changing the password invalidates earlier sessions and other reset links.
Requests are throttled by account email and client address. Reset tokens are
never returned by the public API. SMTP mode hides reset email bodies from the
admin preview and clears the sensitive body after successful delivery.

## SMTP setup

The root launcher loads `.env`. Start with `.env.example` and configure:

```dotenv
MAIL_MODE=smtp
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-sending-account@example.com
MAIL_PASSWORD=your-smtp-credential
MAIL_FROM=your-sending-account@example.com
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
APP_PUBLIC_URL=https://your-web-app.example
```

Use the SMTP credentials and sending-address rules required by your email
provider. Gmail accounts generally require a provider-issued app password when
that account supports it; business SMTP servers may use different requirements.
Use `APP_PUBLIC_URL` for the actual frontend address so reset links open the
correct site. Keep credentials in the ignored local environment file.

Emails are queued transactionally and delivered by a background worker with
connection/read/write timeouts. Failed messages retry with increasing delays up
to five attempts. Administrators can inspect status and explicitly retry failed
delivery from the outbox. Local messages are not automatically sent when SMTP is
enabled later.

## In-app notifications and messages

**Notifications** lists the signed-in account's updates and supports marking one
or all as read. Events include approvals, listing activity, bidding, auction
results, invoices, payments and support messages.

**Messages** opens private marketplace-support conversations. Approved users can
contact support; administrators can respond or start a conversation with an
approved account. Other accounts cannot list, open, reply to or change the
conversation. Closing a conversation stops replies until it is reopened.

The incompatible prototype messaging endpoints stay disabled. The working inbox
uses `/api/conversations` and preserves historical tables.

Automated SMTP verification uses a real loopback SMTP listener and JavaMail. It
does not send email to outside recipients.
