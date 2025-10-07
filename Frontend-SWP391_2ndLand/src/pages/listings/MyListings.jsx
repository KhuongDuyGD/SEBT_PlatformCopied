import React, { useEffect, useState, useCallback, useContext } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
import listingsApi from '../../api/listings';
import { mapListingArray, normalizeImage } from '../../utils/listingMapper';
import { Link } from 'react-router-dom';
import { Card, Tag, Button, Pagination, Empty, Space, Typography, Skeleton } from 'antd';
import { ThunderboltOutlined, EyeOutlined } from '@ant-design/icons';
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

    useEffect(() => {
        if (isLoggedIn) fetchData();
    }, [fetchData, isLoggedIn]);



    const statusTag = (st) => {
        const map = {
            PENDING: { color: 'gold', text: 'Chờ duyệt' },
            APPROVED: { color: 'green', text: 'Đã duyệt' },
            REJECTED: { color: 'red', text: 'Từ chối' },
            SOLD: { color: 'purple', text: 'Đã bán' }
        };
        const cfg = map[st] || { color: 'default', text: st };
        return <Tag color={cfg.color}>{cfg.text}</Tag>;
    };

    return (
        <div style={{ padding:'2.5rem 1.5rem', maxWidth:1400, margin:'0 auto' }}>
            <Space align="center" style={{ width:'100%', justifyContent:'space-between', marginBottom: 28 }}>
                <Typography.Title level={3} style={{ margin:0, fontSize:26 }}>Bài đăng của tôi</Typography.Title>
                <Button type="primary"><Link to="/post-listing">+ Đăng mới</Link></Button>
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
                            actions={[<Button size="small" type="link"><Link to={`/listings/${it.id}`}>Chi tiết</Link></Button>]}
                        >
                            <Space direction="vertical" size={6} style={{ width:'100%' }}>
                                <Typography.Text strong ellipsis={{ tooltip: it.title }} style={{ fontSize:14 }}>{it.title}</Typography.Text>
                                <Space size={[4,4]} wrap>
                                    {it.category && <Tag bordered={false}>{it.category}</Tag>}
                                    {statusTag(it.status)}
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
    );
}