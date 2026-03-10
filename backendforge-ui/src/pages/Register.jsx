import { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, Lock, User, TerminalSquare } from 'lucide-react';
import api from '../api';

const Register = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setIsLoading(true);

    try {
      const response = await api.post('/auth/register', { name, email, password });
      if (response.status === 201) {
        setSuccess('Account created! Please check your email for the verification link.');
        setName('');
        setEmail('');
        setPassword('');
      }
    } catch (err) {
      if (err.response?.status === 400 && err.response?.data?.email) {
        setError(err.response.data.email);
      } else {
        setError(err.response?.data || 'Registration failed. Please try again.');
        if(typeof err.response?.data === 'object') {
           setError('Invalid data or email already taken');
        }
      }
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className="auth-page"
    >
      <div className="glass-panel auth-card">
        <h1>Forge Your Account</h1>
        <p className="subtitle">Join the realm and start building.</p>

        {error && <div className="message error">{error}</div>}
        {success && <div className="message success">{success}</div>}

        <form onSubmit={handleRegister}>

          <div className="input-group">
            <label>Name</label>
            <div style={{ position: 'relative' }}>
              <User size={18} style={{ position: 'absolute', top: '14px', left: '16px', color: 'rgba(255,255,255,0.4)' }} />
              <input 
                type="text" 
                className="input-field" 
                placeholder="Developer"
                style={{ paddingLeft: '44px' }}
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label>Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={18} style={{ position: 'absolute', top: '14px', left: '16px', color: 'rgba(255,255,255,0.4)' }} />
              <input 
                type="email" 
                className="input-field" 
                placeholder="developer@forge.com"
                style={{ paddingLeft: '44px' }}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label>Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} style={{ position: 'absolute', top: '14px', left: '16px', color: 'rgba(255,255,255,0.4)' }} />
              <input 
                type="password" 
                className="input-field" 
                placeholder="••••••••"
                style={{ paddingLeft: '44px' }}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                minLength="6"
                required
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" disabled={isLoading}>
            {isLoading ? <div className="loader" style={{ width: '18px', height: '18px' }}></div> : (
              <>
                <TerminalSquare size={18} /> Intialize Account
              </>
            )}
          </button>
        </form>

        <div className="auth-links" style={{marginTop: '30px'}}>
          Already have an account? <Link to="/login">Sign In</Link>
        </div>
      </div>
    </motion.div>
  );
};

export default Register;
