import test from 'node:test';
import assert from 'node:assert/strict';
import {
  auctionState,
  minimumBid,
  canBid,
  canSell,
  dashboardPath,
  localDateTime,
} from '../src/shared/lib/format.js';

test('auction state respects moderation and exact start/end boundaries', () => {
  const now = new Date('2026-10-24T12:00:00Z').getTime();
  const product = {
    productStatus: 'APPROVED',
    auctionStartDate: '2026-10-24T12:00:00Z',
    auctionEndDate: '2026-10-24T13:00:00Z',
  };
  assert.equal(auctionState(product, now - 1), 'Upcoming');
  assert.equal(auctionState(product, now), 'Live');
  assert.equal(auctionState(product, now + 3600000), 'Ended');
  assert.equal(auctionState({ ...product, productStatus: 'PENDING' }, now), 'Pending');
  assert.equal(auctionState({ ...product, productStatus: 'SOLD' }, now), 'Sold');
});
test('minimum bid starts at starting price and advances from highest bid', () => {
  const product = { startingPrice: 100, incrementGap: 10 };
  assert.equal(minimumBid(product), 100);
  assert.equal(minimumBid(product, [{ amount: 110 }, { amount: 150 }, { amount: 130 }]), 160);
  assert.equal(minimumBid({ startingPrice: 0.1, incrementGap: 0.2 }, [{ amount: 0.1 }]), 0.3);
});
test('role routing and participation support combined accounts without exposing admin routes', () => {
  assert.equal(dashboardPath('ADMIN'), '/admin');
  assert.equal(dashboardPath('BOTH'), '/seller');
  assert.equal(dashboardPath('BIDDER'), '/bidder');
  assert.equal(canBid({ role: 'BOTH' }), true);
  assert.equal(canSell({ role: 'BOTH' }), true);
  assert.equal(canBid({ role: 'SELLER' }), false);
  assert.equal(canSell({ role: 'BIDDER' }), false);
  assert.equal(canBid(null), false);
});
test('datetime inputs retain the local wall-clock time', () => {
  const date = new Date(2026, 9, 24, 15, 30);
  assert.equal(localDateTime(date), '2026-10-24T15:30');
});
