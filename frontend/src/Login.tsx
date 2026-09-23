import React, { useState } from 'react';
import { IconLock, IconUser } from './icons';

export type AuthUser = { token: string; userId: number; username: string; fullName: string; roles: string[] };

const API = 'http://localhost:8080/api';

export default function Login({ onLogin }: { onLogin: (u: AuthUser) => void }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!username.trim() || !password) { setError('Username and password are both required.'); return; }
    setBusy(true);
    try {
      const r = await fetch(API + '/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: username.trim(), password }),
      });
      const data = await r.json().catch(() => ({}));
      if (!r.ok) throw new Error(data.message || 'Invalid username or password');
      onLogin({ token: data.token, userId: data.userId, username: data.username, fullName: data.fullName, roles: data.roles || [] });
    } catch (x: any) {
      setError(x.message || 'Login failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="watercolor-banner">
          <div className="watercolor-banner__content">
            <h1>RbcTcsWorld</h1>
            <p>PharmaPack QMS &middot; Pharmaceutical Packaging Quality Management</p>
          </div>
        </div>
        <div className="login-card__body">
          <h2>Sign in</h2>
          <p className="subtitle">Log in to access the packaging QMS dashboard.</p>
          {error && <div className="login-error">{error}</div>}
          <form className="login-form" onSubmit={submit}>
            <label><IconUser size={14} /> Username
              <input value={username} onChange={e => setUsername(e.target.value)} autoFocus autoComplete="username" />
            </label>
            <label><IconLock size={14} /> Password
              <input type="password" value={password} onChange={e => setPassword(e.target.value)} autoComplete="current-password" />
            </label>
            <button className="primary btn-icon" disabled={busy}><IconLock size={16} />{busy ? 'Signing in…' : 'Sign In'}</button>
          </form>
          <div className="login-hint">
            <strong>Demo credentials</strong> (portfolio/training data only):<br />
            admin / admin123 &middot; qa_user / qa123 &middot; supervisor / super123<br />
            operator / operator123 &middot; inspector / inspect123
          </div>
        </div>
      </div>
    </div>
  );
}
