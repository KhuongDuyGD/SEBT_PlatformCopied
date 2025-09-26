import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Card, Button, Form, Alert, Container, Row, Col } from "react-bootstrap";
import { useForm } from "react-hook-form";
import api from "../../api/axios";
import "./Auth.css";

function VerifyEmail({ setIsLoggedIn, setUserInfo }) {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const [showAlert, setShowAlert] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");
  const [alertVariant, setAlertVariant] = useState("success");
  const navigate = useNavigate();

  const onSubmit = async (data) => {
    try {
      const response = await api.post('/auth/verify-email', { email: data.email, pins: data.pins });
      setAlertMessage(response.data.message || "Email verified successfully!");
      setAlertVariant("success");
      setShowAlert(true);
      // Sau verify, có thể login auto hoặc redirect login
      setTimeout(async () => {
        const currentUser = await api.get('/auth/current-user');
        setUserInfo(currentUser.data);
        setIsLoggedIn(true);
        navigate('/');
      }, 2000);
    } catch (error) {
      setAlertMessage(error.response?.data?.message || "Verification failed");
      setAlertVariant("danger");
      setShowAlert(true);
    }
  };

  return (
    <div className="auth-page d-flex align-items-center py-5">
      <Container>
        <Row className="justify-content-center">
          <Col md={7} lg={6}>
            <Card className="auth-card shadow-lg border-0">
              <Card.Body>
                <div className="text-center mb-4 auth-header">
                  <h2 className="fw-bold mb-2">Xác Thực Email</h2>
                  <p className="text-muted fs-6">Nhập PIN từ email để hoàn tất đăng ký.</p>
                </div>

                {showAlert && (
                  <Alert variant={alertVariant} onClose={() => setShowAlert(false)} dismissible className="text-center">
                    {alertMessage}
                  </Alert>
                )}

                <Form onSubmit={handleSubmit(onSubmit)}>
                  <Form.Group className="mb-4">
                    <Form.Label className="fw-semibold text-dark mb-2">Địa Chỉ Email</Form.Label>
                    <Form.Control
                      type="email"
                      {...register("email", { required: true, pattern: /^\S+@\S+$/i })}
                      placeholder="example@email.com"
                    />
                    {errors.email && <p className="text-danger small">Email không hợp lệ</p>}
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label className="fw-semibold text-dark mb-2">PIN (6 chữ số)</Form.Label>
                    <Form.Control
                      type="text"
                      {...register("pins", { required: true, minLength: 6, maxLength: 6 })}
                      placeholder="Nhập PIN từ email"
                    />
                    {errors.pins && <p className="text-danger small">PIN phải là 6 chữ số</p>}
                  </Form.Group>

                  <Button type="submit" className="w-100 fw-bold py-3 mb-3 auth-submit-btn">
                    Xác Thực
                  </Button>
                </Form>
              </Card.Body>
            </Card>

            <div className="text-center mt-4">
              <div className="p-3 rounded-3 auth-footer-card">
                <span className="text-dark fs-6">
                  Chưa nhận được PIN?{" "}
                  <Link to="/register" className="fw-bold text-decoration-none auth-footer-link">
                    Gửi lại
                  </Link>
                </span>
              </div>
            </div>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default VerifyEmail;