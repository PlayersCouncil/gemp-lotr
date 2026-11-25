import { computed, signal } from '@angular/core';
import { EditSeat } from '../core/game/table-edit/edit-ops';
import { OtherSeat } from '../core/game/table-edit/table-seat';

/**
 * A TableFlip for the views' tests (flip table): its signals to set, its calls recorded. Unflipped, the other seat the
 * script's, every choice offered, the table flippable.
 */
export function fakeTableFlip() {
  const seat = signal<EditSeat>('freeps');
  return {
    seat,
    flipped: computed(() => seat() === 'shadow'),
    canFlip: signal(true),
    otherSeat: signal<OtherSeat | null>('script'),
    otherSeats: signal<readonly OtherSeat[]>(['script', 'bot', 'person']),
    answers: signal(true),
    elsewhere: signal<EditSeat | null>(null),
    humanSeat: signal<EditSeat>('freeps'),
    looking: computed(() => (seat() === 'shadow' ? 'As Shadow' : null)),
    flipTitle: computed(() => (seat() === 'shadow' ? 'Back to your side (Free Peoples)' : "Look from Shadow's side")),
    busy: signal(false),
    problem: signal<string | null>(null),
    flip: vi.fn(() => Promise.resolve(true)),
    setOtherSeat: vi.fn(() => Promise.resolve(true)),
  };
}

export type FakeTableFlip = ReturnType<typeof fakeTableFlip>;
