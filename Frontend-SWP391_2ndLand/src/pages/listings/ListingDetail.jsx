import React, { useState, useEffect, useCallback, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../api/axios";
import { AuthContext } from "../../contexts/AuthContext";
import {
  Modal,
  Button,
  Card,
  Row,
  Col,
  Descriptions,
  Image,
  Typography,
  Tag,
  Space,
  Spin,
  Result,
  Divider
} from 'antd';

const { Title, Text } = Typography;

function ListingDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn } = useContext(AuthContext) || {};
  const [listing, setListing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAuthModal, setShowAuthModal] = useState(false);

  /**
   * Fetch chi tiết listing từ API
   */
  const fetchListingDetail = useCallback(async () => {
    // Gate: require authentication to view detail
    if (!isLoggedIn) {
      setShowAuthModal(true);
      setLoading(false);
      return;
    }
    try {
      setLoading(true);
      setError(null);

      // Đúng endpoint backend: GET /api/listings/detail/{listingId}
      const response = await api.get(`/listings/detail/${id}`);

      // Backend trả về trực tiếp ListingDetailResponseDTO theo YAML
      const data = response.data;
      if (data) {
        // Chuẩn hóa một số field cho UI hiện tại
        const normalized = {
          id: data.id,
          title: data.title,
          description: data.description,
          price: data.price,
          listingType: data.listingType,
          status: data.status,
          createdAt: data.createdAt,
          updatedAt: data.updatedAt,
          product: data.product,
          location: data.location,
          seller: data.seller,
          thumbnail: data.thumbnail,
          images: Array.isArray(data.images) ? data.images : [],
        };
        setListing(normalized);
      } else {
        setError('Không tìm thấy listing này.');
      }
    } catch (err) {
      console.error('Lỗi khi fetch listing detail:', err);
      if (err.response?.status === 401) {
        setShowAuthModal(true);
        setError(null); // Ẩn error chung, hiển thị modal thay thế
      } else if (err.response?.status === 404) {
        setError('Listing không tồn tại.');
      } else {
        setError('Không thể tải thông tin listing. Vui lòng thử lại sau.');
      }
    } finally {
      setLoading(false);
    }
  }, [id, isLoggedIn]);

  useEffect(() => {
    if (id) {
      fetchListingDetail();
    }
  }, [id, fetchListingDetail]);

  /**
   * Format giá tiền
   */
  const formatPrice = (price) => {
    if (!price) return "Liên hệ";
    
    if (price >= 1000000000) {
      return `${(price / 1000000000).toFixed(1)} tỷ VND`;
    } else if (price >= 1000000) {
      return `${(price / 1000000).toFixed(0)} triệu VND`;
    } else {
      return `${price.toLocaleString()} VND`;
    }
  };

  /**
   * Format ngày tháng
   */
  const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <div style={{ padding: '48px 24px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Spin size="large" />
        <Text type="secondary" style={{ marginTop: 16 }}>Đang tải thông tin listing...</Text>
      </div>
    );
  }

  if (error) {
    return (
      <Result
        status="error"
        title="Không thể tải listing"
        subTitle={error}
        extra={[
          <Button key="retry" type="primary" onClick={fetchListingDetail}>Thử lại</Button>,
          <Button key="back" onClick={() => navigate(-1)}>Quay lại</Button>
        ]}
      />
    );
  }

  // If user is not logged in and auth modal is shown, render only the modal (avoid accessing listing=null)
  if (showAuthModal) {
    return (
      <>
        <Modal
          open={showAuthModal}
            onCancel={() => { setShowAuthModal(false); navigate(-1); }}
          centered
          title={<span>Yêu cầu đăng nhập</span>}
          footer={null}
          destroyOnClose
        >
          <Typography.Paragraph style={{ marginBottom: 24, textAlign: 'center' }}>
            Bạn cần đăng nhập để xem chi tiết listing này.<br/>Vui lòng đăng nhập hoặc tạo tài khoản mới.
          </Typography.Paragraph>
          <Space wrap style={{ width: '100%', justifyContent: 'center' }} size="middle">
            <Button onClick={() => { setShowAuthModal(false); navigate('/'); }}>Trang chủ</Button>
            <Button type="primary" onClick={() => navigate('/login')}>Đăng nhập</Button>
            <Button danger onClick={() => navigate('/register')}>Đăng ký</Button>
          </Space>
        </Modal>
      </>
    );
  }

  if (!listing && !showAuthModal && !loading && !error) {
    return (
      <Result
        status="404"
        title="404"
        subTitle="Không tìm thấy listing."
        extra={<Button onClick={() => navigate(-1)}>Quay lại</Button>}
      />
    );
  }

  return (
    <div style={{ maxWidth: 1240, margin: '0 auto', padding: '16px 20px 48px' }}>
      <Button type="text" onClick={() => navigate(-1)} style={{ marginBottom: 8 }}>
        ← Quay lại
      </Button>
      <Card
        style={{
          background: 'linear-gradient(135deg, #0d47a1 0%, #1976d2 70%, #42a5f5 100%)',
          marginBottom: 24,
          color: '#fff'
        }}
        bodyStyle={{ padding: 24 }}
        bordered={false}
      >
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} md={16}>
            <Title level={2} style={{ margin: 0, color: '#fff' }}>{listing?.title}</Title>
            <Space wrap size="small" style={{ marginTop: 12 }}>
              <Tag color="geekblue" style={{ margin: 0 }}>ID #{listing?.id}</Tag>
              <Tag color="default" style={{ margin: 0 }}>Người bán: {listing?.seller?.username || 'Ẩn danh'}</Tag>
            </Space>
          </Col>
          <Col xs={24} md={8} style={{ textAlign: 'right' }}>
            <Title level={3} style={{ margin: 0, color: '#fff' }}>{formatPrice(listing?.price)}</Title>
            <Text style={{ color: 'rgba(255,255,255,0.85)' }}>{listing?.viewsCount || 0} lượt xem</Text>
          </Col>
        </Row>
      </Card>
      <Row gutter={[24, 24]}>
        <Col xs={24} lg={16}>
          <Card
            title={<Text strong>Hình ảnh</Text>}
            bodyStyle={{ padding: 16 }}
            style={{ marginBottom: 24 }}
          >
            <Image.PreviewGroup>
              <Row gutter={[8, 8]}>
                <Col span={24}>
                  <Image
                    src={listing?.thumbnail || listing?.images?.[0] || '/images/default-listing.jpg'}
                    alt={listing?.title || 'listing'}
                    style={{ objectFit: 'cover', width: '100%', maxHeight: 420 }}
                    fallback={'/images/default-listing.jpg'}
                  />
                </Col>
                {listing?.images && listing.images.length > 1 && listing.images.slice(1, 9).map((img, idx) => (
                  <Col key={idx} xs={6} sm={6} md={6} lg={6}>
                    <Image
                      src={img}
                      alt={`thumb-${idx}`}
                      style={{ objectFit: 'cover', width: '100%', height: 90 }}
                      fallback={'/images/default-listing.jpg'}
                    />
                  </Col>
                ))}
              </Row>
            </Image.PreviewGroup>
          </Card>
          <Card title={<Text strong>Mô tả</Text>} bodyStyle={{ paddingTop: 12 }} style={{ marginBottom: 24 }}>
            <Typography.Paragraph style={{ whiteSpace: 'pre-wrap', marginBottom: 0 }}>
              {listing?.description || 'Chưa có mô tả chi tiết.'}
            </Typography.Paragraph>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          {listing?.product?.vehicle && (
            <Card title="Thông tin xe" size="small" style={{ marginBottom: 20 }}>
              <Descriptions column={1} colon size="small">
                <Descriptions.Item label="Loại">Xe {listing.product.vehicle.type}</Descriptions.Item>
                <Descriptions.Item label="Hãng">{listing.product.vehicle.brand}</Descriptions.Item>
                <Descriptions.Item label="Năm">{listing.product.vehicle.year}</Descriptions.Item>
                <Descriptions.Item label="Số km">{listing.product.vehicle.mileage?.toLocaleString()} km</Descriptions.Item>
                <Descriptions.Item label="Pin">{listing.product.vehicle.batteryCapacity} kWh</Descriptions.Item>
                <Descriptions.Item label="Tình trạng">{listing.product.vehicle.conditionStatus}</Descriptions.Item>
              </Descriptions>
            </Card>
          )}
          {listing?.product?.battery && (
            <Card title="Thông tin pin" size="small" style={{ marginBottom: 20 }}>
              <Descriptions column={1} colon size="small">
                <Descriptions.Item label="Loại">Pin điện</Descriptions.Item>
                <Descriptions.Item label="Hãng">{listing.product.battery.brand}</Descriptions.Item>
                <Descriptions.Item label="Dung lượng">{listing.product.battery.capacity} kWh</Descriptions.Item>
                <Descriptions.Item label="Độ khỏe">{listing.product.battery.healthPercentage}%</Descriptions.Item>
                <Descriptions.Item label="Tình trạng">{listing.product.battery.conditionStatus}</Descriptions.Item>
                {listing.product.battery.compatibleVehicles && (
                  <Descriptions.Item label="Xe tương thích">{listing.product.battery.compatibleVehicles}</Descriptions.Item>
                )}
              </Descriptions>
            </Card>
          )}
          {listing?.location && (
            <Card title="Vị trí" size="small" style={{ marginBottom: 20 }}>
              <Descriptions column={1} colon size="small">
                <Descriptions.Item label="Tỉnh/TP">{listing.location.province}</Descriptions.Item>
                {listing.location.district && (
                  <Descriptions.Item label="Quận/Huyện">{listing.location.district}</Descriptions.Item>
                )}
                {listing.location.details && (
                  <Descriptions.Item label="Chi tiết">{listing.location.details}</Descriptions.Item>
                )}
              </Descriptions>
            </Card>
          )}
          <Card title="Khác" size="small" style={{ marginBottom: 20 }}>
            <Descriptions column={1} colon size="small">
              <Descriptions.Item label="Loại listing">{listing?.listingType}</Descriptions.Item>
              <Descriptions.Item label="Trạng thái">{listing?.status}</Descriptions.Item>
              <Descriptions.Item label="Ngày đăng">{formatDate(listing?.createdAt)}</Descriptions.Item>
              {listing?.expiresAt && (
                <Descriptions.Item label="Hết hạn">{formatDate(listing.expiresAt)}</Descriptions.Item>
              )}
            </Descriptions>
          </Card>
          <Card size="small">
            <Space direction="vertical" style={{ width: '100%' }}>
              <Button type="primary" block>Liên hệ người bán</Button>
              <Text type="secondary" style={{ display: 'block', textAlign: 'center' }}>Nhấn để xem thông tin liên hệ</Text>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default ListingDetail;
