// src/api/admin.js
// API functions cho admin management

import axios from './axios.js';

/**
 * Lấy danh sách listing đang chờ xét duyệt
 * @param {Object} params - Query parameters
 * @param {number} params.page - Trang hiện tại (default: 0)
 * @param {number} params.size - Số lượng items per page (default: 10)
 * @param {string} params.sortBy - Field để sort (default: createdDate)
 * @param {string} params.sortDirection - ASC hoặc DESC (default: DESC)
 * @returns {Promise} Response với danh sách pending listings
 */
export const getPendingListings = async (params = {}) => {
    const {
        page = 0,
        size = 10,
        sortBy = 'createdDate',
        sortDirection = 'DESC'
    } = params;

    try {
        // Use admin endpoint that returns post requests for admin review
        console.log('Trying admin API: /admin/post-request');
        const response = await axios.get('/admin/post-request', {
            params: {
                page,
                size
            }
        });

        console.log('Admin post-request response:', response.data);

        // Normalize response to a { content: [], totalElements, totalPages, size, number }
        const data = response.data || {};
        // When Spring serializes a Page object it usually contains: content, totalElements, totalPages, size, number
        const content = data.content || data.data || [];
        const totalElements = data.totalElements ?? (data.length || 0);
        const totalPages = data.totalPages ?? 1;
        const pageSize = data.size ?? size;
        const pageNumber = data.number ?? page;

        // Map PostListingCartResponseDTO -> listing-like object the UI expects
        const mapped = (Array.isArray(content) ? content : []).map(item => {
            // item may use camelCase or PascalCase (ListingId)
            const id = item.requestId ?? item.requestID;
            const listingId = item.ListingId;
            const title = item.title ?? '—';
            const price = typeof item.price === 'number' ? item.price : (item.price ? Number(item.price) : null);
            const thumbnail = item.thumbnailUrl || null;
            const status = item.status ?? 'PENDING';
            return { id, listingId, title, price, thumbnail, status, raw: item };
        });

        return {
            content: mapped,
            totalElements,
            totalPages,
            size: pageSize,
            number: pageNumber
        };
    } catch (error) {
        console.error('Admin API failed:', error);

        // Fallback: lấy tất cả listings từ cả evCart và batteryCart
        try {
            console.log('Trying fallback APIs: /evCart and /batteryCart');

            // Get both EV and battery listings
            const [evResponse, batteryResponse] = await Promise.all([
                axios.get('/evCart', {
                    params: { page, size }
                }),
                axios.get('/batteryCart', {
                    params: { page, size }
                })
            ]);

            console.log('EV listings response:', evResponse.data);
            console.log('Battery listings response:', batteryResponse.data);

            // Combine both responses
            const evListings = evResponse.data.content || evResponse.data || [];
            const batteryListings = batteryResponse.data.content || batteryResponse.data || [];
            const allListings = [...evListings, ...batteryListings];

            console.log('Combined listings:', allListings);

            // Filter logic: giả sử những listing mới tạo cần được duyệt
            // Hoặc những listing có status khác "APPROVED"
            const filteredListings = allListings.filter(listing => {
                // Có thể filter theo:
                // 1. Status không phải APPROVED
                // 2. Listing mới tạo trong vòng X ngày
                // 3. Listing chưa có admin review

                const status = listing.status || listing.listingStatus;

                // Tạm thời hiển thị tất cả để debug
                return true; // Sẽ sửa logic này sau khi biết cấu trúc data
            });

            console.log('Filtered listings for approval:', filteredListings);

            // Map any combined listing objects to UI listing shape (best-effort)
            const mapped = filteredListings.map(item => ({
                id: item.ListingId,
                title: item.title ?? '—',
                price: typeof item.price === 'number' ? item.price : (item.price ? Number(item.price) : null),
                thumbnail: item.thumbnailUrl || null,
                status: item.status || 'ACTIVE',
                raw: item
            }));

            return {
                content: mapped,
                totalElements: mapped.length,
                totalPages: Math.ceil(mapped.length / size),
                size: size,
                number: page
            };
        } catch (fallbackError) {
            console.error('Fallback API also failed:', fallbackError);
            throw error; // Throw original error
        }
    }
};/**
 * Approve một listing
 * @param {number|string} listingId - ID của listing cần approve
 * @param {string} approvalNote - Ghi chú khi approve (optional)
 * @returns {Promise} Response sau khi approve
 */
export const approveListing = async (listingId, approvalNote = '') => {
    try {
        // Backend exposes GET /api/admin/approve-request/{postRequestId}
        const response = await axios.get(`/admin/approve-request/${listingId}`);
        return response.data;
    } catch (error) {
        console.error('Error approving listing:', error);
        throw error;
    }
};

/**
 * Reject một listing
 * @param {number|string} listingId - ID của listing cần reject
 * @param {string} rejectionReason - Lý do reject (required)
 * @returns {Promise} Response sau khi reject
 */
export const rejectListing = async (listingId, rejectionReason) => {
    if (!rejectionReason?.trim()) {
        throw new Error('Rejection reason is required');
    }

    try {
        // Backend exposes GET /api/admin/reject-request/{postRequestId}?reason=...
        const response = await axios.get(`/admin/reject-request/${listingId}`, {
            params: { reason: rejectionReason }
        });
        return response.data;
    } catch (error) {
        console.error('Error rejecting listing:', error);
        throw error;
    }
};

/**
 * Lấy chi tiết listing để xem trước khi approve/reject
 * @param {number|string} listingId - ID của listing
 * @returns {Promise} Chi tiết listing
 */
export const getListingDetail = async (listingId) => {
    try {
        // Backend provides listing detail at /api/listings/detail/{id}
        const response = await axios.get(`/detail/${listingId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching listing detail:', error);
        throw error;
    }
};

/**
 * Lấy thống kê tổng quan cho admin dashboard
 * @returns {Promise} Thống kê overview
 */
export const getAdminStats = async () => {
    try {
        const response = await axios.get('/api/admin/stats');
        return response.data;
    } catch (error) {
        console.error('Error fetching admin stats:', error);
        throw error;
    }
};

/**
 * Test API để debug - lấy tất cả listings
 */
export const getAllListingsForDebug = async () => {
    try {
        console.log('Debug: Getting all listings from evCart and batteryCart...');

        // Try the actual backend endpoints
        const [evResponse, batteryResponse] = await Promise.all([
            axios.get('/evCart'),
            axios.get('/batteryCart')
        ]);

        const evListings = evResponse.data.content || evResponse.data || [];
        const batteryListings = batteryResponse.data.content || batteryResponse.data || [];
        const allListings = [...evListings, ...batteryListings];

        console.log('Debug: EV listings:', evListings);
        console.log('Debug: Battery listings:', batteryListings);
        console.log('Debug: Combined listings:', allListings);

        return {
            content: allListings,
            totalElements: allListings.length,
            evCount: evListings.length,
            batteryCount: batteryListings.length
        };
    } catch (error) {
        console.error('Debug API failed:', error);

        // Try different endpoints
        const endpoints = [
            '/evCart',
            '/batteryCart',
            '/search',
            '/my-listings'
        ];

        for (const endpoint of endpoints) {
            try {
                console.log(`Trying alternative endpoint: ${endpoint}`);
                const response = await axios.get(endpoint);
                console.log(`Success with ${endpoint}:`, response.data);
                return response.data;
            } catch (err) {
                console.log(`Failed ${endpoint}:`, err.response?.status || err.message);
            }
        }

        throw error;
    }
};

/**
 * Lấy lịch sử xét duyệt listing
 * @param {Object} params - Query parameters
 * @param {number} params.page - Trang hiện tại
 * @param {number} params.size - Số lượng items per page
 * @param {string} params.status - Filter theo status (APPROVED, REJECTED)
 * @returns {Promise} Lịch sử xét duyệt
 */
export const getApprovalHistory = async (params = {}) => {
    const {
        page = 0,
        size = 10,
        status = null
    } = params;

    try {
        // There is no explicit admin listings history endpoint in backend; try admin post-request list as history source
        const response = await axios.get('/admin/post-request', {
            params: { page, size }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching approval history:', error);
        throw error;
    }
};