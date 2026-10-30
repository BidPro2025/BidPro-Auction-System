import { useState } from 'react';
import {
  Link,
  Navigate,
  Route,
  Routes,
  useLocation,
  useNavigate,
  useParams,
} from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import InvoicesPage from '../invoices/InvoicesPage';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { imageUrl, request } from '../../shared/api/client';
import { auctionState, dateTime, localDateTime, money } from '../../shared/lib/format';
import {
  Badge,
  Empty,
  Feedback,
  Field,
  Loading,
  Notice,
  PageHeading,
  ResourceState,
} from '../../shared/components/UI';

function ListingsPage() {
  const { user } = useAuth();
  const location = useLocation();
  const resource = useResource(user.status === 'APPROVED' ? '/products/mine' : null);
  const mutation = useMutation();
  const [query, setQuery] = useState('');
  const products = (resource.data || []).filter((product) =>
    product.title.toLowerCase().includes(query.toLowerCase()),
  );
  const allListings = resource.data || [];
  const listingMetrics = [
    [
      'broadcast',
      'Live auctions',
      allListings.filter((item) => auctionState(item) === 'Live').length,
      'Open for bids',
    ],
    [
      'hourglass-split',
      'Awaiting review',
      allListings.filter((item) => item.productStatus === 'PENDING').length,
      'Before their next chapter',
    ],
    [
      'bag-check',
      'Sold pieces',
      allListings.filter((item) => item.productStatus === 'SOLD').length,
      'A new home found',
    ],
    [
      'hammer',
      'Total bids',
      allListings.reduce((sum, item) => sum + Number(item.bidCount || 0), 0),
      'Across your collection',
    ],
  ];
  const waiting = allListings
    .filter((item) => item.productStatus === 'PENDING')
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0];
  const remove = async (product) => {
    if (!window.confirm(`Delete the pending listing “${product.title}”?`)) return;
    const result = await mutation.run(
      () => request(`/products/${product.productID}`, { method: 'DELETE' }),
      'Listing deleted.',
    );
    if (result.ok) resource.reload();
  };
  const settle = async (product) => {
    const result = await mutation.run(
      () =>
        request(`/invoices?productID=${encodeURIComponent(product.productID)}`, { method: 'POST' }),
      'Invoice generated. You can download it from Invoices.',
    );
    if (result.ok) resource.reload();
  };
  return (
    <>
      <PageHeading
        eyebrow="SELLER WORKSPACE"
        title="My listings"
        action={
          <Link className="btn btn-primary" to="/seller/new">
            <i className="bi bi-plus-lg me-2" />
            New listing
          </Link>
        }
      >
        Track every item from review to the final sale.
      </PageHeading>
      <Notice type="success">{location.state?.success}</Notice>
      <Notice>{location.state?.error}</Notice>
      <Feedback mutation={mutation} />
      {resource.data && user.status === 'APPROVED' && (
        <>
          <div className="seller-summary">
            {listingMetrics.map(([icon, title, count, description]) => (
              <div className="seller-summary-card surface" key={title}>
                <span className="seller-summary-icon">
                  <i className={`bi bi-${icon}`} aria-hidden="true" />
                </span>
                <span>{title}</span>
                <strong>{count}</strong>
                <small>{description}</small>
              </div>
            ))}
          </div>
          {waiting && (
            <div className="seller-review-note">
              <span className="seller-review-note-icon">
                <i className="bi bi-shield-check" aria-hidden="true" />
              </span>
              <div>
                <span>THE NEXT PIECE IN YOUR COLLECTION</span>
                <p>
                  <strong>{waiting.title}</strong> is waiting for administrator review.
                </p>
              </div>
              <Link to={`/seller/edit/${waiting.productID}`}>
                Review details <i className="bi bi-arrow-up-right" aria-hidden="true" />
              </Link>
            </div>
          )}
        </>
      )}
      <div className="d-flex gap-3 mb-3 seller-list-toolbar">
        <input
          type="search"
          aria-label="Search my listings"
          className="form-control search-input"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search my listings"
        />
        <button className="btn btn-outline-secondary" onClick={resource.reload}>
          Refresh
        </button>
      </div>
      <ResourceState resource={resource} isEmpty={!products.length} empty="No listings yet">
        <div className="surface table-responsive">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Item</th>
                <th scope="col">Status</th>
                <th scope="col">Price / bids</th>
                <th scope="col">Auction ends</th>
                <th scope="col">Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.productID}>
                  <td>
                    <Link className="fw-semibold" to={`/products/${product.productID}`}>
                      {product.title}
                    </Link>
                    <small className="d-block text-secondary">{product.categoryName}</small>
                  </td>
                  <td>
                    <Badge value={auctionState(product)} />
                  </td>
                  <td>
                    {money(product.currentBid ?? product.startingPrice)}
                    <small className="d-block text-secondary">{product.bidCount || 0} bids</small>
                  </td>
                  <td>{dateTime(product.auctionEndDate)}</td>
                  <td>
                    <div className="d-flex flex-wrap gap-2">
                      {['PENDING', 'REJECTED'].includes(product.productStatus) && (
                        <>
                          <Link
                            className="btn btn-outline-primary btn-sm"
                            to={`/seller/edit/${product.productID}`}
                          >
                            Edit
                          </Link>
                          <button
                            className="btn btn-outline-danger btn-sm"
                            disabled={mutation.pending || Boolean(product.auctionID)}
                            title={
                              product.auctionID
                                ? 'This listing has auction history and cannot be deleted.'
                                : undefined
                            }
                            onClick={() => remove(product)}
                          >
                            Delete
                          </button>
                        </>
                      )}
                      {auctionState(product) === 'Ended' && product.bidCount > 0 && (
                        <button
                          className="btn btn-outline-primary btn-sm"
                          disabled={mutation.pending}
                          onClick={() => settle(product)}
                        >
                          Generate invoice
                        </button>
                      )}
                      {product.productStatus === 'SOLD' && (
                        <Link className="btn btn-outline-primary btn-sm" to="/seller/invoices">
                          View invoice
                        </Link>
                      )}
                      {!['PENDING', 'REJECTED', 'SOLD'].includes(product.productStatus) &&
                        auctionState(product) !== 'Ended' && (
                          <Link
                            className="btn btn-outline-secondary btn-sm"
                            to={`/products/${product.productID}`}
                          >
                            View auction
                          </Link>
                        )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </ResourceState>
      {!resource.loading && !resource.error && !products.length && user.status === 'APPROVED' && (
        <p className="text-center">
          <Link to="/seller/new">Create your first listing</Link>
        </p>
      )}
    </>
  );
}

function ProductForm({ product }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const categories = useResource('/categories');
  const mutation = useMutation();
  const [form, setForm] = useState(
    product
      ? {
          title: product.title,
          description: product.description,
          categoryID: product.categoryID,
          startingPrice: product.startingPrice,
          incrementGap: product.incrementGap,
          productCondition: product.productCondition,
          auctionStartDate: product.auctionStartDate.slice(0, 16),
          auctionEndDate: product.auctionEndDate.slice(0, 16),
        }
      : {
          title: '',
          description: '',
          categoryID: '',
          startingPrice: '',
          incrementGap: '',
          productCondition: 'USED',
          auctionStartDate: localDateTime(Date.now() + 60000),
          auctionEndDate: localDateTime(Date.now() + 86400000),
        },
  );
  const [image, setImage] = useState(null);
  const [validation, setValidation] = useState('');
  const change = (event) => {
    setValidation('');
    setForm({ ...form, [event.target.name]: event.target.value });
  };
  const submit = async (event) => {
    event.preventDefault();
    setValidation('');
    if (
      new Date(form.auctionEndDate) <= new Date(form.auctionStartDate) ||
      new Date(form.auctionEndDate) <= new Date()
    ) {
      setValidation('The end time must be in the future and after the start time.');
      return;
    }
    if (!form.title.trim() || !form.description.trim()) {
      setValidation('A title and description are required.');
      return;
    }
    if (
      image &&
      (!['image/jpeg', 'image/png'].includes(image.type) || image.size > 5 * 1024 * 1024)
    ) {
      setValidation('Choose a JPEG or PNG image smaller than 5 MB.');
      return;
    }
    const payload = {
      ...form,
      title: form.title.trim(),
      description: form.description.trim(),
      startingPrice: Number(form.startingPrice),
      incrementGap: Number(form.incrementGap),
      auctionStartDate: `${form.auctionStartDate}:00`,
      auctionEndDate: `${form.auctionEndDate}:00`,
    };
    const result = await mutation.run(
      () =>
        request(product ? `/products/${product.productID}` : '/products', {
          method: product ? 'PUT' : 'POST',
          body: payload,
        }),
      '',
    );
    if (!result.ok) return;
    let imageError = '';
    if (image) {
      const body = new FormData();
      body.append('productID', result.data.productID);
      body.append('file', image);
      const uploaded = await mutation.run(
        () => request('/file-upload/product-image', { method: 'POST', body }),
        '',
      );
      if (!uploaded.ok)
        imageError =
          'Your listing was saved, but the image could not be uploaded. Open Edit to try the image again.';
    }
    navigate('/seller', {
      replace: true,
      state: { success: 'Listing submitted for administrator review.', error: imageError },
    });
  };
  return (
    <>
      <PageHeading eyebrow="SELLER WORKSPACE" title={product ? 'Edit listing' : 'Create a listing'}>
        Add accurate details and a schedule. Listings appear publicly after approval.
      </PageHeading>
      <div className="surface form-surface">
        <Notice>{validation || mutation.error}</Notice>
        <form onSubmit={submit}>
          <fieldset disabled={mutation.pending || user.status !== 'APPROVED'}>
            <Field
              label="Item title"
              name="title"
              value={form.title}
              onChange={change}
              required
              maxLength={100}
            />
            <Field label="Description" name="description">
              <textarea
                className="form-control"
                id="description"
                name="description"
                rows={4}
                value={form.description}
                onChange={change}
                required
                maxLength={500}
              />
            </Field>
            <div className="row">
              <Field className="col-sm-6" label="Category" name="categoryID">
                <select
                  className="form-select"
                  id="categoryID"
                  name="categoryID"
                  value={form.categoryID}
                  onChange={change}
                  required
                >
                  <option value="">Select a category</option>
                  {(categories.data || []).map((category) => (
                    <option key={category.categoryID} value={category.categoryID}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </Field>
              <Field className="col-sm-6" label="Condition" name="productCondition">
                <select
                  className="form-select"
                  id="productCondition"
                  name="productCondition"
                  value={form.productCondition}
                  onChange={change}
                >
                  <option value="NEW">New</option>
                  <option value="USED">Used</option>
                  <option value="REFURBISHED">Refurbished</option>
                </select>
              </Field>
            </div>
            <Notice>{categories.error}</Notice>
            {!categories.loading && !categories.data?.length && !categories.error && (
              <Notice type="warning">
                An administrator needs to add a category before you can create a listing.
              </Notice>
            )}
            <div className="row">
              <Field
                className="col-sm-6"
                label="Starting price (INR)"
                name="startingPrice"
                type="number"
                value={form.startingPrice}
                onChange={change}
                min="0.01"
                step="0.01"
                required
              />
              <Field
                className="col-sm-6"
                label="Minimum bid increment (INR)"
                name="incrementGap"
                type="number"
                value={form.incrementGap}
                onChange={change}
                min="0.01"
                step="0.01"
                required
              />
            </div>
            <div className="row">
              <Field
                className="col-sm-6"
                label="Auction starts"
                name="auctionStartDate"
                type="datetime-local"
                value={form.auctionStartDate}
                onChange={change}
                required
              />
              <Field
                className="col-sm-6"
                label="Auction ends"
                name="auctionEndDate"
                type="datetime-local"
                value={form.auctionEndDate}
                onChange={change}
                required
              />
            </div>
            <p className="small text-secondary">Times use your local timezone.</p>
            <Field
              label="Product image (optional)"
              name="productImage"
              type="file"
              accept="image/jpeg,image/png"
              onChange={(event) => setImage(event.target.files?.[0] || null)}
            />
            <p className="small text-secondary">
              JPEG or PNG, up to 5 MB. Upload images before the listing is approved.
            </p>
            {product?.imageUrls?.length > 0 && (
              <div className="d-flex gap-2 mb-3">
                {product.imageUrls.map((url) => (
                  <img className="thumbnail" key={url} src={imageUrl(url)} alt="Current product" />
                ))}
              </div>
            )}
            <div className="d-flex gap-3">
              <button className="btn btn-primary" disabled={!categories.data?.length}>
                {mutation.pending ? 'Saving listing…' : 'Submit for review'}
              </button>
              <Link className="btn btn-outline-secondary" to="/seller">
                Cancel
              </Link>
            </div>
          </fieldset>
        </form>
      </div>
    </>
  );
}
function EditPage() {
  const { productID } = useParams();
  const resource = useResource(`/products/${encodeURIComponent(productID)}`);
  if (resource.loading) return <Loading />;
  return (
    <ResourceState resource={resource}>
      {resource.data && <ProductForm key={productID} product={resource.data} />}
    </ResourceState>
  );
}
function SellerBids() {
  const { user } = useAuth();
  const products = useResource(user.status === 'APPROVED' ? '/products/mine' : null);
  const [selected, setSelected] = useState('');
  const selectedProduct = products.data?.find((product) => product.productID === selected);
  const bids = useResource(
    selectedProduct && ['APPROVED', 'SOLD'].includes(selectedProduct.productStatus)
      ? `/bids/product/${encodeURIComponent(selected)}`
      : null,
  );
  return (
    <>
      <PageHeading eyebrow="SELLER WORKSPACE" title="Bidding activity">
        Follow bids on your listings.
      </PageHeading>
      <ResourceState resource={products}>
        <Field label="Choose a listing" name="listing">
          <select
            className="form-select search-input"
            id="listing"
            value={selected}
            onChange={(event) => setSelected(event.target.value)}
          >
            <option value="">Select a listing</option>
            {(products.data || []).map((product) => (
              <option key={product.productID} value={product.productID}>
                {product.title}
              </option>
            ))}
          </select>
        </Field>
      </ResourceState>
      {selected ? (
        <ResourceState
          resource={bids}
          isEmpty={!bids.data?.length}
          empty="No bids on this listing yet"
        >
          <div className="surface table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th scope="col">Bidder</th>
                  <th scope="col">Amount</th>
                  <th scope="col">Placed</th>
                </tr>
              </thead>
              <tbody>
                {(bids.data || []).map((bid) => (
                  <tr key={bid.bidID}>
                    <td>Bidder …{bid.bidderID?.slice(-6)}</td>
                    <td>{money(bid.amount)}</td>
                    <td>{dateTime(bid.timestamp)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </ResourceState>
      ) : (
        <Empty title="Select a listing to see its bids" />
      )}
    </>
  );
}
export default function SellerPages() {
  return (
    <Routes>
      <Route index element={<ListingsPage />} />
      <Route path="new" element={<ProductForm />} />
      <Route path="edit/:productID" element={<EditPage />} />
      <Route path="bids" element={<SellerBids />} />
      <Route path="invoices" element={<InvoicesPage />} />
      <Route path="*" element={<Navigate to="/seller" replace />} />
    </Routes>
  );
}
