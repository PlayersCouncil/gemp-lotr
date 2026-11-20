import { decision, draftState, position, STEPS } from '../../../testing/authoring';
import { DraftStep } from './authoring-state';
import {
  lastCheckpoint,
  passesByItself,
  placeOf,
  silentPass,
  summary,
  timelineEnd,
  TimelineGroup,
  timelineItems,
  TimelineRow,
} from './timeline';

const human = (seat: string) => seat === 'freeps' || seat === 'human';
const rows = (items: ReturnType<typeof timelineItems>) =>
  items.filter((item): item is TimelineRow => item.kind === 'row');
const groups = (items: ReturnType<typeof timelineItems>) =>
  items.filter((item): item is TimelineGroup => item.kind === 'group');

describe('summary', () => {
  it('says who a step is about, what it waits for (seat, phase and turn short) and what it does', () => {
    expect(summary({ when: 'freeps, action, phase(fellowship), turn(1)', allow: 'pass' }, human)).toEqual({
      who: 'You',
      when: 'action fellowship t1',
      act: 'pass',
    });
    expect(summary({ when: 'shadow, action', answer: 'play(x)', allow: 'pass' }, human)).toEqual({
      who: 'Opp',
      when: 'action',
      act: 'play(x)',
    });
    expect(summary({ when: 'action', allow: { answers: 'play(a)' } }, human)).toEqual({
      who: 'Any',
      when: 'action',
      act: 'play(a)',
    });
    expect(summary({ when: 'freeps', allow: { passWhen: '{}' } }, human).act).toBe('pass when it may');
    expect(summary({}, human)).toEqual({ who: 'Any', when: '', act: '' });
  });

  it('reads pauses and the opening as read only, the opening doing what it does', () => {
    expect(summary({ pause: true, when: 'freeps', answer: 'x' }, human).who).toBe('Read');
    expect(summary({ when: 'opening', do: 'draw(5)' }, human)).toEqual({
      who: 'Read',
      when: 'opening',
      act: 'draw(5)',
    });
    expect(summary({ when: 'opening' }, human).act).toBe('');
  });

  it('reads a When written out in full as its terms, or as {...}', () => {
    expect(summary({ when: { seat: 'freeps', decision: 'action', require: { type: 'x' } } }, human)).toEqual({
      who: 'You',
      when: 'action only while…',
      act: '',
    });
    expect(summary({ when: { odd: true } }, human).when).toBe('{...}');
  });

  it('says what a block is and until when', () => {
    expect(summary(STEPS[2]!, human)).toEqual({ who: 'Block', when: 't1', act: 'any order, until phase(regroup)' });
    expect(summary({ skirmishes: true, anyOrder: [], until: { seat: 'x' } }, human).act).toBe(
      'skirmishes, until {...}',
    );
  });
});

describe('placeOf', () => {
  it('takes the turn and phase from the When, else from where the game saw the step, else from its decision', () => {
    expect(placeOf({ when: 'opening' }, null)).toEqual({ turn: 0, phase: 'opening' });
    expect(placeOf({ when: 'freeps, phase(Shadow), turn( 3 )' }, null)).toEqual({ turn: 3, phase: 'shadow' });
    expect(placeOf({ when: 'freeps' }, { turn: 2, phase: 'regroup' })).toEqual({ turn: 2, phase: 'regroup' });
    expect(placeOf({ when: 'freeps, assign' }, null)).toEqual({ turn: null, phase: 'assignment' });
    expect(placeOf({ when: 'freeps, fighting(frodo)' }, null)).toEqual({ turn: null, phase: 'skirmish' });
    expect(placeOf({ when: { seat: 'freeps' } }, undefined)).toEqual({ turn: null, phase: null });
  });

  it('gives a block its own kind of phase', () => {
    expect(placeOf({ anyOrder: [], when: 'turn(2)' }, null)).toEqual({ turn: 2, phase: 'block' });
    expect(placeOf({ anyOrder: [], when: 'phase(maneuver)' }, null).phase).toBe('maneuver');
    expect(placeOf({ anyOrder: [], skirmishes: true }, { turn: 4, phase: 'x' })).toEqual({
      turn: 4,
      phase: 'skirmishes',
    });
  });
});

describe('silent passes', () => {
  it('is a step that only passes and says nothing', () => {
    expect(silentPass({ when: 'shadow, action', allow: 'pass' })).toBe(true);
    expect(silentPass({ when: 'shadow, action', answer: 'pass?' })).toBe(true);
    expect(silentPass({ when: 'shadow, action', answer: 'pass', allow: { answers: 'pass' } })).toBe(true);
    expect(silentPass({ when: 'x', answer: 'pass', allow: 'play(a)' })).toBe(false);
    expect(silentPass({ when: 'x', allow: { answers: 'pass', passWhen: 'y' } })).toBe(false);
    expect(silentPass({ when: 'x', answer: 'play(a)' })).toBe(false);
    expect(silentPass({ when: 'x' })).toBe(false);
    for (const not of [
      { pause: true, allow: 'pass' },
      { anyOrder: [], allow: 'pass' },
      { messages: ['hi'], allow: 'pass' },
      { checkpoint: true, allow: 'pass' },
      { end: 'complete', allow: 'pass' },
      { end: { result: 'fail' }, allow: 'pass' },
    ] as DraftStep[]) {
      expect(silentPass(not)).toBe(false);
    }
    expect(silentPass({ end: { result: '' } as never, allow: 'pass' })).toBe(true);
  });

  it("passes by itself where it is the opponent's, or the player's in a phase passed through", () => {
    const state = draftState({ draft: { id: 'x', human: 'freeps', options: { autoPass: 'archery, regroup' } } });
    expect(passesByItself(state, { when: 'shadow, action', allow: 'pass' })).toBe(true);
    expect(passesByItself(state, { when: 'freeps, action, phase(archery)', allow: 'pass' })).toBe(true);
    expect(passesByItself(state, { when: 'freeps, action, phase(fellowship)', allow: 'pass' })).toBe(false);
    expect(passesByItself(state, { when: 'freeps, action', allow: 'pass' })).toBe(false);
    expect(passesByItself(state, { when: { seat: 'shadow' }, allow: 'pass' })).toBe(false);
    expect(passesByItself(state, { when: 'shadow, action', answer: 'play(x)' })).toBe(false);
    const everywhere = draftState({ draft: { id: 'x', options: { autoPass: true } } });
    expect(passesByItself(everywhere, { when: 'freeps, phase(maneuver)', allow: 'pass' })).toBe(true);
    const said = draftState({ draft: { id: 'x', options: { autoPass: 'TRUE' } } });
    expect(passesByItself(said, { when: 'freeps, phase(maneuver)', allow: 'pass' })).toBe(true);
    const none = draftState({ draft: { id: 'x' } });
    expect(passesByItself(none, { when: 'freeps, phase(maneuver)', allow: 'pass' })).toBe(false);
  });
});

describe('lastCheckpoint', () => {
  const steps: DraftStep[] = [{}, { checkpoint: true }, {}, { checkpoint: true }, {}];
  const at = (step: number) => draftState({ draft: { id: 'x', steps }, position: position({ step }) });

  it('is the last checkpoint before where the game is, or at it, or the first step', () => {
    expect(lastCheckpoint(at(4), false)).toBe(2);
    expect(lastCheckpoint(at(4), true)).toBe(4);
    expect(lastCheckpoint(at(2), false)).toBe(1);
    expect(lastCheckpoint(at(9), false)).toBe(4);
    expect(lastCheckpoint(at(1), true)).toBe(1);
  });
});

describe('timelineItems', () => {
  it('lists the steps, a block with its steps under it, grouped by turn and phase', () => {
    const items = timelineItems(draftState(), { selected: null, sub: null });
    expect(groups(items).map((group) => group.label)).toEqual([
      'Turn 1 · Fellowship',
      'Turn 1 · Shadow',
      'Turn 1 · Several phases, in any order',
    ]);
    expect(rows(items).map((row) => [row.number, row.summary.who, row.phase, row.block])).toEqual([
      ['1', 'You', 'fellowship', false],
      ['2', 'Opp', 'shadow', false],
      ['3', 'Block', 'block', true],
      ['3.1', 'You', 'block', false],
      ['3.2', 'You', 'block', false],
    ]);
    expect(groups(items).every((group) => !group.even)).toBe(true);
  });

  it('marks where the game is and the step selected, in the script or in a block', () => {
    const atBlock = draftState({ position: position({ step: 3, sub: 2 }) });
    let found = rows(timelineItems(atBlock, { selected: 3, sub: 's3k1' }));
    expect(found.filter((row) => row.current).map((row) => row.number)).toEqual(['3', '3.2']);
    expect(found.filter((row) => row.selected).map((row) => row.number)).toEqual(['3.1']);
    found = rows(timelineItems(draftState(), { selected: 1, sub: null }));
    expect(found.filter((row) => row.selected).map((row) => row.number)).toEqual(['1']);
    for (const where of [position({ over: true }), position({ frontier: true })]) {
      expect(
        rows(timelineItems(draftState({ position: where }), { selected: null, sub: null })).some((r) => r.current),
      ).toBe(false);
    }
  });

  it('gives each row its status, its name, and its marks', () => {
    const steps: DraftStep[] = [
      {
        id: 'hello',
        when: 'freeps',
        if: 'plan',
        unless: 'help',
        do: 'draw(1)',
        messages: ['a'],
        once: true,
        checkpoint: true,
        end: 'complete',
      },
      { when: 'freeps', messages: ['a', 'b'] },
      { when: 'shadow, action', allow: 'pass' },
    ];
    const state = draftState({
      draft: { id: 'x', steps },
      statuses: ['matched'],
      awaiting: ['s1'],
      leftOut: [{ where: 'step 1 (hello).allow', message: 'bad', was: 'x', step: 1, uid: 's1' }],
    });
    const [first, second, third] = rows(timelineItems(state, { selected: null, sub: null }));
    expect(first!.status).toBe('matched');
    expect(second!.status).toBe('unreached');
    expect(first!.id).toBe('hello');
    expect(first!.tags.map((tag) => tag.text)).toEqual([
      'if plan',
      'unless help',
      'do',
      '▬1',
      'once',
      '⚑',
      'end',
      'no answer yet',
      'left out',
    ]);
    expect(first!.tags.find((tag) => tag.kind === 'left-out')!.title).toBe('allow: bad\nIt said: x');
    expect(first!.tags.find((tag) => tag.kind === 'messages')!.title).toBe('1 message');
    expect(second!.tags.find((tag) => tag.kind === 'messages')!.title).toBe('2 messages');
    expect(third!.silent).toBe(true);
    expect(second!.id).toBeNull();
  });

  it('keeps a step that says neither turn nor phase in the group before it, with its colour, and even turns apart', () => {
    const steps: DraftStep[] = [
      { when: 'opening' },
      { when: 'freeps, phase(fellowship), turn(2)' },
      { when: 'freeps' },
      { anyOrder: [{ when: 'freeps' }], until: 'phase(regroup)' },
      { when: 'freeps' },
      { when: 'freeps, phase(regroup)' },
    ];
    const items = timelineItems(draftState({ draft: { id: 'x', steps } }), { selected: null, sub: null });
    expect(groups(items).map((group) => [group.label, group.even])).toEqual([
      ['Turn 0 · Before the first turn', true],
      ['Turn 2 · Fellowship', true],
      ['Turn 2 · Several phases, in any order', true],
      ['Turn 2 · Regroup', true],
    ]);
    // (after a block whose Until names a phase, what follows is "after the block" until a phase is named)
    expect(rows(items).map((row) => row.phase)).toEqual([
      'opening',
      'fellowship',
      'fellowship',
      'block',
      'block',
      'after-block',
      'regroup',
    ]);
    expect(rows(items).every((row) => row.even)).toBe(true);
  });

  it('draws no heading where neither turn nor phase is known, and names a phase it has no words for as it is', () => {
    const items = timelineItems(
      draftState({
        draft: { id: 'x', steps: [{ when: 'freeps' }, { when: 'freeps' }] },
        places: [null, { turn: 3, phase: 'odd' }],
      }),
      { selected: null, sub: null },
    );
    expect(groups(items).map((group) => group.label)).toEqual(['Turn 3 · odd']);
    expect(rows(items).map((row) => [row.phase, row.even])).toEqual([
      ['none', false],
      ['odd', false],
    ]);
    expect(
      timelineItems(draftState({ draft: { id: 'x', steps: [{ when: 'turn(2)' }] } }), { selected: null, sub: null })[0],
    ).toEqual({ kind: 'group', label: 'Turn 2', phase: 'none', even: true });
  });
});

describe('timelineEnd', () => {
  it('says the game is over, how', () => {
    expect(timelineEnd(draftState({ position: position({ over: true, result: 'off_script' }) })).over).toBe(
      'The game is over (off script). Go back to a step to carry on.',
    );
    expect(timelineEnd(draftState({ position: position({ over: true }) })).over).toBe(
      'The game is over. Go back to a step to carry on.',
    );
  });

  it('says the script ends here, and whether what is done is written down', () => {
    const frontier = position({ frontier: true, inHand: false });
    expect(timelineEnd(draftState({ position: frontier })).frontier).toBe(
      'The script ends here. What you do from here on is written down.',
    );
    expect(timelineEnd(draftState({ position: frontier, recording: false })).frontier).toBe(
      'The script ends here. What you do from here on is not written down.',
    );
  });

  it('says what is asked that no step has in hand', () => {
    const free = position({ inHand: false });
    expect(timelineEnd(draftState({ position: free })).asked).toBe(
      'You are being asked: Play Fellowship action or Pass (turn 1, fellowship)',
    );
    const held = draftState({
      position: free,
      waiting: {
        human: null,
        opponent: decision({ seat: 'shadow', held: true, text: 'Play Shadow action', where: 'w' }),
      },
    });
    expect(timelineEnd(held).asked).toBe('The opponent is being asked: Play Shadow action (w)');
    expect(timelineEnd(draftState())).toEqual({ over: null, frontier: null, asked: null });
  });
});

describe('what is being asked, in a row', () => {
  it('reads as what the decision is for, not as the kind of decision it comes as', () => {
    expect(summary({ when: 'freeps, cards, nextSkirmish, after(gandalf)', allow: 'choose(gandalf)' }, human)).toEqual({
      who: 'You',
      when: 'next skirmish after(gandalf)',
      act: 'choose(gandalf)',
    });
    // (not: a choice of cards that is anything but that one)
    expect(summary({ when: 'freeps, cards, not(nextSkirmish), phase(skirmish)' }, human).when).toBe(
      'cards not next skirmish skirmish',
    );
    expect(summary({ when: 'freeps, choice, anotherMove, phase(regroup), turn(1)' }, human).when).toBe(
      'another move? regroup t1',
    );
    expect(summary({ when: 'shadow, assign', answer: 'assign(aragorn: orc1, ringBearer: rest)' }, human)).toEqual({
      who: 'Opp',
      when: 'assign',
      act: 'assign(aragorn: orc1, ringBearer: rest)',
    });
  });

  it('places a step by what is asked where that always comes in one phase', () => {
    expect(placeOf({ when: 'freeps, cards, archeryWound' }, null).phase).toBe('archery');
    expect(placeOf({ when: 'freeps, choice, anotherMove, turn(2)' }, null)).toEqual({ turn: 2, phase: 'regroup' });
    expect(placeOf({ when: 'freeps, cards, reconcile' }, null).phase).toBe('regroup');
    expect(placeOf({ when: 'freeps, cards, nextSkirmish' }, null).phase).toBe('assignment');
    expect(placeOf({ when: 'freeps, cards, nextSkirmish, after(frodo)' }, null).phase).toBe('skirmish');
    expect(placeOf({ when: 'freeps, cards, not(archeryWound)' }, null).phase).toBeNull();
    expect(placeOf({ when: 'freeps, action, optionalResponses' }, null).phase).toBeNull();
    expect(placeOf({ when: 'freeps, cards, archeryWound' }, { turn: 1, phase: 'shadow' }).phase).toBe('shadow');
  });
});
