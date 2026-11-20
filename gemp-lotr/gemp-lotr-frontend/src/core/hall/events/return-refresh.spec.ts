import { Timers } from '../../platform/timers';
import { REFRESH_AFTER_MS, ReturnRefresh } from './return-refresh';

describe('ReturnRefresh', () => {
  let now: number;
  let refreshed: string[];
  let returns: ReturnRefresh;

  beforeEach(() => {
    now = 1000;
    refreshed = [];
    returns = new ReturnRefresh({ now: () => now } as Timers);
    returns.register('calendar', () => refreshed.push('calendar'));
    returns.register('leagues', () => refreshed.push('leagues'));
  });

  it('refreshes a part shown again after 10 s away, not sooner', () => {
    returns.setShown('calendar', true);
    expect(refreshed).toEqual([]); // registered hidden just now
    returns.setShown('calendar', false);
    now += REFRESH_AFTER_MS - 1;
    returns.setShown('calendar', true);
    expect(refreshed).toEqual([]);
    returns.setShown('calendar', false);
    now += REFRESH_AFTER_MS;
    returns.setShown('calendar', true);
    expect(refreshed).toEqual(['calendar']);
  });

  it('counts the page being hidden or blurred as being away', () => {
    returns.setShown('leagues', true);
    now += REFRESH_AFTER_MS;
    returns.setPageShown(false);
    now += REFRESH_AFTER_MS;
    returns.setPageShown(true);
    expect(refreshed).toEqual(['leagues']);
  });

  it('counts a part as left when registered hidden', () => {
    now += REFRESH_AFTER_MS;
    returns.setShown('leagues', true);
    expect(refreshed).toEqual(['leagues']);
  });
});
