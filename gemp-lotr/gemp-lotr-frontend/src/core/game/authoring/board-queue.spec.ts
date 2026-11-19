import { AuthoringBoard, BoardCard, Draft } from './authoring-state';
import { GatheredBoard, GatheredCard } from './authoring-ui-store';
import {
  addAliasProblem,
  addCard,
  addedText,
  bearersOf,
  boardAliases,
  boardType,
  boardView,
  cardChange,
  changeCardTop,
  defaultSiteAlias,
  dropCard,
  existingCard,
  forgetCard,
  gather,
  gatheredCount,
  gatheredOf,
  keepCard,
  opText,
  pathIds,
  pruneGathered,
  renameGathered,
  renameProblem,
  reordered,
  replaceQuestion,
  ringChoices,
  ringTop,
  samePaths,
  seatCards,
  siteAliases,
  sitesTop,
  suggestAlias,
  undoAdded,
  undoOp,
  undoRemoved,
} from './board-queue';

function card(alias: string, extra: Partial<BoardCard> = {}): BoardCard {
  return {
    alias,
    blueprintId: '1_1',
    name: alias,
    type: 'COMPANION',
    owner: 'freeps',
    cardId: 1,
    zone: 'characters',
    at: null,
    wounds: 0,
    tokens: 0,
    on: null,
    ...extra,
  };
}

const site = (number: number, alias: string | null, id = `1_${300 + number}`) => ({
  number,
  id,
  alias,
  name: `Site ${id}`,
});

function board(extra: Partial<AuthoringBoard> = {}): AuthoringBoard {
  return {
    cards: [
      card('frodo', { name: 'Frodo, Resolute Hobbit', blueprintId: '10_121' }),
      card('ring', { type: 'THE_ONE_RING', zone: 'attached', on: 'frodo', blueprintId: '1_2' }),
      card('sam', { zone: 'hand', at: 0 }),
      card('merry', { zone: 'hand', at: 1 }),
      card('pippin', { zone: 'hand', at: 2 }),
      card('orc', { owner: 'shadow', type: 'MINION', zone: 'hand', at: 0 }),
      card('cave', { zone: 'support', type: 'CONDITION', tokens: 1 }),
    ],
    twilight: 2,
    burdens: 1,
    threats: 0,
    sites: { freeps: [site(1, 'site1'), site(2, 'site2')], shadow: [site(1, 'site1opp'), site(2, 'site2opp')] },
    ...extra,
  };
}

const none = (extra: Partial<GatheredBoard> = {}): GatheredBoard => ({
  draft: 'test',
  ops: [],
  added: [],
  removed: [],
  ...extra,
});

function added(alias: string, extra: Partial<GatheredCard> = {}): GatheredCard {
  return {
    alias,
    blueprintId: '1_72',
    name: 'Gandalf',
    type: 'COMPANION',
    seat: 'freeps',
    zone: 'hand',
    host: null,
    ...extra,
  };
}

/** Gathers each change in turn; answers the ops as the server would read them. */
function gathering(...changes: [string, ...(string | number)[]][]) {
  let gathered = none();
  for (const [name, ...args] of changes) gathered = gather(gathered, board(), name, args);
  return gathered.ops.map(opText);
}

describe('the Board tab model', () => {
  describe('what has been gathered', () => {
    it("is the draft's own: another draft's, or one not understood, is nothing", () => {
      const saved = none({ removed: ['sam'] });
      expect(gatheredOf(saved, 'test')).toEqual(saved);
      expect(gatheredOf(saved, 'other')).toEqual({ draft: 'other', ops: [], added: [], removed: [] });
      expect(gatheredOf(null, 'test')).toEqual(none());
      expect(gatheredOf(undefined, 'test')).toEqual(none());
      expect(gatheredOf({ draft: 'test' } as unknown as GatheredBoard, 'test')).toEqual(none());
    });

    it('counts the changes, cards put in and taken out', () => {
      expect(gatheredCount(none())).toBe(0);
      expect(
        gatheredCount(none({ ops: [{ name: 'burdens', args: [2] }], added: [added('g')], removed: ['sam'] })),
      ).toBe(3);
    });

    it('writes a change as the server reads it', () => {
      expect(opText({ name: 'wound', args: ['frodo', 1] })).toBe('wound(frodo, 1)');
      expect(opText({ name: 'order', args: ['freeps.hand', 'sam', 'merry'] })).toBe('order(freeps.hand, sam, merry)');
    });
  });

  describe('gathering a change', () => {
    it('keeps changes in the order they were made', () => {
      expect(gathering(['wound', 'frodo', 1], ['tokens', 'cave', 1], ['move', 'sam', 'freeps.discard'])).toEqual([
        'wound(frodo, 1)',
        'tokens(cave, 1)',
        'move(sam, freeps.discard)',
      ]);
    });

    it('lets an ordering of the same place, or a move of the same card, take the place of the one just before', () => {
      expect(gathering(['order', 'freeps.hand', 'merry', 'sam'], ['order', 'freeps.hand', 'pippin', 'merry'])).toEqual([
        'order(freeps.hand, pippin, merry)',
      ]);
      expect(gathering(['move', 'sam', 'freeps.discard'], ['move', 'SAM', 'freeps.deadPile'])).toEqual([
        'move(SAM, freeps.deadPile)',
      ]);
      // (not when something came between, or it is another place or card)
      expect(
        gathering(['move', 'sam', 'freeps.discard'], ['wound', 'frodo', 1], ['move', 'sam', 'freeps.deadPile']),
      ).toEqual(['move(sam, freeps.discard)', 'wound(frodo, 1)', 'move(sam, freeps.deadPile)']);
      expect(gathering(['order', 'freeps.hand', 'merry'], ['order', 'shadow.hand', 'orc'])).toHaveLength(2);
      expect(gathering(['move', 'sam', 'freeps.discard'], ['move', 'merry', 'freeps.discard'])).toHaveLength(2);
    });

    it('nets wounds and healings of the same card out, one after the other', () => {
      expect(gathering(['wound', 'frodo', 1], ['wound', 'frodo', 1])).toEqual(['wound(frodo, 2)']);
      expect(gathering(['wound', 'frodo', 2], ['heal', 'frodo', 1])).toEqual(['wound(frodo, 1)']);
      expect(gathering(['wound', 'frodo', 1], ['heal', 'frodo', 2])).toEqual(['heal(frodo, 1)']);
      expect(gathering(['heal', 'frodo', 1], ['heal', 'frodo', 1])).toEqual(['heal(frodo, 2)']);
      // (to nothing: gone)
      expect(gathering(['wound', 'frodo', 1], ['heal', 'frodo', 1])).toEqual([]);
      expect(gathering(['burdens', 3], ['wound', 'frodo', 1], ['heal', 'frodo', 1])).toEqual(['burdens(3)']);
      // (another card, or a name written otherwise: apart)
      expect(gathering(['wound', 'frodo', 1], ['wound', 'sam', 1])).toEqual(['wound(frodo, 1)', 'wound(sam, 1)']);
      expect(gathering(['wound', 'frodo', 1], ['wound', 'Frodo', 1])).toHaveLength(2);
      expect(gathering(['wound', 'frodo', 1], ['tokens', 'cave', 1], ['wound', 'frodo', 1])).toHaveLength(3);
    });

    it('adds up tokens on the same card, one after the other', () => {
      expect(gathering(['tokens', 'cave', 1], ['tokens', 'cave', 1], ['tokens', 'CAVE', 1])).toEqual([
        'tokens(CAVE, 3)',
      ]);
      expect(gathering(['tokens', 'cave', 1], ['tokens', 'frodo', 1])).toHaveLength(2);
    });

    it("sets a total once, at what it is to be, and not at all if that is the board's", () => {
      expect(gathering(['burdens', 3], ['wound', 'frodo', 1], ['burdens', 4])).toEqual([
        'wound(frodo, 1)',
        'burdens(4)',
      ]);
      // (the board has 1 burden and 2 twilight)
      expect(gathering(['burdens', 3], ['burdens', 1])).toEqual([]);
      expect(gathering(['twilight', 2])).toEqual([]);
      expect(gathering(['threats', 0], ['threats', 2], ['twilight', 5])).toEqual(['threats(2)', 'twilight(5)']);
      // (with no board to say what it is, it is set)
      expect(gather(none(), null, 'burdens', [1]).ops).toEqual([{ name: 'burdens', args: [1] }]);
    });

    it('changes nothing it is given', () => {
      const before = none({ ops: [{ name: 'wound', args: ['frodo', 1] }] });
      const after = gather(before, board(), 'heal', ['frodo', 1]);
      expect(before.ops).toHaveLength(1);
      expect(after.ops).toHaveLength(0);
    });
  });

  describe('forgetting a card', () => {
    it('drops what was gathered about it: its own changes, what it was put on or had put on it, its place in an order', () => {
      const gathered = none({
        ops: [
          { name: 'wound', args: ['sam', 1] },
          { name: 'heal', args: ['sam', 1] },
          { name: 'tokens', args: ['sam', 1] },
          { name: 'move', args: ['sam', 'freeps.discard'] },
          { name: 'attach', args: ['sam', 'frodo'] },
          { name: 'stack', args: ['cave', 'sam'] },
          { name: 'order', args: ['freeps.hand', 'merry', 'sam', 'pippin'] },
          { name: 'order', args: ['freeps.deck', 'sam', 'merry'] },
          { name: 'order', args: ['freeps.discard', 'sam'] },
          { name: 'wound', args: ['frodo', 1] },
          { name: 'burdens', args: [2] },
        ],
        removed: ['sam'],
      });
      const forgot = forgetCard(gathered, 'sam');
      expect(forgot.ops.map(opText)).toEqual([
        'order(freeps.hand, merry, pippin)',
        'order(freeps.deck, merry)',
        // (an order of no card is no order)
        'wound(frodo, 1)',
        'burdens(2)',
      ]);
      expect(forgot.removed).toEqual(['sam']);
      expect(gathered.ops).toHaveLength(11);
    });
  });

  describe('renaming a card', () => {
    it('has what was gathered about it say its new name', () => {
      const gathered = none({
        ops: [
          { name: 'order', args: ['freeps.hand', 'sam', 'merry'] },
          { name: 'attach', args: ['sam', 'frodo'] },
          { name: 'stack', args: ['cave', 'sam'] },
          { name: 'wound', args: ['sam', 2] },
          { name: 'move', args: ['sam', 'freeps.discard'] },
          { name: 'burdens', args: [2] },
        ],
        removed: ['sam', 'merry'],
        added: [added('gandalf', { zone: 'attach', host: 'sam' })],
      });
      const renamed = renameGathered(gathered, 'sam', 'samwise');
      expect(renamed.ops.map(opText)).toEqual([
        'order(freeps.hand, samwise, merry)',
        'attach(samwise, frodo)',
        'stack(cave, samwise)',
        'wound(samwise, 2)',
        'move(samwise, freeps.discard)',
        'burdens(2)',
      ]);
      expect(renamed.removed).toEqual(['samwise', 'merry']);
      // (as the legacy panel: a card to be added keeps the host it was given)
      expect(renamed.added[0]!.host).toBe('sam');
      // (an order's place is no card's name)
      expect(renameGathered(none({ ops: [{ name: 'order', args: ['x', 'x'] }] }), 'x', 'y').ops[0]!.args).toEqual([
        'x',
        'y',
      ]);
    });
  });

  describe('pruning', () => {
    it('lets go of what is about cards the scenario does not have, and keeps a card put in again in', () => {
      const gathered = none({
        ops: [
          { name: 'wound', args: ['gone', 1] },
          { name: 'wound', args: ['sam', 1] },
          { name: 'attach', args: ['gandalf', 'frodo'] },
          { name: 'stack', args: ['sam', 'gone'] },
          { name: 'order', args: ['freeps.hand', 'sam', 'gone'] },
          { name: 'order', args: ['freeps.hand', 'sam', 'merry'] },
          { name: 'burdens', args: [3] },
        ],
        added: [added('gandalf'), added('merry')],
        removed: ['gone', 'sam', 'merry'],
      });
      const pruned = pruneGathered(gathered, board());
      expect(pruned.ops.map(opText)).toEqual([
        'wound(sam, 1)',
        'attach(gandalf, frodo)',
        'order(freeps.hand, sam, merry)',
        'burdens(3)',
      ]);
      expect(pruned.removed).toEqual(['sam']);
      expect(pruned.added).toHaveLength(2);
    });
  });

  describe('the board as it will be', () => {
    it("is the server's board when nothing has been gathered", () => {
      const view = boardView(board(), none());
      expect(view).toMatchObject({ twilight: 2, burdens: 1, threats: 0 });
      expect(view.cards).toHaveLength(7);
      expect(view.cards.every((each) => !each.touched && !each.added && !each.removing)).toBe(true);
    });

    it('has the cards to be added, where they are put, after what is there; one under a known name takes its place', () => {
      const view = boardView(
        board(),
        none({
          added: [
            added('gandalf', { zone: '' }),
            added('sting', { zone: 'attach', host: 'frodo', type: 'POSSESSION' }),
            added('rope', { zone: 'stack', host: 'cave' }),
            added('sam', { name: 'Sam, Son of Hamfast', blueprintId: '1_311' }),
          ],
        }),
      );
      const of = (alias: string) => view.cards.find((each) => each.alias === alias)!;
      expect(of('gandalf')).toMatchObject({ zone: 'deck', owner: 'freeps', cardId: null, added: true, touched: true });
      expect(of('sting')).toMatchObject({ zone: 'attached', on: 'frodo', type: 'POSSESSION' });
      expect(of('rope')).toMatchObject({ zone: 'stacked', on: 'cave' });
      expect(of('sam')).toMatchObject({ zone: 'hand', name: 'Sam, Son of Hamfast', added: true });
      expect(view.cards.filter((each) => each.alias === 'sam')).toHaveLength(1);
      // (after the hand's own cards)
      expect(
        seatCards(view, 'freeps')
          .filter((each) => each.card.zone === 'hand')
          .map((each) => each.card.alias),
      ).toEqual(['merry', 'pippin', 'sam']);
    });

    it('marks the cards to be taken out', () => {
      const view = boardView(board(), none({ removed: ['sam', 'nobody'] }));
      expect(view.cards.find((each) => each.alias === 'sam')).toMatchObject({ removing: true, touched: true });
    });

    it('makes each change: totals, wounds, healing (not below none), tokens, moves, attaching, stacking', () => {
      const view = boardView(
        board(),
        none({
          ops: [
            { name: 'twilight', args: [5] },
            { name: 'burdens', args: ['x'] },
            { name: 'wound', args: ['frodo', 3] },
            { name: 'heal', args: ['frodo', 1] },
            { name: 'heal', args: ['sam', 2] },
            { name: 'tokens', args: ['cave', 2] },
            { name: 'move', args: ['ring', 'freeps.hand'] },
            { name: 'attach', args: ['merry', 'frodo'] },
            { name: 'stack', args: ['pippin', 'cave'] },
            { name: 'wound', args: ['nobody', 1] },
          ],
        }),
      );
      const of = (alias: string) => view.cards.find((each) => each.alias === alias)!;
      expect(view).toMatchObject({ twilight: 5, burdens: 0, threats: 0 });
      expect(of('frodo')).toMatchObject({ wounds: 2, touched: true });
      expect(of('sam')).toMatchObject({ wounds: 0, touched: true });
      expect(of('cave').tokens).toBe(3);
      expect(of('ring')).toMatchObject({ zone: 'hand', on: null });
      expect(of('merry')).toMatchObject({ zone: 'attached', on: 'frodo' });
      expect(of('pippin')).toMatchObject({ zone: 'stacked', on: 'cave' });
      expect(of('orc').touched).toBe(false);
    });

    it('puts a card moved to the top of a pile before the others, the last put there first', () => {
      const view = boardView(
        board(),
        none({
          ops: [
            { name: 'move', args: ['sam', 'freeps.deck.top'] },
            { name: 'move', args: ['merry', 'freeps.deck.bottom'] },
            { name: 'move', args: ['pippin', 'freeps.deck.top'] },
          ],
        }),
      );
      expect(
        seatCards(view, 'freeps')
          .filter((each) => each.card.zone === 'deck')
          .map((each) => each.card.alias),
      ).toEqual(['pippin', 'sam', 'merry']);
    });

    it('orders a place: the cards named first (only those there), then the rest as they were', () => {
      const view = boardView(
        board(),
        none({ ops: [{ name: 'order', args: ['freeps.hand', 'pippin', 'orc', 'frodo', 'nobody', 'sam'] }] }),
      );
      const hand = seatCards(view, 'freeps').filter((each) => each.card.zone === 'hand');
      expect(hand.map((each) => [each.card.alias, each.card.at, each.card.touched])).toEqual([
        ['pippin', 0, true],
        ['sam', 1, true],
        ['merry', 2, false],
      ]);
    });
  });

  describe("a seat's cards", () => {
    it("come by place, in the list's order, headed by the place and its count; a place it does not know last", () => {
      const view = boardView(board({ cards: [...board().cards, card('odd', { zone: 'void' })] }), none());
      const listed = seatCards(view, 'freeps');
      expect(listed.map((each) => [each.card.alias, each.heading, each.ordered, each.at])).toEqual([
        ['frodo', 'Characters (1)', false, 0],
        ['cave', 'Support area (1)', false, 0],
        ['ring', 'On another card (1)', false, 0],
        ['sam', 'Hand (3)', true, 0],
        ['merry', null, true, 1],
        ['pippin', null, true, 2],
        ['odd', 'void (1)', false, 0],
      ]);
      expect(listed[4]!.among).toEqual(['sam', 'merry', 'pippin']);
      // (one card in a hand is in no order to change)
      expect(seatCards(view, 'shadow').map((each) => [each.card.alias, each.heading, each.ordered])).toEqual([
        ['orc', 'Hand (1)', false],
      ]);
    });

    it('puts a card of an order at another place in it', () => {
      expect(reordered(['a', 'b', 'c'], 2, 0)).toEqual(['c', 'a', 'b']);
      expect(reordered(['a', 'b', 'c'], 0, 1)).toEqual(['b', 'a', 'c']);
    });

    it('can be put on what is on the table (a card on another too), not on a card being taken out', () => {
      const view = boardView(board(), none({ removed: ['frodo'] }));
      expect(bearersOf(view).map((each) => each.alias)).toEqual(['ring', 'cave']);
    });
  });

  describe('names', () => {
    it("of the sites, both seats'", () => {
      expect(siteAliases(board())).toEqual(['site1', 'site2', 'site1opp', 'site2opp']);
      expect(siteAliases(board({ sites: { freeps: [site(1, null)], shadow: [] } }))).toEqual([]);
      expect(siteAliases(null)).toEqual([]);
      expect(defaultSiteAlias('freeps', 'freeps', 3)).toBe('site3');
      expect(defaultSiteAlias('freeps', 'shadow', 3)).toBe('site3opp');
      expect(defaultSiteAlias('shadow', 'freeps', 3)).toBe('site3opp');
    });

    it('taken: the cards the board will have, and the sites', () => {
      const taken = boardAliases(board(), none({ removed: ['sam'], added: [added('gandalf')] }));
      expect(taken).toContain('gandalf');
      expect(taken).not.toContain('sam');
      expect(taken).toContain('site2opp');
    });

    it('suggested for a card found: its first word that says something, free', () => {
      expect(suggestAlias('The One Ring, The Ruling Ring', [])).toBe('one');
      expect(suggestAlias('Palantír of Orthanc', [])).toBe('palantir');
      expect(suggestAlias('A Elbereth Gilthoniel!', [])).toBe('elbereth');
      expect(suggestAlias('Gandalf, Friend of the Shirefolk', ['gandalf', 'gandalf2'])).toBe('gandalf3');
      expect(suggestAlias('7 Rings', [])).toBe('c7');
      expect(suggestAlias('The Of', [])).toBe('card');
      expect(suggestAlias('Úlairë Enquëa', ['ulaire'])).toBe('ulaire2');
    });

    it("for a card to be added: the server's rule, and no site's", () => {
      expect(addAliasProblem('gandalf_2', board())).toBeNull();
      expect(addAliasProblem('old-toby', board())).toBeNull();
      expect(addAliasProblem('2gandalf', board())).toMatch(/letters, digits, dashes and underscores/);
      expect(addAliasProblem('', board())).not.toBeNull();
      expect(addAliasProblem('site2', board())).toBe(
        "site2 is the name of a site of the scenario's: give the card another.",
      );
      expect(addAliasProblem('site9', null)).toBeNull();
    });

    it('for a card renamed', () => {
      expect(renameProblem('samwise', ['sam', 'site1'])).toBeNull();
      expect(renameProblem('site1', ['sam', 'site1'])).toBe('The scenario already has a card called that');
      expect(renameProblem('sam wise', [])).toBe('Letters, digits, dashes and underscores, starting with a letter');
    });
  });

  describe('adding and dropping cards', () => {
    it('adds a card, letting go of what was gathered about one of its name, which is then not taken out', () => {
      const gathered = none({
        ops: [
          { name: 'wound', args: ['sam', 1] },
          { name: 'burdens', args: [2] },
        ],
        removed: ['sam'],
        added: [added('sam', { blueprintId: '1_1' }), added('gandalf')],
      });
      const now = addCard(gathered, added('sam', { blueprintId: '1_311' }));
      expect(now.ops.map(opText)).toEqual(['burdens(2)']);
      expect(now.removed).toEqual([]);
      expect(now.added.map((each) => [each.alias, each.blueprintId])).toEqual([
        ['gandalf', '1_72'],
        ['sam', '1_311'],
      ]);
    });

    it('finds the card a new one would replace, and asks about it', () => {
      const view = boardView(board(), none({ removed: ['merry'], added: [added('gandalf')] }));
      expect(existingCard(view, 'merry')).toBeNull();
      expect(existingCard(view, 'nobody')).toBeNull();
      const sam = existingCard(view, 'sam')!;
      expect(replaceQuestion(sam, { name: 'Sam, Son of Hamfast' })).toBe(
        'sam is already sam (1_1), Free Peoples seat.\n\nSam, Son of Hamfast takes its place: what says sam is then ' +
          'about it, and it goes where you chose.',
      );
      expect(replaceQuestion(existingCard(view, 'gandalf')!, { name: 'X' })).toContain(
        'gandalf is already Gandalf (1_72), Free Peoples seat, waiting to be added.',
      );
      expect(replaceQuestion(existingCard(view, 'orc')!, { name: 'X' })).toContain('Shadow seat');
    });

    it("types a card found as the board's cards are typed", () => {
      expect(boardType('the_one_ring')).toBe('THE_ONE_RING');
      expect(boardType(null)).toBe('');
    });

    it('drops a card: one to be added is not; one the scenario has is to be taken out (once)', () => {
      const gathered = none({ ops: [{ name: 'wound', args: ['sam', 1] }], added: [added('gandalf')] });
      expect(dropCard(gathered, { alias: 'gandalf', added: true }).added).toEqual([]);
      const out = dropCard(gathered, { alias: 'sam', added: false });
      expect(out.removed).toEqual(['sam']);
      expect(out.ops).toEqual([]);
      expect(dropCard(out, { alias: 'sam', added: false }).removed).toEqual(['sam']);
      expect(keepCard(out, 'sam').removed).toEqual([]);
    });

    it('undoes a gathered line', () => {
      const gathered = none({
        ops: [
          { name: 'wound', args: ['gandalf', 1] },
          { name: 'burdens', args: [2] },
        ],
        added: [added('gandalf'), added('sting')],
        removed: ['sam', 'merry'],
      });
      expect(undoRemoved(gathered, 0).removed).toEqual(['merry']);
      const unadded = undoAdded(gathered, 0);
      expect(unadded.added.map((each) => each.alias)).toEqual(['sting']);
      expect(unadded.ops.map(opText)).toEqual(['burdens(2)']);
      expect(undoAdded(gathered, 5)).toBe(gathered);
      expect(undoOp(gathered, 1).ops.map(opText)).toEqual(['wound(gandalf, 1)']);
    });

    it('says a card to be added, and where it goes', () => {
      expect(addedText(added('gandalf'))).toBe('add Gandalf as gandalf (freeps, hand)');
      expect(addedText(added('gandalf', { zone: '' }))).toBe('add Gandalf as gandalf (freeps, deck)');
      expect(addedText(added('gandalf', { zone: 'attach', host: 'frodo' }))).toBe(
        'add Gandalf as gandalf (freeps, on frodo)',
      );
      expect(addedText(added('gandalf', { zone: 'stack', host: 'cave', seat: 'shadow' }))).toBe(
        'add Gandalf as gandalf (shadow, stacked on cave)',
      );
    });
  });

  describe("the scenario's parts a change writes", () => {
    const draft = (extra: Partial<Draft> = {}): Draft => ({
      id: 'test',
      cards: { freeps: { frodo: '10_121', sam: '1_311', merry: '1_303' }, shadow: { orc: '1_191' } },
      setup: {
        freeps: { hand: 'sam, merry', support: 'sam' },
        shadow: { hand: 'orc' },
        attach: { sam: 'frodo', sting: 'frodo' },
        stack: { sam: 'cave' },
        wounds: { sam: 2 },
        tokens: { sam: 1 },
        burdens: 2,
      },
      ...extra,
    });

    it('writes nothing for no cards put in or taken out', () => {
      expect(cardChange(draft(), none({ ops: [{ name: 'burdens', args: [1] }] }))).toBeNull();
    });

    it('takes a card out of its seat and of every place the setup has it', () => {
      const before = draft();
      expect(cardChange(before, none({ removed: ['sam'] }))).toEqual({
        cards: { freeps: { frodo: '10_121', merry: '1_303' }, shadow: { orc: '1_191' } },
        setup: { freeps: { hand: 'merry' }, shadow: { hand: 'orc' }, attach: { sting: 'frodo' }, burdens: 2 },
      });
      expect(before.setup?.wounds).toEqual({ sam: 2 });
      expect(cardChange(draft(), none({ removed: ['orc'] }))!.setup!['shadow']).toBeUndefined();
    });

    it('puts a card in: its seat, its place (after what is there; the bottom of the deck is no place), on a card', () => {
      const change = cardChange(
        draft(),
        none({
          added: [
            added('gandalf', { zone: 'hand' }),
            added('troll', { seat: 'shadow', zone: '' }),
            added('rope', { zone: 'stack', host: 'cave' }),
            added('glamdring', { zone: 'attach', host: 'gandalf' }),
            added('goblin', { seat: 'shadow', zone: 'discard' }),
          ],
        }),
      )!;
      expect(change.cards.freeps).toEqual({
        frodo: '10_121',
        sam: '1_311',
        merry: '1_303',
        gandalf: '1_72',
        rope: '1_72',
        glamdring: '1_72',
      });
      expect(change.cards.shadow).toEqual({ orc: '1_191', troll: '1_72', goblin: '1_72' });
      expect(change.setup).toMatchObject({
        freeps: { hand: 'sam, merry, gandalf', support: 'sam' },
        shadow: { hand: 'orc', discard: 'goblin' },
        stack: { sam: 'cave', rope: 'cave' },
        attach: { sam: 'frodo', sting: 'frodo', glamdring: 'gandalf' },
      });
    });

    it('replaces a card put in under its name, and leaves no setup when none is left', () => {
      const change = cardChange(
        { id: 'test', cards: { freeps: { sam: '1_311' } }, setup: { freeps: { hand: 'sam' } } },
        none({ added: [added('sam', { zone: '', blueprintId: '2_1' })] }),
      );
      expect(change).toEqual({ cards: { freeps: { sam: '2_1' } }, setup: null });
      expect(cardChange({ id: 'test' }, none({ added: [added('g', { seat: 'shadow' })] }))).toEqual({
        cards: { shadow: { g: '1_72' } },
        setup: { shadow: { hand: 'g' } },
      });
    });

    it("makes a card another, keeping its name; nothing for a card the scenario's cards don't name", () => {
      expect(changeCardTop(draft(), { owner: 'freeps', alias: 'sam' }, '2_1')).toEqual({
        cards: { freeps: { frodo: '10_121', sam: '2_1', merry: '1_303' }, shadow: { orc: '1_191' } },
      });
      expect(changeCardTop(draft(), { owner: 'shadow', alias: 'sam' }, '2_1')).toBeNull();
      expect(changeCardTop({ id: 'x' }, { owner: 'shadow', alias: 'sam' }, '2_1')).toBeNull();
    });

    it('writes the paths as lists, or with their names when a site has one of its own', () => {
      const plain = board();
      expect(pathIds(plain, 'freeps')).toEqual(['1_301', '1_302']);
      expect(samePaths(plain)).toBe(true);
      expect(samePaths(board({ sites: { freeps: [site(1, 'site1')], shadow: [] } }))).toBe(false);
      expect(sitesTop(plain, 'freeps', ['1_301', '1_9'], ['1_301', '1_9', '1_10'])).toEqual({
        sites: { freeps: '1_301, 1_9', shadow: '1_301, 1_9, 1_10' },
      });
      const named = board({ sites: { freeps: [site(1, 'shire'), site(2, 'site2')], shadow: [site(1, 'site1opp')] } });
      expect(sitesTop(named, 'freeps', ['1_301', '1_9', '1_10'], ['1_301'])).toEqual({
        sites: { freeps: { shire: '1_301', site2: '1_9', site3: '1_10' }, shadow: '1_301' },
      });
      expect(sitesTop(null, 'shadow', ['a'], ['b'])).toEqual({ sites: { freeps: 'a', shadow: 'b' } });
    });

    it('writes the Ring-bearer or the Ring of a seat (no part when no seat has one)', () => {
      const ringed: Draft = {
        id: 'test',
        ringBearer: { freeps: 'frodo', shadow: 'oppFrodo' },
        ring: { freeps: 'ring' },
      };
      expect(ringTop(ringed, 'ringBearer', 'freeps', 'sam')).toEqual({
        ringBearer: { freeps: 'sam', shadow: 'oppFrodo' },
      });
      expect(ringTop(ringed, 'ring', 'freeps', '')).toEqual({ ring: null });
      expect(ringTop({ id: 'test' }, 'ring', 'shadow', 'oppRing')).toEqual({ ring: { shadow: 'oppRing' } });
      expect(ringed.ring).toEqual({ freeps: 'ring' });
    });

    it("offers a seat's cards of a type for the Ring-bearer and the Ring", () => {
      expect(ringChoices(board(), 'freeps', 'THE_ONE_RING')).toEqual([['ring', 'ring (ring)']]);
      expect(ringChoices(board(), 'shadow', 'COMPANION')).toEqual([]);
    });
  });
});
