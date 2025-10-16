// Import các thư viện và component cần dùng
import { useEffect, useState } from 'react';
import { useListingApproval } from '../../../hooks/useListingApproval.js';
import ListingApprovalCard from '../../../components/admin/listing/ListingApprovalCard.jsx';
import PaginationBar from '../../../components/PaginationBar.jsx';

// Component chính quản lý danh sách tin chờ duyệt
const PendingListings = () => {
    // Lấy dữ liệu và hàm xử lý từ custom hook useListingApproval
    const {
        listings,            // Danh sách tin đăng chờ duyệt
        loading,             // Trạng thái đang tải danh sách
        actionLoading,       // Trạng thái đang duyệt/từ chối từng tin
        pagination,          // Thông tin phân trang (page, size, totalPages, ...)
        error,               // Lỗi nếu có
        fetchPendingListings,// Hàm gọi API lấy danh sách tin
        handleApproveListing,// Hàm duyệt tin đăng
        handleRejectListing, // Hàm từ chối tin đăng
        clearError           // Hàm xoá lỗi
    } = useListingApproval();

    // State cục bộ của component
    const [currentPage, setCurrentPage] = useState(0);           // Trang hiện tại
    const [pageSize, setPageSize] = useState(12);                // Số tin/trang
    const [sortBy, setSortBy] = useState('createdDate');         // Trường sắp xếp
    const [sortDirection, setSortDirection] = useState('DESC');  // Thứ tự sắp xếp
    const [notification, setNotification] = useState(null);      // Thông báo kết quả approve/reject

    // Gọi API mỗi khi trang, kích thước, sắp xếp thay đổi
    useEffect(() => {
        fetchPendingListings({
            page: currentPage,
            size: pageSize,
            sortBy,
            sortDirection
        });
    }, [fetchPendingListings, currentPage, pageSize, sortBy, sortDirection]);

    // Xử lý khi bấm duyệt tin
    const onApproveListing = async (listingId, note) => {
        const result = await handleApproveListing(listingId, note);
        setNotification({
            type: result.success ? 'success' : 'error',
            message: result.message
        });
        // Ẩn thông báo sau 5s
        setTimeout(() => setNotification(null), 5000);
    };

    // Xử lý khi bấm từ chối tin
    const onRejectListing = async (listingId, reason) => {
        const result = await handleRejectListing(listingId, reason);
        setNotification({
            type: result.success ? 'success' : 'error',
            message: result.message
        });
        // Ẩn thông báo sau 5s
        setTimeout(() => setNotification(null), 5000);
    };

    // Khi đổi trang
    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

    // Khi đổi số lượng tin/trang
    const handlePageSizeChange = (size) => {
        setPageSize(size);
        setCurrentPage(0); // Reset về trang đầu
    };

    // Khi đổi kiểu sắp xếp
    const handleSortChange = (field) => {
        if (sortBy === field) {
            // Nếu đang sắp theo cùng field thì đảo chiều ASC/DESC
            setSortDirection(prev => prev === 'ASC' ? 'DESC' : 'ASC');
        } else {
            // Nếu chọn field khác thì mặc định DESC
            setSortBy(field);
            setSortDirection('DESC');
        }
        setCurrentPage(0); // Reset về trang đầu
    };

    return (
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

                {/* Tiêu đề trang và nút refresh */}
                <div className="mb-8">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-3xl font-bold text-gray-900">
                                Xét duyệt tin đăng
                            </h1>
                            <p className="mt-2 text-gray-600">
                                Quản lý và xét duyệt các tin đăng chờ phê duyệt
                            </p>
                        </div>

                        {/* Hiển thị tổng số tin + nút refresh thủ công */}
                        <div className="flex items-center gap-4">
                            <div className="bg-white rounded-lg shadow px-4 py-2">
                                <span className="text-sm text-gray-600">Tổng tin chờ duyệt: </span>
                                <span className="font-bold text-blue-600">{pagination.totalElements}</span>
                            </div>

                            {/* Nút reload danh sách */}
                            <button
                                onClick={() => {
                                    console.log('Manual fetch triggered');
                                    fetchPendingListings({
                                        page: currentPage,
                                        size: pageSize,
                                        sortBy,
                                        sortDirection
                                    });
                                }}
                                className="px-3 py-1 bg-blue-500 text-white text-sm rounded hover:bg-blue-600"
                            >
                                🔄 Refresh
                            </button>
                        </div>
                    </div>
                </div>

                {/* Hiển thị lỗi nếu có */}
                {error && (
                    <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                        <div className="flex justify-between items-center">
                            <span className="text-red-800">{error}</span>
                            <button
                                onClick={clearError}
                                className="text-gray-400 hover:text-gray-600"
                            >
                                ×
                            </button>
                        </div>
                    </div>
                )}

                {/* Bộ lọc: chọn số lượng hiển thị, sắp xếp, thứ tự */}
                <div className="mb-6 bg-white rounded-lg shadow p-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Hiển thị
                                </label>
                                <select
                                    value={pageSize}
                                    onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                                    className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                                >
                                    <option value={5}>5 tin/trang</option>
                                    <option value={10}>10 tin/trang</option>
                                    <option value={20}>20 tin/trang</option>
                                    <option value={50}>50 tin/trang</option>
                                </select>
                            </div>
                        </div>

                        {/* Phần chọn trường sắp xếp và thứ tự */}
                        <div className="flex items-center gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Sắp xếp theo
                                </label>
                                <select
                                    value={sortBy}
                                    onChange={(e) => handleSortChange(e.target.value)}
                                    className="px-3 py-2 border border-gray-300 rounded-md text-sm"
                                >
                                    <option value="createdDate">Ngày tạo</option>
                                    <option value="title">Tiêu đề</option>
                                    <option value="price">Giá</option>
                                    <option value="category">Danh mục</option>
                                </select>
                            </div>

                            {/* Nút đổi thứ tự ASC/DESC */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Thứ tự
                                </label>
                                <button
                                    onClick={() => setSortDirection(prev => prev === 'ASC' ? 'DESC' : 'ASC')}
                                    className="px-3 py-2 border border-gray-300 rounded-md text-sm hover:bg-gray-50"
                                >
                                    {sortDirection === 'ASC' ? '↑ Tăng dần' : '↓ Giảm dần'}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Khi đang tải dữ liệu thì hiện vòng xoay loading */}
                {loading && (
                    <div className="flex justify-center items-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                        <span className="ml-3 text-gray-600">Đang tải danh sách...</span>
                    </div>
                )}

                {/* Khi đã tải xong dữ liệu */}
                {!loading && (
                    <>
                        {listings.length > 0 ? (
                            // Nếu có tin, render từng tin bằng ListingApprovalCard
                            <div className="space-y-6">
                                {listings.map((listing) => (
                                    <ListingApprovalCard
                                        key={listing.id}
                                        listing={listing}
                                        onApprove={onApproveListing}
                                        onReject={onRejectListing}
                                        loading={actionLoading[listing.id] || false}
                                    />
                                ))}
                            </div>
                        ) : (
                            // Nếu không có tin nào chờ duyệt
                            <div className="text-center py-12">
                                <div className="text-gray-400 text-6xl mb-4">📋</div>
                                <h3 className="text-lg font-medium text-gray-900 mb-2">
                                    Không có tin đăng nào chờ duyệt
                                </h3>
                                <p className="text-gray-600">
                                    Tất cả tin đăng đã được xét duyệt.
                                </p>
                            </div>
                        )}
                    </>
                )}

                {/* Thanh phân trang */}
                {!loading && listings.length > 0 && pagination.totalPages > 1 && (
                    <div className="mt-8 flex justify-center">
                        <PaginationBar
                            currentPage={pagination.number}
                            totalPages={pagination.totalPages}
                            onPageChange={handlePageChange}
                        />
                    </div>
                )}

                {/* Footer tóm tắt: hiển thị số lượng và trang hiện tại */}
                {!loading && listings.length > 0 && (
                    <div className="mt-8 bg-white rounded-lg shadow p-4">
                        <div className="flex justify-between items-center text-sm text-gray-600">
                            <span>
                                Hiển thị {pagination.number * pagination.size + 1} - {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)}
                                trong tổng số {pagination.totalElements} tin đăng
                            </span>
                            <span>
                                Trang {pagination.number + 1} / {pagination.totalPages}
                            </span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PendingListings;
