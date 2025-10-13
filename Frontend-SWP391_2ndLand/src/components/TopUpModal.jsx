import { useState, useEffect, useRef } from 'react';
import { Modal, Form, InputNumber, Button, Space, Alert, Typography, Tag } from 'antd';
import { createTopUpIntent } from '../api/wallet';
import api from '../api/axios';

/**
 * TopUpModal - UX nạp tiền VNPay với polling trạng thái giao dịch
 * Props:
 *  - open: boolean
 *  - onClose: () => void
 *  - onCompleted: (tx) => void (khi status COMPLETED)
 *  - refreshBalance: () => void (gọi để cập nhật số dư bên ngoài)
 */
export default function TopUpModal({ open, onClose, onCompleted, refreshBalance }) {
  const [amount, setAmount] = useState();
  const [loading, setLoading] = useState(false);
  const [intent, setIntent] = useState(null); // { orderId, paymentUrl, amount }
  const [status, setStatus] = useState('IDLE'); // IDLE | WAITING | COMPLETED | FAILED
  const [error, setError] = useState(null);
  const [polling, setPolling] = useState(false);
  const [attempts, setAttempts] = useState(0);
  const pollTimerRef = useRef(null);
  const lastVisibilityCheckRef = useRef(0);

  useEffect(() => {
    if (!open) {
      // reset state when closing
      clearPoll();
      setAmount(undefined);
      setLoading(false);
      setIntent(null);
      setStatus('IDLE');
      setError(null);
      setPolling(false);
      setAttempts(0);
    }
  }, [open]);

  // Auto status check & balance refresh when user returns to tab
  useEffect(() => {
    function handleVisibility() {
      if (document.visibilityState === 'visible') {
        // Debounce to avoid spamming when multiple visibility events fire
        const now = Date.now();
        if (now - lastVisibilityCheckRef.current < 1500) return;
        lastVisibilityCheckRef.current = now;
        // Only act if we are waiting for payment
        if (status === 'WAITING' && intent?.orderId) {
          // Perform a single immediate check (faster feedback than waiting for next interval)
            pollOnce(intent.orderId);
        } else if (status === 'COMPLETED') {
          // Ensure latest balance if user navigated away right after success
          refreshBalance?.();
        }
      }
    }
    if (open) {
      document.addEventListener('visibilitychange', handleVisibility);
    }
    return () => document.removeEventListener('visibilitychange', handleVisibility);
  }, [open, status, intent, refreshBalance]);

  function clearPoll() {
    if (pollTimerRef.current) {
      clearInterval(pollTimerRef.current);
      pollTimerRef.current = null;
    }
  }

  async function startTopUp() {
    if (!amount || amount <= 0) {
      setError('Vui lòng nhập số tiền hợp lệ');
      return;
    }
    setLoading(true); setError(null);
    try {
      const data = await createTopUpIntent(amount);
      setIntent(data);
      setStatus('WAITING');
      // Mở VNPay tab
      window.open(data.paymentUrl, '_blank');
      // Bắt đầu polling
      beginPolling(data.orderId);
    } catch (e) {
      console.error('createTopUpIntent failed', e);
      setError('Tạo yêu cầu nạp thất bại, thử lại sau');
    } finally {
      setLoading(false);
    }
  }

  async function pollOnce(orderId) {
    try {
      const { data } = await api.get(`/wallet/topups/${orderId}`);
      if (data && data.status === 'COMPLETED') {
        clearPoll();
        setStatus('COMPLETED');
        setPolling(false);
        refreshBalance?.();
        onCompleted?.(data);
      } else if (data && data.status === 'FAILED') {
        clearPoll();
        setStatus('FAILED');
        setPolling(false);
        setError('Thanh toán thất bại hoặc bị hủy');
      }
    } catch (err) {
      // tạm thời bỏ qua lỗi network lẻ
    } finally {
      setAttempts(prev => prev + 1);
    }
  }

  function beginPolling(orderId) {
    setPolling(true);
    setAttempts(0);
    clearPoll();
    pollTimerRef.current = setInterval(() => {
      setAttempts(a => {
        if (a >= 36) { // ~3 phút (36 * 5s)
          clearPoll();
          setPolling(false);
          return a;
        }
        return a;
      });
      pollOnce(orderId);
    }, 5000);
  }

  function manualCheck() {
    if (intent) pollOnce(intent.orderId);
  }

  function reopenVnpay() {
    if (intent?.paymentUrl) {
      window.open(intent.paymentUrl, '_blank');
    }
  }

  function closeHandler() {
    // Cho phép đóng ngay cả khi đang WAITING; người dùng vẫn có thể quay lại nạp tiếp
    clearPoll();
    onClose?.();
  }

  const disabledWhileWaiting = status === 'WAITING' && !['COMPLETED','FAILED'].includes(status);
  const canFinish = status === 'COMPLETED';

  return (
    <Modal
      open={open}
      onCancel={closeHandler}
      footer={null}
      title="Nạp tiền vào ví"
      destroyOnClose
      centered
    >
      {status === 'IDLE' && (
        <Form layout="vertical" onFinish={startTopUp}>
          <Form.Item label="Số tiền (VND)" required>
            <InputNumber
              value={amount}
              onChange={setAmount}
              min={10000}
              step={50000}
              style={{ width: '100%' }}
              formatter={v => (v ? Number(v).toLocaleString('vi-VN') : '')}
              parser={v => v.replace(/\D/g,'')}
              disabled={loading}
            />
          </Form.Item>
          <Space wrap style={{ marginBottom: 12 }}>
            {[100000,200000,500000,1000000].map(v => (
              <Button key={v} size="small" type={amount===v? 'primary':'default'} onClick={() => setAmount(v)}>{v.toLocaleString('vi-VN')}</Button>
            ))}
          </Space>
          {error && <Alert type="error" showIcon style={{ marginBottom: 12 }} message={error} />}
          <Button type="primary" htmlType="submit" block loading={loading}>Tạo yêu cầu nạp</Button>
        </Form>
      )}

      {status !== 'IDLE' && (
        <Space direction="vertical" style={{ width: '100%' }} size={16}>
          <div>
            <Typography.Text>Số tiền: <strong>{amount?.toLocaleString('vi-VN')} VND</strong></Typography.Text><br />
            {intent && <Typography.Text>Mã giao dịch: <Tag color="blue">{intent.orderId}</Tag></Typography.Text>}
          </div>
          {status === 'WAITING' && (
            <Alert
              type="info"
              message="Đang chờ bạn hoàn tất thanh toán VNPay"
              description="Tab VNPay đã được mở. Sau khi thanh toán thành công quay lại đây, hệ thống sẽ tự kiểm tra. Bạn cũng có thể nhấn 'Kiểm tra trạng thái'."
              showIcon
            />
          )}
          {status === 'COMPLETED' && (
            <Alert type="success" message="Nạp tiền thành công" showIcon />
          )}
          {status === 'FAILED' && (
            <Alert type="error" message={error || 'Giao dịch thất bại'} showIcon />
          )}
          {error && status === 'WAITING' && <Alert type="warning" message={error} />}
          <Space wrap>
            <Button onClick={reopenVnpay} disabled={!intent}>Mở lại VNPay</Button>
            <Button onClick={manualCheck} loading={polling || status==='COMPLETED'} disabled={!intent || status==='COMPLETED'}>
              {status==='COMPLETED' ? 'Đã hoàn tất' : 'Kiểm tra trạng thái'}
            </Button>
            <Button onClick={closeHandler} type={canFinish ? 'primary':'default'}>
              {canFinish ? 'Đóng' : 'Hủy'}
            </Button>
          </Space>
        </Space>
      )}
    </Modal>
  );
}
