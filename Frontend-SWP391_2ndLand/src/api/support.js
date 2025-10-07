// API service cho chức năng hỗ trợ khách hàng
// Xử lý việc gửi form hỗ trợ và lấy danh sách FAQ

import api from './axios';

/**
 * Gửi yêu cầu hỗ trợ đến backend
 * @param {Object} supportData - Dữ liệu form hỗ trợ
 * @param {string} supportData.fullName - Họ và tên khách hàng
 * @param {string} supportData.email - Email khách hàng
 * @param {string} supportData.requestType - Loại yêu cầu
 * @param {string} supportData.subject - Tiêu đề yêu cầu
 * @param {string} supportData.description - Mô tả chi tiết
 * @returns {Promise} Response từ server
 */
export const sendSupportRequest = async (supportData) => {
  try {
    const response = await api.post('/support/send', supportData);
    return response.data;
  } catch (error) {
    // Log lỗi để debug
    console.error('Lỗi khi gửi yêu cầu hỗ trợ:', error);
    
    // Xử lý các trường hợp lỗi khác nhau
    if (error.response) {
      // Server trả về lỗi với status code
      throw new Error(error.response.data.message || 'Có lỗi xảy ra khi gửi yêu cầu');
    } else if (error.request) {
      // Request được gửi nhưng không nhận được response
      throw new Error('Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng');
    } else {
      // Lỗi khác
      throw new Error('Có lỗi không xác định xảy ra');
    }
  }
};

/**
 * Lấy danh sách FAQ từ backend
 * @returns {Promise<Array>} Danh sách câu hỏi thường gặp
 */
export const getFAQList = async () => {
  try {
    const response = await api.get('/support/faq');
    return response.data;
  } catch (error) {
    console.error('Lỗi khi lấy danh sách FAQ:', error);
    
    if (error.response) {
      throw new Error(error.response.data.message || 'Có lỗi khi tải danh sách FAQ');
    } else if (error.request) {
      throw new Error('Không thể kết nối đến server');
    } else {
      throw new Error('Có lỗi không xác định xảy ra');
    }
  }
};

/**
 * Danh sách các loại yêu cầu hỗ trợ
 */
export const SUPPORT_REQUEST_TYPES = {
  TECHNICAL: 'technical',
  ACCOUNT: 'account', 
  LISTING: 'listing',
  PAYMENT: 'payment',
  OTHER: 'other'
};

/**
 * Mapping tên loại yêu cầu sang tiếng Việt
 */
export const SUPPORT_REQUEST_TYPE_LABELS = {
  [SUPPORT_REQUEST_TYPES.TECHNICAL]: 'Hỗ trợ kỹ thuật',
  [SUPPORT_REQUEST_TYPES.ACCOUNT]: 'Vấn đề tài khoản',
  [SUPPORT_REQUEST_TYPES.LISTING]: 'Vấn đề về listing',
  [SUPPORT_REQUEST_TYPES.PAYMENT]: 'Vấn đề thanh toán',
  [SUPPORT_REQUEST_TYPES.OTHER]: 'Khác'
};
