package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.ArrayList;
import java.util.HashMap;
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
    /**
     * @param label What the player did there, in a few words ("Played Gimli", "Passed"), as the player is told it.
     * @param publicLabel The same as anybody else may be told it: without the name of a card they could not see.
     */
    public record Point(int entryCount, String player, String label, String publicLabel, int turn, Phase phase, Kind kind) { }

    private static final List<Phase> FROM_MANEUVER = List.of(Phase.MANEUVER, Phase.ARCHERY, Phase.ASSIGNMENT, Phase.SKIRMISH, Phase.REGROUP);

    private final GameJournal _journal;
    private LotroGame _game;
    private final List<Point> _points = new ArrayList<>();

    private record Offered(String title, Zone zone) { }

    /** The cards the decision being answered offers, by the id it offers each under. */
    private final Map<String, Offered> _offered = new HashMap<>();
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
        //What the cards on offer are and where they lie, before the answer moves any of them
        _offered.clear();
        String[] ids = decision.getDecisionParameters().get("cardId");
        if (ids != null && decision instanceof ArbitraryCardsSelectionDecision arbitrary) {
            List<PhysicalCard> cards = new ArrayList<>(arbitrary.getPhysicalCards());
            for (int i = 0; i < ids.length && i < cards.size(); i++)
                _offered.put(ids[i], new Offered(cards.get(i).getBlueprint().getTitle(), cards.get(i).getZone()));
        } else if (ids != null && decision.getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
            for (String id : ids) {
                PhysicalCard card = _game.getGameState().findCardById(Integer.parseInt(id));
                if (card != null)
                    _offered.put(id, new Offered(card.getBlueprint().getTitle(), card.getZone()));
            }
        }
    }

    @Override
    public void decisionAnswered(String playerId, AwaitingDecision decision, String answer) {
        if (_answeringAt < 0)
            return;
        //Everything before the first turn is the game being set up, whatever phase the game says it is in
        Kind kind = _answeringTurn > 0 ? Kind.PLAY
                : decision.getText() != null && decision.getText().startsWith("Starting fellowship") ? Kind.STARTING_FELLOWSHIP
                : Kind.SETUP;
        String given = answer == null ? "" : answer;
        _points.add(new Point(_answeringAt, playerId, label(decision, given, kind, false), label(decision, given, kind, true),
                _answeringTurn, _answeringPhase, kind));
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

    /**
     * A place to go back to that is not an answer: a sandbox's Stop, an edit to its board, its Start
     * (ScenarioSandbox).  It is the player's own, made now, in the turn and phase the game is in.
     * @param entryCount How many entries the journal holds just before the note that makes it.
     * @param label What was done there, as the player is told it ("Added Gimli").
     */
    public void note(int entryCount, String player, String label) {
        int turn = _game.getGameState().getTurnNumber();
        _points.add(new Point(entryCount, player, label, label, turn, _game.getGameState().getCurrentPhase(), Kind.PLAY));
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

    /**
     * What the player did, in a few words: these go on buttons and in one line of a list.
     * @param forOthers Whether this is for somebody else to read, who is not to learn from it which card was chosen
     *                  from where they cannot see.
     */
    private String label(AwaitingDecision decision, String answer, Kind kind, boolean forOthers) {
        Map<String, String[]> parameters = decision.getDecisionParameters();
        try {
            return switch (decision.getDecisionType()) {
                case CARD_ACTION_CHOICE, ACTION_CHOICE -> {
                    if (answer.isEmpty())
                        yield "Passed";
                    int index = indexOf(parameters.get("actionId"), answer);
                    yield index < 0 ? "Acted" : done(plain(parameters.get("actionText")[index]));
                }
                case MULTIPLE_CHOICE -> "Chose \"" + plain(parameters.get("results")[Integer.parseInt(answer)]) + "\"";
                case INTEGER -> "Chose " + answer;
                case ARBITRARY_CARDS, CARD_SELECTION -> cards(decision, answer, kind, forOthers);
                case ASSIGN_MINIONS -> answer.isEmpty() ? "Assigned no minions" : "Assigned minions";
                default -> "Answered";
            };
        } catch (RuntimeException exp) {
            return "Answered";
        }
    }

    /** "Play Gimli" as something done: "Played Gimli". */
    private static String done(String action) {
        if (action.startsWith("Play "))
            return "Played " + action.substring(5);
        if (action.startsWith("Use "))
            return "Used " + action.substring(4);
        return action;
    }

    private String cards(AwaitingDecision decision, String answer, Kind kind, boolean forOthers) {
        boolean fellowship = kind == Kind.STARTING_FELLOWSHIP;
        if (answer.isEmpty())
            return fellowship ? "Pressed DONE" : "Chose nothing";
        String[] chosen = answer.split(",");
        Offered first = _offered.get(chosen[0]);
        //A card for a starting fellowship goes onto the table; any other is named to others only if it lay in the open
        boolean named = first != null && (!forOthers || fellowship || (first.zone() != null && first.zone().isPublic()));
        String what = !named ? (chosen.length == 1 ? "a card" : chosen.length + " cards")
                : first.title() + (chosen.length == 1 ? "" : " +" + (chosen.length - 1));
        return (fellowship ? "Played " : "Chose ") + what;
    }
}
