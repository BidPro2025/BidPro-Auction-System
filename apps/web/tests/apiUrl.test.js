import test from 'node:test';
import assert from 'node:assert/strict';
import { assetUrl } from '../src/shared/lib/apiUrl.js';

test('product images follow a separately hosted API origin', () => {
  assert.equal(
    assetUrl('/api/images/item.png', 'https://api.example.test/api', 'https://web.example.test'),
    'https://api.example.test/api/images/item.png',
  );
  assert.equal(
    assetUrl('/api/images/item.png', '/api', 'https://web.example.test'),
    'https://web.example.test/api/images/item.png',
  );
});

test('same-origin defaults and existing absolute image URLs remain usable', () => {
  assert.equal(
    assetUrl('/api/images/item.png', '', 'https://web.example.test'),
    '/api/images/item.png',
  );
  assert.equal(
    assetUrl(
      'https://cdn.example.test/item.png',
      'https://api.example.test/api',
      'https://web.example.test',
    ),
    'https://cdn.example.test/item.png',
  );
});
