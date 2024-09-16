import { describe, expect, it } from 'vitest';
import {
  getCardHeightByWidth,
  getCardWidthByHeight,
} from './card-dimension.functions';

describe('card-dimensions.functions', () => {
  describe('getCardHeightByWidth', () => {
    it('should return a height of 45 when passed a width of 63', () => {
      expect(getCardHeightByWidth(63).height).toBe(45);
    });
  });

  describe('getCardWidthByHeight', () => {
    it('should return a width of 63 when passed a height of 88', () => {
      expect(getCardWidthByHeight(88).width).toBe(63);
    });
  });
});
