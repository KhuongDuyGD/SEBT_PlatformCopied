import { Container, Row, Col } from "react-bootstrap";
import UserInfoCard from "./UserInfoCard";
import UpdateProfileCard from "./UpdateProfileCard";
import "./Profile.css";

function Profile() {
    return (
        <Container fluid className="profile-container py-4">
            <Row>
                <Col lg={4} md={5}>
                    <UserInfoCard />
                </Col>
                <Col lg={8} md={7}>
                    <UpdateProfileCard />
                </Col>
            </Row>
        </Container>
    );
}

export default Profile;