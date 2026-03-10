import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { User as UserIcon, Mail, ShieldAlert, LogOut, Code, AlertTriangle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

const Profile = ({ setIsAuthenticated }) => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const res = await api.get('/user/me');
      setProfile(res.data);
    } catch (err) {
      console.error(err);
      setError('Could not fetch profile details.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogoutAll = async () => {
    try {
      await api.post('/auth/logout-all');
      localStorage.removeItem('accessToken');
      setIsAuthenticated(false);
      navigate('/login');
    } catch (err) {
      console.error('Logout all error', err);
      alert('Could not log out from all devices at this time.');
    }
  };

  if (loading) return <div className="loader" style={{ margin: 'auto', marginTop: '20vh' }}></div>;
  if (error) return <div className="message error" style={{ margin: '40px auto', maxWidth: '400px' }}>{error}</div>;
  if (!profile) return null;

  return (
    <motion.div 
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -30 }}
      style={{ padding: '60px 20px', flex: 1, display: 'flex', justifyContent: 'center' }}
      className="container"
    >
      <div className="glass-panel" style={{ width: '100%', maxWidth: '600px', padding: '40px', overflow: 'hidden', position: 'relative' }}>
        
        {/* Background Decorative Glow in Profile */}
        <div style={{
          position: 'absolute',
          top: '-50px',
          right: '-50px',
          width: '200px',
          height: '200px',
          background: 'radial-gradient(circle, rgba(255,0,204,0.15) 0%, transparent 70%)',
          borderRadius: '50%',
          filter: 'blur(30px)',
          zIndex: 0
        }}></div>

        <div style={{ position: 'relative', zIndex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '24px', marginBottom: '40px' }}>
            <div style={{ 
              width: '80px', 
              height: '80px', 
              borderRadius: '24px', 
              background: 'linear-gradient(135deg, rgba(0,255,204,0.2), rgba(255,0,204,0.2))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '1px solid rgba(255,255,255,0.1)'
            }}>
              <UserIcon size={40} style={{ color: 'var(--text-primary)' }} />
            </div>
            <div>
              <h1 style={{ fontSize: '2rem', marginBottom: '8px' }}>{profile.name}</h1>
              <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                <span style={{ 
                  background: 'rgba(0,0,0,0.5)', 
                  padding: '6px 12px', 
                  borderRadius: '20px', 
                  fontSize: '0.85rem',
                  border: '1px solid rgba(255,255,255,0.05)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  color: 'var(--text-secondary)'
                }}>
                  <Mail size={14} /> {profile.email}
                </span>
                {profile.roles?.map(role => (
                  <span key={role} style={{ 
                    background: role === 'ROLE_ADMIN' ? 'rgba(255,51,51,0.15)' : 'rgba(0,255,204,0.15)', 
                    color: role === 'ROLE_ADMIN' ? 'var(--error)' : 'var(--primary-glow)',
                    padding: '6px 12px', 
                    borderRadius: '20px', 
                    fontSize: '0.85rem',
                    border: `1px solid ${role === 'ROLE_ADMIN' ? 'rgba(255,51,51,0.3)' : 'rgba(0,255,204,0.3)'}`,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    fontWeight: 600
                  }}>
                    {role === 'ROLE_ADMIN' ? <ShieldAlert size={14} /> : <Code size={14} />}
                    {role.replace('ROLE_', '')}
                  </span>
                ))}
              </div>
            </div>
          </div>

          <div style={{ marginBottom: '40px' }}>
            <h3 style={{ color: 'var(--text-secondary)', marginBottom: '16px', fontSize: '1rem', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '1px' }}>Account Details</h3>
            
            <div style={{ background: 'rgba(0,0,0,0.3)', borderRadius: '16px', padding: '20px', border: '1px solid rgba(255,255,255,0.05)' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: '12px', marginBottom: '16px' }}>
                <span style={{ color: 'var(--text-secondary)' }}>Account ID</span>
                <span style={{ fontFamily: 'monospace', color: 'rgba(255,255,255,0.8)' }}>{profile.id}</span>
              </div>
              <div style={{ height: '1px', background: 'rgba(255,255,255,0.05)', margin: '0 0 16px 0' }}></div>
              <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: '12px' }}>
                <span style={{ color: 'var(--text-secondary)' }}>Member Since</span>
                <span style={{ color: 'rgba(255,255,255,0.9)' }}>
                  {new Date(profile.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}
                </span>
              </div>
            </div>
          </div>

          <div style={{ background: 'rgba(255,51,51,0.05)', border: '1px solid rgba(255,51,51,0.2)', borderRadius: '16px', padding: '24px' }}>
            <h3 style={{ color: 'var(--error)', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <AlertTriangle size={20} /> Danger Zone
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', marginBottom: '20px' }}>
              You can instantly revoke access to all devices and sessions currently connected to this account. This will force a sign out everywhere.
            </p>
            <button onClick={handleLogoutAll} className="btn" style={{ background: 'var(--error)', color: 'white' }}>
              <LogOut size={18} /> Logout All Sessions
            </button>
          </div>

        </div>
      </div>
    </motion.div>
  );
};

export default Profile;
