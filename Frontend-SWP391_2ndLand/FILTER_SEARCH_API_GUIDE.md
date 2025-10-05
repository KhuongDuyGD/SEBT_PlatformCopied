# Hướng Dẫn Sử Dụng API Filter & Search - Frontend

## 📋 Mục Lục
- [Base URL](#base-url)
- [1. Tìm Kiếm Theo Từ Khóa](#1-tìm-kiếm-theo-từ-khóa)
- [2. Lọc EV (Xe Điện)](#2-lọc-ev-xe-điện)
- [3. Lọc Pin (Battery)](#3-lọc-pin-battery)
- [4. Kết Hợp Nhiều Filter](#4-kết-hợp-nhiều-filter)
- [5. Pagination](#5-pagination)
- [6. React Examples](#6-react-examples)
- [7. Error Handling](#7-error-handling)

---

## Base URL
```
http://localhost:8080/api/listings
```

---

## 1. Tìm Kiếm Theo Từ Khóa

### 🎯 Endpoint
```
GET /api/listings/search
```

### 📝 Mô Tả
Tìm kiếm listing theo từ khóa trong title, description, brand, model.

### ⚙️ Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| keyword | string | ✅ Yes | - | Từ khóa tìm kiếm |
| page | number | ❌ No | 0 | Số trang (bắt đầu từ 0) |
| size | number | ❌ No | 12 | Số items/trang (max: 100) |

### 📤 Request Example

**JavaScript/Axios:**
```javascript
const searchListings = async (keyword, page = 0, size = 12) => {
  try {
    const response = await axios.get('/api/listings/search', {
      params: {
        keyword: keyword,
        page: page,
        size: size
      },
      headers: {
        'X-User-ID': userId // hoặc dùng session cookie
      }
    });
    return response.data;
  } catch (error) {
    console.error('Search error:', error);
    throw error;
  }
};

// Sử dụng
searchListings('Tesla Model 3', 0, 12);
```

**Fetch API:**
```javascript
const searchListings = async (keyword, page = 0, size = 12) => {
  const url = new URL('/api/listings/search', 'http://localhost:8080');
  url.searchParams.append('keyword', keyword);
  url.searchParams.append('page', page);
  url.searchParams.append('size', size);
  
  const response = await fetch(url, {
    headers: {
      'X-User-ID': userId
    }
  });
  return response.json();
};
```

### 📥 Response Example

**Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Tesla Model 3 2023",
      "price": 800000000,
      "thumbnail": "https://res.cloudinary.com/xxx/image.jpg",
      "location": "Hồ Chí Minh",
      "isFavorite": false,
      "brand": "Tesla",
      "model": "Model 3",
      "year": 2023
    },
    {
      "id": 2,
      "title": "Tesla Model Y Long Range",
      "price": 1200000000,
      "thumbnail": "https://res.cloudinary.com/xxx/image2.jpg",
      "location": "Hà Nội",
      "isFavorite": true,
      "brand": "Tesla",
      "model": "Model Y",
      "year": 2023
    }
  ],
  "pagination": {
    "currentPage": 0,
    "totalPages": 3,
    "totalElements": 35,
    "size": 12,
    "hasNext": true,
    "hasPrevious": false,
    "isFirst": true,
    "isLast": false
  },
  "keyword": "Tesla",
  "message": "Tìm thấy 35 kết quả cho 'Tesla'"
}
```

**Error (400):**
```json
{
  "success": false,
  "message": "Từ khóa tìm kiếm không được để trống"
}
```

### 💡 Tips
- Từ khóa có thể chứa khoảng trắng
- Tìm kiếm không phân biệt hoa thường
- Kết quả bao gồm cả EV và Battery

---

## 2. Lọc EV (Xe Điện)

### 🎯 Endpoint
```
GET /api/listings/ev-filter
```

### 📝 Mô Tả
Lọc xe điện theo giá, loại xe, năm sản xuất.

### ⚙️ Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| minPrice | number | ❌ No | null | Giá tối thiểu (VNĐ) |
| maxPrice | number | ❌ No | null | Giá tối đa (VNĐ) |
| vehicleType | string | ❌ No | null | Loại xe (xem enum bên dưới) |
| year | number | ❌ No | null | Năm sản xuất |
| page | number | ❌ No | 0 | Số trang |
| size | number | ❌ No | 12 | Số items/trang |

### 🚗 VehicleType Enum
```javascript
const VehicleType = {
  CAR: 'CAR',           // Xe hơi chung
  SEDAN: 'SEDAN',       // Sedan
  SUV: 'SUV',           // SUV
  TRUCK: 'TRUCK',       // Xe tải
  BUS: 'BUS',           // Xe buýt
  MOTORBIKE: 'MOTORBIKE' // Xe máy
};
```

### 📤 Request Examples

**1. Lọc theo giá:**
```javascript
const filterByPrice = async () => {
  const response = await axios.get('/api/listings/ev-filter', {
    params: {
      minPrice: 500000000,  // 500 triệu
      maxPrice: 1000000000, // 1 tỷ
      page: 0,
      size: 12
    },
    headers: { 'X-User-ID': userId }
  });
  return response.data;
};
```

**2. Lọc theo loại xe:**
```javascript
const filterByType = async () => {
  const response = await axios.get('/api/listings/ev-filter', {
    params: {
      vehicleType: 'SEDAN',
      page: 0,
      size: 12
    },
    headers: { 'X-User-ID': userId }
  });
  return response.data;
};
```

**3. Lọc theo năm:**
```javascript
const filterByYear = async () => {
  const response = await axios.get('/api/listings/ev-filter', {
    params: {
      year: 2023,
      page: 0,
      size: 12
    },
    headers: { 'X-User-ID': userId }
  });
  return response.data;
};
```

**4. Kết hợp nhiều filter:**
```javascript
const advancedFilter = async (filters) => {
  const response = await axios.get('/api/listings/ev-filter', {
    params: {
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      vehicleType: filters.vehicleType,
      year: filters.year,
      page: filters.page || 0,
      size: filters.size || 12
    },
    headers: { 'X-User-ID': userId }
  });
  return response.data;
};

// Sử dụng
advancedFilter({
  minPrice: 700000000,
  maxPrice: 1500000000,
  vehicleType: 'SUV',
  year: 2023
});
```

### 📥 Response Example

```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "title": "VinFast VF8 Plus 2023",
      "price": 950000000,
      "thumbnail": "https://res.cloudinary.com/xxx/vinfast.jpg",
      "location": "Hà Nội",
      "isFavorite": false,
      "brand": "VinFast",
      "model": "VF8 Plus",
      "year": 2023
    }
  ],
  "pagination": {
    "currentPage": 0,
    "totalPages": 2,
    "totalElements": 18,
    "size": 12,
    "hasNext": true,
    "hasPrevious": false,
    "isFirst": true,
    "isLast": false
  },
  "filters": {
    "year": 2023,
    "vehicleType": "SUV",
    "minPrice": 700000000,
    "maxPrice": 1500000000
  },
  "message": "Tìm thấy 18 kết quả"
}
```

### 💡 Tips
- Tất cả filter params đều **optional**
- Có thể dùng 1 hoặc nhiều filter cùng lúc
- `minPrice` và `maxPrice` có thể dùng riêng lẻ
- Kết quả được lọc theo **giao** của các điều kiện (AND logic)

---

## 3. Lọc Pin (Battery)

### 🎯 Endpoint
```
GET /api/listings/battery-filter
```

### 📝 Mô Tả
Lọc pin theo giá và năm sản xuất.

### ⚙️ Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| minPrice | number | ❌ No | null | Giá tối thiểu (VNĐ) |
| maxPrice | number | ❌ No | null | Giá tối đa (VNĐ) |
| year | number | ❌ No | null | Năm sản xuất |
| page | number | ❌ No | 0 | Số trang |
| size | number | ❌ No | 12 | Số items/trang |

### 📤 Request Examples

**1. Lọc theo giá:**
```javascript
const filterBatteryByPrice = async () => {
  const response = await axios.get('/api/listings/battery-filter', {
    params: {
      minPrice: 10000000,  // 10 triệu
      maxPrice: 50000000,  // 50 triệu
      page: 0,
      size: 12
    },
    headers: { 'X-User-ID': userId }
  });
  return response.data;
};
```

**2. Lọc theo năm:**
```javascript
const filterBatteryByYear = async () => {
  const response = await axios.get('/api/listings/battery-filter', {
    params: {
      year: 2023,
      page: 0,
      size: 12
    },
    headers: { 'X-User-ID': userId }
  });
  return response.data;
};
```

**3. Kết hợp nhiều filter:**
```javascript
const filterBattery = async (filters) => {
  const response = await axios.get('/api/listings/battery-filter', {
    params: {
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      year: filters.year,
      page: filters.page || 0,
      size: filters.size || 12
    },
    headers: { 'X-User-ID': userId }
  });
  return response.data;
};

// Sử dụng
filterBattery({
  minPrice: 20000000,
  maxPrice: 40000000,
  year: 2023
});
```

### 📥 Response Example

```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "title": "Pin LG Chem 60kWh",
      "price": 35000000,
      "thumbnail": "https://res.cloudinary.com/xxx/battery.jpg",
      "location": "Đà Nẵng",
      "isFavorite": false,
      "brand": "LG Chem",
      "model": "NCM811",
      "year": 2023
    }
  ],
  "pagination": {
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 8,
    "size": 12,
    "hasNext": false,
    "hasPrevious": false,
    "isFirst": true,
    "isLast": true
  },
  "filters": {
    "year": 2023,
    "minPrice": 20000000,
    "maxPrice": 40000000
  },
  "message": "Tìm thấy 8 kết quả"
}
```

---

## 4. Kết Hợp Nhiều Filter

### 📝 Logic Lọc
Backend sử dụng **AND logic** - kết quả phải thỏa mãn **TẤT CẢ** điều kiện filter.

### 💡 Examples

**Ví dụ 1: Tìm SUV năm 2023, giá từ 700tr - 1.5 tỷ**
```javascript
const filters = {
  vehicleType: 'SUV',
  year: 2023,
  minPrice: 700000000,
  maxPrice: 1500000000
};
```
→ Kết quả: Chỉ SUV năm 2023 VÀ giá từ 700tr-1.5 tỷ

**Ví dụ 2: Tìm xe năm 2022 trở lên**
```javascript
const filters = {
  year: 2022,  // Chỉ năm 2022
};
```
→ Backend hiện tại chỉ lọc năm chính xác, không hỗ trợ range

**Ví dụ 3: Tìm xe dưới 1 tỷ**
```javascript
const filters = {
  maxPrice: 1000000000  // Không set minPrice
};
```
→ Kết quả: Tất cả xe có giá ≤ 1 tỷ

---

## 5. Pagination

### 📊 Pagination Info Structure
```typescript
interface PaginationInfo {
  currentPage: number;      // Trang hiện tại (0-indexed)
  totalPages: number;       // Tổng số trang
  totalElements: number;    // Tổng số items
  size: number;             // Số items/trang
  hasNext: boolean;         // Có trang tiếp theo?
  hasPrevious: boolean;     // Có trang trước?
  isFirst: boolean;         // Là trang đầu?
  isLast: boolean;          // Là trang cuối?
}
```

### 🔄 React Pagination Example

```javascript
import { useState, useEffect } from 'react';

function ListingsWithPagination() {
  const [listings, setListings] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchListings = async (page) => {
    setLoading(true);
    try {
      const response = await axios.get('/api/listings/ev-filter', {
        params: {
          vehicleType: 'SEDAN',
          page: page,
          size: 12
        },
        headers: { 'X-User-ID': userId }
      });
      
      setListings(response.data.data);
      setPagination(response.data.pagination);
      setCurrentPage(page);
    } catch (error) {
      console.error('Fetch error:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchListings(0);
  }, []);

  const handleNextPage = () => {
    if (pagination?.hasNext) {
      fetchListings(currentPage + 1);
    }
  };

  const handlePreviousPage = () => {
    if (pagination?.hasPrevious) {
      fetchListings(currentPage - 1);
    }
  };

  const handlePageClick = (pageNumber) => {
    fetchListings(pageNumber);
  };

  return (
    <div>
      {/* Listings Grid */}
      <div className="grid grid-cols-3 gap-4">
        {listings.map(listing => (
          <ListingCard key={listing.id} listing={listing} />
        ))}
      </div>

      {/* Pagination Controls */}
      {pagination && (
        <div className="pagination">
          <button 
            onClick={handlePreviousPage}
            disabled={!pagination.hasPrevious}
          >
            Previous
          </button>

          {/* Page Numbers */}
          {Array.from({ length: pagination.totalPages }, (_, i) => (
            <button
              key={i}
              onClick={() => handlePageClick(i)}
              className={i === currentPage ? 'active' : ''}
            >
              {i + 1}
            </button>
          ))}

          <button 
            onClick={handleNextPage}
            disabled={!pagination.hasNext}
          >
            Next
          </button>

          <span>
            Page {pagination.currentPage + 1} of {pagination.totalPages}
            ({pagination.totalElements} items)
          </span>
        </div>
      )}
    </div>
  );
}
```

---

## 6. React Examples

### 🔍 Complete Search & Filter Component

```javascript
import { useState, useEffect } from 'react';
import axios from 'axios';

function AdvancedSearchFilter() {
  const [filters, setFilters] = useState({
    keyword: '',
    category: 'EV', // 'EV' hoặc 'BATTERY'
    minPrice: '',
    maxPrice: '',
    vehicleType: '',
    year: '',
    page: 0,
    size: 12
  });

  const [listings, setListings] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch listings based on filters
  const fetchListings = async () => {
    setLoading(true);
    setError(null);

    try {
      let endpoint = '';
      let params = {
        page: filters.page,
        size: filters.size
      };

      // Xác định endpoint và params dựa vào filters
      if (filters.keyword) {
        // Nếu có keyword, dùng search
        endpoint = '/api/listings/search';
        params.keyword = filters.keyword;
      } else if (filters.category === 'EV') {
        // Nếu không có keyword, dùng filter theo category
        endpoint = '/api/listings/ev-filter';
        if (filters.minPrice) params.minPrice = parseFloat(filters.minPrice);
        if (filters.maxPrice) params.maxPrice = parseFloat(filters.maxPrice);
        if (filters.vehicleType) params.vehicleType = filters.vehicleType;
        if (filters.year) params.year = parseInt(filters.year);
      } else {
        // Battery filter
        endpoint = '/api/listings/battery-filter';
        if (filters.minPrice) params.minPrice = parseFloat(filters.minPrice);
        if (filters.maxPrice) params.maxPrice = parseFloat(filters.maxPrice);
        if (filters.year) params.year = parseInt(filters.year);
      }

      const response = await axios.get(endpoint, {
        params,
        headers: { 'X-User-ID': localStorage.getItem('userId') }
      });

      setListings(response.data.data);
      setPagination(response.data.pagination);
    } catch (err) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra');
      console.error('Fetch error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Fetch khi filters thay đổi
  useEffect(() => {
    fetchListings();
  }, [filters.page]); // Auto-fetch khi page thay đổi

  // Handle form submission
  const handleSearch = (e) => {
    e.preventDefault();
    setFilters(prev => ({ ...prev, page: 0 })); // Reset về page 0
    fetchListings();
  };

  // Handle filter change
  const handleFilterChange = (key, value) => {
    setFilters(prev => ({
      ...prev,
      [key]: value,
      page: 0 // Reset về page 0 khi filter thay đổi
    }));
  };

  // Handle page change
  const handlePageChange = (newPage) => {
    setFilters(prev => ({ ...prev, page: newPage }));
  };

  return (
    <div className="search-filter-container">
      {/* Search & Filter Form */}
      <form onSubmit={handleSearch} className="filter-form">
        {/* Keyword Search */}
        <input
          type="text"
          placeholder="Tìm kiếm..."
          value={filters.keyword}
          onChange={(e) => handleFilterChange('keyword', e.target.value)}
        />

        {/* Category */}
        <select 
          value={filters.category}
          onChange={(e) => handleFilterChange('category', e.target.value)}
        >
          <option value="EV">Xe Điện</option>
          <option value="BATTERY">Pin</option>
        </select>

        {/* Price Range */}
        <input
          type="number"
          placeholder="Giá từ"
          value={filters.minPrice}
          onChange={(e) => handleFilterChange('minPrice', e.target.value)}
        />
        <input
          type="number"
          placeholder="Giá đến"
          value={filters.maxPrice}
          onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
        />

        {/* Vehicle Type (chỉ hiện khi category = EV) */}
        {filters.category === 'EV' && (
          <select
            value={filters.vehicleType}
            onChange={(e) => handleFilterChange('vehicleType', e.target.value)}
          >
            <option value="">Tất cả loại xe</option>
            <option value="CAR">Xe hơi</option>
            <option value="SEDAN">Sedan</option>
            <option value="SUV">SUV</option>
            <option value="TRUCK">Xe tải</option>
            <option value="BUS">Xe buýt</option>
            <option value="MOTORBIKE">Xe máy</option>
          </select>
        )}

        {/* Year */}
        <input
          type="number"
          placeholder="Năm"
          value={filters.year}
          onChange={(e) => handleFilterChange('year', e.target.value)}
          min="2000"
          max={new Date().getFullYear()}
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Đang tìm...' : 'Tìm kiếm'}
        </button>
      </form>

      {/* Error Message */}
      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {/* Loading State */}
      {loading && <div className="loading">Đang tải...</div>}

      {/* Results */}
      {!loading && (
        <>
          {/* Listings Grid */}
          <div className="listings-grid">
            {listings.length > 0 ? (
              listings.map(listing => (
                <ListingCard key={listing.id} listing={listing} />
              ))
            ) : (
              <div className="no-results">Không tìm thấy kết quả</div>
            )}
          </div>

          {/* Pagination */}
          {pagination && pagination.totalPages > 1 && (
            <div className="pagination">
              <button
                onClick={() => handlePageChange(filters.page - 1)}
                disabled={!pagination.hasPrevious}
              >
                ← Trước
              </button>

              <span>
                Trang {pagination.currentPage + 1} / {pagination.totalPages}
                ({pagination.totalElements} kết quả)
              </span>

              <button
                onClick={() => handlePageChange(filters.page + 1)}
                disabled={!pagination.hasNext}
              >
                Sau →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default AdvancedSearchFilter;
```

### 🎯 Custom Hook cho Filter & Search

```javascript
// hooks/useListingFilter.js
import { useState, useCallback } from 'react';
import axios from 'axios';

export const useListingFilter = () => {
  const [listings, setListings] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchListings = useCallback(async (filters) => {
    setLoading(true);
    setError(null);

    try {
      let endpoint = '';
      let params = { page: filters.page || 0, size: filters.size || 12 };

      if (filters.keyword) {
        endpoint = '/api/listings/search';
        params.keyword = filters.keyword;
      } else if (filters.category === 'EV') {
        endpoint = '/api/listings/ev-filter';
        if (filters.minPrice) params.minPrice = filters.minPrice;
        if (filters.maxPrice) params.maxPrice = filters.maxPrice;
        if (filters.vehicleType) params.vehicleType = filters.vehicleType;
        if (filters.year) params.year = filters.year;
      } else if (filters.category === 'BATTERY') {
        endpoint = '/api/listings/battery-filter';
        if (filters.minPrice) params.minPrice = filters.minPrice;
        if (filters.maxPrice) params.maxPrice = filters.maxPrice;
        if (filters.year) params.year = filters.year;
      }

      const response = await axios.get(endpoint, {
        params,
        headers: { 'X-User-ID': localStorage.getItem('userId') }
      });

      setListings(response.data.data);
      setPagination(response.data.pagination);
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Có lỗi xảy ra';
      setError(errorMsg);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const reset = useCallback(() => {
    setListings([]);
    setPagination(null);
    setError(null);
  }, []);

  return {
    listings,
    pagination,
    loading,
    error,
    fetchListings,
    reset
  };
};

// Sử dụng:
// const { listings, pagination, loading, error, fetchListings } = useListingFilter();
// fetchListings({ category: 'EV', minPrice: 500000000, page: 0 });
```

---

## 7. Error Handling

### ❌ Common Errors

**1. Empty Keyword (400)**
```json
{
  "success": false,
  "message": "Từ khóa tìm kiếm không được để trống"
}
```

**2. Unauthorized (401)**
```json
{
  "success": false,
  "message": "Cần đăng nhập để xem danh sách"
}
```

**3. Server Error (500)**
```json
{
  "success": false,
  "message": "Lỗi server: ...",
  "error": "ExceptionType"
}
```

### 🛡️ Error Handling Best Practices

```javascript
const handleApiError = (error) => {
  if (!error.response) {
    // Network error
    return 'Lỗi kết nối. Vui lòng kiểm tra internet.';
  }

  const { status, data } = error.response;

  switch (status) {
    case 400:
      return data.message || 'Dữ liệu không hợp lệ';
    case 401:
      // Redirect to login
      window.location.href = '/login';
      return 'Vui lòng đăng nhập';
    case 404:
      return 'Không tìm thấy kết quả';
    case 500:
      return 'Lỗi server. Vui lòng thử lại sau.';
    default:
      return data.message || 'Có lỗi xảy ra';
  }
};

// Sử dụng
try {
  const data = await fetchListings(filters);
} catch (error) {
  const errorMessage = handleApiError(error);
  setError(errorMessage);
  console.error('API Error:', error);
}
```

---

## 📌 Best Practices

### ✅ DO's
1. **Luôn validate input** trước khi gửi request
2. **Debounce keyword search** để tránh spam API
3. **Cache results** khi có thể
4. **Show loading state** khi fetching
5. **Handle pagination** đúng cách (0-indexed)
6. **Reset page về 0** khi filter thay đổi
7. **Check hasNext/hasPrevious** trước khi navigate

### ❌ DON'Ts
1. **Không gửi request với keyword rỗng**
2. **Không hardcode user ID** - dùng authentication
3. **Không ignore error handling**
4. **Không fetch quá nhiều items** (size > 100)
5. **Không fetch lại khi đã có cache**

---

## 🔗 Related APIs

- [GET /api/listings/evCart](./API_GUIDE.md#get-evcart) - Lấy danh sách tất cả EV
- [GET /api/listings/batteryCart](./API_GUIDE.md#get-batterycart) - Lấy danh sách tất cả Battery
- [GET /api/listings/detail/{id}](./API_GUIDE.md#get-detail) - Chi tiết listing
- [GET /api/listings/my-listings](./API_GUIDE.md#get-my-listings) - Listing của tôi

---

## 📞 Support

Nếu gặp vấn đề, liên hệ:
- Backend Team
- Check logs tại `Backend-SWP391_2ndLand/logs`

**Last Updated:** October 5, 2025

