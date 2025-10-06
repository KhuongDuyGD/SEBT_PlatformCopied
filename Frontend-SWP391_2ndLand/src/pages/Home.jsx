// src/pages/Home.jsx
import { Link, useNavigate } from 'react-router-dom';
import { useContext, useState, useEffect, useCallback } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import {
  Card,
  Button,
  Carousel,
  Modal,
  Row,
  Col,
  Typography,
  Tag,
  Space,
  Skeleton,
  Divider,
  Timeline,
  Empty,
  Tooltip,
  Segmented
} from 'antd';
import {
  ThunderboltOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
  CustomerServiceOutlined,
  CommentOutlined,
  UserOutlined,
  FireOutlined,
  StarOutlined,
  ClockCircleOutlined,
  CheckCircleTwoTone,
  ProfileOutlined,
  PhoneOutlined,
  ShoppingCartOutlined
} from '@ant-design/icons';
import backgroundImage from '../assets/background.jpg';
import '../css/header.css';
import api from '../api/axios';
import { mapListingArray } from '../utils/listingMapper';

function Home() {
  const { isLoggedIn } = useContext(AuthContext);
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);
  const [loadingVeh, setLoadingVeh] = useState(true);
  const [loadingBat, setLoadingBat] = useState(true);
  const [loadingNewest, setLoadingNewest] = useState(true);
  const [vehListings, setVehListings] = useState([]);
  const [batListings, setBatListings] = useState([]);
  const [newestListings, setNewestListings] = useState([]);
  const [viewMode, setViewMode] = useState('grid'); // for newest section (grid | compact)

  const safeMap = (payload) => {
    const raw = Array.isArray(payload?.data) ? payload.data : [];
    return mapListingArray(raw).slice(0, 8);
  };

  const fetchVeh = useCallback(async () => {
    try {
      setLoadingVeh(true);
      const res = await api.get('/listings/evCart?page=0&size=8');
      setVehListings(safeMap(res.data));
    } catch (e) { setVehListings([]); }
    finally { setLoadingVeh(false); }
  }, []);

  const fetchBat = useCallback(async () => {
    try {
      setLoadingBat(true);
      const res = await api.get('/listings/batteryCart?page=0&size=8');
      setBatListings(safeMap(res.data));
    } catch (e) { setBatListings([]); }
    finally { setLoadingBat(false); }
  }, []);

  const fetchNewest = useCallback(async () => {
    // tạm thời tái sử dụng evCart + batteryCart rồi merge & sort theo id desc
    try {
      setLoadingNewest(true);
      const [evRes, batRes] = await Promise.all([
        api.get('/listings/evCart?page=0&size=6'),
        api.get('/listings/batteryCart?page=0&size=6')
      ]);
      const ev = safeMap(evRes.data);
      const bt = safeMap(batRes.data);
      const merged = [...ev, ...bt].sort((a,b)=> (b.id||0) - (a.id||0)).slice(0,8);
      setNewestListings(merged);
    } catch (e) { setNewestListings([]); }
    finally { setLoadingNewest(false); }
  }, []);

  useEffect(()=> {
    fetchVeh();
    fetchBat();
    fetchNewest();
  }, [fetchVeh, fetchBat, fetchNewest]);

  const handlePostListingClick = () => {
    if (isLoggedIn) {
      navigate("/post-listing"); // Navigate to post-listing if logged in
    } else {
      setShowModal(true); // Show popup if not logged in
    }
  };

  return (
    <>
      {/* Hero Section with Overlay and Animation */}
      <div className="hero-wrapper">
        <div className="hero-bg" style={{ backgroundImage: `url(${backgroundImage})` }} />
        <div className="hero-overlay" />
        <div className="hero-content">
          <h1 style={{ fontSize: '3.2rem', fontWeight: 700, color: '#fee877', lineHeight: 1.1, marginBottom: '1.2rem' }}>
            FPT EV Secondhand Marketplace
          </h1>
          <p style={{ fontSize: '1.25rem', color: '#f1f5ff', fontWeight: 500, maxWidth: 640 }}>
            Xe & Pin điện đã qua sử dụng – Kết nối giá trị, tiếp năng lượng xanh.
          </p>
          <div className="hero-buttons">
            <Button type="primary" size="large" style={{ borderRadius: 30, padding: '0 42px', fontWeight: 600, background: 'linear-gradient(90deg,#fee877,#f9d848)', color: '#294a9b', border: 'none' }} onClick={handlePostListingClick}>
              Đăng bán ngay
            </Button>
            <Button size="large" style={{ borderRadius: 30 }}>
              <Link to="/listings?category=pin">Tìm Pin</Link>
            </Button>
            <Button size="large" style={{ borderRadius: 30 }}>
              <Link to="/listings?category=cars">Tìm Xe</Link>
            </Button>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div style={{ paddingTop: '3.5rem', paddingBottom: '2.5rem', maxWidth: 1300, margin: '0 auto' }}>
        <h2 className="text-center fw-bold" style={{ fontSize: '2.3rem', color: '#27407a', marginBottom: 32 }}>Vì sao chọn chúng tôi?</h2>
        <div className="feature-grid" style={{ marginBottom: 0 }}>
          <Card className="fade-card shadow-sm" hoverable style={{ border: 'none' }}>
            <div style={{ padding: '1.25rem' }}>
              <div style={{ fontSize: 40, color: '#2f6fdd', marginBottom: 12 }}><RocketOutlined /></div>
              <h5 style={{ fontWeight: 600, marginBottom: 8 }}>Đăng bán cực nhanh</h5>
              <p className="text-muted" style={{ fontSize: 14, minHeight: 40 }}>Quy trình 3 bước rõ ràng – tối ưu tỉ lệ chuyển đổi.</p>
              <Button type="link"><Link to="/post-listing">Tạo ngay →</Link></Button>
            </div>
          </Card>
          <Card className="fade-card shadow-sm" hoverable style={{ border: 'none' }}>
            <div style={{ padding: '1.25rem' }}>
              <div style={{ fontSize: 40, color: '#46b57d', marginBottom: 12 }}><SafetyCertificateOutlined /></div>
              <h5 style={{ fontWeight: 600, marginBottom: 8 }}>Niêm yết minh bạch</h5>
              <p className="text-muted" style={{ fontSize: 14, minHeight: 40 }}>Thông tin chuẩn hóa giúp người mua dễ so sánh.</p>
              <Button type="link"><Link to="/listings?category=cars">Xem xe →</Link></Button>
            </div>
          </Card>
          <Card className="fade-card shadow-sm" hoverable style={{ border: 'none' }}>
            <div style={{ padding: '1.25rem' }}>
              <div style={{ fontSize: 40, color: '#f2a13a', marginBottom: 12 }}><CustomerServiceOutlined /></div>
              <h5 style={{ fontWeight: 600, marginBottom: 8 }}>Hỗ trợ tận tâm</h5>
              <p className="text-muted" style={{ fontSize: 14, minHeight: 40 }}>Đội ngũ sẵn sàng giải đáp và đồng hành.</p>
              <Button type="link"><Link to="/support">Liên hệ →</Link></Button>
            </div>
          </Card>
          <Card className="fade-card shadow-sm" hoverable style={{ border: 'none' }}>
            <div style={{ padding: '1.25rem' }}>
              <div style={{ fontSize: 40, color: '#d84f5f', marginBottom: 12 }}><ThunderboltOutlined /></div>
              <h5 style={{ fontWeight: 600, marginBottom: 8 }}>Tối ưu trải nghiệm</h5>
              <p className="text-muted" style={{ fontSize: 14, minHeight: 40 }}>Tốc độ tải nhanh & giao diện thống nhất.</p>
              <Button type="link"><Link to="/listings?category=pin">Tìm pin →</Link></Button>
            </div>
          </Card>
        </div>
      </div>

      {/* Statistics Section with Gradient */}
      <div className="stats-band">
        <div style={{ maxWidth: 1300, margin:'0 auto' }}>
          <Row gutter={[24,24]}>
            <Col xs={12} md={6} lg={6} xl={6} xxl={6} className="stat-item"><div className="stat-number">1,000+</div><div className="text-muted" style={{ marginTop:4 }}>Xe Điện Đã Bán</div></Col>
            <Col xs={12} md={6} lg={6} xl={6} xxl={6} className="stat-item"><div className="stat-number" style={{ background:'linear-gradient(90deg,#2fa879,#5bd597)', WebkitBackgroundClip:'text', color:'transparent' }}>500+</div><div className="text-muted" style={{ marginTop:4 }}>Khách Hàng Hài Lòng</div></Col>
            <Col xs={12} md={6} lg={6} xl={6} xxl={6} className="stat-item"><div className="stat-number" style={{ background:'linear-gradient(90deg,#f29f3d,#f6c25b)', WebkitBackgroundClip:'text', color:'transparent' }}>99%</div><div className="text-muted" style={{ marginTop:4 }}>Tỷ Lệ Thành Công</div></Col>
            <Col xs={12} md={6} lg={6} xl={6} xxl={6} className="stat-item"><div className="stat-number" style={{ background:'linear-gradient(90deg,#d94862,#ff6c85)', WebkitBackgroundClip:'text', color:'transparent' }}>24/7</div><div className="text-muted" style={{ marginTop:4 }}>Hỗ Trợ</div></Col>
          </Row>
        </div>
      </div>

      {/* Trending Vehicles Section */}
      <div style={{ maxWidth: 1300, margin: '0 auto', padding: '3rem 0 1.5rem' }}>
        <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
          <Col><Typography.Title level={3} style={{ margin: 0, display: 'flex', alignItems: 'center', gap: 8 }}><FireOutlined style={{ color: '#ff4d4f' }} /> Xe điện nổi bật</Typography.Title></Col>
          <Col><Button type="link" onClick={()=> navigate('/listings?category=cars')}>Xem tất cả →</Button></Col>
        </Row>
        <Row gutter={[16,16]}>
          {loadingVeh && Array.from({ length: 4 }).map((_,i)=>(
            <Col xs={12} md={6} key={i}>
              <Card hoverable>
                <Skeleton.Image active style={{ width: '100%', height: 120, objectFit:'cover' }} />
                <Skeleton active title paragraph={{ rows: 2 }} style={{ marginTop: 12 }} />
              </Card>
            </Col>
          ))}
          {!loadingVeh && vehListings.length === 0 && (
            <Col span={24}><Empty description="Chưa có dữ liệu" /></Col>
          )}
          {!loadingVeh && vehListings.map(item => (
            <Col xs={12} md={6} key={item.id}>
              <Card
                hoverable
                size="small"
                cover={<div style={{ position:'relative', paddingTop:'60%', overflow:'hidden', background:'#f5f5f5' }}>
                  <img src={item.thumbnail} alt={item.title} style={{ position:'absolute', inset:0, width:'100%', height:'100%', objectFit:'cover' }} loading="lazy" />
                  {item.listingType === 'PREMIUM' && <Tag color="gold" style={{ position:'absolute', top:8, right:8 }}>VIP</Tag>}
                </div>}
                styles={{ body:{ padding:12 } }}
                onClick={()=> navigate(`/listings/${item.id}`)}
              >
                <Typography.Paragraph style={{ marginBottom:4, fontWeight:600 }} ellipsis={{ rows:2 }}>{item.title}</Typography.Paragraph>
                <Space size={[4,4]} wrap>
                  <Tag bordered={false} color="blue">{item.price ? item.price.toLocaleString('vi-VN') + ' ₫' : 'Liên hệ'}</Tag>
                  <Tag bordered={false}>ID #{item.id}</Tag>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      </div>

      {/* Trending Batteries Section */}
      <div style={{ maxWidth: 1300, margin: '0 auto', padding: '1rem 0 2.5rem' }}>
        <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
          <Col><Typography.Title level={3} style={{ margin: 0, display:'flex', alignItems:'center', gap:8 }}><StarOutlined style={{ color:'#faad14' }} /> Pin nổi bật</Typography.Title></Col>
          <Col><Button type="link" onClick={()=> navigate('/listings?category=pin')}>Xem tất cả →</Button></Col>
        </Row>
        <Row gutter={[16,16]}>
          {loadingBat && Array.from({ length: 4 }).map((_,i)=>(
            <Col xs={12} md={6} key={i}>
              <Card hoverable>
                <Skeleton.Image active style={{ width: '100%', height: 120 }} />
                <Skeleton active title paragraph={{ rows: 2 }} style={{ marginTop: 12 }} />
              </Card>
            </Col>
          ))}
          {!loadingBat && batListings.length === 0 && (
            <Col span={24}><Empty description="Chưa có dữ liệu" /></Col>
          )}
          {!loadingBat && batListings.map(item => (
            <Col xs={12} md={6} key={item.id}>
              <Card
                hoverable
                size="small"
                cover={<div style={{ position:'relative', paddingTop:'60%', overflow:'hidden', background:'#f5f5f5' }}>
                  <img src={item.thumbnail} alt={item.title} style={{ position:'absolute', inset:0, width:'100%', height:'100%', objectFit:'cover' }} loading="lazy" />
                  {item.listingType === 'PREMIUM' && <Tag color="gold" style={{ position:'absolute', top:8, right:8 }}>VIP</Tag>}
                </div>}
                styles={{ body:{ padding:12 } }}
                onClick={()=> navigate(`/listings/${item.id}`)}
              >
                <Typography.Paragraph style={{ marginBottom:4, fontWeight:600 }} ellipsis={{ rows:2 }}>{item.title}</Typography.Paragraph>
                <Space size={[4,4]} wrap>
                  <Tag bordered={false} color="green">{item.price ? item.price.toLocaleString('vi-VN') + ' ₫' : 'Liên hệ'}</Tag>
                  <Tag bordered={false}>ID #{item.id}</Tag>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      </div>

      {/* Newest Listings Section */}
      <div style={{ maxWidth: 1300, margin: '0 auto', padding: '0 0 3rem' }}>
        <Row justify="space-between" align="middle" style={{ marginBottom: 12 }}>
          <Col><Typography.Title level={3} style={{ margin: 0, display:'flex', alignItems:'center', gap:8 }}><ClockCircleOutlined /> Mới nhất</Typography.Title></Col>
          <Col>
            <Segmented
              size="small"
              value={viewMode}
              onChange={setViewMode}
              options={[{ label:'Lưới', value:'grid' }, { label:'Gọn', value:'compact' }]}
            />
          </Col>
        </Row>
        {loadingNewest && (
          <Row gutter={[16,16]}>
            {Array.from({ length: 6 }).map((_,i)=>(
              <Col xs={12} md={8} lg={6} key={i}>
                <Card hoverable>
                  <Skeleton.Image active style={{ width:'100%', height:130 }} />
                  <Skeleton active title paragraph={{ rows:2 }} style={{ marginTop:12 }} />
                </Card>
              </Col>
            ))}
          </Row>
        )}
        {!loadingNewest && newestListings.length === 0 && <Empty description="Không có dữ liệu" />}
        {!loadingNewest && newestListings.length > 0 && viewMode === 'grid' && (
          <Row gutter={[16,16]}>
            {newestListings.map(item => (
              <Col xs={12} md={8} lg={6} key={item.id}>
                <Card
                  hoverable
                  cover={<div style={{ position:'relative', paddingTop:'62%', background:'#f2f2f2' }}>
                    <img src={item.thumbnail} alt={item.title} style={{ position:'absolute', inset:0, width:'100%', height:'100%', objectFit:'cover' }} />
                  </div>}
                  styles={{ body:{ padding:12 } }}
                  size="small"
                  onClick={()=> navigate(`/listings/${item.id}`)}
                >
                  <Typography.Paragraph style={{ marginBottom:4, fontWeight:600 }} ellipsis={{ rows:2 }}>{item.title}</Typography.Paragraph>
                  <Space size={[4,4]} wrap>
                    <Tag bordered={false}>{item.price ? item.price.toLocaleString('vi-VN') + ' ₫' : 'Liên hệ'}</Tag>
                    {item.listingType === 'PREMIUM' && <Tag color="gold">PREMIUM</Tag>}
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>
        )}
        {!loadingNewest && newestListings.length > 0 && viewMode === 'compact' && (
          <Card size="small" bodyStyle={{ padding: 0 }}>
            {newestListings.map((item, idx) => (
              <div key={item.id} style={{ display:'flex', alignItems:'center', gap:12, padding:'10px 14px', borderBottom: idx===newestListings.length-1 ? 'none':'1px solid #f0f0f0', cursor:'pointer' }} onClick={()=> navigate(`/listings/${item.id}`)}>
                <div style={{ width:70, height:50, borderRadius:4, overflow:'hidden', background:'#fafafa' }}>
                  <img src={item.thumbnail} alt={item.title} style={{ width:'100%', height:'100%', objectFit:'cover' }} />
                </div>
                <div style={{ flex:1 }}>
                  <Typography.Text strong ellipsis style={{ display:'block', maxWidth:'100%' }}>{item.title}</Typography.Text>
                  <Typography.Text type="secondary" style={{ fontSize:12 }}>{item.price ? item.price.toLocaleString('vi-VN') + ' ₫' : 'Liên hệ'}</Typography.Text>
                </div>
                <Tooltip title="Xem chi tiết"><Button size="small" type="text" icon={<ProfileOutlined />} /></Tooltip>
              </div>
            ))}
          </Card>
        )}
      </div>

      <Divider style={{ margin:'0 auto 48px', maxWidth: 1100 }}>
        <Typography.Text type="secondary">Quy trình</Typography.Text>
      </Divider>

      {/* How It Works Timeline */}
      <div style={{ maxWidth: 1000, margin:'0 auto', padding: '0 0 3.2rem' }}>
        <Typography.Title level={3} style={{ textAlign:'center', marginBottom: 32 }}>Cách nền tảng vận hành</Typography.Title>
        <Timeline
          mode="alternate"
          items={[
            {
              children: <div><strong>1. Đăng ký / Đăng nhập</strong><br/><Typography.Text type="secondary">Tạo tài khoản để bắt đầu đăng bán hoặc lưu tin.</Typography.Text></div>,
              dot: <UserOutlined />
            },
            {
              color: 'green',
              children: <div><strong>2. Tạo listing</strong><br/><Typography.Text type="secondary">Mô tả sản phẩm, thêm ảnh, điền thông số chuẩn hóa.</Typography.Text></div>,
              dot: <CheckCircleTwoTone twoToneColor="#52c41a" />
            },
            {
              children: <div><strong>3. Người mua tìm kiếm</strong><br/><Typography.Text type="secondary">Bộ lọc nâng cao giúp tìm sản phẩm phù hợp nhanh.</Typography.Text></div>,
              dot: <ShoppingCartOutlined />
            },
            {
              color: 'blue',
              children: <div><strong>4. Liên hệ & giao dịch</strong><br/><Typography.Text type="secondary">Trao đổi trực tiếp và chốt giao dịch an toàn.</Typography.Text></div>,
              dot: <PhoneOutlined />
            }
          ]}
        />
      </div>

      {/* New Testimonial Section for Credibility */}
      <div style={{ padding: '3rem 0', maxWidth: 1200, margin:'0 auto' }}>
        <h2 className="text-center fw-bold mb-4" style={{ color: '#27407a' }}>Khách hàng nói gì?</h2>
        <div className="testimonial-carousel" style={{ maxWidth: 820, margin:'0 auto' }}>
          <Carousel autoplay dots={{ className:'custom-dots'}}>
            {[{ text: 'Dễ dàng tìm được xe điện chất lượng với giá tốt. Hỗ trợ tuyệt vời!', author:'Nguyễn Văn A, Hà Nội' }, { text:'Đăng bán xe điện cũ nhanh chóng, nhận được nhiều offer.', author:'Trần Thị B, TP.HCM' }, { text:'Nền tảng đáng tin cậy cho xe điện second-hand.', author:'Lê Văn C, Đà Nẵng' }].map((t,i)=>(
              <div key={i}>
                <div style={{ textAlign:'center', padding:'1.5rem 1rem' }}>
                  <CommentOutlined style={{ fontSize: 38, color:'#9aa8c6', marginBottom: 18 }} />
                  <p style={{ fontStyle:'italic', fontSize: '1.1rem', lineHeight:1.6 }}>{`"${t.text}"`}</p>
                  <p style={{ fontWeight:600, marginTop: 12 }}>{t.author}</p>
                </div>
              </div>
            ))}
          </Carousel>
        </div>
      </div>

      {/* Call To Action Band */}
      <div style={{ background: 'linear-gradient(90deg,#1d4ba8,#2563eb,#1d4ba8)', padding: '60px 16px', marginTop: 16 }}>
        <div style={{ maxWidth: 1100, margin: '0 auto', display:'flex', flexDirection:'column', alignItems:'center', textAlign:'center', gap: 20 }}>
          <Typography.Title level={2} style={{ color:'#fff', margin:0 }}>Sẵn sàng tiếp năng lượng cho giao dịch của bạn?</Typography.Title>
          <Typography.Text style={{ color:'rgba(255,255,255,0.85)', fontSize:16 }}>
            Đăng tin ngay hôm nay để tiếp cận cộng đồng người dùng xe & pin điện đang phát triển.
          </Typography.Text>
          <Space size="large" wrap>
            <Button size="large" type="primary" style={{ background:'#fee877', color:'#244581', border:'none', fontWeight:600 }} onClick={handlePostListingClick}>Đăng bán ngay</Button>
            <Button size="large" ghost onClick={()=> navigate('/listings?category=cars')}>Khám phá xe</Button>
            <Button size="large" ghost onClick={()=> navigate('/listings?category=pin')}>Khám phá pin</Button>
          </Space>
        </div>
      </div>

      {/* Updated Modal for Login Prompt */}
      <Modal
        open={showModal}
        onCancel={() => setShowModal(false)}
        centered
        footer={null}
        title={<div style={{ display:'flex', alignItems:'center', gap:8 }}><UserOutlined style={{ fontSize:22, color:'#416adcff' }} />Yêu Cầu Đăng Nhập</div>}
      >
        <p style={{ fontSize:'1.05rem', textAlign:'center', marginBottom:24 }}>Bạn cần đăng nhập để đăng bán sản phẩm.<br/>Nếu chưa có tài khoản, hãy đăng ký ngay!</p>
        <Space style={{ display:'flex', justifyContent:'center' }} size={12} wrap>
          <Button onClick={() => setShowModal(false)} shape="round">Đóng</Button>
          <Button type="primary" shape="round" onClick={()=> { setShowModal(false); navigate('/login'); }}>Đăng nhập</Button>
          <Button danger shape="round" onClick={()=> { setShowModal(false); navigate('/register'); }}>Đăng ký</Button>
        </Space>
      </Modal>

      <style>{`
        .custom-modal .modal-content {border-radius:22px;box-shadow:0 10px 36px -8px rgba(34,56,104,.3);} 
        /* Small responsive tweaks for new sections */
        @media (max-width: 575px) {
          .hero-content h1 { font-size: 2.2rem !important; }
        }
      `}</style>
    </>
  );
}

export default Home;
