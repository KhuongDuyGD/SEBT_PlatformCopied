import React from 'react';
import { Modal, Button, Badge } from 'react-bootstrap';
import { normalizeImage } from '../../../utils/listingMapper.js';
import { formatVnd } from '../../../utils/numberFormatting.js';

/**
 * Modal hiển thị đầy đủ thông tin mà backend trả về cho yêu cầu duyệt tin.
 * Backend fields (theo yêu cầu):
 *  - requestId: number
 *  - ListingId: number (có thể là listingId ở vài API khác)
 *  - thumbnailUrl: string (ảnh đại diện)
 *  - price: number
 *  - title: string
 *  - status: string (PENDING | APPROVED | REJECTED | ...)
 *
 * Props
 *  - isOpen: boolean
 *  - onClose: () => void
 *  - data: object chứa các field ở trên
 */
const ModalListingApprovalCard = ({ isOpen, onClose, data }) => {

    const requestId = data?.requestId ?? null;
    const listingId = data?.ListingId ?? null;
    const title = data?.title ?? '—';
    const price = typeof data?.price === 'number' ? data.price : (data?.price ? Number(data.price) : null);
    const status = data?.status ?? '—';
    const thumbnail = normalizeImage(data?.thumbnailUrl || '');

    // Map status -> label + bootstrap variant (theo enum mới từ backend)
    const statusVariant = (val) => {
        switch (val) {
            case 'ACTIVE': return 'success'; // Đang hoạt động
            case 'SOLD': return 'secondary'; // Đã bán
            case 'EXPIRED': return 'warning'; // Hết hạn
            case 'REMOVED': return 'secondary'; // Đã gỡ
            case 'SUSPENDED': return 'danger'; // Tạm khóa
            default: return 'secondary';
        }
    };

    const statusLabel = (val) => {
        switch (val) {
            case 'ACTIVE': return 'Đang hoạt động';
            case 'SOLD': return 'Đã bán';
            case 'EXPIRED': return 'Hết hạn';
            case 'REMOVED': return 'Đã gỡ';
            case 'SUSPENDED': return 'Tạm khóa';
            default: return val || '—';
        }
    };

    return (
        <Modal show={isOpen} onHide={onClose} centered size="lg" scrollable>
            <Modal.Header closeButton>
                <Modal.Title>Chi tiết yêu cầu duyệt tin</Modal.Title>
            </Modal.Header>

            <Modal.Body style={{ maxHeight: '65vh', overflowY: 'auto' }}>
                <div className="d-flex align-items-start gap-3">
                    {/* Ảnh thumbnail */}
                    <div className="me-1" style={{ flex: '0 0 auto' }}>
                        <img
                            src={thumbnail}
                            alt={title}
                            className="rounded border"
                            style={{ width: 160, height: 160, objectFit: 'cover' }}
                            onError={(e) => { e.currentTarget.src = 'https://placehold.co/160x160?text=No+Image'; }}
                        />
                    </div>

                    {/* Thông tin chính */}
                    <div className="flex-grow-1 min-w-0">
                        <h4 className="h5 mb-3">{title}</h4>

                        <div className="row g-2 small">
                            <div className="col-12 col-sm-6 d-flex align-items-center">
                                <div className="text-muted me-2" style={{ minWidth: 100 }}>Request ID</div>
                                <div className="fw-medium">{requestId != null ? `#${requestId}` : 'N/A'}</div>
                            </div>
                            <div className="col-12 col-sm-6 d-flex align-items-center">
                                <div className="text-muted me-2" style={{ minWidth: 100 }}>Listing ID</div>
                                <div className="fw-medium">{listingId != null ? `#${listingId}` : 'N/A'}</div>
                            </div>
                            <div className="col-12 col-sm-6 d-flex align-items-center">
                                <div className="text-muted me-2" style={{ minWidth: 100 }}>Giá</div>
                                <div className="fw-semibold text-success">
                                    {price === null || Number.isNaN(price) ? 'N/A' : `${formatVnd(price)} ₫`}
                                </div>
                            </div>
                            <div className="col-12 col-sm-6 d-flex align-items-center">
                                <div className="text-muted me-2" style={{ minWidth: 100 }}>Trạng thái</div>
                                <Badge bg={statusVariant(status)} pill>
                                    {statusLabel(status)}
                                </Badge>
                            </div>
                        </div>

                        {data?.thumbnailUrl && (
                            <div className="mt-2 text-muted small" style={{ wordBreak: 'break-all' }} title={data.thumbnailUrl}>
                                Ảnh nguồn: <a href={normalizeImage(data.thumbnailUrl)} target="_blank" rel="noreferrer">{data.thumbnailUrl}</a>
                            </div>
                        )}
                    </div>
                </div>
            </Modal.Body>

            <Modal.Footer>
                <Button variant="secondary" onClick={onClose}>Đóng</Button>
            </Modal.Footer>
        </Modal>
    );
};

export default ModalListingApprovalCard;

