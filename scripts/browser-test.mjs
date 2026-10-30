import assert from 'node:assert/strict';
import { existsSync } from 'node:fs';
import { mkdir, readFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
if (!process.env.PLAYWRIGHT_BROWSERS_PATH && existsSync(resolve(root, '.cache/ms-playwright'))) {
  process.env.PLAYWRIGHT_BROWSERS_PATH = resolve(root, '.cache/ms-playwright');
}
const { chromium, expect } = await import('@playwright/test');
const base = process.env.WEB_URL || 'http://127.0.0.1:5173';
const suffix = Date.now().toString(36);
const title = `Browser camera ${suffix}`;
const email = `browser-${suffix}@example.test`;
const password = process.env.SMOKE_PASSWORD || 'BidPro123!';
const artifacts = resolve(root, 'test-results');
await mkdir(artifacts, { recursive: true });
const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({
  viewport: { width: 1440, height: 1000 },
  timezoneId: 'Asia/Kolkata',
});
const page = await context.newPage();
page.setDefaultTimeout(15000);
const failures = [];
page.on('pageerror', (error) => failures.push(error.message));
page.on('response', (response) => {
  if (response.url().includes('/api/') && response.status() >= 500)
    failures.push(`${response.status()} ${response.url()}`);
});
page.on('dialog', (dialog) => dialog.accept());
let steps = 0;
function checked(description) {
  steps++;
  console.log(`Browser ${steps}: ${description}`);
}
async function signin(account) {
  await page.goto(`${base}/login`);
  await page.getByLabel('Email or username').fill(account);
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Sign in', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Sign out' })).toBeVisible();
}
async function signout() {
  await page.getByRole('button', { name: 'Sign out' }).click();
  await expect(page).toHaveURL(/\/login$/);
}

try {
  await expect
    .poll(
      async () => {
        try {
          return (await fetch(`${base}/api/health`, { signal: AbortSignal.timeout(3000) })).ok;
        } catch {
          return false;
        }
      },
      { timeout: 60000, message: 'The local API must be ready through the frontend proxy' },
    )
    .toBe(true);
  await page.goto(base);
  await expect(page.getByRole('heading', { name: /Great finds/ })).toBeVisible();
  await page.screenshot({ path: resolve(artifacts, 'home-desktop.png'), fullPage: true });
  await page.goto(`${base}/admin/users`);
  await expect(page).toHaveURL(/\/login$/);
  checked('Public home and authentication redirect');

  await page.goto(`${base}/register`);
  await page.getByLabel('First name', { exact: true }).fill('Browser');
  await page.getByLabel('Last name', { exact: true }).fill('Bidder');
  await page.getByLabel('Username', { exact: true }).fill(`browser-${suffix}`);
  await page.getByLabel('Email', { exact: true }).fill(email);
  await page.getByLabel('Password', { exact: true }).fill(password);
  await page.getByLabel('Confirm password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'Create account', exact: true }).click();
  await expect(page.getByText(/An administrator must approve it/)).toBeVisible();
  await page.goto(`${base}/admin`);
  await expect(page).toHaveURL(/\/bidder$/);
  await signout();
  checked('Registration, pending account and role guard');

  await signin('admin@bidpro.local');
  await page.goto(`${base}/admin/users`);
  await page.getByLabel('Search users').fill(email);
  await page.getByRole('combobox', { name: `Status for ${email}` }).selectOption('APPROVED');
  await expect(
    page.getByRole('status').filter({ hasText: 'Account status updated.' }),
  ).toBeVisible();
  await page.goto(`${base}/admin/categories`);
  await page.getByLabel('Category name', { exact: true }).fill(`Browser ${suffix}`);
  await page.getByLabel('Description', { exact: true }).fill('Browser workflow category');
  await page.getByRole('button', { name: 'Create category', exact: true }).click();
  await expect(page.getByRole('row').filter({ hasText: `Browser ${suffix}` })).toBeVisible();
  await signout();
  checked('Admin account approval and category creation');

  await signin('seller@bidpro.local');
  await page.goto(`${base}/seller/new`);
  await page.getByLabel('Item title', { exact: true }).fill(title);
  await page
    .getByLabel('Description', { exact: true })
    .fill('Camera listed and verified through the real browser.');
  await page.getByLabel('Category', { exact: true }).selectOption({ label: `Browser ${suffix}` });
  await page.getByLabel('Starting price (INR)', { exact: true }).fill('100');
  await page.getByLabel('Minimum bid increment (INR)', { exact: true }).fill('10');
  const dates = await page.evaluate(() => {
    const local = (value) => {
      const date = new Date(value);
      date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
      return date.toISOString().slice(0, 16);
    };
    return [local(Date.now() - 120000), local(Date.now() + 3600000)];
  });
  await page.getByLabel('Auction starts', { exact: true }).fill(dates[0]);
  await page.getByLabel('Auction ends', { exact: true }).fill(dates[1]);
  // A valid PNG is created in browser canvas, avoiding platform image utilities.
  const encoded = await page.evaluate(() => {
    const canvas = document.createElement('canvas');
    canvas.width = 2;
    canvas.height = 2;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#174c3c';
    ctx.fillRect(0, 0, 2, 2);
    return canvas.toDataURL('image/png').split(',')[1];
  });
  await page.getByLabel('Product image (optional)', { exact: true }).setInputFiles({
    name: 'camera.png',
    mimeType: 'image/png',
    buffer: Buffer.from(encoded, 'base64'),
  });
  await page.getByRole('button', { name: 'Submit for review', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'My listings', exact: true })).toBeVisible();
  let listingRow = page.getByRole('row').filter({ hasText: title });
  await expect(listingRow).toBeVisible();
  const productPath = await listingRow
    .getByRole('link', { name: title, exact: true })
    .getAttribute('href');
  await listingRow.getByRole('link', { name: 'Edit', exact: true }).click();
  await page
    .getByLabel('Description', { exact: true })
    .fill('Updated camera description from the browser workflow.');
  await page.getByRole('button', { name: 'Submit for review', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'My listings', exact: true })).toBeVisible();
  await page.goto(`${base}${productPath}`);
  await expect(page.getByRole('heading', { name: title, exact: true })).toBeVisible();
  await expect(
    page.getByText('Updated camera description from the browser workflow.', { exact: true }),
  ).toBeVisible();
  await expect(page.locator('.product-art img')).toBeVisible();
  assert.equal(
    await page.locator('.product-art img').evaluate((img) => img.complete && img.naturalWidth > 0),
    true,
  );
  await signout();
  checked('Seller creates, uploads and edits a pending listing');

  await signin('admin@bidpro.local');
  await page.goto(`${base}/admin/products`);
  await page.getByLabel('Search products').fill(title);
  await page
    .getByRole('row')
    .filter({ hasText: title })
    .getByRole('button', { name: 'Approve', exact: true })
    .click();
  await expect(page.getByRole('status').filter({ hasText: 'Listing approved.' })).toBeVisible();
  await signout();
  checked('Admin approval publishes the linked auction');

  await signin(email);
  await page.goto(`${base}${productPath}`);
  const bidInput = page.getByLabel(/Your bid/);
  await bidInput.fill('100');
  await page.getByRole('button', { name: 'Place bid', exact: true }).click();
  await expect(
    page.getByRole('status').filter({ hasText: 'Your bid has been placed.' }),
  ).toBeVisible();
  await expect(page.getByRole('row').filter({ hasText: 'You' })).toHaveCount(1);
  await bidInput.fill('110');
  await page.getByRole('button', { name: 'Place bid', exact: true }).click();
  await expect(page.getByRole('row').filter({ hasText: 'You' })).toHaveCount(2);
  await page.reload();
  await expect(page.getByRole('button', { name: 'Sign out' })).toBeVisible();
  await page.goto(`${base}/bidder/bids`);
  await expect(page.getByRole('row').filter({ hasText: title })).toHaveCount(2);
  await signout();
  checked('Bidder places increasing bids and session survives reload');

  await signin('admin@bidpro.local');
  await page.goto(`${base}/admin/auctions`);
  const auctionRow = page.getByRole('row').filter({ hasText: title });
  await auctionRow.getByRole('button', { name: 'Close auction', exact: true }).click();
  await expect(
    auctionRow.getByRole('button', { name: 'Generate invoice', exact: true }),
  ).toBeVisible();
  await auctionRow.getByRole('button', { name: 'Generate invoice', exact: true }).click();
  await expect(page.getByRole('status').filter({ hasText: 'Invoice generated.' })).toBeVisible();
  await page.goto(`${base}/admin/invoices`);
  const invoiceRow = page.getByRole('row').filter({ hasText: title });
  await expect(invoiceRow).toBeVisible();
  await expect(invoiceRow).toContainText('110');
  await invoiceRow.getByRole('combobox', { name: /Payment status for/ }).selectOption('PAID');
  await expect(
    page.getByRole('status').filter({ hasText: /Payment record updated/ }),
  ).toBeVisible();
  const downloadPromise = page.waitForEvent('download');
  await invoiceRow.getByRole('button', { name: 'Download PDF' }).click();
  const download = await downloadPromise;
  const pdfPath = resolve(artifacts, 'browser-invoice.pdf');
  await download.saveAs(pdfPath);
  assert.equal((await readFile(pdfPath)).subarray(0, 5).toString(), '%PDF-');
  await signout();
  checked('Admin closes, settles, records payment and downloads a valid PDF');

  await signin('seller@bidpro.local');
  await page.goto(`${base}/seller/invoices`);
  await expect(page.getByRole('row').filter({ hasText: title })).toContainText('Paid');
  await signout();
  await signin(email);
  await page.goto(`${base}/bidder/invoices`);
  await expect(page.getByRole('row').filter({ hasText: title })).toContainText('Paid');
  await signout();
  checked('Buyer and seller can see the same paid invoice');

  await page.route('**/api/products/search?**', (route) => route.abort());
  await page.goto(`${base}/auctions`);
  await expect(page.getByText(/Cannot reach the server/)).toBeVisible();
  await page.unroute('**/api/products/search?**');
  await page.getByRole('button', { name: 'Try again', exact: true }).click();
  await expect(page.getByText(/Cannot reach the server/)).toHaveCount(0);
  checked('Network errors display a working retry');

  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(base);
  await expect(page.getByRole('heading', { name: /Great finds/ })).toBeVisible();
  await expect
    .poll(() => page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1))
    .toBe(true);
  await page.screenshot({ path: resolve(artifacts, 'home-mobile.png'), fullPage: true });
  assert.deepEqual(failures, [], 'No browser exceptions or server failures');
  checked('Mobile layout has no horizontal overflow or runtime errors');
  console.log(`Browser workflow passed (${steps} stages). Fixture: ${title}`);
} catch (error) {
  await page
    .screenshot({ path: resolve(artifacts, 'browser-failure.png'), fullPage: true })
    .catch(() => {});
  console.error(`Browser workflow failed at ${page.url()}: ${error.message}`);
  if (failures.length) console.error(failures.join('\n'));
  process.exitCode = 1;
} finally {
  await browser.close();
}
