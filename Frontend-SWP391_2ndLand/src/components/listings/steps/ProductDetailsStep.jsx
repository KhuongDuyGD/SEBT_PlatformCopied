import React from 'react';
import { Car, Battery } from 'lucide-react';
import { formatNumberWithDots, createFormattedInputHandler } from '../../../utils/numberFormatting';

// Added register + errors props
export default function ProductDetailsStep({ formData, onChange, register, errors }) {
  const getReg = (name) => register ? register(name) : {};
  return (
    <div>
      <div className="step-header">
        <h2 className="step-header-title">
          {formData.productType === 'VEHICLE' ? (
            <Car className="w-7 h-7 text-blue-500 mr-3" />
          ) : (
            <Battery className="w-7 h-7 text-blue-500 mr-3" />
          )}
          Chi tiết sản phẩm
        </h2>
        <p className="step-header-subtitle">
          Nhập thông tin chi tiết về {formData.productType === 'VEHICLE' ? 'xe điện' : 'pin'}
        </p>
      </div>
      <div className="step-content">
        {formData.productType === 'VEHICLE' && (
          <div className="vehicle-form-section">
            <div className="vehicle-form-header">
              <Car className="w-6 h-6 text-blue-600" />
              <h3>Thông tin xe điện</h3>
            </div>
            <div className="form-row cols-3">
              <div className="form-group">
                <label className="form-label required">Loại xe</label>
                <select
                  name="vehicle.type"
                  value={formData.vehicle.type}
                  onChange={(e)=> { onChange(e); const r=getReg('vehicle.type'); r.onChange && r.onChange(e); }}
                  ref={getReg('vehicle.type').ref}
                  className={`form-select ${errors?.vehicle?.type ? 'input-error' : ''}`}
                >
                  <option value="CAR">Ô tô điện</option>
                  <option value="BIKE">Xe đạp điện</option>
                  <option value="MOTORBIKE">Xe máy điện</option>
                </select>
                {errors?.vehicle?.type && <p className="field-error">{errors.vehicle.type.message}</p>}
              </div>
              <div className="form-group">
                <label className="form-label required">Hãng xe</label>
                <input
                  type="text"
                  name="vehicle.brand"
                  value={formData.vehicle.brand}
                  onChange={(e)=> { onChange(e); const r=getReg('vehicle.brand'); r.onChange && r.onChange(e); }}
                  ref={getReg('vehicle.brand').ref}
                  className={`form-input ${errors?.vehicle?.brand ? 'input-error' : ''}`}
                  placeholder="VD: VinFast"
                  required
                />
                {errors?.vehicle?.brand && <p className="field-error">{errors.vehicle.brand.message}</p>}
              </div>
              <div className="form-group">
                <label className="form-label required">Tên xe</label>
                <input
                  type="text"
                  name="vehicle.name"
                  value={formData.vehicle.name}
                  onChange={(e)=> { onChange(e); const r=getReg('vehicle.name'); r.onChange && r.onChange(e); }}
                  ref={getReg('vehicle.name').ref}
                  className={`form-input ${errors?.vehicle?.name ? 'input-error' : ''}`}
                  placeholder="VD: VF8"
                  required
                />
                {errors?.vehicle?.name && <p className="field-error">{errors.vehicle.name.message}</p>}
              </div>
            </div>
            <div className="form-row cols-3">
              <div className="form-group">
                <label className="form-label">Model</label>
                <input
                  type="text"
                  name="vehicle.model"
                  value={formData.vehicle.model}
                  onChange={(e)=> { onChange(e); const r=getReg('vehicle.model'); r.onChange && r.onChange(e); }}
                  ref={getReg('vehicle.model').ref}
                  className="form-input"
                  placeholder="VD: Plus"
                />
              </div>
              <div className="form-group">
                <label className="form-label required">Năm sản xuất</label>
                <input
                  type="number"
                  name="vehicle.year"
                  value={formData.vehicle.year}
                  onChange={(e)=> { onChange(e); const r=getReg('vehicle.year'); r.onChange && r.onChange(e); }}
                  ref={getReg('vehicle.year').ref}
                  min="2000"
                  max={new Date().getFullYear() + 1}
                  className={`form-input ${errors?.vehicle?.year ? 'input-error' : ''}`}
                  required
                />
                {errors?.vehicle?.year && <p className="field-error">{errors.vehicle.year.message}</p>}
              </div>
              <div className="form-group">
                <label className="form-label">Số km đã đi</label>
                <input
                  type="text"
                  name="vehicle.mileage"
                  // Display formatted value with dots for better readability
                  value={formatNumberWithDots(formData.vehicle.mileage)}
                  // Use custom handler to format input and pass raw value to form
                  onChange={createFormattedInputHandler((e) => {
                    onChange(e);
                    const r = getReg('vehicle.mileage');
                    r.onChange && r.onChange(e);
                  }, 'vehicle.mileage')}
                  ref={getReg('vehicle.mileage').ref}
                  className="form-input"
                  placeholder="100.000"
                />
              </div>
            </div>
            <div className="form-row cols-2">
              <div className="form-group">
                <label className="form-label required">Dung lượng pin (kWh)</label>
                <input
                  type="text"
                  name="vehicle.batteryCapacity"
                  // Display formatted value with dots for large numbers
                  value={formatNumberWithDots(formData.vehicle.batteryCapacity)}
                  // Use custom handler to format input and pass raw value to form
                  onChange={createFormattedInputHandler((e) => {
                    onChange(e);
                    const r = getReg('vehicle.batteryCapacity');
                    r.onChange && r.onChange(e);
                  }, 'vehicle.batteryCapacity')}
                  ref={getReg('vehicle.batteryCapacity').ref}
                  className={`form-input ${errors?.vehicle?.batteryCapacity ? 'input-error' : ''}`}
                  placeholder="75.3"
                  required
                />
                {errors?.vehicle?.batteryCapacity && <p className="field-error">{errors.vehicle.batteryCapacity.message}</p>}
              </div>
              <div className="form-group">
                <label className="form-label required">Tình trạng</label>
                <select
                  name="vehicle.conditionStatus"
                  value={formData.vehicle.conditionStatus}
                  onChange={(e)=> { onChange(e); const r=getReg('vehicle.conditionStatus'); r.onChange && r.onChange(e); }}
                  ref={getReg('vehicle.conditionStatus').ref}
                  className="form-select"
                >
                  <option value="EXCELLENT">⭐ Xuất sắc</option>
                  <option value="GOOD">👍 Tốt</option>
                  <option value="FAIR">👌 Khá</option>
                  <option value="POOR">👎 Kém</option>
                  <option value="NEEDS_MAINTENANCE">🔧 Cần bảo trì</option>
                </select>
              </div>
            </div>
          </div>
        )}
        {formData.productType === 'BATTERY' && (
          <div className="battery-form-section">
            <div className="battery-form-header">
              <Battery className="w-6 h-6 text-amber-600" />
              <h3>Thông tin pin xe điện</h3>
            </div>
            <div className="form-row cols-2">
              <div className="form-group">
                <label className="form-label required">Hãng pin</label>
                <input
                  type="text"
                  name="battery.brand"
                  value={formData.battery.brand}
                  onChange={(e)=> { onChange(e); const r=getReg('battery.brand'); r.onChange && r.onChange(e); }}
                  ref={getReg('battery.brand').ref}
                  className={`form-input ${errors?.battery?.brand ? 'input-error' : ''}`}
                  placeholder="VD: CATL, LG Chem, BYD"
                  required
                />
                {errors?.battery?.brand && <p className="field-error">{errors.battery.brand.message}</p>}
              </div>
              <div className="form-group">
                <label className="form-label">Model pin</label>
                <input
                  type="text"
                  name="battery.model"
                  value={formData.battery.model}
                  onChange={(e)=> { onChange(e); const r=getReg('battery.model'); r.onChange && r.onChange(e); }}
                  ref={getReg('battery.model').ref}
                  className="form-input"
                  placeholder="VD: NCM523, LFP"
                />
              </div>
            </div>
            <div className="form-row cols-2">
              <div className="form-group">
                <label className="form-label required">Dung lượng (kWh)</label>
                <input
                  type="text"
                  name="battery.capacity"
                  // Display formatted value with dots for large numbers
                  value={formatNumberWithDots(formData.battery.capacity)}
                  // Use custom handler to format input and pass raw value to form
                  onChange={createFormattedInputHandler((e) => {
                    onChange(e);
                    const r = getReg('battery.capacity');
                    r.onChange && r.onChange(e);
                  }, 'battery.capacity')}
                  ref={getReg('battery.capacity').ref}
                  className={`form-input ${errors?.battery?.capacity ? 'input-error' : ''}`}
                  placeholder="75.3"
                  required
                />
                {errors?.battery?.capacity && <p className="field-error">{errors.battery.capacity.message}</p>}
              </div>
              <div className="form-group">
                <label className="form-label required">Độ khỏe pin (%)</label>
                <input
                  type="text"
                  name="battery.healthPercentage"
                  // Display formatted value with dots for consistency
                  value={formatNumberWithDots(formData.battery.healthPercentage)}
                  // Use custom handler to format input and pass raw value to form
                  onChange={createFormattedInputHandler((e) => {
                    onChange(e);
                    const r = getReg('battery.healthPercentage');
                    r.onChange && r.onChange(e);
                  }, 'battery.healthPercentage')}
                  ref={getReg('battery.healthPercentage').ref}
                  className={`form-input ${errors?.battery?.healthPercentage ? 'input-error' : ''}`}
                  placeholder="85"
                  min="0"
                  max="100"
                  required
                />
                {errors?.battery?.healthPercentage && <p className="field-error">{errors.battery.healthPercentage.message}</p>}
              </div>
            </div>
            <div className="form-row cols-2">
              <div className="form-group">
                <label className="form-label required">Tình trạng</label>
                <select
                  name="battery.conditionStatus"
                  value={formData.battery.conditionStatus}
                  onChange={(e)=> { onChange(e); const r=getReg('battery.conditionStatus'); r.onChange && r.onChange(e); }}
                  ref={getReg('battery.conditionStatus').ref}
                  className="form-select"
                >
                  <option value="EXCELLENT">⭐ Xuất sắc</option>
                  <option value="GOOD">👍 Tốt</option>
                  <option value="FAIR">👌 Khá</option>
                  <option value="POOR">👎 Kém</option>
                  <option value="NEEDS_REPLACEMENT">🔄 Cần thay thế</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Xe tương thích</label>
                <input
                  type="text"
                  name="battery.compatibleVehicles"
                  value={formData.battery.compatibleVehicles}
                  onChange={(e)=> { onChange(e); const r=getReg('battery.compatibleVehicles'); r.onChange && r.onChange(e); }}
                  ref={getReg('battery.compatibleVehicles').ref}
                  className="form-input"
                  placeholder="VD: VinFast VF8, VF9"
                />
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
