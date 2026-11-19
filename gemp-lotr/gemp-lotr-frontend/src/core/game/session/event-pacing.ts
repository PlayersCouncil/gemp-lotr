/**
 * How long each game event holds the page, as the legacy page's animation queue did (gameAnimations.js): the next
 * event waits until a card put into play has flown to its place, a removed card has faded, an activated card has
 * flashed, a played event has been shown. Pure: the page draws the animation (GameAnimation) for that time.
 */
import { GameEvent } from '../protocol/game-events';

/** The legacy durations at normal speed (ms). */
export const DURATIONS = {
  playEvent: 1500,
  putCardIntoPlay: 1500,
  cardAffectsCard: 1200,
  cardActivated: 1200,
  decision: 1200,
  removeCardFromPlay: 600,
  flip: 80,
} as const;

/** What the page draws while an event holds it. */
export type GameAnimation =
  | { kind: 'put'; cardId: string }
  | { kind: 'remove'; cardIds: readonly string[] }
  | { kind: 'flip'; cardIds: readonly string[]; phase: 'out' | 'in' }
  | { kind: 'activate'; cardId: string }
  | { kind: 'event'; blueprintId: string; testingText: string | null }
  | { kind: 'affect'; blueprintId: string; cardIds: readonly string[] }
  | { kind: 'decision' };

/** One stretch of time an event holds the page: drawn as `animation`, before or after the event changes the game. */
export interface EventPause {
  animation: GameAnimation;
  /** ms at normal speed (times the speed's factor). */
  duration: number;
  when: 'before' | 'after';
}

export interface PacingContext {
  /** False for the game's whole state as the page opens (nothing animates then). */
  animate: boolean;
  replay: boolean;
  spectator: boolean;
  /** The participant at the bottom of the table. */
  bottom: string | null;
}

/** Zones a card put into play doesn't fly into (piles and the hand). */
const NO_FLIGHT = ['DISCARD', 'DEAD', 'HAND', 'ADVENTURE_DECK', 'DECK'];

/** The pauses of one event, in order. The player's own actions aren't played out to them (unless watching). */
export function eventPauses(event: GameEvent, context: PacingContext): EventPause[] {
  if (!context.animate) return [];
  const others = (participantId: string | null) =>
    context.spectator || context.replay || participantId !== context.bottom;
  switch (event.type) {
    case 'PCIP':
      return others(event.participantId) && !NO_FLIGHT.includes(event.zone ?? '')
        ? [{ animation: { kind: 'put', cardId: event.cardId }, duration: DURATIONS.putCardIntoPlay, when: 'after' }]
        : [];
    case 'RCFP':
      return others(event.participantId)
        ? [
            {
              animation: { kind: 'remove', cardIds: event.cardIds },
              duration: DURATIONS.removeCardFromPlay,
              when: 'before',
            },
          ]
        : [];
    case 'FCIP':
      return event.cardIds.length === 0
        ? []
        : [
            {
              animation: { kind: 'flip', cardIds: event.cardIds, phase: 'out' },
              duration: DURATIONS.flip,
              when: 'before',
            },
            {
              animation: { kind: 'flip', cardIds: event.cardIds, phase: 'in' },
              duration: DURATIONS.flip,
              when: 'after',
            },
          ];
    case 'CA':
      return others(event.participantId)
        ? [{ animation: { kind: 'activate', cardId: event.cardId }, duration: DURATIONS.cardActivated, when: 'after' }]
        : [];
    case 'EP':
      return others(event.participantId)
        ? [
            {
              animation: { kind: 'event', blueprintId: event.blueprintId, testingText: event.testingText },
              duration: DURATIONS.playEvent,
              when: 'after',
            },
          ]
        : [];
    case 'CAC':
      return others(event.participantId)
        ? [
            {
              animation: { kind: 'affect', blueprintId: event.blueprintId, cardIds: event.affectedCardIds },
              duration: DURATIONS.cardAffectsCard,
              when: 'after',
            },
          ]
        : [];
    case 'D':
      // A replay shows each decision a while
      return context.replay ? [{ animation: { kind: 'decision' }, duration: DURATIONS.decision, when: 'after' }] : [];
    default:
      return [];
  }
}

/** The factor of the animations' durations for the animation speed setting (-4 to 4): 2 to the minus speed. */
export function speedFactor(speed: number): number {
  return 2 ** -speed;
}
