import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import {
  Card,
  Button,
  Form,
  Alert,
  Container,
  Row,
  Col,
  InputGroup,
} from "react-bootstrap";
import "./Auth.css"; // Import shared auth styles

function Login({ setIsLoggedIn, setUserInfo }) {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [showAlert, setShowAlert] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");
  const [alertVariant, setAlertVariant] = useState("success");
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!isValidEmail(formData.email)) {
      setAlertMessage("Vui lòng nhập địa chỉ email hợp lệ!");
      setAlertVariant("danger");
      setShowAlert(true);
      return;
    }
    if (formData.password.length < 6) {
      setAlertMessage("Mật khẩu phải có ít nhất 6 ký tự!");
      setAlertVariant("danger");
      setShowAlert(true);
      return;
    }
    setAlertMessage("Đăng nhập thành công!");
    setAlertVariant("success");
    setShowAlert(true);

    const userInfo = {
      email: formData.email,
      fullName: "Người dùng",
      loginTime: new Date().toISOString(),
    };

    setTimeout(() => {
      setIsLoggedIn(true);
      setUserInfo(userInfo);
      navigate("/");
    }, 1500);
  };

  return (
    <div className="auth-page d-flex align-items-center py-5">
      <Container>
        <Row className="justify-content-center">
          <Col md={7} lg={6}>
            <Card className="auth-card shadow-lg border-0">
              <Card.Body>
                <div className="text-center mb-4 auth-header">
                  <h2 className="fw-bold mb-2">Chào Mừng Trở Lại!</h2>
                  <p className="text-muted fs-6">
                    Đăng nhập để tiếp tục khám phá.
                  </p>
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

                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-4">
                    <Form.Label className="fw-semibold text-dark mb-2">
                      Địa Chỉ Email
                    </Form.Label>
                    <Form.Control
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      placeholder="example@email.com"
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label className="fw-semibold text-dark mb-2">
                      Mật Khẩu
                    </Form.Label>
                    <InputGroup>
                      <Form.Control
                        type={showPassword ? "text" : "password"}
                        name="password"
                        value={formData.password}
                        onChange={handleInputChange}
                        placeholder="Nhập mật khẩu"
                        required
                      />
                      <Button
                        variant="light"
                        onClick={() => setShowPassword(!showPassword)}
                      >
                        {showPassword ? (
                          <i className="bi bi-eye-slash"></i>
                        ) : (
                          <i className="bi bi-eye"></i>
                        )}
                      </Button>
                    </InputGroup>
                  </Form.Group>

                  <Button type="submit" className="w-100 fw-bold py-3 mb-3 auth-submit-btn">
                    Đăng Nhập
                  </Button>

                  <div className="text-center">
                    <Link to="#" className="text-decoration-none p-0 auth-link">
                      Quên mật khẩu?
                    </Link>
                  </div>
                </Form>
              </Card.Body>
            </Card>

            <div className="text-center mt-4">
              <div className="p-3 rounded-3 auth-footer-card">
                <span className="text-dark fs-6">
                  Chưa có tài khoản?{" "}
                  <Link
                    to="/register"
                    className="fw-bold text-decoration-none auth-footer-link"
                  >
                    Đăng ký ngay
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

export default Login;