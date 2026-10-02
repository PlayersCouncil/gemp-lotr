package com.gempukku.lotro.game;

import com.gempukku.lotro.AbstractServer;
import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.chat.ChatCommandErrorException;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.db.DeckDAO;
import com.gempukku.lotro.game.state.GameExtraInfo;
import com.gempukku.lotro.hall.GameSettings;
import com.gempukku.lotro.packs.PackOpener;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.bots.BotPlayer;
import com.gempukku.lotro.bots.BotService;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.scenario.LiveScenario;
import com.gempukku.lotro.scenario.ScenarioDefinition;
import com.gempukku.lotro.scenario.ScenarioRunLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LotroServer extends AbstractServer {
    private static final Logger log = LogManager.getLogger(LotroServer.class);

    private final LotroCardBlueprintLibrary _lotroCardBlueprintLibrary;

    private final Map<String, LotroGameMediator> _runningGames = new ConcurrentHashMap<>();
    private final Set<String> _gameDeathWarningsSent = new HashSet<>();

    private final Map<String, Date> _finishedGamesTime = Collections.synchronizedMap(new LinkedHashMap<>());
    private final long _timeToGameDeath = 1000 * 60 * 5; // 5 minutes
    private final long _timeToGameDeathWarning = 1000 * 60 * 4; // 4 minutes

    private int _nextGameId = 1;

    private final DeckDAO _deckDao;

    private final ChatServer _chatServer;
    private final GameRecorder _gameRecorder;
    private final MarkdownParser _markdownParser;

    private final BotService _botService;
    private final PackOpener _packOpener;

    private final ReadWriteLock _lock = new ReentrantReadWriteLock();

    /**
     * A scenario's player is given a day to finish and half an hour over any one decision.  The clock is only there
     * to notice that somebody has gone, never to hurry anybody who is still reading.
     */
    public static final GameTimer SCENARIO_TIMER = new GameTimer(false, "Scenario", 60 * 60 * 24, 60 * 30);

    /** The scenario each player is in the middle of, by player name.  A player has one at a time. */
    private final Map<String, String> _scenarioGames = new HashMap<>();
    private final Map<String, Object> _scenarioStartLocks = new ConcurrentHashMap<>();
    /** Gives the scenarios' scripted opponents their answers, each after its pause. */
    private final ScheduledExecutorService _scenarioTimer;
    /** Keeps the record of scenario games.  Set once the services it needs exist; until then nothing is recorded. */
    private ScenarioRunLog _scenarioRunLog;

    public void setScenarioRunLog(ScenarioRunLog scenarioRunLog) {
        _scenarioRunLog = scenarioRunLog;
    }

    public LotroServer(DeckDAO deckDao, LotroCardBlueprintLibrary library, ChatServer chatServer,
            GameRecorder gameRecorder, MarkdownParser parser, BotService botService,
            PackOpener packOpener) {
        _deckDao = deckDao;
        _lotroCardBlueprintLibrary = library;
        _chatServer = chatServer;
        _gameRecorder = gameRecorder;
        _markdownParser = parser;
        _botService = botService;
        _packOpener = packOpener;

        AtomicInteger threadNumber = new AtomicInteger();
        _scenarioTimer = Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "scenario-opponent-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    protected void cleanup() {
        _lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();

            LinkedHashMap<String, Date> copy = new LinkedHashMap<>(_finishedGamesTime);
            for (Map.Entry<String, Date> finishedGame : copy.entrySet()) {
                String gameId = finishedGame.getKey();
                if (currentTime > finishedGame.getValue().getTime() + _timeToGameDeathWarning
                        && !_gameDeathWarningsSent.contains(gameId)) {
                    try {
                        _chatServer.getChatRoom(getChatRoomName(gameId)).sendMessage("System", "This game is already finished and will be shortly removed, please move to the Game Hall", true);
                    } catch (PrivateInformationException exp) {
                        // Ignore, sent as admin
                    } catch (ChatCommandErrorException e) {
                        // Ignore, no command
                    }
                    _gameDeathWarningsSent.add(gameId);
                }
                if (currentTime > finishedGame.getValue().getTime() + _timeToGameDeath) {
                    _runningGames.get(gameId).destroy();
                    _gameDeathWarningsSent.remove(gameId);
                    _runningGames.remove(gameId);
                    _chatServer.destroyChatRoom(getChatRoomName(gameId));
                    _finishedGamesTime.remove(gameId);
                } else {
                    break;
                }
            }

        } finally {
            _lock.writeLock().unlock();
        }

        // Per-game cleanup is done outside the server-wide lock. Each mediator's cleanup()
        // needs that game's own write lock, which is held for the whole duration of a player
        // action. Holding the server-wide write lock while waiting for a busy game blocked
        // every getGameById() reader (all game long-polls and decision submits on the server)
        // for as long as that one action took.
        for (LotroGameMediator mediator : new ArrayList<>(_runningGames.values())) {
            mediator.cleanup();
        }
    }

    private String getChatRoomName(String gameId) {
        return "Game" + gameId;
    }

    public LotroGameMediator createNewGame(String tournamentName, final LotroGameParticipant[] participants, GameSettings gameSettings, GameExtraInfo extraInfo) {
        _lock.writeLock().lock();
        try {
            if(gameSettings.isSolo()) {
                if (participants.length < 1) {
                    throw new IllegalArgumentException("There has to be at least one player in a solo game");
                }
                if (participants.length == 1 && !_botService.hasBotParticipant(gameSettings.format())) {
                    throw new IllegalArgumentException("Deck was not provided for untrained format; bot cannot be initialized");
                }
            }
            else {
                if (participants.length < 2) {
                    throw new IllegalArgumentException("There has to be at least two players");
                }
            }

            final String gameId = String.valueOf(_nextGameId);

            if (gameSettings.competitive()) {
                Set<String> allowedUsers = new HashSet<>();
                for (LotroGameParticipant participant : participants)
                    allowedUsers.add(participant.getPlayerId());
                _chatServer.createPrivateChatRoom(getChatRoomName(gameId), false, allowedUsers, 30);
            } else
                _chatServer.createChatRoom(getChatRoomName(gameId), false, 30, false, null, null);

            // Allow spectators for leagues, but not tournaments
            // Also: yes, yes, we're very proud that you found a way to assign this boolean in one line.
            // The point of the code setting it like this is to make each case painfully explicit.
            boolean spectate = true;
            if(gameSettings.league() != null) {
                spectate = true;
            }
            else if(gameSettings.privateGame() || gameSettings.hiddenGame()) {
                spectate = false;
            }

            LotroGameMediator lotroGameMediator = new LotroGameMediator(gameId, gameSettings.format(), participants,
                    _lotroCardBlueprintLibrary, gameSettings.timeSettings(), spectate, !gameSettings.competitive(),
                    gameSettings.hiddenGame(), tournamentName, _markdownParser, _botService, gameSettings.isSolo(),
                    extraInfo, _packOpener);
            lotroGameMediator.addGameResultListener(
                new GameResultListener() {
                    @Override
                    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                        _finishedGamesTime.put(gameId, new Date());
                    }

                    @Override
                    public void gameCancelled() {
                        _finishedGamesTime.put(gameId, new Date());
                    }
                });
            var formatName = gameSettings.format().getName();
            lotroGameMediator.sendMessageToPlayers("You are starting a game of " + formatName);
            if(formatName.contains("PC")) {
                lotroGameMediator.sendMessageToPlayers(LotroFormat.PCSummary);
            }


            StringBuilder players = new StringBuilder();
            Map<String, LotroDeck> decks =  new HashMap<>();
            for (LotroGameParticipant participant : participants) {
                if (players.length() > 0)
                    players.append(", ");
                players.append(participant.getPlayerId());
                decks.put(participant.getPlayerId(), participant.getDeck());
            }

            BotPlayer botPlayer = lotroGameMediator.getBotPlayer();
            if (botPlayer != null && participants.length < 2) {
                decks.put(botPlayer.getName(), lotroGameMediator.getBotDeck());
                players.append(", ");
                players.append(botPlayer.getName());
            }

            lotroGameMediator.sendMessageToPlayers("Players in the game are: " + players);

            final var gameRecordingInProgress = _gameRecorder.recordGame(lotroGameMediator, gameSettings.format(), tournamentName, decks);
            lotroGameMediator.addGameResultListener(
                new GameResultListener() {
                    @Override
                    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                        final var loserEntry = loserPlayerIdsWithReasons.entrySet().iterator().next();

                        //potentially this is where to kick off any "reveal deck" events
                        //lotroGameMediator.readoutParticipantDecks();
                        gameRecordingInProgress.finishRecording(winnerPlayerId, winReason, loserEntry.getKey(), loserEntry.getValue());
                    }

                    @Override
                    public void gameCancelled() {
                        if (participants.length == 2) {
                            gameRecordingInProgress.finishRecording(participants[0].getPlayerId(), "Game cancelled due to error", participants[1].getPlayerId(), "Game cancelled due to error");
                        } else {
                            gameRecordingInProgress.finishRecording(participants[0].getPlayerId(), "Game cancelled due to error", botPlayer.getName(), "Game cancelled due to error");
                        }
                    }
                }
            );

            _runningGames.put(gameId, lotroGameMediator);
            _nextGameId++;
            return lotroGameMediator;
        } finally {
            _lock.writeLock().unlock();
        }
    }

    /**
     * Starts a scenario for a player: a game against the scenario's scripted opponent, on the board and with the
     * decks its file describes.  The game is not listed in the hall and nobody else can watch it.  If the player was
     * in the middle of another scenario, that one is given up.
     * @param format The scenario's format, already looked up.
     * @param text Turns the text of the scenario's messages into what the client shows.
     * @param fromCheckpoint Whether to pick up at the last checkpoint the player reached in their previous game of
     *                       this same scenario, if that game is still known and reached one.  Otherwise, and if that
     *                       cannot be done after all (the scenario has changed since), the game starts at the
     *                       beginning.
     * @return The game, set going and waiting on its first decision.
     * @throws RuntimeException if the scenario's board cannot be laid out.  A scenario that passes the headless
     * runner never does this.
     */
    public LotroGameMediator createScenarioGame(Player player, ScenarioDefinition scenario, LotroFormat format,
                                                LiveScenario.TextRenderer text, boolean fromCheckpoint) {
        return createScenarioGame(player, scenario, format, text, fromCheckpoint, null);
    }

    /**
     * @param campaign Where the scenario stands in its campaign, for the person's screen (see
     *                 ScenarioLibrary.campaignInfo); null if it is in none.
     */
    public LotroGameMediator createScenarioGame(Player player, ScenarioDefinition scenario, LotroFormat format,
                                                LiveScenario.TextRenderer text, boolean fromCheckpoint, Map<String, Object> campaign) {
        final String playerName = player.getName();
        //One at a time for each player.  A second request waits for the first to finish, and then gives up the game
        // the first one started, like any other request to start a scenario while in the middle of one.
        synchronized (_scenarioStartLocks.computeIfAbsent(playerName, name -> new Object())) {
            final String gameId;
            final LotroGameMediator previous;

            _lock.writeLock().lock();
            try {
                String previousId = _scenarioGames.get(playerName);
                previous = previousId == null ? null : _runningGames.get(previousId);
                gameId = String.valueOf(_nextGameId);
                _nextGameId++;
            } finally {
                _lock.writeLock().unlock();
            }

            //Everything from here to the next lock is outside the server-wide one: it takes games' own locks, and
            // laying out and replaying a game is real work.  Nobody else can reach the new game until it is registered.
            GameJournal checkpoint = null;
            if (previous != null) {
                //The previous game is either still going or finished within the last few minutes
                if (fromCheckpoint && previous.getScenario() != null && previous.getScenario().Definition() == scenario)
                    checkpoint = previous.checkpointJournal();
                if (!previous.isFinished())
                    previous.concede(player);
            }

            StartedScenario started = null;
            if (checkpoint != null) {
                started = startScenario(gameId, player, scenario, format, text, checkpoint, campaign);
                String problem = started.mediator.getScenario().Table().FollowProblem();
                if (problem != null) {
                    log.warn("Scenario " + scenario.id + " v" + scenario.version + " could not be picked up at its checkpoint for "
                            + playerName + " and was started from the beginning: " + problem);
                    started.mediator.discardScenario();
                    started = null;
                }
            }
            if (started == null)
                started = startScenario(gameId, player, scenario, format, text, null, campaign);
            if (started.watch != null)
                started.watch.keep();

            _lock.writeLock().lock();
            try {
                _chatServer.createPrivateChatRoom(getChatRoomName(gameId), false, new HashSet<>(Set.of(playerName)), 30);
                _runningGames.put(gameId, started.mediator);
                _scenarioGames.put(playerName, gameId);
            } finally {
                _lock.writeLock().unlock();
            }
            return started.mediator;
        }
    }

    /**
     * Somebody writing a scenario sits over a decision for as long as it takes to write what is said about it.  The
     * clock is still only there to notice that they have gone.
     */
    public static final GameTimer AUTHORING_TIMER = new GameTimer(false, "Authoring", 60 * 60 * 24 * 3, 60 * 60 * 12);

    /**
     * Seats somebody at a game of a scenario they are writing (see ScenarioAuthoring, which builds the game).  It
     * takes the place of whatever scenario they were in the middle of, like any other scenario's game, but nothing
     * about it is recorded: it is not a play of anything.  The new game is set going before the old one is given up,
     * so that a draft which cannot be laid out leaves its author where they were.
     * @throws RuntimeException if the scenario's board cannot be laid out.
     */
    public LotroGameMediator seatAuthoringGame(Player player, LiveScenario live) {
        final String playerName = player.getName();
        synchronized (_scenarioStartLocks.computeIfAbsent(playerName, name -> new Object())) {
            final String gameId;
            final LotroGameMediator previous;
            _lock.writeLock().lock();
            try {
                String previousId = _scenarioGames.get(playerName);
                previous = previousId == null ? null : _runningGames.get(previousId);
                gameId = String.valueOf(_nextGameId);
                _nextGameId++;
            } finally {
                _lock.writeLock().unlock();
            }

            LotroGameMediator mediator = new LotroGameMediator(gameId, live, AUTHORING_TIMER, false);
            mediator.addGameResultListener(
                new GameResultListener() {
                    @Override
                    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                        _finishedGamesTime.put(gameId, new Date());
                    }

                    @Override
                    public void gameCancelled() {
                        _finishedGamesTime.put(gameId, new Date());
                    }
                });
            mediator.startGame();

            if (previous != null && !previous.isFinished())
                previous.concede(player);

            _lock.writeLock().lock();
            try {
                _chatServer.createPrivateChatRoom(getChatRoomName(gameId), false, new HashSet<>(Set.of(playerName)), 30);
                _runningGames.put(gameId, mediator);
                _scenarioGames.put(playerName, gameId);
            } finally {
                _lock.writeLock().unlock();
            }
            return mediator;
        }
    }

    private record StartedScenario(LotroGameMediator mediator, ScenarioRunLog.Watch watch) { }

    /**
     * Builds a scenario's game and sets it going.  Everything that has to hear how the game ends is listening before
     * it starts: the scripted opponent can act, and in principle end the game, the moment it does.
     */
    private StartedScenario startScenario(String gameId, Player player, ScenarioDefinition scenario, LotroFormat format,
                                          LiveScenario.TextRenderer text, GameJournal checkpoint, Map<String, Object> campaign) {
        LiveScenario live = new LiveScenario(scenario, _lotroCardBlueprintLibrary, format, player.getName(), text, _scenarioTimer, checkpoint);
        live.SetCampaign(campaign);
        LotroGameMediator mediator = new LotroGameMediator(gameId, live, SCENARIO_TIMER, false);
        GameRecorder.ScenarioRecording recording = _gameRecorder.recordScenarioGame(mediator, format, player.getName(), scenario.id, scenario.version);
        ScenarioRunLog.Watch watch = _scenarioRunLog == null ? null : _scenarioRunLog.watch(player, mediator, recording);
        mediator.addGameResultListener(
            new GameResultListener() {
                @Override
                public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                    _finishedGamesTime.put(gameId, new Date());
                }

                @Override
                public void gameCancelled() {
                    _finishedGamesTime.put(gameId, new Date());
                }
            });
        mediator.startGame();
        return new StartedScenario(mediator, watch);
    }

    /**
     * @return The scenario game a player is in the middle of, or null if they are not in one.
     */
    public LotroGameMediator getScenarioGame(String playerName) {
        _lock.readLock().lock();
        try {
            String gameId = _scenarioGames.get(playerName);
            LotroGameMediator mediator = gameId == null ? null : _runningGames.get(gameId);
            return mediator == null || mediator.isFinished() ? null : mediator;
        } finally {
            _lock.readLock().unlock();
        }
    }

    public LotroDeck getParticipantDeck(Player player, String deckName) {
        return _deckDao.getDeckForPlayer(player, deckName);
    }

    public LotroDeck createDeckWithValidate(String deckName, String contents, String targetFormat, String notes) {
        if (contents.contains("|")) {
            // New format
            int cnt = 0;
            for (char c : contents.toCharArray()) {
                if (c == '|')
                    cnt++;
            }

            if (cnt < 3 || cnt > 4)
                return null;

            return _deckDao.buildDeckFromContents(deckName, contents, targetFormat, notes);
        } else {
            // Old format
            List<String> cards = Arrays.asList(contents.split(","));
            if (cards.size() < 2)
                return null;

            return _deckDao.buildDeckFromContents(deckName, contents, targetFormat, notes);
        }
    }

    public LotroGameMediator getGameById(String gameId) {
        _lock.readLock().lock();
        try {
            return _runningGames.get(gameId);
        } finally {
            _lock.readLock().unlock();
        }
    }
}

