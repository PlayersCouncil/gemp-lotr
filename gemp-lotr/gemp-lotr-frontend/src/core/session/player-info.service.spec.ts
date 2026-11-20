import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../api/gemp-interceptors';
import { readServerResponse } from '../../testing/files';
import { ApiError } from '../api/api-error';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PlayerInfo } from './player-info.service';

describe('PlayerInfo', () => {
  let info: PlayerInfo;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideGempHttp(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: '' }],
    });
    info = TestBed.inject(PlayerInfo);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  const player = () => backend.expectOne((r) => r.url === '/gemp-lotr-server/player');

  it('asks the server once, whoever asks, and keeps the name', () => {
    const answers: string[] = [];
    info.load().subscribe((text) => answers.push(text));
    info.load().subscribe((text) => answers.push(text));
    expect(info.name()).toBeNull();
    player().flush('{"name":"alice","type":"u"}');
    expect(answers).toHaveLength(2);
    expect(info.name()).toBe('alice');
    // Later askers get the same answer
    info.load().subscribe((text) => answers.push(text));
    expect(answers).toHaveLength(3);
  });

  it('has no name when the server says none', () => {
    info.load().subscribe();
    player().flush('not json');
    expect(info.name()).toBeNull();
  });

  it('has no name for an answer without one', () => {
    info.load().subscribe();
    player().flush('{"name":""}');
    expect(info.name()).toBeNull();
  });

  it('passes a failure on (the login guard reads it)', () => {
    let failed = false;
    info.load().subscribe({ error: () => (failed = true) });
    player().flush('', { status: 401, statusText: 'Unauthorized' });
    expect(failed).toBe(true);
  });

  const expectPlayerRequest = () =>
    backend.expectOne((r) => r.url === '/gemp-lotr-server/player' && r.method === 'GET');

  it('asks GET /player once and shares the answer', async () => {
    expect(info.state()).toEqual({ status: 'unknown' });
    const first = info.whoAmI();
    const second = info.whoAmI();
    const request = expectPlayerRequest();
    expect(request.request.params.has('participantId')).toBe(false);
    request.flush(readServerResponse('player.json'));
    await expect(first).resolves.toEqual({ name: 'Visual tester', type: 'un', hasTrophies: true });
    await expect(second).resolves.toBe(await first);
    expect(info.player()?.name).toBe('Visual tester');
    expect(info.state().status).toBe('player');
    await info.whoAmI();
  });

  it('is nobody when not logged in (401)', async () => {
    const loaded = info.whoAmI();
    expectPlayerRequest().flush('', { status: 401, statusText: 'Unauthorized' });
    await expect(loaded).resolves.toBeNull();
    expect(info.state()).toEqual({ status: 'anonymous' });
    expect(info.player()).toBeNull();
  });

  it('stays unknown after another failure, and asks again next time', async () => {
    const failed = info.whoAmI();
    expectPlayerRequest().flush('', { status: 500, statusText: 'Server Error' });
    await expect(failed).rejects.toBeInstanceOf(ApiError);
    expect(info.state()).toEqual({ status: 'unknown' });
    const retried = info.whoAmI();
    expectPlayerRequest().flush(readServerResponse('player.json'));
    await expect(retried).resolves.toMatchObject({ name: 'Visual tester' });
  });

  it('asks again on refresh (after logging in)', async () => {
    const anonymous = info.whoAmI();
    expectPlayerRequest().flush('', { status: 401, statusText: 'Unauthorized' });
    await anonymous;
    const refreshed = info.refresh();
    expect(info.state()).toEqual({ status: 'anonymous' });
    expectPlayerRequest().flush(readServerResponse('player.json'));
    await expect(refreshed).resolves.toMatchObject({ name: 'Visual tester' });
    expect(info.player()?.name).toBe('Visual tester');
    await expect(info.whoAmI()).resolves.toMatchObject({ name: 'Visual tester' });
  });
});

describe('PlayerInfo for an admin acting as a player', () => {
  it("sends the session's participantId (an admin acting as a player)", async () => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=bob' },
      ],
    });
    const backend = TestBed.inject(HttpTestingController);
    const info = TestBed.inject(PlayerInfo);
    const loaded = info.whoAmI();
    const request = backend.expectOne((r) => r.url === '/gemp-lotr-server/player' && r.method === 'GET');
    expect(request.request.params.get('participantId')).toBe('bob');
    request.flush('{"hasTrophies":false,"name":"bob","type":"u"}');
    await expect(loaded).resolves.toMatchObject({ name: 'bob' });
  });
});
