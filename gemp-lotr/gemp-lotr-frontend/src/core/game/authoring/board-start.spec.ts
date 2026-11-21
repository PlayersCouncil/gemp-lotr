import { draftBoard, draftState } from '../../../testing/authoring';
import { DraftStart } from './authoring-state';
import { GatheredBoard } from './authoring-ui-store';
import {
  behind,
  controlledNumbers,
  defaultMoves,
  fightCards,
  gatherControl,
  gatherFight,
  gatherPathSite,
  gatherStart,
  setupWithoutSite,
  siteNumberer,
  skirmishesWithout,
  START_DEFAULT,
  startOf,
  startPartOf,
  startParts,
  startSetup,
  startText,
  startView,
  startWithout,
  undoStart,
} from './board-start';

const start = (extra: Partial<DraftStart> = {}): DraftStart => ({ ...START_DEFAULT, ...extra });
const gathered = (extra: Partial<GatheredBoard> = {}): GatheredBoard => ({
  draft: 'test',
  ops: [],
  added: [],
  removed: [],
  ...extra,
});
const board = draftBoard();

describe('where the story starts', () => {
  it("is the server's, or the start of a game when it says nothing", () => {
    expect(startOf(draftState())).toBe(START_DEFAULT);
    const said = start({ site: 3 });
    expect(startOf(draftState({ start: said }))).toBe(said);
    expect(defaultMoves('fellowship')).toBe(0);
    expect(defaultMoves('maneuver')).toBe(1);
  });

  describe('as it will be', () => {
    it("is the draft's when nothing is gathered, and not late at the start of a game", () => {
      const view = startView(start(), gathered(), false);
      expect(view).toMatchObject({ site: 1, opponentSite: 1, ownOpponentSite: null, phase: 'fellowship', moves: 0 });
      expect(view.late).toBe(false);
      expect([...view.changed]).toEqual([]);
    });

    it('takes the parts gathered, and is late for anything but the start of a game', () => {
      const view = startView(
        start(),
        gathered({ start: { site: 3, phase: 'maneuver', twilight: 2, opponentSite: 2 } }),
        false,
      );
      expect(view).toMatchObject({ site: 3, opponentSite: 2, ownOpponentSite: 2, moves: 1, movesSet: false });
      expect(view.late).toBe(true);
      expect([...view.changed]).toEqual(['site', 'opponentSite', 'phase', 'twilight']);
      for (const late of [{ turn: 2 }, { twilight: 1 }, { site: 2 }, { phase: 'shadow' }]) {
        expect(startView(start(), gathered({ start: late }), false).late).toBe(true);
      }
      expect(startView(start(), gathered({ start: { skirmishes: { frodo: ['orc'] } } }), false).late).toBe(true);
      expect(startView(start(), gathered({ start: { opponentSite: 3 } }), false).late).toBe(false);
    });

    it("keeps the file's moves, or the phase's own; and the Shadow marker level with the fellowship's when together", () => {
      expect(
        startView(start({ phase: 'maneuver', site: 4, moves: 2, movesSet: true }), gathered(), false),
      ).toMatchObject({ moves: 2, movesSet: true, late: true });
      expect(startView(start({ moves: 1 }), gathered({ start: { phase: 'shadow' } }), false).moves).toBe(1);
      expect(startView(start(), gathered({ start: { phase: 'shadow', moves: null } }), false).moves).toBe(1);
      const together = startView(
        start({ markersTogether: true, opponentSite: 2, opponentSiteSet: true }),
        gathered({ start: { site: 5 } }),
        false,
      );
      expect([together.opponentSite, together.ownOpponentSite]).toEqual([5, 2]);
    });

    it('takes, for the setup, the changes to the board that say a part of it', () => {
      const ops = [
        { name: 'marker', args: ['freeps', 4] },
        { name: 'marker', args: ['shadow', 3] },
        { name: 'twilight', args: [5] },
        { name: 'control', args: [2, 'shadow'] },
        { name: 'control', args: [1, 'freeps'] },
        { name: 'liberate', args: ['site1'] },
        { name: 'control', args: ['nowhere', 'shadow'] },
        { name: 'wound', args: ['frodo', 1] },
      ];
      const view = startView(start({ controlled: { shadow: ['1'] } }), gathered({ ops }), true, board);
      expect(view).toMatchObject({ site: 4, opponentSite: 3, twilight: 5, controlled: { shadow: ['2'] } });
      expect([...view.changed].sort()).toEqual(['controlled', 'opponentSite', 'site', 'twilight']);
      expect(startView(start(), gathered({ ops }), false, board).site).toBe(1);
    });
  });

  it('names sites by number, by their place on setup.path, or by their names; the sites behind both markers', () => {
    const numberOf = siteNumberer(board, ['site2opp']);
    expect([numberOf('2'), numberOf('site2opp'), numberOf('site3'), numberOf('site3opp'), numberOf('x')]).toEqual([
      2,
      1,
      3,
      3,
      null,
    ]);
    expect(siteNumberer(null, [])('site3')).toBeNull();
    expect(behind({ site: 4, opponentSite: 3 })).toEqual([1, 2]);
    expect(behind({ site: 1, opponentSite: 3 })).toEqual([]);
    const view = startView(start({ controlled: { shadow: ['1', 'site2', 'gone'] } }), gathered(), false);
    expect([...controlledNumbers(view, 'shadow', board)]).toEqual([1, 2]);
    expect([...controlledNumbers(view, 'freeps', board)]).toEqual([]);
  });

  describe('gathering a part', () => {
    it('gathers it, and lets it go when it is what the draft says', () => {
      let made = gatherStart(gathered(), start(), board, 'site', 3, false);
      expect(made.start).toEqual({ site: 3 });
      made = gatherStart(made, start(), board, 'site', 1, false);
      expect(made.start).toBeUndefined();
      expect(gatherStart(gathered(), start({ turn: 2 }), board, 'turn', 2, false).start).toBeUndefined();
      expect(gatherStart(gathered(), start(), board, 'twilight', 0, false).start).toBeUndefined();
      expect(gatherStart(gathered(), start(), board, 'phase', 'fellowship', false).start).toBeUndefined();
    });

    it('says the Shadow marker at site 1 by saying nothing, and keeps a site 1 when the file said another', () => {
      expect(gatherStart(gathered(), start(), board, 'opponentSite', 1, false).start).toBeUndefined();
      expect(gatherStart(gathered(), start(), board, 'opponentSite', 2, false).start).toEqual({ opponentSite: 2 });
      const said = start({ opponentSite: 2, opponentSiteSet: true });
      expect(gatherStart(gathered(), said, board, 'opponentSite', 1, false).start).toEqual({ opponentSite: null });
      expect(gatherStart(gathered(), said, board, 'opponentSite', 2, false).start).toBeUndefined();
    });

    it("keeps moves only when they are not the phase's own", () => {
      const maneuver = start({ phase: 'maneuver', site: 4, moves: 1 });
      expect(gatherStart(gathered(), maneuver, board, 'moves', 1, false).start).toBeUndefined();
      expect(gatherStart(gathered(), maneuver, board, 'moves', 2, false).start).toEqual({ moves: 2 });
      const twice = start({ phase: 'maneuver', site: 4, moves: 2, movesSet: true });
      expect(gatherStart(gathered(), twice, board, 'moves', 1, false).start).toEqual({ moves: null });
      expect(gatherStart(gathered(), twice, board, 'moves', 2, false).start).toBeUndefined();
    });

    it('takes the skirmishes away with a phase that has none, and the moves back with the Fellowship phase', () => {
      const fighting = start({ phase: 'skirmish', site: 2, moves: 1, skirmishes: { frodo: ['orc'] } });
      expect(gatherStart(gathered(), fighting, board, 'phase', 'assignment', false).start).toEqual({
        phase: 'assignment',
      });
      expect(gatherStart(gathered(), fighting, board, 'phase', 'maneuver', false).start).toEqual({
        phase: 'maneuver',
        skirmishes: {},
      });
      const twice = start({ phase: 'maneuver', site: 4, moves: 2, movesSet: true });
      expect(gatherStart(gathered(), twice, board, 'phase', 'fellowship', false).start).toEqual({
        phase: 'fellowship',
        moves: null,
      });
    });

    it('frees a site no longer behind both markers', () => {
      const held = start({
        site: 4,
        opponentSite: 4,
        opponentSiteSet: true,
        controlled: { shadow: ['1', 'site3', 'x'] },
      });
      expect(gatherStart(gathered(), held, board, 'opponentSite', 2, false).start).toEqual({
        opponentSite: 2,
        controlled: { shadow: ['1', 'x'] },
      });
      expect(gatherStart(gathered(), held, board, 'site', 5, false).start).toEqual({ site: 5 });
    });

    it('lets go, for the setup, of a change to the board that says the same part', () => {
      const ops = [
        { name: 'marker', args: ['freeps', 4] },
        { name: 'marker', args: ['shadow', 3] },
        { name: 'twilight', args: [5] },
        { name: 'control', args: [1, 'shadow'] },
      ];
      expect(gatherStart(gathered({ ops }), start(), board, 'site', 2, true).ops.map((op) => op.args[0])).toEqual([
        'shadow',
        5,
        1,
      ]);
      expect(gatherStart(gathered({ ops }), start(), board, 'site', 2, false).ops).toHaveLength(4);
      expect(gatherStart(gathered({ ops }), start(), board, 'twilight', 1, true).ops).toHaveLength(3);
      // (the site controlled is no longer behind the Shadow marker: what the changes said of it goes with it)
      const freed = gatherStart(gathered({ ops }), start(), board, 'opponentSite', 1, true);
      expect(freed.ops.map((op) => op.name)).toEqual(['marker', 'twilight']);
      expect(freed.start).toBeUndefined();
      expect(startPartOf('marker', ['freeps', 2])).toBe('site');
      expect(startPartOf('marker', ['shadow', 2])).toBe('opponentSite');
      expect(startPartOf('liberate', [2])).toBe('controlled');
      expect(startPartOf('wound', ['frodo', 1])).toBeNull();
    });

    it('ticks a site for one seat (out of the other), and unticks it', () => {
      const past = start({ site: 4, opponentSite: 4, opponentSiteSet: true, controlled: { freeps: ['site2'] } });
      let made = gatherControl(gathered(), past, board, 'shadow', 2, true, false);
      expect(made.start).toEqual({ controlled: { shadow: ['2'] } });
      made = gatherControl(made, past, board, 'shadow', 1, true, false);
      expect(made.start?.controlled).toEqual({ shadow: ['2', '1'] });
      made = gatherControl(made, past, board, 'shadow', 2, false, false);
      expect(made.start?.controlled).toEqual({ shadow: ['1'] });
      expect(gatherControl(gathered(), past, board, 'freeps', 2, true, false).start).toBeUndefined();
      // (a change to the board for the setup said it: it is in what is gathered now, and goes)
      const ops = [{ name: 'control', args: [3, 'shadow'] }];
      made = gatherControl(gathered({ ops }), past, board, 'shadow', 1, true, true);
      expect(made.ops).toEqual([]);
      expect(made.start?.controlled).toEqual({ freeps: ['site2'], shadow: ['3', '1'] });
    });

    it('puts a minion in one skirmish only, and takes it out', () => {
      const fighting = start({ phase: 'assignment', site: 2, skirmishes: { frodo: ['orc'] } });
      let made = gatherFight(gathered(), fighting, board, 'aragorn', 'orc', true);
      expect(made.start).toEqual({ skirmishes: { aragorn: ['orc'] } });
      made = gatherFight(made, fighting, board, 'aragorn', 'troll', true);
      expect(made.start?.skirmishes).toEqual({ aragorn: ['orc', 'troll'] });
      made = gatherFight(made, fighting, board, 'aragorn', 'orc', false);
      expect(made.start?.skirmishes).toEqual({ aragorn: ['troll'] });
      expect(gatherFight(gathered(), fighting, board, 'frodo', 'orc', true).start).toBeUndefined();
    });

    it("lays another seat's site at a place, the Free Peoples' own being said by saying nothing at the end", () => {
      let made = gatherPathSite(gathered(), start({ site: 3 }), board, 2, 'site2opp');
      expect(made.start).toEqual({ path: ['site1', 'site2opp'] });
      made = gatherPathSite(made, start({ site: 3 }), board, 3, 'site3opp');
      expect(made.start?.path).toEqual(['site1', 'site2opp', 'site3opp']);
      made = gatherPathSite(made, start({ site: 3 }), board, 3, '');
      expect(made.start?.path).toEqual(['site1', 'site2opp']);
      made = gatherPathSite(made, start({ site: 3 }), board, 2, '');
      expect(made.start).toBeUndefined();
      expect(gatherPathSite(gathered(), start(), null, 1, 'x').start).toEqual({ path: ['x'] });
    });
  });

  it('lists, undoes and says the parts gathered', () => {
    const made = gathered({ start: { twilight: 3, site: 2 } });
    expect(startParts(made)).toEqual(['site', 'twilight']);
    expect(startParts(gathered())).toEqual([]);
    expect(undoStart(made, 'site').start).toEqual({ twilight: 3 });
    expect(undoStart(undoStart(made, 'site'), 'twilight').start).toBeUndefined();
    const said = {
      site: 3,
      opponentSite: null,
      phase: 'maneuver',
      turn: 2,
      moves: 2,
      twilight: 4,
      skirmishes: { frodo: ['orc', 'troll'], sam: ['runner'] },
      controlled: { shadow: ['1', '2'], freeps: ['site1'] },
      path: ['site1', 'site2opp'],
    };
    expect(
      (['site', 'opponentSite', 'phase', 'turn', 'moves', 'twilight', 'skirmishes', 'controlled', 'path'] as const).map(
        (part) => startText(part, said),
      ),
    ).toEqual([
      'start at site 3',
      'Shadow marker at site 1',
      'start in the maneuver phase',
      'start on turn 2',
      'moves this turn: 2',
      'twilight 4 at the start',
      'skirmishes: frodo: orc + troll; sam: runner',
      'controlled: freeps site1; shadow 1, 2',
      'path: site1, site2opp',
    ]);
    const none = { moves: null, skirmishes: {}, controlled: {}, path: [], opponentSite: 2 };
    expect(
      (['moves', 'skirmishes', 'controlled', 'path', 'opponentSite'] as const).map((part) => startText(part, none)),
    ).toEqual([
      'moves this turn: as usual',
      'no skirmishes at the start',
      'no sites controlled at the start',
      "path: the Free Peoples' sites",
      'Shadow marker at site 2',
    ]);
    expect(startText('skirmishes', {})).toBe('no skirmishes at the start');
    expect(startText('path', {})).toBe("path: the Free Peoples' sites");
  });

  it('lets go of a card taken out (or not known) from the skirmishes gathered', () => {
    const made = gathered({ start: { skirmishes: { frodo: ['orc', 'troll'], sam: ['runner'] } } });
    expect(startWithout(made, (alias) => alias !== 'orc').start?.skirmishes).toEqual({
      frodo: ['troll'],
      sam: ['runner'],
    });
    expect(startWithout(made, (alias) => alias !== 'sam').start?.skirmishes).toEqual({ frodo: ['orc', 'troll'] });
    expect(startWithout(made, () => true)).toBe(made);
    const none = gathered();
    expect(startWithout(none, () => false)).toBe(none);
  });

  describe('the setup it writes', () => {
    it('is the setup as it is when nothing of the start is gathered', () => {
      const setup = { burdens: 2 };
      expect(startSetup(setup, start(), gathered())).toBe(setup);
    });

    it("writes startAt's parts that are not their default, in the writer's order, with the twilight", () => {
      const written = startSetup(
        { freeps: { hand: 'sam' }, startAt: 'maneuver', twilight: 2 },
        start(),
        gathered({
          start: {
            twilight: 4,
            moves: 2,
            turn: 3,
            phase: 'maneuver',
            opponentSite: 2,
            site: 4,
            skirmishes: { frodo: ['orc', 'troll'] },
            controlled: { shadow: ['1'] },
            path: ['site1', 'site2opp'],
          },
        }),
      );
      expect(written).toEqual({
        freeps: { hand: 'sam' },
        startAt: { site: 4, opponentSite: 2, phase: 'maneuver', turn: 3, moves: 2, twilight: 4 },
        skirmishes: { frodo: 'orc + troll' },
        controlled: { shadow: '1' },
        path: 'site1, site2opp',
      });
      expect(Object.keys((written as { startAt: object }).startAt)).toEqual([
        'site',
        'opponentSite',
        'phase',
        'turn',
        'moves',
        'twilight',
      ]);
    });

    it('writes the pool alone as setup.twilight, and leaves no setup when nothing is left', () => {
      expect(startSetup(null, start(), gathered({ start: { twilight: 3 } }))).toEqual({ twilight: 3 });
      expect(startSetup({ startAt: { site: 3 } }, start({ site: 3 }), gathered({ start: { site: 1 } }))).toBeNull();
      expect(
        startSetup(
          { skirmishes: { frodo: 'orc' }, controlled: { shadow: '1' }, path: 'site1' },
          start({ skirmishes: { frodo: ['orc'] }, controlled: { shadow: ['1'] }, path: ['site1'] }),
          gathered({ start: { skirmishes: {}, controlled: {}, path: [] } }),
        ),
      ).toBeNull();
    });
  });

  it('takes a card out of the skirmishes the setup has (its own with it)', () => {
    const skirmishes = { frodo: 'orc + troll', sam: 'runner' };
    expect(skirmishesWithout(skirmishes, 'orc')).toEqual({ frodo: 'troll', sam: 'runner' });
    expect(skirmishesWithout(skirmishes, 'frodo')).toEqual({ sam: 'runner' });
    expect(skirmishesWithout({ sam: 'runner' }, 'runner')).toBeNull();
    expect(skirmishesWithout(undefined, 'runner')).toBeNull();
  });

  it('takes a site off the path out of the sites controlled, and lays setup.path only up to it', () => {
    const setup = {
      burdens: 1,
      controlled: { shadow: 'site1, site3opp', freeps: 'site3opp' },
      path: 'site1, site2opp, site3opp',
    };
    expect(setupWithoutSite(setup, 'site3opp')).toEqual({
      burdens: 1,
      controlled: { shadow: 'site1' },
      path: 'site1, site2opp',
    });
    expect(setupWithoutSite(setup, 'site1')).toEqual({
      burdens: 1,
      controlled: { shadow: 'site3opp', freeps: 'site3opp' },
    });
    expect(setupWithoutSite({ path: 'site2opp' }, 'site2opp')).toBeNull();
    expect(setupWithoutSite(undefined, 'site2')).toBeNull();
  });

  it("offers for a skirmish the setup's Free Peoples characters (the Ring-bearer first) and its minions", () => {
    const draft = {
      setup: { freeps: { characters: 'aragorn, frodo' }, shadow: { minions: 'orc, troll' } },
      ringBearer: { freeps: 'frodo' },
    };
    expect(fightCards(draft, gathered())).toEqual({ characters: ['frodo', 'aragorn'], minions: ['orc', 'troll'] });
    const add = (alias: string, seat: 'freeps' | 'shadow', zone: string) => ({
      alias,
      seat,
      zone,
      blueprintId: '1_1',
      name: alias,
      type: 'MINION',
      host: null,
    });
    expect(
      fightCards(
        draft,
        gathered({
          added: [add('runner', 'shadow', 'minions'), add('gimli', 'freeps', 'characters'), add('x', 'shadow', 'hand')],
          removed: ['troll', 'frodo'],
        }),
      ),
    ).toEqual({ characters: ['aragorn', 'gimli'], minions: ['orc', 'runner'] });
    expect(fightCards({}, gathered())).toEqual({ characters: [], minions: [] });
  });
});
