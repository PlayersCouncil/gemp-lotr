import { describe, expect, it } from 'vitest';
import { generateRangeOfNumbers } from './range-generation.functions';

describe('range-generation.functions', () => {
  describe('generateRangeOfNumbers', () => {
    it('should return [1, 2, 3], when passed lower of 1 and upper bound of 3', () => {
      const expected = [1, 2, 3];
      expect(generateRangeOfNumbers(1, 3)).toStrictEqual(expected);
    });

    it('should return an array of 3-12, when passed lower of 1 and upper bound of 12', () => {
      const expected = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
      expect(generateRangeOfNumbers(3, 12)).toStrictEqual(expected);
    });
  });
});
