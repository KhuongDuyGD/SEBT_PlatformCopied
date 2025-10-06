import { useEffect, useState, useCallback, useRef } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import listingsApi from '../../api/listings';
import { mapListingArray } from '../../utils/listingMapper';
import { Row, Col, Card, Select, Tag, Button, Empty, Skeleton, Pagination, Space, Typography } from 'antd';
import ListingSkeletonGrid from '../../components/ListingSkeletonGrid';
import '../../css/header.css';

export default function SearchResults() {
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = searchParams.get('keyword') || '';
  const pageParam = Number(searchParams.get('page')) || 0;
  const sizeParam = Number(searchParams.get('size')) || 12;

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [results, setResults] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [page, setPage] = useState(pageParam);
  const [size, setSize] = useState(sizeParam);

  const normalizeImage = (url) => {
    if (!url) return 'https://placehold.co/400x300?text=No+Image';
    if (url.startsWith('http')) return url;
    if (url.startsWith('/')) return `http://localhost:8080${url}`; // backend static images
    return url;
  };

  const abortRef = useRef();
  const fetchData = useCallback(async () => {
    if (!keyword.trim()) { setResults([]); setPagination(null); return; }
    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    try {
      setLoading(true); setError(null);
      const res = await listingsApi.keywordSearch(keyword, page, size, { signal: controller.signal });
      if (res?.success) {
        const mapped = mapListingArray(Array.isArray(res.data) ? res.data : []);
        if (mapped.length && import.meta.env.DEV) console.debug('[SearchResults] sample item', mapped[0]);
        setResults(mapped);
        setPagination(res.pagination || null);
      } else { setResults([]); setPagination(null); }
    } catch (e) {
      if (e.name === 'AbortError' || e.name === 'CanceledError') {
        if (import.meta.env.DEV) console.debug('[SearchResults] aborted');
      } else {
        console.error('Keyword search error', e);
        setError('Không thể tìm kiếm lúc này.');
        setResults([]); setPagination(null);
      }
    } finally { setLoading(false); }
  }, [keyword, page, size]);

  useEffect(() => { fetchData(); }, [fetchData]);

  useEffect(() => {
    setSearchParams({ keyword, page: String(page), size: String(size) });
  }, [keyword, page, size]);

  const nextPage = () => pagination?.hasNext && setPage(p => p + 1);
  const prevPage = () => pagination?.hasPrevious && setPage(p => Math.max(0, p - 1));

  const sizeOptions = [6,12,24,36].map(v => ({ value: v, label: `${v} / trang` }));
  return (
    <div style={{ padding: '1.25rem 1.5rem', maxWidth: 1400, margin: '0 auto' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 16 }} wrap>
        <Typography.Title level={4} style={{ margin: 0, fontSize: 20 }}>
          Kết quả tìm kiếm cho: <span style={{ color: '#1677ff' }}>{keyword}</span>
        </Typography.Title>
        <Select
          size="small"
          style={{ width: 140 }}
            value={size}
            options={sizeOptions}
            onChange={(val)=> { setSize(val); setPage(0); }}
        />
      </Space>
      {error && (
        <Card size="small" style={{ borderColor: '#ff4d4f', marginBottom: 16 }}>
          <Typography.Text type="danger" style={{ fontSize: 12 }}>{error}</Typography.Text>
        </Card>
      )}
      {loading ? (
        <ListingSkeletonGrid count={Math.min(size, 8)} />
      ) : results.length === 0 ? (
        <Empty description="Không có kết quả" style={{ padding: '64px 0' }} />
      ) : (
        <Row gutter={[20,20]}>
          {results.map(item => (
            <Col key={item.id} xs={24} sm={12} md={8} lg={6}>
              <Card
                hoverable
                cover={<div style={{ position:'relative', width: '100%', paddingTop: '66%', background: '#f5f5f5' }}>
                  <img
                    src={item.thumbnail}
                    alt={item.title}
                    style={{ position:'absolute', inset:0, width:'100%', height:'100%', objectFit:'cover' }}
                    loading="lazy"
                  />
                  <Tag style={{ position:'absolute', top:8, left:8 }} color="geekblue">{item.views} 👁</Tag>
                  {item.listingType === 'PREMIUM' && <Tag style={{ position:'absolute', top:8, right:8 }} color="gold">PREMIUM</Tag>}
                </div>}
                style={{ display:'flex', flexDirection:'column' }}
                styles={{ body: { display:'flex', flexDirection:'column', padding:14 } }}
                actions={[<Button size="small" type="link"><Link to={`/listings/${item.id}`}>Chi tiết</Link></Button>]}
              >
                <Space direction="vertical" size={4} style={{ width:'100%' }}>
                  <Typography.Text strong style={{ fontSize: 14, lineHeight: 1.3 }} ellipsis={{ tooltip: item.title }}>{item.title}</Typography.Text>
                  <Space size={[4,4]} wrap>
                    <Tag bordered={false}>#{item.id}</Tag>
                    {item.favorited && <Tag color="red">Yêu thích</Tag>}
                  </Space>
                  <Typography.Text style={{ fontWeight:600, color:'#1677ff' }}>
                    {typeof item.price === 'number' ? item.price.toLocaleString('vi-VN') + ' VND' : 'Liên hệ'}
                  </Typography.Text>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      )}
      {pagination && (
        <div style={{ display:'flex', justifyContent:'center', marginTop: 24 }}>
          <Pagination
            disabled={loading}
            current={(pagination.currentPage ?? pagination.page ?? page) + 1}
            total={pagination.totalElements || 0}
            pageSize={pagination.size || size}
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
