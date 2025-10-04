// src/pages/listings/ListingPage.jsx
import { useEffect, useState, useCallback, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Layout,
  Card,
  Form,
  Select,
  InputNumber,
  Slider,
  Tag,
  Button,
  Pagination,
  Empty,
  Skeleton,
  Space,
  Typography,
  message
} from 'antd';
import { FilterOutlined, ThunderboltOutlined, ReloadOutlined } from '@ant-design/icons';
import api from '../../api/axios';
import { mapListingArray } from '../../utils/listingMapper';

const { Sider, Content } = Layout;
const { Title, Text } = Typography;

function ListingPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const params = new URLSearchParams(location.search);
  const initialCategoryParam = params.get("category");

  // Internal canonical categories: 'cars' | 'pin'
  const deriveInitial = () => {
    if (["pin"].includes(initialCategoryParam)) return "pin";
    return "cars"; // default
  };

  const [category, setCategory] = useState(deriveInitial());
  // vehicleType: CAR | BIKE | MOTORBIKE (backend enum) when category === 'cars'
  const [vehicleType, setVehicleType] = useState(null);
  // Mặc định tăng max price rất cao để không vô tình lọc mất các xe ô tô đắt tiền (trước đó 100,000,000 làm CAR biến mất)
  const [priceRange, setPriceRange] = useState([0, 2000000000]); // 0 -> 2,000,000,000 (client-side filter)
  const [year, setYear] = useState(""); // client-side only
  const debounceRef = useRef(null);
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);
  const [pagination, setPagination] = useState(null);

  const fetchListings = useCallback(async () => {
    setLoading(true);
    setError(null);
    const controller = new AbortController();
    const runStartedAt = Date.now();
    // attach to ref if you want cancellation across re-renders (simplified local)
    try {
      // Decide endpoint: if battery -> batteryCart, if vehicleType filter -> advanced-search, else evCart
      let endpoint = '';
      if (category === 'pin') {
        endpoint = `/listings/batteryCart?page=${page}&size=${size}`;
      } else if (vehicleType) {
        endpoint = `/listings/advanced-search?vehicleType=${vehicleType}&page=${page}&size=${size}`;
      } else {
        endpoint = `/listings/evCart?page=${page}&size=${size}`;
      }
      const res = await api.get(endpoint, { signal: controller.signal });
      const payload = res.data || {};
      const raw = Array.isArray(payload.data) ? payload.data : [];
      setPagination(payload.pagination || null);
      let mapped = mapListingArray(raw);
      if (mapped.length && import.meta.env.DEV) console.debug('[ListingPage] sample item', mapped[0]);

      // Client-side light filters (only affect current page data)
      if (year) {
        mapped = mapped.filter(l => String(l.year || '') === String(year));
      }
      if (priceRange && Array.isArray(priceRange)) {
        mapped = mapped.filter(l => typeof l.price === 'number' && l.price >= priceRange[0] && l.price <= priceRange[1]);
      }

      setListings(mapped);
    } catch (err) {
      if (err.name === 'CanceledError' || err.name === 'AbortError') {
        if (import.meta.env.DEV) console.debug('[ListingPage] aborted fetch (stale request)', runStartedAt);
      } else {
        console.error('Fetch listings error', err);
        setError('Không thể tải danh sách lúc này.');
        setListings([]);
        setPagination(null);
      }
    } finally {
      setLoading(false);
    }
  }, [category, vehicleType, page, size, year, priceRange]);

  // Fetch when dependencies change
  useEffect(() => { fetchListings(); }, [fetchListings]);

  // Sync URL query -> state (category + vehicleType) whenever location.search changes
  useEffect(() => {
    const p = new URLSearchParams(location.search);
    const cat = p.get('category');
    const type = p.get('type'); // for batteries (currently unused) or future filters

    // Map Vietnamese slugs to backend vehicle types
    const slugMap = {
      'xe-may-dien': 'MOTORBIKE',
      'xe-dap-dien': 'BIKE',
      'o-to-dien': 'CAR'
    };

    if (cat) {
      if (cat === 'pin') {
        if (category !== 'pin') setCategory('pin');
        if (vehicleType !== null) setVehicleType(null); // clear vehicle filter
      } else if (slugMap[cat]) {
        if (category !== 'cars') setCategory('cars');
        const vt = slugMap[cat];
        if (vehicleType !== vt) setVehicleType(vt);
      } else if (['cars','ev'].includes(cat)) {
        if (category !== 'cars') setCategory('cars');
        if (vehicleType !== null) setVehicleType(null);
      }
    }

    // For future: battery subtype (type param) could be stored if needed
    // if (cat === 'pin' && type) { ... }
  }, [location.search]);

  // Reset page when high-level category or vehicleType changes
  useEffect(() => { setPage(0); }, [category, vehicleType]);

  const nextPage = () => { if (pagination?.hasNext) setPage(p => p + 1); };
  const prevPage = () => { if (pagination?.hasPrevious) setPage(p => Math.max(0, p - 1)); };

  return (
    <Layout style={{ minHeight: 'calc(100vh - 140px)' }}>
      <Sider width={300} breakpoint="lg" collapsedWidth={0} theme="light" style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
        <div style={{ padding: '16px' }}>
          <Space align="center" style={{ marginBottom: 8 }}>
            <FilterOutlined />
            <Title level={5} style={{ margin: 0 }}>Bộ lọc</Title>
          </Space>
          <Form layout="vertical" size="middle">
            <Form.Item label="Danh mục">
              <Select
                value={category}
                onChange={(v) => { setCategory(v); setVehicleType(null); }}
                options={[
                  { value: 'cars', label: 'Xe điện' },
                  { value: 'pin', label: 'Pin' }
                ]}
              />
            </Form.Item>
            {category === 'cars' && (
              <Form.Item label="Loại xe">
                <Select
                  allowClear
                  placeholder="Tất cả"
                  value={vehicleType || undefined}
                  onChange={(v) => setVehicleType(v || null)}
                  options={[
                    { value: 'CAR', label: 'Ô tô điện' },
                    { value: 'MOTORBIKE', label: 'Xe máy điện' },
                    { value: 'BIKE', label: 'Xe đạp điện' }
                  ]}
                />
              </Form.Item>
            )}
            <Form.Item label={<span>Năm sản xuất <Text type="secondary" style={{ fontSize: 12 }}>(client)</Text></span>}>
              <InputNumber
                placeholder="VD: 2022"
                value={year ? +year : undefined}
                style={{ width: '100%' }}
                controls={false}
                onChange={(val) => {
                  if (debounceRef.current) clearTimeout(debounceRef.current);
                  debounceRef.current = setTimeout(()=> setYear(val ? String(val) : ''), 400);
                }}
              />
            </Form.Item>
            <Form.Item label={<span>Khoảng giá (VND) <Text type="secondary" style={{ fontSize: 12 }}>(lọc cục bộ trên trang)</Text></span>}>
              <Slider
                range
                min={0}
                max={2_000_000_000}
                step={10_000_000}
                value={priceRange}
                tooltip={{ formatter: (v) => v?.toLocaleString('vi-VN') }}
                onChange={(vals) => {
                  setPriceRange(vals);
                }}
              />
              <Space style={{ width: '100%', justifyContent: 'space-between' }} size={8}>
                <InputNumber
                  value={priceRange[0]}
                  min={0}
                  max={priceRange[1]}
                  step={10_000_000}
                  onChange={(v)=> setPriceRange([v || 0, priceRange[1]])}
                />
                <InputNumber
                  value={priceRange[1]}
                  min={priceRange[0]}
                  max={2_000_000_000}
                  step={10_000_000}
                  onChange={(v)=> setPriceRange([priceRange[0], v || priceRange[1]])}
                />
              </Space>
            </Form.Item>
            <Form.Item label={<span>Kích thước trang</span>}>
              <Select
                size="small"
                value={size}
                onChange={(v)=> { setSize(v); setPage(0); }}
                options={[6,12,24,36].map(v=>({ value:v, label:v }))}
              />
            </Form.Item>
            <Space.Compact block>
              <Button icon={<ReloadOutlined />} loading={loading} onClick={()=> fetchListings()} type="primary" block>
                Làm mới
              </Button>
            </Space.Compact>
            <Text type="secondary" style={{ fontSize: 11, display: 'block', marginTop: 12 }}>* Bộ lọc giá & năm hiện tại chỉ áp dụng trên dữ liệu trang đang xem.</Text>
          </Form>
        </div>
      </Sider>
      <Content style={{ padding: '16px 24px' }}>
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
            <Title level={4} style={{ margin: 0 }}>
              {category === 'pin' ? 'Danh sách Pin' : 'Danh sách Xe'}{' '}
              {vehicleType && category === 'cars' && (
                <Tag color="blue" style={{ marginLeft: 4 }}>
                  {vehicleType === 'CAR' ? 'Ô tô' : vehicleType === 'MOTORBIKE' ? 'Xe máy' : 'Xe đạp'}
                </Tag>
              )}
            </Title>
            {pagination && <Text type="secondary">Tổng: {pagination.totalElements}</Text>}
          </div>
          {error && (
            <Card size="small" style={{ borderColor: '#ff4d4f' }}>
              <Text type="danger">{error}</Text>
            </Card>
          )}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill,minmax(250px,1fr))',
              gap: 20
            }}
          >
            {loading && Array.from({ length: Math.min(size, 6) }).map((_, i) => (
              <Card key={i} hoverable>
                <Skeleton.Image active style={{ width: '100%', height: 140, objectFit: 'cover' }} />
                <Skeleton active title paragraph={{ rows: 2 }} style={{ marginTop: 12 }} />
              </Card>
            ))}
            {!loading && listings.length === 0 && (
              <div style={{ gridColumn: '1/-1' }}>
                <Empty description="Không có dữ liệu" />
              </div>
            )}
            {!loading && listings.map(item => (
              <Card
                key={item.id}
                hoverable
                cover={<div style={{ position: 'relative', width: '100%', paddingTop: '62%', overflow: 'hidden', background: '#f5f5f5' }}>
                  <img
                    src={item.thumbnail || 'https://via.placeholder.com/400'}
                    alt={item.title}
                    style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' }}
                    loading="lazy"
                  />
                  <Tag style={{ position: 'absolute', top: 8, left: 8 }} color="geekblue">{item.views} 👁</Tag>
                  {item.listingType === 'PREMIUM' && (
                    <Tag style={{ position: 'absolute', top: 8, right: 8 }} color="gold" icon={<ThunderboltOutlined />}>PREMIUM</Tag>
                  )}
                </div>}
                style={{ display: 'flex', flexDirection: 'column' }}
                styles={{ body: { display: 'flex', flexDirection: 'column', padding: 16 } }}
                actions={[
                  <Button size="small" type="link" onClick={()=> navigate(`/listings/${item.id}`)}>Chi tiết</Button>
                ]}
              >
                <Space direction="vertical" size={6} style={{ width: '100%' }}>
                  <Title level={5} style={{ margin: 0, fontSize: 15, lineHeight: 1.3 }}>{item.title}</Title>
                  <Space size={[4,4]} wrap>
                    <Tag bordered={false}>ID #{item.id}</Tag>
                    {item.favorited && <Tag color="red">Yêu thích</Tag>}
                  </Space>
                  <Text strong style={{ fontSize: 16, color: '#1677ff' }}>
                    {typeof item.price === 'number' ? item.price.toLocaleString('vi-VN') + ' VND' : 'Liên hệ'}
                  </Text>
                </Space>
              </Card>
            ))}
          </div>
          <div style={{ display: 'flex', justifyContent: 'center', marginTop: 8 }}>
            <Pagination
              current={(pagination?.page ?? 0) + 1}
              pageSize={pagination?.size || size}
              total={pagination?.totalElements || 0}
              showSizeChanger
              pageSizeOptions={[6,12,24,36]}
              onShowSizeChange={(_, ps)=> { setSize(ps); setPage(0); }}
              onChange={(p)=> {
                // Ant Design pages are 1-based, backend is 0-based
                const zero = p - 1;
                if (zero !== page) setPage(zero);
              }}
            />
          </div>
        </Space>
      </Content>
    </Layout>
  );
}

export default ListingPage;
