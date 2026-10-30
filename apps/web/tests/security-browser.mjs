import assert from 'node:assert/strict';
import { existsSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../../../', import.meta.url));
if (!process.env.PLAYWRIGHT_BROWSERS_PATH && existsSync(resolve(root, '.cache/ms-playwright'))) {
  process.env.PLAYWRIGHT_BROWSERS_PATH = resolve(root, '.cache/ms-playwright');
}
const { chromium, expect } = await import('@playwright/test');
const base = (process.env.WEB_URL || 'http://127.0.0.1:5173').replace(/\/$/, '');
assert.ok(
  ['localhost', '127.0.0.1'].includes(new URL(base).hostname),
  'Run identity-isolation checks against a local development instance',
);
const password = 'BrowserSecurity123!';
const unique = Date.now().toString(36);
const failures = [];
async function api(path, { token, body, method = 'GET', expected = 200 } = {}) {
  const response = await fetch(`${base}/api${path}`, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body ? { 'Content-Type': 'application/json' } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
    signal: AbortSignal.timeout(15000),
  });
  assert.equal(response.status, expected, `${method} ${path}: ${response.status}`);
  return (await response.json()).data;
}
const account = async (suffix) => {
  const profile = {
    firstName: `Security${suffix}`,
    lastName: 'Browser',
    email: `security-${suffix.toLowerCase()}-${unique}@example.test`,
    username: `sec${suffix.toLowerCase()}${unique}`,
    password,
    role: 'BIDDER',
  };
  const session = await api('/auth/signup', { method: 'POST', body: profile });
  return { ...profile, ...session };
};
const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({ viewport: { width: 1280, height: 900 } });
context.on('page', (page) => page.on('pageerror', (error) => failures.push(error.message)));
const first = await context.newPage();
const second = await context.newPage();
const signIn = async (profile) => {
  await first.goto(`${base}/login`);
  await first.getByLabel('Email or username').fill(profile.username);
  await first.getByLabel('Password', { exact: true }).fill(password);
  await first.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(first.getByRole('button', { name: 'Sign out', exact: true })).toBeVisible();
};

try {
  const a = await account('A');
  const b = await account('B');
  const independent = await api('/auth/signin', {
    method: 'POST',
    body: { email: a.email, password },
  });
  await signIn(a);
  await first.goto(`${base}/account?source=security#profile`);
  await expect(first.getByLabel('First name', { exact: true })).toHaveValue(a.firstName);
  await second.goto(`${base}/account`);
  await expect(second.getByLabel('First name', { exact: true })).toHaveValue(a.firstName);
  // Simulate a queued identity change from another tab while this profile stays mounted.
  await second.evaluate((token) => localStorage.setItem('token', token), b.token);
  await expect(first.getByLabel('First name', { exact: true })).toHaveValue(b.firstName);
  await expect(first.locator('.detail-list')).toContainText(b.email);
  await second.evaluate((token) => localStorage.setItem('token', token), a.token);
  await expect(first.getByLabel('First name', { exact: true })).toHaveValue(a.firstName);
  await second.reload();
  await expect(second.getByLabel('First name', { exact: true })).toHaveValue(a.firstName);
  console.log('Browser security 1: Username sign-in and mounted cross-tab profile isolation');

  const signedOut = first.waitForResponse(
    (response) =>
      response.url().endsWith('/api/auth/logout') && response.request().method() === 'POST',
  );
  await first.getByRole('button', { name: 'Sign out', exact: true }).click();
  assert.equal((await signedOut).status(), 200);
  await expect(
    first.getByRole('heading', { name: 'Sign in to BidPro', exact: true }),
  ).toBeVisible();
  await expect(
    second.getByRole('heading', { name: 'Sign in to BidPro', exact: true }),
  ).toBeVisible();
  await api('/auth/validate', { token: a.token, expected: 401 });
  await api('/auth/validate', { token: independent.token });
  console.log(
    'Browser security 2: Current-session sign-out revokes its token and clears other tabs',
  );

  await first.goto(`${base}/account?source=deep-link#profile`);
  await expect(
    first.getByRole('heading', { name: 'Sign in to BidPro', exact: true }),
  ).toBeVisible();
  await first.getByLabel('Email or username').fill(a.username);
  await first.getByLabel('Password', { exact: true }).fill(password);
  await first.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(first).toHaveURL(`${base}/account?source=deep-link#profile`);
  await expect(first.getByRole('heading', { name: 'My profile', exact: true })).toBeVisible();
  const currentToken = await first.evaluate(() => localStorage.getItem('token'));
  await second.goto(`${base}/account`);
  await expect(second.getByRole('heading', { name: 'My profile', exact: true })).toBeVisible();
  const allSignedOut = first.waitForResponse(
    (response) =>
      response.url().endsWith('/api/auth/logout-all') && response.request().method() === 'POST',
  );
  await first.getByRole('button', { name: 'Sign out all devices', exact: true }).click();
  assert.equal((await allSignedOut).status(), 200);
  await expect(
    first.getByRole('heading', { name: 'Sign in to BidPro', exact: true }),
  ).toBeVisible();
  await expect(
    second.getByRole('heading', { name: 'Sign in to BidPro', exact: true }),
  ).toBeVisible();
  await api('/auth/validate', { token: currentToken, expected: 401 });
  await api('/auth/validate', { token: independent.token, expected: 401 });
  await api('/auth/validate', { token: b.token });
  console.log(
    'Browser security 3: Query/hash deep links survive sign-in and logout-all isolates account ownership',
  );

  await signIn(b);
  await first.route('**/api/auth/logout', (route) => route.abort('failed'));
  await first.getByRole('button', { name: 'Sign out', exact: true }).click();
  await expect(
    first.getByRole('heading', { name: 'Sign in to BidPro', exact: true }),
  ).toBeVisible();
  await expect(first.getByRole('status')).toContainText('could not confirm session revocation');
  assert.equal(await first.evaluate(() => localStorage.getItem('token')), null);
  await first.unroute('**/api/auth/logout');
  await api('/auth/logout-all', { method: 'POST', token: b.token });
  assert.deepEqual(failures, [], 'No frontend exceptions during session transitions');
  console.log(
    'Browser security 4: Offline sign-out always clears local credentials and reports unconfirmed revocation',
  );
  console.log(
    `Browser security checks passed (4 stages). Fixtures: security-*-${unique}@example.test`,
  );
} finally {
  await context.close();
  await browser.close();
}
