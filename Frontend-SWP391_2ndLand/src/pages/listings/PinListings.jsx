import React, { useState, useEffect, useCallback } from "react";
import { Layout, Row, Col, Pagination, Spin, Alert, Typography, Space } from "antd";
import { ThunderboltOutlined } from '@ant-design/icons';
import BatteryAdvancedFilter from "../../components/listings/BatteryAdvancedFilter";
import { mapListingArray } from "../../utils/listingMapper";
import { formatPrice, formatBatteryCapacity, formatBatteryHealth } from "../../constants/filterOptions";
import listingsApi from "../../api/listings";

const { Content } = Layout;
const { Title } = Typography;

/**
 * Trang danh sách pin điện với filter nâng cao
 * Đã cập nhật để sử dụng API filter mới và database schema mới
 */
function PinListings() {
  const [listings, setListings] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentFilters, setCurrentFilters] = useState({});
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 12;

  /**
   * Fetch danh sách pin từ API với filter
   */
  const fetchBatteryListings = useCallback(async (filters = {}, page = 0) => {
    try {
      setLoading(true);
      setError(null);
      
      const filterParams = {
        ...filters,
        page: page,
        size: pageSize
      };
      
      console.log('[PIN_LISTINGS] Fetching with filters:', filterParams);
      
      const response = await listingsApi.batteryFilterListings(filterParams);
      console.log('[PIN_LISTINGS] API Response:', response);
      
      if (response && Array.isArray(response.content)) {
        const mappedListings = mapListingArray(response.content);
        setListings(mappedListings);
        setPagination(response);
      } else {
        console.warn('API response format không đúng:', response);
        setListings([]);
        setPagination(null);
      }
    } catch (err) {
      console.error('Lỗi khi fetch battery listings:', err);
      setError('Không thể tải danh sách pin điện. Vui lòng thử lại sau.');
      setListings([]);
      setPagination(null);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Handle filter change từ BatteryAdvancedFilter component
   */
  const handleFilterChange = useCallback((filters) => {
    console.log('[PIN_LISTINGS] Filter changed:', filters);
    setCurrentFilters(filters);
    setCurrentPage(0);
    fetchBatteryListings(filters, 0);
  }, [fetchBatteryListings]);

  /**
   * Handle pagination change
   */
  const handlePageChange = useCallback((page) => {
    const pageIndex = page - 1; // Ant Design uses 1-based, API uses 0-based
    setCurrentPage(pageIndex);
    fetchBatteryListings(currentFilters, pageIndex);
  }, [currentFilters, fetchBatteryListings]);

  // Load initial data khi component mount
  useEffect(() => {
    fetchBatteryListings({}, 0);
  }, [fetchBatteryListings]);

  if (error) {
    return (
      <Layout style={{ minHeight: '100vh', padding: '24px' }}>
        <Content>
          <Alert
            message="Lỗi tải dữ liệu"
            description={error}
            type="error"
            showIcon
            action={
              <button 
                onClick={() => fetchBatteryListings(currentFilters, currentPage)}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
              >
                Thử lại
              </button>
            }
          />
        </Content>
      </Layout>
    );
  }

  return (
    <Layout style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <Content style={{ padding: '24px' }}>
        {/* Page Header */}
        <div style={{ marginBottom: '24px', textAlign: 'center' }}>
          <Space>
            <ThunderboltOutlined style={{ fontSize: '32px', color: '#52c41a' }} />
            <Title level={2} style={{ margin: 0 }}>
              Pin Điện
            </Title>
          </Space>
          <p style={{ color: '#666', fontSize: '16px', marginTop: '8px' }}>
            Tìm kiếm và lọc pin điện theo nhu cầu của bạn
          </p>
        </div>

        {/* Advanced Filter */}
        <BatteryAdvancedFilter 
          onFilter={handleFilterChange}
          loading={loading}
          initialValues={currentFilters}
        />

        {/* Results Section */}
        <div style={{ marginTop: '24px' }}>
          {/* Results Summary */}
          {pagination && (
            <div style={{ marginBottom: '16px', color: '#666' }}>
              Tìm thấy <strong>{pagination.totalElements}</strong> pin điện
              {Object.keys(currentFilters).length > 0 && ' với bộ lọc hiện tại'}
            </div>
          )}

          {/* Loading State */}
          {loading && (
            <div style={{ textAlign: 'center', padding: '60px 0' }}>
              <Spin size="large" />
              <p style={{ marginTop: '16px', color: '#666' }}>Đang tải dữ liệu...</p>
            </div>
          )}

          {/* Listings Grid */}
          {!loading && (
            <>
              <Row gutter={[16, 16]}>
                {listings.map((listing) => (
                  <Col xs={24} sm={12} md={8} lg={6} key={listing.id}>
                    <div className="listing-card" style={{
                      backgroundColor: 'white',
                      borderRadius: '8px',
                      padding: '16px',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                      height: '100%',
                      cursor: 'pointer',
                      transition: 'all 0.3s ease'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.transform = 'translateY(-4px)';
                      e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.15)';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.transform = 'translateY(0)';
                      e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                    }}>
                      {/* Image */}
                      <div style={{ marginBottom: '12px' }}>
                        <img 
                          src={listing.image || "/images/default-battery.jpg"} 
                          alt={listing.title}
                          style={{
                            width: '100%',
                            height: '160px',
                            objectFit: 'cover',
                            borderRadius: '6px'
                          }}
                        />
                      </div>

                      {/* Title */}
                      <h3 style={{ 
                        fontSize: '16px', 
                        marginBottom: '8px',
                        fontWeight: '600',
                        lineHeight: '1.4',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {listing.title}
                      </h3>

                      {/* Price */}
                      <div style={{ 
                        fontSize: '18px', 
                        fontWeight: 'bold', 
                        color: '#d4380d',
                        marginBottom: '8px'
                      }}>
                        {formatPrice(listing.price)}
                      </div>

                      {/* Battery Info */}
                      <div style={{ 
                        fontSize: '12px', 
                        color: '#666',
                        marginBottom: '8px',
                        display: 'flex',
                        justifyContent: 'space-between'
                      }}>
                        <span>⚡ {listing.batteryCapacity ? formatBatteryCapacity(listing.batteryCapacity) : 'N/A'}</span>
                        <span>🔋 {listing.healthPercentage ? formatBatteryHealth(listing.healthPercentage) : 'N/A'}</span>
                      </div>

                      {/* Stats */}
                      <div style={{ 
                        fontSize: '12px', 
                        color: '#666',
                        display: 'flex',
                        justifyContent: 'space-between'
                      }}>
                        <span>👁 {listing.viewsCount || 0} lượt xem</span>
                        <span>📱 {listing.sellerPhone || 'Liên hệ'}</span>
                      </div>
                    </div>
                  </Col>
                ))}
              </Row>

              {/* Empty State */}
              {listings.length === 0 && (
                <div style={{ textAlign: 'center', padding: '60px 0' }}>
                  <ThunderboltOutlined style={{ fontSize: '64px', color: '#d9d9d9' }} />
                  <h3 style={{ color: '#666', marginTop: '16px' }}>
                    Không tìm thấy pin điện nào
                  </h3>
                  <p style={{ color: '#999' }}>
                    Thử điều chỉnh bộ lọc hoặc tìm kiếm với từ khóa khác
                  </p>
                </div>
              )}

              {/* Pagination */}
              {pagination && pagination.totalElements > pageSize && (
                <div style={{ textAlign: 'center', marginTop: '32px' }}>
                  <Pagination
                    current={currentPage + 1} // Ant Design uses 1-based
                    total={pagination.totalElements}
                    pageSize={pageSize}
                    showSizeChanger={false}
                    showQuickJumper
                    showTotal={(total, range) => 
                      `${range[0]}-${range[1]} của ${total} pin điện`
                    }
                    onChange={handlePageChange}
                  />
                </div>
              )}
            </>
          )}
        </div>
      </Content>
    </Layout>
  );
}

export default PinListings;