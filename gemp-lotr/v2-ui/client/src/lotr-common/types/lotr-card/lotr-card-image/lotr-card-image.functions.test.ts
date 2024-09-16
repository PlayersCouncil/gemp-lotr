import { describe, expect, it } from 'vitest';
import { getCardImage } from './lotr-card-image.functions';
import { mainImageLocation, rulesImageHref } from '../lotr-card.constants';

describe('card-image.functions', () => {
  describe('getCardImage', () => {
    it('should return the rules image (relative URL) when provided "rules" as the name', () => {
      const input = 'rules';

      expect(getCardImage(input)).toBe(rulesImageHref);
    });

    it('should return the fully qualified URL pointing to an image of the provided ID', () => {
      const input = '01_23';
      expect(getCardImage(input)).toBe(`${mainImageLocation}/LOTR01023.jpg`);
    });
  });
});
