import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { accountRequest, answerAccount, setUpAccount } from '../../../testing/account-server';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { AccountApi } from './account-api.service';
import { NO_GAME_HISTORY_FILTERS } from './account-models';
import { sentForm } from '../../../testing/sent-form';
import { containing } from '../../../testing/matchers';

describe('AccountApi', () => {
  let backend: ReturnType<typeof setUpAccount>['backend'];
  let api: AccountApi;

  beforeEach(() => {
    ({ backend } = setUpAccount([{ provide: PAGE_QUERY_STRING, useValue: '?participantId=bob' }]));
    api = TestBed.inject(AccountApi);
  });

  afterEach(() => backend.verify());

  it('asks for a page of history with only the filters given, as the player acted as', async () => {
    const page = firstValueFrom(
      api.gameHistory(20, 20, { ...NO_GAME_HISTORY_FILTERS, opponent: 'bo', to: '2026-11-01' }),
    );
    const request = answerAccount(backend, '/gameHistory', 'game-history.xml');
    const params = request.request.params;
    expect(params.keys().filter((key) => key !== '_')).toEqual(['start', 'count', 'opponent', 'to']);
    expect([params.get('start'), params.get('count'), params.has('participantId')]).toEqual(['20', '20', false]);
    expect((await page)?.matching).toBe(23);
  });

  it('reads the filters, the stats and the migration', async () => {
    const filters = firstValueFrom(api.gameHistoryFilters());
    answerAccount(backend, '/gameHistory/filters', 'game-history-filters.json');
    expect((await filters).formats).toHaveLength(2);
    const stats = firstValueFrom(api.playerStats());
    answerAccount(backend, '/playerStats', 'player-stats.xml');
    expect((await stats)?.casual).toHaveLength(1);
    const migrated = firstValueFrom(api.migrateTrophies());
    accountRequest(backend, '/collection/migrate', 'POST').flush('{"name":"bob"}');
    await migrated;
  });

  it("sends the shelf's calls", async () => {
    const mine = firstValueFrom(api.myScenarios());
    const request = answerAccount(backend, '/scenarios/mine', 'scenarios-mine.json');
    expect(request.request.params.has('participantId')).toBe(false);
    expect((await mine).scenarios).toHaveLength(4);

    const copy = firstValueFrom(api.copy('a.hjson'));
    const post = accountRequest(backend, '/scenarios/mine/copy', 'POST');
    expect(sentForm(post.request).toString()).toBe('path=a.hjson');
    post.flush('{"ok":true,"id":"x","path":"b.hjson"}');
    expect(await copy).toEqual({ id: 'x', path: 'b.hjson' });

    const submitted = firstValueFrom(api.submit('a.hjson'));
    accountRequest(backend, '/scenarios/mine/submit', 'POST').flush('{"ok":true}');
    await submitted;
    const publics = firstValueFrom(api.publicScenarios());
    answerAccount(backend, '/scenarios/public', 'scenarios-public.json');
    expect(await publics).toHaveLength(2);
  });

  it('turns an unreadable answer into an ApiError', async () => {
    const mine = firstValueFrom(api.myScenarios());
    accountRequest(backend, '/scenarios/mine').flush('<html>');
    await expect(mine).rejects.toEqual(containing({ status: INVALID_RESPONSE_STATUS }));
    const history = firstValueFrom(api.gameHistory(0, 20, NO_GAME_HISTORY_FILTERS));
    accountRequest(backend, '/gameHistory').flush('not xml <');
    await expect(history).rejects.toBeInstanceOf(ApiError);
  });
});
