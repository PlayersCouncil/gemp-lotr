import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import {
  draftBoard,
  draftGameXml,
  draftState,
  MODIFY_WORDS,
  puzzleSlot,
  puzzleState,
  result,
} from '../../../testing/authoring';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { sentForm } from '../../../testing/sent-form';
import { parseJson } from '../../api/json';
import { ApiError } from '../../api/api-error';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { CardInfoService } from '../../cards/card-info.service';
import { HallTab } from '../../platform/hall-tab.service';
import { PageCues } from '../../platform/page-cues';
import { PageExit } from '../../platform/page-exit';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { DraftState, FoundCard } from '../authoring/authoring-state';
import { Authoring } from '../authoring/authoring.service';
import { BoardChanges, WHERE_REFUSED } from '../authoring/board-changes.service';
import { modifyArgs } from '../authoring/board-modifiers';
import { addSlotPatch } from '../authoring/puzzle-slots';
import { provideGameState } from '../game-state';
import { GameSession } from '../session/game-session.service';
import { AuthorBackend, NOT_AN_AUTHORS } from './author-backend';
import { AUTHOR_REFUSALS } from './author-ops';
import { EditOp } from './edit-ops';
import { TableEdit } from './table-edit.service';
import { textContaining } from '../../../testing/matchers';
import { AUTHOR } from '../../../testing/authoring-requests';

const GAME = '/gemp-lotr-server/game/7';

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

const withBoard = (extra: Partial<DraftState> = {}) =>
  draftState({ draft: DRAFT, board: draftBoard(), modify: MODIFY_WORDS, ...extra });

const GANDALF: FoundCard = {
  id: '1_72',
  name: 'Gandalf, Friend of the Shirefolk',
  type: 'companion',
  culture: 'gandalf',
  side: 'free_people',
  cost: 4,
};
const SAM: FoundCard = {
  id: '1_311',
  name: 'Sam, Son of Hamfast',
  type: 'COMPANION',
  culture: null,
  side: null,
  cost: null,
};

describe("AuthorBackend: the author's table", () => {
  let author: AuthorBackend;
  let changes: BoardChanges;
  let authoring: Authoring;
  let edit: TableEdit;
  let backend: HttpTestingController;

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
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    author = TestBed.inject(AuthorBackend);
    changes = TestBed.inject(BoardChanges);
    edit = TestBed.inject(TableEdit);
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  async function ready(state: DraftState = withBoard()) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    backend.expectOne((r) => r.url === `${AUTHOR}state`).flush(JSON.stringify(state));
    await settle();
  }

  /** A request to the authoring service as a line: what, and what it sent. */
  const line = (request: TestRequest): string => {
    const what = request.request.url.slice(AUTHOR.length);
    const body = sentForm(request.request);
    if (what === 'do') return `do ${body.get('op')} setup=${body.get('setup')}`;
    if (what === 'edit') return `edit ${body.get('patch')}`;
    return what;
  };

  /**
   * The calls `act` makes to the authoring service, each answered (the state as it was: nothing is gone to); the card
   * look-up answered with Gandalf.
   */
  async function sentBy(act: () => Promise<unknown>): Promise<string[]> {
    const sent: string[] = [];
    let done = false;
    const acting = act().finally(() => (done = true));
    for (let round = 0; round < 12 && !done; round++) {
      await settle();
      TestBed.tick();
      for (const request of backend.match((r) => r.url === `${AUTHOR}cards`)) {
        request.flush(JSON.stringify({ cards: [GANDALF] }));
      }
      for (const request of backend.match((r) => r.url.startsWith(AUTHOR) && r.method === 'POST')) {
        sent.push(line(request));
        request.flush(JSON.stringify(result(withBoard())));
      }
    }
    await acting.catch(() => undefined);
    return sent;
  }

  /** What the table sends for an edit, and what the tab sends for the same change made with its own forms. */
  async function both(op: EditOp, tab: () => unknown | Promise<unknown>): Promise<[string[], string[]]> {
    const table = await sentBy(() => firstValueFrom(author.apply('7', [op])));
    expect(changes.count()).toBe(0);
    const formed = await sentBy(async () => {
      await tab();
      await changes.submit();
    });
    return [table, formed];
  }

  it('claims the page for the author while a draft is written: play until Edit table, then edit, remembered', async () => {
    expect(author.claim()).toBeNull();
    await ready();
    expect(author.writing()).toBe(true);
    expect(author.claim()).toMatchObject({ mode: 'play' });
    expect(edit.level()).toBe('author');
    expect(edit.mode()).toBe('play');
    expect(edit.editing()).toBe(false);
    author.toggle();
    expect(authoring.ui().editTable).toBe(true);
    expect(edit.mode()).toBe('edit');
    expect(edit.editing()).toBe(true);
    expect(edit.may('add')).toBe(true);
    expect(edit.may('piles')).toBe(false);
    TestBed.tick();
    expect(edit.board()?.level).toBe('author');
    // (no decision is answered meanwhile)
    expect(edit.allows()).toBe(false);
    author.toggle(false);
    expect(edit.editing()).toBe(false);
  });

  it('has no Stop, Start or Reset, and refuses an edit while the Board tab has changes waiting', async () => {
    await ready();
    author.toggle(true);
    for (const call of [author.stop(), author.start()]) {
      await expect(firstValueFrom(call)).rejects.toMatchObject({ said: NOT_AN_AUTHORS });
    }
    await expect(firstValueFrom(author.reset())).rejects.toBeInstanceOf(ApiError);
    changes.queue('burdens', [3]);
    await expect(firstValueFrom(author.apply('7', [{ op: 'wounds', cardId: 25, amount: 1 }]))).rejects.toMatchObject({
      said: WHERE_REFUSED,
    });
    changes.reset();
    await expect(firstValueFrom(author.apply('7', [{ op: 'pick', slot: 'x', blueprint: null }]))).rejects.toMatchObject(
      {
        said: AUTHOR_REFUSALS.pick,
      },
    );
    expect(backend.match((r) => r.url.startsWith(AUTHOR) && r.method === 'POST')).toEqual([]);
  });

  it('switches where changes go as the tab does: one state, refused with changes waiting', async () => {
    await ready();
    expect(author.inSetup()).toBe(false);
    expect(author.setWhere('setup')).toBe(true);
    expect(authoring.ui().where).toBe('setup');
    expect(author.inSetup()).toBe(true);
    changes.queue('wound', ['frodo', 1]);
    expect(author.setWhere('now')).toBe(false);
    expect(authoring.problem()).toBe(WHERE_REFUSED);
  });

  for (const where of ['now', 'setup'] as const) {
    describe(`to the board ${where === 'now' ? 'as it stands' : 'the scenario starts with'}`, () => {
      beforeEach(async () => {
        await ready();
        authoring.remember({ where });
        author.toggle(true);
      });

      const cases: [string, EditOp, (changes: BoardChanges) => unknown][] = [
        [
          'a move',
          { op: 'move', cardId: 30, to: 'freeps.characters' },
          (c) => c.queue('move', ['sam', 'freeps.characters']),
        ],
        ['an attach', { op: 'attach', cardId: 31, toId: 25 }, (c) => c.queue('attach', ['merry', 'frodo'])],
        ['a stack', { op: 'stack', cardId: 32, toId: 27 }, (c) => c.queue('stack', ['orc', 'oppFrodo'])],
        ['a wound', { op: 'wounds', cardId: 25, amount: 1 }, (c) => c.queue('wound', ['frodo', 1])],
        ['a heal', { op: 'wounds', cardId: 25, amount: -1 }, (c) => c.queue('heal', ['frodo', 1])],
        ['a token', { op: 'tokens', cardId: 25, amount: 1, culture: 'tokens' }, (c) => c.queue('tokens', ['frodo', 1])],
        [
          'a modifier',
          { op: 'modify', cardId: 25, what: 'strength', amount: 2, until: 'turn' },
          (c) => {
            const made = modifyArgs('frodo', MODIFY_WORDS.words[0]!, '+2', 'turn');
            if ('args' in made) c.queue('modify', made.args);
          },
        ],
        [
          'a modifier off',
          { op: 'unmodify', cardId: 25, what: 'strength' },
          (c) => c.queue('unmodify', ['frodo', 'strength']),
        ],
        ['the twilight', { op: 'twilight', set: 3 }, (c) => c.queue('twilight', [3])],
        ['a burden more', { op: 'burdens', amount: 1 }, (c) => c.queue('burdens', [2])],
        ['a marker', { op: 'marker', seat: 'freeps', site: 2 }, (c) => c.queue('marker', ['freeps', 2])],
        ['a site controlled', { op: 'control', site: 1, seat: 'shadow' }, (c) => c.queue('control', [1, 'shadow'])],
        ['a site freed', { op: 'liberate', site: 1 }, (c) => c.queue('liberate', [1])],
        [
          'a card added',
          { op: 'add', blueprint: '1_72', to: 'freeps.hand' },
          (c) => c.add(GANDALF, 'gandalf', 'freeps', 'hand', null),
        ],
        [
          'a card added to the top of the deck',
          { op: 'add', blueprint: '1_72', to: 'freeps.deck.top' },
          async (c) => {
            await c.add(GANDALF, 'gandalf', 'freeps', '', null);
            c.queue('move', ['gandalf', 'freeps.deck.top']);
          },
        ],
        [
          'a card added on another',
          { op: 'add', blueprint: '1_72', attachId: 25 },
          (c) => c.add(GANDALF, 'gandalf', 'freeps', 'attach', 'frodo'),
        ],
        ['a copy', { op: 'copy', cardId: 30 }, (c) => c.add(SAM, 'sam2', 'freeps', 'hand', null)],
        ['a card taken out', { op: 'delete', cardId: 30 }, (c) => c.drop({ alias: 'sam', added: false })],
      ];
      for (const [name, op, tab] of cases) {
        it(`sends ${name} exactly as the tab's own forms do`, async () => {
          const [table, formed] = await both(op, () => tab(changes));
          expect(table.length).toBeGreaterThan(0);
          expect(table).toEqual(formed);
        });
      }
    });
  }

  it("lays a site at the start as Whose sites does, and puts a seat's site in as the path's Change does", async () => {
    await ready();
    author.toggle(true);
    authoring.remember({ where: 'setup' });
    let [table, formed] = await both({ op: 'site', number: 2, from: 'shadow' }, () =>
      changes.setPathSite(2, 'site2opp'),
    );
    expect(table).toEqual(formed);
    expect(table[0]).toMatch(/^edit .*"path":"site1, site2opp"/);
    const site = await sentBy(() => firstValueFrom(author.apply('7', [{ op: 'site', number: 2, blueprint: '1_999' }])));
    const changed = await sentBy(() => changes.changeSite('freeps', 1, '1_999'));
    expect(site).toEqual(changed);
    expect(site).toHaveLength(1);
    // (a change the tab makes nothing of sends nothing: the marker is where it is)
    authoring.remember({ where: 'now' });
    [table, formed] = [
      await sentBy(() => firstValueFrom(author.apply('7', [{ op: 'wounds', cardId: 25, amount: 0 }]))),
      [],
    ];
    expect(table).toEqual(formed);
  });

  it('says a refusal in the server’s words, and lets go of what it gathered', async () => {
    await ready();
    author.toggle(true);
    const asking = firstValueFrom(author.apply('7', [{ op: 'wounds', cardId: 25, amount: 1 }]));
    await settle();
    const request = backend.expectOne((r) => r.url === `${AUTHOR}do`);
    request.flush(JSON.stringify({ error: 'Frodo cannot take it.' }), { status: 400, statusText: 'No' });
    await expect(asking).rejects.toMatchObject({ said: textContaining('Frodo cannot take it.') });
    expect(changes.count()).toBe(0);
  });

  it("opens a puzzle's slot at a place as the tab's Add slot does, and shows its form", async () => {
    const puzzle = withBoard({ draft: { ...DRAFT, kind: 'puzzle' }, puzzle: puzzleState({ slots: [] }) });
    await ready(puzzle);
    author.toggle(true);
    expect(author.slotted()).toBe(true);
    const opened = author.openSlot({ seat: 'shadow', zone: 'support' });
    await settle();
    const request = backend.expectOne((r) => r.url === `${AUTHOR}edit`);
    expect(parseJson(sentForm(request.request).get('patch')!)).toEqual(
      addSlotPatch(puzzle.draft, 'slot1', { zone: 'support', seat: 'shadow' }, null),
    );
    expect(authoring.ui()).toMatchObject({ tab: 'board', showSlot: 'slot1' });
    request.flush(JSON.stringify(result(puzzle)));
    expect(await opened).toBeNull();
    // (a deck's top is its first place; the bottom is no place a slot says)
    expect(await author.openSlot({ seat: 'freeps', zone: 'removed' })).toBe('A slot cannot be there.');
    const top = author.openSlot({ seat: 'freeps', zone: 'deck.top' });
    await settle();
    const refused = backend.expectOne((r) => r.url === `${AUTHOR}edit`);
    expect(parseJson(sentForm(refused.request).get('patch')!)).toEqual(
      addSlotPatch(puzzle.draft, 'slot1', { zone: 'deck' }, null),
    );
    refused.flush(JSON.stringify({ error: 'No.' }), { status: 400, statusText: 'No' });
    expect(await top).toBe('No.');
    expect(authoring.ui().showSlot).toBeNull();
    author.showSlot('ally');
    expect(authoring.ui()).toMatchObject({ tab: 'board', showSlot: 'ally' });
  });

  it("draws a puzzle's empty slots while the table is edited, and no slot outside a puzzle", async () => {
    const slots = [puzzleSlot('ally', { pick: null, pickName: null }), puzzleSlot('hand1', { zone: 'hand' })];
    await ready(withBoard({ draft: { ...DRAFT, kind: 'puzzle' }, puzzle: puzzleState({ slots }) }));
    expect(author.emptySlots()).toEqual([]);
    author.toggle(true);
    expect(author.emptySlots().map((filled) => filled.slot.id)).toEqual(['ally']);
    expect(author.emptySlots()[0]!.card).toBeNull();
  });

  it('offers no slot, and has no table, outside a draft written here', async () => {
    expect(await author.openSlot({ seat: 'freeps', zone: 'hand' })).not.toBeNull();
    await expect(firstValueFrom(author.state())).rejects.toBeInstanceOf(ApiError);
    await ready();
    author.toggle(true);
    expect(author.slotted()).toBe(false);
    expect((await firstValueFrom(author.state())).level).toBe('author');
  });

  it("names a card it cannot look up by its blueprint, and gives tokens taken off at the start the setup's count", async () => {
    const tokened = withBoard({ draft: { ...DRAFT, setup: { ...DRAFT.setup, tokens: { frodo: 2 } } } });
    await ready(tokened);
    author.toggle(true);
    authoring.remember({ where: 'setup' });
    const adding = firstValueFrom(author.apply('7', [{ op: 'add', blueprint: '9_99', to: 'freeps.hand' }]));
    await settle();
    backend.expectOne((r) => r.url === `${AUTHOR}cards`).flush('{}', { status: 500, statusText: 'No' });
    await settle();
    const edited = backend.expectOne((r) => r.url === `${AUTHOR}edit`);
    expect(
      (parseJson(sentForm(edited.request).get('patch')!) as { top: { cards: { freeps: unknown } } }).top.cards.freeps,
    ).toMatchObject({ c999: '9_99' });
    edited.flush(JSON.stringify(result(tokened)));
    await adding;
    const sent = await sentBy(() => firstValueFrom(author.apply('7', [{ op: 'tokens', cardId: 25, amount: -2 }])));
    expect(sent).toEqual(['do tokens(frodo, -2) setup=true']);
  });
});
