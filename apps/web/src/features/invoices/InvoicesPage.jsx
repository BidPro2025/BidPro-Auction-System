import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { downloadInvoice, request } from '../../shared/api/client';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { dateTime, money } from '../../shared/lib/format';
import { Badge, Feedback, Notice, PageHeading, ResourceState } from '../../shared/components/UI';
import RazorpayButton from '../payments/RazorpayButton';

export default function InvoicesPage() {
  const { user } = useAuth();
  const resource = useResource(user.status === 'APPROVED' ? '/invoices/mine' : null);
  const mutation = useMutation();
  const paymentConfig = useResource(user.status === 'APPROVED' ? '/payments/config' : null);
  const ownsUnpaid = (resource.data || []).some(
    (invoice) => invoice.buyerID === user.userID && invoice.paymentStatus !== 'PAID',
  );
  const updatePayment = async (invoice, paymentStatus) => {
    const result = await mutation.run(
      () =>
        request(`/invoices/${invoice.invoiceID}/payment-status`, {
          method: 'PUT',
          body: { paymentStatus },
        }),
      'Payment record updated.',
    );
    if (result.ok) resource.reload();
  };
  return (
    <>
      <PageHeading
        eyebrow="YOUR RECORDS"
        title="Invoices"
        action={
          <button className="btn btn-outline-secondary" onClick={resource.reload}>
            Refresh
          </button>
        }
      >
        Pay securely and download invoices for your completed purchases and sales.
      </PageHeading>
      <Feedback mutation={mutation} />
      {ownsUnpaid && paymentConfig.data && (
        <Notice type={paymentConfig.data.enabled ? 'info' : 'warning'}>
          {paymentConfig.data.message}
        </Notice>
      )}
      {ownsUnpaid && paymentConfig.error && (
        <Notice>
          {paymentConfig.error}{' '}
          <button className="btn btn-outline-danger btn-sm ms-2" onClick={paymentConfig.reload}>
            Retry payment settings
          </button>
        </Notice>
      )}
      <ResourceState resource={resource} isEmpty={!resource.data?.length} empty="No invoices yet">
        <div className="surface table-responsive">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Item</th>
                <th scope="col">Buyer / seller</th>
                <th scope="col">Amount</th>
                <th scope="col">Date</th>
                <th scope="col">Payment record</th>
                <th scope="col">Invoice</th>
              </tr>
            </thead>
            <tbody>
              {(resource.data || []).map((invoice) => (
                <tr key={invoice.invoiceID}>
                  <td>
                    <Link to={`/products/${invoice.productID}`}>
                      {invoice.productTitle || invoice.productID}
                    </Link>
                    <small className="d-block text-secondary">{invoice.invoiceID}</small>
                  </td>
                  <td>
                    {invoice.buyerID === user.userID
                      ? `Seller: ${invoice.sellerName || invoice.sellerID}`
                      : `Buyer: ${invoice.buyerName || invoice.buyerID}`}
                  </td>
                  <td className="fw-semibold">{money(invoice.amount)}</td>
                  <td>{dateTime(invoice.invoiceDate)}</td>
                  <td>
                    {user.role === 'ADMIN' &&
                    invoice.paymentStatus !== 'PAID' &&
                    !invoice.hasOnlinePaymentOrder &&
                    invoice.paymentSource !== 'RAZORPAY' ? (
                      <select
                        className="form-select form-select-sm"
                        aria-label={`Payment status for ${invoice.invoiceID}`}
                        value={invoice.paymentStatus}
                        disabled={mutation.pending}
                        onChange={(event) => updatePayment(invoice, event.target.value)}
                      >
                        {['PENDING', 'PAID', 'FAILED'].map((status) => (
                          <option key={status} value={status}>
                            {status.charAt(0) + status.slice(1).toLowerCase()}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <Badge value={invoice.paymentStatus} />
                    )}
                    <small className="d-block text-secondary mt-1">
                      {invoice.paymentSource === 'LOCAL_TEST'
                        ? 'Local test payment (no money transferred)'
                        : invoice.paymentSource === 'RAZORPAY'
                          ? invoice.paymentMode === 'test'
                            ? 'Test payment verified by Razorpay (no real charge)'
                            : 'Verified by Razorpay'
                          : invoice.paymentSource === 'MANUAL'
                            ? 'Recorded manually by administrator'
                            : invoice.hasOnlinePaymentOrder
                              ? 'Online payment order created'
                              : 'Awaiting payment'}
                    </small>
                    {invoice.paymentReference && (
                      <small className="d-block text-secondary">{invoice.paymentReference}</small>
                    )}
                    {invoice.buyerID === user.userID && invoice.paymentStatus !== 'PAID' && (
                      <RazorpayButton
                        invoice={invoice}
                        configuration={paymentConfig.data}
                        user={user}
                        onUpdate={resource.reload}
                      />
                    )}
                  </td>
                  <td>
                    <button
                      className="btn btn-outline-primary btn-sm"
                      disabled={mutation.pending}
                      onClick={() =>
                        mutation.run(
                          () => downloadInvoice(invoice.invoiceID),
                          'Invoice downloaded.',
                        )
                      }
                    >
                      <i className="bi bi-download me-1" />
                      Download PDF
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </ResourceState>
    </>
  );
}
