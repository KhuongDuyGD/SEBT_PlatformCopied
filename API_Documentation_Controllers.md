# API Documentation: Listing Controller

## Base URL
```
/api/listings
```

## 1. Create Listing
**Endpoint:** `POST /api/listings/create`  
**Content-Type:** `multipart/form-data`

### Request Headers
- `X-User-ID`: (optional) User ID for authentication if not using session

### Request Body
```json
{
  "title": "string",
  "description": "string",
  "price": "number",
  "category": "EV | BATTERY",
  "listingType": "NORMAL",
  "images": "File[]",
  "product.ev": {
    "type": "CAR | BIKE | MOTORBIKE",
    "name": "string",
    "brand": "string",
    "year": "number",
    "mileage": "number",
    "batteryCapacity": "number",
    "conditionStatus": "GOOD | FAIR | POOR"
  },
  "product.battery": {
    "brand": "string",
    "capacity": "number",
    "healthPercentage": "number",
    "compatibleVehicles": "string",
    "conditionStatus": "GOOD | FAIR | POOR"
  },
  "location": {
    "province": "string",
    "district": "string",
    "details": "string"
  }
}
```

### Responses

#### Success Response (200 OK)
```json
{
  "success": true,
  "message": "Tạo bài đăng thành công"
}
```

#### Error Responses

**401 Unauthorized**
```json
{
  "success": false,
  "message": "Vui lòng đăng nhập để tạo bài đăng"
}
```

**400 Bad Request**
```json
{
  "success": false,
  "message": "Vui lòng tải lên ít nhất một ảnh cho bài đăng"
}
// or
{
  "success": false,
  "message": "Tải lên ảnh thất bại. Vui lòng thử lại"
}
// or
{
  "success": false,
  "message": "Tạo bài đăng thất bại. Vui lòng kiểm tra lại thông tin"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "message": "Lỗi khi tạo bài đăng"
}
```

## 2. Get EV Listings
**Endpoint:** `GET /api/listings/evCart`

### Query Parameters
- `page` (optional, default: 0): Page number
- `size` (optional, default: 12): Items per page

### Response

#### Success Response (200 OK)
```json
{
  "content": [
    {
      "id": "number",
      "title": "string",
      "price": "number",
      "thumbnailImage": "string",
      "thumbnailPublicId": "string",
      "description": "string",
      "status": "PENDING | ACTIVE | SOLD | SUSPENDED",
      "listingType": "NORMAL",
      "viewsCount": "number",
      "createdAt": "string (ISO date)",
      "seller": {
        "id": "number",
        "username": "string",
        "email": "string",
        "avatar": "string"
      }
    }
  ],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number",
  "first": "boolean",
  "last": "boolean",
  "empty": "boolean"
}
```

## 3. Get Battery Listings
**Endpoint:** `GET /api/listings/batteryCart`

### Query Parameters
Same as EV Listings

### Response
Same format as EV Listings

## 4. Get Listing Detail
**Endpoint:** `GET /api/listings/detail/{listingId}`

### Path Parameters
- `listingId`: ID of the listing to retrieve

### Response

#### Success Response (200 OK)
```json
{
  "id": "number",
  "title": "string",
  "description": "string",
  "price": "number",
  "status": "PENDING | ACTIVE | SOLD | SUSPENDED",
  "listingType": "NORMAL",
  "viewsCount": "number",
  "createdAt": "string (ISO date)",
  "seller": {
    "id": "number",
    "username": "string",
    "email": "string",
    "avatar": "string"
  },
  "images": [
    {
      "id": "number",
      "url": "string",
      "publicId": "string",
      "displayOrder": "number"
    }
  ],
  "product": {
    // For EV
    "type": "CAR | BIKE | MOTORBIKE",
    "name": "string",
    "brand": "string",
    "year": "number",
    "mileage": "number",
    "batteryCapacity": "number",
    "conditionStatus": "GOOD | FAIR | POOR"
    // OR for Battery
    "brand": "string",
    "capacity": "number",
    "healthPercentage": "number",
    "compatibleVehicles": "string",
    "conditionStatus": "GOOD | FAIR | POOR"
  }
}
```

#### Error Responses

**400 Bad Request**
```json
{
  "success": false,
  "message": "ID bài đăng không hợp lệ"
}
```

**404 Not Found**
```json
{
  "success": false,
  "message": "Không tìm thấy bài đăng với ID đã cho"
}
```

## 5. Search Listings
**Endpoint:** `GET /api/listings/search`

### Query Parameters
- `keyword`: Search term
- `page` (optional, default: 0)
- `size` (optional, default: 12)

### Response
Same format as EV/Battery Listings response

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Vui lòng nhập từ khóa tìm kiếm"
}
```

## 6. Filter EV Listings
**Endpoint:** `GET /api/listings/ev-filter`

### Query Parameters (tất cả optional)
- `vehicleType`: CAR | BIKE | MOTORBIKE
- `year`: Năm sản xuất
- `brand`: Hãng xe
- `province`: Tỉnh/thành phố
- `minMileage` / `maxMileage`: Khoảng số km đã đi
- `minBatteryCapacity` / `maxBatteryCapacity`: Khoảng dung lượng pin (kWh)
- `minPrice` / `maxPrice`: Khoảng giá (VND)
- `page` (default: 0)
- `size` (default: 12)

### Response
Giống format danh sách EV Listings (có thể thêm `favorited` nếu người dùng đăng nhập)

## 7. Filter Battery Listings
**Endpoint:** `GET /api/listings/battery-filter`

### Query Parameters (tất cả optional)
- `brand`: Hãng pin
- `name`: Tên pin (autocomplete search)
- `year`: Năm sản xuất pin
- `province`: Tỉnh/thành phố
- `minPrice` / `maxPrice`: Khoảng giá (VND)
- `page` (default: 0)
- `size` (default: 12)

### Response
Giống format Battery Listings (kèm `favorited` nếu có phiên đăng nhập)

## 8. Get My Listings
**Endpoint:** `GET /api/listings/my-listings`

### Query Parameters
- `page` (optional, default: 0)
- `size` (optional, default: 12)
- `status` (optional): Filter by status (PENDING | ACTIVE | SOLD | SUSPENDED)

### Response
Same format as EV/Battery Listings response

#### Error Response (401 Unauthorized)
```json
{
  "success": false,
  "message": "Vui lòng đăng nhập để xem bài đăng của bạn"
}
```

## 9. Favorites (New RESTful)

### 9.1 Mark Favorite
**Endpoint:** `PUT /api/members/favorites/{listingId}`

Yêu cầu phiên đăng nhập hợp lệ (cookie session). Trả về:
```json
{
  "listingId": 123,
  "favorited": true
}
```

### 9.2 Unmark Favorite
**Endpoint:** `DELETE /api/members/favorites/{listingId}`

Phản hồi:
```json
{
  "listingId": 123,
  "favorited": false
}
```

### 9.3 Legacy (Deprecated) — sẽ gỡ sau
`PUT /api/members/favorite?userId=&listingId=`
`DELETE /api/members/favorite?userId=&listingId=`

Không nên dùng nữa; frontend đã bỏ fallback.

## Common Error Handling in Frontend

```javascript
// Example using axios
const handleError = (error) => {
  if (error.response) {
    // Server responded with error status
    const message = error.response.data?.message || 'Có lỗi xảy ra';
    const status = error.response.status;
    
    switch (status) {
      case 401:
        // Redirect to login
        break;
      case 400:
        // Show validation error
        break;
      case 404:
        // Show not found message
        break;
      case 500:
        // Show server error
        break;
    }
    return { success: false, message };
  } else if (error.request) {
    // Network error
    return {
      success: false,
      message: 'Không thể kết nối đến server'
    };
  } else {
    return {
      success: false,
      message: 'Có lỗi xảy ra'
    };
  }
};

// Example usage
try {
  const response = await listingsApi.createListing(formData);
  if (response.success) {
    // Handle success
  } else {
    // Handle error with response.message
  }
} catch (error) {
  const errorResult = handleError(error);
  // Show error message to user
}
```

---

## Database Seeding Changes & New Features

### Recent Updates (Phiên bản mới nhất)

#### 1. **Loại bỏ trường `model`**
- **Thay đổi**: Đã xóa hoàn toàn trường `model` khỏi cả `EvVehicleEntity` và `BatteryEntity`
- **Ảnh hưởng**: 
  - API không còn nhận/trả về field `model` trong request/response
  - Filter API đã được cập nhật để loại bỏ parameter `model`
  - Database schema không còn cột `model`

#### 2. **Trạng thái mới: PENDING**
- **Thêm mới**: Trạng thái `PENDING` cho listings chờ admin duyệt
- **Ý nghĩa các trạng thái**:
  - `PENDING`: Chờ admin duyệt (40% listings)
  - `ACTIVE`: Đã được duyệt và đang bán (35% listings)  
  - `SOLD`: Đã được bán (15% listings)
  - `SUSPENDED`: Bị admin cấm/tạm ngưng (10% listings)

#### 3. **Nâng cấp Database Seeding**
- **Số lượng**: Tăng từ 100 lên **200 listings** (100 xe điện + 100 pin)
- **Nội dung tiếng Việt**: 
  - Tiêu đề và mô tả đa dạng, chuyên nghiệp
  - Địa chỉ sử dụng tên đường tiếng Việt
- **Phân bổ địa lý**: Đảm bảo tất cả **63 tỉnh thành** Việt Nam đều có listings
- **URL ảnh mặc định**:
  - Xe điện: `https://res.cloudinary.com/dkvldb91c/image/upload/v1759568865/swp391/listings/c5jic9fai7l0rq87ojng.webp`
  - Pin điện: `https://res.cloudinary.com/dkvldb91c/image/upload/v1760317167/images_2_mrdrjy.jpg`

#### 4. **Cải tiến Account Management**
- **Tài khoản**: 13 tài khoản (2 admin + 11 member)
- **Mật khẩu thống nhất**: Tất cả đều là `123456`
- **Email mới**: Thêm `npln.0307@gmail.com`

#### 5. **Filter API Enhancements**
- **EV Filter**: Loại bỏ `model`, thêm `minMileage/maxMileage`, `province`
- **Battery Filter**: Loại bỏ `model`, thêm `name`, `year`, `province`
- **Location Support**: Tất cả filter đều support tìm kiếm theo tỉnh thành

### Migration Notes cho Frontend

#### Breaking Changes
```javascript
// CŨ - sẽ gây lỗi
const evData = {
  name: "Tesla Model 3",
  model: "Model 3",  // ❌ Field này không còn tồn tại
  brand: "Tesla"
};

// MỚI - format đúng
const evData = {
  name: "Tesla Model 3",
  brand: "Tesla"      // ✅ Chỉ cần name và brand
};
```

#### Status Handling
```javascript
// Cập nhật enum status
const LISTING_STATUS = {
  PENDING: 'PENDING',     // ✅ Mới
  ACTIVE: 'ACTIVE',       // ✅ Đổi từ APPROVED
  SOLD: 'SOLD',          // ✅ Giữ nguyên
  SUSPENDED: 'SUSPENDED'  // ✅ Đổi từ REJECTED
};

// Cập nhật UI status display
const getStatusDisplay = (status) => {
  switch(status) {
    case 'PENDING': return 'Chờ duyệt';
    case 'ACTIVE': return 'Đang bán';
    case 'SOLD': return 'Đã bán';
    case 'SUSPENDED': return 'Bị cấm';
    default: return 'Không xác định';
  }
};
```

#### Filter Updates
```javascript
// EV Filter - loại bỏ model
const evFilters = {
  vehicleType: 'CAR',
  year: 2023,
  brand: 'VinFast',
  // model: 'VF8',        // ❌ Xóa field này
  province: 'TP. Hồ Chí Minh',  // ✅ Mới
  minMileage: 0,                 // ✅ Mới
  maxMileage: 50000,            // ✅ Mới
  minBatteryCapacity: 50,
  maxBatteryCapacity: 100,
  minPrice: 500000000,
  maxPrice: 2000000000
};

// Battery Filter - cập nhật parameters
const batteryFilters = {
  brand: 'CATL',
  name: 'Battery Pack',      // ✅ Mới - autocomplete
  year: 2023,               // ✅ Mới
  province: 'Hà Nội',       // ✅ Mới
  minPrice: 50000000,
  maxPrice: 200000000
};
```
