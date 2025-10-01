# API Documentation - SWP391 2ndLand Platform Controllers

## Tổng quan
Tài liệu này mô tả chi tiết các API endpoints của 3 controllers chính trong hệ thống SWP391 2ndLand Platform.

---

## 1. AuthController (`/api/auth`)

### 1.1 POST `/api/auth/register`
**Mô tả**: Đăng ký tài khoản mới

**Request Body**:
```json
{
  "email": "string (required)",
  "password": "string (required, min 6 characters)"
}
```

**Response**:
- **200 OK**: Đăng ký thành công, email xác thực đã được gửi
  ```json
  {
    "message": "Please check your email for verification."
  }
  ```
- **400 Bad Request**: Lỗi validation
  ```json
  {
    "error": "Password must be at least 6 characters"
  }
  ```
- **500 Internal Server Error**: Lỗi server

**Validation**:
- Email format hợp lệ
- Password tối thiểu 6 ký tự
- Email và password không được để trống

---

### 1.2 POST `/api/auth/verify-email`
**Mô tả**: Xác thực email với mã PIN

**Request Body**:
```json
{
  "email": "string (required)",
  "pins": "string (required)"
}
```

**Response**:
- **200 OK**: Xác thực thành công
  ```json
  {
    "message": "Email verified, register successfully"
  }
  ```
- **400 Bad Request**: PIN không đúng hoặc không tìm thấy session
  ```json
  {
    "error": "OTP does not match the registered email."
  }
  ```

**Lưu ý**: Cần có session từ quá trình đăng ký trước đó

---

### 1.3 POST `/api/auth/login`
**Mô tả**: Đăng nhập vào hệ thống

**Request Body**:
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Response**:
- **200 OK**: Đăng nhập thành công
  ```json
  {
    "id": "long",
    "username": "string",
    "email": "string",
    "role": "USER_ROLE",
    "status": "USER_STATUS",
    "sessionId": "string"
  }
  ```
- **401 Unauthorized**: Email hoặc password không đúng
- **403 Forbidden**: Tài khoản chưa được kích hoạt

**Session**: Tạo session với các thuộc tính userId, username, email, role, status

---

### 1.4 GET `/api/auth/current-user`
**Mô tả**: Lấy thông tin user hiện tại

**Headers**: Session cookie required

**Response**:
- **200 OK**: Trả về thông tin user
  ```json
  {
    "id": "long",
    "username": "string", 
    "email": "string",
    "role": "USER_ROLE",
    "status": "USER_STATUS",
    "sessionId": "string"
  }
  ```
- **401 Unauthorized**: Không có session hoặc session không hợp lệ

---

### 1.5 GET `/api/auth/check-session`
**Mô tả**: Kiểm tra trạng thái session

**Headers**: Session cookie required

**Response**:
- **200 OK**: 
  ```json
  {
    "valid": "boolean",
    "message": "string"
  }
  ```

---

### 1.6 POST `/api/auth/logout`
**Mô tả**: Đăng xuất khỏi hệ thống

**Headers**: Session cookie required

**Response**:
- **200 OK**: Đăng xuất thành công
  ```json
  {
    "message": "Logout successful"
  }
  ```
- **500 Internal Server Error**: Lỗi khi đăng xuất

**Chức năng**: 
- Invalidate session
- Xóa cookies SEBT_SESSION và JSESSIONID

---

## 2. MemberController (`/api/members`)

### 2.1 POST `/api/members/update-profile`
**Mô tả**: Cập nhật thông tin profile của user

**Headers**: Session cookie required

**Request Body**:
```json
{
  "username": "string (optional) - Tênhiển thị",
  "phoneNumber": "string (optional) - Số điện thoại",
  "avatarUrl": "string (optional) - URL ảnh đại diện"
}
```

**Response**:
- **200 OK**: Cập nhật thành công
  ```json
  {
    "message": "Profile updated successfully"
  }
  ```
- **400 Bad Request**: Cập nhật thất bại
  ```json
  {
    "error": "Failed to update profile. User not found."
  }
  ```
- **401 Unauthorized**: Không có session hoặc session không hợp lệ
- **500 Internal Server Error**: Lỗi server

**Authorization**: Cần session hợp lệ với userId

---

### 2.2 GET `/api/members/profile`
**Mô tả**: Lấy thông tin profile của user hiện tại

**Headers**: Session cookie required

**Response**:
- **200 OK**: Trả về thông tin profile
  ```json
  {
    "message": "Profile retrieved successfully"
  }
  ```
- **401 Unauthorized**: Không có session hoặc session không hợp lệ
- **404 Not Found**: Không tìm thấy profile user
- **500 Internal Server Error**: Lỗi server

**Authorization**: Cần session hợp lệ với userId

---

### 2.3 GET `/api/members/session-info`
**Mô tả**: Lấy thông tin chi tiết về session hiện tại

**Headers**: Session cookie required

**Response**:
- **200 OK**: Trả về thông tin session
  ```json
  {
    "message": "Session info retrieved",
    "data": {
      "sessionId": "string",
      "userId": "long",
      "username": "string", 
      "email": "string",
      "creationTime": "long",
      "lastAccessedTime": "long",
      "maxInactiveInterval": "int"
    }
  }
  ```
- **401 Unauthorized**: Không có session active
- **500 Internal Server Error**: Lỗi khi lấy thông tin session

---

## 3. ListingController (`/api/listings`)

### 3.1 PUT `/api/listings/create`
**Mô tả**: Tạo listing mới cho việc bán xe điện và pin

**Headers**: Session cookie required

**Authorization**: Cần session hợp lệ với userId

**Content-Type**: `multipart/form-data`

**Request Parts**:
- `createListingForm` (JSON string): Thông tin chi tiết về listing
- `listingImages` (File[]): Danh sách ảnh sản phẩm (bắt buộc, ít nhất 1 ảnh)
- `thumbnailImage` (File): Ảnh thumbnail chính (bắt buộc)

**CreateListingForm JSON Structure**:
```json
{
  "title": "string (required) - Tiêu đề listing",
  "product": {
    "ev": {
      "type": "VehicleType enum (optional) - Loại xe: CAR, MOTORCYCLE, BICYCLE, SCOOTER",
      "name": "string (optional) - Tên xe",
      "model": "string (optional) - Model xe", 
      "brand": "string (optional) - Thương hiệu xe",
      "year": "integer (optional) - Năm sản xuất",
      "mileage": "integer (optional) - Số km đã đi",
      "batteryCapacity": "double (optional) - Dung lượng pin (kWh)",
      "conditionStatus": "VehicleCondition enum (optional) - Tình trạng: NEW, LIKE_NEW, GOOD, FAIR, POOR"
    },
    "battery": {
      "brand": "string (optional) - Thương hiệu pin",
      "model": "string (optional) - Model pin",
      "capacity": "double (optional) - Dung lượng pin",
      "healthPercentage": "integer (optional) - % sức khỏe pin (0-100)",
      "compatibleVehicles": "string (optional) - Các xe tương thích",
      "conditionStatus": "BatteryCondition enum (optional) - Tình trạng: NEW, GOOD, DEGRADED, NEEDS_REPLACEMENT"
    }
  },
  "listingType": "ListingType enum (required) - Loại listing: NORMAL, PREMIUM, FEATURED",
  "description": "string (optional) - Mô tả chi tiết sản phẩm",
  "price": "double (required) - Giá bán",
  "category": "string (required) - Danh mục sản phẩm",
  "location": {
    "province": "string (required) - Tỉnh/Thành phố",
    "district": "string (required) - Quận/Huyện", 
    "details": "string (required) - Địa chỉ chi tiết"
  }
}
```

**Example Request (Multipart Form)**:
```
POST /api/listings/create
Content-Type: multipart/form-data

Parts:
- createListingForm: {
    "title": "Xe điện VinFast VF8 2023 như mới",
    "product": {
      "ev": {
        "type": "CAR",
        "name": "VinFast VF8",
        "model": "VF8 Plus",
        "brand": "VinFast",
        "year": 2023,
        "mileage": 5000,
        "batteryCapacity": 87.7,
        "conditionStatus": "LIKE_NEW"
      },
      "battery": {
        "brand": "CATL",
        "model": "LFP Battery Pack",
        "capacity": 87.7,
        "healthPercentage": 95,
        "compatibleVehicles": "VinFast VF8, VF9",
        "conditionStatus": "GOOD"
      }
    },
    "listingType": "NORMAL",
    "description": "Xe điện VinFast VF8 2023 tình trạng như mới, chạy 5000km. Pin CATL còn 95% dung lượng.",
    "price": 1200000000.0,
    "category": "Ô tô điện",
    "location": {
      "province": "TP. Hồ Chí Minh",
      "district": "Quận 1",
      "details": "123 Nguyen Hue Street, Ben Nghe Ward, District 1"
    }
  }
- listingImages: [image1.jpg, image2.jpg, image3.jpg]  // Array of image files
- thumbnailImage: thumbnail.jpg  // Single thumbnail image file
```

**Response**:
- **200 OK**: Tạo listing thành công
  ```
  "Create listing request successfully"
  ```
- **400 Bad Request**: Validation errors
  ```
  "Create listing form is required"
  "At least one listing image is required"
  "Thumbnail image is required"
  "Create listing request failed"
  ```
- **401 Unauthorized**: Session issues
  ```
  "No active session. Please login first."
  "Invalid session. Please login again."
  ```
- **500 Internal Server Error**: Server errors
  ```
  "Internal server error: {error_message}"
  ```

**Business Logic**:
- **Image Upload**: Ảnh được tự động upload lên Cloudinary
  - `listingImages` được upload vào folder "listings"
  - `thumbnailImage` được upload vào folder "thumbnails"
  - Trả về Image objects với URL và metadata
- **Validation**: 
  - Kiểm tra session hợp lệ
  - Bắt buộc có createListingForm, listingImages, và thumbnailImage
  - CreateListingFormDTO được parse từ JSON string
- **Database Operations**:
  - Tự động tạo các entity liên quan (Product, Location, PostRequest)
  - Lưu URLs của ảnh đã upload
  - Set userId từ session làm seller

**Validation Rules**:
- **Required Fields**: 
  - `title`, `price`, `category`, `listingType`
  - `location.province`, `location.district`, `location.details`
  - `listingImages` (ít nhất 1 ảnh)
  - `thumbnailImage`
- **Optional Fields**: 
  - Tất cả fields trong `product.ev` và `product.battery`
  - `description`
- **File Requirements**:
  - Hỗ trợ: JPG, JPEG, PNG, GIF, WEBP, BMP
  - Kích thước tối đa: 10MB/ảnh
  - Số lượng ảnh listing: không giới hạn (khuyến nghị 3-10 ảnh)

**Integration với Cloudinary**:
- **Upload Process**: 
  1. Upload tất cả `listingImages` song song lên folder "listings"
  2. Upload `thumbnailImage` lên folder "thumbnails"
  3. Nhận về Image objects chứa URL, publicId, và metadata
  4. Lưu Image objects vào database cùng với listing
- **Error Handling**: Nếu upload ảnh thất bại, toàn bộ request sẽ fail
- **Folder Structure**:
  ```
  📁 cloudinary_root/
  ├── 📁 listings/     # Ảnh chi tiết sản phẩm
  └── 📁 thumbnails/   # Ảnh thumbnail chính
  ```

**Frontend Integration Guide**:
```javascript
// Tạo FormData cho multipart request
const formData = new FormData();

// Thêm JSON data
const listingData = {
  title: "Xe điện VinFast VF8 2023",
  product: {
    ev: {
      type: "CAR",
      name: "VinFast VF8",
      // ... other fields
    }
  },
  listingType: "NORMAL",
  price: 1200000000,
  category: "Ô tô điện",
  location: {
    province: "TP. Hồ Chí Minh",
    district: "Quận 1", 
    details: "123 Nguyen Hue Street"
  }
};

formData.append('createListingForm', JSON.stringify(listingData));

// Thêm ảnh listings
listingImages.forEach(image => {
  formData.append('listingImages', image);
});

// Thêm thumbnail
formData.append('thumbnailImage', thumbnailImage);

// Gửi request
fetch('/api/listings/create', {
  method: 'PUT',
  body: formData,
  credentials: 'include' // Để gửi session cookies
})
.then(response => response.text())
.then(data => console.log(data));
```

**Lưu ý quan trọng**:
1. **HTTP Method**: Sử dụng PUT thay vì POST (có thể cần review)
2. **Content-Type**: Bắt buộc phải là `multipart/form-data`
3. **Session**: Cần login trước và gửi session cookies
4. **JSON Parsing**: `createListingForm` phải là valid JSON string
5. **File Upload**: Ảnh được upload trước khi tạo listing record
6. **Error Recovery**: Nếu có lỗi, ảnh đã upload có thể cần cleanup manual

**Performance Considerations**:
- Upload nhiều ảnh có thể mất thời gian, cần implement progress indicator
- Consider image compression trước khi upload
- Validate file size và type ở frontend để giảm failed requests
---

## Các DTO và Models cần tham khảo

### Request DTOs
- `UserRegisterFormDTO`: email, password
- `UserLoginFormDTO`: email, password  
- `UserVerifyEmailFormDTO`: email, pins
- `UpdateProfileFormDTO`: Cần xem chi tiết
- `CreateListingFormDTO`: Cần xem chi tiết

### Response DTOs
- `ErrorResponseDTO`: error message
- `SuccessResponseDTO`: success message + optional data
- `UserSessionResponseDTO`: id, username, email, role, status, sessionId
- `SessionInfoResponseDTO`: sessionId, userId, username, email, creationTime, lastAccessedTime, maxInactiveInterval

### Enums
- `UserRole`: Các vai trò người dùng
- `UserStatus`: ACTIVE, INACTIVE, etc.

---

## Lưu ý về Authentication & Session

1. **Session Management**: 
   - Hệ thống sử dụng HTTP Session để quản lý authentication
   - Session cookies: `SEBT_SESSION` và `JSESSIONID`
   - Session timeout có thể được cấu hình

2. **Security**:
   - Tất cả endpoints trong MemberController yêu cầu authentication
   - Session validation được thực hiện ở mỗi request
   - Password validation: tối thiểu 6 ký tự

3. **Error Handling**:
   - Tất cả controllers đều có try-catch để handle exceptions
   - Error messages được standardize qua ErrorResponseDTO
   - Server errors được log ra console

4. **CORS**: Cần kiểm tra CorsConfig để đảm bảo frontend có thể kết nối

---

## Khuyến nghị

1. **API Consistency**: 
   - ListingController nên sử dụng POST thay vì PUT cho create operation
   - Standardize response format cho tất cả endpoints

2. **Security Enhancement**:
   - Thêm rate limiting cho login/register endpoints
   - Implement password hashing strength validation
   - Add request logging cho security monitoring

3. **Documentation**:
   - Cần document chi tiết các DTO fields
   - Thêm example requests/responses đầy đủ
   - API versioning consideration

4. **Error Handling**:
   - Implement global exception handler
   - Standardize error codes
   - Add more specific error messages
