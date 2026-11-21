import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { answerWith, eventsRequest, failWith, setUpEvents } from '../../../testing/events-server';
import { readServerResponse } from '../../../testing/files';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { EventsApi } from './events-api.service';

describe('EventsApi', () => {
  let api: EventsApi;
  let backend: ReturnType<typeof setUpEvents>['backend'];

  beforeEach(() => {
    backend = setUpEvents().backend;
    api = TestBed.inject(EventsApi);
  });

  afterEach(() => backend.verify());

  it('asks the calendar for the days given', async () => {
    const answer = firstValueFrom(api.calendar('2026-10-26', '2026-12-06'));
    const request = answerWith(backend, '/calendar', 'events-calendar.json');
    expect(request.request.params.get('from')).toBe('2026-10-26');
    expect(request.request.params.get('to')).toBe('2026-12-06');
    expect((await answer).events).toHaveLength(5);
  });

  it('asks the event history by kind and month', async () => {
    const months = firstValueFrom(api.eventHistoryMonths('league'));
    const monthsRequest = answerWith(backend, '/eventHistory/months', 'event-history-months.json');
    expect(monthsRequest.request.params.get('kind')).toBe('league');
    expect(await months).toEqual(['2026-10', '2026-08', '2025-12']);
    const month = firstValueFrom(api.eventHistory('2026-10', 'tournament'));
    const monthRequest = answerWith(backend, '/eventHistory', 'event-history-tournament.json');
    expect(monthRequest.request.params.get('month')).toBe('2026-10');
    expect((await month).isAdmin).toBe(true);
  });

  it('reads leagues and tournaments, and gives an unreadable answer as INVALID_RESPONSE_STATUS', async () => {
    const leagues = firstValueFrom(api.leagues());
    answerWith(backend, '/league', 'leagues.xml');
    expect(await leagues).toHaveLength(3);
    const league = firstValueFrom(api.league('1700000001'));
    answerWith(backend, '/league/1700000001', 'league-detail.xml');
    expect((await league)?.name).toBe('Constructed League');
    const tournament = firstValueFrom(api.tournament('daily one'));
    eventsRequest(backend, '/tournament/daily%20one').flush('<tournament');
    await expect(tournament).rejects.toMatchObject({ status: INVALID_RESPONSE_STATUS });
    const live = firstValueFrom(api.liveTournaments());
    answerWith(backend, '/tournament', 'tournaments.xml');
    expect(await live).toHaveLength(2);
  });

  it('joins a league with a POST', async () => {
    const joined = firstValueFrom(api.joinLeague('1700000001'));
    eventsRequest(backend, '/league/1700000001', 'POST').flush('<result>OK</result>');
    await expect(joined).resolves.toBeUndefined();
    const refused = firstValueFrom(api.joinLeague('1700000001'));
    failWith(backend, '/league/1700000001', 409, 'POST');
    await expect(refused).rejects.toBeInstanceOf(ApiError);
  });

  it("asks one format's definition by code; an unknown code is none", async () => {
    const known = firstValueFrom(api.formatDefinition('fotr_block'));
    eventsRequest(backend, '/hall/format/fotr_block/json').flush(readServerResponse('hall-format-definition.json'));
    expect((await known)?.sitesName).toBe('Fellowship block');
    const unknown = firstValueFrom(api.formatDefinition('gone'));
    failWith(backend, '/hall/format/gone/json', 404);
    expect(await unknown).toBeNull();
    const failed = firstValueFrom(api.formatDefinition('fotr_block'));
    failWith(backend, '/hall/format/fotr_block/json', 500);
    await expect(failed).rejects.toMatchObject({ status: 500 });
  });
});
