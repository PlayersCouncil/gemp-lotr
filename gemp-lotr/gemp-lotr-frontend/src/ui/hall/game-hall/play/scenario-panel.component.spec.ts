import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import * as w from '../../../../core/hall/play/play-messages';
import { PlayOverlay } from '../../../../core/hall/play/play-overlay.service';
import { SCENARIO_FOLDER_COOKIE } from '../../../../core/hall/play/scenarios/play-scenario.service';
import { HallReadyOffers } from '../../../../core/hall/ready/hall-ready-offers.service';
import { FakeNotifier } from '../../../../testing/fake-notifier';
import { Notifier } from '../../../../core/notifier/notifier';
import { readServerResponse } from '../../../../testing/files';
import { HallTestBed, setUpHall } from '../../../../testing/hall';
import { OpenPlayOverlay, openPlayOverlay } from '../../../../testing/play-overlay';

const SCENARIOS = JSON.parse(readServerResponse('scenarios.json')) as Record<string, unknown>;

describe('ScenarioPanelComponent (Play ▸ Play Scenario or Tutorial, PlayScenario.js)', () => {
  let hall: HallTestBed;

  beforeEach(() => {
    hall = setUpHall([provideRouter([])]);
  });

  afterEach(() => hall.finish());

  async function openScenarios(listing: unknown = SCENARIOS): Promise<OpenPlayOverlay> {
    const play = await openPlayOverlay(hall, 'scenario');
    hall.request('/scenarios', 'GET').flush(listing as object);
    await play.update();
    return play;
  }

  it('notes a window too small for a tutorial’s pointers', async () => {
    const width = vi.spyOn(window, 'innerWidth', 'get').mockReturnValue(800);
    try {
      const play = await openScenarios();
      expect(play.text('.gd-scenario__note')).toBe(w.SCENARIO_SMALL_WINDOW.replace(/\s+/g, ' ').trim());
    } finally {
      width.mockRestore();
    }
  });

  it('shows a folder’s scenarios when it is picked, and remembers it', async () => {
    const play = await openScenarios();
    const folders = Array.from(play.el.querySelectorAll<HTMLButtonElement>('.gd-scenario__folder'));
    folders[3]!.click();
    await play.update();
    expect(folders[3]!.getAttribute('aria-selected')).toBe('true');
    expect(hall.cookies.read(SCENARIO_FOLDER_COOKIE)).not.toBeNull();
  });

  it('goes on with the game in progress, in a new tab, and the overlay closes', async () => {
    const play = await openScenarios();
    await play.click('[data-action=continue]');
    expect(hall.navigation.reserved.map((tab) => tab.url)).toEqual([
      '/gemp-lotr/ng/game?gameId=g-77&participantId=alice',
    ]);
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it('says why a scenario did not start: an answer without a game, its error, or a failed call', async () => {
    const play = await openScenarios();
    await play.click('[data-scenario=t1] [data-action=play]');
    hall.request('/scenarios/start').flush({ error: 'You are in a game already.' });
    await play.update();
    expect(play.text('.gd-play-form__result-box')).toContain('You are in a game already.');
    expect(hall.navigation.reserved[0]?.closed).toBe(true);

    await play.click('[data-scenario=t1] [data-action=play]');
    hall.request('/scenarios/start').flush({});
    await play.update();
    expect(play.text('.gd-play-form__result-box')).toContain(w.SCENARIO_NOT_STARTED);

    await play.click('[data-scenario=t1] [data-action=play]');
    hall.request('/scenarios/start').flush('', { status: 404, statusText: 'Not Found' });
    await play.update();
    // the list is asked for again: it may be what was out of date
    hall.request('/scenarios', 'GET').flush({ scenarios: [], mayAuthor: false });
    await play.update();
    expect(play.text('.gd-scenario__empty')).toBe(w.NO_SCENARIOS);
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(true);
  });

  it('says the list could not be loaded', async () => {
    const play = await openPlayOverlay(hall, 'scenario');
    hall.request('/scenarios', 'GET').flush('', { status: 500, statusText: 'Error' });
    await play.update();
    expect(play.text('.gd-scenario__empty')).toBe(w.SCENARIO_LIST_FAILED);
    expect(play.one('.gd-play-form__result-box').getAttribute('data-kind')).toBe('error');
  });

  it("opens an unreadable file of the player's in the authoring tool, or says it cannot", async () => {
    const play = await openScenarios({
      ...SCENARIOS,
      current: null,
      unreadable: [
        ...(SCENARIOS['unreadable'] as object[]),
        { path: 'users/alice/x.hjson', owner: 'alice', mine: true, id: null, errors: [], openable: false },
      ],
    });
    const files = play.el.querySelectorAll('.gd-scenario__unreadable-file');
    expect(files[1]?.querySelector('li')?.textContent).toBe(w.UNREADABLE_DEFAULT_ERROR);
    expect(files[1]?.querySelector('.gd-scenario__unreadable-note')?.textContent).toBe(w.CANNOT_OPEN_NOTE);
    await play.click('[data-action=edit]');
    hall.request('/scenarios/author/open').flush('', { status: 500, statusText: 'Error' });
    await play.update();
    expect(play.text('.gd-play-form__result-box')).toContain(w.scenarioOpenFailed(500));
    expect(hall.navigation.reserved[0]?.closed).toBe(true);

    hall.navigation.blockPopups = true;
    await play.click('[data-action=edit]');
    hall.request('/scenarios/author/open').flush({ gameId: 'g-9' });
    await play.update();
    expect(
      TestBed.inject(HallReadyOffers)
        .offers()
        .map((o) => o.key),
    ).toEqual(['game:g-9']);
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });

  it('does not open a file while a draft with unsaved changes stays, when the player says no', async () => {
    const play = await openScenarios({ ...SCENARIOS, authoring: { gameId: 'g-5', id: 'x', title: 'X', dirty: true } });
    (TestBed.inject(Notifier) as FakeNotifier).answers.push(false);
    await play.click('[data-action=edit]');
    expect((TestBed.inject(Notifier) as FakeNotifier).dialogs[0]).toMatch(/^confirm: /);
    expect(hall.navigation.reserved).toEqual([]);
  });

  it("goes to My Scenarios from the author's note, and the overlay closes", async () => {
    const play = await openScenarios();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const link = play.one<HTMLAnchorElement>('.gd-scenario__author a');
    expect(link.getAttribute('href')).toBe('/gemp-lotr/ng/account/scenarios');
    link.click();
    await play.update();
    expect(navigate).toHaveBeenCalledWith(['/account/scenarios'], { queryParams: {} });
    expect(TestBed.inject(PlayOverlay).isOpen()).toBe(false);
  });
});
