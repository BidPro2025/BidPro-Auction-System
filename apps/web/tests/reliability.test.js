import test from 'node:test';
import assert from 'node:assert/strict';
import { createSingleFlight } from '../src/shared/lib/singleFlight.js';
import { apiFailure } from '../src/shared/lib/apiError.js';
import { safeReturnTo } from '../src/shared/lib/navigation.js';
import { createCheckoutLoader } from '../src/features/payments/checkoutLoader.js';

test('same-tick and in-flight repeated submissions share one result and can retry later', async () => {
  const run = createSingleFlight();
  let calls = 0;
  let release;
  const operation = async () => {
    calls++;
    return new Promise((resolve) => {
      release = resolve;
    });
  };
  const first = run(operation);
  const duplicate = run(operation);
  assert.equal(first, duplicate);
  await Promise.resolve();
  assert.equal(calls, 1);
  assert.equal(run(operation), first);
  release('saved');
  assert.equal(await first, 'saved');
  assert.equal(await run(() => 'retry'), 'retry');
});

test('failed and synchronously throwing submissions release the operation lock', async () => {
  const run = createSingleFlight();
  await assert.rejects(
    run(() => {
      throw new Error('rejected');
    }),
    /rejected/,
  );
  assert.equal(await run(() => 2), 2);
});

test('supportable errors retain safe request references and decode JSON from failed PDF downloads', async () => {
  const error = await apiFailure({
    response: {
      status: 503,
      data: new Blob([
        JSON.stringify({
          message: 'Temporarily unavailable',
          requestId: 'support-123',
          errorCode: 'UNAVAILABLE',
        }),
      ]),
      headers: {},
    },
  });
  assert.equal(error.status, 503);
  assert.equal(error.requestId, 'support-123');
  assert.equal(error.errorCode, 'UNAVAILABLE');
  assert.match(error.message, /Reference: support-123/);
  const invalid = await apiFailure({
    response: {
      status: 502,
      data: new Blob(['<html>proxy failed</html>']),
      headers: { 'x-request-id': 'edge-456' },
    },
  });
  assert.equal(invalid.requestId, 'edge-456');
  assert.doesNotMatch(invalid.message, /html|proxy failed/);
});

test('rate limits expose retry timing and timeout errors have actionable messages', async () => {
  const limited = await apiFailure({
    response: {
      status: 429,
      data: { message: 'Please wait', errorCode: 'RATE_LIMITED' },
      headers: { 'retry-after': '27' },
    },
  });
  assert.equal(limited.retryAfter, 27);
  assert.match(limited.message, /27 seconds/);
  const timeout = await apiFailure({ code: 'ECONNABORTED' });
  assert.match(timeout.message, /too long/);
});

test('login preserves internal query/hash deep links and rejects external or malformed redirects', () => {
  const internal = '/bidder/invoices?state=pending#invoice-123';
  assert.equal(safeReturnTo(internal, '/bidder'), internal);
  for (const path of [
    'https://evil.invalid',
    '//evil.invalid',
    '/\\evil.invalid',
    '/%5cevil.invalid',
    '/%2f%2fevil.invalid',
    '/%00evil',
    '/%zz',
    null,
  ]) {
    assert.equal(safeReturnTo(path, '/bidder'), '/bidder');
  }
});

test('checkout loader shares an attempt and retries both network and invalid-script failures', async () => {
  const scripts = [];
  let available = false;
  const document = () => ({
    createElement: () => ({
      remove() {
        this.removed = true;
      },
    }),
    head: { appendChild: (script) => scripts.push(script) },
  });
  const load = createCheckoutLoader({ ready: () => available, document });
  const first = load();
  assert.equal(load(), first);
  scripts[0].onerror();
  await assert.rejects(first, /Cannot load/);
  assert.equal(scripts[0].removed, true);
  const second = load();
  scripts[1].onload();
  await assert.rejects(second, /did not load/);
  available = true;
  await load();
  assert.equal(scripts.length, 2);
});

test('checkout script timeout removes a hung load and permits a fresh attempt', async () => {
  const scripts = [];
  const document = () => ({
    createElement: () => ({
      remove() {
        this.removed = true;
      },
    }),
    head: { appendChild: (script) => scripts.push(script) },
  });
  const load = createCheckoutLoader({ ready: () => false, document, timeoutMs: 5 });
  await assert.rejects(load(), /too long/);
  assert.equal(scripts[0].removed, true);
  const retry = load();
  scripts[1].onerror();
  await assert.rejects(retry, /Cannot load/);
});
