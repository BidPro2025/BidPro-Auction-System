import { useEffect } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../features/auth/AuthContext';
import { canBid, canSell, dashboardPath } from '../shared/lib/format';
import useResource from '../shared/hooks/useResource';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">
        Skip to content
      </a>
      <header className="site-header glass-header">
        <nav className="container header-content" aria-label="Main navigation">
          <Link className="brand" to="/" aria-label="BidPro home">
            <span className="brand-icon">
              <i className="bi bi-hammer" aria-hidden="true" />
            </span>
            Bid<span>Pro</span>
          </Link>
          <div className="main-nav nav-capsule">
            <NavLink to="/auctions">Browse auctions</NavLink>
            <NavLink to="/categories">Categories</NavLink>
            <NavLink to="/help">How it works</NavLink>
          </div>
          <div className="account-nav account-cluster">
            {user ? (
              <>
                <AccountLinks user={user} />
                <Link className="dashboard-link" to={dashboardPath(user.role)}>
                  <span className="user-avatar" aria-hidden="true">
                    {(user.firstName || user.username || 'B').slice(0, 1).toUpperCase()}
                  </span>
                  <span className="user-copy">
                    <span className="user-name">{user.firstName || user.username}</span>
                    <span className="small text-secondary d-block">
                      {user.role === 'ADMIN' ? 'Administrator' : 'My workspace'}
                    </span>
                  </span>
                </Link>
                <button
                  className="btn btn-outline-secondary btn-sm"
                  onClick={async () => {
                    const result = await logout();
                    navigate('/login', {
                      state: result.revoked
                        ? null
                        : {
                            signoutNotice:
                              'Signed out on this device. The server could not confirm session revocation. Sign in again when connected to manage your sessions.',
                          },
                    });
                  }}
                >
                  Sign out
                </button>
              </>
            ) : (
              <>
                <Link className="sign-in-link text-dark text-decoration-none" to="/login">
                  Sign in
                </Link>
                <Link className="btn btn-primary btn-sm" to="/register">
                  Get started
                  <i className="bi bi-arrow-up-right ms-2" aria-hidden="true" />
                </Link>
              </>
            )}
          </div>
        </nav>
      </header>
      <main id="main-content" className="container app-main" tabIndex={-1}>
        <Outlet />
      </main>
      <footer className="site-footer">
        <div className="container">
          <div className="footer-main">
            <div className="footer-about">
              <Link className="brand footer-brand" to="/" aria-label="BidPro home">
                <span className="brand-icon">
                  <i className="bi bi-hammer" aria-hidden="true" />
                </span>
                Bid<span>Pro</span>
              </Link>
              <p className="footer-statement">Find something worth keeping.</p>
              <p className="footer-description">
                A place for considered finds, confident bids, and the next chapter of a good object.
              </p>
            </div>
            <nav className="footer-navigation" aria-label="Footer navigation">
              <div className="footer-link-group">
                <h2>Explore</h2>
                <Link to="/auctions">The auction floor</Link>
                <Link to="/categories">Browse collections</Link>
                <Link to="/help">A guide to BidPro</Link>
              </div>
              <div className="footer-link-group">
                <h2>Your BidPro</h2>
                {user ? (
                  <>
                    <Link to={dashboardPath(user.role)}>Your workspace</Link>
                    <Link to="/account">My profile</Link>
                    <Link to="/messages">Contact support</Link>
                  </>
                ) : (
                  <>
                    <Link to="/register">Create an account</Link>
                    <Link to="/login">Member sign in</Link>
                    <Link to="/forgot-password">Account recovery</Link>
                  </>
                )}
              </div>
            </nav>
            <div className="footer-invitation">
              <span className="eyebrow">MAKE YOUR NEXT MOVE</span>
              <h2>
                {user && canSell(user)
                  ? 'Give a great piece a new chapter.'
                  : 'Your next find is waiting.'}
              </h2>
              <Link
                className="footer-action"
                to={user && canSell(user) ? '/seller/new' : '/auctions'}
              >
                {user && canSell(user) ? 'List an item' : 'Explore the collection'}
                <i className="bi bi-arrow-up-right" aria-hidden="true" />
              </Link>
            </div>
          </div>
          <div className="footer-bottom">
            <span>&copy; {new Date().getFullYear()} BidPro. Every find has a story.</span>
            <span className="footer-assurance">
              <i className="bi bi-shield-check" aria-hidden="true" />
              Reviewed listings. Every bid recorded.
            </span>
            <div className="footer-legal">
              <Link to="/help">Help</Link>
              <Link to="/terms">Terms</Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

export function WorkspaceNav({ role }) {
  const { user } = useAuth();
  const links =
    role === 'ADMIN'
      ? [
          ['/admin', 'Overview', 'grid-1x2'],
          ['/admin/users', 'Users', 'people'],
          ['/admin/products', 'Products', 'box-seam'],
          ['/admin/categories', 'Categories', 'collection'],
          ['/admin/auctions', 'Auctions', 'broadcast'],
          ['/admin/bids', 'Bids', 'hammer'],
          ['/admin/invoices', 'Invoices', 'receipt'],
          ['/admin/email-outbox', 'Email outbox', 'envelope'],
        ]
      : role === 'SELLER'
        ? [
            ['/seller', 'My listings', 'box-seam'],
            ['/seller/new', 'New listing', 'plus-circle'],
            ['/seller/bids', 'Bidding activity', 'activity'],
            ['/seller/invoices', 'Invoices', 'receipt'],
          ]
        : [
            ['/bidder', 'Browse auctions', 'compass'],
            ['/bidder/bids', 'My bids', 'hammer'],
            ['/bidder/invoices', 'Invoices', 'receipt'],
          ];
  const roleName =
    role === 'ADMIN'
      ? 'Marketplace admin'
      : role === 'SELLER'
        ? 'Seller studio'
        : 'Collector studio';
  return (
    <aside className="workspace-sidebar">
      <div className="sidebar-identity">
        <span className="sidebar-avatar" aria-hidden="true">
          {(user.firstName || user.username || 'B').slice(0, 1).toUpperCase()}
        </span>
        <div>
          <span className="sidebar-greeting">YOUR WORKSPACE</span>
          <strong>{user.firstName || user.username}</strong>
        </div>
        <span className="sidebar-presence" aria-hidden="true" />
      </div>
      <div className="sidebar-role">
        <i
          className={`bi bi-${role === 'ADMIN' ? 'shield-check' : role === 'SELLER' ? 'shop-window' : 'gem'}`}
          aria-hidden="true"
        />
        {roleName}
      </div>
      <nav className="workspace-nav" aria-label="Workspace navigation">
        <span className="sidebar-section-label">
          {role === 'ADMIN' ? 'MARKETPLACE' : 'YOUR COLLECTION'}
        </span>
        <div className="sidebar-links">
          {links.map(([to, text, icon]) => (
            <NavLink className="workspace-link" key={to} to={to} end>
              <i className={`bi bi-${icon}`} aria-hidden="true" />
              <span>{text}</span>
              <i className="bi bi-chevron-right sidebar-link-arrow" aria-hidden="true" />
            </NavLink>
          ))}
          {role === 'SELLER' && canBid(user) && (
            <Link className="workspace-link" to="/bidder">
              <i className="bi bi-compass" aria-hidden="true" />
              <span>Buyer workspace</span>
              <i className="bi bi-arrow-up-right sidebar-link-arrow" aria-hidden="true" />
            </Link>
          )}
          {role === 'BIDDER' && canSell(user) && (
            <Link className="workspace-link" to="/seller">
              <i className="bi bi-shop-window" aria-hidden="true" />
              <span>Seller workspace</span>
              <i className="bi bi-arrow-up-right sidebar-link-arrow" aria-hidden="true" />
            </Link>
          )}
        </div>
        <div className="sidebar-footer">
          <span className="sidebar-section-label">STAY CONNECTED</span>
          <NavLink className="workspace-link workspace-utility" to="/messages">
            <i className="bi bi-chat-square-text" aria-hidden="true" />
            <span>Messages</span>
          </NavLink>
          <NavLink className="workspace-link workspace-utility" to="/notifications">
            <i className="bi bi-bell" aria-hidden="true" />
            <span>Notifications</span>
          </NavLink>
          <NavLink className="workspace-link workspace-utility" to="/account">
            <i className="bi bi-person-circle" aria-hidden="true" />
            <span>Profile</span>
          </NavLink>
        </div>
      </nav>
      <Link className="sidebar-discover" to="/auctions">
        <span className="sidebar-discover-icon">
          <i className="bi bi-stars" aria-hidden="true" />
        </span>
        <span>
          A new discovery
          <br />
          <strong>is always waiting.</strong>
        </span>
        <i className="bi bi-arrow-up-right" aria-hidden="true" />
      </Link>
    </aside>
  );
}

function AccountLinks({ user }) {
  const notifications = useResource(user ? '/notifications' : null);
  useEffect(() => {
    const timer = setInterval(notifications.reload, 30000);
    window.addEventListener('bidpro:notifications-updated', notifications.reload);
    return () => {
      clearInterval(timer);
      window.removeEventListener('bidpro:notifications-updated', notifications.reload);
    };
  }, [notifications.reload]);
  const unread = (notifications.data || []).filter((item) => !item.isRead).length;
  return (
    <div className="account-shortcuts">
      <Link to="/messages" className="account-icon" aria-label="Messages" title="Messages">
        <i className="bi bi-chat-left-text" aria-hidden="true" />
      </Link>
      <Link
        to="/notifications"
        className="account-icon"
        aria-label={unread ? `Notifications, ${unread} unread` : 'Notifications'}
        title="Notifications"
      >
        <i className="bi bi-bell" aria-hidden="true" />
        {unread > 0 && <span className="notification-count">{unread > 99 ? '99+' : unread}</span>}
      </Link>
    </div>
  );
}
