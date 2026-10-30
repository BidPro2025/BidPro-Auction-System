export const money = (value) =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(Number(value) || 0);
export const dateTime = (value) =>
  value && Number.isFinite(new Date(value).getTime())
    ? new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(
        new Date(value),
      )
    : 'Not scheduled';
export const label = (value) =>
  String(value || '')
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
export const dashboardPath = (role) =>
  role === 'ADMIN' ? '/admin' : role === 'SELLER' || role === 'BOTH' ? '/seller' : '/bidder';
export const canSell = (user) => ['SELLER', 'BOTH'].includes(user?.role);
export const canBid = (user) => ['BIDDER', 'BOTH'].includes(user?.role);
export const auctionState = (product, now = Date.now()) => {
  if (product.productStatus !== 'APPROVED') return label(product.productStatus);
  if (new Date(product.auctionStartDate).getTime() > now) return 'Upcoming';
  if (new Date(product.auctionEndDate).getTime() <= now) return 'Ended';
  return 'Live';
};
export const minimumBid = (product, bids = []) => {
  const highest = bids.reduce((max, bid) => Math.max(max, Number(bid.amount) || 0), 0);
  return highest
    ? Math.round((highest + Number(product.incrementGap)) * 100) / 100
    : Number(product.startingPrice);
};
export const localDateTime = (value) => {
  const date = new Date(value);
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 16);
};
