import { readServerResponse } from '../../../testing/files';
import {
  canSubmitTournament,
  draftTimerLabel,
  draftTimerOf,
  initialTournamentForm,
  parseTournamentFormats,
  tournamentFields,
  withFormat,
  withPlayers,
  withType,
} from './tournament-form';

const formats = parseTournamentFormats(JSON.parse(readServerResponse('tournament-formats.json')));

describe('the Create Tournament form', () => {
  it('reads the formats each type offers', () => {
    expect(formats.tableDrafts).toEqual([
      { code: 'fotr_table_draft', name: 'FotR Table Draft', maxPlayers: 8, recommendedTimer: 'SLOW' },
    ]);
    expect(formats.constructed.map((f) => f.code)).toEqual(['fotr_block', 'movie']);
    expect(formats.draftTimerTypes.map(draftTimerLabel)).toEqual(['Classic', 'Slow', 'No Timer']);
    expect(parseTournamentFormats(null).sealed).toEqual([]);
  });

  it('shows nothing below the type until one is chosen, then the fields of that type', () => {
    let form = initialTournamentForm();
    expect(tournamentFields(form, formats)).toMatchObject({ startup: false, deck: false });
    expect(canSubmitTournament(form)).toBe(false);

    form = withType(form, 'constructed', formats);
    expect(form.format).toBe('fotr_block');
    expect(tournamentFields(form, formats)).toMatchObject({
      startup: true,
      deck: true,
      deckbuilding: false,
      draftInfo: false,
      draftTimer: false,
    });
    expect(canSubmitTournament(form)).toBe(true);

    form = withType(form, 'sealed', formats);
    expect(form.deckbuilding).toBe('30');
    expect(tournamentFields(form, formats)).toMatchObject({ deck: false, deckbuilding: true });

    form = withType(withPlayers(form, '12'), 'table_draft', formats);
    expect(form).toMatchObject({ deckbuilding: '15', players: '8', draftTimer: 'SLOW' });
    expect(tournamentFields(form, formats)).toMatchObject({ draftInfo: true, draftTimer: true, maxPlayers: 8 });
  });

  it('changes the other settings only when the player count crosses the 2-player line', () => {
    let form = { ...initialTournamentForm(), readyCheck: 120, earlyStart: false };
    form = withPlayers(form, '2');
    expect(form).toMatchObject({ small: true, readyCheck: -1, earlyStart: false });
    expect(tournamentFields(form, formats).largeSettingsEnabled).toBe(false);
    form = withPlayers(form, '1');
    expect(form.readyCheck).toBe(-1);
    form = withPlayers(form, '6');
    expect(form).toMatchObject({ small: false, readyCheck: 120, earlyStart: false });
    expect(withPlayers(initialTournamentForm(), '')).toMatchObject({ small: false, readyCheck: 60 });
  });

  it('sends the server first draft timer when none was chosen', () => {
    const form = withFormat(withType(initialTournamentForm(), 'constructed', formats), 'movie', formats);
    expect(draftTimerOf(form, formats)).toBe('CLASSIC');
    expect(draftTimerOf(form, null)).toBe('');
  });
});
