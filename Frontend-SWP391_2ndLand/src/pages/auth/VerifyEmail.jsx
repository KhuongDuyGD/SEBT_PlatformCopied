import { useState } from "react";
import { useNavigate } from "react-router-dom";
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
      // Lấy email từ localStorage (đã lưu khi register)
      const email = localStorage.getItem("registerEmail");

      if (!email) {
        setAlertMessage("Không tìm thấy email trong phiên đăng ký. Vui lòng đăng ký lại.");
        setAlertVariant("danger");
        setShowAlert(true);
        return;
      }

      const payload = { pins: data.pins, email };

      const response = await api.post("/auth/verify-email", payload);
      setAlertMessage(response.data.message || "Xác minh thành công!");
      setAlertVariant("success");
      setShowAlert(true);

      setTimeout(() => {
        navigate("/login"); // Sau khi verify thì về trang login
      }, 1500);
    } catch (error) {
      setAlertMessage(error.response?.data?.message || "Xác minh thất bại");
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
                  <h2 className="fw-bold mb-2">Xác Minh Email</h2>
                  <p className="text-muted fs-6">Nhập mã PIN đã được gửi đến email của bạn.</p>
                </div>

                {showAlert && (
                  <Alert
                    variant={alertVariant}
                    onClose={() => setShowAlert(false)}
                    dismissible
                    className="text-center"
                  >
                    {alertMessage}
                  </Alert>
                )}

                <Form onSubmit={handleSubmit(onSubmit)}>
                  <Form.Group className="mb-4">
                    <Form.Label className="fw-semibold text-dark mb-2">Mã PIN</Form.Label>
                    <Form.Control
                      type="text"
                      {...register("pins", { required: true })}
                      placeholder="Nhập mã PIN"
                    />
                    {errors.pins && <p className="text-danger small">Mã PIN là bắt buộc</p>}
                  </Form.Group>

                  <Button
                    type="submit"
                    className="w-100 fw-bold py-3 mb-3 auth-submit-btn"
                  >
                    Xác Minh
                  </Button>
                </Form>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default VerifyEmail;
