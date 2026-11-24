import { readServerXml } from '../../../testing/files';
import { HallItemChange } from '../hall-models';
import { parseHallUpdate } from '../hall-parsers';
import { RowOrder } from '../hall-row-order';
import { HallTablesState } from './hall-tables-state';

/** One counter for every apply, as HallBoard has. */
const order = new RowOrder();

const changesOf = (file: string) => parseHallUpdate(readServerXml(file), false)!.tables;

const add = (id: string, status: string, extra: Record<string, string> = {}): HallItemChange => ({
  action: 'add',
  id,
  attributes: { status, players: 'p' + id, ...extra },
});
const update = (id: string, status: string, extra: Record<string, string> = {}): HallItemChange => ({
  ...add(id, status, extra),
  action: 'update',
});
const remove = (id: string): HallItemChange => ({ action: 'remove', id, attributes: {} });

const ids = (state: HallTablesState) => {
  const { waiting, playing, finished } = state.sections();
  const of = (rows: typeof waiting) => rows.map((row) => row.table.id);
  return { waiting: of(waiting), playing: of(playing), finished: of(finished) };
};

describe('HallTablesState', () => {
  it('draws a snapshot into its sections', () => {
    const state = HallTablesState.EMPTY.apply(changesOf('hall.xml'), true, order);
    expect(ids(state)).toEqual({ waiting: ['11'], playing: ['12'], finished: [] });
    const [row] = state.sections().playing;
    expect(row.action).toBe('watch');
    expect(row.gameUrl).toBe('/gemp-lotr/ng/game?gameId=g-12');
    expect(state.sections().waiting[0]).toMatchObject({ action: 'leave', note: '[new player, go easy]' });
  });

  it('applies incremental updates: moves, removals and additions', () => {
    let state = HallTablesState.EMPTY.apply(changesOf('hall.xml'), true, order);
    state = state.apply(changesOf('hall-update.xml'), false, order);
    expect(ids(state)).toEqual({ waiting: [], playing: ['11'], finished: [] });
    expect(state.get('11')).toMatchObject({ players: ['alice', 'erin'], gameId: 'g-11' });
    expect(state.sections().playing[0].action).toBe('play');
    expect(state.get('12')).toBeNull();
  });

  it('keeps rows in arrival order, updates in place, and moves a row that changes section to its end', () => {
    let state = HallTablesState.EMPTY.apply(
      [add('1', 'WAITING'), add('2', 'WAITING'), add('3', 'PLAYING')],
      true,
      order,
    );
    state = state.apply([update('1', 'WAITING', { players: 'x,y' })], false, order);
    expect(ids(state).waiting).toEqual(['1', '2']);
    expect(state.get('1')?.players).toEqual(['x', 'y']);

    state = state.apply([update('2', 'PLAYING', { gameId: 'g2' }), add('4', 'WAITING')], false, order);
    expect(ids(state)).toEqual({ waiting: ['1', '4'], playing: ['3', '2'], finished: [] });

    state = state.apply([update('3', 'FINISHED', { winner: 'p3' }), remove('1'), remove('nope')], false, order);
    expect(ids(state)).toEqual({ waiting: ['4'], playing: ['2'], finished: ['3'] });
    expect(state.size).toBe(3);
  });

  it('starts over on a snapshot', () => {
    let state = HallTablesState.EMPTY.apply([add('1', 'WAITING'), add('2', 'PLAYING')], true, order);
    state = state.apply([add('3', 'WAITING')], true, order);
    expect(ids(state)).toEqual({ waiting: ['3'], playing: [], finished: [] });
  });

  it('lists no table of an unknown status, and an update of an unknown table is an addition', () => {
    const state = HallTablesState.EMPTY.apply([add('1', 'CANCELLED'), update('2', 'WAITING')], false, order);
    expect(ids(state)).toEqual({ waiting: ['2'], playing: [], finished: [] });
    expect(state.get('1')?.status).toBe('CANCELLED');
  });

  it('knows the games still running', () => {
    const state = HallTablesState.EMPTY.apply(
      [add('1', 'PLAYING', { gameId: 'g1' }), add('2', 'FINISHED', { gameId: 'g2' }), add('3', 'WAITING')],
      true,
      order,
    );
    expect([...state.liveGameIds()]).toEqual(['g1']);
  });
});
