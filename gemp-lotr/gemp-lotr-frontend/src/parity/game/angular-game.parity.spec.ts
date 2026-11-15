import { TestBed } from '@angular/core/testing';
import { AngularGamePage, openAngularGamePage, playAngularUntil } from '../../testing/game/angular-game-page';
import { FakeGameServer } from '../../testing/game/fake-game-server';
import { comparable, legacyView } from '../../testing/game/legacy-projection';
import { gameRecordingNames, readGameRecording } from '../../testing/game/recording';

/**
 * The recorded games (visual-check/game, `npm run check:game:record`) played on the Angular game page in jsdom with
 * the gestures the legacy page was given (legacy-game.parity.spec.ts): the page must send exactly the recorded
 * requests, in their order, and hold after every step the game the legacy page held in the browser.
 */
describe.each(gameRecordingNames())('the Angular game page replays the recorded game "%s"', (name) => {
  let page: AngularGamePage;
  let server: FakeGameServer;
  const recording = readGameRecording(name);

  beforeEach(async () => {
    vi.useFakeTimers();
    sessionStorage.clear();
    server = new FakeGameServer(recording);
    page = await openAngularGamePage(server);
  });

  afterEach(() => {
    page.close();
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  it('sends the recorded requests and holds the recorded game after every step', async () => {
    for (const step of recording.steps) {
      // Recorded requests the Angular page doesn't make: none left (card information with the server's modifiers came
      // with G4 of trymarz/gemp-lotr-private#27)
      const skipped = () => false;
      await playAngularUntil(page, server, step.exchanges, step.gestures, skipped);
      expect({ step: step.name, model: comparable(legacyView(page.model())) }).toEqual({
        step: step.name,
        model: comparable(step.model),
      });
    }
    expect(server.problems).toEqual([]);
  });
});
