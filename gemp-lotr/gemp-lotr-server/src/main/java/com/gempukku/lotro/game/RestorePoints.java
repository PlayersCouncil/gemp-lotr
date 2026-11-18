package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The places a game can be taken back to: every answer a player has given, with where in the journal it sits, when in
 * the game it was given and words to show for it.  Taking the game back to one puts it at the moment that answer's
 * decision was waiting (LotroGameMediator.revertTo with the point's entryCount).
 *
 * To keep them for a game: add this to the game's feedback as a decision observer, before the game starts.  A game
 * rebuilt from its journal gathers its points again the same way, by being played.
 */
public class RestorePoints implements DecisionObserver {
    public enum Kind {
        /** The bids, the choice of who goes first, the mulligans: never gone back to. */
        SETUP,
        /** A card chosen for a starting fellowship, or the saying that it is complete: until the hands are dealt. */
        STARTING_FELLOWSHIP,
        /** Anything answered once the first turn has begun. */
        PLAY
    }

    /**
     * @param entryCount How many entries the journal held when the answer was given.
     * @param turn The number of the turn it was given in; 0 before the first.
     */
    public record Point(int entryCount, String player, String label, int turn, Phase phase, Kind kind) { }

    private static final List<Phase> FROM_MANEUVER = List.of(Phase.MANEUVER, Phase.ARCHERY, Phase.ASSIGNMENT, Phase.SKIRMISH, Phase.REGROUP);

    private final GameJournal _journal;
    private LotroGame _game;
    private final List<Point> _points = new ArrayList<>();

    private int _answeringAt = -1;
    private int _answeringTurn;
    private Phase _answeringPhase;
    /** The latest turn that has got as far as its maneuver phase. */
    private int _turnPastManeuver = -1;

    public RestorePoints(GameJournal journal) {
        _journal = journal;
    }

    public void setGame(LotroGame game) {
        _game = game;
    }

    private void lookAtPhase() {
        if (_game.getGameState().getCurrentPhase() != null && FROM_MANEUVER.contains(_game.getGameState().getCurrentPhase()))
            _turnPastManeuver = Math.max(_turnPastManeuver, _game.getGameState().getTurnNumber());
    }

    @Override
    public void decisionIssued(String playerId, AwaitingDecision decision) {
        lookAtPhase();
    }

    @Override
    public void decisionAnswering(String playerId, AwaitingDecision decision) {
        lookAtPhase();
        _answeringAt = _journal.entries().size();
        _answeringTurn = _game.getGameState().getTurnNumber();
        _answeringPhase = _game.getGameState().getCurrentPhase();
    }

    @Override
    public void decisionAnswered(String playerId, AwaitingDecision decision, String answer) {
        if (_answeringAt < 0)
            return;
        //Everything before the first turn is the game being set up, whatever phase the game says it is in
        Kind kind = _answeringTurn > 0 ? Kind.PLAY
                : decision.getText() != null && decision.getText().startsWith("Starting fellowship") ? Kind.STARTING_FELLOWSHIP
                : Kind.SETUP;
        _points.add(new Point(_answeringAt, playerId, label(decision, answer == null ? "" : answer), _answeringTurn, _answeringPhase, kind));
        _answeringAt = -1;
    }

    /**
     * @return The points this player may ask to have the game taken back to as things stand, the latest first.
     */
    public List<Point> offered(String player, UndoPolicy policy) {
        lookAtPhase();
        int turn = _game.getGameState().getTurnNumber();
        boolean dealt = false;
        for (String anybody : _game.getGameState().getPlayerOrder() == null ? List.<String>of() : _game.getGameState().getPlayerOrder().getAllPlayers())
            dealt |= !_game.getGameState().getHand(anybody).isEmpty();

        List<Point> result = new ArrayList<>();
        for (int i = _points.size() - 1; i >= 0; i--) {
            Point point = _points.get(i);
            if (!point.player().equals(player))
                continue;
            boolean reachable = switch (point.kind()) {
                case SETUP -> false;
                case STARTING_FELLOWSHIP -> !dealt;
                //The turn before stays within reach until the new turn has got to its maneuver phase: the Free
                // Peoples player's fellowship phase and the Shadow player's shadow phase
                case PLAY -> !policy.horizon() || point.turn() == turn || (point.turn() == turn - 1 && _turnPastManeuver < turn);
            };
            if (reachable)
                result.add(point);
        }
        return result;
    }

    /** Every point there is, soonest first, whoever's and however long ago. */
    public List<Point> all() {
        return List.copyOf(_points);
    }

    private static String plain(String text) {
        return text == null ? "" : text.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }

    private static int indexOf(String[] values, String value) {
        if (values != null)
            for (int i = 0; i < values.length; i++)
                if (values[i].equals(value))
                    return i;
        return -1;
    }

    /** What was asked and what was chosen, in words a player would know it by. */
    private String label(AwaitingDecision decision, String answer) {
        Map<String, String[]> parameters = decision.getDecisionParameters();
        String asked = plain(decision.getText());
        String chosen;
        try {
            chosen = switch (decision.getDecisionType()) {
                case CARD_ACTION_CHOICE, ACTION_CHOICE -> {
                    if (answer.isEmpty())
                        yield "Pass";
                    int index = indexOf(parameters.get("actionId"), answer);
                    yield index < 0 ? asked : plain(parameters.get("actionText")[index]);
                }
                case MULTIPLE_CHOICE -> asked + ": " + plain(parameters.get("results")[Integer.parseInt(answer)]);
                case INTEGER -> asked + ": " + answer;
                case ARBITRARY_CARDS, CARD_SELECTION -> answer.isEmpty() ? asked + ": none"
                        : asked + ": " + answer.split(",").length + (answer.split(",").length == 1 ? " card" : " cards");
                default -> asked;
            };
        } catch (RuntimeException exp) {
            chosen = asked;
        }
        String when = _answeringTurn > 0 ? "Turn " + _answeringTurn + ", " : "";
        return when + (_answeringPhase == null ? "" : _answeringPhase.getHumanReadable() + ": ") + chosen;
    }
}
