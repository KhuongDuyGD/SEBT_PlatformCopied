import React, { useEffect, useState } from 'react';
import { Modal, Button, Badge, Row, Col, Card, Spinner } from 'react-bootstrap';
import { normalizeImage } from '../../../utils/listingMapper.js';
import { formatVnd } from '../../../utils/numberFormatting.js';
import { getListingDetail } from '../../../api/admin.js';

/**
 * Modal hiển thị đầy đủ thông tin listing cho admin duyệt tin
 * Fetch chi tiết từ backend khi mở modal
 */
const ModalListingApprovalCard = ({ isOpen, onClose, data }) => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [detail, setDetail] = useState(null);

    // Fetch detail when modal opens
    useEffect(() => {
        let listingId = data?.listingId || data?.ListingId || data?.raw?.listingId;

        // FALLBACK: If no listingId, try using requestId
        if (!listingId && data?.requestId) {
            listingId = data.requestId;
        }

        if (isOpen && listingId) {
            fetchDetail();
        }
        // Reset when modal closes
        if (!isOpen) {
            setDetail(null);
            setError(null);
        }
    }, [isOpen, data?.listingId, data?.ListingId, data?.raw?.listingId, data?.requestId]);

    const fetchDetail = async () => {
        setLoading(true);
        setError(null);
        try {
            let listingId = data?.listingId || data?.ListingId || data?.raw?.listingId;

            // FALLBACK: If no listingId found, try to get from requestId
            if (!listingId && data?.requestId) {
                listingId = data.requestId;
            }

            if (!listingId) {
                throw new Error('Không tìm thấy listingId');
            }

            const response = await getListingDetail(listingId);
            setDetail(response);
        } catch (err) {
            console.error('Error fetching listing detail:', err);
            setError('Không thể tải chi tiết listing. Vui lòng thử lại.');
        } finally {
            setLoading(false);
        }
    };

    // Use detail data if available, fallback to basic data
    const displayData = detail || data || {};

    const requestId = data?.requestId ?? null;
    const listingId = displayData?.id || data?.listingId || data?.raw?.listingId || null;
    const title = displayData?.title ?? data?.title ?? '—';
    const description = detail?.description ?? '—'; // From ListingDetailResponseDTO
    const price = typeof displayData?.price === 'number' ? displayData.price : (displayData?.price ? Number(displayData.price) : null);
    const status = detail?.status ?? '—'; // Listing status: ACTIVE/SUSPENDED/SOLD
    const listingType = detail?.listingType ?? '—'; // NORMAL/FEATURED/PREMIUM
    const thumbnail = normalizeImage(displayData?.thumbnail || displayData?.thumbnailImage || data?.thumbnailUrl || '');
    const createdAt = displayData?.createdAt ? new Date(displayData.createdAt).toLocaleString('vi-VN') : '—';

    // Determine category from product
    let category = '—';
    if (detail?.product) {
        category = detail.product.ev ? 'EV' : (detail.product.battery ? 'BATTERY' : '—');
    }

    // Seller info from detail
    const sellerName = detail?.seller?.username ?? '—';
    const sellerEmail = detail?.seller?.email ?? '—';
    const sellerPhone = detail?.seller?.phoneNumber ?? '—';

    // Location info from detail
    const province = detail?.location?.province ?? '—';
    const district = detail?.location?.district ?? '—';

    // EV info from detail
    const ev = detail?.product?.ev;
    const vehicleType = ev?.type ?? null;
    const vehicleBrand = ev?.brand ?? null;
    const vehicleName = ev?.name ?? null;
    const vehicleYear = ev?.year ?? null;
    const vehicleMileage = ev?.mileage ?? null;
    const vehicleBatteryCapacity = ev?.batteryCapacity ?? null;
    const vehicleCondition = ev?.conditionStatus ?? null;

    // Battery info from detail
    const battery = detail?.product?.battery;
    const batteryBrand = battery?.brand ?? null;
    const batteryCapacity = battery?.capacity ?? null;
    const batteryHealth = battery?.healthPercentage ?? null;
    const batteryCompatibleVehicles = battery?.compatibleVehicles ?? null;
    const batteryCondition = battery?.conditionStatus ?? null;

    const isEV = category === 'EV';
    const isBattery = category === 'BATTERY';

    // Map status -> label + bootstrap variant
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
        // Handle enum object {name: "NORMAL"} or string "NORMAL"
        const type = typeof val === 'object' && val?.name ? val.name : val;
        switch (type) {
            case 'NORMAL': return '🔵 Thường';
            case 'FEATURED': return '⭐ Nổi bật';
            case 'PREMIUM': return '👑 Cao cấp';
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

    return (
        <Modal show={isOpen} onHide={onClose} centered size="xl" scrollable>
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết yêu cầu duyệt tin</Modal.Title>
            </Modal.Header>

            <Modal.Body style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                {/* Loading State */}
                {loading && (
                    <div className="text-center py-5">
                        <Spinner animation="border" variant="primary" />
                        <p className="mt-3 text-muted">Đang tải chi tiết...</p>
                    </div>
                )}

                {/* Error State */}
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

                {/* Content - Only show when not loading and no error */}
                {!loading && !error && (
                    <>
                        {/* Header with thumbnail and basic info */}
                        <div className="d-flex align-items-start gap-3 mb-4">
                            <div className="me-1" style={{ flex: '0 0 auto' }}>
                                <img
                                    src={thumbnail}
                                    alt={title}
                                    className="rounded border"
                                    style={{ width: 200, height: 200, objectFit: 'cover' }}
                                    onError={(e) => { e.currentTarget.src = 'https://placehold.co/200x200?text=No+Image'; }}
                                />
                            </div>

                            <div className="flex-grow-1 min-w-0">
                                <h4 className="h5 mb-3">{title}</h4>

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
                                        <span className="text-muted">Loại sản phẩm:</span> <strong>{category === 'EV' ? '🚗 Xe điện' : category === 'BATTERY' ? '🔋 Pin' : '—'}</strong>
                                    </Col>
                                    <Col xs={6} md={4}>
                                        <span className="text-muted">Ngày tạo:</span> <strong>{createdAt}</strong>
                                    </Col>
                                </Row>

                                <div className="small">
                                    <strong>Mô tả:</strong>
                                    <p className="mb-0 mt-1 text-muted" style={{ whiteSpace: 'pre-wrap' }}>{description}</p>
                                </div>
                            </div>
                        </div>

                        <hr />

                        {/* Detailed Information Cards */}
                        <Row className="g-3">
                            {/* Seller Information */}
                            <Col md={6}>
                                <Card className="h-100">
                                    <Card.Header className="bg-primary text-white">
                                        <strong>👤 Thông tin người bán</strong>
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

                            {/* Location Information */}
                            <Col md={6}>
                                <Card className="h-100">
                                    <Card.Header className="bg-success text-white">
                                        <strong>📍 Vị trí</strong>
                                    </Card.Header>
                                    <Card.Body>
                                        <div className="small">
                                            <p className="mb-2"><strong>Tỉnh/TP:</strong> {province}</p>
                                            <p className="mb-0"><strong>Quận/Huyện:</strong> {district}</p>
                                        </div>
                                    </Card.Body>
                                </Card>
                            </Col>

                            {/* Product Information - EV */}
                            {isEV && (
                                <Col md={12}>
                                    <Card>
                                        <Card.Header className="bg-info text-white">
                                            <strong>🚗 Thông tin xe điện</strong>
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

                            {/* Product Information - Battery */}
                            {isBattery && (
                                <Col md={12}>
                                    <Card>
                                        <Card.Header className="bg-warning text-dark">
                                            <strong>🔋 Thông tin pin điện</strong>
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

            <Modal.Footer>
                <Button variant="secondary" onClick={onClose}>Đóng</Button>
            </Modal.Footer>
        </Modal>
    );
};

export default ModalListingApprovalCard;

