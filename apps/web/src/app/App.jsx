import { Component, lazy, Suspense } from 'react';
import {
  BrowserRouter,
  Link,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
} from 'react-router-dom';
import AuthProvider from '../features/auth/AuthProvider';
import { useAuth } from '../features/auth/AuthContext';
import { LoginPage, RegisterPage } from '../features/auth/AuthPages';
import { ForgotPasswordPage, ResetPasswordPage } from '../features/auth/RecoveryPages';
import { Loading, Notice, PageHeading, PendingAccount } from '../shared/components/UI';
import { dashboardPath } from '../shared/lib/format';
import Layout, { WorkspaceNav } from './Layout';
import RouteAccessibility from './RouteAccessibility';
import {
  CatalogPage,
  CategoriesPage,
  HomePage,
  ProductPage,
} from '../features/catalog/CatalogPages';
const AdminPages = lazy(() => import('../features/admin/AdminPages'));
const SellerPages = lazy(() => import('../features/seller/SellerPages'));
const BidderPages = lazy(() => import('../features/bidder/BidderPages'));
const AccountPage = lazy(() => import('../features/account/AccountPage'));
const InboxPage = lazy(() => import('../features/messaging/InboxPage'));
const NotificationsPage = lazy(() => import('../features/notifications/NotificationsPage'));
const MailOutboxPage = lazy(() => import('../features/notifications/MailOutboxPage'));

class ErrorBoundary extends Component {
  state = { error: false };
  static getDerivedStateFromError() {
    return { error: true };
  }
  render() {
    return this.state.error ? (
      <div className="container py-5">
        <h1>Something went wrong</h1>
        <p>Please reload the page to try again.</p>
        <button className="btn btn-primary" onClick={() => window.location.reload()}>
          Reload page
        </button>
      </div>
    ) : (
      this.props.children
    );
  }
}
function Protected({ roles }) {
  const { user, checking, sessionError, refresh } = useAuth();
  const location = useLocation();
  if (checking) return <Loading />;
  if (sessionError)
    return (
      <Notice>
        {sessionError}{' '}
        <button className="btn btn-outline-danger btn-sm ms-2" onClick={refresh}>
          Retry session
        </button>
      </Notice>
    );
  if (!user)
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: `${location.pathname}${location.search}${location.hash}` }}
      />
    );
  if (roles && !roles.includes(user.role))
    return <Navigate to={dashboardPath(user.role)} replace />;
  return <Outlet />;
}
function Workspace({ role }) {
  const { user } = useAuth();
  return (
    <div className="workspace-shell">
      <WorkspaceNav role={role} />
      <div className="workspace-content">
        <PendingAccount user={user} />
        <Suspense fallback={<Loading />}>
          <Outlet />
        </Suspense>
      </div>
    </div>
  );
}
function DashboardRedirect() {
  const { user } = useAuth();
  return <Navigate to={dashboardPath(user?.role)} replace />;
}
function InfoPage({ kind }) {
  if (kind === 'terms')
    return (
      <div className="surface reading-page">
        <PageHeading title="Marketplace terms" />
        <h2>Accounts and listings</h2>
        <p>
          Provide accurate information and list only items you have the right to sell.
          Administrators review accounts and listings before participation.
        </p>
        <h2>Bidding</h2>
        <p>
          Place bids only if you intend to purchase the item. Bids must meet the starting price or
          the current highest bid plus the required increment. You cannot bid on your own listings.
        </p>
        <h2>Auction results and invoices</h2>
        <p>
          The highest valid bid when an auction ends wins. Winning bidders can generate and download
          an invoice. Payment options and their status are shown alongside the invoice.
        </p>
        <h2>Support</h2>
        <p>
          Contact your marketplace administrator about account approval, disputed listings, and
          purchases.
        </p>
      </div>
    );
  return (
    <div className="surface reading-page">
      <PageHeading eyebrow="A SIMPLE START" title="How BidPro works" />
      <h2>1. Create an account</h2>
      <p>
        Choose a bidder, seller, or combined account. An administrator reviews new accounts before
        listing or bidding is enabled.
      </p>
      <h2>2. Discover or list an item</h2>
      <p>
        Browse approved listings by category. Sellers provide a description, condition, starting
        price, minimum increment, and auction schedule. New listings are reviewed before they appear
        publicly.
      </p>
      <h2>3. Place your bid</h2>
      <p>
        Open a live listing to see the highest bid and minimum next bid. You can increase your bid
        while the auction is live. The page refreshes live bid data automatically.
      </p>
      <h2>4. Complete the sale</h2>
      <p>
        After the auction ends, the winning bidder can generate an invoice from My bids. Both the
        seller and buyer can download it from their workspace.
      </p>
      <Link className="btn btn-primary" to="/auctions">
        Explore auctions
      </Link>
    </div>
  );
}
export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <RouteAccessibility />
          <Suspense fallback={<Loading />}>
            <Routes>
              <Route element={<Layout />}>
                <Route index element={<HomePage />} />
                <Route path="auctions" element={<CatalogPage />} />
                <Route path="auction" element={<Navigate to="/auctions" replace />} />
                <Route path="products/:productID" element={<ProductPage />} />
                <Route path="categories" element={<CategoriesPage />} />
                <Route path="help" element={<InfoPage />} />
                <Route path="terms" element={<InfoPage kind="terms" />} />
                <Route path="forgot-password" element={<ForgotPasswordPage />} />
                <Route path="reset-password" element={<ResetPasswordPage />} />
                <Route path="login" element={<LoginPage />} />
                <Route path="register" element={<RegisterPage />} />
                <Route element={<Protected />}>
                  <Route path="dashboard" element={<DashboardRedirect />} />
                  <Route path="account" element={<AccountPage />} />
                  <Route path="messages" element={<InboxPage />} />
                  <Route path="messages/:threadID" element={<InboxPage />} />
                  <Route path="notifications" element={<NotificationsPage />} />
                </Route>
                <Route element={<Protected roles={['ADMIN']} />}>
                  <Route element={<Workspace role="ADMIN" />}>
                    <Route path="admin/email-outbox" element={<MailOutboxPage />} />
                    <Route path="admin/*" element={<AdminPages />} />
                  </Route>
                </Route>
                <Route element={<Protected roles={['SELLER', 'BOTH']} />}>
                  <Route element={<Workspace role="SELLER" />}>
                    <Route path="seller/*" element={<SellerPages />} />
                  </Route>
                </Route>
                <Route element={<Protected roles={['BIDDER', 'BOTH']} />}>
                  <Route element={<Workspace role="BIDDER" />}>
                    <Route path="bidder/*" element={<BidderPages />} />
                  </Route>
                </Route>
                <Route
                  path="*"
                  element={
                    <div className="surface reading-page">
                      <PageHeading title="Page not found">This page does not exist.</PageHeading>
                      <Link to="/" className="btn btn-primary">
                        Back to home
                      </Link>
                    </div>
                  }
                />
              </Route>
            </Routes>
          </Suspense>
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
