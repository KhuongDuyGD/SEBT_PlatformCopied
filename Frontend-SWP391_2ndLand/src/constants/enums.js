// Centralized enum / constant definitions for syncing with backend
// If backend updates enums, adjust here (optionally generate from OpenAPI in future).

// Listing lifecycle status (backend ListingStatus enum)
export const ListingStatus = Object.freeze({
  ACTIVE: 'ACTIVE',
  PAY_WAITING: 'PAY_WAITING',
  SOLD: 'SOLD',
  EXPIRED: 'EXPIRED',
  REMOVED: 'REMOVED',
  SUSPENDED: 'SUSPENDED',
});

// Separate approval workflow status (backend ApprovalStatus enum)
export const ApprovalStatus = Object.freeze({
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  REQUIRES_CHANGES: 'REQUIRES_CHANGES',
});

export const ListingType = Object.freeze({
  NORMAL: 'NORMAL',
  PREMIUM: 'PREMIUM',
  FEATURED: 'FEATURED',
});

export const VehicleType = Object.freeze({
  BIKE: 'BIKE',
  CAR: 'CAR',
  MOTORBIKE: 'MOTORBIKE',
});

export const VehicleCondition = Object.freeze({
  EXCELLENT: 'EXCELLENT',
  GOOD: 'GOOD',
  FAIR: 'FAIR',
  POOR: 'POOR',
  NEEDS_MAINTENANCE: 'NEEDS_MAINTENANCE',
});

export const BatteryCondition = Object.freeze({
  EXCELLENT: 'EXCELLENT',
  GOOD: 'GOOD',
  FAIR: 'FAIR',
  POOR: 'POOR',
  NEEDS_REPLACEMENT: 'NEEDS_REPLACEMENT', // ensure matches backend if added
});

export const UserRole = Object.freeze({
  ADMIN: 'ADMIN',
  MEMBER: 'MEMBER',
  STAFF: 'STAFF',
});

export const UserStatus = Object.freeze({
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  BLOCKED: 'BLOCKED',
  SUSPENDED: 'SUSPENDED',
});

// Utility helpers
export function isActiveListing(status) {
  return status === ListingStatus.ACTIVE;
}

export function isPremium(listingType) {
  return listingType === ListingType.PREMIUM || listingType === ListingType.FEATURED;
}

export default {
  ListingStatus,
  ApprovalStatus,
  ListingType,
  VehicleType,
  VehicleCondition,
  BatteryCondition,
  UserRole,
  UserStatus,
  isActiveListing,
  isPremium,
};
