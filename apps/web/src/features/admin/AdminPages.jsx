import { useState } from 'react';
import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import InvoicesPage from '../invoices/InvoicesPage';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { request } from '../../shared/api/client';
import { dateTime, label, money } from '../../shared/lib/format';
import { Badge, Feedback, Field, PageHeading, ResourceState } from '../../shared/components/UI';

function OverviewPage() {
  const { user } = useAuth();
  const resource = useResource('/admins/dashboard');
  const data = resource.data || {};
  const recent = data.recentProducts || [];
  const active = data.activeAuctions || [];
  const pending = recent.filter((item) => item.productStatus === 'PENDING').length;
  const statuses = ['APPROVED', 'PENDING', 'SOLD', 'REJECTED'].map((status) => ({
    status,
    count: recent.filter((item) => item.productStatus === status).length,
  }));
  return (
    <>
      <PageHeading
        eyebrow="CONTROL ROOM / OVERVIEW"
        title="Marketplace overview"
        action={
          <button className="btn btn-outline-secondary" onClick={resource.reload}>
            <i className="bi bi-arrow-clockwise me-2" aria-hidden="true" />
            Refresh
          </button>
        }
      >
        Welcome back, {user.firstName || user.username}. Here is what is happening across BidPro.
      </PageHeading>
      <ResourceState resource={resource}>
        <div className="stats-grid dashboard-stats">
          {[
            [
              'people',
              'Registered users',
              data.totalUsers || 0,
              '/admin/users',
              'The people behind the marketplace',
            ],
            [
              'box-seam',
              'Total listings',
              data.totalProducts || 0,
              '/admin/products',
              'Every piece, from review to sale',
            ],
            [
              'broadcast',
              'Live auctions',
              data.totalActiveAuctions || 0,
              '/admin/auctions',
              'Open for bidding right now',
            ],
            [
              'hammer',
              'Bids placed',
              data.totalBids || 0,
              '/admin/bids',
              'Recorded across the auction floor',
            ],
          ].map(([icon, text, value, to, description]) => (
            <Link className="surface stat-card" to={to} key={text}>
              <i className={`bi bi-${icon}`} aria-hidden="true" />
              <span>{text}</span>
              <strong>{value}</strong>
              <small className="stat-card-caption">{description}</small>
            </Link>
          ))}
        </div>
        <div className="dashboard-bento">
          <section className="surface dashboard-signal">
            <div className="dashboard-panel-kicker">
              <span className="live-dot" /> THE AUCTION FLOOR
            </div>
            <h2>
              {active.length ? 'Good pieces. Real momentum.' : 'The next chapter starts here.'}
            </h2>
            <p>
              {active.length
                ? `${active.length} ${active.length === 1 ? 'auction is' : 'auctions are'} live. Keep an eye on the pieces finding their next owner.`
                : 'Approve a reviewed listing to bring the next auction to the floor.'}
            </p>
            <div className="dashboard-signal-bottom">
              <div>
                <span>Recorded paid value</span>
                <strong>{money(data.revenue || 0)}</strong>
              </div>
              <Link
                className="dashboard-round-link"
                to="/admin/invoices"
                aria-label="View invoice records"
              >
                <i className="bi bi-arrow-up-right" aria-hidden="true" />
              </Link>
            </div>
          </section>
          <section className="surface dashboard-status-panel">
            <div className="dashboard-panel-header">
              <div>
                <span className="eyebrow">AT A GLANCE</span>
                <h2>Recent listing statuses</h2>
              </div>
              <i className="bi bi-sliders2" aria-hidden="true" />
            </div>
            <p className="dashboard-panel-note">
              Status breakdown of the {recent.length} listing records in this overview.
            </p>
            <div className="status-breakdown">
              {statuses.map(({ status, count }) => (
                <div className={`status-breakdown-row tone-${status.toLowerCase()}`} key={status}>
                  <div>
                    <span>{label(status)}</span>
                    <strong>{count}</strong>
                  </div>
                  <div className="status-meter">
                    <span
                      style={{ width: `${recent.length ? (count / recent.length) * 100 : 0}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </section>
          <section className="surface dashboard-auctions">
            <div className="dashboard-panel-header">
              <div>
                <span className="eyebrow">LIVE RIGHT NOW</span>
                <h2>On the auction floor</h2>
              </div>
              <Link to="/admin/auctions" className="dashboard-text-link">
                View all <i className="bi bi-arrow-up-right" aria-hidden="true" />
              </Link>
            </div>
            {!active.length ? (
              <p className="dashboard-panel-note">No auctions are live at the moment.</p>
            ) : (
              <div className="dashboard-auction-list">
                {active.slice(0, 3).map((auction, index) => (
                  <Link
                    className="auction-mini-row"
                    key={auction.auctionID}
                    to={`/products/${auction.productID}`}
                  >
                    <span className="auction-mini-index">{String(index + 1).padStart(2, '0')}</span>
                    <div>
                      <strong>{auction.title}</strong>
                      <small>Ends {dateTime(auction.endDate)}</small>
                    </div>
                    <span className="auction-mini-price">
                      {money(auction.startingPrice)}
                      <small>Starting price</small>
                    </span>
                    <i className="bi bi-arrow-up-right" aria-hidden="true" />
                  </Link>
                ))}
              </div>
            )}
          </section>
          <section className="surface dashboard-review-card">
            <span className="dashboard-review-icon">
              <i className="bi bi-shield-check" aria-hidden="true" />
            </span>
            <span className="eyebrow">THE NEXT GREAT FIND</span>
            <h2>Keep the collection moving.</h2>
            <p>
              {pending
                ? `${pending} ${pending === 1 ? 'listing in this overview is' : 'listings in this overview are'} waiting for review.`
                : 'Reviewed listings make a more considered marketplace. Explore the latest submissions.'}
            </p>
            <Link className="btn btn-primary" to="/admin/products">
              Manage products <i className="bi bi-arrow-right ms-2" aria-hidden="true" />
            </Link>
          </section>
        </div>
        <section className="surface dashboard-members">
          <div className="dashboard-panel-header">
            <div>
              <span className="eyebrow">THE PEOPLE BEHIND THE PIECES</span>
              <h2>Recent registrations</h2>
            </div>
            <Link to="/admin/users" className="dashboard-text-link">
              Manage users <i className="bi bi-arrow-up-right" aria-hidden="true" />
            </Link>
          </div>
          {!data.recentUsers?.length ? (
            <p className="text-secondary">No users yet.</p>
          ) : (
            <div className="table-responsive">
              <table className="table align-middle">
                <thead>
                  <tr>
                    <th scope="col">Member</th>
                    <th scope="col">Role</th>
                    <th scope="col">Status</th>
                    <th scope="col">Joined</th>
                  </tr>
                </thead>
                <tbody>
                  {data.recentUsers.map((member) => (
                    <tr key={member.userID}>
                      <td>
                        <div className="member-cell">
                          <span className="member-avatar" aria-hidden="true">
                            {(member.firstName || member.username || 'B').slice(0, 1)}
                          </span>
                          <div>
                            <strong>
                              {member.firstName} {member.lastName}
                            </strong>
                            <small className="d-block text-secondary">{member.email}</small>
                          </div>
                        </div>
                      </td>
                      <td>{label(member.role)}</td>
                      <td>
                        <Badge value={member.status} />
                      </td>
                      <td className="text-secondary">{dateTime(member.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </ResourceState>
    </>
  );
}
function UsersPage() {
  const { user: currentUser } = useAuth();
  const resource = useResource('/admins/users');
  const mutation = useMutation();
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const users = (resource.data || []).filter(
    (user) =>
      `${user.firstName} ${user.lastName} ${user.email} ${user.username}`
        .toLowerCase()
        .includes(query.toLowerCase()) &&
      (!status || user.status === status),
  );
  const update = async (user, next) => {
    const result = await mutation.run(
      () =>
        request(`/admins/users/${user.userID}/status`, { method: 'PUT', body: { status: next } }),
      'Account status updated.',
    );
    if (result.ok) resource.reload();
  };
  return (
    <>
      <PageHeading eyebrow="ADMINISTRATOR WORKSPACE" title="Manage users">
        Review registrations and control marketplace participation.
      </PageHeading>
      <Feedback mutation={mutation} />
      <div className="surface filter-bar">
        <Field
          label="Search users"
          name="userSearch"
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Name, email, or username"
        />
        <Field label="Account status" name="userStatus">
          <select
            className="form-select"
            id="userStatus"
            value={status}
            onChange={(event) => setStatus(event.target.value)}
          >
            <option value="">All statuses</option>
            {['PENDING', 'APPROVED', 'REJECTED'].map((item) => (
              <option key={item} value={item}>
                {label(item)}
              </option>
            ))}
          </select>
        </Field>
        <button className="btn btn-outline-secondary align-self-end mb-3" onClick={resource.reload}>
          Refresh
        </button>
      </div>
      <ResourceState resource={resource} isEmpty={!users.length} empty="No users match your search">
        <div className="surface table-responsive mt-4">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Name</th>
                <th scope="col">Contact</th>
                <th scope="col">Role</th>
                <th scope="col">Joined</th>
                <th scope="col">Status</th>
                <th scope="col">Update status</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.userID}>
                  <td className="fw-semibold">
                    {user.firstName} {user.lastName}
                    <small className="d-block fw-normal text-secondary">@{user.username}</small>
                  </td>
                  <td>
                    {user.email}
                    <small className="d-block text-secondary">{user.phone}</small>
                  </td>
                  <td>{label(user.role)}</td>
                  <td>{dateTime(user.createdAt)}</td>
                  <td>
                    <Badge value={user.status} />
                  </td>
                  <td>
                    <select
                      className="form-select form-select-sm"
                      aria-label={`Status for ${user.email}`}
                      disabled={
                        mutation.pending ||
                        user.role === 'ADMIN' ||
                        user.userID === currentUser.userID
                      }
                      value={user.status}
                      onChange={(event) => update(user, event.target.value)}
                    >
                      {['PENDING', 'APPROVED', 'REJECTED'].map((item) => (
                        <option key={item} value={item}>
                          {label(item)}
                        </option>
                      ))}
                    </select>
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
function ProductsPage() {
  const resource = useResource('/admins/products');
  const mutation = useMutation();
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const products = (resource.data || []).filter(
    (product) =>
      product.title.toLowerCase().includes(query.toLowerCase()) &&
      (!status || product.productStatus === status),
  );
  const moderate = async (product, next) => {
    const result = await mutation.run(
      () =>
        request(`/admins/products/${product.productID}/status`, {
          method: 'PUT',
          body: { status: next },
        }),
      next === 'APPROVED' ? 'Listing approved. Its auction is scheduled.' : 'Listing rejected.',
    );
    if (result.ok) resource.reload();
  };
  return (
    <>
      <PageHeading eyebrow="ADMINISTRATOR WORKSPACE" title="Manage products">
        Approve reviewed listings to publish their auctions.
      </PageHeading>
      <Feedback mutation={mutation} />
      <div className="surface filter-bar">
        <Field
          label="Search products"
          name="productSearch"
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        <Field label="Listing status" name="productStatus">
          <select
            className="form-select"
            id="productStatus"
            value={status}
            onChange={(event) => setStatus(event.target.value)}
          >
            <option value="">All statuses</option>
            {['PENDING', 'APPROVED', 'REJECTED', 'SOLD'].map((item) => (
              <option key={item} value={item}>
                {label(item)}
              </option>
            ))}
          </select>
        </Field>
        <button className="btn btn-outline-secondary align-self-end mb-3" onClick={resource.reload}>
          Refresh
        </button>
      </div>
      <ResourceState
        resource={resource}
        isEmpty={!products.length}
        empty="No products match your search"
      >
        <div className="surface table-responsive mt-4">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Listing</th>
                <th scope="col">Category</th>
                <th scope="col">Price</th>
                <th scope="col">Schedule</th>
                <th scope="col">Status</th>
                <th scope="col">Review</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.productID}>
                  <td>
                    <Link className="fw-semibold" to={`/products/${product.productID}`}>
                      {product.title}
                    </Link>
                    <span className="table-description">{product.description}</span>
                  </td>
                  <td>{product.categoryName || product.categoryID}</td>
                  <td>
                    {money(product.startingPrice)}
                    <small className="d-block text-secondary">
                      Step {money(product.incrementGap)}
                    </small>
                  </td>
                  <td className="small">
                    {dateTime(product.auctionStartDate)}
                    <br />
                    to {dateTime(product.auctionEndDate)}
                  </td>
                  <td>
                    <Badge value={product.productStatus} />
                  </td>
                  <td>
                    <div className="d-flex gap-2">
                      {product.productStatus !== 'SOLD' && !product.bidCount && (
                        <>
                          {product.productStatus !== 'APPROVED' && (
                            <button
                              className="btn btn-outline-success btn-sm"
                              disabled={mutation.pending}
                              onClick={() => moderate(product, 'APPROVED')}
                            >
                              Approve
                            </button>
                          )}
                          {product.productStatus !== 'REJECTED' && (
                            <button
                              className="btn btn-outline-danger btn-sm"
                              disabled={mutation.pending}
                              onClick={() => moderate(product, 'REJECTED')}
                            >
                              Reject
                            </button>
                          )}
                        </>
                      )}
                      {product.bidCount > 0 && (
                        <Link className="btn btn-outline-secondary btn-sm" to="/admin/auctions">
                          Manage auction
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
    </>
  );
}

const emptyCategory = {
  name: '',
  description: '',
  visibility: true,
  displayOrder: 0,
  parentCategory: '',
};
function CategoriesAdminPage() {
  const resource = useResource('/admins/categories');
  const mutation = useMutation();
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyCategory);
  const edit = (category) => {
    setEditing(category.categoryID);
    setForm({
      name: category.name,
      description: category.description || '',
      visibility: category.visibility !== false,
      displayOrder: category.displayOrder || 0,
      parentCategory: category.parentCategory || '',
    });
  };
  const change = (event) =>
    setForm({
      ...form,
      [event.target.name]:
        event.target.type === 'checkbox' ? event.target.checked : event.target.value,
    });
  const submit = async (event) => {
    event.preventDefault();
    const result = await mutation.run(
      () =>
        request(editing ? `/admins/categories/${editing}` : '/admins/categories', {
          method: editing ? 'PUT' : 'POST',
          body: {
            ...form,
            name: form.name.trim(),
            description: form.description.trim(),
            displayOrder: Number(form.displayOrder),
            parentCategory: form.parentCategory || null,
          },
        }),
      editing ? 'Category updated.' : 'Category created.',
    );
    if (result.ok) {
      setForm(emptyCategory);
      setEditing(null);
      resource.reload();
    }
  };
  const remove = async (category) => {
    if (
      !window.confirm(
        `Delete category “${category.name}”? Categories with listings cannot be deleted.`,
      )
    )
      return;
    const result = await mutation.run(
      () => request(`/admins/categories/${category.categoryID}`, { method: 'DELETE' }),
      'Category deleted.',
    );
    if (result.ok) resource.reload();
  };
  return (
    <>
      <PageHeading eyebrow="ADMINISTRATOR WORKSPACE" title="Manage categories">
        Organize the marketplace and choose what appears publicly.
      </PageHeading>
      <Feedback mutation={mutation} />
      <div className="admin-category-grid">
        <section className="surface">
          <h2>{editing ? 'Edit category' : 'Create category'}</h2>
          <form onSubmit={submit}>
            <fieldset disabled={mutation.pending}>
              <Field
                label="Category name"
                name="name"
                value={form.name}
                onChange={change}
                maxLength={50}
                required
              />
              <Field label="Description" name="description">
                <textarea
                  className="form-control"
                  id="description"
                  name="description"
                  value={form.description}
                  onChange={change}
                  rows={3}
                  maxLength={255}
                />
              </Field>
              <Field
                label="Display order"
                name="displayOrder"
                value={form.displayOrder}
                onChange={change}
                type="number"
                min="0"
                step="1"
                required
              />
              <Field label="Parent category" name="parentCategory">
                <select
                  className="form-select"
                  id="parentCategory"
                  name="parentCategory"
                  value={form.parentCategory}
                  onChange={change}
                >
                  <option value="">None</option>
                  {(resource.data || [])
                    .filter((item) => item.categoryID !== editing)
                    .map((item) => (
                      <option key={item.categoryID} value={item.categoryID}>
                        {item.name}
                      </option>
                    ))}
                </select>
              </Field>
              <div className="form-check mb-4">
                <input
                  className="form-check-input"
                  type="checkbox"
                  id="visibility"
                  name="visibility"
                  checked={form.visibility}
                  onChange={change}
                />
                <label className="form-check-label" htmlFor="visibility">
                  Visible in the marketplace
                </label>
              </div>
              <div className="d-flex gap-2">
                <button className="btn btn-primary">
                  {mutation.pending ? 'Saving…' : editing ? 'Save category' : 'Create category'}
                </button>
                {editing && (
                  <button
                    type="button"
                    className="btn btn-outline-secondary"
                    onClick={() => {
                      setEditing(null);
                      setForm(emptyCategory);
                    }}
                  >
                    Cancel edit
                  </button>
                )}
              </div>
            </fieldset>
          </form>
        </section>
        <ResourceState
          resource={resource}
          isEmpty={!resource.data?.length}
          empty="No categories yet"
        >
          <div className="surface table-responsive">
            <table className="table align-middle">
              <thead>
                <tr>
                  <th scope="col">Category</th>
                  <th scope="col">Visibility</th>
                  <th scope="col">Actions</th>
                </tr>
              </thead>
              <tbody>
                {(resource.data || []).map((category) => (
                  <tr key={category.categoryID}>
                    <td className="fw-semibold">
                      {category.name}
                      <small className="d-block fw-normal text-secondary">
                        {category.description}
                      </small>
                    </td>
                    <td>
                      <Badge value={category.visibility ? 'Visible' : 'Hidden'} />
                    </td>
                    <td>
                      <div className="d-flex gap-2">
                        <button
                          className="btn btn-outline-primary btn-sm"
                          onClick={() => edit(category)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn btn-outline-danger btn-sm"
                          disabled={mutation.pending}
                          onClick={() => remove(category)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </ResourceState>
      </div>
    </>
  );
}
function AuctionsAdminPage() {
  const resource = useResource('/admins/auctions');
  const mutation = useMutation();
  const close = async (auction) => {
    if (!window.confirm(`Close “${auction.title}” now? New bids will no longer be accepted.`))
      return;
    const result = await mutation.run(
      () => request(`/auctions/${auction.auctionID}/close`, { method: 'POST' }),
      'Auction closed. The seller or winning bidder can generate the invoice.',
    );
    if (result.ok) resource.reload();
  };
  const settle = async (auction) => {
    const result = await mutation.run(
      () =>
        request(`/invoices?productID=${encodeURIComponent(auction.productID)}`, { method: 'POST' }),
      'Invoice generated. View it under Invoices.',
    );
    if (result.ok) resource.reload();
  };
  return (
    <>
      <PageHeading
        eyebrow="ADMINISTRATOR WORKSPACE"
        title="Manage auctions"
        action={
          <button className="btn btn-outline-secondary" onClick={resource.reload}>
            Refresh
          </button>
        }
      >
        Auctions are created when products are approved and follow the listing schedule.
      </PageHeading>
      <Feedback mutation={mutation} />
      <ResourceState resource={resource} isEmpty={!resource.data?.length} empty="No auctions yet">
        <div className="surface table-responsive">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Auction</th>
                <th scope="col">Starts</th>
                <th scope="col">Ends</th>
                <th scope="col">Starting price</th>
                <th scope="col">Status</th>
                <th scope="col">Actions</th>
              </tr>
            </thead>
            <tbody>
              {(resource.data || []).map((auction) => (
                <tr key={auction.auctionID}>
                  <td>
                    <Link to={`/products/${auction.productID}`}>{auction.title}</Link>
                  </td>
                  <td>{dateTime(auction.startDate)}</td>
                  <td>{dateTime(auction.endDate)}</td>
                  <td>{money(auction.startingPrice)}</td>
                  <td>
                    <Badge value={auction.status} />
                  </td>
                  <td>
                    {auction.status === 'ONGOING' ? (
                      <button
                        className="btn btn-outline-danger btn-sm"
                        disabled={mutation.pending}
                        onClick={() => close(auction)}
                      >
                        Close auction
                      </button>
                    ) : auction.status === 'PAST' ? (
                      <button
                        className="btn btn-outline-primary btn-sm"
                        disabled={mutation.pending}
                        onClick={() => settle(auction)}
                      >
                        Generate invoice
                      </button>
                    ) : (
                      '—'
                    )}
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
function BidsAdminPage() {
  const resource = useResource('/admins/bids');
  const products = useResource('/admins/products');
  return (
    <>
      <PageHeading
        eyebrow="ADMINISTRATOR WORKSPACE"
        title="Bidding activity"
        action={
          <button className="btn btn-outline-secondary" onClick={resource.reload}>
            Refresh
          </button>
        }
      >
        Review recorded bids across the marketplace.
      </PageHeading>
      <ResourceState
        resource={resource}
        isEmpty={!resource.data?.length}
        empty="No bids have been placed yet"
      >
        <div className="surface table-responsive">
          <table className="table align-middle">
            <thead>
              <tr>
                <th scope="col">Item</th>
                <th scope="col">Bidder</th>
                <th scope="col">Amount</th>
                <th scope="col">Placed</th>
              </tr>
            </thead>
            <tbody>
              {(resource.data || [])
                .slice()
                .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
                .map((bid) => (
                  <tr key={bid.bidID}>
                    <td>
                      <Link to={`/products/${bid.productID}`}>
                        {(products.data || []).find(
                          (product) => product.productID === bid.productID,
                        )?.title || bid.productID}
                      </Link>
                    </td>
                    <td>…{bid.bidderID?.slice(-8)}</td>
                    <td className="fw-semibold">{money(bid.amount)}</td>
                    <td>{dateTime(bid.timestamp)}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      </ResourceState>
    </>
  );
}
export default function AdminPages() {
  return (
    <Routes>
      <Route index element={<OverviewPage />} />
      <Route path="users" element={<UsersPage />} />
      <Route path="products" element={<ProductsPage />} />
      <Route path="categories" element={<CategoriesAdminPage />} />
      <Route path="auctions" element={<AuctionsAdminPage />} />
      <Route path="bids" element={<BidsAdminPage />} />
      <Route path="invoices" element={<InvoicesPage />} />
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  );
}
