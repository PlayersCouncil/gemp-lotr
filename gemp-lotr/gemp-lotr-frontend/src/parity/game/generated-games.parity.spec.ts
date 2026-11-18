import { TestBed } from '@angular/core/testing';
import type { LegacyGameModel } from '../../../visual-check/game/controls.mjs';
import { openAngularGamePage, playAngularUntil } from '../../testing/game/angular-game-page';
import { FakeGameServer } from '../../testing/game/fake-game-server';
import { generatedGame } from '../../testing/game/generated-game';
import { openLegacyGamePage } from '../../testing/game/legacy-game-page';
import { comparable, legacyView } from '../../testing/game/legacy-projection';
import { GameRecording } from '../../testing/game/recording';
import { playUntil } from '../../testing/game/replay-driver';

/**
 * How many games, starting from which seed. A few run with the unit tests; `npm run test:parity` (part of check:all in
 * visual-check) runs many. A failure names its seed: PARITY_GAME_SEED=<seed> PARITY_GAMES=1 runs that game alone.
 */
const GAMES = Number(process.env['PARITY_GAMES'] ?? 4);
const FIRST_SEED = Number(process.env['PARITY_GAME_SEED'] ?? 1);

/** The legacy page through the game: the game it holds after each step. Fails if the generated game doesn't fit it. */
async function legacyModels(recording: GameRecording): Promise<LegacyGameModel[]> {
  const server = new FakeGameServer(recording);
  // Not past the first step while the page opens: the made-up polls are answered at once
  server.limit = recording.steps[0]?.exchanges ?? Infinity;
  const page = await openLegacyGamePage(server);
  try {
    const models: LegacyGameModel[] = [];
    for (const step of recording.steps) {
      try {
        playUntil(page, server, step.exchanges, step.gestures);
      } catch (error) {
        throw new Error(`${String(error)}; the page's errors: ${page.errors.join('; ')}`, { cause: error });
      }
      models.push(page.model());
    }
    expect(server.problems, 'the legacy page took the generated game differently').toEqual([]);
    expect(page.errors, 'the legacy page failed on the generated game').toEqual([]);
    return models;
  } finally {
    page.close();
  }
}

/**
 * Games made up from a seed (src/testing/game/generated-game.ts): random events that fit together and decisions of
 * every kind, each answered with the page's gestures. The legacy game page plays each in jsdom first; the Angular game
 * page must then send exactly the same requests (the answers its gestures give) and hold the same game after every
 * step.
 */
describe('generated games on both game pages', () => {
  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  // A whole game on both pages; the first also loads game.html's scripts, and coverage (Maven's test run) slows it
  // past the default 5 s
  it.each(Array.from({ length: GAMES }, (_, i) => FIRST_SEED + i))('seed %i', { timeout: 30_000 }, async (seed) => {
    const recording = generatedGame(seed);
    const legacy = await legacyModels(recording);

    vi.useFakeTimers();
    sessionStorage.clear();
    const server = new FakeGameServer(recording);
    server.limit = recording.steps[0]?.exchanges ?? Infinity;
    const page = await openAngularGamePage(server);
    try {
      for (const [i, step] of recording.steps.entries()) {
        await playAngularUntil(page, server, step.exchanges, step.gestures);
        expect({ seed, step: step.name, model: comparable(legacyView(page.model())) }).toEqual({
          seed,
          step: step.name,
          model: comparable(legacy[i]!),
        });
      }
      expect(server.problems, `seed ${seed}`).toEqual([]);
    } finally {
      page.close();
    }
  });
});
