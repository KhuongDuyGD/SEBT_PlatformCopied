# CloudinaryService - Hướng dẫn sử dụng

## Tổng quan
CloudinaryService là service quản lý upload, lưu trữ và xóa ảnh trên Cloudinary cho ứng dụng SEBT Platform. Service này hỗ trợ upload một ảnh hoặc nhiều ảnh cùng lúc, đặc biệt phù hợp cho việc upload ảnh listing bán xe và pin.

## Cấu hình đã thiết lập

### 1. Dependencies
- `cloudinary-http44:1.36.0` - Cloudinary Java SDK
- Đã thêm vào `pom.xml`

### 2. Configuration
- **Cloud Name**: ddkdnc4qo
- **API Key**: 359585565382774
- **API Secret**: NSR_ETB4eVpJno0RII0B9JXME4E
- **Secure**: true (sử dụng HTTPS)

### 3. File types hỗ trợ
- JPG, JPEG, PNG, GIF, WEBP, BMP
- Kích thước tối đa: 10MB

## API Endpoints

### 1. Upload một ảnh
```
POST /api/images/upload-single?folder=listings
Content-Type: multipart/form-data

Body:
- file: [image file]
- folder: "listings" (optional, default: "general")
```

**Response:**
```json
{
  "success": true,
  "message": "Image uploaded successfully",
  "data": {
    "publicId": "listings/image_1234567890",
    "url": "http://res.cloudinary.com/ddkdnc4qo/image/upload/v1234567890/listings/image_1234567890.jpg",
    "secureUrl": "https://res.cloudinary.com/ddkdnc4qo/image/upload/v1234567890/listings/image_1234567890.jpg",
    "format": "jpg",
    "bytes": 123456,
    "width": 800,
    "height": 600,
    "uploadedAt": "2024-01-01T10:00:00",
    "folder": "listings"
  }
}
```

### 2. Upload nhiều ảnh
```
POST /api/images/upload-multiple?folder=listings
Content-Type: multipart/form-data

Body:
- files: [array of image files]
- folder: "listings" (optional)
```

**Response:**
```json
{
  "success": true,
  "message": "Uploaded 3/3 images successfully",
  "data": {
    "uploadedImages": [...],
    "failedImages": [],
    "totalUploaded": 3,
    "totalFailed": 0,
    "allSuccess": true
  }
}
```

### 3. Upload ảnh cho listing cụ thể
```
POST /api/images/upload-listing
Content-Type: multipart/form-data

Body:
- files: [array of image files]
- listingId: 123 (optional)
```

### 4. Upload avatar
```
POST /api/images/upload-avatar
Content-Type: multipart/form-data

Body:
- file: [image file]
- userId: 123
```

### 5. Xóa ảnh
```
DELETE /api/images/{publicId}
```
**Lưu ý:** PublicId có chứa "/" cần encode thành "_SLASH_"

### 6. Xóa nhiều ảnh
```
DELETE /api/images/bulk
Content-Type: application/json

Body: ["publicId1", "publicId2", "publicId3"]
```

### 7. Lấy ảnh với kích thước tùy chỉnh
```
GET /api/images/transform/{publicId}?width=300&height=200
```

## Sử dụng trong Code

### 1. Inject CloudinaryService
```java
@Service
public class YourService {
    private final CloudinaryService cloudinaryService;
    
    @Autowired
    public YourService(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }
}
```

### 2. Upload một ảnh
```java
// Upload một ảnh
ImageUploadResponseDTO result = cloudinaryService.uploadImage(file, "listings");
String imageUrl = result.secureUrl();
```

### 3. Upload nhiều ảnh
```java
// Upload nhiều ảnh
List<MultipartFile> files = Arrays.asList(file1, file2, file3);
MultipleImageUploadResponseDTO result = cloudinaryService.uploadMultipleImages(files, "listings");

// Lấy danh sách URLs
List<String> urls = result.uploadedImages()
    .stream()
    .map(img -> img.secureUrl())
    .collect(Collectors.toList());
```

### 4. Xóa ảnh
```java
// Xóa một ảnh
boolean deleted = cloudinaryService.deleteImage("listings/image_1234567890");

// Xóa nhiều ảnh
List<String> publicIds = Arrays.asList("id1", "id2", "id3");
int deletedCount = cloudinaryService.deleteMultipleImages(publicIds);
```

### 5. Tạo URL với kích thước tùy chỉnh
```java
String transformedUrl = cloudinaryService.getTransformedImageUrl("listings/image_1234567890", 300, 200);
```

## Tích hợp với ListingService

### Upload ảnh khi tạo listing
```java
// Trong ListingService
List<String> imageUrls = uploadListingImages(files, listingId);

// Lưu URLs vào database
for (String url : imageUrls) {
    ListingImageEntity image = new ListingImageEntity();
    image.setImageUrl(url);
    image.setListing(listing);
    listingImageRepository.save(image);
}
```

### Xóa ảnh khi cập nhật listing
```java
// Lấy public IDs từ URLs cũ
List<String> publicIds = oldImageUrls.stream()
    .map(url -> extractPublicIdFromUrl(url))
    .collect(Collectors.toList());

// Xóa ảnh cũ
int deletedCount = deleteListingImages(publicIds);
```

## Folder Structure trên Cloudinary

```
📁 Root
├── 📁 general/          # Ảnh chung
├── 📁 listings/         # Ảnh listings
│   ├── 📁 listing_1/    # Ảnh của listing ID 1
│   ├── 📁 listing_2/    # Ảnh của listing ID 2
│   └── 📁 temp_xxx/     # Ảnh tạm khi chưa có listing ID
├── 📁 avatars/          # Avatar users
│   ├── 📁 user_1/       # Avatar của user ID 1
│   └── 📁 user_2/       # Avatar của user ID 2
└── 📁 products/         # Ảnh sản phẩm khác
```

## Best Practices

### 1. Naming Convention
- Tên file tự động được clean (loại bỏ ký tự đặc biệt)
- Thêm timestamp để tránh trùng lặp
- Format: `{cleanName}_{timestamp}`

### 2. Error Handling
- Validate file type và size trước khi upload
- Sử dụng try-catch để xử lý exceptions
- Log errors để debug

### 3. Performance
- Upload nhiều ảnh sử dụng CompletableFuture (parallel processing)
- Thread pool với 5 threads đồng thời
- Optimize images tự động với quality="auto:good"

### 4. Security
- Validate file content type
- Giới hạn file size (10MB)
- Chỉ cho phép upload image files

## Error Codes

| Error | Description |
|-------|-------------|
| `File cannot be null or empty` | File không được null hoặc rỗng |
| `File size exceeds maximum limit` | File vượt quá 10MB |
| `File format not supported` | Format file không được hỗ trợ |
| `File must be a valid image` | File không phải là ảnh hợp lệ |
| `Failed to upload image` | Lỗi upload lên Cloudinary |

## Testing

### 1. Test với Postman
```bash
# Upload một ảnh
curl -X POST "http://localhost:8080/api/images/upload-single?folder=test" \
  -F "file=@/path/to/image.jpg"

# Upload nhiều ảnh
curl -X POST "http://localhost:8080/api/images/upload-multiple?folder=test" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg"
```

### 2. Test với Frontend
```javascript
// Upload một ảnh
const formData = new FormData();
formData.append('file', file);
formData.append('folder', 'listings');

fetch('/api/images/upload-single', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => console.log(data));

// Upload nhiều ảnh
const formData = new FormData();
files.forEach(file => formData.append('files', file));

fetch('/api/images/upload-multiple?folder=listings', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

## Monitoring & Maintenance

### 1. Logs
- Upload success/failure được log
- Performance metrics
- Error tracking

### 2. Cloudinary Dashboard
- Monitor usage quota
- Check storage limits
- View transformation usage

### 3. Cleanup
- Xóa ảnh tạm thường xuyên
- Cleanup ảnh của listings đã xóa
- Monitor storage usage

---

## Kết luận

CloudinaryService đã được thiết lập hoàn chỉnh và ready để sử dụng cho:
- ✅ Upload ảnh listing bán xe/pin
- ✅ Upload avatar users  
- ✅ Upload nhiều ảnh cùng lúc
- ✅ Quản lý và xóa ảnh
- ✅ Tối ưu performance và security
- ✅ Error handling và logging đầy đủ

Service này sẽ giúp cải thiện trải nghiệm người dùng khi upload ảnh và đảm bảo hiệu suất tốt cho ứng dụng.
