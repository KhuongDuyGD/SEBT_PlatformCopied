import React from 'react';
import { MapPin, CheckCircle } from 'lucide-react';

// Added register + errors props
export default function LocationReviewStep({ formData, onChange, register, errors }) {
  const regProvince = register ? register('location.province') : {};
  const regDistrict = register ? register('location.district') : {};
  const regDetails = register ? register('location.details') : {};
  return (
    <div>
      <div className="step-header">
        <h2 className="step-header-title">
          <MapPin className="w-7 h-7 text-blue-500 mr-3" />
          Vị trí & Hoàn tất
        </h2>
        <p className="step-header-subtitle">Nhập thông tin vị trí và xem lại toàn bộ listing</p>
      </div>
      <div className="step-content">
        <div className="form-group mb-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
            <MapPin className="w-5 h-5 text-blue-500 mr-2" />
            Thông tin vị trí
          </h3>
          <div className="form-row cols-2">
            <div className="form-group">
              <label className="form-label required">Tỉnh/Thành phố</label>
              <input
                type="text"
                name="location.province"
                value={formData.location.province}
                onChange={(e)=> { onChange(e); regProvince.onChange && regProvince.onChange(e);} }
                ref={regProvince.ref}
                className={`form-input ${errors?.location?.province ? 'input-error' : ''}`}
                placeholder="VD: Hà Nội"
                required
              />
              {errors?.location?.province && <p className="field-error">{errors.location.province.message}</p>}
            </div>
            <div className="form-group">
              <label className="form-label">Quận/Huyện</label>
              <input
                type="text"
                name="location.district"
                value={formData.location.district}
                onChange={(e)=> { onChange(e); regDistrict.onChange && regDistrict.onChange(e);} }
                ref={regDistrict.ref}
                className="form-input"
                placeholder="VD: Ba Đình"
              />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Địa chỉ chi tiết</label>
            <textarea
              name="location.details"
              value={formData.location.details}
              onChange={(e)=> { onChange(e); regDetails.onChange && regDetails.onChange(e);} }
              ref={regDetails.ref}
              rows={3}
              className="form-textarea"
              placeholder="Số nhà, tên đường, phường/xã..."
            />
          </div>
        </div>
        <div className="review-section">
          <div className="review-header">
            <CheckCircle className="w-6 h-6" />
            <h3>Xem lại thông tin listing</h3>
          </div>
          <div className="space-y-3">
            <div className="review-item">
              <span className="review-label">Tiêu đề:</span>
              <span className="review-value">{formData.title || 'Chưa nhập'}</span>
            </div>
            <div className="review-item">
              <span className="review-label">Giá:</span>
              <span className="review-value font-bold">{formData.price ? `${Number(formData.price).toLocaleString()} VND` : 'Chưa nhập'}</span>
            </div>
            <div className="review-item">
              <span className="review-label">Loại sản phẩm:</span>
              <span className="review-value">{formData.productType === 'VEHICLE' ? 'Xe điện' : 'Pin'}</span>
            </div>
            {formData.productType === 'VEHICLE' && (
              <div className="review-item">
                <span className="review-label">Xe:</span>
                <span className="review-value">{formData.vehicle.brand} {formData.vehicle.name} ({formData.vehicle.year})</span>
              </div>
            )}
            {formData.productType === 'BATTERY' && (
              <div className="review-item">
                <span className="review-label">Pin:</span>
                <span className="review-value">{formData.battery.brand} - {formData.battery.capacity}kWh ({formData.battery.healthPercentage}%)</span>
              </div>
            )}
            <div className="review-item">
              <span className="review-label">Vị trí:</span>
              <span className="review-value">{formData.location.province || 'Chưa nhập'}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
