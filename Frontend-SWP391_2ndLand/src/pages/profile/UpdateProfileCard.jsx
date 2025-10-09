import { useState, useEffect, useRef } from "react";
import { Card, Form, Button, Alert, Image, Spinner } from "react-bootstrap";
import { Camera, ArrowClockwise, Save2 } from "react-bootstrap-icons";
import { useForm } from "react-hook-form";
import api from "../../api/axios";

function UpdateProfileCard({ userInfo, onUpdated }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
    watch
  } = useForm({
    defaultValues: {
      username: userInfo?.username || "",
      phoneNumber: userInfo?.phoneNumber || "",
      avatarUrl: userInfo?.avatarUrl || "" 
    }
  });

  const [isLoading, setIsLoading] = useState(false);
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  const [message, setMessage] = useState({ type: "", content: "" });
  const [previewImage, setPreviewImage] = useState(
    userInfo?.avatarUrl || userInfo?.avatar || ""
  );
  const [selectedFile, setSelectedFile] = useState(null);

  const fileInputRef = useRef(null);

  useEffect(() => {
    if (userInfo) {
      reset({
        username: userInfo.username || "",
        phoneNumber: userInfo.phoneNumber || "",
        avatarUrl: userInfo.avatarUrl || ""
      });
      setPreviewImage(userInfo.avatarUrl || userInfo.avatar || "");
      setSelectedFile(null);
    }
  }, [userInfo, reset]);

  const uploadAvatarIfNeeded = async () => {
    if (!selectedFile) return null;
    try {
      setIsUploadingAvatar(true);
      const formData = new FormData();
      formData.append("file", selectedFile);
      const res = await api.post("/files/upload-avatar", formData, {
        headers: { "Content-Type": "multipart/form-data" },
        withCredentials: true
      });
      const url = res.data?.url || res.data?.data?.url;
      if (!url) throw new Error("Không lấy được URL ảnh sau upload");
      return url;
    } catch (e) {
      console.error("Upload avatar error:", e);
      setMessage({
        type: "danger",
        content:
          e.response?.data?.message ||
          e.response?.data?.error ||
          e.message ||
          "Upload ảnh thất bại"
      });
      return null;
    } finally {
      setIsUploadingAvatar(false);
    }
  };

  const onSubmit = async (values) => {
    try {
      setIsLoading(true);
      setMessage({ type: "", content: "" });

      // Nếu chọn file → upload trước để lấy URL
      let finalAvatarUrl = values.avatarUrl?.trim(); // fallback nếu vẫn muốn cho nhập URL
      if (selectedFile) {
        const uploaded = await uploadAvatarIfNeeded();
        if (!uploaded) {
          // upload fail => dừng
            return;
        }
        finalAvatarUrl = uploaded;
      }

      const payload = {};

      if (
        values.username?.trim() &&
        values.username.trim() !== userInfo?.username
      )
        payload.username = values.username.trim();

      if (
        values.phoneNumber?.trim() &&
        values.phoneNumber.trim() !== userInfo?.phoneNumber
      )
        payload.phoneNumber = values.phoneNumber.trim();

      if (
        finalAvatarUrl &&
        finalAvatarUrl !== userInfo?.avatarUrl &&
        finalAvatarUrl !== userInfo?.avatar
      )
        payload.avatarUrl = finalAvatarUrl;

      if (Object.keys(payload).length === 0) {
        setMessage({
          type: "warning",
          content: "Không có thay đổi nào."
        });
        return;
      }

      const res = await api.post("/members/update-profile", payload);
      console.log("[update-profile] response:", res.data);
      const body = res.data || {};

      const failed =
        body.success === false ||
        body.error ||
        /fail/i.test(body.message || "");

      if (failed) {
        setMessage({
          type: "danger",
          content: body.message || body.error || "Cập nhật thất bại"
        });
        return;
      }

      setMessage({
        type: "success",
        content: body.message || "Cập nhật thành công!"
      });

      const partial = body.data
        ? {
            ...(body.data.username ? { username: body.data.username } : {}),
            ...(body.data.phoneNumber
              ? { phoneNumber: body.data.phoneNumber }
              : {}),
            ...(body.data.avatarUrl
              ? { avatarUrl: body.data.avatarUrl }
              : { avatarUrl: finalAvatarUrl })
          }
        : {
            ...(payload.username ? { username: payload.username } : {}),
            ...(payload.phoneNumber
              ? { phoneNumber: payload.phoneNumber }
              : {}),
            ...(payload.avatarUrl ? { avatarUrl: payload.avatarUrl } : {})
          };

      if (onUpdated) onUpdated(partial);
    } catch (err) {
      console.error("Update profile error:", err);
      setMessage({
        type: "danger",
        content:
          err.response?.data?.error ||
          err.response?.data?.message ||
          "Có lỗi xảy ra khi cập nhật"
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    reset({
      username: userInfo?.username || "",
      phoneNumber: userInfo?.phoneNumber || "",
      avatarUrl: userInfo?.avatarUrl || ""
    });
    setPreviewImage(userInfo?.avatarUrl || userInfo?.avatar || "");
    setSelectedFile(null);
    setMessage({ type: "", content: "" });
  };

  return (
    <Card className="update-profile-card shadow-sm">
      <Card.Header className="bg-white border-0">
        <h4 className="fw-bold mb-0 py-2">Cập Nhật Thông Tin</h4>
      </Card.Header>
      <Card.Body>
        {message.content && (
          <Alert variant={message.type}>{message.content}</Alert>
        )}

        <Form onSubmit={handleSubmit(onSubmit)}>
          {/* Avatar bằng file */}
          <Form.Group className="mb-4">
            <Form.Label className="fw-medium d-block mb-3">
              Ảnh đại diện mới
            </Form.Label>
            <div className="avatar-upload-container">
              <div className="preview-container mb-3">
                <Image
                  src={
                    previewImage ||
                    userInfo?.avatarUrl ||
                    userInfo?.avatar ||
                    "http://localhost:8080/images/avatar_classic.jpg"
                  }
                  roundedCircle
                  className="preview-avatar"
                  alt="Preview Avatar"
                  onClick={() => fileInputRef.current?.click()}
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
                {...register("avatarFile", {
                  onChange: (e) => {
                    const file = e.target.files?.[0];
                    if (!file) return;
                    const maxMB = 3;
                    if (file.size > maxMB * 1024 * 1024) {
                      setMessage({
                        type: "danger",
                        content: `Kích thước tối đa ${maxMB}MB`
                      });
                      e.target.value = "";
                      return;
                    }
                    const reader = new FileReader();
                    reader.onloadend = () => {
                      setPreviewImage(reader.result);
                      setSelectedFile(file);
                    };
                    reader.readAsDataURL(file);
                  }
                })}
              />
              {isUploadingAvatar && (
                <div className="small text-primary text-center">
                  <Spinner animation="border" size="sm" className="me-1" />
                  Đang upload ảnh...
                </div>
              )}
              <Form.Text className="text-muted text-center d-block">
                Click vào ảnh để thay đổi. Hỗ trợ PNG, JPG, JPEG (tối đa 3MB).
              </Form.Text>
            </div>
          </Form.Group>

          {/* (Tùy chọn) Giữ input URL nếu muốn fallback */}
          {/* 
          <Form.Group className="mb-4">
            <Form.Label className="fw-medium">Hoặc dán URL ảnh</Form.Label>
            <Form.Control
              type="text"
              placeholder="https://..."
              {...register("avatarUrl", {
                pattern: {
                  value: /^https?:\/\/.+$/i,
                  message: "URL không hợp lệ"
                }
              })}
              isInvalid={!!errors.avatarUrl}
            />
            {errors.avatarUrl && (
              <Form.Control.Feedback type="invalid">
                {errors.avatarUrl.message}
              </Form.Control.Feedback>
            )}
          </Form.Group>
          */}

          <Form.Group className="mb-4">
            <Form.Label className="fw-medium">Tên người dùng</Form.Label>
            <Form.Control
              type="text"
              placeholder="Nhập tên người dùng"
              {...register("username", {
                required: "Vui lòng nhập tên người dùng",
                minLength: { value: 3, message: "Ít nhất 3 ký tự" }
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
              placeholder="Nhập số điện thoại"
              {...register("phoneNumber", {
                pattern: {
                  value: /^[0-9]{10}$/,
                  message: "Số điện thoại phải đủ 10 số"
                }
              })}
              isInvalid={!!errors.phoneNumber}
            />
            {errors.phoneNumber && (
              <Form.Control.Feedback type="invalid">
                {errors.phoneNumber.message}
              </Form.Control.Feedback>
            )}
          </Form.Group>

          <div className="d-flex gap-3 justify-content-end">
            <Button
              type="button"
              variant="outline-secondary"
              onClick={handleReset}
              disabled={isLoading || isUploadingAvatar}
            >
              <ArrowClockwise size={18} /> Đặt Lại
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={isLoading || isUploadingAvatar}
            >
              {isLoading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" />
                  Đang cập nhật...
                </>
              ) : (
                <>
                  <Save2 size={18} /> Lưu Thay Đổi
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