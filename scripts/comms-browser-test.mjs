import assert from 'node:assert/strict';
import { existsSync } from 'node:fs';
import { mkdir } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
if (!process.env.PLAYWRIGHT_BROWSERS_PATH && existsSync(resolve(root, '.cache/ms-playwright'))) {
  process.env.PLAYWRIGHT_BROWSERS_PATH = resolve(root, '.cache/ms-playwright');
}
const { chromium, expect } = await import('@playwright/test');
const base = process.env.WEB_URL || 'http://127.0.0.1:5173';
const suffix = Date.now().toString(36);
const email = `communications-${suffix}@example.test`;
const password = 'CommsInitial123!';
const changedPassword = 'CommsChanged123!';
const subject = `Delivery question ${suffix}`;
let stages = 0;
const failures = [];
const browser = await chromium.launch({ headless: true });
const contextOptions = { viewport: { width: 1440, height: 1000 }, timezoneId: 'Asia/Kolkata' };
const ownerContext = await browser.newContext(contextOptions);
const adminContext = await browser.newContext(contextOptions);
const recoveryContext = await browser.newContext(contextOptions);
const outsiderContext = await browser.newContext(contextOptions);
const owner = await ownerContext.newPage();
const admin = await adminContext.newPage();
const recovery = await recoveryContext.newPage();
const outside = await outsiderContext.newPage();
for (const page of [owner, admin, recovery, outside]) {
  page.on('pageerror', (error) => failures.push(error.message));
  page.on('response', (response) => {
    if (response.url().includes('/api/') && response.status() >= 500)
      failures.push(`${response.status()} ${response.url()}`);
  });
}
async function api(path, { method = 'GET', body, token, status = 200 } = {}) {
  const response = await fetch(`${base}/api${path}`, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(15000),
  });
  const payload = await response.json();
  assert.equal(response.status, status, `${method} ${path}: ${payload.message}`);
  return payload.data;
}
async function login(page, username, secret) {
  await page.goto(`${base}/login`);
  await page.getByLabel('Email or username').fill(username);
  await page.getByLabel('Password', { exact: true }).fill(secret);
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Sign out', exact: true })).toBeVisible();
}
function passed(message) {
  stages++;
  console.log(`Communications ${stages}: ${message}`);
}
try {
  await expect
    .poll(
      async () => {
        try {
          const response = await fetch(`${base}/api/health`, { signal: AbortSignal.timeout(3000) });
          await response.arrayBuffer();
          return response.ok;
        } catch {
          return false;
        }
      },
      { timeout: 60000 },
    )
    .toBe(true);
  const adminSession = await api('/auth/signin', {
    method: 'POST',
    body: {
      email: process.env.SMOKE_ADMIN_EMAIL || 'admin@bidpro.local',
      password: process.env.SMOKE_PASSWORD || 'BidPro123!',
    },
  });
  const fixture = await api('/auth/signup', {
    method: 'POST',
    body: {
      firstName: 'Communications',
      lastName: 'Bidder',
      username: `communications-${suffix}`,
      email,
      password,
      role: 'BIDDER',
    },
  });
  await api(`/admins/users/${fixture.user.userID}/status`, {
    method: 'PUT',
    token: adminSession.token,
    body: { status: 'APPROVED' },
  });
  const outsider = await api('/auth/signup', {
    method: 'POST',
    body: {
      firstName: 'Outside',
      lastName: 'Account',
      username: `outside-${suffix}`,
      email: `outside-${suffix}@example.test`,
      password,
      role: 'BIDDER',
    },
  });
  await api(`/admins/users/${outsider.user.userID}/status`, {
    method: 'PUT',
    token: adminSession.token,
    body: { status: 'APPROVED' },
  });
  await login(owner, email, password);
  await login(
    admin,
    process.env.SMOKE_ADMIN_EMAIL || 'admin@bidpro.local',
    process.env.SMOKE_PASSWORD || 'BidPro123!',
  );
  await owner.goto(`${base}/messages`);
  await owner.getByRole('button', { name: 'New conversation', exact: true }).click();
  await owner.getByLabel('Subject', { exact: true }).fill(subject);
  await owner
    .getByLabel('Your message', { exact: true })
    .fill('Can you help me check the auction delivery?');
  await owner.getByRole('button', { name: 'Start conversation', exact: true }).click();
  await expect(owner.getByRole('heading', { name: subject, exact: true })).toBeVisible();
  const threadID = new URL(owner.url()).pathname.split('/').at(-1);
  passed('Approved user starts a persistent private support conversation');

  await api(`/conversations/${threadID}`, { token: outsider.token, status: 403 });
  await api(`/conversations/${threadID}/messages`, {
    method: 'POST',
    token: outsider.token,
    body: { content: 'Forged reply' },
    status: 403,
  });
  await api('/conversations', {
    method: 'POST',
    token: fixture.token,
    body: { subject: 'Recipient spoof', message: 'Invalid', recipientUserID: outsider.user.userID },
    status: 403,
  });
  await api('/admins/email-outbox', { token: fixture.token, status: 403 });
  await login(outside, `outside-${suffix}@example.test`, password);
  await outside.goto(`${base}/messages/${threadID}`);
  await expect(
    outside.getByRole('alert').filter({
      hasText: 'This conversation is private to its participant and marketplace support',
    }),
  ).toBeVisible();
  await expect(outside.getByRole('heading', { name: subject, exact: true })).toHaveCount(0);
  passed('Outsider cannot read/reply, choose recipients, or access email previews');

  await admin.goto(`${base}/messages/${threadID}`);
  await expect(admin.getByRole('heading', { name: subject, exact: true })).toBeVisible();
  await expect(
    admin.getByText('Can you help me check the auction delivery?', { exact: true }),
  ).toBeVisible();
  await admin
    .getByLabel('Reply', { exact: true })
    .fill('Support checked the delivery and will keep you updated.');
  await admin.getByRole('button', { name: 'Send reply', exact: true }).click();
  await expect(
    admin.getByText('Support checked the delivery and will keep you updated.', { exact: true }),
  ).toBeVisible();
  await owner.reload();
  await expect(
    owner.getByText('Support checked the delivery and will keep you updated.', { exact: true }),
  ).toBeVisible();
  await owner.getByRole('button', { name: 'Close conversation', exact: true }).click();
  await expect(
    owner.getByRole('button', { name: 'Reopen conversation', exact: true }),
  ).toBeVisible();
  await expect(owner.getByLabel('Reply', { exact: true })).toHaveCount(0);
  await admin.reload();
  await admin.getByRole('button', { name: 'Reopen conversation', exact: true }).click();
  await owner.reload();
  await expect(owner.getByLabel('Reply', { exact: true })).toBeVisible();
  passed('Admin replies persist and close/reopen controls enforce thread state');

  await owner.goto(`${base}/notifications`);
  await expect(owner.getByText(`New message: ${subject}`, { exact: true })).toBeVisible();
  await owner.getByRole('button', { name: 'Mark all as read', exact: true }).click();
  await expect(
    owner.getByRole('status').filter({ hasText: 'All notifications marked as read.' }),
  ).toBeVisible();
  await expect(owner.getByRole('button', { name: 'Mark all as read', exact: true })).toBeDisabled();
  passed('Real event notifications are visible and owner read status persists');

  await recovery.goto(`${base}/forgot-password`);
  await recovery.getByLabel('Account email', { exact: true }).fill(email);
  await recovery.getByRole('button', { name: 'Send reset instructions', exact: true }).click();
  await expect(
    recovery.getByRole('status').filter({ hasText: 'If an account matches that email' }),
  ).toBeVisible();
  await admin.goto(`${base}/admin/email-outbox`);
  const resetEmail = admin
    .getByRole('row')
    .filter({ hasText: email })
    .filter({ hasText: 'Reset your BidPro password' });
  await expect(resetEmail).toBeVisible();
  await resetEmail.getByRole('button', { name: 'View email', exact: true }).click();
  const link = await admin
    .getByRole('link', { name: 'Open local password reset link', exact: true })
    .getAttribute('href');
  const resetURL = new URL(link);
  const token = resetURL.searchParams.get('token');
  assert.equal(typeof token, 'string');
  await recovery.goto(`${base}${resetURL.pathname}${resetURL.search}`);
  await recovery.getByLabel('New password', { exact: true }).fill(changedPassword);
  await recovery.getByLabel('Confirm new password', { exact: true }).fill(changedPassword);
  await recovery.getByRole('button', { name: 'Reset password', exact: true }).click();
  await expect(
    recovery.getByRole('link', { name: 'Continue to sign in', exact: true }),
  ).toBeVisible();
  assert.equal(new URL(recovery.url()).searchParams.has('token'), false);
  passed('Local email preview delivers a one-use password reset link into a signed-out context');

  await api('/auth/validate', { token: fixture.token, status: 401 });
  await api('/auth/signin', { method: 'POST', body: { email, password }, status: 401 });
  await api('/auth/reset-password', {
    method: 'POST',
    body: { token, password: 'AnotherPassword123!' },
    status: 400,
  });
  await login(recovery, email, changedPassword);
  await recovery.goto(`${base}/account`);
  await expect(recovery.getByRole('heading', { name: 'My profile', exact: true })).toBeVisible();
  passed(
    'Reset revokes old sessions/passwords, rejects token replay, and permits the new password',
  );

  await recovery.setViewportSize({ width: 390, height: 844 });
  await expect
    .poll(() =>
      recovery.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1),
    )
    .toBe(true);
  await mkdir(resolve(root, 'test-results'), { recursive: true });
  await recovery.screenshot({
    path: resolve(root, 'test-results/communications-mobile.png'),
    fullPage: true,
  });
  assert.deepEqual(failures, [], 'No browser exceptions or backend 5xx responses');
  passed('Authenticated mobile navigation fits without horizontal overflow');
  console.log(`Communications browser workflow passed (${stages} stages). Fixture: ${email}`);
} catch (error) {
  await mkdir(resolve(root, 'test-results'), { recursive: true });
  await owner
    .screenshot({ path: resolve(root, 'test-results/communications-failure.png'), fullPage: true })
    .catch(() => {});
  console.error(`Communications workflow failed: ${error.message}`);
  if (failures.length) console.error(failures.join('\n'));
  process.exitCode = 1;
} finally {
  await browser.close();
}
