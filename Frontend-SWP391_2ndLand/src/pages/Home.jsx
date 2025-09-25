import { Link } from "react-router-dom";
import { Button, Card, Row, Col, Container } from "react-bootstrap";
import {
  BatteryCharging,
  Handshake,
  ShieldCheck,
  Tag,
} from "lucide-react";
import backgroundImage from "../assets/background.jpg";

function Home() {
  return (
    <>
      {/* Hero Section */}
      <div
        className="hero-section d-flex align-items-center"
        style={{
          backgroundImage: `url(${backgroundImage})`,
          backgroundSize: "cover",
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
          minHeight: "60vh",
          color: "white",
        }}
      >
        <Container>
          <Row className="align-items-center">
            <Col md={6} className="text-start">
              <h1
                className="display-4 fw-bold mb-4"
                style={{ color: "#416adcff" }}
              >
                FPT EV Secondhand Marketplace
              </h1>

              <p
                className="lead mb-4 fs-5 fw-bold"
                style={{ color: "#000000ff" }}
              >
                Xe và Pin điện cũ – Kết nối giá trị, tiếp năng lượng
              </p>
              <div className="d-flex gap-3 flex-wrap">
                <Button
                  as={Link}
                  to="/battery"
                  variant="light"
                  size="lg"
                  className="px-4 py-2 fw-semibold"
                  style={{ color: "#416adcff", backgroundColor: "#fee877ff" }}
                >
                  Tìm Pin ngay
                </Button>
                <Button
                  as={Link}
                  to="/cars"
                  variant="outline-light"
                  size="lg"
                  className="px-4 py-2 fw-semibold"
                  style={{
                    color: "#416adcff",
                    borderColor: "#416adcff",
                    borderWidth: "1.5px",
                  }}
                >
                  Tìm xe
                </Button>
                <Button
                  as={Link}
                  to="/post-listing"
                  variant="outline-light"
                  size="lg"
                  className="px-4 py-2 fw-semibold"
                  style={{
                    color: "#416adcff",
                    borderColor: "#416adcff",
                    borderWidth: "1.5px",
                  }}
                >
                  Đăng bài
                </Button>
              </div>
            </Col>
            <Col md={6}></Col>
          </Row>
        </Container>
      </div>

      {/* Features Section */}
      <Container className="py-5">
        <h2 className="text-center mb-5 fw-bold text-primary">
          Tại Sao Chọn Chúng Tôi?
        </h2>

        <Row className="g-4 mb-5">
          <Col md={4}>
            <Card className="h-100 shadow-lg border-0 hover-card">
              <Card.Body className="text-center p-4">
                <div className="mb-3 text-success">
                  <BatteryCharging size={48} />
                </div>
                <h5 className="card-title fw-bold text-success mb-3">
                  Mua Pin Chất Lượng
                </h5>
                <p className="card-text text-muted mb-4">
                  Khám phá hàng ngàn pin EV đã qua kiểm định chất lượng với giá
                  cả hợp lý.
                </p>
                <Button as={Link} to="/buy" variant="success" className="px-4">
                  Xem Pin →
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="h-100 shadow-lg border-0 hover-card">
              <Card.Body className="text-center p-4">
                <div className="mb-3 text-info">
                  <Tag size={48} />
                </div>
                <h5 className="card-title fw-bold text-info mb-3">
                  Bán Pin Dễ Dàng
                </h5>
                <p className="card-text text-muted mb-4">
                  Đăng bán pin của bạn với quy trình đơn giản và nhận được giá
                  tốt nhất.
                </p>
                <Button as={Link} to="/sell" variant="info" className="px-4">
                  Đăng Bán →
                </Button>
              </Card.Body>
            </Card>
          </Col>

          <Col md={4}>
            <Card className="h-100 shadow-lg border-0 hover-card">
              <Card.Body className="text-center p-4">
                <div className="mb-3 text-warning">
                  <Handshake size={48} />
                </div>
                <h5 className="card-title fw-bold text-warning mb-3">
                  Hỗ Trợ 24/7
                </h5>
                <p className="card-text text-muted mb-4">
                  Đội ngũ chuyên gia sẵn sàng hỗ trợ bạn mọi lúc với dịch vụ tận
                  tâm.
                </p>
                <Button
                  as={Link}
                  to="/support"
                  variant="warning"
                  className="px-4"
                >
                  Liên Hệ →
                </Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Statistics Section */}
        <div className="bg-light p-5 rounded-3 text-center mb-5">
          <Row>
            <Col md={3}>
              <div className="mb-3 text-primary">
                <ShieldCheck size={40} />
                <h3 className="fw-bold mt-2">1,000+</h3>
                <p className="text-muted">Pin Đã Bán</p>
              </div>
            </Col>
            <Col md={3}>
              <div className="mb-3 text-success">
                <ShieldCheck size={40} />
                <h3 className="fw-bold mt-2">500+</h3>
                <p className="text-muted">Khách Hàng Hài Lòng</p>
              </div>
            </Col>
            <Col md={3}>
              <div className="mb-3 text-info">
                <ShieldCheck size={40} />
                <h3 className="fw-bold mt-2">99%</h3>
                <p className="text-muted">Tỷ Lệ Thành Công</p>
              </div>
            </Col>
            <Col md={3}>
              <div className="mb-3 text-warning">
                <ShieldCheck size={40} />
                <h3 className="fw-bold mt-2">24/7</h3>
                <p className="text-muted">Hỗ Trợ</p>
              </div>
            </Col>
          </Row>
        </div>
      </Container>

      <style>{`
        .hover-card {
          transition: transform 0.3s ease, box-shadow 0.3s ease;
        }
        .hover-card:hover {
          transform: translateY(-10px);
          box-shadow: 0 1rem 1.5rem rgba(0,0,0,0.1) !important;
        }
      `}</style>
    </>
  );
}

export default Home;