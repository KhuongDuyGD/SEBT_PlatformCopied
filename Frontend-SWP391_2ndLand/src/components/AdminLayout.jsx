import React, { useState, useEffect } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import '../css/AdminLayout.css'

const menu = [
  { group: 'MAIN', items: [{ to: '/admin', label: 'Dashboard', icon: 'bi-speedometer2', end: true }] },
  {
    group: 'QUẢN LÝ', items: [
      { to: '/admin/pending-listings', label: 'Xét duyệt tin', icon: 'bi-clock-history' },
      { to: '/admin/listings', label: 'Tin đăng', icon: 'bi-card-list' },
      { to: '/admin/transactions', label: 'Giao dịch', icon: 'bi-arrow-left-right' },
      { to: '/admin/users', label: 'Người dùng', icon: 'bi-people' },
      { to: '/admin/complaints', label: 'Khiếu nại', icon: 'bi-exclamation-triangle' },
      { to: '/admin/fees', label: 'Phí & Hoa hồng', icon: 'bi-percent' },
      { to: '/admin/reports', label: 'Báo cáo', icon: 'bi-graph-up' },
    ]
  },
  {
    group: 'HỆ THỐNG', items: [
      { to: '/admin/settings', label: 'Cài đặt', icon: 'bi-gear' },
      { to: '/admin/audit', label: 'Audit Log', icon: 'bi-shield-lock' },
    ]
  }
]

const SIDEBAR_WIDTH = 250
const SIDEBAR_COLLAPSED = 76
const LS_KEY = 'admin_sidebar_collapsed'

const AdminLayout = () => {
  const [collapsed, setCollapsed] = useState(false)

  useEffect(() => {
    const saved = localStorage.getItem(LS_KEY)
    if (saved === '1') setCollapsed(true)
  }, [])

  useEffect(() => {
    localStorage.setItem(LS_KEY, collapsed ? '1' : '0')
  }, [collapsed])

  return (
    <div className="admin-shell">
      <aside className={`admin-side ${collapsed ? 'collapsed' : ''}`}>
        <div className="side-header">
          <div className="brand">
            <i className="bi bi-lightning-charge-fill text-warning fs-5 me-2"></i>
            {!collapsed && <span className="fw-semibold">EV Admin</span>}
          </div>
          <button
            className="btn btn-sm btn-outline-light toggle-btn"
            onClick={() => setCollapsed(c => !c)}
            title={collapsed ? 'Mở rộng' : 'Thu gọn'}
          >
            <i className={`bi ${collapsed ? 'bi-chevron-double-right' : 'bi-chevron-double-left'}`}></i>
          </button>
        </div>

        <div className="side-scroll">
          {menu.map(section => (
            <div key={section.group} className="menu-section">
              {!collapsed && (
                <div className="section-title">{section.group}</div>
              )}
              <ul className="list-unstyled m-0 nav-list">
                {section.items.map(item => (
                  <li key={item.to}>
                    <NavLink
                      to={item.to}
                      end={item.end}
                      className={({ isActive }) =>
                        'nav-entry' + (isActive ? ' active' : '')
                      }
                      data-label={item.label}
                    >
                      <i className={`bi ${item.icon} entry-icon`}></i>
                      <span className="entry-text">{item.label}</span>
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="side-footer">
          {!collapsed && <div className="small text-secondary">v1.0.0</div>}
        </div>
      </aside>

      <div
        className="admin-content"
        style={{
          marginLeft: collapsed ? SIDEBAR_COLLAPSED : SIDEBAR_WIDTH,
          transition: 'margin-left .22s cubic-bezier(.4,0,.2,1)'
        }}
      >
        <div className="content-inner">
          <Outlet />
        </div>
      </div>
    </div>
  )
}

export default AdminLayout