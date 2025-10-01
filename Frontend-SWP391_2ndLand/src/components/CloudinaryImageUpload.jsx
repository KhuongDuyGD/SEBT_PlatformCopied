import React, { useState } from 'react';
import { Upload, Image, X, AlertCircle } from 'lucide-react';

/**
 * Component upload ảnh lên Cloudinary cho dự án SWP391
 * 
 * HƯỚNG DẪN SETUP CLOUDINARY:
 * 1. Đăng nhập Cloudinary Dashboard với tài khoản SWP391
 * 2. Tạo Upload Preset:
 *    - Vào Settings > Upload > Add Upload Preset
 *    - Preset name: 'swp391_upload' 
 *    - Signing Mode: 'Unsigned' (quan trọng!)
 *    - Asset folder: 'swp391/listings' (khuyến nghị cho tổ chức file)
 *    - Save preset
 * 
 * HƯỚNG DẪN TEST VỚI CLOUDINARY KHÁC:
 * - Thay đổi CLOUDINARY_CLOUD_NAME và CLOUDINARY_UPLOAD_PRESET ở dòng 24-25
 * - Tạo upload preset tương tự trong tài khoản Cloudinary của bạn
 * 
 * @param {Object} props - Props của component
 * @param {Function} props.onImageUpload - Callback khi upload thành công
 * @param {string} props.currentImage - URL ảnh hiện tại
 * @param {string} props.className - CSS class tùy chọn
 * @param {boolean} props.disabled - Trạng thái disable
 */
const CloudinaryImageUpload = ({ 
  onImageUpload, 
  currentImage = '', 
  className = '',
  disabled = false
}) => {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [previewImage, setPreviewImage] = useState(currentImage);

  // Cấu hình Cloudinary - SWP391 Project  
  // QUAN TRỌNG: Để đảm bảo hoạt động ổn định, chúng ta include cả API key
  const CLOUDINARY_CLOUD_NAME = 'SWP391'; // Cloud name của dự án SWP391
  const CLOUDINARY_UPLOAD_PRESET = 'swp391_upload'; // Upload preset phải là Unsigned mode
  const CLOUDINARY_API_KEY = '246726946671738'; // API key của SWP391 (để đảm bảo authentication)

  /**
   * Xử lý upload ảnh lên Cloudinary
   * @param {File} file - File ảnh được chọn
   */
  const handleImageUpload = async (file) => {
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setError('Vui lòng chọn file ảnh hợp lệ');
      return;
    }

    // Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      setError('Kích thước file không được vượt quá 10MB');
      return;
    }

    try {
      setUploading(true);
      setError(null);

      // Tạo FormData với đầy đủ thông tin cần thiết
      const formData = new FormData();
      formData.append('file', file);
      formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET);
      formData.append('api_key', CLOUDINARY_API_KEY); // Thêm API key để đảm bảo authentication
      formData.append('folder', 'swp391/listings'); // Folder để tổ chức file
      
      // Debug information chi tiết
      console.log('🔄 Bắt đầu upload ảnh với cấu hình:', {
        cloud_name: CLOUDINARY_CLOUD_NAME,
        upload_preset: CLOUDINARY_UPLOAD_PRESET,
        api_key: CLOUDINARY_API_KEY ? `${CLOUDINARY_API_KEY.substring(0, 8)}...` : 'không có',
        file_name: file.name,
        file_size: `${(file.size / 1024 / 1024).toFixed(2)}MB`,
        file_type: file.type,
        endpoint: `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`
      });

      // Gọi API Cloudinary với endpoint chính xác
      const uploadUrl = `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`;
      
      const response = await fetch(uploadUrl, {
        method: 'POST',
        body: formData,
        // Không set Content-Type header, để browser tự handle với FormData
      });

      const data = await response.json();

      if (response.ok && data.secure_url) {
        // Upload thành công
        const imageUrl = data.secure_url;
        console.log('✅ Upload ảnh thành công:', {
          url: imageUrl,
          public_id: data.public_id,
          folder: data.folder,
          format: data.format,
          resource_type: data.resource_type,
          created_at: data.created_at,
          bytes: data.bytes,
          width: data.width,
          height: data.height
        });
        
        setPreviewImage(imageUrl);
        onImageUpload(imageUrl);
      } else {
        // Debug chi tiết lỗi Cloudinary
        console.error('❌ Cloudinary Upload Error - Response Details:', {
          status: response.status,
          statusText: response.statusText,
          ok: response.ok,
          headers: Object.fromEntries(response.headers.entries()),
          error_data: data.error,
          full_response: data
        });
        
        // Phân tích loại lỗi cụ thể
        let errorMessage = 'Upload ảnh thất bại';
        let errorDetails = '';
        
        if (data.error) {
          errorMessage = data.error.message || data.error;
          errorDetails = `Error code: ${data.error.http_code || response.status}`;
          
          // Các lỗi thường gặp và giải thích
          if (errorMessage.includes('API key')) {
            errorDetails += ' - Lỗi API key không hợp lệ';
          } else if (errorMessage.includes('preset')) {
            errorDetails += ' - Upload preset không tồn tại hoặc chưa cấu hình đúng';
          } else if (errorMessage.includes('signature')) {
            errorDetails += ' - Lỗi xác thực chữ ký';
          } else if (errorMessage.includes('resource')) {
            errorDetails += ' - Lỗi tài nguyên hoặc giới hạn upload';
          }
        }
        
        console.error('📋 Error Analysis:', {
          message: errorMessage,
          details: errorDetails,
          suggestion: 'Kiểm tra Cloudinary dashboard và cấu hình preset'
        });
        
        throw new Error(`${errorMessage}${errorDetails ? ` (${errorDetails})` : ''}`);
      }
    } catch (err) {
      // Enhanced error logging với thông tin chi tiết
      console.error('🚫 Exception trong quá trình upload:', {
        error_name: err.name,
        error_message: err.message,
        error_stack: err.stack,
        cloudinary_config: {
          cloud_name: CLOUDINARY_CLOUD_NAME,
          upload_preset: CLOUDINARY_UPLOAD_PRESET,
          api_key_exists: !!CLOUDINARY_API_KEY
        },
        file_info: {
          name: file.name,
          size: file.size,
          type: file.type
        },
        timestamp: new Date().toISOString()
      });
      
      // Set user-friendly error message dựa trên loại lỗi
      let userErrorMessage = 'Có lỗi xảy ra khi upload ảnh';
      
      if (err.message.includes('API key')) {
        userErrorMessage = '❌ Lỗi cấu hình Cloudinary: API key không hợp lệ';
      } else if (err.message.includes('preset')) {
        userErrorMessage = '❌ Lỗi upload preset: Preset chưa được cấu hình đúng';
      } else if (err.message.includes('network') || err.message.includes('fetch')) {
        userErrorMessage = '🌐 Lỗi kết nối mạng: Vui lòng kiểm tra internet và thử lại';
      } else if (err.message.includes('signature')) {
        userErrorMessage = '🔐 Lỗi xác thực: Signature không hợp lệ';
      } else if (err.message.includes('size') || err.message.includes('large')) {
        userErrorMessage = '📦 File quá lớn: Vui lòng chọn ảnh nhỏ hơn 10MB';
      } else if (err.message.includes('format') || err.message.includes('type')) {
        userErrorMessage = '🖼️ Định dạng file không hỗ trợ: Chỉ chấp nhận JPG, PNG, GIF';
      }
      
      console.error('👤 User Error Message:', userErrorMessage);
      setError(userErrorMessage);
    } finally {
      setUploading(false);
    }
  };

  /**
   * Xử lý khi người dùng chọn file
   */
  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      handleImageUpload(file);
    }
  };

  /**
   * Xử lý xóa ảnh
   */
  const handleRemoveImage = () => {
    setPreviewImage('');
    onImageUpload('');
    setError(null);
  };

  /**
   * Xử lý drag & drop
   */
  const handleDrop = (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) {
      handleImageUpload(file);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  return (
    <div className={`cloudinary-upload-container ${className}`}>
      {/* Hiển thị ảnh preview nếu có */}
      {previewImage ? (
        <div className="image-preview-container">
          <div className="relative">
            <img 
              src={previewImage} 
              alt="Preview" 
              className="preview-image"
            />
            {!disabled && (
              <button
                type="button"
                onClick={handleRemoveImage}
                className="remove-image-btn"
                title="Xóa ảnh"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      ) : (
        /* Upload area */
        <div 
          className={`upload-area ${uploading ? 'uploading' : ''} ${disabled ? 'disabled' : ''}`}
          onDrop={handleDrop}
          onDragOver={handleDragOver}
        >
          <input
            type="file"
            accept="image/*"
            onChange={handleFileSelect}
            disabled={disabled || uploading}
            className="file-input"
            id="image-upload"
          />
          <label htmlFor="image-upload" className="upload-label">
            <div className="upload-content">
              {uploading ? (
                <>
                  <div className="loading-spinner"></div>
                  <p>Đang upload ảnh lên Cloudinary...</p>
                  <p className="text-sm opacity-75">Vui lòng đợi trong giây lát</p>
                </>
              ) : (
                <>
                  <Upload className="upload-icon" />
                  <p className="upload-text">
                    <span className="upload-text-primary">Kích vào để chọn ảnh</span>
                    <br />
                    <span className="upload-text-secondary">hoặc kéo thả ảnh vào đây</span>
                  </p>
                  <p className="upload-info">
                    Hỗ trợ: JPG, PNG, GIF (tối đa 10MB)
                  </p>
                </>
              )}
            </div>
          </label>
        </div>
      )}

      {/* Hiển thị lỗi với khả năng đóng */}
      {error && (
        <div className="upload-error">
          <AlertCircle className="w-4 h-4 mr-2" />
          <span>{error}</span>
          <button 
            type="button" 
            onClick={() => setError(null)}
            className="ml-2 text-sm underline hover:no-underline"
            title="Đóng thông báo lỗi"
          >
            Đóng
          </button>
        </div>
      )}
    </div>
  );
};

export default CloudinaryImageUpload;
