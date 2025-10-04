import { describe, it, expect } from 'vitest';
import { mapListingCart, mapListingArray, normalizeImage } from './listingMapper';

describe('listingMapper', () => {
  it('maps a single raw listing correctly', () => {
    const raw = { listingId: 10, title: 'Xe điện A', price: 123000000, thumbnailUrl: '/images/a.jpg', viewCount: 5, favorite: true };
    const mapped = mapListingCart(raw);
    expect(mapped.id).toBe(10);
    expect(mapped.title).toBe('Xe điện A');
    expect(mapped.price).toBe(123000000);
    expect(mapped.views).toBe(5);
    expect(mapped.favorited).toBe(true);
  });

  it('fallbacks to placeholder when missing image', () => {
    const raw = { listingId: 2, title: 'No Image' };
    const mapped = mapListingCart(raw);
    expect(mapped.thumbnail).toBeTruthy();
  });

  it('maps array safely', () => {
    const arr = [ { listingId:1, title:'A' }, null, { listingId:2, title:'B' } ];
    const res = mapListingArray(arr);
    expect(res.length).toBe(2);
  });

  it('normalizes relative path', () => {
    const out = normalizeImage('/media/img.png');
    expect(out.includes('/media/img.png')).toBe(true);
  });
});
