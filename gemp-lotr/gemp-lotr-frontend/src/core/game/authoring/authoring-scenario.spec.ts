import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { draftGameXml, draftState, result } from '../../../testing/authoring';
import { FakeNotifier, provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { CardInfoService } from '../../cards/card-info.service';
import { HallTab } from '../../platform/hall-tab.service';
import { PageCues } from '../../platform/page-cues';
import { PageExit } from '../../platform/page-exit';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { provideGameState } from '../game-state';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { GameSession } from '../session/game-session.service';
import { DraftState, SaveReport } from './authoring-state';
import { Authoring } from './authoring.service';
import { sentForm } from '../../../testing/sent-form';
import { authorCall, authorCalls } from '../../../testing/authoring-requests';

const GAME = '/gemp-lotr-server/game/7';

const SAVED: SaveReport = {
  path: 'users/asdf/tutorials/test.hjson',
  id: 'test',
  playable: true,
  verified: true,
  errors: [],
  warnings: [],
  report: null,
};

/** What Authoring does for the Scenario tab: Save as, Rename, Submit, Check, Try other answers, See the file. */
describe('Authoring, for the Scenario tab', () => {
  let authoring: Authoring;
  let backend: HttpTestingController;
  let notifier: FakeNotifier;
  let hall: { go: ReturnType<typeof vi.fn>; elsewhere: ReturnType<typeof signal> };

  beforeEach(() => {
    vi.useFakeTimers();
    sessionStorage.clear();
    hall = { go: vi.fn(), elsewhere: signal<string | null>(null) };
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
        CardInfoService,
        { provide: PageCues, useValue: { go: vi.fn() } },
        { provide: HallTab, useValue: hall },
        { provide: PageExit, useValue: { onLeave: () => () => undefined, beacon: () => true } },
      ],
    });
    authoring = TestBed.inject(Authoring);
    backend = TestBed.inject(HttpTestingController);
    notifier = TestBed.inject(FakeNotifier);
  });

  afterEach(() => {
    backend.match(() => true);
    vi.useRealTimers();
  });

  const asked = (what: string, method = 'POST') => authorCalls(backend, what, method);

  async function ready(state: DraftState = draftState()) {
    TestBed.inject(GameSession).open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(draftGameXml());
    TestBed.tick();
    asked('state', 'GET')[0]!.flush(JSON.stringify(state));
    await settle();
    TestBed.tick();
  }

  async function answer(what: string, body: unknown, status = 200, method = 'POST'): Promise<TestRequest> {
    const request = authorCall(backend, what, method);
    request.flush(JSON.stringify(body), { status, statusText: status === 200 ? 'OK' : 'No' });
    await settle();
    TestBed.tick();
    return request;
  }

  const form = (request: TestRequest, name: string) => sentForm(request.request).get(name);

  /** The Step tab's step was refused (what a save says of it). */
  function refusedStep() {
    authoring.attachStepTab({
      flush: () => {
        authoring.problem.set('Bad When.');
        return Promise.resolve({ went: false, note: 'Step 1 is not in the file.' });
      },
      engaged: () => false,
      reset: () => undefined,
    });
  }

  it('saves a copy where it is told, and says how that went as a save does', async () => {
    await ready(draftState({ dirty: true }));
    const done = authoring.saveAs('copy', 'my copy.hjson', false);
    await settle();
    const request = await answer('save', result(draftState(), { saved: SAVED }));
    expect([form(request, 'as'), form(request, 'path'), form(request, 'canonical')]).toEqual([
      'copy',
      'my copy.hjson',
      'false',
    ]);
    expect(await done).toBe(true);
    expect(authoring.saved()?.text).toBe('Saved to users/asdf/tutorials/test.hjson. Scenario validated.');
  });

  it('moves the file with Rename, which stays said however the draft changes', async () => {
    await ready();
    const done = authoring.saveAs('rename', 'b.hjson', true);
    await settle();
    await answer('save', result(draftState(), { renamed: 'users/asdf/tutorials/b.hjson' }));
    expect(await done).toBe(true);
    expect(authoring.saved()).toEqual({
      good: true,
      why: false,
      stays: true,
      text: 'The file is now users/asdf/tutorials/b.hjson.',
    });
    authoring.setState(draftState({ dirty: true, revision: 5 }));
    expect(authoring.saved()).not.toBeNull();
  });

  it("says, with a Rename, that the Step tab's refused step is not in the file, and keeps its error", async () => {
    await ready();
    refusedStep();
    void authoring.saveAs('rename', 'b.hjson', true);
    await settle();
    await answer('save', result(draftState(), { renamed: 'b.hjson' }));
    expect(authoring.saved()).toEqual({
      good: false,
      why: false,
      stays: true,
      text: 'The file is now b.hjson. Step 1 is not in the file.',
    });
    expect(authoring.problem()).toBe('Bad When.');
  });

  it('keeps the form when filing is refused, with the reason said', async () => {
    await ready();
    const done = authoring.saveAs('copy', 'x.hjson', false);
    await settle();
    await answer('save', { error: 'There is already a scenario at x.hjson.' }, 400);
    expect(await done).toBe(false);
    expect(authoring.problem()).toBe('There is already a scenario at x.hjson.');
    expect(authoring.saved()).toBeNull();
  });

  it('submits a saved draft at once, and goes to My Scenarios', async () => {
    await ready(draftState({ own: true }));
    const done = authoring.submit();
    await settle();
    expect(asked('save')).toEqual([]);
    await answer('submit', { ok: true, submitted: 'tutorials/test.hjson' });
    expect(await done).toBe(true);
    expect(authoring.gone()).toBe(true);
    expect(hall.go).toHaveBeenCalledWith('#account/scenarios');
  });

  it('saves a changed draft first, and submits it only if that save plays through', async () => {
    await ready(draftState({ own: true, dirty: true }));
    void authoring.submit();
    await settle();
    await answer('save', result(draftState(), { saved: SAVED }));
    await answer('submit', { ok: true, submitted: 'tutorials/test.hjson' });
    expect(hall.go).toHaveBeenCalledOnce();
  });

  it('submits nothing when the save does not play through, and says why as a save does', async () => {
    await ready(draftState({ own: true, onDisk: false }));
    const done = authoring.submit();
    await settle();
    await answer('save', result(draftState(), { saved: { ...SAVED, verified: false, report: 'Stopped.' } }));
    expect(await done).toBe(false);
    expect(asked('submit')).toEqual([]);
    expect(authoring.saved()?.text).toContain('It does not play through yet.');
    expect(hall.go).not.toHaveBeenCalled();
  });

  it("submits nothing when the Step tab's step was refused", async () => {
    await ready(draftState({ own: true, dirty: true }));
    refusedStep();
    void authoring.submit();
    await settle();
    await answer('save', result(draftState(), { saved: SAVED }));
    expect(asked('submit')).toEqual([]);
    expect(authoring.saved()?.text).toContain('Step 1 is not in the file.');
  });

  it('submits nothing when the author keeps the comments, or the save or the submission fails', async () => {
    await ready(draftState({ own: true, dirty: true, commentsLost: 3 }));
    notifier.answers.push(false);
    expect(await authoring.submit()).toBe(false);
    expect(asked('save')).toEqual([]);

    authoring.setState(draftState({ own: true, dirty: true }));
    const failed = authoring.submit();
    await settle();
    await answer('save', { error: 'Disk full.' }, 500);
    expect(await failed).toBe(false);

    authoring.setState(draftState({ own: true }));
    const refused = authoring.submit();
    await settle();
    await answer('submit', { error: 'It has been submitted already.' }, 400);
    expect(await refused).toBe(false);
    expect(authoring.problem()).toBe('It has been submitted already.');
    expect(authoring.gone()).toBe(false);
  });

  it('submits nothing without a draft', async () => {
    expect(await authoring.submit()).toBe(false);
  });

  it('reports a check, its warnings, and other answers tried', async () => {
    await ready();
    void authoring.check();
    await settle();
    await answer('check', { passed: true, report: 'All steps matched.', warnings: ['Step 2 never shows.'] });
    expect(authoring.report()).toBe('Scenario validated.\n\nAll steps matched.\n\nWarnings:\nStep 2 never shows.');

    void authoring.offScript();
    await settle();
    expect(authoring.report()).toBe('Trying other answers...');
    await answer('offscript', { tries: 4, handled: false, report: 'Step 1: pass is not handled.' });
    expect(authoring.report()).toBe('Some other answers are not handled.\n\nStep 1: pass is not handled.');

    void authoring.offScript();
    await settle();
    await answer('offscript', { error: 'No.' }, 400);
    expect(authoring.report()).toBeNull();
    expect(authoring.problem()).toBe('No.');
  });

  it('shows the file as it would be saved', async () => {
    await ready();
    void authoring.seeFile();
    await settle();
    await answer('source', { source: '{\n\tid: test\n}' }, 200, 'GET');
    expect(authoring.report()).toBe('{\n\tid: test\n}');
  });

  it("has the scenario's menu call the draft by its title as it changes (its id when it has none)", async () => {
    const guide = TestBed.inject(ScenarioGuide);
    await ready();
    expect(guide.title()).toBe('A Test');
    authoring.setState(draftState({ revision: 2, draft: { ...draftState().draft, title: 'Renamed' } }));
    expect(guide.title()).toBe('Renamed');
    authoring.setState(draftState({ revision: 3, draft: { id: 'bare', steps: [] } }));
    expect(guide.title()).toBe('bare');
    // (another game's state: the scenario's own)
    authoring.setState(draftState({ revision: 4, gameId: '9' }));
    expect(guide.title()).toBe('A Test');
  });
});
