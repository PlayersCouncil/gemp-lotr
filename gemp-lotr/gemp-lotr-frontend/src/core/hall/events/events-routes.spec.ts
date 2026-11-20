import { eventsCommands, legacyEventsLink, parseEventsRoute } from './events-routes';

describe('events routes', () => {
  it('reads the routes', () => {
    expect(parseEventsRoute(null, null)).toEqual({ sub: 'calendar' });
    expect(parseEventsRoute('leagues', '1700000001')).toEqual({ sub: 'leagues', code: '1700000001' });
    expect(parseEventsRoute('leagues', 'bad code!')).toEqual({ sub: 'leagues', code: null });
    expect(parseEventsRoute('tournaments', 'daily-pc_movie.1')).toEqual({ sub: 'tournaments', id: 'daily-pc_movie.1' });
    expect(parseEventsRoute('admin', null)).toBeNull();
  });

  it('makes the router commands', () => {
    expect(eventsCommands({ sub: 'calendar' })).toEqual(['events', 'calendar']);
    expect(eventsCommands({ sub: 'leagues', code: '17' })).toEqual(['events', 'leagues', '17']);
    expect(eventsCommands({ sub: 'tournaments', id: null })).toEqual(['events', 'tournaments']);
  });

  it('maps the legacy hall links', () => {
    expect(legacyEventsLink('#events')).toEqual({ sub: 'calendar' });
    expect(legacyEventsLink('#events/tournaments')).toEqual({ sub: 'tournaments', id: null });
    expect(legacyEventsLink('#league-1700000001')).toEqual({ sub: 'leagues', code: '1700000001' });
    expect(legacyEventsLink('#tournament-wc.2026')).toEqual({ sub: 'tournaments', id: 'wc.2026' });
    expect(legacyEventsLink('#help/formats')).toBeNull();
  });
});
