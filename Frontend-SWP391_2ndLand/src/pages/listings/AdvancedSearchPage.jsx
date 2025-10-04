import React, { useEffect, useState, useCallback } from 'react';
import listingsApi from '../../api/listings';
import { useSearchParams, Link } from 'react-router-dom';
import './Listings.css';

// Utility: build query params object -> string
const sanitizeParams = (params) => {
  const cleaned = {};
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '' && !(typeof v === 'number' && isNaN(v))) {
      cleaned[k] = v;
    }
  });
  return cleaned;
};

const initialFilters = {
  title: '',
  brand: '',
  vehicleType: '',
  year: '',
  minPrice: '',
  maxPrice: ''
};

export default function AdvancedSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState(initialFilters);
  const [page, setPage] = useState(Number(searchParams.get('page')) || 0);
  const [size] = useState(12);
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [applied, setApplied] = useState({});

  // Populate from URL on mount
  useEffect(() => {
    const f = { ...initialFilters };
    Object.keys(initialFilters).forEach(key => {
      const val = searchParams.get(key);
      if (val) f[key] = val;
    });
    setFilters(f);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchResults = useCallback(async (paramsObj) => {
    try {
      setLoading(true);
      setError(null);
      const res = await listingsApi.advancedSearchListings({ ...paramsObj, page, size });
      // Backend expected shape: { success, data, pagination }
      if (res?.success) {
        setData(res.data || []);
        setPagination(res.pagination || null);
      } else {
        setData([]);
        setPagination(null);
      }
    } catch (e) {
      console.error('Advanced search error', e);
      setError('Không thể tìm kiếm lúc này.');
      setData([]);
      setPagination(null);
    } finally {
      setLoading(false);
    }
  }, [page, size]);

  // Handle form input change
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const validatePriceRange = () => {
    if (filters.minPrice && filters.maxPrice) {
      const min = Number(filters.minPrice);
      const max = Number(filters.maxPrice);
      if (!isNaN(min) && !isNaN(max) && min > max) return false;
    }
    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validatePriceRange()) {
      setError('Khoảng giá không hợp lệ (Giá từ > Giá đến).');
      return;
    }
    const cleaned = sanitizeParams(filters);
    setApplied(cleaned);
    setPage(0);
    setSearchParams({ ...cleaned, page: '0' });
    fetchResults(cleaned);
  };

  // Refetch when page changes (and applied filters exist)
  useEffect(() => {
    if (Object.keys(applied).length > 0) {
      fetchResults(applied);
      setSearchParams({ ...applied, page: String(page) });
    }
  }, [page]); // eslint-disable-line react-hooks/exhaustive-deps

  const resetFilters = () => {
    setFilters(initialFilters);
    setApplied({});
    setPage(0);
    setData([]);
    setPagination(null);
    setSearchParams({});
  };

  const nextPage = () => {
    if (pagination?.hasNext) setPage(p => p + 1);
  };
  const prevPage = () => {
    if (pagination?.hasPrevious) setPage(p => Math.max(0, p - 1));
  };

  const mapCard = (item) => {
    // Flexible mapping (similar to CarListings)
    return {
      id: item.id,
      title: item.title || item.listingTitle,
      price: item.price,
      image: item.mainImageUrl || item.mainImage || 'https://via.placeholder.com/400',
      year: item.year || item.ev?.year,
      brand: item.brand || item.ev?.brand,
      mileage: item.mileage || item.ev?.mileage,
      listingType: item.listingType,
      status: item.status || item.listingStatus,
      category: item.category || item.type || (item.ev ? 'VEHICLE' : (item.battery ? 'BATTERY' : ''))
    };
  };

  const cards = data.map(mapCard);

  return (
    <div className="container mx-auto px-4 py-8 advanced-search-page">
      <div className="flex flex-col md:flex-row gap-8">
        {/* Filter Panel */}
        <div className="w-full md:w-72">
          <form onSubmit={handleSubmit} className="p-4 rounded border bg-white space-y-4 shadow-sm">
            <h2 className="text-lg font-semibold">Tìm kiếm nâng cao</h2>
            <div className="space-y-3">
              <div>
                <label className="form-label block text-sm font-medium mb-1">Tiêu đề</label>
                <input name="title" value={filters.title} onChange={handleChange} className="form-input w-full" placeholder="VD: Vinfast..." />
              </div>
              <div>
                <label className="form-label block text-sm font-medium mb-1">Hãng</label>
                <input name="brand" value={filters.brand} onChange={handleChange} className="form-input w-full" placeholder="Vinfast" />
              </div>
              <div>
                <label className="form-label block text-sm font-medium mb-1">Loại xe</label>
                <select name="vehicleType" value={filters.vehicleType} onChange={handleChange} className="form-select w-full">
                  <option value="">-- Tất cả --</option>
                  <option value="CAR">Ô tô</option>
                  <option value="BIKE">Xe đạp điện</option>
                  <option value="MOTORBIKE">Xe máy điện</option>
                </select>
              </div>
              <div>
                <label className="form-label block text-sm font-medium mb-1">Năm</label>
                <input type="number" name="year" value={filters.year} onChange={handleChange} className="form-input w-full" placeholder="2023" />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="form-label block text-xs font-medium mb-1">Giá từ</label>
                  <input type="number" name="minPrice" value={filters.minPrice} onChange={handleChange} className="form-input w-full" />
                </div>
                <div>
                  <label className="form-label block text-xs font-medium mb-1">Giá đến</label>
                  <input type="number" name="maxPrice" value={filters.maxPrice} onChange={handleChange} className="form-input w-full" />
                </div>
              </div>
            </div>
            <div className="flex gap-2 pt-2">
              <button type="submit" className="px-4 py-2 rounded bg-blue-600 text-white text-sm disabled:opacity-60" disabled={loading}>Tìm kiếm</button>
              <button type="button" onClick={resetFilters} className="px-3 py-2 rounded border text-sm">Reset</button>
            </div>
            {!validatePriceRange() && (
              <p className="text-xs text-red-600">Khoảng giá không hợp lệ.</p>
            )}
          </form>

          {/* Applied filters badges */}
          {Object.keys(applied).length > 0 && (
            <div className="mt-4 space-y-2">
              <h4 className="text-sm font-semibold">Đang áp dụng:</h4>
              <div className="flex flex-wrap gap-2">
                {Object.entries(applied).map(([k,v]) => k !== 'page' && (
                  <span key={k} className="px-2 py-1 bg-gray-200 rounded text-xs">{k}: {v}</span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Results */}
        <div className="flex-1 min-h-[400px]">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-xl font-bold">Kết quả</h1>
            <Link to="/post-listing" className="text-sm text-blue-600 underline">+ Đăng mới</Link>
          </div>

            {error && (
              <div className="p-3 bg-red-100 text-red-700 rounded mb-4 text-sm">{error}</div>
            )}

            {loading && (
              <div className="py-16 text-center">
                <div className="animate-spin h-10 w-10 border-2 border-blue-500 border-t-transparent rounded-full mx-auto" />
                <p className="mt-4 text-gray-600 text-sm">Đang tìm kiếm...</p>
              </div>
            )}

            {!loading && !error && data.length === 0 && Object.keys(applied).length > 0 && (
              <div className="py-16 text-center border rounded bg-white">
                <p className="text-gray-600 mb-2 text-sm">Không có kết quả phù hợp.</p>
                <button onClick={resetFilters} className="text-blue-600 underline text-sm">Xóa bộ lọc</button>
              </div>
            )}

            {!loading && !error && data.length > 0 && (
              <>
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                  {cards.map(card => (
                    <div key={card.id} className="rounded shadow bg-white overflow-hidden group">
                      <div className="aspect-video bg-gray-100 overflow-hidden">
                        <img src={card.image} alt={card.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                      </div>
                      <div className="p-4 space-y-1">
                        <h3 className="font-semibold text-sm line-clamp-2">{card.title}</h3>
                        <div className="text-xs text-gray-500 flex gap-2 flex-wrap">
                          {card.brand && <span>{card.brand}</span>}
                          {card.year && <span>• {card.year}</span>}
                          {card.category && <span>• {card.category}</span>}
                        </div>
                        <div className="text-blue-600 font-bold text-sm">{card.price?.toLocaleString()} VND</div>
                        <div className="flex justify-between items-center mt-1">
                          {card.listingType === 'PREMIUM' && <span className="text-[10px] px-2 py-0.5 bg-yellow-200 text-yellow-800 rounded">PREMIUM</span>}
                          <Link to={`/listings/${card.id}`} className="text-xs text-blue-500 hover:underline">Chi tiết</Link>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                {pagination && (
                  <div className="flex items-center justify-center gap-4 mt-8">
                    <button disabled={!pagination.hasPrevious} onClick={prevPage} className="px-3 py-1 border rounded disabled:opacity-40 text-sm">Trước</button>
                    <span className="text-sm">Trang {pagination.currentPage + 1} / {pagination.totalPages || 1}</span>
                    <button disabled={!pagination.hasNext} onClick={nextPage} className="px-3 py-1 border rounded disabled:opacity-40 text-sm">Tiếp</button>
                  </div>
                )}
              </>
            )}
        </div>
      </div>
    </div>
  );
}
