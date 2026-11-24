import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { PUZZLE_SLOTS } from '../../../testing/table-edit';
import { ApiError } from '../../api/api-error';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { ScenarioGuide } from '../scenario/scenario-guide.service';
import { readPuzzleSlots } from './puzzle-slots';
import { EditBoard } from './edit-board';
import { NO_VERDICT, NOT_A_SANDBOX, PuzzleBackend, readVerdict, SlotVerdict } from './puzzle-backend';

const PICKS = '/gemp-lotr-server/scenarios/picks';

describe('PuzzleBackend', () => {
  let puzzle: PuzzleBackend;
  let backend: HttpTestingController;
  const info = signal<{ id: string; mode: 'edit' | 'play'; slots: ReturnType<typeof readPuzzleSlots> } | null>(null);

  const answer = (slot: string, verdict: SlotVerdict) => ({ ok: verdict.fits, picks: { [slot]: verdict } });

  beforeEach(() => {
    info.set({ id: 'GamePlayer3/check-p10', mode: 'edit', slots: readPuzzleSlots(PUZZLE_SLOTS) });
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: ScenarioGuide, useValue: { info } },
        PuzzleBackend,
      ],
    });
    puzzle = TestBed.inject(PuzzleBackend);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('is the puzzle level, with the slots its info has, empty while held', async () => {
    expect(puzzle.level).toBe('puzzle');
    expect(puzzle.filled().map((filled) => [filled.slot.id, filled.card])).toEqual([
      ['condition', null],
      ['ally', null],
    ]);
    expect(puzzle.missing()).toEqual(['a Rohan condition', 'a Rohan ally']);
    const board = await firstValueFrom(puzzle.state());
    expect(board).toEqual(expect.objectContaining({ mode: 'edit', level: 'puzzle', tools: [], cards: [] }));
  });

  it('checks a pick with the server beside the other slots, and holds it once it fits', () => {
    const boards: EditBoard[] = [];
    puzzle.apply('12', [{ op: 'pick', slot: 'condition', blueprint: '5_87' }]).subscribe((board) => boards.push(board));
    const asked = backend.expectOne((r) => r.url === PICKS && r.method === 'POST');
    expect(asked.request.body?.get('id')).toBe('GamePlayer3/check-p10');
    expect(JSON.parse(asked.request.body?.get('picks') ?? '')).toEqual({ condition: '5_87' });
    asked.flush(answer('condition', { fits: true, why: null, name: 'Parapet' }));
    expect(boards.length).toBe(1);
    expect(puzzle.picks()).toEqual({ condition: { blueprintId: '5_87', name: 'Parapet' } });
    expect(puzzle.missing()).toEqual(['a Rohan ally']);

    // The next is checked with the first beside it (uniqueness counts it)
    puzzle.apply('12', [{ op: 'pick', slot: 'ally', blueprint: '4_262' }]).subscribe();
    const next = backend.expectOne(PICKS);
    expect(JSON.parse(next.request.body?.get('picks') ?? '')).toEqual({ condition: '5_87', ally: '4_262' });
    next.flush(answer('ally', { fits: true, why: null, name: null }));
    expect(puzzle.picks()['ally']).toEqual({ blueprintId: '4_262', name: '4_262' });
    expect(puzzle.filled().map((filled) => filled.card?.blueprintId)).toEqual(['5_87', '4_262']);
  });

  it("refuses a pick that does not fit in the server's words, and holds nothing", () => {
    let refusal: ApiError | null = null;
    puzzle
      .apply('12', [{ op: 'pick', slot: 'condition', blueprint: '4_262' }])
      .subscribe({ error: (error) => (refusal = ApiError.from(error)) });
    backend
      .expectOne(PICKS)
      .flush(answer('condition', { fits: false, why: 'Aldor is not a Rohan condition.', name: 'Aldor' }));
    expect(refusal!.said).toBe('Aldor is not a Rohan condition.');
    expect(puzzle.picks()).toEqual({});

    // An answer that says nothing of the slot, or nothing at all of why
    puzzle.apply('12', [{ op: 'pick', slot: 'condition', blueprint: '4_262' }]).subscribe({
      error: (error) => (refusal = ApiError.from(error)),
    });
    backend.expectOne(PICKS).flush({ ok: false, picks: {} });
    expect(refusal!.said).toBe(NO_VERDICT);
    puzzle.apply('12', [{ op: 'pick', slot: 'condition', blueprint: '4_262' }]).subscribe({
      error: (error) => (refusal = ApiError.from(error)),
    });
    backend.expectOne(PICKS).flush(answer('condition', { fits: false, why: null, name: null }));
    expect(refusal!.said).toBe(NO_VERDICT);
  });

  it('empties a slot on the page, asking nobody', async () => {
    puzzle.picks.set({ condition: { blueprintId: '5_87', name: 'Parapet' } });
    await firstValueFrom(puzzle.apply('12', [{ op: 'pick', slot: 'condition', blueprint: null }]));
    expect(puzzle.picks()).toEqual({});
  });

  it('refuses anything but one pick, and Stop, Start and Reset', () => {
    const said: (string | null)[] = [];
    const refused = { error: (error: unknown) => said.push(ApiError.from(error).said) };
    puzzle.apply('12', [{ op: 'add', blueprint: '1_50', to: 'freeps.hand' }]).subscribe(refused);
    puzzle
      .apply('12', [
        { op: 'pick', slot: 'a', blueprint: '1_1' },
        { op: 'pick', slot: 'b', blueprint: '1_1' },
      ])
      .subscribe(refused);
    puzzle.apply('12', []).subscribe(refused);
    puzzle.stop().subscribe(refused);
    puzzle.start().subscribe(refused);
    puzzle.reset().subscribe(refused);
    expect(said).toEqual(Array(6).fill(NOT_A_SANDBOX));
  });

  it('submits the picks as a fresh game of the puzzle', () => {
    puzzle.picks.set({
      condition: { blueprintId: '5_87', name: 'Parapet' },
      ally: { blueprintId: '4_262', name: 'Aldor' },
    });
    let fresh = '';
    puzzle.submit().subscribe((gameId) => (fresh = gameId));
    const sent = backend.expectOne((r) => r.url === '/gemp-lotr-server/scenarios/start' && r.method === 'POST');
    expect(sent.request.body?.get('id')).toBe('GamePlayer3/check-p10');
    expect(JSON.parse(sent.request.body?.get('picks') ?? '')).toEqual({ condition: '5_87', ally: '4_262' });
    sent.flush({ gameId: 124, resumedAt: null, mode: 'play' });
    expect(fresh).toBe('124');

    info.set(null);
    let said: string | null = null;
    puzzle.submit().subscribe({ error: (error) => (said = ApiError.from(error).said) });
    expect(said).toBe(NOT_A_SANDBOX);
  });

  it('shows the cards the game was built with once it is played', () => {
    const slots = readPuzzleSlots(
      PUZZLE_SLOTS.map((slot) => ({ ...slot, pick: '5_87', pickName: slot.id === 'ally' ? null : 'Parapet' })),
    );
    info.set({ id: 'x', mode: 'play', slots });
    expect(puzzle.filled().map((filled) => filled.card)).toEqual([
      { blueprintId: '5_87', name: 'Parapet' },
      { blueprintId: '5_87', name: '5_87' },
    ]);
    info.set({ id: 'x', mode: 'play', slots: readPuzzleSlots(PUZZLE_SLOTS) });
    expect(puzzle.filled().map((filled) => filled.card)).toEqual([null, null]);
  });

  it("asks the server's word on a card for a slot once, beside the other slots' cards", async () => {
    puzzle.picks.set({ ally: { blueprintId: '4_262', name: 'Aldor' } });
    const first = firstValueFrom(puzzle.verdict('condition', '5_87'));
    const asked = backend.expectOne(PICKS);
    expect(JSON.parse(asked.request.body?.get('picks') ?? '')).toEqual({ ally: '4_262', condition: '5_87' });
    asked.flush(answer('condition', { fits: true, why: null, name: 'Parapet' }));
    expect(await first).toEqual({ fits: true, why: null, name: 'Parapet' });
    // (asked again: known)
    expect(await firstValueFrom(puzzle.verdict('condition', '5_87'))).toEqual({
      fits: true,
      why: null,
      name: 'Parapet',
    });
    backend.expectNone(PICKS);
    // (its own slot's card is no other card)
    puzzle.picks.update((held) => ({ ...held, condition: { blueprintId: '5_79', name: 'Armory' } }));
    void firstValueFrom(puzzle.verdict('condition', '5_87'));
    backend.expectNone(PICKS);
  });

  it('says a card does not fit when the server cannot be asked, and asks again next time', async () => {
    const failed = firstValueFrom(puzzle.verdict('ally', '1_1'));
    backend
      .expectOne(PICKS)
      .flush({ error: 'There is no scenario called x.' }, { status: 404, statusText: 'Not Found' });
    expect(await failed).toEqual({ fits: false, why: 'There is no scenario called x.', name: null });
    const again = firstValueFrom(puzzle.verdict('ally', '1_1'));
    backend.expectOne(PICKS).flush({ ok: true, picks: { ally: { fits: true, why: null, name: 'One' } } });
    expect((await again).fits).toBe(true);
    // (an answer without the slot)
    const blank = firstValueFrom(puzzle.verdict('ally', '1_2'));
    backend.expectOne(PICKS).flush({});
    expect(await blank).toEqual({ fits: false, why: NO_VERDICT, name: null });
  });
});

describe('readVerdict', () => {
  it("reads one slot's verdict from an answer of scenarios/picks", () => {
    expect(readVerdict({ picks: { a: { fits: true, why: null, name: 'X' } } }, 'a')).toEqual({
      fits: true,
      why: null,
      name: 'X',
    });
    expect(readVerdict({ picks: { a: { fits: 'yes' } } }, 'a')).toBeNull();
    expect(readVerdict(null, 'a')).toBeNull();
  });
});
