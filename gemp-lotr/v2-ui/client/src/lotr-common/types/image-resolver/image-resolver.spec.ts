import { describe, expect, it } from 'vitest';

import { getImageUrl } from './image-resolver';

describe('image-resolver', () => {
  describe('getImageUrl', () => {
    it('should generate a URL for an unknown image based on the default config', () => {
      // NOTE: this test basically validates that we have no guard clause for "reasonable" cards
      const input = '4000_100';
      const expected = 'https://i.lotrtcgpc.net/decipher/LOTR4000100.jpg';

      expect(getImageUrl(input)).toBe(expected);
    });

    it('should return a URL for a card from the hobbit list', () => {
      const input = '30_3';
      const expected = 'https://i.lotrtcgpc.net/hobbit/HDG30003.jpg';

      expect(getImageUrl(input)).toBe(expected);
    });

    it('should return a URL for a card from the players committee mega list', () => {
      const input = '101_3';
      const expected = 'https://i.lotrtcgpc.net/sets/vset1/V1_003.jpg';

      expect(getImageUrl(input)).toBe(expected);
    });

    it('should return a URL for a card from the default booster list', () => {
      const input = '(S)FotR - Starter';
      const expected = '/gemp-lotr/images/boosters/fotr_starter_selection.png';

      expect(getImageUrl(input)).toBe(expected);
    });

    it('should return a URL for a card from the set40 list', () => {
      const input = '40_3';
      const expected = 'http://lotrtcg2e.club/images/premiere/balinslament.jpg';

      expect(getImageUrl(input)).toBe(expected);
    });
  });
});
