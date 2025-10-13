import React, { useState, useEffect } from 'react';
import { Form, Select, InputNumber, Row, Col, Button, Space, Divider, Typography, AutoComplete, Collapse, Card } from 'antd';
import { SearchOutlined, ClearOutlined, FilterOutlined } from '@ant-design/icons';
import { 
  VEHICLE_TYPES, 
  VEHICLE_CONDITIONS, 
  PRICE_RANGES,
  YEAR_RANGES,
  BATTERY_CAPACITY_RANGES,
  MILEAGE_RANGES 
} from '../../constants/filterOptions';
import listingsApi from '../../api/listings';

const { Option } = Select;
const { Title, Text } = Typography;
const { Panel } = Collapse;

/**
 * Component Filter nâng cao cho xe điện
 * Hỗ trợ đầy đủ các tiêu chí filter theo database schema mới
 */
const EvAdvancedFilter = ({ onFilter, loading = false, initialValues = {} }) => {
  const [form] = Form.useForm();
  const [filterData, setFilterData] = useState({
    provinces: [],
    districts: [],
    brands: [],
    years: []
  });
  const [loadingData, setLoadingData] = useState(false);

  // Load filter data từ API khi component mount
  useEffect(() => {
    loadFilterData();
  }, []);

  // Set initial values nếu có
  useEffect(() => {
    if (initialValues && Object.keys(initialValues).length > 0) {
      form.setFieldsValue(initialValues);
    }
  }, [initialValues, form]);

  /**
   * Load dữ liệu cho dropdowns từ backend
   */
  const loadFilterData = async () => {
    setLoadingData(true);
    try {
      const [provinces, brands, years] = await Promise.all([
        listingsApi.getProvinces(),
        listingsApi.getEvBrands(), 
        listingsApi.getEvYears()
      ]);

      setFilterData({
        provinces: provinces || [],
        districts: [],
        brands: brands || [],
        years: years || []
      });
    } catch (error) {
      console.error('Lỗi khi load filter data:', error);
    } finally {
      setLoadingData(false);
    }
  };

  /**
   * Load districts khi user chọn province
   */
  const handleProvinceChange = async (province) => {
    if (!province) {
      setFilterData(prev => ({ ...prev, districts: [] }));
      form.setFieldsValue({ district: undefined });
      return;
    }

    try {
      const districts = await listingsApi.getDistricts(province);
      setFilterData(prev => ({ ...prev, districts: districts || [] }));
    } catch (error) {
      console.error('Lỗi khi load districts:', error);
    }
  };

  /**
   * Handle submit form - gọi callback onFilter với values
   */
  const handleSubmit = (values) => {
    // Loại bỏ các giá trị null/undefined/empty
    const cleanedValues = Object.fromEntries(
      Object.entries(values).filter(([, v]) => v !== undefined && v !== null && v !== '')
    );
    
    console.log('[EV_FILTER_SUBMIT]', cleanedValues);
    onFilter(cleanedValues);
  };

  /**
   * Reset form về trạng thái ban đầu
   */
  const handleReset = () => {
    form.resetFields();
    setFilterData(prev => ({ ...prev, districts: [] }));
    onFilter({}); // Gọi filter with empty object
  };

  /**
   * Apply quick filter preset
   */
  const applyPreset = (preset) => {
    form.setFieldsValue(preset);
    handleSubmit({ ...form.getFieldsValue(), ...preset });
  };

  return (
    <Card 
      title={
        <Space>
          <FilterOutlined />
          <Title level={4} style={{ margin: 0 }}>Lọc xe điện nâng cao</Title>
        </Space>
      }
      size="small"
      style={{ marginBottom: 16 }}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={initialValues}
      >
        {/* Quick Filter Presets */}
        <Collapse ghost>
          <Panel header="Bộ lọc nhanh" key="presets">
            <Space wrap>
              <Button 
                size="small" 
                onClick={() => applyPreset({ vehicleType: 'CAR', conditionStatus: 'EXCELLENT' })}
              >
                Ô tô xuất sắc
              </Button>
              <Button 
                size="small"
                onClick={() => applyPreset({ vehicleType: 'CAR', minYear: 2023 })}
              >
                Ô tô mới 2023+
              </Button>
              <Button 
                size="small"
                onClick={() => applyPreset({ maxMileage: 20000, conditionStatus: 'GOOD' })}
              >
                Ít km, tình trạng tốt
              </Button>
              <Button 
                size="small"
                onClick={() => applyPreset({ minBatteryCapacity: 70 })}
              >
                Pin dung lượng cao
              </Button>
            </Space>
          </Panel>
        </Collapse>

        <Divider />

        <Row gutter={[16, 16]}>
          {/* Loại xe */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Loại xe" name="vehicleType">
              <Select placeholder="Chọn loại xe" allowClear>
                {VEHICLE_TYPES.map(type => (
                  <Option key={type.value} value={type.value}>
                    {type.label}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>

          {/* Thương hiệu */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Thương hiệu" name="brand">
              <AutoComplete
                placeholder="Chọn hoặc nhập thương hiệu"
                options={filterData.brands.map(brand => ({ value: brand, label: brand }))}
                filterOption={(inputValue, option) =>
                  option.value.toLowerCase().includes(inputValue.toLowerCase())
                }
                allowClear
              />
            </Form.Item>
          </Col>

          {/* Tình trạng xe */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Tình trạng xe" name="conditionStatus">
              <Select placeholder="Chọn tình trạng" allowClear>
                {VEHICLE_CONDITIONS.map(condition => (
                  <Option key={condition.value} value={condition.value}>
                    {condition.label}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>

          {/* Năm sản xuất chính xác */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Năm sản xuất" name="year">
              <Select placeholder="Chọn năm" allowClear showSearch>
                {filterData.years.map(year => (
                  <Option key={year} value={year}>
                    {year}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>

          {/* Khoảng năm tối thiểu */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Từ năm" name="minYear">
              <InputNumber 
                placeholder="VD: 2020"
                min={2018}
                max={2024}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>

          {/* Khoảng năm tối đa */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Đến năm" name="maxYear">
              <InputNumber 
                placeholder="VD: 2024"
                min={2018}
                max={2024}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>

          {/* Tỉnh/Thành phố */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Tỉnh/Thành phố" name="province">
              <Select 
                placeholder="Chọn tỉnh/thành phố" 
                allowClear
                showSearch
                loading={loadingData}
                onChange={handleProvinceChange}
              >
                {filterData.provinces.map(province => (
                  <Option key={province} value={province}>
                    {province}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>

          {/* Quận/Huyện */}
          <Col xs={24} sm={12} md={8}>
            <Form.Item label="Quận/Huyện" name="district">
              <Select 
                placeholder="Chọn quận/huyện" 
                allowClear
                showSearch
                disabled={!filterData.districts.length}
              >
                {filterData.districts.map(district => (
                  <Option key={district} value={district}>
                    {district}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left">
          <Text type="secondary">Thông số kỹ thuật</Text>
        </Divider>

        <Row gutter={[16, 16]}>
          {/* Quãng đường đã đi - Min */}
          <Col xs={24} sm={12} md={6}>
            <Form.Item label="Quãng đường từ (km)" name="minMileage">
              <InputNumber 
                placeholder="0"
                min={0}
                max={200000}
                formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={value => value.replace(/\$\s?|(,*)/g, '')}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>

          {/* Quãng đường đã đi - Max */}
          <Col xs={24} sm={12} md={6}>
            <Form.Item label="Quãng đường đến (km)" name="maxMileage">
              <InputNumber 
                placeholder="100000"
                min={0}
                max={200000}
                formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={value => value.replace(/\$\s?|(,*)/g, '')}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>

          {/* Dung lượng pin - Min */}
          <Col xs={24} sm={12} md={6}>
            <Form.Item label="Dung lượng pin từ (kWh)" name="minBatteryCapacity">
              <InputNumber 
                placeholder="40"
                min={20}
                max={150}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>

          {/* Dung lượng pin - Max */}
          <Col xs={24} sm={12} md={6}>
            <Form.Item label="Dung lượng pin đến (kWh)" name="maxBatteryCapacity">
              <InputNumber 
                placeholder="100"
                min={20}
                max={150}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left">
          <Text type="secondary">Mức giá</Text>
        </Divider>

        <Row gutter={[16, 16]}>
          {/* Giá tối thiểu */}
          <Col xs={24} sm={12}>
            <Form.Item label="Giá từ (VNĐ)" name="minPrice">
              <InputNumber 
                placeholder="50,000,000"
                min={0}
                step={10000000}
                formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={value => value.replace(/\$\s?|(,*)/g, '')}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>

          {/* Giá tối đa */}
          <Col xs={24} sm={12}>
            <Form.Item label="Giá đến (VNĐ)" name="maxPrice">
              <InputNumber 
                placeholder="2,000,000,000"
                min={0}
                step={10000000}
                formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                parser={value => value.replace(/\$\s?|(,*)/g, '')}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>
        </Row>

        <Divider />

        {/* Action buttons */}
        <Row justify="end" gutter={[8, 8]}>
          <Col>
            <Button 
              onClick={handleReset}
              icon={<ClearOutlined />}
            >
              Xóa bộ lọc
            </Button>
          </Col>
          <Col>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={loading}
              icon={<SearchOutlined />}
            >
              Tìm kiếm
            </Button>
          </Col>
        </Row>
      </Form>
    </Card>
  );
};

export default EvAdvancedFilter;
