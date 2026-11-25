import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../../testing/files';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallDialogs } from '../hall-dialogs.service';
import { CreateTournament } from './create-tournament.service';

describe('CreateTournament', () => {
  let hall: HallTestBed;
  let tournament: CreateTournament;
  const elves = { name: 'Elves', source: 'player' as const };

  beforeEach(() => {
    hall = setUpHall();
    tournament = TestBed.inject(CreateTournament);
  });

  afterEach(() => hall.finish());

  async function loaded(): Promise<void> {
    const loading = tournament.loadFormats();
    hall.request('/tournament/tournamentFormats', 'GET').flush(readServerResponse('tournament-formats.json'));
    await loading;
  }

  it('hosts a Constructed tournament and says what happens next', async () => {
    await loaded();
    tournament.setType('constructed');
    expect(await tournament.submit(null)).toBe(false);
    expect(tournament.result().message).toBe('You must select your deck before starting a Constructed tournament.');
    tournament.setPlayers('0');
    expect(await tournament.submit(elves)).toBe(false);
    expect(tournament.result().message).toBe('Enter the number of players (1 or more).');

    tournament.setPlayers('6');
    tournament.patch({ competitive: true });
    const hosting = tournament.submit(elves);
    const req = hall.request('/tournament/create');
    expect(hall.body(req)).toEqual({
      type: 'constructed',
      deckName: 'Elves',
      deckSource: 'player',
      maxPlayers: '6',
      formatCode: 'fotr_block',
      tableDraftTimer: 'CLASSIC',
      playoff: 'SWISS',
      deckbuildingDuration: '15',
      competitive: 'true',
      startable: 'true',
      readyCheck: '60',
    });
    req.flush('"OK"');
    expect(await hosting).toBe(true);
    const [dialog] = TestBed.inject(HallDialogs).open();
    expect(dialog.title).toBe('Tournament Created');
    expect(dialog.body).toEqual([
      {
        kind: 'text',
        text:
          'Your Competitive Fellowship Block tournament is open, and you are signed up for it. It is listed in ' +
          'Waiting Tables, where other players can join it; it starts when 6 players have joined, or earlier when ' +
          'you press its Start button.',
      },
    ]);
    expect(JSON.parse(hall.cookies.read('tournament-decks')!).fotr_block).toEqual(['Elves', 'player']);
  });

  it('shows the server refusal, and a formats load failure', async () => {
    const loading = tournament.loadFormats();
    hall.request('/tournament/tournamentFormats', 'GET').flush('', { status: 500, statusText: 'Error' });
    await loading;
    expect(tournament.result().message).toBe(
      'Could not load the tournament formats: The server ran into an error. Please try again, and report a bug if it keeps happening.',
    );
    await loaded();
    tournament.setType('sealed');
    expect(tournament.formatInfo()?.text).toBe(
      'Everyone receives the same sealed product for this format and builds a deck from it.',
    );
    const hosting = tournament.submit(null);
    hall
      .request('/tournament/create')
      .flush(JSON.stringify({ error: 'Not now' }), { status: 400, statusText: 'Bad Request' });
    expect(await hosting).toBe(false);
    expect(tournament.result()).toEqual({ kind: 'error', message: 'Not now' });
    tournament.resetForm();
    expect(tournament.form().type).toBe('');
  });
});
