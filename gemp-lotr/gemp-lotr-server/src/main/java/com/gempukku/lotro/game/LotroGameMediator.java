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
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.hall.GameTimer;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.simulation.BotGameStateListener;
import com.gempukku.lotro.simulation.BotPlayer;
import com.gempukku.lotro.simulation.BotService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LotroGameMediator {
    private static final Logger LOG = LogManager.getLogger(LotroGameMediator.class);

    private final Map<String, GameCommunicationChannel> _communicationChannels = Collections.synchronizedMap(new HashMap<>());
    private final DefaultUserFeedback _userFeedback;
    private final DefaultLotroGame _lotroGame;
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

    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
    private final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();
    private int _channelNextIndex = 0;
    private volatile boolean _destroyed;

    public LotroGameMediator(String gameId, LotroFormat lotroFormat, LotroGameParticipant[] participants, LotroCardBlueprintLibrary library,
                             GameTimer gameTimer, boolean allowSpectators, boolean cancellable, boolean showInGameHall,
                             String tournamentName, MarkdownParser parser, BotService botService, boolean isSolo) {
        _gameId = gameId;
        _timeSettings = gameTimer;
        _allowSpectators = allowSpectators;
        _cancellable = cancellable;
        this._showInGameHall = showInGameHall;
        _soloGame = isSolo;
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

        if (_soloGame && participants.length == 1) {
            BotService.BotWithDeck bot = botService.getBotParticipant(lotroFormat);
            _botPlayer = bot.getBotPlayer();
            _botDeck = bot.getLotroDeck();
            _playerDecks.put(_botPlayer.getName(), _botDeck);
            _playerClocks.put(_botPlayer.getName(), 0);
            _playersPlaying.add(_botPlayer.getName());
        } else {
            _botPlayer = null;
            _botDeck = null;
        }

        _userFeedback = new DefaultUserFeedback();
        _lotroGame = new DefaultLotroGame(lotroFormat, _playerDecks, _userFeedback, library, _timeSettings.toString(),
                _allowSpectators, tournamentName);

        if (_botPlayer != null) {
            _lotroGame.addGameStateListener(_botPlayer.getName(), new BotGameStateListener(_botPlayer, this));
        }

        _userFeedback.setGame(_lotroGame);
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
        if (_playersPlaying.contains(playerId)) {
            _lotroGame.setPlayerAutoPassSettings(playerId, phases);
        }
    }

    public void sendMessageToPlayers(String message) {
        _lotroGame.getGameState().sendMessage(message);
    }

    public void addGameStateListener(String playerId, GameStateListener listener) {
        _lotroGame.addGameStateListener(playerId, listener);
    }

    public void removeGameStateListener(GameStateListener listener) {
        _lotroGame.removeGameStateListener(listener);
    }

    public void addGameResultListener(GameResultListener listener) {
        _lotroGame.addGameResultListener(listener);
    }

    public void removeGameResultListener(GameResultListener listener) {
        _lotroGame.removeGameResultListener(listener);
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

            if (card.getZone().isInPlay() || card.getZone() == Zone.HAND) {
                StringBuilder sb = new StringBuilder();

                if (card.getZone() == Zone.HAND)
                    sb.append("<b>Card is in hand - stats are only provisional</b><br><br>");
                else if (!Filters.hasActive(_lotroGame, card))
                    sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>");

                sb.append("<b>Affecting card:</b>");
                Collection<Modifier> modifiers = _lotroGame.getModifiersQuerying().getModifiersAffecting(_lotroGame, card);
                for (Modifier modifier : modifiers) {
                    PhysicalCard source = modifier.getSource();
                    if (source != null) {
                        sb.append("<br><b>")
                                .append(GameUtils.getCardLink(source))
                                .append(":</b> ")
                                .append(modifier.getText(_lotroGame, card));
                    }
                    else {
                        sb.append("<br><b><i>System</i>:</b> ")
                                .append(modifier.getText(_lotroGame, card));
                    }
                }
                if (modifiers.size() == 0) {
                    sb.append("<br><i>nothing</i>");
                }

                if (card.getZone().isInPlay() && card.getBlueprint().getCardType() == CardType.SITE) {
                    sb.append("<br><b>Owner:</b> ")
                            .append(card.getOwner());
                }

                Map<Token, Integer> map = _lotroGame.getGameState().getTokens(card);
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
                    int twilightCost = _lotroGame.getModifiersQuerying().getTwilightCostToPlay(_lotroGame, card, target, 0, false);
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
            _lotroGame.startGame();
            startClocksForUsersPendingDecision();
        } finally {
            _writeLock.unlock();
        }
    }

    public void cleanup() {
        _writeLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            Map<String, GameCommunicationChannel> channelsCopy = new HashMap<>(_communicationChannels);
            for (Map.Entry<String, GameCommunicationChannel> playerChannels : channelsCopy.entrySet()) {
                String playerId = playerChannels.getKey();
                // Channel is stale (user no longer connected to game, to save memory, we remove the channel
                // User can always reconnect and establish a new channel
                GameCommunicationChannel channel = playerChannels.getValue();
                if (currentTime > channel.getLastAccessed() + _timeSettings.maxSecondsPerDecision() * 1000L) {
                    _lotroGame.removeGameStateListener(channel);
                    _communicationChannels.remove(playerId);
                }
            }

            if (_lotroGame != null && _lotroGame.getWinnerPlayerId() == null) {
                for (Map.Entry<String, Long> playerDecision : new HashMap<>(_decisionQuerySentTimes).entrySet()) {
                    String player = playerDecision.getKey();
                    long decisionSent = playerDecision.getValue();
                    if (currentTime > decisionSent + _timeSettings.maxSecondsPerDecision() * 1000L) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _lotroGame.playerLost(player, "Player decision timed-out");
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
                    if (awaitingDecision != null) {
                        if (awaitingDecision.getAwaitingDecisionId() == decisionId && !_lotroGame.isFinished()) {
                            try {
                                _userFeedback.participantDecided(playerName);
                                awaitingDecision.decisionMade(answer);

                                // Decision successfully made, add the time to user clock
                                addTimeSpentOnDecisionToUserClock(playerName);

                                _lotroGame.carryOutPendingActionsUntilDecisionNeeded();
                                startClocksForUsersPendingDecision();

                            } catch (DecisionResultInvalidException decisionResultInvalidException) {
                                // Participant provided wrong answer - send a warning message, and ask again for the same decision
                                _lotroGame.getGameState().sendWarning(playerName, decisionResultInvalidException.getWarningMessage());
                                _userFeedback.sendAwaitingDecision(playerName, awaitingDecision);
                            } catch (RuntimeException runtimeException) {
                                LOG.error("Error processing game decision", runtimeException);
                                _lotroGame.cancelGame();
                            }
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
            if (awaitingDecision != null) {
                if (awaitingDecision.getAwaitingDecisionId() == decisionId && !_lotroGame.isFinished()) {
                    try {
                        _userFeedback.participantDecided(botName);
                        awaitingDecision.decisionMade(answer);

                        // Decision successfully made, add the time to user clock
                        addTimeSpentOnDecisionToUserClock(botName);

                        _lotroGame.carryOutPendingActionsUntilDecisionNeeded();
                        startClocksForUsersPendingDecision();

                    } catch (DecisionResultInvalidException decisionResultInvalidException) {
                        // Bot provided wrong answer - send a warning message, and ask again for the same decision
                        System.out.println(botName + " wrong answer - " + decisionResultInvalidException.getWarningMessage());
                        _userFeedback.sendAwaitingDecision(botName, awaitingDecision);
                    } catch (RuntimeException runtimeException) {
                        LOG.error("Error processing game decision", runtimeException);
                        _lotroGame.cancelGame();
                    }
                }
            }
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
            visitor.visitClock(secondsLeft);
        } finally {
            _readLock.unlock();
        }
    }

    public void signupUserForGame(Player player, ParticipantCommunicationVisitor visitor) throws PrivateInformationException {
        String playerName = player.getName();
        if (!player.hasType(Player.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        _readLock.lock();
        try {
            int number = _channelNextIndex;
            _channelNextIndex++;

            GameCommunicationChannel participantCommunicationChannel = new GameCommunicationChannel(playerName, number, _lotroGame.getFormat());
            _communicationChannels.put(playerName, participantCommunicationChannel);

            _lotroGame.addGameStateListener(playerName, participantCommunicationChannel);

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

            visitor.visitClock(secondsLeft);
        } finally {
            _readLock.unlock();
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
