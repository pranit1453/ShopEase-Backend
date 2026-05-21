import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, User, UserCheck, ShieldAlert, CheckCircle2, ArrowRight } from 'lucide-react';

export const Auth = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, register, verifyOtp, resendOtp } = useAuth();

  const [activeTab, setActiveTab] = useState('login'); // 'login', 'register', 'otp'
  const [formData, setFormData] = useState({
    username: '',
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    gender: 'MALE',
    otp: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login(formData.username, formData.password);
      // Redirect to home or original page
      const from = location.state?.from?.pathname || '/';
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || 'Login failed. Invalid credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match!");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await register({
        username: formData.username,
        name: formData.name,
        email: formData.email,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
        gender: formData.gender
      });
      setSuccess("Account pre-registered! We have sent a verification OTP to your email.");
      setActiveTab('otp');
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await verifyOtp(formData.email, formData.otp);
      setSuccess("Email verified successfully! You can now log in.");
      setActiveTab('login');
    } catch (err) {
      setError(err.message || 'OTP verification failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    if (!formData.email) {
      setError("Please provide your email address first.");
      return;
    }
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await resendOtp(formData.email);
      setSuccess("A new OTP has been sent to your email.");
    } catch (err) {
      setError(err.message || 'Resending OTP failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      maxWidth: '450px',
      width: '100%',
      margin: '2rem auto',
      animation: 'fadeInUp var(--transition-normal) forwards'
    }}>
      <div className="card glass-panel" style={{ padding: '2rem' }}>
        {/* Logo / Header */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.8rem', fontWeight: 800 }}>Welcome to ShopEase</h2>
          <p style={{ color: 'hsl(var(--text-muted))', fontSize: '0.9rem', marginTop: '0.25rem' }}>
            {activeTab === 'login' && 'Sign in to access your cart, orders and profile'}
            {activeTab === 'register' && 'Create your new customer account'}
            {activeTab === 'otp' && 'Verify your email address'}
          </p>
        </div>

        {/* Tab Headers */}
        {activeTab !== 'otp' && (
          <div style={{
            display: 'flex',
            backgroundColor: 'hsl(var(--bg-surface))',
            borderRadius: 'var(--radius-md)',
            padding: '0.25rem',
            marginBottom: '2rem',
            border: '1px solid hsl(var(--border))'
          }}>
            <button
              onClick={() => { setActiveTab('login'); setError(null); setSuccess(null); }}
              style={{
                flex: 1,
                padding: '0.5rem',
                border: 'none',
                background: activeTab === 'login' ? 'hsl(var(--bg-secondary))' : 'transparent',
                color: activeTab === 'login' ? '#fff' : 'hsl(var(--text-muted))',
                borderRadius: 'var(--radius-sm)',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'var(--transition-fast)'
              }}
            >
              Sign In
            </button>
            <button
              onClick={() => { setActiveTab('register'); setError(null); setSuccess(null); }}
              style={{
                flex: 1,
                padding: '0.5rem',
                border: 'none',
                background: activeTab === 'register' ? 'hsl(var(--bg-secondary))' : 'transparent',
                color: activeTab === 'register' ? '#fff' : 'hsl(var(--text-muted))',
                borderRadius: 'var(--radius-sm)',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'var(--transition-fast)'
              }}
            >
              Sign Up
            </button>
          </div>
        )}

        {/* Alerts */}
        {error && (
          <div style={{
            padding: '0.75rem 1rem',
            backgroundColor: 'hsl(var(--error) / 0.1)',
            border: '1px solid hsl(var(--error) / 0.3)',
            borderRadius: 'var(--radius-md)',
            color: 'hsl(var(--error))',
            fontSize: '0.85rem',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}>
            <ShieldAlert size={16} />
            <span>{error}</span>
          </div>
        )}
        {success && (
          <div style={{
            padding: '0.75rem 1rem',
            backgroundColor: 'hsl(var(--success) / 0.1)',
            border: '1px solid hsl(var(--success) / 0.3)',
            borderRadius: 'var(--radius-md)',
            color: 'hsl(var(--success))',
            fontSize: '0.85rem',
            marginBottom: '1.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}>
            <CheckCircle2 size={16} />
            <span>{success}</span>
          </div>
        )}

        {/* LOGIN FORM */}
        {activeTab === 'login' && (
          <form onSubmit={handleLoginSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <div style={{ position: 'relative' }}>
                <User size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'hsl(var(--text-muted))' }} />
                <input
                  id="username"
                  name="username"
                  type="text"
                  required
                  placeholder="enter username"
                  value={formData.username}
                  onChange={handleInputChange}
                  className="input-field"
                  style={{ paddingLeft: '2.5rem' }}
                />
              </div>
            </div>

            <div className="form-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label htmlFor="password">Password</label>
                <Link to="/forgot-password" style={{ fontSize: '0.75rem', color: 'hsl(var(--primary))', textDecoration: 'none' }}>
                  Forgot password?
                </Link>
              </div>
              <div style={{ position: 'relative' }}>
                <Lock size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'hsl(var(--text-muted))' }} />
                <input
                  id="password"
                  name="password"
                  type="password"
                  required
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="input-field"
                  style={{ paddingLeft: '2.5rem' }}
                />
              </div>
            </div>

            <button type="submit" disabled={loading} className="btn btn-primary" style={{ marginTop: '0.5rem', width: '100%' }}>
              {loading ? 'Logging in...' : 'Sign In'} <ArrowRight size={16} />
            </button>
          </form>
        )}

        {/* REGISTER FORM */}
        {activeTab === 'register' && (
          <form onSubmit={handleRegisterSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="form-group">
              <label htmlFor="reg-name">Full Name</label>
              <input
                id="reg-name"
                name="name"
                type="text"
                required
                placeholder="John Doe"
                value={formData.name}
                onChange={handleInputChange}
                className="input-field"
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-username">Username</label>
              <input
                id="reg-username"
                name="username"
                type="text"
                required
                placeholder="johndoe12"
                value={formData.username}
                onChange={handleInputChange}
                className="input-field"
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-email">Email Address</label>
              <div style={{ position: 'relative' }}>
                <Mail size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'hsl(var(--text-muted))' }} />
                <input
                  id="reg-email"
                  name="email"
                  type="email"
                  required
                  placeholder="john@example.com"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="input-field"
                  style={{ paddingLeft: '2.5rem' }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem' }}>
              <div className="form-group" style={{ flex: 1.2 }}>
                <label htmlFor="reg-password">Password</label>
                <input
                  id="reg-password"
                  name="password"
                  type="password"
                  required
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="input-field"
                />
              </div>
              <div className="form-group" style={{ flex: 1.2 }}>
                <label htmlFor="reg-confirm">Confirm</label>
                <input
                  id="reg-confirm"
                  name="confirmPassword"
                  type="password"
                  required
                  placeholder="••••••••"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  className="input-field"
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="reg-gender">Gender</label>
              <select
                id="reg-gender"
                name="gender"
                value={formData.gender}
                onChange={handleInputChange}
                className="input-field"
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <button type="submit" disabled={loading} className="btn btn-primary" style={{ marginTop: '0.5rem', width: '100%' }}>
              {loading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>
        )}

        {/* OTP VERIFICATION FORM */}
        {activeTab === 'otp' && (
          <form onSubmit={handleOtpSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div style={{ textAlign: 'center', margin: '0.5rem 0' }}>
              <p style={{ fontSize: '0.85rem', color: 'hsl(var(--text-secondary))' }}>
                We sent a 6-digit OTP code to:
              </p>
              <p style={{ fontSize: '0.9rem', fontWeight: 700, color: 'hsl(var(--primary))', marginTop: '0.25rem' }}>
                {formData.email}
              </p>
            </div>

            <div className="form-group">
              <label htmlFor="otp-code">Verification OTP</label>
              <div style={{ position: 'relative' }}>
                <UserCheck size={16} style={{ position: 'absolute', left: '12px', top: '14px', color: 'hsl(var(--text-muted))' }} />
                <input
                  id="otp-code"
                  name="otp"
                  type="text"
                  maxLength={6}
                  required
                  placeholder="Enter 6-digit code"
                  value={formData.otp}
                  onChange={handleInputChange}
                  className="input-field"
                  style={{ paddingLeft: '2.5rem', letterSpacing: '0.25em', textAlign: 'center', fontSize: '1.1rem' }}
                />
              </div>
            </div>

            <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%' }}>
              {loading ? 'Verifying...' : 'Verify Email'}
            </button>

            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center', fontSize: '0.85rem' }}>
              <span style={{ color: 'hsl(var(--text-muted))' }}>Didn't receive code?</span>
              <button
                type="button"
                onClick={handleResendOtp}
                disabled={loading}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'hsl(var(--primary))',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
              >
                Resend Code
              </button>
            </div>
            
            <button
              type="button"
              onClick={() => { setActiveTab('login'); setError(null); setSuccess(null); }}
              className="btn btn-secondary"
              style={{ width: '100%', border: 'none', padding: '0.5rem' }}
            >
              Back to Sign In
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
export default Auth;
