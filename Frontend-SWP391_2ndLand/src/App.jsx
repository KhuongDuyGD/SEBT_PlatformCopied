import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import AppNavbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import CarListings from "./pages/listings/CarListings";
import PinListings from "./pages/listings/PinListings";
import { useState, useEffect } from "react";
import { Container } from "react-bootstrap";
import "./App.css";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userInfo, setUserInfo] = useState(null);

  useEffect(() => {
    const savedLogin = localStorage.getItem("isLoggedIn");
    const savedUserInfo = localStorage.getItem("userInfo");
    if (savedLogin === "true") {
      setIsLoggedIn(true);
      if (savedUserInfo) {
        try {
          setUserInfo(JSON.parse(savedUserInfo));
        } catch (error) {
          console.error("Error parsing user info:", error);
        }
      }
    }
  }, []);

  useEffect(() => {
    localStorage.setItem("isLoggedIn", isLoggedIn);
    if (isLoggedIn && userInfo) {
      localStorage.setItem("userInfo", JSON.stringify(userInfo));
    } else {
      localStorage.removeItem("userInfo");
      setUserInfo(null);
    }
  }, [isLoggedIn, userInfo]);

  return (
    <Router>
      <AppNavbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/battery" element={<PinListings />} />
          <Route path="/cars" element={<CarListings />} />
          <Route
            path="/support"
            element={
              <Container className="py-5 text-center">
                <h2 className="fw-bold text-warning mb-4">Hỗ Trợ Khách Hàng</h2>
                <p className="text-muted">
                  Liên hệ: support@evbatteryhub.com
                </p>
              </Container>
            }
          />
          <Route
            path="/notifications"
            element={
              <Container className="py-5 text-center">
                <h2 className="fw-bold text-info mb-4">Thông Báo</h2>
                <p className="text-muted">Bạn chưa có thông báo mới.</p>
              </Container>
            }
          />
          <Route
            path="/account"
            element={
              <Container className="py-5 text-center">
                <h2 className="fw-bold text-secondary mb-4">Hồ Sơ Cá Nhân</h2>
                {userInfo ? (
                  <p>
                    Chào mừng: <strong>{userInfo.email}</strong>
                  </p>
                ) : (
                  <p className="text-muted">
                    Vui lòng đăng nhập để xem thông tin.
                  </p>
                )}
              </Container>
            }
          />
          <Route
            path="/orders"
            element={
              <Container className="py-5 text-center">
                <h2 className="fw-bold mb-4">Đơn Hàng Của Tôi</h2>
                <p className="text-muted">Tính năng đang được phát triển.</p>
              </Container>
            }
          />
          <Route
            path="/favorites"
            element={
              <Container className="py-5 text-center">
                <h2 className="fw-bold text-danger mb-4">
                  Danh Sách Yêu Thích
                </h2>
                <p className="text-muted">Tính năng đang được phát triển.</p>
              </Container>
            }
          />
          <Route
            path="/settings"
            element={
              <Container className="py-5 text-center">
                <h2 className="fw-bold mb-4">Cài Đặt</h2>
                <p className="text-muted">Tính năng đang được phát triển.</p>
              </Container>
            }
          />
          <Route
            path="/login"
            element={
              <Login setIsLoggedIn={setIsLoggedIn} setUserInfo={setUserInfo} />
            }
          />
          <Route
            path="/register"
            element={
              <Register
                setIsLoggedIn={setIsLoggedIn}
                setUserInfo={setUserInfo}
              />
            }
          />
        </Routes>
      </main>
      <footer className="footer text-center">
        <Container>
          <p className="mb-0">
            © 2024 EV Battery Hub - Nền tảng pin EV cũ hàng đầu Việt Nam
          </p>
        </Container>
      </footer>
    </Router>
  );
}

export default App;