import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../api/axios";

function ListingDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [listing, setListing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /**
   * Fetch chi tiết listing từ API
   */
  const fetchListingDetail = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await api.get(`/listings/${id}`);
      
      if (response.data) {
        setListing(response.data);
      } else {
        setError('Không tìm thấy listing này.');
      }
    } catch (err) {
      console.error('Lỗi khi fetch listing detail:', err);
      if (err.response?.status === 404) {
        setError('Listing không tồn tại.');
      } else {
        setError('Không thể tải thông tin listing. Vui lòng thử lại sau.');
      }
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    if (id) {
      fetchListingDetail();
    }
  }, [id, fetchListingDetail]);

  /**
   * Format giá tiền
   */
  const formatPrice = (price) => {
    if (!price) return "Liên hệ";
    
    if (price >= 1000000000) {
      return `${(price / 1000000000).toFixed(1)} tỷ VND`;
    } else if (price >= 1000000) {
      return `${(price / 1000000).toFixed(0)} triệu VND`;
    } else {
      return `${price.toLocaleString()} VND`;
    }
  };

  /**
   * Format ngày tháng
   */
  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Đang tải thông tin listing...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <p className="text-red-600 mb-4">{error}</p>
          <div className="space-x-4">
            <button 
              onClick={fetchListingDetail}
              className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
            >
              Thử lại
            </button>
            <button 
              onClick={() => navigate(-1)}
              className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded"
            >
              Quay lại
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!listing) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <p className="text-gray-600">Không tìm thấy listing.</p>
          <button 
            onClick={() => navigate(-1)}
            className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded mt-4"
          >
            Quay lại
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      <div className="mb-6">
        <button 
          onClick={() => navigate(-1)}
          className="text-blue-600 hover:text-blue-800 flex items-center"
        >
          ← Quay lại
        </button>
      </div>

      <div className="bg-white shadow-lg rounded-lg overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white p-6">
          <h1 className="text-3xl font-bold mb-2">{listing.title}</h1>
          <div className="flex items-center justify-between">
            <p className="text-lg opacity-90">
              Đăng bởi: {listing.seller?.username || 'Ẩn danh'}
            </p>
            <div className="text-right">
              <p className="text-2xl font-bold">{formatPrice(listing.price)}</p>
              <p className="text-sm opacity-75">
                {listing.viewsCount || 0} lượt xem
              </p>
            </div>
          </div>
        </div>

        <div className="p-6">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Ảnh chính */}
            <div className="lg:col-span-2">
              <div className="mb-6">
                <img 
                  src={listing.mainImage || "/images/default-listing.jpg"} 
                  alt={listing.title}
                  className="w-full h-96 object-cover rounded-lg shadow-md"
                  onError={(e) => {
                    e.target.src = "/images/default-listing.jpg";
                  }}
                />
              </div>

              {/* Mô tả */}
              <div className="mb-6">
                <h3 className="text-xl font-semibold mb-3">Mô tả</h3>
                <div className="bg-gray-50 p-4 rounded-lg">
                  <p className="text-gray-700 whitespace-pre-wrap">
                    {listing.description || "Chưa có mô tả chi tiết."}
                  </p>
                </div>
              </div>
            </div>

            {/* Thông tin bên phải */}
            <div className="space-y-6">
              {/* Thông tin sản phẩm */}
              <div className="bg-gray-50 p-4 rounded-lg">
                <h3 className="text-lg font-semibold mb-3">Thông tin sản phẩm</h3>
                
                {listing.product?.vehicle && (
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="font-medium">Loại:</span>
                      <span>Xe {listing.product.vehicle.type}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Hãng:</span>
                      <span>{listing.product.vehicle.brand}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Năm:</span>
                      <span>{listing.product.vehicle.year}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Số km:</span>
                      <span>{listing.product.vehicle.mileage?.toLocaleString()} km</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Pin:</span>
                      <span>{listing.product.vehicle.batteryCapacity} kWh</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Tình trạng:</span>
                      <span>{listing.product.vehicle.conditionStatus}</span>
                    </div>
                  </div>
                )}

                {listing.product?.battery && (
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="font-medium">Loại:</span>
                      <span>Pin điện</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Hãng:</span>
                      <span>{listing.product.battery.brand}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Dung lượng:</span>
                      <span>{listing.product.battery.capacity} kWh</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Độ khỏe:</span>
                      <span>{listing.product.battery.healthPercentage}%</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="font-medium">Tình trạng:</span>
                      <span>{listing.product.battery.conditionStatus}</span>
                    </div>
                    {listing.product.battery.compatibleVehicles && (
                      <div className="flex justify-between">
                        <span className="font-medium">Xe tương thích:</span>
                        <span className="text-right">{listing.product.battery.compatibleVehicles}</span>
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Thông tin vị trí */}
              {listing.location && (
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="text-lg font-semibold mb-3">Vị trí</h3>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="font-medium">Tỉnh/TP:</span>
                      <span>{listing.location.province}</span>
                    </div>
                    {listing.location.district && (
                      <div className="flex justify-between">
                        <span className="font-medium">Quận/Huyện:</span>
                        <span>{listing.location.district}</span>
                      </div>
                    )}
                    {listing.location.details && (
                      <div>
                        <span className="font-medium">Chi tiết:</span>
                        <p className="text-sm text-gray-600 mt-1">{listing.location.details}</p>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Thông tin khác */}
              <div className="bg-gray-50 p-4 rounded-lg">
                <h3 className="text-lg font-semibold mb-3">Thông tin khác</h3>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="font-medium">Loại listing:</span>
                    <span className={`px-2 py-1 rounded text-xs ${
                      listing.listingType === 'PREMIUM' ? 'bg-yellow-200 text-yellow-800' :
                      listing.listingType === 'FEATURED' ? 'bg-purple-200 text-purple-800' :
                      'bg-gray-200 text-gray-800'
                    }`}>
                      {listing.listingType}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="font-medium">Trạng thái:</span>
                    <span className={`px-2 py-1 rounded text-xs ${
                      listing.status === 'ACTIVE' ? 'bg-green-200 text-green-800' :
                      listing.status === 'SOLD' ? 'bg-red-200 text-red-800' :
                      'bg-yellow-200 text-yellow-800'
                    }`}>
                      {listing.status}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="font-medium">Ngày đăng:</span>
                    <span>{formatDate(listing.createdAt)}</span>
                  </div>
                  {listing.expiresAt && (
                    <div className="flex justify-between">
                      <span className="font-medium">Hết hạn:</span>
                      <span>{formatDate(listing.expiresAt)}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Contact button */}
              <div className="bg-blue-50 p-4 rounded-lg">
                <button className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded-lg transition duration-300">
                  Liên hệ người bán
                </button>
                <p className="text-xs text-gray-500 text-center mt-2">
                  Click để xem thông tin liên hệ
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ListingDetail;
