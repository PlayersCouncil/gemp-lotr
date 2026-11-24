/** Test data of a sandbox's piles, path and counters (core/game/table-edit edit-piles, edit-path; their controls). */
import { Provider, signal } from '@angular/core';
import { EditSeating } from '../core/game/table-edit/edit-seating.service';
import { editSeats } from '../core/game/table-edit/edit-seats';
import { CounterPopups } from '../ui/game/table-edit/counter-popups';
import { PathPopups } from '../ui/game/table-edit/path-popups';
import { SANDBOX_STATE } from './table-edit';

const card = (alias: string, fields: Record<string, unknown>) => ({
  alias,
  wounds: 0,
  tokens: 0,
  on: null,
  modifiers: [],
  cultureTokens: {},
  culture: null,
  site: null,
  printed: null,
  added: true,
  at: null,
  ...fields,
});

/**
 * SANDBOX_STATE with piles in it (two cards in the freeps draw deck, Legolas on top; the Goblin Runner in the shadow
 * hand; Armor in the shadow discard pile; an adventure deck of each seat), site 2 laid and controlled by the shadow
 * seat, both markers at site 3, and counters that are not 0.
 */
export const SANDBOX_PILES_STATE = {
  ...SANDBOX_STATE,
  twilight: 3,
  burdens: 1,
  threats: 0,
  cards: [
    ...SANDBOX_STATE.cards,
    card('boromir2', {
      blueprintId: '1_97',
      name: 'Boromir, Son of Denethor',
      type: 'COMPANION',
      owner: 'freeps',
      cardId: 41,
      zone: 'deck',
      at: 1,
    }),
    card('legolas', {
      blueprintId: '1_50',
      name: 'Legolas, Greenleaf',
      type: 'COMPANION',
      owner: 'freeps',
      cardId: 40,
      zone: 'deck',
      at: 0,
    }),
    card('armor2', {
      blueprintId: '1_92',
      name: 'Armor',
      type: 'POSSESSION',
      owner: 'shadow',
      cardId: 42,
      zone: 'discard',
      at: 0,
    }),
    card('site2', {
      blueprintId: '1_331',
      name: 'Ettenmoors',
      type: 'SITE',
      owner: 'freeps',
      cardId: 14,
      zone: 'support',
      site: 2,
      printed: 2,
      added: false,
    }),
    card('site3', {
      blueprintId: '1_341',
      name: 'Rivendell Valley',
      type: 'SITE',
      owner: 'freeps',
      cardId: 15,
      zone: 'adventureDeck',
      printed: 3,
      added: false,
    }),
    card('site4opp', {
      blueprintId: '1_343',
      name: "Balin's Tomb",
      type: 'SITE',
      owner: 'shadow',
      cardId: 33,
      zone: 'adventureDeck',
      printed: 4,
      added: false,
    }),
    card('site3opp', {
      blueprintId: '1_341',
      name: 'Rivendell Valley',
      type: 'SITE',
      owner: 'shadow',
      cardId: 34,
      zone: 'adventureDeck',
      printed: 3,
      added: false,
    }),
    card('nocard', {
      blueprintId: '1_1',
      name: 'No id',
      type: 'EVENT',
      owner: 'freeps',
      cardId: null,
      zone: 'deck',
      at: 2,
    }),
  ],
  piles: {
    freeps: { hand: 0, deck: 3, discard: 0, deadPile: 0, removed: 0, adventureDeck: 1 },
    shadow: { hand: 1, deck: 0, discard: 1, deadPile: 0, removed: 0, adventureDeck: 2 },
  },
  markers: { freeps: 3, shadow: 3, nobody: 9 },
  currentSite: 3,
  path: [
    {
      number: 2,
      alias: 'site2',
      blueprintId: '1_331',
      name: 'Ettenmoors',
      printed: 2,
      owner: 'freeps',
      controlledBy: 'shadow',
    },
    {
      number: 1,
      alias: 'site1',
      blueprintId: '1_319',
      name: 'Bag End',
      printed: 1,
      owner: 'freeps',
      controlledBy: null,
    },
    { alias: 'nonumber' },
  ],
};

/** An EditSeating for the views' tests: alice in the freeps seat, the scripted opponent "Sandbox" in the shadow one. */
export function fakeEditSeating() {
  return {
    seats: signal(editSeats({ participantId: 'alice', players: ['alice', '~Scenario'] }, 'freeps')),
    names: signal({ freeps: 'alice', shadow: 'Sandbox' }),
    looked: signal<'freeps' | 'shadow'>('freeps'),
  };
}

/**
 * The providers a view of the path's or the counters' controls needs beside a fake TableEdit: PathPopups,
 * CounterPopups and a fake EditSeating.
 */
export function providePathTesting(): Provider[] {
  return [PathPopups, CounterPopups, { provide: EditSeating, useValue: fakeEditSeating() }];
}
