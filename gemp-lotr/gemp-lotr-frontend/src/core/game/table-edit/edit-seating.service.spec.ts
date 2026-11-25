import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { GameSession } from '../session/game-session.service';
import { emptyGame, GameModel } from '../state/game-model';
import { EditSeating } from './edit-seating.service';

describe('EditSeating', () => {
  const info = signal<{ humanSeat: 'freeps' | 'shadow'; seat?: 'freeps' | 'shadow' } | null>({ humanSeat: 'freeps' });
  const model = signal<GameModel>(emptyGame());

  function seating(): EditSeating {
    TestBed.configureTestingModule({
      providers: [
        { provide: ScenarioGuide, useValue: { info } },
        { provide: GameSession, useValue: { model } },
        EditSeating,
      ],
    });
    return TestBed.inject(EditSeating);
  }

  it("seats the page's player in the scenario's seat for them, and names each seat by its player as shown", () => {
    model.set({
      ...emptyGame(),
      participantId: 'alice',
      players: ['alice', '~Scenario'],
      scenario: { info: { opponent: '~Scenario', opponentName: 'Sandbox' } } as GameModel['scenario'],
    });
    const seats = seating();
    expect(seats.seats().players).toEqual({ freeps: 'alice', shadow: '~Scenario' });
    expect(seats.names()).toEqual({ freeps: 'alice', shadow: 'Sandbox' });
    info.set({ humanSeat: 'shadow' });
    expect(seats.seats().human).toBe('shadow');
    expect(seats.names()).toEqual({ freeps: 'Sandbox', shadow: 'alice' });
  });

  it('names a seat by its side before its player is known (the free peoples seat for a game with no scenario)', () => {
    info.set(null);
    model.set(emptyGame());
    expect(seating().names()).toEqual({ freeps: 'Free Peoples', shadow: 'Shadow' });
  });

  it('follows the seat the page looks from: flipped, its own player is the other seat', () => {
    info.set({ humanSeat: 'freeps', seat: 'shadow' });
    model.set({
      ...emptyGame(),
      participantId: '~Scenario',
      players: ['alice', '~Scenario'],
      scenario: { info: { opponent: '~Scenario', opponentName: 'Sandbox' } } as GameModel['scenario'],
    });
    const seats = seating();
    expect(seats.looked()).toBe('shadow');
    expect(seats.seats()).toEqual({ human: 'freeps', players: { freeps: 'alice', shadow: '~Scenario' } });
    expect(seats.names()).toEqual({ freeps: 'alice', shadow: 'Sandbox' });
  });
});
