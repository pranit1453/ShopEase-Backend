import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { User, Key, CheckCircle2, ShieldAlert, Mail, Send } from 'lucide-react';

export const Profile = () => {
  const { user, updateProfile, changePassword, requestEmailChange, verifyEmailChange } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) navigate('/auth');
  }, [user, navigate]);

  const [profileForm, setProfileForm] = useState({
    name: user?.name || '',
    gender: user?.gender || 'MALE'
  });

  const [passForm, setPassForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  // Email change — two-step: request OTP then verify
  const [emailStep, setEmailStep] = useState('input'); // 'input' | 'otp'
  const [emailForm, setEmailForm] = useState({ newEmail: '', otp: '' });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  if (!user) return null;

  const showAlert = (msg, isError = false) => {
    if (isError) setError(msg);
    else setSuccess(msg);
    setTimeout(() => { setError(null); setSuccess(null); }, 5000);
  };

  // ─── Update Profile ──────────────────────────────────────────────────────────
  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError(null); setSuccess(null);
    try {
      await updateProfile(profileForm.name, profileForm.gender);
      showAlert('Profile details updated successfully!');
    } catch (err) {
      showAlert(err.message || 'Failed to update profile.', true);
    } finally { setLoading(false); }
  };

  // ─── Change Password ─────────────────────────────────────────────────────────
  const handlePassSubmit = async (e) => {
    e.preventDefault();
    if (passForm.newPassword !== passForm.confirmPassword) {
      showAlert('New passwords do not match!', true); return;
    }
    setLoading(true); setError(null); setSuccess(null);
    try {
      await changePassword(passForm.oldPassword, passForm.newPassword, passForm.confirmPassword);
      showAlert('Password updated successfully!');
      setPassForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      showAlert(err.message || 'Failed to update password.', true);
    } finally { setLoading(false); }
  };

  // ─── Request Email Change OTP ────────────────────────────────────────────────
  // POST /auth/api/v1/user/update/email  { email: newEmail }
  const handleEmailRequest = async (e) => {
    e.preventDefault();
    if (!emailForm.newEmail) { showAlert('Please enter a new email address.', true); return; }
    setLoading(true); setError(null); setSuccess(null);
    try {
      await requestEmailChange(emailForm.newEmail);
      showAlert(`OTP sent to ${emailForm.newEmail}. Check your inbox.`);
      setEmailStep('otp');
    } catch (err) {
      showAlert(err.message || 'Failed to send email OTP.', true);
    } finally { setLoading(false); }
  };

  // ─── Verify Email OTP & Confirm Change ──────────────────────────────────────
  // POST /auth/api/v1/user/email/verify  { newEmail, otp }
  const handleEmailVerify = async (e) => {
    e.preventDefault();
    if (!emailForm.otp) { showAlert('Please enter the OTP code.', true); return; }
    setLoading(true); setError(null); setSuccess(null);
    try {
      await verifyEmailChange(emailForm.newEmail, emailForm.otp);
      showAlert('Email address updated successfully!');
      setEmailForm({ newEmail: '', otp: '' });
      setEmailStep('input');
    } catch (err) {
      showAlert(err.message || 'OTP verification failed.', true);
    } finally { setLoading(false); }
  };

  const sectionTitle = (Icon, label) => (
    <h2 style={{ fontSize: '1.15rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
      <Icon size={18} style={{ color: 'hsl(var(--primary))' }} /> {label}
    </h2>
  );

  return (
    <div className="animate-fade-in" style={{ maxWidth: '860px', margin: '1rem auto', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', borderBottom: '1px solid hsl(var(--border))', paddingBottom: '1.25rem' }}>
        <div style={{ width: '60px', height: '60px', borderRadius: '50%', background: 'linear-gradient(135deg, hsl(var(--primary)), hsl(var(--warning)))', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem', fontWeight: 800, color: '#fff' }}>
          {user.name?.charAt(0).toUpperCase()}
        </div>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>{user.name}</h1>
          <p style={{ color: 'hsl(var(--text-muted))', fontSize: '0.9rem' }}>@{user.username} · {user.gender}</p>
        </div>
      </div>

      {/* Global Alerts */}
      {error && (
        <div style={{ padding: '0.75rem 1rem', backgroundColor: 'hsl(var(--error) / 0.1)', border: '1px solid hsl(var(--error) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--error))', fontSize: '0.85rem', display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <ShieldAlert size={16} /> {error}
        </div>
      )}
      {success && (
        <div style={{ padding: '0.75rem 1rem', backgroundColor: 'hsl(var(--success) / 0.1)', border: '1px solid hsl(var(--success) / 0.3)', borderRadius: 'var(--radius-md)', color: 'hsl(var(--success))', fontSize: '0.85rem', display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <CheckCircle2 size={16} /> {success}
        </div>
      )}

      {/* Row 1: Profile + Password */}
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '2rem' }}>
        {/* Personal Details */}
        <div className="card glass-panel" style={{ flex: '1 1 340px' }}>
          {sectionTitle(User, 'Personal Details')}
          <form onSubmit={handleProfileSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="form-group">
              <label>Username (read-only)</label>
              <input disabled type="text" value={user.username} className="input-field"
                style={{ opacity: 0.5, cursor: 'not-allowed' }} />
            </div>
            <div className="form-group">
              <label htmlFor="prof-name">Full Name</label>
              <input id="prof-name" required type="text" placeholder="Your Name"
                value={profileForm.name} onChange={(e) => setProfileForm({ ...profileForm, name: e.target.value })}
                className="input-field" />
            </div>
            <div className="form-group">
              <label htmlFor="prof-gender">Gender</label>
              <select id="prof-gender" value={profileForm.gender}
                onChange={(e) => setProfileForm({ ...profileForm, gender: e.target.value })}
                className="input-field">
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <button type="submit" disabled={loading} className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>
              Update Info
            </button>
          </form>
        </div>

        {/* Change Password */}
        <div className="card glass-panel" style={{ flex: '1 1 340px' }}>
          {sectionTitle(Key, 'Change Password')}
          <form onSubmit={handlePassSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="form-group">
              <label htmlFor="old-pass">Current Password</label>
              <input id="old-pass" required type="password" placeholder="••••••••"
                value={passForm.oldPassword} onChange={(e) => setPassForm({ ...passForm, oldPassword: e.target.value })}
                className="input-field" />
            </div>
            <div className="form-group">
              <label htmlFor="new-pass">New Password</label>
              <input id="new-pass" required type="password" placeholder="••••••••"
                value={passForm.newPassword} onChange={(e) => setPassForm({ ...passForm, newPassword: e.target.value })}
                className="input-field" />
            </div>
            <div className="form-group">
              <label htmlFor="confirm-pass">Confirm New Password</label>
              <input id="confirm-pass" required type="password" placeholder="••••••••"
                value={passForm.confirmPassword} onChange={(e) => setPassForm({ ...passForm, confirmPassword: e.target.value })}
                className="input-field" />
            </div>
            <button type="submit" disabled={loading} className="btn btn-primary" style={{ alignSelf: 'flex-start' }}>
              Update Password
            </button>
          </form>
        </div>
      </div>

      {/* Row 2: Email Change */}
      <div className="card glass-panel">
        {sectionTitle(Mail, 'Change Email Address')}
        <p style={{ fontSize: '0.85rem', color: 'hsl(var(--text-muted))', marginBottom: '1.25rem' }}>
          Enter a new email address. We will send a one-time verification code to confirm the change.
        </p>

        {emailStep === 'input' ? (
          /* Step 1: Enter new email */
          <form onSubmit={handleEmailRequest} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div className="form-group" style={{ flex: '1 1 300px' }}>
              <label htmlFor="new-email">New Email Address</label>
              <input
                id="new-email"
                required
                type="email"
                placeholder="newemail@example.com"
                value={emailForm.newEmail}
                onChange={(e) => setEmailForm({ ...emailForm, newEmail: e.target.value })}
                className="input-field"
              />
            </div>
            <button type="submit" disabled={loading} className="btn btn-primary"
              style={{ alignSelf: 'flex-end', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.05rem' }}>
              <Send size={16} /> Send OTP
            </button>
          </form>
        ) : (
          /* Step 2: Enter OTP */
          <div>
            <div style={{ padding: '0.6rem 1rem', background: 'hsl(var(--primary) / 0.08)', border: '1px solid hsl(var(--primary) / 0.3)', borderRadius: 'var(--radius-md)', marginBottom: '1.25rem', fontSize: '0.85rem', color: 'hsl(var(--text-secondary))' }}>
              OTP sent to <strong>{emailForm.newEmail}</strong>.{' '}
              <button onClick={() => setEmailStep('input')} style={{ background: 'none', border: 'none', color: 'hsl(var(--primary))', cursor: 'pointer', fontSize: '0.85rem', textDecoration: 'underline' }}>
                Change email
              </button>
            </div>
            <form onSubmit={handleEmailVerify} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
              <div className="form-group" style={{ flex: '1 1 220px' }}>
                <label htmlFor="email-otp">Enter OTP Code</label>
                <input
                  id="email-otp"
                  required
                  type="text"
                  placeholder="6-digit OTP"
                  maxLength={8}
                  value={emailForm.otp}
                  onChange={(e) => setEmailForm({ ...emailForm, otp: e.target.value })}
                  className="input-field"
                />
              </div>
              <button type="submit" disabled={loading} className="btn btn-primary"
                style={{ alignSelf: 'flex-end', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.05rem' }}>
                <CheckCircle2 size={16} /> Verify & Update Email
              </button>
            </form>
          </div>
        )}
      </div>

      {/* User Roles display */}
      <div className="card glass-panel">
        <h2 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Account Roles & Permissions</h2>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          {user.roles?.map(role => (
            <span key={role} className="badge badge-success" style={{ fontSize: '0.8rem', padding: '0.35rem 0.85rem' }}>
              {role}
            </span>
          ))}
          {user.permissions?.slice(0, 8).map(perm => (
            <span key={perm} style={{ fontSize: '0.7rem', padding: '0.25rem 0.65rem', borderRadius: 'var(--radius-full)', background: 'hsl(var(--primary) / 0.1)', border: '1px solid hsl(var(--primary) / 0.3)', color: 'hsl(var(--primary))' }}>
              {perm}
            </span>
          ))}
          {(user.permissions?.length || 0) > 8 && (
            <span style={{ fontSize: '0.7rem', color: 'hsl(var(--text-muted))', padding: '0.25rem 0.5rem' }}>
              +{user.permissions.length - 8} more
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
export default Profile;
