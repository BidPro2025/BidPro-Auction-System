import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { dashboardPath } from '../../shared/lib/format';
import { Field, Notice, PageHeading } from '../../shared/components/UI';
import useMutation from '../../shared/hooks/useMutation';
import { safeReturnTo } from '../../shared/lib/navigation';

export function LoginPage() {
  const { user, authenticate } = useAuth();
  const [form, setForm] = useState({ email: '', password: '' });
  const mutation = useMutation();
  const navigate = useNavigate();
  const location = useLocation();
  if (user)
    return <Navigate to={safeReturnTo(location.state?.from, dashboardPath(user.role))} replace />;
  const submit = async (event) => {
    event.preventDefault();
    const result = await mutation.run(
      () => authenticate('/auth/signin', { email: form.email.trim(), password: form.password }),
      '',
    );
    if (result.ok)
      navigate(safeReturnTo(location.state?.from, dashboardPath(result.data.role)), {
        replace: true,
      });
  };
  return (
    <div className="auth-card surface">
      <PageHeading eyebrow="WELCOME BACK" title="Sign in to BidPro">
        Your next great find is waiting.
      </PageHeading>
      <Notice type="warning">{location.state?.signoutNotice}</Notice>
      <Notice type="success">
        {location.state?.passwordReset
          ? 'Your password has been reset. Sign in with your new password.'
          : ''}
      </Notice>
      <Notice>{mutation.error}</Notice>
      <form onSubmit={submit}>
        <Field
          label="Email or username"
          name="email"
          autoComplete="username"
          value={form.email}
          onChange={(event) => setForm({ ...form, email: event.target.value })}
          required
          maxLength={100}
        />
        <Field
          label="Password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={form.password}
          onChange={(event) => setForm({ ...form, password: event.target.value })}
          required
        />
        <button className="btn btn-primary w-100" disabled={mutation.pending}>
          {mutation.pending ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
      <p className="text-center mt-4 mb-2">
        New here? <Link to="/register">Create an account</Link>
      </p>
      <p className="text-center small mb-0">
        <Link to="/forgot-password">Forgot password?</Link>
      </p>
    </div>
  );
}

export function RegisterPage() {
  const { user, authenticate } = useAuth();
  const navigate = useNavigate();
  const mutation = useMutation();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    role: 'BIDDER',
  });
  const [validation, setValidation] = useState('');
  if (user) return <Navigate to={dashboardPath(user.role)} replace />;
  const change = (event) => {
    setValidation('');
    setForm({ ...form, [event.target.name]: event.target.value });
  };
  const submit = async (event) => {
    event.preventDefault();
    if (form.password !== form.confirmPassword) {
      setValidation('Passwords do not match.');
      return;
    }
    const { confirmPassword: _confirmation, ...payload } = form;
    const result = await mutation.run(
      () =>
        authenticate('/auth/signup', {
          ...payload,
          firstName: payload.firstName.trim(),
          lastName: payload.lastName.trim(),
          username: payload.username.trim(),
          email: payload.email.trim(),
        }),
      '',
    );
    if (result.ok) navigate(dashboardPath(result.data.role), { replace: true });
  };
  return (
    <div className="auth-card auth-wide surface">
      <PageHeading eyebrow="JOIN THE MARKETPLACE" title="Create your account">
        Buy, sell, or do both. New accounts are reviewed by an administrator.
      </PageHeading>
      <Notice>{validation || mutation.error}</Notice>
      <form onSubmit={submit}>
        <div className="row">
          <Field
            className="col-sm-6"
            label="First name"
            name="firstName"
            value={form.firstName}
            onChange={change}
            autoComplete="given-name"
            required
            maxLength={50}
          />
          <Field
            className="col-sm-6"
            label="Last name"
            name="lastName"
            value={form.lastName}
            onChange={change}
            autoComplete="family-name"
            required
            maxLength={50}
          />
        </div>
        <Field
          label="Username"
          name="username"
          value={form.username}
          onChange={change}
          autoComplete="username"
          required
          minLength={3}
          maxLength={50}
          pattern="[A-Za-z0-9_.-]+"
          title="Letters, numbers, dots, underscores, and hyphens"
        />
        <Field
          label="Email"
          name="email"
          type="email"
          value={form.email}
          onChange={change}
          autoComplete="email"
          required
          maxLength={100}
        />
        <Field
          label="Phone (optional)"
          name="phone"
          type="tel"
          value={form.phone}
          onChange={change}
          autoComplete="tel"
          maxLength={20}
        />
        <Field label="Account type" name="role">
          <select className="form-select" id="role" name="role" value={form.role} onChange={change}>
            <option value="BIDDER">Bidder — buy at auction</option>
            <option value="SELLER">Seller — list items</option>
            <option value="BOTH">Buyer and seller</option>
          </select>
        </Field>
        <div className="row">
          <Field
            className="col-sm-6"
            label="Password"
            name="password"
            type="password"
            value={form.password}
            onChange={change}
            autoComplete="new-password"
            required
            minLength={8}
            maxLength={72}
          />
          <Field
            className="col-sm-6"
            label="Confirm password"
            name="confirmPassword"
            type="password"
            value={form.confirmPassword}
            onChange={change}
            autoComplete="new-password"
            required
          />
        </div>
        <p className="small text-secondary">
          Use at least 8 characters. By registering, you agree to our <Link to="/terms">terms</Link>
          .
        </p>
        <button className="btn btn-primary w-100" disabled={mutation.pending}>
          {mutation.pending ? 'Creating account…' : 'Create account'}
        </button>
      </form>
      <p className="text-center mt-4 mb-0">
        Already registered? <Link to="/login">Sign in</Link>
      </p>
    </div>
  );
}
