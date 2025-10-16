// src/api/admin.js
// File này chứa các hàm API dùng cho quản trị viên (admin) để quản lý các bài đăng (listing)

import axios from './axios.js'; // Import instance axios đã được cấu hình sẵn (baseURL, interceptor, v.v.)

/**
 * Lấy danh sách các bài đăng đang chờ duyệt
 * params: gồm page, size, sortBy, sortDirection
 */
export const getPendingListings = async (params = {}) => {
    // Giải nén giá trị mặc định cho các tham số nếu người dùng không truyền vào
    const {
        page = 0,
        size = 10,
        sortBy = 'createdDate',
        sortDirection = 'DESC'
    } = params;

    try {
        // Gửi request GET đến endpoint admin/post-request (API backend dùng để lấy danh sách cần duyệt)
        const response = await axios.get('/admin/post-request', {
            params: { page, size } // Truyền query parameters vào axios
        });

        // Lấy dữ liệu từ response, nếu không có thì dùng object rỗng
        const data = response.data || {};

        // Backend thường trả về object chứa các thuộc tính: content, totalElements, totalPages, size, number
        const content = data.content || data.data || []; // danh sách item thực tế
        const totalElements = data.totalElements ?? (data.length || 0); // tổng số phần tử
        const totalPages = data.totalPages ?? 1; // tổng số trang
        const pageSize = data.size ?? size; // kích thước trang hiện tại
        const pageNumber = data.number ?? page; // trang hiện tại

        // Chuyển đổi từng item trong danh sách thành object chuẩn để hiển thị trên UI
        const mapped = (Array.isArray(content) ? content : []).map(item => {
            const id = item.requestId; // ID của yêu cầu duyệt
            const listingId = item.listingId; // ID của bài đăng gốc
            const title = item.title ?? '—'; // tiêu đề bài đăng
            const price = typeof item.price === 'number'
                ? item.price
                : (item.price ? Number(item.price) : null); // chuyển giá sang số
            const thumbnail = item.thumbnailUrl || null; // ảnh thumbnail
            const status = item.status ?? 'PENDING'; // trạng thái bài đăng
            return { id, listingId, title, price, thumbnail, status, raw: item }; // trả về object chuẩn
        });

        // Trả về dữ liệu đã được chuẩn hóa
        return {
            content: mapped,
            totalElements,
            totalPages,
            size: pageSize,
            number: pageNumber
        };
    } catch (error) {
        // Nếu gọi API chính thất bại, log lỗi
        console.error('Admin API failed:', error);

        try {
            // Gọi dự phòng đến 2 endpoint lấy EV và battery listings
            const [evResponse, batteryResponse] = await Promise.all([
                axios.get('/evCart', { params: { page, size } }),
                axios.get('/batteryCart', { params: { page, size } })
            ]);

            // Lấy dữ liệu từ 2 response
            const evListings = evResponse.data.content || evResponse.data || [];
            const batteryListings = batteryResponse.data.content || batteryResponse.data || [];
            const allListings = [...evListings, ...batteryListings]; // Gộp lại

            // Tạm thời hiển thị tất cả listing để debug
            const filteredListings = allListings.filter(listing => true);

            // Chuyển đổi từng listing về dạng chuẩn để hiển thị
            const mapped = filteredListings.map(item => ({
                id: item.listingId,
                title: item.title ?? '—',
                price: typeof item.price === 'number'
                    ? item.price
                    : (item.price ? Number(item.price) : null),
                thumbnail: item.thumbnailUrl || null,
                status: item.status || 'ACTIVE',
                raw: item
            }));

            // Trả về kết quả dự phòng
            return {
                content: mapped,
                totalElements: mapped.length,
                totalPages: Math.ceil(mapped.length / size),
                size: size,
                number: page
            };
        } catch (fallbackError) {
            console.error('Fallback API also failed:', fallbackError);
            throw error; // Ném lỗi gốc ra ngoài
        }
    }
};

// Approve (chấp thuận) một bài đăng
export const approveListing = async (listingId, approvalNote = '') => {
    try {
        // Gọi endpoint approve-request/{id} để phê duyệt bài đăng
        const response = await axios.get(`/admin/approve-request/${listingId}`);
        return response.data;
    } catch (error) {
        console.error('Error approving listing:', error);
        throw error;
    }
};

// Reject (từ chối) một bài đăng
export const rejectListing = async (listingId, rejectionReason) => {
    // Kiểm tra lý do từ chối có tồn tại hay không
    if (!rejectionReason?.trim()) {
        throw new Error('Rejection reason is required');
    }

    try {
        // Gọi endpoint reject-request/{id}?reason=...
        const response = await axios.get(`/admin/reject-request/${listingId}`, {
            params: { reason: rejectionReason }
        });
        return response.data;
    } catch (error) {
        console.error('Error rejecting listing:', error);
        throw error;
    }
};

// Lấy chi tiết bài đăng (kể cả ở trạng thái PENDING)
export const getListingDetail = async (listingId) => {
    try {
        // Gọi endpoint admin riêng để xem chi tiết bài đăng
        const response = await axios.get(`/admin/listing-detail/${listingId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching listing detail:', error);
        throw error;
    }
};

// Lấy thống kê tổng quan cho trang dashboard admin
export const getAdminStats = async () => {
    try {
        // Gọi endpoint thống kê
        const response = await axios.get('/api/admin/stats');
        return response.data;
    } catch (error) {
        console.error('Error fetching admin stats:', error);
        throw error;
    }
};

// Hàm test/debug: Lấy tất cả EV và Battery listings
export const getAllListingsForDebug = async () => {
    try {
        console.log('Debug: Getting all listings from evCart and batteryCart...');

        // Gọi song song 2 endpoint
        const [evResponse, batteryResponse] = await Promise.all([
            axios.get('/evCart'),
            axios.get('/batteryCart')
        ]);

        const evListings = evResponse.data.content || evResponse.data || [];
        const batteryListings = batteryResponse.data.content || batteryResponse.data || [];
        const allListings = [...evListings, ...batteryListings];

        // Trả về thông tin chi tiết
        return {
            content: allListings,
            totalElements: allListings.length,
            evCount: evListings.length,
            batteryCount: batteryListings.length
        };
    } catch (error) {
        console.error('Debug API failed:', error);

        // Thử các endpoint khác nếu 2 cái trên lỗi
        const endpoints = ['/evCart', '/batteryCart', '/search', '/my-listings'];

        for (const endpoint of endpoints) {
            try {
                const response = await axios.get(endpoint);
                return response.data;
            } catch (err) {
                // Nếu lỗi thì thử endpoint tiếp theo
            }
        }

        // Nếu tất cả đều lỗi, ném lỗi ra
        throw error;
    }
};

// Lấy lịch sử xét duyệt (approve/reject)
export const getApprovalHistory = async (params = {}) => {
    const {
        page = 0,
        size = 10,
        status = null
    } = params;

    try {
        // Hiện chưa có endpoint riêng cho lịch sử, nên tạm dùng /admin/post-request
        const response = await axios.get('/admin/post-request', {
            params: { page, size }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching approval history:', error);
        throw error;
    }
};
