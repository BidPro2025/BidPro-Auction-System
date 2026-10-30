import { useEffect } from 'react';
import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { CatalogPage } from '../catalog/CatalogPages';
import InvoicesPage from '../invoices/InvoicesPage';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { request } from '../../shared/api/client';
import { auctionState, dateTime, money } from '../../shared/lib/format';
import { Badge, Feedback, PageHeading, ResourceState } from '../../shared/components/UI';

function MyBidsPage() {
  const { user } = useAuth();
  const resource = useResource(user.status === 'APPROVED' ? '/bids/mine' : null);
  const products = useResource('/products');
  const mutation = useMutation();
  const reloadBids = resource.reload;
  const reloadProducts = products.reload;
  useEffect(() => {
    const id = setInterval(() => {
      reloadBids();
      reloadProducts();
    }, 30000);
    return () => clearInterval(id);
  }, [reloadBids, reloadProducts]);
  const settle = async (productID) => {
    const result = await mutation.run(
      () => request(`/invoices?productID=${encodeURIComponent(productID)}`, { method: 'POST' }),
      'Invoice generated. Download it from Invoices.',
    );
    if (result.ok) {
      resource.reload();
      products.reload();
    }
  };
  return (
    <>
      <PageHeading
        eyebrow="BIDDER WORKSPACE"
        title="My bids"
        action={
          <button
            className="btn btn-outline-secondary"
            onClick={() => {
              resource.reload();
              products.reload();
            }}
          >
            Refresh
          </button>
        }
      >
        Track your bids and generate an invoice when you win.
      </PageHeading>
      <Feedback mutation={mutation} />
      <ResourceState
        resource={resource}
        isEmpty={!resource.data?.length}
        empty="You have not placed any bids yet"
      >
        <div className="surface table-responsive">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Item</th>
                <th scope="col">Your bid</th>
                <th scope="col">Current highest</th>
                <th scope="col">Auction</th>
                <th scope="col">Placed</th>
                <th scope="col">Next step</th>
              </tr>
            </thead>
            <tbody>
              {(resource.data || [])
                .slice()
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                .map((bid) => {
                  const product = (products.data || []).find(
                    (item) => item.productID === bid.productID,
                  );
                  const state = product ? auctionState(product) : 'Unavailable';
                  const winning = product && Number(bid.amount) === Number(product.currentBid);
                  return (
                    <tr key={bid.bidID}>
                      <td>
                        <Link to={`/products/${bid.productID}`}>
                          {product?.title || bid.productTitle || bid.productID}
                        </Link>
                      </td>
                      <td className="fw-semibold">{money(bid.amount)}</td>
                      <td>
                        {product ? money(product.currentBid) : '—'}
                        {winning && (
                          <small className="d-block text-success">
                            {state === 'Live'
                              ? 'Leading bid'
                              : state === 'Ended' || state === 'Sold'
                                ? 'Winning bid'
                                : ''}
                          </small>
                        )}
                      </td>
                      <td>
                        <Badge value={state} />
                      </td>
                      <td>{dateTime(bid.timestamp)}</td>
                      <td>
                        {state === 'Live' ? (
                          <Link
                            className="btn btn-outline-primary btn-sm"
                            to={`/products/${bid.productID}`}
                          >
                            Increase bid
                          </Link>
                        ) : state === 'Ended' && winning ? (
                          <button
                            className="btn btn-primary btn-sm"
                            disabled={mutation.pending}
                            onClick={() => settle(bid.productID)}
                          >
                            Generate invoice
                          </button>
                        ) : state === 'Sold' && winning ? (
                          <Link className="btn btn-outline-primary btn-sm" to="/bidder/invoices">
                            View invoice
                          </Link>
                        ) : (
                          '—'
                        )}
                      </td>
                    </tr>
                  );
                })}
            </tbody>
          </table>
        </div>
      </ResourceState>
    </>
  );
}
export default function BidderPages() {
  return (
    <Routes>
      <Route index element={<CatalogPage title="Your next bid starts here" />} />
      <Route path="bids" element={<MyBidsPage />} />
      <Route path="invoices" element={<InvoicesPage />} />
      <Route path="*" element={<Navigate to="/bidder" replace />} />
    </Routes>
  );
}
