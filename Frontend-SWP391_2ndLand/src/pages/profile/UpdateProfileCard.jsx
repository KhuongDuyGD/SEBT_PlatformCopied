import { useState, useRef } from "react";
import { Card, Form, Button, Alert, Image } from "react-bootstrap";
import { Camera, ArrowClockwise, Save2 } from "react-bootstrap-icons";
import { useForm } from "react-hook-form";
import api from "../../api/axios";
import "./Profile.css";

function UpdateProfileCard() {
    const { register, handleSubmit, reset, formState: { errors } } = useForm();
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState({ type: "", content: "" });
    const [previewImage, setPreviewImage] = useState(null);
    const fileInputRef = useRef(null);

    const onSubmit = async (data) => {
        try {
            setIsLoading(true);
            setMessage({ type: "", content: "" });

            const formData = new FormData();
            if (data.avatar[0]) {
                formData.append("avatar", data.avatar[0]);
            }
            formData.append("username", data.username);
            formData.append("phone", data.phone);

            await api.post("/members/update-profile", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });

            setMessage({
                type: "success",
                content: "Cập nhật thông tin thành công!"
            });

            // Reload page after 1.5s to show updated info
            setTimeout(() => {
                window.location.reload();
            }, 1500);

        } catch (err) {
            setMessage({
                type: "danger",
                content: err.response?.data?.message || "Có lỗi xảy ra khi cập nhật thông tin"
            });
        } finally {
            setIsLoading(false);
        }
    };

    const handleReset = () => {
        reset({
            username: "",
            phone: "",
            avatar: ""
        });
        setMessage({ type: "", content: "" });
        setPreviewImage(null);
    };

    return (
        <Card className="update-profile-card shadow-sm">
            <Card.Header className="bg-white border-0">
                <h4 className="fw-bold mb-0 py-2">Cập Nhật Thông Tin</h4>
            </Card.Header>
            
            <Card.Body>
                {message.content && (
                    <Alert variant={message.type} className="mb-4">
                        {message.content}
                    </Alert>
                )}

                <Form onSubmit={handleSubmit(onSubmit)}>
                    <Form.Group className="mb-4">
                        <Form.Label className="fw-medium d-block mb-3">Ảnh đại diện mới</Form.Label>
                        <div className="avatar-upload-container">
                            <div className="preview-container mb-3">
                                <Image
                                    src={previewImage || "/default-avatar.png"}
                                    roundedCircle
                                    className="preview-avatar"
                                />
                                <div 
                                    className="upload-overlay"
                                    onClick={() => fileInputRef.current?.click()}
                                >
                                    <Camera size={24} />
                                    <span>Tải ảnh lên</span>
                                </div>
                            </div>
                            <Form.Control
                                ref={fileInputRef}
                                type="file"
                                accept="image/*"
                                className="d-none"
                                {...register("avatar", {
                                    onChange: (e) => {
                                        if (e.target.files?.[0]) {
                                            const file = e.target.files[0];
                                            const reader = new FileReader();
                                            reader.onloadend = () => {
                                                setPreviewImage(reader.result);
                                            };
                                            reader.readAsDataURL(file);
                                        }
                                    }
                                })}
                            />
                            <Form.Text className="text-muted text-center d-block">
                                Click vào ảnh để thay đổi. Chấp nhận: PNG, JPG, JPEG
                            </Form.Text>
                        </div>
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-medium">Tên người dùng</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Nhập tên người dùng mới"
                            {...register("username", {
                                required: "Vui lòng nhập tên người dùng",
                                minLength: {
                                    value: 3,
                                    message: "Tên người dùng phải có ít nhất 3 ký tự"
                                }
                            })}
                            isInvalid={!!errors.username}
                        />
                        {errors.username && (
                            <Form.Control.Feedback type="invalid">
                                {errors.username.message}
                            </Form.Control.Feedback>
                        )}
                    </Form.Group>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-medium">Số điện thoại</Form.Label>
                        <Form.Control
                            type="tel"
                            placeholder="Nhập số điện thoại mới"
                            {...register("phone", {
                                pattern: {
                                    value: /^[0-9]{10}$/,
                                    message: "Số điện thoại không hợp lệ"
                                }
                            })}
                            isInvalid={!!errors.phone}
                        />
                        {errors.phone && (
                            <Form.Control.Feedback type="invalid">
                                {errors.phone.message}
                            </Form.Control.Feedback>
                        )}
                    </Form.Group>

                    <div className="d-flex gap-3 justify-content-end">
                        <Button 
                            variant="outline-secondary" 
                            onClick={handleReset}
                            disabled={isLoading}
                            className="btn-with-icon"
                        >
                            <ArrowClockwise size={18} />
                            <span>Đặt Lại</span>
                        </Button>
                        <Button 
                            type="submit" 
                            variant="primary"
                            disabled={isLoading}
                            className="btn-with-icon"
                        >
                            {isLoading ? (
                                <>
                                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                    <span>Đang cập nhật...</span>
                                </>
                            ) : (
                                <>
                                    <Save2 size={18} />
                                    <span>Lưu Thay Đổi</span>
                                </>
                            )}
                        </Button>
                    </div>
                </Form>
            </Card.Body>
        </Card>
    );
}

export default UpdateProfileCard;