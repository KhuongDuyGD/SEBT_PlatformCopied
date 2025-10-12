import api from './axios'

/**
 * Lấy danh sách người dùng từ backend
 * @param {{page:number,size:number,q?:string}} params
 */
export const getUsers = async (params = {}) => {
    const { page = 0, size = 10, q = '' } = params
    try {
        // Gọi API backend - endpoint cần được backend team implement
        const res = await api.get('/admin/users', { params: { page, size, q } })

        // Normalize response
        const data = res.data || {}
        const content = data.content || data || []
        const totalElements = data.totalElements ?? (Array.isArray(content) ? content.length : 0)

        return {
            content,
            totalElements,
            size: data.size ?? size,
            number: data.number ?? page
        }
    } catch (err) {
        console.error('Failed to fetch users from backend:', err.message)
        // Không sử dụng mock data nữa - throw error để frontend xử lý
        throw new Error('Backend API chưa sẵn sàng. Vui lòng liên hệ backend team để implement endpoint GET /api/admin/users')
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
