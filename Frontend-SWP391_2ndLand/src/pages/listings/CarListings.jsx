// DEPRECATED: CarListings page logic đã được hợp nhất vào ListingPage thông qua query param
import React, { useState, useEffect, useCallback } from "react";
import ListingPage from "./ListingPage";
import listingsApi from "../../api/listings";

function CarListings() {
  const [carListings, setCarListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /**
   * Fetch danh sách xe từ API
   */
  const fetchCarListings = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await listingsApi.fetchEvListingCarts(0, 30); // lấy 30 đầu tiên cho page này
      const data = response?.data || [];

      if (Array.isArray(data)) {
        const formattedListings = data.map(listing => ({
          id: listing.id,
          image: listing.mainImageUrl || listing.mainImage || "/images/default-car.jpg",
          brand: listing.brand || listing?.ev?.brand || listing.title,
          location: listing.locationProvince || listing.location?.province || "Không rõ",
          km: listing.mileage ? `${listing.mileage.toLocaleString()} km` : (listing.ev?.mileage ? `${listing.ev.mileage.toLocaleString()} km` : "Không rõ"),
          left: '—',
          price: formatPrice(listing.price),
          owner: listing.sellerName || listing.seller?.username || "Ẩn danh",
          comments: 0,
          description: listing.description || "Chưa có mô tả",
          certified: listing.listingType === 'PREMIUM',
          year: listing.year || listing.ev?.year,
          condition: listing.conditionStatus || listing.ev?.conditionStatus,
          batteryCapacity: listing.batteryCapacity || listing.ev?.batteryCapacity,
          viewsCount: listing.viewsCount || 0,
          createdAt: listing.createdAt
        }));
        
        setCarListings(formattedListings);
      } else {
        console.warn('API response không đúng format:', response);
        setCarListings([]);
      }
    } catch (err) {
      console.error('Lỗi khi fetch car listings:', err);
      setError('Không thể tải danh sách xe. Vui lòng thử lại sau.');
      setCarListings([]);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Tính toán thời gian còn lại của listing
   * @param {string} expiresAt - Thời gian hết hạn
   * @returns {string} - Chuỗi mô tả thời gian còn lại
   */
  const calculateTimeLeft = (expiresAt) => {
    if (!expiresAt) return "Không giới hạn";
    
    const now = new Date();
    const expiration = new Date(expiresAt);
    const diff = expiration - now;
    
    if (diff <= 0) return "Đã hết hạn";
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    
    if (days > 0) return `${days} ngày`;
    if (hours > 0) return `${hours} giờ`;
    return "Sắp hết hạn";
  };

  /**
   * Format giá tiền
   * @param {number} price - Giá
   * @returns {string} - Chuỗi giá đã format
   */
  const formatPrice = (price) => {
    if (!price) return "Liên hệ";
    
    if (price >= 1000000000) {
      return `${(price / 1000000000).toFixed(1)} tỷ`;
    } else if (price >= 1000000) {
      return `${(price / 1000000).toFixed(0)} triệu`;
    } else {
      return `${price.toLocaleString()} VND`;
    }
  };

  // Fetch data khi component mount
  useEffect(() => {
    fetchCarListings();
  }, [fetchCarListings]);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải danh sách xe...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <button 
            onClick={fetchCarListings}
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            Thử lại
          </button>
        </div>
      </div>
    );
  }

  return (
    <ListingPage
      pageTitle="Các xe mới nhất"
      searchPlaceholder="Tìm kiếm xe..."
      brandPlaceholder="VD: Vinfast, Peugeot..."
      items={carListings}
      showStatusFilter={true}
      onRefresh={fetchCarListings}
    />
  );
}

export default CarListings;