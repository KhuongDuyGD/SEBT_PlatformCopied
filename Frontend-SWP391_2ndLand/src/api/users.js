import api from './axios'
import { mockUsers } from '../data/mockData'

/**
 * Lấy danh sách người dùng (với fallback về mock data nếu API không có)
 * @param {{page:number,size:number,q?:string}} params
 */
export const getUsers = async (params = {}) => {
    const { page = 0, size = 10, q = '' } = params
    try {
        const res = await api.get('/users', { params: { page, size, q } })
        // Try to normalize response
        const data = res.data || {}
        const content = data.content || data || []
        const totalElements = data.totalElements ?? (Array.isArray(content) ? content.length : 0)
        return { content, totalElements, size: data.size ?? size, number: data.number ?? page }
    } catch (err) {
        console.warn('getUsers API failed, falling back to mockUsers', err.message)
        // Simple pagination of mock data
        const start = page * size
        const end = start + size
        const filtered = q
            ? mockUsers.filter(u => (u.name + ' ' + u.email).toLowerCase().includes(q.toLowerCase()))
            : mockUsers
        return { content: filtered.slice(start, end), totalElements: filtered.length, size, number: page }
    }
}

export const blockUser = async (userId) => {
    try {
        const res = await api.post(`/admin/users/${userId}/block`)
        return res.data
    } catch (err) {
        console.warn('blockUser API failed, simulating success', err.message)
        return { success: true }
    }
}

export const unblockUser = async (userId) => {
    try {
        const res = await api.post(`/admin/users/${userId}/unblock`)
        return res.data
    } catch (err) {
        console.warn('unblockUser API failed, simulating success', err.message)
        return { success: true }
    }
}
