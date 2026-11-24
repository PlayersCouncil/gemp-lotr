import { cardByAlias, EditBoard, readEditBoard } from './edit-board';
import { EditTool } from './edit-ops';
import {
  deckSites,
  laidSite,
  nextSite,
  siteBound,
  siteFromDeck,
  siteFromSeat,
  siteMenu,
  siteMenuHasEdits,
  siteOfBlueprint,
  sitesNumbered,
} from './edit-path';
import { editSeats } from './edit-seats';
import { SANDBOX_STATE } from '../../../testing/table-edit';
import { SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';

const seats = editSeats({ participantId: 'alice', players: ['alice', '~Scenario'] }, 'freeps');
const names = { freeps: 'Alice', shadow: 'Sandbox' };
const all = () => true;

describe('edit path', () => {
  const board = readEditBoard(SANDBOX_PILES_STATE);

  it('finds the next site not laid, and the site laid at a number', () => {
    expect(nextSite(board)).toBe(3);
    expect(nextSite(readEditBoard(SANDBOX_STATE))).toBe(1);
    expect(nextSite(null)).toBe(1);
    const full: EditBoard = {
      ...board,
      path: Array.from({ length: 9 }, (_, at) => ({ ...board.path![0]!, number: at + 1 })),
    };
    expect(nextSite(full)).toBeNull();
    expect(laidSite(board, 2)?.controlledBy).toBe('shadow');
    expect(laidSite(board, 5)).toBeNull();
    expect(laidSite(null, 1)).toBeNull();
  });

  it("bounds a site's search by its number where the game's sites have numbers", () => {
    expect(sitesNumbered(board)).toBe(true);
    expect(sitesNumbered(readEditBoard({ cards: [] }))).toBe(false);
    expect(sitesNumbered(null)).toBe(false);
    expect(siteBound(4, true)).toEqual({ filter: 'cardType:SITE siteNumber:4', label: 'Site 4' });
    expect(siteBound(4, false)).toEqual({ filter: 'cardType:SITE', label: 'Sites' });
  });

  it("lists a seat's adventure deck by printed number, the other's only with the hidden piles open", () => {
    expect(deckSites(board, 'shadow', 'freeps')?.map((card) => card.alias)).toEqual(['site3opp', 'site4opp']);
    expect(deckSites(board, 'freeps', 'freeps')?.map((card) => card.alias)).toEqual(['site3']);
    const closed: EditBoard = { ...board, hiddenPilesOpen: false };
    expect(deckSites(closed, 'shadow', 'freeps')).toBeNull();
    expect(deckSites(closed, 'freeps', 'freeps')).toHaveLength(1);
    expect(deckSites(null, 'freeps', 'freeps')).toBeNull();
    const same: EditBoard = {
      ...board,
      cards: [
        { ...cardByAlias(board, 'site3')!, alias: 'b', name: 'Beta', printed: null },
        { ...cardByAlias(board, 'site3')!, alias: 'a', name: 'Alpha', printed: null },
      ],
    };
    expect(deckSites(same, 'freeps', 'freeps')?.map((card) => card.alias)).toEqual(['a', 'b']);
  });

  it('lays a site from a deck by name, from a seat by number, or a new one by blueprint', () => {
    expect(siteFromDeck(3, cardByAlias(board, 'site3opp')!)).toEqual({ op: 'site', number: 3, card: 'site3opp' });
    expect(siteFromSeat(4, 'shadow')).toEqual({ op: 'site', number: 4, from: 'shadow' });
    expect(siteOfBlueprint(5, '1_350')).toEqual({ op: 'site', number: 5, blueprint: '1_350' });
  });

  it("gives a laid site its markers, control and Replace…, each seat by its player's name", () => {
    const menu = siteMenu(board, cardByAlias(board, 'site1')!, all, seats, names)!;
    expect(menu).toEqual({
      number: 1,
      markers: [
        { seat: 'freeps', label: 'Alice', here: false },
        { seat: 'shadow', label: 'Sandbox', here: false },
      ],
      control: [
        { seat: 'freeps', label: 'Alice', here: false },
        { seat: 'shadow', label: 'Sandbox', here: false },
      ],
      liberate: false,
      replace: true,
    });
    expect(siteMenuHasEdits(menu)).toBe(true);
    // A controlled site (in the support area, still numbered): who controls it, and Liberate
    const controlled = siteMenu(board, cardByAlias(board, 'site2')!, all, seats, names)!;
    expect(controlled.control.map((seat) => seat.here)).toEqual([false, true]);
    expect(controlled.liberate).toBe(true);
  });

  it('marks where a marker is, and offers only what the tools allow; nothing for a card that is no laid site', () => {
    const at3: EditBoard = { ...board, path: [...board.path!, { ...board.path![0]!, number: 3, alias: 'site3' }] };
    const site3 = { ...cardByAlias(board, 'site3')!, site: 3, zone: 'path' };
    expect(siteMenu(at3, site3, all, seats, names)!.markers.map((seat) => seat.here)).toEqual([true, true]);
    const sitesOnly = (tool: EditTool) => tool === 'sites';
    const menu = siteMenu(board, cardByAlias(board, 'site2')!, sitesOnly, seats, names)!;
    expect(menu).toMatchObject({ markers: [], control: [], liberate: false, replace: true });
    const none = siteMenu(board, cardByAlias(board, 'site1')!, () => false, seats, names)!;
    expect(siteMenuHasEdits(none)).toBe(false);
    expect(siteMenu(board, cardByAlias(board, 'frodo')!, all, seats, names)).toBeNull();
    expect(siteMenu(board, cardByAlias(board, 'site3')!, all, seats, names)).toBeNull();
    expect(
      siteMenu({ ...board, markers: undefined, path: undefined }, cardByAlias(board, 'site1')!, all, seats, names),
    ).toMatchObject({ liberate: false });
  });
});
