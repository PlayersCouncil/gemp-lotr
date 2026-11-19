import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { ScenarioApi, StartedScenario } from './scenario-api.service';

describe('ScenarioApi', () => {
  let api: ScenarioApi;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        ScenarioApi,
      ],
    });
    api = TestBed.inject(ScenarioApi);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('tells the server a step has been read, with the button pressed if it ended in a choice', () => {
    let done = 0;
    api.acknowledge('g 1', 4, null).subscribe(() => done++);
    const read = backend.expectOne((r) => r.url === '/gemp-lotr-server/game/g%201/scenarioAck' && r.method === 'POST');
    expect(read.request.body?.get('step')).toBe('4');
    expect(read.request.body?.get('choice')).toBe('');
    read.flush('');
    api.acknowledge('7', 5, 'yes').subscribe(() => done++);
    const chosen = backend.expectOne((r) => r.url === '/gemp-lotr-server/game/7/scenarioAck');
    expect(chosen.request.body?.get('choice')).toBe('yes');
    chosen.flush('OK');
    expect(done).toBe(2);
  });

  it('starts a game of a scenario, from the beginning or from its last checkpoint, and gives its id', () => {
    const started: StartedScenario[] = [];
    api.start('basics-01').subscribe((game) => started.push(game));
    const fresh = backend.expectOne((r) => r.url === '/gemp-lotr-server/scenarios/start' && r.method === 'POST');
    expect(fresh.request.body?.get('id')).toBe('basics-01');
    expect(fresh.request.body?.has('from')).toBe(false);
    fresh.flush('{"gameId":42}');
    api.start('basics-01', true).subscribe((game) => started.push(game));
    const resumed = backend.expectOne((r) => r.url === '/gemp-lotr-server/scenarios/start');
    expect(resumed.request.body?.get('from')).toBe('checkpoint');
    resumed.flush('{"gameId":"abc"}');
    expect(started).toEqual([{ gameId: '42' }, { gameId: 'abc' }]);
  });
});
