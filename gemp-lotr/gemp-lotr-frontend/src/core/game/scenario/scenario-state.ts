/**
 * What the page holds of a scripted game (a scenario) beside the game itself: who it is, the step on the screen, and
 * how it ended. Part of the game's model (GameModel.scenario; null in an ordinary game), changed only by the server's
 * scenario events and by the player's answer. Pure.
 */
import { ScenarioEnd, ScenarioEvent, ScenarioInfo, ScenarioOffScript, ScenarioStep } from './scenario-events';

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
}

const NOTHING: ScenarioState = { info: null, step: null, shown: 0, notice: null, end: null };

/** A scenario event applied. */
export function applyScenarioEvent(state: ScenarioState | null, event: ScenarioEvent): ScenarioState {
  const scenario = state ?? NOTHING;
  switch (event.type) {
    case 'info':
      return { ...scenario, info: event };
    case 'step':
      return { ...scenario, step: event, shown: scenario.shown + 1, notice: null };
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
