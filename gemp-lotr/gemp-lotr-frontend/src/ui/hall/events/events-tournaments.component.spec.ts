import { signal } from '@angular/core';
import { PlayerInfo } from '../../../core/session/player-info.service';
import { EventsPage, failWith, setUpEventsPage } from '../../../testing/events-server';

/** tournamentResultsUi.js in list mode: the live tournaments, each in a drawer; admins see each id as its report's link. */
describe('EventsTournamentsComponent', () => {
  let events: EventsPage;
  const player = signal<{ name: string; type: string } | null>({ name: 'alice', type: 'u' });

  beforeEach(async () => {
    player.set({ name: 'alice', type: 'u' });
    // the page asks for reduced motion when it scrolls an opened row to the top
    vi.stubGlobal('matchMedia', () => ({ matches: true }));
    events = await setUpEventsPage([{ provide: PlayerInfo, useValue: { player } }]);
  });

  afterEach(() => vi.unstubAllGlobals());

  const titles = (el: HTMLElement) =>
    Array.from(el.querySelectorAll('.gd-event-row__title')).map((title) => title.textContent);

  it("opens a tournament's link with its drawer: its standings", async () => {
    const el = await events.open('/events/tournaments/daily-pc_movie-20261120');
    events.answer('/tournament/daily-pc_movie-20261120', 'tournament-playing.xml');
    await events.render();
    const row = el.querySelector('gd-event-row[data-key="daily-pc_movie-20261120"]')!;
    expect(row.querySelector('.gd-event-row__toggle')?.textContent?.trim()).toBe('− Details');
    const detail = row.querySelector('gd-tournament-detail')!;
    expect(detail.textContent).toContain('alice');
    expect(detail.textContent).toContain('25.00%');
    expect(el.querySelector('.gd-events__code')).toBeNull();
  });

  it("shows admins and league admins each tournament's id as a link to its report", async () => {
    player.set({ name: 'alice', type: 'la' });
    const el = await events.open('/events/tournaments');
    expect(titles(el)).toEqual(['Daily PC-Movie', 'Casual PC-Fellowship']);
    const links = Array.from(el.querySelectorAll<HTMLAnchorElement>('.gd-events__code a'));
    expect(links.map((a) => [a.textContent, a.getAttribute('href'), a.target])).toEqual([
      ['daily-pc_movie-20261120', '/gemp-lotr-server/tournament/daily-pc_movie-20261120/report/html', '_blank'],
      [
        'casual-pc_fotr_block1794232985000',
        '/gemp-lotr-server/tournament/casual-pc_fotr_block1794232985000/report/html',
        '_blank',
      ],
    ]);
  });

  it('says when no tournament is running', async () => {
    await events.harness.navigateByUrl('/events/tournaments');
    events.answer('/league', 'leagues.xml');
    events.backend
      .match((request) => request.url.endsWith('/tournament'))
      .forEach((request) => request.flush('<tournaments/>'));
    await events.render();
    expect(events.page().querySelector('.gd-events__empty')?.textContent).toBe(
      'There are no running tournaments at the moment.',
    );
  });

  it('shows why the list is missing', async () => {
    await events.harness.navigateByUrl('/events/tournaments');
    failWith(events.backend, '/tournament', 500);
    await events.render();
    expect(events.page().querySelector('.gd-events__error')?.textContent).toMatch(
      /^Could not load the current tournaments\. /,
    );
    expect(events.page().querySelector('gd-event-row')).toBeNull();
  });
});
