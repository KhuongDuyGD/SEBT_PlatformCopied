# 🔧 HƯỚNG DẪN SETUP CLOUDINARY CHO DỰ ÁN SWP391

## 📋 Thông tin Cloudinary dự án

**Tài khoản SWP391:**
- **Cloud Name:** `SWP391`
- **API Key:** `246726946671738`
- **API Secret:** `mJbxND9lhZVit4vKBY6LEvX5qtU`
- **Dashboard URL:** https://cloudinary.com/console

---

## ⚙️ CẤU HÌNH UPLOAD PRESET (Bắt buộc)

### Bước 1: Đăng nhập Cloudinary Dashboard
1. Truy cập: https://cloudinary.com/console
2. Đăng nhập với tài khoản SWP391

### Bước 2: Tạo Upload Preset
1. Vào **Settings** > **Upload**
2. Click **"Add Upload Preset"**
3. Điền thông tin:
   ```
   Preset name: swp391_upload
   Signing Mode: Unsigned ⚠️ (Quan trọng!)
   Asset folder: swp391/listings (khuyến nghị để tổ chức file)
   ```
4. **Advanced settings** (tùy chọn):
   - Auto-tagging: `product,listing,swp391`
   - Max file size: `10 MB`
   - Allowed formats: `jpg,png,jpeg,gif`
5. Click **Save**

### Bước 3: Verify Setup
- Upload preset name: `swp391_upload`
- Signing mode phải là: `Unsigned`
- Asset folder: `swp391/listings`
- URL endpoint: `https://api.cloudinary.com/v1_1/SWP391/image/upload`

### Bước 4: Kiểm tra Folder Structure
Sau khi upload, ảnh sẽ được lưu với cấu trúc:
```
📁 SWP391 (Cloud)
  └── 📁 swp391/
      └── 📁 listings/
          ├── 🖼️ car_image_xyz123.jpg
          ├── 🖼️ battery_image_abc456.png
          └── 🖼️ ...
```

URL ảnh sẽ có format:
```
https://res.cloudinary.com/SWP391/image/upload/v1234567890/swp391/listings/filename.jpg
```

---

## 🧪 TESTING VỚI CLOUDINARY KHÁC

Nếu bạn muốn test với tài khoản Cloudinary khác:

### Option 1: Thay đổi trực tiếp trong code
```javascript
// File: src/components/CloudinaryImageUpload.jsx (dòng 24-25)
const CLOUDINARY_CLOUD_NAME = 'your-cloud-name';
const CLOUDINARY_UPLOAD_PRESET = 'your-upload-preset';
```

### Option 2: Sử dụng environment variables
1. Copy `.env.example` thành `.env`
2. Cập nhật:
   ```
   REACT_APP_CLOUDINARY_CLOUD_NAME=your-cloud-name
   REACT_APP_CLOUDINARY_UPLOAD_PRESET=your-upload-preset
   ```
3. Cập nhật component để sử dụng `process.env`

### Setup Upload Preset cho tài khoản khác:
1. Đăng nhập dashboard Cloudinary của bạn
2. Settings > Upload > Add Upload Preset
3. **Quan trọng:** Chọn Signing Mode = "Unsigned"
4. Save và sử dụng preset name đó

---

## 🔒 BẢO MẬT

### ⚠️ Lưu ý quan trọng:
- **API Secret không được sử dụng trong client-side**
- Chỉ sử dụng Cloud Name và Upload Preset trong React
- Upload preset phải ở chế độ "Unsigned"
- File `.env` đã được thêm vào `.gitignore`

### Các thông tin cần bảo mật:
```javascript
// ✅ An toàn - sử dụng trong client
Cloud Name: SWP391
Upload Preset: swp391_upload

// ❌ Không dùng trong client - chỉ server-side
API Key: 246726946671738
API Secret: mJbxND9lhZVit4vKBY6LEvX5qtU
```

---

## 🐛 TROUBLESHOOTING

### Lỗi thường gặp:

1. **"Upload preset not found"**
   - Kiểm tra preset name chính xác: `swp391_upload`
   - Đảm bảo preset đã được tạo và saved

2. **"Unsigned upload disabled"**
   - Upload preset phải ở chế độ "Unsigned"
   - Vào Settings > Upload > Edit preset > Signing Mode = Unsigned

3. **"Invalid cloud name"**
   - Kiểm tra cloud name: `SWP391` (case-sensitive)
   - Đảm bảo không có khoảng trắng thừa

4. **CORS Error**
   - Cloudinary tự động handle CORS cho uploads
   - Kiểm tra network requests trong Developer Tools

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra Console logs trong trình duyệt
2. Verify upload preset trong Cloudinary dashboard  
3. Test với Cloudinary's upload widget demo
4. Liên hệ team development

---

*Cập nhật: 1 tháng 10, 2025*
*Dự án: SWP391 - 2ndLand Platform*
