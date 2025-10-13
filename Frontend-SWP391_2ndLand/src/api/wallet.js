import api from './axios'

/**
 * Lấy số dư ví hiện tại của user (dựa trên session / cookie backend)
 * @returns {Promise<{balance:string|number}>}
 */
export async function getMyWalletBalance() {
  const { data } = await api.get('/wallet/me')
  // Backend trả { balance: BigDecimal } -> stringify để chuẩn hóa hiển thị
  return { balance: data.balance }
}

/**
 * Lấy lịch sử giao dịch ví
 * @param {{page?:number,size?:number,purpose?:string}} params
 * @returns {Promise<{items:any[], page:number, size:number, totalPages?:number, totalElements?:number}>}
 */
export async function getWalletTransactions(params = {}) {
  const { page = 0, size = 10, purpose } = params
  const query = new URLSearchParams(
    Object.entries({ page, size, purpose }).filter(([,v]) => v !== undefined && v !== null && v !== '')
  ).toString()
  const { data } = await api.get(`/wallet/transactions?${query}`)
  // Hiện backend trả Page<DTO> (Spring Data) mapping sang JSON có content,... hay map()? -> ta phòng cả hai trường hợp
  if (data && Array.isArray(data.content)) {
    return {
      items: data.content,
      page: data.number ?? page,
      size: data.size ?? size,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
    }
  }
  // Nếu backend map() trả trực tiếp page content array (ít metadata)
  if (Array.isArray(data)) {
    return { items: data, page, size }
  }
  return { items: [], page, size }
}

/**
 * Tạo top-up intent (VNPay) – trả về orderId & paymentUrl
 * @param {number} amount
 * @returns {Promise<{orderId:string,paymentUrl:string,amount:number,expiresAt:string}>}
 */
export async function createTopUpIntent(amount) {
  const { data } = await api.post('/wallet/topups', { amount })
  return data
}

/**
 * (Future) Hoàn tất top-up nếu cần manual trigger /poll.
 * Placeholder: backend hiện có endpoint complete riêng nếu thiết kế.
 */
export async function completeTopUp(orderId) {
  const { data } = await api.post(`/wallet/topups/${orderId}/complete`)
  return data
}

/**
 * Preview phí đăng listing để hiển thị trước (frontend gọi trước khi submit form) 
 * @param {{category:string, price:number}} params
 */
export async function previewListingFee({ category, price }) {
  const query = new URLSearchParams(
    Object.entries({ category, price }).filter(([,v]) => v !== undefined && v !== null && v !== '')
  ).toString()
  const { data } = await api.get(`/listings/fee/preview?${query}`)
  return data // { fee: number }
}

export default {
  getMyWalletBalance,
  getWalletTransactions,
  createTopUpIntent,
  completeTopUp,
  previewListingFee,
}
