import { boardCard, draftBoard, draftState, MODIFY_WORDS, puzzleSlot, puzzleState } from '../../../testing/authoring';
import { DraftState } from '../authoring/authoring-state';
import { GameCard } from '../state/game-model';
import { AUTHOR_TOOLS, authorBoard, AuthorTable, OWN_TOKENS, setupCount } from './author-board';
import { cardByAlias } from './edit-board';

const gameCard = (id: string, extra: Partial<GameCard> = {}): GameCard => ({
  id,
  blueprintId: '1_323',
  zone: 'ADVENTURE_PATH',
  owner: 'asdf',
  hindered: false,
  numbersHidden: false,
  index: null,
  attachedTo: null,
  stackedOn: null,
  attachedAt: 0,
  tokens: {},
  inactive: false,
  assignedTo: null,
  inSkirmish: false,
  testingText: null,
  backSideTestingText: null,
  ...extra,
});

const STATE: DraftState = draftState({
  draft: {
    id: 'test',
    title: 'A Test',
    kind: 'puzzle',
    human: 'freeps',
    steps: [],
    setup: { wounds: { frodo: 2 }, tokens: { frodo: 1 } },
  },
  board: draftBoard({
    cards: [
      boardCard('frodo', {
        wounds: 1,
        tokens: 3,
        ringBearer: true,
        modifiers: [
          { id: 4, what: 'strength', amount: 2, until: 'turn', text: 'Strength +2', written: 'strength +2 turn' },
        ],
      }),
      boardCard('ring', {
        blueprintId: '1_2',
        type: 'THE_ONE_RING',
        cardId: 26,
        zone: 'attached',
        on: 'frodo',
        ring: true,
      }),
    ],
    markers: { freeps: 1, shadow: 1 },
    currentSite: 1,
    path: [
      {
        number: 1,
        alias: 'site1',
        blueprintId: '1_323',
        name: 'Site 1',
        owner: 'freeps',
        controlledBy: null,
        printed: 1,
      },
    ],
  }),
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
    phases: [],
    lastSite: 9,
    modifiers: { frodo: [{ what: 'archer', amount: null, until: 'removed', written: 'archer' }] },
  },
  modify: MODIFY_WORDS,
  puzzle: puzzleState({
    slots: [
      puzzleSlot('ally', { zone: 'support', pick: '1_69', pickName: 'Albert Dreary' }),
      puzzleSlot('hand1', { zone: 'hand', pick: null, pickName: null }),
    ],
  }),
});

const table = (extra: Partial<AuthorTable> = {}): AuthorTable => ({
  state: STATE,
  inSetup: false,
  mode: 'edit',
  tools: AUTHOR_TOOLS,
  cards: {
    '24': gameCard('24', { index: 1 }),
    '25': gameCard('25', { blueprintId: '10_121', zone: 'FREE_CHARACTERS', tokens: { SHIRE: 3 } }),
    '30': gameCard('30', { blueprintId: '1_69', zone: 'SUPPORT' }),
    '31': gameCard('31', { blueprintId: '1_69', zone: 'SUPPORT', owner: '~Scenario' }),
  },
  zones: { asdf: { hand: 2, discard: 0, adventureDeck: 8, dead: 0, deck: 5, removed: null, handRevealed: false } },
  seats: { human: 'freeps', players: { freeps: 'asdf', shadow: '~Scenario' } },
  ...extra,
});

describe("the author's board", () => {
  it('is nothing until the board is laid out', () => {
    expect(authorBoard(table({ state: draftState() }))).toBeNull();
  });

  it('says the level, mode and tools, and the board as it stands: wounds, own tokens, modifiers in force', () => {
    const board = authorBoard(table())!;
    expect(board).toMatchObject({
      mode: 'edit',
      level: 'author',
      tools: AUTHOR_TOOLS,
      canStop: false,
      canStart: false,
    });
    expect(board.hiddenPilesOpen).toBe(true);
    expect(board.cultures).toEqual([OWN_TOKENS]);
    const frodo = cardByAlias(board, 'frodo')!;
    expect(frodo).toMatchObject({ cardId: '25', wounds: 1, cultureTokens: { [OWN_TOKENS]: 3 }, culture: OWN_TOKENS });
    expect(frodo.ringBearer).toBe(true);
    expect(cardByAlias(board, 'ring')!.ring).toBe(true);
    expect(frodo.modifiers).toEqual([{ id: 4, what: 'strength', amount: 2, until: 'turn', text: 'Strength +2' }]);
    expect(board.modify.words.map((word) => word.what)).toEqual(MODIFY_WORDS.words.map((word) => word.what));
    expect(board.modify.until).toEqual(['phase', 'turn', 'removed']);
    expect(board.markers).toEqual({ freeps: 1, shadow: 1 });
    expect(board.currentSite).toBe(1);
  });

  it('at the start, shows what the setup puts on a card: its wounds, tokens and modifiers', () => {
    const frodo = cardByAlias(authorBoard(table({ inSetup: true })), 'frodo')!;
    expect(frodo.wounds).toBe(2);
    expect(frodo.cultureTokens).toEqual({ [OWN_TOKENS]: 1 });
    expect(frodo.modifiers).toEqual([{ id: 1, what: 'archer', amount: null, until: 'removed', text: 'Archer' }]);
    expect(cardByAlias(authorBoard(table({ inSetup: true })), 'ring')!.cultureTokens).toEqual({});
  });

  it("lays the path's sites as cards of the page's, and each seat's other sites as its adventure deck", () => {
    const board = authorBoard(table())!;
    expect(cardByAlias(board, 'site1')).toMatchObject({
      cardId: '24',
      zone: 'path',
      site: 1,
      type: 'SITE',
      printed: 1,
    });
    expect(board.path).toEqual([
      {
        number: 1,
        alias: 'site1',
        blueprintId: '1_323',
        name: 'Site 1',
        printed: 1,
        owner: 'freeps',
        controlledBy: null,
      },
    ]);
    const deck = board.cards.filter((card) => card.zone === 'adventureDeck');
    expect(deck.map((card) => card.alias)).toEqual(['site2', 'site3', 'site1opp', 'site2opp', 'site3opp']);
    expect(deck[0]).toMatchObject({ cardId: null, owner: 'freeps', printed: 2 });
    // (a site laid with no card of the page's for it is left out)
    expect(authorBoard(table({ cards: {} }))!.cards.some((card) => card.zone === 'path')).toBe(false);
  });

  it("finds a slot's card on the page by its card and where its slot puts it, marked with its slot", () => {
    const board = authorBoard(table())!;
    const ally = board.cards.find((card) => card.slot === 'ally')!;
    expect(ally).toMatchObject({
      alias: 'ally',
      cardId: '30',
      owner: 'freeps',
      zone: 'support',
      name: 'Albert Dreary',
    });
    expect(board.cards.filter((card) => card.slot !== undefined)).toHaveLength(1);
    // (the opponent's slot's card is the other player's)
    const theirs = draftState({
      ...STATE,
      puzzle: puzzleState({ slots: [puzzleSlot('ally', { seat: 'opponent', zone: 'support', pick: '1_69' })] }),
    });
    expect(authorBoard(table({ state: theirs }))!.cards.find((card) => card.slot)?.cardId).toBe('31');
  });

  it("counts the piles as the game does, or else the scenario's cards in each", () => {
    const board = authorBoard(table())!;
    expect(board.piles.freeps).toEqual({ hand: 2, deck: 5, discard: 0, deadPile: 0, adventureDeck: 8 });
    expect(board.piles.shadow).toEqual({});
    const counted = authorBoard(table({ zones: {} }))!;
    expect(counted.piles.freeps).toEqual({ characters: 1, attached: 1 });
  });

  it('reads how many of a setup part a card has, 0 when it says nothing', () => {
    expect(setupCount(STATE, 'wounds', 'frodo')).toBe(2);
    expect(setupCount(STATE, 'tokens', 'ring')).toBe(0);
    expect(setupCount(draftState(), 'tokens', 'frodo')).toBe(0);
  });

  it('reads a sparse state: nameless sites, an odd vocabulary, nothing more', () => {
    const sparse = draftState({
      board: draftBoard({
        cards: [boardCard('frodo')],
        path: [{ number: 1, alias: null, blueprintId: '1_323', name: null, owner: 'freeps', controlledBy: 'shadow' }],
        sites: { freeps: [{ number: 1, id: '1_323', alias: null, name: null }], shadow: [] },
      }),
      modify: {
        words: [{ what: 'odd', label: 'Odd', kind: 'keyword', number: 'positive', types: [] }],
        until: ['never'],
      },
    });
    const board = authorBoard(table({ state: sparse, cards: { '24': gameCard('24', { index: 1 }) } }))!;
    expect(board.path).toEqual([
      {
        number: 1,
        alias: 'site1',
        blueprintId: '1_323',
        name: '1_323',
        printed: null,
        owner: 'freeps',
        controlledBy: 'shadow',
      },
    ]);
    expect(cardByAlias(board, 'site1')).toMatchObject({ name: '1_323', printed: null });
    expect(board.cards.filter((card) => card.zone === 'adventureDeck')).toEqual([]);
    expect(board.modify).toEqual({
      words: [{ what: 'odd', label: 'Odd', kind: 'keyword', number: 'positive', types: [] }],
      until: ['phase', 'turn', 'removed'],
    });
    expect(board.markers).toEqual({});
    expect(board.currentSite).toBeNull();
    const plain = authorBoard(table({ state: draftState({ board: draftBoard({ cards: [boardCard('frodo')] }) }) }))!;
    expect(plain.modify.words).toEqual([]);
    expect(cardByAlias(plain, 'frodo')!.modifiers).toEqual([]);
    expect(
      cardByAlias(authorBoard(table({ state: draftState({ board: draftBoard() }), inSetup: true })), 'frodo')!
        .modifiers,
    ).toEqual([]);
  });
});
