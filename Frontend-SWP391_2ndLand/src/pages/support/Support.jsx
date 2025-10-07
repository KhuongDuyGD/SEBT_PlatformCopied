import { useState } from 'react';
import {
  Layout,
  Card,
  Form,
  Input,
  Select,
  Button,
  Alert,
  Space,
  Typography,
  Row,
  Col,
  Divider,
  message
} from 'antd';
import { 
  CustomerServiceOutlined, 
  MailOutlined, 
  PhoneOutlined,
  QuestionCircleOutlined,
  BugOutlined,
  UserOutlined
} from '@ant-design/icons';

// Import các service và component mới
import { sendSupportRequest, SUPPORT_REQUEST_TYPES } from '../../api/support';
import FAQDropdown from '../../components/support/FAQDropdown';

const { Content } = Layout;
const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;
const { Option } = Select;

function Support() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [submitResult, setSubmitResult] = useState(null); // Lưu kết quả gửi form

  // Xử lý gửi form hỗ trợ - Gọi API thực tế
  const handleSubmit = async (values) => {
    setLoading(true);
    setSubmitResult(null); // Reset kết quả trước đó
    
    try {
      // Gọi API gửi yêu cầu hỗ trợ
      const response = await sendSupportRequest(values);
      
      // Handle different response formats
      const isSuccess = response && (response.success === true || response.success === undefined);
      
      if (isSuccess) {
        setSubmitResult({
          type: 'success',
          message: response?.message || 'Yêu cầu hỗ trợ đã được gửi thành công! Chúng tôi sẽ phản hồi qua email của bạn trong vòng 24 giờ.'
        });
        message.success('✅ Gửi yêu cầu hỗ trợ thành công!');
        form.resetFields();
      } else {
        setSubmitResult({
          type: 'error',
          message: response?.message || 'Có lỗi xảy ra khi gửi yêu cầu. Vui lòng thử lại.'
        });
        message.error('❌ Gửi yêu cầu thất bại!');
      }
    } catch (err) {
      console.error('Lỗi khi gửi yêu cầu hỗ trợ:', err);
      setSubmitResult({
        type: 'error',
        message: err.message || 'Có lỗi xảy ra khi kết nối đến server. Vui lòng kiểm tra kết nối mạng và thử lại.'
      });
      message.error('❌ Có lỗi xảy ra khi gửi yêu cầu!');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh', background: '#f5f5f5' }}>
      <Content style={{ padding: '24px', maxWidth: 1200, margin: '0 auto', width: '100%' }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {/* Header */}
          <div style={{ textAlign: 'center', marginBottom: 32 }}>
            <CustomerServiceOutlined style={{ fontSize: 48, color: '#1677ff', marginBottom: 16 }} />
            <Title level={2}>Hỗ Trợ Khách Hàng</Title>
            <Text type="secondary">
              Chúng tôi luôn sẵn sàng hỗ trợ bạn. Hãy liên hệ với chúng tôi khi cần thiết.
            </Text>
          </div>

          <Row gutter={[24, 24]}>
            {/* Contact Information */}
            <Col xs={24} lg={8}>
              <Card title="Thông Tin Liên Hệ" style={{ height: '100%' }}>
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  <div>
                    <Space>
                      <PhoneOutlined style={{ color: '#1677ff' }} />
                      <div>
                        <Text strong>Hotline</Text>
                        <br />
                        <Text>1900-xxxx (24/7)</Text>
                      </div>
                    </Space>
                  </div>
                  
                  <div>
                    <Space>
                      <MailOutlined style={{ color: '#1677ff' }} />
                      <div>
                        <Text strong>Email</Text>
                        <br />
                        <Text>support@2ndland.com</Text>
                      </div>
                    </Space>
                  </div>

                  <Divider />
                  
                  <div>
                    <Title level={5}>Giờ Làm Việc</Title>
                    <Paragraph>
                      <Text>Thứ 2 - Thứ 6: 8:00 - 18:00</Text>
                      <br />
                      <Text>Thứ 7 - Chủ nhật: 9:00 - 17:00</Text>
                    </Paragraph>
                  </div>
                </Space>
              </Card>
            </Col>

            {/* Support Form */}
            <Col xs={24} lg={16}>
              <Card title="Gửi Yêu Cầu Hỗ Trợ">
                {/* Hiển thị kết quả gửi form */}
                {submitResult && (
                  <Alert
                    type={submitResult.type}
                    message={submitResult.type === 'success' ? 'Gửi thành công!' : 'Gửi thất bại!'}
                    description={submitResult.message}
                    showIcon
                    closable
                    onClose={() => setSubmitResult(null)}
                    style={{ marginBottom: '1.5rem' }}
                  />
                )}
                
                <Form
                  form={form}
                  layout="vertical"
                  onFinish={handleSubmit}
                  size="large"
                >
                  <Row gutter={16}>
                    <Col xs={24} sm={12}>
                      <Form.Item
                        label="Họ và tên"
                        name="fullName"
                        rules={[
                          { required: true, message: 'Vui lòng nhập họ và tên' },
                          { min: 2, message: 'Họ và tên phải có ít nhất 2 ký tự' }
                        ]}
                      >
                        <Input
                          prefix={<UserOutlined />}
                          placeholder="Nhập họ và tên của bạn"
                        />
                      </Form.Item>
                    </Col>

                    <Col xs={24} sm={12}>
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
                          placeholder="Nhập email của bạn"
                        />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Form.Item
                    label="Loại yêu cầu"
                    name="requestType"
                    rules={[{ required: true, message: 'Vui lòng chọn loại yêu cầu' }]}
                  >
                    <Select placeholder="Chọn loại yêu cầu">
                      <Option value={SUPPORT_REQUEST_TYPES.TECHNICAL}>
                        <Space>
                          <BugOutlined />
                          Hỗ trợ kỹ thuật
                        </Space>
                      </Option>
                      <Option value={SUPPORT_REQUEST_TYPES.ACCOUNT}>
                        <Space>
                          <UserOutlined />
                          Vấn đề tài khoản
                        </Space>
                      </Option>
                      <Option value={SUPPORT_REQUEST_TYPES.LISTING}>
                        <Space>
                          <QuestionCircleOutlined />
                          Vấn đề về listing
                        </Space>
                      </Option>
                      <Option value={SUPPORT_REQUEST_TYPES.PAYMENT}>
                        <Space>
                          <MailOutlined />
                          Vấn đề thanh toán
                        </Space>
                      </Option>
                      <Option value={SUPPORT_REQUEST_TYPES.OTHER}>
                        <Space>
                          <QuestionCircleOutlined />
                          Khác
                        </Space>
                      </Option>
                    </Select>
                  </Form.Item>

                  <Form.Item
                    label="Tiêu đề"
                    name="subject"
                    rules={[
                      { required: true, message: 'Vui lòng nhập tiêu đề' },
                      { min: 5, message: 'Tiêu đề phải có ít nhất 5 ký tự' }
                    ]}
                  >
                    <Input placeholder="Nhập tiêu đề ngắn gọn về vấn đề" />
                  </Form.Item>

                  <Form.Item
                    label="Mô tả chi tiết"
                    name="description"
                    rules={[
                      { required: true, message: 'Vui lòng mô tả chi tiết vấn đề' },
                      { min: 20, message: 'Mô tả phải có ít nhất 20 ký tự' }
                    ]}
                  >
                    <TextArea
                      rows={6}
                      placeholder="Vui lòng mô tả chi tiết vấn đề bạn gặp phải..."
                    />
                  </Form.Item>

                  <Form.Item>
                    <Button
                      type="primary"
                      htmlType="submit"
                      loading={loading}
                      size="large"
                      style={{ width: '100%' }}
                    >
                      {loading ? 'Đang gửi...' : 'Gửi Yêu Cầu Hỗ Trợ'}
                    </Button>
                    
                    {/* Thông báo hướng dẫn */}
                    <div style={{ 
                      marginTop: '1rem', 
                      padding: '0.75rem',
                      backgroundColor: '#f0f8ff',
                      border: '1px solid #d1ecf1',
                      borderRadius: '6px',
                      fontSize: '0.9rem',
                      color: '#0c5460'
                    }}>
                      <div style={{ fontWeight: '600', marginBottom: '0.5rem' }}>
                        📧 Sau khi gửi yêu cầu:
                      </div>
                      <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                        <li>Email sẽ được gửi đến đội ngũ hỗ trợ</li>
                        <li>Bạn sẽ nhận được phản hồi qua email trong vòng 24 giờ</li>
                        <li>Vui lòng kiểm tra cả thư mục spam/junk mail</li>
                      </ul>
                    </div>
                  </Form.Item>
                </Form>
              </Card>
            </Col>
          </Row>

          {/* FAQ Section - Component mới với dropdown */}
          <Card style={{ padding: '1rem' }}>
            <FAQDropdown />
          </Card>
        </Space>
      </Content>
    </Layout>
  );
}

export default Support;
