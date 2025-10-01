// src/pages/listings/ListingPage.jsx
import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import {
  Container,
  Row,
  Col,
  Form,
  Button,
  Card,
  Spinner,
} from "react-bootstrap";
import api from "../../api/axios";

function ListingPage() {
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const initialCategory = params.get("category") || "cars"; // default cars
  const initialType = params.get("type") || "";

  const [category, setCategory] = useState(initialCategory);
  const [type, setType] = useState(initialType);
  const [priceRange, setPriceRange] = useState([0, 100000000]); // 0-100 triệu default
  const [year, setYear] = useState("");
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchListings = async () => {
    setLoading(true);
    try {
      let endpoint = "/api/listings/all";
      if (category === "cars") endpoint = "/api/listings/cars";
      if (category === "pin") endpoint = "/api/listings/pins";

      const res = await api.get(endpoint);
      let data = res.data || [];

      // filter client-side demo
      if (year) data = data.filter((l) => l.productInfo?.year == year);
      if (priceRange)
        data = data.filter(
          (l) => l.price >= priceRange[0] && l.price <= priceRange[1]
        );
      if (type)
        data = data.filter((l) =>
          l.productInfo?.type?.toLowerCase().includes(type.toLowerCase())
        );

      setListings(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchListings();
  }, [category, type, year, priceRange]);

  return (
    <Container fluid className="py-4">
      <Row>
        {/* Sidebar Filter */}
        <Col md={3} className="mb-4">
          <Card className="p-3 shadow-sm">
            <h5 className="fw-bold mb-3">Bộ lọc</h5>
            <Form.Group className="mb-3">
              <Form.Label>Loại</Form.Label>
              <Form.Select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              >
                <option value="cars">Xe</option>
                <option value="pin">Pin</option>
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Năm sản xuất</Form.Label>
              <Form.Control
                type="number"
                placeholder="VD: 2022"
                value={year}
                onChange={(e) => setYear(e.target.value)}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Khoảng giá</Form.Label>
              <div className="d-flex gap-2">
                <Form.Control
                  type="number"
                  value={priceRange[0]}
                  onChange={(e) =>
                    setPriceRange([+e.target.value, priceRange[1]])
                  }
                />
                <Form.Control
                  type="number"
                  value={priceRange[1]}
                  onChange={(e) =>
                    setPriceRange([priceRange[0], +e.target.value])
                  }
                />
              </div>
            </Form.Group>
            <Button variant="primary" onClick={fetchListings}>
              Áp dụng
            </Button>
          </Card>
        </Col>

        {/* Listings */}
        <Col md={9}>
          {loading ? (
            <div className="text-center py-5">
              <Spinner animation="border" />
            </div>
          ) : (
            <Row className="g-4">
              {listings.map((item) => (
                <Col md={4} key={item.id}>
                  <Card className="h-100 shadow-sm">
                    <Card.Img
                      variant="top"
                      src={item.mainImage || "https://via.placeholder.com/300"}
                    />
                    <Card.Body>
                      <Card.Title>{item.title}</Card.Title>
                      <Card.Text>{item.price?.toLocaleString()} VND</Card.Text>
                      <Button
                        variant="outline-primary"
                        size="sm"
                        href={`/listings/${item.id}`}
                      >
                        Xem chi tiết
                      </Button>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          )}
        </Col>
      </Row>
    </Container>
  );
}

export default ListingPage;
