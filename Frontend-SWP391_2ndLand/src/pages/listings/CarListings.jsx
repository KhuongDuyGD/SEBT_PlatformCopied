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
      console.log('Response from API:', response); // Debug log
      
      // Response theo format từ API docs: Page<ListingCartResponseDTO>
      const listings = response?.content || [];

      if (Array.isArray(listings)) {
        const formattedListings = listings.map(listing => ({
          id: listing.listingId,
          image: listing.thumbnailUrl || "/images/default-car.jpg",
          brand: listing.title, // Sử dụng title làm brand
          location: "Không rõ", // Chưa có trong ListingCartResponseDTO
          km: "—", // Chưa có trong ListingCartResponseDTO
          left: '—',
          price: formatPrice(listing.price),
          owner: listing.sellerPhoneNumber || "Liên hệ",
          comments: 0,
          description: "Chưa có mô tả", // Chưa có trong ListingCartResponseDTO
          certified: listing.favorite, // Sử dụng favorite status
          year: "—", // Chưa có trong ListingCartResponseDTO
          condition: "—", // Chưa có trong ListingCartResponseDTO
          batteryCapacity: "—", // Chưa có trong ListingCartResponseDTO
          viewsCount: listing.viewCount || 0,
          createdAt: null // Chưa có trong ListingCartResponseDTO
        }));
        
        setCarListings(formattedListings);
      } else {
        console.warn('API response content không đúng format:', response);
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

  // Removed calculateTimeLeft function as it's not used in current implementation

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