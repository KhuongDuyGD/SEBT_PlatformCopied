// src/utils/listingMapper.js
// Helper chuẩn hóa dữ liệu listing từ nhiều endpoint khác nhau về 1 cấu trúc thống nhất

const PLACEHOLDER = 'https://placehold.co/400x300?text=No+Image';

/**
 * Chuẩn hóa URL ảnh (thêm host nếu backend trả đường dẫn tương đối)
 */
export function normalizeImage(url) {
  if (!url) return PLACEHOLDER;
  if (url.startsWith('http')) return url;
  if (url.startsWith('/')) {
    // Cho phép override base host qua env khác nếu cần
    const host = import.meta.env.VITE_BACKEND_ORIGIN || 'http://localhost:8080';
    return host + url;
  }
  return url;
}

/**
 * Map 1 record listing (cart/detail minimal) về object dùng cho UI card
 */
export function mapListingCart(raw) {
  if (!raw) return null;
  const id = raw.listingId ?? raw.id;
  const priceNum = typeof raw.price === 'number' ? raw.price : (raw.price ? Number(raw.price) : null);
  return {
    id,
    title: raw.title ?? raw.listingTitle ?? '—',
    price: priceNum,
    thumbnail: normalizeImage(
      raw.thumbnailUrl || raw.mainImageUrl || raw.thumbnailImage || raw.mainImage || raw.image || raw.thumbnail
    ),
    views: raw.viewCount ?? raw.viewsCount ?? raw.view ?? 0,
    favorited: raw.favorite ?? raw.favorited ?? raw.isFavorited ?? false,
    listingType: raw.listingType,
    status: raw.status || raw.listingStatus,
    category: raw.category || (raw.product?.ev ? 'VEHICLE' : (raw.product?.battery ? 'BATTERY' : undefined)),
    // Mapping year từ nhiều nguồn có thể trong backend response
    year: raw.year || raw.product?.ev?.year || raw.product?.battery?.year || raw.manufacturingYear || raw.productionYear || raw.yearOfManufacture,
    // Giữ nguyên để phòng cần field khác
    raw
  };
}

/**
 * Map danh sách listings an toàn.
 */
export function mapListingArray(arr) {
  if (!Array.isArray(arr)) return [];
  return arr.map(mapListingCart).filter(Boolean);
}
