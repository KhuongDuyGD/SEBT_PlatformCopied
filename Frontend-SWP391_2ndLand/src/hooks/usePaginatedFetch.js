// src/hooks/usePaginatedFetch.js
// Generic hook cho việc fetch có phân trang + abort + cache nhẹ theo key
import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * @param {function({page,size,signal}): Promise<{data:any[], pagination?:object}>} fetcher
 * @param {object} deps object các tham số phụ thuộc (sẽ ảnh hưởng cache key)
 * @param {number} initialPage
 * @param {number} initialSize
 */
export function usePaginatedFetch(fetcher, deps = {}, initialPage = 0, initialSize = 12) {
  const [page, setPage] = useState(initialPage);
  const [size, setSize] = useState(initialSize);
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const cacheRef = useRef(new Map()); // key -> { data, pagination, ts }
  const abortRef = useRef();

  const buildKey = () => JSON.stringify({ ...deps, page, size });

  const load = useCallback(async () => {
    const key = buildKey();
    if (cacheRef.current.has(key)) {
      const cached = cacheRef.current.get(key);
      setData(cached.data);
      setPagination(cached.pagination);
      if (import.meta.env.DEV) console.debug('[usePaginatedFetch] cache hit', key);
      return;
    }
    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    setLoading(true); setError(null);
    try {
      const res = await fetcher({ page, size, signal: controller.signal });
      const arr = Array.isArray(res.data) ? res.data : [];
      setData(arr);
      setPagination(res.pagination || null);
      cacheRef.current.set(key, { data: arr, pagination: res.pagination || null, ts: Date.now() });
    } catch (e) {
      if (e.name === 'AbortError') {
        if (import.meta.env.DEV) console.debug('[usePaginatedFetch] aborted request');
      } else {
        console.error('[usePaginatedFetch] error', e);
        setError('Không thể tải dữ liệu.');
        setData([]); setPagination(null);
      }
    } finally {
      setLoading(false);
    }
  }, [page, size, ...Object.values(deps)]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => { load(); }, [load]);

  const nextPage = () => { if (pagination?.hasNext) setPage(p => p + 1); };
  const prevPage = () => { if (pagination?.hasPrevious) setPage(p => Math.max(0, p - 1)); };
  const changeSize = (s) => { setSize(s); setPage(0); };
  const refresh = () => { // clear cache key and reload
    const key = buildKey();
    cacheRef.current.delete(key);
    load();
  };

  return { data, pagination, loading, error, page, size, setPage, setSize: changeSize, nextPage, prevPage, refresh };
}

export default usePaginatedFetch;
