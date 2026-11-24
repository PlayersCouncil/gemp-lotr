import { cardByAlias, cardById, editLevel, editMode, editTools, onTable, readEditBoard } from './edit-board';
import { SANDBOX_STATE } from '../../../testing/table-edit';

describe('readEditBoard', () => {
  it('reads the state the sandbox sends', () => {
    const board = readEditBoard(SANDBOX_STATE);
    expect(board.mode).toBe('edit');
    expect(board.level).toBe('sandbox');
    expect(board.tools).toHaveLength(13);
    expect(board.canStop).toBe(false);
    expect(board.canStart).toBe(true);
    expect(board.over).toBe(false);
    expect(board.cards).toHaveLength(6);
    expect(board.cards[3]).toEqual({
      alias: 'boromir',
      blueprintId: '1_97',
      name: 'Boromir, Son of Denethor',
      type: 'COMPANION',
      owner: 'freeps',
      cardId: '30',
      zone: 'characters',
      wounds: 1,
      cultureTokens: { gondor: 2 },
      culture: 'gondor',
      on: null,
      modifiers: [{ id: 1, what: 'strength', amount: 2, until: 'turn', text: 'Strength +2' }],
      added: true,
    });
    expect(board.piles.shadow['hand']).toBe(1);
    expect(board.hiddenPilesOpen).toBe(true);
    expect(board.cultures).toContain('uruk_hai');
    expect(board.modify.words.map((word) => [word.what, word.kind, word.number])).toEqual([
      ['strength', 'stat', 'signed'],
      ['resistance', 'stat', 'signed'],
      ['damage', 'keyword', 'positive'],
      ['fierce', 'keyword', 'none'],
    ]);
    expect(board.modify.until).toEqual(['phase', 'turn', 'removed']);
  });

  it('reads anything else as an empty table in play mode, leaving out what it cannot read', () => {
    const board = readEditBoard('nonsense');
    expect(board).toEqual(
      expect.objectContaining({ mode: 'play', level: 'none', tools: [], canStop: false, canStart: false, cards: [] }),
    );
    expect(board.modify.until).toEqual(['phase', 'turn', 'removed']);
    const odd = readEditBoard({
      cards: [
        null,
        { alias: 'x' },
        { alias: 'x', blueprintId: '1_1', owner: 'nobody' },
        {
          alias: 'y',
          blueprintId: '1_1',
          owner: 'shadow',
          cardId: 'extra7',
          cultureTokens: { moria: 'two', sauron: 1 },
          modifiers: [{ id: 'a' }, { id: 3, what: 'fierce', until: 'never' }],
        },
      ],
      modify: { words: [{ label: 'nameless' }, { what: 'odd', kind: 'other', number: 'many' }], until: ['soon'] },
      piles: { freeps: { hand: 'one' } },
    });
    expect(odd.cards).toEqual([
      {
        alias: 'y',
        blueprintId: '1_1',
        name: '1_1',
        type: '',
        owner: 'shadow',
        cardId: 'extra7',
        zone: '',
        wounds: 0,
        cultureTokens: { sauron: 1 },
        culture: null,
        on: null,
        modifiers: [{ id: 3, what: 'fierce', amount: null, until: 'removed', text: 'fierce' }],
        added: false,
      },
    ]);
    expect(odd.modify.words).toEqual([{ what: 'odd', label: 'odd', kind: 'keyword', number: 'none', types: [] }]);
    expect(odd.modify.until).toEqual(['phase', 'turn', 'removed']);
    expect(odd.piles.freeps).toEqual({});
  });

  it('reads the mode, the level and the tools as the info event gives them too', () => {
    expect(editMode('edit')).toBe('edit');
    expect(editMode('stopped')).toBe('play');
    expect(editLevel('puzzle')).toBe('puzzle');
    expect(editLevel('god')).toBe('none');
    expect(editTools(['piles', 'add', 'paint', 'add'])).toEqual(['add', 'piles']);
    expect(editTools('add')).toEqual([]);
  });

  it('finds a card by its id or its name, and says whether it is in play', () => {
    const board = readEditBoard(SANDBOX_STATE);
    expect(cardById(board, '31')?.alias).toBe('armor');
    expect(cardById(board, '99')).toBeNull();
    expect(cardById(null, '31')).toBeNull();
    expect(cardByAlias(board, 'boromir')?.cardId).toBe('30');
    expect(cardByAlias(board, null)).toBeNull();
    expect(cardByAlias(null, 'boromir')).toBeNull();
    expect(board.cards.map((card) => onTable(card))).toEqual([true, true, false, true, true, false]);
  });
});
