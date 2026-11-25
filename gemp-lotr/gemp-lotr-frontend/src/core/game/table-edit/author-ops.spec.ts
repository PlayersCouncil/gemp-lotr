import { boardCard, draftBoard, draftState, MODIFY_WORDS } from '../../../testing/authoring';
import { AUTHOR_TOOLS, authorBoard, OWN_TOKENS } from './author-board';
import { AUTHOR_REFUSALS, AuthorContext, authorStep, authorSteps } from './author-ops';
import { EditBoard } from './edit-board';
import { EditOp } from './edit-ops';

const BOARD: EditBoard = authorBoard({
  state: draftState({
    board: draftBoard({
      cards: [
        ...draftBoard().cards,
        boardCard('sting', {
          blueprintId: '1_313',
          name: 'Sting',
          type: 'POSSESSION',
          cardId: 40,
          zone: 'attached',
          on: 'frodo',
        }),
        boardCard('rb', { cardId: 41, ringBearer: true, owner: 'shadow' }),
        boardCard('oring', { cardId: 42, ring: true, type: 'THE_ONE_RING', owner: 'shadow' }),
        boardCard('cloak', {
          blueprintId: '1_9',
          name: 'Cloak',
          type: 'POSSESSION',
          cardId: 43,
          zone: 'stacked',
          on: 'frodo',
        }),
        boardCard('gone', { cardId: 44, zone: 'removed', at: null }),
        boardCard('deep', { cardId: 45, zone: 'deck', at: 3 }),
      ],
    }),
    modify: MODIFY_WORDS,
  }),
  inSetup: false,
  mode: 'edit',
  tools: AUTHOR_TOOLS,
  cards: {},
  zones: {},
  seats: { human: 'freeps', players: { freeps: 'asdf', shadow: '~Scenario' } },
})!;

const context = (extra: Partial<AuthorContext> = {}): AuthorContext => ({
  board: BOARD,
  inSetup: false,
  realOpening: false,
  sites: draftBoard().sites,
  human: 'freeps',
  markersTogether: false,
  setupTokens: () => 0,
  ...extra,
});
const setup = context({ inSetup: true });

const steps = (op: EditOp, at = context()) => authorStep(at, op);
const queued = (name: string, ...args: (string | number)[]) => ({ steps: [{ kind: 'queue', name, args }] });
const added = (extra: Record<string, unknown>) => ({
  steps: [{ kind: 'add', blueprint: '1_72', seat: 'freeps', host: null, then: null, name: null, type: null, ...extra }],
});

describe("the author's edits, said as the Board tab says them", () => {
  it('adds a card as Add a card does: to a place of the setup, the bottom of the deck being ""', () => {
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.hand' })).toEqual(added({ zone: 'hand' }));
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.support' })).toEqual(added({ zone: 'support' }));
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.deck.bottom' })).toEqual(added({ zone: '' }));
    expect(steps({ op: 'add', blueprint: '1_72', to: 'shadow.minions' })).toEqual(
      added({ seat: 'shadow', zone: 'minions' }),
    );
  });

  it("adds to a place the form has not by adding to the deck's bottom and moving it there", () => {
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.deck.top' })).toEqual(
      added({ zone: '', then: 'freeps.deck.top' }),
    );
    expect(steps({ op: 'add', blueprint: '1_72', to: 'shadow.removed' })).toEqual(
      added({ seat: 'shadow', zone: '', then: 'shadow.removed' }),
    );
  });

  it('adds a card attached to, or stacked on, another, named as the board names it', () => {
    expect(steps({ op: 'add', blueprint: '1_72', attachId: 25 })).toEqual(added({ zone: 'attach', host: 'frodo' }));
    expect(steps({ op: 'add', blueprint: '1_72', stackId: 27, seat: 'shadow' })).toEqual(
      added({ seat: 'shadow', zone: 'stack', host: 'oppFrodo' }),
    );
    expect(steps({ op: 'add', blueprint: '1_72', attachId: 999 })).toEqual({ refused: AUTHOR_REFUSALS.unknown });
  });

  it('refuses what the add-card form cannot do: Free Peoples minions at the start, a deck only with the game’s opening', () => {
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.minions' })).toEqual({
      refused: AUTHOR_REFUSALS.freepsMinions,
    });
    const opening = context({ realOpening: true, inSetup: true });
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.hand' }, opening)).toEqual({
      refused: AUTHOR_REFUSALS.deckOnly,
    });
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.deck.bottom' }, opening)).toEqual(added({ zone: '' }));
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.removed' }, opening)).toEqual({
      refused: AUTHOR_REFUSALS.deckOnly,
    });
    expect(steps({ op: 'add', blueprint: '1_72', to: 'nobody.hand' })).toEqual({ refused: AUTHOR_REFUSALS.place });
    expect(steps({ op: 'add', blueprint: '1_72', to: 'freeps.path' })).toEqual({ refused: AUTHOR_REFUSALS.place });
  });

  it('copies a card as another of it added where it is (the tab has no copy)', () => {
    const known = { name: 'Sam, Son of Hamfast', type: 'COMPANION' };
    expect(steps({ op: 'copy', cardId: 30 })).toEqual(added({ blueprint: '1_311', zone: 'hand', ...known }));
    expect(steps({ op: 'copy', card: 'sting' })).toEqual(
      added({ blueprint: '1_313', zone: 'attach', host: 'frodo', name: 'Sting', type: 'POSSESSION' }),
    );
    expect(steps({ op: 'copy', card: 'cloak' })).toEqual(
      added({ blueprint: '1_9', zone: 'stack', host: 'frodo', name: 'Cloak', type: 'POSSESSION' }),
    );
    expect(steps({ op: 'copy', card: 'deep' })).toMatchObject({ steps: [{ zone: '', then: null }] });
    expect(steps({ op: 'copy', card: 'gone' })).toMatchObject({ steps: [{ zone: '', then: 'freeps.removed' }] });
    expect(steps({ op: 'copy', card: 'oring' })).toEqual({ refused: AUTHOR_REFUSALS.ring });
  });

  it('deletes a card as its × does, but never the Ring-bearer or the Ring', () => {
    expect(steps({ op: 'delete', cardId: 30 })).toEqual({ steps: [{ kind: 'drop', alias: 'sam' }] });
    expect(steps({ op: 'delete', card: 'rb' })).toEqual({ refused: AUTHOR_REFUSALS.ringBearer });
    expect(steps({ op: 'delete', card: 'oring' })).toEqual({ refused: AUTHOR_REFUSALS.ring });
    expect(steps({ op: 'delete', card: 'nobody' })).toEqual({ refused: AUTHOR_REFUSALS.unknown });
  });

  it('moves a card within its own seat, as the move select does', () => {
    expect(steps({ op: 'move', cardId: 30, to: 'freeps.characters' })).toEqual(
      queued('move', 'sam', 'freeps.characters'),
    );
    expect(steps({ op: 'move', cardId: 30, to: 'freeps.deck.top' })).toEqual(queued('move', 'sam', 'freeps.deck.top'));
    expect(steps({ op: 'move', cardId: 30, to: 'shadow.hand' })).toEqual({ refused: AUTHOR_REFUSALS.seat });
    expect(steps({ op: 'move', cardId: 30, to: 'freeps.minions' }, setup)).toEqual({
      refused: AUTHOR_REFUSALS.freepsMinions,
    });
    expect(steps({ op: 'move', cardId: 30, to: 'freeps.hand' }, context({ realOpening: true, inSetup: true }))).toEqual(
      {
        refused: AUTHOR_REFUSALS.deckOnly,
      },
    );
    expect(steps({ op: 'move', cardId: 30, to: 'freeps.nowhere' })).toEqual({ refused: AUTHOR_REFUSALS.place });
    expect(steps({ op: 'move', cardId: 30, to: 'x' })).toEqual({ refused: AUTHOR_REFUSALS.place });
  });

  it('puts a card on another as the put select does', () => {
    expect(steps({ op: 'attach', card: 'sting', toId: 25 })).toEqual(queued('attach', 'sting', 'frodo'));
    expect(steps({ op: 'stack', cardId: 32, toId: 27 })).toEqual(queued('stack', 'orc', 'oppFrodo'));
    expect(steps({ op: 'attach', card: 'sting', toId: 999 })).toEqual({ refused: AUTHOR_REFUSALS.unknown });
    expect(steps({ op: 'attach', card: 'sting', toId: 25 }, context({ realOpening: true, inSetup: true }))).toEqual({
      refused: AUTHOR_REFUSALS.deckOnly,
    });
  });

  it('wounds and heals as − and + do, by the amount', () => {
    expect(steps({ op: 'wounds', cardId: 25, amount: 2 })).toEqual(queued('wound', 'frodo', 2));
    expect(steps({ op: 'wounds', cardId: 25, amount: -1 })).toEqual(queued('heal', 'frodo', 1));
    expect(steps({ op: 'wounds', cardId: 25, amount: 0 })).toEqual({ steps: [] });
  });

  it('puts on tokens of its own culture as +token does; takes them off now, and at the start only what it has', () => {
    expect(steps({ op: 'tokens', cardId: 25, amount: 1, culture: OWN_TOKENS })).toEqual(queued('tokens', 'frodo', 1));
    expect(steps({ op: 'tokens', cardId: 25, amount: 2 })).toEqual(queued('tokens', 'frodo', 2));
    expect(steps({ op: 'tokens', cardId: 25, amount: -1 })).toEqual(queued('tokens', 'frodo', -1));
    expect(steps({ op: 'tokens', cardId: 25, amount: 0 })).toEqual({ steps: [] });
    expect(steps({ op: 'tokens', cardId: 25, amount: 1, culture: 'elven' })).toEqual({
      refused: AUTHOR_REFUSALS.ownTokens,
    });
    expect(steps({ op: 'tokens', cardId: 25, amount: -2 }, setup)).toEqual({ refused: AUTHOR_REFUSALS.startTokens });
    expect(steps({ op: 'tokens', cardId: 25, amount: -2 }, context({ inSetup: true, setupTokens: () => 2 }))).toEqual(
      queued('tokens', 'frodo', -2),
    );
  });

  it('modifies as mod… does (its words, the amount signed, how long), and takes off by kind', () => {
    expect(steps({ op: 'modify', cardId: 25, what: 'strength', amount: 2, until: 'turn' })).toEqual(
      queued('modify', 'frodo', 'strength', '+2', 'turn'),
    );
    expect(steps({ op: 'modify', cardId: 25, what: 'strength', amount: -1 })).toEqual(
      queued('modify', 'frodo', 'strength', '-1'),
    );
    expect(steps({ op: 'modify', cardId: 25, what: 'archer', until: 'phase' })).toEqual(
      queued('modify', 'frodo', 'archer', 'phase'),
    );
    expect(steps({ op: 'modify', cardId: 25, what: 'strength', amount: 0 })).toEqual({
      refused: 'Strength needs the amount it is changed by, other than 0: +2, -1',
    });
    expect(steps({ op: 'modify', cardId: 25, what: 'flying' })).toEqual({ refused: AUTHOR_REFUSALS.word });
    expect(steps({ op: 'unmodify', cardId: 25, what: 'strength' })).toEqual(queued('unmodify', 'frodo', 'strength'));
    expect(steps({ op: 'unmodify', cardId: 25 })).toEqual(queued('unmodify', 'frodo'));
  });

  it('takes a modifier off by its id as unmodify(card, what): the file has no ids', () => {
    const board: EditBoard = {
      ...BOARD,
      cards: BOARD.cards.map((card) =>
        card.alias === 'frodo'
          ? { ...card, modifiers: [{ id: 3, what: 'damage', amount: 1, until: 'removed', text: 'Damage +1' }] }
          : card,
      ),
    };
    expect(steps({ op: 'unmodify', id: 3 }, context({ board }))).toEqual(queued('unmodify', 'frodo', 'damage'));
    expect(steps({ op: 'unmodify', id: 9 }, context({ board }))).toEqual({ refused: AUTHOR_REFUSALS.modifier });
  });

  it('sets a total as Set does: a step by an amount is set to what it comes to, never below 0', () => {
    expect(steps({ op: 'twilight', set: 3 })).toEqual(queued('twilight', 3));
    expect(steps({ op: 'burdens', amount: 1 })).toEqual(queued('burdens', 2));
    expect(steps({ op: 'threats', amount: -1 })).toEqual(queued('threats', 0));
  });

  it('moves a marker, and controls or frees a site, as On the table does', () => {
    expect(steps({ op: 'marker', seat: 'freeps', site: 2 })).toEqual(queued('marker', 'freeps', 2));
    expect(steps({ op: 'marker', seat: 'shadow', site: 2 }, context({ markersTogether: true }))).toEqual({
      refused: AUTHOR_REFUSALS.together,
    });
    expect(steps({ op: 'marker', seat: 'freeps', site: 2 }, context({ realOpening: true, inSetup: true }))).toEqual({
      refused: AUTHOR_REFUSALS.deckOnly,
    });
    expect(steps({ op: 'control', site: 1, seat: 'shadow' })).toEqual(queued('control', 1, 'shadow'));
    expect(steps({ op: 'liberate', site: 1 })).toEqual(queued('liberate', 1));
  });

  it("lays a site at the start as Whose sites does; a seat's site by its blueprint as the path's Change does", () => {
    expect(steps({ op: 'site', number: 2, from: 'shadow' }, setup)).toEqual({
      steps: [{ kind: 'path', number: 2, alias: 'site2opp' }],
    });
    expect(steps({ op: 'site', number: 2, from: 'freeps' }, setup)).toEqual({
      steps: [{ kind: 'path', number: 2, alias: '' }],
    });
    expect(steps({ op: 'site', number: 3, card: 'site3opp' }, setup)).toEqual({
      steps: [{ kind: 'path', number: 3, alias: 'site3opp' }],
    });
    expect(steps({ op: 'site', number: 7, from: 'shadow' }, setup)).toEqual({ refused: AUTHOR_REFUSALS.noSite });
    expect(steps({ op: 'site', number: 2, card: 'nowhere' }, setup)).toEqual({ refused: AUTHOR_REFUSALS.noSite });
    expect(steps({ op: 'site', number: 2, from: 'shadow' })).toEqual({ refused: AUTHOR_REFUSALS.laidNow });
    expect(steps({ op: 'site', number: 2, from: 'shadow' }, context({ inSetup: true, realOpening: true }))).toEqual({
      refused: AUTHOR_REFUSALS.deckOnly,
    });
    expect(steps({ op: 'site', number: 2, blueprint: '1_999' })).toEqual({
      steps: [{ kind: 'site', seat: 'freeps', at: 1, blueprint: '1_999' }],
    });
    expect(steps({ op: 'site', number: 4, blueprint: '1_999', seat: 'shadow' })).toEqual({
      steps: [{ kind: 'site', seat: 'shadow', at: 3, blueprint: '1_999' }],
    });
    expect(steps({ op: 'site', number: 5, blueprint: '1_999' })).toEqual({ refused: AUTHOR_REFUSALS.siteOrder });
  });

  it("refuses a puzzle's pick, and any edit of a slot's card", () => {
    expect(steps({ op: 'pick', slot: 'x', blueprint: '1_1' })).toEqual({ refused: AUTHOR_REFUSALS.pick });
    const board: EditBoard = {
      ...BOARD,
      cards: [...BOARD.cards, { ...BOARD.cards[0]!, alias: 'ally', cardId: '60', slot: 'ally' }],
    };
    expect(steps({ op: 'wounds', cardId: 60, amount: 1 }, context({ board }))).toEqual({
      refused: AUTHOR_REFUSALS.slot,
    });
    expect(steps({ op: 'attach', card: 'sting', toId: 60 }, context({ board }))).toEqual({
      refused: AUTHOR_REFUSALS.slot,
    });
    expect(steps({ op: 'add', blueprint: '1_72', attachId: 60 }, context({ board }))).toEqual({
      refused: AUTHOR_REFUSALS.slot,
    });
  });

  it('makes several edits in order, stops at the first refusal, and sends a path site alone', () => {
    expect(
      authorSteps(context(), [
        { op: 'wounds', cardId: 25, amount: 1 },
        { op: 'twilight', set: 2 },
      ]),
    ).toEqual({
      steps: [
        { kind: 'queue', name: 'wound', args: ['frodo', 1] },
        { kind: 'queue', name: 'twilight', args: [2] },
      ],
    });
    expect(
      authorSteps(context(), [
        { op: 'wounds', cardId: 25, amount: 1 },
        { op: 'pick', slot: 'x', blueprint: null },
      ]),
    ).toEqual({ refused: AUTHOR_REFUSALS.pick });
    expect(
      authorSteps(context(), [
        { op: 'wounds', cardId: 25, amount: 1 },
        { op: 'site', number: 2, blueprint: '1_9' },
      ]),
    ).toEqual({ refused: AUTHOR_REFUSALS.mixed });
  });
});
