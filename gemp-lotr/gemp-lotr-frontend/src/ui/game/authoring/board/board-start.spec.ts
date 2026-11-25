import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { CardHover } from '../../../../core/cards/card-hover.service';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { CardViews } from '../../../../core/cards/card-views.service';
import { AuthoringBoard, DraftStart, DraftState, LaidSite } from '../../../../core/game/authoring/authoring-state';
import { AUTHOR_UI_KEY, GatheredBoard } from '../../../../core/game/authoring/authoring-ui-store';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { START_DEFAULT } from '../../../../core/game/authoring/board-start';
import { provideGameState } from '../../../../core/game/game-state';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { HallTab } from '../../../../core/platform/hall-tab.service';
import { PageCues } from '../../../../core/platform/page-cues';
import { PageExit } from '../../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../../core/platform/page-location';
import { boardCard, draftBoard, draftGameXml, draftState, result } from '../../../../testing/authoring';
import { provideFakeNotifier, settle } from '../../../../testing/fake-notifier';
import { BoardTabComponent } from '../board-tab.component';
import { sentForm } from '../../../../testing/sent-form';
import { AUTHOR, answerCall, authorCall, sentPatch } from '../../../../testing/authoring-requests';

const GAME = '/gemp-lotr-server/game/7';

const DRAFT = {
  id: 'test',
  title: 'A Test',
  kind: 'tutorial' as const,
  human: 'freeps' as const,
  steps: [],
  rules: { oneSided: true },
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', aragorn: '1_89' },
    shadow: { oppFrodo: '10_121', orc: '1_191', troll: '1_165' },
  },
  ringBearer: { freeps: 'frodo', shadow: 'oppFrodo' },
  setup: { freeps: { characters: 'aragorn' }, shadow: { minions: 'orc, troll' } },
};

const laid = (number: number, controlledBy: LaidSite['controlledBy'] = null): LaidSite => ({
  number,
  alias: `site${number}`,
  blueprintId: ['1_323', '1_329', '1_341'][number - 1]!,
  name: `Site ${number}`,
  owner: 'freeps',
  controlledBy,
});

/** The board of the draft: Frodo, Aragorn and two minions on the table; the fellowship at site 3, the Shadow at 2. */
function board(extra: Partial<AuthoringBoard> = {}): AuthoringBoard {
  const plain = draftBoard();
  return {
    ...plain,
    cards: [
      ...plain.cards,
      boardCard('aragorn', { blueprintId: '1_89', name: 'Aragorn, Ranger of the North', cardId: 40 }),
      boardCard('orc', {
        blueprintId: '1_191',
        name: 'Goblin Runner',
        type: 'MINION',
        owner: 'shadow',
        zone: 'minions',
        cardId: 41,
      }),
      boardCard('troll', {
        blueprintId: '1_165',
        name: 'Cave Troll',
        type: 'MINION',
        owner: 'shadow',
        zone: 'minions',
        cardId: 42,
      }),
    ],
    sites: {
      freeps: [...plain.sites.freeps, { number: 4, id: '1_348', alias: 'site4', name: 'Site 1_348' }],
      shadow: [...plain.sites.shadow, { number: 4, id: '1_348', alias: 'site4opp', name: 'Site 1_348' }],
    },
    markers: { freeps: 3, shadow: 2 },
    currentSite: 3,
    moves: 1,
    path: [laid(1, 'shadow'), laid(2), laid(3)],
    skirmishes: {},
    ...extra,
  };
}

const start = (extra: Partial<DraftStart> = {}): DraftStart => ({ ...START_DEFAULT, ...extra });

const withStart = (extra: Partial<DraftStart> = {}, state: Partial<DraftState> = {}) =>
  draftState({ draft: DRAFT, board: board(), start: start(extra), ...state });

describe('the Board tab: where the story starts, the markers, sites without numbers', () => {
  let fixture: ComponentFixture<BoardTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        CardInfoService,
        CardHover,
        CardViews,
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    fixture = TestBed.createComponent(BoardTabComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  /**
   * The tab on the draft's game; the number printed on each site (`printed`, 0: none) as the state says it. Answers
   * the cards looked up meanwhile (none: the state says the numbers).
   */
  async function open(state: DraftState = withStart(), printed = 1) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    backend.expectOne((r) => r.url === `${AUTHOR}state`).flush(JSON.stringify(withPrinted(state, printed)));
    await redraw();
    const lookUps = backend.match((r) => r.url === `${AUTHOR}cards`);
    await redraw();
    return lookUps;
  }

  async function redraw() {
    await settle();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  async function update(state: DraftState) {
    authoring.setState(state);
    await redraw();
  }

  const find = <T extends Element = HTMLElement>(selector: string) => {
    const found = el.querySelector<T>(selector);
    expect(found, selector).not.toBeNull();
    return found!;
  };
  const all = (selector: string) => [...el.querySelectorAll<HTMLElement>(selector)];
  const text = (selector: string) => find(selector).textContent?.replace(/\s+/g, ' ').trim() ?? '';
  const row = (part: string) => `gd-author-board-start .gd-author-board-start__row[data-part=${part}]`;

  async function click(selector: string) {
    find<HTMLElement>(selector).click();
    await redraw();
  }

  async function choose(selector: string, value: string) {
    const select = find<HTMLSelectElement>(selector);
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await redraw();
  }

  async function tick(selector: string, on: boolean) {
    const box = find<HTMLInputElement>(`${selector} input`);
    box.checked = on;
    box.dispatchEvent(new Event('change'));
    await redraw();
  }

  /** Types in a number field; `then`: the event that gathers it (none: it is left typed). */
  async function type(part: string, value: string, then: 'change' | 'enter' | null = 'change') {
    const field = find<HTMLInputElement>(`${row(part)} input`);
    field.value = value;
    field.dispatchEvent(new Event('input'));
    await redraw();
    if (then === 'change') field.dispatchEvent(new Event('change'));
    if (then === 'enter') field.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await redraw();
    return field;
  }

  const gathered = () => all('.gd-author-board-gathered__text').map((line) => line.textContent?.trim());
  const stored = () => (JSON.parse(sessionStorage.getItem(AUTHOR_UI_KEY)!) as { board: GatheredBoard | null }).board;
  const patch = (request: TestRequest) => sentPatch(request) as { top: Record<string, unknown> };

  async function answer(what: string, body: unknown, error: string | null = null): Promise<TestRequest> {
    const request = authorCall(backend, what);
    answerCall(request, body, error);
    await settle();
    TestBed.tick();
    await redraw();
    return request;
  }

  describe('where the story starts', () => {
    it('shows the start as the draft says it: no late start, no moves, skirmishes or sites to control at site 1', async () => {
      await open();
      expect(text('gd-author-board-start .gd-author-board__heading')).toBe('Where the story starts');
      expect(
        all('gd-author-board-start .gd-author-board-start__row:not([data-part=path])').map(
          (each) => each.dataset['part'],
        ),
      ).toEqual(['site', 'opponentSite', 'phase', 'turn', 'twilight']);
      expect(find<HTMLSelectElement>(`${row('site')} select`).value).toBe('1');
      expect(all(`${row('site')} option`)).toHaveLength(9);
      expect(find<HTMLSelectElement>(`${row('phase')} select`).value).toBe('fellowship');
      expect(find<HTMLInputElement>(`${row('turn')} input`).value).toBe('1');
      expect(el.querySelector('.gd-author-board-start__late')).toBeNull();
      expect(el.querySelector('.gd-author-board-start__fights')).toBeNull();
      expect(el.querySelector('.gd-author-board-start__held')).toBeNull();
      expect(el.querySelector('[data-problem]')).toBeNull();
    });

    it('shows a late start, the moves past the Fellowship phase at site 3, the Shadow marker level when together', async () => {
      await open(
        withStart({
          site: 4,
          opponentSite: 4,
          phase: 'maneuver',
          moves: 2,
          movesSet: true,
          markersTogether: true,
          late: true,
        }),
      );
      expect(text('.gd-author-board-start__late')).toBe('late start');
      expect(find('.gd-author-board-start__late').title).toMatch(/^The game is carried on from here/);
      expect(find<HTMLInputElement>(`${row('moves')} input`).value).toBe('2');
      const shadow = find<HTMLSelectElement>(`${row('opponentSite')} select`);
      expect([shadow.value, shadow.disabled]).toEqual(['4', true]);
      expect(shadow.title).toMatch(/level with the fellowship/);
    });

    it('gathers each part with the tab’s changes, a line each; a part as the draft has it is not gathered', async () => {
      await open();
      await choose(`${row('site')} select`, '3');
      await choose(`${row('phase')} select`, 'maneuver');
      await type('twilight', '4', 'enter');
      await type('turn', '2');
      expect(gathered()).toEqual([
        'start at site 3',
        'start in the maneuver phase',
        'start on turn 2',
        'twilight 4 at the start',
      ]);
      expect(find(row('site')).hasAttribute('data-changed')).toBe(true);
      expect(find(row('opponentSite')).hasAttribute('data-changed')).toBe(false);
      expect(text('.gd-author-board-start__late')).toBe('late start');
      expect(find<HTMLInputElement>(`${row('moves')} input`).value).toBe('1');
      expect(text('[data-action=submit]')).toBe('Submit changes (4)');
      expect(stored()!.start).toEqual({ site: 3, phase: 'maneuver', turn: 2, twilight: 4 });
      await choose(`${row('site')} select`, '1');
      expect(gathered()).toEqual(['start in the maneuver phase', 'start on turn 2', 'twilight 4 at the start']);
      // (moves go where they cannot be other than the phase's own)
      expect(el.querySelector(row('moves'))).toBeNull();
      all('[data-kind=start] [data-action=undo]')[0]!.click();
      await redraw();
      expect(gathered()).toEqual(['start on turn 2', 'twilight 4 at the start']);
      expect(find('[data-kind=start]').dataset['part']).toBe('turn');
    });

    it('keeps what is typed while the state comes again, and gathers only a number', async () => {
      await open();
      const field = await type('twilight', '7', null);
      await update(withStart({ twilight: 2 }, { revision: 2 }));
      expect(field.value).toBe('7');
      expect(gathered()).toEqual([]);
      field.dispatchEvent(new Event('change'));
      await redraw();
      expect(gathered()).toEqual(['twilight 7 at the start']);
      await type('turn', 'x');
      field.dispatchEvent(new Event('change'));
      await redraw();
      expect(gathered()).toEqual(['twilight 7 at the start']);
      expect(find<HTMLInputElement>(`${row('turn')} input`).value).toBe('1');
    });

    it('assigns skirmishes at an Assignment start: the setup’s characters (the Ring-bearer first) and minions', async () => {
      await open(withStart({ site: 2, phase: 'assignment', skirmishes: { frodo: ['orc'] } }));
      expect(all('.gd-author-board-start__fight').map((each) => each.dataset['character'])).toEqual([
        'frodo',
        'aragorn',
      ]);
      expect(find('[data-character=frodo] [data-minion=orc]').title).toBe('Goblin Runner');
      const options = () =>
        all('[data-character=aragorn] select[data-control=fight] option').map((each) => each.getAttribute('value'));
      expect(options()).toEqual(['', 'troll']);
      await choose('[data-character=aragorn] select[data-control=fight]', 'troll');
      expect(gathered()).toEqual(['skirmishes: frodo: orc; aragorn: troll']);
      expect(el.querySelector('[data-character=aragorn] select[data-control=fight]')).toBeNull();
      await click('[data-character=frodo] [data-minion=orc] [data-action=unfight]');
      expect(gathered()).toEqual(['skirmishes: aragorn: troll']);
      await choose('[data-character=frodo] select[data-control=fight]', '');
      expect(gathered()).toEqual(['skirmishes: aragorn: troll']);
      await choose(`${row('phase')} select`, 'maneuver');
      expect(gathered()).toEqual(['start in the maneuver phase', 'no skirmishes at the start']);
      expect(el.querySelector('.gd-author-board-start__fights')).toBeNull();
    });

    it('says when the setup puts no character on the table', async () => {
      await open(withStart({ site: 2, phase: 'skirmish' }, { draft: { ...DRAFT, ringBearer: {}, setup: {} } }));
      expect(text('.gd-author-board-start__fights .gd-author-note')).toBe('The setup puts no character on the table.');
    });

    it('ticks the sites behind both markers for the Shadow seat (the Free Peoples too in a game of two sides)', async () => {
      await open(withStart({ site: 4, opponentSite: 3, opponentSiteSet: true, controlled: { shadow: ['site1'] } }));
      expect(all('.gd-author-board-start__seat').map((each) => each.dataset['seat'])).toEqual(['shadow']);
      const ticks = () =>
        all('[data-seat=shadow] gd-author-check').map((each) => [
          each.dataset['site'],
          each.querySelector('input')!.checked,
        ]);
      expect(ticks()).toEqual([
        ['1', true],
        ['2', false],
      ]);
      await tick('[data-seat=shadow] gd-author-check[data-site="2"]', true);
      expect(gathered()).toEqual(['controlled: shadow site1, 2']);
      expect(find('.gd-author-board-start__seat').hasAttribute('data-changed')).toBe(true);
      await update(
        withStart({ site: 4, opponentSite: 3, opponentSiteSet: true }, { draft: { ...DRAFT, rules: {} }, revision: 2 }),
      );
      expect(all('.gd-author-board-start__seat').map((each) => each.dataset['seat'])).toEqual(['shadow', 'freeps']);
    });

    it('lays another seat’s site at a place, under the fold', async () => {
      await open(withStart({ site: 3 }));
      expect(find<HTMLDetailsElement>('details.gd-author-board-start__fold').open).toBe(false);
      const fold = find<HTMLDetailsElement>('details.gd-author-board-start__fold');
      fold.open = true;
      fold.dispatchEvent(new Event('toggle'));
      await redraw();
      expect(all(`${row('path')}`).map((each) => each.dataset['site'])).toEqual(['1', '2', '3']);
      const options = all(`${row('path')}[data-site="2"] option`).map((each) => each.textContent?.trim());
      expect(options.slice(0, 3)).toEqual(['site2 (Site 1_329)', 'site1 (Site 1_323)', 'site3 (Site 1_341)']);
      await choose(`${row('path')}[data-site="2"] select`, 'site2opp');
      expect(gathered()).toEqual(['path: site1, site2opp']);
      expect(find('summary').hasAttribute('data-changed')).toBe(true);
    });

    it('submits the start with the cards as one change to the setup, then the board’s changes', async () => {
      await open();
      await choose(`${row('site')} select`, '3');
      await choose(`${row('phase')} select`, 'maneuver');
      await type('twilight', '4');
      await click('gd-author-board-laid [data-site="3"] [data-action=marker][data-seat=shadow]');
      await click('[data-action=submit]');
      const sent = await answer('edit', result(withStart(), { gameId: '9', rebuilt: true }));
      expect(patch(sent).top).toEqual({
        setup: {
          freeps: { characters: 'aragorn' },
          shadow: { minions: 'orc, troll' },
          startAt: { site: 3, phase: 'maneuver', twilight: 4 },
        },
      });
      expect(stored()!.start).toBeUndefined();
      const ops = await answer('do', result(withStart(), { gameId: '9', rebuilt: true }));
      expect(sentForm(ops.request).get('op')).toBe('marker(shadow, 3)');
      expect(gathered()).toEqual([]);
    });

    it('says a refused start where it is changed, keeping what was gathered', async () => {
      await open();
      await choose(`${row('site')} select`, '3');
      await choose(`${row('phase')} select`, 'maneuver');
      await type('moves', '5');
      await click('[data-action=submit]');
      await answer('edit', null, 'setup.startAt.moves: more moves than sites');
      expect(text('[data-problem]')).toBe('setup.startAt.moves: more moves than sites');
      expect(gathered()).toEqual(['start at site 3', 'start in the maneuver phase', 'moves this turn: 5']);
      await type('turn', '2');
      expect(el.querySelector('[data-problem]')).toBeNull();
      await click('[data-action=submit]');
      await answer('edit', result(withStart({ site: 3, moves: 5, movesSet: true, turn: 2 })));
      expect(el.querySelector('[data-problem]')).toBeNull();
      expect(gathered()).toEqual([]);
    });

    it('takes a card taken out of the scenario out of the skirmishes it is in', async () => {
      const state = withStart(
        { site: 2, phase: 'assignment', skirmishes: { frodo: ['orc', 'troll'] } },
        {
          draft: {
            ...DRAFT,
            setup: { ...DRAFT.setup, startAt: { site: 2, phase: 'assignment' }, skirmishes: { frodo: 'orc + troll' } },
          },
        },
      );
      await open(state);
      await click('.gd-author-board-card[data-alias="orc"] [data-action=drop]');
      await click('[data-action=submit]');
      const sent = patch(await answer('edit', result(state)));
      expect(sent.top['setup']).toEqual({
        freeps: { characters: 'aragorn' },
        shadow: { minions: 'troll' },
        startAt: { site: 2, phase: 'assignment' },
        skirmishes: { frodo: 'troll' },
      });
      expect((sent.top['cards'] as { shadow: object }).shadow).toEqual({ oppFrodo: '10_121', troll: '1_165' });
    });

    it('for the setup, a marker or the twilight set on the board takes the place of the start’s part, and back', async () => {
      await open();
      await choose(`${row('site')} select`, '3');
      // (where the story starts is the setup's whichever board is chosen: it does not keep the choice from changing)
      await click('[data-where=setup] input');
      expect(authoring.ui().where).toBe('setup');
      await click('gd-author-board-laid [data-site="2"] [data-action=marker][data-seat=freeps]');
      expect(gathered()).toEqual(['marker(freeps, 2)']);
      expect(find<HTMLSelectElement>(`${row('site')} select`).value).toBe('2');
      await choose(`${row('site')} select`, '4');
      expect(gathered()).toEqual(['start at site 4']);
      await click('[data-counter=twilight] [data-action=set]');
      await type('twilight', '3');
      expect(gathered()).toEqual(['start at site 4', 'twilight 3 at the start']);
    });
  });

  describe('the path on the table', () => {
    it('shows each site laid with the markers and who controls it, and the next one to lay', async () => {
      await open();
      const rows = all('.gd-author-board-laid__site');
      expect(rows.map((each) => [each.dataset['site'], each.hasAttribute('data-laid')])).toEqual([
        ['1', true],
        ['2', true],
        ['3', true],
        ['4', false],
      ]);
      expect(find('.gd-author-board-laid__site[data-site="3"] [data-seat=freeps]').hasAttribute('data-here')).toBe(
        true,
      );
      expect(find('.gd-author-board-laid__site[data-site="2"] [data-seat=shadow]').getAttribute('aria-pressed')).toBe(
        'true',
      );
      // (only site 1 is behind both markers: it can be controlled)
      expect(find<HTMLSelectElement>('[data-site="1"] [data-control=control] select').value).toBe('shadow');
      expect(el.querySelector('[data-site="2"] [data-control=control]')).toBeNull();
      expect(text('.gd-author-board-laid__site[data-site="4"] .cardHint')).toBe('Site 1_348');
    });

    it('gathers a marker put on a site (laying the path on), and a site freed or controlled', async () => {
      await open();
      await click('[data-site="3"] [data-action=marker][data-seat=freeps]');
      expect(gathered()).toEqual([]);
      await click('[data-site="4"] [data-action=marker][data-seat=shadow]');
      expect(gathered()).toEqual(['marker(shadow, 4)']);
      expect(all('.gd-author-board-laid__site').map((each) => each.dataset['site'])).toEqual(['1', '2', '3', '4']);
      expect(find('.gd-author-board-laid__site[data-site="4"]').hasAttribute('data-changed')).toBe(true);
      await choose('[data-site="2"] [data-control=control] select', 'shadow');
      await choose('[data-site="1"] [data-control=control] select', '');
      expect(gathered()).toEqual(['marker(shadow, 4)', 'control(2, shadow)', 'liberate(1)']);
      await click('[data-action=submit]');
      const sent = await answer('do', result(withStart(), { events: true }));
      expect(sentForm(sent.request).get('op')).toBe('marker(shadow, 4), control(2, shadow), liberate(1)');
      expect(sentForm(sent.request).get('setup')).toBe('false');
    });
  });

  describe('the path on the table, for the setup', () => {
    it('shows the markers and who controls a site as the scenario starts', async () => {
      await open(withStart({ site: 3, opponentSite: 3, opponentSiteSet: true, controlled: { shadow: ['2'] } }));
      await click('[data-where=setup] input');
      const laidRow = (number: number) => `.gd-author-board-laid__site[data-site="${number}"]`;
      expect(find(`${laidRow(3)} [data-seat=shadow]`).hasAttribute('data-here')).toBe(true);
      expect(find<HTMLSelectElement>(`${laidRow(2)} [data-control=control] select`).value).toBe('shadow');
      expect(find<HTMLSelectElement>(`${laidRow(1)} [data-control=control] select`).value).toBe('');
      await click(`${laidRow(3)} [data-action=marker][data-seat=shadow]`);
      expect(gathered()).toEqual([]);
      await click(`${laidRow(4)} [data-action=marker][data-seat=freeps]`);
      expect(gathered()).toEqual(['marker(freeps, 4)']);
      expect(find(laidRow(4)).hasAttribute('data-changed')).toBe(true);
      expect(find<HTMLSelectElement>(`${row('site')} select`).value).toBe('4');
    });
  });

  describe('the path editor', () => {
    it('takes a site taken off the path out of the setup’s sites controlled and path', async () => {
      const state = withStart(
        { site: 3 },
        {
          draft: {
            ...DRAFT,
            setup: { ...DRAFT.setup, controlled: { shadow: 'site1, site4' }, path: 'site1, site2, site4' },
          },
        },
      );
      await open(state);
      await click('.gd-author-board-path__site[data-site="4"] [data-action=drop-site]');
      expect(patch(await answer('edit', result(state))).top).toEqual({
        sites: { freeps: '1_323, 1_329, 1_341', shadow: '1_323, 1_329, 1_341' },
        setup: {
          freeps: { characters: 'aragorn' },
          shadow: { minions: 'orc, troll' },
          controlled: { shadow: 'site1' },
          path: 'site1, site2',
        },
      });
    });

    it('takes the number printed on each path’s first site from the state; a numbered path is as it was', async () => {
      const lookUps = await open();
      expect(lookUps).toEqual([]);
      expect(text('.gd-author-board-path__site[data-site="2"] .gd-author-board-path__number')).toBe('Site 2');
      expect(el.querySelector('[data-unnumbered]')).toBeNull();
      expect(text('gd-author-board-path > [data-action=add-site]')).toBe('+ Site 5');
    });

    it('lists a path of sites without numbers: the first laid first, ↑ puts a site before the one above, + Site', async () => {
      await open(withStart(), 0);
      expect(all('.gd-author-board-path__site[data-unnumbered]')).toHaveLength(4);
      expect(text('.gd-author-board-path__site[data-site="2"] .gd-author-board-path__number')).toBe('#2');
      expect(text('.gd-author-board-path__site[data-site="1"] .gd-author-board-path__first')).toBe('laid first');
      expect(el.querySelector('.gd-author-board-path__site[data-site="1"] [data-action=raise]')).toBeNull();
      expect(text('gd-author-board-path > [data-action=add-site]')).toBe('+ Site');
      await click('.gd-author-board-path__site[data-site="2"] [data-action=raise]');
      expect(patch(await answer('edit', result(withPrinted(withStart(), 0)))).top).toEqual({
        sites: {
          freeps: { site2: '1_329', site1: '1_323', site3: '1_341', site4: '1_348' },
          shadow: { site2opp: '1_329', site1opp: '1_323', site3opp: '1_341', site4opp: '1_348' },
        },
      });
      // (a site found: those without a number first)
      await click('gd-author-board-path > [data-action=add-site]');
      const search = find<HTMLInputElement>('gd-author-board-path gd-author-card-search input');
      search.value = 'road';
      search.dispatchEvent(new Event('input'));
      vi.advanceTimersByTime(400);
      const requests = backend.match((r) => r.url === `${AUTHOR}cards`);
      const site = (id: string, number: number) => ({
        id,
        name: id,
        type: 'site',
        culture: null,
        side: null,
        cost: 0,
        site: number,
      });
      requests[0]!.flush(JSON.stringify({ cards: [site('1_323', 1), site('11_236', 0)] }));
      await redraw();
      expect(all('gd-author-board-path .gd-author-search__result').map((each) => each.textContent?.trim())).toEqual([
        '11_236  ·  11_236  ·  no number',
        '1_323  ·  1_323  ·  site 1',
      ]);
    });
  });
});

/** The state with the number printed on every site of both paths (and of the path laid) said: `printed` (0: none). */
function withPrinted(state: DraftState, printed: number): DraftState {
  const board = state.board;
  if (!board) return state;
  return {
    ...state,
    board: {
      ...board,
      sites: {
        freeps: board.sites.freeps.map((site) => ({ printed, ...site })),
        shadow: board.sites.shadow.map((site) => ({ printed, ...site })),
      },
      path: board.path?.map((site) => ({ printed, ...site })),
    },
  };
}
