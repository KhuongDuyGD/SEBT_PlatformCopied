import React from 'react'
import { mockStats, mockTransactions, mockListings } from './data/mockData'
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, BarChart, Bar } from 'recharts'

const revenueTrend = [
  { month: 'T4', value: 210 },
  { month: 'T5', value: 245 },
  { month: 'T6', value: 310 },
  { month: 'T7', value: 340 },
  { month: 'T8', value: 390 },
  { month: 'T9', value: 420 },
  { month: 'T10', value: 458 },
]

const listingByType = [
  { type: 'Xe', count: 210 },
  { type: 'Pin', count: 102 },
  { type: 'Khác', count: 34 },
]

const formatCurrency = n =>
  n.toLocaleString('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 })

const StatCard = ({ icon, label, value, suffix, color = 'primary', sub }) => (
  <div className="col-12 col-sm-6 col-xl-3">
    <div className="stat-card">
      <div className={`icon-wrap bg-${color}-subtle text-${color}`}>
        <i className={icon}></i>
      </div>
      <div className="fw-semibold small text-secondary text-uppercase">{label}</div>
      <div className="fs-5 fw-bold">
        {value}{suffix && <small className="ms-1 fw-semibold text-secondary">{suffix}</small>}
      </div>
      {sub && <div className="small text-secondary">{sub}</div>}
    </div>
  </div>
)

const AdminDashboard = () => {
  return (
    <div className="dashboard">
      <h5 className="mb-3 fw-semibold">Tổng quan hệ thống</h5>
      <div className="row g-3 mb-4">
        <StatCard
          icon="bi bi-people"
          label="Người dùng"
          value={mockStats.totalUsers}
          color="primary"
          sub="+58 trong 7 ngày"
        />
        <StatCard
          icon="bi bi-card-checklist"
          label="Tin đang hoạt động"
          value={mockStats.activeListings}
          color="success"
          sub="Chờ duyệt: 14"
        />
        <StatCard
          icon="bi bi-currency-exchange"
          label="Doanh thu (tháng)"
          value={(mockStats.monthlyRevenue / 1_000_000).toFixed(1)}
          suffix="triệu ₫"
          color="warning"
          sub="Tỷ lệ chuyển đổi " />
        <StatCard
          icon="bi bi-exclamation-triangle"
          label="Khiếu nại mở"
          value={mockStats.pendingComplaints}
          color="danger"
          sub="Trung bình xử lý 1.8 ngày"
        />
      </div>

      <div className="row g-3">
        <div className="col-12 col-xl-8">
          <div className="card-flex mb-3">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="mb-0 fw-semibold">Xu hướng doanh thu (triệu ₫)</h6>
              <small className="text-secondary">7 tháng gần nhất</small>
            </div>
            <div style={{ width: '100%', height: 260 }}>
              <ResponsiveContainer>
                <LineChart data={revenueTrend}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="value" stroke="#0d6efd" strokeWidth={2} dot={{ r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="card-flex">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="mb-0 fw-semibold">Giao dịch gần đây</h6>
              <a href="/admin/transactions" className="small text-decoration-none">Xem tất cả →</a>
            </div>
            <div className="table-responsive">
              <table className="table table-sm align-middle">
                <thead>
                  <tr>
                    <th>Mã</th>
                    <th>Listing</th>
                    <th>Số tiền</th>
                    <th>Phí</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {mockTransactions.slice(0, 5).map(t => (
                    <tr key={t.id}>
                      <td className="fw-semibold">{t.id}</td>
                      <td>{t.listing}</td>
                      <td>{formatCurrency(t.amount)}</td>
                      <td><span className="text-secondary small">{formatCurrency(t.fee)}</span></td>
                      <td>
                        <span className={`badge rounded-pill text-bg-${t.status === 'success' ? 'success' : t.status === 'pending' ? 'warning' : 'secondary'
                          }`}>
                          {t.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                  {mockTransactions.length === 0 && (
                    <tr><td colSpan="5" className="text-center text-secondary small">Chưa có giao dịch</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
        {/* Right column */}
        <div className="col-12 col-xl-4 d-flex flex-column gap-3">
          <div className="card-flex">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="mb-0 fw-semibold">Phân loại tin đăng</h6>
            </div>
            <div style={{ width: '100%', height: 230 }}>
              <ResponsiveContainer>
                <BarChart data={listingByType}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
                  <XAxis dataKey="type" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="count" fill="#198754" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
          <div className="card-flex">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h6 className="mb-0 fw-semibold">Tin chờ duyệt</h6>
              <a href="/admin/pending-listings" className="small text-decoration-none">Tất cả →</a>
            </div>
            <ul className="list-group list-group-flush">
              {mockListings.filter(l => l.status === 'pending').slice(0, 4).map(l => (
                <li key={l.id} className="list-group-item px-0 d-flex flex-column">
                  <div className="d-flex justify-content-between">
                    <span className="fw-semibold small">{l.title}</span>
                    <span className="badge bg-warning-subtle text-warning border border-warning-subtle">Chờ</span>
                  </div>
                  <small className="text-secondary">
                    {l.id} • {formatCurrency(l.price)}
                  </small>
                </li>
              ))}
              {mockListings.filter(l => l.status === 'pending').length === 0 && (
                <li className="list-group-item px-0 small text-secondary">Không có tin nào.</li>
              )}
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AdminDashboard;