import { bothSeats, editSeats, seatOf } from './edit-seats';

describe('edit seats', () => {
  it("gives the person's seat to the page's player and the other to the game's other player", () => {
    const seats = editSeats({ participantId: 'alice', players: ['alice', '~Scenario'] }, 'freeps');
    expect(seats).toEqual({ human: 'freeps', players: { freeps: 'alice', shadow: '~Scenario' } });
    expect(seatOf(seats, 'alice')).toBe('freeps');
    expect(seatOf(seats, '~Scenario')).toBe('shadow');
    expect(seatOf(seats, 'carol')).toBeNull();
    expect(bothSeats(seats)).toEqual(['freeps', 'shadow']);
  });

  it('works from the shadow seat, and before the players are known', () => {
    const seats = editSeats({ participantId: 'bob', players: ['~Scenario', 'bob'] }, 'shadow');
    expect(seats.players).toEqual({ shadow: 'bob', freeps: '~Scenario' });
    expect(bothSeats(seats)).toEqual(['shadow', 'freeps']);
    const unknown = editSeats({ participantId: null, players: [] }, 'freeps');
    expect(unknown.players).toEqual({ freeps: '', shadow: '' });
    expect(seatOf(unknown, '')).toBeNull();
  });
});
