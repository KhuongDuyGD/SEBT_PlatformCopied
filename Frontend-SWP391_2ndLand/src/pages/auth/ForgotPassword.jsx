import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Layout,
  Card,
  Form,
  Input,
  Button,
  Steps,
  Alert,
  Space,
  Typography,
  message
} from 'antd';
import { 
  MailOutlined, 
  LockOutlined,
  SafetyOutlined,
  CheckCircleOutlined
} from '@ant-design/icons';
// import api from '../../api/axios'; // TODO: Sẽ dùng khi implement backend APIs

const { Content } = Layout;
const { Title, Text } = Typography;
const { Password } = Input;

function ForgotPassword() {
  const [form] = Form.useForm();
  const [resetForm] = Form.useForm();
  const navigate = useNavigate();
  
  // State management
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [resendCooldown, setResendCooldown] = useState(0);
  
  // OTP input refs
  const inputRefs = useRef([]);

  // Steps configuration
  const steps = [
    {
      title: 'Nhập Email',
      description: 'Nhập email đã đăng ký',
      icon: <MailOutlined />
    },
    {
      title: 'Xác Thực OTP',
      description: 'Nhập mã OTP đã gửi',
      icon: <SafetyOutlined />
    },
    {
      title: 'Đặt Mật Khẩu Mới',
      description: 'Tạo mật khẩu mới',
      icon: <LockOutlined />
    },
    {
      title: 'Hoàn Thành',
      description: 'Đặt lại mật khẩu thành công',
      icon: <CheckCircleOutlined />
    }
  ];

  // Cooldown timer effect
  useEffect(() => {
    if (resendCooldown > 0) {
      const timer = setTimeout(() => {
        setResendCooldown(resendCooldown - 1);
      }, 1000);
      return () => clearTimeout(timer);
    }
  }, [resendCooldown]);

  // Auto-focus first OTP input when step changes
  useEffect(() => {
    if (currentStep === 1) {
      inputRefs.current[0]?.focus();
    }
  }, [currentStep]);

  // Handle email submission (Step 1)
  const handleEmailSubmit = async (values) => {
    setLoading(true);
    try {
      // TODO: Implement backend API endpoints
      // Tạm thời simulate việc kiểm tra email và gửi OTP
      
      // Mock validation - kiểm tra format email
      if (!values.email.includes('@')) {
        throw new Error('Invalid email format');
      }
      
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      // Mock success response
      setEmail(values.email);
      setCurrentStep(1);
      setResendCooldown(60); // 60 seconds cooldown
      message.success('Mã OTP đã được gửi đến email của bạn (Demo mode)');
      
    } catch (error) {
      console.error('Forgot password error:', error);
      message.error('Email không tồn tại trong hệ thống hoặc có lỗi xảy ra');
    } finally {
      setLoading(false);
    }
  };

  // Handle OTP input change
  const handleOtpChange = (index, value) => {
    // Only allow numeric characters
    if (!/^\d*$/.test(value) || value.length > 1) return;
    
    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    // Auto focus to next input
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  // Handle OTP backspace
  const handleOtpKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  // Handle OTP verification (Step 2)
  const handleOtpVerify = async () => {
    const otpString = otp.join('');
    if (otpString.length !== 6) {
      message.error('Vui lòng nhập đầy đủ mã OTP');
      return;
    }

    setLoading(true);
    try {
      // TODO: Implement backend OTP verification API
      // Tạm thời mock OTP verification
      
      // Demo: accept "123456" as valid OTP
      if (otpString === '123456') {
        setCurrentStep(2);
        message.success('Xác thực OTP thành công (Demo mode)');
      } else {
        throw new Error('Invalid OTP');
      }
      
    } catch (error) {
      console.error('OTP verification error:', error);
      message.error('Mã OTP không đúng. Demo mode: sử dụng "123456"');
    } finally {
      setLoading(false);
    }
  };

  // Handle resend OTP
  const handleResendOtp = async () => {
    if (resendCooldown > 0) return;
    
    setLoading(true);
    try {
      // TODO: Implement backend resend OTP API
      // Simulate resend delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      setResendCooldown(60);
      setOtp(['', '', '', '', '', '']);
      message.success('Mã OTP mới đã được gửi (Demo mode)');
    } catch (error) {
      console.error('Resend OTP error:', error);
      message.error('Không thể gửi lại mã OTP. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  // Handle password reset (Step 3)
  const handlePasswordReset = async (values) => {
    setLoading(true);
    try {
      // TODO: Implement backend password reset API
      // Simulate password reset
      console.log('Resetting password for:', email, 'New password:', values.newPassword);
      
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      setCurrentStep(3);
      message.success('Đặt lại mật khẩu thành công (Demo mode)');
      
      // Redirect to login after 3 seconds
      setTimeout(() => {
        navigate('/login');
      }, 3000);
      
    } catch (error) {
      console.error('Password reset error:', error);
      message.error('Có lỗi xảy ra. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  // Render step content
  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return (
          <Form
            form={form}
            layout="vertical"
            onFinish={handleEmailSubmit}
            size="large"
          >
            <Form.Item
              label="Email"
              name="email"
              rules={[
                { required: true, message: 'Vui lòng nhập email' },
                { type: 'email', message: 'Email không hợp lệ' }
              ]}
            >
              <Input
                prefix={<MailOutlined />}
                placeholder="Nhập email đã đăng ký"
              />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                style={{ width: '100%' }}
              >
                Gửi Mã OTP
              </Button>
            </Form.Item>
          </Form>
        );

      case 1:
        return (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div style={{ textAlign: 'center' }}>
              <Text type="secondary">
                Mã OTP đã được gửi đến email: <Text strong>{email}</Text>
              </Text>
              <br />
              <Alert 
                message="Demo Mode" 
                description="Sử dụng mã OTP: 123456" 
                type="info" 
                showIcon 
                style={{ marginTop: 12, fontSize: 12 }}
              />
            </div>

            {/* OTP Input */}
            <div style={{ display: 'flex', justifyContent: 'center', gap: 8 }}>
              {otp.map((digit, index) => (
                <Input
                  key={index}
                  ref={el => inputRefs.current[index] = el}
                  value={digit}
                  onChange={(e) => handleOtpChange(index, e.target.value)}
                  onKeyDown={(e) => handleOtpKeyDown(index, e)}
                  style={{
                    width: 48,
                    height: 48,
                    textAlign: 'center',
                    fontSize: 18,
                    fontWeight: 'bold'
                  }}
                  maxLength={1}
                />
              ))}
            </div>

            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Button
                type="primary"
                onClick={handleOtpVerify}
                loading={loading}
                style={{ width: '100%' }}
                disabled={otp.join('').length !== 6}
              >
                Xác Thực OTP
              </Button>

              <div style={{ textAlign: 'center' }}>
                <Button
                  type="link"
                  onClick={handleResendOtp}
                  disabled={resendCooldown > 0 || loading}
                >
                  {resendCooldown > 0 
                    ? `Gửi lại mã sau ${resendCooldown}s`
                    : 'Gửi lại mã OTP'
                  }
                </Button>
              </div>
            </Space>
          </Space>
        );

      case 2:
        return (
          <Form
            form={resetForm}
            layout="vertical"
            onFinish={handlePasswordReset}
            size="large"
          >
            <Form.Item
              label="Mật khẩu mới"
              name="newPassword"
              rules={[
                { required: true, message: 'Vui lòng nhập mật khẩu mới' },
                { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' }
              ]}
            >
              <Password
                prefix={<LockOutlined />}
                placeholder="Nhập mật khẩu mới"
              />
            </Form.Item>

            <Form.Item
              label="Xác nhận mật khẩu"
              name="confirmPassword"
              dependencies={['newPassword']}
              rules={[
                { required: true, message: 'Vui lòng xác nhận mật khẩu' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('newPassword') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('Mật khẩu xác nhận không khớp'));
                  },
                }),
              ]}
            >
              <Password
                prefix={<LockOutlined />}
                placeholder="Nhập lại mật khẩu mới"
              />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                style={{ width: '100%' }}
              >
                Đặt Lại Mật Khẩu
              </Button>
            </Form.Item>
          </Form>
        );

      case 3:
        return (
          <div style={{ textAlign: 'center' }}>
            <CheckCircleOutlined 
              style={{ fontSize: 64, color: '#52c41a', marginBottom: 16 }} 
            />
            <Title level={3}>Đặt Lại Mật Khẩu Thành Công!</Title>
            <Text type="secondary">
              Mật khẩu của bạn đã được cập nhật thành công.
              <br />
              Bạn sẽ được chuyển về trang đăng nhập trong giây lát...
            </Text>
            
            <div style={{ marginTop: 24 }}>
              <Button 
                type="primary" 
                onClick={() => navigate('/login')}
              >
                Về Trang Đăng Nhập
              </Button>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <Layout style={{ minHeight: '100vh', background: '#f5f5f5' }}>
      <Content style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center',
        padding: '24px'
      }}>
        <Card 
          style={{ 
            width: '100%', 
            maxWidth: 500,
            boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
          }}
        >
          <div style={{ textAlign: 'center', marginBottom: 32 }}>
            <LockOutlined style={{ fontSize: 48, color: '#1677ff', marginBottom: 16 }} />
            <Title level={2}>Quên Mật Khẩu</Title>
            <Text type="secondary">
              Thực hiện theo các bước để đặt lại mật khẩu của bạn
            </Text>
          </div>

          {/* Progress Steps */}
          <div style={{ marginBottom: 32 }}>
            <Steps
              current={currentStep}
              size="small"
              items={steps.map(step => ({
                title: step.title,
                icon: step.icon
              }))}
            />
          </div>

          {/* Step Content */}
          {renderStepContent()}

          {/* Back to Login */}
          {currentStep < 3 && (
            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Button type="link" onClick={() => navigate('/login')}>
                Quay lại trang đăng nhập
              </Button>
            </div>
          )}
        </Card>
      </Content>
    </Layout>
  );
}

export default ForgotPassword;
