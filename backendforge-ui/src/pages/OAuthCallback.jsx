import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const OAuthCallback = ({ setIsAuthenticated }) => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Parse the token from the URL
    // The backend OAuthSuccessHandler redirects like: http://localhost:5173/oauth/callback?token=...
    const urlParams = new URLSearchParams(location.search);
    const token = urlParams.get('token');

    if (token) {
      localStorage.setItem('accessToken', token);
      setIsAuthenticated(true);
      navigate('/dashboard');
    } else {
      // Handle the error or go back to login
      console.error("No token found in OAuthCallback URL");
      navigate('/login');
    }
  }, [location, navigate, setIsAuthenticated]);

  return (
    <div className="auth-page">
      <div className="glass-panel" style={{ padding: '60px', textAlign: 'center' }}>
        <div className="loader" style={{ margin: '0 auto 20px', width: '40px', height: '40px' }}></div>
        <h2>Authenticating your signature...</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: '10px' }}>Completing OAuth Linkage</p>
      </div>
    </div>
  );
};

export default OAuthCallback;
