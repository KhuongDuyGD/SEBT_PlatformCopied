import React, { useEffect, useState } from 'react'
import { Form, InputGroup, Table, Pagination, Spinner, Badge, Alert } from 'react-bootstrap'
import { getUsers } from '../../../api/users'

const UserRow = ({ user }) => {
    return (
        <tr>
            <td>{user.id}</td>
            <td>
                <div className="fw-semibold">{user.name || user.username || '—'}</div>
                <div className="small text-muted">{user.email}</div>
            </td>
            <td>{user.phoneNumber || user.phone || '—'}</td>
            <td>
                <Badge bg={user.role === 'ADMIN' ? 'danger' : 'secondary'}>
                    {user.role || 'MEMBER'}
                </Badge>
            </td>
            <td>
                <Badge bg={user.status === 'ACTIVE' ? 'success' : 'secondary'}>
                    {user.status || 'ACTIVE'}
                </Badge>
            </td>
            <td>{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '—'}</td>
        </tr>
    )
}

const UserManagement = () => {
    const [q, setQ] = useState('')
    const [page, setPage] = useState(0)
    const [size] = useState(10)
    const [loading, setLoading] = useState(false)
    const [users, setUsers] = useState([])
    const [total, setTotal] = useState(0)
    const [error, setError] = useState(null)

    const load = async (p = page, query = q) => {
        setLoading(true)
        setError(null)
        try {
            const res = await getUsers({ page: p, size, q: query })
            setUsers(res.content || [])
            setTotal(res.totalElements ?? 0)
            setPage(res.number ?? p)
        } catch (err) {
            console.error('Load users failed', err)
            setError(err.message || 'Không thể tải danh sách người dùng')
            setUsers([])
            setTotal(0)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load(0, '') }, [])

    const handleSearch = (e) => {
        e.preventDefault()
        load(0, q)
    }

    const totalPages = Math.max(1, Math.ceil(total / size))

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h3 className="mb-0">Quản lý người dùng</h3>
                <Form className="d-flex" onSubmit={handleSearch}>
                    <InputGroup>
                        <Form.Control
                            placeholder="Tìm theo tên hoặc email"
                            value={q}
                            onChange={e => setQ(e.target.value)}
                            disabled={loading}
                        />
                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            Tìm
                        </button>
                    </InputGroup>
                </Form>
            </div>

            {error && (
                <Alert variant="warning" dismissible onClose={() => setError(null)}>
                    <Alert.Heading>Không thể tải dữ liệu</Alert.Heading>
                    <p className="mb-0">{error}</p>
                </Alert>
            )}

            <div className="card shadow-sm">
                <div className="card-body p-0">
                    {loading ? (
                        <div className="d-flex justify-content-center py-5">
                            <Spinner animation="border" />
                        </div>
                    ) : (
                        <Table hover responsive className="mb-0">
                            <thead className="table-light">
                                <tr>
                                    <th style={{ width: 80 }}>ID</th>
                                    <th>Tên / Email</th>
                                    <th>Điện thoại</th>
                                    <th style={{ width: 120 }}>Vai trò</th>
                                    <th style={{ width: 120 }}>Trạng thái</th>
                                    <th style={{ width: 140 }}>Ngày tạo</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.length === 0 ? (
                                    <tr>
                                        <td colSpan={6} className="text-center py-4 text-muted">
                                            {error ? 'Backend API chưa sẵn sàng' : 'Không có người dùng'}
                                        </td>
                                    </tr>
                                ) : users.map(u => (
                                    <UserRow key={u.id} user={u} />
                                ))}
                            </tbody>
                        </Table>
                    )}
                </div>

                {!error && users.length > 0 && (
                    <div className="card-footer d-flex justify-content-between align-items-center">
                        <div className="small text-muted">Tổng: {total} người dùng</div>
                        <Pagination className="mb-0">
                            <Pagination.First onClick={() => load(0)} disabled={page === 0 || loading} />
                            <Pagination.Prev onClick={() => load(Math.max(0, page - 1))} disabled={page === 0 || loading} />
                            <Pagination.Item active>{page + 1} / {totalPages}</Pagination.Item>
                            <Pagination.Next onClick={() => load(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1 || loading} />
                            <Pagination.Last onClick={() => load(totalPages - 1)} disabled={page >= totalPages - 1 || loading} />
                        </Pagination>
                    </div>
                )}
            </div>
        </div>
    )
}

export default UserManagement
