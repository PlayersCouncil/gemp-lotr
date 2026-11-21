import { opponentOrder, opponentState } from '../../../testing/authoring';
import { OpponentAnswering } from './authoring-state';
import {
  answeringLine,
  BLANK_ORDER,
  changeOps,
  deleteOps,
  emptyMemory,
  fallbackOptions,
  fallbackPatch,
  formChanged,
  formOf,
  insertOps,
  leftoversOptions,
  leftoversPatch,
  moveOps,
  orderOf,
  placeAfterDelete,
  placeAfterMove,
  placeForAnswering,
  playsPatch,
  playsWith,
  recallMemory,
  refOf,
  refusedOrder,
} from './opponent-orders';

const order = opponentOrder;

const answering = (extra: Partial<OpponentAnswering> = {}): OpponentAnswering => ({
  order: null,
  id: null,
  answer: null,
  gives: null,
  rule: null,
  when: 'shadow, assign, phase(assignment), turn(1)',
  ...extra,
});

describe('opponent-orders', () => {
  it('names an order by its id, else by its place', () => {
    expect(refOf(order(2, { id: 'first' }))).toEqual({ id: 'first' });
    expect(refOf(order(2))).toEqual({ index: 2 });
  });

  it("makes a form of an order's parts, and says when it differs", () => {
    const one = order(0, { id: 'a', note: 'n', once: true, answers: ['play(orc)', 'pass'] });
    expect(formOf(one)).toEqual({
      id: 'a',
      note: 'n',
      once: true,
      when: 'shadow, action, phase(shadow)',
      answer: 'play(orc), pass',
    });
    expect(formOf(order(0))).toEqual({ ...BLANK_ORDER, when: 'shadow, action, phase(shadow)', answer: 'play(orc)' });
    expect(formChanged(one, formOf(one))).toBe(false);
    expect(formChanged(one, { ...formOf(one), note: 'm' })).toBe(true);
    expect(formChanged(null, BLANK_ORDER)).toBe(false);
    expect(formChanged(null, { ...BLANK_ORDER, when: 'x' })).toBe(true);
  });

  it('merges only the parts that changed, unsetting what was cleared, by the name it had', () => {
    const one = order(1, { id: 'a', note: 'n', once: true });
    expect(changeOps(one, formOf(one))).toEqual([]);
    expect(changeOps(one, { ...formOf(one), note: '  ', once: false, id: 'b ' })).toEqual([
      { op: 'merge', id: 'a', set: { id: 'b' }, unset: ['note', 'once'] },
    ]);
    expect(changeOps(order(1), { ...formOf(order(1)), once: true, note: 'Only once', answer: ' pass ' })).toEqual([
      { op: 'merge', index: 1, set: { note: 'Only once', once: true, answer: 'pass' } },
    ]);
    expect(changeOps(one, { ...formOf(one), id: '' })).toEqual([{ op: 'merge', id: 'a', unset: ['id'] }]);
    expect(changeOps(one, { ...formOf(one), when: '{ terms: "shadow, assign" }' })).toEqual([
      { op: 'merge', id: 'a', set: { when: '{ terms: "shadow, assign" }' } },
    ]);
  });

  it('writes a new order as the file has one, blank parts left out', () => {
    expect(
      orderOf({ id: ' x ', note: '', once: true, when: ' shadow, assign ', answer: 'assign(frodo: rest)' }),
    ).toEqual({
      id: 'x',
      once: true,
      when: 'shadow, assign',
      answer: 'assign(frodo: rest)',
    });
    expect(orderOf({ ...BLANK_ORDER, note: 'n', when: 'w', answer: 'a' })).toEqual({
      note: 'n',
      when: 'w',
      answer: 'a',
    });
  });

  it('puts a new order in at its place or at the end; a When in full is merged into it as typed', () => {
    const form = { ...BLANK_ORDER, when: 'shadow, assign', answer: 'pass' };
    expect(insertOps(form, null, 2)).toEqual([{ op: 'insert', order: { when: 'shadow, assign', answer: 'pass' } }]);
    expect(insertOps(form, 5, 2)).toEqual([{ op: 'insert', order: { when: 'shadow, assign', answer: 'pass' } }]);
    expect(insertOps(form, 1, 2)).toEqual([
      { op: 'insert', order: { when: 'shadow, assign', answer: 'pass' }, before: 1 },
    ]);
    expect(insertOps(form, -1, 2)[0]).toEqual({
      op: 'insert',
      order: { when: 'shadow, assign', answer: 'pass' },
      before: 0,
    });
    const full = { ...form, when: '{ terms: shadow }' };
    expect(insertOps(full, null, 2)).toEqual([
      { op: 'insert', order: { when: '{ terms: shadow }', answer: 'pass' } },
      { op: 'merge', index: 2, set: { when: '{ terms: shadow }' } },
    ]);
    expect(insertOps(full, 0, 2)[1]).toEqual({ op: 'merge', index: 0, set: { when: '{ terms: shadow }' } });
  });

  it('deletes and moves an order by its name, not past either end', () => {
    expect(deleteOps(order(1, { id: 'a' }))).toEqual([{ op: 'delete', id: 'a' }]);
    expect(moveOps(order(1), -1, 3)).toEqual([{ op: 'move', index: 1, to: 0 }]);
    expect(moveOps(order(1, { id: 'a' }), 1, 3)).toEqual([{ op: 'move', id: 'a', to: 2 }]);
    expect(moveOps(order(0), -1, 3)).toEqual([]);
    expect(moveOps(order(2), 1, 3)).toEqual([]);
  });

  it('follows the order open as others are taken out or moved', () => {
    expect(placeAfterDelete(null, 1)).toBeNull();
    expect(placeAfterDelete(1, 1)).toBeNull();
    expect(placeAfterDelete(2, 1)).toBe(1);
    expect(placeAfterDelete(0, 1)).toBe(0);
    expect(placeAfterMove(null, 0, 1)).toBeNull();
    expect(placeAfterMove(1, 1, 0)).toBe(0);
    expect(placeAfterMove(0, 1, 0)).toBe(1);
    expect(placeAfterMove(2, 0, 1)).toBe(2);
    expect(placeAfterMove(1, 0, 1)).toBe(0);
    expect(placeAfterMove(1, 2, 1)).toBe(2);
  });

  it("reads which order a refusal is about from the parser's words", () => {
    expect(refusedOrder("opponent.orders[2].answer: there is no card called 'nobody'")).toBe(1);
    expect(refusedOrder('opponent.plays: is the cards...')).toBeNull();
    expect(refusedOrder(null)).toBeNull();
  });

  it('says what answers the waiting decision, and where an order for it goes', () => {
    expect(answeringLine(answering({ order: 1, id: 'first', answer: 'play(orc)' }))).toEqual({
      text: 'Order 2 (first)',
      title: 'Its standing order 2 answers it: play(orc)',
    });
    expect(answeringLine(answering({ order: 0 })).text).toBe('Order 1');
    expect(answeringLine(answering({ order: 0 })).title).toBe('Its standing order 1 answers it: ');
    expect(answeringLine(answering({ rule: 'leftovers' })).text).toBe('Leftovers');
    expect(answeringLine(answering({ rule: 'sandbox' })).text).toBe('Sandbox');
    expect(answeringLine(answering()).text).toBe('Nothing');
    expect(placeForAnswering(answering({ order: 1 }))).toBe(1);
    expect(placeForAnswering(answering())).toBeNull();
    expect(placeForAnswering(null)).toBeNull();
  });

  it('changes the fallback, the sandbox’s cards and the leftovers by the opponent patch', () => {
    expect(fallbackPatch('fail')).toEqual({ unset: ['fallback', 'plays'] });
    expect(fallbackPatch('sandbox')).toEqual({ set: { fallback: 'sandbox' } });
    expect(fallbackPatch('pass')).toEqual({ set: { fallback: 'pass' }, unset: ['plays'] });
    expect(playsPatch([])).toEqual({ unset: ['plays'] });
    expect(playsPatch(['a', 'b'])).toEqual({ set: { plays: 'a, b' } });
    expect(playsWith(['a', 'b'], 'a', true)).toEqual(['b', 'a']);
    expect(playsWith(['a', 'b'], 'a', false)).toEqual(['b']);
    expect(leftoversPatch('')).toEqual({ unset: ['leftovers'] });
    expect(leftoversPatch('ringBearer')).toEqual({ set: { leftovers: 'ringBearer' } });
  });

  it('offers the fallbacks the server names, and the leftovers', () => {
    expect(fallbackOptions(undefined)).toEqual([
      ['fail', 'fail: off the script'],
      ['pass', 'pass: does least'],
    ]);
    const opponent = opponentState({ fallbacks: ['fail', 'sandbox', 'odd'], leftoversChoices: ['ringBearer', 'sam'] });
    expect(fallbackOptions(opponent)).toEqual([
      ['fail', 'fail: off the script'],
      ['sandbox', 'sandbox: plays minions'],
      ['odd', 'odd'],
    ]);
    expect(leftoversOptions(opponent)).toEqual([
      ['', 'none'],
      ['ringBearer', 'the Ring-bearer'],
      ['sam', 'sam'],
    ]);
  });

  it('remembers the tab for its draft only, and forgets what it does not understand', () => {
    const kept = { draft: 'd', open: 'new', before: 1, form: { ...BLANK_ORDER, when: 'w' }, refusal: 'no', scroll: 40 };
    expect(recallMemory(kept, 'd')).toEqual(kept);
    expect(recallMemory(kept, 'other')).toEqual(emptyMemory('other'));
    expect(recallMemory(null, 'd')).toEqual(emptyMemory('d'));
    expect(recallMemory({ draft: 'd', open: 2, before: -1, form: { when: 1 }, refusal: 3, scroll: 'x' }, 'd')).toEqual({
      ...emptyMemory('d'),
      open: 2,
    });
    expect(recallMemory({ draft: 'd', open: 1.5, form: 'no' }, 'd').open).toBeNull();
  });
});
