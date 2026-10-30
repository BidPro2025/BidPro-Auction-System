import axios from 'axios';
import { assetUrl } from '../lib/apiUrl';
import { ApiError, apiFailure } from '../lib/apiError';

const configuredBase = import.meta.env.VITE_API_BASE_URL?.trim();
const client = axios.create({ baseURL: configuredBase || '/api', timeout: 15000 });
export const imageUrl = (path) => assetUrl(path, configuredBase, window.location.origin);

export { ApiError } from '../lib/apiError';

export async function request(path, options = {}) {
  const { method = 'GET', body, auth = true, responseType, signal } = options;
  const token = auth ? localStorage.getItem('token') : null;
  try {
    const response = await client({
      url: path,
      method,
      data: body,
      responseType,
      signal,
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (responseType === 'blob') return response.data;
    const envelope = response.data;
    if (envelope?.success === false)
      throw new ApiError(envelope.message || 'Request failed.', response.status, {
        requestId: envelope.requestId,
        errorCode: envelope.errorCode,
      });
    return envelope?.data ?? null;
  } catch (error) {
    if (axios.isCancel(error)) throw error;
    if (error instanceof ApiError) throw error;
    const status = error.response?.status;
    if (status === 401 && token && localStorage.getItem('token') === token)
      window.dispatchEvent(new Event('bidpro:session-expired'));
    throw await apiFailure(error);
  }
}

export async function downloadInvoice(invoiceID) {
  const blob = await request(`/invoices/${encodeURIComponent(invoiceID)}/download`, {
    responseType: 'blob',
  });
  if (!(blob instanceof Blob) || (await blob.slice(0, 5).text()) !== '%PDF-')
    throw new ApiError('The invoice document was not returned correctly. Please try again.');
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `BidPro-invoice-${invoiceID}.pdf`;
  anchor.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
