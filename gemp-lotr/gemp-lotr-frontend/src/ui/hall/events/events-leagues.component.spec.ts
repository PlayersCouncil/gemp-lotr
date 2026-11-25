import { signal } from '@angular/core';
import { PlayerInfo } from '../../../core/session/player-info.service';
import { EventsPage, failWith, setUpEventsPage } from '../../../testing/events-server';

/** leagueResultsUi.js in list mode: the current leagues, each in a drawer; league admins see each league's code. */
describe('EventsLeaguesComponent', () => {
  let events: EventsPage;
  const player = signal<{ name: string; type: string } | null>({ name: 'alice', type: 'u' });

  beforeEach(async () => {
    player.set({ name: 'alice', type: 'u' });
    // the page asks for reduced motion when it scrolls an opened row to the top
    vi.stubGlobal('matchMedia', () => ({ matches: true }));
    events = await setUpEventsPage([{ provide: PlayerInfo, useValue: { player } }]);
  });

  afterEach(() => vi.unstubAllGlobals());

  it("shows league admins each league's code; others see none", async () => {
    let el = await events.open('/events/leagues');
    expect(el.querySelector('.gd-events__code')).toBeNull();

    player.set({ name: 'alice', type: 'l' });
    await events.render();
    el = events.page();
    const codes = Array.from(el.querySelectorAll<HTMLElement>('.gd-events__code'));
    expect(codes.map((c) => c.textContent)).toEqual(
      Array.from(el.querySelectorAll('gd-event-row')).map((row) => row.getAttribute('data-key')),
    );
    expect(codes.length).toBeGreaterThan(0);
    expect(codes[0].title).toBe('League code - click to select');
  });

  it('says when there is no current league', async () => {
    await events.harness.navigateByUrl('/events/leagues');
    events.backend
      .match((request) => request.url.endsWith('/league'))
      .forEach((request) => request.flush('<leagues/>'));
    await events.render();
    expect(events.page().querySelector('.gd-events__empty')?.textContent).toBe(
      'There are no current leagues at the moment.',
    );
  });

  it('shows why the list is missing', async () => {
    await events.harness.navigateByUrl('/events/leagues');
    failWith(events.backend, '/league', 500);
    await events.render();
    expect(events.page().querySelector('.gd-events__error')?.textContent).toMatch(
      /^Could not load the current leagues\. /,
    );
    expect(events.page().querySelector('gd-event-row')).toBeNull();
  });
});
