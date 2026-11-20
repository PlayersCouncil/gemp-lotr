import { draftState, position } from '../../../testing/authoring';
import { DraftStep } from './authoring-state';
import { own } from './steps';
import {
  canon,
  conditionsAsBefore,
  diff,
  refusalLines,
  fieldsOf,
  inPlayWords,
  partOf,
  passWhenInPlay,
  playsOf,
  rebase,
  untilInPlay,
  valueAt,
  whereOf,
  withValue,
  working,
  written,
} from './step-model';

describe('step model', () => {
  it('makes every part editable: messages as objects, allow and end as objects, lists as lines', () => {
    const work = working({
      when: 'freeps, action',
      allow: 'pass',
      messages: ['Hello', { text: 'Look', anchor: ['card:a', 'card:b'], highlight: ['x'], gap: 3 } as never],
      end: 'complete',
    });
    expect(work.when).toBe('freeps, action');
    expect(work.messages).toEqual([
      { text: 'Hello' },
      { text: 'Look', anchor: 'card:a, card:b', highlight: 'x', gap: '3' },
    ]);
    expect(work.allow).toEqual({ answers: 'pass', hints: {} });
    expect(work.end).toEqual({ result: 'complete', messages: [] });
    expect(working({}).end).toEqual({ result: '', messages: [] });
    expect(working({ end: { result: 'fail', messages: 'Lost' } }).end).toEqual({
      result: 'fail',
      messages: [{ text: 'Lost' }],
    });
  });

  it('edits what is written out in full as the server writes it, else as JSON; a block without its steps', () => {
    const step: DraftStep = { until: { seat: 'freeps', phase: 'regroup' }, skipWhen: [{ type: 'x' }], anyOrder: [] };
    expect(working(step, { until: 'seat: freeps' }).until).toBe('seat: freeps');
    expect(working(step).skipWhen).toBe('[\n\t{\n\t\t"type": "x"\n\t}\n]');
    expect('anyOrder' in working(step)).toBe(false);
    expect(working({ allow: { passWhen: { type: 'canSpot' } } }, { passWhen: 'type: canSpot' }).allow.passWhen).toBe(
      'type: canSpot',
    );
    expect(working({ allow: { passWhen: 'freeps' } }).allow.passWhen).toBe('freeps');
  });

  it('takes a When with requirements apart, its requirements as the server writes them', () => {
    const step: DraftStep = { when: { seat: 'freeps', decision: 'action', require: { type: 'canSpot' } } };
    expect(working(step).when).toBe('freeps, action');
    expect(working(step).whenRequire).toBe('{\n\t"type": "canSpot"\n}');
    expect(working(step, { whenRequire: 'type: canSpot' }).whenRequire).toBe('type: canSpot');
  });

  it('writes the step back as its file has it: blanks dropped, a When folded, allow and end as short as can be', () => {
    const work = working({ when: 'freeps', messages: [{ text: 'Hi', once: false }] });
    expect(
      written({
        ...work,
        id: '',
        pause: false,
        checkpoint: true,
        whenRequire: ' type: canSpot ',
        allow: { answers: 'pass', hints: { aragorn: 'No', '': 'x', boromir: '' } },
        end: { result: 'fail', messages: [{ text: 'Lost', header: '' }] },
      }),
    ).toEqual({
      checkpoint: true,
      when: '{\n\tterms: "freeps"\n\trequire: type: canSpot\n}',
      messages: [{ text: 'Hi', once: false }],
      allow: { answers: 'pass', hints: { aragorn: 'No' } },
      end: { result: 'fail', messages: [{ text: 'Lost' }] },
    });
    expect(
      written({ ...work, allow: { answers: 'pass', hints: {} }, end: { result: 'complete', messages: [] } }),
    ).toEqual({
      when: 'freeps',
      messages: [{ text: 'Hi', once: false }],
      allow: 'pass',
      end: 'complete',
    });
    expect(
      written({
        ...work,
        allow: { passWhen: 'x', otherwise: 'No', hints: {} },
        messages: [{ text: '', choices: { a: 'A', '': 'B' } }],
      }),
    ).toEqual({
      when: 'freeps',
      messages: [{ text: '', choices: { a: 'A' } }],
      allow: { passWhen: 'x', otherwise: 'No' },
    });
    expect(written({ ...work, messages: [{ choices: { '': 'B' } } as never] })['messages']).toEqual([{ text: '' }]);
  });

  it('a step read and written again is the step', () => {
    const steps = draftState().draft.steps ?? [];
    for (const step of [steps[1]!, steps[2]!.anyOrder![0]!]) expect(canon(written(working(step)))).toBe(canon(step));
    expect(canon(written(working(steps[2]!)))).toBe(canon(own(steps[2]!)));
  });

  it('says which parts differ, whatever order their keys come in', () => {
    expect(diff({ a: { x: 1, y: 2 }, b: 1 }, { a: { y: 2, x: 1 }, c: 1 })).toEqual({
      set: { b: 1 },
      unset: ['c'],
      any: true,
    });
    expect(diff({ a: 1 }, { a: 1 }).any).toBe(false);
  });

  it('reads and changes a value by its path, sharing the rest', () => {
    const work = working({ messages: [{ text: 'a' }, { text: 'b' }] });
    expect(valueAt(work, 'messages.1.text')).toBe('b');
    expect(valueAt(work, 'messages.5.text')).toBeUndefined();
    const next = withValue(work, 'messages.1.text', 'c');
    expect(next.messages[1]?.text).toBe('c');
    expect(next.messages[0]).toBe(work.messages[0]);
    expect(work.messages[1]?.text).toBe('b');
    expect('id' in withValue({ ...work, id: 'x' }, 'id', undefined)).toBe(false);
    expect(withValue(work, 'allow.hints.aragorn', 'No').allow.hints).toEqual({ aragorn: 'No' });
    expect(partOf('messages.2.text')).toBe('messages');
    expect(partOf('whenRequire')).toBe('when');
    expect(fieldsOf('when')).toEqual(['when', 'whenRequire']);
    expect(fieldsOf('allow')).toEqual(['allow']);
  });

  it("takes the server's parts where the form has not been changed since, and keeps what was typed", () => {
    const work = working({ when: 'freeps', answer: 'pass', messages: [{ text: 'Hi' }] });
    const origin = written(work);
    const typed = withValue(work, 'messages.0.text', 'Hello');
    const theirs = working({
      when: 'freeps',
      answer: 'play(sword)',
      allow: 'play(sword)',
      messages: [{ text: 'Hi!' }],
    });
    const next = rebase(typed, origin, theirs);
    expect(next.changed).toBe(true);
    expect(next.work.answer).toBe('play(sword)');
    expect(next.work.allow.answers).toBe('play(sword)');
    expect(next.work.messages[0]?.text).toBe('Hello');
    expect(next.origin['answer']).toBe('play(sword)');
    expect(next.origin['messages']).toEqual([{ text: 'Hi' }]);
    // (a part the server took out goes; a part the same on both sides stays as it is)
    const gone = rebase(work, origin, working({ when: 'freeps', messages: [{ text: 'Hi' }] }));
    expect(gone.work.answer).toBeUndefined();
    expect('answer' in gone.origin).toBe(false);
    expect(rebase(work, origin, work).changed).toBe(false);
  });

  it('says what has become of a step: current, completed, skipped, not on this way, still to come; no answer yet', () => {
    const state = draftState({
      statuses: ['matched', 'skipped', 'unreached'],
      substatuses: [[], [], ['bypassed', 'unreached']],
      awaiting: ['s2'],
    });
    expect(whereOf(state, 0, -1)).toEqual({ words: 'Current Step', status: 'matched' });
    expect(whereOf(state, 1, -1)).toEqual({ words: 'skipped, no answer yet', status: 'skipped' });
    expect(whereOf(state, 2, 0).words).toBe('not on this way through');
    expect(whereOf({ ...state, statuses: ['unreached', 'matched'] }, 1, -1).words).toBe(
      'Completed Step, no answer yet',
    );
    expect(whereOf(state, 2, 1).words).toBe('Future Step');
    const inBlock = draftState({ position: position({ step: 3, sub: 2, inHand: true }) });
    expect(whereOf(inBlock, 2, 1).words).toBe('Current Step');
    expect(whereOf(inBlock, 2, 0).words).toBe('Future Step');
    const between = draftState({ position: position({ step: 3, sub: 0, inHand: false, underWay: true }) });
    expect(whereOf(between, 2, -1).words).toBe('Current Step');
    expect(whereOf(draftState({ position: position({ over: true }) }), 0, -1).words).toBe('Future Step');
    expect(whereOf(draftState({ statuses: [] }), 7, -1).status).toBe('unreached');
  });

  it('finds the cards a step plays, and writes what has them in play', () => {
    const work = working({ allow: 'play(sword), play( shield )', answer: 'play(sword), pass' });
    expect(playsOf(work)).toEqual(['sword', 'shield']);
    expect(playsOf(working({}))).toEqual([]);
    expect(passWhenInPlay(['a'])).toBe('[\n\t{\n\t\ttype: canSpot\n\t\tfilter: alias(a)\n\t}\n]');
    expect(untilInPlay('freeps', ['a'])).toBe(
      '{\n\tseat: freeps\n\tdecision: action\n\trequire: [\n\t\t{\n\t\t\ttype: canSpot\n\t\t\tfilter: alias(a)\n\t\t}\n\t]\n}',
    );
    expect(inPlayWords([])).toBe('they are');
    expect(inPlayWords(['a'])).toBe('a is');
    expect(inPlayWords(['a', 'b'])).toBe('a and b are');
  });

  it("edits a message's condition as the file writes it, and a step's refusals a line each", () => {
    const require = [{ type: 'canSpot', filter: 'name(Aragorn)' }];
    const step: DraftStep = {
      when: 'freeps, cards',
      allow: 'choose(frodo)',
      refuses: ['2: choose(boromir)', 'pass'],
      messages: ['Plain', { text: 'If', require }, { text: 'One', require: { type: 'x' } }],
      end: { result: 'complete', messages: [{ text: 'Bye', require: { type: 'y' } }] },
    };
    const work = working(step, { 'messages.1.require': 'type: canSpot' });
    expect(work.messages.map((message) => message.require)).toEqual([
      undefined,
      'type: canSpot',
      '{\n\t"type": "x"\n}',
    ]);
    expect(work.end.messages[0]?.require).toBe('{\n\t"type": "y"\n}');
    expect(work.refuses).toBe('2: choose(boromir)\npass');
    expect(working({ allow: 'pass', refuses: 'pass, choose(a)' }).refuses).toBe('pass, choose(a)');
    // (written back: the condition as typed, the refusals one line as a line, more as a list, a blank one gone)
    const back = written(work);
    expect(back['messages']).toEqual([
      { text: 'Plain' },
      { text: 'If', require: 'type: canSpot' },
      { text: 'One', require: '{\n\t"type": "x"\n}' },
    ]);
    expect(back['refuses']).toEqual(['2: choose(boromir)', 'pass']);
    expect(written({ ...work, refuses: ' pass \n\n' })['refuses']).toBe('pass');
    expect(written({ ...work, refuses: '  ' })['refuses']).toBeUndefined();
    expect(written(withValue(work, 'messages.1.require', ''))['messages']).toEqual([
      { text: 'Plain' },
      { text: 'If' },
      { text: 'One', require: '{\n\t"type": "x"\n}' },
    ]);
    expect(refusalLines(undefined)).toEqual([]);
  });

  it('puts the conditions back as they were sent, so that the rest of the messages can go', () => {
    const before = [{ text: 'A', require: 'old' }, { text: 'B' }];
    expect(
      conditionsAsBefore('messages', [{ text: 'A2', require: 'new' }, { text: 'B', require: 'x' }, 'C'], before),
    ).toEqual([{ text: 'A2', require: 'old' }, { text: 'B' }, 'C']);
    // (nothing to keep back, or nothing else to send: nothing)
    expect(conditionsAsBefore('messages', [{ text: 'A2', require: 'old' }, { text: 'B' }], before)).toBeNull();
    expect(conditionsAsBefore('messages', [{ text: 'A', require: 'new' }, { text: 'B' }], before)).toBeNull();
    expect(conditionsAsBefore('messages', 'x', before)).toBeNull();
    expect(conditionsAsBefore('answer', [], before)).toBeNull();
    expect(conditionsAsBefore('messages', [{ text: 'Z', require: 'x' }], undefined)).toEqual([{ text: 'Z' }]);
    expect(
      conditionsAsBefore('end', { result: 'fail', messages: [{ text: 'Z', require: 'x' }] }, { result: 'fail' }),
    ).toEqual({ result: 'fail', messages: [{ text: 'Z' }] });
    expect(conditionsAsBefore('end', 'fail', 'complete')).toBeNull();
    expect(conditionsAsBefore('end', { result: 'fail' }, 'complete')).toBeNull();
  });
});
