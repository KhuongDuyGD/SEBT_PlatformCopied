import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Card, Button, Form, Alert, Container, Row, Col, InputGroup } from "react-bootstrap";
import { useForm } from "react-hook-form";
import api from "../../api/axios";
import "../../css/Auth.css";

function Login({ setIsLoggedIn, setUserInfo }) {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const [showAlert, setShowAlert] = useState(false);
    const [alertMessage, setAlertMessage] = useState("");
    const [alertVariant, setAlertVariant] = useState("success");
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();

    const onSubmit = async (data) => {
        setIsLoading(true);
        try {
            const response = await api.post('/auth/login', data);
            setUserInfo(response.data);
            setAlertMessage("Đăng nhập thành công!");
            setAlertVariant("success");
            setShowAlert(true);
            setIsLoggedIn(true);
            // Remove delay - redirect immediately
            navigate('/');
        } catch (error) {
            setAlertMessage(error.response?.data?.message || "Đăng nhập thất bại");
            setAlertVariant("danger");
            setShowAlert(true);
        } finally {
            setIsLoading(false);
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
                                    <h2 className="fw-bold mb-2">Chào Mừng Trở Lại!</h2>
                                    <p className="text-muted fs-6">Đăng nhập để tiếp tục khám phá.</p>
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
                                        <Form.Label className="fw-semibold text-dark mb-2">Mật Khẩu</Form.Label>
                                        <InputGroup>
                                            <Form.Control
                                                type={showPassword ? "text" : "password"}
                                                {...register("password", { required: true, minLength: 6 })}
                                                placeholder="Nhập mật khẩu"
                                            />
                                            <Button variant="light" onClick={() => setShowPassword(!showPassword)}>
                                                {showPassword ? <i className="bi bi-eye-slash"></i> : <i className="bi bi-eye"></i>}
                                            </Button>
                                        </InputGroup>
                                        {errors.password && <p className="text-danger small">Mật khẩu phải ít nhất 6 ký tự</p>}
                                    </Form.Group>

                                    <Button 
                                        type="submit" 
                                        disabled={isLoading}
                                        className="w-100 fw-bold py-3 mb-3 auth-submit-btn"
                                    >
                                        {isLoading ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                                                Đang đăng nhập...
                                            </>
                                        ) : (
                                            "Đăng Nhập"
                                        )}
                                    </Button>

                                    <div className="text-center">
                                        <Link to="#" className="text-decoration-none p-0 auth-link">Quên mật khẩu?</Link>
                                    </div>
                                </Form>
                            </Card.Body>
                        </Card>

                        <div className="text-center mt-4">
                            <div className="p-3 rounded-3 auth-footer-card">
                <span className="text-dark fs-6">
                  Chưa có tài khoản?{" "}
                    <Link to="/register" className="fw-bold text-decoration-none auth-footer-link">
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