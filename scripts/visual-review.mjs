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
const artifacts = resolve(root, 'test-results/glass');
await mkdir(artifacts, { recursive: true });
const browser = await chromium.launch({ headless: true });
const contexts = [];
const failures = [];
const captures = [];
let activePage;

async function context(token) {
  const item = await browser.newContext({
    viewport: { width: 1440, height: 1000 },
    timezoneId: 'Asia/Kolkata',
    reducedMotion: 'reduce',
  });
  contexts.push(item);
  if (token) {
    await item.addInitScript((value) => localStorage.setItem('token', value), token);
  }
  const page = await item.newPage();
  const media = await item.newCDPSession(page);
  await media.send('Emulation.setEmulatedMedia', {
    features: [
      { name: 'prefers-reduced-motion', value: 'reduce' },
      { name: 'prefers-reduced-transparency', value: 'no-preference' },
    ],
  });
  page.on('pageerror', (error) => failures.push(error.message));
  page.on('response', (response) => {
    if (response.url().includes('/api/') && response.status() >= 500) {
      failures.push(`${response.status()} ${response.url()}`);
    }
  });
  return page;
}

async function signin(role) {
  const response = await fetch(`${base}/api/auth/signin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email: process.env[`SMOKE_${role.toUpperCase()}_EMAIL`] || `${role}@bidpro.local`,
      password: process.env.SMOKE_PASSWORD || 'BidPro123!',
    }),
    signal: AbortSignal.timeout(15000),
  });
  const payload = await response.json();
  assert.equal(response.status, 200, `${role} sign-in: ${payload.message}`);
  return payload.data.token;
}

async function capture(page, path, name, heading, mobile = false) {
  activePage = page;
  await page.setViewportSize(mobile ? { width: 390, height: 844 } : { width: 1440, height: 1000 });
  await page.goto(`${base}${path}`);
  await expect(
    page.getByRole('heading', { name: heading, exact: typeof heading === 'string' }),
  ).toBeVisible();
  await expect(page.getByText('Something went wrong', { exact: true })).toHaveCount(0);
  // Wait for actual resources rather than capturing loading skeletons.
  await expect(page.locator('.loading-state')).toHaveCount(0, { timeout: 15000 });
  const editorial = page.locator('.collector-hero-media img');
  if (await editorial.count()) {
    await editorial.evaluate(async (image) => {
      await image.decode();
      if (!image.naturalWidth) throw new Error('Editorial image did not load');
      // Decoding can complete before the compositor paints the image into glass layers.
      await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    });
  }
  await expect
    .poll(() => page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1))
    .toBe(true);
  await page.screenshot({ path: resolve(artifacts, `${name}.png`), fullPage: mobile });
  captures.push(name);
  console.log(`Visual capture: ${name}`);
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

  const publicPage = await context();
  await capture(publicPage, '/', 'home-desktop', /Great finds/);
  const material = await publicPage.evaluate(() =>
    [...document.querySelectorAll('.glass-header,.header-content,.nav-capsule')].map((element) => {
      const style = getComputedStyle(element);
      return {
        selector: element.className,
        backdropFilter: style.backdropFilter || style.webkitBackdropFilter,
        background: style.backgroundColor,
      };
    }),
  );
  assert.ok(
    material.some((item) => item.backdropFilter?.includes('blur')),
    'Navigation should use a real backdrop blur material',
  );
  const initialSelectionCount = await publicPage.locator('.auction-section .product-card').count();
  for (const [button, state] of [
    ['Coming soon', 'Upcoming'],
    ['Live now', 'Live'],
  ]) {
    const filter = publicPage.getByRole('button', { name: button, exact: true });
    await filter.click();
    await expect(filter).toHaveAttribute('aria-pressed', 'true');
    const statuses = await publicPage
      .locator('.auction-section .product-card .badge')
      .allTextContents();
    assert.ok(
      statuses.every((value) => value.trim() === state),
      'Featured auction filters match the chosen status',
    );
  }
  await publicPage.getByRole('button', { name: 'All selections', exact: true }).click();
  await expect(publicPage.locator('.auction-section .product-card')).toHaveCount(
    initialSelectionCount,
  );
  await capture(publicPage, '/auctions', 'catalog-desktop', 'Find your next great thing');
  const catalogCards = publicPage.locator('.catalog-page .product-card');
  const catalogSort = publicPage.getByLabel('Sort by', { exact: true });
  const catalogSearch = publicPage.getByRole('searchbox', { name: 'Search listings' });
  const readPrices = async () =>
    (await catalogCards.locator('.price').allTextContents()).map((value) =>
      Number(value.replace(/[^\d.-]/g, '')),
    );
  const catalogCount = await catalogCards.count();
  assert.ok(catalogCount > 0, 'The catalog should contain listings to exercise sorting');
  const readProductIDs = async () =>
    catalogCards
      .locator('h3 a')
      .evaluateAll((links) => links.map((link) => link.getAttribute('href').split('/').at(-1)));
  const nextCatalogResponse = (sort, page = 0) =>
    publicPage.waitForResponse((response) => {
      const url = new URL(response.url());
      return (
        url.pathname.endsWith('/api/products/search') &&
        url.searchParams.get('sort') === sort &&
        Number(url.searchParams.get('page') || 0) === page &&
        response.ok()
      );
    });
  const verifyCatalogResponse = async (response, direction) => {
    const payload = (await (await response).json()).data;
    assert.ok(
      payload.items.length <= payload.size && payload.size <= 48,
      'Catalog responses have bounded page sizes',
    );
    await expect.poll(readProductIDs).toEqual(payload.items.map((item) => item.productID));
    const expectedPrices = payload.items.map(
      (item) => Number(item.currentBid) || Number(item.startingPrice),
    );
    await expect.poll(readPrices).toEqual(expectedPrices);
    assert.ok(expectedPrices.every(Number.isFinite), 'Displayed listing prices should be numeric');
    if (direction)
      assert.deepEqual(
        expectedPrices,
        [...expectedPrices].sort((a, b) => (a - b) * direction),
        'Prices are ordered across the server page',
      );
    return payload;
  };
  let highPage;
  for (const [value, direction] of [
    ['price-low', 1],
    ['price-high', -1],
  ]) {
    const response = nextCatalogResponse(value);
    await catalogSort.selectOption(value);
    await expect(catalogSort).toHaveValue(value);
    await expect(publicPage).toHaveURL(new RegExp(`[?&]sort=${value}(?:&|$)`));
    const data = await verifyCatalogResponse(response, direction);
    if (value === 'price-high') highPage = data;
  }
  let paginationChecked = false;
  if (highPage.totalPages > 1) {
    const response = nextCatalogResponse('price-high', 1);
    await publicPage.getByRole('button', { name: 'Next page', exact: true }).click();
    const secondPage = await verifyCatalogResponse(response, -1);
    assert.equal(secondPage.page, 1);
    const firstPrices = highPage.items.map(
      (item) => Number(item.currentBid) || Number(item.startingPrice),
    );
    const secondPrices = secondPage.items.map(
      (item) => Number(item.currentBid) || Number(item.startingPrice),
    );
    assert.ok(
      firstPrices.at(-1) >= secondPrices[0],
      'Descending price order continues across pages',
    );
    await expect(publicPage.getByRole('navigation', { name: 'Catalog pages' })).toContainText(
      'Page 2 of',
    );
    paginationChecked = true;
  }
  await catalogSearch.fill('unlikely-unique-no-match');
  await expect(
    publicPage.getByRole('heading', { name: 'No listings match your search', exact: true }),
  ).toBeVisible();
  await expect(catalogCards).toHaveCount(0);
  const clearedResponse = nextCatalogResponse('price-high');
  await publicPage.getByRole('button', { name: 'Clear filters', exact: true }).click();
  const restored = await verifyCatalogResponse(clearedResponse, -1);
  await expect(catalogCards).toHaveCount(restored.items.length);
  await expect(catalogSearch).toHaveValue('');
  await expect(catalogSort).toHaveValue('price-high');
  assert.equal(new URL(publicPage.url()).searchParams.get('sort'), 'price-high');
  assert.equal(new URL(publicPage.url()).searchParams.has('q'), false);
  assert.equal(
    new URL(publicPage.url()).searchParams.has('page'),
    false,
    'Filtering and clearing reset the catalog to its first page',
  );
  await catalogSort.selectOption('ending');
  const catalogChecks = {
    visibleListingCount: restored.items.length,
    totalListingCount: restored.totalItems,
    priceSorting: true,
    clearPreservesSort: true,
    serverPagination: paginationChecked,
  };
  await capture(publicPage, '/login', 'login-desktop', 'Sign in to BidPro');
  await capture(publicPage, '/', 'home-mobile', /Great finds/, true);
  await capture(publicPage, '/login', 'login-mobile', 'Sign in to BidPro', true);
  const mobileLoginInputs = await publicPage.locator('.auth-card input').evaluateAll((inputs) =>
    inputs.map((input) => ({
      name: input.name,
      fontSize: Number.parseFloat(getComputedStyle(input).fontSize),
      height: input.getBoundingClientRect().height,
    })),
  );
  assert.ok(mobileLoginInputs.length >= 2, 'Mobile sign-in should expose both account fields');
  for (const input of mobileLoginInputs) {
    assert.ok(input.fontSize >= 16, `${input.name} uses readable mobile text without input zoom`);
    assert.ok(input.height >= 44, `${input.name} has a usable mobile touch target`);
  }
  const mobileSigninTarget = await publicPage
    .getByRole('button', { name: 'Sign in', exact: true })
    .boundingBox();
  assert.ok(
    mobileSigninTarget?.height >= 44 && mobileSigninTarget.width >= 44,
    'Mobile sign-in action has a usable touch target',
  );

  const admin = await context(await signin('admin'));
  await capture(admin, '/admin', 'admin-overview', 'Marketplace overview');
  await capture(admin, '/admin/users', 'admin-users', 'Manage users');
  await capture(admin, '/admin/products', 'admin-products', 'Manage products');
  await capture(admin, '/admin/invoices', 'admin-invoices', 'Invoices');
  await capture(admin, '/admin', 'admin-mobile', 'Marketplace overview', true);

  const seller = await context(await signin('seller'));
  await capture(seller, '/seller', 'seller-listings', 'My listings');
  await capture(seller, '/seller/new', 'seller-form', 'Create a listing');
  await capture(seller, '/seller', 'seller-mobile', 'My listings', true);

  const bidder = await context(await signin('bidder'));
  await capture(bidder, '/bidder/invoices', 'bidder-invoices', 'Invoices');
  await capture(bidder, '/messages', 'messages-desktop', 'Messages');
  await capture(bidder, '/notifications', 'notifications-mobile', 'Notifications', true);
  await capture(bidder, '/account', 'account-mobile', 'My profile', true);

  const motion = await bidder.evaluate(() => ({
    reduced: matchMedia('(prefers-reduced-motion: reduce)').matches,
    animations: document.getAnimations().filter((animation) => animation.playState === 'running')
      .length,
  }));
  assert.equal(motion.reduced, true);
  assert.equal(
    motion.animations,
    0,
    'Reduced-motion mode should not leave ambient animations running',
  );
  const fallbackMedia = await bidder.context().newCDPSession(bidder);
  await fallbackMedia.send('Emulation.setEmulatedMedia', {
    features: [
      { name: 'prefers-reduced-motion', value: 'reduce' },
      { name: 'prefers-reduced-transparency', value: 'reduce' },
    ],
  });
  const fallback = await bidder.locator('.header-content').evaluate((element) => {
    const style = getComputedStyle(element);
    return { backdrop: style.backdropFilter, background: style.backgroundColor };
  });
  assert.equal(fallback.backdrop, 'none', 'Reduced transparency uses a solid material');
  assert.deepEqual(failures, [], 'No frontend exceptions or API 5xx responses');
  await writeFile(
    resolve(artifacts, 'review.json'),
    JSON.stringify(
      { captures, material, catalogChecks, mobileLoginInputs, motion, fallback, failures },
      null,
      2,
    ),
  );
  console.log(`Visual review passed (${captures.length} views).`);
} catch (error) {
  await activePage
    ?.screenshot({ path: resolve(artifacts, 'failure.png'), fullPage: true })
    .catch(() => {});
  console.error(`Visual review failed: ${error.message}`);
  if (failures.length) console.error(failures.join('\n'));
  process.exitCode = 1;
} finally {
  for (const item of contexts) await item.close();
  await browser.close();
}
