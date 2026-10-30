import { useRef, useState } from 'react';
import { request } from '../../shared/api/client';
import { Notice } from '../../shared/components/UI';
import { money } from '../../shared/lib/format';
import Dialog from '../../shared/components/Dialog';
import { createSingleFlight } from '../../shared/lib/singleFlight';
import { createCheckoutLoader } from './checkoutLoader';

const loadCheckout = createCheckoutLoader();

export default function RazorpayButton({ invoice, configuration, user, onUpdate }) {
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('info');
  const [error, setError] = useState('');
  const [localOrder, setLocalOrder] = useState(null);
  const operation = useRef(createSingleFlight());
  const trigger = useRef(null);
  const pay = () =>
    operation.current(async () => {
      setBusy(true);
      setError('');
      setMessage('');
      try {
        const order = await request('/payments/orders', {
          method: 'POST',
          body: { invoiceID: invoice.invoiceID },
        });
        if (order.mode === 'local-test') {
          setLocalOrder(order);
          setBusy(false);
          return;
        }
        await loadCheckout();
        const checkout = new window.Razorpay({
          key: order.keyID,
          amount: order.amount,
          currency: order.currency,
          name: order.name,
          description: `Auction invoice ${invoice.invoiceID}`,
          order_id: order.orderID,
          prefill: {
            name: `${user.firstName} ${user.lastName}`,
            email: user.email,
            contact: user.phone || '',
          },
          theme: { color: '#174c3c' },
          handler: async (response) => {
            setBusy(true);
            try {
              const result = await request('/payments/verify', {
                method: 'POST',
                body: {
                  invoiceID: invoice.invoiceID,
                  razorpay_order_id: response.razorpay_order_id,
                  razorpay_payment_id: response.razorpay_payment_id,
                  razorpay_signature: response.razorpay_signature,
                },
              });
              setMessage(
                result.status === 'CAPTURED'
                  ? 'Payment received and verified.'
                  : 'Payment authorized. We are waiting for the provider to confirm capture. Refresh your invoices shortly.',
              );
              setMessageType(result.status === 'CAPTURED' ? 'success' : 'info');
              onUpdate();
            } catch (failure) {
              setError(failure.message);
            } finally {
              setBusy(false);
            }
          },
          modal: {
            ondismiss: () => {
              setBusy(false);
              setMessage('Checkout closed. You can retry using the same payment order.');
              setMessageType('info');
              onUpdate();
            },
          },
        });
        checkout.on('payment.failed', () => {
          setError('The payment was not completed. You can retry securely.');
          setBusy(false);
          onUpdate();
        });
        checkout.open();
      } catch (failure) {
        setError(failure.message);
        setBusy(false);
      }
    });
  const localOutcome = (outcome) =>
    operation.current(async () => {
      setBusy(true);
      setError('');
      try {
        const result = await request('/payments/local-test', {
          method: 'POST',
          body: { invoiceID: invoice.invoiceID, outcome },
        });
        setMessage(
          result.status === 'CAPTURED'
            ? 'Local test payment completed. No money was transferred.'
            : 'Local test payment failed. No money was transferred. You can retry.',
        );
        setMessageType(result.status === 'CAPTURED' ? 'success' : 'warning');
        setLocalOrder(null);
        onUpdate();
      } catch (failure) {
        setError(failure.message);
      } finally {
        setBusy(false);
      }
    });
  return (
    <div className="mt-2">
      <button
        ref={trigger}
        className="btn btn-primary btn-sm"
        disabled={busy || !configuration?.enabled}
        title={configuration?.message}
        onClick={pay}
      >
        {busy
          ? 'Confirming payment...'
          : configuration?.mode === 'local-test'
            ? 'Open local test checkout'
            : configuration?.mode === 'test'
              ? 'Pay in test mode'
              : 'Pay securely'}
      </button>
      {configuration?.mode === 'test' && configuration.enabled && (
        <small className="d-block text-secondary mt-1">Test mode (no real charge)</small>
      )}
      {configuration?.mode === 'local-test' && (
        <small className="d-block text-secondary mt-1">
          Local test only (no money transferred)
        </small>
      )}
      {localOrder && (
        <Dialog
          labelledBy={`local-payment-${invoice.invoiceID}`}
          describedBy={`local-payment-description-${invoice.invoiceID}`}
          busy={busy}
          restoreFocusRef={trigger}
          onClose={() => setLocalOrder(null)}
        >
          <div className="modal-header">
            <h2 id={`local-payment-${invoice.invoiceID}`} className="modal-title fs-5">
              Local test checkout
            </h2>
            <button
              className="btn-close"
              aria-label="Close local checkout"
              disabled={busy}
              onClick={() => setLocalOrder(null)}
            />
          </div>
          <div className="modal-body">
            <p id={`local-payment-description-${invoice.invoiceID}`}>
              <strong>No money is transferred.</strong> This checkout tests the invoice payment flow
              locally.
            </p>
            <p className="fs-4 fw-semibold">{money(localOrder.amount / 100)}</p>
            <p className="small text-secondary">
              Choose a test outcome. No card or bank details are required.
            </p>
            <Notice>{error}</Notice>
          </div>
          <div className="modal-footer">
            <button
              className="btn btn-outline-secondary"
              disabled={busy}
              onClick={() => setLocalOrder(null)}
            >
              Cancel
            </button>
            <button
              className="btn btn-outline-danger"
              disabled={busy}
              onClick={() => localOutcome('FAILURE')}
            >
              Simulate failed payment
            </button>
            <button
              className="btn btn-primary"
              disabled={busy}
              onClick={() => localOutcome('SUCCESS')}
            >
              Simulate successful payment
            </button>
          </div>
        </Dialog>
      )}
      <Notice>{localOrder ? '' : error}</Notice>
      <Notice type={messageType}>{message}</Notice>
    </div>
  );
}
