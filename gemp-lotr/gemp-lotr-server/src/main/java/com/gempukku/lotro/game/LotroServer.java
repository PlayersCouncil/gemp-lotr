package com.gempukku.lotro.game;

import com.gempukku.lotro.AbstractServer;
import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.chat.ChatCommandErrorException;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.db.DeckDAO;
import com.gempukku.lotro.hall.GameSettings;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private final ReadWriteLock _lock = new ReentrantReadWriteLock();

    public LotroServer(DeckDAO deckDao, LotroCardBlueprintLibrary library, ChatServer chatServer, GameRecorder gameRecorder) {
        _deckDao = deckDao;
        _lotroCardBlueprintLibrary = library;
        _chatServer = chatServer;
        _gameRecorder = gameRecorder;
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

            for (LotroGameMediator lotroGameMediator : _runningGames.values())
                lotroGameMediator.cleanup();
        } finally {
            _lock.writeLock().unlock();
        }
    }

    private String getChatRoomName(String gameId) {
        return "Game" + gameId;
    }

    public LotroGameMediator createNewGame(String tournamentName, final LotroGameParticipant[] participants, GameSettings gameSettings) {
        _lock.writeLock().lock();
        try {
            if (participants.length < 2)
                throw new IllegalArgumentException("There has to be at least two players");
            final String gameId = String.valueOf(_nextGameId);

            if (gameSettings.competitive()) {
                Set<String> allowedUsers = new HashSet<>();
                for (LotroGameParticipant participant : participants)
                    allowedUsers.add(participant.getPlayerId());
                _chatServer.createPrivateChatRoom(getChatRoomName(gameId), false, allowedUsers, 30);
            } else
                _chatServer.createChatRoom(getChatRoomName(gameId), false, 30, false, null);

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

            LotroGameMediator lotroGameMediator = new LotroGameMediator(gameId, gameSettings.format(), participants, _lotroCardBlueprintLibrary,
                    gameSettings.timeSettings(),
                    spectate, !gameSettings.competitive(), gameSettings.hiddenGame(), tournamentName);
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
                        gameRecordingInProgress.finishRecording(participants[0].getPlayerId(), "Game cancelled due to error", participants[1].getPlayerId(), "Game cancelled due to error");
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

