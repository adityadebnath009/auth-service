import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Activity, LogOut, User, Menu } from 'lucide-react';
import api from '../api';

const Navbar = ({ isAuthenticated, setIsAuthenticated }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (err) {
      console.error('Logout error UI side', err);
    } finally {
      localStorage.removeItem('accessToken');
      setIsAuthenticated(false);
      navigate('/login');
    }
  };

  return (
    <nav className="navbar">
      <Link to="/" className="nav-logo">
        BackendForge
      </Link>
      
      <div className="nav-links">
        {isAuthenticated ? (
          <>
            <Link to="/dashboard" className={`nav-link ${location.pathname === '/dashboard' ? 'active' : ''}`}>
              <Activity size={18} style={{ display: 'inline', marginRight: '6px', verticalAlign: 'text-bottom' }} /> Dashboard
            </Link>
            <Link to="/profile" className={`nav-link ${location.pathname === '/profile' ? 'active' : ''}`}>
              <User size={18} style={{ display: 'inline', marginRight: '6px', verticalAlign: 'text-bottom' }} /> Profile
            </Link>
            <button onClick={handleLogout} className="btn-secondary" style={{ padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', background: 'transparent', border: '1px solid rgba(255,255,255,0.1)', color: 'white' }}>
              <LogOut size={16} style={{ display: 'inline', marginRight: '6px', verticalAlign: 'text-bottom' }} /> Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className={`nav-link ${location.pathname === '/login' ? 'active' : ''}`}>Login</Link>
            <Link to="/register" className={`nav-link ${location.pathname === '/register' ? 'active' : ''}`}>Sign Up</Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
