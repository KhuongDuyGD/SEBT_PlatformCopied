import { describe, it, expect } from 'vitest'
import enums, { ListingStatus, ApprovalStatus, ListingType, VehicleType, VehicleCondition, BatteryCondition, UserRole, UserStatus, isActiveListing, isPremium } from './enums'

describe('enums export', () => {
  it('should export main enums object', () => {
    expect(enums).toBeTruthy()
    expect(Object.keys(enums)).toContain('ListingStatus')
  })

  it('ListingStatus has ACTIVE', () => {
    expect(ListingStatus.ACTIVE).toBe('ACTIVE')
  })

  it('isActiveListing helper works', () => {
    expect(isActiveListing(ListingStatus.ACTIVE)).toBe(true)
    expect(isActiveListing(ListingStatus.SUSPENDED)).toBe(false)
  })

  it('ListingStatus set matches backend subset', () => {
    expect(Object.values(ListingStatus).sort()).toEqual(['ACTIVE','EXPIRED','REMOVED','SOLD','SUSPENDED'].sort())
  })

  it('ApprovalStatus includes workflow states', () => {
    expect(ApprovalStatus.PENDING).toBe('PENDING')
    expect(ApprovalStatus.REQUIRES_CHANGES).toBe('REQUIRES_CHANGES')
  })

  it('isPremium helper works', () => {
    expect(isPremium('PREMIUM')).toBe(true)
    expect(isPremium('FEATURED')).toBe(true)
    expect(isPremium('NORMAL')).toBe(false)
  })

  it('VehicleType contains CAR', () => {
    expect(VehicleType.CAR).toBe('CAR')
  })

  it('Enums immutability (freeze) cannot be reassigned', () => {
    expect(() => {
      // @ts-ignore
      ListingType.PREMIUM = 'X'
    }).toThrow(TypeError)
    expect(ListingType.PREMIUM).toBe('PREMIUM')
  })
})
