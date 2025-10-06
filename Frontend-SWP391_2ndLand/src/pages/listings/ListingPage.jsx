// src/pages/listings/ListingPage.jsx
import { useEffect, useState, useCallback, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Layout,
  Card,
  Form,
  Select,
  InputNumber,
  AutoComplete,
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
  
  // Vehicle filters (cars) - for xe điện listings
  const [vehicleType, setVehicleType] = useState(null); // CAR | BIKE | MOTORBIKE (backend enum)
  const [year, setYear] = useState(""); // server-side filter cho cars
  const [carBrand, setCarBrand] = useState(""); // client-side brand filter cho xe điện
  const [carCondition, setCarCondition] = useState(""); // client-side condition filter cho xe điện
  const [carBatteryCapacity, setCarBatteryCapacity] = useState([0, 200]); // client-side battery capacity range cho xe điện

  // Battery filters (pin) - for pin listings
  const [batteryBrand, setBatteryBrand] = useState(""); // client-side brand filter cho pin
  const [batteryCondition, setBatteryCondition] = useState(""); // client-side condition filter cho pin
  const [batteryCapacity, setBatteryCapacity] = useState([0, 200]); // client-side capacity range cho pin

  // Common filters
  const [priceRange, setPriceRange] = useState([0, 2000000000]); // client-side price filter
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);
  const [pagination, setPagination] = useState(null);

  // Danh sách thương hiệu pin phổ biến cho AutoComplete - dựa trên data thực tế từ database
  const batteryBrandOptions = [
    'Tesla', 'CATL', 'BYD', 'SK Innovation', 'FinDreams', 
    'LG Energy Solution', 'Panasonic', 'Samsung SDI',
    'AESC', 'Gotion High-Tech', 'EVE Energy', 'Lishen'
  ];

  // Danh sách thương hiệu xe điện phổ biến cho AutoComplete - dựa trên database
  const carBrandOptions = [
    'VinFast', 'Tesla', 'BYD', 'Xpeng', 'NIO', 'Li Auto',
    'BMW', 'Mercedes-Benz', 'Audi', 'Hyundai', 'Kia', 'Ford'
  ];

  // Options cho condition status của pin - dựa trên BatteryCondition enum
  const batteryConditionOptions = [
    { value: 'EXCELLENT', label: 'Tuyệt vời' },
    { value: 'GOOD', label: 'Tốt' },
    { value: 'FAIR', label: 'Khá' },
    { value: 'POOR', label: 'Kém' },
    { value: 'NEEDS_REPLACEMENT', label: 'Cần thay thế' }
  ];

  // Options cho condition status của xe điện - dựa trên VehicleCondition enum
  const carConditionOptions = [
    { value: 'EXCELLENT', label: 'Tuyệt vời' },
    { value: 'GOOD', label: 'Tốt' },
    { value: 'FAIR', label: 'Khá' },
    { value: 'POOR', label: 'Kém' },
    { value: 'NEEDS_MAINTENANCE', label: 'Cần bảo trì' }
  ];

  const fetchListings = useCallback(async () => {
    setLoading(true);
    setError(null);
    const controller = new AbortController();
    const runStartedAt = Date.now();
    let usedEndpoint = ''; // For error logging
    // attach to ref if you want cancellation across re-renders (simplified local)
    try {
      // Decide endpoint based on category and filters
      let endpoint = '';
      
      if (category === 'pin') {
        // Pin category - backend không hỗ trợ brand filter, chỉ hỗ trợ year qua battery-filter
        // Brand filter sẽ được xử lý client-side
        if (year) {
          // Pin category with year filter - use battery-filter endpoint
          const params = new URLSearchParams();
          params.append('page', page);
          params.append('size', size);
          params.append('year', year);
          endpoint = `/listings/battery-filter?${params.toString()}`;
          usedEndpoint = endpoint;
        } else {
          // Pin category without server-side filters - use simple batteryCart endpoint
          endpoint = `/listings/batteryCart?page=${page}&size=${size}`;
          usedEndpoint = endpoint;
        }
      } else {
        // Vehicle category (cars)
        if (vehicleType || year) {
          // Vehicle category with filters - use ev-filter endpoint  
          const params = new URLSearchParams();
          params.append('page', page);
          params.append('size', size);
          if (vehicleType) params.append('vehicleType', vehicleType);
          if (year) params.append('year', year);
          endpoint = `/listings/ev-filter?${params.toString()}`;
          usedEndpoint = endpoint;
        } else {
          // Vehicle category without filters - use simple evCart endpoint  
          endpoint = `/listings/evCart?page=${page}&size=${size}`;
          usedEndpoint = endpoint;
        }
      }
      const res = await api.get(endpoint, { signal: controller.signal });
      const payload = res.data || {};
      const raw = Array.isArray(payload.data) ? payload.data : [];
      setPagination(payload.pagination || null);
      
      let mapped = mapListingArray(raw);
      if (mapped.length && import.meta.env.DEV) console.debug('[ListingPage] sample item', mapped[0]);

      // Client-side filters (áp dụng trên dữ liệu đã load từ server)
      
      if (priceRange && Array.isArray(priceRange)) {
        // Price range filtering cho cả cars và pin
        mapped = mapped.filter(l => typeof l.price === 'number' && l.price >= priceRange[0] && l.price <= priceRange[1]);
      }

      // Client-side filters cho pin (backend không hỗ trợ các filter này)
      if (category === 'pin') {
        // Brand filter cho pin
        if (batteryBrand) {
          const originalCount = mapped.length;
          mapped = mapped.filter(l => {
            // Backend ListingCartResponseDTO không có brand field trong current version
            // Tìm brand trong raw data hoặc extract từ title
            let brandValue = l.raw?.product?.battery?.brand || l.raw?.brand || '';
            
            // Nếu không có brand field, extract từ title
            // Title pattern: "Pin [Brand] [Capacity]kWh - [Health]% Health"
            // Ví dụ: "Pin Tesla 75.00kWh - 95% Health", "Pin CATL 80.00kWh - 92% Health"
            if (!brandValue && l.title) {
              const titleMatch = l.title.match(/Pin\s+([A-Za-z\s&]+?)(?:\s+\d|$)/i);
              if (titleMatch && titleMatch[1]) {
                brandValue = titleMatch[1].trim();
                // Loại bỏ các từ thừa phổ biến
                brandValue = brandValue.replace(/\s+(Innovation|Energy|Solution|Systems)$/i, '');
              }
            }
            
            const matches = brandValue && brandValue.toLowerCase().includes(batteryBrand.toLowerCase());
            
            // Debug logging trong development
            if (import.meta.env.DEV) {
              console.debug(`[PIN BRAND FILTER] Title: "${l.title}" -> Brand: "${brandValue}" -> Filter: "${batteryBrand}" -> Match: ${matches}`);
            }
            
            return matches;
          });
          
          if (import.meta.env.DEV) {
            console.debug(`[PIN BRAND FILTER] Filtered ${originalCount} -> ${mapped.length} items for brand: "${batteryBrand}"`);
          }
        }

        // Condition filter cho pin - dựa trên title parsing vì backend không expose condition trong ListingCartResponseDTO
        if (batteryCondition) {
          mapped = mapped.filter(l => {
            // Parse condition từ title patterns
            // Title patterns có thể chứa: "Pin Tesla ... - 95% Health", "Pin BYD ... - Tình trạng tốt"
            let hasCondition = false;
            
            if (l.title) {
              const title = l.title.toLowerCase();
              
              // Map UI condition values với title patterns
              switch (batteryCondition) {
                case 'EXCELLENT':
                  hasCondition = title.includes('như mới') || title.includes('tuyệt vời') || title.includes('excellent');
                  break;
                case 'GOOD':
                  hasCondition = title.includes('tốt') || title.includes('good') || (!title.includes('kém') && !title.includes('cần'));
                  break;
                case 'FAIR':
                  hasCondition = title.includes('khá') || title.includes('fair') || title.includes('bình thường');
                  break;
                case 'POOR':
                  hasCondition = title.includes('kém') || title.includes('poor') || title.includes('xấu');
                  break;
                case 'NEEDS_REPLACEMENT':
                  hasCondition = title.includes('cần thay') || title.includes('thay thế') || title.includes('replacement');
                  break;
                default:
                  hasCondition = true; // Fallback: show all if can't determine
              }
            }
            
            return hasCondition;
          });
        }

        // Capacity range filter cho pin - extract từ title vì backend không expose capacity details
        if (batteryCapacity && Array.isArray(batteryCapacity)) {
          mapped = mapped.filter(l => {
            let capacityValue = null;
            
            // Extract capacity từ title pattern "Pin Brand XXkWh"
            if (l.title) {
              const capacityMatch = l.title.match(/(\d+(?:\.\d+)?)\s*kWh/i);
              if (capacityMatch && capacityMatch[1]) {
                capacityValue = parseFloat(capacityMatch[1]);
              }
            }
            
            // Nếu không parse được capacity từ title, bỏ qua filter này (show all)
            if (capacityValue === null) return true;
            
            return capacityValue >= batteryCapacity[0] && capacityValue <= batteryCapacity[1];
          });
        }
      } else {
        // Client-side filters cho xe điện (backend không hỗ trợ brand, condition, battery capacity)
        
        // Brand filter cho xe điện - extract từ title vì backend không expose brand trong ListingCartResponseDTO
        if (carBrand) {
          mapped = mapped.filter(l => {
            let brandValue = '';
            
            if (l.title) {
              // Extract brand từ title patterns
              // Ví dụ: "VinFast VF8 Plus 2024", "BMW iX3 xDrive30i 2023", "Honda PCX Electric 2022"
              const words = l.title.split(' ');
              if (words.length > 0) {
                brandValue = words[0]; // First word is usually the brand
              }
            }
            
            return brandValue && brandValue.toLowerCase().includes(carBrand.toLowerCase());
          });
        }

        // Condition filter cho xe điện - dựa trên title parsing
        if (carCondition) {
          mapped = mapped.filter(l => {
            // Parse condition từ title patterns
            let hasCondition = false;
            
            if (l.title) {
              const title = l.title.toLowerCase();
              
              // Map UI condition values với title patterns
              switch (carCondition) {
                case 'EXCELLENT':
                  hasCondition = title.includes('như mới') || title.includes('tuyệt vời') || title.includes('excellent');
                  break;
                case 'GOOD':
                  hasCondition = title.includes('tốt') || title.includes('good') || (!title.includes('kém') && !title.includes('cần'));
                  break;
                case 'FAIR':
                  hasCondition = title.includes('khá') || title.includes('fair') || title.includes('bình thường');
                  break;
                case 'POOR':
                  hasCondition = title.includes('kém') || title.includes('poor') || title.includes('xấu');
                  break;
                case 'NEEDS_MAINTENANCE':
                  hasCondition = title.includes('cần bảo trì') || title.includes('maintenance') || title.includes('sửa chữa');
                  break;
                default:
                  hasCondition = true; // Fallback: show all if can't determine
              }
            }
            
            return hasCondition;
          });
        }

        // Battery capacity range filter cho xe điện - dựa trên title parsing  
        if (carBatteryCapacity && Array.isArray(carBatteryCapacity)) {
          mapped = mapped.filter(l => {
            // Parse capacity từ title patterns
            let hasCapacityInRange = false;
            
            if (l.title) {
              // Tìm pattern "X kWh" hoặc "X.X kWh" trong title
              const capacityMatch = l.title.match(/(\d+(?:\.\d+)?)\s*kWh/i);
              
              if (capacityMatch) {
                const capacity = parseFloat(capacityMatch[1]);
                hasCapacityInRange = capacity >= carBatteryCapacity[0] && capacity <= carBatteryCapacity[1];
              } else {
                // Fallback: nếu không parse được, hiển thị tất cả
                hasCapacityInRange = true;
              }
            } else {
              hasCapacityInRange = true; // Fallback for empty title
            }
            
            return hasCapacityInRange;
          });
        }
      }

      setListings(mapped);
    } catch (err) {
      if (err.name === 'CanceledError' || err.name === 'AbortError') {
        if (import.meta.env.DEV) console.debug('[ListingPage] aborted fetch (stale request)', runStartedAt);
      } else {
        console.error('Fetch listings error:', err);
        console.error('Error response:', err.response?.data);
        console.error('Error status:', err.response?.status);
        console.error('Used endpoint:', usedEndpoint);
        
        // Cải thiện error message dựa trên status code
        let errorMessage = 'Không thể tải danh sách lúc này.';
        if (err.response?.status === 404) {
          errorMessage = 'API endpoint không tồn tại. Vui lòng liên hệ admin.';
        } else if (err.response?.status === 400) {
          errorMessage = 'Tham số tìm kiếm không hợp lệ.';
        } else if (err.response?.status >= 500) {
          errorMessage = 'Lỗi server. Vui lòng thử lại sau.';
        }
        
        setError(errorMessage);
        setListings([]);
        setPagination(null);
      }
    } finally {
      setLoading(false);
    }
  }, [category, vehicleType, page, size, year, priceRange, batteryBrand, batteryCondition, batteryCapacity, carBrand, carCondition, carBatteryCapacity]);

  // Fetch when dependencies change
  useEffect(() => { fetchListings(); }, [fetchListings]);

  // Sync URL query -> state (category + vehicleType) whenever location.search changes
  useEffect(() => {
    const p = new URLSearchParams(location.search);
    const cat = p.get('category');
    
    // Map Vietnamese slugs to backend vehicle types
    const slugMap = {
      'xe-may-dien': 'MOTORBIKE',
      'xe-dap-dien': 'BIKE',
      'o-to-dien': 'CAR'
    };

    if (cat) {
      if (cat === 'pin') {
        if (category !== 'pin') {
          setCategory('pin');
          setVehicleType(null); // clear vehicle filter khi chuyển sang pin
          // Reset car filters khi chuyển sang pin
          setCarBrand('');
          setCarCondition('');
          setCarBatteryCapacity([0, 200]);
        }
      } else if (slugMap[cat]) {
        if (category !== 'cars') {
          setCategory('cars');
          // Reset battery filters khi chuyển sang cars
          setBatteryBrand('');
          setBatteryCondition('');
          setBatteryCapacity([0, 200]);
        }
        const vt = slugMap[cat];
        if (vehicleType !== vt) setVehicleType(vt);
      } else if (['cars','ev'].includes(cat)) {
        if (category !== 'cars') {
          setCategory('cars');
          // Reset battery filters khi chuyển sang cars
          setBatteryBrand('');
          setBatteryCondition('');
          setBatteryCapacity([0, 200]);
        }
        if (vehicleType !== null) setVehicleType(null);
      }
    }
  }, [location.search, category, vehicleType]);

  // Reset page when filters change
  useEffect(() => { setPage(0); }, [category, vehicleType, year, batteryBrand, batteryCondition, batteryCapacity, carBrand, carCondition, carBatteryCapacity]);

  // Removed unused nextPage and prevPage functions

  return (
    <Layout style={{ minHeight: 'calc(100vh - 140px)' }}>
      <Sider width={300} breakpoint="lg" collapsedWidth={0} theme="light" style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
        <div style={{ padding: '16px' }}>
          <div style={{ marginBottom: 8 }}>
            <Title level={5} style={{ margin: 0 }}>Bộ lọc</Title>
          </div>
          <Form layout="vertical" size="middle">
            <Form.Item label="Danh mục">
              <Select
                value={category}
                onChange={(v) => { 
                  // Cập nhật category state
                  setCategory(v); 
                  setVehicleType(null);
                  // Cập nhật URL để reflect category mới
                  navigate(`/listings?category=${v}`);
                }}
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
                  placeholder="Chọn loại xe"
                  value={vehicleType || undefined}
                  onChange={(v) => {
                    // Cập nhật vehicleType state
                    setVehicleType(v || null);
                    
                    // Cập nhật URL để reflect vehicleType mới
                    if (v) {
                      // Map backend vehicle types to Vietnamese slugs for URL
                      const typeSlugMap = {
                        'CAR': 'o-to-dien',
                        'MOTORBIKE': 'xe-may-dien', 
                        'BIKE': 'xe-dap-dien'
                      };
                      const slug = typeSlugMap[v];
                      if (slug) {
                        navigate(`/listings?category=${slug}`);
                      }
                    } else {
                      // Clear vehicleType - về cars general
                      navigate('/listings?category=cars');
                    }
                  }}
                  options={[
                    { value: 'CAR', label: 'Ô tô điện' },
                    { value: 'MOTORBIKE', label: 'Xe máy điện' },
                    { value: 'BIKE', label: 'Xe đạp điện' }
                  ]}
                />
              </Form.Item>
            )}
            {category === 'cars' && (
              <Form.Item label={<span>Năm sản xuất <Text type="secondary" style={{ fontSize: 12 }}>(server-side)</Text></span>}>
                <Select
                  placeholder="Chọn năm sản xuất"
                  value={year || undefined}
                  style={{ width: '100%' }}
                  allowClear
                  onChange={(val) => {
                    // Cập nhật year filter với giá trị được chọn từ dropdown
                    setYear(val || '');
                  }}
                  options={(() => {
                    // Tạo danh sách các năm từ 2010 đến năm hiện tại (2025)
                    const currentYear = new Date().getFullYear();
                    const years = [];
                    for (let year = currentYear; year >= 2010; year--) {
                      years.push({ value: String(year), label: String(year) });
                    }
                    return years;
                  })()}
                />
              </Form.Item>
            )}
            {/* Filters cho Pin Listings */}
            {category === 'pin' && (
              <>
                <Form.Item label={<span>Thương hiệu pin <Text type="secondary" style={{ fontSize: 12 }}>(client-side)</Text></span>}>
                  <AutoComplete
                    placeholder="Nhập thương hiệu pin..."
                    value={batteryBrand || undefined}
                    onChange={(val) => setBatteryBrand(val || '')}
                    allowClear
                    filterOption={(inputValue, option) =>
                      option.value.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1
                    }
                    options={batteryBrandOptions.map(brandName => ({ value: brandName, label: brandName }))}
                  />
                </Form.Item>

                <Form.Item label={<span>Tình trạng pin <Text type="secondary" style={{ fontSize: 12 }}>(client-side)</Text></span>}>
                  <Select
                    placeholder="Chọn tình trạng pin"
                    value={batteryCondition || undefined}
                    onChange={(val) => setBatteryCondition(val || '')}
                    allowClear
                    options={batteryConditionOptions}
                  />
                </Form.Item>

                <Form.Item label={<span>Dung lượng pin (kWh) <Text type="secondary" style={{ fontSize: 12 }}>(client-side)</Text></span>}>
                  <Slider
                    range
                    min={0}
                    max={200}
                    step={5}
                    value={batteryCapacity}
                    tooltip={{ formatter: (v) => `${v} kWh` }}
                    onChange={(vals) => setBatteryCapacity(vals)}
                  />
                  <Space style={{ width: '100%', justifyContent: 'space-between' }} size={8}>
                    <InputNumber
                      value={batteryCapacity[0]}
                      min={0}
                      max={batteryCapacity[1]}
                      step={5}
                      addonAfter="kWh"
                      size="small"
                      onChange={(v) => setBatteryCapacity([v || 0, batteryCapacity[1]])}
                    />
                    <InputNumber
                      value={batteryCapacity[1]}
                      min={batteryCapacity[0]}
                      max={200}
                      step={5}
                      addonAfter="kWh"
                      size="small"
                      onChange={(v) => setBatteryCapacity([batteryCapacity[0], v || 200])}
                    />
                  </Space>
                </Form.Item>
              </>
            )}

            {/* Filters cho Vehicle Listings */}
            {category === 'cars' && (
              <>
                <Form.Item label={<span>Thương hiệu xe <Text type="secondary" style={{ fontSize: 12 }}>(client-side)</Text></span>}>
                  <AutoComplete
                    placeholder="Nhập thương hiệu xe..."
                    value={carBrand || undefined}
                    onChange={(val) => setCarBrand(val || '')}
                    allowClear
                    filterOption={(inputValue, option) =>
                      option.value.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1
                    }
                    options={carBrandOptions.map(brandName => ({ value: brandName, label: brandName }))}
                  />
                </Form.Item>

                <Form.Item label={<span>Tình trạng xe <Text type="secondary" style={{ fontSize: 12 }}>(client-side)</Text></span>}>
                  <Select
                    placeholder="Chọn tình trạng xe"
                    value={carCondition || undefined}
                    onChange={(val) => setCarCondition(val || '')}
                    allowClear
                    options={carConditionOptions}
                  />
                </Form.Item>

                <Form.Item label={<span>Dung lượng pin xe (kWh) <Text type="secondary" style={{ fontSize: 12 }}>(client-side)</Text></span>}>
                  <Slider
                    range
                    min={0}
                    max={200}
                    step={5}
                    value={carBatteryCapacity}
                    tooltip={{ formatter: (v) => `${v} kWh` }}
                    onChange={(vals) => setCarBatteryCapacity(vals)}
                  />
                  <Space style={{ width: '100%', justifyContent: 'space-between' }} size={8}>
                    <InputNumber
                      value={carBatteryCapacity[0]}
                      min={0}
                      max={carBatteryCapacity[1]}
                      step={5}
                      addonAfter="kWh"
                      size="small"
                      onChange={(v) => setCarBatteryCapacity([v || 0, carBatteryCapacity[1]])}
                    />
                    <InputNumber
                      value={carBatteryCapacity[1]}
                      min={carBatteryCapacity[0]}
                      max={200}
                      step={5}
                      addonAfter="kWh"
                      size="small"
                      onChange={(v) => setCarBatteryCapacity([carBatteryCapacity[0], v || 200])}
                    />
                  </Space>
                </Form.Item>
              </>
            )}
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
              <Button loading={loading} onClick={()=> fetchListings()} type="primary" block>
                Làm mới
              </Button>
            </Space.Compact>
            <Text type="secondary" style={{ fontSize: 11, display: 'block', marginTop: 12 }}>
              * Server-side: Năm sản xuất, Loại xe.
              <br />
              * Client-side: Giá, Thương hiệu, Tình trạng, Dung lượng pin.
            </Text>
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
