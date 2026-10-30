import { useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { request } from '../../shared/api/client';
import useMutation from '../../shared/hooks/useMutation';
import { Badge, Feedback, Field, PageHeading } from '../../shared/components/UI';
import { dateTime } from '../../shared/lib/format';
import { useNavigate } from 'react-router-dom';

export default function AccountPage() {
  const { user, refresh, logout } = useAuth();
  const navigate = useNavigate();
  const mutation = useMutation();
  const [form, setForm] = useState({
    firstName: user.firstName || '',
    lastName: user.lastName || '',
    username: user.username || '',
    phone: user.phone || '',
  });
  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });
  const submit = async (event) => {
    event.preventDefault();
    const result = await mutation.run(
      () =>
        request('/users/me', {
          method: 'PUT',
          body: {
            ...form,
            firstName: form.firstName.trim(),
            lastName: form.lastName.trim(),
            username: form.username.trim(),
          },
        }),
      'Profile updated.',
    );
    if (result.ok) refresh();
  };
  return (
    <>
      <PageHeading eyebrow="YOUR ACCOUNT" title="My profile">
        Keep your account details up to date.
      </PageHeading>
      <div className="account-grid">
        <section className="surface">
          <h2>Personal information</h2>
          <Feedback mutation={mutation} />
          <form onSubmit={submit}>
            <div className="row">
              <Field
                className="col-sm-6"
                label="First name"
                name="firstName"
                value={form.firstName}
                onChange={change}
                maxLength={50}
                required
              />
              <Field
                className="col-sm-6"
                label="Last name"
                name="lastName"
                value={form.lastName}
                onChange={change}
                maxLength={50}
                required
              />
            </div>
            <Field
              label="Username"
              name="username"
              value={form.username}
              onChange={change}
              minLength={3}
              maxLength={50}
              required
              pattern="[A-Za-z0-9_.-]+"
            />
            <Field
              label="Phone"
              name="phone"
              value={form.phone}
              onChange={change}
              type="tel"
              maxLength={20}
            />
            <button className="btn btn-primary" disabled={mutation.pending}>
              {mutation.pending ? 'Saving…' : 'Save profile'}
            </button>
          </form>
        </section>
        <aside className="surface">
          <h2>Account details</h2>
          <dl className="detail-list">
            <div>
              <dt>Email</dt>
              <dd className="break-word">{user.email}</dd>
            </div>
            <div>
              <dt>Account type</dt>
              <dd>
                <Badge value={user.role} />
              </dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>
                <Badge value={user.status} />
              </dd>
            </div>
            <div>
              <dt>Joined</dt>
              <dd>{dateTime(user.createdAt)}</dd>
            </div>
          </dl>
          <button className="btn btn-outline-secondary btn-sm" onClick={refresh}>
            Refresh account status
          </button>
          <div className="border-top mt-4 pt-4">
            <h2>Account security</h2>
            <p className="small text-secondary">
              Sign out every session if you no longer trust a device you used to access BidPro. You
              will need to sign in again here too.
            </p>
            <button
              className="btn btn-outline-danger btn-sm"
              onClick={async () => {
                const result = await logout({ all: true });
                navigate('/login', {
                  state: result.revoked
                    ? null
                    : {
                        signoutNotice:
                          'Signed out on this device. The server could not confirm revocation on other devices. Sign in again when connected and retry.',
                      },
                });
              }}
            >
              Sign out all devices
            </button>
          </div>
        </aside>
      </div>
    </>
  );
}
