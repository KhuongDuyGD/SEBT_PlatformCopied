/**
 * Hằng số cho các options filter - Sync với backend enum values
 * Đảm bảo frontend filter options match chính xác với database
 */

// Loại xe điện - VehicleType enum
export const VEHICLE_TYPES = [
  { value: 'CAR', label: 'Ô tô điện' },
  { value: 'MOTORBIKE', label: 'Xe máy điện' },
  { value: 'BIKE', label: 'Xe đạp điện' }
];

// Tình trạng xe điện - VehicleCondition enum  
export const VEHICLE_CONDITIONS = [
  { value: 'EXCELLENT', label: 'Xuất sắc' },
  { value: 'GOOD', label: 'Tốt' },
  { value: 'FAIR', label: 'Khá' },
  { value: 'POOR', label: 'Kém' },
  { value: 'NEEDS_MAINTENANCE', label: 'Cần bảo trì' }
];

// Tình trạng pin - BatteryCondition enum
export const BATTERY_CONDITIONS = [
  { value: 'EXCELLENT', label: 'Xuất sắc' },
  { value: 'GOOD', label: 'Tốt' },
  { value: 'FAIR', label: 'Khá' },
  { value: 'POOR', label: 'Kém' },
  { value: 'NEEDS_REPLACEMENT', label: 'Cần thay thế' }
];

// Trạng thái listing - ListingStatus enum (cho admin filter)
export const LISTING_STATUSES = [
  { value: 'DRAFT', label: 'Bản nháp' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'PAY_WAITING', label: 'Chờ thanh toán' },
  { value: 'SOLD', label: 'Đã bán' },
  { value: 'EXPIRED', label: 'Hết hạn' },
  { value: 'REMOVED', label: 'Đã xóa' },
  { value: 'SUSPENDED', label: 'Tạm ngưng' }
];

// Price ranges cho quick filter (VND)
export const PRICE_RANGES = [
  { value: null, label: 'Tất cả mức giá' },
  { value: { min: 0, max: 100000000 }, label: 'Dưới 100 triệu' },
  { value: { min: 100000000, max: 300000000 }, label: '100 - 300 triệu' },
  { value: { min: 300000000, max: 500000000 }, label: '300 - 500 triệu' },
  { value: { min: 500000000, max: 800000000 }, label: '500 - 800 triệu' },
  { value: { min: 800000000, max: 1200000000 }, label: '800 triệu - 1.2 tỷ' },
  { value: { min: 1200000000, max: null }, label: 'Trên 1.2 tỷ' }
];

// Year ranges cho quick filter
export const YEAR_RANGES = [
  { value: null, label: 'Tất cả năm' },
  { value: { min: 2024, max: null }, label: '2024' },
  { value: { min: 2023, max: 2023 }, label: '2023' },
  { value: { min: 2022, max: 2022 }, label: '2022' },
  { value: { min: 2021, max: 2021 }, label: '2021' },
  { value: { min: 2020, max: 2020 }, label: '2020' },
  { value: { min: 2018, max: 2019 }, label: '2018-2019' }
];

// Battery capacity ranges (kWh) cho quick filter
export const BATTERY_CAPACITY_RANGES = [
  { value: null, label: 'Tất cả dung lượng' },
  { value: { min: 0, max: 50 }, label: 'Dưới 50 kWh' },
  { value: { min: 50, max: 70 }, label: '50 - 70 kWh' },
  { value: { min: 70, max: 90 }, label: '70 - 90 kWh' },
  { value: { min: 90, max: null }, label: 'Trên 90 kWh' }
];

// Mileage ranges (km) cho EV filter
export const MILEAGE_RANGES = [
  { value: null, label: 'Tất cả quãng đường' },
  { value: { min: 0, max: 10000 }, label: 'Dưới 10,000 km' },
  { value: { min: 10000, max: 30000 }, label: '10,000 - 30,000 km' },
  { value: { min: 30000, max: 50000 }, label: '30,000 - 50,000 km' },
  { value: { min: 50000, max: 80000 }, label: '50,000 - 80,000 km' },
  { value: { min: 80000, max: null }, label: 'Trên 80,000 km' }
];

// Battery health ranges (%) cho battery filter
export const BATTERY_HEALTH_RANGES = [
  { value: null, label: 'Tất cả sức khỏe pin' },
  { value: { min: 90, max: 100 }, label: '90% - 100%' },
  { value: { min: 80, max: 89 }, label: '80% - 89%' },
  { value: { min: 70, max: 79 }, label: '70% - 79%' },
  { value: { min: 60, max: 69 }, label: '60% - 69%' }
];

// Sorting options
export const SORT_OPTIONS = [
  { value: 'createdAt_desc', label: 'Mới nhất' },
  { value: 'createdAt_asc', label: 'Cũ nhất' },
  { value: 'price_asc', label: 'Giá thấp → cao' },
  { value: 'price_desc', label: 'Giá cao → thấp' },
  { value: 'viewsCount_desc', label: 'Nhiều lượt xem nhất' },
  { value: 'year_desc', label: 'Năm sản xuất mới nhất' },
  { value: 'mileage_asc', label: 'Ít km nhất (EV)' },
  { value: 'batteryCapacity_desc', label: 'Dung lượng pin cao nhất' },
  { value: 'healthPercentage_desc', label: 'Sức khỏe pin tốt nhất (Battery)' }
];

// Helper functions để format giá trị
export const formatPrice = (price) => {
  if (!price) return 'Liên hệ';
  
  if (price >= 1000000000) {
    return `${(price / 1000000000).toFixed(1)} tỷ`;
  } else if (price >= 1000000) {
    return `${Math.round(price / 1000000)} triệu`;
  } else {
    return `${price.toLocaleString('vi-VN')} VNĐ`;
  }
};

export const formatYear = (year) => {
  return year ? `Năm ${year}` : 'Không rõ năm';
};

export const formatMileage = (mileage) => {
  if (!mileage) return '0 km';
  return `${mileage.toLocaleString('vi-VN')} km`;
};

export const formatBatteryCapacity = (capacity) => {
  return capacity ? `${capacity} kWh` : 'Không rõ';
};

export const formatBatteryHealth = (health) => {
  return health ? `${health}%` : 'Không rõ';
};

// Danh sách tỉnh/thành phố Việt Nam
export const PROVINCES = [
  'Hà Nội',
  'TP. Hồ Chí Minh',
  'Đà Nẵng',
  'Hải Phòng',
  'Cần Thơ',
  'An Giang',
  'Bà Rịa - Vũng Tàu',
  'Bắc Giang',
  'Bắc Kạn',
  'Bạc Liêu',
  'Bắc Ninh',
  'Bến Tre',
  'Bình Định',
  'Bình Dương',
  'Bình Phước',
  'Bình Thuận',
  'Cà Mau',
  'Cao Bằng',
  'Đắk Lắk',
  'Đắk Nông',
  'Điện Biên',
  'Đồng Nai',
  'Đồng Tháp',
  'Gia Lai',
  'Hà Giang',
  'Hà Nam',
  'Hà Tĩnh',
  'Hải Dương',
  'Hậu Giang',
  'Hòa Bình',
  'Hưng Yên',
  'Khánh Hòa',
  'Kiên Giang',
  'Kon Tum',
  'Lai Châu',
  'Lâm Đồng',
  'Lạng Sơn',
  'Lào Cai',
  'Long An',
  'Nam Định',
  'Nghệ An',
  'Ninh Bình',
  'Ninh Thuận',
  'Phú Thọ',
  'Phú Yên',
  'Quảng Bình',
  'Quảng Nam',
  'Quảng Ngãi',
  'Quảng Ninh',
  'Quảng Trị',
  'Sóc Trăng',
  'Sơn La',
  'Tây Ninh',
  'Thái Bình',
  'Thái Nguyên',
  'Thanh Hóa',
  'Thừa Thiên Huế',
  'Tiền Giang',
  'Trà Vinh',
  'Tuyên Quang',
  'Vĩnh Long',
  'Vĩnh Phúc',
  'Yên Bái'
];

// Danh sách năm sản xuất (từ 2000 đến hiện tại)
export const YEARS = (() => {
  const currentYear = new Date().getFullYear();
  const startYear = 2000;
  const years = [];
  
  for (let year = currentYear; year >= startYear; year--) {
    years.push(year);
  }
  
  return years;
})();

// Helper function để tìm label từ value
export const getLabelByValue = (options, value) => {
  const option = options.find(opt => opt.value === value);
  return option ? option.label : value;
};
