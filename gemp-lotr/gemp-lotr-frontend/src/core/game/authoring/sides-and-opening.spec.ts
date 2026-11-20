/**
 * The pure logic the last UI pass added to files the earlier passes made (two-sided scenarios, A3; the game's own
 * opening, B10; the loose ends): the When composer's role and the opening's decisions, the Timeline, the panel's
 * where-line, a refusal's decision number, where the Board tab lets a card go, a change of sides, a closing message's
 * condition as the file has it, and the opening step.
 */
import { decision, draftState, position } from '../../../testing/authoring';
import { DraftState, DraftStep } from './authoring-state';
import { addPlacesFor, movesFor } from './board-queue';
import { START_DEFAULT } from './board-start';
import { leftoversLost } from './game-rules';
import { decisionWhere, roleWords, turnWords, whereLine } from './notices';
import { refusesAt, withRefusalAt } from './refusals';
import { working } from './step-model';
import { mayAddOpening } from './step-ops';
import {
  askedOf,
  nextUntil,
  opensTheGame,
  pickersOf,
  putAsked,
  putTerm,
  rank,
  roleOf,
  whenFacts,
  whenParts,
} from './terms';
import { passesByItself, placeOf, stepRole, timelineEnd, timelineItems, turnOwner } from './timeline';

/** A two-sided draft (the person in the Free Peoples seat unless said), its steps those given. */
function twoSided(steps: DraftStep[], extra: Partial<DraftState> = {}): DraftState {
  return draftState({
    twoSided: true,
    ...extra,
    draft: { id: 'x', human: 'freeps', steps, ...extra.draft },
  });
}

describe('the When composer: the role (A3)', () => {
  it('puts as(...) after the turn, as the server writes it, and reads it back', () => {
    expect(putTerm('human, action, phase(regroup), turn(2)', 'role', 'shadow')).toBe(
      'human, action, phase(regroup), turn(2), as(shadow)',
    );
    expect(putTerm('skirmish(1), human, as(freeps)', 'role', 'shadow')).toBe('human, as(shadow), skirmish(1)');
    expect(putTerm('human, as(shadow)', 'role', '')).toBe('human');
    expect(rank('as(shadow)')).toBe(3.5);
    expect(pickersOf('human, As(Shadow)')?.role).toBe('shadow');
    expect(roleOf('human, as(freeps)')).toBe('freeps');
    expect(roleOf('human, as(both)')).toBeNull();
    expect(roleOf('human')).toBeNull();
    expect(roleOf('{ seat: human }')).toBeNull();
  });

  it('reads `as` from a When written out with requirements', () => {
    expect(whenParts({ seat: 'human', as: 'shadow', require: { type: 'canSpot' } })?.terms).toBe('human, as(shadow)');
  });

  it('knows the draft’s facts: two sides, and the game’s own opening', () => {
    expect(whenFacts(null)).toEqual({ twoSided: false, opening: false });
    expect(whenFacts(draftState({ twoSided: true }))).toEqual({ twoSided: true, opening: false });
    expect(whenFacts(draftState({ draft: { id: 'x', setup: { opening: 'game' } } })).opening).toBe(true);
    expect(whenFacts(draftState({ start: { ...START_DEFAULT, opening: 'game' } })).opening).toBe(true);
  });
});

describe('the opening’s decisions (B10)', () => {
  it('are what is asked, before the phase, and can be negated', () => {
    expect(putAsked('human, number', 'bid', false)).toBe('human, number, bid');
    expect(askedOf('human, not(goFirst)')).toEqual({ asked: 'goFirst', not: true });
    expect(opensTheGame('human, startingFellowship')).toBe(true);
    expect(opensTheGame('human, not(mulligan)')).toBe(false);
    expect(opensTheGame(undefined)).toBe(false);
  });

  it('guess what a repeating one repeats until: the opening’s next decision, the same seat’s; then turn 1', () => {
    expect(nextUntil('human, startingFellowship')).toBe('human, mulligan');
    expect(nextUntil('bid')).toBe('goFirst');
    expect(nextUntil('opponent, mulligan')).toBe('phase(fellowship)');
  });

  it('come before turn 1 in the Timeline, in a group of their own, read as what they are', () => {
    const state = draftState({
      start: { ...START_DEFAULT, opening: 'game' },
      draft: {
        id: 'x',
        human: 'freeps',
        steps: [
          { when: 'freeps, number, bid', allow: 'number(1)' },
          { when: 'shadow, number, bid', answer: 'number(0)' },
          { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass' },
        ],
      },
      places: [{ turn: 0, phase: 'put_ring_bearer' }, null, null],
    });
    expect(placeOf(state.draft.steps![0]!, state.places[0])).toEqual({ turn: 0, phase: 'game-opening' });
    const items = timelineItems(state, { selected: null, sub: null });
    expect(items.filter((item) => item.kind === 'group').map((item) => item.label)).toEqual([
      'The game’s opening',
      'Turn 1 · Fellowship',
    ]);
    const first = items.find((item) => item.kind === 'row');
    expect(first?.kind === 'row' && [first.summary.who, first.summary.when]).toEqual(['You', 'the bid']);
  });

  it('have no opening step before them, and the panel says where the game is in its own words', () => {
    const opening = draftState({
      start: { ...START_DEFAULT, opening: 'game' },
      position: position({ turn: 0, phase: 'put_ring_bearer' }),
      waiting: { human: decision({ where: 'turn 0, put_ring_bearer', kind: 'bid' }), opponent: null },
      draft: { id: 'x', steps: [] },
    });
    expect(mayAddOpening(opening)).toBe(false);
    expect(mayAddOpening(draftState({ draft: { id: 'x', steps: [] } }))).toBe(true);
    expect(mayAddOpening(draftState({ draft: { id: 'x', steps: [], setup: { opening: 'game' } } }))).toBe(false);
    expect(whereLine(opening)).toBe('Step 1 of 0 · the game’s opening');
    expect(decisionWhere(opening, opening.waiting.human!)).toBe('the game’s opening');
    expect(timelineEnd({ ...opening, position: { ...opening.position, inHand: false } }).asked).toBe(
      'You are being asked: Play Fellowship action or Pass (the game’s opening)',
    );
  });
});

describe('a two-sided game in the Timeline and the where-line (A3)', () => {
  const steps: DraftStep[] = [
    { when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass' },
    { when: 'human, action, phase(regroup), turn(2)', allow: 'pass' },
    { when: 'opponent, action, phase(maneuver), turn(2)', answer: 'pass' },
    { when: 'human, as(shadow)', allow: 'pass' },
    { when: 'freeps, action, phase(regroup)', allow: 'pass' },
  ];

  it('says whose turn each turn is, by the seat the story starts with', () => {
    const state = twoSided(steps);
    expect([1, 2, 3, null, 0].map((turn) => turnOwner(state, turn))).toEqual([
      'human',
      'opponent',
      'human',
      null,
      null,
    ]);
    expect(turnOwner(twoSided(steps, { draft: { id: 'x', human: 'shadow' } }), 1)).toBe('opponent');
    expect(turnOwner(twoSided(steps, { start: { ...START_DEFAULT, turn: 4 } }), 5)).toBe('opponent');
    expect(turnOwner(draftState(), 1)).toBeNull();
  });

  it('gives each step its seat’s role: its as(...), else by whose turn it comes in', () => {
    const state = twoSided(steps);
    expect(stepRole(state, steps[0]!, 1)).toBe('freeps');
    expect(stepRole(state, steps[1]!, 2)).toBe('shadow');
    expect(stepRole(state, steps[2]!, 2)).toBe('freeps');
    expect(stepRole(state, steps[3]!, null)).toBe('shadow');
    expect(stepRole(state, { when: 'action', allow: 'pass' }, 1)).toBeNull();
    expect(stepRole(draftState(), steps[1]!, 2)).toBeNull();
  });

  it('heads each group with whose turn it is, and marks a row whose seat’s name does not say its role', () => {
    const items = timelineItems(twoSided(steps), { selected: null, sub: null });
    expect(items.filter((item) => item.kind === 'group').map((item) => item.label)).toEqual([
      'Turn 1 (yours) · Fellowship',
      'Turn 2 (theirs) · Regroup',
      'Turn 2 (theirs) · Maneuver',
      'Turn 2 (theirs) · Regroup',
    ]);
    const roles = items.flatMap((item) => (item.kind === 'row' ? [item.role] : []));
    // (the person as Shadow on turn 2; the opponent as Free Peoples there; the freeps seat as Shadow on turn 2)
    expect(roles).toEqual([null, 'shadow', 'freeps', 'shadow', 'shadow']);
  });

  it('says whose turn it is in the where-line, and the person’s role where their seat does not', () => {
    const theirs = twoSided(steps, {
      position: position({
        turn: 2,
        phase: 'shadow',
        turnOf: 'opponent',
        roles: { human: 'shadow', opponent: 'freeps' },
      }),
    });
    expect(whereLine(theirs)).toBe('Step 1 of 5 · turn 2, shadow · their turn, you as Shadow');
    expect(
      turnWords({
        ...theirs,
        position: { ...theirs.position, turnOf: 'human', roles: { human: 'freeps', opponent: 'shadow' } },
      }),
    ).toBe(' · your turn');
    expect(turnWords(draftState())).toBe('');
    expect(roleWords(theirs, decision({ role: 'shadow' }))).toBe(' · as Shadow');
    expect(roleWords(theirs, decision({ role: 'freeps' }))).toBe(' · as Free Peoples');
    expect(roleWords(draftState(), decision({ role: 'shadow' }))).toBe('');
    expect(decisionWhere(theirs, decision({ role: 'shadow' }))).toBe('turn 1, fellowship · as Shadow');
  });

  it('passes the person through a phase by the role they have at the step (per-role autoPass)', () => {
    const silent: DraftStep[] = [
      { when: 'freeps, action, phase(regroup), turn(1)', allow: 'pass' },
      { when: 'freeps, action, phase(regroup), turn(2)', allow: 'pass' },
      { when: 'freeps, action, phase(regroup), as(shadow)', allow: 'pass' },
      { when: 'freeps, action, phase(maneuver)', allow: 'pass' },
    ];
    const state = twoSided(silent, {
      draft: { id: 'x', human: 'freeps', options: { autoPass: { freeps: false, shadow: 'regroup' } } },
    });
    expect(silent.map((step) => passesByItself(state, step))).toEqual([false, true, true, false]);
    // (one list holds in both roles)
    const list = twoSided(silent, { draft: { id: 'x', human: 'freeps', options: { autoPass: 'regroup' } } });
    expect(silent.map((step) => passesByItself(list, step))).toEqual([true, true, true, false]);
    // (one-sided: the person's seat's own role)
    const one = draftState({ draft: { id: 'x', options: { autoPass: { freeps: 'regroup' } }, steps: silent } });
    expect(passesByItself(one, silent[0]!)).toBe(true);
  });
});

describe('the loose ends', () => {
  it('a refusal at the decision in hand of a step that repeats is numbered, once', () => {
    expect(withRefusalAt('pass', 'choose(x)', 2)).toBe('pass\n2: choose(x)');
    expect(withRefusalAt('2: choose(x)', 'choose(x)', 2)).toBe('2: choose(x)');
    expect(withRefusalAt('2: choose(x)', 'choose(x)', 3)).toBe('2: choose(x)\n3: choose(x)');
    expect(withRefusalAt('choose(x)', 'choose(x)', 3)).toBe('choose(x)');
    expect(withRefusalAt(undefined, 'pass', 0)).toBe('pass');
    expect(refusesAt('2: pass', 'pass', 0)).toBe(true);
    expect(refusesAt('2: pass', 'pass', 1)).toBe(false);
  });

  it('the Board tab offers no Free Peoples minions for the setup, and only a deck for the game’s own opening', () => {
    expect(movesFor('freeps', true, false)).not.toContain('minions');
    expect(movesFor('freeps', false, false)).toContain('minions');
    expect(movesFor('shadow', true, false)).toContain('minions');
    expect(movesFor('shadow', true, true)).toEqual(['deck.top', 'deck.bottom']);
    expect(addPlacesFor('freeps', false, false).map(([zone]) => zone)).not.toContain('minions');
    expect(addPlacesFor('shadow', false, true).map(([zone]) => zone)).toEqual(
      expect.arrayContaining(['minions', 'attach', 'stack']),
    );
    expect(addPlacesFor('shadow', true, true)).toEqual([['', 'deck (bottom)']]);
  });

  it('a change of sides says which leftovers the server would refuse', () => {
    const draft = {
      id: 'x',
      human: 'shadow' as const,
      cards: { freeps: { frodo: '1' }, shadow: { gollum: '2' } },
      opponent: { leftovers: 'frodo' },
    };
    expect(leftoversLost(draft, false)).toBe('frodo');
    expect(leftoversLost(draft, true)).toBeNull();
    expect(leftoversLost({ ...draft, opponent: { leftovers: 'gollum' } }, true)).toBe('gollum');
    expect(leftoversLost({ ...draft, opponent: { leftovers: 'ringBearer' } }, false)).toBeNull();
    expect(leftoversLost({ id: 'x' }, false)).toBeNull();
  });

  it('a closing message’s condition is edited as the server gives its text, else as JSON', () => {
    const step: DraftStep = {
      when: 'freeps',
      end: { result: 'complete', messages: ['Done.', { text: 'With him.', require: { type: 'canSpot' } }] },
    };
    expect(working(step, { 'end.messages.1.require': '{\n\ttype: canSpot\n}' }).end.messages[1]?.require).toBe(
      '{\n\ttype: canSpot\n}',
    );
    expect(working(step).end.messages[1]?.require).toBe('{\n\t"type": "canSpot"\n}');
  });
});
