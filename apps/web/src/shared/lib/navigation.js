// Login return paths must stay inside this app, even if navigation state was forged.
export function safeReturnTo(value, fallback = '/') {
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//'))
    return fallback;
  try {
    const decoded = decodeURIComponent(value);
    if (
      decoded.startsWith('//') ||
      decoded.includes('\\') ||
      [...decoded].some(
        (character) => character.charCodeAt(0) < 32 || character.charCodeAt(0) === 127,
      )
    )
      return fallback;
    return new URL(value, 'https://bidpro.invalid').origin === 'https://bidpro.invalid'
      ? value
      : fallback;
  } catch {
    return fallback;
  }
}
