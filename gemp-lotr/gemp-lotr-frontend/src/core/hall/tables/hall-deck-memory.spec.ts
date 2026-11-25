import { defaultDeck, hasDeck, MAX_REMEMBERED, readDeckMemory, recallDeck, rememberDeck } from './hall-deck-memory';
import { HallDeckOption } from './hall-table-models';

const mine: HallDeckOption[] = [
  { name: 'Elves', formatName: 'Movie Block', source: 'player' },
  { name: 'Orcs', formatName: 'Fellowship Block', source: 'player' },
];
const library: HallDeckOption[] = [{ name: 'Starter', formatName: 'Fellowship Block', source: 'library' }];

describe('hall deck memory', () => {
  it('reads the cookie leniently', () => {
    expect(readDeckMemory(null)).toEqual({});
    expect(readDeckMemory('not json')).toEqual({});
    expect(readDeckMemory('[1]')).toEqual({});
    expect(readDeckMemory('{"movie":["Elves","player"]}')).toEqual({ movie: ['Elves', 'player'] });
  });

  it('remembers per key and overall, the latest last', () => {
    let memory = rememberDeck({}, { name: 'Elves', source: 'player' }, 'movie');
    memory = rememberDeck(memory, { name: 'Starter', source: 'library' }, 'fotr_block');
    expect(Object.keys(memory)).toEqual(['movie', 'fotr_block', '_']);
    expect(recallDeck(memory, 'movie')).toEqual({ name: 'Elves', source: 'player' });
    expect(recallDeck(memory, 'other')).toEqual({ name: 'Starter', source: 'library' });
    expect(recallDeck(memory, null)).toEqual({ name: 'Starter', source: 'library' });
    expect(recallDeck({}, null)).toBeNull();
  });

  it(`keeps at most ${MAX_REMEMBERED} keys, the last deck overall included`, () => {
    let memory = {};
    for (let i = 0; i < 20; i++) {
      memory = rememberDeck(memory, { name: `d${i}`, source: 'player' }, `f${i}`);
    }
    expect(Object.keys(memory).length).toBe(MAX_REMEMBERED);
    expect(recallDeck(memory, 'f0')).toEqual({ name: 'd19', source: 'player' });
    expect(recallDeck(memory, 'f19')).toEqual({ name: 'd19', source: 'player' });
  });

  it('defaults to the first own deck, or a library deck for a player with none', () => {
    expect(defaultDeck(mine, library)).toEqual({ name: 'Elves', source: 'player' });
    expect(defaultDeck([], library)).toEqual({ name: 'Starter', source: 'library' });
    expect(defaultDeck([], [])).toBeNull();
    expect(hasDeck({ name: 'Starter', source: 'player' }, mine, library)).toBe(false);
    expect(hasDeck({ name: 'Starter', source: 'library' }, mine, library)).toBe(true);
  });
});
