import { Draft } from './authoring-state';
import {
  ENDINGS,
  endingMessages,
  endingResult,
  messageObject,
  messageValue,
  outcomeWithEnding,
} from './outcome-endings';

const draft = (outcome?: Draft['outcome']): Draft => ({ id: 'x', ...(outcome ? { outcome } : {}) });

/** An outcome with every part set. */
const FULL_OUTCOME: NonNullable<Draft['outcome']> = {
  win: [{ type: 'canSpot', filter: 'name(Aragorn)' }],
  lose: { type: 'hasCardInDeadPile', filter: 'companion' },
  loseMessage: 'Alas.',
  offScript: { message: 'Off you go.', result: 'fail' },
  winMessages: ['Won.'],
  loseMessages: [{ header: 'Lost', text: 'Lost.', require: { type: 'canSpot', filter: 'orc' } } as never],
  gameWon: { result: 'complete', messages: ['Hurrah.'] },
  gameLost: 'complete',
};

describe('outcome-endings', () => {
  it('has the four endings, two of them with a result of their own', () => {
    expect(ENDINGS.map((ending) => [ending.kind, ending.stock])).toEqual([
      ['winMessages', null],
      ['loseMessages', null],
      ['gameWon', 'complete'],
      ['gameLost', 'fail'],
    ]);
  });

  it('reads a message as an object and writes plain text back as text', () => {
    expect(messageObject('Hi')).toEqual({ text: 'Hi' });
    expect(messageObject({ text: 'Hi', header: 'H' })).toEqual({ text: 'Hi', header: 'H' });
    expect(messageValue({ text: 'Hi' })).toBe('Hi');
    expect(messageValue({ text: 'Hi', header: undefined })).toBe('Hi');
    expect(messageValue({ text: 'Hi', header: 'H' })).toEqual({ text: 'Hi', header: 'H' });
  });

  it("reads each ending's messages and result, as written or by default", () => {
    expect(endingMessages(undefined, 'winMessages')).toEqual([]);
    expect(endingMessages({ winMessages: 'Just this' }, 'winMessages')).toEqual([{ text: 'Just this' }]);
    expect(endingMessages(FULL_OUTCOME, 'gameWon')).toEqual([{ text: 'Hurrah.' }]);
    expect(endingMessages(FULL_OUTCOME, 'gameLost')).toEqual([]);
    expect(endingMessages({ gameLost: { result: 'fail', messages: { text: 'One' } } }, 'gameLost')).toEqual([
      { text: 'One' },
    ]);
    expect(endingMessages({ gameLost: { result: 'fail' } }, 'gameLost')).toEqual([]);
    expect(endingResult(undefined, 'gameLost')).toBe('fail');
    expect(endingResult(undefined, 'gameWon')).toBe('complete');
    expect(endingResult(FULL_OUTCOME, 'gameLost')).toBe('complete');
    expect(endingResult(FULL_OUTCOME, 'gameWon')).toBe('complete');
    expect(endingResult({ gameLost: {} as never }, 'gameLost')).toBe('complete');
    expect(endingResult(FULL_OUTCOME, 'winMessages')).toBeNull();
  });

  it('changes one ending and copies every other part of the outcome as it is', () => {
    const changed = outcomeWithEnding(draft(FULL_OUTCOME), 'gameLost', {
      messages: [{ header: 'The end', text: 'Frodo has fallen.' }],
    });
    expect(changed).toEqual({
      ...FULL_OUTCOME,
      gameLost: { result: 'complete', messages: [{ header: 'The end', text: 'Frodo has fallen.' }] },
    });
    const won = outcomeWithEnding(draft(FULL_OUTCOME), 'winMessages', { messages: [] });
    const rest = { ...FULL_OUTCOME };
    delete rest.winMessages;
    expect(won).toEqual(rest);
  });

  it('writes a game ending as little as it can: taken out at its stock result, a result alone, or both', () => {
    expect(outcomeWithEnding(draft(), 'gameLost', { result: 'fail' })).toBeNull();
    expect(outcomeWithEnding(draft(), 'gameLost', { result: 'complete' })).toEqual({ gameLost: 'complete' });
    expect(outcomeWithEnding(draft(), 'gameLost', { messages: [{ text: 'Lost.' }] })).toEqual({
      gameLost: { result: 'fail', messages: ['Lost.'] },
    });
    expect(outcomeWithEnding(draft({ gameWon: 'fail' }), 'gameWon', { messages: [{ text: 'W' }] })).toEqual({
      gameWon: { result: 'fail', messages: ['W'] },
    });
    expect(
      outcomeWithEnding(draft({ gameWon: { result: 'complete', messages: ['W'] } }), 'gameWon', { result: 'fail' }),
    ).toEqual({
      gameWon: { result: 'fail', messages: ['W'] },
    });
    expect(outcomeWithEnding(draft(), 'loseMessages', { messages: [{ text: 'L' }] })).toEqual({ loseMessages: ['L'] });
    expect(outcomeWithEnding(draft({ loseMessages: ['L'] }), 'loseMessages', {})).toEqual({ loseMessages: ['L'] });
  });
});
