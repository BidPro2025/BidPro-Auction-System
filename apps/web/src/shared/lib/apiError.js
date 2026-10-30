export class ApiError extends Error {
  constructor(message, status, { requestId, errorCode, retryAfter, fieldErrors } = {}) {
    super(requestId && status >= 500 ? `${message} Reference: ${requestId}.` : message);
    this.name = 'ApiError';
    this.status = status;
    this.requestId = requestId || null;
    this.errorCode = errorCode || null;
    this.retryAfter = retryAfter || null;
    this.fieldErrors = fieldErrors || {};
  }
}

export async function apiFailure(error) {
  const response = error.response;
  let data = response?.data;
  if (typeof Blob !== 'undefined' && data instanceof Blob) {
    try {
      data = JSON.parse(await data.text());
    } catch {
      data = null;
    }
  }
  const header = (name) =>
    response?.headers?.get?.(name) ?? response?.headers?.[name.toLowerCase()];
  const requestId = data?.requestId || header('X-Request-ID');
  const retryHeader = header('Retry-After');
  const retryAfter = retryHeader && /^\d+$/.test(String(retryHeader)) ? Number(retryHeader) : null;
  const fallback =
    response?.status === 429
      ? 'Too many requests. Please wait before trying again.'
      : error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT'
        ? 'The server took too long to respond. Please try again.'
        : response
          ? 'The request could not be completed.'
          : 'Cannot reach the server. Check your connection and try again.';
  const message =
    typeof data?.message === 'string' && data.message.trim() ? data.message : fallback;
  return new ApiError(
    response?.status === 429 && retryAfter
      ? `${message} Try again in ${retryAfter} seconds.`
      : message,
    response?.status,
    { requestId, errorCode: data?.errorCode, retryAfter, fieldErrors: data?.fieldErrors },
  );
}
