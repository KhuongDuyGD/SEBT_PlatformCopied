import { useState, useEffect } from "react";
import { Container, Row, Col } from "react-bootstrap";
import UserInfoCard from "./UserInfoCard";
import UpdateProfileCard from "./UpdateProfileCard";
import api from "../../api/axios";
import "./Profile.css";

function Profile() {
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await api.get("/members/profile");
        setUserInfo(res.data.data);
      } catch (err) {
        console.error("Không thể tải thông tin người dùng:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchUser();
  }, []);

  if (loading) {
    return <div className="text-center py-5">Đang tải thông tin...</div>;
  }

  return (
    <Container fluid className="profile-container py-4">
      <Row>
        <Col lg={4} md={5}>
          <UserInfoCard userInfo={userInfo} />
        </Col>
        <Col lg={8} md={7}>
          <UpdateProfileCard userInfo={userInfo} />
        </Col>
      </Row>
    </Container>
  );
}

export default Profile;
