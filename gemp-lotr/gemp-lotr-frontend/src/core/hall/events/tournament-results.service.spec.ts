import { TestBed } from '@angular/core/testing';
import { answerWith, failWith, setUpEvents } from '../../../testing/events-server';
import { settle } from '../../../testing/fake-notifier';
import { TournamentResults } from './tournament-results.service';

describe('TournamentResults', () => {
  let backend: ReturnType<typeof setUpEvents>['backend'];
  let tournaments: TournamentResults;

  beforeEach(() => {
    backend = setUpEvents().backend;
    tournaments = TestBed.inject(TournamentResults);
  });

  afterEach(() => backend.verify());

  it('lists the live tournaments with their start dates and opens their details', async () => {
    const done = tournaments.loadList();
    answerWith(backend, '/tournament', 'tournaments.xml');
    await done;
    expect(tournaments.rows().map((row) => [row.key, row.title(), row.date()])).toEqual([
      ['daily-pc_movie-20261120', 'Daily PC-Movie', '2026-11-20'],
      ['casual-pc_fotr_block1794232985000', 'Casual PC-Fellowship', ''],
    ]);
    const row = tournaments.rows()[0]!;
    row.drawer.open();
    answerWith(backend, '/tournament/daily-pc_movie-20261120', 'tournament-playing.xml');
    await settle();
    expect(row.drawer.content()).toMatchObject({ kind: 'data', data: { stage: 'Playing Games' } });
  });

  it('says why the list or a tournament could not be loaded', async () => {
    const done = tournaments.loadList();
    failWith(backend, '/tournament', 401);
    await done;
    expect(tournaments.list()).toEqual({
      kind: 'error',
      text: 'Could not load the current tournaments. You are not logged in.',
    });
    const drawer = tournaments.createDrawer('x');
    drawer.open();
    failWith(backend, '/tournament/x', 410);
    await settle();
    expect(drawer.content()).toEqual({
      kind: 'error',
      text: 'Could not load the tournament (HTTP 410). You were logged out after being inactive; refresh the page.',
    });
  });

  it('leaves the list showing for a tournament that is not live', async () => {
    const shown = tournaments.showTournament('scheduled-later');
    answerWith(backend, '/tournament', 'tournaments.xml');
    await shown;
    expect(tournaments.revealed()).toBeNull();
    expect(tournaments.rows().every((row) => !row.drawer.isOpen())).toBe(true);
  });

  it('shows a scheduled tournament that has not started without asking the server', () => {
    const drawer = tournaments.createDrawer('wc', { fallback: true, fetch: false });
    drawer.open();
    expect(drawer.content()).toEqual({ kind: 'fallback' });
  });
});
