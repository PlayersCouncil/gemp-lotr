import { GameEvent } from '../protocol/game-events';
import { ScenarioMessage, ScenarioStep } from '../scenario/scenario-events';
import { DURATIONS, eventPauses, PacingContext, readingTime, speedFactor } from './event-pacing';

const PLAYING: PacingContext = { animate: true, replay: false, spectator: false, bottom: 'alice' };
const REPLAY: PacingContext = { ...PLAYING, replay: true };

const message = (html: string, on: ScenarioMessage['on'] = null) => ({ html, on }) as ScenarioMessage;
const step = (...messages: ScenarioMessage[]): GameEvent => ({
  type: 'S',
  scenario: { type: 'step', messages } as unknown as ScenarioStep,
});

describe('event pacing', () => {
  it("plays out the opponent's card put into play, not the player's own, and nothing while the page opens", () => {
    const put = (participantId: string, zone = 'SHADOW_CHARACTERS') =>
      ({ type: 'PCIP', cardId: '5', participantId, zone }) as GameEvent;
    expect(eventPauses(put('bob'), PLAYING)).toEqual([
      { animation: { kind: 'put', cardId: '5' }, duration: DURATIONS.putCardIntoPlay, when: 'after' },
    ]);
    expect(eventPauses(put('alice'), PLAYING)).toEqual([]);
    expect(eventPauses(put('bob', 'HAND'), PLAYING)).toEqual([]);
    expect(eventPauses(put('bob'), { ...PLAYING, animate: false })).toEqual([]);
    expect(eventPauses(put('alice'), { ...PLAYING, spectator: true })).toHaveLength(1);
  });

  it('shows a decision a while in a replay only', () => {
    const decision = { type: 'D' } as GameEvent;
    expect(eventPauses(decision, PLAYING)).toEqual([]);
    expect(eventPauses(decision, REPLAY)).toEqual([
      { animation: { kind: 'decision' }, duration: DURATIONS.decision, when: 'after' },
    ]);
  });

  it("holds a replay on each message a scripted game's step opens with, as long as it takes to read", () => {
    const opening = step(
      message('<p>One two three</p>'),
      message('<p>Picked</p>', { moment: 'picked', card: 'frodo' }),
      message('Four', { moment: 'start', card: null }),
    );
    expect(eventPauses(opening, REPLAY)).toEqual([
      { animation: { kind: 'read', message: 0 }, duration: 1500 + 3 * 280, when: 'after' },
      { animation: { kind: 'read', message: 1 }, duration: 1500 + 280, when: 'after' },
    ]);
    // Played, the player reads at their own pace; other scenario events don't hold the page
    expect(eventPauses(opening, PLAYING)).toEqual([]);
    expect(eventPauses({ type: 'S', scenario: { type: 'offscript', message: null } }, REPLAY)).toEqual([]);
    expect(eventPauses({ type: 'S', scenario: null }, REPLAY)).toEqual([]);
  });

  it('gives a message 1.5 s and a little more for each word, its markup left out', () => {
    expect(readingTime('')).toBe(1500);
    expect(readingTime('<p>Hello <b>there</b>\n  friend</p>')).toBe(1500 + 3 * 280);
    expect(readingTime('<img src="x.png"/>')).toBe(1500);
  });

  it('halves the durations for each step of the speed setting up, and doubles them for each down', () => {
    expect(speedFactor(0)).toBe(1);
    expect(speedFactor(2)).toBe(0.25);
    expect(speedFactor(-1)).toBe(2);
  });
});
