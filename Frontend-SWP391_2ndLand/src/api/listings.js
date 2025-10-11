import api from './axios';

// Create listing - GỬI MULTIPART/FORM-DATA với file ảnh
export const createListing = (formData, userId) => {
    const headers = {
        'Content-Type': 'multipart/form-data',
        ...(userId ? { 'X-User-ID': userId } : {})
    };
    return api.post('/listings/create', formData, { headers });
};

/**
 * Keyword + lightweight param search (same backend endpoint /listings/search)
 * @deprecated Sẽ tách rõ filter nâng cao EV/Battery. Dùng keywordSearch hoặc evFilterListings / batteryFilterListings.
 */
export const advancedSearchListings = (params = {}) => {
    const { keyword = '', ...otherParams } = params;
    const query = new URLSearchParams(
        Object.entries({keyword, ...otherParams}).filter(([, v]) => v !== undefined && v !== null && v !== '')
    ).toString();
    return api.get(`/listings/search?${query}`).then(r => r.data);
};

// Keyword search (accept optional axios config: { signal } etc.)
export const keywordSearch = (keyword, page=0, size=12, config={}) => {
    return api.get(`/listings/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`,
        config).then(r => r.data);
};

// EV listing carts
export const fetchEvListingCarts = (page=0, size=12) => {
    return api.get(`/listings/evCart?page=${page}&size=${size}`).then(r => r.data);
};

// Battery listing carts
export const fetchBatteryListingCarts = (page=0, size=12) => {
    return api.get(`/listings/batteryCart?page=${page}&size=${size}`).then(r => r.data);
};

// My listings
export const fetchMyListings = (page=0, size=12) => {
    return api.get(`/listings/my-listings?page=${page}&size=${size}`).then(r => r.data);
};

// Get detail
export const fetchListingDetail = (id) => {
    return api.get(`/listings/detail/${id}`).then(r => r.data);
};

// Build query helper ignoring null/undefined/empty
function buildQuery(params) {
    return new URLSearchParams(
        Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
    ).toString()
}

/**
 * Filter EV listings with flattened query params.
 * Supported keys: vehicleType, year, brand, location, minBatteryCapacity, maxBatteryCapacity, minPrice, maxPrice, page, size
 */
export function evFilterListings(filter = {}) {
    const query = buildQuery(filter)
    return api.get(`/listings/ev-filter?${query}`).then(r => r.data)
}

/**
 * Filter Battery listings with flattened query params.
 * Supported keys: brand, location, compatibility, minBatteryCapacity, maxBatteryCapacity, minPrice, maxPrice, page, size
 */
export function batteryFilterListings(filter = {}) {
    const query = buildQuery(filter)
    return api.get(`/listings/battery-filter?${query}`).then(r => r.data)
}

export default {
    createListing,
    advancedSearchListings,
    keywordSearch,
    fetchEvListingCarts,
    fetchBatteryListingCarts,
    fetchMyListings,
    fetchListingDetail,
    evFilterListings,
    batteryFilterListings,
};