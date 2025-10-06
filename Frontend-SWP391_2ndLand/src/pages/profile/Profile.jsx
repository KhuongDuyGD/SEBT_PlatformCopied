import { useState, useEffect, useCallback } from "react";
import { Container, Row, Col, Spinner, Alert } from "react-bootstrap";
import UserInfoCard from "./UserInfoCard";
import UpdateProfileCard from "./UpdateProfileCard";
import api from "../../api/axios";
import "../../css/Profile.css";

function Profile() {
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  const fetchUser = useCallback(async () => {
    try {
      setErr("");
      const res = await api.get("/members/profile?ts=" + Date.now());
      const data = res.data?.data || res.data;
      setUserInfo(data);
      console.log("[fetchUser] profile =", data);
    } catch (e) {
      console.error("[fetchUser] error:", e);
      setErr(
        e.response?.data?.message ||
        e.response?.data?.error ||
        "Không thể tải thông tin người dùng."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  const handleUpdated = async (partial) => {
    // Optimistic
    if (partial && Object.keys(partial).length) {
      setUserInfo(prev => ({ ...prev, ...partial }));
    }
    // Refetch để bảo đảm đồng bộ
    await fetchUser();
  };

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" size="sm" className="mb-2" />
        <div>Đang tải thông tin...</div>
      </div>
    );
  }

  if (err) {
    return (
      <div className="p-4 text-center">
        <Alert variant="danger" className="mb-3">{err}</Alert>
        <button className="btn btn-primary" onClick={fetchUser}>Thử lại</button>
      </div>
    );
  }

  if (!userInfo) {
    return (
      <div className="p-4 text-center">
        <Alert variant="warning">Không có dữ liệu người dùng.</Alert>
        <button className="btn btn-primary" onClick={fetchUser}>Tải lại</button>
      </div>
    );
  }

  return (
    <Container fluid className="profile-container py-4">
      <Row>
        <Col lg={4} md={5}>
          <UserInfoCard userInfo={userInfo} />
        </Col>
        <Col lg={8} md={7}>
          <UpdateProfileCard
            userInfo={userInfo}
            onUpdated={handleUpdated}
          />
        </Col>
      </Row>
    </Container>
  );
}

export default Profile;