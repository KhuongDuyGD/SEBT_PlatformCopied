import api from './axios';

// Create listing
export const createListing = (payload, userId) => {
  const headers = userId ? { 'X-User-ID': userId } : {};
  return api.post('/listings/create', payload, { headers }); // trả nguyên response để dùng status
};

// Advanced search
export const advancedSearchListings = (params = {}) => {
  const query = new URLSearchParams(
    Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== '')
  ).toString();
  return api.get(`/listings/advanced-search?${query}`).then(r => r.data);
};

// Keyword search
export const keywordSearch = (keyword, page=0, size=12) => {
  return api.get(`/listings/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`)
    .then(r => r.data);
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

export default {
  createListing,
  advancedSearchListings,
  keywordSearch,
  fetchEvListingCarts,
  fetchBatteryListingCarts,
  fetchMyListings,
  fetchListingDetail,
};
