import { readEditBoard } from './edit-board';
import { COUNTER_LABELS, counterValue, EDIT_COUNTERS, setCounter, stepCounter } from './edit-counters';
import { SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';

describe('edit counters', () => {
  it('reads each counter from the board (0 before it is read), and names it in a word', () => {
    const board = readEditBoard(SANDBOX_PILES_STATE);
    expect(EDIT_COUNTERS.map((counter) => counterValue(board, counter))).toEqual([3, 1, 0]);
    expect(counterValue(null, 'twilight')).toBe(0);
    expect(EDIT_COUNTERS.map((counter) => COUNTER_LABELS[counter])).toEqual(['Twilight', 'Burdens', 'Threats']);
  });

  it('sets a counter to a whole number from 0 to 99, and nothing else', () => {
    expect(setCounter('twilight', ' 7 ')).toEqual({ op: 'twilight', set: 7 });
    expect(setCounter('burdens', '0')).toEqual({ op: 'burdens', set: 0 });
    expect(setCounter('threats', '99')).toEqual({ op: 'threats', set: 99 });
    for (const text of ['', '100', '-1', '2.5', 'three']) expect(setCounter('twilight', text)).toBeNull();
  });

  it('steps a counter by one, never below 0', () => {
    expect(stepCounter('twilight', 1, 0)).toEqual({ op: 'twilight', amount: 1 });
    expect(stepCounter('burdens', -1, 2)).toEqual({ op: 'burdens', amount: -1 });
    expect(stepCounter('threats', -1, 0)).toBeNull();
  });
});
