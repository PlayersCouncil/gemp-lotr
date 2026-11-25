import {
  answersHere,
  flipTitle,
  isFlipped,
  lookedFrom,
  lookingText,
  readOtherSeat,
  readOtherSeats,
  readSeat,
  SeatInfo,
  waitingElsewhere,
  waitingText,
} from './table-seat';

/** The default sandbox's info as it opens (ef-notes Part 2 E): the person in the freeps seat, the script opposite. */
const SANDBOX: SeatInfo = {
  humanSeat: 'freeps',
  mode: 'play',
  author: false,
  seat: 'freeps',
  otherSeat: 'script',
  otherSeats: ['script', 'bot', 'person'],
  waitingOn: 'freeps',
  canFlip: true,
};

describe('table seats', () => {
  it("reads who plays the other seat in any case, the choices once each in the server's order, and a seat", () => {
    expect(readOtherSeat('Bot')).toBe('bot');
    expect(readOtherSeat('nobody')).toBeNull();
    expect(readOtherSeat(3)).toBeNull();
    expect(readOtherSeats(['person', 'SCRIPT', 'x', 'person'])).toEqual(['person', 'script']);
    expect(readOtherSeats('script')).toEqual([]);
    expect(readSeat('shadow')).toBe('shadow');
    expect(readSeat('human')).toBeNull();
  });

  it("looks from the person's own seat unless the info says another", () => {
    expect(lookedFrom(SANDBOX)).toBe('freeps');
    expect(isFlipped(SANDBOX)).toBe(false);
    expect(lookedFrom({ ...SANDBOX, seat: 'shadow' })).toBe('shadow');
    expect(isFlipped({ ...SANDBOX, seat: 'shadow' })).toBe(true);
    // (an info of an older server says no seat: the person's own)
    expect(lookedFrom({ humanSeat: 'shadow' })).toBe('shadow');
    expect(isFlipped({ humanSeat: 'shadow' })).toBe(false);
    expect(lookedFrom(null)).toBe('freeps');
    expect(isFlipped(null)).toBe(false);
  });

  it('answers what the page is asked unless it looks from a seat that plays itself', () => {
    expect(answersHere(SANDBOX)).toBe(true);
    expect(answersHere(null)).toBe(true);
    const flipped = { ...SANDBOX, seat: 'shadow' as const };
    expect(answersHere(flipped)).toBe(false);
    expect(answersHere({ ...flipped, otherSeat: 'bot' })).toBe(false);
    expect(answersHere({ ...flipped, otherSeat: 'person' })).toBe(true);
    // (an author's table: the authoring tool takes what it has left for the author)
    expect(answersHere({ ...flipped, otherSeat: null, author: true })).toBe(true);
  });

  it('says which seat out of sight is to choose, only where the person plays it, in play mode', () => {
    // The Shadow phase waits on the other seat
    const shadow = { ...SANDBOX, waitingOn: 'shadow' as const };
    expect(waitingElsewhere(shadow)).toBeNull();
    expect(waitingElsewhere({ ...shadow, otherSeat: 'person' })).toBe('shadow');
    expect(waitingElsewhere({ ...shadow, otherSeat: 'person', mode: 'edit' })).toBeNull();
    expect(waitingElsewhere({ ...shadow, otherSeat: 'person', canFlip: false })).toBeNull();
    // Flipped to it, it is what this page is asked
    expect(waitingElsewhere({ ...shadow, otherSeat: 'person', seat: 'shadow' })).toBeNull();
    // Flipped away from the person's own: always theirs (whoever plays the other seat)
    expect(waitingElsewhere({ ...SANDBOX, seat: 'shadow' })).toBe('freeps');
    // Nothing waited on; an author's table (the panel says who is asked)
    expect(waitingElsewhere({ ...SANDBOX, waitingOn: null, seat: 'shadow' })).toBeNull();
    expect(waitingElsewhere({ ...SANDBOX, seat: 'shadow', author: true })).toBeNull();
    expect(waitingElsewhere(null)).toBeNull();
  });

  it('words it short: who is to choose, where Flip turns to, the seat looked from', () => {
    expect(waitingText('shadow')).toBe('Shadow to choose');
    expect(waitingText('freeps')).toBe('Free Peoples to choose');
    expect(flipTitle(SANDBOX)).toBe("Look from Shadow's side");
    expect(flipTitle({ ...SANDBOX, seat: 'shadow' })).toBe('Back to your side (Free Peoples)');
    expect(flipTitle(null)).toBe("Look from Shadow's side");
    expect(lookingText(SANDBOX)).toBeNull();
    expect(lookingText({ ...SANDBOX, seat: 'shadow' })).toBe('As Shadow');
  });
});
