import { price } from './price';

describe('price (common.js formatPrice)', () => {
  it('writes prices as the legacy page did', () => {
    expect(price(1050)).toEqual({ gold: 10, silver: '50' });
    expect(price(5)).toEqual({ gold: 0, silver: '05' });
  });
});
