import React, { useState, useRef } from 'react';
import { Upload, Image as ImageIcon, X, AlertCircle, Star } from 'lucide-react';
import './CloudinaryImageUploadGallery.css';

/**
 * Component upload ảnh LOCAL - GIỮ FILE OBJECTS thay vì upload lên Cloudinary
 * Files sẽ được gửi trực tiếp xuống backend khi submit form
 *
 * @param {Object} props - Props của component
 * @param {Function} props.onFilesSelect - Callback khi chọn files (nhận array File objects)
 * @param {Array} props.currentFiles - Array File objects hiện tại
 * @param {boolean} props.selectMain - Cho phép chọn ảnh chính (default false)
 * @param {number} props.mainIndex - Index ảnh chính hiện tại
 * @param {Function} props.onChangeMain - Callback khi đổi ảnh chính
 * @param {string} props.className - CSS class tùy chọn
 * @param {boolean} props.disabled - Trạng thái disable
 * @param {number} props.maxFiles - Số lượng file tối đa (default: 10)
 * @param {number} props.maxFileSize - Kích thước file tối đa MB (default: 10)
 */
const LocalImageUpload = ({
    onFilesSelect,
    currentFiles = [],
    className = '',
    disabled = false,
    maxFiles = 10,
    maxFileSize = 10,
    selectMain = false,
    mainIndex = 0,
    onChangeMain
}) => {
    const [error, setError] = useState(null);
    const [previewUrls, setPreviewUrls] = useState([]);
    const fileInputRef = useRef(null);

    // Tạo preview URLs khi currentFiles thay đổi
    React.useEffect(() => {
        // Cleanup old URLs
        previewUrls.forEach(url => {
            if (url.startsWith('blob:')) {
                URL.revokeObjectURL(url);
            }
        });

        // Tạo preview URLs mới
        const newPreviews = currentFiles.map(file => {
            if (file instanceof File) {
                return URL.createObjectURL(file);
            }
            return file; // Nếu là string URL (fallback)
        });

        setPreviewUrls(newPreviews);

        // Cleanup on unmount
        return () => {
            newPreviews.forEach(url => {
                if (url.startsWith('blob:')) {
                    URL.revokeObjectURL(url);
                }
            });
        };
    }, [currentFiles]);

    /**
     * Validate file trước khi thêm
     */
    const validateFile = (file) => {
        // Check file type
        if (!file.type.startsWith('image/')) {
            return `${file.name} không phải là file ảnh`;
        }

        // Check file size
        const sizeMB = file.size / 1024 / 1024;
        if (sizeMB > maxFileSize) {
            return `${file.name} vượt quá ${maxFileSize}MB (${sizeMB.toFixed(2)}MB)`;
        }

        return null;
    };

    /**
     * Handle file selection
     */
    const handleFileSelect = (e) => {
        const files = Array.from(e.target.files || []);
        setError(null);

        if (files.length === 0) return;

        // Check total count
        if (currentFiles.length + files.length > maxFiles) {
            setError(`Chỉ có thể upload tối đa ${maxFiles} ảnh. Hiện tại: ${currentFiles.length}`);
            return;
        }

        // Validate each file
        const validFiles = [];
        const errors = [];

        for (const file of files) {
            const validationError = validateFile(file);
            if (validationError) {
                errors.push(validationError);
            } else {
                validFiles.push(file);
            }
        }

        if (errors.length > 0) {
            setError(errors.join('; '));
        }

        if (validFiles.length > 0) {
            const newFiles = [...currentFiles, ...validFiles];
            onFilesSelect(newFiles);
            console.log(`✅ Đã chọn ${validFiles.length} ảnh:`, validFiles.map(f => f.name));
        }

        // Reset input để có thể chọn lại cùng file
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    /**
     * Remove image
     */
    const handleRemoveImage = (index) => {
        const newFiles = currentFiles.filter((_, i) => i !== index);
        onFilesSelect(newFiles);

        // Adjust main index if needed
        if (selectMain && onChangeMain) {
            if (index === mainIndex) {
                onChangeMain(0);
            } else if (index < mainIndex) {
                onChangeMain(mainIndex - 1);
            }
        }
    };

    /**
     * Handle main image selection
     */
    const handleSetMainImage = (index) => {
        if (selectMain && onChangeMain) {
            onChangeMain(index);
        }
    };

    return (
        <div className={`cloudinary-upload-container ${className}`}>
            {/* Upload Button */}
            <div className="upload-dropzone">
                <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    multiple
                    onChange={handleFileSelect}
                    disabled={disabled || currentFiles.length >= maxFiles}
                    className="hidden"
                    id="file-upload-input"
                />
                <label
                    htmlFor="file-upload-input"
                    className={`upload-label ${disabled || currentFiles.length >= maxFiles ? 'disabled' : ''}`}
                >
                    <Upload className="w-8 h-8 mb-3" />
                    <span className="upload-label-title">
                        {currentFiles.length >= maxFiles
                            ? `Đã đủ ${maxFiles} ảnh`
                            : 'Chọn ảnh từ máy tính'
                        }
                    </span>
                    <span className="upload-label-subtitle">
                        Tối đa {maxFiles} ảnh, mỗi ảnh &lt; {maxFileSize}MB
                    </span>
                </label>
            </div>

            {/* Error Display */}
            {error && (
                <div className="upload-error">
                    <AlertCircle className="w-5 h-5 mr-2" />
                    <span>{error}</span>
                </div>
            )}

            {/* Preview Grid */}
            {previewUrls.length > 0 && (
                <div className="preview-grid">
                    {previewUrls.map((url, index) => (
                        <div
                            key={`preview-${index}`}
                            className={`preview-item ${selectMain && index === mainIndex ? 'main-image' : ''}`}
                        >
                            <img
                                src={url}
                                alt={`Preview ${index + 1}`}
                                className="preview-image"
                            />

                            {/* Main Image Badge */}
                            {selectMain && index === mainIndex && (
                                <div className="main-badge">
                                    <Star className="w-4 h-4 fill-current" />
                                    <span>Ảnh chính</span>
                                </div>
                            )}

                            {/* Image Actions */}
                            <div className="preview-actions">
                                {selectMain && (
                                    <button
                                        type="button"
                                        onClick={() => handleSetMainImage(index)}
                                        className="action-button set-main"
                                        title="Đặt làm ảnh chính"
                                    >
                                        <Star className={`w-4 h-4 ${index === mainIndex ? 'fill-current' : ''}`} />
                                    </button>
                                )}
                                <button
                                    type="button"
                                    onClick={() => handleRemoveImage(index)}
                                    className="action-button remove"
                                    title="Xóa ảnh"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>

                            {/* File Info */}
                            <div className="file-info">
                                <span className="file-name">
                                    {currentFiles[index]?.name || `Image ${index + 1}`}
                                </span>
                                <span className="file-size">
                                    {currentFiles[index]?.size
                                        ? `${(currentFiles[index].size / 1024 / 1024).toFixed(2)} MB`
                                        : ''
                                    }
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Upload Summary */}
            {currentFiles.length > 0 && (
                <div className="upload-summary">
                    <ImageIcon className="w-4 h-4 mr-2" />
                    <span>Đã chọn {currentFiles.length}/{maxFiles} ảnh</span>
                    {selectMain && (
                        <span className="ml-4 text-sm text-gray-500">
                            Ảnh chính: #{mainIndex + 1}
                        </span>
                    )}
                </div>
            )}
        </div>
    );
};

export default LocalImageUpload;

