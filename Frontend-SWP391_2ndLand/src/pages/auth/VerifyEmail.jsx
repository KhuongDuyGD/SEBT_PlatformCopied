import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Card, Button, Alert, Container, Row, Col } from "react-bootstrap";
import api from "../../api/axios";
import "./Auth.css";

function VerifyEmail({ setIsLoggedIn, setUserInfo }) {
  const [otp, setOtp] = useState(["", "", "", "", "", ""]);
  const [showAlert, setShowAlert] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");
  const [alertVariant, setAlertVariant] = useState("success");
  const [isLoading, setIsLoading] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);
  const navigate = useNavigate();
  const inputRefs = useRef([]);

  // Auto-focus first input on mount
  useEffect(() => {
    inputRefs.current[0]?.focus();
  }, []);

  // Cooldown timer effect
  useEffect(() => {
    if (resendCooldown > 0) {
      const timer = setTimeout(() => {
        setResendCooldown(resendCooldown - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [resendCooldown]);

  // Handle OTP input change
  const handleOtpChange = (index, value) => {
    // Only allow numeric characters
    if (!/^\d*$/.test(value) || value.length > 1) return;
    
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    // Auto focus to next input
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  // Handle backspace
  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  // Handle paste
  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const newOtp = [...otp];
    
    for (let i = 0; i < pastedData.length && i < 6; i++) {
      newOtp[i] = pastedData[i];
    }
    setOtp(newOtp);
    
    // Focus on the next empty input or last input
    const nextIndex = Math.min(pastedData.length, 5);
    inputRefs.current[nextIndex]?.focus();
  };

  const handleSubmit = async () => {
    const otpString = otp.join('');
    if (otpString.length !== 6) {
      setAlertMessage("Vui lòng nhập đầy đủ 6 số của mã OTP");
      setAlertVariant("danger");
      setShowAlert(true);
      return;
    }

    setIsLoading(true);
    try {
      const email = localStorage.getItem("registerEmail");

      if (!email) {
        setAlertMessage("Không tìm thấy email trong phiên đăng ký. Vui lòng đăng ký lại.");
        setAlertVariant("danger");
        setShowAlert(true);
        setIsLoading(false);
        return;
      }

      const payload = { pins: otpString, email };

      const response = await api.post("/auth/verify-email", payload);
      
      // Sau khi xác minh thành công, tự động đăng nhập luôn
      if (response.data && response.data.success) {
        // Thực hiện auto-login với thông tin từ response
        try {
          // Lấy password từ localStorage nếu có (lưu tạm trong quá trình register)
          const savedPassword = localStorage.getItem("tempPassword");
          if (savedPassword) {
            // Tự động đăng nhập
            const loginResponse = await api.post('/auth/login', {
              email: email,
              password: savedPassword
            });
            
            // Lưu thông tin user từ login response
            if (loginResponse.data) {
              // Cập nhật state trong App.jsx (quan trọng để AuthContext biết user đã đăng nhập)
              if (setIsLoggedIn && setUserInfo) {
                setIsLoggedIn(true);
                setUserInfo(loginResponse.data);
              }
              
              // Lưu thông tin user vào localStorage (backup)
              localStorage.setItem("userInfo", JSON.stringify(loginResponse.data));
              localStorage.setItem("isLoggedIn", "true");
              
              console.log("Auto-login thành công:", loginResponse.data);
            }
            
            // Xóa thông tin tạm thời
            localStorage.removeItem("registerEmail");
            localStorage.removeItem("tempPassword");
            
            setAlertMessage("Xác minh và đăng nhập thành công! Chào mừng bạn đến với EV Secondhand Marketplace!");
            setAlertVariant("success");
            setShowAlert(true);
            setIsLoading(false);
            
            // Chuyển thẳng vào trang chủ với trạng thái đã đăng nhập
            navigate("/");
          } else {
            // Fallback: chuyển về login nếu không có password
            setAlertMessage(response.data.message || "Xác minh thành công! Vui lòng đăng nhập.");
            setAlertVariant("success");
            setShowAlert(true);
            setIsLoading(false);
            navigate("/login");
          }
        } catch (loginError) {
          // Log chi tiết lỗi auto-login để debug
          console.error("Auto-login thất bại:", loginError);
          
          // Hiển thị thông báo cụ thể dựa trên lỗi
          const errorMessage = loginError.response?.data?.message || "Không thể tự động đăng nhập";
          setAlertMessage(`Xác minh thành công! ${errorMessage}. Vui lòng đăng nhập thủ công.`);
          setAlertVariant("warning"); // Dùng warning thay vì success để người dùng chú ý
          setShowAlert(true);
          setIsLoading(false);
          
          // Xóa thông tin tạm thời dù auto-login thất bại
          localStorage.removeItem("registerEmail");
          localStorage.removeItem("tempPassword");
          
          navigate("/login");
        }
      } else {
        // Xử lý trường hợp response không có success flag
        setAlertMessage(response.data.message || "Xác minh thành công! Vui lòng đăng nhập.");
        setAlertVariant("success");
        setShowAlert(true);
        setIsLoading(false);
        navigate("/login");
      }
    } catch (error) {
      setAlertMessage(error.response?.data?.message || "Mã OTP không chính xác hoặc đã hết hạn");
      setAlertVariant("danger");
      setShowAlert(true);
      // Clear OTP on error
      setOtp(["", "", "", "", "", ""]);
      inputRefs.current[0]?.focus();
      setIsLoading(false);
    }
  };

  const handleResendOtp = async () => {
    if (resendCooldown > 0) return;

    setIsLoading(true);
    try {
      const email = localStorage.getItem("registerEmail");
      if (!email) {
        setAlertMessage("Không tìm thấy email. Vui lòng đăng ký lại.");
        setAlertVariant("danger");
        setShowAlert(true);
        setIsLoading(false);
        return;
      }

      await api.post("/auth/resend-otp", { email });
      setAlertMessage("Mã OTP mới đã được gửi đến email của bạn");
      setAlertVariant("success");
      setShowAlert(true);
      setResendCooldown(60); // 60 seconds cooldown
      setOtp(["", "", "", "", "", ""]);
      inputRefs.current[0]?.focus();
    } catch (error) {
      setAlertMessage(error.response?.data?.message || "Không thể gửi lại mã OTP. Vui lòng thử lại sau.");
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
          <Col md={8} lg={6}>
            <Card className="auth-card shadow-lg border-0">
              <Card.Body>
                <div className="text-center mb-4 auth-header">
                  <div className="verify-icon mb-3">
                    <i className="fas fa-envelope-open-text text-primary" style={{fontSize: '3rem'}}></i>
                  </div>
                  <h2 className="fw-bold mb-2">Xác Minh Email</h2>
                  <p className="text-muted fs-6 mb-1">
                    Chúng tôi đã gửi mã OTP gồm 6 số đến email
                  </p>
                  <p className="text-primary fw-semibold">
                    {localStorage.getItem("registerEmail")}
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

                <div className="otp-container mb-4">
                  <label className="fw-semibold text-dark mb-3 d-block text-center">
                    Nhập mã OTP
                  </label>
                  <div className="otp-inputs d-flex justify-content-center gap-2 mb-3">
                    {otp.map((digit, index) => (
                      <input
                        key={index}
                        ref={(el) => (inputRefs.current[index] = el)}
                        type="text"
                        maxLength="1"
                        value={digit}
                        onChange={(e) => handleOtpChange(index, e.target.value)}
                        onKeyDown={(e) => handleKeyDown(index, e)}
                        onPaste={handlePaste}
                        className="otp-input text-center"
                        disabled={isLoading}
                      />
                    ))}
                  </div>
                </div>

                <Button
                  onClick={handleSubmit}
                  disabled={isLoading || otp.join('').length !== 6}
                  className="w-100 fw-bold py-3 mb-3 auth-submit-btn"
                >
                  {isLoading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      Đang xác minh...
                    </>
                  ) : (
                    "Xác Minh"
                  )}
                </Button>

                <div className="text-center">
                  <p className="text-muted small mb-2">Không nhận được mã?</p>
                  <Button
                    variant="link"
                    onClick={handleResendOtp}
                    disabled={resendCooldown > 0 || isLoading}
                    className="p-0 text-decoration-none auth-link"
                  >
                    {resendCooldown > 0 
                      ? `Gửi lại sau ${resendCooldown}s` 
                      : isLoading 
                        ? "Đang gửi..."
                        : "Gửi lại mã OTP"
                    }
                  </Button>
                </div>

                <div className="text-center mt-4">
                  <Button
                    variant="link"
                    onClick={() => navigate("/register")}
                    className="p-0 text-decoration-none auth-link"
                  >
                    ← Quay lại đăng ký
                  </Button>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default VerifyEmail;
