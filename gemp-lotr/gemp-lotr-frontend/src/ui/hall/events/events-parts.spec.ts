import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { provideCardInteractions } from '../../../core/cards/card-interactions';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { calendarFacts } from '../../../core/hall/events/calendar-details';
import { EventDrawer } from '../../../core/hall/events/event-drawer';
import { EventHistoryBrowser, EventHistorySource } from '../../../core/hall/events/event-history';
import {
  CompletedEventsMonth,
  EventKind,
  LeagueDetail,
  TournamentDetail,
} from '../../../core/hall/events/events-models';
import {
  parseCalendar,
  parseEventHistory,
  parseLeagueDetail,
  parseTournamentDetail,
} from '../../../core/hall/events/events-parsers';
import { EVENTS_SERVICES } from '../../../core/hall/events/events-tab.service';
import { LeagueResults } from '../../../core/hall/events/league-results.service';
import { PageNavigation } from '../../../core/platform/page-navigation';
import { Timers } from '../../../core/platform/timers';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { CalendarFactsComponent } from './calendar-facts.component';
import { EventHistoryComponent } from './event-history.component';
import { EventRowComponent } from './event-row.component';
import { EVENTS_AUTO_ZOOM_COOKIE, EventsCardActions } from './events-tab.component';
import { LeagueDetailComponent } from './league-detail.component';
import { PriceComponent } from '../shared/price.component';
import { TournamentDetailComponent } from './tournament-detail.component';

const LEAGUE = parseLeagueDetail(readServerResponse('league-detail.xml'))!;
const RTMD = parseLeagueDetail(readServerResponse('league-detail-rtmd.xml'))!;
const PLAYING = parseTournamentDetail(readServerResponse('tournament-playing.xml'))!;
const FINISHED = parseTournamentDetail(readServerResponse('tournament-finished.xml'))!;

let opened: string[];

function setUp(): void {
  opened = [];
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      provideHttpClientTesting(),
      provideFakeNotifier(),
      ...EVENTS_SERVICES,
      provideCardInteractions({ actions: EventsCardActions, autoZoomCookie: EVENTS_AUTO_ZOOM_COOKIE }),
      { provide: Timers, useValue: { now: () => Date.UTC(2026, 10, 20), after: setTimeout, clear: clearTimeout } },
      {
        provide: PageNavigation,
        useValue: { openInNewTab: (url: string) => opened.push(url) > 0, go: () => undefined },
      },
    ],
  });
}

@Component({
  imports: [LeagueDetailComponent],
  template: '<gd-league-detail [league]="league()" [membership]="membership()" />',
})
class LeagueHost {
  readonly league = signal<LeagueDetail>(LEAGUE);
  readonly membership = signal(true);
}

@Component({
  imports: [TournamentDetailComponent],
  template: '<gd-tournament-detail [tournament]="tournament()" [showRounds]="rounds()" />',
})
class TournamentHost {
  readonly tournament = signal<TournamentDetail>(PLAYING);
  readonly rounds = signal(true);
}

@Component({
  imports: [EventRowComponent],
  template: `<gd-event-row kind="league" key="17" title="A league" date="2026-11-01" [drawer]="drawer" [goLabel]="go()">
    <span gdRowExtras class="extra">x</span>
    <p class="body">body</p>
  </gd-event-row>`,
})
class RowHost {
  readonly requests = new Subject<string | null>();
  readonly go = signal<string | null>(null);
  readonly drawer = new EventDrawer<string>({
    load: () => this.requests,
    failure: () => 'failed',
    unreadableText: 'unreadable',
    emptyText: 'empty',
  });
}

describe('the Events tab parts', () => {
  beforeEach(setUp);

  it('draws a price with its coins', () => {
    const fixture = TestBed.createComponent(PriceComponent);
    fixture.componentRef.setInput('price', { gold: 10, silver: '05' });
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.getAttribute('aria-label')).toBe('10 gold 05 silver');
    expect(el.textContent?.replace(/\s+/g, ' ').trim()).toBe('10 05');
    expect(Array.from(el.querySelectorAll('.gd-price__coin')).map((coin) => coin.getAttribute('data-coin'))).toEqual([
      'gold',
      'silver',
    ]);
  });

  it('draws a row: the toggle, the drawer states and the projected content; or a fixed button', async () => {
    const fixture = TestBed.createComponent(RowHost);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    const toggle = el.querySelector<HTMLButtonElement>('.gd-event-row__toggle')!;
    expect(toggle.textContent?.trim()).toBe('+ Details');
    expect(el.querySelector('.gd-event-row__date')?.getAttribute('title')).toBe('Server time (UTC)');
    expect(el.querySelector('.gd-event-row__extras .extra')).not.toBeNull();
    expect(el.querySelector('.gd-event-row__drawer')).toBeNull();
    toggle.click();
    fixture.detectChanges();
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(el.querySelector('.gd-event-row__loading')?.textContent).toBe('Loading details…');
    fixture.componentInstance.requests.next('done');
    fixture.detectChanges();
    expect(el.querySelector('.gd-event-row__drawer')?.getAttribute('data-state')).toBe('data');
    expect(el.querySelector('.gd-event-row__drawer .body')).not.toBeNull();
    toggle.click();
    fixture.detectChanges();
    expect(el.querySelector<HTMLElement>('.gd-event-row__drawer')!.hidden).toBe(true);
    fixture.componentInstance.go.set('Go to League');
    fixture.detectChanges();
    expect(el.querySelector('.gd-event-row__go')?.textContent?.trim()).toBe('Go to League');
    expect(el.querySelector('.gd-event-row__toggle')).toBeNull();
    await settle();
  });

  it('draws a league: cost, membership, description, series, and keeps the results tab chosen', () => {
    const fixture = TestBed.createComponent(LeagueHost);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.gd-league-detail__cost')?.textContent).toContain('Cost:');
    expect(el.querySelector('[data-membership=joinable]')?.textContent).toContain(
      "You're not a member of this league.",
    );
    expect(el.querySelector('.gd-league-detail__description')?.textContent).toBe('Play three games.');
    expect(Array.from(el.querySelectorAll('.gd-league-detail__serie-name')).map((name) => name.textContent)).toEqual([
      'Serie 1 - 2026-11-01 to 2026-11-14',
      'Finals - 2026-11-15 to 2026-11-29',
    ]);
    expect(el.querySelector('.gd-league-detail__format')?.textContent?.trim()).toBe('Constructed Fellowship Block');
    expect(el.querySelectorAll('.gd-league-detail__format')).toHaveLength(1);
    expect(el.querySelector('[data-serie="2"]')?.textContent).toContain('old_format (retired)');
    // overall standings: alice, carol | bob
    const rows = el.querySelectorAll('.gd-league-detail__standings tr');
    expect(Array.from(rows[1]?.querySelectorAll('td') ?? []).map((td) => td.textContent?.trim())).toEqual([
      '1',
      'alice',
      '4',
      '2',
      '75.00%',
      '',
      '3',
      'bob',
      '1',
      '3',
      '33.33%',
    ]);
    expect(rows[2]?.querySelectorAll('td')).toHaveLength(5);
    el.querySelector<HTMLButtonElement>('[data-tab=matches]')!.click();
    fixture.detectChanges();
    expect(el.querySelector('.gd-league-detail__panel')?.textContent).toContain('alice');
    fixture.componentInstance.league.set({ ...LEAGUE, member: true, joinable: false });
    fixture.detectChanges();
    expect(el.querySelector('[data-tab=matches]')?.getAttribute('aria-selected')).toBe('true');
    expect(el.querySelector('[data-membership=member]')).not.toBeNull();
  });

  it('asks before joining, opens the draft, and shows no membership for a finished league', () => {
    const fixture = TestBed.createComponent(LeagueHost);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    el.querySelector<HTMLButtonElement>('[data-action=join]')!.click();
    expect(TestBed.inject(LeagueResults).joinQuestion()).toEqual({
      code: '1700000001',
      cost: { gold: 10, silver: '50' },
    });
    fixture.componentInstance.league.set({ ...RTMD, draftable: true });
    fixture.detectChanges();
    el.querySelector<HTMLButtonElement>('[data-action=draft]')!.click();
    expect(opened).toEqual(['/gemp-lotr/soloDraft.html?eventId=1700000002']);
    fixture.componentInstance.membership.set(false);
    fixture.detectChanges();
    expect(el.querySelector('.gd-league-detail__membership')).toBeNull();
    expect(el.querySelector('gd-league-detail')?.getAttribute('data-context')).toBe('history');
  });

  it("draws an RTMD league's path, marks the viewer's level and shows a site's card", () => {
    const fixture = TestBed.createComponent(LeagueHost);
    fixture.componentInstance.league.set(RTMD);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.gd-league-detail__level')?.textContent?.replace(/\s+/g, ' ').trim()).toBe(
      'Your level: 2 of 3',
    );
    const sites = Array.from(el.querySelectorAll<HTMLButtonElement>('.gd-league-detail__site'));
    expect(sites.map((site) => site.dataset['mark'])).toEqual(['passed', 'current', 'none']);
    expect(sites[0]!.querySelector('.gd-league-detail__site-overlay')).not.toBeNull();
    sites[0]!.click();
    const card = TestBed.inject(CardInfoService).card();
    expect([card?.blueprintId, card?.overlayImage !== null]).toEqual(['1_331', true]);
    expect(el.querySelector('.gd-league-detail__standings th:nth-child(2)')?.textContent).toBe('Level');
  });

  it("draws a tournament's lines and, once finished, its players' deck links", () => {
    const fixture = TestBed.createComponent(TournamentHost);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(
      Array.from(el.querySelectorAll('.gd-tournament-detail__line')).map((line) => line.getAttribute('data-line')),
    ).toEqual(['rounds', 'format', 'collection', 'round']);
    expect(el.querySelector('.gd-tournament-detail__deck')).toBeNull();
    fixture.componentInstance.tournament.set(FINISHED);
    fixture.componentInstance.rounds.set(false);
    fixture.detectChanges();
    const deck = el.querySelector<HTMLAnchorElement>('.gd-tournament-detail__deck')!;
    expect([deck.textContent, deck.getAttribute('href'), deck.target]).toEqual([
      'Gandalf the Grey',
      '/gemp-lotr-server/tournament/daily-pc_movie-20261031/deck/Gandalf%20the%20Grey/html',
      '_blank',
    ]);
    expect(el.querySelector('[data-line=rounds]')).toBeNull();
  });

  it("draws a calendar event's facts", () => {
    const events = parseCalendar(JSON.parse(readServerResponse('events-calendar.json'))).events;
    const fixture = TestBed.createComponent(CalendarFactsComponent);
    fixture.componentRef.setInput('facts', calendarFacts(events[0]!));
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(Array.from(el.querySelectorAll('.gd-calendar-facts__label')).map((label) => label.textContent)).toEqual([
      'Runs',
      'Cost',
    ]);
    expect(el.querySelector('gd-price')).not.toBeNull();
    expect(el.querySelectorAll('.gd-calendar-facts__series tbody tr')).toHaveLength(2);
    expect(el.querySelector('.gd-calendar-facts__description')?.textContent).toBe('Play three games a week.');
  });

  it('draws a completed-events panel: header, dropdown, status, table and details', async () => {
    const month = parseEventHistory(
      JSON.parse(readServerResponse('event-history-tournament.json')),
      '2026-10',
      'tournament',
    );
    const source: EventHistorySource = {
      months: () => of(['2026-10', '2025-12']),
      month: () => of(month),
      league: () => of(null),
      tournament: () => of(FINISHED),
      monthsCache: new Map<EventKind, string[]>(),
      monthCache: new Map<string, CompletedEventsMonth>(),
    };
    const browser = new EventHistoryBrowser('tournament', source, TestBed.inject(Timers));
    browser.start();
    await settle();
    const fixture = TestBed.createComponent(EventHistoryComponent);
    fixture.componentRef.setInput('browser', browser);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.gd-event-history__heading')?.textContent).toBe('Completed tournaments');
    expect(el.querySelector('.gd-event-history__title')?.textContent).toContain('October 2026');
    expect(el.querySelector<HTMLButtonElement>('[data-action=next]')!.disabled).toBe(true);
    expect(el.querySelector('.gd-event-history__status')?.textContent?.trim()).toBe(
      'October 2026: 2 completed tournaments.',
    );
    expect(Array.from(el.querySelectorAll('thead th')).map((th) => th.textContent)).toEqual([
      '',
      'Name',
      'Date',
      'Format',
      'Players',
      'Rounds',
      'Admin',
    ]);
    expect(el.querySelector('.gd-event-history__admin-link')?.getAttribute('href')).toBe(
      '/gemp-lotr-server/tournament/daily-pc_movie-20261031/report/html',
    );
    expect(el.querySelector('.gd-event-history__admin-text')?.textContent).toBe('evil');

    el.querySelector<HTMLButtonElement>('.gd-event-history__title')!.click();
    fixture.detectChanges();
    expect(
      Array.from(el.querySelectorAll('.gd-event-history__dd-month')).map((m) => m.getAttribute('data-month')),
    ).toEqual(['2026-10', '2025-12']);
    el.querySelector('.gd-event-history__title')!.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }),
    );
    fixture.detectChanges();
    expect(el.querySelector('.gd-event-history__dropdown')).toBeNull();

    el.querySelector<HTMLButtonElement>('.gd-event-history__detail-cell button')!.click();
    fixture.detectChanges();
    expect(el.querySelector('.gd-event-history__row')?.hasAttribute('data-expanded')).toBe(true);
    expect(el.querySelector('.gd-event-history__detail-row gd-tournament-detail')?.getAttribute('data-context')).toBe(
      'history',
    );
  });

  it('opens a format description from a serie and draws its lines', async () => {
    const fixture = TestBed.createComponent(LeagueHost);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    el.querySelector<HTMLButtonElement>('.gd-league-detail__format')!.click();
    fixture.detectChanges();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url.endsWith('/hall/format/fotr_block/json'))
      .flush(readServerResponse('hall-format-definition.json'));
    await settle();
    fixture.detectChanges();
    const dialog = el.querySelector('dialog[data-dialog=format-description]')!;
    expect(dialog.querySelector('.gd-format-description__name')?.textContent).toBe('Fellowship Block');
    expect(Array.from(dialog.querySelectorAll('.gd-format-description__line'))[1]?.textContent?.trim()).toBe(
      'Sites: Fellowship block',
    );
    dialog.querySelector<HTMLButtonElement>('.gd-format-description__card')!.click();
    expect(TestBed.inject(CardInfoService).card()?.blueprintId).toBe('1_45');
  });
});
