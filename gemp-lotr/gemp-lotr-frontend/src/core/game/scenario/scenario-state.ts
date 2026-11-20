/**
 * What the page holds of a scripted game (a scenario) beside the game itself: who it is, the step on the screen, and
 * how it ended. Part of the game's model (GameModel.scenario; null in an ordinary game), changed only by the server's
 * scenario events and by the player's answer. Pure.
 */
import {
  ScenarioEnd,
  ScenarioEvent,
  ScenarioInfo,
  ScenarioOffScript,
  ScenarioSeat,
  ScenarioStep,
} from './scenario-events';

export interface ScenarioState {
  /** Null only if a step came before the scenario said who it is (it shouldn't). */
  info: ScenarioInfo | null;
  /** The step the script has put up, until it is answered, read or replaced. */
  step: ScenarioStep | null;
  /** How many steps have been put up: the same step put up again (rewritten by its author, say) is news too. */
  shown: number;
  /** The game has left its script and goes on as an ordinary one: said once. */
  notice: ScenarioOffScript | null;
  end: ScenarioEnd | null;
  /**
   * The last checkpoint step the game has reached, as the last step event said (a step event only comes with a step,
   * so it is kept between them); a game brought back to one starts at it. Null: none, and nothing to go back to.
   */
  lastCheckpoint: number | null;
  /** The role the last step said the person plays (kept between steps); null: none said yet. */
  role: ScenarioSeat | null;
}

const NOTHING: ScenarioState = {
  info: null,
  step: null,
  shown: 0,
  notice: null,
  end: null,
  lastCheckpoint: null,
  role: null,
};

/** A scenario event applied. */
export function applyScenarioEvent(state: ScenarioState | null, event: ScenarioEvent): ScenarioState {
  const scenario = state ?? NOTHING;
  switch (event.type) {
    case 'info':
      return { ...scenario, info: event, lastCheckpoint: scenario.lastCheckpoint ?? event.resumedAt };
    case 'step':
      return {
        ...scenario,
        step: event,
        shown: scenario.shown + 1,
        notice: null,
        lastCheckpoint: event.lastCheckpoint ?? scenario.lastCheckpoint,
        role: event.role ?? scenario.role,
      };
    case 'offscript':
      return { ...scenario, step: null, notice: event };
    case 'end':
      return { ...scenario, step: null, notice: null, end: event };
  }
}

/**
 * The player has answered the decision: a step about that decision has had its say. (An opponent's step, and one read
 * before the decision is asked, go when they have been read: the server is told, see ScenarioGuide.)
 */
export function scenarioAnswered(state: ScenarioState | null): ScenarioState | null {
  if (state?.step?.seat !== 'human' || state.step.pause) return state;
  return { ...state, step: null };
}

/**
 * The role the person plays now: the shown step's, else the last one's, else their seat's. In a one-sided game always
 * the seat's; in a two-sided one the Free Peoples player on their own turn and the Shadow player on the opponent's.
 */
export function scenarioRole(state: ScenarioState | null): ScenarioSeat {
  return state?.step?.role ?? state?.role ?? state?.info?.humanSeat ?? 'freeps';
}

/** A step that only waited to be read has been read. */
export function stepRead(state: ScenarioState | null): ScenarioState | null {
  return state?.step ? { ...state, step: null } : state;
}

/** The off-script notice has been read. */
export function noticeRead(state: ScenarioState | null): ScenarioState | null {
  return state?.notice ? { ...state, notice: null } : state;
}

const escapeHtml = (text: string) =>
  text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

/**
 * A participant's name as the page shows it: the scripted opponent plays under one account for every scenario, and
 * each scenario gives it a name of its own.
 */
export function displayName(state: ScenarioState | null, participantId: string): string {
  const info = state?.info;
  return info && info.opponent !== '' && participantId === info.opponent ? info.opponentName : participantId;
}

/** The server's HTML (a log line, a dialog's title) with the scripted opponent under its shown name. */
export function renameOpponent(state: ScenarioState | null, html: string): string {
  const info = state?.info;
  if (!info || info.opponent === '' || info.opponent === info.opponentName) return html;
  return html.split(info.opponent).join(escapeHtml(info.opponentName));
}
