// src/pages/Home.jsx
import { Link } from "react-router-dom";
import { Button, Card, Row, Col, Container, Carousel } from "react-bootstrap";
import {
  BatteryCharging,
  Handshake,
  ShieldCheck,
  Tag,
  Quote,
} from "lucide-react";
import backgroundImage from "../assets/background.jpg";

function Home() {
  return (
    <>
      {/* Hero Section with Overlay and Animation */}
      <div
        className="hero-section position-relative d-flex align-items-center"
        style={{
          backgroundImage: `url(${backgroundImage})`,
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          minHeight: "70vh",
          color: "white",
        }}
      >
        <div
          className="position-absolute top-0 start-0 w-100 h-100"
          style={{ backgroundColor: "rgba(0, 0, 0, 0.4)" }} // Overlay for better text visibility
        ></div>
        <Container className="position-relative">
          <Row className="align-items-center">
            <Col md={6} className="text-start animate-fade-in">
              <h1
                className="display-3 fw-bold mb-4"
                style={{ color: "#fee877ff" }} // Change to yellow for highlight
              >
                FPT EV Secondhand Marketplace
              </h1>
              <p
                className="lead mb-4 fs-4 fw-semibold"
                style={{ color: "#ffffff" }}
              >
                Xe và Pin điện cũ – Kết nối giá trị, tiếp năng lượng xanh
              </p>
              <div className="d-flex gap-3 flex-wrap align-items-center">
                <Button
                  as={Link}
                  to="/post-listing"
                  variant="light"
                  size="lg"
                  className="px-5 py-3 fw-bold" // Larger padding for prominence
                  style={{
                    color: "#416adcff",
                    backgroundColor: "#fee877ff",
                    transition: "transform 0.3s ease, box-shadow 0.3s ease",
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.transform = "scale(1.05)";
                    e.target.style.boxShadow = "0 6px 12px rgba(0, 0, 0, 0.3)";
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.transform = "scale(1)";
                    e.target.style.boxShadow = "none";
                  }}
                >
                  Đăng bán
                </Button>
                <Button
                  as={Link}
                  to="/battery"
                  variant="outline-light"
                  size="lg"
                  className="px-4 py-3 fw-bold"
                  style={{
                    color: "#fee877ff",
                    borderColor: "#fee877ff",
                    borderWidth: "2px",
                    transition: "background-color 0.3s ease",
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.backgroundColor = "rgba(254, 232, 119, 0.2)";
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.backgroundColor = "transparent";
                  }}
                >
                  Tìm pin
                </Button>
                <Button
                  as={Link}
                  to="/cars"
                  variant="outline-light"
                  size="lg"
                  className="px-4 py-3 fw-bold"
                  style={{
                    color: "#fee877ff",
                    borderColor: "#fee877ff",
                    borderWidth: "2px",
                    transition: "background-color 0.3s ease",
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.backgroundColor = "rgba(254, 232, 119, 0.2)";
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.backgroundColor = "transparent";
                  }}
                >
                  Tìm xe
                </Button>
              </div>
            </Col>
            <Col md={6}></Col>
          </Row>
        </Container>
      </div>

      {/* Features Section */}
      <Container className="py-5">
        <h2 className="text-center mb-5 fw-bold" style={{ color: "#416adcff" }}>
          Tại Sao Chọn Chúng Tôi?
        </h2>
        <Row className="g-4 mb-5">
          <Col md={4}>
            <Card className="h-100 shadow-lg border-0 rounded-4 hover-card">
              <Card.Body className="text-center p-4">
                <div className="mb-3 text-success">
                  <BatteryCharging size={48} />
                </div>
                <h5 className="card-title fw-bold text-success mb-3">
                  Mua Pin Chất Lượng
                </h5>
                <p className="card-text text-muted mb-4">
                  Khám phá hàng ngàn pin EV đã qua kiểm định chất lượng với giá cả hợp lý.
                </p>
                <Button as={Link} to="/buy" variant="success" className="px-4 rounded-pill">
                  Xem Pin →
                </Button>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="h-100 shadow-lg border-0 rounded-4 hover-card">
              <Card.Body className="text-center p-4">
                <div className="mb-3 text-info">
                  <Tag size={48} />
                </div>
                <h5 className="card-title fw-bold text-info mb-3">
                  Bán Pin Dễ Dàng
                </h5>
                <p className="card-text text-muted mb-4">
                  Đăng bán pin của bạn với quy trình đơn giản và nhận được giá tốt nhất.
                </p>
                <Button as={Link} to="/sell" variant="info" className="px-4 rounded-pill">
                  Đăng Bán →
                </Button>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="h-100 shadow-lg border-0 rounded-4 hover-card">
              <Card.Body className="text-center p-4">
                <div className="mb-3 text-warning">
                  <Handshake size={48} />
                </div>
                <h5 className="card-title fw-bold text-warning mb-3">
                  Hỗ Trợ 24/7
                </h5>
                <p className="card-text text-muted mb-4">
                  Đội ngũ chuyên gia sẵn sàng hỗ trợ bạn mọi lúc với dịch vụ tận tâm.
                </p>
                <Button as={Link} to="/support" variant="warning" className="px-4 rounded-pill">
                  Liên Hệ →
                </Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      {/* Statistics Section with Gradient */}
      <div className="stats-section py-5" style={{ background: "linear-gradient(180deg, #f8f9fa, #ffffff)" }}>
        <Container>
          <Row className="text-center">
            <Col md={3}>
              <div className="mb-4">
                <ShieldCheck size={40} className="text-primary mb-2" />
                <h3 className="fw-bold">1,000+</h3>
                <p className="text-muted">Pin Đã Bán</p>
              </div>
            </Col>
            <Col md={3}>
              <div className="mb-4">
                <ShieldCheck size={40} className="text-success mb-2" />
                <h3 className="fw-bold">500+</h3>
                <p className="text-muted">Khách Hàng Hài Lòng</p>
              </div>
            </Col>
            <Col md={3}>
              <div className="mb-4">
                <ShieldCheck size={40} className="text-info mb-2" />
                <h3 className="fw-bold">99%</h3>
                <p className="text-muted">Tỷ Lệ Thành Công</p>
              </div>
            </Col>
            <Col md={3}>
              <div className="mb-4">
                <ShieldCheck size={40} className="text-warning mb-2" />
                <h3 className="fw-bold">24/7</h3>
                <p className="text-muted">Hỗ Trợ</p>
              </div>
            </Col>
          </Row>
        </Container>
      </div>

      {/* New Testimonial Section for Credibility */}
      <Container className="py-5">
        <h2 className="text-center mb-5 fw-bold" style={{ color: "#416adcff" }}>
          Khách Hàng Nói Gì Về Chúng Tôi?
        </h2>
        <Carousel variant="dark">
          <Carousel.Item>
            <div className="text-center p-4">
              <Quote size={32} className="text-secondary mb-3" />
              <p className="lead fst-italic">"Dễ dàng tìm được pin chất lượng với giá tốt. Hỗ trợ tuyệt vời!"</p>
              <p className="fw-bold">- Nguyễn Văn A, Hà Nội</p>
            </div>
          </Carousel.Item>
          <Carousel.Item>
            <div className="text-center p-4">
              <Quote size={32} className="text-secondary mb-3" />
              <p className="lead fst-italic">"Đăng bán xe điện cũ nhanh chóng, nhận được nhiều offer."</p>
              <p className="fw-bold">- Trần Thị B, TP.HCM</p>
            </div>
          </Carousel.Item>
          <Carousel.Item>
            <div className="text-center p-4">
              <Quote size={32} className="text-secondary mb-3" />
              <p className="lead fst-italic">"Nền tảng đáng tin cậy cho pin EV second-hand."</p>
              <p className="fw-bold">- Lê Văn C, Đà Nẵng</p>
            </div>
          </Carousel.Item>
        </Carousel>
      </Container>

      <style>{`
        .animate-fade-in {
          animation: fadeIn 1s ease-in-out;
        }
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: translateY(0); }
        }
        .hover-card {
          transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        .hover-card:hover {
          transform: translateY(-10px);
          box-shadow: 0 1rem 2rem rgba(0,0,0,0.15) !important;
        }
      `}</style>
    </>
  );
}

export default Home;