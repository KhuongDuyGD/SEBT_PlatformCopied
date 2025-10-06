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

const { Content } = Layout;
const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;
const { Option } = Select;

function Support() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  // Handle form submission
  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      // Trong thực tế sẽ gọi API support với values
      console.log('Support request:', values);
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      message.success('Yêu cầu hỗ trợ đã được gửi thành công. Chúng tôi sẽ phản hồi trong vòng 24 giờ.');
      form.resetFields();
    } catch (err) {
      console.error('Support request error:', err);
      message.error('Có lỗi xảy ra. Vui lòng thử lại sau.');
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
                      <Option value="technical">
                        <Space>
                          <BugOutlined />
                          Hỗ trợ kỹ thuật
                        </Space>
                      </Option>
                      <Option value="account">
                        <Space>
                          <UserOutlined />
                          Vấn đề tài khoản
                        </Space>
                      </Option>
                      <Option value="listing">
                        <Space>
                          <QuestionCircleOutlined />
                          Vấn đề về listing
                        </Space>
                      </Option>
                      <Option value="payment">
                        <Space>
                          <MailOutlined />
                          Vấn đề thanh toán
                        </Space>
                      </Option>
                      <Option value="other">
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
                      Gửi Yêu Cầu Hỗ Trợ
                    </Button>
                  </Form.Item>
                </Form>
              </Card>
            </Col>
          </Row>

          {/* FAQ Section */}
          <Card title="Câu Hỏi Thường Gặp">
            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <Card size="small" style={{ height: '100%' }}>
                  <Title level={5}>Làm sao để đăng tin bán xe/pin?</Title>
                  <Text type="secondary">
                    Bạn cần đăng nhập tài khoản, sau đó click vào "Đăng tin" và điền đầy đủ thông tin sản phẩm.
                  </Text>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card size="small" style={{ height: '100%' }}>
                  <Title level={5}>Tại sao tài khoản tôi bị khóa?</Title>
                  <Text type="secondary">
                    Tài khoản có thể bị khóa do vi phạm quy định. Vui lòng liên hệ support để được hỗ trợ.
                  </Text>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card size="small" style={{ height: '100%' }}>
                  <Title level={5}>Làm sao để thay đổi thông tin cá nhân?</Title>
                  <Text type="secondary">
                    Vào trang Profile, click "Chỉnh sửa" và cập nhật thông tin mới.
                  </Text>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card size="small" style={{ height: '100%' }}>
                  <Title level={5}>Quên mật khẩu phải làm sao?</Title>
                  <Text type="secondary">
                    Click vào "Quên mật khẩu" tại trang đăng nhập và làm theo hướng dẫn.
                  </Text>
                </Card>
              </Col>
            </Row>
          </Card>
        </Space>
      </Content>
    </Layout>
  );
}

export default Support;
