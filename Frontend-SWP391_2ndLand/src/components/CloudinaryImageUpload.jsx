import React, { useState, useCallback } from 'react';
import { Upload, Image, X, AlertCircle, CheckCircle, Loader } from 'lucide-react';

/**
 * Component upload nhiều ảnh đồng thời lên Cloudinary cho dự án SWP391
 * HƯỚNG DẪN SETUP CLOUDINARY:
 * 1. Đăng nhập Cloudinary Dashboard với tài khoản SWP391
 * 2. Tạo Upload Preset:
 *    - Vào Settings > Upload > Add Upload Preset
 *    - Preset name: 'swp391_upload'
 *    - Signing Mode: 'Unsigned' (quan trọng!)
 *    - Asset folder: 'swp391/listings' (khuyến nghị cho tổ chức file)
 *    - Save preset
 *
 * @param {Object} props - Props của component
 * @param {Function} props.onImagesUpload - Callback khi upload thành công (nhận array URLs)
 * @param {Array} props.currentImages - Array URLs ảnh hiện tại
 * @param {string} props.className - CSS class tùy chọn
 * @param {boolean} props.disabled - Trạng thái disable
 * @param {number} props.maxFiles - Số lượng file tối đa (default: 10)
 * @param {number} props.maxConcurrent - Số upload đồng thời tối đa (default: 3)
 * @param {boolean} props.allowMultiple - Cho phép chọn nhiều file (default: true)
 */
const CloudinaryImageUpload = ({
                                   onImagesUpload,
                                   currentImages = [],
                                   className = '',
                                   disabled = false,
                                   maxFiles = 10,
                                   maxConcurrent = 3, // Giới hạn 3 upload đồng thời để tránh quá tải
                                   allowMultiple = true
                               }) => {
    const [uploadQueue, setUploadQueue] = useState([]); // Queue các file đang chờ upload
    const [activeUploads, setActiveUploads] = useState(new Map()); // Map tracking active uploads
    const [uploadedImages, setUploadedImages] = useState(currentImages); // Array các ảnh đã upload
    const [globalError, setGlobalError] = useState(null);

    // Cấu hình Cloudinary - SWP391 Project
    const CLOUDINARY_CLOUD_NAME = 'SWP391';
    const CLOUDINARY_UPLOAD_PRESET = 'swp391_upload';
    const CLOUDINARY_API_KEY = '246726946671738';

    /**
     * Upload một file với progress tracking
     * @param {File} file - File cần upload
     * @param {string} uploadId - Unique ID cho upload này
     */
    const uploadSingleFile = useCallback(async (file, uploadId) => {
        try {
            // Update trạng thái upload bắt đầu
            setActiveUploads(prev => new Map(prev.set(uploadId, {
                file,
                status: 'uploading',
                progress: 0,
                error: null,
                url: null
            })));

            console.log(`🔄 [${uploadId}] Bắt đầu upload:`, {
                name: file.name,
                size: `${(file.size / 1024 / 1024).toFixed(2)}MB`,
                type: file.type
            });

            // Tạo FormData
            const formData = new FormData();
            formData.append('file', file);
            formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET);
            formData.append('api_key', CLOUDINARY_API_KEY);
            formData.append('folder', 'swp391/listings');

            // Upload với XMLHttpRequest để track progress
            const uploadPromise = new Promise((resolve, reject) => {
                const xhr = new XMLHttpRequest();

                // Track upload progress
                xhr.upload.addEventListener('progress', (e) => {
                    if (e.lengthComputable) {
                        const progress = Math.round((e.loaded * 100) / e.total);
                        setActiveUploads(prev => {
                            const updated = new Map(prev);
                            const current = updated.get(uploadId);
                            if (current) {
                                updated.set(uploadId, { ...current, progress });
                            }
                            return updated;
                        });
                    }
                });

                xhr.addEventListener('load', () => {
                    if (xhr.status >= 200 && xhr.status < 300) {
                        try {
                            const response = JSON.parse(xhr.responseText);
                            resolve(response);
                        } catch (e) {
                            reject(new Error('Invalid JSON response'));
                        }
                    } else {
                        reject(new Error(`HTTP ${xhr.status}: ${xhr.statusText}`));
                    }
                });

                xhr.addEventListener('error', () => {
                    reject(new Error('Network error occurred'));
                });

                xhr.addEventListener('timeout', () => {
                    reject(new Error('Upload timeout'));
                });

                // Cấu hình request
                xhr.timeout = 60000; // 60 seconds timeout
                xhr.open('POST', `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`);
                xhr.send(formData);
            });

            const data = await uploadPromise;

            if (data.secure_url) {
                console.log(`✅ [${uploadId}] Upload thành công:`, {
                    url: data.secure_url,
                    public_id: data.public_id,
                    format: data.format,
                    bytes: data.bytes
                });

                // Update trạng thái thành công
                setActiveUploads(prev => {
                    const updated = new Map(prev);
                    updated.set(uploadId, {
                        file,
                        status: 'success',
                        progress: 100,
                        error: null,
                        url: data.secure_url
                    });
                    return updated;
                });

                // Thêm vào danh sách ảnh đã upload
                setUploadedImages(prev => {
                    const newImages = [...prev, data.secure_url];
                    onImagesUpload(newImages); // Callback với danh sách mới
                    return newImages;
                });

                return data.secure_url;
            } else {
                throw new Error(data.error?.message || 'Upload failed');
            }

        } catch (error) {
            console.error(`❌ [${uploadId}] Upload failed:`, error);

            // Update trạng thái lỗi
            setActiveUploads(prev => {
                const updated = new Map(prev);
                updated.set(uploadId, {
                    file,
                    status: 'error',
                    progress: 0,
                    error: error.message,
                    url: null
                });
                return updated;
            });

            throw error;
        }
    }, [CLOUDINARY_CLOUD_NAME, CLOUDINARY_UPLOAD_PRESET, CLOUDINARY_API_KEY, onImagesUpload]);

    /**
     * Process upload queue với giới hạn concurrent
     */
    const processUploadQueue = useCallback(async () => {
        setUploadQueue(prev => {
            const currentActiveCount = Array.from(activeUploads.values())
                .filter(upload => upload.status === 'uploading').length;

            const availableSlots = maxConcurrent - currentActiveCount;

            if (availableSlots <= 0 || prev.length === 0) {
                return prev;
            }

            // Lấy các file cần upload
            const filesToUpload = prev.slice(0, availableSlots);
            const remainingQueue = prev.slice(availableSlots);

            // Bắt đầu upload các file
            filesToUpload.forEach(({ file, uploadId }) => {
                uploadSingleFile(file, uploadId).catch(error => {
                    console.error(`Upload failed for ${uploadId}:`, error);
                });
            });

            return remainingQueue;
        });
    }, [activeUploads, maxConcurrent, uploadSingleFile]);

    /**
     * Thêm files vào upload queue
     * @param {FileList} files - Danh sách files cần upload
     */
    const addFilesToQueue = useCallback((files) => {
        const fileArray = Array.from(files);

        // Validate số lượng file
        const totalFiles = uploadedImages.length + activeUploads.size + uploadQueue.length + fileArray.length;
        if (totalFiles > maxFiles) {
            setGlobalError(`Chỉ được upload tối đa ${maxFiles} ảnh. Hiện tại: ${uploadedImages.length + activeUploads.size + uploadQueue.length}`);
            return;
        }

        // Validate từng file
        const validFiles = [];
        for (const file of fileArray) {
            if (!file.type.startsWith('image/')) {
                setGlobalError(`File "${file.name}" không phải là ảnh hợp lệ`);
                continue;
            }

            if (file.size > 10 * 1024 * 1024) {
                setGlobalError(`File "${file.name}" vượt quá 10MB`);
                continue;
            }

            validFiles.push(file);
        }

        if (validFiles.length === 0) return;

        // Thêm vào queue với unique IDs
        const newQueueItems = validFiles.map(file => ({
            file,
            uploadId: `upload_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        }));

        setUploadQueue(prev => [...prev, ...newQueueItems]);
        setGlobalError(null);

        console.log(`📋 Added ${validFiles.length} files to upload queue`);
    }, [uploadedImages.length, activeUploads.size, uploadQueue.length, maxFiles]);

    // Auto-process queue khi có thay đổi
    React.useEffect(() => {
        if (uploadQueue.length > 0) {
            processUploadQueue();
        }
    }, [uploadQueue, processUploadQueue]);

    /**
     * Xử lý khi người dùng chọn files
     */
    const handleFileSelect = (e) => {
        const files = e.target.files;
        if (files && files.length > 0) {
            addFilesToQueue(files);
        }
        // Reset input để cho phép chọn lại file
        e.target.value = '';
    };

    /**
     * Retry upload cho file thất bại
     */
    const retryUpload = (uploadId) => {
        const upload = activeUploads.get(uploadId);
        if (upload && upload.status === 'error') {
            setUploadQueue(prev => [...prev, { file: upload.file, uploadId }]);
            setActiveUploads(prev => {
                const updated = new Map(prev);
                updated.delete(uploadId);
                return updated;
            });
        }
    };

    /**
     * Xóa ảnh đã upload
     */
    const removeUploadedImage = (indexToRemove) => {
        setUploadedImages(prev => {
            const newImages = prev.filter((_, index) => index !== indexToRemove);
            onImagesUpload(newImages);
            return newImages;
        });
    };

    /**
     * Hủy upload đang thực hiện
     */
    const cancelUpload = (uploadId) => {
        setActiveUploads(prev => {
            const updated = new Map(prev);
            updated.delete(uploadId);
            return updated;
        });
    };

    /**
     * Xử lý drag & drop
     */
    const handleDrop = (e) => {
        e.preventDefault();
        const files = e.dataTransfer.files;
        if (files && files.length > 0) {
            addFilesToQueue(files);
        }
    };

    const handleDragOver = (e) => {
        e.preventDefault();
    };

    // Tính toán trạng thái tổng quát
    const totalUploading = Array.from(activeUploads.values()).filter(u => u.status === 'uploading').length;
    const totalQueue = uploadQueue.length;
    const totalErrors = Array.from(activeUploads.values()).filter(u => u.status === 'error').length;
    const isUploading = totalUploading > 0 || totalQueue > 0;

    return (
        <div className={`cloudinary-multiupload-container ${className}`}>
            {/* Upload Area */}
            <div
                className={`upload-area ${isUploading ? 'uploading' : ''} ${disabled ? 'disabled' : ''}`}
                onDrop={handleDrop}
                onDragOver={handleDragOver}
            >
                <input
                    type="file"
                    accept="image/*"
                    multiple={allowMultiple}
                    onChange={handleFileSelect}
                    disabled={disabled}
                    className="file-input"
                    id="images-upload"
                />
                <label htmlFor="images-upload" className="upload-label">
                    <div className="upload-content">
                        <Upload className="upload-icon" />
                        <p className="upload-text">
              <span className="upload-text-primary">
                {allowMultiple ? 'Kích để chọn nhiều ảnh' : 'Kích để chọn ảnh'}
              </span>
                            <br />
                            <span className="upload-text-secondary">hoặc kéo thả ảnh vào đây</span>
                        </p>
                        <p className="upload-info">
                            Hỗ trợ: JPG, PNG, GIF (tối đa 10MB/ảnh, {maxFiles} ảnh)
                        </p>
                    </div>
                </label>
            </div>

            {/* Upload Status Summary */}
            {(isUploading || totalErrors > 0) && (
                <div className="upload-summary">
                    <div className="upload-stats">
                        {totalUploading > 0 && (
                            <span className="stat uploading">
                <Loader className="w-4 h-4 animate-spin mr-1" />
                                {totalUploading} đang upload
              </span>
                        )}
                        {totalQueue > 0 && (
                            <span className="stat queued">
                📋 {totalQueue} chờ xử lý
              </span>
                        )}
                        {uploadedImages.length > 0 && (
                            <span className="stat success">
                <CheckCircle className="w-4 h-4 mr-1" />
                                {uploadedImages.length} thành công
              </span>
                        )}
                        {totalErrors > 0 && (
                            <span className="stat error">
                <AlertCircle className="w-4 h-4 mr-1" />
                                {totalErrors} lỗi
              </span>
                        )}
                    </div>
                </div>
            )}

            {/* Active Uploads Progress */}
            {activeUploads.size > 0 && (
                <div className="active-uploads">
                    <h4 className="uploads-title">Tiến trình upload:</h4>
                    {Array.from(activeUploads.entries()).map(([uploadId, upload]) => (
                        <div key={uploadId} className={`upload-item ${upload.status}`}>
                            <div className="upload-item-info">
                                <span className="file-name">{upload.file.name}</span>
                                <span className="file-size">
                  ({(upload.file.size / 1024 / 1024).toFixed(2)}MB)
                </span>
                            </div>

                            <div className="upload-item-progress">
                                {upload.status === 'uploading' && (
                                    <>
                                        <div className="progress-bar">
                                            <div
                                                className="progress-fill"
                                                style={{ width: `${upload.progress}%` }}
                                            ></div>
                                        </div>
                                        <span className="progress-text">{upload.progress}%</span>
                                        <button
                                            onClick={() => cancelUpload(uploadId)}
                                            className="cancel-btn"
                                            title="Hủy upload"
                                        >
                                            <X className="w-3 h-3" />
                                        </button>
                                    </>
                                )}

                                {upload.status === 'success' && (
                                    <div className="success-indicator">
                                        <CheckCircle className="w-4 h-4 text-green-500" />
                                        <span>Thành công</span>
                                    </div>
                                )}

                                {upload.status === 'error' && (
                                    <div className="error-indicator">
                                        <AlertCircle className="w-4 h-4 text-red-500" />
                                        <span className="error-text">{upload.error}</span>
                                        <button
                                            onClick={() => retryUpload(uploadId)}
                                            className="retry-btn"
                                            title="Thử lại"
                                        >
                                            🔄
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Uploaded Images Gallery */}
            {uploadedImages.length > 0 && (
                <div className="uploaded-images-gallery">
                    <h4 className="gallery-title">Ảnh đã upload ({uploadedImages.length}):</h4>
                    <div className="images-grid">
                        {uploadedImages.map((imageUrl, index) => (
                            <div key={index} className="image-item">
                                <img
                                    src={imageUrl}
                                    alt={`Uploaded ${index + 1}`}
                                    className="gallery-image"
                                />
                                {!disabled && (
                                    <button
                                        onClick={() => removeUploadedImage(index)}
                                        className="remove-image-btn"
                                        title="Xóa ảnh"
                                    >
                                        <X className="w-4 h-4" />
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Global Error */}
            {globalError && (
                <div className="upload-error">
                    <AlertCircle className="w-4 h-4 mr-2" />
                    <span>{globalError}</span>
                    <button
                        onClick={() => setGlobalError(null)}
                        className="ml-2 text-sm underline hover:no-underline"
                    >
                        Đóng
                    </button>
                </div>
            )}
        </div>
    );
};

export default CloudinaryImageUpload;