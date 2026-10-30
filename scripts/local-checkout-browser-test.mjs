import assert from 'node:assert/strict';
import { existsSync } from 'node:fs';
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
if (!process.env.PLAYWRIGHT_BROWSERS_PATH && existsSync(resolve(root, '.cache/ms-playwright'))) {
  process.env.PLAYWRIGHT_BROWSERS_PATH = resolve(root, '.cache/ms-playwright');
}
const { chromium, expect } = await import('@playwright/test');
const base = (process.env.WEB_URL || 'http://127.0.0.1:5173').replace(/\/$/, '');
const password = process.env.SMOKE_PASSWORD || 'BidPro123!';
const title = `Local checkout ${Date.now().toString(36)}`;
const artifacts = resolve(root, 'test-results');
await mkdir(artifacts, { recursive: true });
const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({
  viewport: { width: 1440, height: 1000 },
  timezoneId: 'Asia/Kolkata',
});
const failures = [];
let stages = 0;
page.on('pageerror', (error) => failures.push(error.message));
page.on('response', (response) => {
  if (response.url().includes('/api/') && response.status() >= 500)
    failures.push(`${response.status()} ${response.url()}`);
});
async function api(path, { method = 'GET', body, token, statuses = [200, 201] } = {}) {
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
  assert.ok(
    statuses.includes(response.status),
    `${method} ${path}: ${response.status} ${payload.message}`,
  );
  return payload.data;
}
async function signin(role) {
  return api('/auth/signin', { method: 'POST', body: { email: `${role}@bidpro.local`, password } });
}
function localDate(milliseconds) {
  const date = new Date(milliseconds);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 19);
}
function passed(message) {
  stages++;
  console.log(`Local checkout ${stages}: ${message}`);
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
  const admin = await signin('admin');
  const seller = await signin('seller');
  const buyer = await signin('bidder');
  const config = await api('/payments/config', { token: buyer.token });
  assert.equal(
    config.mode,
    'local-test',
    'Run this check with the local profile and PAYMENTS_ENABLED=false',
  );
  assert.equal(config.enabled, true);
  const categories = await api('/categories');
  assert.ok(categories.length, 'A visible category is required');
  const product = await api('/products', {
    method: 'POST',
    token: seller.token,
    body: {
      title,
      description: 'Local checkout browser verification, no real payment.',
      categoryID: categories[0].categoryID,
      auctionStartDate: localDate(Date.now() - 120000),
      auctionEndDate: localDate(Date.now() + 3600000),
      startingPrice: 100,
      incrementGap: 10,
      productCondition: 'USED',
    },
  });
  const approved = await api(`/admins/products/${product.productID}/status`, {
    method: 'PUT',
    token: admin.token,
    body: { status: 'APPROVED' },
  });
  await api('/bids', {
    method: 'POST',
    token: buyer.token,
    body: { productID: product.productID, amount: 110 },
  });
  await api(`/auctions/${approved.auctionID}/close`, { method: 'POST', token: admin.token });
  const invoice = await api(`/invoices?productID=${product.productID}`, {
    method: 'POST',
    token: buyer.token,
  });
  passed('Real seller listing, approval, winning bid, closing and invoice fixture');

  await page.goto(`${base}/login`);
  await page.getByLabel('Email or username').fill('bidder@bidpro.local');
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Sign out', exact: true })).toBeVisible();
  await page.goto(`${base}/bidder/invoices`);
  const row = page.getByRole('row').filter({ hasText: title });
  await expect(row).toBeVisible();
  await expect(page).toHaveTitle('Invoices · BidPro');
  await expect(page.locator('#main-content')).toBeFocused();
  const originalOverflow = await page.evaluate(() => document.body.style.overflow);
  const firstResponse = page.waitForResponse(
    (response) =>
      response.url().endsWith('/api/payments/orders') && response.request().method() === 'POST',
  );
  await row.getByRole('button', { name: 'Open local test checkout', exact: true }).click();
  const firstOrder = (await (await firstResponse).json()).data;
  const dialog = page.getByRole('dialog', { name: 'Local test checkout' });
  await expect(dialog).toBeVisible();
  await expect(dialog).toContainText('No money is transferred.');
  const closeCheckout = dialog.getByRole('button', { name: 'Close local checkout', exact: true });
  const successfulOutcome = dialog.getByRole('button', {
    name: 'Simulate successful payment',
    exact: true,
  });
  await expect(closeCheckout).toBeFocused();
  assert.equal(
    await page.locator('#root').evaluate((element) => element.inert),
    true,
    'Checkout makes background content inert',
  );
  assert.equal(
    await page.evaluate(() => document.body.style.overflow),
    'hidden',
    'Checkout locks background scrolling',
  );
  await page.keyboard.press('Shift+Tab');
  await expect(successfulOutcome).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(closeCheckout).toBeFocused();
  await page.keyboard.press('Escape');
  await expect(dialog).toHaveCount(0);
  await expect(
    row.getByRole('button', { name: 'Open local test checkout', exact: true }),
  ).toBeFocused();
  assert.equal(
    await page.locator('#root').evaluate((element) => element.inert),
    false,
    'Closing checkout restores background interactivity',
  );
  assert.equal(await page.evaluate(() => document.body.style.overflow), originalOverflow);
  await row.getByRole('button', { name: 'Open local test checkout', exact: true }).click();
  await expect(dialog).toBeVisible();
  assert.equal(
    await dialog.locator('input').count(),
    0,
    'Local checkout must not collect payment instrument data',
  );
  let releaseOutcome;
  let outcomeStarted;
  const release = new Promise((resolve) => {
    releaseOutcome = resolve;
  });
  const started = new Promise((resolve) => {
    outcomeStarted = resolve;
  });
  const localPaymentRoute = '**/api/payments/local-test';
  await page.route(localPaymentRoute, async (route) => {
    outcomeStarted();
    await release;
    await route.continue();
  });
  try {
    await dialog.getByRole('button', { name: 'Simulate failed payment', exact: true }).click();
    await started;
    await expect(dialog).toHaveAttribute('aria-busy', 'true');
    await page.keyboard.press('Escape');
    await expect(dialog).toBeVisible();
    await expect(closeCheckout).toBeDisabled();
  } finally {
    releaseOutcome();
  }
  await expect(row.getByText('Failed', { exact: true })).toBeVisible();
  await page.unroute(localPaymentRoute);
  const failed = await api(`/invoices/${invoice.invoiceID}`, { token: buyer.token });
  assert.equal(failed.paymentStatus, 'FAILED');
  assert.equal(failed.paymentSource, 'LOCAL_TEST');
  const retryOrder = await api('/payments/orders', {
    method: 'POST',
    token: buyer.token,
    body: { invoiceID: invoice.invoiceID },
  });
  assert.equal(retryOrder.orderID, firstOrder.orderID);
  passed(
    'Keyboard-accessible checkout, focus restoration, busy-close guard, failed payment and idempotent order retry',
  );

  await row.getByRole('button', { name: 'Open local test checkout', exact: true }).click();
  await expect(dialog).toBeVisible();
  await dialog.getByRole('button', { name: 'Simulate successful payment', exact: true }).click();
  await expect(row.getByText('Paid', { exact: true })).toBeVisible();
  await expect(row).toContainText('Local test payment (no money transferred)');
  const complete = await api(`/invoices/${invoice.invoiceID}`, { token: buyer.token });
  assert.equal(complete.paymentSource, 'LOCAL_TEST');
  assert.equal(complete.paymentMode, 'local-test');
  assert.ok(complete.paymentReference.startsWith('local_payment_'));
  for (const outcome of ['SUCCESS', 'FAILURE']) {
    const duplicate = await api('/payments/local-test', {
      method: 'POST',
      token: buyer.token,
      body: { invoiceID: invoice.invoiceID, outcome },
    });
    assert.equal(duplicate.status, 'CAPTURED');
    assert.equal(duplicate.paymentStatus, 'PAID');
    assert.equal(duplicate.paymentID, complete.paymentReference);
  }
  passed('Successful local checkout records test source and cannot be duplicated or downgraded');

  for (const account of [seller, admin]) {
    await api('/payments/local-test', {
      method: 'POST',
      token: account.token,
      body: { invoiceID: invoice.invoiceID, outcome: 'SUCCESS' },
      statuses: [403],
    });
  }
  const pdf = await fetch(`${base}/api/invoices/${invoice.invoiceID}/download`, {
    headers: { Authorization: `Bearer ${buyer.token}` },
    signal: AbortSignal.timeout(15000),
  });
  assert.equal(pdf.status, 200);
  const bytes = Buffer.from(await pdf.arrayBuffer());
  assert.equal(bytes.subarray(0, 5).toString(), '%PDF-');
  await writeFile(resolve(artifacts, 'local-checkout-invoice.pdf'), bytes);
  await page.screenshot({
    path: resolve(artifacts, 'local-checkout-complete.png'),
    fullPage: true,
  });
  assert.deepEqual(failures, [], 'No browser exceptions or backend failures');
  passed('Wrong-role calls rejected and valid invoice PDF downloaded');
  console.log(`Local checkout browser passed (${stages} stages). Fixture: ${title}`);
} catch (error) {
  await page
    .screenshot({ path: resolve(artifacts, 'local-checkout-failure.png'), fullPage: true })
    .catch(() => {});
  console.error(`Local checkout failed at ${page.url()}: ${error.message}`);
  if (failures.length) console.error(failures.join('\n'));
  process.exitCode = 1;
} finally {
  await browser.close();
}
