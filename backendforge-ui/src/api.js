import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // Crucial for sending/receiving refresh_token HTTP-only cookie
});

// Request interceptor to attach access token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle 401s and refresh token
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Prevent infinite loops if refresh fails
    if (error.response?.status === 401 && !originalRequest._retry && originalRequest.url !== '/auth/refresh') {
      originalRequest._retry = true;

      try {
        // We rely on the HTTP-only cookie to be sent automatically here
        const res = await api.post('/auth/refresh');
        const newAccessToken = res.data.token; // It seems backend LoginResponseDTO has the token inside it, lets check the field name mapping carefully. Usually it just returns {"token": "..."} or similar. Looking at LoginResponseDTO, it returns accessToken.
        
        // Wait, looking at the backend code: public record LoginResponseDTO(String accessToken) {}. So it maps to { "accessToken": "..." }
        const newAccessTokenFixed = res.data.accessToken;
        
        if (newAccessTokenFixed) {
            localStorage.setItem('accessToken', newAccessTokenFixed);
            originalRequest.headers['Authorization'] = `Bearer ${newAccessTokenFixed}`;
            return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh token is invalid/expired
        console.error('Session expired, please log in again.');
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
