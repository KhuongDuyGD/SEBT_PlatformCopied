import api from './axios';

// Create listing - GỬI MULTIPART/FORM-DATA với file ảnh
export const createListing = (formData, userId) => {
    const headers = {
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

// Fee preview (listing publication fee) based on category (EV|BATTERY) & optional price
export const feePreview = (category, price) => {
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    if (price !== undefined && price !== null && price !== '') params.append('price', price);
    return api.get(`/listings/fee/preview?${params.toString()}`)
        .then(r => r.data);
};

// Build query helper ignoring null/undefined/empty
function buildQuery(params) {
    return new URLSearchParams(
        Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
    ).toString()
}

/**
 * Filter EV listings với các tham số đầy đủ theo database mới
 * Supported keys: vehicleType, year, minYear, maxYear, brand, province, district, 
 * conditionStatus, minMileage, maxMileage, minBatteryCapacity, maxBatteryCapacity, 
 * minPrice, maxPrice, page, size
 */
export function evFilterListings(filter = {}) {
    const query = buildQuery(filter)
    return api.get(`/listings/ev-filter?${query}`).then(r => r.data)
}

/**
 * Filter Battery listings với các tham số đầy đủ theo database mới
 * Supported keys: brand, name, year, minYear, maxYear, province, district, 
 * conditionStatus, compatibility, minBatteryCapacity, maxBatteryCapacity, 
 * minHealthPercentage, maxHealthPercentage, minPrice, maxPrice, page, size
 */
export function batteryFilterListings(filter = {}) {
    const query = buildQuery(filter)
    return api.get(`/listings/battery-filter?${query}`).then(r => r.data)
}

// ==========================
// FILTER DATA APIs - Lấy dữ liệu cho dropdown và autocomplete
// ==========================

/**
 * Lấy danh sách tỉnh thành để hiển thị trong filter location
 */
export const getProvinces = () => {
    return api.get('/listings/filter-data/provinces').then(r => r.data);
};

/**
 * Lấy danh sách quận huyện theo tỉnh (hoặc tất cả nếu không có tỉnh)
 */
export const getDistricts = (province = null) => {
    const query = province ? `?province=${encodeURIComponent(province)}` : '';
    return api.get(`/listings/filter-data/districts${query}`).then(r => r.data);
};

/**
 * Lấy danh sách thương hiệu xe điện
 */
export const getEvBrands = () => {
    return api.get('/listings/filter-data/ev-brands').then(r => r.data);
};

/**
 * Lấy danh sách năm sản xuất xe điện
 */
export const getEvYears = () => {
    return api.get('/listings/filter-data/ev-years').then(r => r.data);
};

/**
 * Lấy danh sách thương hiệu pin
 */
export const getBatteryBrands = () => {
    return api.get('/listings/filter-data/battery-brands').then(r => r.data);
};

/**
 * Lấy danh sách tên pin cho autocomplete
 */
export const getBatteryNames = () => {
    return api.get('/listings/filter-data/battery-names').then(r => r.data);
};

/**
 * Lấy danh sách năm sản xuất pin
 */
export const getBatteryYears = () => {
    return api.get('/listings/filter-data/battery-years').then(r => r.data);
};

/**
 * Lấy danh sách xe tương thích cho filter pin
 */
export const getCompatibleVehicles = () => {
    return api.get('/listings/filter-data/compatible-vehicles').then(r => r.data);
};

export default {
    createListing,
    advancedSearchListings,
    keywordSearch,
    fetchEvListingCarts,
    fetchBatteryListingCarts,
    fetchMyListings,
    fetchListingDetail,
    feePreview,
    evFilterListings,
    batteryFilterListings,
    // Filter data APIs
    getProvinces,
    getDistricts,
    getEvBrands,
    getEvYears,
    getBatteryBrands,
    getBatteryNames,
    getBatteryYears,
    getCompatibleVehicles,
};