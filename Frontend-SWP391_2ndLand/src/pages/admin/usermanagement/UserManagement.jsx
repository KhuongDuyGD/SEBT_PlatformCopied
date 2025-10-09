import React, { useEffect, useState } from 'react'
import { Button, Form, InputGroup, Table, Pagination, Spinner, Badge } from 'react-bootstrap'
import { getUsers, blockUser, unblockUser } from '../../../api/users'

const UserRow = ({ user, onToggleBlock }) => {
    const isBlocked = user.status === 'blocked' || user.status === 'BLOCKED'
    return (
        <tr>
            <td>{user.id}</td>
            <td>
                <div className="fw-semibold">{user.name || user.username || '—'}</div>
                <div className="small text-muted">{user.email}</div>
            </td>
            <td>{user.phone || '—'}</td>
            <td>{user.role || (user.isAdmin ? 'admin' : 'member')}</td>
            <td>
                {isBlocked ? <Badge bg="danger">Blocked</Badge> : <Badge bg="success">Active</Badge>}
            </td>
            <td>{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '—'}</td>
            <td>
                <Button size="sm" variant={isBlocked ? 'outline-success' : 'outline-danger'} onClick={() => onToggleBlock(user)}>
                    {isBlocked ? 'Unblock' : 'Block'}
                </Button>
            </td>
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

    const load = async (p = page, query = q) => {
        setLoading(true)
        try {
            const res = await getUsers({ page: p, size, q: query })
            setUsers(res.content || [])
            setTotal(res.totalElements ?? 0)
            setPage(res.number ?? p)
        } catch (err) {
            console.error('Load users failed', err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load(0, '') }, [])

    const handleSearch = (e) => {
        e.preventDefault()
        load(0, q)
    }

    const handleToggleBlock = async (user) => {
        const isBlocked = user.status === 'blocked' || user.status === 'BLOCKED'
        try {
            if (isBlocked) {
                await unblockUser(user.id)
                user.status = 'active'
            } else {
                await blockUser(user.id)
                user.status = 'blocked'
            }
            // update local state
            setUsers(prev => prev.map(u => (u.id === user.id ? { ...u, status: user.status } : u)))
        } catch (err) {
            console.error('Toggle block failed', err)
        }
    }

    const totalPages = Math.max(1, Math.ceil(total / size))

    return (
        <div className="container py-4">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h3 className="mb-0">Quản lý người dùng</h3>
                <Form className="d-flex" onSubmit={handleSearch}>
                    <InputGroup>
                        <Form.Control placeholder="Tìm theo tên hoặc email" value={q} onChange={e => setQ(e.target.value)} />
                        <Button type="submit" variant="primary">Tìm</Button>
                    </InputGroup>
                </Form>
            </div>

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
                                    <th style={{ width: 120 }}>ID</th>
                                    <th>Tên / Email</th>
                                    <th>Điện thoại</th>
                                    <th>Vai trò</th>
                                    <th>Trạng thái</th>
                                    <th>Ngày tạo</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.length === 0 ? (
                                    <tr><td colSpan={7} className="text-center py-4 text-muted">Không có người dùng</td></tr>
                                ) : users.map(u => (
                                    <UserRow key={u.id} user={u} onToggleBlock={handleToggleBlock} />
                                ))}
                            </tbody>
                        </Table>
                    )}
                </div>

                <div className="card-footer d-flex justify-content-between align-items-center">
                    <div className="small text-muted">Tổng: {total}</div>
                    <Pagination className="mb-0">
                        <Pagination.First onClick={() => load(0)} disabled={page === 0} />
                        <Pagination.Prev onClick={() => load(Math.max(0, page - 1))} disabled={page === 0} />
                        <Pagination.Item active>{page + 1}</Pagination.Item>
                        <Pagination.Next onClick={() => load(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1} />
                        <Pagination.Last onClick={() => load(totalPages - 1)} disabled={page >= totalPages - 1} />
                    </Pagination>
                </div>
            </div>
        </div>
    )
}

export default UserManagement
