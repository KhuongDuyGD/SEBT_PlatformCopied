import axios from 'axios';

// Ưu tiên biến môi trường để dễ cấu hình khi deploy
const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

const api = axios.create({
    baseURL,
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
});

export default api;