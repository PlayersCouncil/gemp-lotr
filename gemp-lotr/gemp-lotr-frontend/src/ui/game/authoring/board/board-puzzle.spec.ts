import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { BOUND_TYPING_MS } from '../../../../core/game/authoring/bound-check';
import { CardHover } from '../../../../core/cards/card-hover.service';
import { CardInfoService } from '../../../../core/cards/card-info.service';
import { CardViews } from '../../../../core/cards/card-views.service';
import { Draft, DraftSlot, DraftState, FoundCard } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { provideGameState } from '../../../../core/game/game-state';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { HallTab } from '../../../../core/platform/hall-tab.service';
import { PageCues } from '../../../../core/platform/page-cues';
import { PageExit } from '../../../../core/platform/page-exit';
import { PAGE_QUERY_STRING } from '../../../../core/platform/page-location';
import {
  boardCard,
  draftBoard,
  draftGameXml,
  draftState,
  MODIFY_WORDS,
  puzzleSlot,
  puzzleState,
  result,
} from '../../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../../testing/fake-notifier';
import { BoardTabComponent } from '../board-tab.component';
import { SEARCH_MS } from '../widgets/card-search.component';
import { parseJson } from '../../../../core/api/json';
import { sentForm } from '../../../../testing/sent-form';
import { AUTHOR, authorCall, authorCalls, authorForm, sentPatch } from '../../../../testing/authoring-requests';

const GAME = '/gemp-lotr-server/game/7';
const BOUND = '/gemp-lotr-server/scenarios/author/bound';

const DRAFT: Draft = {
  id: 'test',
  kind: 'puzzle',
  human: 'freeps',
  steps: [],
  cards: {
    freeps: { frodo: '10_121', ring: '1_2', sam: '1_311', merry: '1_303' },
    shadow: { oppFrodo: '10_121', orc: '1_191' },
  },
  ringBearer: { freeps: 'frodo', shadow: 'oppFrodo' },
  setup: { freeps: { hand: 'sam, merry' }, shadow: { hand: 'orc' }, modifiers: { frodo: 'damage +1 turn' } },
  pool: 'set:1-6',
  slots: {
    condition: { zone: 'support', filter: 'culture:ROHAN cardtype:CONDITION', label: 'a Rohan condition' },
    hand1: 'hand',
  },
  solutions: [{ name: 'published', picks: { condition: '5_87', hand1: '1_2' }, answers: ['use(condition)'] }],
};

const SLOTS = [
  puzzleSlot('condition'),
  puzzleSlot('hand1', {
    label: null,
    zone: 'hand',
    filter: null,
    bound: 'set:1-6',
    pick: '1_2',
    pickName: 'The One Ring, The Ruling Ring',
  }),
];

/** Frodo with Strength +2 put on by hand, and the setup's Damage +1 for the turn. */
const withPuzzle = (extra: Partial<DraftState> = {}): DraftState => {
  const board = draftBoard();
  return draftState({
    draft: DRAFT,
    board: {
      ...board,
      cards: board.cards.map((card) =>
        card.alias === 'frodo'
          ? {
              ...card,
              modifiers: [
                { id: 1, what: 'damage', amount: 1, until: 'turn', text: 'Damage +1', written: 'damage +1 turn' },
                { id: 2, what: 'strength', amount: 2, until: 'removed', text: 'Strength +2', written: 'strength +2' },
              ],
            }
          : card,
      ),
    },
    modify: MODIFY_WORDS,
    puzzle: puzzleState({ pool: 'set:1-6', slots: SLOTS, picks: { condition: '5_87', hand1: '1_2' } }),
    ...extra,
  });
};

const ALDOR: FoundCard = {
  id: '4_262',
  name: 'Aldor, Soldier of Edoras',
  type: 'ally',
  culture: 'rohan',
  side: 'free_people',
  cost: 1,
};

describe('Board tab: modifiers and slots', () => {
  let fixture: ComponentFixture<BoardTabComponent>;
  let el: HTMLElement;
  let backend: HttpTestingController;
  let authoring: Authoring;
  let notifier: FakeNotifier;

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
    notifier = TestBed.inject(FakeNotifier);
    fixture = TestBed.createComponent(BoardTabComponent);
    el = fixture.nativeElement as HTMLElement;
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  async function open(state: DraftState = withPuzzle()) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    backend.expectOne((r) => r.url === `${AUTHOR}state`).flush(JSON.stringify(state));
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
    find<HTMLElement>(selector).click();
    await settle();
    await redraw();
  }

  async function type(selector: string, value: string, events = ['input', 'change']) {
    const field = find<HTMLInputElement>(selector);
    field.value = value;
    for (const name of events) field.dispatchEvent(new Event(name));
    await settle();
    await redraw();
  }

  async function choose(selector: string, value: string) {
    const select = find<HTMLSelectElement>(selector);
    select.value = value;
    select.dispatchEvent(new Event('change'));
    await settle();
    await redraw();
  }

  const asked = (what: string) => authorCalls(backend, what);
  const gathered = () => all('.gd-author-board-gathered__text').map((line) => line.textContent?.trim());
  const card = (alias: string) => `.gd-author-board-card[data-alias="${alias}"]`;
  const slot = (id: string) => `.gd-author-slot[data-slot="${id}"]`;

  /** The one edit sent: its patch. */
  function patch(): Record<string, unknown> {
    return sentPatch(authorCall(backend, 'edit')) as Record<string, unknown>;
  }

  /** The one board change sent (POST do): what it says, and to which board. */
  function done(): { op: string | null; setup: string | null } {
    const body = authorForm(backend, 'do');
    return { op: body.get('op'), setup: body.get('setup') };
  }

  async function searched(total: number): Promise<string[]> {
    const requests = backend.match((r) => r.url === BOUND);
    for (const request of requests) request.flush(JSON.stringify({ ok: true, why: null, total }));
    await settle();
    await redraw();
    // (a slot's bound is counted with the pool, each searched by itself: `filter | pool`)
    return requests.map((request: TestRequest) =>
      [request.request.params.get('filter') ?? '', request.request.params.get('pool')].filter(Boolean).join(' | '),
    );
  }

  /* ---------------------------------------------------------------- modifiers (em T5) */

  it('shows the modifiers in force on a card, and offers mod… where the card has words to be modified in', async () => {
    await open();
    expect(
      all(`${card('frodo')} .gd-author-mods__chip`).map((chip) => chip.textContent?.replace(/\s+/g, ' ').trim()),
    ).toEqual(['Damage +1 (turn) ×', 'Strength +2 ×']);
    expect(el.querySelector(`${card('frodo')} [data-action=modify]`)).not.toBeNull();
    // (The One Ring has no word in the vocabulary)
    expect(el.querySelector(`${card('ring')} [data-action=modify]`)).toBeNull();
  });

  it('gathers a modifier, and sends it to the board as it stands as the notes write it', async () => {
    await open();
    await click(`${card('frodo')} [data-action=modify]`);
    expect(all(`${card('frodo')} [data-control=what] option`).map((one) => one.getAttribute('value'))).toEqual([
      'strength',
      'resistance',
      'cost',
      'damage',
      'archer',
    ]);
    expect(find<HTMLInputElement>(`${card('frodo')} [data-control=amount]`).value).toBe('+1');
    await type(`${card('frodo')} [data-control=amount]`, '+2', ['input']);
    await choose(`${card('frodo')} [data-control=until] select`, 'turn');
    await click(`${card('frodo')} [data-action=add]`);
    expect(gathered()).toEqual(['modify(frodo, strength, +2, turn)']);
    expect(
      find(`${card('frodo')} .gd-author-mods__chip[data-pending]`)
        .textContent?.replace(/\s+/g, ' ')
        .trim(),
    ).toBe('Strength +2 (turn) ×');
    await click('[data-action=submit]');
    expect(done()).toEqual({ op: 'modify(frodo, strength, +2, turn)', setup: 'false' });
  });

  it('puts on a keyword with no number until removed, and refuses a stat changed by 0', async () => {
    await open();
    await click(`${card('frodo')} [data-action=modify]`);
    await type(`${card('frodo')} [data-control=amount]`, '0', ['input']);
    await click(`${card('frodo')} [data-action=add]`);
    expect(text(`${card('frodo')} .gd-author-mods__problem`)).toBe(
      'Strength needs the amount it is changed by, other than 0: +2, -1',
    );
    expect(gathered()).toEqual([]);
    await choose(`${card('frodo')} [data-control=what] select`, 'archer');
    expect(el.querySelector(`${card('frodo')} [data-control=amount]`)).toBeNull();
    await click(`${card('frodo')} [data-action=add]`);
    expect(gathered()).toEqual(['modify(frodo, archer)']);
    // (a gathered one's × lets go of it)
    await click(`${card('frodo')} .gd-author-mods__chip[data-pending] [data-action=unmodify]`);
    expect(gathered()).toEqual([]);
    await click(`${card('frodo')} [data-action=modify]`);
    await click(`${card('frodo')} [data-action=cancel]`);
    expect(el.querySelector(`${card('frodo')} .gd-author-mods__form`)).toBeNull();
  });

  it('takes the modifiers of a kind off a card (unmodify), marking them till it is sent', async () => {
    await open();
    await click(`${card('frodo')} .gd-author-mods__chip[data-what=strength] [data-action=unmodify]`);
    expect(gathered()).toEqual(['unmodify(frodo, strength)']);
    expect(find(`${card('frodo')} .gd-author-mods__chip[data-what=strength]`).hasAttribute('data-removing')).toBe(true);
    expect(el.querySelector(`${card('frodo')} [data-what=strength] [data-action=unmodify]`)).toBeNull();
    await click('[data-action=submit]');
    expect(done()).toEqual({ op: 'unmodify(frodo, strength)', setup: 'false' });
  });

  it('shows the setup’s modifiers at the board the scenario starts with, and sends changes there', async () => {
    const state = withPuzzle({
      start: {
        site: 1,
        opponentSite: 1,
        opponentSiteSet: false,
        phase: 'fellowship',
        turn: 1,
        twilight: 0,
        moves: 0,
        movesSet: false,
        late: false,
        skirmishes: {},
        controlled: {},
        path: [],
        markersTogether: false,
        phases: ['fellowship'],
        lastSite: 3,
        modifiers: { frodo: [{ what: 'damage', amount: 1, until: 'turn', written: 'damage +1 turn' }] },
      },
    });
    await open(state);
    await click('[data-where=setup] input');
    expect(all(`${card('frodo')} .gd-author-mods__chip`).map((chip) => chip.getAttribute('data-what'))).toEqual([
      'damage',
    ]);
    await click(`${card('frodo')} [data-action=modify]`);
    await choose(`${card('frodo')} [data-control=what] select`, 'resistance');
    await type(`${card('frodo')} [data-control=amount]`, '-1', ['input']);
    await click(`${card('frodo')} [data-action=add]`);
    await click(`${card('frodo')} .gd-author-mods__chip[data-what=damage] [data-action=unmodify]`);
    await click('[data-action=submit]');
    expect(done()).toEqual({ op: 'modify(frodo, resistance, -1), unmodify(frodo, damage)', setup: 'true' });
  });

  /* ---------------------------------------------------------------- slots (ep P2) */

  it('lists the slots, each part of each as the file has it, with the card the game has in it', async () => {
    await open();
    expect(all('.gd-author-slot').map((row) => row.getAttribute('data-slot'))).toEqual(['condition', 'hand1']);
    expect(text(`${slot('condition')} .gd-author-slot__pick`)).toBe('Parapet');
    expect(find<HTMLInputElement>(`${slot('condition')} [data-part=slot-label]`).value).toBe('a Rohan condition');
    expect(find<HTMLSelectElement>(`${slot('condition')} [data-part=slot-zone] select`).value).toBe('support');
    expect(find<HTMLSelectElement>(`${slot('condition')} [data-part=slot-seat] select`).value).toBe('');
    expect(find<HTMLInputElement>(`${slot('condition')} input[data-part=slot-filter]`).value).toBe(
      'culture:ROHAN cardtype:CONDITION',
    );
    expect(el.querySelector(`${slot('condition')} [data-part=slot-on]`)).toBeNull();
    expect(el.querySelector(`${slot('condition')} [data-part=slot-position]`)).toBeNull();
    expect(find<HTMLInputElement>(`${slot('hand1')} [data-part=slot-label]`).value).toBe('');
    // (each bound counted with the pool by the server, each searched by itself; an empty one says nothing)
    expect(await searched(11)).toEqual(['culture:ROHAN cardtype:CONDITION | set:1-6']);
    expect(text(`${slot('condition')} .gd-author-filter__verdict`)).toBe('11 cards');
  });

  it('changes a slot’s parts at once, sending the whole slots part (the notes’ patch)', async () => {
    await open();
    const condition = DRAFT.slots!['condition'] as DraftSlot;
    await type(`${slot('hand1')} [data-part=slot-label]`, 'a card in hand');
    expect(patch()).toEqual({ top: { slots: { condition, hand1: { zone: 'hand', label: 'a card in hand' } } } });
    await choose(`${slot('hand1')} [data-part=slot-seat] select`, 'shadow');
    expect(patch()).toEqual({ top: { slots: { condition, hand1: { zone: 'hand', seat: 'shadow' } } } });
    await choose(`${slot('hand1')} [data-part=slot-zone] select`, 'deck');
    expect(patch()).toEqual({ top: { slots: { condition, hand1: 'deck' } } });
    await type(`${slot('condition')} [data-part=slot-tokens]`, '3');
    expect(patch()).toEqual({ top: { slots: { condition: { ...condition, tokens: 3 }, hand1: 'hand' } } });
    await type(`${slot('condition')} [data-part=slot-wounds]`, '1');
    expect(patch()).toEqual({ top: { slots: { condition: { ...condition, wounds: 1 }, hand1: 'hand' } } });
    await type(`${slot('condition')} [data-part=slot-label]`, '');
    expect(patch()).toEqual({
      top: { slots: { condition: { zone: 'support', filter: 'culture:ROHAN cardtype:CONDITION' }, hand1: 'hand' } },
    });
  });

  it('puts a slot on a declared card, and at a place in the draw deck', async () => {
    const draft: Draft = {
      ...DRAFT,
      slots: { trap: { zone: 'attached', on: 'frodo' }, tale: { zone: 'deck', position: 3 } },
    };
    await open(withPuzzle({ draft, puzzle: puzzleState({ slots: [] }) }));
    expect(all(`${slot('trap')} [data-part=slot-on] option`).map((one) => one.getAttribute('value'))).toEqual([
      '',
      'frodo',
      'ring',
      'oppFrodo',
    ]);
    expect(find<HTMLSelectElement>(`${slot('trap')} [data-part=slot-on] select`).value).toBe('frodo');
    await choose(`${slot('trap')} [data-part=slot-on] select`, 'oppFrodo');
    expect(patch()).toEqual({
      top: { slots: { trap: { zone: 'attached', on: 'oppFrodo' }, tale: { zone: 'deck', position: 3 } } },
    });
    expect(find<HTMLInputElement>(`${slot('tale')} [data-part=slot-position]`).value).toBe('3');
    await type(`${slot('tale')} [data-part=slot-position]`, '1');
    expect(patch()).toEqual({ top: { slots: { trap: { zone: 'attached', on: 'frodo' }, tale: 'deck' } } });
  });

  it('sends a bound typed, says why the server refused one, and takes a bound handed in from outside', async () => {
    await open();
    await searched(11);
    await type(`${slot('hand1')} input[data-part=slot-filter]`, 'culture:GONDOR');
    const sent = asked('edit');
    expect(parseJson(sentForm(sent[0]!.request).get('patch') as string)).toEqual({
      top: { slots: { condition: DRAFT.slots!['condition'], hand1: { zone: 'hand', filter: 'culture:GONDOR' } } },
    });
    sent[0]!.flush(JSON.stringify({ error: "slots.hand1.filter: 'culture:gondor': in capitals" }), {
      status: 400,
      statusText: 'No',
    });
    await settle();
    await redraw();
    expect(text(`${slot('hand1')} .gd-author-filter__verdict`)).toBe('refused');
    expect(text(`${slot('hand1')} .gd-author-filter__why`)).toMatch(/in capitals/);
    // (the table's search window hands a bound to the panel: the slot's field takes it and sends it)
    authoring.offerSlotBound('condition', 'culture:ROHAN cardtype:CONDITION side:FREE_PEOPLE');
    await settle();
    await redraw();
    expect(authoring.ui().tab).toBe('board');
    expect(authoring.slotBoundOffer()).toBeNull();
    expect(find<HTMLInputElement>(`${slot('condition')} input[data-part=slot-filter]`).value).toBe(
      'culture:ROHAN cardtype:CONDITION side:FREE_PEOPLE',
    );
    const offered = asked('edit');
    expect(parseJson(sentForm(offered[0]!.request).get('patch') as string)).toEqual({
      top: {
        slots: {
          condition: {
            zone: 'support',
            filter: 'culture:ROHAN cardtype:CONDITION side:FREE_PEOPLE',
            label: 'a Rohan condition',
          },
          hand1: 'hand',
        },
      },
    });
    expect(await searched(4)).toEqual(['culture:ROHAN cardtype:CONDITION side:FREE_PEOPLE | set:1-6']);
    offered[0]!.flush(JSON.stringify(result(withPuzzle())));
    await settle();
    await redraw();
    // (an offer for a slot there is not is left for a form that has it)
    authoring.offerSlotBound('nothing', 'x');
    await settle();
    await redraw();
    expect(authoring.slotBoundOffer()?.slot).toBe('nothing');
  });

  it('counts a bound with the pool, the server searching each by itself, also when they say the same key', async () => {
    const overlapping = [puzzleSlot('condition', { overlap: ['culture'] }), SLOTS[1]!];
    await open(withPuzzle({ puzzle: puzzleState({ pool: 'set:1-6', slots: overlapping }) }));
    expect(await searched(20)).toEqual(['culture:ROHAN cardtype:CONDITION | set:1-6']);
    expect(find(`${slot('condition')} .gd-author-filter__verdict`).title).toMatch(/and the pool/);
  });

  it('checks a bound as it is typed, once the typing rests, and says why the server will not take it', async () => {
    await open();
    await searched(11);
    const field = find<HTMLInputElement>(`${slot('hand1')} input[data-part=slot-filter]`);
    field.value = 'colour:red';
    field.dispatchEvent(new Event('input'));
    field.value = 'colour:red ';
    field.dispatchEvent(new Event('input'));
    expect(backend.match((r) => r.url === BOUND)).toHaveLength(0);
    vi.advanceTimersByTime(BOUND_TYPING_MS);
    const requests = backend.match((r) => r.url === BOUND);
    expect(requests.map((one) => [one.request.params.get('filter'), one.request.params.get('pool')])).toEqual([
      ['colour:red', 'set:1-6'],
    ]);
    requests[0]!.flush(
      JSON.stringify({ ok: false, why: "'colour:red': the deck builder's search has no 'colour'", total: null }),
    );
    await settle();
    await redraw();
    expect(text(`${slot('hand1')} .gd-author-filter__verdict`)).toBe('refused');
    expect(text(`${slot('hand1')} .gd-author-filter__why`)).toMatch(/has no 'colour'/);
    expect(asked('edit')).toHaveLength(0);
  });

  it('adds a slot with each solution’s card for it, and takes one out of the slots and the solutions', async () => {
    await open();
    await type('[data-part=new-slot-id]', 'frodo', ['input']);
    expect(find<HTMLButtonElement>('[data-action=add-slot]').disabled).toBe(true);
    expect(text('.gd-author-slots__problem')).toMatch(/frodo is taken/);
    await choose('[data-part=new-slot-zone] select', 'support');
    await type('[data-part=new-slot-id]', 'ally', ['input']);
    await click('[data-action=new-slot-pick]');
    const field = find<HTMLInputElement>('.gd-author-slots__add gd-author-card-search input');
    field.value = 'aldor';
    field.dispatchEvent(new Event('input'));
    vi.advanceTimersByTime(SEARCH_MS);
    backend
      .match((r) => r.url === `${AUTHOR}cards`)
      .forEach((request) => request.flush(JSON.stringify({ cards: [ALDOR] })));
    await settle();
    await redraw();
    await click('.gd-author-search__result[data-id="4_262"]');
    expect(text('[data-action=new-slot-pick]')).toBe('Aldor, Soldier of Edoras');
    await click('[data-action=add-slot]');
    expect(patch()).toEqual({
      top: {
        slots: { ...DRAFT.slots, ally: 'support' },
        solutions: [
          { name: 'published', picks: { condition: '5_87', hand1: '1_2', ally: '4_262' }, answers: ['use(condition)'] },
        ],
      },
    });
    notifier.answers.push(true);
    await click(`${slot('hand1')} [data-action=remove]`);
    expect(notifier.dialogs.at(-1)).toBe('confirm: Take the slot hand1 out, and its card out of every solution?');
    expect(patch()).toEqual({
      top: {
        slots: { condition: DRAFT.slots!['condition'] },
        solutions: [{ name: 'published', picks: { condition: '5_87' }, answers: ['use(condition)'] }],
      },
    });
  });

  it('adds a site slot as one of the scenario’s sites, and renames a slot as a card is renamed', async () => {
    const draft: Draft = { ...DRAFT, solutions: [] };
    await open(withPuzzle({ draft }));
    await choose('[data-part=new-slot-zone] select', 'site');
    expect(all('[data-part=new-slot-site] option').map((one) => one.getAttribute('value'))).toEqual([
      '',
      'site1',
      'site2',
      'site3',
      'site1opp',
      'site2opp',
      'site3opp',
    ]);
    await choose('[data-part=new-slot-site] select', 'site2');
    expect(el.querySelector('[data-action=new-slot-pick]')).toBeNull();
    await click('[data-action=add-slot]');
    expect(patch()).toEqual({ top: { slots: { ...DRAFT.slots, site2: 'site' } } });
    await click(`${slot('hand1')} [data-action=rename]`);
    const rename = find<HTMLInputElement>(`${slot('hand1')} gd-author-board-rename input`);
    rename.value = 'trick';
    rename.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    await settle();
    await redraw();
    const renamed = asked('alias');
    expect(renamed).toHaveLength(1);
    expect(sentForm(renamed[0]!.request).get('from')).toBe('hand1');
    expect(sentForm(renamed[0]!.request).get('to')).toBe('trick');
  });

  it('shows no slots in a draft that is no puzzle, and says when a puzzle has none', async () => {
    const tutorial: Draft = { id: 'test', kind: 'tutorial', human: 'freeps', steps: [], cards: DRAFT.cards! };
    await open(withPuzzle({ draft: tutorial }));
    expect(el.querySelector('gd-author-board-slots .gd-author-board__heading')).toBeNull();
  });

  it('says a puzzle has no slots', async () => {
    await open(
      withPuzzle({
        draft: { id: 'test', kind: 'puzzle', steps: [] },
        board: draftBoard({ cards: [boardCard('frodo')] }),
      }),
    );
    expect(text('gd-author-board-slots [data-empty]')).toBe('No slots: every card is the scenario’s.');
  });

  it('shows a site slot (no seat, no rename), an empty one, a host the file names, and takes one out with no question', async () => {
    const draft: Draft = {
      ...DRAFT,
      solutions: [],
      slots: { site2: 'site', trap: { zone: 'stacked', on: 'gone' }, mine: { zone: 'hand', seat: 'freeps' } },
    };
    const slots = [
      puzzleSlot('site2', { zone: 'site', pick: null, pickName: null, filter: null }),
      puzzleSlot('trap', { zone: 'stacked', on: 'gone', pick: null, pickName: null }),
    ];
    await open(withPuzzle({ draft, puzzle: puzzleState({ slots, pool: null }) }));
    expect(el.querySelector(`${slot('site2')} [data-part=slot-seat]`)).toBeNull();
    expect(el.querySelector(`${slot('site2')} [data-action=rename]`)).toBeNull();
    expect(find(`${slot('trap')} .gd-author-slot__pick`).hasAttribute('data-empty')).toBe(true);
    expect(find<HTMLSelectElement>(`${slot('trap')} [data-part=slot-on] select`).value).toBe('gone');
    expect(find<HTMLSelectElement>(`${slot('mine')} [data-part=slot-seat] select`).value).toBe('');
    await choose(`${slot('trap')} [data-part=slot-on] select`, '');
    expect(patch()).toEqual({
      top: { slots: { site2: 'site', trap: 'stacked', mine: { zone: 'hand', seat: 'freeps' } } },
    });
    await click(`${slot('mine')} [data-action=remove]`);
    expect(notifier.dialogs).toEqual([]);
    expect(patch()).toEqual({ top: { slots: { site2: 'site', trap: { zone: 'stacked', on: 'gone' } } } });
    await searched(0);
  });

  it('keeps a slot unchanged when the page’s question is answered no, and lets go of a card being found', async () => {
    await open();
    notifier.answers.push(false);
    await click(`${slot('hand1')} [data-action=remove]`);
    expect(asked('edit')).toHaveLength(0);
    await click('[data-action=new-slot-pick]');
    await click('.gd-author-slots__add [data-action=cancel]');
    expect(el.querySelector('.gd-author-slots__add gd-author-card-search')).toBeNull();
    // (a site slot's id is a site's name)
    await choose('[data-part=new-slot-zone] select', 'site');
    expect(find<HTMLButtonElement>('[data-action=add-slot]').disabled).toBe(true);
    await searched(5);
  });

  it('offers no mod… from a server that has no Modify… vocabulary, and still shows what is on a card', async () => {
    const state = withPuzzle({ modify: undefined });
    await open(state);
    expect(el.querySelector(`${card('frodo')} [data-action=modify]`)).toBeNull();
    expect(all(`${card('frodo')} .gd-author-mods__chip`)).toHaveLength(2);
    await searched(1);
  });

  it('adds a slot with no solutions to fill, keeping the form when the server refuses it', async () => {
    const draft: Draft = { ...DRAFT, solutions: [] };
    await open(withPuzzle({ draft, puzzle: undefined }));
    expect(all('[data-part=new-slot-zone] option').map((one) => one.getAttribute('value'))).toEqual(['hand']);
    expect(el.querySelector('[data-action=new-slot-pick]')).toBeNull();
    await type('[data-part=new-slot-id]', 'trick', ['input']);
    await click('[data-action=add-slot]');
    const sent = asked('edit');
    expect(parseJson(sentForm(sent[0]!.request).get('patch') as string)).toEqual({
      top: { slots: { ...DRAFT.slots, trick: 'hand' } },
    });
    sent[0]!.flush(JSON.stringify({ error: 'No.' }), { status: 400, statusText: 'No' });
    await settle();
    await redraw();
    expect(find<HTMLInputElement>('[data-part=new-slot-id]').value).toBe('trick');
    await searched(1);
  });

  it('shows a slot’s form asked for from the table, the focus in its label; marks the Ring-bearer and the Ring', async () => {
    const state = withPuzzle();
    const cards = state.board!.cards.map((card) =>
      card.alias === 'frodo' ? { ...card, ringBearer: true } : card.alias === 'ring' ? { ...card, ring: true } : card,
    );
    await open({ ...state, board: { ...state.board!, cards } });
    authoring.remember({ showSlot: 'hand1' });
    await redraw();
    await redraw();
    expect(authoring.ui().showSlot).toBeNull();
    expect(document.activeElement).toBe(find(`${slot('hand1')} input[data-part=slot-label]`));
    expect(text(`${card('frodo')} [data-tag=ring-bearer]`)).toBe('Ring-bearer');
    expect(text(`${card('ring')} [data-tag=ring]`)).toBe('Ring');
    expect(find(`${card('frodo')} button[data-action=drop]`).title).toMatch(/Ring-bearer: the server takes it out/);
    expect(find(`${card('ring')} button[data-action=drop]`).title).toMatch(/^Its seat’s Ring:/);
    expect(find(`${card('sam')} button[data-action=drop]`).title).toMatch(/^Take this card out/);
    await searched(1);
  });
});
