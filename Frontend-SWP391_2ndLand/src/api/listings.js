// Import instance axios đã được cấu hình sẵn (baseURL, interceptors, headers...)
import api from './axios';

// ==========================
// TẠO TIN ĐĂNG MỚI
// ==========================

// Gửi dữ liệu form tạo tin đăng mới (dạng multipart/form-data, có chứa file ảnh)
// Tham số:
//   - formData: chứa dữ liệu tin (ảnh, thông tin...)
//   - userId: id của user (nếu có) để gửi header X-User-ID
export const createListing = (formData, userId) => {
    // Nếu có userId thì thêm header 'X-User-ID', ngược lại không thêm gì
    const headers = {
        ...(userId ? { 'X-User-ID': userId } : {})
    };
    // Gửi request POST đến endpoint /listings/create
    // axios sẽ tự động set Content-Type = multipart/form-data
    return api.post('/listings/create', formData, { headers });
};

// ==========================
// TÌM KIẾM NÂNG CAO (đã deprecated)
// ==========================

/**
 * Keyword + lightweight param search (same backend endpoint /listings/search)
 * @deprecated Sẽ tách rõ filter nâng cao EV/Battery. Dùng keywordSearch hoặc evFilterListings / batteryFilterListings.
 */
export const advancedSearchListings = (params = {}) => {
    // Tách keyword ra khỏi params, các param còn lại gom vào otherParams
    const { keyword = '', ...otherParams } = params;

    // Tạo query string, lọc bỏ giá trị null/undefined/rỗng
    const query = new URLSearchParams(
        Object.entries({ keyword, ...otherParams }).filter(([, v]) => v !== undefined && v !== null && v !== '')
    ).toString();

    // Gửi GET request với query string
    // .then(r => r.data): chỉ lấy phần data trong response
    return api.get(`/listings/search?${query}`).then(r => r.data);
};

// ==========================
// TÌM KIẾM THEO TỪ KHÓA
// ==========================

// Tìm kiếm bằng từ khóa (keyword) có hỗ trợ phân trang và cấu hình axios
export const keywordSearch = (keyword, page = 0, size = 12, config = {}) => {
    // encodeURIComponent giúp mã hóa keyword có dấu hoặc ký tự đặc biệt
    return api
        .get(
            `/listings/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`,
            config
        )
        .then(r => r.data);
};

// ==========================
// LẤY DANH SÁCH XE ĐIỆN TRONG GIỎ (EV CART)
// ==========================

// Lấy danh sách tin xe điện có trong giỏ hàng
export const fetchEvListingCarts = (page = 0, size = 12) => {
    return api.get(`/listings/evCart?page=${page}&size=${size}`).then(r => r.data);
};

// ==========================
// LẤY DANH SÁCH PIN TRONG GIỎ (BATTERY CART)
// ==========================

export const fetchBatteryListingCarts = (page = 0, size = 12) => {
    return api.get(`/listings/batteryCart?page=${page}&size=${size}`).then(r => r.data);
};

// ==========================
// LẤY DANH SÁCH TIN CỦA TÔI
// ==========================

// Dành cho người dùng xem các tin mà họ đã đăng
export const fetchMyListings = (page = 0, size = 12) => {
    return api.get(`/listings/my-listings?page=${page}&size=${size}`).then(r => r.data);
};

// Đếm số bài đăng chờ thanh toán của user (badge navbar)
export const countPendingPaymentListings = () => {
    return api.get('/members/listings/pending-payment-count').then(r => r.data);
};

// Thanh toán phí đăng tin cho listing cụ thể
export const payListingFee = (listingId) => {
    return api.post(`/members/listings/${listingId}/pay-fee`).then(r => r.data);
};

// Lấy phí phải thanh toán cho listing ở trạng thái PAY_WAITING
export const getListingFee = (listingId) => {
    return api.get(`/members/listings/${listingId}/fee`).then(r => r.data); // { fee }
};

// Kiểm tra mức độ hoàn thiện hồ sơ (hiện chỉ phoneNumber)
export const getProfileCompleteness = () => {
    return api.get('/members/profile-completeness').then(r => r.data); // { phonePresent }
};

// ==========================
// LẤY CHI TIẾT MỘT TIN
// ==========================

// Lấy chi tiết tin đăng theo id
export const fetchListingDetail = (id) => {
    return api.get(`/listings/detail/${id}`).then(r => r.data);
};

// ==========================
// XEM TRƯỚC PHÍ ĐĂNG TIN
// ==========================

// Xem phí đăng tin dựa vào loại (EV/BATTERY) và giá trị
export const feePreview = (category, price) => {
    const params = new URLSearchParams();
    // Thêm param category và price nếu có
    if (category) params.append('category', category);
    if (price !== undefined && price !== null && price !== '') params.append('price', price);
    // Gửi request GET kèm query string
    return api.get(`/listings/fee/preview?${params.toString()}`).then(r => r.data);
};

// ==========================
// HÀM XÂY DỰNG QUERY CHUNG (Helper function)
// ==========================

// Hàm buildQuery: lọc bỏ các param không hợp lệ và chuyển sang dạng query string
function buildQuery(params) {
    return new URLSearchParams(
        Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
    ).toString();
}

// ==========================
// FILTER CHO XE ĐIỆN (EV)
// ==========================

/**
 * Filter EV listings với các tham số đầy đủ theo database mới
 * Supported keys: vehicleType, year, minYear, maxYear, brand, province, district,
 * conditionStatus, minMileage, maxMileage, minBatteryCapacity, maxBatteryCapacity,
 * minPrice, maxPrice, page, size
 */
export function evFilterListings(filter = {}) {
    // Xây query string từ object filter (loại bỏ null/undefined)
    const query = buildQuery(filter);
    // Gửi GET request đến endpoint filter EV
    return api.get(`/listings/ev-filter?${query}`).then(r => r.data);
}

// ==========================
// FILTER CHO PIN (BATTERY)
// ==========================

/**
 * Filter Battery listings với các tham số đầy đủ theo database mới
 * Supported keys: brand, name, year, minYear, maxYear, province, district,
 * conditionStatus, compatibility, minBatteryCapacity, maxBatteryCapacity,
 * minHealthPercentage, maxHealthPercentage, minPrice, maxPrice, page, size
 */
export function batteryFilterListings(filter = {}) {
    const query = buildQuery(filter);
    return api.get(`/listings/battery-filter?${query}`).then(r => r.data);
}

// ==========================
//  APIs LẤY DỮ LIỆU FILTER (Dropdown, Autocomplete)
// ==========================

// Lấy danh sách tỉnh/thành để hiển thị trong bộ lọc vị trí
export const getProvinces = () => {
    return api.get('/listings/filter-data/provinces').then(r => r.data);
};

// Lấy danh sách quận/huyện theo tỉnh, nếu không truyền province thì lấy tất cả
export const getDistricts = (province = null) => {
    const query = province ? `?province=${encodeURIComponent(province)}` : '';
    return api.get(`/listings/filter-data/districts${query}`).then(r => r.data);
};

// Lấy danh sách thương hiệu xe điện
export const getEvBrands = () => {
    return api.get('/listings/filter-data/ev-brands').then(r => r.data);
};

// Lấy danh sách năm sản xuất xe điện
export const getEvYears = () => {
    return api.get('/listings/filter-data/ev-years').then(r => r.data);
};

// Lấy danh sách thương hiệu pin
export const getBatteryBrands = () => {
    return api.get('/listings/filter-data/battery-brands').then(r => r.data);
};

// Lấy danh sách tên pin (autocomplete)
export const getBatteryNames = () => {
    return api.get('/listings/filter-data/battery-names').then(r => r.data);
};

// Lấy danh sách năm sản xuất pin
export const getBatteryYears = () => {
    return api.get('/listings/filter-data/battery-years').then(r => r.data);
};

// Lấy danh sách xe tương thích để filter pin
export const getCompatibleVehicles = () => {
    return api.get('/listings/filter-data/compatible-vehicles').then(r => r.data);
};

// ==========================
// EXPORT TỔNG HỢP
// ==========================

// Gom tất cả các hàm ở trên thành 1 object export mặc định
export default {
    createListing,
    advancedSearchListings,
    keywordSearch,
    fetchEvListingCarts,
    fetchBatteryListingCarts,
    fetchMyListings,
    countPendingPaymentListings,
    payListingFee,
    getListingFee,
    getProfileCompleteness,
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
