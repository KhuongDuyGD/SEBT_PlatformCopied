import React, { useEffect, useState, useCallback, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Layout, Form, Input, Select, InputNumber, Button, Card, Tag, Space, Typography, Row, Col, Empty, Pagination, Skeleton, Divider, Tooltip } from 'antd';
import { SearchOutlined, ReloadOutlined, FilterOutlined, ClearOutlined } from '@ant-design/icons';
import listingsApi from '../../api/listings';
import { mapListingArray } from '../../utils/listingMapper';

// Ant Design aliases
const { Title, Text } = Typography;
const VEHICLE_TYPE_OPTIONS = [
  { value: 'CAR', label: 'Ô tô' },
  { value: 'MOTORBIKE', label: 'Xe máy điện' },
  { value: 'BIKE', label: 'Xe đạp điện' }
];

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
  title: undefined,
  brand: undefined,
  vehicleType: undefined,
  year: undefined,
  minPrice: undefined,
  maxPrice: undefined
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
      if (val !== null && val !== '') f[key] = val;
    });
    setFilters(f);
    form.setFieldsValue(f);
    // If there are any URL filters, auto trigger search
    const any = Object.values(f).some(v => v !== undefined && v !== '');
    if (any) {
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
      const res = await listingsApi.advancedSearchListings({ ...paramsObj, page: pageArg, size: sizeArg, signal: controller.signal });
      if (res?.success) {
        const arr = Array.isArray(res.data) ? res.data : [];
        const mapped = mapListingArray(arr);
        setListings(mapped);
        setPagination(res.pagination || null);
      } else {
        setListings([]); setPagination(null);
      }
    } catch (e) {
      if (e.name === 'CanceledError' || e.name === 'AbortError') return; // silent cancel
      console.error('Advanced search error', e);
      setError('Không thể tìm kiếm lúc này.');
      setListings([]); setPagination(null);
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
            <Form.Item label="Tiêu đề" name="title">
              <Input allowClear placeholder="VD: VinFast..." prefix={<SearchOutlined style={{ color: '#999' }} />} />
            </Form.Item>
            <Form.Item label="Hãng" name="brand">
              <Input allowClear placeholder="VinFast" />
            </Form.Item>
            <Form.Item label="Loại xe" name="vehicleType">
              <Select allowClear placeholder="Tất cả" options={VEHICLE_TYPE_OPTIONS} />
            </Form.Item>
            {/* Updated: Changed from InputNumber to Select dropdown for better UX */}
            <Form.Item label="Năm sản xuất" name="year">
              <Select
                allowClear
                placeholder="Chọn năm sản xuất"
                style={{ width: '100%' }}
                showSearch
                filterOption={(input, option) =>
                  (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                options={(() => {
                  // Generate year options from current year back to 2010
                  // Most electric vehicles are from recent years
                  const currentYear = new Date().getFullYear();
                  const startYear = 2010; // Electric vehicles became more common around this time
                  const years = [];

                  // Add years in descending order (newest first)
                  for (let year = currentYear; year >= startYear; year--) {
                    years.push({
                      value: year,
                      label: year.toString()
                    });
                  }

                  return years;
                })()}
              />
            </Form.Item>
            <Row gutter={8}>
              <Col span={12}>
                <Form.Item label="Giá từ" name="minPrice" tooltip="VND" rules={[{ validator: (_, v) => (v === undefined || v >= 0) ? Promise.resolve() : Promise.reject('>=0') }]}>
                  <InputNumber style={{ width: '100%' }} min={0} step={1000000} placeholder="0" />
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
                  <InputNumber style={{ width: '100%' }} min={0} step={1000000} placeholder="..." />
                </Form.Item>
              </Col>
            </Row>
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
            <Title level={4} style={{ margin: 0 }}>Kết quả nâng cao</Title>
            <Space size={12} wrap>
              <Link to="/post-listing"><Button type="dashed" size="small">+ Đăng mới</Button></Link>
              {pagination && <Text type="secondary">Tổng: {pagination.totalElements}</Text>}
            </Space>
          </Space>
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
                        <Text strong style={{ lineHeight: 1.25 }}>{highlight(item.title, filters.title)}</Text>
                        <Space size={[4, 4]} wrap>
                          {item.raw?.brand && <Tag bordered={false}>{item.raw.brand}</Tag>}
                          {item.raw?.year && <Tag bordered={false}>{item.raw.year}</Tag>}
                          {item.listingType === 'PREMIUM' && <Tag color="gold">VIP</Tag>}
                        </Space>
                        <Text strong style={{ color: '#1677ff' }}>{typeof item.price === 'number' ? item.price.toLocaleString('vi-VN') + ' VND' : 'Liên hệ'}</Text>
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
