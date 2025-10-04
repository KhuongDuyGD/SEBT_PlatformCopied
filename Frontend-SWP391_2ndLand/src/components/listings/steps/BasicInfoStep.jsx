import React from 'react';
import { FileText, DollarSign } from 'lucide-react';
import CloudinaryImageUpload from '../../CloudinaryImageUpload';

// Added register + errors for react-hook-form binding
export default function BasicInfoStep({ formData, onChange, onImagesUpload, onChangeMainImage, loading, register, errors }) {
  const regTitle = register ? register('title') : {};
  const regPrice = register ? register('price') : {};
  const regDescription = register ? register('description') : {};
  return (
    <div>
      <div className="step-header">
        <h2 className="step-header-title">
          <FileText className="w-7 h-7 text-blue-500 mr-3" />
          Thông tin cơ bản
        </h2>
        <p className="step-header-subtitle">Nhập thông tin cơ bản về listing của bạn</p>
      </div>
      <div className="step-content">
        <div className="form-row cols-2">
          <div className="form-group">
            <label className="form-label required">Tiêu đề listing</label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={(e)=> { onChange(e); regTitle.onChange && regTitle.onChange(e); }}
              ref={regTitle.ref}
              className={`form-input ${errors?.title ? 'input-error' : ''}`}
              placeholder="VD: VinFast VF8 2023 như mới"
              required
            />
            {errors?.title && <p className="field-error">{errors.title.message}</p>}
          </div>
          <div className="form-group">
            <label className="form-label required">Giá bán (VND)</label>
            <div className="input-with-icon">
              <DollarSign className="input-icon w-5 h-5" />
              <input
                type="number"
                name="price"
                value={formData.price}
                onChange={(e)=> { onChange(e); regPrice.onChange && regPrice.onChange(e); }}
                ref={regPrice.ref}
                className={`form-input with-icon ${errors?.price ? 'input-error' : ''}`}
                placeholder="500000000"
                required
              />
            </div>
            {errors?.price && <p className="field-error">{errors.price.message}</p>}
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Mô tả chi tiết</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={(e)=> { onChange(e); regDescription.onChange && regDescription.onChange(e); }}
            ref={regDescription.ref}
            rows={4}
            className={`form-textarea ${errors?.description ? 'input-error' : ''}`}
            placeholder="Mô tả chi tiết về sản phẩm, tình trạng, lịch sử sử dụng..."
          />
          {errors?.description && <p className="field-error">{errors.description.message}</p>}
        </div>
        <div className="form-group">
          <label className="form-label">Ảnh sản phẩm (chọn ảnh chính)</label>
          <CloudinaryImageUpload
            onImagesUpload={onImagesUpload}
            currentImages={formData.images}
            disabled={loading}
            className="mt-2"
            selectMain
            mainIndex={formData.mainImageIndex}
            onChangeMain={onChangeMainImage}
          />
          <p className="text-sm text-gray-500 mt-2">Tối đa 10 ảnh. Nhấn vào ngôi sao để đặt ảnh chính.</p>
          {errors?.images && <p className="field-error">{errors.images.message}</p>}
        </div>
      </div>
    </div>
  );
}
