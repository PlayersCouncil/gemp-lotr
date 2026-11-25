import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardHover } from '../../../core/cards/card-hover.service';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { CardViews } from '../../../core/cards/card-views.service';
import { DraftState, FoundCard } from '../../../core/game/authoring/authoring-state';
import { AUTHOR_UI_KEY, GatheredBoard } from '../../../core/game/authoring/authoring-ui-store';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { WHERE_REFUSED } from '../../../core/game/authoring/board-changes.service';
import { provideGameState } from '../../../core/game/game-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { HallTab } from '../../../core/platform/hall-tab.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageExit } from '../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { boardCard, draftBoard, draftGameXml, draftState, position, result } from '../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { START_DEFAULT } from '../../../core/game/authoring/board-start';
import { BoardTabComponent } from './board-tab.component';
import { foundText, SEARCH_MS } from './widgets/card-search.component';
import { parseJson } from '../../../core/api/json';
import { sentForm } from '../../../testing/sent-form';

const GAME = '/gemp-lotr-server/game/7';
const AUTHOR = '/gemp-lotr-server/scenarios/author/';

const DRAFT = {
  id: 'test',
  title: 'A Test',
  kind: 'tutorial' as const,
  human: 'freeps' as const,
  steps: [],
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', sam: '1_311', merry: '1_303' },
    shadow: { oppFrodo: '10_121', orc: '1_191' },
  },
  ringBearer: { freeps: 'frodo', shadow: 'oppFrodo' },
  ring: { freeps: 'ring' },
  setup: { freeps: { hand: 'sam, merry' }, shadow: { hand: 'orc' } },
};

const withBoard = (extra: Partial<DraftState> = {}) => draftState({ draft: DRAFT, board: draftBoard(), ...extra });

const GANDALF: FoundCard = {
  id: '1_72',
  name: 'Gandalf, Friend of the Shirefolk',
  type: 'companion',
  culture: 'gandalf',
  side: 'free_people',
  cost: 4,
};
const TROLL: FoundCard = {
  id: '1_165',
  name: 'Cave Troll of Moria',
  type: 'minion',
  culture: 'moria',
  side: 'shadow',
  cost: 10,
};

describe('BoardTabComponent', () => {
  let fixture: ComponentFixture<BoardTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;
  let notifier: FakeNotifier;
  let cues: { go: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    cues = { go: vi.fn() };
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
        { provide: PageCues, useValue: cues },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    notifier = TestBed.inject(FakeNotifier);
    fixture = TestBed.createComponent(BoardTabComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  /** The tab on the draft's game; the number printed on each site as the state says it (`printed`, 0: none). */
  async function open(state: DraftState = withBoard(), printed = 1) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    const board = state.board;
    const sites = board && {
      freeps: board.sites.freeps.map((site) => ({ printed, ...site })),
      shadow: board.sites.shadow.map((site) => ({ printed, ...site })),
    };
    backend
      .expectOne((r) => r.url === `${AUTHOR}state`)
      .flush(JSON.stringify(board && sites ? { ...state, board: { ...board, sites } } : state));
    await settle();
    await redraw();
  }

  async function redraw() {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  const find = <T extends Element = HTMLElement>(selector: string) => {
    const found = el.querySelector<T>(selector);
    expect(found, selector).not.toBeNull();
    return found!;
  };
  const all = (selector: string) => [...el.querySelectorAll<HTMLElement>(selector)];
  const text = (selector: string) => find(selector).textContent?.replace(/\s+/g, ' ').trim() ?? '';

  async function click(selector: string) {
    find(selector).click();
    await settle();
    await redraw();
  }

  async function type(selector: string, value: string, events = ['input']) {
    const field = find<HTMLInputElement>(selector);
    field.value = value;
    for (const name of events) field.dispatchEvent(new Event(name));
    await redraw();
    return field;
  }

  async function key(selector: string, name: string) {
    find(selector).dispatchEvent(new KeyboardEvent('keydown', { key: name }));
    await settle();
    await redraw();
  }

  async function choose(selector: string, value: string) {
    const select = find<HTMLSelectElement>(selector);
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await settle();
    await redraw();
    return select;
  }

  const asked = (what: string, method = 'POST') => backend.match((r) => r.url === AUTHOR + what && r.method === method);

  async function answer(what: string, body: unknown, error: string | null = null): Promise<TestRequest> {
    const requests = asked(what);
    expect(requests, what).toHaveLength(1);
    if (error === null) requests[0]!.flush(JSON.stringify(body));
    else requests[0]!.flush(JSON.stringify({ error }), { status: 400, statusText: 'No' });
    await settle();
    TestBed.tick();
    await redraw();
    return requests[0]!;
  }

  /** The cards search sent (after typing stops), answered with these. */
  async function found(cards: readonly FoundCard[]): Promise<TestRequest> {
    vi.advanceTimersByTime(SEARCH_MS);
    const requests = asked('cards', 'GET');
    expect(requests).toHaveLength(1);
    requests[0]!.flush(JSON.stringify({ cards }));
    await settle();
    await redraw();
    return requests[0]!;
  }

  const gathered = () => all('.gd-author-board-gathered__text').map((line) => line.textContent?.trim());
  const card = (alias: string) => `.gd-author-board-card[data-alias="${alias}"]`;
  const patch = (request: TestRequest) =>
    JSON.parse(sentForm(request.request).get('patch')!) as { top: Record<string, unknown> };
  const stored = () => (JSON.parse(sessionStorage.getItem(AUTHOR_UI_KEY)!) as { board: GatheredBoard | null }).board;

  it('says when the board has not been laid out', async () => {
    await open(draftState());
    expect(text('[data-empty]')).toBe('The board has not been laid out.');
    expect(el.querySelector('gd-author-board-gathered')).toBeNull();
  });

  it("shows the board: where changes go, the totals, the path, each seat's cards by place, and the search", async () => {
    await open(withBoard({ pending: ['wound(frodo, 1)'] }));
    expect(text('[data-action=submit]')).toBe('Submit changes (0)');
    expect(find<HTMLButtonElement>('[data-action=submit]').disabled).toBe(true);
    expect(find<HTMLButtonElement>('[data-action=reset]').disabled).toBe(true);
    expect(text('gd-author-board-gathered .gd-author-note')).toBe('Changes wait here until submitted.');
    expect(find<HTMLInputElement>('[data-where=now] input').checked).toBe(true);
    expect(text('[data-where=now] .gd-author-note')).toBe('Step 1 does it on every play.');
    expect(find('[data-where=now]').title).toMatch(/^Done once submitted, and by step 1 whenever/);
    expect(text('.gd-author-board__pending')).toBe('Waiting for a step to carry them: wound(frodo, 1)');
    expect(find<HTMLInputElement>('[data-counter=burdens] input').value).toBe('1');
    expect(all('.gd-author-board-path__site').map((row) => row.getAttribute('data-site'))).toEqual(['1', '2', '3']);
    const parts = (row: string) =>
      all(`${row} .gd-author-board-path__name > span`).map((part) => part.textContent?.trim());
    expect(parts('.gd-author-board-path__site[data-site="2"]')).toEqual([
      'Site 2',
      'Site 1_329',
      '1_329',
      'site2',
      'site2opp',
    ]);
    expect(find('.gd-author-board-path__site[data-site="2"] .cardHint').getAttribute('value')).toBe('1_329');
    expect(find('gd-author-board-seat[data-seat=freeps] .gd-author-board__heading').textContent).toBe(
      'Free Peoples seat',
    );
    expect(
      all('gd-author-board-seat[data-seat=freeps] .gd-author-board-seat__place').map((place) =>
        place.textContent?.trim(),
      ),
    ).toEqual(['Characters (1)', 'On another card (1)', 'Hand (2)']);
    const alias = find(`${card('sam')} .gd-author-board-card__alias`);
    expect(alias.classList.contains('cardHint')).toBe(true);
    expect(alias.getAttribute('value')).toBe('1_311');
    // (a card on the table can be wounded; one in a hand put in another order)
    expect(el.querySelector(`${card('frodo')} [data-action=wound]`)).not.toBeNull();
    expect(el.querySelector(`${card('sam')} [data-action=wound]`)).toBeNull();
    expect(find<HTMLButtonElement>(`${card('sam')} [data-action=up]`).disabled).toBe(true);
    expect(find<HTMLButtonElement>(`${card('merry')} [data-action=down]`).disabled).toBe(true);
    expect(el.querySelector(`${card('orc')} [data-action=up]`)).toBeNull();
    expect(find<HTMLSelectElement>('gd-author-board-seat[data-seat=freeps] [data-ring=bearer] select').value).toBe(
      'frodo',
    );
    expect(find<HTMLSelectElement>('gd-author-board-seat[data-seat=shadow] [data-ring=ring] select').value).toBe('');
    expect(find('gd-author-board-add .gd-author-input').getAttribute('placeholder')).toBe('name of a card');
  });

  it("says what a change to the board as it stands does when no step has the game's decision", async () => {
    await open(withBoard({ position: position({ inHand: false }) }));
    expect(text('[data-where=now] .gd-author-note')).toBe('The next step you write does it.');
  });

  it('gathers changes from the cards and lists them, each with a × that undoes it', async () => {
    await open();
    await click(`${card('frodo')} [data-action=wound]`);
    await click(`${card('frodo')} [data-action=wound]`);
    expect(text(`${card('frodo')} .gd-author-board-card__count`)).toBe('2w');
    await click(`${card('frodo')} [data-action=heal]`);
    await click(`${card('merry')} [data-action=top]`);
    const move = await choose(`${card('orc')} select[data-control=move]`, 'shadow.discard');
    expect(move.value).toBe('');
    await choose(`${card('sam')} select[data-control=put]`, 'stack|frodo');
    await choose(`${card('frodo')} select[data-control=move]`, '');
    expect(gathered()).toEqual([
      'wound(frodo, 1)',
      'order(freeps.hand, merry, sam)',
      'move(orc, shadow.discard)',
      'stack(sam, frodo)',
    ]);
    expect(text('[data-action=submit]')).toBe('Submit changes (4)');
    expect(find(card('frodo')).hasAttribute('data-changed')).toBe(true);
    expect(find(card('oppFrodo')).hasAttribute('data-changed')).toBe(false);
    expect(stored()!.ops).toHaveLength(4);
    all('[data-kind=op] [data-action=undo]')[0]!.click();
    await redraw();
    expect(gathered()).toHaveLength(3);
    await click('[data-action=reset]');
    expect(gathered()).toEqual([]);
  });

  it('puts a card in another order with ↑ and ↓, and gives a card on the table tokens', async () => {
    const board = draftBoard();
    await open(
      withBoard({
        board: {
          ...board,
          cards: [...board.cards, boardCard('cave', { zone: 'support', type: 'CONDITION', tokens: 1 })],
        },
      }),
    );
    await click(`${card('sam')} [data-action=down]`);
    expect(gathered()).toEqual(['order(freeps.hand, merry, sam)']);
    await click(`${card('sam')} [data-action=up]`);
    expect(gathered()).toEqual(['order(freeps.hand, sam, merry)']);
    expect(text(`${card('cave')} [data-action=token]`)).toBe('+token (1)');
    await click(`${card('cave')} [data-action=token]`);
    await click(`${card('cave')} [data-action=token]`);
    expect(gathered()).toEqual(['order(freeps.hand, sam, merry)', 'tokens(cave, 2)']);
    expect(text(`${card('cave')} [data-action=token]`)).toBe('+token (3)');
  });

  it("says a pile's order from its top, offers tokens on support, and nothing to put a card on with no other on the table", async () => {
    const pile = (alias: string, at: number) => boardCard(alias, { zone: 'discard', at, cardId: 40 + at });
    await open(withBoard({ board: draftBoard({ cards: [boardCard('frodo'), pile('a', 0), pile('b', 1)] }) }));
    expect(find(`${card('b')} [data-action=up]`).title).toBe('One nearer the top');
    expect(find(`${card('a')} [data-action=down]`).title).toBe('One nearer the bottom');
    expect(find(`${card('b')} [data-action=top]`).title).toBe('To the top');
    expect(el.querySelector(`${card('frodo')} select[data-control=put]`)).toBeNull();
    await choose(`${card('a')} select[data-control=put]`, '');
    expect(gathered()).toEqual([]);
    authoring.setState(
      withBoard({ board: draftBoard({ cards: [boardCard('cave', { zone: 'support', type: 'CONDITION' })] }) }),
    );
    await redraw();
    expect(text(`${card('cave')} [data-action=token]`)).toBe('+token');
  });

  it('takes a card out (Keep it keeps it), and does not add one after all', async () => {
    await open();
    await click(`${card('sam')} [data-action=drop]`);
    expect(find(card('sam')).hasAttribute('data-removing')).toBe(true);
    expect(text(`${card('sam')} .gd-author-board-card__tag`)).toBe('to be taken out');
    expect(gathered()).toEqual(['take out sam']);
    await click(`${card('sam')} [data-action=keep]`);
    expect(gathered()).toEqual([]);
    await click(`${card('sam')} [data-action=drop]`);
    await click('[data-kind=removed] [data-action=undo]');
    expect(gathered()).toEqual([]);
  });

  it('sets a total; what is typed stays while the state comes again; twilight is set in the setup too', async () => {
    await open();
    await type('[data-counter=burdens] input', '4');
    authoring.setState(withBoard({ revision: 2 }));
    await redraw();
    expect(find<HTMLInputElement>('[data-counter=burdens] input').value).toBe('4');
    await click('[data-counter=burdens] [data-action=set]');
    expect(gathered()).toEqual(['burdens(4)']);
    expect(find('[data-counter=burdens]').hasAttribute('data-changed')).toBe(true);
    await type('[data-counter=threats] input', 'x');
    await click('[data-counter=threats] [data-action=set]');
    expect(gathered()).toEqual(['burdens(4)']);
    await click('[data-counter=twilight] [data-action=set]');
    expect(find<HTMLInputElement>('[data-counter=burdens] input').value).toBe('4');
    await click('[data-action=reset]');
    await click('[data-where=setup] input');
    expect(find<HTMLButtonElement>('[data-counter=twilight] [data-action=set]').disabled).toBe(false);
    // (for the setup, what the board in hand has is no reason not to gather it: the setup may say another)
    await click('[data-counter=twilight] [data-action=set]');
    expect(gathered()).toEqual(['twilight(0)']);
  });

  it('refuses to change where changes go while changes are gathered, the radio staying', async () => {
    await open();
    await click(`${card('frodo')} [data-action=wound]`);
    await click('[data-where=setup] input');
    expect(authoring.problem()).toBe(WHERE_REFUSED);
    expect(find<HTMLInputElement>('[data-where=now] input').checked).toBe(true);
    expect(find<HTMLInputElement>('[data-where=setup] input').checked).toBe(false);
    expect(authoring.ui().where).toBe('now');
  });

  it('submits what is gathered', async () => {
    await open();
    await click(`${card('frodo')} [data-action=wound]`);
    await click('[data-action=submit]');
    const sent = await answer('do', result(withBoard(), { events: true }));
    expect(sentForm(sent.request).get('op')).toBe('wound(frodo, 1)');
    expect(gathered()).toEqual([]);
  });

  it('renames a card where it stands: Enter renames, a bad name says why, Escape leaves it', async () => {
    await open();
    await click(`${card('sam')} [data-action=rename]`);
    const field = find<HTMLInputElement>(`${card('sam')} gd-author-board-rename input`);
    expect(field.value).toBe('sam');
    expect(field.maxLength).toBe(40);
    await type(`${card('sam')} gd-author-board-rename input`, 'merry');
    await click(`${card('sam')} [data-action=rename]`);
    expect(field.hasAttribute('data-bad')).toBe(true);
    expect(field.title).toBe('The scenario already has a card called that');
    await type(`${card('sam')} gd-author-board-rename input`, 'sam wise');
    await key(`${card('sam')} gd-author-board-rename input`, 'Enter');
    expect(field.title).toBe('Letters, digits, dashes and underscores, starting with a letter');
    expect(asked('alias')).toEqual([]);
    await key(`${card('sam')} gd-author-board-rename input`, 'Escape');
    expect(el.querySelector('gd-author-board-rename')).toBeNull();
    // (the same name: nothing to do)
    await click(`${card('sam')} [data-action=rename]`);
    await click(`${card('sam')} [data-action=rename]`);
    expect(el.querySelector('gd-author-board-rename')).toBeNull();
    await click(`${card('sam')} [data-action=rename]`);
    await type(`${card('sam')} gd-author-board-rename input`, ' samwise ');
    await click(`${card('sam')} [data-action=rename]`);
    // (on its way: nothing more is done)
    await key(`${card('sam')} gd-author-board-rename input`, 'Escape');
    await key(`${card('sam')} gd-author-board-rename input`, 'Enter');
    const sent = await answer('alias', result(withBoard({ gameId: '9' }), { gameId: '9', rebuilt: true }));
    expect([sentForm(sent.request).get('from'), sentForm(sent.request).get('to')]).toEqual(['sam', 'samwise']);
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
    await click(`${card('merry')} [data-action=rename]`);
    await click(`${card('merry')} gd-author-board-rename [data-action=cancel]`);
    expect(el.querySelector('gd-author-board-rename')).toBeNull();
  });

  it('changes a card for another found by name, its versions searched at once; "this one" changes nothing', async () => {
    await open();
    await click(`${card('sam')} [data-action=change]`);
    expect(find<HTMLInputElement>(`${card('sam')} gd-author-card-search input`).value).toBe('Sam');
    const search = asked('cards', 'GET');
    expect(search).toHaveLength(1);
    expect(search[0]!.request.params.get('q')).toBe('Sam');
    search[0]!.flush(JSON.stringify({ cards: [{ ...GANDALF, id: '1_311', name: 'Sam, Son of Hamfast' }, GANDALF] }));
    await settle();
    await redraw();
    const results = all(`${card('sam')} .gd-author-search__result`);
    expect(results.map((result) => result.textContent?.trim())).toEqual([
      'Sam, Son of Hamfast  ·  1_311  ·  gandalf companion (4)  ·  this one',
      'Gandalf, Friend of the Shirefolk  ·  1_72  ·  gandalf companion (4)',
    ]);
    expect(results[0]!.hasAttribute('data-current')).toBe(true);
    results[0]!.click();
    await settle();
    await redraw();
    expect(asked('edit')).toEqual([]);
    expect(el.querySelector(`${card('sam')} gd-author-card-search`)).toBeNull();
    await click(`${card('sam')} [data-action=change]`);
    asked('cards', 'GET')[0]!.flush(JSON.stringify({ cards: [GANDALF] }));
    await settle();
    await redraw();
    await click(`${card('sam')} .gd-author-search__result`);
    expect((patch(await answer('edit', result(withBoard()))).top['cards'] as typeof DRAFT.cards).freeps.sam).toBe(
      '1_72',
    );
    await click(`${card('sam')} [data-action=change]`);
    await click(`${card('sam')} gd-author-card-search [data-action=cancel]`);
    expect(el.querySelector(`${card('sam')} gd-author-card-search`)).toBeNull();
  });

  it('changes the path at once: a site found by name (that place first), the last taken off, one added', async () => {
    await open();
    await click('.gd-author-board-path__site[data-site="2"] [data-action=change]');
    await type('.gd-author-board-path__site[data-site="2"] gd-author-card-search input', 'bree');
    const search = await found([
      { ...GANDALF, id: '1_340', name: 'Bree Gate', site: 3 },
      { ...GANDALF, id: '1_327', name: 'Bree Streets', site: 2 },
    ]);
    expect(search.request.params.get('sites')).toBe('true');
    const results = all('.gd-author-search__result');
    expect(results.map((result) => result.textContent?.trim())).toEqual([
      'Bree Streets  ·  1_327  ·  site 2',
      'Bree Gate  ·  1_340  ·  site 3',
    ]);
    results[0]!.click();
    await settle();
    expect(patch(await answer('edit', result(withBoard()))).top).toEqual({
      sites: { freeps: '1_323, 1_327, 1_341', shadow: '1_323, 1_327, 1_341' },
    });
    expect(el.querySelectorAll('[data-action=drop-site]')).toHaveLength(1);
    await click('.gd-author-board-path__site[data-site="3"] [data-action=drop-site]');
    expect(patch(await answer('edit', result(withBoard()))).top).toEqual({
      sites: { freeps: '1_323, 1_329', shadow: '1_323, 1_329' },
    });
    expect(text('[data-action=add-site]')).toBe('+ Site 4');
    await click('[data-action=add-site]');
    await type('gd-author-board-path > gd-author-card-search input', 'x');
    await found([]);
    expect(text('gd-author-board-path [data-none]')).toBe('No site by that name.');
    await type('gd-author-board-path > gd-author-card-search input', 'y');
    await found([{ ...GANDALF, id: '1_348', name: 'Pass', site: 4 }]);
    await click('gd-author-board-path .gd-author-search__result');
    expect(patch(await answer('edit', result(withBoard()))).top['sites']).toMatchObject({
      freeps: '1_323, 1_329, 1_341, 1_348',
    });
    await click('[data-action=add-site]');
    await click('gd-author-board-path [data-action=cancel]');
    expect(el.querySelector('gd-author-board-path gd-author-card-search')).toBeNull();
  });

  it("edits the seats' paths apart once unticked, and renames a site", async () => {
    await open();
    find<HTMLInputElement>('[data-option=same-sites] input').click();
    await settle();
    await redraw();
    expect(all('.gd-author-board-path__seat').map((seat) => seat.textContent?.trim())).toEqual([
      'Free Peoples',
      'Shadow',
    ]);
    expect(
      all('.gd-author-board-path__site[data-seat=shadow][data-site="1"] .gd-author-board-path__alias').map((name) =>
        name.textContent?.trim(),
      ),
    ).toEqual(['site1opp']);
    await click('.gd-author-board-path__site[data-seat=shadow][data-site="1"] [data-action=rename]');
    await type('gd-author-board-path gd-author-board-rename input', 'bagEnd');
    await key('gd-author-board-path gd-author-board-rename input', 'Enter');
    const sent = await answer('alias', result(withBoard()));
    expect([sentForm(sent.request).get('from'), sentForm(sent.request).get('to')]).toEqual(['site1opp', 'bagEnd']);
    expect(el.querySelector('gd-author-board-path gd-author-board-rename')).toBeNull();
  });

  it("sets a seat's Ring-bearer and Ring at once", async () => {
    await open();
    expect(
      [...find<HTMLSelectElement>('gd-author-board-seat[data-seat=freeps] [data-ring=bearer] select').options].map(
        (option) => option.text,
      ),
    ).toEqual(['frodo (Frodo, Resolute Hobbit)', 'sam (Sam, Son of Hamfast)', 'merry (Merry, Friend to Sam)']);
    await choose('gd-author-board-seat[data-seat=freeps] [data-ring=ring] select', '');
    expect(patch(await answer('edit', result(withBoard()))).top).toEqual({ ring: null });
    await choose('gd-author-board-seat[data-seat=freeps] [data-ring=bearer] select', 'sam');
    expect(patch(await answer('edit', result(withBoard()))).top).toEqual({
      ringBearer: { freeps: 'sam', shadow: 'oppFrodo' },
    });
  });

  it('searches once typing stops, shows only the last answer, and says nothing more of a search that failed', async () => {
    await open();
    await type('gd-author-board-add gd-author-card-search input', 'gan');
    vi.advanceTimersByTime(SEARCH_MS - 1);
    expect(asked('cards', 'GET')).toEqual([]);
    vi.advanceTimersByTime(1);
    const [first] = asked('cards', 'GET');
    await type('gd-author-board-add gd-author-card-search input', 'gandalf');
    vi.advanceTimersByTime(SEARCH_MS);
    const [second] = asked('cards', 'GET');
    second!.flush(JSON.stringify({ cards: [GANDALF] }));
    await settle();
    first!.flush(JSON.stringify({ cards: [TROLL, GANDALF] }));
    await settle();
    await redraw();
    expect(all('gd-author-board-add .gd-author-search__result')).toHaveLength(1);
    await type('gd-author-board-add gd-author-card-search input', 'x');
    vi.advanceTimersByTime(SEARCH_MS);
    asked('cards', 'GET')[0]!.flush(JSON.stringify({ error: 'Down.' }), { status: 500, statusText: 'No' });
    await settle();
    await redraw();
    expect(authoring.problem()).toBe('Down.');
    expect(all('gd-author-board-add .gd-author-search__result')).toHaveLength(1);
    // (Escape leaves a search that can't be cancelled as it is, and one that can)
    await key('gd-author-board-add gd-author-card-search input', 'Escape');
    expect(el.querySelector('gd-author-board-add gd-author-card-search')).not.toBeNull();
    await click('[data-action=add-site]');
    await key('gd-author-board-path gd-author-card-search input', 'Escape');
    expect(el.querySelector('gd-author-board-path gd-author-card-search')).toBeNull();
  });

  it('adds a card found by name, under a name suggested, in the seat and place its side says', async () => {
    await open();
    await type('gd-author-board-add gd-author-card-search input', 'gandalf');
    await found([GANDALF, TROLL]);
    expect(all('gd-author-board-add .gd-author-search__result')).toHaveLength(2);
    await click('gd-author-board-add .gd-author-search__result');
    expect(el.querySelector('gd-author-board-add .gd-author-search__result')).toBeNull();
    expect(text('.gd-author-board-add__card')).toBe('Gandalf, Friend of the Shirefolk (1_72)');
    expect(find<HTMLInputElement>('[data-field=alias]').value).toBe('gandalf');
    expect(find<HTMLSelectElement>('[data-field=seat] select').value).toBe('freeps');
    expect(find<HTMLSelectElement>('[data-field=zone] select').value).toBe('hand');
    expect(el.querySelector('[data-field=host]')).toBeNull();
    await type('[data-field=alias]', 'wizard');
    await choose('[data-field=zone] select', 'attach');
    expect(find<HTMLInputElement>('[data-field=alias]').value).toBe('wizard');
    expect(find<HTMLSelectElement>('[data-field=host] select').value).toBe('frodo');
    await choose('[data-field=host] select', 'oppFrodo');
    await click('[data-action=add]');
    expect(gathered()).toEqual(['add Gandalf, Friend of the Shirefolk as wizard (freeps, on oppFrodo)']);
    expect(text(`${card('wizard')} .gd-author-board-card__tag`)).toBe('new');
    // (drawn afresh: the search empty)
    expect(el.querySelector('.gd-author-board-add__chosen')).toBeNull();
    expect(find<HTMLInputElement>('gd-author-board-add gd-author-card-search input').value).toBe('');
    await click(`${card('wizard')} [data-action=drop]`);
    expect(gathered()).toEqual([]);
  });

  it('offers a Shadow card for the bottom of the Shadow deck; a bad name is said; a name taken asks first', async () => {
    await open();
    await type('gd-author-board-add gd-author-card-search input', 'troll');
    await found([TROLL]);
    await click('gd-author-board-add .gd-author-search__result');
    expect(find<HTMLSelectElement>('[data-field=seat] select').value).toBe('shadow');
    expect(find<HTMLSelectElement>('[data-field=zone] select').value).toBe('');
    expect(find<HTMLInputElement>('[data-field=alias]').value).toBe('cave');
    await choose('[data-field=seat] select', 'freeps');
    await type('[data-field=alias]', '1troll');
    await click('[data-action=add]');
    expect(authoring.problem()).toMatch(/letters, digits, dashes and underscores/);
    expect(el.querySelector('.gd-author-board-add__chosen')).not.toBeNull();
    await type('[data-field=alias]', 'orc');
    notifier.answers.push(true);
    await click('[data-action=add]');
    expect(notifier.words[0]).toMatchObject({ title: 'Replace orc?', ok: 'Replace orc' });
    expect(gathered()).toEqual(['add Cave Troll of Moria as orc (freeps, deck)']);
  });

  it('shows a card large under the mouse, and its information on a click', async () => {
    await open();
    const hover = TestBed.inject(CardHover);
    const alias = find(`${card('sam')} .gd-author-board-card__alias`);
    alias.dispatchEvent(new PointerEvent('pointerenter', { pointerType: 'touch' }));
    expect(hover.hovered()).toBeNull();
    alias.dispatchEvent(new PointerEvent('pointerenter', { pointerType: 'mouse' }));
    expect(hover.hovered()?.card.blueprintId).toBe('1_311');
    alias.dispatchEvent(new PointerEvent('pointerdown'));
    expect(hover.presses()).toBe(1);
    alias.dispatchEvent(new PointerEvent('pointerleave'));
    expect(hover.hovered()).toBeNull();
    alias.click();
    expect(TestBed.inject(CardInfoService).card()?.blueprintId).toBe('1_311');
  });

  it('lists a card found by name with what it is', () => {
    expect(foundText({ ...GANDALF, culture: null, cost: null }, false, null)).toBe(
      'Gandalf, Friend of the Shirefolk  ·  1_72  ·  companion',
    );
    expect(foundText({ ...GANDALF, site: undefined }, true, '1_72')).toBe(
      'Gandalf, Friend of the Shirefolk  ·  1_72  ·  site ?  ·  this one',
    );
  });

  describe('how the game opens, and what a setup can have (B10, A3)', () => {
    const gameOpening = (extra: Partial<DraftState> = {}) =>
      withBoard({
        start: { ...START_DEFAULT, opening: 'game' },
        draft: { ...DRAFT, setup: { opening: 'game', freeps: { deck: 'sam' } } },
        ...extra,
      });
    const moves = (alias: string) =>
      [
        ...el.querySelectorAll<HTMLOptionElement>(
          `.gd-author-board-card[data-alias=${alias}] select[data-control=move] option`,
        ),
      ]
        .slice(1)
        .map((one) => one.value);

    it('opens with the game after the page asks what goes, the setup keeping only its decks', async () => {
      await open();
      expect(find<HTMLSelectElement>('gd-author-board-opening select').value).toBe('board');
      expect(el.querySelector('[data-note=opening]')).toBeNull();
      notifier.answers.push(true);
      await choose('gd-author-board-opening select', 'game');
      expect(notifier.dialogs[0]).toBe(
        "confirm: Its setup keeps only each deck's order. Lost: Free Peoples hand: sam, merry; Shadow hand: orc.",
      );
      const request = await answer('edit', result(withBoard()));
      expect(parseJson(sentForm(request.request).get('patch')!)).toEqual({ top: { setup: { opening: 'game' } } });
    });

    it('puts the select back when the author keeps the board', async () => {
      await open();
      notifier.answers.push(false);
      await choose('gd-author-board-opening select', 'game');
      expect(asked('edit')).toEqual([]);
      expect(find<HTMLSelectElement>('gd-author-board-opening select').value).toBe('board');
    });

    it('says that there are no cards before they are dealt, and offers only how the game opens', async () => {
      await open(gameOpening({ board: null }));
      expect(el.querySelector('[data-empty]')).toBeNull();
      expect(find<HTMLSelectElement>('gd-author-board-opening select').value).toBe('game');
      expect(text('[data-note=undealt]')).toBe('No cards yet: they are dealt once the seats are chosen.');
      expect(el.querySelector('gd-author-board-start')).toBeNull();
    });

    it('to the setup of the game’s own opening, offers only a deck’s order', async () => {
      const deck = (alias: string, at: number) => boardCard(alias, { zone: 'deck', at, cardId: 40 + at });
      await open(gameOpening({ board: draftBoard({ cards: [boardCard('frodo'), deck('a', 0), deck('b', 1)] }) }));
      expect(text('[data-note=opening]')).toBe('Bid, seats, fellowships and draw are played.');
      expect(el.querySelector('.gd-author-board-start__row[data-part=site]')).toBeNull();
      expect(el.querySelector('gd-author-board-counters')).not.toBeNull();
      await click('[data-where=setup] input');
      expect(el.querySelector('gd-author-board-counters')).toBeNull();
      expect(moves('a')).toEqual(['freeps.deck.top', 'freeps.deck.bottom']);
      expect(el.querySelector('.gd-author-board-card[data-alias=a] [data-action=down]')).not.toBeNull();
      expect(el.querySelector('.gd-author-board-card[data-alias=frodo] [data-action=wound]')).toBeNull();
      expect(
        all('gd-author-board-laid button[data-action=marker]').every(
          (button) => (button as HTMLButtonElement).disabled,
        ),
      ).toBe(true);
    });

    it('offers no minions on the table for the Free Peoples seat’s setup, nor the Shadow marker while markers keep together', async () => {
      await open(withBoard({ start: { ...START_DEFAULT, markersTogether: true } }));
      expect(moves('sam')).toContain('freeps.minions');
      await click('[data-where=setup] input');
      expect(moves('sam')).not.toContain('freeps.minions');
      expect(moves('orc')).toContain('shadow.minions');
      const markers = all('gd-author-board-laid button[data-action=marker]') as HTMLButtonElement[];
      expect(markers.filter((button) => button.dataset['seat'] === 'shadow').every((button) => button.disabled)).toBe(
        true,
      );
      expect(markers.filter((button) => button.dataset['seat'] === 'freeps').some((button) => button.disabled)).toBe(
        false,
      );
    });

    it('adds a card only where its seat’s setup can have it', async () => {
      await open();
      await type('gd-author-board-add gd-author-card-search input', 'troll');
      vi.advanceTimersByTime(SEARCH_MS);
      asked('cards', 'GET')[0]!.flush(JSON.stringify({ cards: [TROLL] }));
      await settle();
      await redraw();
      await click('gd-author-board-add .gd-author-search__result');
      const zones = () =>
        [...el.querySelectorAll<HTMLOptionElement>('gd-author-board-add [data-field=zone] option')].map(
          (one) => one.value,
        );
      expect(zones()).toContain('minions');
      await choose('gd-author-board-add [data-field=zone] select', 'minions');
      await choose('gd-author-board-add [data-field=seat] select', 'freeps');
      expect(zones()).not.toContain('minions');
      expect(find<HTMLSelectElement>('gd-author-board-add [data-field=zone] select').value).toBe('');
    });
  });
});
