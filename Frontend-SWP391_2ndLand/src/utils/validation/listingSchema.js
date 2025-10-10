import * as yup from 'yup';

// Common helpers
const CURRENT_YEAR = new Date().getFullYear();

export const vehicleSchema = yup.object({
  type: yup.string().oneOf(['CAR','BIKE','MOTORBIKE']).required(),
  brand: yup.string().trim().min(2,'Hãng quá ngắn').max(60,'Quá dài').required('Hãng bắt buộc'),
  name: yup.string().trim().min(1).max(80).required('Tên xe bắt buộc'),
  model: yup.string().trim().nullable().default(null), // có thể để trống nếu chưa rõ
  year: yup.number().typeError('Năm không hợp lệ').min(2010,'>= 2010').max(CURRENT_YEAR + 1,'Quá lớn').required(),
  mileage: yup.number().typeError('Số km không hợp lệ').min(0,'>=0').nullable().transform(v=> (v === '' || isNaN(v)) ? undefined : v),
  batteryCapacity: yup.number()
    .typeError('Dung lượng pin không hợp lệ')
    .min(0.5,'>=0.5')
    .max(200,'<=200 kWh')
    .required('Dung lượng pin bắt buộc'),
  conditionStatus: yup.string().oneOf(['EXCELLENT','GOOD','FAIR','POOR','NEEDS_MAINTENANCE']).required()
});

export const batterySchema = yup.object({
  brand: yup.string().trim().min(2).max(60).required('Hãng pin bắt buộc'),
  model: yup.string().trim().min(1,'Model bắt buộc').max(80).required('Model pin bắt buộc'),
  capacity: yup.number().typeError('Dung lượng không hợp lệ').positive('>0').max(200,'<=200 kWh').required(),
  healthPercentage: yup.number().typeError('% pin không hợp lệ').min(1,'>=1').max(100,'<=100').required(),
  compatibleVehicles: yup.string().trim().nullable().default(null),
  conditionStatus: yup.string().oneOf(['EXCELLENT','GOOD','FAIR','POOR','NEEDS_REPLACEMENT']).required()
});

export const locationSchema = yup.object({
  province: yup.string().trim().min(2).required('Tỉnh/TP bắt buộc'),
  district: yup.string().trim().min(2,'>=2 ký tự').required('Quận/Huyện bắt buộc'),
  details: yup.string().trim().nullable().default(null)
});

export const baseListingSchema = yup.object({
  title: yup.string().trim().min(5,'>=5 ký tự').max(120,'<=120 ký tự').required('Tiêu đề bắt buộc'),
  description: yup.string().trim().nullable().default(null),
  price: yup.number().typeError('Giá phải là số').positive('Giá > 0').required('Giá bắt buộc'),
  productType: yup.string().oneOf(['VEHICLE','BATTERY']).required(),
  images: yup.array().min(1,'Cần ít nhất 1 ảnh').max(10,'Tối đa 10 ảnh').test(
    'is-file-or-url',
    'Ảnh phải là File hoặc URL hợp lệ',
    (value) => {
      if (!value) return false;
      return value.every(item => item instanceof File || (typeof item === 'string' && item.length > 0));
    }
  ),
  mainImageIndex: yup.number().min(0).default(0),
  vehicle: vehicleSchema.when('productType', {
    is: 'VEHICLE',
    then: schema => schema.required(),
    otherwise: schema => schema.strip()
  }),
  battery: batterySchema.when('productType', {
    is: 'BATTERY',
    then: schema => schema.required(),
    otherwise: schema => schema.strip()
  }),
  location: locationSchema
});

export const buildPayload = (values) => {
  return {
    title: values.title.trim(),
    description: values.description || null,
    price: Number(values.price),
    listingType: 'NORMAL',
    category: values.productType === 'VEHICLE' ? 'EV' : 'BATTERY',
    mainImageUrl: values.images[values.mainImageIndex] || values.images[0],
    imageUrls: values.images,
    product: values.productType === 'VEHICLE' ? {
      ev: {
        type: values.vehicle.type,
        name: values.vehicle.name,
        model: values.vehicle.model || null,
        brand: values.vehicle.brand,
        year: values.vehicle.year,
        mileage: values.vehicle.mileage || 0,
  batteryCapacity: values.vehicle.batteryCapacity, // đã validate >0 ở schema
        conditionStatus: values.vehicle.conditionStatus
      },
      battery: null
    } : {
      ev: null,
      battery: {
        brand: values.battery.brand,
        model: values.battery.model || null,
        capacity: values.battery.capacity,
        healthPercentage: values.battery.healthPercentage,
        compatibleVehicles: values.battery.compatibleVehicles || null,
        conditionStatus: values.battery.conditionStatus
      }
    },
    location: {
      province: values.location.province,
  district: values.location.district,
      details: values.location.details || null
    }
  };
};
