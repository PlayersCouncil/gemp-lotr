/**
 * Finding one's way about the draft's steps, as the state names them: by place (from 0) or by name (uid), in the
 * script or in a block. Pure.
 */
import { ScenarioSettings } from '../scenario/scenario-events';
import { Draft, DraftMessageValue, DraftState, DraftStep, LeftOutPart } from './authoring-state';

/** The draft's steps (none when it has none). */
export function stepsOf(state: DraftState): readonly DraftStep[] {
  return state.draft.steps ?? [];
}

/** The names of a block's steps, by the block's place in the script (from 0); none for a step that is no block. */
export function kidsOf(state: DraftState, index: number): readonly string[] {
  return state.kids[index] ?? [];
}

/** Where a step is: its place in the script and, for one of a block's steps, its place in the block (-1: none). */
export interface Located {
  index: number;
  sub: number;
  step: DraftStep;
}

/** The step of a name, in the script or in one of its blocks; null if there is none. */
export function locate(state: DraftState, uid: string | null): Located | null {
  if (uid === null) return null;
  for (const [index, step] of stepsOf(state).entries()) {
    if (state.uids[index] === uid) return { index, sub: -1, step };
    const at = kidsOf(state, index).indexOf(uid);
    const child = at > -1 ? step.anyOrder?.[at] : undefined;
    if (child) return { index, sub: at, step: child };
  }
  return null;
}

/** What a step is called: "34", or "34.2" for the second of a block's steps. */
export function numberOf(index: number, sub = -1): string {
  return `${index + 1}${sub > -1 ? `.${sub + 1}` : ''}`;
}

/** A step without its block's steps: what a block says of itself. */
export function own(step: DraftStep): DraftStep {
  if (step.anyOrder === undefined) return step;
  const rest = { ...step };
  delete rest.anyOrder;
  return rest;
}

/** Whether a step is the opening: read before the first turn, on the board as it is laid out. */
export function isOpening(step: DraftStep | null | undefined): boolean {
  return typeof step?.when === 'string' && step.when.trim().toLowerCase() === 'opening';
}

export function isBlock(step: DraftStep | null | undefined): boolean {
  return step?.anyOrder !== undefined;
}

/** Whether a step's or an ending's messages are a list (a file may give a single one). */
export const isMessageList = (
  value: readonly DraftMessageValue[] | DraftMessageValue | undefined,
): value is readonly DraftMessageValue[] => Array.isArray(value);

/** A step's messages, as a list (a file may give a single one). */
export function messagesOf(step: DraftStep): readonly DraftMessageValue[] {
  const messages = step.messages;
  if (messages === undefined) return [];
  return isMessageList(messages) ? messages : [messages];
}

/** Whether the seat named is the player's ("human", or the seat the draft gives the player). */
export function isHumanSeat(state: DraftState, seat: string): boolean {
  return seat === 'human' || seat === (state.draft.human ?? 'freeps');
}

/** Whether the step waits for its answer: what is done at it becomes it. */
export function isAwaiting(state: DraftState, uid: string | null): boolean {
  return uid !== null && state.awaiting.includes(uid);
}

/** What was left out of the step of this name when the file was opened. */
export function leftOutOf(state: DraftState, uid: string | null): LeftOutPart[] {
  return uid === null ? [] : state.leftOut.filter((part) => part.uid === uid);
}

/** Whether this page shows the draft's current game. */
export function isThisGame(state: DraftState, gameId: string | null): boolean {
  return gameId !== null && String(state.gameId) === gameId;
}

/** The player's settings the draft fixes for its game (what the scenario's info would say of them). */
export function settingsOf(draft: Draft): ScenarioSettings {
  const options = draft.options ?? {};
  return {
    autoAccept: options.autoAccept !== false,
    autoDismissDeckReveal: options.autoDismissDeckReveal ?? (draft.kind ?? 'tutorial') === 'tutorial',
    animationSpeed: options.animationSpeed ?? null,
  };
}
