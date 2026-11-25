import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { provideCardInteractions } from '../../../core/cards/card-interactions';
import { LeagueDetail } from '../../../core/hall/events/events-models';
import { parseLeagueDetail } from '../../../core/hall/events/events-parsers';
import { EVENTS_SERVICES } from '../../../core/hall/events/events-tab.service';
import { Timers } from '../../../core/platform/timers';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { EVENTS_AUTO_ZOOM_COOKIE, EventsCardActions } from './events-tab.component';
import { LeagueDetailComponent } from './league-detail.component';

const LEAGUE = parseLeagueDetail(readServerResponse('league-detail.xml'))!;
const RTMD = parseLeagueDetail(readServerResponse('league-detail-rtmd.xml'))!;

@Component({
  imports: [LeagueDetailComponent],
  template: '<gd-league-detail [league]="league()" />',
})
class Host {
  readonly league = signal<LeagueDetail>(LEAGUE);
}

/** leagueResultsUi.js loadedLeague: the membership line, the results tabs (a serie's own standings), the cards. */
describe('LeagueDetailComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        ...EVENTS_SERVICES,
        provideCardInteractions({ actions: EventsCardActions, autoZoomCookie: EVENTS_AUTO_ZOOM_COOKIE }),
        { provide: Timers, useValue: { now: () => Date.UTC(2026, 10, 20), after: setTimeout, clear: clearTimeout } },
      ],
    });
  });

  function render(league: LeagueDetail = LEAGUE) {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.league.set(league);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  const cells = (row: Element | undefined) =>
    Array.from(row?.querySelectorAll('td') ?? []).map((td) => td.textContent?.trim());

  it('says an invitation-only league is joined by invitation', () => {
    const { el } = render({ ...LEAGUE, joinable: false, inviteOnly: true });
    const line = el.querySelector('[data-membership=inviteOnly]')!;
    expect(line.querySelector('b')?.textContent).toBe('Invitation-only. See below for how to join.');
    expect(el.querySelector('[data-action=join]')).toBeNull();
  });

  it("shows a serie's tab: its most matches and its standings; Overall results goes back", () => {
    const { fixture, el } = render();
    el.querySelector<HTMLButtonElement>('[data-tab=serie-1]')!.click();
    fixture.detectChanges();
    const panel = el.querySelector('.gd-league-detail__panel')!;
    expect(panel.querySelector('div')?.textContent).toBe('Maximum ranked matches in serie: 10');
    const rows = panel.querySelectorAll('.gd-league-detail__standings tr');
    expect(cells(rows[1])).toEqual(['1', 'alice', '4', '2', '75.00%', '', '2', 'carol', '3', '2', '50.00%']);

    // Finals has no standings yet: only its line
    el.querySelector<HTMLButtonElement>('[data-tab=serie-2]')!.click();
    fixture.detectChanges();
    expect(panel.textContent?.trim()).toBe('Maximum ranked matches in serie: 8');
    expect(panel.querySelector('table')).toBeNull();

    el.querySelector<HTMLButtonElement>('[data-tab=overall]')!.click();
    fixture.detectChanges();
    expect(el.querySelector('[data-tab=overall]')?.getAttribute('aria-selected')).toBe('true');
    expect(cells(panel.querySelectorAll('.gd-league-detail__standings tr')[1])[1]).toBe('alice');
  });

  it("shows a meta-site's card on a right click too, without the browser's menu", () => {
    const { el } = render(RTMD);
    const site = el.querySelector<HTMLButtonElement>('.gd-league-detail__site')!;
    const menu = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    site.dispatchEvent(menu);
    expect(menu.defaultPrevented).toBe(true);
    expect(TestBed.inject(CardInfoService).card()?.blueprintId).toBe('1_331');
  });

  it("closes a format's description", async () => {
    const { fixture, el } = render();
    el.querySelector<HTMLButtonElement>('.gd-league-detail__format')!.click();
    fixture.detectChanges();
    TestBed.inject(HttpTestingController)
      .expectOne((request) => request.url.endsWith('/hall/format/fotr_block/json'))
      .flush(readServerResponse('hall-format-definition.json'));
    await settle();
    fixture.detectChanges();
    const dialog = el.querySelector<HTMLDialogElement>('dialog[data-dialog=format-description]')!;
    expect(dialog.open).toBe(true);
    dialog.querySelector<HTMLButtonElement>('.gd-dialog__close')!.click();
    fixture.detectChanges();
    expect(dialog.open).toBe(false);
  });
});
