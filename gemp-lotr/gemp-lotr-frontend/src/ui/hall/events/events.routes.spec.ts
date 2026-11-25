import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { EVENTS_LEAGUE_JOIN_LISTENER, EVENTS_QUEUE_JOINER } from '../../../core/hall/events/events-hooks';
import { CalendarQueueOffer } from '../../../core/hall/events/events-models';
import { Timers } from '../../../core/platform/timers';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { EVENTS_ROUTES } from './events.routes';

const SERVER = '/gemp-lotr-server';

describe('the Events tab', () => {
  let backend: HttpTestingController;
  let harness: RouterTestingHarness;
  let offers: CalendarQueueOffer[];
  let joined: string[];

  beforeEach(async () => {
    offers = [];
    joined = [];
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        provideRouter([{ path: 'events', children: EVENTS_ROUTES }]),
        {
          provide: Timers,
          useValue: { now: () => Date.UTC(2026, 10, 20, 12), after: setTimeout, clear: clearTimeout },
        },
        { provide: EVENTS_QUEUE_JOINER, useValue: { joinQueue: (offer: CalendarQueueOffer) => offers.push(offer) } },
        { provide: EVENTS_LEAGUE_JOIN_LISTENER, useValue: { leagueJoined: (code: string) => joined.push(code) } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  const page = () => harness.routeNativeElement as HTMLElement;
  const answer = (path: string, file: string, method = 'GET') =>
    backend
      .match((request) => request.url === SERVER + path && request.method === method)
      .forEach((request) => request.flush(readServerResponse(file)));
  async function render(): Promise<void> {
    await settle();
    harness.detectChanges();
    await harness.fixture.whenStable();
  }

  async function open(url: string): Promise<HTMLElement> {
    await harness.navigateByUrl(url);
    answer('/league', 'leagues.xml');
    answer('/tournament', 'tournaments.xml');
    answer('/calendar', 'events-calendar.json');
    await render();
    answer('/eventHistory/months', 'event-history-months.json');
    await render();
    backend
      .match((request) => request.url === `${SERVER}/eventHistory`)
      .forEach((request) =>
        request.flush(
          readServerResponse(
            request.request.params.get('kind') === 'league'
              ? 'event-history-league.json'
              : 'event-history-tournament.json',
          ),
        ),
      );
    await render();
    return page();
  }

  it('opens on the calendar, with the sub-tabs and the month', async () => {
    const el = await open('/events');
    expect(TestBed.inject(Router).url).toBe('/events/calendar');
    expect(el.dataset['subTab']).toBe('calendar');
    expect(Array.from(el.querySelectorAll('.gd-hall-sub-tabs__tab')).map((tab) => tab.textContent?.trim())).toEqual([
      'Calendar',
      'Current Leagues',
      'Current Tournaments',
    ]);
    expect(el.querySelector('.gd-events-calendar__title')?.textContent).toBe('November 2026');
    expect(el.querySelectorAll('.gd-events-calendar__week')).toHaveLength(6);
    expect(el.querySelector('.gd-events-calendar__placeholder')?.textContent).toBe(
      'Click on an event above to load details.',
    );
    const today = el.querySelector('.gd-events-calendar__day-number[data-today]');
    expect(today?.textContent?.trim()).toBe('20');
    const league = el.querySelector<HTMLElement>('.gd-events-calendar__bar[data-kind=league]')!;
    expect(league.hasAttribute('data-joined')).toBe(true);
    expect(league.style.getPropertyValue('--gd-column')).toBe('7');
  });

  it("previews a league in its drawer and goes to its row; a scheduled tournament offers the hall's Join Queue", async () => {
    const el = await open('/events/calendar');
    el.querySelector<HTMLButtonElement>('.gd-events-calendar__bar[data-kind=league]')!.click();
    await render();
    answer('/league/1700000001', 'league-detail.xml');
    await render();
    const preview = el.querySelector('.gd-events-calendar__preview')!;
    expect(preview.querySelector('.gd-event-row__title')?.textContent).toBe('Constructed League');
    expect(preview.querySelector('.gd-events-calendar__signed-up')?.textContent).toBe('You are signed up');
    expect(preview.querySelector('gd-league-detail')).not.toBeNull();
    preview.querySelector<HTMLButtonElement>('.gd-event-row__go')!.click();
    await render();
    expect(TestBed.inject(Router).url).toBe('/events/leagues/1700000001');

    await harness.navigateByUrl('/events/calendar');
    await render();
    el.ownerDocument
      .querySelectorAll<HTMLButtonElement>('.gd-events-calendar__bar[data-kind=tournament]')
      .forEach((bar) => bar.textContent?.includes('WC Qualifier') && bar.click());
    await render();
    const facts = page().querySelector('gd-calendar-facts');
    expect(facts?.textContent).toContain('Starts');
    page().querySelector<HTMLButtonElement>('.gd-events-calendar__join-queue')!.click();
    expect(offers.map((offer) => offer.id)).toEqual(['wc2026-qualifier']);
  });

  it("opens a league's link, asks before joining and tells the hall after", async () => {
    const el = await open('/events/leagues/1700000001');
    answer('/league/1700000001', 'league-detail.xml');
    await render();
    const row = el.querySelector('gd-event-row[data-key="1700000001"]')!;
    expect(row.querySelector('.gd-event-row__toggle')?.textContent?.trim()).toBe('− Details');
    expect(row.querySelector('.gd-league-detail__membership')?.textContent).toContain("You're not a member");
    row.querySelector<HTMLButtonElement>('[data-action=join]')!.click();
    await render();
    const dialog = el.querySelector('dialog[data-dialog=league-join]')!;
    expect(dialog.textContent).toContain('Do you want to join the league by paying');
    dialog.querySelector<HTMLButtonElement>('[data-action=yes]')!.click();
    answer('/league/1700000001', 'hall-table-ok.xml', 'POST');
    await render();
    answer('/league/1700000001', 'league-detail.xml');
    answer('/calendar', 'events-calendar.json');
    await render();
    expect(joined).toEqual(['1700000001']);
  });

  it('lists the live tournaments and the completed ones of the newest month', async () => {
    await open('/events/calendar');
    await harness.navigateByUrl('/events/tournaments');
    await render();
    const el = page();
    expect(Array.from(el.querySelectorAll('.gd-event-row__title')).map((title) => title.textContent)).toEqual([
      'Daily PC-Movie',
      'Casual PC-Fellowship',
    ]);
    const history = el.querySelector('gd-event-history[data-kind=tournament]')!;
    expect(history.querySelector('.gd-event-history__status')?.textContent?.trim()).toBe(
      'October 2026: 2 completed tournaments.',
    );
    expect(Array.from(history.querySelectorAll('th')).map((th) => th.textContent)).toContain('Admin');
  });
});
