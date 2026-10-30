import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { request } from '../../shared/api/client';
import { useAuth } from './AuthContext';
import useMutation from '../../shared/hooks/useMutation';
import { Feedback, Field, Notice, PageHeading } from '../../shared/components/UI';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const mutation = useMutation();
  const submit = async (event) => {
    event.preventDefault();
    await mutation.run(
      () =>
        request('/auth/forgot-password', {
          method: 'POST',
          auth: false,
          body: { email: email.trim() },
        }),
      'If an account matches that email, password reset instructions have been sent. Check your inbox and spam folder.',
    );
  };
  return (
    <div className="auth-card surface">
      <PageHeading eyebrow="ACCOUNT RECOVERY" title="Reset your password">
        Enter the email address you used to register.
      </PageHeading>
      <Feedback mutation={mutation} />
      <form onSubmit={submit}>
        <Field
          label="Account email"
          name="recoveryEmail"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          maxLength={100}
          required
        />
        <button className="btn btn-primary w-100" disabled={mutation.pending}>
          {mutation.pending ? 'Requesting reset...' : 'Send reset instructions'}
        </button>
      </form>
      <p className="small text-secondary mt-3">
        Reset links expire after 30 minutes and can be used once. If you do not receive an email,
        contact marketplace support.
      </p>
      <Link to="/login">Back to sign in</Link>
    </div>
  );
}

export function ResetPasswordPage() {
  const [params, setParams] = useSearchParams();
  const token = params.get('token') || '';
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [validation, setValidation] = useState('');
  const [complete, setComplete] = useState(false);
  const mutation = useMutation();
  const submit = async (event) => {
    event.preventDefault();
    setValidation('');
    if (password !== confirmation) {
      setValidation('Passwords do not match.');
      return;
    }
    if (new TextEncoder().encode(password).length > 72) {
      setValidation('Use a password of at most 72 bytes.');
      return;
    }
    const result = await mutation.run(
      () =>
        request('/auth/reset-password', { method: 'POST', auth: false, body: { token, password } }),
      'Your password has been reset. Sign in with your new password.',
    );
    if (result.ok) {
      logout({ localOnly: true });
      if (user) {
        navigate('/login', { replace: true, state: { passwordReset: true } });
        return;
      }
      setPassword('');
      setConfirmation('');
      setComplete(true);
      setParams({}, { replace: true });
    }
  };
  return (
    <div className="auth-card surface">
      <PageHeading eyebrow="ACCOUNT RECOVERY" title="Choose a new password">
        Use a strong password that you have not used elsewhere.
      </PageHeading>
      <Notice>{validation || mutation.error}</Notice>
      <Feedback mutation={{ ...mutation, error: '' }} />
      {complete ? (
        <Link className="btn btn-primary w-100" to="/login">
          Continue to sign in
        </Link>
      ) : token ? (
        <form onSubmit={submit}>
          <Field
            label="New password"
            name="newPassword"
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            minLength={8}
            maxLength={72}
          />
          <Field
            label="Confirm new password"
            name="confirmNewPassword"
            type="password"
            autoComplete="new-password"
            value={confirmation}
            onChange={(event) => setConfirmation(event.target.value)}
            required
            minLength={8}
            maxLength={72}
          />
          <p className="small text-secondary">
            At least 8 characters. Your existing sessions will be signed out.
          </p>
          <button className="btn btn-primary w-100" disabled={mutation.pending}>
            {mutation.pending ? 'Resetting password...' : 'Reset password'}
          </button>
          <p className="small mt-3 mb-0">
            <Link to="/forgot-password">Request a new reset link</Link>
          </p>
        </form>
      ) : (
        <>
          <Notice type="warning">Open the password reset link from your email to continue.</Notice>
          <Link to="/forgot-password">Request a new reset link</Link>
        </>
      )}
    </div>
  );
}
