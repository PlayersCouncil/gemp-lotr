import { FakeGameServer } from '../../testing/game/fake-game-server';
import { LegacyGamePage, openLegacyGamePage } from '../../testing/game/legacy-game-page';
import { gameRecordingNames, readGameRecording } from '../../testing/game/recording';
import { playUntil } from '../../testing/game/replay-driver';

/**
 * The recorded games (visual-check/game, `npm run check:game:record`) played back on the unchanged legacy game page in
 * jsdom: the page must send exactly the recorded requests, making the browser check's gestures, and hold the same game
 * after every step as it did in the browser. This is the ground the Angular game page is compared on.
 */
describe.each(gameRecordingNames())('the legacy game page replays the recorded game "%s"', (name) => {
  let page: LegacyGamePage;
  let server: FakeGameServer;
  const recording = readGameRecording(name);

  beforeEach(async () => {
    server = new FakeGameServer(recording);
    page = await openLegacyGamePage(server);
  });

  afterEach(() => page.close());

  it('sends the recorded requests and holds the recorded game after every step', () => {
    for (const step of recording.steps) {
      playUntil(page, server, step.exchanges, step.gestures);
      expect({ step: step.name, model: page.model() }).toEqual({ step: step.name, model: step.model });
    }
    expect(server.problems).toEqual([]);
    expect(page.errors).toEqual([]);
  });
});
