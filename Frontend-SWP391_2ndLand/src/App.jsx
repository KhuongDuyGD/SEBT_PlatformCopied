import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useLocation
} from "react-router-dom";
import { useState, useEffect, useCallback } from "react";
import { Container, Spinner } from "react-bootstrap";

import AppNavbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import VerifyEmail from "./pages/auth/VerifyEmail";
import ForgotPassword from "./pages/auth/ForgotPassword";
import Support from "./pages/support/Support";
import Profile from "./pages/profile/Profile";
import CreateListing from "./pages/listings/CreateListing";
import ListingPage from "./pages/listings/ListingPage";
import MyListings from "./pages/listings/MyListings";
import AdvancedSearchPage from "./pages/listings/AdvancedSearchPage";
import SearchResults from "./pages/listings/SearchResults";
import ListingDetail from "./pages/listings/ListingDetail";

//============================= ADMIN PAGES =============================
import AdminDashboard from "./AdminDashboard";          // đảm bảo file này tồn tại
import AdminLayout from "./components/AdminLayout";     // đảm bảo đúng đường dẫn
import PendingListings from "./pages/admin/PendingListings"; // Trang xét duyệt listing
//============================= ADMIN PAGES =============================

import api from "./api/axios";
import { AuthContext } from "./contexts/AuthContext";
import "./App.css";
import "./css/theme.css";

// Placeholder cho các trang chưa làm
const Placeholder = ({ title }) => (
  <Container className="py-5 text-center">
    <h2 className="fw-bold mb-4">{title}</h2>
    <p className="text-muted">Tính năng đang được phát triển.</p>
  </Container>
);

function AppContent({
  isLoggedIn,
  setIsLoggedIn,
  userInfo,
  setUserInfo,
  handleLogout
}) {
  const location = useLocation();
  // const navigate = useNavigate(); // Not used in this component

  // Biến kiểm tra quyền admin (không normalize, chỉ đọc trực tiếp)
  const isAdmin = userInfo?.role === "ADMIN";

  const inAdmin = location.pathname.startsWith("/admin");


  return (
    <>
      {!inAdmin && (
        <AppNavbar
          isLoggedIn={isLoggedIn}
          userInfo={userInfo}
          setIsLoggedIn={setIsLoggedIn}
          setUserInfo={setUserInfo}
        />
      )}

      <main>
        <Routes>
          {/* USER ROUTES */}
          <Route path="/" element={<Home />} />
          <Route path="/listings" element={<ListingPage />} />
          <Route path="/listings/:id" element={<ListingDetail />} />
          <Route path="/my-listings" element={isLoggedIn ? <MyListings /> : <Navigate to="/login" replace />} />
          <Route path="/advanced-search" element={<AdvancedSearchPage />} />
          <Route path="/search" element={<SearchResults />} />

          <Route
            path="/post-listing"
            element={
              isLoggedIn ? <CreateListing /> : <Navigate to="/login" replace />
            }
          />

          <Route
            path="/account"
            element={
              isLoggedIn ? <Profile /> : <Navigate to="/login" replace />
            }
          />

          <Route
            path="/orders"
            element={<Placeholder title="Đơn Hàng Của Tôi" />}
          />
          <Route
            path="/favorites"
            element={<Placeholder title="Danh Sách Yêu Thích" />}
          />
          <Route path="/settings" element={<Placeholder title="Cài Đặt" />} />
          <Route
            path="/support"
            element={<Support />}
          />
          <Route
            path="/notifications"
            element={<Placeholder title="Thông Báo" />}
          />

          <Route
            path="/login"
            element={
              isLoggedIn
                ? (isAdmin
                  ? <Navigate to="/admin" replace />
                  : <Navigate to="/" replace />)
                : (
                  <Login
                    setIsLoggedIn={setIsLoggedIn}
                    setUserInfo={setUserInfo}
                  />
                )
            }
          />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route
            path="/verify-email"
            element={
              <VerifyEmail
                setIsLoggedIn={setIsLoggedIn}
                setUserInfo={setUserInfo}
              />
            }
          />

          {/* ADMIN ROUTES */}
          <Route
            path="/admin/*"
            element={
              (isLoggedIn && isAdmin) ? (
                <AdminLayout
                  userInfo={userInfo}
                  onLogout={handleLogout}
                />
              ) : isLoggedIn ? (
                <Navigate to="/" replace />
              ) : (
                <Navigate to="/login" replace />
              )
            }
          >
            <Route index element={<AdminDashboard />} />
            <Route path="pending-listings" element={<PendingListings />} />
            {/*
              Thêm các route admin khác khi bạn đã tạo file:
              <Route path="listings" element={<AdminListings />} />
              ...
            */}
          </Route>

          {/* CATCH ALL */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      {!inAdmin && (
        <footer className="footer text-center">
          <Container>
            <p className="mb-0">
              © 2025 EV Secondhand Marketplace - Nền tảng xe điện cũ hàng đầu
              Việt Nam
            </p>
          </Container>
        </footer>
      )}
    </>
  );
}

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);

  // Logout dùng chung
  const handleLogout = useCallback(async () => {
    try {
      await api.post("/auth/logout");
    } catch {
      /* ignore */
    } finally {
      sessionStorage.clear();
      localStorage.removeItem("userInfo");
      setIsLoggedIn(false);
      setUserInfo(null);
      window.location.href = "/login";
    }
  }, []);

  useEffect(() => {
    const init = async () => {
      try {
        const res = await api.get("/auth/current-user");
        setUserInfo(res.data);
        setIsLoggedIn(true);
        localStorage.setItem("userInfo", JSON.stringify(res.data));
      } catch (error) {
        console.debug('User not logged in:', error.message);
        setIsLoggedIn(false);
        setUserInfo(null);
        localStorage.removeItem("userInfo");
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center vh-100">
        <Spinner animation="border" variant="primary" />
        <span className="ms-3">Đang kiểm tra phiên đăng nhập...</span>
      </div>
    );
  }

  return (
    <AuthContext.Provider value={{ isLoggedIn, setIsLoggedIn, userInfo, setUserInfo }}>
      <Router>
        <AppContent
          isLoggedIn={isLoggedIn}
          setIsLoggedIn={setIsLoggedIn}
          userInfo={userInfo}
          setUserInfo={setUserInfo}
          handleLogout={handleLogout}
        />
      </Router>
    </AuthContext.Provider>
  );
}

export default App;