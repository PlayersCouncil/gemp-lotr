import { describe, expect, it } from 'vitest';
import * as LoginErrors from './account-validation-errors.constants';
import { isLoginValid } from './create-account-validation.functions';

describe('create-account-validation.functions', () => {
  describe('isValidLogin', () => {
    it('should return false when the login is fewer than three characters', () => {
      const input = 'f';

      const result = isLoginValid(input);
      expect(result.success).toBeFalsy();
      expect(result.error.login).toBe(LoginErrors.invalidLength);
    });

    it('should return false when the login is more than ten characters', () => {
      const input = 'tswizzleistoolong';

      const result = isLoginValid(input);
      expect(result.success).toBeFalsy();
      expect(result.error?.login).toBe(LoginErrors.invalidLength);
    });

    it('should return false non-allowed chars are used', () => {
      const input = 'inv$alid$#';

      const result = isLoginValid(input);
      expect(result.success).toBeFalsy();
      expect(result.error?.login).toBe(LoginErrors.invalidLoginCharacters);
    });

    it('should return true when the login containers text and digits', () => {
      const input = 'tswizzle13';

      const result = isLoginValid(input);
      expect(result.success).toBeTruthy();
    });

    it('should return true when the login meets all requirements', () => {
      const input = 'tswizzle';

      const result = isLoginValid(input);
      expect(result.success).toBeTruthy();
    });
  });
});
