import assert from 'node:assert/strict';
import { setTimeout as delay } from 'node:timers/promises';
import { deflateSync } from 'node:zlib';

// Run against a local API with the development seed enabled. Fixtures have a
// unique name on every run so the check also works with the persistent dev DB.
const base = (process.env.API_URL || 'http://localhost:8080').replace(/\/$/, '');
const password = process.env.SMOKE_PASSWORD || 'BidPro123!';
const suffix = Date.now().toString(36);
let checks = 0;

async function request(path, { method = 'GET', token, body, statuses = [200, 201], form } = {}) {
  const response = await fetch(`${base}/api${path}`, {
    method,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: form || (body === undefined ? undefined : JSON.stringify(body)),
    signal: AbortSignal.timeout(15000),
  });
  const text = await response.text();
  let payload;
  try {
    payload = JSON.parse(text);
  } catch {
    payload = text;
  }
  assert.ok(
    statuses.includes(response.status),
    `${method} ${path}: expected ${statuses.join('/')} but received ${response.status}: ${text.slice(0, 1000)}`,
  );
  if (response.ok)
    assert.equal(payload.success, true, `${method} ${path}: success envelope required`);
  checks++;
  return payload.data;
}

function verify(condition, message) {
  assert.ok(condition, message);
  checks++;
}

function localDate(milliseconds) {
  const date = new Date(milliseconds);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 19);
}

function fixturePng() {
  const chunk = (name, bytes) => {
    const content = Buffer.concat([Buffer.from(name), bytes]);
    let crc = 0xffffffff;
    for (const byte of content) {
      crc ^= byte;
      for (let bit = 0; bit < 8; bit++) crc = (crc >>> 1) ^ (crc & 1 ? 0xedb88320 : 0);
    }
    const size = Buffer.alloc(4);
    size.writeUInt32BE(bytes.length);
    const checksum = Buffer.alloc(4);
    checksum.writeUInt32BE((crc ^ 0xffffffff) >>> 0);
    return Buffer.concat([size, content, checksum]);
  };
  const header = Buffer.alloc(13);
  header.writeUInt32BE(1, 0);
  header.writeUInt32BE(1, 4);
  header[8] = 8;
  header[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(Buffer.from([0, 255, 255, 255, 255]))),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

async function signin(role) {
  const email = process.env[`SMOKE_${role.toUpperCase()}_EMAIL`] || `${role}@bidpro.local`;
  const session = await request('/auth/signin', { method: 'POST', body: { email, password } });
  verify(
    typeof session.token === 'string' && session.token.split('.').length === 3,
    `${role}: signed token returned`,
  );
  verify(session.user?.role === role.toUpperCase(), `${role}: correct authenticated role returned`);
  return session;
}

async function main() {
  await request('/health');
  await request('/categories');
  await request('/products');
  await request('/products', { method: 'POST', body: {}, statuses: [401] });
  await request('/auth/validate', { statuses: [401] });
  await request('/auth/validate', { token: 'invalid.token.value', statuses: [401] });
  await request('/auth/signin', {
    method: 'POST',
    body: { email: 'admin@bidpro.local', password: 'incorrect-password' },
    statuses: [401],
  });
  await request('/auth/signup', {
    method: 'POST',
    body: {
      firstName: 'Smoke',
      lastName: 'Admin',
      username: `admin-${suffix}`,
      email: `admin-${suffix}@example.test`,
      password,
      role: 'ADMIN',
    },
    statuses: [400, 403],
  });

  const admin = await signin('admin');
  const seller = await signin('seller');
  const bidder = await signin('bidder');
  await request('/auth/validate', { token: bidder.token });
  await request('/admins/dashboard', { token: admin.token });
  await request('/admins/users', { token: bidder.token, statuses: [403] });
  await request('/admins/categories', {
    method: 'POST',
    token: seller.token,
    body: { name: 'Forbidden' },
    statuses: [403],
  });

  const category = await request('/admins/categories', {
    method: 'POST',
    token: admin.token,
    body: {
      name: `Smoke ${suffix}`,
      description: 'Created by the local end-to-end API check.',
      visibility: true,
      displayOrder: 99,
    },
  });
  verify(category.categoryID, 'Category receives a persistent identifier');
  const categories = await request('/categories');
  verify(
    categories.some((item) => item.categoryID === category.categoryID),
    'Created visible category is publicly listed',
  );

  const now = Date.now();
  const end = now + 18000;
  const product = await request('/products', {
    method: 'POST',
    token: seller.token,
    body: {
      title: `Smoke auction ${suffix}`,
      description: 'End-to-end verification listing.',
      categoryID: category.categoryID,
      auctionStartDate: localDate(now - 60000),
      auctionEndDate: localDate(end),
      startingPrice: 100,
      incrementGap: 10,
      productCondition: 'USED',
    },
  });
  verify(
    product.productID && product.productStatus === 'PENDING',
    'Seller listing starts pending review',
  );
  verify(product.sellerID === seller.user.userID, 'Listing uses authenticated seller identity');
  await request(`/products/${product.productID}`, {
    method: 'DELETE',
    token: bidder.token,
    statuses: [403],
  });
  await request('/bids', {
    method: 'POST',
    token: bidder.token,
    body: { productID: product.productID, amount: 100 },
    statuses: [400, 403, 409],
  });
  // Upload while the listing is pending; approval makes listing data immutable.
  const png = fixturePng();
  const imageForm = new FormData();
  imageForm.append('file', new Blob([png], { type: 'image/png' }), 'smoke.png');
  imageForm.append('productID', product.productID);
  const imageUrl = await request('/file-upload/product-image', {
    method: 'POST',
    token: seller.token,
    form: imageForm,
  });
  verify(typeof imageUrl === 'string' && imageUrl.length > 0, 'Upload returns an image URL');
  const imageResponse = await fetch(new URL(imageUrl, base), {
    signal: AbortSignal.timeout(15000),
  });
  verify(
    imageResponse.ok && (await imageResponse.arrayBuffer()).byteLength > 0,
    'Uploaded image is retrievable',
  );

  await request('/admins/products/' + product.productID + '/status', {
    method: 'PUT',
    token: admin.token,
    body: { status: 'APPROVED' },
  });
  const approved = await request(`/products/${product.productID}`);
  verify(
    approved.productStatus === 'APPROVED' && approved.auctionID,
    'Approval creates a linked auction',
  );
  verify(approved.imageUrls.includes(imageUrl), 'Listing includes the uploaded image');
  await request(`/auctions/${approved.auctionID}/register`, {
    method: 'POST',
    token: bidder.token,
  });

  await request('/bids', {
    method: 'POST',
    token: bidder.token,
    body: { productID: product.productID, amount: -1 },
    statuses: [400, 409],
  });
  const bid = await request('/bids', {
    method: 'POST',
    token: bidder.token,
    body: { productID: product.productID, amount: 100 },
  });
  verify(
    bid.bidderID === bidder.user.userID && Number(bid.amount) === 100,
    'Bid belongs to authenticated bidder',
  );
  await request('/bids', {
    method: 'POST',
    token: bidder.token,
    body: { productID: product.productID, amount: 105 },
    statuses: [400, 409],
  });
  await request('/bids', {
    method: 'POST',
    token: bidder.token,
    body: { productID: product.productID, amount: 110 },
  });
  const ownBids = await request('/bids/mine', { token: bidder.token });
  verify(
    ownBids.some((item) => item.productID === product.productID && Number(item.amount) === 110),
    'Bidder history includes latest bid',
  );
  await request(`/invoices?productID=${product.productID}`, {
    method: 'POST',
    token: bidder.token,
    statuses: [400, 409],
  });

  const waitMilliseconds = Math.max(0, end + 1200 - Date.now());
  if (waitMilliseconds) {
    console.log(`Waiting ${Math.ceil(waitMilliseconds / 1000)}s for the test auction to close...`);
    await delay(waitMilliseconds);
  }
  await request('/bids', {
    method: 'POST',
    token: bidder.token,
    body: { productID: product.productID, amount: 120 },
    statuses: [400, 409],
  });
  const invoice = await request(`/invoices?productID=${product.productID}`, {
    method: 'POST',
    token: bidder.token,
  });
  verify(invoice.invoiceID && Number(invoice.amount) === 110, 'Invoice uses the winning amount');
  verify(
    invoice.buyerID === bidder.user.userID && invoice.sellerID === seller.user.userID,
    'Invoice parties match winner and seller',
  );
  const duplicate = await request(`/invoices?productID=${product.productID}`, {
    method: 'POST',
    token: bidder.token,
  });
  verify(duplicate.invoiceID === invoice.invoiceID, 'Settlement is idempotent');
  for (const account of [seller, bidder]) {
    const invoices = await request('/invoices/mine', { token: account.token });
    verify(
      invoices.some((item) => item.invoiceID === invoice.invoiceID),
      'Invoice is visible to its authenticated party',
    );
  }
  const pdf = await fetch(`${base}/api/invoices/${invoice.invoiceID}/download`, {
    headers: { Authorization: `Bearer ${bidder.token}` },
    signal: AbortSignal.timeout(15000),
  });
  verify(
    pdf.ok && pdf.headers.get('content-type')?.includes('application/pdf'),
    'Invoice download returns PDF',
  );
  verify(
    Buffer.from(await pdf.arrayBuffer())
      .subarray(0, 5)
      .toString() === '%PDF-',
    'Invoice download contains a PDF document',
  );

  console.log(`API smoke test passed (${checks} checks). Fixture: ${product.title}`);
}

main().catch((error) => {
  console.error(`API smoke test failed after ${checks} checks: ${error.message}`);
  process.exitCode = 1;
});
