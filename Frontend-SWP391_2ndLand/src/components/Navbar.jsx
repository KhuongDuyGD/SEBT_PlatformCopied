import { Link, useNavigate, useLocation } from "react-router-dom";
import { Navbar, Nav, Container, NavDropdown, Button } from "react-bootstrap";
import "./MegaMenu.css"; // Import custom CSS

function AppNavbar({ isLoggedIn, setIsLoggedIn }) {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    setIsLoggedIn(false);
    navigate("/");
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
          <span>EV Battery Hub</span>
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
                    <Link to="/products/xe-dien" className="dropdown-item">
                      Xe điện
                    </Link>
                  </li>
                  <li>
                    <Link to="/products/xe-may-dien" className="dropdown-item">
                      Xe máy điện
                    </Link>
                  </li>
                  <li>
                    <Link to="/products/xe-dap-dien" className="dropdown-item">
                      Xe đạp điện
                    </Link>
                  </li>
                </ul>
              </NavDropdown.Item>
              <NavDropdown.Item className="dropdown-submenu">
                <span className="submenu-title">Pin ▸</span>
                <ul className="submenu">
                  <li>
                    <Link to="/products/pin-xe" className="dropdown-item">
                      Pin xe điện
                    </Link>
                  </li>
                  <li>
                    <Link to="/products/pin-du-phong" className="dropdown-item">
                      Pin dự phòng
                    </Link>
                  </li>
                  <li>
                    <Link to="/products/pin-cu" className="dropdown-item">
                      Pin second-hand
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
                3
              </span>
            </Nav.Link>

            {isLoggedIn ? (
              <NavDropdown
                title={<span className="text-white fw-semibold">👤 Tài Khoản</span>}
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
                  onClick={handleLogout}
                  className="text-danger fw-semibold"
                >
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
    </Navbar>
  );
}

export default AppNavbar;