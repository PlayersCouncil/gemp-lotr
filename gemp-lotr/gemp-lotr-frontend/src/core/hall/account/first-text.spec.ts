import { firstText } from './first-text';

describe('firstText', () => {
  it('takes the first text that is not empty', () => {
    expect(firstText(null, '', 'b', 'c')).toBe('b');
    expect(firstText(undefined, null)).toBe('');
  });
});
