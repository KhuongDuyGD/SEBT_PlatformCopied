// src/components/Navbar.jsx

import { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Input, Dropdown, Button, Avatar, Modal, Typography, Space, Badge, message } from 'antd';
import { LogoutOutlined, SearchOutlined, CarOutlined, UserOutlined, AppstoreOutlined, BellOutlined, ThunderboltOutlined } from '@ant-design/icons';
import api from '../api/axios';
// import { createTopUpIntent } from '../api/wallet'; // replaced by modal controlled flow
import TopUpModal from './TopUpModal';
import useWalletBalance from '../hooks/useWalletBalance';
import '../css/header.css';

function AppNavbar({ isLoggedIn, setIsLoggedIn, setUserInfo }) {
  // Thêm setUserInfo vào props
  const navigate = useNavigate();
  const location = useLocation();

  // State để quản lý modal logout
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState("");

  // Global search state
  const [searchValue, setSearchValue] = useState("");
  const debounceRef = useRef(null);

  // Khi điều hướng sang trang search mà có keyword trong URL -> sync vào input
  useEffect(() => {
    if (location.pathname.startsWith('/search')) {
      const params = new URLSearchParams(location.search);
      const kw = params.get('keyword') || '';
      setSearchValue(kw);
    }
  }, [location.pathname, location.search]);

  const submitSearch = (e) => {
    e.preventDefault();
    const kw = searchValue.trim();
    if (!kw) return;
    navigate(`/search?keyword=${encodeURIComponent(kw)}&page=0`);
  };

  const handleSearchChange = (e) => {
    const val = e.target.value;
    setSearchValue(val);
    // Optional auto navigate after debounce when on search page
    if (location.pathname.startsWith('/search')) {
      if (debounceRef.current) clearTimeout(debounceRef.current);
      debounceRef.current = setTimeout(() => {
        if (val.trim()) {
          navigate(`/search?keyword=${encodeURIComponent(val.trim())}&page=0`);
        }
      }, 500);
    }
  };

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

  const categoryMenuItems = [
    {
      key: 'vehicles',
      label: 'Xe điện',
      icon: <CarOutlined />,
      children: [
        { key: 'xe-may-dien', label: <Link to="/listings?category=xe-may-dien">Xe máy điện</Link> },
        { key: 'xe-dap-dien', label: <Link to="/listings?category=xe-dap-dien">Xe đạp điện</Link> },
        { key: 'o-to-dien', label: <Link to="/listings?category=o-to-dien">Ô tô điện</Link> }
      ]
    },
    {
      key: 'batteries',
      label: 'Pin',
      icon: <ThunderboltOutlined />,
      children: [
        { key: 'pin-xe-may', label: <Link to="/listings?category=pin&type=xe-may">Pin xe máy</Link> },
        { key: 'pin-xe-dap', label: <Link to="/listings?category=pin&type=xe-dap">Pin xe đạp</Link> },
        { key: 'pin-o-to', label: <Link to="/listings?category=pin&type=o-to">Pin ô tô</Link> }
      ]
    }
  ];

  const { balance, refresh: refreshBalance } = useWalletBalance(isLoggedIn);
  const [showTopUpModal, setShowTopUpModal] = useState(false);

  const accountMenuItems = [
    { key: 'balance', disabled: true, label: <span>Số dư: <strong>{balance == null ? '...' : Number(balance).toLocaleString()} VND</strong></span> },
    { type: 'divider' },
    { key: 'wallet', label: <Link to="/wallet">Ví của tôi</Link> },
    { key: 'profile', label: <Link to="/account">Hồ Sơ Cá Nhân</Link> },
    { key: 'orders', label: <Link to="/orders">Đơn Hàng Của Tôi</Link> },
    { key: 'favorites', label: <Link to="/favorites">Yêu Thích</Link> },
    { key: 'settings', label: <Link to="/settings">Cài Đặt</Link> },
    { type: 'divider' },
    { key: 'logout', icon: <LogoutOutlined style={{ color: '#ff4d4f' }} />, label: <span style={{ color: '#ff4d4f' }}>Đăng Xuất</span>, onClick: handleLogoutClick }
  ];

  function handleTopUp() {
    setShowTopUpModal(true);
  }

  return (
    <div className="app-header-gradient" style={{ position: 'sticky', top: 0, zIndex: 100 }}>
      <div className="app-header">
        <div className="brand" onClick={() => navigate('/')}>⚡ EV Secondhand</div>
        <div className="nav-search">
          <form onSubmit={submitSearch} style={{ display: 'flex', gap: 8 }}>
            <Input
              size="middle"
              allowClear
              value={searchValue}
              onChange={handleSearchChange}
              placeholder="Tìm tiêu đề..."
              prefix={<SearchOutlined style={{ color: '#6b86c9' }} />}
              style={{ borderRadius: 8 }}
            />
          </form>
        </div>
        <div className="nav-spacer" />
        <Space size={14} className="nav-actions">
          <Dropdown
            menu={{ items: categoryMenuItems }}
            trigger={['hover']}
          >
            <Button type="text" icon={<AppstoreOutlined style={{ fontSize: 18 }} />} style={{ color: '#fff' }}>Danh mục</Button>
          </Dropdown>
          <Button type="text" style={{ color: '#fff' }} onClick={() => navigate('/support')}>Hỗ Trợ</Button>
          <Badge count={3} size="small" offset={[0,3]}>
            <Button type="text" style={{ color: '#fff' }} onClick={() => navigate('/notifications')} icon={<BellOutlined style={{ fontSize: 18 }} />} />
          </Badge>
          {isLoggedIn && (
            <Button type="primary" onClick={handleTopUp} style={{ fontWeight: 600 }}>
              Nạp tiền
            </Button>
          )}
          {isLoggedIn ? (
            <Dropdown
              menu={{ items: accountMenuItems }}
              placement="bottomRight"
              trigger={['click']}
            >
              <Avatar style={{ background: '#2f4fa5', cursor: 'pointer' }} icon={<UserOutlined />} />
            </Dropdown>
          ) : (
            <Button onClick={() => navigate('/login')} type="primary" style={{ borderRadius: 20, fontWeight: 600 }}>Đăng Nhập</Button>
          )}
        </Space>
      </div>

      <Modal
        open={showLogoutModal}
        onCancel={cancelLogout}
        centered
        onOk={confirmLogout}
        okText={isLoggingOut ? 'Đang đăng xuất...' : 'Đăng xuất'}
        cancelText="Hủy"
        okButtonProps={{ danger: true, loading: isLoggingOut }}
        title="Xác nhận đăng xuất"
      >
        <Typography.Paragraph>
          Bạn có chắc chắn muốn đăng xuất? Bạn sẽ cần đăng nhập lại để tiếp tục sử dụng đầy đủ tính năng.
        </Typography.Paragraph>
      </Modal>
      <TopUpModal
        open={showTopUpModal}
        onClose={() => setShowTopUpModal(false)}
        refreshBalance={refreshBalance}
        onCompleted={() => {
          message.success('Nạp tiền thành công');
          setShowTopUpModal(false);
        }}
      />
    </div>
  );
}

export default AppNavbar;
