// Component FAQDropdown - Hiển thị danh sách câu hỏi thường gặp dưới dạng accordion
// Sử dụng trong trang Support để người dùng có thể tìm câu trả lời nhanh chóng

import { useState, useEffect } from 'react';
import { getFAQList } from '../../api/support';
import '../../css/SupportFAQ.css';

const FAQDropdown = () => {
  // State quản lý danh sách FAQ và trạng thái loading
  const [faqList, setFaqList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedItems, setExpandedItems] = useState(new Set()); // Lưu các item đang được mở rộng

  // Tải danh sách FAQ khi component mount
  useEffect(() => {
    loadFAQList();
  }, []);

  /**
   * Tải danh sách FAQ từ backend
   */
  const loadFAQList = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await getFAQList();
      
      if (response && Array.isArray(response)) {
        setFaqList(response);
      } else if (response && typeof response === 'object' && Array.isArray(response.data)) {
        // Fallback: if response has wrapper format
        setFaqList(response.data);
      } else {
        console.warn('FAQ response is not an array:', response);
        setFaqList([]); // Ensure faqList is always an array
        setError('Dữ liệu FAQ không hợp lệ');
      }
    } catch (err) {
      console.error('Lỗi khi tải FAQ:', err);
      setError(err.message || 'Có lỗi xảy ra khi tải FAQ');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Toggle mở/đóng một FAQ item
   */
  const toggleFAQItem = (index) => {
    const newExpandedItems = new Set(expandedItems);
    
    if (newExpandedItems.has(index)) {
      newExpandedItems.delete(index);
    } else {
      newExpandedItems.add(index);
    }
    
    setExpandedItems(newExpandedItems);
  };

  // Không cần lọc FAQ nữa vì đã bỏ tìm kiếm

  // Render loading state
  if (loading) {
    return (
      <div className="faq-container">
        <h3 className="faq-title">Câu Hỏi Thường Gặp</h3>
        <div className="faq-loading">Đang tải danh sách câu hỏi</div>
      </div>
    );
  }

  // Render error state
  if (error) {
    return (
      <div className="faq-container">
        <h3 className="faq-title">Câu Hỏi Thường Gặp</h3>
        <div className="faq-error">
          {error}
          <br />
          <button 
            onClick={loadFAQList}
            style={{
              marginTop: '1rem',
              padding: '0.5rem 1rem',
              backgroundColor: '#416adcff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Thử lại
          </button>
        </div>
      </div>
    );
  }

  // Render empty state
  if (faqList.length === 0) {
    return (
      <div className="faq-container">
        <h3 className="faq-title">Câu Hỏi Thường Gặp</h3>
        <div className="faq-empty">
          Chưa có câu hỏi thường gặp nào được cập nhật.
        </div>
      </div>
    );
  }

  return (
    <div className="faq-container">
      <h3 className="faq-title">💡 Câu Hỏi Thường Gặp</h3>

      {/* Danh sách FAQ - đã bỏ thanh tìm kiếm */}
      <div className="faq-list">
        {!Array.isArray(faqList) || faqList.length === 0 ? (
          <div style={{ 
            textAlign: 'center', 
            padding: '2rem',
            color: '#6c757d',
            fontStyle: 'italic'
          }}>
            {loading ? 'Đang tải câu hỏi thường gặp...' : 'Chưa có câu hỏi thường gặp nào.'}
          </div>
        ) : (
          faqList.map((item, index) => (
          <div key={index} className="faq-item">
            {/* Câu hỏi - clickable để mở/đóng */}
            <button
              className={`faq-question ${expandedItems.has(index) ? 'active' : ''}`}
              onClick={() => toggleFAQItem(index)}
              aria-expanded={expandedItems.has(index)}
              aria-controls={`faq-answer-${index}`}
            >
              <span className="faq-question-text">
                {item.question}
              </span>
              <span 
                className={`faq-icon ${expandedItems.has(index) ? 'expanded' : ''}`}
              >
                +
              </span>
            </button>

            {/* Câu trả lời - hiển thị khi được mở rộng */}
            <div 
              id={`faq-answer-${index}`}
              className={`faq-answer ${expandedItems.has(index) ? 'expanded' : ''}`}
              role="region"
              aria-labelledby={`faq-question-${index}`}
            >
              <div className="faq-answer-content">
                {item.answer}
              </div>
            </div>
          </div>
          ))
        )}
      </div>

      {/* Thông tin hỗ trợ thêm */}
      <div style={{ 
        textAlign: 'center', 
        marginTop: '2rem', 
        padding: '1rem',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        border: '1px solid #e9ecef'
      }}>
        <p style={{ margin: 0, color: '#6c757d', fontSize: '0.9rem' }}>
          Không tìm thấy câu trả lời? 
          <br />
          Hãy gửi yêu cầu hỗ trợ bên dưới để được giải đáp chi tiết.
        </p>
      </div>
    </div>
  );
};

export default FAQDropdown;
