import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, Lock, Github, CheckCircle2 } from 'lucide-react';
import api from '../api';

const Login = ({ setIsAuthenticated }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await api.post('/auth/login', { email, password });
      const { accessToken } = response.data;
      
      if (accessToken) {
        localStorage.setItem('accessToken', accessToken);
        setIsAuthenticated(true);
        navigate('/dashboard');
      }
    } catch (err) {
      if (err.response?.status === 401) {
        setError('Invalid credentials');
      } else if (err.response?.status === 400) {
        setError('Please verify your email or check credentials');
      } else {
        setError('Login failed. Please try again later.');
      }
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOAuthLogin = (provider) => {
      // The backend redirects logic
      window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
  };

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -20 }}
      className="auth-page"
    >
      <div className="glass-panel auth-card">
        <h1>Welcome Back</h1>
        <p className="subtitle">Enter your details to access your forge.</p>

        {error && <div className="message error">{error}</div>}

        <form onSubmit={handleLogin}>
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
                required
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" disabled={isLoading}>
            {isLoading ? <div className="loader" style={{ width: '18px', height: '18px' }}></div> : 'Sign In To Forge'}
          </button>
        </form>

        <div className="auth-divider">OR CONTINUE WITH</div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
          <button type="button" className="btn btn-oauth" onClick={() => handleOAuthLogin('google')}>
           <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/></svg>
            Google
          </button>
          <button type="button" className="btn btn-oauth" onClick={() => handleOAuthLogin('github')}>
            <Github size={20} />
            GitHub
          </button>
        </div>

        <div className="auth-links">
          New to BackendForge? <Link to="/register">Create an account</Link>
        </div>
      </div>
    </motion.div>
  );
};

export default Login;
