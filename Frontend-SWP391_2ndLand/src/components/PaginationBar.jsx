// src/components/PaginationBar.jsx
// Reusable pagination bar component
import React from 'react';
import { Button } from 'react-bootstrap';

export default function PaginationBar({ pagination, loading, onPrev, onNext, className='' }) {
  if (!pagination) return null;
  return (
    <div className={`d-flex align-items-center justify-content-center gap-3 ${className}`.trim()}>
      <Button size="sm" variant="outline-secondary" disabled={!pagination.hasPrevious || loading} onClick={onPrev}>
        Trước
      </Button>
      <span className="small">
        Trang {pagination.currentPage + 1} / {pagination.totalPages || 1}
      </span>
      <Button size="sm" variant="outline-secondary" disabled={!pagination.hasNext || loading} onClick={onNext}>
        Tiếp
      </Button>
    </div>
  );
}
