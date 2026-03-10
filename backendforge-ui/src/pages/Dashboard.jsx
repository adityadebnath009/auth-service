import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Monitor, Smartphone, Globe, Clock, Server, Shield } from 'lucide-react';
import api from '../api';

const Dashboard = () => {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSessions();
  }, []);

  const fetchSessions = async () => {
    try {
      setLoading(true);
      const res = await api.get('/auth/sessions');
      setSessions(res.data);
    } catch (err) {
      console.error(err);
      setError('Could not fetch sessions data.');
    } finally {
      setLoading(false);
    }
  };

  const getDeviceIcon = (deviceName) => {
    const name = deviceName?.toLowerCase() || '';
    if (name.includes('mobile') || name.includes('phone') || name.includes('android') || name.includes('iphone') || name.includes('ios')) {
      return <Smartphone size={24} style={{ color: 'var(--primary-glow)' }} />;
    }
    return <Monitor size={24} style={{ color: 'var(--primary-glow)' }} />;
  };

  const terminateSession = async (sessionId) => {
    try {
      await api.delete(`/auth/sessions/${sessionId}`);
      setSessions(sessions.filter(s => s.sessionId !== sessionId));
    } catch (err) {
      console.error("Error terminating session", err);
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      style={{ padding: '40px 20px', flex: 1 }}
      className="container"
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' }}>
        <div>
          <h1 style={{ fontSize: '2.5rem', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '12px' }}>
            <Server style={{ color: 'var(--primary-glow)' }} size={32} /> Central Hub
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>Monitor your active sessions and connected devices.</p>
        </div>
      </div>

      {error && <div className="message error">{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px' }}>
        {/* Metric Cards */}
        <motion.div whileHover={{ y: -5 }} className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '20px' }}>
            <h3 style={{ color: 'var(--text-secondary)', fontWeight: 500 }}>Active Sessions</h3>
            <div style={{ padding: '8px', background: 'rgba(0,255,204,0.1)', borderRadius: '12px' }}>
              <Globe style={{ color: 'var(--primary-glow)' }} size={20} />
            </div>
          </div>
          <div style={{ fontSize: '3rem', fontWeight: 800 }}>{sessions.length}</div>
        </motion.div>

        <motion.div whileHover={{ y: -5 }} className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '20px' }}>
            <h3 style={{ color: 'var(--text-secondary)', fontWeight: 500 }}>Security Status</h3>
            <div style={{ padding: '8px', background: 'rgba(0,255,102,0.1)', borderRadius: '12px' }}>
              <Shield style={{ color: 'var(--success)' }} size={20} />
            </div>
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--success)' }}>Secured</div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '8px' }}>JWT & OAuth2 Activated</p>
        </motion.div>
      </div>

      {/* Sessions List */}
      <div style={{ marginTop: '40px' }}>
        <h2 style={{ fontSize: '1.8rem', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Clock size={24} style={{ color: 'var(--secondary-glow)' }} /> Recent Activity
        </h2>

        {loading ? (
          <div className="loader" style={{ margin: '40px auto' }}></div>
        ) : sessions.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
            No active sessions found.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {sessions.map((session, idx) => (
              <motion.div 
                key={session.sessionId}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: idx * 0.1 }}
                className="glass-panel" 
                style={{ 
                  padding: '24px', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  background: 'rgba(255,255,255,0.02)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                  <div style={{ padding: '12px', background: 'rgba(0,0,0,0.4)', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)' }}>
                    {getDeviceIcon(session.deviceName)}
                  </div>
                  <div>
                    <h4 style={{ fontSize: '1.1rem', marginBottom: '4px' }}>{session.deviceName || 'Unknown Device'}</h4>
                    <div style={{ display: 'flex', gap: '16px', color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                      <span>IP: <span style={{ color: 'white' }}>{session.ipAddress}</span></span>
                      <span>Started: <span style={{ color: 'white' }}>{new Date(session.createdDate).toLocaleDateString()}</span></span>
                    </div>
                  </div>
                </div>

                <button 
                  onClick={() => terminateSession(session.sessionId)}
                  className="btn-secondary" 
                  style={{ 
                    padding: '8px 16px', 
                    borderRadius: '8px', 
                    fontSize: '0.9rem',
                    color: 'var(--error)',
                    borderColor: 'rgba(255,51,51,0.2)'
                  }}
                  onMouseOver={(e) => {
                    e.target.style.background = 'rgba(255,51,51,0.1)';
                    e.target.style.borderColor = 'var(--error)';
                  }}
                  onMouseOut={(e) => {
                    e.target.style.background = 'transparent';
                    e.target.style.borderColor = 'rgba(255,51,51,0.2)';
                  }}
                >
                  Revoke
                </button>
              </motion.div>
            ))}
          </div>
        )}
      </div>

    </motion.div>
  );
};

export default Dashboard;
