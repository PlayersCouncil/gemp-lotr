import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { draftBoard, draftGameXml, draftState, result } from '../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { CardInfoService } from '../../cards/card-info.service';
import { HallTab } from '../../platform/hall-tab.service';
import { PageCues } from '../../platform/page-cues';
import { PageExit } from '../../platform/page-exit';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { provideGameState } from '../game-state';
import { GameSession } from '../session/game-session.service';
import { DraftState, FoundCard } from './authoring-state';
import { AUTHOR_UI_KEY, GatheredBoard } from './authoring-ui-store';
import { Authoring, StepFlushResult } from './authoring.service';
import { BoardChanges, PART_MADE, WHERE_REFUSED } from './board-changes.service';
import { START_DEFAULT } from './board-start';
import { sentForm } from '../../../testing/sent-form';
import { parseJson } from '../../api/json';
import { AUTHOR, answerCall, authorCall, authorCalls, sentPatch } from '../../../testing/authoring-requests';

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

const withBoard = (extra: Partial<DraftState> = {}) => draftState({ draft: DRAFT, board: draftBoard(), ...extra });

const GANDALF: FoundCard = {
  id: '1_72',
  name: 'Gandalf, Friend of the Shirefolk',
  type: 'companion',
  culture: 'gandalf',
  side: 'free_people',
  cost: 4,
};

describe('BoardChanges', () => {
  let changes: BoardChanges;
  let authoring: Authoring;
  let backend: HttpTestingController;
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
        BoardChanges,
        CardInfoService,
        { provide: PageCues, useValue: cues },
        { provide: HallTab, useValue: { go: vi.fn(), elsewhere: signal(null) } },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    authoring = TestBed.inject(Authoring);
    changes = TestBed.inject(BoardChanges);
    notifier = TestBed.inject(FakeNotifier);
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

  const asked = (what: string) => authorCalls(backend, what);

  /** The one call to `what` on its way, answered (or refused with `error`); what it sent. */
  async function answer(what: string, body: unknown, error: string | null = null): Promise<TestRequest> {
    const request = authorCall(backend, what);
    answerCall(request, body, error);
    await settle();
    TestBed.tick();
    return request;
  }

  const form = (request: TestRequest, name: string) => sentForm(request.request).get(name);
  const patchOf = (request: TestRequest) => sentPatch(request) as { top: Record<string, unknown> };
  const stored = () => (JSON.parse(sessionStorage.getItem(AUTHOR_UI_KEY)!) as { board: GatheredBoard | null }).board;
  const ops = () => changes.gathered()!.ops.map((op) => `${op.name}(${op.args.join(', ')})`);
  const rebuilt = (gameId: string) => result(withBoard({ gameId }), { gameId, rebuilt: true });

  it('has nothing until the board is laid out', async () => {
    await ready(draftState());
    expect(changes.board()).toBeNull();
    expect(changes.gathered()).toBeNull();
    expect(changes.view()).toBeNull();
    expect(changes.count()).toBe(0);
    expect(changes.bearers()).toEqual([]);
    expect(changes.aliases()).toEqual([]);
    changes.queue('burdens', [2]);
    expect(authoring.ui().board).toBeNull();
    await changes.submit();
    expect(asked('edit')).toEqual([]);
    expect(await changes.add(GANDALF, 'gandalf', 'freeps', 'hand', null)).toBe(false);
    await changes.changeSite('freeps', 0, 'x');
    await changes.changeCard({ owner: 'freeps', alias: 'frodo' }, 'x');
    expect(asked('edit')).toEqual([]);
    expect(changes.path('freeps')).toEqual([]);
    expect(changes.ringChoices('freeps', 'COMPANION')).toEqual([]);
    expect(changes.ringOf('ring', 'shadow')).toBe('');
  });

  it('gathers changes for the draft, remembered, and shows the board as they will make it', async () => {
    await ready();
    changes.queue('wound', ['frodo', 1]);
    changes.queue('wound', ['frodo', 1]);
    changes.queue('burdens', [3]);
    expect(ops()).toEqual(['wound(frodo, 2)', 'burdens(3)']);
    expect(changes.count()).toBe(2);
    expect(stored()).toMatchObject({
      draft: 'test',
      ops: [{ name: 'wound', args: ['frodo', 2] }, { name: 'burdens' }],
    });
    expect(changes.view()!.cards.find((card) => card.alias === 'frodo')!.wounds).toBe(2);
    expect(changes.view()!.burdens).toBe(3);
    changes.undoOp(0);
    expect(ops()).toEqual(['burdens(3)']);
    changes.drop({ alias: 'sam', added: false });
    expect(changes.gathered()!.removed).toEqual(['sam']);
    expect(changes.aliases()).not.toContain('sam');
    changes.keep('sam');
    changes.drop({ alias: 'sam', added: false });
    changes.undoRemoved(0);
    expect(changes.gathered()!.removed).toEqual([]);
    expect(changes.bearers().map((card) => card.alias)).toEqual(['frodo', 'ring', 'oppFrodo']);
    changes.reset();
    expect(changes.count()).toBe(0);
    expect(stored()).toBeNull();
  });

  it("lets go of another draft's changes, and of what is about cards the board does not have", async () => {
    authoring.remember({
      board: {
        draft: 'test',
        ops: [
          { name: 'wound', args: ['gone', 1] },
          { name: 'wound', args: ['sam', 1] },
        ],
        added: [],
        removed: [],
      },
    });
    await ready();
    expect(ops()).toEqual(['wound(sam, 1)']);
    authoring.remember({ board: { draft: 'other', ops: [{ name: 'burdens', args: [2] }], added: [], removed: [] } });
    expect(changes.count()).toBe(0);
  });

  it('changes where changes are made only with nothing gathered', async () => {
    await ready();
    expect(changes.inSetup()).toBe(false);
    expect(changes.setWhere('setup')).toBe(true);
    expect(changes.inSetup()).toBe(true);
    changes.queue('burdens', [3]);
    expect(changes.setWhere('now')).toBe(false);
    expect(authoring.problem()).toBe(WHERE_REFUSED);
    expect(changes.inSetup()).toBe(true);
  });

  it('sends nothing with nothing gathered, nor when the Step tab could not send its form', async () => {
    await ready();
    await changes.submit();
    changes.queue('burdens', [3]);
    authoring.attachStepTab({
      flush: () => Promise.resolve<StepFlushResult>({ went: false, note: 'no' }),
      engaged: () => false,
      reset: () => undefined,
    });
    await changes.submit();
    expect(asked('do')).toEqual([]);
    expect(changes.count()).toBe(1);
  });

  it('sends the changes to the board as it stands as one request, and the game carries on', async () => {
    await ready();
    changes.queue('wound', ['frodo', 1]);
    changes.queue('burdens', [3]);
    void changes.submit();
    await settle();
    expect(asked('edit')).toEqual([]);
    const sent = await answer('do', result(withBoard(), { events: true }));
    expect(form(sent, 'op')).toBe('wound(frodo, 1), burdens(3)');
    expect(form(sent, 'setup')).toBe('false');
    expect(changes.count()).toBe(0);
    expect(cues.go).not.toHaveBeenCalled();
  });

  it('sends the cards first, holding the fresh game until the board has been changed too, then goes to it', async () => {
    await ready();
    changes.setWhere('setup');
    expect(await changes.add(GANDALF, 'gandalf', 'freeps', 'hand', 'frodo')).toBe(true);
    changes.drop({ alias: 'merry', added: false });
    changes.queue('burdens', [3]);
    void changes.submit();
    await settle();
    expect(asked('do')).toEqual([]);
    const edit = await answer('edit', rebuilt('9'));
    expect(patchOf(edit).top).toEqual({
      cards: {
        freeps: { frodo: '10_121', ring: '1_2', sam: '1_311', gandalf: '1_72' },
        shadow: { oppFrodo: '10_121', orc: '1_191' },
      },
      setup: { freeps: { hand: 'sam, gandalf' }, shadow: { hand: 'orc' } },
    });
    expect(form(edit, 'goto')).toBeNull();
    expect(cues.go).not.toHaveBeenCalled();
    expect(stored()).toMatchObject({ added: [], removed: [], ops: [{ name: 'burdens' }] });
    const sent = await answer('do', rebuilt('10'));
    expect(form(sent, 'setup')).toBe('true');
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=10');
    expect(stored()).toMatchObject({ ops: [], added: [], removed: [] });
  });

  it('goes to the fresh game the cards brought when no change to the board was gathered', async () => {
    await ready();
    changes.drop({ alias: 'merry', added: false });
    void changes.submit();
    await settle();
    await answer('edit', rebuilt('9'));
    expect(asked('do')).toEqual([]);
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
    expect(stored()).toMatchObject({ removed: [] });
  });

  it('keeps everything gathered when the cards are refused, and sends nothing more', async () => {
    await ready();
    changes.drop({ alias: 'merry', added: false });
    changes.queue('burdens', [3]);
    void changes.submit();
    await settle();
    await answer('edit', null, 'No such card.');
    expect(asked('do')).toEqual([]);
    expect(authoring.problem()).toBe('No such card.');
    expect(changes.count()).toBe(2);
    expect(cues.go).not.toHaveBeenCalled();
    // (nothing is held any more: a later fresh game is gone to)
    expect(authoring.releaseArrivals()).toBe(false);
  });

  it('lets go of the changes to the board as it stands when one is refused (those before it were made), and says so', async () => {
    await ready();
    changes.drop({ alias: 'merry', added: false });
    changes.queue('wound', ['frodo', 1]);
    changes.queue('burdens', [3]);
    void changes.submit();
    await settle();
    await answer('edit', rebuilt('9'));
    await answer('do', null, 'The game is over: go back to a step first.');
    const said = `${PART_MADE}The game is over: go back to a step first.`;
    expect(authoring.problem()).toBe(said);
    expect(stored()).toMatchObject({ ops: [], removed: [] });
    // (carried to the fresh game the cards brought)
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
    expect(parseJson(sessionStorage.getItem(AUTHOR_UI_KEY)!)).toMatchObject({ carried: said });
  });

  it('keeps the changes to the setup when one is refused (none was made), the problem said here when no game came', async () => {
    await ready();
    changes.setWhere('setup');
    changes.queue('burdens', [3]);
    changes.queue('twilight', [2]);
    void changes.submit();
    await settle();
    await answer('do', null, 'The starting board has no twilight pool to set.');
    expect(authoring.problem()).toBe('The starting board has no twilight pool to set.');
    expect(ops()).toEqual(['burdens(3)', 'twilight(2)']);
    expect(cues.go).not.toHaveBeenCalled();
    expect(authoring.ui().carried).toBeNull();
  });

  it('keeps the changes to the setup refused after the cards went, carrying the problem to their fresh game', async () => {
    await ready();
    changes.setWhere('setup');
    await changes.add(GANDALF, 'gandalf', 'freeps', 'hand', null);
    changes.queue('wound', ['gandalf', 1]);
    void changes.submit();
    await settle();
    await answer('edit', rebuilt('9'));
    await answer('do', null, 'No.');
    expect(stored()).toMatchObject({ added: [], ops: [{ name: 'wound', args: ['gandalf', 1] }] });
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
    expect(authoring.ui().carried).toBe('No.');
  });

  it('renames a card: what was gathered about it says the new name, and the old again if it is refused', async () => {
    await ready();
    changes.queue('wound', ['sam', 1]);
    void changes.rename('sam', 'samwise');
    await settle();
    expect(stored()!.ops[0]!.args).toEqual(['samwise', 1]);
    const sent = await answer('alias', null, 'The scenario already has a card called that.');
    expect([form(sent, 'from'), form(sent, 'to')]).toEqual(['sam', 'samwise']);
    expect(stored()!.ops[0]!.args).toEqual(['sam', 1]);
    const done = changes.rename('sam', 'samwise');
    await settle();
    await answer('alias', rebuilt('9'));
    expect(await done).toBe(true);
    expect(cues.go).toHaveBeenCalledWith('/gemp-lotr/ng/game?gameId=9');
    expect(stored()!.ops[0]!.args).toEqual(['samwise', 1]);
  });

  it('changes a card for another at once, keeping its name', async () => {
    await ready();
    void changes.changeCard({ owner: 'freeps', alias: 'sam' }, '2_1');
    await settle();
    const sent = await answer('edit', result(withBoard()));
    expect(patchOf(sent).top).toEqual({ cards: { ...DRAFT.cards, freeps: { ...DRAFT.cards.freeps, sam: '2_1' } } });
    void changes.changeCard({ owner: 'shadow', alias: 'sam' }, '2_1');
    await settle();
    expect(asked('edit')).toEqual([]);
  });

  it('changes the path at once: both seats while they are the same, one seat when they are apart', async () => {
    await ready();
    expect(changes.sitesApart()).toBe(false);
    expect(changes.path('freeps').map((site) => site.id)).toEqual(['1_323', '1_329', '1_341']);
    void changes.changeSite('freeps', 1, '1_327');
    await settle();
    expect(patchOf(await answer('edit', result(withBoard()))).top).toEqual({
      sites: { freeps: '1_323, 1_327, 1_341', shadow: '1_323, 1_327, 1_341' },
    });
    void changes.changeSite('freeps', 3, '1_348');
    await settle();
    expect(patchOf(await answer('edit', result(withBoard()))).top['sites']).toMatchObject({
      freeps: '1_323, 1_329, 1_341, 1_348',
    });
    await changes.samePath(false);
    expect(changes.sitesApart()).toBe(true);
    expect(asked('edit')).toEqual([]);
    void changes.changeSite('shadow', 2, null);
    await settle();
    expect(patchOf(await answer('edit', result(withBoard()))).top).toEqual({
      sites: { freeps: '1_323, 1_329, 1_341', shadow: '1_323, 1_329' },
    });
    void changes.changeSite('freeps', 0, '1_324');
    await settle();
    expect(patchOf(await answer('edit', result(withBoard()))).top).toEqual({
      sites: { freeps: '1_324, 1_329, 1_341', shadow: '1_323, 1_329, 1_341' },
    });
  });

  it("makes the paths the same, the Free Peoples' for both, when they are not", async () => {
    const board = draftBoard();
    await ready(withBoard({ board: { ...board, sites: { ...board.sites, shadow: board.sites.shadow.slice(0, 1) } } }));
    expect(changes.sitesApart()).toBe(true);
    void changes.samePath(true);
    await settle();
    expect(patchOf(await answer('edit', result(withBoard()))).top).toEqual({
      sites: { freeps: '1_323, 1_329, 1_341', shadow: '1_323, 1_329, 1_341' },
    });
    expect(authoring.ui().sitesApart).toBe(false);
    // (already the same: nothing to send)
    await changes.samePath(true);
    expect(asked('edit')).toEqual([]);
  });

  it("sets a seat's Ring-bearer and Ring at once", async () => {
    await ready();
    expect(changes.ringChoices('freeps', 'COMPANION').map(([alias]) => alias)).toEqual(['frodo', 'sam', 'merry']);
    expect(changes.ringOf('ringBearer', 'shadow')).toBe('oppFrodo');
    void changes.setRing('ringBearer', 'freeps', 'sam');
    await settle();
    expect(patchOf(await answer('edit', result(withBoard()))).top).toEqual({
      ringBearer: { freeps: 'sam', shadow: 'oppFrodo' },
    });
    void changes.setRing('ring', 'freeps', '');
    await settle();
    expect(patchOf(await answer('edit', result(withBoard()))).top).toEqual({ ring: null });
  });

  it('adds a card under a name: refused names said; a name taken asks whether to replace it', async () => {
    await ready();
    expect(await changes.add(GANDALF, '9lives', 'freeps', 'hand', null)).toBe(false);
    expect(authoring.problem()).toMatch(/letters, digits, dashes and underscores/);
    expect(await changes.add(GANDALF, 'site2opp', 'freeps', 'hand', null)).toBe(false);
    expect(authoring.problem()).toMatch(/site2opp is the name of a site/);
    expect(await changes.add(GANDALF, 'gandalf', 'freeps', 'attach', 'frodo')).toBe(true);
    expect(changes.gathered()!.added).toEqual([
      {
        alias: 'gandalf',
        blueprintId: '1_72',
        name: GANDALF.name,
        type: 'COMPANION',
        seat: 'freeps',
        zone: 'attach',
        host: 'frodo',
      },
    ]);
    expect(notifier.dialogs).toEqual([]);
    notifier.answers.push(false);
    expect(await changes.add(GANDALF, 'sam', 'freeps', '', 'frodo')).toBe(false);
    expect(notifier.dialogs[0]).toMatch(/^confirm: sam is already Sam, Son of Hamfast \(1_311\), Free Peoples seat\./);
    expect(notifier.words[0]).toMatchObject({ title: 'Replace sam?', ok: 'Replace sam', cancel: 'Cancel' });
    notifier.answers.push(true);
    expect(await changes.add(GANDALF, 'sam', 'freeps', '', 'frodo')).toBe(true);
    expect(changes.gathered()!.added.map((add) => [add.alias, add.zone, add.host])).toEqual([
      ['gandalf', 'attach', 'frodo'],
      ['sam', '', null],
    ]);
    changes.undoAdded(0);
    expect(changes.gathered()!.added.map((add) => add.alias)).toEqual(['sam']);
  });

  describe('how the game opens (B10)', () => {
    it('opens with the game: the setup keeps only its decks, after the page asks what goes, and the opening step goes', async () => {
      const state = withBoard({
        draft: { ...DRAFT, setup: { freeps: { hand: 'sam', deck: 'merry' } }, steps: [{ when: 'opening' }] },
      });
      await ready(state);
      expect(changes.opening()).toBe('board');
      notifier.answers.push(false);
      expect(await changes.setOpening('game')).toBe(false);
      expect(notifier.dialogs[0]).toBe(
        "confirm: Its setup keeps only each deck's order. Lost: Free Peoples hand: sam; step 1, read before the first turn.",
      );
      expect(notifier.words[0]).toMatchObject({ ok: 'Open with the game', cancel: 'Cancel' });
      expect(asked('edit')).toEqual([]);
      notifier.answers.push(true);
      const going = changes.setOpening('game');
      await settle();
      const request = await answer('edit', rebuilt('8'));
      expect(parseJson(form(request, 'patch')!)).toEqual({
        top: { setup: { opening: 'game', freeps: { deck: 'merry' } } },
        steps: [{ op: 'delete', uid: 's1' }],
      });
      expect(await going).toBe(true);
      expect(await changes.setOpening('board')).toBe(false);
    });

    it('opens with the game at once when nothing would go; back to a board laid out, asked first where steps answer the opening', async () => {
      await ready(withBoard({ draft: { ...DRAFT, setup: undefined } }));
      const going = changes.setOpening('game');
      await settle();
      expect(patchOf(await answer('edit', rebuilt('8'))).top).toEqual({ setup: { opening: 'game' } });
      expect(await going).toBe(true);
      expect(notifier.dialogs).toEqual([]);
      authoring.setState(
        withBoard({
          start: { ...START_DEFAULT, opening: 'game' },
          draft: { ...DRAFT, setup: { opening: 'game' }, steps: [{ when: 'human, bid', allow: 'number(1)' }] },
        }),
      );
      expect(changes.opening()).toBe('game');
      expect(changes.deckOnly()).toBe(false);
      authoring.remember({ where: 'setup' });
      expect(changes.deckOnly()).toBe(true);
      notifier.answers.push(true);
      const back = changes.setOpening('board');
      await settle();
      expect(notifier.dialogs[0]).toBe('confirm: With the board laid out, step 1 will not be reached.');
      expect(patchOf(await answer('edit', rebuilt('9'))).top).toEqual({ setup: null });
      expect(await back).toBe(true);
    });

    it('is not changed with changes gathered', async () => {
      await ready();
      changes.queue('twilight', [3]);
      expect(await changes.setOpening('game')).toBe(false);
      expect(authoring.problem()).toBe(WHERE_REFUSED);
    });
  });

  it('knows a path without numbers by the number printed on its first site, as the state says', async () => {
    const board = draftBoard();
    const site = { number: 1, id: '11_236', alias: 'site1', name: 'A Road', printed: 0 };
    await ready(withBoard({ board: { ...board, sites: { freeps: [site], shadow: [{ ...site, printed: 1 }] } } }));
    expect(changes.unnumbered('freeps')).toBe(true);
    expect(changes.unnumbered('shadow')).toBe(false);
    expect(asked('cards')).toEqual([]);
  });
});
