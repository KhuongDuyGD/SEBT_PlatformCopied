export const mockStats = {
  totalUsers: 1842,
  activeListings: 312,
  monthlyRevenue: 582000000,
  pendingComplaints: 7,
  conversionRate: 0.37,
  avgTimeToSell: 12, // days
}

export const mockUsers = [
  {
    id: 'U001',
    name: 'Nguyễn Văn A',
    email: 'a@example.com',
    phone: '0901234567',
    status: 'active',
    role: 'member',
    createdAt: '2024-08-12',
    listings: 5,
    lastLogin: '2025-09-28 10:20',
    kyc: 'verified'
  },
  {
    id: 'U002',
    name: 'Trần Thị B',
    email: 'b@example.com',
    phone: '0912345678',
    status: 'blocked',
    role: 'member',
    createdAt: '2024-10-01',
    listings: 2,
    lastLogin: '2025-09-25 08:11',
    kyc: 'pending'
  }
]

export const mockListings = [
  {
    id: 'L1001',
    type: 'xe',
    title: 'Xe máy điện VinFast Klara S 2023',
    price: 17500000,
    batteryHealth: 92,
    mileage: 4200,
    year: 2023,
    status: 'pending',
    seller: 'Nguyễn Văn A',
    createdAt: '2025-09-25'
  },
  {
    id: 'L1002',
    type: 'pin',
    title: 'Pack pin Lithium 60V 34Ah (chuẩn hãng)',
    price: 6500000,
    batteryHealth: 88,
    mileage: null,
    year: 2024,
    status: 'approved',
    seller: 'Trần Thị B',
    createdAt: '2025-09-20'
  }
]

export const mockComplaints = [
  {
    id: 'C001',
    user: 'U001',
    listing: 'L1002',
    type: 'payment',
    status: 'open',
    createdAt: '2025-09-22',
    description: 'Người bán không phản hồi sau khi thanh toán.'
  }
]

export const mockTransactions = [
  {
    id: 'T2025001',
    buyer: 'U003',
    seller: 'U001',
    listing: 'L1002',
    amount: 6400000,
    fee: 320000,
    status: 'success',
    createdAt: '2025-09-26 14:33'
  },
  {
    id: 'T2025002',
    buyer: 'U004',
    seller: 'U002',
    listing: 'L1001',
    amount: 17000000,
    fee: 850000,
    status: 'pending',
    createdAt: '2025-09-28 09:12'
  }
]

export const mockFeesConfig = {
  platformFeePercent: 5,
  listingVerificationFee: 30000,
  highlightFeePerDay: 15000,
  auctionSuccessFeePercent: 3
}