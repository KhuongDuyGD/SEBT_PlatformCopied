import React from "react";
import {
  Container,
  Row,
  Col,
  Card,
  Badge,
  Form,
  Button,
} from "react-bootstrap";
import { MapPin, Gauge, MessageCircle, Tag } from "lucide-react";
import "./Listings.css";

function ListingPage({
  pageTitle,
  searchPlaceholder,
  brandPlaceholder,
  items,
  showStatusFilter,
}) {
  return (
    <Container fluid className="listing-page py-4">
      <Row>
        {/* Sidebar */}
        <Col md={3}>
          <Card className="filter-card shadow-sm">
            <Card.Body>
              <Card.Title className="fw-bold mb-3">Bộ lọc</Card.Title>
              <Form>
                <Form.Group className="mb-3">
                  <Form.Label>Tìm kiếm</Form.Label>
                  <Form.Control type="text" placeholder={searchPlaceholder} />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label>Hãng</Form.Label>
                  <Form.Control type="text" placeholder={brandPlaceholder} />
                </Form.Group>
                <Form.Group className="mb-3">
                  <Form.Label>Vị trí</Form.Label>
                  <Form.Control type="text" placeholder="Chọn vị trí" />
                </Form.Group>
                {showStatusFilter && (
                  <Form.Group className="mb-3">
                    <Form.Label>Trạng thái</Form.Label>
                    <Form.Check type="checkbox" label="Đã kiểm định" />
                    <Form.Check type="checkbox" label="Chưa kiểm định" />
                  </Form.Group>
                )}
                <Button variant="primary" type="submit" className="w-100">
                  Áp dụng
                </Button>
              </Form>
            </Card.Body>
          </Card>
        </Col>

        {/* Listings */}
        <Col md={9}>
          <h2 className="mb-4 fw-bold">{pageTitle}</h2>
          <Row>
            {items.map((item) => (
              <Col key={item.id} md={4} className="mb-4">
                <Card className="listing-card h-100 shadow-sm border-0">
                  <div className="position-relative">
                    <Card.Img
                      variant="top"
                      src={item.image}
                      className="listing-card-img"
                    />
                    <div className="card-img-overlay d-flex flex-column p-0">
                      <div className="mt-auto card-overlay-footer">
                        <span>
                          Phiên còn lại: <b>{item.left}</b>
                        </span>
                        <span>
                          Cao nhất: <b>{item.price}</b>
                        </span>
                      </div>
                    </div>
                  </div>
                  <Card.Body>
                    <Card.Title className="fw-bold text-primary">
                      {item.brand}
                    </Card.Title>
                    <div className="d-flex align-items-center text-muted mb-2">
                      <MapPin size={16} className="me-2" /> {item.location}
                      <Gauge size={16} className="ms-3 me-2" /> {item.km}
                    </div>
                    <div className="d-flex align-items-center text-muted">
                      <Tag size={16} className="me-2" />
                      {item.owner || "Chưa có"}
                      {item.comments > 0 && (
                        <div className="ms-auto d-flex align-items-center">
                          <MessageCircle size={16} className="me-1" />
                          <Badge pill bg="danger">
                            {item.comments}
                          </Badge>
                        </div>
                      )}
                    </div>
                    <Card.Text className="mt-2 text-muted fst-italic">
                      "{item.description}"
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Col>
      </Row>
    </Container>
  );
}

export default ListingPage;
