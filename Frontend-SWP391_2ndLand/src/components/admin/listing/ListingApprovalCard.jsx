// Import các thư viện React và component cần thiết
import React, { useState } from 'react';
import { normalizeImage } from '../../../utils/listingMapper.js'; // Hàm chuẩn hóa đường dẫn ảnh
import ModalListingApprovalCard from './ModalListingApprovalCard.jsx'; // Modal hiển thị chi tiết tin đăng

// Component chính hiển thị từng thẻ tin chờ duyệt
const ListingApprovalCard = ({ listing, onApprove, onReject, loading = false }) => {
    // Khai báo các state dùng để quản lý hiển thị modal và input
    const [showRejectModal, setShowRejectModal] = useState(false); // Bật/tắt modal từ chối
    const [rejectionReason, setRejectionReason] = useState('');    // Lưu lý do từ chối
    const [approvalNote, setApprovalNote] = useState('');          // Lưu ghi chú phê duyệt
    const [showApprovalNote, setShowApprovalNote] = useState(false); // Hiển thị/ẩn ô ghi chú phê duyệt
    const [showDetailModal, setShowDetailModal] = useState(false);   // Hiển thị/ẩn modal chi tiết tin

    // Lấy ID của tin từ các trường có thể có trong object listing
    const requestId = listing.listingId;

    // Xử lý khi admin bấm nút "Phê duyệt"
    const handleApprove = () => {
        // Nếu admin có mở phần ghi chú -> gửi cả ghi chú
        if (showApprovalNote) {
            onApprove(requestId, approvalNote);
            setApprovalNote('');
            setShowApprovalNote(false);
        }
        // Nếu không có ghi chú -> chỉ gửi requestId
        else {
            onApprove(requestId);
        }
    };

    // Xử lý khi admin bấm nút "Từ chối"
    const handleReject = () => {
        // Nếu admin có nhập lý do -> gọi onReject và đóng modal
        if (rejectionReason.trim()) {
            onReject(requestId, rejectionReason);
            setRejectionReason('');
            setShowRejectModal(false);
        }
    };

    // Hàm định dạng tiền VND
    const formatPrice = (price) => {
        if (!price) return 'Chưa có giá';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    };

    // Hàm lấy badge hiển thị tình trạng sản phẩm (Mới, Như mới, Tốt, ...)
    const getConditionBadge = () => {
        const condition = listing.product?.ev?.conditionStatus || listing.product?.battery?.conditionStatus;
        if (!condition) return null;

        // Map các trạng thái -> màu và text
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

    // JSX chính hiển thị card của listing
    return (
        <div className="bg-white rounded-lg shadow-md border border-gray-200 overflow-hidden">
            {/* Phần header: tiêu đề + trạng thái */}
            <div className="p-4 border-b border-gray-100">
                <div className="flex justify-between items-start mb-2">
                    <h3 className="text-lg font-semibold text-gray-900 line-clamp-2">
                        {listing.title}
                    </h3>

                    {/* Hiển thị tình trạng và trạng thái "Chờ duyệt" */}
                    <div className="flex gap-2 items-center">
                        {getConditionBadge()}
                        <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs font-medium">
                            Chờ duyệt
                        </span>
                    </div>
                </div>

                {/* Hiển thị ID và nút xem chi tiết */}
                <div className="flex items-center justify-between text-sm text-gray-600">
                    <span>ID: #{listing.listingId}</span>
                    <div className="flex items-center gap-3">
                        <button
                            type="button"
                            onClick={() => setShowDetailModal(true)}
                            className="px-2 py-1 text-xs rounded-md bg-blue-50 text-blue-700 hover:bg-blue-100"
                            title="Xem chi tiết"
                        >
                            Xem chi tiết
                        </button>
                    </div>
                </div>
            </div>

            {/* Thân thẻ: chứa ảnh và các nút hành động */}
            <div className="p-4">
                <div className="flex gap-4">
                    {/* Ảnh thumbnail sản phẩm */}
                    <div className="flex-shrink-0 ">
                        <img
                            src={normalizeImage(listing.thumbnail)} // Chuẩn hóa đường dẫn ảnh
                            alt={listing.title || 'Listing image'}
                            className="object-cover rounded-lg"
                            style={{ width: '200px', height: '200px' }}
                            onError={(e) => {
                                e.target.src = 'https://placehold.co/200x200?text=No+Image'; // fallback nếu ảnh lỗi
                            }}
                        />
                    </div>
                </div>

                {/* Các nút thao tác */}
                <div className="mt-4 flex gap-3 justify-end border-t pt-4">
                    {/* Nút bật/tắt ghi chú phê duyệt */}
                    <button
                        onClick={() => setShowApprovalNote(!showApprovalNote)}
                        className="px-3 py-1 text-sm text-blue-600 hover:text-blue-800 underline"
                    >
                        {showApprovalNote ? 'Ẩn ghi chú' : 'Thêm ghi chú'}
                    </button>

                    {/* Nút mở modal từ chối */}
                    <button
                        onClick={() => setShowRejectModal(true)}
                        disabled={loading}
                        className="px-4 py-2 rounded-md hover:bg-red-700 disabled:bg-red-300 disabled:text-white disabled:opacity-60 disabled:cursor-not-allowed border-none"
                        style={{ backgroundColor: '#dc2626', color: '#ffffff' }}
                    >
                        {loading ? 'Đang xử lý...' : 'Từ chối'}
                    </button>

                    {/* Nút phê duyệt */}
                    <button
                        onClick={handleApprove}
                        disabled={loading}
                        className="px-4 py-2 rounded-md hover:bg-green-700 disabled:bg-green-300 disabled:text-white disabled:opacity-60 disabled:cursor-not-allowed border-none"
                        style={{ backgroundColor: '#16a34a', color: '#ffffff' }}
                    >
                        {loading ? 'Đang xử lý...' : 'Phê duyệt'}
                    </button>
                </div>

                {/* Nếu bật "Ghi chú phê duyệt" thì hiện ô textarea */}
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

            {/* Modal xác nhận từ chối */}
            {showRejectModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-lg p-6 w-96 max-w-90vw">
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">
                            Từ chối tin đăng
                        </h3>

                        {/* Ô nhập lý do từ chối */}
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

                        {/* Nút xác nhận / hủy */}
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

            {/* Modal hiển thị chi tiết tin đăng */}
            {showDetailModal && (
                <ModalListingApprovalCard
                    isOpen={showDetailModal}
                    onClose={() => setShowDetailModal(false)}
                    data={{
                        // Truyền toàn bộ thông tin cần thiết qua prop data
                        requestId: listing.id,
                        listingId: listing.listingId,
                        thumbnailUrl: listing.thumbnail,
                        price: listing.price,
                        title: listing.title,
                        status: listing.status,
                    }}
                />
            )}
        </div>
    );
};

// Xuất component ra để các file khác import
export default ListingApprovalCard;
