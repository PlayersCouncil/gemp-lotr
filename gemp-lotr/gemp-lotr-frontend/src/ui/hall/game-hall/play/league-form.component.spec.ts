import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import * as w from '../../../../core/hall/play/play-messages';
import { LEAGUE_COOKIE } from '../../../../core/hall/play/league-table.service';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import { FakeNotifier } from '../../../../testing/fake-notifier';
import { readServerResponse } from '../../../../testing/files';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { OpenPlayOverlay, openPlayOverlay } from '../../../../testing/play-overlay';

describe('LeagueFormComponent (Play ▸ Open League Table, CreateLeagueTable.js)', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => hall.finish());

  /** Opens the league flow; both GET /league (the dropdown's, the list's) answered with `leagues`. */
  async function openLeague(leagues = readServerResponse('leagues.xml')): Promise<OpenPlayOverlay> {
    const play = await openPlayOverlay(hall, 'league');
    for (const req of hall.backend.match((r) => r.url.endsWith('/league'))) {
      req.flush(leagues);
    }
    await play.update();
    return play;
  }

  it('creates a table in the league chosen with the deck chosen, and remembers the league', async () => {
    const play = await openLeague();
    await play.click('h1 .gd-play-form__info');
    expect(play.text('.gd-play-form__info-text')).toBe(w.HELP_LEAGUE.replace(/\s+/g, ' ').trim());
    await play.set('[data-field=league]', '1700000002');
    await play.set('[data-deck=library]', 'Starter');
    await play.click('.gd-play-form__create');
    const req = hall.request('/hall');
    expect(hall.body(req)).toMatchObject({ format: '1700000002', deckName: 'Starter', deckSource: 'library' });
    expect(hall.cookies.read(LEAGUE_COOKIE)).toBe('1700000002');
    req.flush('<result>OK</result>');
    await play.update();
    await play.update();
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it('says to join a league first when the leagues could not be loaded, and shows the list’s failure', async () => {
    const play = await openPlayOverlay(hall, 'league');
    for (const req of hall.backend.match((r) => r.url.endsWith('/league'))) {
      req.flush('', { status: 500, statusText: 'Error' });
    }
    await play.update();
    expect(play.text('.gd-play-overlay__error')).toContain('Could not load your leagues');
    const option = play.one<HTMLOptionElement>('[data-field=league] option');
    expect([option.textContent?.trim(), option.disabled]).toEqual([w.NO_LEAGUE_YET, true]);
    expect(play.text('.gd-play-form__league-error')).toContain('Could not load the current leagues.');
    await play.click('.gd-play-form__create');
    expect(play.text('.gd-play-form__result-box')).toBe(w.MUST_SELECT_LEAGUE.replace(/\s+/g, ' '));
  });

  it('joins a league from the list below, and offers the next steps', async () => {
    const play = await openLeague();
    expect(play.text('.gd-play-form__join-heading')).toBe(w.JOIN_LEAGUES_HEADING);
    const rows = play.el.querySelectorAll('.gd-play-form__league-list gd-event-row');
    expect(rows.length).toBe(3);
    rows[2]!.querySelector<HTMLButtonElement>('.gd-event-row__toggle')!.click();
    await play.update();
    hall.request('/league/1700000003', 'GET').flush(readServerResponse('league-detail.xml'));
    await play.update();
    await play.click('gd-league-detail [data-action=join]');
    expect(play.text('dialog[data-dialog=league-join] p')).toContain('Do you want to join the league by paying');
    await play.click('dialog[data-dialog=league-join] [data-action=yes]');
    hall.request('/league/1700000001').flush('');
    await play.update();
    // the dropdown's leagues again, then the league's own detail (a Limited serie: the deck builder too)
    hall.request('/league', 'GET').flush(readServerResponse('leagues.xml'));
    await play.update();
    hall.request('/league/1700000001', 'GET').flush(readServerResponse('league-detail.xml'));
    await play.update();
    await play.update();
    expect(play.one('.gd-play-form__next-steps').hasAttribute('data-error')).toBe(false);
    expect(play.text('.gd-play-form__next-steps > div')).toBe(w.leagueJoined('Constructed League'));
    expect(
      Array.from(play.el.querySelectorAll<HTMLElement>('.gd-play-form__next-buttons [data-step]')).map(
        (e) => e.dataset['step'],
      ),
    ).toEqual(['create-table', 'deckbuilder']);
    expect(play.one<HTMLAnchorElement>('[data-step=deckbuilder]').getAttribute('href')).toBe(
      '/gemp-lotr/ng/deckbuilder/',
    );
    expect(play.text('.gd-play-form__next-steps .gd-play-form__hint')).toBe(
      w.NEXT_LIMITED_HINT.replace(/\s+/g, ' ').trim(),
    );

    // the form is scrolled into view (jsdom has no scrollIntoView: one stands in for it)
    const scrolled: Element[] = [];
    Object.defineProperty(Element.prototype, 'scrollIntoView', {
      configurable: true,
      value(this: Element) {
        scrolled.push(this);
      },
    });
    try {
      await play.set('[data-field=league]', '1700000002');
      await play.click('[data-step=create-table]');
    } finally {
      delete (Element.prototype as { scrollIntoView?: unknown }).scrollIntoView;
    }
    expect(play.one<HTMLSelectElement>('[data-field=league]').value).toBe('1700000001');
    expect(scrolled.map((e) => e.tagName)).toEqual(['H1']);
    hall.backend.match(() => true);
  });

  it('says with the next steps why a league could not be joined, not in an alert', async () => {
    const play = await openLeague();
    const rows = play.el.querySelectorAll('.gd-play-form__league-list gd-event-row');
    rows[2]!.querySelector<HTMLButtonElement>('.gd-event-row__toggle')!.click();
    await play.update();
    hall.request('/league/1700000003', 'GET').flush(readServerResponse('league-detail.xml'));
    await play.update();
    await play.click('gd-league-detail [data-action=join]');
    await play.click('dialog[data-dialog=league-join] [data-action=yes]');
    hall.request('/league/1700000001').flush('', { status: 409, statusText: 'Conflict' });
    await play.update();
    await play.update();
    expect(play.one('.gd-play-form__next-steps').hasAttribute('data-error')).toBe(true);
    expect(play.text('.gd-play-form__next-steps > div')).toBe(
      w.leagueJoinFailed("You don't have enough funds to join this league."),
    );
    expect(play.el.querySelector('.gd-play-form__next-buttons [data-step]')).toBeNull();
    expect(TestBed.inject(FakeNotifier).dialogs).toEqual([]);
  });

  it('offers the solo draft of a league that can be drafted, in a new tab', async () => {
    const play = await openLeague();
    const rows = play.el.querySelectorAll('.gd-play-form__league-list gd-event-row');
    rows[2]!.querySelector<HTMLButtonElement>('.gd-event-row__toggle')!.click();
    await play.update();
    hall.request('/league/1700000003', 'GET').flush(readServerResponse('league-detail.xml'));
    await play.update();
    await play.click('gd-league-detail [data-action=join]');
    await play.click('dialog[data-dialog=league-join] [data-action=yes]');
    hall.request('/league/1700000001').flush('');
    await play.update();
    hall.request('/league', 'GET').flush(readServerResponse('leagues.xml'));
    await play.update();
    hall.request('/league/1700000001', 'GET').flush(
      readServerResponse('league-detail.xml')
        .replace(/draftable="false"/, 'draftable="true"')
        .replace(/limited="true"/g, 'limited="false"'),
    );
    await play.update();
    await play.update();
    await play.click('[data-step=go-to-draft]');
    expect(hall.navigation.opened).toEqual(['/gemp-lotr/soloDraft.html?eventId=1700000001']);
    expect(play.el.querySelector('.gd-play-form__next-steps .gd-play-form__hint')).toBeNull();
    hall.backend.match(() => true);
  });
});
