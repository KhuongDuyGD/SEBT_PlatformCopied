
import React, { useState } from 'react';
import { normalizeImage } from '../../../utils/listingMapper.js';
import ModalListingApprovalCard from './ModalListingApprovalCard.jsx';

const ListingApprovalCard = ({ listing, onApprove, onReject, loading = false }) => {
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');
    const [approvalNote, setApprovalNote] = useState('');
    const [showApprovalNote, setShowApprovalNote] = useState(false);
    const [showDetailModal, setShowDetailModal] = useState(false);

    // Production: avoid console debug logs

    const handleApprove = () => {
        if (showApprovalNote) {
            onApprove(listing.listingId, approvalNote);
            setApprovalNote('');
            setShowApprovalNote(false);
        } else {
            onApprove(listing.listingId);
        }
    };

    const handleReject = () => {
        if (rejectionReason.trim()) {
            onReject(listing.listingId, rejectionReason);
            setRejectionReason('');
            setShowRejectModal(false);
        }
    };

    const formatPrice = (price) => {
        if (!price) return 'Chưa có giá';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    };

   

    const getConditionBadge = () => {
        const condition = listing.product?.ev?.conditionStatus || listing.product?.battery?.conditionStatus;
        if (!condition) return null;

        const conditionMap = {
            NEW: { text: 'Mới', color: 'bg-green-100 text-green-800' },
            LIKE_NEW: { text: 'Như mới', color: 'bg-blue-100 text-blue-800' },
            GOOD: { text: 'Tốt', color: 'bg-yellow-100 text-yellow-800' },
            FAIR: { text: 'Ổn', color: 'bg-orange-100 text-orange-800' },
            POOR: { text: 'Kém', color: 'bg-red-100 text-red-800' },
            DEGRADED: { text: 'Giảm hiệu suất', color: 'bg-orange-100 text-orange-800' },
            NEEDS_REPLACEMENT: { text: 'Cần thay thế', color: 'bg-red-100 text-red-800' }
        };

        const conditionInfo = conditionMap[condition];
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-medium ${conditionInfo?.color || 'bg-gray-100 text-gray-800'}`}>
                {conditionInfo?.text || condition}
            </span>
        );
    };

    return (
        <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
            {/* Header với thông tin cơ bản */}
            <div className="p-4 border-b border-gray-100">
                <div className="flex justify-between items-start mb-2">
                    <h3 className="text-lg font-semibold text-gray-900 line-clamp-2">
                        {listing.title}
                    </h3>
                    <div className="flex gap-2 items-center">
                        {getConditionBadge()}
                        <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs font-medium">
                            Chờ duyệt
                        </span>
                    </div>
                </div>

                <div className="flex items-center justify-between text-sm text-gray-600">
                    <span>ID: #{listing.listingId || 'N/A'}</span>
                    <div className="flex items-center gap-3">
                        <button
                            type="button"
                            onClick={() => setShowDetailModal(true)}
                            className="px-2 py-1 text-xs rounded-md bg-blue-50 text-blue-700 hover:bg-blue-100"
                            title="Xem chi tiết"
                        >
                            Xem chi tiết
                        </button>
                        <span>{listing.createdDate ? new Date(listing.createdDate).toLocaleDateString('vi-VN') : 'N/A'}</span>
                    </div>
                </div>
            </div>

            <div className="p-4">
                {/* compact header/summary area */}

                <div className="flex gap-4">
                    {/* Ảnh thumbnail */}
                    <div className="flex-shrink-0">
                        <img
                            src={normalizeImage(listing.thumbnailUrl || listing.thumbnail || listing.mainImage || listing.image)}
                            alt={listing.title || 'Listing image'}
                            className="w-24 h-24 object-cover rounded-lg"
                            onError={(e) => {
                                e.target.src = 'https://placehold.co/96x96?text=No+Image';
                            }}
                        />
                    </div>

                    {/* Thông tin chi tiết */}
                    <div className="flex-1 space-y-2">


                        {/* Thông tin sản phẩm chi tiết */}
                        {listing.product?.ev && (
                            <div className="mt-3 p-3 bg-blue-50 rounded-lg">
                                <h4 className="font-medium text-blue-900 mb-2">Thông tin xe điện</h4>
                                <div className="grid grid-cols-2 gap-2 text-sm">
                                    {listing.product.ev.year && (
                                        <span><strong>Năm:</strong> {listing.product.ev.year}</span>
                                    )}
                                    {listing.product.ev.mileage && (
                                        <span><strong>Km đã đi:</strong> {listing.product.ev.mileage.toLocaleString()} km</span>
                                    )}
                                    {listing.product.ev.batteryCapacity && (
                                        <span><strong>Dung lượng pin:</strong> {listing.product.ev.batteryCapacity} kWh</span>
                                    )}
                                </div>
                            </div>
                        )}

                        {listing.product?.battery && (
                            <div className="mt-3 p-3 bg-green-50 rounded-lg">
                                <h4 className="font-medium text-green-900 mb-2">Thông tin pin</h4>
                                <div className="grid grid-cols-2 gap-2 text-sm">
                                    {listing.product.battery.capacity && (
                                        <span><strong>Dung lượng:</strong> {listing.product.battery.capacity} kWh</span>
                                    )}
                                    {listing.product.battery.healthPercentage && (
                                        <span><strong>Sức khỏe pin:</strong> {listing.product.battery.healthPercentage}%</span>
                                    )}
                                </div>
                            </div>
                        )}

                        {/* Địa chỉ */}
                        {listing.location && (
                            <div className="mt-3">
                                <span className="font-medium text-gray-700">Địa chỉ:</span>
                                <p className="text-gray-600 text-sm">
                                    {listing.location.details}, {listing.location.district}, {listing.location.province}
                                </p>
                            </div>
                        )}

                        {/* Mô tả */}
                        {listing.description && (
                            <div className="mt-3">
                                <span className="font-medium text-gray-700">Mô tả:</span>
                                <p className="text-gray-600 text-sm line-clamp-3">{listing.description}</p>
                            </div>
                        )}
                    </div>
                </div>

                {/* Action buttons */}
                <div className="mt-4 flex gap-3 justify-end border-t pt-4">
                    <button
                        onClick={() => setShowApprovalNote(!showApprovalNote)}
                        className="px-3 py-1 text-sm text-blue-600 hover:text-blue-800 underline"
                    >
                        {showApprovalNote ? 'Ẩn ghi chú' : 'Thêm ghi chú'}
                    </button>

                    <button
                        onClick={() => setShowRejectModal(true)}
                        disabled={loading}
                        className="px-4 py-2 rounded-md hover:bg-red-700 disabled:bg-red-300 disabled:text-white disabled:opacity-60 disabled:cursor-not-allowed border-none"
                        style={{ backgroundColor: '#dc2626', color: '#ffffff' }}
                    >
                        {loading ? 'Đang xử lý...' : 'Từ chối'}
                    </button>

                    <button
                        onClick={handleApprove}
                        disabled={loading}
                        className="px-4 py-2 rounded-md hover:bg-green-700 disabled:bg-green-300 disabled:text-white disabled:opacity-60 disabled:cursor-not-allowed border-none"
                        style={{ backgroundColor: '#16a34a', color: '#ffffff' }}
                    >
                        {loading ? 'Đang xử lý...' : 'Phê duyệt'}
                    </button>
                </div>

                {/* Approval note input */}
                {showApprovalNote && (
                    <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Ghi chú phê duyệt (tùy chọn)
                        </label>
                        <textarea
                            value={approvalNote}
                            onChange={(e) => setApprovalNote(e.target.value)}
                            placeholder="Nhập ghi chú cho việc phê duyệt..."
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm resize-none"
                            rows="2"
                        />
                    </div>
                )}
            </div>

            {/* Reject Modal */}
            {showRejectModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-6 w-96 max-w-90vw">
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">
                            Từ chối tin đăng
                        </h3>

                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Lý do từ chối <span className="text-red-500">*</span>
                            </label>
                            <textarea
                                value={rejectionReason}
                                onChange={(e) => setRejectionReason(e.target.value)}
                                placeholder="Nhập lý do từ chối tin đăng này..."
                                className="w-full px-3 py-2 border border-gray-300 rounded-md resize-none"
                                rows="4"
                                required
                            />
                        </div>

                        <div className="flex gap-3 justify-end">
                            <button
                                onClick={() => setShowRejectModal(false)}
                                className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
                            >
                                Hủy
                            </button>
                            <button
                                onClick={handleReject}
                                disabled={!rejectionReason.trim() || loading}
                                className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Xác nhận từ chối
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Detail Modal */}
            {showDetailModal && (
                <ModalListingApprovalCard
                    isOpen={showDetailModal}
                    onClose={() => setShowDetailModal(false)}
                    data={{
                        requestId: listing.id ?? listing.requestId ?? listing.raw?.requestId,
                        listingId: listing.listingId ?? listing.raw?.listingId,
                        thumbnailUrl: listing.thumbnailUrl || listing.thumbnail || listing.mainImage || listing.image || listing.raw?.thumbnailUrl,
                        price: listing.price ?? listing.raw?.price,
                        title: listing.title ?? listing.raw?.title,
                        status: listing.status ?? listing.raw?.status,
                        raw: listing.raw
                    }}
                />
            )}
        </div>
    );
};

export default ListingApprovalCard;