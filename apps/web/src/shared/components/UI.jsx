import { label } from '../lib/format';

export function Notice({ type = 'danger', children }) {
  const icon =
    {
      danger: 'exclamation-circle',
      warning: 'exclamation-triangle',
      success: 'check-circle',
      info: 'info-circle',
      light: 'info-circle',
    }[type] || 'info-circle';
  return children ? (
    <div className={`alert alert-${type}`} role={type === 'danger' ? 'alert' : 'status'}>
      <i className={`bi bi-${icon} notice-icon`} aria-hidden="true" />
      <div className="notice-content">{children}</div>
    </div>
  ) : null;
}
export function Feedback({ mutation }) {
  return (
    <>
      <Notice>{mutation.error}</Notice>
      <Notice type="success">{mutation.success}</Notice>
    </>
  );
}
export function Loading() {
  return (
    <div className="loading-state" role="status">
      <span className="loading-indicator" aria-hidden="true">
        <span className="spinner-border spinner-border-sm" />
      </span>
      Loading…
    </div>
  );
}
export function Empty({ title = 'Nothing here yet', children }) {
  return (
    <div className="empty-state">
      <i className="bi bi-inbox empty-icon" aria-hidden="true" />
      <h3>{title}</h3>
      {children && <p className="text-secondary mb-0">{children}</p>}
    </div>
  );
}
export function ResourceState({ resource, children, empty, isEmpty = false }) {
  if (resource.loading && resource.data === null) return <Loading />;
  if (resource.error)
    return (
      <Notice>
        {resource.error}{' '}
        <button className="btn btn-sm btn-outline-danger ms-2" onClick={resource.reload}>
          Try again
        </button>
      </Notice>
    );
  if (isEmpty) return <Empty title={empty} />;
  return children;
}
export function Badge({ value }) {
  return (
    <span className={`badge status-${String(value).toLowerCase()}`}>
      <span className="badge-dot" aria-hidden="true" />
      {label(value)}
    </span>
  );
}
export function PageHeading({ eyebrow, title, children, action }) {
  return (
    <div className="page-heading">
      <div className="heading-copy">
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {children && <p className="text-secondary mb-0">{children}</p>}
      </div>
      {action && <div className="heading-actions">{action}</div>}
    </div>
  );
}
export function Field({ label: text, name, children, className = '', ...props }) {
  return (
    <div className={`mb-3 ${className}`}>
      <label htmlFor={name} className="form-label">
        {text}
      </label>
      {children || <input id={name} name={name} className="form-control" {...props} />}
    </div>
  );
}
export function PendingAccount({ user }) {
  return user?.status !== 'APPROVED' ? (
    <Notice type="warning">
      Your account is {label(user?.status).toLowerCase()}. An administrator must approve it before
      you can create listings or place bids.
    </Notice>
  ) : null;
}
