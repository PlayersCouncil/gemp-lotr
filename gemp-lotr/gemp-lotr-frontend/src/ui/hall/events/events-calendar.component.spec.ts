import { signal } from '@angular/core';
import { PlayerInfo } from '../../../core/session/player-info.service';
import { EventsPage, failWith, setUpEventsPage } from '../../../testing/events-server';

/** calendar.js: the month's arrows and Today, a scheduled league's chip, a running tournament's standings, a failure. */
describe('EventsCalendarComponent', () => {
  let events: EventsPage;

  beforeEach(async () => {
    vi.stubGlobal('matchMedia', () => ({ matches: true }));
    events = await setUpEventsPage([
      { provide: PlayerInfo, useValue: { player: signal({ name: 'alice', type: 'u' }) } },
    ]);
  });

  afterEach(() => vi.unstubAllGlobals());

  const title = (el: HTMLElement) => el.querySelector('.gd-events-calendar__title')?.textContent;
  const bar = (el: HTMLElement, name: string) =>
    Array.from(el.querySelectorAll<HTMLButtonElement>('.gd-events-calendar__bar')).find((b) =>
      b.textContent?.includes(name),
    )!;

  it('steps a month back and forth, and Today comes back to this month', async () => {
    const el = await events.open('/events/calendar');
    const press = async (action: string) => {
      el.querySelector<HTMLButtonElement>(`.gd-events-calendar__header [data-action=${action}]`)!.click();
      await events.render();
      events.answer('/calendar', 'events-calendar.json');
      await events.render();
    };
    await press('next');
    expect(title(el)).toBe('December 2026');
    await press('previous');
    await press('previous');
    expect(title(el)).toBe('October 2026');
    await press('today');
    expect(title(el)).toBe('November 2026');
  });

  it('previews a scheduled league with its chip, and a running tournament with its standings', async () => {
    const el = await events.open('/events/calendar');
    bar(el, 'December League').click();
    await events.render();
    const preview = () => el.querySelector('.gd-events-calendar__preview')!;
    expect(preview().querySelector('.gd-events-chip')?.textContent).toBe('Scheduled');
    expect(preview().querySelector('.gd-events-chip')?.getAttribute('data-kind')).toBe('projected');

    bar(el, 'Daily PC-Movie').click();
    await events.render();
    events.answer('/tournament/daily-pc_movie-20261120', 'tournament-playing.xml');
    await events.render();
    expect(preview().querySelector('gd-tournament-detail')?.textContent).toContain('alice');
    expect(preview().querySelector('.gd-events-chip')).toBeNull();
  });

  it('says why the month could not be shown', async () => {
    await events.harness.navigateByUrl('/events/calendar');
    failWith(events.backend, '/calendar', 400);
    await events.render();
    expect(events.page().querySelector('.gd-events-calendar__error')?.textContent).toBe('Could not load the calendar.');
    expect(events.page().querySelector('.gd-events-calendar__header')).toBeNull();
  });
});
