import { ScenarioEnd, ScenarioInfo, ScenarioOffScript, ScenarioStep } from './scenario-events';
import {
  applyScenarioEvent,
  displayName,
  noticeRead,
  renameOpponent,
  ScenarioState,
  scenarioAnswered,
  stepRead,
} from './scenario-state';

const INFO: ScenarioInfo = {
  type: 'info',
  id: 'basics-01',
  kind: 'tutorial',
  title: 'The Basics',
  humanSeat: 'freeps',
  opponent: '~Scenario',
  opponentName: 'Basic <Shadow> & "Co"',
  settings: { autoAccept: true, autoDismissDeckReveal: true, animationSpeed: null },
  bubbleGap: null,
  resumedAt: null,
  campaign: null,
  author: false,
  draftTest: false,
};

const step = (extra: Partial<ScenarioStep> = {}): ScenarioStep => ({
  type: 'step',
  step: 1,
  sub: 0,
  seat: 'human',
  pause: false,
  messages: [],
  parts: {},
  allow: null,
  cards: {},
  ...extra,
});
const OFF: ScenarioOffScript = { type: 'offscript', message: null };
const END: ScenarioEnd = { type: 'end', result: 'complete', messages: [], message: null, checkpoint: null };

const withInfo = (info: Partial<ScenarioInfo>): ScenarioState => applyScenarioEvent(null, { ...INFO, ...info });

describe('scenario state', () => {
  it('keeps who the scenario is, the step put up (counting each), the notice and the end', () => {
    let state = applyScenarioEvent(null, INFO);
    expect(state).toEqual({ info: INFO, step: null, shown: 0, notice: null, end: null });
    const first = step();
    state = applyScenarioEvent(state, first);
    expect(state).toMatchObject({ step: first, shown: 1 });
    // The same step put up again is news too
    state = applyScenarioEvent(state, first);
    expect(state.shown).toBe(2);
    // Leaving the script takes the step away and says so; a step after that takes the notice away
    state = applyScenarioEvent(state, OFF);
    expect(state).toMatchObject({ step: null, notice: OFF, shown: 2 });
    state = applyScenarioEvent(state, step({ step: 2 }));
    expect(state).toMatchObject({ step: { step: 2 }, notice: null, shown: 3 });
    // The end takes both away
    state = applyScenarioEvent(applyScenarioEvent(state, OFF), END);
    expect(state).toMatchObject({ info: INFO, step: null, notice: null, end: END });
  });

  it("takes away a step about the player's decision once it is answered, and only that kind", () => {
    const answering = applyScenarioEvent(null, step());
    expect(scenarioAnswered(answering)?.step).toBeNull();
    const opponents = applyScenarioEvent(null, step({ seat: 'opponent' }));
    expect(scenarioAnswered(opponents)).toBe(opponents);
    const paused = applyScenarioEvent(null, step({ pause: true }));
    expect(scenarioAnswered(paused)).toBe(paused);
    expect(scenarioAnswered(null)).toBeNull();
  });

  it('takes away a step that has been read, and a notice that has been read', () => {
    const reading = applyScenarioEvent(null, step({ seat: 'opponent' }));
    expect(stepRead(reading)).toMatchObject({ step: null, shown: 1 });
    const nothing = applyScenarioEvent(null, INFO);
    expect(stepRead(nothing)).toBe(nothing);
    expect(stepRead(null)).toBeNull();
    const notice = applyScenarioEvent(null, OFF);
    expect(noticeRead(notice)).toMatchObject({ notice: null });
    expect(noticeRead(nothing)).toBe(nothing);
    expect(noticeRead(null)).toBeNull();
  });

  it('shows the scripted opponent under the name the scenario gives it, and everyone else as they are', () => {
    const state = withInfo({});
    expect(displayName(state, '~Scenario')).toBe('Basic <Shadow> & "Co"');
    expect(displayName(state, 'asdf')).toBe('asdf');
    expect(displayName(null, '~Scenario')).toBe('~Scenario');
    expect(displayName(applyScenarioEvent(null, step()), '~Scenario')).toBe('~Scenario');
    expect(displayName(withInfo({ opponent: '' }), '')).toBe('');
  });

  it("renames the opponent in the server's HTML, its shown name escaped", () => {
    const state = withInfo({});
    expect(renameOpponent(state, '<b>~Scenario</b> plays; ~Scenario passes')).toBe(
      '<b>Basic &lt;Shadow&gt; &amp; &quot;Co&quot;</b> plays; Basic &lt;Shadow&gt; &amp; &quot;Co&quot; passes',
    );
    // Nothing to rename: an ordinary game, a scenario without an opponent, or one under its own name
    expect(renameOpponent(null, '~Scenario')).toBe('~Scenario');
    expect(renameOpponent(withInfo({ opponent: '' }), 'a b')).toBe('a b');
    expect(renameOpponent(withInfo({ opponentName: '~Scenario' }), '~Scenario')).toBe('~Scenario');
  });
});
