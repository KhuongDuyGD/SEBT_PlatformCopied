import axios from 'axios';

// Ưu tiên biến môi trường để dễ cấu hình khi deploy
const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

const api = axios.create({
    baseURL,
    withCredentials: true,
    // Không set 'Content-Type' cố định để FormData tự sinh boundary; request json sẽ được axios tự gán.
    headers: { 'Accept': 'application/json' },
});

// Unified error normalization
function normalizeError(error) {
    if (error.response) {
        const { status, data, config } = error.response
        return {
            status,
            message: data?.message || data?.error || data?.reason || (typeof data === 'string' ? data : 'Request failed'),
            path: config?.url,
            raw: data,
        }
    }
    if (error.request) {
        return { status: 0, message: 'No response from server', path: error.config?.url, raw: null }
    }
    return { status: 0, message: error.message || 'Unknown error', path: undefined, raw: null }
}

// Response interceptor for auth + error normalization
api.interceptors.response.use(
    (res) => res,
    (error) => {
        const norm = normalizeError(error)
        if (norm.status === 401) {
            // Avoid infinite loop if already on login
            if (!window.location.pathname.startsWith('/login')) {
                // Optionally keep intended path
                const redirect = encodeURIComponent(window.location.pathname + window.location.search)
                window.location.href = `/login?redirect=${redirect}`
            }
        }
        return Promise.reject(norm)
    }
)

export { normalizeError }
export default api;