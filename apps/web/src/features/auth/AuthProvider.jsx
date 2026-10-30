import { Fragment, useCallback, useEffect, useRef, useState } from 'react';
import { request } from '../../shared/api/client';
import { AuthContext } from './AuthContext';

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [checking, setChecking] = useState(Boolean(localStorage.getItem('token')));
  const [sessionError, setSessionError] = useState('');
  const sessionRevision = useRef(0);
  const clearSession = useCallback(() => {
    sessionRevision.current++;
    localStorage.removeItem('token');
    setUser(null);
    setSessionError('');
    setChecking(false);
  }, []);
  const logout = useCallback(
    async ({ all = false, localOnly = false } = {}) => {
      // Capture the existing bearer token before clearing it locally. Local cleanup
      // must still happen when an offline connection prevents server revocation.
      const revocation =
        !localOnly && localStorage.getItem('token')
          ? request(all ? '/auth/logout-all' : '/auth/logout', { method: 'POST' })
          : null;
      clearSession();
      if (!revocation) return { revoked: true };
      try {
        await revocation;
        return { revoked: true };
      } catch (error) {
        return { revoked: error.status === 401, error };
      }
    },
    [clearSession],
  );
  const refresh = useCallback(async () => {
    const revision = ++sessionRevision.current;
    const token = localStorage.getItem('token');
    if (!token) {
      setUser(null);
      setSessionError('');
      setChecking(false);
      return;
    }
    setSessionError('');
    try {
      const account = await request('/auth/validate');
      if (sessionRevision.current === revision && localStorage.getItem('token') === token)
        setUser(account);
    } catch (error) {
      if (sessionRevision.current === revision && localStorage.getItem('token') === token) {
        if (error.status === 401 || error.status === 403) clearSession();
        else setSessionError(error.message);
      }
    } finally {
      if (sessionRevision.current === revision || !localStorage.getItem('token'))
        setChecking(false);
    }
  }, [clearSession]);
  useEffect(() => {
    refresh();
    const onStorage = (event) => {
      if (event.key === 'token' || event.key === null) refresh();
    };
    const onVisible = () => {
      if (document.visibilityState === 'visible') refresh();
    };
    const timer = window.setInterval(onVisible, 120000);
    window.addEventListener('bidpro:session-expired', clearSession);
    window.addEventListener('storage', onStorage);
    window.addEventListener('focus', onVisible);
    document.addEventListener('visibilitychange', onVisible);
    return () => {
      window.clearInterval(timer);
      window.removeEventListener('bidpro:session-expired', clearSession);
      window.removeEventListener('storage', onStorage);
      window.removeEventListener('focus', onVisible);
      document.removeEventListener('visibilitychange', onVisible);
    };
  }, [clearSession, refresh]);
  const authenticate = async (endpoint, credentials) => {
    const startingToken = localStorage.getItem('token');
    const session = await request(endpoint, { method: 'POST', body: credentials, auth: false });
    if (!session?.token || !session?.user)
      throw new Error('The server returned an incomplete session. Please try again.');
    if (localStorage.getItem('token') !== startingToken)
      throw new Error('Your session changed in another tab. Please sign in again.');
    localStorage.setItem('token', session.token);
    sessionRevision.current++;
    setUser(session.user);
    setSessionError('');
    setChecking(false);
    return session.user;
  };
  return (
    <AuthContext.Provider value={{ user, checking, sessionError, refresh, logout, authenticate }}>
      <Fragment key={user?.userID || 'anonymous'}>{children}</Fragment>
    </AuthContext.Provider>
  );
}
