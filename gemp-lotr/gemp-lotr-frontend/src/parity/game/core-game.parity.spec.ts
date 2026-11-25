import { applyEvents } from '../../core/game/state/apply-event';
import { emptyGame, GameModel } from '../../core/game/state/game-model';
import { assignMinion, decisionAnswered, unassignMinion } from '../../core/game/state/local-choices';
import { parseGameUpdate } from '../../core/game/protocol/game-xml';
import { parseXml } from '../../core/api/xml';
import { comparable, legacyView } from '../../testing/game/legacy-projection';
import { gameRecordingNames, readGameRecording, RecordedExchange } from '../../testing/game/recording';

/** A request of the game itself (its state, polls, answers, concede, cancel): its answer carries game events. */
const carriesEvents = (exchange: RecordedExchange) =>
  exchange.path.startsWith('/game/<id>') &&
  !exchange.path.endsWith('/cardInfo') &&
  exchange.contentType.includes('xml');

/**
 * The Angular core (src/core/game) against the legacy game page, on every recorded game: the server's answers applied
 * in the recorded order, each answer the player sent closing its decision, the player's assignments as they made them;
 * after every step the core must hold what the legacy page held in the browser (legacy-projection.ts).
 */
describe.each(gameRecordingNames())('the game core holds what the legacy page held in "%s"', (name) => {
  const recording = readGameRecording(name);

  it('after every step', () => {
    let model: GameModel = emptyGame();
    let next = 0;
    for (const step of recording.steps) {
      // The gestures of this step that sent nothing (assignments), each after the exchanges recorded before it
      const gestures = [...step.gestures];
      const local = (upTo: number) => {
        while (gestures.length > 0 && (gestures[0]?.after ?? 0) <= upTo) {
          const gesture = gestures.shift();
          if (gesture?.assign) model = assignMinion(model, ...gesture.assign);
          if (gesture?.unassign) model = unassignMinion(model, gesture.unassign);
        }
      };
      for (; next < step.exchanges; next++) {
        local(next);
        const exchange = recording.exchanges[next];
        if (!exchange || !carriesEvents(exchange)) continue;
        if ('decisionId' in exchange.params) model = decisionAnswered(model);
        const update = parseGameUpdate(parseXml(exchange.body));
        if (update) model = applyEvents(model, update.events);
      }
      local(next);
      expect({ step: step.name, model: comparable(legacyView(model)) }).toEqual({
        step: step.name,
        model: comparable(step.model),
      });
    }
  });
});
