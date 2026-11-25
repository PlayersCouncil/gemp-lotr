import { clockText } from './game-clock';

describe('clockText', () => {
  it('writes minutes and seconds, hours from an hour on, and a sign below zero', () => {
    expect(clockText(0)).toBe('0:00');
    expect(clockText(65)).toBe('1:05');
    expect(clockText(2700)).toBe('45:00');
    expect(clockText(3600)).toBe('1:00:00');
    expect(clockText(3725)).toBe('1:02:05');
    expect(clockText(-7)).toBe('-0:07');
  });
});
