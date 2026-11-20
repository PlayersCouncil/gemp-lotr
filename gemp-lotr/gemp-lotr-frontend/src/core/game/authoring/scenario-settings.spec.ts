import { draftState } from '../../../testing/authoring';
import { Draft } from './authoring-state';
import {
  aboutText,
  aboutValue,
  autoAcceptValue,
  autoPassOf,
  bubbleGapValue,
  checkReport,
  commentsNote,
  COMPANION_LOST,
  FILE_PARTS,
  hasPart,
  kindOf,
  loseChoiceOf,
  loseOptions,
  movesNote,
  offScriptReport,
  optionsWith,
  optionsWithAutoPass,
  outcomeWithLose,
  outcomeWithMessage,
  pathLine,
  rebuild,
  revealByKind,
  revealValue,
  ruleNumber,
  rulesWithOneSided,
  rulesWithSeat,
  seatRulesOf,
  skipWith,
  SPEEDS,
  speedValue,
} from './scenario-settings';
import { settingsOf } from './steps';

const draft = (extra: Partial<Draft> = {}): Draft => ({ id: 'test', title: 'A Test', kind: 'tutorial', ...extra });
const modifier = (m: Record<string, unknown>) => ({ type: 'modifier', modifier: m });

describe('the Scenario tab', () => {
  describe('the file', () => {
    it('says where it is, whether it is public, and whether it is saved', () => {
      expect(pathLine(draftState())).toBe('tutorials/test.hjson (public) — saved');
      expect(pathLine(draftState({ canonical: false, dirty: true }))).toBe(
        'tutorials/test.hjson — changed since it was saved',
      );
      expect(pathLine(draftState({ onDisk: false, dirty: true, path: null }))).toBe(' (public) — not saved yet');
    });

    it('says what saving would drop, and where it would move the file', () => {
      expect(commentsNote(draftState())).toBeNull();
      expect(commentsNote(draftState({ commentsLost: 1 }))?.text).toBe('Saving drops 1 line of comment.');
      expect(commentsNote(draftState({ commentsLost: 3 }))?.text).toBe('Saving drops 3 lines of comment.');
      expect(movesNote(draftState())).toBeNull();
      expect(movesNote(draftState({ onDisk: false, fileOnSave: 'puzzles/test.hjson' }))).toBeNull();
      const state = draftState({ fileOnSave: 'puzzles/test.hjson', kindFolder: 'puzzles' });
      expect(movesNote({ ...state, draft: { ...state.draft, kind: 'puzzle' } })).toEqual({
        text: 'Saving moves it to puzzles/test.hjson.',
        hint: 'A puzzle is kept under puzzles/.',
      });
      expect(movesNote({ ...state, draft: { id: 'x' } })?.hint).toBe('A tutorial is kept under puzzles/.');
    });

    it('reports a check and other answers tried', () => {
      expect(checkReport({ passed: false, report: null, warnings: [] })).toBe('It does not play through.\n\n');
      expect(offScriptReport({ tries: 1, handled: true, report: null })).toBe('Every other answer is handled.\n\n');
    });
  });

  describe('about it', () => {
    it('sends blank as nothing, and the version as a number', () => {
      expect(aboutValue('title', '')).toBeNull();
      expect(aboutValue('tags', 'a, b')).toBe('a, b');
      expect(aboutValue('version', '3')).toBe(3);
      expect(aboutValue('version', 'three')).toBe(1);
      expect(aboutText(draft({ version: 2 }), 'version')).toBe('2');
      expect(aboutText(draft(), 'summary')).toBe('');
    });
  });

  describe('how it plays', () => {
    it('ticks the phases the player is passed through', () => {
      expect(autoPassOf(undefined)).toEqual([]);
      expect(autoPassOf({ autoPass: true })).toEqual([]);
      expect(autoPassOf({ autoPass: 'fellowship, regroup' })).toEqual(['fellowship', 'regroup']);
      const passing = draft({ options: { autoPass: 'fellowship', bubbleGap: '2' } });
      expect(optionsWithAutoPass(passing, 'regroup', true)).toEqual({
        autoPass: 'fellowship, regroup',
        bubbleGap: '2',
      });
      expect(optionsWithAutoPass(passing, 'fellowship', false)).toEqual({ bubbleGap: '2' });
      expect(optionsWithAutoPass(draft({ options: { autoPass: 'fellowship' } }), 'fellowship', false)).toBeNull();
    });

    it("fixes the player's own settings for the game, writing only what is not the default", () => {
      expect(autoAcceptValue(true)).toBeNull();
      expect(autoAcceptValue(false)).toBe(false);
      expect(revealByKind(draft())).toBe(true);
      expect(revealByKind(draft({ kind: undefined }))).toBe(true);
      expect(revealByKind(draft({ kind: 'puzzle' }))).toBe(false);
      expect(revealValue(draft(), true)).toBeNull();
      expect(revealValue(draft(), false)).toBe(false);
      expect(revealValue(draft({ kind: 'puzzle' }), true)).toBe(true);
      expect(revealValue(draft({ kind: 'puzzle' }), false)).toBeNull();
      expect(optionsWith(draft(), 'autoAccept', false)).toEqual({ autoAccept: false });
      expect(optionsWith(draft({ options: { autoAccept: false } }), 'autoAccept', null)).toBeNull();
      // (and the game plays by them: settingsOf)
      expect(settingsOf(draft({ kind: 'puzzle', options: { autoAccept: false, animationSpeed: -2 } }))).toEqual({
        autoAccept: false,
        autoDismissDeckReveal: false,
        animationSpeed: -2,
      });
    });

    it("offers the animation speeds as the slider's steps, or the player's own", () => {
      expect(SPEEDS).toHaveLength(10);
      expect(SPEEDS[0]).toEqual(['', "player's own"]);
      expect(SPEEDS.find(([value]) => value === '2')?.[1]).toBe('+2: 4× faster');
      expect(SPEEDS.find(([value]) => value === '-1')?.[1]).toBe('-1: 2× slower');
      expect(SPEEDS.find(([value]) => value === '0')?.[1]).toBe('0: usual');
      expect(speedValue('')).toBeNull();
      expect(speedValue('-3')).toBe(-3);
    });

    it('takes a blank bubble gap as the stock one', () => {
      expect(bubbleGapValue('  ')).toBeNull();
      expect(bubbleGapValue(' 20px ')).toBe('20px');
    });
  });

  describe('rules', () => {
    const hand = modifier({ type: 'modifyHandSize', amount: -3 });
    const skip = modifier({ type: 'skipPhase', phase: 'MANEUVER' });
    const ring = modifier({ type: 'disableGameText', filter: 'name(The One Ring)' });
    const roaming = modifier({ type: 'modifyRoamingPenalty', filter: 'minion', amount: '-2' });
    const other = modifier({ type: 'modifyStrength', filter: 'frodo', amount: 1 });

    it('knows the usual effects, and nothing that only looks like one', () => {
      expect([hand, skip, ring, roaming].map(kindOf)).toEqual(['hand', 'skip', 'ring', 'roaming']);
      expect(kindOf(other)).toBeNull();
      expect(kindOf({ ...hand, extra: 1 })).toBeNull();
      expect(kindOf({ type: 'trigger' })).toBeNull();
      expect(kindOf({ type: 'modifier', modifier: 'x' })).toBeNull();
      expect(kindOf(modifier({ type: 'modifyHandSize', amount: 1, filter: 'x' }))).toBeNull();
      expect(kindOf(modifier({ type: 'disableGameText', filter: 'frodo' }))).toBeNull();
      expect(kindOf(modifier({ type: 'modifyRoamingPenalty', filter: 'orc', amount: 1 }))).toBeNull();
      expect(kindOf(null)).toBeNull();
      expect(kindOf([hand])).toBeNull();
    });

    it("reads a seat's rules, adding up what is said twice and keeping the rest", () => {
      const rules = { shadow: [hand, skip, other, modifier({ type: 'modifyHandSize', amount: 1 }), ring, roaming] };
      expect(seatRulesOf(rules, 'shadow')).toEqual({
        hand: -2,
        skip: ['maneuver'],
        ring: true,
        roaming: -2,
        others: [other],
      });
      expect(seatRulesOf(undefined, 'freeps')).toEqual({ hand: 0, skip: [], ring: false, roaming: 0, others: [] });
      expect(seatRulesOf({ freeps: [modifier({ type: 'modifyHandSize', amount: 'x' })] }, 'freeps').hand).toBe(0);
    });

    it('writes them back in order, the rest after', () => {
      expect(rebuild({ hand: 1, skip: ['regroup', 'fellowship'], ring: true, roaming: 2, others: [other] })).toEqual([
        modifier({ type: 'modifyHandSize', amount: 1 }),
        ring,
        modifier({ type: 'skipPhase', phase: 'fellowship' }),
        modifier({ type: 'skipPhase', phase: 'regroup' }),
        modifier({ type: 'modifyRoamingPenalty', filter: 'minion', amount: 2 }),
        other,
      ]);
      expect(rebuild({ hand: 0, skip: [], ring: false, roaming: 0, others: [] })).toEqual([]);
    });

    it('changes a seat, and takes the part out when nothing is left', () => {
      const changed = rulesWithSeat(draft(), 'shadow', { skip: ['maneuver'] });
      expect(changed).toEqual({ shadow: [modifier({ type: 'skipPhase', phase: 'maneuver' })] });
      expect(rulesWithSeat(draft({ rules: changed! }), 'shadow', { skip: [] })).toBeNull();
      expect(rulesWithSeat(draft({ rules: { oneSided: true, freeps: [hand] } }), 'freeps', { hand: 0 })).toEqual({
        oneSided: true,
      });
      expect(rulesWithSeat(draft({ rules: { shadow: [other] } }), 'shadow', { ring: true })).toEqual({
        shadow: [ring, other],
      });
    });

    it('makes it one-sided or not', () => {
      expect(rulesWithOneSided(draft(), true)).toEqual({ oneSided: true });
      expect(rulesWithOneSided(draft({ rules: { oneSided: true } }), false)).toBeNull();
      expect(rulesWithOneSided(draft({ rules: { oneSided: true, freeps: [hand] } }), false)).toEqual({
        freeps: [hand],
      });
    });

    it('ticks a skipped phase, and reads a number typed', () => {
      const known = seatRulesOf({ freeps: [skip] }, 'freeps');
      expect(skipWith(known, 'archery', true)).toEqual(['maneuver', 'archery']);
      expect(skipWith(known, 'maneuver', false)).toEqual([]);
      expect(ruleNumber('-3')).toBe(-3);
      expect(ruleNumber('')).toBe(0);
    });
  });

  describe('how it is lost', () => {
    it('knows how the player loses, offering "as the file says" only when it does', () => {
      expect(loseChoiceOf(draft())).toBe('');
      expect(loseChoiceOf(draft({ outcome: { lose: { ...COMPANION_LOST } } }))).toBe('companion');
      expect(loseChoiceOf(draft({ outcome: { lose: [COMPANION_LOST] } }))).toBe('other');
      expect(loseOptions('').map(([value]) => value)).toEqual(['', 'companion']);
      expect(loseOptions('other').map(([value]) => value)).toEqual(['', 'companion', 'other']);
    });

    it('changes how the player loses; "as the file says" changes nothing', () => {
      expect(outcomeWithLose(draft(), 'companion')).toEqual({ lose: COMPANION_LOST });
      expect(outcomeWithLose(draft({ outcome: { lose: COMPANION_LOST, loseMessage: 'x' } }), '')).toEqual({
        loseMessage: 'x',
      });
      expect(outcomeWithLose(draft({ outcome: { lose: COMPANION_LOST } }), '')).toBeNull();
      expect(outcomeWithLose(draft(), 'other')).toBeUndefined();
    });

    it('says what the player is told, or nothing of its own', () => {
      expect(outcomeWithMessage(draft(), 'Too bad.')).toEqual({ loseMessage: 'Too bad.' });
      expect(outcomeWithMessage(draft({ outcome: { loseMessage: 'x' } }), '  ')).toBeNull();
    });
  });

  it('offers the rest of the file part by part, saying which the file has', () => {
    expect(FILE_PARTS).toContain('setup');
    expect(FILE_PARTS).not.toContain('steps');
    expect(hasPart(draft({ rng: 'a' }), 'rng')).toBe(true);
    expect(hasPart(draft(), 'setup')).toBe(false);
  });
});
