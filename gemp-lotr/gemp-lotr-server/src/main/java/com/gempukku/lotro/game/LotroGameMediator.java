package com.gempukku.lotro.game;

import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.game.state.GameExtraInfo;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.GameSeed;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.packs.PackOpener;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.bots.BotGameStateListener;
import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.AssignmentLegality;
import com.gempukku.lotro.bots.BotDecisionFallback;
import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.scenario.LiveScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LotroGameMediator {
    private static final Logger LOG = LogManager.getLogger(LotroGameMediator.class);

    private final Map<String, GameCommunicationChannel> _communicationChannels = Collections.synchronizedMap(new HashMap<>());
    // The game and its feedback are replaced when the game is taken back to an earlier decision (revertTo).  Only
    // ever written inside the write lock.
    private volatile DefaultUserFeedback _userFeedback;
    private volatile DefaultLotroGame _lotroGame;
    /**
     * Everything that steered this game (see GameJournal); what it is rebuilt from.  A scenario's game keeps its own
     * (ScenarioTable.Journal), and this is that one; null for a scenario's game that cannot be built again (one
     * with an author).
     */
    private GameJournal _journal;
    /** What each player has been shown and when, for saying what taking the game back would undo.  Null with no journal. */
    private InformationLedger _ledger;
    /** The places the game can be taken back to.  Null with no journal. */
    private RestorePoints _restorePoints;
    private volatile UndoPolicy _undoPolicy = UndoPolicy.CASUAL;
    /** What the people at the game are told apart from the game's own events (see GameNotices). */
    private final GameNotices _notices = new GameNotices();
    /** The players who have said they refuse every request to take this game back. */
    private final Set<String> _autoDecline = Collections.synchronizedSet(new HashSet<>());
    /** The request to take the game back that is waiting to be agreed to, if there is one.  The game stands still meanwhile. */
    private PendingUndo _pendingUndo;
    private int _nextUndoId = 1;
    /** How many of their own choices each player has taken back in the phase named by _ownUndoPhase. */
    private final Map<String, Integer> _ownUndos = new HashMap<>();
    /**
     * For each answer given (by how many entries the journal held then): how many events each recording of the game
     * had been sent.  When the game is taken back there, everything a recording was sent after that is undone play.
     */
    private final TreeMap<Integer, Map<GameStateListener, Integer>> _recordedAt = new TreeMap<>();
    /** The journal of the game as it stood each time it was taken back, with a line saying so, soonest first. */
    private final List<String> _journalsLeftBehind = new ArrayList<>();
    private final List<Revert> _reverts = new ArrayList<>();

    /** One taking back of the game, for its summary. */
    public record Revert(String requester, String tier, String label, int turn) { }
    private String _ownUndoPhase = "";

    private record PendingUndo(int id, String requester, RestorePoints.Point point, RevertAssessment assessment,
                               Set<String> waitingOn, long startedAt) { }

    /**
     * How a request to take the game back was met.
     * @param status "done" (the game was taken back), "pending" (the other players are being asked) or "refused".
     * @param message Words for the player who asked; null when there is nothing to add.
     * @param requestId The number of the request, when it is pending.
     */
    public record UndoReply(String status, String message, int requestId) { }

    /**
     * A place a player may ask for the game to be taken back to, with what that would undo for them.
     * @param tier GREEN, YELLOW or RED (see RevertAssessment.Tier).
     * @param unavailable Why it cannot be asked for as things stand; null if it can.
     */
    public record RestorePointView(int entryCount, String label, int turn, String phase, String tier, List<String> reasons, String unavailable) { }
    /** What the game is built from, kept so that it can be built again.  Null for a scenario's game. */
    private final LotroFormat _format;
    private final LotroCardBlueprintLibrary _library;
    private final String _tournamentName;
    private final GameExtraInfo _extraInfo;
    private final PackOpener _packOpener;
    /** Everybody listening to the game, in the order they began, so that a rebuilt game can be given the same listeners. */
    private final Map<GameStateListener, String> _gameStateListeners = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Set<GameResultListener> _gameResultListeners = Collections.synchronizedSet(new LinkedHashSet<>());
    private BotGameStateListener _botListener;
    /** The auto-pass settings each player's game is running with, as last applied. */
    private final Map<String, Set<Phase>> _autoPass = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Integer> _playerClocks = new HashMap<>();
    private final Map<String, Long> _decisionQuerySentTimes = new HashMap<>();
    private final Set<String> _playersPlaying = new HashSet<>();
    private final Map<String, LotroDeck> _playerDecks = new HashMap<>();

    private final String _gameId;

    private GameTimer _timeSettings;
    private final boolean _allowSpectators;
    private final boolean _cancellable;
    private final boolean _showInGameHall;

    private final boolean _soloGame;
    private final BotPlayer _botPlayer;
    private final LotroDeck _botDeck;
    /**
     * Set if this is a scenario's game: the scenario built the game, plays the other seat and decides when it ends.
     * Replaced, with the game, when the game is taken back (revertTo).
     */
    private volatile LiveScenario _scenario;

    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
    private final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();
    private int _channelNextIndex = 0;
    private volatile boolean _destroyed;

    private int botDecisions = 0;

    // Any single decision that holds the game's write lock longer than this is logged with
    // enough context (game, player, decision, answer, phase) to find it in the replay.
    private static final long SLOW_ACTION_THRESHOLD_MS = 1000;

    public LotroGameMediator(String gameId, LotroFormat lotroFormat, LotroGameParticipant[] participants, LotroCardBlueprintLibrary library,
                             GameTimer gameTimer, boolean allowSpectators, boolean cancellable, boolean showInGameHall,
                             String tournamentName, MarkdownParser parser, BotService botService, boolean isSolo,
                             GameExtraInfo extraInfo, PackOpener packOpener) {
        _gameId = gameId;
        _timeSettings = gameTimer;
        _allowSpectators = allowSpectators;
        _cancellable = cancellable;
        this._showInGameHall = showInGameHall;
        _soloGame = isSolo;
        _scenario = null;
        _format = lotroFormat;
        _library = library;
        _tournamentName = tournamentName;
        _extraInfo = extraInfo;
        _packOpener = packOpener;
        if (participants.length < 1)
            throw new IllegalArgumentException("Game can't have less than one participant");

        for (LotroGameParticipant participant : participants) {
            String participantId = participant.getPlayerId();
            var deck = participant.getDeck();
            deck.setNotes(parser.renderMarkdown(deck.getNotes(), true));
            _playerDecks.put(participantId, deck);
            _playerClocks.put(participantId, 0);
            _playersPlaying.add(participantId);
        }

        if (_soloGame) {
            if (participants.length == 1) {
                BotService.BotWithDeck bot = botService.getBotParticipant(lotroFormat);
                _botPlayer = bot.getBotPlayer();
                _botDeck = bot.getLotroDeck();
                _playerDecks.put(_botPlayer.getName(), _botDeck);
                _playerClocks.put(_botPlayer.getName(), 0);
                _playersPlaying.add(_botPlayer.getName());
            } else {
                BotPlayer tmpBot = null;
                LotroDeck tmpDeck = null;
                for (LotroGameParticipant participant : participants) {
                    String participantId = participant.getPlayerId();
                    var deck = participant.getDeck();
                    if (participantId.equals(BotService.GENERAL_BOT_NAME)) {
                        BotService.BotWithDeck bot = botService.getBotForDeck(deck);
                        tmpBot = bot.getBotPlayer();
                        tmpDeck = bot.getLotroDeck();
                    }
                }
                _botPlayer = tmpBot;
                _botDeck = tmpDeck;
            }
        } else {
            _botPlayer = null;
            _botDeck = null;
        }

        Built built = build(null);
        _userFeedback = built.feedback;
        _lotroGame = built.game;
        _journal = built.journal;
        _ledger = built.ledger;
        _restorePoints = built.restorePoints;
        _undoPolicy = _soloGame ? UndoPolicy.SOLO : UndoPolicy.CASUAL;
        attachBot();
        listenForTheEnd();
    }

    private record Built(DefaultUserFeedback feedback, DefaultLotroGame game, GameJournal journal, InformationLedger ledger,
                         RestorePoints restorePoints) { }

    /**
     * However a game ends (won, conceded, timed out, called off), everybody is told to fetch the table: somebody who
     * is looking at a decision would otherwise not hear of it until they answered.
     */
    private void listenForTheEnd() {
        for (String player : _playersPlaying)
            _notices.know(player);
        addGameResultListener(new GameResultListener() {
            @Override
            public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                _pendingUndo = null;
                _notices.sendAll(GameNotices.RESYNC, Map.of("reason", "ended"));
            }

            @Override
            public void gameCancelled() {
                _pendingUndo = null;
                _notices.sendAll(GameNotices.RESYNC, Map.of("reason", "ended"));
            }
        });
    }

    /**
     * Builds this mediator's game from what it was made of, with a journal kept of it from the first moment.
     * @param toRepeat The journal of the game it is to be a repeat of (its random outcomes come out the same for as
     *                 long as that journal lasts); null for a game of its own.
     */
    private Built build(GameJournal toRepeat) {
        DefaultUserFeedback feedback = new DefaultUserFeedback();
        GameJournal journal = new GameJournal();
        feedback.addDecisionObserver(journal);
        final InformationLedger ledger = new InformationLedger(_playerDecks.keySet(), _format, journal);
        feedback.addDecisionObserver(ledger);
        RestorePoints restorePoints = new RestorePoints(journal);
        feedback.addDecisionObserver(restorePoints);
        feedback.addDecisionObserver(recordingsCounted(feedback, journal));
        //The same map of decks every time: the game seats its players in the order the map gives them
        DefaultLotroGame game = new DefaultLotroGame(_format, _playerDecks, feedback, _library, _timeSettings.toString(),
                _allowSpectators, _tournamentName, _extraInfo);
        if (_packOpener != null) {
            //What comes out of a pack is chance that no journal holds
            game.setPackOpener(new PackOpener() {
                @Override
                public List<String> getAvailablePackIds() {
                    return _packOpener.getAvailablePackIds();
                }

                @Override
                public List<String> openPack(String packId) {
                    ledger.barrier("a booster pack was opened");
                    return _packOpener.openPack(packId);
                }
            });
        }
        feedback.setGame(game);
        ledger.setGame(game);
        restorePoints.setGame(game);
        if (toRepeat != null)
            journal.replayRandomFrom(toRepeat);
        //(GameSeed: the game's own draws, which a test server started with a seed makes the same for every game)
        game.getGameState().setRandom(journal.random(ledger.watching(GameSeed.newGameRandom())));
        game.addGameStateListener(InformationLedger.LISTENER_NAME, ledger);
        return new Built(feedback, game, journal, ledger, restorePoints);
    }

    /**
     * @return Something to observe a game's decisions that notes, each time an answer is about to be given, how many
     * events each recording of the game has been sent so far (_recordedAt).
     */
    private DecisionObserver recordingsCounted(DefaultUserFeedback feedback, GameJournal journal) {
        return new DecisionObserver() {
            @Override
            public void decisionIssued(String playerId, AwaitingDecision decision) { }

            @Override
            public void decisionAnswering(String playerId, AwaitingDecision decision) {
                //Only of the game being played: a game being rebuilt has no recordings listening to it yet
                if (feedback != _userFeedback)
                    return;
                Map<GameStateListener, Integer> sent = new HashMap<>();
                for (GameStateListener listener : new ArrayList<>(_gameStateListeners.keySet()))
                    if (listener instanceof GameCommunicationChannel recording && !recording.isLiveConnection())
                        sent.put(recording, recording.eventCount());
                if (!sent.isEmpty())
                    _recordedAt.put(journal.entries().size(), sent);
            }

            @Override
            public void decisionAnswered(String playerId, AwaitingDecision decision, String answer) { }
        };
    }

    /**
     * Has a scenario's game, which the scenario built and keeps the journal of, watched the way build has an
     * ordinary game watched: what each player is shown, and the places the game can be taken back to.  To be called
     * before the scenario is begun.
     */
    private Built watch(LiveScenario scenario) {
        DefaultUserFeedback feedback = scenario.Table().userFeedback();
        DefaultLotroGame game = scenario.Table().game();
        GameJournal journal = scenario.Table().Journal();
        InformationLedger ledger = new InformationLedger(scenario.Table().Decks().keySet(), game.getFormat(), journal);
        feedback.addDecisionObserver(ledger);
        RestorePoints restorePoints = new RestorePoints(journal);
        feedback.addDecisionObserver(restorePoints);
        feedback.addDecisionObserver(recordingsCounted(feedback, journal));
        ledger.setGame(game);
        restorePoints.setGame(game);
        scenario.Table().WatchRandom(ledger::watching);
        game.addGameStateListener(InformationLedger.LISTENER_NAME, ledger);
        //A sandbox's Stops, edits and Starts are places to go back to as well
        if (scenario.Sandbox() != null)
            scenario.Sandbox().OnPoint((entryCount, label) -> restorePoints.note(entryCount, scenario.HumanPlayer(), label));
        return new Built(feedback, game, journal, ledger, restorePoints);
    }

    /** The bot hears the game through a listener that knows the game's rules, so each game gets one of its own. */
    private void attachBot() {
        if (_botPlayer == null)
            return;
        _botListener = new BotGameStateListener(_botPlayer, this, new AssignmentLegality(_lotroGame));
        _lotroGame.addGameStateListener(_botPlayer.getName(), _botListener);
    }

    /**
     * A scenario's game.  The scenario has already built the game, with the board and the decks its file describes;
     * the mediator takes it over from there and lets the scenario answer for the seat nobody is sitting in.
     */
    public LotroGameMediator(String gameId, LiveScenario scenario, GameTimer gameTimer, boolean allowSpectators) {
        _gameId = gameId;
        _timeSettings = gameTimer;
        _allowSpectators = allowSpectators;
        _cancellable = false;
        _showInGameHall = false;
        _soloGame = true;
        _botPlayer = null;
        _botDeck = null;
        _scenario = scenario;
        _undoPolicy = UndoPolicy.OFF;
        _format = null;
        _library = null;
        _tournamentName = null;
        _extraInfo = null;
        _packOpener = null;

        for (Map.Entry<String, LotroDeck> deck : scenario.Table().Decks().entrySet()) {
            _playerDecks.put(deck.getKey(), deck.getValue());
            _playerClocks.put(deck.getKey(), 0);
            _playersPlaying.add(deck.getKey());
        }

        _userFeedback = scenario.Table().userFeedback();
        _lotroGame = scenario.Table().game();
        //The person may take back anything of their own, as in a game against a bot: there is nobody to ask
        if (scenario.CanBeBuiltAgain()) {
            Built watched = watch(scenario);
            _journal = watched.journal;
            _ledger = watched.ledger;
            _restorePoints = watched.restorePoints;
            //(unless the scenario says the person may not: options.undo, off by default in a puzzle)
            _undoPolicy = scenario.Definition().undoAllowed() ? UndoPolicy.SOLO : UndoPolicy.OFF;
        }
        scenario.SetHost(this::scenarioOpponentAnswered);
        listenForTheEnd();
    }

    public LiveScenario getScenario() {
        return _scenario;
    }

    /**
     * For a scenario's game that is not going to be used after all: stops it doing anything further.  Nobody has
     * been told about the game, so there is nobody to tell that it is over.
     */
    public void discardScenario() {
        _writeLock.lock();
        try {
            //The game itself is left as it is.  It will never be asked for anything again, and ending it would
            // tell listeners that a game with this id had finished.
            if (_scenario != null)
                _scenario.Discard();
        } finally {
            _writeLock.unlock();
        }
    }

    /**
     * @return For a scenario's game, the journal that brings a new game of the scenario to the last checkpoint this
     * one reached; null if it has reached none (or this is not a scenario's game).
     */
    public GameJournal checkpointJournal() {
        if (_scenario == null)
            return null;
        _readLock.lock();
        try {
            return _scenario.CheckpointJournal();
        } finally {
            _readLock.unlock();
        }
    }

    public boolean isVisibleToUser(String username) {
        return !_showInGameHall || _playersPlaying.contains(username);
    }

    public boolean isDestroyed() {
        return _destroyed;
    }

    public void destroy() {
        _destroyed = true;
    }

    public String getGameId() {
        return _gameId;
    }

    public DefaultLotroGame getGame() { return _lotroGame; }

    public boolean isAllowSpectators() {
        return _allowSpectators;
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        //A scenario says where its player is asked to pass; its script depends on it
        if (_scenario != null)
            return;
        if (!_playersPlaying.contains(playerId))
            return;
        //Sent with every request a client makes, and nearly always the same as last time
        if (phases.equals(_autoPass.get(playerId)))
            return;
        _writeLock.lock();
        try {
            applyAutoPass(playerId, phases);
        } finally {
            _writeLock.unlock();
        }
    }

    private static final String AUTO_PASS_NOTE = "autopass";

    /**
     * Which phases a player is passed through decides which decisions the game asks, so a change is written in the
     * journal where it was made and made again at the same point when the journal is followed.
     */
    private void applyAutoPass(String playerId, Set<Phase> phases) {
        if (phases.equals(_autoPass.get(playerId)))
            return;
        _autoPass.put(playerId, EnumSet.copyOf(phases.isEmpty() ? EnumSet.noneOf(Phase.class) : phases));
        _lotroGame.setPlayerAutoPassSettings(playerId, phases);
        if (_journal != null) {
            List<String> names = new ArrayList<>();
            for (Phase phase : _autoPass.get(playerId))
                names.add(phase.name());
            _journal.note(playerId, AUTO_PASS_NOTE, String.join(",", names));
        }
    }

    private static Set<Phase> autoPassOf(String names) {
        Set<Phase> phases = EnumSet.noneOf(Phase.class);
        if (!names.isEmpty())
            for (String name : names.split(","))
                phases.add(Phase.valueOf(name));
        return phases;
    }

    public void sendMessageToPlayers(String message) {
        _lotroGame.getGameState().sendMessage(message);
    }

    public void addGameStateListener(String playerId, GameStateListener listener) {
        _gameStateListeners.put(listener, playerId);
        _lotroGame.addGameStateListener(playerId, listener);
    }

    public void removeGameStateListener(GameStateListener listener) {
        _gameStateListeners.remove(listener);
        _lotroGame.removeGameStateListener(listener);
    }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
        _lotroGame.addGameResultListener(listener);
    }

    public void removeGameResultListener(GameResultListener listener) {
        _gameResultListeners.remove(listener);
        _lotroGame.removeGameResultListener(listener);
    }

    /**
     * @return How many entries the game's journal holds now.  Taking the game back to this many (revertTo) is taking
     * it back to this moment.  0 for a game that cannot be taken back (a scenario's with an author).
     */
    public int journalSize() {
        _readLock.lock();
        try {
            return _journal == null ? 0 : _journal.entries().size();
        } finally {
            _readLock.unlock();
        }
    }

    /**
     * Takes the game back to the moment its journal held this many entries; cut just before an answer, that is the
     * moment the answer's decision was waiting.
     *
     * The engine cannot be wound back, so a second game is built beside this one and given the journal up to that
     * point (a scenario's game by its scenario, LiveScenario.Again, which lays its board out and follows the journal
     * itself).  Only if it took every answer as the first game did, and has come to rest on the same decision, does
     * it take the first game's place: everybody listening to the old game is moved over to the new one and sent the
     * table as it now stands.  Otherwise the new game is thrown away and the game in progress is not touched.
     *
     * The players' clocks are not taken back: whatever time has been spent stays spent.
     *
     * @return Null if the game was taken back; otherwise why it could not be.
     */
    public String revertTo(int entryCount) {
        return revertTo(entryCount, "The game was reverted to an earlier point", "", "", "");
    }

    private String revertTo(int entryCount, String announcement, String requester, String tier, String label) {
        _writeLock.lock();
        try {
            if (_journal == null || (_scenario != null && !_scenario.CanBeBuiltAgain()))
                return "A scenario's game with an author is not taken back this way";
            if (_lotroGame.isFinished())
                return "The game is over";
            List<GameJournal.Entry> entries = _journal.entries();
            if (entryCount < 0 || entryCount > entries.size())
                return "The game's journal has no entry " + entryCount;
            String barrier = _ledger.barrierSince(entryCount);
            if (barrier != null)
                return "Cannot go back that far: " + barrier;
            GameJournal prefix = _journal.upTo(entryCount);

            final LiveScenario again = _scenario == null ? null : _scenario.Again(prefix);
            final Built built = again == null ? build(prefix) : watch(again);
            String problem;
            if (again != null) {
                try {
                    //Whatever the scripted opponent is given to do at once waits for the lock this thread holds, and
                    // is dropped if the game it was for is not the one being played by then
                    again.SetHost(this::scenarioOpponentAnswered);
                    again.Begin();
                    problem = again.Table().FollowProblem();
                } catch (RuntimeException exp) {
                    LOG.error("Scenario game " + _gameId + " could not be rebuilt from its journal", exp);
                    problem = "the rebuilt game broke: " + exp;
                }
            } else try {
                built.game.startGame();
                JournalFollower follower = new JournalFollower().follow(prefix, new JournalFollower.Table() {
                    @Override
                    public String playerOf(String key) {
                        return key;
                    }

                    @Override
                    public AwaitingDecision awaiting(String player) {
                        return built.feedback.getAwaitingDecision(player);
                    }

                    @Override
                    public void answer(String player, AwaitingDecision decision, String answer) {
                        built.feedback.participantDecided(player);
                        try {
                            decision.decisionMade(answer);
                        } catch (DecisionResultInvalidException exp) {
                            throw new RuntimeException(exp.getWarningMessage());
                        }
                        built.feedback.decisionAccepted(player, decision, answer);
                        built.game.carryOutPendingActionsUntilDecisionNeeded();
                    }

                    @Override
                    public boolean note(GameJournal.Entry note) {
                        if (!note.detail().equals(AUTO_PASS_NOTE))
                            return false;
                        built.game.setPlayerAutoPassSettings(note.player(), autoPassOf(note.value()));
                        //The new game's journal has to hold it too, at the same place
                        built.journal.note(note.player(), AUTO_PASS_NOTE, note.value());
                        return true;
                    }
                });
                problem = follower.problem();
            } catch (RuntimeException exp) {
                LOG.error("Game " + _gameId + " could not be rebuilt from its journal", exp);
                problem = "the rebuilt game broke: " + exp;
            }
            if (problem == null)
                problem = built.journal.divergence();
            if (problem == null && built.game.isFinished())
                problem = "the rebuilt game is over";
            if (problem == null && entryCount < entries.size() && entries.get(entryCount).kind() == GameJournal.Kind.ANSWER) {
                GameJournal.Entry next = entries.get(entryCount);
                //(a scenario's journal writes its players down by seat)
                String asked = next.player();
                for (String player : _playersPlaying)
                    if (_journal.keyOf(player).equals(next.player()))
                        asked = player;
                if (!GameJournal.matches(next, built.feedback.getAwaitingDecision(asked)))
                    problem = "the rebuilt game is not waiting on the decision " + asked + " was asked at that point";
            }
            if (problem != null) {
                if (again != null)
                    again.Discard();
                LOG.warn("Game " + _gameId + " was not taken back to journal entry " + entryCount + " of " + entries.size() + ": " + problem);
                return problem;
            }

            //Whatever anybody has spent on the decision they were looking at is theirs to keep
            for (String player : new ArrayList<>(_decisionQuerySentTimes.keySet()))
                addTimeSpentOnDecisionToUserClock(player);

            //The old game is left exactly as it is, with nobody listening to it: it must not tell anybody anything more
            DefaultLotroGame old = _lotroGame;
            _journalsLeftBehind.add("# " + announcement.replace('\n', ' ') + " (entry " + entryCount + " of " + entries.size() + ")\n" + _journal.write());
            _reverts.add(new Revert(requester, tier, label, old.getGameState().getTurnNumber()));
            for (GameResultListener listener : new ArrayList<>(_gameResultListeners))
                old.removeGameResultListener(listener);
            for (GameStateListener listener : new ArrayList<>(_gameStateListeners.keySet()))
                old.removeGameStateListener(listener);
            if (_botListener != null)
                old.removeGameStateListener(_botListener);

            _userFeedback = built.feedback;
            _lotroGame = built.game;
            _journal = built.journal;
            _ledger = built.ledger;
            _restorePoints = built.restorePoints;
            if (again != null) {
                //Whoever was waiting to hear how the scenario ends has been handed on to the new one (Again)
                LiveScenario was = _scenario;
                _scenario = again;
                was.Discard();
            }

            //The players' settings as they are now, which may not be as they were back then
            Map<String, Set<Phase>> wanted = new HashMap<>(_autoPass);
            _autoPass.clear();
            for (GameJournal.Entry entry : prefix.entries())
                if (entry.kind() == GameJournal.Kind.NOTE && entry.detail().equals(AUTO_PASS_NOTE))
                    _autoPass.put(entry.player(), autoPassOf(entry.value()));
            for (Map.Entry<String, Set<Phase>> setting : wanted.entrySet())
                applyAutoPass(setting.getKey(), setting.getValue());

            for (GameResultListener listener : new ArrayList<>(_gameResultListeners))
                _lotroGame.addGameResultListener(listener);
            //Each is sent the whole table, as anybody who joins a game is.  Somebody watching from a browser is given
            // a new channel with a new number to do it on: an answer on its way from that browser, sent to a decision
            // of the game as it was, then finds its channel gone and is refused.  (Decisions cannot be told apart by
            // their own numbers; most are numbered 1.)
            Map<String, Integer> newChannels = new HashMap<>();
            //For a recording, which keeps everything it was ever sent: where in it the undone play begins
            Map<GameStateListener, Integer> skipFrom = new HashMap<>();
            Map<GameStateListener, Integer> recordedThen = _recordedAt.get(entryCount);
            for (Map.Entry<GameStateListener, String> listener : new ArrayList<>(_gameStateListeners.entrySet())) {
                String who = listener.getValue();
                if (listener.getKey() instanceof GameCommunicationChannel channel && _communicationChannels.get(who) == channel) {
                    GameCommunicationChannel fresh = new GameCommunicationChannel(who, _channelNextIndex++, true, _lotroGame.getFormat());
                    _gameStateListeners.remove(channel);
                    _gameStateListeners.put(fresh, who);
                    _communicationChannels.put(who, fresh);
                    _lotroGame.addGameStateListener(who, fresh);
                    newChannels.put(who, fresh.getChannelNumber());
                    skipFrom.put(fresh, 0);
                } else {
                    if (listener.getKey() instanceof GameCommunicationChannel recording) {
                        Integer then = recordedThen == null ? null : recordedThen.get(recording);
                        //Not a place an answer was given at: at least the table being sent again counts for nothing
                        skipFrom.put(recording, then != null ? then : recording.eventCount());
                    }
                    _lotroGame.addGameStateListener(who, listener.getKey());
                }
            }
            _recordedAt.tailMap(entryCount, false).clear();
            _lotroGame.getGameState().sendMessage(announcement);
            for (GameStateListener listener : new ArrayList<>(_gameStateListeners.keySet()))
                listener.gameReverted(requester, tier, announcement, skipFrom.getOrDefault(listener, 0));
            attachBot();
            for (String person : newChannels.keySet())
                _notices.know(person);
            for (String person : _notices.people()) {
                Map<String, String> fields = new LinkedHashMap<>();
                fields.put("reason", "revert");
                fields.put("message", announcement);
                fields.put("tier", tier);
                if (newChannels.containsKey(person))
                    fields.put("channelNumber", String.valueOf(newChannels.get(person)));
                _notices.send(person, GameNotices.RESYNC, fields);
            }

            startClocksForUsersPendingDecision();
            return null;
        } finally {
            _writeLock.unlock();
        }
    }

    /**
     * @return What taking the game back to the moment its journal held this many entries would undo, for the player
     * who wants it: nothing of anybody else's (green), other players' choices or a shuffle (yellow), or something
     * that player has seen since (red).  Null for a game that cannot be taken back.
     */
    public RevertAssessment assessRevert(String requester, int entryCount) {
        _readLock.lock();
        try {
            return _ledger == null ? null : _ledger.assess(entryCount, requester);
        } finally {
            _readLock.unlock();
        }
    }

    /**
     * @return Everything that steered this game, for keeping beside its replay: the journal of the game as it stood
     * each time it was taken back, each under a line beginning "# " that says who took it back and how far, and
     * last, under "# The game as it ended", the journal of the game as it was finally played.  Each is a whole
     * journal (GameJournal.read), so any of the games it was at some time can be built again from the same decks.
     * Null for a game that cannot be taken back.
     */
    public String getAuditJournal() {
        _readLock.lock();
        try {
            if (_journal == null)
                return null;
            StringBuilder sb = new StringBuilder();
            for (String left : _journalsLeftBehind)
                sb.append(left);
            sb.append("# The game as it ended\n").append(_journal.write());
            return sb.toString();
        } finally {
            _readLock.unlock();
        }
    }

    /** Each time the game was taken back, soonest first. */
    public List<Revert> getReverts() {
        _readLock.lock();
        try {
            return new ArrayList<>(_reverts);
        } finally {
            _readLock.unlock();
        }
    }

    public UndoPolicy getUndoPolicy() {
        return _undoPolicy;
    }

    /** What taking back is allowed at this table.  A game that cannot be built again has none, whatever is set. */
    public void setUndoPolicy(UndoPolicy policy) {
        if (_journal != null)
            _undoPolicy = policy;
    }

    public GameNotices getNotices() {
        return _notices;
    }

    public boolean isAutoDeclining(String player) {
        return _autoDecline.contains(player);
    }

    /**
     * Has a player refuse every request to take this game back from now on (or stop doing so).  A request that is
     * waiting on them is refused at once.
     */
    public void setAutoDecline(String player, boolean on) {
        _writeLock.lock();
        try {
            if (!_playersPlaying.contains(player))
                return;
            if (on)
                _autoDecline.add(player);
            else
                _autoDecline.remove(player);
            if (on && _pendingUndo != null && _pendingUndo.waitingOn().contains(player))
                settlePendingUndo("declined", player);
        } finally {
            _writeLock.unlock();
        }
    }

    private List<String> othersThan(String player) {
        List<String> others = new ArrayList<>(_playersPlaying);
        others.remove(player);
        return others;
    }

    private boolean isBot(String player) {
        return _botPlayer != null && _botPlayer.getName().equals(player);
    }

    private String phaseNow() {
        return _lotroGame.getGameState().getTurnNumber() + ":" + _lotroGame.getGameState().getCurrentPhase();
    }

    /**
     * @return Why this player cannot ask for that as things stand; null if they can.
     */
    private String unavailable(String requester, RevertAssessment assessment) {
        if (!assessment.possible())
            return "Cannot go back that far: " + assessment.barrier();
        if (!_undoPolicy.allows(assessment.tier()))
            return assessment.tier() == RevertAssessment.Tier.GREEN ? "Undo is off in this game" : "Only Undo is allowed in this game";
        if (assessment.tier() == RevertAssessment.Tier.GREEN) {
            int taken = phaseNow().equals(_ownUndoPhase) ? _ownUndos.getOrDefault(requester, 0) : 0;
            if (_undoPolicy.greenPerPhase() > 0 && taken >= _undoPolicy.greenPerPhase())
                return "Undo limit reached this phase (" + _undoPolicy.greenPerPhase() + ")";
            return null;
        }
        if (!_undoPolicy.othersAgreeAtOnce())
            for (String other : othersThan(requester))
                if (!isBot(other) && _autoDecline.contains(other))
                    return other + " is auto-declining reverts";
        return null;
    }

    /**
     * @return The places this player may ask to have the game taken back to as things stand, the latest first, each
     * with what going back to it would undo for them.  Empty for anybody who is not playing, and for a game that cannot be taken back.
     */
    public List<RestorePointView> getRestorePoints(String player) {
        _readLock.lock();
        try {
            List<RestorePointView> result = new ArrayList<>();
            if (_restorePoints == null || !_playersPlaying.contains(player) || _lotroGame.isFinished())
                return result;
            for (RestorePoints.Point point : _restorePoints.offered(player, _undoPolicy)) {
                RevertAssessment assessment = _ledger.assess(point.entryCount(), player);
                result.add(new RestorePointView(point.entryCount(), point.label(), point.turn(),
                        point.phase() == null ? "" : point.phase().getHumanReadable(), assessment.tier().name(),
                        assessment.reasons(), unavailable(player, assessment)));
            }
            return result;
        } finally {
            _readLock.unlock();
        }
    }

    /**
     * @return The request that is waiting to be agreed to, for the people it concerns to be shown: its number, who
     * asked, what for, its tier and reasons, who has yet to answer and how many seconds are left.  Null if none is.
     */
    public Map<String, Object> getPendingUndo() {
        _readLock.lock();
        try {
            PendingUndo pending = _pendingUndo;
            if (pending == null)
                return null;
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("requestId", pending.id());
            result.put("requester", pending.requester());
            result.put("label", pending.point().publicLabel());
            result.put("turn", pending.point().turn());
            result.put("phase", pending.point().phase() == null ? "" : pending.point().phase().getHumanReadable());
            result.put("tier", pending.assessment().tier().name());
            result.put("reasons", pending.assessment().reasons());
            result.put("waitingOn", new ArrayList<>(pending.waitingOn()));
            result.put("secondsLeft", Math.max(0, _undoPolicy.timeoutSeconds() - (int) ((System.currentTimeMillis() - pending.startedAt()) / 1000)));
            return result;
        } finally {
            _readLock.unlock();
        }
    }

    /**
     * A player asks for the game to be taken back.
     *
     * If that undoes nothing but the player's own choices it is done at once.  Otherwise every other player is
     * asked, and the game stands still until they have all agreed, one of them has refused, the player has
     * withdrawn the request, or the time allowed has run out.  The wait is charged to the clock of the player who
     * asked.
     *
     * @param entryCount Which of the player's restore points (its entryCount); null for the latest, their last answer.
     */
    public UndoReply requestUndo(String requester, Integer entryCount) {
        return requestUndo(requester, entryCount, false);
    }

    /**
     * @param ownOnly Whether this is the Undo button's ask, which undoes a choice of the player's own and never asks
     *                anybody: refused if the place has stopped being green since the button was drawn (the other
     *                player has just answered, say), where requestUndo would go on to ask.
     */
    public UndoReply requestUndo(String requester, Integer entryCount, boolean ownOnly) {
        _writeLock.lock();
        try {
            if (!_playersPlaying.contains(requester))
                return new UndoReply("refused", "Only players can ask", 0);
            if (_restorePoints == null)
                return new UndoReply("refused", "This game cannot be reverted", 0);
            if (_lotroGame.isFinished())
                return new UndoReply("refused", "The game is over", 0);
            if (_pendingUndo != null)
                return new UndoReply("refused", "A revert request is already pending", 0);

            RestorePoints.Point point = null;
            for (RestorePoints.Point offered : _restorePoints.offered(requester, _undoPolicy))
                if (point == null && (entryCount == null || offered.entryCount() == entryCount))
                    point = offered;
            if (point == null)
                return new UndoReply("refused", entryCount == null ? "Nothing to undo" : "No longer available", 0);

            RevertAssessment assessment = _ledger.assess(point.entryCount(), requester);
            String unavailable = unavailable(requester, assessment);
            if (unavailable != null)
                return new UndoReply("refused", unavailable, 0);
            if (ownOnly && assessment.tier() != RevertAssessment.Tier.GREEN)
                return new UndoReply("refused", "No longer yours to undo", 0);

            Set<String> toAsk = new LinkedHashSet<>();
            if (assessment.tier() != RevertAssessment.Tier.GREEN && !_undoPolicy.othersAgreeAtOnce())
                for (String other : othersThan(requester))
                    if (!isBot(other))
                        toAsk.add(other);
            if (toAsk.isEmpty())
                return carryOutUndo(requester, point, assessment, "");

            //The game stands still while they make up their minds, and so do the clocks of whoever it was waiting on
            for (String player : new ArrayList<>(_decisionQuerySentTimes.keySet()))
                addTimeSpentOnDecisionToUserClock(player);
            PendingUndo pending = new PendingUndo(_nextUndoId++, requester, point, assessment, toAsk, System.currentTimeMillis());
            _pendingUndo = pending;
            _lotroGame.getGameState().sendMessage(requester + " asks to revert: " + point.publicLabel()
                    + " (" + String.join("; ", assessment.reasons()) + ")");
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("requestId", String.valueOf(pending.id()));
            fields.put("requester", requester);
            fields.put("label", point.publicLabel());
            fields.put("turn", String.valueOf(point.turn()));
            fields.put("phase", point.phase() == null ? "" : point.phase().getHumanReadable());
            fields.put("tier", assessment.tier().name());
            fields.put("reasons", String.join("; ", assessment.reasons()));
            fields.put("seconds", String.valueOf(_undoPolicy.timeoutSeconds()));
            _notices.sendAll(GameNotices.UNDO_REQUESTED, fields);
            return new UndoReply("pending", null, pending.id());
        } finally {
            _writeLock.unlock();
        }
    }

    /**
     * Somebody answers the request that is waiting.
     * @param reply "approve", "decline", "declineAll" (decline, and every later request in this game too) from a
     *              player who is being asked; "cancel" from the player who asked.
     */
    public UndoReply replyUndo(String player, int requestId, String reply) {
        _writeLock.lock();
        try {
            PendingUndo pending = _pendingUndo;
            if (pending == null || pending.id() != requestId)
                return new UndoReply("refused", "That request is no longer pending", 0);
            if (reply.equals("cancel")) {
                if (!player.equals(pending.requester()))
                    return new UndoReply("refused", "Only the asker can withdraw", 0);
                return settlePendingUndo("cancelled", player);
            }
            if (!pending.waitingOn().contains(player))
                return new UndoReply("refused", "The request is not waiting on you", 0);
            switch (reply) {
                case "approve" -> {
                    pending.waitingOn().remove(player);
                    if (!pending.waitingOn().isEmpty())
                        return new UndoReply("pending", null, pending.id());
                    return settlePendingUndo("approved", player);
                }
                case "declineAll" -> {
                    _autoDecline.add(player);
                    return settlePendingUndo("declined", player);
                }
                case "decline" -> {
                    return settlePendingUndo("declined", player);
                }
                default -> {
                    return new UndoReply("refused", "'" + reply + "' is not an answer to a request", 0);
                }
            }
        } finally {
            _writeLock.unlock();
        }
    }

    /**
     * The waiting request is over, one way or another.  Whoever asked pays for the wait.
     * @param outcome "approved", "declined", "cancelled" or "timedOut".
     * @param by Who settled it; null if nobody did (the time ran out).
     */
    private UndoReply settlePendingUndo(String outcome, String by) {
        PendingUndo pending = _pendingUndo;
        _pendingUndo = null;
        int waited = (int) ((System.currentTimeMillis() - pending.startedAt()) / 1000);
        _playerClocks.put(pending.requester(), _playerClocks.get(pending.requester()) + waited);

        if (outcome.equals("approved"))
            return carryOutUndo(pending.requester(), pending.point(), pending.assessment(), String.valueOf(pending.id()));

        String said = switch (outcome) {
            case "declined" -> by + " declined the revert";
            case "cancelled" -> pending.requester() + " withdrew the revert request";
            default -> "Revert request timed out";
        };
        _lotroGame.getGameState().sendMessage(said);
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("requestId", String.valueOf(pending.id()));
        fields.put("outcome", outcome);
        fields.put("message", said);
        _notices.sendAll(GameNotices.UNDO_RESOLVED, fields);
        startClocksForUsersPendingDecision();
        return new UndoReply("refused", said, pending.id());
    }

    private UndoReply carryOutUndo(String requester, RestorePoints.Point point, RevertAssessment assessment, String requestId) {
        String phase = phaseNow();
        //An undo is of one's own choice, which nobody else was touched by; anything more is a revert
        String said = assessment.tier() == RevertAssessment.Tier.GREEN ? requester + " undid: " + point.publicLabel()
                : requester + " reverted: " + point.publicLabel();
        String problem = revertTo(point.entryCount(), said, requester, assessment.tier().name(), point.publicLabel());
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("requestId", requestId);
        fields.put("requester", requester);
        fields.put("tier", assessment.tier().name());
        if (problem != null) {
            String failed = "Revert failed; the game continues as it was";
            _lotroGame.getGameState().sendMessage(failed);
            fields.put("outcome", "failed");
            fields.put("message", failed);
            _notices.sendAll(GameNotices.UNDO_RESOLVED, fields);
            startClocksForUsersPendingDecision();
            return new UndoReply("refused", failed, 0);
        }
        if (assessment.tier() == RevertAssessment.Tier.GREEN) {
            if (!phase.equals(_ownUndoPhase)) {
                _ownUndoPhase = phase;
                _ownUndos.clear();
            }
            _ownUndos.merge(requester, 1, Integer::sum);
        }
        fields.put("outcome", "done");
        fields.put("message", said);
        _notices.sendAll(GameNotices.UNDO_RESOLVED, fields);
        return new UndoReply("done", said, 0);
    }

    /** For tests of this package: what the game is waiting on a player for. */
    AwaitingDecision awaitingDecision(String playerId) {
        return _userFeedback.getAwaitingDecision(playerId);
    }

    public String getWinner() {
        return _lotroGame.getWinnerPlayerId();
    }

    public List<String> getPlayersPlaying() {
        return new LinkedList<>(_playersPlaying);
    }

    public String getGameStatus() {
        if (_lotroGame.isCancelled())
            return "Cancelled";
        if (_lotroGame.isFinished())
            return "Finished";
        final Phase currentPhase = _lotroGame.getGameState().getCurrentPhase();
        if (currentPhase == Phase.PLAY_STARTING_FELLOWSHIP || currentPhase == Phase.PUT_RING_BEARER)
            return "Preparation";
        return "At sites: " + getPlayerPositions();
    }

    public boolean isFinished() {
        return _lotroGame.isFinished();
    }

    public String produceCardInfo(Player player, int cardId) {
        _readLock.lock();
        try {
            PhysicalCard card = _lotroGame.getGameState().findCardById(cardId);
            if (card == null || card.getZone() == null)
                return null;

            boolean visible = card.getZone().isPublic()
                    || (player.getName().equals(card.getOwner()) && card.getZone().isVisibleByOwner());
            if (!visible)
                return null;

            if (card.getZone().isInPlay() || card.getZone() == Zone.HAND) {
                StringBuilder sb = new StringBuilder();

                if (card.getZone() == Zone.HAND)
                    sb.append("<b>Card is in hand - stats are only provisional</b><br><br>");
                else if (!Filters.hasActive(_lotroGame, card))
                    sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>");

                sb.append("<b>Affecting card:</b>");
                Collection<Modifier> modifiers = _lotroGame.getModifiersQuerying().getModifiersAffecting(_lotroGame, card);
                List<String> modifierLines = new ArrayList<>();
                for (Modifier modifier : modifiers) {
                    PhysicalCard source = modifier.getSource();
                    if (source != null) {
                        modifierLines.add("<br><b>" + GameUtils.getCardLink(source) + ":</b> "
                                + modifier.getText(_lotroGame, card));
                    }
                    else {
                        modifierLines.add("<br><b><i>System</i>:</b> " + modifier.getText(_lotroGame, card));
                    }
                }
                // Seeded test servers: the same game shows the same list, whatever the modifiers' hash order
                if (GameSeed.isSeeded())
                    Collections.sort(modifierLines);
                modifierLines.forEach(sb::append);
                if (modifiers.size() == 0) {
                    sb.append("<br><i>nothing</i>");
                }

                if (card.getZone().isInPlay() && card.getBlueprint().getCardType() == CardType.SITE) {
                    sb.append("<br><b>Owner:</b> ")
                            .append(card.getOwner());
                }

                Map<Token, Integer> map = _lotroGame.getGameState().getTokensHeld(card);
                if (map != null && map.size() > 0) {
                    sb.append("<br><b>Tokens:</b>");
                    for (Map.Entry<Token, Integer> tokenIntegerEntry : map.entrySet()) {
                        sb.append("<br>")
                                .append(tokenIntegerEntry.getKey().toString())
                                .append(": ")
                                .append(tokenIntegerEntry.getValue());
                    }
                }

                List<PhysicalCard> stackedCards = _lotroGame.getGameState().getStackedCards(card);
                if (stackedCards != null && stackedCards.size() > 0) {
                    sb.append("<br><b>Stacked cards:</b>")
                            .append("<br>")
                            .append(GameUtils.getAppendedNames(stackedCards));
                }

                final String extraDisplayableInformation = card.getBlueprint().getDisplayableInformation(card);
                if (extraDisplayableInformation != null) {
                    sb.append("<br><b>Extra information:</b>")
                            .append("<br>")
                            .append(extraDisplayableInformation);
                }

                sb.append("<br><br><b>Effective stats:</b>");
                try {
                    PhysicalCard target = card.getAttachedTo();
                    // A card in play costs the number printed on it (CR 4.2 "twilight cost"), and a site there
                    // shows its Shadow number as it stands; a card anywhere else shows what it would cost to play.
                    int twilightCost = card.getZone() != null && card.getZone().isInPlay()
                            ? _lotroGame.getModifiersQuerying().getCurrentTwilightCost(_lotroGame, card)
                            : _lotroGame.getModifiersQuerying().getTwilightCostToPlay(_lotroGame, card, target, 0, false);
                    sb.append("<br><b>Twilight cost:</b> ")
                            .append(twilightCost);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int strength = _lotroGame.getModifiersQuerying().getStrength(_lotroGame, card);
                    sb.append("<br><b>Strength:</b> ")
                            .append(strength);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int vitality = _lotroGame.getModifiersQuerying().getVitality(_lotroGame, card);
                    sb.append("<br><b>Vitality:</b> ")
                            .append(vitality);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int resistance = _lotroGame.getModifiersQuerying().getResistance(_lotroGame, card);
                    sb.append("<br><b>Resistance:</b> ")
                            .append(resistance);
                } catch (UnsupportedOperationException ignored) {
                }
                try {
                    int siteNumber = _lotroGame.getModifiersQuerying().getMinionSiteNumber(_lotroGame, card);
                    sb.append("<br><b>Site number:</b> ")
                            .append(siteNumber);
                } catch (UnsupportedOperationException ignored) {
                }

                StringBuilder keywords = new StringBuilder();
                for (Keyword keyword : Keyword.values()) {
                    if (keyword.isInfoDisplayable()) {
                        if (keyword.isMultiples()) {
                            int count = _lotroGame.getModifiersQuerying().getKeywordCount(_lotroGame, card, keyword);
                            if (count > 0)
                                keywords.append(keyword.getHumanReadable())
                                        .append(" +")
                                        .append(count)
                                        .append(", ");
                        } else {
                            if (_lotroGame.getModifiersQuerying().hasKeyword(_lotroGame, card, keyword))
                                keywords.append(keyword.getHumanReadable())
                                        .append(", ");
                        }
                    }
                }
                if (keywords.length() > 0) {
                    sb.append("<br><b>Keywords:</b> ")
                            .append(keywords.substring(0, keywords.length() - 2));
                }

                sb.append("<br><br><b>Printed Game Text: </b><br>")
                        .append(card.getBlueprint().getFormattedGameText());

                String helpText = card.getBlueprint().getHelpText();
                if (helpText != null && !helpText.isEmpty()) {
                    sb.append("<br><br><b>Additional Info: </b><br>")
                            .append(helpText);
                }

                return sb.toString();
            } else {
                return null;
            }
        } finally {
            _readLock.unlock();
        }
    }

    public void startGame() {
        _writeLock.lock();
        try {
            if (_scenario != null)
                _scenario.Begin();
            else
                _lotroGame.startGame();
            startClocksForUsersPendingDecision();
        } finally {
            _writeLock.unlock();
        }
    }

    public void cleanup() {
        // The cleaner runs every second. If this game is in the middle of processing a player
        // action (which holds the write lock for the whole action), skip this pass instead of
        // waiting for it; the next pass will pick it up.
        if (!_writeLock.tryLock())
            return;
        try {
            long currentTime = System.currentTimeMillis();
            if (_pendingUndo != null && currentTime > _pendingUndo.startedAt() + _undoPolicy.timeoutSeconds() * 1000L)
                settlePendingUndo("timedOut", null);
            Map<String, GameCommunicationChannel> channelsCopy = new HashMap<>(_communicationChannels);
            for (Map.Entry<String, GameCommunicationChannel> playerChannels : channelsCopy.entrySet()) {
                String playerId = playerChannels.getKey();
                // Channel is stale (user no longer connected to game, to save memory, we remove the channel
                // User can always reconnect and establish a new channel
                GameCommunicationChannel channel = playerChannels.getValue();
                if (currentTime > channel.getLastAccessed() + _timeSettings.maxSecondsPerDecision() * 1000L) {
                    removeGameStateListener(channel);
                    _communicationChannels.remove(playerId);
                }
            }

            if (_scenario != null) {
                //Whoever the game is waiting on, it is the person who is keeping it waiting: the scenario's own seat
                // answers at once, unless it is waiting for the person to read something first.
                if (_lotroGame.getWinnerPlayerId() == null) {
                    for (long decisionSent : new ArrayList<>(_decisionQuerySentTimes.values())) {
                        if (currentTime > decisionSent + _timeSettings.maxSecondsPerDecision() * 1000L) {
                            _scenario.Abandoned();
                            _lotroGame.playerLost(_scenario.HumanPlayer(), "Player decision timed-out");
                            break;
                        }
                    }
                }
            } else if (_lotroGame != null && _lotroGame.getWinnerPlayerId() == null) {
                for (Map.Entry<String, Long> playerDecision : new HashMap<>(_decisionQuerySentTimes).entrySet()) {
                    String player = playerDecision.getKey();
                    long decisionSent = playerDecision.getValue();
                    if (currentTime > decisionSent + _timeSettings.maxSecondsPerDecision() * 1000L) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _lotroGame.playerLost(player, "Player decision timed-out");
                    }
                    // Bot player 5 secs time out
                    if (player.startsWith("~") && (currentTime > decisionSent + 5 * 1000L)) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _lotroGame.playerLost(player, "Bot got stuck on a decision");
                    }
                }

                for (Map.Entry<String, Integer> playerClock : _playerClocks.entrySet()) {
                    String player = playerClock.getKey();
                    if (_timeSettings.maxSecondsPerPlayer() - playerClock.getValue() - getCurrentUserPendingTime(player) < 0) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _lotroGame.playerLost(player, "Player run out of time");
                    }
                }
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public void concede(Player player) {
        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (_lotroGame.getWinnerPlayerId() == null && _playersPlaying.contains(playerId)) {
                addTimeSpentOnDecisionToUserClock(playerId);
                if (_scenario != null)
                    _scenario.Abandoned();
                _lotroGame.playerLost(playerId, "Concession");
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public void cancel(Player player) {
        _lotroGame.getGameState().sendWarning(player.getName(), "You can't cancel this game");

        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (_playersPlaying.contains(playerId))
                _lotroGame.requestCancel(playerId);
        } finally {
            _writeLock.unlock();
        }
    }

    public synchronized void playerAnswered(Player player, int channelNumber, int decisionId, String answer) throws SubscriptionConflictException, SubscriptionExpiredException {
        String playerName = player.getName();
        _writeLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel != null) {
                if (communicationChannel.getChannelNumber() == channelNumber) {
                    AwaitingDecision awaitingDecision = _userFeedback.getAwaitingDecision(playerName);
                    if (awaitingDecision != null && _pendingUndo != null) {
                        //The game stands still while a request to take it back is waiting for an answer
                        _lotroGame.getGameState().sendWarning(playerName, "Waiting on a revert request");
                    } else if (awaitingDecision != null && _scenario != null && _scenario.Stopped()) {
                        //A stopped sandbox takes no answers: its decision is asked again, afresh, at Start
                        _lotroGame.getGameState().sendWarning(playerName, "The game is stopped: press Start to play on");
                    } else if (awaitingDecision != null) {
                        if (awaitingDecision.getAwaitingDecisionId() == decisionId && !_lotroGame.isFinished()) {
                            long startedAt = System.currentTimeMillis();
                            String decisionText = awaitingDecision.getText();

                            try {
                                _userFeedback.participantDecided(playerName);
                                awaitingDecision.decisionMade(answer);
                                _userFeedback.decisionAccepted(playerName, awaitingDecision, answer);

                                // Decision successfully made, add the time to user clock
                                addTimeSpentOnDecisionToUserClock(playerName);

                                _lotroGame.carryOutPendingActionsUntilDecisionNeeded();
                                startClocksForUsersPendingDecision();

                            } catch (DecisionResultInvalidException decisionResultInvalidException) {
                                // Participant provided wrong answer - send a warning message, and ask again for the same decision
                                _lotroGame.getGameState().sendWarning(playerName, decisionResultInvalidException.getWarningMessage());
                                _userFeedback.sendAwaitingDecision(playerName, awaitingDecision);
                                _userFeedback.atRest();
                            } catch (RuntimeException runtimeException) {
                                LOG.error("Error processing game decision", runtimeException);
                                _lotroGame.cancelGame();
                            } finally {
                                logIfSlow("player", playerName, decisionId, decisionText, answer, startedAt);
                            }
                            scenarioAfterActions();
                        }
                    }
                } else {
                    throw new SubscriptionConflictException();
                }
            } else {
                throw new SubscriptionExpiredException();
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public synchronized void botAnswered(String botName, int decisionId, String answer) {
        _writeLock.lock();
        try {
            AwaitingDecision awaitingDecision = _userFeedback.getAwaitingDecision(botName);
            if (awaitingDecision != null && _pendingUndo == null) {
                if (awaitingDecision.getAwaitingDecisionId() == decisionId && !_lotroGame.isFinished()) {
                    if (botDecisions > 1000) {
                        // Bot made tons of decisions this game, probably non-trivial loop - concede
                        System.out.println(botName + " probably in non-trivial loop, ending the game");

                        addTimeSpentOnDecisionToUserClock(botName);
                        if (_scenario != null)
                            _scenario.Broke("the opponent has answered more than 1000 decisions: probably a loop");
                        _lotroGame.playerLost(botName, "Possible loop detected");
                        
                    }

                    try {
                        _userFeedback.participantDecided(botName);
                        // A rejected (or missing) bot answer falls back to a valid default before conceding (#1097)
                        String accepted = BotDecisionFallback.decide(_lotroGame, botName, awaitingDecision, answer);
                        if (!accepted.equals(answer))
                            LOG.warn("Bot " + botName + " game=" + _gameId + " decision=\"" + awaitingDecision.getText()
                                    + "\": answer \"" + answer + "\" was rejected, used fallback \"" + accepted + "\"");

                        _userFeedback.decisionAccepted(botName, awaitingDecision, accepted);

                        // Decision successfully made, add the time to user clock
                        addTimeSpentOnDecisionToUserClock(botName);

                        _lotroGame.carryOutPendingActionsUntilDecisionNeeded();
                        startClocksForUsersPendingDecision();
                        
                        botDecisions++;

                    } catch (DecisionResultInvalidException decisionResultInvalidException) {
                        // Bot provided wrong answer - concede to prevent loop
                        System.out.println(botName + " wrong answer - " + decisionResultInvalidException.getWarningMessage());

                        addTimeSpentOnDecisionToUserClock(botName);
                        if (_scenario != null)
                            _scenario.Broke("the game would take no answer from the opponent to \"" + awaitingDecision.getText()
                                    + "\": " + decisionResultInvalidException.getWarningMessage());
                        _lotroGame.playerLost(botName, "Invalid decision");
                    } catch (RuntimeException runtimeException) {
                        LOG.error("Error processing game decision", runtimeException);
                        _lotroGame.cancelGame();
                    }
                    scenarioAfterActions();
                }
            }
        } finally {
            _writeLock.unlock();
        }
    }

    /**
     * A scenario decides for itself when it is over, and looks each time its game comes to rest.
     */
    private void scenarioAfterActions() {
        if (_scenario == null)
            return;
        try {
            _scenario.AfterActions();
            startClocksForUsersPendingDecision();
        } catch (RuntimeException exp) {
            LOG.error("Error in scenario " + _scenario.Definition().id + ", game " + _gameId, exp);
            _lotroGame.cancelGame();
            _scenario.Broke("the scenario threw " + exp);
        }
    }

    /**
     * A scenario's scripted opponent gives its answer.  Decision ids are no guide to which decision is meant (nearly
     * every decision has the same one), so the answer is only taken if the opponent is still being asked the very
     * decision it was worked out for, and the scenario is still going.
     */
    private synchronized void scenarioOpponentAnswered(String opponent, AwaitingDecision decision, String answer) {
        //(synchronized first and the lock second, the same order botAnswered and playerAnswered take them in)
        _writeLock.lock();
        try {
            //(nor while a sandbox is stopped: its decision will be asked again, and answered then)
            if (_scenario.Result() == null && !_scenario.Stopped() && _userFeedback.getAwaitingDecision(opponent) == decision)
                botAnswered(opponent, decision.getAwaitingDecisionId(), answer);
        } finally {
            _writeLock.unlock();
        }
    }

    /**
     * The person playing a scenario has read a step that was waiting to be read: what one of the opponent's steps
     * had to say, so the opponent may act; or a pause over a decision of their own.
     * @param choice The button they pressed, if the step ended in a choice; null otherwise.
     */
    public void scenarioAcknowledged(Player player, int stepNumber, String choice) {
        _writeLock.lock();
        try {
            if (_scenario != null && !_lotroGame.isFinished()) {
                try {
                    _scenario.Acknowledge(player.getName(), stepNumber, choice);
                } catch (RuntimeException exp) {
                    //Reading a pause sets the game's decisions moving again, and that can go wrong like any answer
                    LOG.error("Error in scenario " + _scenario.Definition().id + ", game " + _gameId, exp);
                    _lotroGame.cancelGame();
                    _scenario.Broke("the scenario threw " + exp);
                }
                //A pause can be the last thing in a scenario
                scenarioAfterActions();
            }
        } finally {
            _writeLock.unlock();
        }
    }

    /**
     * Does something to a scenario's game from outside it (the authoring tool: reading where the game stands,
     * rewriting the step in hand, changing the board), with the game held still meanwhile.  Afterwards the scenario
     * is given its usual look at whether it is over.
     */
    public <T> T scenarioLocked(java.util.function.Supplier<T> action) {
        _writeLock.lock();
        try {
            T result = action.get();
            if (!_lotroGame.isFinished())
                scenarioAfterActions();
            return result;
        } finally {
            _writeLock.unlock();
        }
    }

    public void scenarioAcknowledged(Player player, int stepNumber) {
        scenarioAcknowledged(player, stepNumber, null);
    }

    /* ---- a sandbox (ScenarioSandbox): Stop, edits, Start, and what the person may see ---- */

    /**
     * @return The sandbox of this game, if the player is the person playing it; null otherwise.  To be called with
     * the write lock held.
     */
    private com.gempukku.lotro.scenario.ScenarioSandbox sandboxOf(String player) {
        if (_scenario == null || _scenario.Sandbox() == null || !_scenario.HumanPlayer().equals(player))
            return null;
        return _scenario.Sandbox();
    }

    private static final String NOT_A_SANDBOX = "This is not a sandbox you are playing";

    /**
     * The person playing a sandbox stops it, to change its board.
     * @return Null if it was stopped; otherwise why not.
     */
    public String sandboxStop(String player) {
        return sandbox(player, sandbox -> sandbox.Stop());
    }

    /**
     * The person playing a sandbox starts it again: the decision it was stopped at is asked afresh.
     * @return Null if it was started; otherwise why not.
     */
    public String sandboxStart(String player) {
        return sandbox(player, sandbox -> sandbox.Start());
    }

    /**
     * The person playing a stopped sandbox changes its board (ScenarioSandbox.Edit): a list of edits, made whole
     * or not at all.  An edit that fails after others of its list were made has the game taken back to before the
     * list (where it can be: a game that cannot be built again keeps what was made, and says so).
     * @return Null if every edit was made; otherwise why not.
     */
    public String sandboxEdit(String player, String opsJson) {
        return sandbox(player, sandbox -> {
            int before = _journal == null ? -1 : _journal.entries().size();
            com.gempukku.lotro.scenario.ScenarioSandbox.Outcome outcome = sandbox.Edit(opsJson);
            if (outcome.problem() == null || !outcome.partlyMade())
                return outcome.problem();
            String undone = before < 0 ? "not taken back" : revertTo(before, "The edits were not made", player, "GREEN", "");
            return outcome.problem() + (undone == null ? " (nothing in the list was made)"
                    : ".  The edits before it were made and could not be taken back: " + undone);
        });
    }

    /** @return What the person playing a sandbox may see of it (ScenarioSandbox.State); null if they are not. */
    public Map<String, Object> sandboxState(String player) {
        _writeLock.lock();
        try {
            com.gempukku.lotro.scenario.ScenarioSandbox sandbox = sandboxOf(player);
            return sandbox == null ? null : sandbox.State();
        } finally {
            _writeLock.unlock();
        }
    }

    private String sandbox(String player, java.util.function.Function<com.gempukku.lotro.scenario.ScenarioSandbox, String> action) {
        _writeLock.lock();
        try {
            com.gempukku.lotro.scenario.ScenarioSandbox sandbox = sandboxOf(player);
            if (sandbox == null)
                return NOT_A_SANDBOX;
            if (_lotroGame.isFinished())
                return "The game is over";
            String problem;
            try {
                problem = action.apply(sandbox);
            } catch (RuntimeException exp) {
                LOG.error("Sandbox " + _scenario.Definition().id + ", game " + _gameId + ": could not do what was asked", exp);
                _lotroGame.cancelGame();
                _scenario.Broke("the sandbox threw " + exp);
                return "That broke the game: " + exp;
            }
            //(the person's clock starts afresh with each thing they do: a sandbox being set up is not a person gone away)
            scenarioAfterActions();
            return problem;
        } finally {
            _writeLock.unlock();
        }
    }

    public GameState getGameState() {
        _readLock.lock();
        try {
            return _lotroGame.getGameState();
        } finally {
            _readLock.unlock();
        }
    }

    public BotPlayer getBotPlayer() {
        return _botPlayer;
    }

    public LotroDeck getBotDeck() {
        return _botDeck;
    }

    /**
     * Logs one line when a decision took longer than SLOW_ACTION_THRESHOLD_MS to process. The
     * time covers everything that ran under the game's write lock as a result of the answer:
     * the action itself, every triggered response, and for a human answer also any AI decisions
     * that followed before the next human decision was needed (those are logged separately as
     * kind "ai" so they can be told apart).
     */
    private void logIfSlow(String kind, String playerName, int decisionId, String decisionText, String answer, long startedAt) {
        long elapsedMs = System.currentTimeMillis() - startedAt;
        if (elapsedMs < SLOW_ACTION_THRESHOLD_MS)
            return;
        String phase = null;
        String currentPlayer = null;
        try {
            if (_lotroGame.getGameState() != null) {
                phase = String.valueOf(_lotroGame.getGameState().getCurrentPhase());
                currentPlayer = _lotroGame.getGameState().getCurrentPlayerId();
            }
        } catch (RuntimeException ignored) {
            // Diagnostics only; never let logging break the game.
        }
        LOG.warn("Slow action: took=" + elapsedMs + "ms kind=" + kind + " game=" + _gameId
                + " player=" + playerName + " decisionId=" + decisionId
                + " decision=\"" + decisionText + "\" answer=\"" + answer + "\""
                + " phase=" + phase + " turnOf=" + currentPlayer);
    }

    public GameCommunicationChannel getCommunicationChannel(Player player, int channelNumber) throws PrivateInformationException, SubscriptionConflictException, SubscriptionExpiredException {
        String playerName = player.getName();
        if (!player.hasType(Player.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        _readLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel != null) {
                if (communicationChannel.getChannelNumber() == channelNumber) {
                    return communicationChannel;
                } else {
                    throw new SubscriptionConflictException();
                }
            } else {
                throw new SubscriptionExpiredException();
            }
        } finally {
            _readLock.unlock();
        }
    }

    public void processVisitor(GameCommunicationChannel communicationChannel, int channelNumber, String playerName, ParticipantCommunicationVisitor visitor) {
        _readLock.lock();
        try {
            visitor.visitChannelNumber(channelNumber);
            for (GameEvent gameEvent : communicationChannel.consumeGameEvents())
                visitor.visitGameEvent(gameEvent);

            Map<String, Integer> secondsLeft = new HashMap<>();
            for (Map.Entry<String, Integer> playerClock : _playerClocks.entrySet()) {
                String playerClockName = playerClock.getKey();
                secondsLeft.put(playerClockName, _timeSettings.maxSecondsPerPlayer() - playerClock.getValue() - getCurrentUserPendingTime(playerClockName));

                if (_decisionQuerySentTimes.containsKey(playerClockName))
                    secondsLeft.put("decisionClock", getCurrentUserPendingTime(playerClockName));
            }
            // the decision time limit, so the client can show "elapsed / limit" and warn before a decision times out
            secondsLeft.put("decisionLimit", _timeSettings.maxSecondsPerDecision());
            visitor.visitClock(secondsLeft);
        } finally {
            _readLock.unlock();
        }
    }

    public void signupUserForGame(Player player, ParticipantCommunicationVisitor visitor) throws PrivateInformationException {
        String playerName = player.getName();
        if (!player.hasType(Player.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        // The write lock: this changes the channel counter, the channels and the game's listeners, so two players
        // joining at the same moment must not run it together (a ConcurrentModificationException in GameState did)
        _writeLock.lock();
        try {
            int number = _channelNextIndex;
            _channelNextIndex++;

            GameCommunicationChannel participantCommunicationChannel = new GameCommunicationChannel(playerName, number, true, _lotroGame.getFormat());
            _communicationChannels.put(playerName, participantCommunicationChannel);

            addGameStateListener(playerName, participantCommunicationChannel);

            visitor.visitChannelNumber(number);

            for (GameEvent gameEvent : participantCommunicationChannel.consumeGameEvents())
                visitor.visitGameEvent(gameEvent);

            Map<String, Integer> secondsLeft = new HashMap<>();
            for (Map.Entry<String, Integer> playerClock : _playerClocks.entrySet()) {
                String playerId = playerClock.getKey();
                secondsLeft.put(playerId, _timeSettings.maxSecondsPerPlayer() - playerClock.getValue() - getCurrentUserPendingTime(playerId));

                if (_decisionQuerySentTimes.containsKey(playerId))
                    secondsLeft.put("decisionClock", getCurrentUserPendingTime(playerId));
            }
            secondsLeft.put("decisionLimit", _timeSettings.maxSecondsPerDecision());

            visitor.visitClock(secondsLeft);
        } finally {
            _writeLock.unlock();
        }
    }

    private void startClocksForUsersPendingDecision() {
        long currentTime = System.currentTimeMillis();
        Set<String> users = _userFeedback.getUsersPendingDecision();
        for (String user : users)
            _decisionQuerySentTimes.put(user, currentTime);
    }

    private void addTimeSpentOnDecisionToUserClock(String participantId) {
        Long queryTime = _decisionQuerySentTimes.remove(participantId);
        if (queryTime != null) {
            long currentTime = System.currentTimeMillis();
            long diffSec = (currentTime - queryTime) / 1000;
            _playerClocks.put(participantId, _playerClocks.get(participantId) + (int) diffSec);
        }
    }

    public int getCurrentUserPendingTime(String participantId) {
        //While a request to take the game back is waiting, it is the clock of whoever asked that runs
        PendingUndo pending = _pendingUndo;
        if (pending != null && pending.requester().equals(participantId))
            return (int) ((System.currentTimeMillis() - pending.startedAt()) / 1000);
        if (!_decisionQuerySentTimes.containsKey(participantId))
            return 0;
        long queryTime = _decisionQuerySentTimes.get(participantId);
        long currentTime = System.currentTimeMillis();
        return (int) ((currentTime - queryTime) / 1000);
    }

    public GameTimer getTimeSettings() {
        return _timeSettings;
    }

    public Map<String, Integer> getPlayerClocks() { return Collections.unmodifiableMap(_playerClocks); }

    public String getPlayerPositions() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String player : _playersPlaying) {
            stringBuilder.append(_lotroGame.getGameState().getPlayerPosition(player) + ", ");
        }
        if (stringBuilder.length() > 0)
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());

        return stringBuilder.toString();
    }
}
