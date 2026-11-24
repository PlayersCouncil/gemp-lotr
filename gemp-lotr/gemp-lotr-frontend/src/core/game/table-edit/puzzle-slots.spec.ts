import {
  comesText,
  emptySlots,
  PuzzleSlot,
  readPuzzleSlots,
  slotLabel,
  slotPlace,
  slotRow,
  slotSearch,
} from './puzzle-slots';

/** Puzzle 10's first slot as the info event has it (ep-notes.md P3). */
const CONDITION = {
  id: 'condition',
  label: 'a Rohan condition',
  zone: 'support',
  seat: 'human',
  on: null,
  position: null,
  bound: 'set:1-6 culture:ROHAN cardtype:CONDITION',
  pool: 'set:1-6',
  filter: 'culture:ROHAN cardtype:CONDITION',
  overlap: [],
  comes: { tokens: 0, wounds: 0 },
  pick: null,
  pickName: null,
};

const slot = (extra: Partial<PuzzleSlot> = {}): PuzzleSlot => ({ ...readPuzzleSlots([CONDITION])[0]!, ...extra });

describe('readPuzzleSlots', () => {
  it('reads each slot of the info event', () => {
    expect(readPuzzleSlots([CONDITION])).toEqual([
      {
        id: 'condition',
        label: 'a Rohan condition',
        zone: 'support',
        seat: 'human',
        on: null,
        position: null,
        bound: 'set:1-6 culture:ROHAN cardtype:CONDITION',
        pool: 'set:1-6',
        filter: 'culture:ROHAN cardtype:CONDITION',
        overlap: [],
        comes: { tokens: 0, wounds: 0 },
        pick: null,
        pickName: null,
        siteNumber: null,
      },
    ]);
  });

  it('reads what a slot says defensively, leaving out a slot without an id or a zone', () => {
    const [read] = readPuzzleSlots([
      { id: 'x', zone: 'deck', seat: 'opponent', position: 3, overlap: ['set', 4], comes: { tokens: 2.5, wounds: -1 } },
      { id: 'y' },
      { zone: 'hand' },
      'junk',
    ]);
    expect(read).toEqual(
      expect.objectContaining({
        id: 'x',
        label: null,
        seat: 'opponent',
        position: 3,
        bound: '',
        overlap: ['set'],
        comes: { tokens: 2, wounds: 0 },
      }),
    );
    expect(readPuzzleSlots('nope')).toEqual([]);
    expect(readPuzzleSlots([{ id: 'z', zone: 'hand', pool: 'set:1', filter: 'name:x' }])[0]!.bound).toBe(
      'set:1 name:x',
    );
  });

  it("knows a site slot's number from the server, or from its name", () => {
    expect(readPuzzleSlots([{ id: 'site2', zone: 'site' }])[0]!.siteNumber).toBe(2);
    expect(readPuzzleSlots([{ id: 'keep', zone: 'site', siteNumber: 3 }])[0]!.siteNumber).toBe(3);
    expect(readPuzzleSlots([{ id: 'keep', zone: 'site' }])[0]!.siteNumber).toBeNull();
    expect(readPuzzleSlots([{ id: 'site2', zone: 'hand' }])[0]!.siteNumber).toBeNull();
  });
});

describe('a slot on the page', () => {
  it('is called by its label, else its name', () => {
    expect(slotLabel(slot())).toBe('a Rohan condition');
    expect(slotLabel(slot({ label: null }))).toBe('condition');
  });

  it('says where its card goes in a word or two', () => {
    expect(slotPlace(slot())).toBe('Support');
    expect(slotPlace(slot({ zone: 'deck', position: 3 }))).toBe('Deck, 3rd');
    expect(slotPlace(slot({ zone: 'deck', position: null }))).toBe('Deck, 1st');
    expect(slotPlace(slot({ zone: 'deck', position: 2 }))).toBe('Deck, 2nd');
    expect(slotPlace(slot({ zone: 'deck', position: 11 }))).toBe('Deck, 11th');
    expect(slotPlace(slot({ zone: 'deck', position: 24 }))).toBe('Deck, 24th');
    expect(slotPlace(slot({ zone: 'attached', on: 'frodo' }))).toBe('On frodo');
    expect(slotPlace(slot({ zone: 'stacked', on: 'smeagol' }))).toBe('Under smeagol');
    expect(slotPlace(slot({ zone: 'stacked' }))).toBe('Under ?');
    expect(slotPlace(slot({ zone: 'site', siteNumber: 2, seat: 'opponent' }))).toBe('Site 2');
    expect(slotPlace(slot({ zone: 'site' }))).toBe('Site');
    expect(slotPlace(slot({ zone: 'deadPile', seat: 'opponent' }))).toBe('Theirs: Dead pile');
    expect(slotPlace(slot({ zone: 'elsewhere' }))).toBe('elsewhere');
  });

  it('is shown in its row of the table, or (no row for its place) in the slots strip', () => {
    expect(slotRow(slot())).toBe('supportPlayer');
    expect(slotRow(slot({ seat: 'opponent' }))).toBe('supportOpponent');
    expect(slotRow(slot({ zone: 'characters' }))).toBe('charactersPlayer');
    expect(slotRow(slot({ zone: 'characters', seat: 'opponent' }))).toBe('charactersOpponent');
    expect(slotRow(slot({ zone: 'minions', seat: 'opponent' }))).toBe('minions');
    expect(slotRow(slot({ zone: 'hand' }))).toBe('hand');
    for (const zone of ['deck', 'discard', 'deadPile', 'attached', 'stacked', 'site']) {
      expect(slotRow(slot({ zone }))).toBeNull();
    }
    expect(slotRow(slot({ zone: 'hand', seat: 'opponent' }))).toBeNull();
  });

  it('searches within its bound, labelled as it is called', () => {
    expect(slotSearch(slot())).toEqual({
      filter: 'set:1-6 culture:ROHAN cardtype:CONDITION',
      exact: true,
      label: 'a Rohan condition',
    });
  });

  it('says what it comes with', () => {
    expect(comesText({ tokens: 3, wounds: 0 })).toBe('3 tokens');
    expect(comesText({ tokens: 1, wounds: 1 })).toBe('1 token, 1 wound');
    expect(comesText({ tokens: 0, wounds: 2 })).toBe('2 wounds');
    expect(comesText({ tokens: 0, wounds: 0 })).toBeNull();
  });

  it('lists the slots still empty', () => {
    const ally = slot({ id: 'ally', label: 'a Rohan ally' });
    expect(emptySlots([slot(), ally], {})).toEqual(['a Rohan condition', 'a Rohan ally']);
    expect(emptySlots([slot(), ally], { ally: { blueprintId: '4_262' } })).toEqual(['a Rohan condition']);
  });
});
