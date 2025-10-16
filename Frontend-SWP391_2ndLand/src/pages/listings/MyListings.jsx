import React, { useEffect, useState, useCallback, useContext } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
import listingsApi from '../../api/listings';
import { mapListingArray, normalizeImage } from '../../utils/listingMapper';
import { Link } from 'react-router-dom';
import { Card, Tag, Button, Pagination, Empty, Space, Typography, Skeleton, message, Tooltip, Modal } from 'antd';
import { ThunderboltOutlined, EyeOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { ListingStatus, ApprovalStatus } from '../../constants/enums.js';
import { payListingFee, getListingFee, getProfileCompleteness } from '../../api/listings';
import TopUpModal from '../../components/TopUpModal';
import useWalletBalance from '../../hooks/useWalletBalance';
import '../../css/header.css';

/**
 * Trang hiển thị danh sách listing của người dùng hiện tại
 * Gọi backend /api/listings/my-listings
 */
export default function MyListings() {
    const { isLoggedIn } = useContext(AuthContext) || {};
    const [items, setItems] = useState([]);
    const [pagination, setPagination] = useState(null);
    const [page, setPage] = useState(0);
    const [size] = useState(12);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showTopUpModal, setShowTopUpModal] = useState(false);
    const [pendingRetryListingId, setPendingRetryListingId] = useState(null); // listing sẽ retry sau khi nạp
    const { balance, refresh: refreshBalance } = useWalletBalance(isLoggedIn);
    const [fees, setFees] = useState({}); // cache fee by listingId
    const [confirmListing, setConfirmListing] = useState(null); // listing chờ xác nhận thanh toán phí
    const [confirmLoading, setConfirmLoading] = useState(false);

    const fetchData = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const res = await listingsApi.fetchMyListings(page, size);
            // Backend trả về Page object theo YAML spec
            if (res && Array.isArray(res.content)) {
                const mapped = mapListingArray(res.content);
                setItems(mapped);
                setPagination(res);
            } else { setItems([]); setPagination(null);}
        } catch (e) {
            console.error('Fetch my listings error', e);
            setError('Không thể tải danh sách bài đăng của bạn.');
        } finally {
            setLoading(false);
        }
    }, [page, size]);

    // Helper nhận diện trạng thái chờ thanh toán phí (PAY_WAITING) bất kể backend dùng field nào
    const isPayWaiting = (l) => {
        if (!l) return false;
        const raw = l.raw || {};
        return [l.status, l.listingStatus, raw.status, raw.listingStatus].some(v => v === ListingStatus.PAY_WAITING);
    };

    // Debug: log after fetchData completes
    useEffect(() => {
        if (items && items.length) {
            // Only log once per page load
            console.group('[MyListings] Debug items');
            items.forEach(it => {
                console.log('Listing', it.id, { mappedStatus: it.status, mappedListingStatus: it.listingStatus, rawStatus: it.raw?.status, rawListingStatus: it.raw?.listingStatus });
            });
            console.groupEnd();
        }
    }, [items]);

    useEffect(() => {
        if (isLoggedIn) fetchData();
    }, [fetchData, isLoggedIn]);



    const statusTag = (item) => {
        // Prefer approvalStatus if present and not APPROVED (still in workflow)
        const approval = item.approvalStatus || item.raw?.approvalStatus;
        const listingSt = item.status || item.listingStatus;
        let key;
        if (approval && approval !== ApprovalStatus.APPROVED) {
            key = approval; // show workflow state
        } else {
            key = listingSt;
        }
        const map = {
            [ApprovalStatus.PENDING]: { color: 'gold', text: 'Chờ duyệt' },
            [ApprovalStatus.REJECTED]: { color: 'red', text: 'Từ chối' },
            [ApprovalStatus.REQUIRES_CHANGES]: { color: 'orange', text: 'Cần chỉnh sửa' },
            [ListingStatus.ACTIVE]: { color: 'green', text: 'Đang hoạt động' },
            [ListingStatus.SUSPENDED]: { color: 'volcano', text: 'Tạm treo' },
            [ListingStatus.SOLD]: { color: 'purple', text: 'Đã bán' },
            [ListingStatus.EXPIRED]: { color: 'default', text: 'Hết hạn' },
            [ListingStatus.REMOVED]: { color: 'default', text: 'Gỡ bỏ' },
            [ListingStatus.PAY_WAITING]: { color: 'gold', text: 'Chờ thanh toán phí' },
        };
        const cfg = map[key] || { color: 'default', text: key || '—' };
        const tagEl = <Tag color={cfg.color}>{cfg.text}</Tag>;
        if (key === ApprovalStatus.PENDING) {
            return <Tooltip title="Bài đăng đang chờ admin duyệt. Sau khi duyệt sẽ yêu cầu thanh toán phí.">{tagEl}</Tooltip>;
        }
        if (isPayWaiting(item)) {
            const feeVal = fees[item.id];
            return <Tooltip title={feeVal != null ? `Phí cần trả: ${Number(feeVal).toLocaleString('vi-VN')} VND` : 'Đang tải phí...'}>{tagEl}</Tooltip>;
        }
        return tagEl;
    };

    // Fetch fee for listings in PAY_WAITING when items change
    useEffect(() => {
        const fetchFees = async () => {
            const targets = items.filter(it => isPayWaiting(it) && fees[it.id] == null);
            if (targets.length === 0) return;
            for (const it of targets) {
                try {
                    const res = await getListingFee(it.id);
                    if (res && typeof res.fee === 'number') {
                        setFees(prev => ({ ...prev, [it.id]: res.fee }));
                    }
                } catch (e) {
                    // silent; keep undefined
                }
            }
        };
        fetchFees();
    }, [items, fees]);

    return (
        <>
        <div style={{ padding:'2.5rem 1.5rem', maxWidth:1400, margin:'0 auto' }}>
            <Space align="center" style={{ width:'100%', justifyContent:'space-between', marginBottom: 28 }}>
                <Typography.Title level={3} style={{ margin:0, fontSize:26 }}>Bài đăng của tôi</Typography.Title>
                <Button
                    type="primary"
                    onClick={async () => {
                        try {
                            const completeness = await getProfileCompleteness();
                            if (!completeness.phonePresent) {
                                message.warning('Bạn cần cập nhật số điện thoại trong hồ sơ trước khi đăng bài.');
                                // Điều hướng tới trang profile để cập nhật
                                window.location.href = '/account';
                                return;
                            }
                            window.location.href = '/post-listing';
                        } catch (e) {
                            message.error('Không kiểm tra được hồ sơ. Vui lòng thử lại.');
                        }
                    }}
                >+ Đăng mới</Button>
            </Space>
            {error && (
                <Card size="small" style={{ borderColor:'#ff4d4f', marginBottom:16 }}>
                    <Typography.Text type="danger" style={{ fontSize:12 }}>{error}</Typography.Text>
                </Card>
            )}
            {loading ? (
                <div style={{ display:'grid', gap:20, gridTemplateColumns:'repeat(auto-fill,minmax(250px,1fr))' }}>
                    {Array.from({ length: 6 }).map((_,i)=>(
                        <Card key={i} hoverable>
                            <div style={{ position:'relative', paddingTop:'62%', background:'#f2f4f7', borderRadius:4 }}>
                                <Skeleton.Image active style={{ position:'absolute', inset:0, width:'100%', height:'100%' }} />
                            </div>
                            <Skeleton active title paragraph={{ rows: 2 }} style={{ marginTop: 12 }} />
                        </Card>
                    ))}
                </div>
            ) : items.length === 0 ? (
                <Empty description="Bạn chưa có bài đăng" style={{ padding:'80px 0' }}>
                    <Button type="link"><Link to="/post-listing">Đăng bài đầu tiên</Link></Button>
                </Empty>
            ) : (
                <div style={{ display:'grid', gap:20, gridTemplateColumns:'repeat(auto-fill,minmax(250px,1fr))' }}>
                    {items.map(it => (
                        <Card
                            key={it.id}
                            hoverable
                            cover={<div style={{ position:'relative', width:'100%', paddingTop:'62%', background:'#f5f5f5' }}>
                                <img
                                    src={it.thumbnail || normalizeImage(it.raw?.thumbnailUrl)}
                                    alt={it.title}
                                    style={{ position:'absolute', inset:0, width:'100%', height:'100%', objectFit:'cover' }}
                                    loading="lazy"
                                />
                                <Tag style={{ position:'absolute', top:8, left:8 }} color="geekblue">ID #{it.id}</Tag>
                                {it.listingType === 'PREMIUM' && <Tag style={{ position:'absolute', top:8, right:8 }} icon={<ThunderboltOutlined />} color="gold">PREMIUM</Tag>}
                            </div>}
                            styles={{ body: { display:'flex', flexDirection:'column', padding:14 } }}
                            actions={[
                                <Button size="small" type="link"><Link to={`/listings/${it.id}`}>Chi tiết</Link></Button>,
                                                                isPayWaiting(it) && (
                                                                        <Button
                                                                            size="small"
                                                                            type="primary"
                                                                            onClick={async () => {
                                                                                // Đảm bảo có fee trước khi mở modal xác nhận
                                                                                if (fees[it.id] == null) {
                                                                                    try {
                                                                                        const resFee = await getListingFee(it.id);
                                                                                        if (resFee && typeof resFee.fee === 'number') {
                                                                                            setFees(prev => ({ ...prev, [it.id]: resFee.fee }));
                                                                                        }
                                                                                    } catch {/* silent */}
                                                                                }
                                                                                setConfirmListing(it);
                                                                            }}
                                                                        >Thanh toán phí</Button>
                                                                )
                                                        ].filter(Boolean)}
                        >
                            <Space direction="vertical" size={6} style={{ width:'100%' }}>
                                <Typography.Text strong ellipsis={{ tooltip: it.title }} style={{ fontSize:14 }}>{it.title}</Typography.Text>
                                <Space size={[4,4]} wrap>
                                    {it.category && <Tag bordered={false}>{it.category}</Tag>}
                                    {statusTag(it)}
                                    {/* Fallback badge nếu không render đúng tag nhưng thực tế là PAY_WAITING */}
                                    {!isPayWaiting(it) && (it.raw?.status === 'PAY_WAITING' || it.raw?.listingStatus === 'PAY_WAITING') && (
                                        <Tag color="gold">Chờ thanh toán (raw)</Tag>
                                    )}
                                </Space>
                                <Typography.Text style={{ fontWeight:600, color:'#1677ff' }}>
                                    {typeof it.price === 'number' ? it.price.toLocaleString('vi-VN') + ' VND' : 'Liên hệ'}
                                </Typography.Text>
                            </Space>
                        </Card>
                    ))}
                </div>
            )}
            {pagination && items.length > 0 && (
                <div style={{ display:'flex', justifyContent:'center', marginTop:32 }}>
                    <Pagination
                        disabled={loading}
                        current={(pagination.currentPage ?? pagination.page ?? page) + 1}
                        total={pagination.totalPages ? pagination.totalPages * size : (pagination.totalElements || 0)}
                        pageSize={size}
                        showSizeChanger={false}
                        onChange={(p)=> {
                            const zero = p - 1;
                            if (zero !== page) setPage(zero);
                        }}
                    />
                </div>
            )}
        </div>
        <TopUpModal
                    open={showTopUpModal}
                    onClose={() => {
                        setShowTopUpModal(false);
                        setPendingRetryListingId(null);
                    }}
                    refreshBalance={refreshBalance}
                    onCompleted={async () => {
                        // Sau khi nạp thành công, tự retry thanh toán nếu còn pending
                        if (pendingRetryListingId) {
                            try {
                                const res = await payListingFee(pendingRetryListingId);
                                if (res.paid) {
                                    message.success('Thanh toán phí thành công sau khi nạp tiền.');
                                    fetchData();
                                    refreshBalance?.();
                                    // Phát event toàn cục để Navbar (và nơi khác) cập nhật
                                    window.dispatchEvent(new Event('wallet:refresh'));
                                } else if (res.insufficientBalance) {
                                    message.warning('Vẫn không đủ số dư sau khi nạp.');
                                } else {
                                    message.info(res.message || 'Không thể thanh toán sau khi nạp.');
                                }
                            } catch (err) {
                                message.error('Lỗi retry thanh toán sau nạp tiền');
                            } finally {
                                setPendingRetryListingId(null);
                            }
                        }
                    }}
        />
        <Modal
            open={!!confirmListing}
            title={<span><ExclamationCircleOutlined style={{ color:'#faad14', marginRight:8 }} />Xác nhận thanh toán phí đăng tin</span>}
            onCancel={() => !confirmLoading && setConfirmListing(null)}
            okText={confirmLoading ? 'Đang xử lý...' : 'Thanh toán ngay'}
            okButtonProps={{ disabled: confirmLoading }}
            cancelText="Hủy"
            destroyOnClose
            onOk={async () => {
                if (!confirmListing) return;
                setConfirmLoading(true);
                try {
                    const res = await payListingFee(confirmListing.id);
                    if (res.insufficientBalance) {
                        message.warning('Không đủ số dư. Vui lòng nạp tiền.');
                        setPendingRetryListingId(confirmListing.id);
                        setShowTopUpModal(true);
                    } else if (res.paid) {
                        message.success('Thanh toán phí thành công. Bài đăng đã ACTIVE.');
                        fetchData();
                        refreshBalance?.();
                        window.dispatchEvent(new Event('wallet:refresh'));
                    } else {
                        message.info(res.message || 'Không thể thanh toán');
                    }
                } catch (e) {
                    message.error('Lỗi khi thanh toán phí');
                } finally {
                    setConfirmLoading(false);
                    setConfirmListing(null);
                }
            }}
        >
            {confirmListing ? (
                <Space direction="vertical" size={12} style={{ width:'100%' }}>
                    <Typography.Text>
                        Bạn chuẩn bị thanh toán phí đăng tin cho bài: <strong>{confirmListing.title}</strong>
                    </Typography.Text>
                    <Typography.Text>
                        Mức phí: <strong>{fees[confirmListing.id] != null ? fees[confirmListing.id].toLocaleString('vi-VN') + ' VND' : 'Đang tải...'}</strong>
                    </Typography.Text>
                    <Typography.Paragraph style={{ fontSize:12, color:'#555' }}>
                        Sau khi thanh toán, bài đăng sẽ chuyển sang trạng thái ACTIVE và phí sẽ bị trừ khỏi ví của bạn. Hành động này không thể hoàn tác.
                    </Typography.Paragraph>
                    {balance != null && (
                        <Typography.Text type={balance < (fees[confirmListing.id] || 0) ? 'danger' : 'secondary'}>
                            Số dư ví hiện tại: {balance.toLocaleString('vi-VN')} VND
                        </Typography.Text>
                    )}
                </Space>
            ) : null}
        </Modal>
    </>
    );
}