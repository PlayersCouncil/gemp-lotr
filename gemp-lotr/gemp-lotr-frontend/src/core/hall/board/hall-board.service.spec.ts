import { TestBed } from '@angular/core/testing';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallBoard } from './hall-board.service';
import {
  HALL_SETTINGS_COOKIE,
  HallSectionSettings,
  readHallSettings,
  writeHallSettings,
} from './hall-section-settings.service';

describe('HallBoard', () => {
  let hall: HallTestBed;
  let board: HallBoard;

  beforeEach(() => {
    vi.useFakeTimers();
    hall = setUpHall();
    board = TestBed.inject(HallBoard);
  });

  afterEach(() => {
    hall.finish();
    vi.useRealTimers();
  });

  const keys = (rows: readonly { key: string }[]) => rows.map((row) => row.key);

  it('merges queues, tournaments and tables in the order the legacy hall drew them', () => {
    hall.connect();
    const sections = board.sections();
    expect(keys(sections.waiting)).toEqual(['queue-waiting:wc_queue', 'queue-waiting:pm1', 'table:11']);
    expect(keys(sections.playing)).toEqual(['tournament:t1', 'tournament:t2', 'tournament:t3', 'table:12']);
    expect(sections.wc.map((r) => r.queue.id)).toEqual(['wc_queue']);
    expect(sections.recurring.map((r) => r.queue.id)).toEqual(['daily']);
    expect(sections.scheduled).toEqual([]);
    expect(board.counts()).toMatchObject({ waiting: 3, playing: 4, finished: 0, wc: 1, recurring: 1 });
    expect(board.shownSections()).toEqual(['waiting', 'playing', 'finished', 'wc', 'recurring']);
  });

  it('keeps an updated row in place, adds a row that comes into Waiting Tables at the end, and drops removed ones', () => {
    hall.connect();
    hall.poll(
      '<hall channelNumber="8"><table action="add" id="13" status="WAITING" players="x"/>' +
        '<queue action="update" id="daily" queue="Daily FotR" recurring="true" displayInWaitingTables="true"/>' +
        '<queue action="update" id="wc_queue" queue="WC" wc="true" displayInWaitingTables="true"/>' +
        '<queue action="remove" id="pm1"/><tournament action="remove" id="t1"/></hall>',
    );
    const sections = board.sections();
    expect(keys(sections.waiting)).toEqual(['queue-waiting:wc_queue', 'table:11', 'queue-waiting:daily', 'table:13']);
    expect(keys(sections.playing)).toEqual(['tournament:t2', 'tournament:t3', 'table:12']);
    expect(sections.wc[0].queue.name).toBe('WC');
    hall.poll('<hall channelNumber="8"><queue action="remove" id="wc_queue"/></hall>');
    expect(board.shownSections()).not.toContain('wc');
  });

  it('keeps the sections open or closed in the hallSettings cookie', () => {
    hall.cookies.write(HALL_SETTINGS_COOKIE, '1|0|1|0|0|0|1|0');
    const settings = TestBed.inject(HallSectionSettings);
    hall.connect();
    expect(settings.open()).toEqual({
      waiting: true,
      playing: false,
      finished: true,
      wc: false,
      scheduled: false,
      recurring: true,
    });
    settings.toggle('wc');
    expect(hall.cookies.read(HALL_SETTINGS_COOKIE)).toBe('1|0|1|1|0|0|1|0');
    // the scheduled section is not on the page: its slot is kept
    hall.cookies.write(HALL_SETTINGS_COOKIE, '1|0|1|1|1|0|1|0');
    settings.toggle('playing');
    expect(hall.cookies.read(HALL_SETTINGS_COOKIE)).toBe('1|1|1|1|1|0|1|0');
  });

  it('reads the defaults and writes only the slots it is given', () => {
    expect(readHallSettings(null)).toEqual(['1', '1', '0', '0', '0', '0', '0', '0']);
    expect(writeHallSettings(['1', '1', '0'], { finished: true, recurring: true })).toBe('1|1|1||||1');
  });
});
