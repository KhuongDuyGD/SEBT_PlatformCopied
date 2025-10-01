// src/App.jsx (Updated with loading state to prevent premature redirect)
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import AppNavbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import VerifyEmail from "./pages/auth/VerifyEmail";
import Profile from "./pages/profile/Profile";
import CarListings from "./pages/listings/CarListings";
import PinListings from "./pages/listings/PinListings";
import CreateListing from "./pages/listings/CreateListing";
import ListingDetail from "./pages/listings/ListingDetail";
import { useState, useEffect } from "react";
import { Container, Spinner } from "react-bootstrap"; // Add Spinner for loading
import api from "./api/axios";
import { AuthContext } from "./contexts/AuthContext"; // Import AuthContext từ file riêng
import "./App.css";

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userInfo, setUserInfo] = useState(null);
    const [loading, setLoading] = useState(true); // Add loading state

    useEffect(() => {
        const checkLoggedIn = async () => {
            try {
                const response = await api.get('/auth/current-user');
                setUserInfo(response.data);
                setIsLoggedIn(true);
            } catch (error) {
                // Log lỗi để debug nếu cần
                console.log('User chưa đăng nhập hoặc session hết hạn:', error.message);
                setIsLoggedIn(false);
                setUserInfo(null);
                localStorage.removeItem('userInfo');
            } finally {
                setLoading(false); // End loading after check
            }
        };
        checkLoggedIn();
    }, []);

    useEffect(() => {
        localStorage.setItem("isLoggedIn", isLoggedIn.toString()); // Store as string
        if (isLoggedIn && userInfo) {
            localStorage.setItem("userInfo", JSON.stringify(userInfo));
        } else {
            localStorage.removeItem("userInfo");
        }
    }, [isLoggedIn, userInfo]);

    // Show loading spinner while checking auth
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
                <AppNavbar isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} setUserInfo={setUserInfo} />
                <main>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/battery" element={<PinListings />} />
                        <Route path="/cars" element={<CarListings />} />
                        <Route path="/post-listing" element={
                            isLoggedIn ? (
                                <CreateListing />
                            ) : (
                                <Navigate to="/login" replace />
                            )
                        } />
                        <Route path="/listings/create" element={
                            isLoggedIn ? (
                                <CreateListing />
                            ) : (
                                <Navigate to="/login" replace />
                            )
                        } />
                        <Route path="/listings/:id" element={<ListingDetail />} />
                        <Route path="/support" element={
                            <Container className="py-5 text-center">
                                <h2 className="fw-bold text-warning mb-4">Hỗ Trợ Khách Hàng</h2>
                                <p className="text-muted">Liên hệ: support@evsecondhand.com</p>
                            </Container>
                        } />
                        <Route path="/notifications" element={
                            <Container className="py-5 text-center">
                                <h2 className="fw-bold text-info mb-4">Thông Báo</h2>
                                <p className="text-muted">Bạn chưa có thông báo mới.</p>
                            </Container>
                        } />
                        <Route
                            path="/account"
                            element={
                                isLoggedIn ? (
                                    <Profile />
                                ) : (
                                    <Navigate to="/login" replace />
                                )
                            }
                        />
                        <Route path="/orders" element={
                            <Container className="py-5 text-center">
                                <h2 className="fw-bold mb-4">Đơn Hàng Của Tôi</h2>
                                <p className="text-muted">Tính năng đang được phát triển.</p>
                            </Container>
                        } />
                        <Route path="/favorites" element={
                            <Container className="py-5 text-center">
                                <h2 className="fw-bold text-danger mb-4">Danh Sách Yêu Thích</h2>
                                <p className="text-muted">Tính năng đang được phát triển.</p>
                            </Container>
                        } />
                        <Route path="/settings" element={
                            <Container className="py-5 text-center">
                                <h2 className="fw-bold mb-4">Cài Đặt</h2>
                                <p className="text-muted">Tính năng đang được phát triển.</p>
                            </Container>
                        } />
                        <Route path="/login" element={
                            <Login setIsLoggedIn={setIsLoggedIn} setUserInfo={setUserInfo} />
                        } />
                        <Route path="/register" element={
                            <Register />
                        } />
                        <Route path="/verify-email" element={<VerifyEmail setIsLoggedIn={setIsLoggedIn} setUserInfo={setUserInfo} />} />
                    </Routes>
                </main>
                <footer className="footer text-center">
                    <Container>
                        <p className="mb-0">© 2024 EV Secondhand Marketplace - Nền tảng xe điện cũ hàng đầu Việt Nam</p>
                    </Container>
                </footer>
            </Router>
        </AuthContext.Provider>
    );
}

export default App;