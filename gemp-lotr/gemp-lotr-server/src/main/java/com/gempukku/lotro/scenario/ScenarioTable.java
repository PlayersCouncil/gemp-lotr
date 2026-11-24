package com.gempukku.lotro.scenario;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.DecisionArbiter;
import com.gempukku.lotro.game.DecisionObserver;
import com.gempukku.lotro.game.GameJournal;
import com.gempukku.lotro.game.GameRandom;
import com.gempukku.lotro.game.JournalFollower;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.PregameSetup;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.scenario.ScenarioDefinition.CardDef;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoOp;
import com.gempukku.lotro.scenario.ScenarioDefinition.SetupZone;
import com.gempukku.lotro.virtualtable.VirtualTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A virtual table laid out the way a scenario file says: each seat gets a deck made of exactly the cards the
 * scenario names for it, the scenario's rules are in force from the start, and the first turn begins with the board
 * the scenario describes instead of with starting fellowships, an opening draw and mulligans.
 *
 * This is everything about a scenario's game that does not depend on who is answering the decisions.  A headless
 * run answers them all from the script (see ScenarioRunner); a live game answers one seat's from the script and
 * leaves the other to a person.
 */
public class ScenarioTable extends VirtualTable implements PregameSetup {
    /** Parsing card-definition JSON needs the card builder's factories, which are costly to set up and safe to share. */
    private static final LotroCardBlueprintBuilder BUILDER = new LotroCardBlueprintBuilder();
    /** Rules cards need a blueprint ID of the usual shape.  No set has this number, and no client is ever sent it. */
    private static final String RULES_BLUEPRINT_ID = "999_1";

    private final ScenarioDefinition _def;
    private final LotroCardBlueprintLibrary _library;
    private final LotroFormat _format;
    private final String _freepsPlayer;
    private final String _shadowPlayer;

    private final Map<String, PhysicalCardImpl> _aliases = new LinkedHashMap<>();
    private final Map<Seat, PhysicalCardImpl> _rulesCards = new EnumMap<>(Seat.class);
    private final Map<String, Requirement[]> _requirements = new HashMap<>();
    private final List<String> _problems = new ArrayList<>();
    private final List<DecisionObserver> _observers = new ArrayList<>();
    private DecisionArbiter _arbiter;
    private final Map<String, LotroDeck> _decks = new HashMap<>();
    private ScenarioRandom _random;
    private boolean _arranged;
    private AwaitingDecision _opening;

    /** Players are written down by seat, so that a journal can be followed whoever is sitting there. */
    private final GameJournal _journal = new GameJournal(player -> SeatOf(player).key());
    private GameJournal _following;
    /** Feeds this game the journal it follows; shared with everything else that follows a journal. */
    private final JournalFollower _follower = new JournalFollower();
    private String _followProblem;
    private java.util.function.Consumer<GameJournal.Entry> _noteFollower;
    private java.util.function.BiPredicate<String, Phase> _askToPass;
    private java.util.function.BiConsumer<String, Phase> _autoPassed;

    /**
     * @param humanPlayer The name of the player in the scenario's "human" seat.
     * @param opponentPlayer The name of the player in the other seat.
     */
    public ScenarioTable(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format,
                         String humanPlayer, String opponentPlayer) {
        _def = def;
        _library = library;
        _format = format;
        _freepsPlayer = def.human == Seat.FREEPS ? humanPlayer : opponentPlayer;
        _shadowPlayer = def.human == Seat.FREEPS ? opponentPlayer : humanPlayer;
        Cards.put(_freepsPlayer, new HashMap<>());
        Cards.put(_shadowPlayer, new HashMap<>());
    }

    @Override
    public String P1() { return _freepsPlayer; }
    @Override
    public String P2() { return _shadowPlayer; }

    public ScenarioDefinition Definition() { return _def; }

    public String Player(Seat seat) { return seat == Seat.FREEPS ? _freepsPlayer : _shadowPlayer; }
    public Seat SeatOf(String player) { return player.equals(_freepsPlayer) ? Seat.FREEPS : Seat.SHADOW; }
    public String HumanPlayer() { return Player(_def.human); }
    public String OpponentPlayer() { return Player(_def.human.other()); }

    /**
     * The role a player is playing at this moment: the Free Peoples player is the one whose turn it is, the other is
     * the Shadow player.  (In a one-sided game that is always the Free Peoples seat's turn; before the first turn,
     * and while the game opens, the seat that is to take the first.)  A seat is a chair, and in a two-sided game it
     * plays both roles in turn.
     */
    public com.gempukku.lotro.common.Side RoleOf(String player) {
        //(before the first turn, whoever the game has as its current player while it opens, the seats are as named)
        String current = _gameState == null || _gameState.getTurnNumber() < 1 ? null : _gameState.getCurrentPlayerId();
        String freePeoples = current != null ? current : _freepsPlayer;
        return player.equals(freePeoples) ? com.gempukku.lotro.common.Side.FREE_PEOPLE : com.gempukku.lotro.common.Side.SHADOW;
    }

    /** The seat whose turn it is (before the first turn, the seat that is to take it). */
    public Seat TurnSeat() {
        return RoleOf(_freepsPlayer) == com.gempukku.lotro.common.Side.FREE_PEOPLE ? Seat.FREEPS : Seat.SHADOW;
    }

    /**
     * @return The card a scenario alias refers to.
     */
    public PhysicalCardImpl Card(String alias) {
        PhysicalCardImpl card = _aliases.get(alias);
        if (card == null && _def.setup.realOpening && !_dealt)
            throw new IllegalStateException("The scenario's cards have no names yet: in the game's own opening (setup.opening: game) the "
                    + "cards are dealt once the seats have been chosen, so nothing about the bid or the choice of going first can name one ('"
                    + alias + "')");
        if (card == null)
            throw new IllegalStateException("The scenario has no card called '" + alias + "' on the table");
        return card;
    }

    /**
     * @return The scenario alias of a card, or null if the scenario did not name it.
     */
    public String AliasOf(PhysicalCard card) {
        for (Map.Entry<String, PhysicalCardImpl> entry : _aliases.entrySet())
            if (entry.getValue() == card)
                return entry.getKey();
        return null;
    }

    public String AliasOfCardId(String cardId) {
        for (Map.Entry<String, PhysicalCardImpl> entry : _aliases.entrySet())
            if (String.valueOf(entry.getValue().getCardId()).equals(cardId))
                return entry.getKey();
        return null;
    }

    public ScenarioRandom Random() { return _random; }

    /**
     * Things that went wrong while laying the table out or while the scenario's own instructions were carried out.
     * A well-formed scenario produces none.
     */
    public List<String> Problems() {
        List<String> all = new ArrayList<>(_problems);
        if (_random != null)
            all.addAll(_random.problems());
        return all;
    }

    /**
     * Has an observer told about every decision of the game and every answer to one.  Only before the game exists.
     */
    public void Observe(DecisionObserver observer) {
        if (_game != null)
            throw new IllegalStateException("The game already exists; it is too late to start observing it");
        _observers.add(observer);
    }

    /**
     * The record of this game: every answer either seat has given since the board was laid out, and every random
     * outcome since the game began.  See GameJournal.
     */
    public GameJournal Journal() { return _journal; }

    /**
     * Has this game repeat an earlier game of the same scenario: Begin will give it the earlier game's random
     * outcomes and then its answers, one after another, and leave it where the earlier journal ends.  Only before
     * the game exists.
     */
    public void Follow(GameJournal earlier) {
        if (_game != null)
            throw new IllegalStateException("The game already exists; it is too late to have it follow another");
        _following = earlier;
        _journal.replayRandomFrom(earlier);
    }

    /**
     * Has somebody handed each note of a journal this game follows, at the point where it was written.  Notes are
     * about whatever runs the game, so it is for whatever runs the game to act on them.
     */
    public void OnNote(java.util.function.Consumer<GameJournal.Entry> follower) {
        _noteFollower = follower;
    }

    /**
     * Has a player asked to pass in a phase they would otherwise be passed through, whenever this says so (see
     * ScenarioDirector.asksToPass).  It is not asked until the board has been laid out.
     */
    public void AskToPass(java.util.function.BiPredicate<String, Phase> askToPass) {
        _askToPass = askToPass;
    }

    /**
     * Is told each time a player passes in a phase without being asked (nothing to play, and a phase they are
     * passed through), once the board has been laid out.  Told inside the game's processing, before the pass is made.
     */
    public void AutoPassed(java.util.function.BiConsumer<String, Phase> autoPassed) {
        _autoPassed = autoPassed;
    }

    /**
     * Has the game's random outcomes pass through something else on their way (something that wants to hear of each
     * shuffle, say), outside the journal and the scenario's own random.  Only before the game is begun.
     */
    public void WatchRandom(java.util.function.UnaryOperator<GameRandom> watcher) {
        _gameState.setRandom(watcher.apply(_journal.random(_random)));
    }

    /** How many of the earlier game's answers this one was given. */
    public int AnswersFollowed() { return _follower.answersFollowed(); }

    /** Whether the earlier game it followed took this one anywhere: an answer given, or a note acted on. */
    public boolean FollowedAnything() { return _follower.answersFollowed() > 0 || _follower.notesFollowed() > 0; }

    /**
     * @return Why this game could not follow the earlier one to its end; null if it did (or was not asked to).  A
     * game that could not is somewhere between the start and where it was meant to get to, and is best thrown away.
     */
    public String FollowProblem() { return _followProblem; }

    /**
     * Has the game hold its decisions back until it comes to rest, and lets the arbiter say which are put to their
     * players then.  Only before the game exists.
     */
    public void Arbitrate(DecisionArbiter arbiter) {
        if (_game != null)
            throw new IllegalStateException("The game already exists; it is too late to arbitrate its decisions");
        _arbiter = arbiter;
    }

    /**
     * @return Whether the scenario's board has been laid out yet.  Until it has, no alias refers to anything.
     */
    public boolean Arranged() { return _arranged; }

    /** The deck each player was given, by player name. */
    public Map<String, LotroDeck> Decks() { return _decks; }

    /**
     * Builds the decks, starts the game, seats the players and lets the game run to its first decision: the Free
     * Peoples seat's first choice of the first turn, with the board arranged as the scenario describes.
     */
    public void Start() {
        Prepare();
        Begin();
    }

    /**
     * The first half of Start: builds the decks and the game, and stops there.  Nothing has happened in the game
     * yet, so it can still be handed to whatever is going to run it.
     */
    public void Prepare() {
        for (Seat seat : Seat.values()) {
            LotroDeck deck = new LotroDeck(_def.title);
            String ringBearer = _def.ringBearer.get(seat);
            String ring = _def.ring.get(seat);
            deck.setRingBearer(_def.cards.get(ringBearer).blueprintId());
            if (ring != null)
                deck.setRing(_def.cards.get(ring).blueprintId());
            for (String site : _def.sites.get(seat))
                deck.addSite(site);
            for (CardDef card : _def.cards.values())
                if (card.owner() == seat && !card.alias().equals(ringBearer) && !card.alias().equals(ring))
                    deck.addCard(card.blueprintId());
            //The format decides which version of each card is in force, the same as at a hall table
            _decks.put(Player(seat), _format.applyErrata(deck));
        }

        CreateGame(_format, _decks, _library, null);
    }

    /**
     * The second half of Start: sets the game going and answers its opening questions.
     */
    public void Begin() {
        _game.startGame();

        //The Free Peoples seat outbids the other and elects to go first.  From there the game runs on its own
        // through the first site and the Ring-bearers, into arrangeBoard below, and on to the first turn.  (Unless
        // the game's own opening is the scenario's to play: then every one of these is the script's to answer.)
        if (!_def.setup.realOpening) {
            FreepsDecided("1");
            ShadowDecided("0");
            FreepsDecided("0");
            answerOpeningSite();
        }

        if (_following != null)
            follow(_following);
    }

    /**
     * On a path whose sites have no numbers (from the Shadows block on), the game's opening has the first player
     * choose which of their sites is site 1, before the board is laid out.  That is the table's to answer, as the bid
     * and the seat are: the first site setup.path names, if it is one of the Free Peoples seat's, and otherwise the
     * first site the scenario lists for that seat.
     */
    private void answerOpeningSite() {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(_freepsPlayer);
        if (_arranged || decision == null || decision.getDecisionType() != com.gempukku.lotro.logic.decisions.AwaitingDecisionType.ARBITRARY_CARDS)
            return;
        List<String> wanted = new ArrayList<>();
        if (!_def.setup.path.isEmpty())
            wanted.add(_def.setup.path.getFirst());
        wanted.addAll(_def.siteAliases.getOrDefault(Seat.FREEPS, List.of()));
        String[] ids = decision.getDecisionParameters().get("cardId");
        String[] blueprints = decision.getDecisionParameters().get("blueprintId");
        List<String> blueprintsWanted = _def.sites.getOrDefault(Seat.FREEPS, List.of());
        //(the aliases are bound only as the board is laid out, so the choice is known by its blueprint: the first
        // listed site that is on offer, in the order the scenario lists them)
        for (String alias : wanted) {
            int at = _def.siteAliases.getOrDefault(Seat.FREEPS, List.of()).indexOf(alias);
            if (at < 0 || ids == null || blueprints == null)
                continue;
            String blueprint = _format.applyErrata(blueprintsWanted.get(at));
            for (int i = 0; i < blueprints.length; i++)
                if (blueprints[i].equals(blueprint)) {
                    FreepsDecided(ids[i]);
                    return;
                }
        }
        if (ids != null && ids.length > 0)
            FreepsDecided(ids[0]);
    }

    private void follow(GameJournal earlier) {
        _follower.follow(earlier, new JournalFollower.Table() {
            @Override
            public String playerOf(String key) {
                return Player(key.equals(Seat.FREEPS.key()) ? Seat.FREEPS : Seat.SHADOW);
            }

            @Override
            public AwaitingDecision awaiting(String player) {
                return userFeedback().getAwaitingDecision(player);
            }

            @Override
            public void answer(String player, AwaitingDecision decision, String answer) {
                PlayerDecided(player, answer);
            }

            @Override
            public boolean note(GameJournal.Entry note) {
                if (_noteFollower == null)
                    return false;
                _noteFollower.accept(note);
                return true;
            }

            @Override
            public String describe(String key) {
                return "the " + key + " seat";
            }
        });
        _followProblem = _follower.problem();
        if (_followProblem == null && _journal.divergence() != null)
            _followProblem = _journal.divergence();
    }

    @Override
    protected String GameName() { return "Scenario - " + _def.title; }

    @Override
    protected void BeforeGameStarts() {
        for (DecisionObserver observer : _observers)
            _userFeedback.addDecisionObserver(observer);
        if (_arbiter != null)
            _userFeedback.setDecisionArbiter(_arbiter);
        //The table answers the bid and the choice of seat itself, the same way every time; they are not the game's story
        // (unless the scenario plays the game's own opening, which is: see Dealt)
        _arranged = _def.setup.realOpening;
        _userFeedback.addDecisionObserver(new DecisionObserver() {
            @Override
            public void decisionIssued(String playerId, AwaitingDecision decision) {
                Dealt();
            }

            @Override
            public void decisionAnswering(String playerId, AwaitingDecision decision) {
                if (_arranged)
                    _journal.decisionAnswering(playerId, decision);
            }

            @Override
            public void decisionAnswered(String playerId, AwaitingDecision decision, String answer) {
                if (_arranged)
                    _journal.decisionAnswered(playerId, decision, answer);
            }
        });

        _random = new ScenarioRandom(this, _def);
        _gameState.setRandom(_journal.random(_random));
        if (!_def.setup.realOpening)
            _gameState.setPregameSetup(this);
        if (_def.oneSided)
            _gameState.setFixedFreePeoplePlayer(_freepsPlayer);

        //The person playing is asked to pass exactly where the scenario says, whatever their own settings are.  The
        // scripted opponent is only ever asked when it has something it could do.
        Set<Phase> passed = EnumSet.noneOf(Phase.class);
        passed.addAll(_def.autoPass);
        if (_def.autoPassByRole != null)
            _def.autoPassByRole.values().forEach(passed::addAll);
        _game.setPlayerAutoPassSettings(HumanPlayer(), passed);
        _game.setPlayerAutoPassSettings(OpponentPlayer(), EnumSet.allOf(Phase.class));
        //...except where the script's next step is about a pass: then whoever it is gets asked, to be passed by hand.
        // And where autoPass is said for each role, a phase is passed through only in the role it is said for: the
        // game knows one set of phases for a player, so it is given both roles' and asks in the role a phase is not
        // the person's to be passed through in.
        _game.setAskToPass((player, phase) -> _arranged && ((_askToPass != null && _askToPass.test(player, phase))
                || (_def.autoPassByRole != null && player.equals(HumanPlayer()) && !_def.autoPassFor(RoleOf(player)).contains(phase))));
        _game.setAutoPassed((player, phase) -> {
            if (_arranged && _autoPassed != null)
                _autoPassed.accept(player, phase);
        });

        for (Seat seat : Seat.values()) {
            try {
                JSONObject definition = new JSONObject();
                definition.put("title", "Scenario Rules");
                definition.put("type", "metasite");
                String rules = _def.rules.get(seat);
                if (rules != null)
                    definition.put("effects", new JSONParser().parse(rules));
                LotroCardBlueprint blueprint = BUILDER.buildFromJson(RULES_BLUEPRINT_ID, definition);
                PhysicalCardImpl rulesCard = _gameState.createLooseCard(Player(seat), RULES_BLUEPRINT_ID, blueprint);
                rulesCard.startAffectingGame(_game);
                _rulesCards.put(seat, rulesCard);
                _game.getActionsEnvironment().addAlwaysOnActionProxy(new RulesCardTriggers(rulesCard));
            } catch (InvalidCardDefinitionException | ParseException exp) {
                throw new IllegalArgumentException("rules." + seat.key() + ": " + exp.getMessage(), exp);
            }
        }
        _game.getActionsEnvironment().addAlwaysOnActionProxy(new EndOfThePath());
        if (_def.oneSided && _def.markersTogether)
            _game.getActionsEnvironment().addAlwaysOnActionProxy(new MarkersTogether());
    }

    /**
     * Why the game was ended by its fellowship coming to the end of the scenario's adventure path; null if it was not.
     * See EndOfThePath.
     */
    public String PathEnded() { return _pathEnded; }

    private String _pathEnded;

    /**
     * A scenario's path can be shorter than the game's nine sites, and a fellowship at its last site that has to move
     * on (the move of the Fellowship phase is not optional) would move to a site that is not there: the game cannot
     * carry on from that, and once broke on it.  So the game is ended just before such a move, as the move begins,
     * with the reason said in its log, and whatever runs the game is told why (PathEnded).
     */
    private class EndOfThePath extends AbstractActionProxy {
        @Override
        public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect) {
            //(this is asked once, as the effect is about to be played; what is done here is done there and then,
            // rather than as a trigger, which would have the player asked to put it in order with any card's own)
            if (effect.getType() != Effect.Type.BEFORE_MOVE_FROM || _pathEnded != null || game.getWinnerPlayerId() != null)
                return null;
            int at = game.getGameState().getCurrentSiteNumber();
            if (game.getGameState().getSite(at + 1) != null)
                return null;
            _pathEnded = "the fellowship came to the end of the scenario's path: it is at site " + at
                    + " and would have to move on to site " + (at + 1) + ", which nobody's adventure deck in the scenario has";
            game.getGameState().sendMessage("This scenario's path ends at site " + at + ": there is no site " + (at + 1)
                    + " to move to, so the game is over.");
            game.playerLost(game.getGameState().getCurrentPlayerId(), "The scenario's path ends at site " + at);
            return null;
        }
    }

    /**
     * rules.markers: together.  In a one-sided game the Shadow seat never takes a turn, so its marker never moves of
     * itself, and a site is only controlled once every marker is past it.  This has the Shadow seat's marker go
     * where the fellowship's has gone each time the fellowship moves, just after the marker moves and before
     * anything that answers the fellowship moving to its new site.
     */
    private class MarkersTogether extends AbstractActionProxy {
        @Override
        public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect) {
            //(done as it is asked, as EndOfThePath is, and for the same reason; it can be done any number of times)
            if (effect.getType() == Effect.Type.BEFORE_MOVE_TO) {
                int at = game.getGameState().getPlayerPosition(_freepsPlayer);
                if (game.getGameState().getPlayerPosition(_shadowPlayer) < at)
                    game.getGameState().setPlayerPosition(_shadowPlayer, at);
            }
            return null;
        }
    }

    /**
     * A rules card is in no zone, so nothing collects its triggers the way an in-play card's are collected.  This
     * does.  (Modifiers need no such help: startAffectingGame registers them.)
     */
    private static class RulesCardTriggers extends AbstractActionProxy {
        private final PhysicalCardImpl _rulesCard;

        private RulesCardTriggers(PhysicalCardImpl rulesCard) {
            _rulesCard = rulesCard;
        }

        @Override
        public List<? extends RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect) {
            return _rulesCard.getBlueprint().getRequiredBeforeTriggers(game, effect, _rulesCard);
        }

        @Override
        public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
            return _rulesCard.getBlueprint().getRequiredAfterTriggers(game, effectResult, _rulesCard);
        }
    }

    /**
     * Called by the game in place of the usual opening, once every card exists.
     */
    @Override
    public void arrangeBoard(LotroGame game) {
        for (Seat seat : Seat.values())
            bindAliases(seat);

        for (Seat seat : Seat.values()) {
            Map<SetupZone, List<String>> zones = _def.setup.zones.getOrDefault(seat, Map.of());
            for (Map.Entry<SetupZone, List<String>> entry : zones.entrySet()) {
                if (entry.getKey() == SetupZone.DECK)
                    continue;
                for (String alias : entry.getValue())
                    place(Card(alias), seat, entry.getKey(), false);
            }
            //Cards left in the deck are already in the order the scenario declares them.  The ones it lists for the
            // deck go on top of those, first listed topmost.
            List<String> deck = zones.getOrDefault(SetupZone.DECK, List.of());
            for (String alias : deck.reversed())
                MoveCardsToTopOfDeck(Card(alias));
        }

        for (Map.Entry<String, String> entry : _def.setup.attach.entrySet())
            AttachCardsTo(Card(entry.getValue()), Card(entry.getKey()));
        for (Map.Entry<String, String> entry : _def.setup.stack.entrySet())
            StackCardsOn(Card(entry.getValue()), Card(entry.getKey()));
        for (Map.Entry<String, Integer> entry : _def.setup.wounds.entrySet())
            AddWoundsToChar(Card(entry.getKey()), entry.getValue());
        for (Map.Entry<String, Integer> entry : _def.setup.tokens.entrySet())
            AddTokensToCard(Card(entry.getKey()), entry.getValue());
        //(one for the phase lasts to the end of the phase the game starts in: the board is laid out between turns)
        for (Map.Entry<String, List<ScenarioDefinition.Modification>> entry : _def.setup.modifiers.entrySet())
            for (ScenarioDefinition.Modification modification : entry.getValue())
                modify(Card(entry.getKey()), modification, _def.setup.phase, "setup.modifiers." + entry.getKey());

        //The burden that was bid for the first turn does not count; the scenario says how many there are.
        RemoveBurdens(GetBurdens());
        AddBurdens(_def.setup.burdens);
        if (_def.setup.threats > 0)
            AddThreats(_def.setup.threats);

        arrangeStart();

        //How the table got to this point (the bid, the choice of seat) is not part of the scenario's story
        _gameState.forgetMessages();
        _arranged = true;

        //A scenario whose first step waits for the opening has the game wait here, with the board laid out and the
        // first turn not begun, until that step has been read.  The game is made to wait the only way it can be: by
        // being asked something.  The scripted seat is asked, and the script answers for it once the step is done
        // with (ScenarioDirector.scriptedAnswer), so nobody ever sees the question.
        if (!_def.steps.isEmpty() && _def.steps.getFirst().when.opening) {
            _opening = new MultipleChoiceAwaitingDecision(1, "The scenario begins", new String[] {"Begin"}) {
                @Override
                protected void validDecisionMade(int index, String result) { }
            };
            _userFeedback.sendAwaitingDecision(OpponentPlayer(), _opening);
        }
    }

    /**
     * The part of the board that says where the story has got to (setup.startAt and what goes with it): the sites
     * the fellowships have passed, laid on the path; each seat's marker; the sites controlled; the twilight pool; the
     * skirmishes already assigned.  Each is put there the way the game itself puts it there, without any of the
     * game's reactions to it (nobody "plays" a site or "moves" to one here).
     */
    private void arrangeStart() {
        ScenarioDefinition.Setup setup = _def.setup;
        int opponentSite = setup.opponentSite(_def);
        layPath(Math.max(Math.max(setup.site, opponentSite), setup.path.size()), "setup.startAt");
        _gameState.setPlayerPosition(_freepsPlayer, setup.site);
        _gameState.setPlayerPosition(_shadowPlayer, opponentSite);

        for (Map.Entry<Seat, List<String>> entry : setup.controlled.entrySet())
            for (String site : entry.getValue()) {
                PhysicalCard card = siteOnPath(site, "setup.controlled." + entry.getKey().key());
                if (card != null)
                    control(card, entry.getKey(), "setup.controlled." + entry.getKey().key());
            }

        if (setup.twilight > 0)
            SetTwilight(setup.twilight);

        //The same call the assignment phase makes for each pairing it settles
        for (Map.Entry<String, List<String>> skirmish : setup.skirmishes.entrySet()) {
            java.util.Set<PhysicalCard> minions = new java.util.LinkedHashSet<>();
            for (String minion : skirmish.getValue())
                minions.add(Card(minion));
            _gameState.assignToSkirmishes(Card(skirmish.getKey()), minions);
        }
    }

    /**
     * Lays the adventure path out as far as a site, as the game lays a site when a fellowship first comes to it: the
     * site goes from its owner's adventure deck to its place on the path.  Whose site goes where is setup.path's to
     * say, and otherwise it is the Free Peoples seat's: its site of that number on a numbered path, its next listed
     * site on one that is not.  Site 1 is there already, laid by the game's opening (a different one is put in its
     * place, if setup.path says so).  Sites already on the path stay.
     */
    private void layPath(int last, String context) {
        for (int number = 1; number <= last; number++) {
            PhysicalCard there = _gameState.getSite(number);
            String named = number <= _def.setup.path.size() ? _def.setup.path.get(number - 1) : null;
            PhysicalCard wanted = named != null ? Card(named) : null;
            if (there != null && (wanted == null || wanted == there || _arranged))
                continue;
            if (wanted == null)
                wanted = siteToLay(number);
            if (wanted == null) {
                _problems.add(context + ": the path cannot be laid out to site " + number + ": the Free Peoples seat's sites have no site "
                        + number + " (list one under sites, or say whose it is in setup.path)");
                return;
            }
            if (wanted.getZone() != Zone.ADVENTURE_DECK) {
                _problems.add(context + ": '" + AliasOf(wanted) + "' cannot be site " + number + ": it is already on the path");
                return;
            }
            if (there != null) {
                //(the site the opening laid goes back where it came from, as a replaced site does)
                _gameState.removeCardsFromZone(there.getOwner(), List.of(there));
                ((PhysicalCardImpl) there).setSiteNumber(null);
                _gameState.addCardToZone(game(), there, Zone.ADVENTURE_DECK);
            }
            _gameState.removeCardsFromZone(wanted.getOwner(), List.of(wanted));
            ((PhysicalCardImpl) wanted).setSiteNumber(number);
            _gameState.addCardToZone(game(), wanted, Zone.ADVENTURE_PATH);
        }
    }

    /** The Free Peoples seat's site that is laid as the site of that number when nothing says whose it is. */
    private PhysicalCard siteToLay(int number) {
        List<? extends PhysicalCard> deck = _gameState.getAdventureDeck(_freepsPlayer);
        if (_gameState.usesUnorderedSites(_freepsPlayer)) {
            //(on a path whose sites have no numbers, the seat's sites in the order the scenario lists them)
            for (String alias : _def.siteAliases.getOrDefault(Seat.FREEPS, List.of())) {
                PhysicalCard card = _aliases.get(alias);
                if (card != null && card.getZone() == Zone.ADVENTURE_DECK)
                    return card;
            }
            return null;
        }
        for (PhysicalCard candidate : deck)
            if (candidate.getBlueprint().getSiteNumber() == number)
                return candidate;
        return null;
    }

    /**
     * A site on the adventure path, said by its number or its name.
     * @return Null, with the problem noted, if it is not on the path.
     */
    private PhysicalCard siteOnPath(String site, String context) {
        PhysicalCard card = site.matches("\\d+") ? _gameState.getSite(Integer.parseInt(site)) : _aliases.get(site);
        if (card == null || card.getZone() == null || !(card.getZone() == Zone.ADVENTURE_PATH || card.getCardController() != null)) {
            _problems.add(context + ": " + (site.matches("\\d+") ? "there is no site " + site : "'" + site + "' is not")
                    + " on the adventure path" + (site.matches("\\d+") ? "" : " (a site gets there by a fellowship coming to it)"));
            return null;
        }
        return card;
    }

    /**
     * A site controlled by a seat, the way the game has a site taken control of.  A site can only be controlled once
     * every fellowship has passed it.
     */
    private void control(PhysicalCard site, Seat seat, String context) {
        int number = site.getSiteNumber();
        for (Seat other : Seat.values())
            if (_gameState.getPlayerPosition(Player(other)) <= number) {
                _problems.add(context + ": site " + number + " cannot be controlled: the " + other.key() + " seat's marker is at site "
                        + _gameState.getPlayerPosition(Player(other)) + ", and a site is controlled only once every marker is past it"
                        + " (put it further on: setup.startAt.opponentSite, do: marker(" + other.key() + ", n), or rules.markers: together)");
                return;
            }
        if (site.getCardController() != null)
            _gameState.loseControlOfCard(site, Zone.ADVENTURE_PATH);
        _gameState.takeControlOfCard(Player(seat), game(), site, Zone.SUPPORT);
        //Between turns nothing is affecting the game, and the start of the turn starts everything there is,
        // controlled sites among them: one that was started here would be started twice
        if (!_gameState.getCurrentPhase().isCardsAffectGame())
            ((PhysicalCardImpl) site).stopAffectingGameControlledSite();
    }

    /**
     * A seat's marker put on a site, the path laid out to it as needed: not a move (nothing that answers moving
     * does), only where the marker is.  The site the fellowship whose turn it is stands at is the one whose text is
     * in force, so its marker taking it elsewhere has the one site stop and the other start, as a move does.
     */
    private void putMarker(Seat seat, int site, String context) {
        layPath(site, context);
        if (_gameState.getSite(site) == null)
            return;
        String player = Player(seat);
        boolean current = player.equals(_gameState.getCurrentPlayerId()) && _gameState.getCurrentPhase().isCardsAffectGame();
        if (current && _gameState.getCurrentSite() != null)
            ((PhysicalCardImpl) _gameState.getCurrentSite()).stopAffectingGame();
        _gameState.setPlayerPosition(player, site);
        if (current)
            ((PhysicalCardImpl) _gameState.getCurrentSite()).startAffectingGame(game());
    }

    /**
     * Called by the game once the board has been arranged (and the opening, if the scenario has one, read).  A game
     * that begins further on than the start of turn 1 at site 1 is carried on from where its story has got to.
     */
    @Override
    public com.gempukku.lotro.logic.timing.processes.GameProcess carryOn(LotroGame game) {
        if (!_def.setup.lateStart())
            return null;
        return new com.gempukku.lotro.logic.timing.processes.pregame.MidTurnStartGameProcess(_freepsPlayer, _def.setup.turn,
                _def.setup.phase, _def.setup.moves());
    }

    /** Set once the scenario's names are given to the game's cards (see Dealt). */
    private boolean _dealt;
    /** Why the game's own opening did not seat the players as the scenario declares them; null while it did. */
    private String _seatingProblem;

    /**
     * In a game whose own opening is played (setup.opening: game), the game's cards do not exist until the seats have
     * been chosen (the bid and the choice of going first come before them).  This gives them their names in the
     * scenario as soon as they do, before any decision about them is ruled on, and puts the cards setup.<seat>.deck
     * lists on top of their decks (the opening draw comes from there).  It is asked whenever a decision is about to
     * be ruled on, and does it once.
     */
    public void Dealt() {
        if (!_def.setup.realOpening || _dealt || _gameState == null || _gameState.getPlayerOrder() == null)
            return;
        _dealt = true;
        for (Seat seat : Seat.values())
            bindAliases(seat);
        for (Seat seat : Seat.values())
            for (String alias : _def.setup.zones.getOrDefault(seat, Map.of()).getOrDefault(SetupZone.DECK, List.of()).reversed())
                MoveCardsToTopOfDeck(Card(alias));
        //The seats are chairs the scenario declares: the Free Peoples seat is the one that goes first.  In the game's
        // own opening who goes first is decided by the bid and the choice, which the script has to make come out so
        String first = _gameState.getFirstPlayerId();
        if (first != null && !first.equals(_freepsPlayer))
            _seatingProblem = "the Shadow seat was seated to go first, but the scenario's Free Peoples seat (freeps) is the one that goes first: "
                    + "the answers to the bid and to the choice of going first (the person's allow, the opponent's answer) have to have the "
                    + "freeps seat bid more and choose to go first, or the shadow seat choose to go second";
    }

    /**
     * Whether the scenario's cards have their names: always once the board is laid out; in a game whose own opening
     * is played, once the seats have been chosen and the cards dealt (see Dealt).
     */
    public boolean CardsDealt() {
        return _arranged && (!_def.setup.realOpening || _dealt);
    }

    /**
     * Why the game's own opening seated the players otherwise than the scenario declares them (see Dealt); null if
     * it did not (or the scenario does not play the game's opening).  A game that did is not the scenario's game.
     */
    public String SeatingProblem() { return _seatingProblem; }

    /** Whether this is the table's own question at the opening (see arrangeBoard), not one of the game's. */
    public boolean IsOpening(AwaitingDecision decision) {
        return decision != null && decision == _opening;
    }

    private void bindAliases(Seat seat) {
        String player = Player(seat);
        String ringBearer = _def.ringBearer.get(seat);
        String ring = _def.ring.get(seat);
        bind(player, ringBearer, (PhysicalCardImpl) _gameState.getRingBearer(player));
        if (ring != null)
            bind(player, ring, (PhysicalCardImpl) _gameState.getRing(player));

        //Copies of the same card are told apart by position: the first copy in the deck is the first alias declared
        for (PhysicalCard card : _gameState.getDeck(player)) {
            String match = null;
            for (CardDef candidate : _def.cards.values()) {
                if (candidate.owner() == seat && !_aliases.containsKey(candidate.alias())
                        && !candidate.alias().equals(ringBearer) && !candidate.alias().equals(ring)
                        && _format.applyErrata(candidate.blueprintId()).equals(card.getBlueprintId())) {
                    match = candidate.alias();
                    break;
                }
            }
            if (match == null)
                _problems.add("A " + card.getBlueprintId() + " in the " + seat.key() + " deck matches no card alias");
            else
                bind(player, match, (PhysicalCardImpl) card);
        }

        //The seat's sites, by their number: the ones still in its adventure deck, and the one on the path already
        List<PhysicalCard> sites = new ArrayList<>(_gameState.getAdventureDeck(player));
        for (PhysicalCard card : _gameState.getInPlay())
            if (card.getBlueprint().getCardType() == com.gempukku.lotro.common.CardType.SITE && card.getOwner().equals(player))
                sites.add(card);
        List<String> aliases = _def.siteAliases.getOrDefault(seat, List.of());
        List<String> blueprints = _def.sites.getOrDefault(seat, List.of());
        for (int at = 0; at < aliases.size(); at++) {
            int number = at + 1;
            String blueprint = _format.applyErrata(blueprints.get(at));
            PhysicalCard found = null;
            for (PhysicalCard card : sites)
                if (found == null && card.getBlueprintId().equals(blueprint) && !_aliases.containsValue(card))
                    found = card;
            for (PhysicalCard card : sites)
                if (found == null && card.getBlueprint().getSiteNumber() == number && !_aliases.containsValue(card))
                    found = card;
            if (found == null)
                _problems.add("Site " + number + " of the " + seat.key() + " seat (" + aliases.get(at) + ") is not in the game");
            else
                bind(player, aliases.get(at), (PhysicalCardImpl) found);
        }
    }

    private void bind(String player, String alias, PhysicalCardImpl card) {
        _aliases.put(alias, card);
        Cards.get(player).put(alias, card);
        _gameState.labelCard(alias, card);
    }

    private void place(PhysicalCardImpl card, Seat seat, SetupZone zone, boolean bottom) {
        switch (zone) {
            case CHARACTERS -> {
                //A Ring-bearer is already on the table, with the Ring if there is one
                if (card.getZone() != Zone.FREE_CHARACTERS)
                    MoveCardToZone(card, Zone.FREE_CHARACTERS);
            }
            case SUPPORT -> MoveCardToZone(card, Zone.SUPPORT);
            case MINIONS -> MoveCardToZone(card, Zone.SHADOW_CHARACTERS);
            case HAND -> MoveCardToZone(card, Zone.HAND);
            case DISCARD -> MoveCardToZone(card, Zone.DISCARD);
            case DEAD_PILE -> MoveCardToZone(card, Zone.DEAD);
            case REMOVED -> MoveCardToZone(card, Zone.REMOVED);
            case DECK -> {
                if (bottom)
                    MoveCardsToBottomOfDeck(card);
                else
                    MoveCardsToTopOfDeck(card);
            }
        }
        if (card.getOwner() != null && !card.getOwner().equals(Player(seat)))
            _problems.add("'" + AliasOf(card) + "' was put in a " + seat.key() + " zone but belongs to the other seat");
    }

    /**
     * Carries out one of the scenario's direct changes to the board.  Only do this while the game is waiting on a
     * decision, and bear in mind that the decision it is waiting on was worked out before the change.
     */
    public void Apply(DoOp op) {
        try {
            switch (op.kind()) {
                case MOVE -> {
                    ScenarioParser.ZoneRef ref = ScenarioParser.parseZoneRef(_def, op.target(), "do");
                    place(Card(op.card()), ref.seat(), ref.zone(), ref.bottom());
                }
                case ATTACH -> AttachCardsTo(Card(op.target()), Card(op.card()));
                case STACK -> StackCardsOn(Card(op.target()), Card(op.card()));
                case WOUND -> AddWoundsToChar(Card(op.card()), op.amount());
                case HEAL -> RemoveWoundsFromChar(Card(op.card()), Math.min(op.amount(), GetWoundsOn(Card(op.card()))));
                case TOKENS -> AddTokensToCard(Card(op.card()), op.amount());
                case DRAW -> {
                    //(from the top of the deck, as far as the deck goes)
                    String player = Player(ScenarioParser.parseZoneRef(_def, op.target() + ".hand", "do").seat());
                    for (int i = 0; i < op.amount(); i++)
                        _gameState.playerDrawsCard(player);
                }
                case ORDER -> {
                    ScenarioParser.ZoneRef ref = ScenarioParser.parseZoneRef(_def, op.target(), "do");
                    List<? extends PhysicalCard> there = CardsIn(ref.seat(), ref.zone());
                    List<PhysicalCardImpl> ordered = new ArrayList<>();
                    for (String alias : op.card().split(",")) {
                        PhysicalCardImpl card = Card(alias.trim());
                        if (there.contains(card))
                            ordered.add(card);
                        else
                            _problems.add(op.source() + ": '" + alias.trim() + "' is not in " + op.target());
                    }
                    for (PhysicalCard card : List.copyOf(there))
                        if (!ordered.contains(card))
                            ordered.add((PhysicalCardImpl) card);
                    //A card is put in its place by being taken out and put back at the end (the bottom, of a deck).
                    // The cards that already come first, in this order, are left where they are: every card moved
                    // is a card the players watch move.
                    int settled = 0;
                    for (PhysicalCard card : there)
                        if (settled < ordered.size() && card == ordered.get(settled))
                            settled++;
                    for (PhysicalCardImpl card : ordered.subList(settled, ordered.size()))
                        place(card, ref.seat(), ref.zone(), true);
                }
                case MARKER -> putMarker(Seat.valueOf(op.target().toUpperCase(java.util.Locale.ROOT)), op.amount(), op.source());
                case CONTROL -> {
                    PhysicalCard site = siteOnPath(op.card() != null ? op.card() : String.valueOf(op.amount()), op.source());
                    if (site != null)
                        control(site, Seat.valueOf(op.target().toUpperCase(java.util.Locale.ROOT)), op.source());
                }
                case LIBERATE -> {
                    PhysicalCard site = siteOnPath(op.card() != null ? op.card() : String.valueOf(op.amount()), op.source());
                    if (site != null && site.getCardController() == null)
                        _problems.add(op.source() + ": site " + site.getSiteNumber() + " is controlled by nobody");
                    else if (site != null)
                        _gameState.loseControlOfCard(site, Zone.ADVENTURE_PATH);
                }
                case TWILIGHT -> SetTwilight(Math.max(0, op.relative() ? GetTwilight() + op.amount() : op.amount()));
                case BURDENS -> setTotal(GetBurdens(), op, this::AddBurdens, this::RemoveBurdens);
                case THREATS -> setTotal(GetThreats(), op, this::AddThreats, this::RemoveThreats);
                case MODIFY -> modify(Card(op.card()), op.modification(), _gameState.getCurrentPhase(), op.source());
                case UNMODIFY -> {
                    if (Unmodify(Card(op.card()), op.target()) == 0)
                        _problems.add(op.source() + ": '" + op.card() + "' has no " + (op.target() == null ? "" : op.target() + " ")
                                + "modifier put on it by hand to take off");
                }
            }
        } catch (ScenarioFormatException exp) {
            _problems.add(exp.getMessage());
        }
    }

    /* ------------------------------------------------------------------ modifiers put on by hand (see CardModifiers) */

    /** What this game has had put on its cards by hand; counted afresh in each game. */
    private final CardModifiers _modifiers = new CardModifiers();

    private void modify(PhysicalCard card, ScenarioDefinition.Modification modification, Phase phase, String context) {
        try {
            _modifiers.put(_game, card, modification, phase);
            //(the board being laid out is sent as the first turn begins)
            if (_arranged)
                _game.refreshGameStats();
        } catch (IllegalArgumentException exp) {
            _problems.add(context + ": '" + AliasOf(card) + "': " + exp.getMessage());
        }
    }

    /**
     * Puts a modifier on a card by hand, as do: modify(...) does: for the phase (the one the game is in), the turn, or
     * until it is taken off.  For a sandbox's edits as well as a script's.  Only while the game waits on a decision.
     * @return What was put on, with its number in this game (see CardModifiers.Put.id).
     * @throws IllegalArgumentException if it means nothing on that card (strength for a condition, say), with words
     * that say why.
     */
    public CardModifiers.Put Modify(PhysicalCard card, ScenarioDefinition.Modification modification) {
        CardModifiers.Put put = _modifiers.put(_game, card, modification, _gameState.getCurrentPhase());
        //(the numbers on the table are sent as the game goes, and a change made while it waits has to be sent itself)
        _game.refreshGameStats();
        return put;
    }

    /**
     * Takes one modifier put on by hand off again, by its number.
     * @return Whether there was one of that number still in force.
     */
    public boolean Unmodify(int id) {
        boolean taken = _modifiers.remove(_game, id);
        _game.refreshGameStats();
        return taken;
    }

    /**
     * Takes off a card's modifiers put on by hand: every one of a kind, or all of them.
     * @param what strength, fierce and so on (CardModifiers.WORDS); null for all of them.
     * @return How many were taken off.
     */
    public int Unmodify(PhysicalCard card, String what) {
        int taken = _modifiers.removeFrom(_game, card, what);
        _game.refreshGameStats();
        return taken;
    }

    /** A card's modifiers put on by hand that are still in force, in the order they were put on. */
    public List<CardModifiers.Put> ModifiersOn(PhysicalCard card) {
        return _modifiers.on(_game, card);
    }

    /**
     * The same as the page is told of them: [{id, what, amount, until, text, written}] (see CardModifiers.Put.json).
     */
    public List<Map<String, Object>> DescribeModifiers(PhysicalCard card) {
        List<Map<String, Object>> all = new ArrayList<>();
        for (CardModifiers.Put put : ModifiersOn(card))
            all.add(put.json());
        return all;
    }

    /**
     * @return The cards in a seat's hand or in one of its piles, in order: a hand as it is shown, a pile from the top
     * down.  Empty for anywhere that has no order.
     */
    public List<? extends PhysicalCard> CardsIn(Seat seat, SetupZone zone) {
        String player = Player(seat);
        return switch (zone) {
            case HAND -> _gameState.getHand(player);
            case DECK -> _gameState.getDeck(player);
            case DISCARD -> _gameState.getDiscard(player);
            case DEAD_PILE -> _gameState.getDeadPile(player);
            default -> List.of();
        };
    }

    private static void setTotal(int current, DoOp op, java.util.function.IntConsumer add, java.util.function.IntConsumer remove) {
        int wanted = Math.max(0, op.relative() ? current + op.amount() : op.amount());
        if (wanted > current)
            add.accept(wanted - current);
        else if (wanted < current)
            remove.accept(current - wanted);
    }

    /**
     * Checks card-definition requirements (the same ones cards use for "requires") against the game as it stands.
     * @param requirementsJson A JSON array of requirement objects; null or empty always holds.
     * @param player Who "you" refers to in the requirements.
     */
    public boolean Holds(String requirementsJson, String player) {
        if (requirementsJson == null)
            return true;
        Requirement[] requirements = _requirements.computeIfAbsent(requirementsJson, ScenarioTable::buildRequirements);
        ActionContext context = new DefaultActionContext(player, _game, _rulesCards.get(SeatOf(player)), null, null);
        for (Requirement requirement : requirements)
            if (!requirement.accepts(context))
                return false;
        return true;
    }

    /**
     * @throws IllegalArgumentException if the JSON does not describe requirements the card builder understands.
     */
    public static Requirement[] buildRequirements(String requirementsJson) {
        try {
            JSONArray array = (JSONArray) new JSONParser().parse(requirementsJson);
            JSONObject[] objects = new JSONObject[array.size()];
            for (int i = 0; i < objects.length; i++)
                objects[i] = (JSONObject) array.get(i);
            return BUILDER.getRequirementFactory().getRequirements(objects, BUILDER);
        } catch (InvalidCardDefinitionException | ParseException exp) {
            throw new IllegalArgumentException(exp.getMessage(), exp);
        }
    }

    /** Filters whose parameter is itself a filter: a card's name in the scenario is looked for inside them. */
    private static final java.util.Set<String> FILTER_HOLDERS = java.util.Set.of("not", "and", "or", "inskirmishagainst",
            "recentlyinskirmishagainst", "attachedto", "hasattached", "hasstacked", "stackedon", "assignedtoskirmishagainst",
            "notassignedtoskirmishagainst", "canexert");

    /**
     * Card filters as cards write them, with the scenario's names for its cards allowed in them: a word that is one
     * (aragorn), on its own or inside not(...), inSkirmishAgainst(...) and the like, becomes alias(aragorn).
     */
    static String cardFilter(ScenarioDefinition def, String text) throws ScenarioFormatException {
        List<String> parts = new ArrayList<>();
        for (String part : Terms.split(text, ',', "filter")) {
            int open = part.indexOf('(');
            if (open < 0) {
                parts.add(ScenarioParser.isAlias(def, part) ? "alias(" + part + ")" : part);
                continue;
            }
            String name = part.substring(0, open).trim();
            if (!FILTER_HOLDERS.contains(name.toLowerCase(java.util.Locale.ROOT)) || !part.endsWith(")")) {
                parts.add(part);
                continue;
            }
            parts.add(name + "(" + cardFilter(def, part.substring(open + 1, part.length() - 1)) + ")");
        }
        return String.join(",", parts);
    }

    private static final Map<String, com.gempukku.lotro.cards.build.FilterableSource> FILTERS = new java.util.concurrent.ConcurrentHashMap<>();

    private static com.gempukku.lotro.cards.build.FilterableSource filter(String filter) {
        return FILTERS.computeIfAbsent(filter, text -> {
            try {
                return BUILDER.getFilterFactory().generateFilter(text, BUILDER);
            } catch (InvalidCardDefinitionException | RuntimeException exp) {
                throw new IllegalArgumentException(exp.getMessage(), exp);
            }
        });
    }

    /** @throws IllegalArgumentException if the card builder does not understand the filter. */
    public static void checkFilter(String filter) {
        filter(filter);
    }

    /**
     * @return Those of the cards that a choose(...)'s pick fits, in the order they were given.
     * @param player Who "you" is in the pick's filters.
     */
    public List<PhysicalCard> Picked(ScenarioDefinition.Pick pick, String player, List<? extends PhysicalCard> among) {
        ActionContext context = new DefaultActionContext(player, _game, _rulesCards.get(SeatOf(player)), null, null);
        com.gempukku.lotro.common.Filterable filterable = filter(pick.asFilter()).getFilterable(context);
        List<PhysicalCard> fit = new ArrayList<>();
        for (PhysicalCard card : among)
            if (card != null && com.gempukku.lotro.filters.Filters.accepts(_game, card, filterable))
                fit.add(card);
        return fit;
    }

    /** The player a decision waits for; the human if it is neither's (any more). */
    public String PlayerOf(AwaitingDecision decision) {
        for (Seat seat : Seat.values())
            if (_userFeedback.getAwaitingDecision(Player(seat)) == decision)
                return Player(seat);
        return Player(_def.human);
    }

    /**
     * @throws IllegalArgumentException if the JSON does not describe effects the card builder understands.
     */
    public static void checkRules(String effectsJson) {
        try {
            JSONObject definition = new JSONObject();
            definition.put("title", "Scenario Rules");
            definition.put("type", "metasite");
            definition.put("effects", new JSONParser().parse(effectsJson));
            BUILDER.buildFromJson(RULES_BLUEPRINT_ID, definition);
        } catch (InvalidCardDefinitionException | ParseException exp) {
            throw new IllegalArgumentException(exp.getMessage(), exp);
        }
    }
}
