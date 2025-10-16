// Import các hook và component cần thiết
import React, { useEffect, useState } from 'react';
import { Modal, Button, Badge, Row, Col, Card, Spinner } from 'react-bootstrap';

// Import các hàm tiện ích
import { normalizeImage } from '../../../utils/listingMapper.js'; // Chuẩn hóa URL hình ảnh
import { formatVnd } from '../../../utils/numberFormatting.js'; // Định dạng số tiền VND
import { getListingDetail } from '../../../api/admin.js'; // API lấy chi tiết listing

/**
 * Component Modal hiển thị chi tiết tin đăng (listing) cho admin duyệt
 * - Khi modal mở, nó sẽ tự động fetch chi tiết từ backend.
 */
const ModalListingApprovalCard = ({ isOpen, onClose, data }) => {
    // --- State ---
    const [loading, setLoading] = useState(false); // Hiển thị trạng thái "đang tải"
    const [error, setError] = useState(null); // Lưu lỗi khi fetch
    const [detail, setDetail] = useState(null); // Lưu thông tin chi tiết tin đăng

    // --- useEffect: Gọi API khi modal mở ---
    useEffect(() => {
        // Lấy listingId từ nhiều nguồn khác nhau (phòng trường hợp backend gửi khác key)
        let listingId = data?.listingId || data?.ListingId || data?.raw?.listingId;

        // Nếu không có listingId thì thử fallback sang requestId
        if (!listingId && data?.requestId) {
            listingId = data.requestId;
        }

        // Khi modal mở và có listingId thì gọi API lấy chi tiết
        if (isOpen && listingId) {
            fetchDetail();
        }

        // Khi modal đóng → reset dữ liệu chi tiết & lỗi
        if (!isOpen) {
            setDetail(null);
            setError(null);
        }
    }, [isOpen, data?.listingId, data?.ListingId, data?.raw?.listingId, data?.requestId]);

    // --- Hàm fetch chi tiết listing ---
    const fetchDetail = async () => {
        setLoading(true);
        setError(null);
        try {
            // Tương tự trên: lấy listingId từ nhiều nguồn
            let listingId = data?.listingId || data?.ListingId || data?.raw?.listingId;
            if (!listingId && data?.requestId) listingId = data.requestId;

            if (!listingId) throw new Error('Không tìm thấy listingId');

            // Gọi API lấy chi tiết
            const response = await getListingDetail(listingId);
            setDetail(response); // Lưu dữ liệu vào state
        } catch (err) {
            console.error('Error fetching listing detail:', err);
            setError('Không thể tải chi tiết listing. Vui lòng thử lại.');
        } finally {
            setLoading(false);
        }
    };

    // --- Nếu có chi tiết thì dùng, nếu không thì dùng data truyền vào ---
    const displayData = detail || data || {};

    // --- Chuẩn hóa các biến hiển thị ---
    const requestId = data?.requestId ?? null;
    const listingId = data?.listingId ||  null;
    const title = detail?.title ?? '—';
    const description = detail?.description ?? '—';
    const price =
        typeof displayData?.price === 'number'
            ? displayData.price
            : (displayData?.price ? Number(displayData.price) : null);
    const status = detail?.status ?? '—';
    const listingType = detail?.listingType ?? '—';
    const thumbnail = normalizeImage( data?.thumbnailUrl || '');
    const createdAt = displayData?.createdAt
        ? new Date(displayData.createdAt).toLocaleString('vi-VN')
        : '—';

    // --- Xác định loại sản phẩm ---
    let category = '—';
    if (detail?.product) {
        category = detail.product.ev ? 'EV' : (detail.product.battery ? 'BATTERY' : '—');
    }

    // --- Thông tin người bán ---
    const sellerName = detail?.seller?.name ?? '—';
    const sellerEmail = detail?.seller?.email ?? '—';
    const sellerPhone = detail?.seller?.phoneNumber ?? '—';

    // --- Thông tin vị trí ---
    const province = detail?.location?.province ?? '—';
    const district = detail?.location?.district ?? '—';

    // --- Chi tiết xe điện (EV) ---
    const ev = detail?.product?.ev;
    const vehicleType = ev?.type ?? null;
    const vehicleBrand = ev?.brand ?? null;
    const vehicleName = ev?.name ?? null;
    const vehicleYear = ev?.year ?? null;
    const vehicleMileage = ev?.mileage ?? null;
    const vehicleBatteryCapacity = ev?.batteryCapacity ?? null;
    const vehicleCondition = ev?.conditionStatus ?? null;

    // --- Chi tiết pin ---
    const battery = detail?.product?.battery;
    const batteryBrand = battery?.brand ?? null;
    const batteryCapacity = battery?.capacity ?? null;
    const batteryHealth = battery?.healthPercentage ?? null;
    const batteryCompatibleVehicles = battery?.compatibleVehicles ?? null;
    const batteryCondition = battery?.conditionStatus ?? null;

    // --- Xác định loại ---
    const isEV = category === 'EV';
    const isBattery = category === 'BATTERY';

    // --- Map status sang màu và nhãn ---
    const statusVariant = (val) => {
        switch (val) {
            case 'PENDING': return 'warning';
            case 'APPROVED': return 'success';
            case 'REJECTED': return 'danger';
            case 'ACTIVE': return 'success';
            case 'SOLD': return 'secondary';
            case 'SUSPENDED': return 'danger';
            default: return 'secondary';
        }
    };

    const statusLabel = (val) => {
        switch (val) {
            case 'ACTIVE': return 'Hoạt động';
            case 'SUSPENDED': return 'Tạm khóa';
            case 'SOLD': return 'Đã bán';
            case 'PENDING': return 'Chờ duyệt';
            default: return val || '—';
        }
    };

    const listingTypeLabel = (val) => {
        const type = typeof val === 'object' && val?.name ? val.name : val;
        switch (type) {
            case 'NORMAL': return 'Thường';
            case 'FEATURED': return 'Nổi bật';
            case 'PREMIUM': return 'Cao cấp';
            default: return type || '—';
        }
    };

    const conditionLabel = (val) => {
        const labels = {
            'GOOD': 'Tốt',
            'FAIR': 'Khá',
            'POOR': 'Trung bình',
            'EXCELLENT': 'Xuất sắc',
            'NEW': 'Mới',
            'LIKE_NEW': 'Như mới',
            'NEEDS_REPLACEMENT': 'Cần thay thế'
        };
        return labels[val] || val || '—';
    };

    const vehicleTypeLabel = (val) => {
        const labels = {
            'CAR': 'Ô tô',
            'BIKE': 'Xe đạp điện',
            'MOTORBIKE': 'Xe máy điện'
        };
        return labels[val] || val || '—';
    };

    // --- Render giao diện Modal ---
    return (
        <Modal show={isOpen} onHide={onClose} centered size="xl" scrollable>
            {/* Header modal */}
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết yêu cầu duyệt tin</Modal.Title>
            </Modal.Header>

            <Modal.Body style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                {/* Trạng thái đang tải */}
                {loading && (
                    <div className="text-center py-5">
                        <Spinner animation="border" variant="primary" />
                        <p className="mt-3 text-muted">Đang tải chi tiết...</p>
                    </div>
                )}

                {/* Trạng thái lỗi */}
                {error && !loading && (
                    <div className="alert alert-danger" role="alert">
                        <strong>Lỗi!</strong> {error}
                        <Button
                            variant="link"
                            size="sm"
                            onClick={fetchDetail}
                            className="ms-2"
                        >
                            Thử lại
                        </Button>
                    </div>
                )}

                {/* Hiển thị nội dung khi có dữ liệu */}
                {!loading && !error && (
                    <>
                        {/* Ảnh thumbnail + thông tin cơ bản */}
                        <div className="d-flex align-items-start gap-3 mb-4">
                            {/* Hình ảnh */}
                            <div className="me-1" style={{ flex: '0 0 auto' }}>
                                <img
                                    src={thumbnail}
                                    alt={title}
                                    className="rounded border"
                                    style={{ width: 200, height: 200, objectFit: 'cover' }}
                                    onError={(e) => { e.currentTarget.src = 'https://placehold.co/200x200?text=No+Image'; }}
                                />
                            </div>

                            {/* Thông tin cơ bản */}
                            <div className="flex-grow-1 min-w-0">
                                <h4 className="h5 mb-3">{title}</h4>

                                {/* Grid hiển thị thông tin */}
                                <Row className="g-2 small mb-3">
                                    <Col xs={6} md={4}>
                                        <span className="text-muted">Request ID:</span> <strong>#{requestId}</strong>
                                    </Col>
                                    <Col xs={6} md={4}>
                                        <span className="text-muted">Listing ID:</span> <strong>#{listingId}</strong>
                                    </Col>
                                    <Col xs={12} md={4}>
                                        <span className="text-muted">Giá:</span> <strong className="text-success">{formatVnd(price)} ₫</strong>
                                    </Col>
                                    <Col xs={6} md={4}>
                                        <span className="text-muted">Trạng thái:</span>{' '}
                                        <Badge bg={statusVariant(status)} pill>{statusLabel(status)}</Badge>
                                    </Col>
                                    <Col xs={6} md={4}>
                                        <span className="text-muted">Loại tin:</span>{' '}
                                        <strong>{listingTypeLabel(listingType)}</strong>
                                    </Col>
                                    <Col xs={6} md={4}>
                                        <span className="text-muted">Loại sản phẩm:</span> <strong>{category === 'EV' ? 'Xe điện' : category === 'BATTERY' ? 'Pin' : '—'}</strong>
                                    </Col>
                                    <Col xs={6} md={4}>
                                        <span className="text-muted">Ngày tạo:</span> <strong>{createdAt}</strong>
                                    </Col>
                                </Row>

                                {/* Mô tả */}
                                <div className="small">
                                    <strong>Mô tả:</strong>
                                    <p className="mb-0 mt-1 text-muted" style={{ whiteSpace: 'pre-wrap' }}>{description}</p>
                                </div>
                            </div>
                        </div>

                        <hr />

                        {/* Thông tin chi tiết (Seller, Location, Product) */}
                        <Row className="g-3">
                            {/* Seller Info */}
                            <Col md={6}>
                                <Card className="h-100">
                                    <Card.Header className="bg-primary text-white">
                                        <strong> Thông tin người bán</strong>
                                    </Card.Header>
                                    <Card.Body>
                                        <div className="small">
                                            <p className="mb-2"><strong>Tên:</strong> {sellerName}</p>
                                            <p className="mb-2"><strong>Email:</strong> {sellerEmail}</p>
                                            <p className="mb-0"><strong>SĐT:</strong> {sellerPhone}</p>
                                        </div>
                                    </Card.Body>
                                </Card>
                            </Col>

                            {/* Location Info */}
                            <Col md={6}>
                                <Card className="h-100">
                                    <Card.Header className="bg-success text-white">
                                        <strong> Vị trí</strong>
                                    </Card.Header>
                                    <Card.Body>
                                        <div className="small">
                                            <p className="mb-2"><strong>Tỉnh/TP:</strong> {province}</p>
                                            <p className="mb-0"><strong>Quận/Huyện:</strong> {district}</p>
                                        </div>
                                    </Card.Body>
                                </Card>
                            </Col>

                            {/* EV Info */}
                            {isEV && (
                                <Col md={12}>
                                    <Card>
                                        <Card.Header className="bg-info text-white">
                                            <strong>Thông tin xe điện</strong>
                                        </Card.Header>
                                        <Card.Body>
                                            <Row className="g-2 small">
                                                <Col xs={6} md={4}>
                                                    <strong>Loại xe:</strong> {vehicleTypeLabel(vehicleType)}
                                                </Col>
                                                <Col xs={6} md={4}>
                                                    <strong>Hãng:</strong> {vehicleBrand || '—'}
                                                </Col>
                                                <Col xs={6} md={4}>
                                                    <strong>Tên xe:</strong> {vehicleName || '—'}
                                                </Col>
                                                <Col xs={6} md={4}>
                                                    <strong>Năm SX:</strong> {vehicleYear || '—'}
                                                </Col>
                                                <Col xs={6} md={4}>
                                                    <strong>Số km:</strong> {vehicleMileage != null ? `${vehicleMileage.toLocaleString()} km` : '—'}
                                                </Col>
                                                <Col xs={6} md={4}>
                                                    <strong>Dung lượng pin:</strong> {vehicleBatteryCapacity != null ? `${vehicleBatteryCapacity} kWh` : '—'}
                                                </Col>
                                                <Col xs={12}>
                                                    <strong>Tình trạng:</strong>{' '}
                                                    <Badge bg="secondary">{conditionLabel(vehicleCondition)}</Badge>
                                                </Col>
                                            </Row>
                                        </Card.Body>
                                    </Card>
                                </Col>
                            )}

                            {/* Battery Info */}
                            {isBattery && (
                                <Col md={12}>
                                    <Card>
                                        <Card.Header className="bg-warning text-dark">
                                            <strong>Thông tin pin điện</strong>
                                        </Card.Header>
                                        <Card.Body>
                                            <Row className="g-2 small">
                                                <Col xs={6} md={4}>
                                                    <strong>Hãng:</strong> {batteryBrand || '—'}
                                                </Col>
                                                <Col xs={6} md={4}>
                                                    <strong>Dung lượng:</strong> {batteryCapacity != null ? `${batteryCapacity} kWh` : '—'}
                                                </Col>
                                                <Col xs={6} md={4}>
                                                    <strong>Sức khỏe:</strong> {batteryHealth != null ? `${batteryHealth}%` : '—'}
                                                </Col>
                                                <Col xs={12}>
                                                    <strong>Xe tương thích:</strong> {batteryCompatibleVehicles || '—'}
                                                </Col>
                                                <Col xs={12}>
                                                    <strong>Tình trạng:</strong>{' '}
                                                    <Badge bg="secondary">{conditionLabel(batteryCondition)}</Badge>
                                                </Col>
                                            </Row>
                                        </Card.Body>
                                    </Card>
                                </Col>
                            )}
                        </Row>

                        {/* Hiển thị nguồn ảnh nếu có */}
                        {data?.thumbnailUrl && (
                            <div className="mt-3 p-2 bg-light rounded">
                                <small className="text-muted">
                                    Ảnh nguồn:{' '}
                                    <a
                                        href={normalizeImage(data.thumbnailUrl)}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="text-break"
                                    >
                                        {data.thumbnailUrl}
                                    </a>
                                </small>
                            </div>
                        )}
                    </>
                )}
            </Modal.Body>

            {/* Footer của Modal */}
            <Modal.Footer>
                <Button variant="secondary" onClick={onClose}>Đóng</Button>
            </Modal.Footer>
        </Modal>
    );
};

export default ModalListingApprovalCard;
