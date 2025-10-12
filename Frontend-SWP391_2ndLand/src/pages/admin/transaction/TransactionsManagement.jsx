import React, { useState } from 'react'
import { mockTransactions } from '../../../data/mockData'

const TransactionsManagement = () => {
  const [status, setStatus] = useState('all')
  const filtered = mockTransactions.filter(t => status === 'all' || t.status === status)

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5 className="mb-0 fw-semibold">Giao dịch</h5>
        <div className="d-flex gap-2">
          <select
            className="form-select form-select-sm"
            style={{ width: 180 }}
            value={status}
            onChange={e => setStatus(e.target.value)}
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="success">Success</option>
            <option value="pending">Pending</option>
            <option value="failed">Failed</option>
          </select>
          <button className="btn btn-sm btn-outline-secondary">
            <i className="bi bi-download me-1"></i> Xuất CSV
          </button>
        </div>
      </div>
      <div className="card-flex">
        <div className="table-responsive">
          <table className="table table-sm align-middle">
            <thead>
              <tr>
                <th>Mã GD</th>
                <th>Listing</th>
                <th>Buyer</th>
                <th>Seller</th>
                <th>Số tiền</th>
                <th>Phí</th>
                <th>Thời gian</th>
                <th>Trạng thái</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(t => (
                <tr key={t.id}>
                  <td className="fw-semibold">{t.id}</td>
                  <td>{t.listing}</td>
                  <td>{t.buyer}</td>
                  <td>{t.seller}</td>
                  <td>{t.amount.toLocaleString('vi-VN')}</td>
                  <td className="small text-secondary">{t.fee.toLocaleString('vi-VN')}</td>
                  <td className="small">{t.createdAt}</td>
                  <td>
                    <span className={`badge rounded-pill text-bg-${
                      t.status === 'success' ? 'success'
                        : t.status === 'pending' ? 'warning' : 'danger'
                    }`}>{t.status}</span>
                  </td>
                  <td className="text-end">
                    <div className="btn-group btn-group-sm">
                      <button className="btn btn-light">
                        <i className="bi bi-eye"></i>
                      </button>
                      <button className="btn btn-light">
                        <i className="bi bi-receipt"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {filtered.length === 0 && (
                <tr><td colSpan="9" className="text-center small text-secondary">Không có dữ liệu.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

export default TransactionsManagement