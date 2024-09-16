import { describe, expect, it } from 'vitest';

import { buildNotFoundUrl } from './not-found-image.functions';

describe('not-found-image.functions', () => {
  describe('buildNotFoundImageUrl', () => {
    it('should generate a URL that is hosted on the LOTR TCG Wiki and has a query param containing the original blueprint ID', () => {
      const input = '02_024';
      expect(buildNotFoundUrl(input)).toContain(input);
    });
  });
});
