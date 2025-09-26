import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',  // Thay bằng URL backend (ví dụ nếu deploy thì đổi)
    withCredentials: true,  // Bắt buộc cho session cookie
    headers: { 'Content-Type': 'application/json' },
});

export default api;