// ===== IMPORT CÁC THƯ VIỆN VÀ DEPENDENCIES =====

// Import các React hooks cần thiết
import { useState, useEffect, useRef } from "react";
// - useState: quản lý state trong functional component
// - useEffect: chạy side effect (như gọi API, subscribe events)
// - useRef: tạo reference đến DOM element hoặc giá trị không trigger re-render

// Import các component UI từ React Bootstrap
import { Card, Form, Button, Alert, Image, Spinner } from "react-bootstrap";
// - Card: component hiển thị nội dung dạng thẻ
// - Form: các component liên quan đến form (Form.Group, Form.Control, etc.)
// - Button: nút bấm
// - Alert: thông báo
// - Image: hiển thị ảnh
// - Spinner: icon loading xoay tròn

// Import các icon từ React Bootstrap Icons
import { Camera, ArrowClockwise, Save2 } from "react-bootstrap-icons";
// - Camera: icon máy ảnh (dùng cho upload avatar)
// - ArrowClockwise: icon mũi tên xoay (dùng cho nút reset)
// - Save2: icon lưu (dùng cho nút submit)

// Import useForm hook từ react-hook-form để quản lý form
import { useForm } from "react-hook-form";
// react-hook-form giúp quản lý form state, validation, và submission dễ dàng hơn

// Import axios instance đã config sẵn để gọi API
import api from "../../api/axios";
// axios instance này đã được config với baseURL, headers, interceptors...


// ===== ĐỊNH NGHĨA COMPONENT CHÍNH =====

// Component UpdateProfileCard nhận 2 props:
// - userInfo: object chứa thông tin user hiện tại (username, phoneNumber, avatarUrl...)
// - onUpdated: callback function được gọi khi cập nhật thành công
function UpdateProfileCard({ userInfo, onUpdated }) {
  
  // ===== KHỞI TẠO REACT-HOOK-FORM =====
  
  const {
    register,      // Function để đăng ký input vào form, tự động bind value và onChange
    handleSubmit,  // Function wrapper cho submit handler, tự động validate trước khi gọi onSubmit
    reset,         // Function để reset toàn bộ form về giá trị mặc định
    formState: { errors }, // Object chứa các lỗi validation của từng field
    watch          // Function để theo dõi giá trị real-time của field (ở đây chưa dùng)
  } = useForm({
    // Thiết lập giá trị mặc định cho các field khi khởi tạo form
    defaultValues: {
      // Nếu userInfo có username thì dùng, không thì để chuỗi rỗng
      username: userInfo?.username || "",
      // Optional chaining (?.) tránh lỗi nếu userInfo là null/undefined
      phoneNumber: userInfo?.phoneNumber || "",
      avatarUrl: userInfo?.avatarUrl || ""
    }
  });

  
  // ===== KHỞI TẠO CÁC STATE =====
  
  // State để track trạng thái loading khi đang submit form
  const [isLoading, setIsLoading] = useState(false);
  // true: đang gửi request update profile
  // false: không có request nào đang chạy
  
  // State để track trạng thái upload avatar
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  // true: đang upload ảnh lên server
  // false: không có upload nào đang chạy
  
  // State để lưu thông báo hiển thị cho user
  const [message, setMessage] = useState({ type: "", content: "" });
  // type: loại thông báo ("success", "danger", "warning", "info")
  // content: nội dung thông báo
  
  // State để lưu URL ảnh preview (ảnh hiển thị trước khi upload)
  const [previewImage, setPreviewImage] = useState(
    // Ưu tiên avatarUrl, nếu không có thì dùng avatar
    userInfo?.avatarUrl || userInfo?.avatar || ""
  );
  
  // State để lưu file ảnh được chọn từ input
  const [selectedFile, setSelectedFile] = useState(null);
  // null: chưa chọn file nào
  // File object: đã chọn file để upload

  
  // ===== KHỞI TẠO REF =====
  
  // Tạo ref để truy cập DOM element của input file (được ẩn)
  const fileInputRef = useRef(null);
  // useRef không trigger re-render khi giá trị thay đổi
  // Dùng để programmatically trigger click event vào input file

  
  // ===== EFFECT: CẬP NHẬT FORM KHI USERINFO THAY ĐỔI =====
  
  // useEffect chạy khi userInfo hoặc reset thay đổi
  useEffect(() => {
    // Kiểm tra nếu userInfo tồn tại (không null/undefined)
    if (userInfo) {
      // Reset form về giá trị mới từ userInfo
      reset({
        username: userInfo.username || "",
        phoneNumber: userInfo.phoneNumber || "",
        avatarUrl: userInfo.avatarUrl || ""
      });

      // Cập nhật ảnh preview về ảnh gốc của user
      setPreviewImage(userInfo.avatarUrl || userInfo.avatar || "");

      // Xóa file đã chọn (vì đang load data mới)
      setSelectedFile(null);
    }
  }, [userInfo, reset]); 
  // Dependency array: effect chạy lại khi userInfo hoặc reset thay đổi

  
  // ===== HÀM UPLOAD AVATAR =====
  
  // Function async để upload ảnh lên server, trả về URL ảnh hoặc null nếu lỗi
  const uploadAvatarIfNeeded = async () => {
    // Kiểm tra nếu không có file được chọn thì không cần upload
    if (!selectedFile) return null;
    
    // Sử dụng try-catch để bắt lỗi
    try {
      // Bật trạng thái đang upload
      setIsUploadingAvatar(true);

      // Tạo FormData object để chứa file
      const formData = new FormData();
      // FormData là format đặc biệt để gửi file qua HTTP
      
      // Thêm file vào FormData với key là "file"
      formData.append("file", selectedFile);
      // Key "file" phải match với tên field mà backend expect

      // Gửi POST request lên endpoint upload avatar
      const res = await api.post("/files/upload-avatar", formData, {
        // Header cho biết đây là multipart form (có file)
        headers: { "Content-Type": "multipart/form-data" },
        // Gửi kèm cookie để xác thực (nếu API yêu cầu)
        withCredentials: true
      });

      // Lấy URL ảnh từ response
      // Thử 2 cấu trúc response khác nhau: res.data.url hoặc res.data.data.url
      const url = res.data?.url || res.data?.data?.url;

      // Kiểm tra nếu không lấy được URL thì throw error
      if (!url) throw new Error("Không lấy được URL ảnh sau upload");

      // Return URL để hàm gọi có thể sử dụng
      return url;
      
    } catch (e) {
      // Nếu có lỗi xảy ra trong quá trình upload
      
      // Log error ra console để debug
      console.error("Upload avatar error:", e);
      
      // Hiển thị thông báo lỗi cho user
      setMessage({
        type: "danger", // Loại thông báo: danger (màu đỏ)
        // Ưu tiên lấy message từ response, nếu không có thì dùng e.message
        content:
          e.response?.data?.message ||  // Message từ API response
          e.response?.data?.error ||     // Error từ API response
          e.message ||                    // Message từ Error object
          "Upload ảnh thất bại"          // Fallback message
      });
      
      // Return null để báo hiệu upload thất bại
      return null;
      
    } finally {
      // Block finally luôn chạy dù có lỗi hay không
      // Tắt trạng thái upload
      setIsUploadingAvatar(false);
    }
  };

  
  // ===== HÀM XỬ LÝ SUBMIT FORM =====
  
  // Function async xử lý khi user nhấn nút "Lưu Thay Đổi"
  // Tham số values chứa dữ liệu từ form (đã qua validation)
  const onSubmit = async (values) => {
    try {
      // Bật trạng thái loading (disable các nút, hiển thị spinner)
      setIsLoading(true);
      
      // Xóa thông báo cũ (nếu có)
      setMessage({ type: "", content: "" });

      // --- XỬ LÝ UPLOAD AVATAR (NẾU CÓ) ---
      
      // Lấy avatarUrl từ form input, trim() để xóa khoảng trắng thừa
      let finalAvatarUrl = values.avatarUrl?.trim();
      
      // Nếu user đã chọn file ảnh mới
      if (selectedFile) {
        // Gọi hàm upload và chờ kết quả
        const uploaded = await uploadAvatarIfNeeded();
        
        // Nếu upload thất bại (trả về null)
        if (!uploaded) return; // Dừng lại, không submit form
        
        // Upload thành công thì gán URL mới
        finalAvatarUrl = uploaded;
      }

      // --- SO SÁNH VÀ TẠO PAYLOAD ---
      
      // Tạo object rỗng để chứa dữ liệu sẽ gửi lên server
      const payload = {};
      // Chỉ gửi những field nào thay đổi so với dữ liệu cũ

      // Kiểm tra username
      if (
        values.username?.trim() &&  // Có giá trị và không phải chuỗi rỗng
        values.username.trim() !== userInfo?.username  // Khác với username cũ
      )
        // Thêm username mới vào payload
        payload.username = values.username.trim();

      // Kiểm tra phoneNumber
      if (
        values.phoneNumber?.trim() &&  // Có giá trị
        values.phoneNumber.trim() !== userInfo?.phoneNumber  // Khác số cũ
      )
        // Thêm phoneNumber mới vào payload
        payload.phoneNumber = values.phoneNumber.trim();

      // Kiểm tra avatarUrl
      if (
        finalAvatarUrl &&  // Có URL
        finalAvatarUrl !== userInfo?.avatarUrl &&  // Khác avatar URL cũ
        finalAvatarUrl !== userInfo?.avatar        // Khác avatar cũ (field khác tên)
      )
        // Thêm avatarUrl mới vào payload
        payload.avatarUrl = finalAvatarUrl;

      // Kiểm tra nếu payload rỗng (không có gì thay đổi)
      if (Object.keys(payload).length === 0) {
        // Hiển thị thông báo warning
        setMessage({
          type: "warning",
          content: "Không có thay đổi nào."
        });
        return; // Dừng lại, không gọi API
      }

      // --- GỬI REQUEST CẬP NHẬT ---
      
      // Gọi API để cập nhật profile
      const res = await api.post("/members/update-profile", payload);
      
      // Log response để debug
      console.log("[update-profile] response:", res.data);
      
      // Lấy body từ response, fallback về {} nếu null
      const body = res.data || {};

      // Kiểm tra nếu backend trả về kết quả thất bại
      const failed =
        body.success === false ||      // Có field success = false
        body.error ||                   // Có field error
        /fail/i.test(body.message || "");  // Message chứa từ "fail" (case-insensitive)

      // Nếu request thất bại
      if (failed) {
        // Hiển thị thông báo lỗi
        setMessage({
          type: "danger",
          // Ưu tiên lấy message từ các field khác nhau
          content: body.message || body.error || "Cập nhật thất bại"
        });
        return; // Dừng lại
      }

      // --- XỬ LÝ THÀNH CÔNG ---
      
      // Hiển thị thông báo thành công
      setMessage({
        type: "success",
        content: body.message || "Cập nhật thành công!"
      });

      // Tạo object chứa dữ liệu đã cập nhật
      // Để truyền cho component cha qua callback onUpdated
      const partial = body.data
        ? {
            // Nếu response có trả data thì dùng data đó
            ...(body.data.username ? { username: body.data.username } : {}), //Nếu body.data.username có giá trị (truthy)
                                                                            // → Tạo object: { username: "John" } (giả sử username là "John") Nếu body.data.username không có (falsy: null, undefined, "")
                                                                            // → Tạo object rỗng: {} 
                                                                            // Nếu dữ liệu được đổi thành John1 thì sẽ trải object { username: "John1" }
                                                                            
            ...(body.data.phoneNumber ? { phoneNumber: body.data.phoneNumber } : {}),
            ...(body.data.avatarUrl
              ? { avatarUrl: body.data.avatarUrl }
              : { avatarUrl: finalAvatarUrl })  // Fallback dùng URL đã upload
          }
        : {
            // Nếu không có data trong response thì dùng payload đã gửi
            ...(payload.username ? { username: payload.username } : {}),
            ...(payload.phoneNumber ? { phoneNumber: payload.phoneNumber } : {}),
            ...(payload.avatarUrl ? { avatarUrl: payload.avatarUrl } : {})
          };

      // Gọi callback để component cha cập nhật state
      if (onUpdated) onUpdated(partial);
      
    } catch (err) {
      // Bắt các lỗi không mong muốn (network error, server error...)
      
      // Log error để debug
      console.error("Update profile error:", err);
      
      // Hiển thị thông báo lỗi cho user
      setMessage({
        type: "danger",
        content:
          err.response?.data?.error ||      // Error từ API
          err.response?.data?.message ||    // Message từ API
          "Có lỗi xảy ra khi cập nhật"     // Fallback message
      });
      
    } finally {
      // Luôn chạy dù có lỗi hay không
      // Tắt trạng thái loading
      setIsLoading(false);
    }
  };

  
  // ===== HÀM RESET FORM =====
  
  // Function để đặt lại form về trạng thái ban đầu
  const handleReset = () => {
    // Reset form về giá trị từ userInfo
    reset({
      username: userInfo?.username || "",
      phoneNumber: userInfo?.phoneNumber || "",
      avatarUrl: userInfo?.avatarUrl || ""
    });
    
    // Reset ảnh preview về ảnh gốc
    setPreviewImage(userInfo?.avatarUrl || userInfo?.avatar || "");
    
    // Xóa file đã chọn
    setSelectedFile(null);
    
    // Xóa thông báo
    setMessage({ type: "", content: "" });
  };

  
  // ===== RENDER UI =====
  
  return (
    // Card component từ Bootstrap
    <Card className="update-profile-card shadow-sm">
      {/* Header của Card */}
      <Card.Header className="bg-white border-0">
        {/* Tiêu đề */}
        <h4 className="fw-bold mb-0 py-2">Cập Nhật Thông Tin</h4>
      </Card.Header>

      {/* Body của Card */}
      <Card.Body>
        {/* Hiển thị Alert nếu có message */}
        {message.content && (
          <Alert variant={message.type}>{message.content}</Alert>
        )}
        {/* Conditional rendering: chỉ hiển thị khi message.content có giá trị */}

        {/* Form - handleSubmit tự động validate và gọi onSubmit */}
        <Form onSubmit={handleSubmit(onSubmit)}>
          
          {/* ===== PHẦN UPLOAD AVATAR ===== */}
          
          <Form.Group className="mb-4">
            {/* Label */}
            <Form.Label className="fw-medium d-block mb-3">
              Ảnh đại diện mới
            </Form.Label>

            {/* Container chứa phần upload */}
            <div className="avatar-upload-container">
              {/* Container chứa ảnh preview */}
              <div className="preview-container mb-3">
                {/* Ảnh preview */}
                <Image
                  // Hiển thị ảnh theo thứ tự ưu tiên
                  src={
                    previewImage ||  // Ảnh vừa chọn
                    userInfo?.avatarUrl ||  // Avatar URL từ user
                    userInfo?.avatar ||  // Avatar từ user
                    "http://localhost:8080/images/avatar_classic.jpg"  // Ảnh mặc định
                  }
                  roundedCircle  // Bo tròn ảnh
                  className="preview-avatar"
                  alt="Preview Avatar"
                  // Click vào ảnh để mở dialog chọn file
                  onClick={() => fileInputRef.current?.click()}
                />
                {/* Overlay hiển thị khi hover vào ảnh */}
                <div
                  className="upload-overlay"
                  // Click vào overlay cũng mở dialog chọn file
                  onClick={() => fileInputRef.current?.click()}
                >
                  {/* Icon camera */}
                  <Camera size={24} />
                  {/* Text hướng dẫn */}
                  <span>Tải ảnh lên</span>
                </div>
              </div>

              {/* Input file ẩn */}
              <Form.Control
                ref={fileInputRef}  // Gắn ref để có thể trigger click
                type="file"  // Input type file
                accept="image/*"  // Chỉ chấp nhận file ảnh
                className="d-none"  // Ẩn input
                {...register("avatarFile", {  // Register vào form
                  // Custom onChange handler
                  onChange: (e) => {
                    // Lấy file đầu tiên được chọn
                    const file = e.target.files?.[0];
                    // Nếu không có file thì return
                    if (!file) return;

                    // Giới hạn kích thước file
                    const maxMB = 3;  // Tối đa 3MB
                    // Kiểm tra kích thước file (file.size tính bằng bytes)
                    if (file.size > maxMB * 1024 * 1024) {
                      // Hiển thị thông báo lỗi
                      setMessage({
                        type: "danger",
                        content: `Kích thước tối đa ${maxMB}MB`
                      });
                      // Reset input value
                      e.target.value = "";
                      return;
                    }

                    // Tạo FileReader để đọc file
                    const reader = new FileReader();
                    // Callback khi đọc file xong
                    reader.onloadend = () => {
                      // Set ảnh preview (dạng base64 data URL)
                      setPreviewImage(reader.result);
                      // Lưu file để upload sau
                      setSelectedFile(file);
                    };
                    // Bắt đầu đọc file dưới dạng Data URL
                    reader.readAsDataURL(file);
                  }
                })}
              />

              {/* Hiển thị trạng thái upload */}
              {isUploadingAvatar && (
                <div className="small text-primary text-center">
                  {/* Spinner loading */}
                  <Spinner animation="border" size="sm" className="me-1" />
                  Đang upload ảnh...
                </div>
              )}
              
              {/* Text hướng dẫn */}
              <Form.Text className="text-muted text-center d-block">
                Click vào ảnh để thay đổi. Hỗ trợ PNG, JPG, JPEG (tối đa 3MB).
              </Form.Text>
            </div>
          </Form.Group>

          {/* ===== INPUT TÊN NGƯỜI DÙNG ===== */}
          
          <Form.Group className="mb-4">
            {/* Label */}
            <Form.Label className="fw-medium">Tên người dùng</Form.Label>
            {/* Input text */}
            <Form.Control
              type="text"
              placeholder="Nhập tên người dùng"
              {...register("username", {  // Register vào form với validation
                // Required validation
                required: "Vui lòng nhập tên người dùng",
                // Min length validation
                minLength: { value: 3, message: "Ít nhất 3 ký tự" }
              })}
              // Hiển thị border đỏ nếu có lỗi
              isInvalid={!!errors.username}
            />
            {/* Hiển thị thông báo lỗi nếu có */}
            {errors.username && (
              <Form.Control.Feedback type="invalid">
                {errors.username.message}
              </Form.Control.Feedback>
            )}
          </Form.Group>

          {/* ===== INPUT SỐ ĐIỆN THOẠI ===== */}
          
          <Form.Group className="mb-4">
            {/* Label */}
            <Form.Label className="fw-medium">Số điện thoại</Form.Label>
            {/* Input tel */}
            <Form.Control
              type="tel"
              placeholder="Nhập số điện thoại"
              {...register("phoneNumber", {  // Register với validation
                // Pattern validation: regex kiểm tra đúng 10 số
                pattern: {
                  value: /^[0-9]{10}$/,  // Regex: bắt đầu-kết thúc bằng 10 số
                  message: "Số điện thoại phải đủ 10 số"
                }
              })}
              // Hiển thị border đỏ nếu có lỗi
              isInvalid={!!errors.phoneNumber}
            />
            {/* Hiển thị thông báo lỗi nếu có */}
            {errors.phoneNumber && (
              <Form.Control.Feedback type="invalid">
                {errors.phoneNumber.message}
              </Form.Control.Feedback>
            )}
          </Form.Group>

          {/* ===== NHÓM NÚT HÀNH ĐỘNG ===== */}
          
          <div className="d-flex gap-3 justify-content-end">
            {/* Nút "Đặt Lại" */}
            <Button
              type="button"  // Không submit form
              variant="outline-secondary"  // Style button secondary với viền
              onClick={handleReset}  // Gọi hàm reset
              // Disable khi đang loading hoặc upload
              disabled={isLoading || isUploadingAvatar}
            >
              {/* Icon reset */}
              <ArrowClockwise size={18} /> Đặt Lại
            </Button>

            {/* Nút "Lưu Thay Đổi" */}
            <Button
              type="submit"  // Submit form khi click
              variant="primary"  // Style button primary (màu xanh)
              // Disable khi đang loading hoặc upload
              disabled={isLoading || isUploadingAvatar}
            >
              {/* Conditional rendering: hiển thị khác nhau khi loading */}
              {isLoading ? (
                <>
                  {/* Spinner loading */}
                  <span className="spinner-border spinner-border-sm me-2" />
                  Đang cập nhật...
                </>
              ) : (
                <>
                  {/* Icon save */}
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

// Export component để sử dụng ở file khác
export default UpdateProfileCard;