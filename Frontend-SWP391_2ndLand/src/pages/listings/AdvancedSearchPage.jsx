import React, { useEffect, useState, useCallback, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Layout, Form, Input, Select, InputNumber, Button, Card, Tag, Space, Typography, Row, Col, Empty, Pagination, Skeleton, Divider, Tooltip, Radio, Alert } from 'antd';
import { SearchOutlined, ReloadOutlined, FilterOutlined, ClearOutlined, CarOutlined, ThunderboltOutlined } from '@ant-design/icons';
import listingsApi from '../../api/listings';
import { mapListingArray } from '../../utils/listingMapper';
import { 
  VEHICLE_TYPES, 
  VEHICLE_CONDITIONS, 
  BATTERY_CONDITIONS,
  PROVINCES 
} from '../../constants/filterOptions';

// Ant Design aliases
const { Title, Text } = Typography;

// Constants cho search types
const SEARCH_TYPES = {
  EV: 'ev',
  BATTERY: 'battery'
};

// Utility: build query params object -> string
const sanitizeParams = (params) => {
  const cleaned = {};
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '' && !(typeof v === 'number' && isNaN(v))) {
      cleaned[k] = v;
    }
  });
  return cleaned;
};

const initialFilters = {
  // Common filters
  searchType: SEARCH_TYPES.EV,
  keyword: undefined,
  brand: undefined,
  minPrice: undefined,
  maxPrice: undefined,
  minYear: undefined,
  maxYear: undefined,
  province: undefined,
  district: undefined,
  conditionStatus: undefined,
  
  // EV specific
  vehicleType: undefined,
  
  // Battery specific
  minCapacity: undefined,
  maxCapacity: undefined,
  minHealthPercentage: undefined,
  maxHealthPercentage: undefined
};

export default function AdvancedSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [form] = Form.useForm();
  const [filters, setFilters] = useState(initialFilters);
  const [page, setPage] = useState(Number(searchParams.get('page')) || 0);
  const [size, setSize] = useState(12);
  const [listings, setListings] = useState([]); // mapped UI listings
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [applied, setApplied] = useState({});
  const abortRef = useRef(null);

  // Populate from URL on mount
  useEffect(() => {
    const f = { ...initialFilters };
    Object.keys(initialFilters).forEach(key => {
      const val = searchParams.get(key);
      if (val !== null && val !== '') {
        // Handle special cases for numeric fields
        if (['minPrice', 'maxPrice', 'minYear', 'maxYear', 'minCapacity', 'maxCapacity', 'minHealthPercentage', 'maxHealthPercentage'].includes(key)) {
          const numVal = Number(val);
          if (!isNaN(numVal)) f[key] = numVal;
        } else {
          f[key] = val;
        }
      }
    });
    setFilters(f);
    form.setFieldsValue(f);
    // If there are any URL filters (excluding defaults), auto trigger search
    const filtered = { ...f };
    delete filtered.searchType; // Don't count searchType as a filter since it has default value
    const hasFilters = Object.values(filtered).some(v => v !== undefined && v !== '');
    if (hasFilters) {
      const cleaned = sanitizeParams(f);
      setApplied(cleaned);
      fetchResults(cleaned, page, size, true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchResults = useCallback(async (paramsObj, pageArg = page, sizeArg = size, silent = false) => {
    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    try {
      if (!silent) setLoading(true);
      setError(null);
      
      const searchParams = { 
        ...paramsObj, 
        page: pageArg, 
        size: sizeArg 
      };
      
      // Remove searchType from API params
      const { searchType, ...apiParams } = searchParams;
      
      let res;
      if (searchType === SEARCH_TYPES.BATTERY) {
        console.log('[ADVANCED_SEARCH] Using battery filter API:', apiParams);
        res = await listingsApi.batteryFilterListings(apiParams);
      } else {
        console.log('[ADVANCED_SEARCH] Using EV filter API:', apiParams);
        res = await listingsApi.evFilterListings(apiParams);
      }
      
      if (res && Array.isArray(res.content)) {
        const mapped = mapListingArray(res.content);
        setListings(mapped);
        setPagination(res);
      } else {
        console.warn('API response format không đúng:', res);
        setListings([]); 
        setPagination(null);
      }
    } catch (e) {
      if (e.name === 'CanceledError' || e.name === 'AbortError') return; // silent cancel
      console.error('Advanced search error', e);
      setError('Không thể tìm kiếm lúc này. Vui lòng thử lại sau.');
      setListings([]); 
      setPagination(null);
    } finally {
      if (!silent) setLoading(false);
    }
  }, [page, size]);

  const handleSubmit = async (vals) => {
    const cleaned = sanitizeParams(vals);
    setFilters(vals);
    setApplied(cleaned);
    setPage(0);
    setSearchParams({ ...cleaned, page: '0' });
    fetchResults(cleaned, 0, size);
  };

  // Refetch when page changes (and applied filters exist)
  useEffect(() => {
    if (Object.keys(applied).length > 0) {
      fetchResults(applied, page, size);
      setSearchParams({ ...applied, page: String(page) });
    }
  }, [page, size]); // eslint-disable-line react-hooks/exhaustive-deps

  const resetFilters = () => {
    form.resetFields();
    setFilters(initialFilters);
    setApplied({});
    setPage(0);
    setListings([]);
    setPagination(null);
    setSearchParams({});
  };

  // Pagination helpers - removed unused functions

  // Highlight helper
  const highlight = (text, keyword) => {
    if (!keyword || !text) return text;
    try {
      const rx = new RegExp(`(${keyword.replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')})`, 'ig');
      return text.split(rx).map((part, i) =>
        rx.test(part) ? <mark key={i} style={{ padding: 0, background: '#ffe58f' }}>{part}</mark> : <React.Fragment key={i}>{part}</React.Fragment>
      );
    } catch { return text; }
  };

  return (
    <Layout style={{ minHeight: 'calc(100vh - 140px)' }}>
      <Layout.Sider width={300} breakpoint="lg" collapsedWidth={0} theme="light" style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
        <div style={{ padding: 16 }}>
          <Space align="center" style={{ marginBottom: 8 }}>
            <FilterOutlined />
            <Title level={5} style={{ margin: 0 }}>Tìm kiếm nâng cao</Title>
          </Space>
          <Form
            form={form}
            layout="vertical"
            size="middle"
            initialValues={filters}
            onFinish={handleSubmit}
            onValuesChange={(_, all) => setFilters(all)}
          >
            {/* Search Type Selector */}
            <Form.Item label="Loại tìm kiếm" name="searchType">
              <Radio.Group buttonStyle="solid">
                <Radio.Button value={SEARCH_TYPES.EV}>
                  <Space>
                    <CarOutlined />
                    Xe điện
                  </Space>
                </Radio.Button>
                <Radio.Button value={SEARCH_TYPES.BATTERY}>
                  <Space>
                    <ThunderboltOutlined />
                    Pin điện
                  </Space>
                </Radio.Button>
              </Radio.Group>
            </Form.Item>

            <Divider style={{ margin: '12px 0' }} />

            {/* Common Fields */}
            <Form.Item label="Từ khóa" name="keyword">
              <Input allowClear placeholder="VD: VinFast..." prefix={<SearchOutlined style={{ color: '#999' }} />} />
            </Form.Item>
            <Form.Item label="Hãng" name="brand">
              <Input allowClear placeholder="VD: VinFast, Tesla..." />
            </Form.Item>
            
            {/* Price Range */}
            <Row gutter={8}>
              <Col span={12}>
                <Form.Item label="Giá từ" name="minPrice" tooltip="VND" rules={[{ validator: (_, v) => (v === undefined || v >= 0) ? Promise.resolve() : Promise.reject('>=0') }]}>
                  <InputNumber style={{ width: '100%' }} min={0} step={1000000} placeholder="0" formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="Giá đến" name="maxPrice" tooltip="VND" dependencies={['minPrice']} rules={[({ getFieldValue }) => ({
                  validator(_, value) {
                    const min = getFieldValue('minPrice');
                    if (value === undefined || min === undefined || value >= min) return Promise.resolve();
                    return Promise.reject(new Error('>= Giá từ'));
                  }
                })]}>
                  <InputNumber style={{ width: '100%' }} min={0} step={1000000} placeholder="..." formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
                </Form.Item>
              </Col>
            </Row>

            {/* Year Range */}
            <Row gutter={8}>
              <Col span={12}>
                <Form.Item label="Năm từ" name="minYear">
                  <InputNumber style={{ width: '100%' }} min={2000} max={new Date().getFullYear()} placeholder="2020" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="Năm đến" name="maxYear" dependencies={['minYear']} rules={[({ getFieldValue }) => ({
                  validator(_, value) {
                    const min = getFieldValue('minYear');
                    if (value === undefined || min === undefined || value >= min) return Promise.resolve();
                    return Promise.reject(new Error('>= Năm từ'));
                  }
                })]}>
                  <InputNumber style={{ width: '100%' }} min={2000} max={new Date().getFullYear()} placeholder="2024" />
                </Form.Item>
              </Col>
            </Row>

            {/* Location */}
            <Form.Item label="Tỉnh/Thành phố" name="province">
              <Select 
                allowClear 
                placeholder="Chọn tỉnh/thành phố" 
                options={PROVINCES.map(p => ({ value: p, label: p }))}
                showSearch
                filterOption={(input, option) =>
                  option.label.toLowerCase().includes(input.toLowerCase())
                }
              />
            </Form.Item>
            <Form.Item label="Quận/Huyện" name="district">
              <Input allowClear placeholder="VD: Quận 1, Huyện Củ Chi..." />
            </Form.Item>

            {/* Condition */}
            <Form.Item label="Tình trạng" name="conditionStatus">
              <Select 
                allowClear 
                placeholder="Chọn tình trạng"
                options={
                  filters.searchType === SEARCH_TYPES.BATTERY 
                    ? BATTERY_CONDITIONS.map(c => ({ value: c.value, label: c.label }))
                    : VEHICLE_CONDITIONS.map(c => ({ value: c.value, label: c.label }))
                }
              />
            </Form.Item>

            {/* Conditional Fields based on Search Type */}
            <Form.Item noStyle shouldUpdate={(prev, curr) => prev.searchType !== curr.searchType}>
              {({ getFieldValue }) => {
                const searchType = getFieldValue('searchType');
                
                if (searchType === SEARCH_TYPES.EV) {
                  return (
                    <>
                      <Form.Item label="Loại xe" name="vehicleType">
                        <Select 
                          allowClear 
                          placeholder="Chọn loại xe"
                          options={VEHICLE_TYPES.map(v => ({ value: v.value, label: v.label }))}
                        />
                      </Form.Item>
                    </>
                  );
                } else if (searchType === SEARCH_TYPES.BATTERY) {
                  return (
                    <>
                      {/* Battery Capacity Range */}
                      <Row gutter={8}>
                        <Col span={12}>
                          <Form.Item label="Dung lượng từ (kWh)" name="minCapacity">
                            <InputNumber style={{ width: '100%' }} min={0} step={0.1} placeholder="10" />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item label="Dung lượng đến (kWh)" name="maxCapacity" dependencies={['minCapacity']} rules={[({ getFieldValue }) => ({
                            validator(_, value) {
                              const min = getFieldValue('minCapacity');
                              if (value === undefined || min === undefined || value >= min) return Promise.resolve();
                              return Promise.reject(new Error('>= Dung lượng từ'));
                            }
                          })]}>
                            <InputNumber style={{ width: '100%' }} min={0} step={0.1} placeholder="100" />
                          </Form.Item>
                        </Col>
                      </Row>

                      {/* Battery Health Range */}
                      <Row gutter={8}>
                        <Col span={12}>
                          <Form.Item label="Độ khỏe từ (%)" name="minHealthPercentage">
                            <InputNumber style={{ width: '100%' }} min={0} max={100} placeholder="80" />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item label="Độ khỏe đến (%)" name="maxHealthPercentage" dependencies={['minHealthPercentage']} rules={[({ getFieldValue }) => ({
                            validator(_, value) {
                              const min = getFieldValue('minHealthPercentage');
                              if (value === undefined || min === undefined || value >= min) return Promise.resolve();
                              return Promise.reject(new Error('>= Độ khỏe từ'));
                            }
                          })]}>
                            <InputNumber style={{ width: '100%' }} min={0} max={100} placeholder="100" />
                          </Form.Item>
                        </Col>
                      </Row>
                    </>
                  );
                }
                
                return null;
              }}
            </Form.Item>
            <Space.Compact style={{ width: '100%' }}>
              <Button type="primary" htmlType="submit" block loading={loading} icon={<SearchOutlined />}>Tìm kiếm</Button>
              <Tooltip title="Xóa tất cả">
                <Button icon={<ClearOutlined />} onClick={resetFilters} disabled={loading} />
              </Tooltip>
            </Space.Compact>
            {Object.keys(applied).length > 0 && (
              <div style={{ marginTop: 16 }}>
                <Text type="secondary" style={{ fontSize: 12 }}>Đang áp dụng:</Text>
                <div style={{ marginTop: 6, display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                  {Object.entries(applied).filter(([k]) => k !== 'page').map(([k, v]) => (
                    <Tag
                      key={k}
                      closable
                      onClose={(e) => {
                        e.preventDefault();
                        const next = { ...applied };
                        delete next[k];
                        setApplied(next);
                        form.setFieldValue(k, undefined);
                        setFilters(f => ({ ...f, [k]: undefined }));
                        setPage(0);
                        if (Object.keys(next).length) {
                          setSearchParams({ ...next, page: '0' });
                          fetchResults(next, 0, size);
                        } else {
                          resetFilters();
                        }
                      }}
                    >{k}: {String(v)}</Tag>
                  ))}
                </div>
              </div>
            )}
            <Divider style={{ margin: '16px 0' }} />
            <Button icon={<ReloadOutlined />} block onClick={() => fetchResults(applied || sanitizeParams(filters), page, size)} disabled={loading}>Làm mới</Button>
          </Form>
        </div>
      </Layout.Sider>
      <Layout.Content style={{ padding: '16px 24px' }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Space style={{ justifyContent: 'space-between', width: '100%', flexWrap: 'wrap' }}>
            <Title level={4} style={{ margin: 0 }}>
              {filters.searchType === SEARCH_TYPES.BATTERY ? (
                <Space><ThunderboltOutlined />Kết quả Pin điện</Space>
              ) : (
                <Space><CarOutlined />Kết quả Xe điện</Space>
              )}
            </Title>
            <Space size={12} wrap>
              <Link to="/post-listing"><Button type="dashed" size="small">+ Đăng mới</Button></Link>
              {pagination && <Text type="secondary">Tổng: {pagination.totalElements}</Text>}
            </Space>
          </Space>

          {/* Alert thông báo về tính năng mới */}
          <Alert
            message="Tính năng tìm kiếm nâng cao đã được cập nhật!"
            description="Giờ đây bạn có thể tìm kiếm riêng biệt cho xe điện và pin điện với nhiều bộ lọc chi tiết hơn."
            type="info"
            showIcon
            closable
            style={{ marginBottom: '16px' }}
          />
          {error && (
            <Card size="small" style={{ borderColor: '#ff4d4f' }}>
              <Text type="danger">{error}</Text>
            </Card>
          )}
          {loading && (
            <Row gutter={[16, 16]}>
              {Array.from({ length: 6 }).map((_, i) => (
                <Col xs={24} sm={12} md={12} lg={8} key={i}>
                  <Card hoverable>
                    <Skeleton.Image active style={{ width: '100%', height: 140 }} />
                    <Skeleton active title paragraph={{ rows: 2 }} style={{ marginTop: 12 }} />
                  </Card>
                </Col>
              ))}
            </Row>
          )}
          {!loading && !error && listings.length === 0 && Object.keys(applied).length > 0 && (
            <Card>
              <Empty description={<span>Không có kết quả phù hợp</span>}>
                <Button type="link" onClick={resetFilters}>Xóa bộ lọc</Button>
              </Empty>
            </Card>
          )}
          {!loading && !error && listings.length === 0 && Object.keys(applied).length === 0 && (
            <Card>
              <Empty description="Nhập điều kiện và nhấn Tìm kiếm" />
            </Card>
          )}
          {!loading && !error && listings.length > 0 && (
            <>
              <Row gutter={[16, 16]}>
                {listings.map(item => (
                  <Col xs={24} sm={12} md={12} lg={8} key={item.id}>
                    <Card
                      hoverable
                      cover={
                        <div style={{ position: 'relative', width: '100%', paddingTop: '62%', overflow: 'hidden', background: '#f5f5f5' }}>
                          <img src={item.thumbnail} alt={item.title} style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' }} loading="lazy" />
                          {item.listingType === 'PREMIUM' && (
                            <Tag color="gold" style={{ position: 'absolute', top: 8, right: 8 }}>PREMIUM</Tag>
                          )}
                        </div>
                      }
                      actions={[<Link to={`/listings/${item.id}`} key="detail">Chi tiết</Link>]}
                      styles={{ body: { padding: 14 } }}
                    >
                      <Space direction="vertical" size={4} style={{ width: '100%' }}>
                        <Text strong style={{ lineHeight: 1.25 }}>{highlight(item.title, filters.keyword)}</Text>
                        <Space size={[4, 4]} wrap>
                          {item.brand && <Tag bordered={false}>{item.brand}</Tag>}
                          {item.year && <Tag bordered={false}>{item.year}</Tag>}
                          {item.conditionStatus && <Tag color="blue">{item.conditionStatus}</Tag>}
                          {filters.searchType === SEARCH_TYPES.BATTERY && item.batteryCapacity && (
                            <Tag color="green">{item.batteryCapacity} kWh</Tag>
                          )}
                          {filters.searchType === SEARCH_TYPES.BATTERY && item.healthPercentage && (
                            <Tag color="orange">{item.healthPercentage}% Khỏe</Tag>
                          )}
                          {filters.searchType === SEARCH_TYPES.EV && item.vehicleType && (
                            <Tag color="cyan">{VEHICLE_TYPES.find(v => v.value === item.vehicleType)?.label || item.vehicleType}</Tag>
                          )}
                          {item.listingType === 'PREMIUM' && <Tag color="gold">VIP</Tag>}
                        </Space>
                        <Text strong style={{ color: '#1677ff' }}>
                          {typeof item.price === 'number' ? item.price.toLocaleString('vi-VN') + ' VND' : 'Liên hệ'}
                        </Text>
                        {/* Location info */}
                        {(item.province || item.district) && (
                          <Text type="secondary" style={{ fontSize: '12px' }}>
                            📍 {[item.district, item.province].filter(Boolean).join(', ')}
                          </Text>
                        )}
                        {/* View count */}
                        <Text type="secondary" style={{ fontSize: '12px' }}>
                          👁 {item.viewsCount || 0} lượt xem
                        </Text>
                      </Space>
                    </Card>
                  </Col>
                ))}
              </Row>
              {pagination && (
                <div style={{ display: 'flex', justifyContent: 'center', marginTop: 12 }}>
                  <Pagination
                    current={(pagination.page ?? pagination.currentPage ?? 0) + 1}
                    total={pagination.totalElements || 0}
                    pageSize={pagination.size || size}
                    showSizeChanger
                    pageSizeOptions={[6, 12, 24, 36]}
                    onShowSizeChange={(_, ps) => { setSize(ps); setPage(0); }}
                    onChange={(p) => { const zero = p - 1; if (zero !== page) setPage(zero); }}
                  />
                </div>
              )}
            </>
          )}
        </Space>
      </Layout.Content>
    </Layout>
  );
}
