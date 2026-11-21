import { TestBed } from '@angular/core/testing';
import { Notifier } from '../../../notifier/notifier';
import { FakeNotifier } from '../../../../testing/fake-notifier';
import { readServerResponse } from '../../../../testing/files';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { HallReadyOffers } from '../../ready/hall-ready-offers.service';
import { PlayScenario, SCENARIO_FOLDER_COOKIE } from './play-scenario.service';

describe('PlayScenario', () => {
  let hall: HallTestBed;
  let play: PlayScenario;

  beforeEach(() => {
    hall = setUpHall();
  });

  afterEach(() => hall.finish());

  async function refreshed(): Promise<void> {
    play = TestBed.inject(PlayScenario);
    const refreshing = play.refresh();
    hall.request('/scenarios', 'GET').flush(readServerResponse('scenarios.json'));
    await refreshing;
  }

  it('lists the scenarios by folder, with the game in progress and the unreadable files', async () => {
    hall.cookies.write(SCENARIO_FOLDER_COOKIE, 'Puzzles');
    await refreshed();
    expect(play.selectedFolder()?.key).toBe('Puzzles');
    expect(play.current()).toEqual({ gameId: 'g-77', title: 'Ring Trouble' });
    const row = play.row(play.groups()[0].scenarios[0]);
    expect(row).toMatchObject({ button: 'Start Over', notes: ['By bob'], status: { kind: 'progress' } });
    expect(play.result()).toEqual({
      kind: 'error',
      message: "users/alice/puzzles/broken.hjson cannot be played: Unknown card 'the'  (and 1 more, above)",
    });
    play.selectFolder('gone');
    expect(play.selectedFolder()?.key).toBe('Tutorials');
    expect(hall.cookies.read(SCENARIO_FOLDER_COOKIE)).toBe('gone');
  });

  it('starts a scenario in the tab reserved on the click', async () => {
    await refreshed();
    const starting = play.start(play.listing()!.scenarios[0]);
    expect(play.starting()).toBe(true);
    const req = hall.request('/scenarios/start');
    expect(hall.body(req)).toEqual({ participantId: 'alice', id: 't1' });
    req.flush({ gameId: 'g-5', resumedAt: null });
    expect(await starting).toBe(true);
    expect(hall.navigation.reserved).toEqual([
      { url: '/gemp-lotr/ng/game?gameId=g-5&participantId=alice', closed: false },
    ]);
    expect(play.result().message).toBe('Started.');
  });

  it('offers the game when the browser gives no tab, and says why a start failed', async () => {
    await refreshed();
    hall.navigation.blockPopups = true;
    const starting = play.start(play.listing()!.scenarios[0]);
    hall.request('/scenarios/start').flush({ gameId: 'g-6' });
    await starting;
    expect(
      TestBed.inject(HallReadyOffers)
        .offers()
        .map((o) => o.key),
    ).toEqual(['game:g-6']);

    hall.navigation.blockPopups = false;
    const failing = play.start(play.listing()!.scenarios[0]);
    hall
      .request('/scenarios/start')
      .flush({ error: "There is no scenario called 't1'." }, { status: 404, statusText: 'Not Found' });
    expect(await failing).toBe(false);
    expect(hall.navigation.reserved[0].closed).toBe(true);
    expect(play.result().message).toBe("There is no scenario called 't1'.");
    hall.request('/scenarios', 'GET').flush(readServerResponse('scenarios.json'));
  });

  it('asks before dropping an unsaved draft to edit a file', async () => {
    const json = JSON.parse(readServerResponse('scenarios.json')) as Record<string, unknown>;
    json['authoring'] = { gameId: 'g-9', id: 'x', title: 'X', dirty: true };
    play = TestBed.inject(PlayScenario);
    const refreshing = play.refresh();
    hall.request('/scenarios', 'GET').flush(json);
    await refreshing;
    const notifier = TestBed.inject(Notifier) as FakeNotifier;
    expect(await play.edit('broken')).toBe(false);
    expect(notifier.dialogs[0]).toContain('You have a draft open');
    notifier.answers.push(true);
    const editing = play.edit('broken');
    await Promise.resolve();
    await Promise.resolve();
    hall.request('/scenarios/author/open').flush({ gameId: 'g-10' });
    expect(await editing).toBe(true);
  });

  it('takes no second start or edit while one is being set up (a second click before the buttons are off)', async () => {
    await refreshed();
    const starting = play.start(play.listing()!.scenarios[0]);
    expect(await play.start(play.listing()!.scenarios[1])).toBe(false);
    expect(await play.edit('broken')).toBe(false);
    hall.request('/scenarios/start').flush({ gameId: 'g-5' });
    expect(await starting).toBe(true);
    expect(hall.navigation.reserved.length).toBe(1);
  });
});
