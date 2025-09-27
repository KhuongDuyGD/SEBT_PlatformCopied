import { useState, useEffect } from "react";
import { Card, Image, ListGroup, Badge } from "react-bootstrap";
import { Person, Envelope, Phone, Calendar, Shield, GeoAlt, CheckCircleFill } from "react-bootstrap-icons";
import api from "../../api/axios";
import "./Profile.css";

function UserInfoCard() {
    const [userInfo, setUserInfo] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchUserInfo();
    }, []);

    const fetchUserInfo = async () => {
        try {
            setIsLoading(true);
            const response = await api.get("/members/profile");
            setUserInfo(response.data.data);
            setError(null);
        } catch (err) {
            setError("Không thể tải thông tin người dùng");
            console.error(err);
        } finally {
            setIsLoading(false);
        }
    };

    if (isLoading) {
        return (
            <Card className="user-info-card shadow-sm">
                <Card.Body className="text-center">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Đang tải...</span>
                    </div>
                </Card.Body>
            </Card>
        );
    }

    if (error) {
        return (
            <Card className="user-info-card shadow-sm">
                <Card.Body className="text-center text-danger">
                    {error}
                </Card.Body>
            </Card>
        );
    }

    return (
        <Card className="user-info-card shadow-sm">
            <Card.Body className="text-center">
                <div className="avatar-container mb-4">
                    <div className="avatar-wrapper">
                        <Image
                            src={userInfo?.avatar || "/default-avatar.png"}
                            roundedCircle
                            className="user-avatar"
                            alt="User Avatar"
                        />
                        {userInfo?.verified && (
                            <div className="verified-badge">
                                <CheckCircleFill className="text-primary" />
                            </div>
                        )}
                    </div>
                </div>
                <h4 className="fw-bold mb-2">{userInfo?.username || "Người dùng"}</h4>
                <div className="mb-4 d-flex gap-2 justify-content-center">
                    <Badge bg="primary" className="status-badge">
                        <Shield className="me-1" /> Thành viên
                    </Badge>
                    {userInfo?.location && (
                        <Badge bg="info" className="status-badge">
                            <GeoAlt className="me-1" /> {userInfo.location}
                        </Badge>
                    )}
                </div>

                <ListGroup variant="flush" className="text-start">
                    <ListGroup.Item className="d-flex align-items-center py-3">
                        <Person className="me-3 text-primary" size={20} />
                        <div>
                            <small className="text-muted d-block">Tên người dùng</small>
                            <span className="fw-medium">{userInfo?.username}</span>
                        </div>
                    </ListGroup.Item>

                    <ListGroup.Item className="d-flex align-items-center py-3">
                        <Envelope className="me-3 text-primary" size={20} />
                        <div>
                            <small className="text-muted d-block">Email</small>
                            <span className="fw-medium">{userInfo?.email}</span>
                        </div>
                    </ListGroup.Item>

                    <ListGroup.Item className="d-flex align-items-center py-3">
                        <Phone className="me-3 text-primary" size={20} />
                        <div>
                            <small className="text-muted d-block">Số điện thoại</small>
                            <span className="fw-medium">{userInfo?.phone || "Chưa cập nhật"}</span>
                        </div>
                    </ListGroup.Item>

                    <ListGroup.Item className="d-flex align-items-center py-3">
                        <Calendar className="me-3 text-primary" size={20} />
                        <div>
                            <small className="text-muted d-block">Ngày tham gia</small>
                            <span className="fw-medium">
                                {userInfo?.createdAt 
                                    ? new Date(userInfo.createdAt).toLocaleDateString('vi-VN')
                                    : "Không có thông tin"
                                }
                            </span>
                        </div>
                    </ListGroup.Item>
                </ListGroup>
            </Card.Body>
        </Card>
    );
}

export default UserInfoCard;