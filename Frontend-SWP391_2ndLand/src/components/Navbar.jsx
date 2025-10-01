// src/components/Navbar.jsx

import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import {
  Navbar,
  Nav,
  Container,
  NavDropdown,
  Button,
  Modal,
  Spinner,
  Toast,
  ToastContainer,
} from "react-bootstrap";
import api from "../api/axios"; // Import api (adjust path if needed, ví dụ: "../../api/axios")
import "./MegaMenu.css"; // Import custom CSS

function AppNavbar({ isLoggedIn, setIsLoggedIn, setUserInfo }) {
  // Thêm setUserInfo vào props
  const navigate = useNavigate();
  const location = useLocation();

  // State để quản lý modal logout
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState("");

  // Xử lý khi người dùng click nút đăng xuất
  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  // Xử lý xác nhận đăng xuất
  const confirmLogout = async () => {
    setIsLoggingOut(true);

    try {
      // Gọi API đăng xuất
      await api.post("/auth/logout");

      // Xóa tất cả dữ liệu session và storage
      sessionStorage.clear();
      localStorage.removeItem("isLoggedIn");
      localStorage.removeItem("userInfo");
      localStorage.removeItem("registerEmail");
      localStorage.removeItem("tempPassword");

      // Cập nhật state
      setIsLoggedIn(false);
      setUserInfo(null);

      // Đóng modal và hiển thị thông báo thành công
      setShowLogoutModal(false);
      setToastMessage("Đăng xuất thành công! Hẹn gặp lại bạn.");
      setShowToast(true);

      // Chuyển về trang chủ sau khi hiển thị toast
      setTimeout(() => {
        navigate("/");
      }, 500);
    } catch (error) {
      console.error("Logout failed", error);
      // Vẫn thực hiện logout phía frontend nếu API lỗi
      sessionStorage.clear();
      localStorage.removeItem("isLoggedIn");
      localStorage.removeItem("userInfo");
      localStorage.removeItem("registerEmail");
      localStorage.removeItem("tempPassword");

      setIsLoggedIn(false);
      setUserInfo(null);
      setShowLogoutModal(false);

      // Hiển thị toast thông báo (ngay cả khi API lỗi)
      setToastMessage("Đã đăng xuất khỏi tài khoản.");
      setShowToast(true);

      setTimeout(() => {
        navigate("/");
      }, 500);
    } finally {
      setIsLoggingOut(false);
    }
  };

  // Hủy đăng xuất
  const cancelLogout = () => {
    setShowLogoutModal(false);
  };

  const isActiveLink = (path) => location.pathname === path;

  return (
    <Navbar
      expand="lg"
      className="shadow-sm sticky-top app-navbar"
      variant="dark"
    >
      <Container>
        <Navbar.Brand as={Link} to="/" className="fw-bold fs-3 text-white">
          <span>EV Secondhand Marketplace</span>
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ms-auto align-items-center">
            <Nav.Link
              as={Link}
              to="/"
              className={`fw-semibold mx-2 nav-link-custom ${
                isActiveLink("/") ? "active" : ""
              }`}
            >
              Trang Chủ
            </Nav.Link>

            <NavDropdown
              title={<span className="fw-semibold text-white">Danh mục</span>}
              id="buy-dropdown"
              className="mx-2 multi-level-dropdown"
            >
              <NavDropdown.Item className="dropdown-submenu">
                <span className="submenu-title">Xe ▸</span>
                <ul className="submenu">
                  <li>
                    <Link
                      to="/listings?category=xe-may-dien"
                      className="dropdown-item"
                    >
                      Xe máy điện
                    </Link>
                  </li>
                  <li>
                    <Link
                      to="/listings?category=xe-dap-dien"
                      className="dropdown-item"
                    >
                      Xe đạp điện
                    </Link>
                  </li>
                  <li>
                    <Link
                      to="/listings?category=o-to-dien"
                      className="dropdown-item"
                    >
                      Ô tô điện
                    </Link>
                  </li>
                </ul>
              </NavDropdown.Item>

              <NavDropdown.Item className="dropdown-submenu">
                <span className="submenu-title">Pin ▸</span>
                <ul className="submenu">
                  <li>
                    <Link
                      to="/listings?category=pin&type=xe-may"
                      className="dropdown-item"
                    >
                      Pin xe máy
                    </Link>
                  </li>
                  <li>
                    <Link
                      to="/listings?category=pin&type=xe-dap"
                      className="dropdown-item"
                    >
                      Pin xe đạp
                    </Link>
                  </li>
                  <li>
                    <Link
                      to="/listings?category=pin&type=o-to"
                      className="dropdown-item"
                    >
                      Pin ô tô
                    </Link>
                  </li>
                </ul>
              </NavDropdown.Item>
            </NavDropdown>

            <Nav.Link
              as={Link}
              to="/support"
              className={`fw-semibold mx-2 nav-link-custom ${
                isActiveLink("/support") ? "active" : ""
              }`}
            >
              Hỗ Trợ
            </Nav.Link>

            <Nav.Link
              as={Link}
              to="/notifications"
              className={`fw-semibold mx-2 position-relative nav-link-custom ${
                isActiveLink("/notifications") ? "active" : ""
              }`}
            >
              Thông Báo
              <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger notification-badge">
                3{" "}
                {/* Sau này: replace bằng state fetch từ API /notifications/count */}
              </span>
            </Nav.Link>

            {isLoggedIn ? (
              <NavDropdown
                title={
                  <span className="text-white fw-semibold">👤 Tài Khoản</span>
                }
                id="basic-nav-dropdown"
                className="mx-2"
                menuVariant="light"
              >
                <NavDropdown.Item as={Link} to="/account">
                  Hồ Sơ Cá Nhân
                </NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/orders">
                  Đơn Hàng Của Tôi
                </NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/favorites">
                  Yêu Thích
                </NavDropdown.Item>
                <NavDropdown.Item as={Link} to="/settings">
                  Cài Đặt
                </NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item
                  onClick={handleLogoutClick}
                  className="text-danger fw-semibold"
                >
                  <i className="fas fa-sign-out-alt me-2"></i>
                  Đăng Xuất
                </NavDropdown.Item>
              </NavDropdown>
            ) : (
              <Button
                as={Link}
                to="/login"
                variant="light"
                className="fw-bold ms-3 px-4 login-button"
              >
                Đăng Nhập
              </Button>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>

      {/* Modal xác nhận đăng xuất */}
      <Modal
        show={showLogoutModal}
        onHide={cancelLogout}
        centered
        backdrop="static"
        keyboard={false}
      >
        <Modal.Header className="border-0 pb-2">
          <Modal.Title className="w-100 text-center">
            <i
              className="fas fa-sign-out-alt text-warning me-2"
              style={{ fontSize: "1.5rem" }}
            ></i>
            <span className="fw-bold">Xác nhận đăng xuất</span>
          </Modal.Title>
        </Modal.Header>

        <Modal.Body className="text-center py-4">
          <div className="mb-3">
            <i
              className="fas fa-question-circle text-primary"
              style={{ fontSize: "3rem" }}
            ></i>
          </div>
          <h5 className="mb-3">Bạn có chắc chắn muốn đăng xuất?</h5>
          <p className="text-muted mb-0">
            Bạn sẽ cần đăng nhập lại để sử dụng các tính năng của EV Secondhand
            Marketplace.
          </p>
        </Modal.Body>

        <Modal.Footer className="border-0 pt-0 justify-content-center">
          <Button
            variant="outline-secondary"
            onClick={cancelLogout}
            disabled={isLoggingOut}
            className="px-4 py-2 fw-semibold"
          >
            <i className="fas fa-times me-2"></i>
            Hủy
          </Button>
          <Button
            variant="danger"
            onClick={confirmLogout}
            disabled={isLoggingOut}
            className="px-4 py-2 fw-semibold ms-2"
          >
            {isLoggingOut ? (
              <>
                <Spinner size="sm" className="me-2" />
                Đang đăng xuất...
              </>
            ) : (
              <>
                <i className="fas fa-sign-out-alt me-2"></i>
                Đăng xuất
              </>
            )}
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Toast notification cho logout */}
      <ToastContainer position="top-end" className="p-3">
        <Toast
          show={showToast}
          onClose={() => setShowToast(false)}
          delay={3000}
          autohide
          bg="success"
        >
          <Toast.Header>
            <i className="fas fa-check-circle text-success me-2"></i>
            <strong className="me-auto">EV Secondhand Marketplace</strong>
          </Toast.Header>
          <Toast.Body className="text-white">{toastMessage}</Toast.Body>
        </Toast>
      </ToastContainer>
    </Navbar>
  );
}

export default AppNavbar;
