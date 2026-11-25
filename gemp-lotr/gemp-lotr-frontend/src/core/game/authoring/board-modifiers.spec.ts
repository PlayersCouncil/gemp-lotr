import { MODIFY_WORDS } from '../../../testing/authoring';
import {
  defaultAmount,
  modifierText,
  modifyArgs,
  shownModifiers,
  UNTIL_CHOICES,
  untilMark,
  unmodifyArgs,
  wordsFor,
} from './board-modifiers';
import { opText } from './board-queue';
import { ModifyWord } from './puzzle-state';

const word = (what: string): ModifyWord => MODIFY_WORDS.words.find((one) => one.what === what)!;

describe('board-modifiers (em T5)', () => {
  it('offers the words that suit the card’s type, stats first', () => {
    expect(wordsFor(MODIFY_WORDS, 'COMPANION').map((one) => one.what)).toEqual([
      'strength',
      'resistance',
      'cost',
      'damage',
      'archer',
    ]);
    expect(wordsFor(MODIFY_WORDS, 'MINION').map((one) => one.what)).toEqual([
      'strength',
      'cost',
      'damage',
      'archer',
      'fierce',
    ]);
    expect(wordsFor(MODIFY_WORDS, 'SITE')).toEqual([]);
    expect(wordsFor(undefined, 'COMPANION')).toEqual([]);
    // (a vocabulary whose keyword comes before its stat is still offered stats first)
    const mixed = { words: [word('fierce'), word('strength')], until: [] };
    expect(wordsFor(mixed, 'MINION').map((one) => one.what)).toEqual(['strength', 'fierce']);
  });

  it('starts a stat at +1, a numbered keyword at 1, the others at nothing', () => {
    expect(defaultAmount(word('strength'))).toBe('+1');
    expect(defaultAmount(word('damage'))).toBe('1');
    expect(defaultAmount(word('fierce'))).toBe('');
    expect(defaultAmount(undefined)).toBe('');
  });

  it('writes the board change as the notes do: modify(card, what, ±n, until)', () => {
    const text = (made: ReturnType<typeof modifyArgs>) =>
      'args' in made ? opText({ name: 'modify', args: made.args }) : made.problem;
    expect(text(modifyArgs('aragorn', word('strength'), '+2', 'turn'))).toBe('modify(aragorn, strength, +2, turn)');
    expect(text(modifyArgs('aragorn', word('strength'), '2', ''))).toBe('modify(aragorn, strength, +2)');
    expect(text(modifyArgs('aragorn', word('cost'), ' -1 ', 'phase'))).toBe('modify(aragorn, cost, -1, phase)');
    expect(text(modifyArgs('orc1', word('fierce'), 'ignored', 'phase'))).toBe('modify(orc1, fierce, phase)');
    expect(text(modifyArgs('gimli', word('damage'), '1', ''))).toBe('modify(gimli, damage, +1)');
    expect(text(modifyArgs('gimli', word('archer'), '', ''))).toBe('modify(gimli, archer)');
  });

  it('refuses a stat changed by 0 or nothing, and a numbered keyword under 1', () => {
    expect(modifyArgs('a', word('strength'), '0', '')).toEqual({
      problem: 'Strength needs the amount it is changed by, other than 0: +2, -1',
    });
    expect(modifyArgs('a', word('strength'), 'x', '')).toHaveProperty('problem');
    expect(modifyArgs('a', word('damage'), '0', '')).toEqual({ problem: "Damage's number is 1 or more" });
    expect(modifyArgs('a', word('damage'), '-2', '')).toHaveProperty('problem');
  });

  it('takes them off: of one kind, or all of the card’s', () => {
    expect(opText({ name: 'unmodify', args: unmodifyArgs('aragorn', 'strength') })).toBe('unmodify(aragorn, strength)');
    expect(opText({ name: 'unmodify', args: unmodifyArgs('aragorn', null) })).toBe('unmodify(aragorn)');
  });

  it('shows what is in force at the board as it stands, the setup’s at the start, then those gathered', () => {
    const inForce = [
      { id: 1, what: 'strength', amount: 2, until: 'removed', text: 'Strength +2', written: 'strength +2' },
      { id: 3, what: 'fierce', amount: null, until: 'phase', text: 'Fierce', written: 'fierce phase' },
    ];
    const setup = [{ what: 'damage', amount: 1, until: 'turn', written: 'damage +1 turn' }];
    const gathered = [
      { name: 'wound', args: ['aragorn', 1] },
      { name: 'modify', args: ['aragorn', 'cost', '-1', 'turn'] },
      { name: 'modify', args: ['gimli', 'archer'] },
      { name: 'modify', args: ['aragorn', 'archer'] },
      { name: 'unmodify', args: ['aragorn', 'strength'] },
    ];
    expect(shownModifiers(MODIFY_WORDS, inForce, setup, false, gathered, 'aragorn')).toEqual([
      { what: 'strength', text: 'Strength +2', until: 'removed', op: null, removing: true },
      { what: 'fierce', text: 'Fierce', until: 'phase', op: null, removing: false },
      { what: 'cost', text: 'Twilight cost -1', until: 'turn', op: 1, removing: false },
      { what: 'archer', text: 'Archer', until: 'removed', op: 3, removing: false },
    ]);
    expect(
      shownModifiers(MODIFY_WORDS, inForce, setup, true, [{ name: 'unmodify', args: ['aragorn'] }], 'aragorn'),
    ).toEqual([{ what: 'damage', text: 'Damage +1', until: 'turn', op: null, removing: true }]);
    expect(shownModifiers(undefined, undefined, undefined, true, [], 'aragorn')).toEqual([]);
    expect(shownModifiers(undefined, undefined, undefined, false, [], 'aragorn')).toEqual([]);
  });

  it('says a modifier with the vocabulary’s label, and marks one that runs out', () => {
    expect(modifierText(MODIFY_WORDS, 'strength', -1)).toBe('Strength -1');
    expect(modifierText(undefined, 'glow', null)).toBe('glow');
    expect(untilMark('turn')).toBe('(turn)');
    expect(untilMark('phase')).toBe('(phase)');
    expect(untilMark('removed')).toBe('');
    expect(UNTIL_CHOICES.map(([value]) => value)).toEqual(['', 'turn', 'phase']);
  });
});
