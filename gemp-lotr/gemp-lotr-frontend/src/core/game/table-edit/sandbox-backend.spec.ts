import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SANDBOX_STATE } from '../../../testing/table-edit';
import { ApiError } from '../../api/api-error';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { EditBoard } from './edit-board';
import { SandboxBackend } from './sandbox-backend';

describe('SandboxBackend', () => {
  let sandbox: SandboxBackend;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        // (an admin acting as somebody: a sandbox is still its player's own, so none is sent)
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=Someone' },
        SandboxBackend,
      ],
    });
    sandbox = TestBed.inject(SandboxBackend);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('is the sandbox level', () => {
    expect(sandbox.level).toBe('sandbox');
  });

  it('reads the state of the game', () => {
    const boards: EditBoard[] = [];
    sandbox.state('12').subscribe((board) => boards.push(board));
    const asked = backend.expectOne((r) => r.url === '/gemp-lotr-server/scenarios/sandbox/state' && r.method === 'GET');
    expect(asked.request.params.get('gameId')).toBe('12');
    expect(asked.request.params.has('participantId')).toBe(false);
    asked.flush(SANDBOX_STATE);
    expect(boards[0]?.cards.length).toBe(6);
  });

  it('sends the edits as one JSON list and answers the state after them', () => {
    const boards: EditBoard[] = [];
    sandbox
      .apply('12', [
        { op: 'add', blueprint: '1_50', to: 'freeps.hand' },
        { op: 'twilight', set: 3 },
      ])
      .subscribe((board) => boards.push(board));
    const sent = backend.expectOne((r) => r.url === '/gemp-lotr-server/scenarios/sandbox/edit' && r.method === 'POST');
    expect(sent.request.body?.get('gameId')).toBe('12');
    expect(JSON.parse(sent.request.body?.get('ops') ?? '')).toEqual([
      { op: 'add', blueprint: '1_50', to: 'freeps.hand' },
      { op: 'twilight', set: 3 },
    ]);
    expect(sent.request.body?.has('participantId')).toBe(false);
    sent.flush({ ok: true, state: SANDBOX_STATE });
    expect(boards[0]?.mode).toBe('edit');
  });

  it("gives a refusal with the server's words", () => {
    let refusal: ApiError | null = null;
    sandbox
      .apply('12', [{ op: 'delete', card: 'frodo' }])
      .subscribe({ error: (error) => (refusal = ApiError.from(error)) });
    backend
      .expectOne('/gemp-lotr-server/scenarios/sandbox/edit')
      .flush({ error: 'Frodo cannot be deleted' }, { status: 400, statusText: 'Bad Request' });
    expect(refusal!.said).toBe('Frodo cannot be deleted');
  });

  it('stops, starts and resets', () => {
    const modes: string[] = [];
    sandbox.stop('12').subscribe((board) => modes.push(board.mode));
    backend.expectOne('/gemp-lotr-server/scenarios/sandbox/stop').flush({ ok: true, state: SANDBOX_STATE });
    sandbox.start('12').subscribe((board) => modes.push(board.mode));
    backend
      .expectOne('/gemp-lotr-server/scenarios/sandbox/start')
      .flush({ ok: true, state: { ...SANDBOX_STATE, mode: 'play' } });
    expect(modes).toEqual(['edit', 'play']);
    let fresh = '';
    sandbox.reset('12').subscribe((gameId) => (fresh = gameId));
    const reset = backend.expectOne('/gemp-lotr-server/scenarios/sandbox/reset');
    expect(reset.request.body?.get('gameId')).toBe('12');
    reset.flush({ gameId: 13 });
    expect(fresh).toBe('13');
  });
});
