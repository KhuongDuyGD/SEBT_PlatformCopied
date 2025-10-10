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
    "model": "string",
    "brand": "string",
    "year": "number",
    "mileage": "number",
    "batteryCapacity": "number",
    "conditionStatus": "GOOD | FAIR | POOR"
  },
  "product.battery": {
    "brand": "string",
    "model": "string",
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
      "status": "PENDING | APPROVED | REJECTED | EXPIRED | SOLD",
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
  "status": "PENDING | APPROVED | REJECTED | EXPIRED | SOLD",
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
    "type": "CAR | BIKE | SCOOTER",
    "name": "string",
    "model": "string",
    "brand": "string",
    "year": "number",
    "mileage": "number",
    "batteryCapacity": "number",
    "conditionStatus": "GOOD | FAIR | POOR"
    // OR for Battery
    "brand": "string",
    "model": "string",
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
- `vehicleType`: CAR | BIKE | SCOOTER
- `year`: Năm sản xuất
- `brand`: Hãng
- `location`: Địa điểm (tỉnh/thành)
- `minBatteryCapacity` / `maxBatteryCapacity`: Khoảng dung lượng pin (Wh hoặc kWh tùy chuẩn nội bộ)
- `minPrice` / `maxPrice`: Khoảng giá
- `page` (default: 0)
- `size` (default: 12)

### Response
Giống format danh sách EV Listings (có thể thêm `favorited` nếu người dùng đăng nhập)

## 7. Filter Battery Listings
**Endpoint:** `GET /api/listings/battery-filter`

### Query Parameters (tất cả optional)
- `brand`: Hãng pin
- `location`: Địa điểm
- `compatibility`: Chuỗi mô tả tương thích (ví dụ "CAR,Bike")
- `minBatteryCapacity` / `maxBatteryCapacity`: Khoảng dung lượng
- `minPrice` / `maxPrice`: Khoảng giá
- `page` (default: 0)
- `size` (default: 12)

### Response
Giống format Battery Listings (kèm `favorited` nếu có phiên đăng nhập)

## 8. Get My Listings
**Endpoint:** `GET /api/listings/my-listings`

### Query Parameters
- `page` (optional, default: 0)
- `size` (optional, default: 12)
- `status` (optional): Filter by status (PENDING | APPROVED | REJECTED | EXPIRED | SOLD)

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
