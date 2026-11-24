import { ScenarioEnd, ScenarioInfo, ScenarioOffScript, ScenarioStep } from './scenario-events';
import {
  applyScenarioEvent,
  displayName,
  noticeRead,
  renameOpponent,
  ScenarioState,
  scenarioAnswered,
  scenarioRole,
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
  twoSided: false,
  opening: 'board',
  mode: 'play',
  level: 'none',
  tools: [],
  canStop: false,
  canStart: false,
};

const step = (extra: Partial<ScenarioStep> = {}): ScenarioStep => ({
  type: 'step',
  step: 1,
  sub: 0,
  seat: 'human',
  pause: false,
  role: null,
  turnOf: null,
  turn: null,
  lastCheckpoint: null,
  messages: [],
  parts: {},
  allow: null,
  cards: {},
  ...extra,
});
const OFF: ScenarioOffScript = { type: 'offscript', message: null };
const END: ScenarioEnd = {
  type: 'end',
  result: 'complete',
  messages: [],
  message: null,
  checkpoint: null,
  restart: false,
  winner: null,
  reason: null,
  reset: false,
};

const withInfo = (info: Partial<ScenarioInfo>): ScenarioState => applyScenarioEvent(null, { ...INFO, ...info });

describe('scenario state', () => {
  it('keeps who the scenario is, the step put up (counting each), the notice and the end', () => {
    let state = applyScenarioEvent(null, INFO);
    expect(state).toEqual({
      info: INFO,
      step: null,
      shown: 0,
      notice: null,
      end: null,
      lastCheckpoint: null,
      role: null,
    });
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

  it('keeps the last checkpoint reached and the role last said between steps', () => {
    let state = withInfo({ resumedAt: 4 });
    // (a game brought back to a checkpoint is at it)
    expect(state).toMatchObject({ lastCheckpoint: 4, role: null });
    state = applyScenarioEvent(state, step({ lastCheckpoint: 6, role: 'shadow' }));
    expect(state).toMatchObject({ lastCheckpoint: 6, role: 'shadow' });
    state = applyScenarioEvent(stepRead(state), step({ step: 7 }));
    expect(state).toMatchObject({ lastCheckpoint: 6, role: 'shadow' });
    // (the info said again keeps what was reached)
    expect(applyScenarioEvent(state, INFO)).toMatchObject({ lastCheckpoint: 6 });
  });

  it('says the role the person plays now: the step on the screen, the last one said, or the seat', () => {
    expect(scenarioRole(null)).toBe('freeps');
    expect(scenarioRole(withInfo({ humanSeat: 'shadow' }))).toBe('shadow');
    const theirTurn = applyScenarioEvent(withInfo({ twoSided: true }), step({ role: 'shadow' }));
    expect(scenarioRole(theirTurn)).toBe('shadow');
    expect(scenarioRole(stepRead(theirTurn))).toBe('shadow');
    expect(scenarioRole(applyScenarioEvent(theirTurn, step({ role: 'freeps' })))).toBe('freeps');
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
