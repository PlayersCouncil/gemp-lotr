import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../api/gemp-interceptors';
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
});
