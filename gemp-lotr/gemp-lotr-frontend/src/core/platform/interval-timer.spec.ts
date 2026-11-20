import { TestBed } from '@angular/core/testing';
import { IntervalTimer } from './interval-timer';

describe('IntervalTimer', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('calls the tick every interval until stopped, and stops only once', () => {
    const tick = vi.fn();
    const stop = TestBed.inject(IntervalTimer).every(500, tick);
    vi.advanceTimersByTime(499);
    expect(tick).not.toHaveBeenCalled();
    vi.advanceTimersByTime(1001);
    expect(tick).toHaveBeenCalledTimes(3);
    stop();
    stop();
    vi.advanceTimersByTime(5000);
    expect(tick).toHaveBeenCalledTimes(3);
    expect(vi.getTimerCount()).toBe(0);
  });
});
