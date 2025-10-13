import { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Typography, Space, Card, Pagination, Select, Skeleton, Alert } from 'antd';
import { getWalletTransactions, getMyWalletBalance } from '../../api/wallet';

const purposeColors = {
  TOP_UP: 'green',
  LISTING_FEE: 'red',
  LISTING_FEE_REFUND: 'blue'
};

const statusColors = {
  PENDING: 'gold',
  COMPLETED: 'green',
  FAILED: 'red'
};

export default function WalletTransactions() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [purpose, setPurpose] = useState();
  const [balance, setBalance] = useState(null);
  const [error, setError] = useState(null);

  const load = useCallback(async (opts={}) => {
    setLoading(true); setError(null);
    try {
      const { items, page: p, size: s, totalPages: tp } = await getWalletTransactions({ page: opts.page ?? page, size: opts.size ?? size, purpose: opts.purpose ?? purpose });
      setItems(items);
      setPage(p);
      setSize(s);
      setTotalPages(tp ?? 0);
      if (balance == null) {
        try { const b = await getMyWalletBalance(); setBalance(b.balance); } catch {/* ignore */}
      }
    } catch (e) {
      setError(e.message || 'Không tải được giao dịch');
    } finally {
      setLoading(false);
    }
  }, [page, size, purpose, balance]);

  useEffect(() => { load({ page:0 }); }, [purpose, size]);
  useEffect(() => { load(); }, []); // initial

  function onPageChange(p) { load({ page: p-1 }); }

  const columns = [
    { title: 'Thời gian', dataIndex: 'createdAt', key: 'createdAt', render: v => v ? new Date(v).toLocaleString('vi-VN') : '-' },
    { title: 'Mục đích', dataIndex: 'purpose', key: 'purpose', render: p => <Tag color={purposeColors[p] || 'default'}>{p}</Tag> },
    { title: 'Số tiền', dataIndex: 'amount', key: 'amount', align: 'right', render: a => {
        if (a == null) return '-';
        const num = Number(a);
        const neg = num < 0;
        return <span style={{ color: neg? '#cf1322':'#237804', fontWeight: 600 }}>{neg? '-':''}{Math.abs(num).toLocaleString('vi-VN')} VND</span>;
      }
    },
    { title: 'Balance Sau', dataIndex: 'balanceAfter', key: 'balanceAfter', align: 'right', render: b => b!=null ? Number(b).toLocaleString('vi-VN'): '-' },
    { title: 'Trạng thái', dataIndex: 'status', key: 'status', render: s => <Tag color={statusColors[s] || 'default'}>{s}</Tag> },
    { title: 'Mô tả', dataIndex: 'description', key: 'description', width: 240, ellipsis: true }
  ];

  return (
    <div style={{ maxWidth: 1100, margin: '24px auto', padding: '0 16px' }}>
      <Typography.Title level={3}>Lịch sử giao dịch ví</Typography.Title>
      <Space style={{ marginBottom: 16 }} wrap>
        <Card size="small" style={{ minWidth: 220 }}>
          <Typography.Text>Số dư hiện tại:</Typography.Text><br />
          {balance == null ? <Skeleton.Input active size="small" style={{ width: 120 }} /> : (
            <Typography.Text strong style={{ fontSize: 18 }}>
              {Number(balance).toLocaleString('vi-VN')} VND
            </Typography.Text>
          )}
        </Card>
        <Select
          allowClear
          placeholder="Lọc mục đích"
          value={purpose}
            onChange={v => setPurpose(v)}
          style={{ width: 180 }}
          options={[
            { value: 'TOP_UP', label: 'Nạp tiền' },
            { value: 'LISTING_FEE', label: 'Phí đăng bài' },
            { value: 'LISTING_FEE_REFUND', label: 'Hoàn phí' }
          ]}
        />
        <Select
          value={size}
          onChange={v => { setSize(v); setPage(0); load({ size: v, page:0 }); }}
          style={{ width: 120 }}
          options={[10,20,30,50].map(v => ({ value: v, label: v + '/trang' }))}
        />
      </Space>
      {error && <Alert type="error" showIcon style={{ marginBottom: 16 }} message={error} />}
      <Table
        size="middle"
        rowKey={r => r.id || r.orderId || r.createdAt + Math.random()}
        columns={columns}
        dataSource={items}
        loading={loading}
        pagination={false}
        bordered
      />
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
        <Pagination
          current={page + 1}
          pageSize={size}
          onChange={onPageChange}
          total={totalPages ? totalPages * size : (page+1)*size + (items.length===size? size:0)}
          showSizeChanger={false}
        />
      </div>
    </div>
  );
}
