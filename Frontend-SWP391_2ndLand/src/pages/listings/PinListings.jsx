import React, { useState, useEffect, useCallback } from "react";
import ListingPage from "./ListingPage";
import api from "../../api/axios";

function PinListings() {
  const [pinListings, setPinListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /**
   * Fetch danh sách pin từ API
   */
  const fetchPinListings = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await api.get('/listings/pins');
      
      if (response.data && Array.isArray(response.data)) {
        // Chuyển đổi dữ liệu từ API thành format phù hợp với ListingPage
        const formattedListings = response.data.map(listing => ({
          id: listing.id,
          image: listing.mainImage || "/images/default-battery.jpg",
          brand: (listing.product?.battery?.brand && listing.product?.battery?.model) 
            ? `${listing.product.battery.brand} ${listing.product.battery.model}` 
            : listing.title,
          location: listing.location?.province || "Không rõ",
          km: listing.product?.battery?.healthPercentage ? `Còn ${listing.product?.battery?.healthPercentage}%` : "Không rõ",
          left: calculateTimeLeft(listing.expiresAt),
          price: formatPrice(listing.price),
          owner: listing.seller?.username || "Ẩn danh",
          comments: 0, // Tạm thời để 0, sau này sẽ implement comment system
          description: listing.description || "Chưa có mô tả",
          certified: listing.listingType === 'PREMIUM',
          capacity: listing.product?.battery?.capacity,
          condition: listing.product?.battery?.conditionStatus,
          compatibleVehicles: listing.product?.battery?.compatibleVehicles,
          viewsCount: listing.viewsCount || 0,
          createdAt: listing.createdAt
        }));
        
        setPinListings(formattedListings);
      } else {
        console.warn('API response không đúng format:', response.data);
        setPinListings([]);
      }
    } catch (err) {
      console.error('Lỗi khi fetch pin listings:', err);
      setError('Không thể tải danh sách pin. Vui lòng thử lại sau.');
      setPinListings([]);
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
    fetchPinListings();
  }, [fetchPinListings]);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải danh sách pin...</p>
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
            onClick={fetchPinListings}
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
      pageTitle="Các pin mới nhất"
      searchPlaceholder="Tìm kiếm pin..."
      brandPlaceholder="VD: Lithium-ion, LFP..."
      items={pinListings}
      showStatusFilter={false}
      onRefresh={fetchPinListings}
    />
  );
}

export default PinListings;