/** Test data of the table changed by hand (core/game/table-edit, ui/game/table-edit). */
import { computed, signal } from '@angular/core';
import { EditBoard, EditLevel, EditMode, readEditBoard } from '../core/game/table-edit/edit-board';
import { EDIT_TOOLS, EditOp, EditTool } from '../core/game/table-edit/edit-ops';
import { EditPoint, EditProblem } from '../core/game/table-edit/table-edit.service';
import { FilledSlot, SlotCard, SlotVerdict } from '../core/game/table-edit/puzzle-backend';
import { readPuzzleSlots } from '../core/game/table-edit/puzzle-slots';
import { of } from 'rxjs';

/** The default sandbox's state as the server sends it (es-notes.md 6, shortened), with a modifier put on Boromir. */
export const SANDBOX_STATE = {
  mode: 'edit',
  level: 'sandbox',
  tools: [
    'add',
    'copy',
    'delete',
    'move',
    'attach',
    'stack',
    'wounds',
    'tokens',
    'modify',
    'counters',
    'markers',
    'sites',
    'piles',
  ],
  canStop: false,
  canStart: true,
  over: false,
  modify: {
    words: [
      { what: 'strength', label: 'Strength', kind: 'stat', number: 'signed', types: ['COMPANION', 'ALLY', 'MINION'] },
      { what: 'resistance', label: 'Resistance', kind: 'stat', number: 'signed', types: ['COMPANION'] },
      { what: 'damage', label: 'Damage', kind: 'keyword', number: 'positive', types: ['COMPANION', 'ALLY', 'MINION'] },
      { what: 'fierce', label: 'Fierce', kind: 'keyword', number: 'none', types: ['MINION'] },
    ],
    until: ['phase', 'turn', 'removed'],
  },
  cards: [
    {
      alias: 'frodo',
      blueprintId: '1_290',
      name: 'Frodo, Son of Drogo',
      type: 'COMPANION',
      owner: 'freeps',
      cardId: 25,
      zone: 'characters',
      at: null,
      wounds: 0,
      tokens: 0,
      on: null,
      modifiers: [],
      cultureTokens: {},
      culture: 'shire',
      site: null,
      printed: null,
      added: false,
    },
    {
      alias: 'ring',
      blueprintId: '1_2',
      name: 'The One Ring, The Ruling Ring',
      type: 'THE_ONE_RING',
      owner: 'freeps',
      cardId: 26,
      zone: 'attached',
      at: null,
      wounds: 0,
      tokens: 0,
      on: 'frodo',
      modifiers: [],
      cultureTokens: {},
      culture: null,
      site: null,
      printed: null,
      added: false,
    },
    {
      alias: 'site1',
      blueprintId: '1_319',
      name: 'Bag End',
      type: 'SITE',
      owner: 'freeps',
      cardId: 24,
      zone: 'path',
      at: null,
      wounds: 0,
      tokens: 0,
      on: null,
      modifiers: [],
      cultureTokens: {},
      culture: null,
      site: 1,
      printed: 1,
      added: false,
    },
    {
      alias: 'boromir',
      blueprintId: '1_97',
      name: 'Boromir, Son of Denethor',
      type: 'COMPANION',
      owner: 'freeps',
      cardId: 30,
      zone: 'characters',
      at: null,
      wounds: 1,
      tokens: 2,
      on: null,
      modifiers: [
        { id: 1, what: 'strength', amount: 2, until: 'turn', text: 'Strength +2', written: 'strength +2 turn' },
      ],
      cultureTokens: { gondor: 2 },
      culture: 'gondor',
      site: null,
      printed: null,
      added: true,
    },
    {
      alias: 'armor',
      blueprintId: '1_92',
      name: 'Armor',
      type: 'POSSESSION',
      owner: 'freeps',
      cardId: 31,
      zone: 'attached',
      at: null,
      wounds: 0,
      tokens: 0,
      on: 'boromir',
      modifiers: [],
      cultureTokens: {},
      culture: 'gondor',
      site: null,
      printed: null,
      added: true,
    },
    {
      alias: 'goblinrunner',
      blueprintId: '1_178',
      name: 'Goblin Runner',
      type: 'MINION',
      owner: 'shadow',
      cardId: 32,
      zone: 'hand',
      at: 0,
      wounds: 0,
      tokens: 0,
      on: null,
      modifiers: [],
      cultureTokens: {},
      culture: 'moria',
      site: null,
      printed: null,
      added: true,
    },
  ],
  piles: {
    freeps: { hand: 0, deck: 0, discard: 0, deadPile: 0, removed: 0, adventureDeck: 8 },
    shadow: { hand: 1, deck: 0, discard: 0, deadPile: 0, removed: 0, adventureDeck: 9 },
  },
  hiddenPilesOpen: true,
  twilight: 0,
  burdens: 0,
  threats: 0,
  cultures: ['dwarven', 'elven', 'gondor', 'shire', 'moria', 'uruk_hai'],
};

/** A TableEdit for the views' tests: its signals to set, and its calls recorded (`apply` makes every edit). */
export function fakeTableEdit(start: { level?: EditLevel; mode?: EditMode; tools?: readonly EditTool[] } = {}) {
  const level = signal<EditLevel>(start.level ?? 'sandbox');
  const mode = signal<EditMode>(start.mode ?? 'edit');
  const tools = signal<readonly EditTool[]>(start.tools ?? EDIT_TOOLS);
  const board = signal<EditBoard | null>(readEditBoard(SANDBOX_STATE));
  return {
    level,
    mode,
    tools,
    board,
    canStop: signal(false),
    canStart: signal(true),
    busy: signal(false),
    problem: signal<EditProblem | null>(null),
    editing: computed(() => level() !== 'none' && mode() === 'edit'),
    may: (tool: EditTool) => level() !== 'none' && mode() === 'edit' && tools().includes(tool),
    apply: vi.fn((ops: readonly EditOp[], at?: EditPoint | null) =>
      Promise.resolve(ops.length > 0 && at !== undefined),
    ),
    stop: vi.fn(() => Promise.resolve(true)),
    start: vi.fn(() => Promise.resolve(true)),
    reset: vi.fn(() => Promise.resolve()),
    refresh: vi.fn(),
    dismissProblem: vi.fn(),
    submit: vi.fn(),
  };
}

export type FakeTableEdit = ReturnType<typeof fakeTableEdit>;

/** Puzzle 10's two slots as the scenario's info event has them (ep-notes.md P3), empty (the game held for picks). */
export const PUZZLE_SLOTS = [
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
  },
  {
    id: 'ally',
    label: 'a Rohan ally',
    zone: 'support',
    seat: 'human',
    on: null,
    position: null,
    bound: 'set:1-6 culture:ROHAN cardtype:ALLY',
    pool: 'set:1-6',
    filter: 'culture:ROHAN cardtype:ALLY',
    overlap: [],
    comes: { tokens: 0, wounds: 0 },
    pick: null,
    pickName: null,
  },
];

/**
 * A puzzle's backend for view specs (PuzzleBackend): puzzle 10's slots, the cards held in them (`picks`), and the
 * server's word on a candidate (`verdict`: fits unless `refuse` names it).
 */
export function fakePuzzle(slots: unknown[] = PUZZLE_SLOTS) {
  const read = signal(readPuzzleSlots(slots));
  const picks = signal<Readonly<Record<string, SlotCard>>>({});
  const refuse = new Set<string>();
  return {
    slots: read,
    picks,
    refuse,
    filled: computed<FilledSlot[]>(() => read().map((slot) => ({ slot, card: picks()[slot.id] ?? null }))),
    missing: computed(() =>
      read()
        .filter((slot) => !picks()[slot.id])
        .map((slot) => slot.label ?? slot.id),
    ),
    verdict: vi.fn((slot: string, blueprint: string) =>
      of<SlotVerdict>(
        refuse.has(blueprint)
          ? { fits: false, why: `${blueprint} does not fit ${slot}.`, name: null }
          : { fits: true, why: null, name: `Card ${blueprint}` },
      ),
    ),
  };
}

export type FakePuzzle = ReturnType<typeof fakePuzzle>;
