package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.logic.PlayerOrder;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.GameStats;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState {
    private static final Logger _log = LogManager.getLogger(GameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 50;
    private PlayerOrder _playerOrder;

    private LotroFormat _format;

    private final Map<String, List<PhysicalCardImpl>> _adventureDecks = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _decks = new HashMap<>();
    private final Map<String, Integer> _deckShuffles = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _hands = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _discards = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _deadPiles = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _stacked = new HashMap<>();

    private final Map<String, List<PhysicalCardImpl>> _voids = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _voidsFromHand = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _removed = new HashMap<>();

    private final List<PhysicalCardImpl> _inPlay = new LinkedList<>();

    private final Map<Integer, PhysicalCardImpl> _allCards = new HashMap<>();

    /**
     * Optional callback fired when a card is dynamically created mid-game via
     * {@link #createPhysicalCard(String, LotroCardBlueprintLibrary, String)}.
     * Not fired during initial game setup. Used by test infrastructure to capture
     * references to cards spawned by effects like booster pack opening.
     */
    private java.util.function.Consumer<PhysicalCard> _cardCreationCallback;

    private String _currentPlayerId;
    private String _firstPlayerId;
    private Phase _currentPhase = Phase.PUT_RING_BEARER;
    private int _twilightPool;

    private int _moveCount;
    private boolean _fellowshipArriving;
    private int turnNumber;
    private boolean _fierceSkirmishes;
    private boolean _relentlessSkirmishes;
    private boolean _extraSkirmishes;

    private boolean _wearingRing;
    private boolean _consecutiveAction;

    private final Map<String, Integer> _playerPosition = new HashMap<>();
    private final Map<String, Integer> _playerThreats = new HashMap<>();

    private final Map<PhysicalCard, Map<Token, Integer>> _cardTokens = new HashMap<>();

    private final Map<String, PhysicalCard> _ringBearers = new HashMap<>();
    private final Map<String, PhysicalCard> _rings = new HashMap<>();
    private final Map<String, PhysicalCard> _maps = new HashMap<>();
    private final Map<String, List<PhysicalCard>> _metaSites = new HashMap<>();

    private final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();
    /** Scenario games: what the scenario is showing each player at the moment, by player and then by kind. */
    private final Map<String, Map<String, String>> _scenarioEvents = new HashMap<>();
    private boolean _finished = false;

    private LotroGame _lotroGame;

    private final List<Assignment> _assignments = new LinkedList<>();
    private int _threatsToCome;
    // The minions whose cost of assigning has been paid for the assignment being made (Q-0283)
    private final Set<PhysicalCard> _assignmentsPaidFor = new HashSet<>();
    // True while the Free Peoples player's own assigning is asked and made (Q-0283)
    private boolean _freePeoplesPlayerAssigning;
    private Map<PhysicalCard, Integer> _woundsToCome = Collections.emptyMap();
    // The events printed with several phases that are being played, each with the phase it is being played in
    private final Map<PhysicalCard, Timeword> _phaseOfEventBeingPlayed = new HashMap<>();
    private Skirmish _skirmish = null;
    private PhysicalCard _lastSkirmishCharacter;
    private int _lastSkirmishTurn = -1;

    private final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    // While events are held back (holdBackEvents): what would have been sent, in order, and each listener as the
    // senders see it then; null when nothing is held back
    private List<Runnable> _heldBackEvents;
    private List<GameStateListener> _heldBackListeners;
    private GameStats _heldBackGameStats;
    private final LinkedList<String> _lastMessages = new LinkedList<>();
    // Every message sent since startMessageRecord() was last called; null until somebody asks for a record
    private List<String> _messageRecord;

    private PreGameInfo _preGameInfo;

    private int _nextCardId = 0;

    private GameRandom _random = GameRandom.DEFAULT;
    private String _fixedFreePeoplePlayer;
    private PregameSetup _pregameSetup;
    private final Map<String, PhysicalCard> _cardLabels = new HashMap<>();

    private GameStats _mostRecentGameStats = null;
    private boolean _isInit = false;
    private Map<String, LotroDeck> _lotroDecks;

    public void setGame(LotroGame game) {
        _lotroGame = game;
    }

    private boolean isHandRevealed(String playerId) {
        return _lotroGame != null && _lotroGame.getModifiersQuerying().isHandRevealed(_lotroGame, playerId);
    }

    private int nextCardId() {
        return _nextCardId++;
    }

    //This happens before the bidding, so it has to be done separately from init
    public void initPreGame(PreGameInfo preGameInfo, Map<String, LotroDeck> decks,
            Map<String, List<String>> metaSiteBlueprintIds, LotroCardBlueprintLibrary library, LotroGame game) {
        _preGameInfo = preGameInfo;
        _lotroDecks = decks;
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializePregameBoard(preGameInfo);
        }

        // Create meta-site cards early so their modifiers are active during bidding
        if (metaSiteBlueprintIds != null) {
            for (var entry : metaSiteBlueprintIds.entrySet()) {
                String playerId = entry.getKey();
                List<String> blueprintIds = entry.getValue();
                if (blueprintIds != null && !blueprintIds.isEmpty()) {
                    List<PhysicalCard> metaSiteCards = new ArrayList<>();
                    for (String blueprintId : blueprintIds) {
                        try {
                            var card = createPhysicalCardImpl(playerId, library, blueprintId);
                            card.startAffectingGame(game);
                            metaSiteCards.add(card);
                        } catch (CardNotFoundException exp) {
                            throw new RuntimeException("Unable to create meta-site card: " + blueprintId);
                        }
                    }
                    _metaSites.put(playerId, metaSiteCards);
                }
            }
        }
    }

    public boolean isInit() {
        return _isInit;
    }

    public void init(PlayerOrder playerOrder, String firstPlayer, Map<String, List<String>> cards,
            Map<String, String> ringBearers, Map<String, String> rings, Map<String, String> maps,
            Map<String, List<String>> metaSiteBlueprintIds,
            LotroCardBlueprintLibrary library, LotroFormat format) {
        _isInit = true;
        _playerOrder = playerOrder;
        _currentPlayerId = firstPlayer;
        _firstPlayerId = firstPlayer;
        _format = format;

        for (Map.Entry<String, List<String>> stringListEntry : cards.entrySet()) {
            String playerId = stringListEntry.getKey();
            List<String> decks = stringListEntry.getValue();

            _adventureDecks.put(playerId, new LinkedList<>());
            _decks.put(playerId, new LinkedList<>());
            _hands.put(playerId, new LinkedList<>());
            _voids.put(playerId, new LinkedList<>());
            _voidsFromHand.put(playerId, new LinkedList<>());
            _removed.put(playerId, new LinkedList<>());
            _discards.put(playerId, new LinkedList<>());
            _deadPiles.put(playerId, new LinkedList<>());
            _stacked.put(playerId, new LinkedList<>());

            addPlayerCards(playerId, decks, library);
            try {
                _ringBearers.put(playerId, createPhysicalCardImpl(playerId, library, ringBearers.get(playerId)));
                String ringBlueprintId = rings.get(playerId);
                if (ringBlueprintId != null)
                    _rings.put(playerId, createPhysicalCardImpl(playerId, library, ringBlueprintId));

                if(format.usesMaps()) {
                    _maps.put(playerId, createPhysicalCardImpl(playerId, library, maps.get(playerId)));
                }

            } catch (CardNotFoundException exp) {
                throw new RuntimeException("Unable to create game, due to either ring-bearer or ring being invalid cards");
            }
        }

        for (String playerId : playerOrder.getAllPlayers()) {
            _playerThreats.put(playerId, 0);
        }

        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard(playerOrder.getAllPlayers(), format.discardPileIsPublic());
        }

        //This needs done after the Player Order initialization has been issued, or else the player
        // adventure deck areas don't exist.
        for (String playerId : playerOrder.getAllPlayers()) {
            for(var site : getAdventureDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(site);
                }
            }
        }
    }

    public void finish() {
        _finished = true;
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.endGame();
        }

        if(_playerOrder == null || _playerOrder.getAllPlayers() == null)
            return;

        for (String playerId : _playerOrder.getAllPlayers()) {
            for(var card : getDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(card, true, false);
                }
            }
        }
    }

    private void addPlayerCards(String playerId, List<String> cards, LotroCardBlueprintLibrary library) {
        for (String blueprintId : cards) {
            try {
                PhysicalCardImpl physicalCard = createPhysicalCardImpl(playerId, library, blueprintId);
                if (physicalCard.getBlueprint().getCardType() == CardType.SITE) {
                    physicalCard.setZone(Zone.ADVENTURE_DECK);
                    _adventureDecks.get(playerId).add(physicalCard);
                } else {
                    physicalCard.setZone(Zone.DECK);
                    _decks.get(playerId).add(physicalCard);
                }
            } catch (CardNotFoundException exp) {
                // Ignore the card
            }
        }
    }

    public void setCardCreationCallback(java.util.function.Consumer<PhysicalCard> callback) {
        _cardCreationCallback = callback;
    }

    public PhysicalCard createPhysicalCard(String ownerPlayerId, LotroCardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        PhysicalCard card = createPhysicalCardImpl(ownerPlayerId, library, blueprintId);
        if (_cardCreationCallback != null) {
            _cardCreationCallback.accept(card);
        }
        return card;
    }

    private PhysicalCardImpl createPhysicalCardImpl(String playerId, LotroCardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        LotroCardBlueprint card = library.getLotroCardBlueprint(blueprintId);

        int cardId = nextCardId();
        PhysicalCardImpl result = new PhysicalCardImpl(cardId, blueprintId, playerId, card);

        _allCards.put(cardId, result);

        return result;
    }

    public boolean isConsecutiveAction() {
        return _consecutiveAction;
    }

    public void setConsecutiveAction(boolean consecutiveAction) {
        _consecutiveAction = consecutiveAction;
    }

    public void setWearingRing(boolean wearingRing) {
        _wearingRing = wearingRing;
    }

    public boolean isWearingRing() {
        return _wearingRing;
    }

    public PlayerOrder getPlayerOrder() {
        return _playerOrder;
    }

    public List<String> getPlayerNames() {
        return _preGameInfo.participants();
    }

    public void addGameStateListener(String playerId, GameStateListener gameStateListener, GameStats gameStats) {
        terminateOldListener(playerId);
        _gameStateListeners.add(gameStateListener);
        sendStateToPlayer(playerId, gameStateListener, gameStats);
        _mostRecentGameStats = gameStats;
    }

    private void terminateOldListener(String playerId) {
        for(var listener : new HashSet<>(_gameStateListeners)) {
            if(listener.isLiveConnection() && listener.getAssignedPlayerId().equals(playerId)) {
                _gameStateListeners.remove(listener);
            }
        }
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameStateListeners.remove(gameStateListener);
    }

    private Collection<GameStateListener> getAllGameStateListeners() {
        if (_heldBackEvents != null)
            return _heldBackListeners;
        return Collections.unmodifiableSet(_gameStateListeners);
    }

    /**
     * From now on nothing is sent to the players, nor to anything else listening to the game (the replay's
     * recording, a bot): each event is kept back instead, until {@link #releaseHeldBackEvents} sends what is kept
     * or {@link #dropHeldBackEvents} drops it.  For the rounds of a voluntary loop carried out unseen (CR 4.2
     * "loops"; Ketura on Q-0329: "disable transmission/recording of the game events while doing so, only sending
     * the final 1-2 actions with updated stats"; LoopRun).  The game log's lines are kept back with the rest, from
     * the record of messages too, and the game stats are not sent at all until the release, which sends them as
     * they then stand.  Nothing changes in the game itself.
     */
    public void holdBackEvents() {
        if (_heldBackEvents != null)
            return;
        _heldBackEvents = new ArrayList<>();
        _heldBackListeners = new ArrayList<>();
        for (GameStateListener listener : _gameStateListeners)
            _heldBackListeners.add(heldBack(listener));
    }

    /**
     * Drops what has been held back so far, which is then never sent; events go on being held back.  For when what
     * the players last saw is again how the game stands.
     */
    public void dropHeldBackEvents() {
        if (_heldBackEvents != null)
            _heldBackEvents.clear();
    }

    /**
     * Stops holding events back, sends what was held, in order, and then the game stats as they stand if any were
     * held back.  An event held back is worked out as it is sent: a card is described as it is now.
     */
    public void releaseHeldBackEvents() {
        if (_heldBackEvents == null)
            return;
        final List<Runnable> held = _heldBackEvents;
        final GameStats gameStats = _heldBackGameStats;
        _heldBackEvents = null;
        _heldBackListeners = null;
        _heldBackGameStats = null;
        for (Runnable event : held)
            event.run();
        if (gameStats != null)
            sendGameStats(gameStats);
    }

    // A listener as the senders see it while events are held back: what it is told is kept back for it, and what
    // is asked of it (whose it is, whether it is a person's) it answers at once.
    private GameStateListener heldBack(GameStateListener listener) {
        return (GameStateListener) java.lang.reflect.Proxy.newProxyInstance(GameStateListener.class.getClassLoader(),
                new Class<?>[]{GameStateListener.class},
                (proxy, method, args) -> {
                    if (method.getReturnType() != void.class)
                        return invoke(listener, method, args);
                    _heldBackEvents.add(() -> invoke(listener, method, args));
                    return null;
                });
    }

    private static Object invoke(GameStateListener listener, java.lang.reflect.Method method, Object[] args) {
        try {
            return method.invoke(listener, args);
        } catch (java.lang.reflect.InvocationTargetException exp) {
            if (exp.getCause() instanceof RuntimeException runtime)
                throw runtime;
            throw new IllegalStateException(exp.getCause());
        } catch (IllegalAccessException exp) {
            throw new IllegalStateException(exp);
        }
    }

    private String getPhaseString() {
        if (isFierceSkirmishes())
            return "Fierce " + _currentPhase.getHumanReadable();
        if (isExtraSkirmishes())
            return "Extra " + _currentPhase.getHumanReadable();
        return _currentPhase.getHumanReadable();
    }

    private void sendStateToPlayer(String playerId, GameStateListener listener, GameStats gameStats) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId, Collections.emptySet());
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());
            listener.setTwilight(_twilightPool);
            for (Map.Entry<String, Integer> stringIntegerEntry : _playerPosition.entrySet())
                listener.setPlayerPosition(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());

            Set<PhysicalCard> cardsLeftToSent = new LinkedHashSet<>(_inPlay);
            Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

            int cardsToSendAtLoopStart;
            do {
                cardsToSendAtLoopStart = cardsLeftToSent.size();
                Iterator<PhysicalCard> cardIterator = cardsLeftToSent.iterator();
                while (cardIterator.hasNext()) {
                    PhysicalCard physicalCard = cardIterator.next();
                    PhysicalCard attachedTo = physicalCard.getAttachedTo();
                    if (attachedTo == null || sentCardsFromPlay.contains(attachedTo)) {
                        listener.cardCreated(physicalCard);
                        sentCardsFromPlay.add(physicalCard);

                        cardIterator.remove();
                    }
                }
            } while (cardsToSendAtLoopStart != cardsLeftToSent.size() && !cardsLeftToSent.isEmpty());

            // Finally the stacked ones
            for (List<PhysicalCardImpl> physicalCards : _stacked.values())
                for (PhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            List<PhysicalCardImpl> hand = _hands.get(playerId);
            if (hand != null) {
                for (PhysicalCardImpl physicalCard : hand)
                    listener.cardCreated(physicalCard);
            }

            // Send revealed opponent hands
            for (Map.Entry<String, List<PhysicalCardImpl>> entry : _hands.entrySet()) {
                String handOwner = entry.getKey();
                if (!handOwner.equals(playerId) && isHandRevealed(handOwner)) {
                    for (PhysicalCardImpl physicalCard : entry.getValue())
                        listener.cardCreated(physicalCard, false, true);
                }
            }

            for (List<PhysicalCardImpl> physicalCards : _deadPiles.values())
                for (PhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            for (List<PhysicalCardImpl> physicalCards : _removed.values())
                for (PhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            for (List<PhysicalCardImpl> physicalCards : _discards.values())
                for (PhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            List<PhysicalCardImpl> adventureDeck = _adventureDecks.get(playerId);
            if (adventureDeck != null) {
                for (PhysicalCardImpl physicalCard : adventureDeck)
                    listener.cardCreated(physicalCard);
            }

            for (Assignment assignment : _assignments) {
                if(assignment instanceof NestedAssignment nested) {
                    for(var pending : nested.getPendingSubskirmishes()) {
                        if(!pending.getShadowCharacters().isEmpty()) {
                            listener.addPendingAssignment(pending.getFellowshipCharacter(), pending.getShadowCharacters().stream().findFirst().get());
                        }
                    }
                }
                else {
                    listener.addAssignment(assignment.getFellowshipCharacter(), assignment.getShadowCharacters());
                }
            }

            if (_skirmish != null)
                sendSkirmish(listener);

            for (Map.Entry<PhysicalCard, Map<Token, Integer>> physicalCardMapEntry : _cardTokens.entrySet()) {
                PhysicalCard card = physicalCardMapEntry.getKey();
                for (Map.Entry<Token, Integer> tokenIntegerEntry : physicalCardMapEntry.getValue().entrySet()) {
                    Integer count = tokenIntegerEntry.getValue();
                    if (count != null && count > 0)
                        listener.addTokens(card, tokenIntegerEntry.getKey(), count);
                }
            }

            listener.sendGameStats(gameStats);

            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId, getInactiveCards());
        }

        for (String lastMessage : _lastMessages)
            listener.sendMessage(lastMessage);

        if(_preGameInfo != null) {
            listener.initializePregameBoard(_preGameInfo);
        }

        for (String json : _scenarioEvents.getOrDefault(playerId, Map.of()).values())
            listener.scenarioEvent(playerId, json);

        // Someone joining or refreshing after the end must see the game as over, not be handed the decision that was
        // pending when it ended (a decision that timed out is never "finished" and would otherwise be re-sent)
        if (_finished) {
            listener.endGame();
        } else {
            final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
            if (awaitingDecision != null)
                listener.decisionRequired(playerId, awaitingDecision);
        }
    }

    /**
     * Drops the messages kept for players who connect later.  Anybody already connected has had them.
     */
    public void forgetMessages() {
        _lastMessages.clear();
    }

    /**
     * Starts a full record of the messages sent to the players from now on, dropping any record kept before.  The
     * last few messages are always kept for players who connect later; this is for a reader that needs every one
     * (the virtual table's reading of the game log).  Nothing is recorded until this is first called.
     */
    public void startMessageRecord() {
        _messageRecord = new ArrayList<>();
    }

    /**
     * @return Every message sent since startMessageRecord() was last called, oldest first, or null if it never was.
     */
    public List<String> getMessageRecord() {
        return _messageRecord == null ? null : Collections.unmodifiableList(_messageRecord);
    }

    public void sendMessage(String message) {
        if (_heldBackEvents != null) {
            _heldBackEvents.add(() -> sendMessage(message));
            return;
        }
        _lastMessages.add(message);
        if (_lastMessages.size() > LAST_MESSAGE_STORED_COUNT)
            _lastMessages.removeFirst();
        if (_messageRecord != null)
            _messageRecord.add(message);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendMessage(message);
    }

    public void playerDecisionStarted(String playerId, AwaitingDecision awaitingDecision) {
        _playerDecisions.put(playerId, awaitingDecision);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.decisionRequired(playerId, awaitingDecision);
    }

    public void playerDecisionFinished(String playerId) {
        _playerDecisions.remove(playerId);
    }

    /**
     * Shows a player something on a scenario's behalf, and keeps it so that it is shown again if the player
     * reconnects.  Each player has one thing of each kind on show at a time: a later one replaces an earlier one.
     * @param kind What sort of thing it is ("info", "step", "end").
     * @param json What the client is to be sent.
     */
    public void showScenarioEvent(String playerId, String kind, String json) {
        Map<String, String> events = _scenarioEvents.computeIfAbsent(playerId, x -> new LinkedHashMap<>());
        //Taken out first so that the newest is always the last one sent to somebody reconnecting
        events.remove(kind);
        events.put(kind, json);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.scenarioEvent(playerId, json);
    }

    /**
     * Stops showing a player something on reconnecting.  The player is not told: whatever replaced it on their
     * screen has already told them.
     */
    public void clearScenarioEvent(String playerId, String kind) {
        Map<String, String> events = _scenarioEvents.get(playerId);
        if (events != null)
            events.remove(kind);
    }

    public void transferCard(PhysicalCard card, PhysicalCard transferTo) {
        if (card.getZone() != Zone.ATTACHED)
            ((PhysicalCardImpl) card).setZone(Zone.ATTACHED);

        ((PhysicalCardImpl) card).attachTo((PhysicalCardImpl) transferTo);
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.cardMoved(card);
        }
    }

    public void transferCardAsMinion(PhysicalCard card) {
        if (card.getZone() != Zone.SHADOW_CHARACTERS)
            ((PhysicalCardImpl) card).setZone(Zone.SHADOW_CHARACTERS);

        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.cardMoved(card);
        }
    }

    /**
     * Lays a card that a character bore where that character was, when the character has left play and the card
     * has not gone with him: The One Ring, which is never discarded, when its bearer is killed (Q-0109, Q-0232).
     * The card does not leave play at any point.  It keeps its card id and its text stays in force, though with no
     * bearer for it to speak of; it lies in the zone the bearer was in, borne by nobody, and the clients are told
     * it has moved there.  If the bearer was the fellowship character of the skirmish being fought, the card is
     * also shown in his place in that skirmish (Skirmish.getCardLeftInFellowshipPlace), which gives the skirmish no
     * fellowship character: nothing skirmishes the card and no rule reads it as skirmishing.
     *
     * Called once the bearer is out of play (removeCardsFromZone), with what was read of him before he went.  The
     * card lies there until it is taken up: MakeRingBearerEffect removes it from its zone and attaches it to the
     * new Ring-bearer.  An assignment the bearer had to a skirmish not yet begun is gone with him, and the card is
     * given no part in it.
     * @param card The card left behind, still attached to the bearer who has gone.
     * @param bearersZone The zone the bearer was in while in play.
     * @param bearerWasInSkirmish Whether the bearer was the fellowship character of the skirmish being fought.
     */
    public void layCardWhereItsBearerWas(PhysicalCard card, Zone bearersZone, boolean bearerWasInSkirmish) {
        ((PhysicalCardImpl) card).attachTo(null);
        ((PhysicalCardImpl) card).setZone(bearersZone);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);

        if (bearerWasInSkirmish && _skirmish != null && _skirmish.getFellowshipCharacter() == null) {
            _skirmish.setCardLeftInFellowshipPlace(card);
            for (GameStateListener listener : getAllGameStateListeners())
                listener.addToSkirmish(card);
        }
    }

    public void takeControlOfCard(String playerId, LotroGame game, PhysicalCard card, Zone zone) {
        ((PhysicalCardImpl) card).setCardController(playerId);
        ((PhysicalCardImpl) card).setZone(zone);
        if (card.getBlueprint().getCardType() == CardType.SITE)
            startAffectingControlledSite(game, card);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void loseControlOfCard(PhysicalCard card, Zone zone) {
        if (card.getBlueprint().getCardType() == CardType.SITE)
            stopAffectingControlledSite(card);
        ((PhysicalCardImpl) card).setCardController(null);
        ((PhysicalCardImpl) card).setZone(zone);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void attachCard(LotroGame game, PhysicalCard card, PhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");

        ((PhysicalCardImpl) card).attachTo((PhysicalCardImpl) attachTo);
        addCardToZone(game, card, Zone.ATTACHED);
    }

    public void stackCard(LotroGame game, PhysicalCard card, PhysicalCard stackOn) throws InvalidParameterException {
        if(card == stackOn)
            throw new InvalidParameterException("Cannot stack card on itself!");

        ((PhysicalCardImpl) card).stackOn((PhysicalCardImpl) stackOn);
        addCardToZone(game, card, Zone.STACKED);
    }

    public void cardAffectsCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCards) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardAffectedByCard(playerPerforming, card, affectedCards);
    }

    public void eventPlayed(PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.eventPlayed(card);
    }

    /**
     * Comprehensive Rules 4.2, "phase action": "If a phase action can be played in multiple phases (for example,
     * "Maneuver or Skirmish:"), its action type is of the phase during which the action is taken."  So an event
     * printed with several phases is, while it is being played, an event of the phase it is played in and of no
     * other: Sword-wall ("Skirmish or Regroup") played in the regroup phase is a regroup event and no skirmish
     * event (F-0292).
     *
     * This is that one phase.  It is null for a card that is not being played (nor being priced for a play:
     * asIfBeingPlayed), wherever it lies, and for an event printed with one phase, with or without "Response":
     * such a card is what is printed on it.  Filters.timeword is the reader.
     */
    public Timeword getPhaseOfEventBeingPlayed(PhysicalCard card) {
        if (_phaseOfEventBeingPlayed.isEmpty())
            return null;
        return _phaseOfEventBeingPlayed.get(card);
    }

    /**
     * The play of an event begins (PlayEventAction).  From here to eventPlayFinished an event printed with
     * several phases, the current one among them, is an event of the current phase alone.
     */
    public void eventPlayStarted(PhysicalCard event) {
        final Timeword phase = getPhaseAnEventWouldBePlayedIn(event);
        if (phase != null)
            _phaseOfEventBeingPlayed.put(event, phase);
    }

    /**
     * The play of an event is over, whatever became of the card: it is again what is printed on it.
     */
    public void eventPlayFinished(PhysicalCard event) {
        _phaseOfEventBeingPlayed.remove(event);
    }

    /**
     * Answers a question about playing a card now as it will be answered while the card is being played: what an
     * event printed with several phases costs to play is what it costs as an event of this phase ("each skirmish
     * event you play is twilight cost -1" takes nothing off a "Maneuver or Skirmish" event in the maneuver
     * phase), before the play as well as during it.  Nothing else is changed, and nothing at all for any other
     * card.
     */
    public <T> T asIfBeingPlayed(PhysicalCard card, java.util.function.Supplier<T> question) {
        if (_phaseOfEventBeingPlayed.containsKey(card))
            return question.get();
        final Timeword phase = getPhaseAnEventWouldBePlayedIn(card);
        if (phase == null)
            return question.get();

        _phaseOfEventBeingPlayed.put(card, phase);
        try {
            return question.get();
        } finally {
            _phaseOfEventBeingPlayed.remove(card);
        }
    }

    // The current phase, if the card is an event printed with it and with at least one other phase; else null.
    private Timeword getPhaseAnEventWouldBePlayedIn(PhysicalCard card) {
        final LotroCardBlueprint blueprint = card.getBlueprint();
        if (blueprint.getCardType() != CardType.EVENT)
            return null;
        final Timeword current = Timeword.findByPhase(getCurrentPhase());
        if (current == null || !blueprint.hasTimeword(current))
            return null;
        for (Timeword other : Timeword.values()) {
            if (other != current && other.getPhase() != null && blueprint.hasTimeword(other))
                return current;
        }
        return null;
    }

    public void activatedCard(String playerPerforming, PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardActivated(playerPerforming, card);
    }

    public void setRingBearer(PhysicalCard card) {
        _ringBearers.put(card.getOwner(), card);
    }

    public PhysicalCard getRingBearer(String playerId) {
        return _ringBearers.get(playerId);
    }

    public PhysicalCard getRing(String playerId) {
        return _rings.get(playerId);
    }

    public PhysicalCard getMap(String playerId) {
        return _maps.get(playerId);
    }

    public List<PhysicalCard> getMetaSites(String playerId) {
        return _metaSites.getOrDefault(playerId, Collections.emptyList());
    }

    private List<PhysicalCardImpl> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DECK)
            return _decks.get(playerId);
        else if (zone == Zone.ADVENTURE_DECK)
            return _adventureDecks.get(playerId);
        else if (zone == Zone.DISCARD)
            return _discards.get(playerId);
        else if (zone == Zone.DEAD)
            return _deadPiles.get(playerId);
        else if (zone == Zone.HAND)
            return _hands.get(playerId);
        else if (zone == Zone.VOID)
            return _voids.get(playerId);
        else if (zone == Zone.VOID_FROM_HAND)
            return _voidsFromHand.get(playerId);
        else if (zone == Zone.REMOVED)
            return _removed.get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else
            return _inPlay;
    }

    public void removeCardsFromZone(String playerPerforming, Collection<PhysicalCard> cards) {
        removeCardsFromZone(playerPerforming, cards, false);
    }

    public void removeCardsFromZone(String playerPerforming, Collection<PhysicalCard> cards, boolean skipSkirmishReset) {
        for (PhysicalCard card : cards) {
            List<PhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), card.getZone());
            if (!zoneCards.contains(card))
                _log.error("Card was not found in the expected zone");
        }

        // A character leaving the skirmish with these cards is measured before any of them stops affecting the game
        if (!skipSkirmishReset) {
            for (PhysicalCard card : cards)
                noteStrengthIfSkirmishing(card);
        }

        for (PhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay())
                if (card.getBlueprint().getCardType() != CardType.SITE || (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && getCurrentSite() == card))
                    stopAffecting(card);

            if (zone == Zone.STACKED)
                stopAffectingStacked(card);
            else if (zone == Zone.DISCARD)
                stopAffectingInDiscard(card);

            var zoneCards = getZoneCards(card.getOwner(), zone);
            zoneCards.remove(card);

            if (zone == Zone.ATTACHED)
                ((PhysicalCardImpl) card).attachTo(null);

            if (zone == Zone.STACKED)
                ((PhysicalCardImpl) card).stackOn(null);

            if (!skipSkirmishReset) {
                removeFromAssignment(card);
                removeFromSkirmish(card, false);
            }

            removeAllTokens(card);
            //If this is reset, then there is no way for self-discounting effects (which are evaluated while in the void)
            // to have any sort of permanent effect once the card is in play.
            if(zone != Zone.VOID_FROM_HAND && zone != Zone.VOID)
                card.setWhileInZoneData(null);
        }

        boolean forceVisible = cards.stream().anyMatch(
                card -> card.getZone() == Zone.HAND && isHandRevealed(card.getOwner()));
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardsRemoved(playerPerforming, cards, forceVisible);

        for (PhysicalCard card : cards) {
            ((PhysicalCardImpl) card).setZone(null);
        }
    }

    public void addCardToZone(LotroGame game, PhysicalCard card, Zone zone) {
        addCardToZone(game, card, zone, true);
    }

    private void addCardToZone(LotroGame game, PhysicalCard card, Zone zone, boolean end) {
        addCardToZone(game, card, zone, end, null);
    }

    /**
     * @param cardIdInPlay the id the card is to have once in play, where it takes the place of another card and
     *                     so its id; null for a card coming into play in its own right, which is given a new one.
     */
    private void addCardToZone(LotroGame game, PhysicalCard card, Zone zone, boolean end, Integer cardIdInPlay) {
        if (zone == Zone.DISCARD && game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        //Hindered cards that are being discarded, banished, moved to a deck, or killed do not remain hindered
        if(card.isFlipped() && !zone.isInPlay()) {
            card.setFlipped(false);
        }

        if (zone.isInPlay()) {
            if (cardIdInPlay != null) {
                _allCards.remove(card.getCardId());
                reassignCardId(card, cardIdInPlay);
            }
            else {
                assignNewCardId(card);
            }
        }

        List<PhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), zone);
        if (end) {
            zoneCards.add((PhysicalCardImpl) card);
        }
        else {
            zoneCards.addFirst((PhysicalCardImpl) card);
        }

        if (card.getZone() != null) {
            _log.error("Card was in " + card.getZone() + " when tried to add to zone: " + zone);
        }

        ((PhysicalCardImpl) card).setZone(zone);

        if (zone == Zone.ADVENTURE_PATH) {
            for (GameStateListener listener : getAllGameStateListeners()) {
                listener.setSite(card);
            }

            ((PhysicalCardImpl) card).startAffectingGamePermanentSite(game);
        } else {
            boolean forceVisible = zone == Zone.HAND && isHandRevealed(card.getOwner());
            for (GameStateListener listener : getAllGameStateListeners()) {
                listener.cardCreated(card, false, forceVisible);
            }
        }

        if (_currentPhase.isCardsAffectGame()) {
            if (zone.isInPlay()) {
                if (card.getBlueprint().getCardType() == CardType.SITE) {
                    if (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && getCurrentSite() == card)
                        startAffecting(game, card);
                }
                // A card that is not active as it comes into play has no text in force (CR 4.2 "active"), so its
                // text is not started here: startAffectingCardsForCurrentPlayer starts it, once, when a turn
                // begins in which it is active.  Asked as that method asks it.  No play puts such a card on the
                // table; a card laid directly for the player whose turn it is not is one (G-0055), and started
                // here as well it was in force out of turn and counted twice in its owner's.
                else if (isCardInPlayActive(card)) {
                    startAffecting(game, card);
                }
            }

            if (zone == Zone.STACKED)
                startAffectingStacked(game, card);
            else if (zone == Zone.DISCARD)
                startAffectingInDiscard(game, card);
        }
    }

    private void assignNewCardId(PhysicalCard card) {
        _allCards.remove(card.getCardId());
        int newCardId = nextCardId();
        card.setCardId(newCardId);
        _allCards.put(newCardId, (PhysicalCardImpl)card);
    }

    public void reassignCardId(PhysicalCard card, int newCardId) {
        card.setCardId(newCardId);
        _allCards.put(newCardId, (PhysicalCardImpl)card);
    }

    private void removeAllTokens(PhysicalCard card) {
        Map<Token, Integer> map = _cardTokens.get(card);
        if (map != null) {
            for (Map.Entry<Token, Integer> tokenIntegerEntry : map.entrySet())
                if (tokenIntegerEntry.getValue() > 0)
                    for (GameStateListener listener : getAllGameStateListeners())
                        listener.removeTokens(card, tokenIntegerEntry.getKey(), tokenIntegerEntry.getValue());

            map.clear();
        }
    }

    /**
     * Replaces a character on table with another character.  The new card takes the old card's place entire: its
     * zone, its card id (so that whatever was tracking the old character goes on tracking the new one), its tokens,
     * its assignment, its skirmish, and the cards it bore or had stacked on it, which stay in play as they are and
     * are simply handed over.  The old card is left in the void under a new id; the calling function takes it out
     * of there (removeCardsFromZone) and puts it wherever it is to go.
     * @param oldCard the old card
     * @param newCard the new card
     */
    public void replaceCharacterOnTable(LotroGame game,  PhysicalCard oldCard, PhysicalCard newCard) {
        int oldCardId = oldCard.getCardId();

        //Skirmish assignments will get bulldozed when we remove the old card from its zone, so we back them up first,
        // along with everything else that removing the old card would lose track of.
        var assignment = findAssignment(oldCard);
        var skirmish = findSkirmish(oldCard);
        var tokens = new HashMap<>(getTokens(oldCard));
        var zone = oldCard.getZone();
        var attachedTo = oldCard.getAttachedTo();
        var attachedCardList = getAttachedCards(oldCard);
        var stackedCardList = getStackedCards(oldCard);

        // Remove new card from zone
        removeCardsFromZone(null, Collections.singleton(newCard));
        newCard.copyCardStats(oldCard);

        // Remove old card from zone
        removeCardsFromZone(null, Collections.singleton(oldCard), true);
        assignNewCardId(oldCard);
        addCardToZone(game, oldCard, Zone.VOID);

        // Put the new card where the old card was, once, under the card ID of the replaced card
        if (attachedTo != null) {
            ((PhysicalCardImpl) newCard).attachTo((PhysicalCardImpl) attachedTo);
        }
        addCardToZone(game, newCard, zone, true, oldCardId);

        for(var pair : tokens.entrySet()) {
            addTokens(newCard, pair.getKey(), pair.getValue());
        }

        if(assignment != null) {
            replaceCardInAssignment(oldCard, newCard);
        }

        if(skirmish != null) {
            _skirmish.replaceCharacterInSkirmish(oldCard, newCard);
            restartSkirmish(_skirmish);
        }

        // Hand attached cards to the new card.  They never leave play: they keep their card IDs and their own
        // tokens, and their modifiers are stopped and started once so that they answer for the new bearer.
        for (PhysicalCard attachedCard : attachedCardList) {
            transferCard(attachedCard, newCard);
            reapplyAffectingForCard(game, attachedCard);
        }

        // Hand stacked cards to the new card
        for (PhysicalCard stackedCard : stackedCardList) {
            ((PhysicalCardImpl) stackedCard).stackOn((PhysicalCardImpl) newCard);
            for (GameStateListener listener : getAllGameStateListeners())
                listener.cardMoved(stackedCard);
        }
    }

    public void replaceInSkirmish(PhysicalCard card) {
        var replaced = _skirmish.getFellowshipCharacter();
        _skirmish.setFellowshipCharacter(card);
        _skirmish.noteReplacement(replaced, card);
        for (GameStateListener gameStateListener : getAllGameStateListeners()) {
            gameStateListener.finishSkirmish();
            sendSkirmish(gameStateListener);
        }
    }

    public void breakSkirmishIntoSubSkirmishes(PhysicalCard fp, PhysicalCard firstMinion) {
        var minions = new HashSet<>(_skirmish.getShadowCharacters());
        if(!minions.contains(firstMinion))
            return;

        var pendingMinions = new HashSet<PhysicalCard>();

        //We can't use any of the premade functions for any of this stuff because they will result in cancelling
        // the skirmish or assignments.
        for(var minion : minions) {
            if(minion == firstMinion)
                continue;

            if (_skirmish.getShadowCharacters().remove(minion)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.removeFromSkirmish(minion);
                }
            }

            pendingMinions.add(minion);
        }

        var nested = new NestedAssignment(fp, pendingMinions);

        _assignments.add(nested);

        for (GameStateListener listener : getAllGameStateListeners()) {
            for(var pending : nested.getPendingSubskirmishes()) {
                listener.addPendingAssignment(pending.getFellowshipCharacter(), pending.getShadowCharacters().stream().findFirst().get());
            }
        }
    }

    public void replaceInSkirmishMinion(PhysicalCard card, PhysicalCard removedMinion) {
        //The removed minion is pulled out of the current skirmish
        removeFromSkirmish(removedMinion);
        //If the replacing minion is in a pending assignment, then we remove them from it
        removeFromAssignment(card);

        if(_skirmish.getShadowCharacters().contains(card)) {
            removeFromSkirmish(card);
        }
        _skirmish.getShadowCharacters().add(card);
        _skirmish.noteReplacement(removedMinion, card);
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.finishSkirmish();
            sendSkirmish(listener);
        }
    }

    public void removeFromSkirmish(PhysicalCard card) {
        removeFromSkirmish(card, true);
    }
    private void removeFromSkirmish(PhysicalCard card, boolean notify) {
        if (_skirmish == null)
            return;

        noteStrengthIfSkirmishing(card);

        if (_skirmish.getFellowshipCharacter() == card) {
            _skirmish.setFellowshipCharacter(null);
            _skirmish.addRemovedFromSkirmish(card);
            if (notify) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.removeFromSkirmish(card);
                }
            }
        }
        if (_skirmish.getShadowCharacters().remove(card)) {
            _skirmish.addRemovedFromSkirmish(card);
            if (notify) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.removeFromSkirmish(card);
                }
            }
        }
        // A card that lay where the fellowship character was is taken up from there.  It was never in the skirmish
        // for any rule, so it is not among those removed from it.
        if (_skirmish.getCardLeftInFellowshipPlace() == card) {
            _skirmish.setCardLeftInFellowshipPlace(null);
            if (notify) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.removeFromSkirmish(card);
                }
            }
        }
    }

    public void removeFromAssignment(PhysicalCard card) {
        if( _assignments.isEmpty())
            return;

        for (var assignment : new LinkedList<>(_assignments)) {
            //Nested assignments handle their own removal
            if(assignment instanceof NestedAssignment nested) {
                if(nested.getFellowshipCharacter() == card) {
                    for(var subAssignment : nested.getPendingSubskirmishes()) {
                        removeAssignment(subAssignment);
                        removeSubAssignment(subAssignment);
                    }
                    removeAssignment(assignment);
                }
                continue;
            }

            if (assignment.getFellowshipCharacter() == card) {
                removeAssignment(assignment);
            }
            if (assignment.getShadowCharacters().remove(card)) {
                if (assignment.getShadowCharacters().isEmpty()) {
                    removeAssignment(assignment);
                }
                else {
                    refreshAssignment(assignment);
                }
            }
        }
    }

    public void assignToSkirmishes(PhysicalCard fp, Set<PhysicalCard> minions) {
        removeFromSkirmish(fp);
        for (PhysicalCard minion : minions) {
            removeFromSkirmish(minion);

            for (Assignment assignment : new LinkedList<>(_assignments)) {
                if (assignment.getShadowCharacters().remove(minion)) {
                    if(assignment instanceof NestedAssignment nested) {
                        if(nested.getPendingSubskirmishes().isEmpty()) {
                            removeAssignment(nested);
                        }
                    }
                    else if (assignment.getShadowCharacters().isEmpty()) {
                        removeAssignment(assignment);
                    }
                }
            }
        }

        Assignment assignment = findFreepsAssignment(fp);
        if (assignment != null)
            assignment.getShadowCharacters().addAll(minions);
        else
            _assignments.add(new Assignment(fp, new HashSet<>(minions)));

        for (GameStateListener listener : getAllGameStateListeners())
            listener.addAssignment(fp, minions);
    }

    public void refreshAssignment(Assignment assignment) {
        if(!(assignment instanceof NestedAssignment)) {
            removeAssignment(assignment);
        }
        assignToSkirmishes(assignment.getFellowshipCharacter(), assignment.getShadowCharacters());
    }

    public void removeAssignment(Assignment assignment) {
        _assignments.remove(assignment);
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.removeAssignment(assignment.getFellowshipCharacter());
        }
    }

    //This should only be used to communicate that a split-up subskirmish assignment is now active
    public void removeSubAssignment(Assignment assignment) {
        //As the parent NestedListener is still in the assignment group, we won't remove it
        //from local tracking; instead just inform the client so they don't display the character wrong
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.removePendingAssignment(assignment.getFellowshipCharacter(), assignment.getShadowCharacters().stream().findFirst().get());
        }
    }

    public List<Assignment> getAssignments() {
        return _assignments;
    }

    private Skirmish findSkirmish(PhysicalCard card) {
        if(_skirmish == null)
            return null;

        if(_skirmish.getFellowshipCharacter() == card)
            return _skirmish.copySkirmish();

        if(_skirmish.getShadowCharacters().contains(card))
            return _skirmish.copySkirmish();

        return null;
    }

    private Assignment replaceCardInAssignment(PhysicalCard oldCard, PhysicalCard newCard) {
        for(var assignment : new ArrayList<>(_assignments)) {
            var newAssignment = assignment.replaceCharacter(oldCard, newCard);
            if(newAssignment != null) {
                removeAssignment(assignment);
                refreshAssignment(newAssignment);
                return newAssignment;
            }
        }

        return null;
    }


    private Assignment findAssignment(PhysicalCard card) {
        var fp = findFreepsAssignment(card);
        if(fp != null)
            return fp;

        return findShadowAssignment(card);
    }

    private Assignment findFreepsAssignment(PhysicalCard fp) {
        for (Assignment assignment : _assignments)
            if (assignment.getFellowshipCharacter() == fp)
                return assignment;
        return null;
    }

    private Assignment findShadowAssignment(PhysicalCard sh) {
        for (Assignment assignment : _assignments)
            if (assignment.getShadowCharacters().contains(sh))
                return assignment;
        return null;
    }

    public void startSkirmish(PhysicalCard fellowshipCharacter, Set<PhysicalCard> shadowCharacters) {
        startSkirmish(new Skirmish(fellowshipCharacter, new HashSet<>(shadowCharacters)));
    }

    private void startSkirmish(Skirmish skirmish) {
        _skirmish = skirmish;
        for (GameStateListener listener : getAllGameStateListeners())
            sendSkirmish(listener);
    }

    public void restartSkirmish(Skirmish skirmish) {
        _skirmish = skirmish;
        for (GameStateListener listener : getAllGameStateListeners())
            sendSkirmish(listener);
    }

    /**
     * Tells a client of the skirmish as it stands: who fights in it, and the card, if any, that lies where its
     * fellowship character was (Skirmish.getCardLeftInFellowshipPlace).
     */
    private void sendSkirmish(GameStateListener listener) {
        listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
        if (_skirmish.getCardLeftInFellowshipPlace() != null)
            listener.addToSkirmish(_skirmish.getCardLeftInFellowshipPlace());
    }

    public Skirmish getSkirmish() {
        return _skirmish;
    }

    /** The Free Peoples character of the last skirmish this turn that has been fought to its end; null if none has. */
    public PhysicalCard getLastSkirmishCharacter() {
        return _lastSkirmishTurn == getTurnNumber() ? _lastSkirmishCharacter : null;
    }

    public void finishSkirmish() {
        if (_skirmish != null) {
            _lastSkirmishCharacter = _skirmish.getFellowshipCharacter();
            _lastSkirmishTurn = getTurnNumber();
            _skirmish = null;
            for (GameStateListener listener : getAllGameStateListeners())
                listener.finishSkirmish();
        }
    }

    public void shuffleCardsIntoDeck(Collection<? extends PhysicalCard> cards, String playerId) {
        List<PhysicalCardImpl> zoneCards = _decks.get(playerId);

        for (PhysicalCard card : cards) {
            zoneCards.add((PhysicalCardImpl) card);

            ((PhysicalCardImpl) card).setZone(Zone.DECK);
            //A hindered card moved to a draw deck does not remain hindered (as in addCardToZone)
            card.setFlipped(false);
        }

        shuffleDeck(playerId);
    }

    public void putCardOnBottomOfDeck(PhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, true);
    }

    public void putCardOnTopOfDeck(PhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, false);
    }
    public boolean iterateActiveCards(PhysicalCardVisitor visitor) {
        return iterateActiveCards(visitor, SpotOverride.NONE, _inPlay);
    }
    public boolean iterateActiveCards(PhysicalCardVisitor physicalCardVisitor, Map<InactiveReason, Boolean> spotOverrides) {
        var cards = _inPlay;
        if(spotOverrides.get(InactiveReason.STACKED) != null && spotOverrides.get(InactiveReason.STACKED)) {
            cards = Stream.concat(
                    _inPlay.stream(),
                    _stacked.values().stream().flatMap(List::stream)
            ).toList();
        }
        return iterateActiveCards(physicalCardVisitor, spotOverrides, cards);
    }

    public boolean iterateActiveCards(PhysicalCardVisitor physicalCardVisitor, Map<InactiveReason, Boolean> spotOverrides, Iterable<? extends PhysicalCard> cards) {

        //These represent all the ways a card might be inactive and thus excluded from iteration.
        // However, sometimes card effects wish to pierce the veil of inactivity for one reason or another; such cards
        // need to manually override the default behavior.
        boolean includeOutOfTurn = false;
        boolean includeAttachedToInactive = false;
        boolean includeStacked = false;
        boolean includeHindered = false;

        // If any spotOverrides were supplied, then apply them
        if (spotOverrides != null) {
            if (spotOverrides.get(InactiveReason.OUT_OF_TURN) != null) {
                includeOutOfTurn = spotOverrides.get(InactiveReason.OUT_OF_TURN);
            }
            if (spotOverrides.get(InactiveReason.ATTACHED_TO_INACTIVE) != null) {
                includeAttachedToInactive = spotOverrides.get(InactiveReason.ATTACHED_TO_INACTIVE);
            }
            if (spotOverrides.get(InactiveReason.STACKED) != null) {
                includeStacked = spotOverrides.get(InactiveReason.STACKED);
            }
            if (spotOverrides.get(InactiveReason.HINDERED) != null) {
                includeHindered = spotOverrides.get(InactiveReason.HINDERED);
            }
        }

        for (var card : cards) {
            // Check if the card can be spotted as "active" and include it if it can be.
            if (isCardInPlayActive(card, includeOutOfTurn, includeAttachedToInactive, includeStacked, includeHindered)) {
                if (physicalCardVisitor.visitPhysicalCard(card)) {
                    return true;
                }
            }
        }

        return false;
    }

    public PhysicalCard findCardById(int cardId) {
        return _allCards.get(cardId);
    }

    public Iterable<? extends PhysicalCard> getAllCards() {
        return Collections.unmodifiableCollection(_allCards.values());
    }

    public String getBlueprintId(int cardId) {
        for (PhysicalCardImpl physicalCard : _allCards.values()) {
            if (physicalCard.getCardId() == cardId) {
                return physicalCard.getBlueprintId();
            }
        }
        throw new IllegalArgumentException("Card not found");
    }

    public LotroDeck getLotroDeck(String playerId) {
        return _lotroDecks.get(playerId);
    }

    public List<? extends PhysicalCard> getHand(String playerId) {
        return Collections.unmodifiableList(_hands.get(playerId));
    }

    public List<? extends PhysicalCard> getVoidFromHand(String playerId) {
        return Collections.unmodifiableList(_voidsFromHand.get(playerId));
    }

    public List<? extends PhysicalCard> getRemoved(String playerId) {
        return Collections.unmodifiableList(_removed.get(playerId));
    }

    public List<? extends PhysicalCard> getDeck(String playerId) {
        return Collections.unmodifiableList(_decks.get(playerId));
    }

    public List<? extends PhysicalCard> getDiscard(String playerId) {
        return Collections.unmodifiableList(_discards.get(playerId));
    }

    public List<? extends PhysicalCard> getDeadPile(String playerId) {
        return Collections.unmodifiableList(_deadPiles.get(playerId));
    }

    public List<? extends PhysicalCard> getAdventureDeck(String playerId) {
        return Collections.unmodifiableList(_adventureDecks.get(playerId));
    }

    /**
     * Returns whether a given player is currently using Shadows sites or not.  This is used by Race to Mount Doom
     * which has modifiers which can cause Shadows adventure decks to be mixed with Movie Block adventure decks.
     * Note that this does not handle mixing of pre-Shadows and post-Shadows sites within the same adventure deck.
     * @param playerId The player to look up
     * @return true if a Shadows site is found in the adventure deck, else false
     */
    public boolean usesUnorderedSites(String playerId) {
        var deck = _adventureDecks.get(playerId);
        if (deck == null || deck.isEmpty())
            return false;
        return deck.getFirst().getBlueprint().getSiteNumber() == 0;
    }

    public List<? extends PhysicalCard> getInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }

    public List<? extends PhysicalCard> getStacked(String playerId) {
        return Collections.unmodifiableList(_stacked.get(playerId));
    }

    public String getCurrentPlayerId() {
        return _currentPlayerId;
    }

    public String getFirstPlayerId() {
        return _firstPlayerId;
    }

    public int getCurrentSiteNumber() {
        return _playerPosition.getOrDefault(_currentPlayerId, 0);
    }

    public void setPlayerPosition(String playerId, int i) {
        _playerPosition.put(playerId, i);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerPosition(playerId, i);
    }

    public void addThreats(String playerId, int count) {
        _playerThreats.put(playerId, _playerThreats.get(playerId) + count);
    }

    public void removeThreats(String playerId, int count) {
        final int oldThreats = _playerThreats.get(playerId);
        count = Math.min(count, oldThreats);
        _playerThreats.put(playerId, oldThreats - count);
    }

    public void movePlayerToNextSite(LotroGame game) {
        final String currentPlayerId = getCurrentPlayerId();
        final int oldPlayerPosition = getPlayerPosition(currentPlayerId);
        stopAffecting(getCurrentSite());
        setPlayerPosition(currentPlayerId, oldPlayerPosition + 1);
        increaseMoveCount();
        // What is limited "per site" starts afresh at the site moved to.
        if (game.getModifiersEnvironment() instanceof ModifiersLogic modifiersLogic)
            modifiersLogic.signalFellowshipMoved();
        startAffecting(game, getCurrentSite());
    }

    /**
     * True while a move is being finished: from the moment the player marker has been put on the next site until
     * the twilight of that move has been added.  The rules have the fellowship at the new site only then
     * ("After you have completed any moving actions and have added twilight tokens to the twilight pool, the
     * fellowship has moved to the new site"), so a card's standing check on where the fellowship is ("discard
     * this possession when at an underground site") is not looked at in between.  Everything else reads the
     * new site from the moment the marker moves, "when the fellowship moves to" triggers included.
     */
    public boolean isFellowshipArriving() {
        return _fellowshipArriving;
    }

    /** For the move itself (MovementGameProcess): see isFellowshipArriving. */
    public void setFellowshipArriving(boolean arriving) {
        _fellowshipArriving = arriving;
    }

    public int getPlayerPosition(String playerId) {
        return _playerPosition.getOrDefault(playerId, 0);
    }

    public Map<Token, Integer> getTokens(PhysicalCard card) {
        Map<Token, Integer> map = _cardTokens.get(card);
        if (map == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * The tokens a card has, each kind with its count, leaving out a kind whose count has come down to 0 (a wound
     * healed, the last culture token removed): what the card info lists (engine pass 59).  getTokens keeps such a
     * kind at 0; no filter tells that 0 from a kind never held (Filters.hasAnyCultureTokens at 0 is true of every
     * card since engine pass 60, F-0578), and only the listing drops it.
     * @param card The card.
     * @return The tokens on it, in no order; empty when it has none.
     */
    public Map<Token, Integer> getTokensHeld(PhysicalCard card) {
        Map<Token, Integer> held = new HashMap<>();
        for (Map.Entry<Token, Integer> entry : getTokens(card).entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0)
                held.put(entry.getKey(), entry.getValue());
        }
        return held;
    }

    public int getTokenCount(PhysicalCard physicalCard, Token token) {
        Map<Token, Integer> tokens = _cardTokens.get(physicalCard);
        if (tokens == null)
            return 0;
        Integer count = tokens.get(token);
        if (count == null)
            return 0;
        return count;
    }

    public List<PhysicalCard> getAttachedCards(PhysicalCard card) {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCardImpl physicalCard : _inPlay) {
            if (physicalCard.getAttachedTo() != null && physicalCard.getAttachedTo() == card)
                result.add(physicalCard);
        }
        return result;
    }

    public List<PhysicalCard> getStackedCards(PhysicalCard card) {
        List<PhysicalCard> result = new LinkedList<>();
        for (List<PhysicalCardImpl> physicalCardList : _stacked.values()) {
            for (PhysicalCardImpl physicalCard : physicalCardList) {
                if (physicalCard.getStackedOn() == card)
                    result.add(physicalCard);
            }
        }
        return result;
    }

    public int getWounds(PhysicalCard physicalCard) {
        final int wounds = getTokenCount(physicalCard, Token.WOUND);
        if (_woundsToCome.isEmpty())
            return wounds;
        return wounds + _woundsToCome.getOrDefault(physicalCard, 0);
    }

    /**
     * Answers a question as it would be answered once the given wounds had been placed: the wounds an action
     * will place as its own costs (exerting its own card twice), while a later cost of that action, or a card
     * the action would then play, is asked whether it can be paid or played.  They are counted by getWounds,
     * and so by vitality and by everything that asks whether a character can exert or is wounded, and change
     * nothing else: no token is placed and nobody is told.  Nothing is to come except inside this call.
     */
    public <T> T withWoundsToCome(Map<PhysicalCard, Integer> wounds, java.util.function.Supplier<T> question) {
        if (wounds == null || wounds.isEmpty())
            return question.get();

        final Map<PhysicalCard, Integer> before = _woundsToCome;
        final Map<PhysicalCard, Integer> together = new HashMap<>(before);
        for (Map.Entry<PhysicalCard, Integer> entry : wounds.entrySet())
            together.merge(entry.getKey(), Math.max(0, entry.getValue()), Integer::sum);
        _woundsToCome = together;
        try {
            return question.get();
        } finally {
            _woundsToCome = before;
        }
    }

    public void addBurdens(int burdens) {
        addTokens(_ringBearers.get(getCurrentPlayerId()), Token.BURDEN, Math.max(0, burdens));
    }

    public int getBurdens() {
        return getTokenCount(_ringBearers.get(getCurrentPlayerId()), Token.BURDEN);
    }

    public int getPlayerBurdens(String playerId) {
        return getTokenCount(_ringBearers.get(playerId), Token.BURDEN);
    }

    public int getPlayerThreats(String playerId) {
        return _playerThreats.get(playerId);
    }

    public int getThreats() {
        return _playerThreats.get(getCurrentPlayerId());
    }

    /**
     * Threats that are not on the table yet and are counted as if they were while a question is being answered:
     * the threats an action will add as its own cost, while a card that action would then play is asked whether
     * it could be played.  They take up room under the threat limit (PlayConditions.getMaxAddThreatCount) and
     * change nothing else.  Zero except inside withThreatsToCome.
     */
    public int getThreatsToCome() {
        return _threatsToCome;
    }

    /**
     * Answers a question as it would be answered once the given number of threats had been added.
     */
    public <T> T withThreatsToCome(int threats, java.util.function.Supplier<T> question) {
        final int before = _threatsToCome;
        _threatsToCome = before + Math.max(0, threats);
        try {
            return question.get();
        } finally {
            _threatsToCome = before;
        }
    }

    /**
     * Whether the cost of assigning this minion (ExtraAssignmentCostModifier) has been paid for the assignment
     * being made: the mark "its cost of assigning is paid" (Q-0283).  Two things set it, and both ask it first, so
     * that neither charges twice: the payment the Free Peoples player makes ahead of his own assigning
     * (AssignmentCostRule), and the payment of an assignment a card has him make (AssignFpCharacterToSkirmish),
     * which the assignment then uses at once.  While it is set the cost is not asked for again, since what paid
     * it (the last exertion a companion had) may not be there to pay twice.
     */
    public boolean isAssignmentPaidFor(PhysicalCard minion) {
        return _assignmentsPaidFor.contains(minion);
    }

    /** Marks the cost of assigning this minion paid (Q-0283; isAssignmentPaidFor). */
    public void markAssignmentPaidFor(PhysicalCard minion) {
        _assignmentsPaidFor.add(minion);
    }

    /** The mark of isAssignmentPaidFor is used: by the assignment it paid for, or as that assignment is not made. */
    public void useAssignmentPaidFor(PhysicalCard minion) {
        _assignmentsPaidFor.remove(minion);
    }

    /**
     * Whether the Free Peoples player's own assigning (FreePeoplePlayerAssignsMinionsGameProcess) is being asked
     * and made.  At it a minion with a cost of assigning that has not been paid for ahead is not his to assign
     * (Q-0283; ExtraAssignmentCostModifier).
     */
    public boolean isFreePeoplesPlayerAssigning() {
        return _freePeoplesPlayerAssigning;
    }

    /**
     * Begins or ends the Free Peoples player's own assigning (isFreePeoplesPlayerAssigning).  As it ends, every
     * mark of a cost of assigning paid is gone: used by the assignment made, or lost with the one not made.
     */
    public void setFreePeoplesPlayerAssigning(boolean assigning) {
        _freePeoplesPlayerAssigning = assigning;
        if (!assigning)
            _assignmentsPaidFor.clear();
    }

    public void removeBurdens(int burdens) {
        removeTokens(_ringBearers.get(getCurrentPlayerId()), Token.BURDEN, Math.max(0, burdens));
    }

    public void addWound(PhysicalCard card) {
        addTokens(card, Token.WOUND, 1);
    }

    /**
     * Notes the strength of a character of the current skirmish as it is about to leave it (F-0480), for Legacy
     * Ruling 2's final removed character, whom the skirmish's winner measures as "the losing character" though he may
     * be out of play by then (ResolveSkirmishEffect).  Called while the card is still in play with its text in force.
     */
    private void noteStrengthIfSkirmishing(PhysicalCard card) {
        if (_skirmish == null || _lotroGame == null)
            return;
        if (_skirmish.getFellowshipCharacter() == card || _skirmish.getShadowCharacters().contains(card))
            _skirmish.noteStrengthWhenRemoved(card, _lotroGame.getModifiersQuerying().getStrength(_lotroGame, card));
    }

    public void removeWound(PhysicalCard card) {
        removeTokens(card, Token.WOUND, 1);
    }

    public void hinder(Collection<PhysicalCard> cards) {
        for(var card : cards)
            noteStrengthIfSkirmishing(card);
        for(var card : cards) {
            card.setFlipped(true);
            stopAffecting(card);
            removeFromAssignment(card);
            removeFromSkirmish(card);
        }

        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.cardsFlipped(cards, true);
        }
    }

    public void restore(LotroGame game, Collection<PhysicalCard> cards) {
        for(var card : cards) {
            card.setFlipped(false);
            startAffecting(game, card);
        }

        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.cardsFlipped(cards, false);
        }
    }

    public boolean isHindered(PhysicalCard card) {
        return card.isFlipped();
    }

    public boolean canBeHindered(PhysicalCard card) {
        //Cannot hinder if it is already hindered
        if(card.isFlipped())
            return false;

        var zone = card.getZone();

        //Cards in hand can be hindered
//        if(zone == Zone.HAND)
//            return true;

        //Cards stacked on active cards can be hindered
        if(zone == Zone.STACKED && isCardInPlayActive(card.getStackedOn()))
            return true;

        //Cards can be intercepted on being played and preemptively hindered
//        if(zone == Zone.VOID)
//            return true;

        var bp = card.getBlueprint();
        //An event card that is in the process of being resolved can be hindered (to cancel its effect)
//        if(bp.getCardType() == CardType.EVENT)
//            return zone == Zone.VOID_FROM_HAND;

        //Sites cannot be hindered (CR 5.0 "hinder").  Neither can a Map or a Race to Mount Doom meta-site: they
        // lie in a support area and do nothing there, and are not cards to be hindered (Ketura on Q-0110).
        if(bp.getCardType() == CardType.SITE || bp.getCardType() == CardType.MAP || bp.getCardType() == CardType.METASITE)
            return false;

        //Finally, anything currently active on the table can be hindered
        return isCardInPlayActive(card);
    }


    public void addTokens(PhysicalCard card, Token token, int count) {
        Map<Token, Integer> tokens = _cardTokens.get(card);
        if (tokens == null) {
            tokens = new HashMap<>();
            _cardTokens.put(card, tokens);
        }
        Integer currentCount = tokens.get(token);
        if (currentCount == null)
            tokens.put(token, count);
        else
            tokens.put(token, currentCount + count);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.addTokens(card, token, count);
    }

    public void removeTokens(PhysicalCard card, Token token, int count) {
        Map<Token, Integer> tokens = _cardTokens.get(card);
        if (tokens == null) {
            tokens = new HashMap<>();
            _cardTokens.put(card, tokens);
        }
        Integer currentCount = tokens.get(token);
        if (currentCount != null) {
            if (currentCount < count)
                count = currentCount;

            tokens.put(token, currentCount - count);

            for (GameStateListener listener : getAllGameStateListeners())
                listener.removeTokens(card, token, count);
        }
    }

    public void setTwilight(int twilight) {
        _twilightPool = twilight;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setTwilight(_twilightPool);
    }

    public int getTwilightPool() {
        return _twilightPool;
    }

    /**
     * @return What will arrange the board in place of the usual opening procedure, or null if the game opens as usual.
     */
    public PregameSetup getPregameSetup() {
        return _pregameSetup;
    }

    /**
     * Has this game begin with a prepared position.  Only do this before the game has started.
     */
    public void setPregameSetup(PregameSetup pregameSetup) {
        _pregameSetup = pregameSetup;
    }

    /**
     * Gives one particular card in this game a name of its own, so that it can be picked out later no matter where
     * it has gone or what its card ID has become (see the "alias" filter).
     * @param label The name; labelling a second card with the same name replaces the first.
     */
    public void labelCard(String label, PhysicalCard card) {
        //(names are told apart without regard to case: the card builder lowercases what it reads in a filter)
        _cardLabels.put(label.toLowerCase(java.util.Locale.ROOT), card);
    }

    /**
     * @return The card that was given this name with labelCard, or null if there is none.
     */
    public PhysicalCard getLabeledCard(String label) {
        return label == null ? null : _cardLabels.get(label.toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * @return The name a card was given with labelCard, or null if it has none.
     */
    public String getCardLabel(PhysicalCard card) {
        for (Map.Entry<String, PhysicalCard> entry : _cardLabels.entrySet())
            if (entry.getValue() == card)
                return entry.getKey();
        return null;
    }

    /**
     * Creates a card that is in no deck and no zone, from a blueprint that need not be in the card library.  It is not
     * shown to the players.  This is how a game gets a card whose only purpose is to carry rules (see
     * PhysicalCardImpl.startAffectingGame).
     */
    public PhysicalCardImpl createLooseCard(String ownerPlayerId, String blueprintId, LotroCardBlueprint blueprint) {
        int cardId = nextCardId();
        PhysicalCardImpl result = new PhysicalCardImpl(cardId, blueprintId, ownerPlayerId, blueprint);
        _allCards.put(cardId, result);
        return result;
    }

    /**
     * @return The player who takes every turn as the Free Peoples player, or null if turns pass around the table as
     * usual.
     */
    public String getFixedFreePeoplePlayer() {
        return _fixedFreePeoplePlayer;
    }

    /**
     * Makes one player the Free Peoples player on every turn, so that the other player only ever plays their Shadow
     * cards.  This is for one-sided games (e.g. a lesson that teaches only one half of the game at a time).
     * @param playerId The player to take every turn, or null to have turns pass around the table as usual.
     */
    public void setFixedFreePeoplePlayer(String playerId) {
        _fixedFreePeoplePlayer = playerId;
    }

    public void startPlayerTurn(String playerId, boolean realTurn) {
        _currentPlayerId = playerId;
        if (realTurn) {
            turnNumber++;
        }
        setTwilight(0);
        _moveCount = 0;
        _fellowshipArriving = false;
        _fierceSkirmishes = false;

        for (var listener : getAllGameStateListeners()) {
            listener.setCurrentPlayerId(_currentPlayerId, getInactiveCards());
        }
    }

    public Set<PhysicalCard> getInactiveCards() {
        return Stream.concat(_inPlay.stream(), _stacked.values().stream().flatMap(Collection::stream))
                .filter(x -> !isCardInPlayActive(x, false, false, false, true))
                .collect(Collectors.toSet());
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isExtraSkirmishes() {
        return _extraSkirmishes;
    }

    public void setExtraSkirmishes(boolean extraSkirmishes) {
        _extraSkirmishes = extraSkirmishes;
    }

    public void setRelentlessSkirmishes(boolean value) {
        _relentlessSkirmishes = value;
    }

    public boolean isRelentlessSkirmishes() {
        return _relentlessSkirmishes;
    }

    public void setFierceSkirmishes(boolean value) {
        _fierceSkirmishes = value;
    }

    public boolean isFierceSkirmishes() {
        return _fierceSkirmishes;
    }

    public boolean isNormalSkirmishes() {
        return !_fierceSkirmishes && !_relentlessSkirmishes && !_extraSkirmishes;
    }

    public boolean isCardInPlayActive(PhysicalCard card) { return isCardInPlayActive(card, false, false, false, false); }
    public boolean isCardInPlayActive(PhysicalCard card, boolean includeOutOfTurn, boolean includeAttachedToInactive,
            boolean includeStacked, boolean includeHindered) {
        Side side = card.getBlueprint().getSide();

        //Hindered cards do not count as active for most purposes
        if(!includeHindered && card.isFlipped())
            return false;

//        //out-of-play zones should of course be disqualified, but we make an exception for stack
//        // since there's so many edge cases for it; it will be handled below
//        var zone = card.getZone();
//        if(zone != null && !zone.isInPlay() && zone != Zone.STACKED)
//            return false;


        // Either it's not attached or attached to active card
        // AND is a site or fp/ring of current player or shadow of any other player
        if (card.getBlueprint().getCardType() == CardType.SITE)
            return _currentPhase != Phase.PUT_RING_BEARER && _currentPhase != Phase.PLAY_STARTING_FELLOWSHIP;

        if (card.getBlueprint().getCardType() == CardType.THE_ONE_RING)
            return card.getOwner().equals(_currentPlayerId);

        if (card.getAttachedTo() != null && card.getAttachedTo().getBlueprint().getCardType() != CardType.SITE) {
            //We override the activity check to include hindered cards, as being attached to a hindered card does not inactivate attached cards
            if(!isCardInPlayActive(card.getAttachedTo(), includeOutOfTurn, includeAttachedToInactive, includeStacked, true)) {
                return includeAttachedToInactive;
            }
        }

        if(card.getStackedOn() != null && card.getStackedOn().getBlueprint().getCardType() != CardType.SITE){
            //We override the activity check to include hindered cards, as being stacked on a hindered card does not inactivate stacked cards
            if(!isCardInPlayActive(card.getStackedOn(), includeOutOfTurn, includeAttachedToInactive, includeStacked, true)) {
                return includeStacked;
            }
        }

        if (card.getOwner().equals(_currentPlayerId) && side == Side.SHADOW)
            return includeOutOfTurn;

        if (!card.getOwner().equals(_currentPlayerId) && side == Side.FREE_PEOPLE)
            return includeOutOfTurn;

        return true;
    }

    public void startAffectingCardsForCurrentPlayer(LotroGame game) {
        // Active non-sites are affecting
        for (PhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE
                    && physicalCard.getBlueprint().getCardType() != CardType.METASITE
            )
                startAffecting(game, physicalCard);
            else if (physicalCard.getBlueprint().getCardType() == CardType.SITE &&
                    physicalCard.getCardController() != null) {
                startAffectingControlledSite(game, physicalCard);
            }
        }

        // Current site is affecting
        if (_currentPhase != Phase.PLAY_STARTING_FELLOWSHIP)
            startAffecting(game, getCurrentSite());

        // Stacked cards on active cards are stack-affecting
        for (List<PhysicalCardImpl> stackedCards : _stacked.values())
            for (PhysicalCardImpl stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    startAffectingStacked(game, stackedCard);

        for (List<PhysicalCardImpl> discardedCards : _discards.values())
            for (PhysicalCardImpl discardedCard : discardedCards)
                startAffectingInDiscard(game, discardedCard);
    }

    private void startAffectingControlledSite(LotroGame game, PhysicalCard physicalCard) {
        ((PhysicalCardImpl) physicalCard).startAffectingGameControlledSite(game);
    }

    public void reapplyAffectingForCard(LotroGame game, PhysicalCard card) {
        ((PhysicalCardImpl) card).stopAffectingGame();
        ((PhysicalCardImpl) card).startAffectingGame(game);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (PhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE
                    && physicalCard.getBlueprint().getCardType() != CardType.METASITE
            )
                stopAffecting(physicalCard);
            else if (physicalCard.getBlueprint().getCardType() == CardType.SITE &&
                    physicalCard.getCardController() != null) {
                stopAffectingControlledSite(physicalCard);
            }
        }

        stopAffecting(getCurrentSite());

        for (List<PhysicalCardImpl> stackedCards : _stacked.values())
            for (PhysicalCardImpl stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    stopAffectingStacked(stackedCard);

        for (List<PhysicalCardImpl> discardedCards : _discards.values())
            for (PhysicalCardImpl discardedCard : discardedCards)
                stopAffectingInDiscard(discardedCard);
    }

    private void stopAffectingControlledSite(PhysicalCard physicalCard) {
        ((PhysicalCardImpl) physicalCard).stopAffectingGameControlledSite();
    }

    private void startAffecting(LotroGame game, PhysicalCard card) {
        ((PhysicalCardImpl) card).startAffectingGame(game);
    }

    private void stopAffecting(PhysicalCard card) {
        ((PhysicalCardImpl) card).stopAffectingGame();
    }

    private void startAffectingStacked(LotroGame game, PhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).startAffectingGameStacked(game);
    }

    private void stopAffectingStacked(PhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).stopAffectingGameStacked();
    }

    private void startAffectingInDiscard(LotroGame game, PhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).startAffectingGameInDiscard(game);
    }

    private void stopAffectingInDiscard(PhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).stopAffectingGameInDiscard();
    }

    private boolean isCardAffectingGame(PhysicalCard card) {
        final Side side = card.getBlueprint().getSide();
        if (side == Side.SHADOW)
            return !getCurrentPlayerId().equals(card.getOwner());
        else if (side == Side.FREE_PEOPLE)
            return getCurrentPlayerId().equals(card.getOwner());
        else
            return false;
    }

    public void setCurrentPhase(Phase phase) {
        _currentPhase = phase;
        // A cost of assigning paid in one assignment phase pays for nothing in the next (Q-0283)
        _assignmentsPaidFor.clear();
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPhase(getPhaseString());
    }

    public Phase setFakePhase(Phase phase) {
        var realPhase = _currentPhase;
        _currentPhase = phase;
        return realPhase;
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public PhysicalCard getSite(int siteNumber) {
        for (PhysicalCardImpl physicalCard : _inPlay) {
            LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getCardType() == CardType.SITE && physicalCard.getSiteNumber() == siteNumber)
                return physicalCard;
        }
        return null;
    }

    public PhysicalCard getCurrentSite() {
        return getSite(getCurrentSiteNumber());
    }

    public SitesBlock getCurrentSiteBlock() {
        return getCurrentSite().getBlueprint().getSiteBlock();
    }

    public void increaseMoveCount() {
        _moveCount++;
    }

    public int getMoveCount() {
        return _moveCount;
    }

    public void addTwilight(int twilight) {
        setTwilight(_twilightPool + Math.max(0, twilight));
    }

    public void removeTwilight(int twilight) {
        setTwilight(_twilightPool - Math.min(Math.max(0, twilight), _twilightPool));
    }

    public PhysicalCard removeTopDeckCard(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            final PhysicalCard topDeckCard = deck.get(0);
            removeCardsFromZone(null, Collections.singleton(topDeckCard));
            return topDeckCard;
        } else {
            return null;
        }
    }

    public PhysicalCard removeBottomDeckCard(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            final PhysicalCard topDeckCard = deck.get(deck.size() - 1);
            removeCardsFromZone(null, Collections.singleton(topDeckCard));
            return topDeckCard;
        } else {
            return null;
        }
    }

    public void playerDrawsCard(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            PhysicalCard card = deck.get(0);
            removeCardsFromZone(null, Collections.singleton(card));
            addCardToZone(null, card, Zone.HAND);
        }
    }

    public void shuffleDeck(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
        _random.shuffleDeck(player, deck);
        _deckShuffles.merge(player, 1, Integer::sum);
    }

    /**
     * @return How many times that player's draw deck has been shuffled so far this game, the shuffle before the
     * opening hand included.  Kept so that a test can tell that a card shuffled a deck, or left it alone.
     */
    public int getDeckShuffleCount(String player) {
        return _deckShuffles.getOrDefault(player, 0);
    }

    /**
     * @return Where this game gets its random outcomes from (shuffles, cards picked at random, tied bids).
     */
    public GameRandom getRandom() {
        return _random;
    }

    /**
     * Replaces where this game gets its random outcomes from.  Only do this before the game has started.
     */
    public void setRandom(GameRandom random) {
        _random = random;
    }

    public void sendGameStats(GameStats gameStats) {
        if (_heldBackEvents != null) {
            _heldBackGameStats = gameStats;
            return;
        }
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendGameStats(gameStats);
        _mostRecentGameStats = gameStats;
    }

    public void sendWarning(String player, String warning) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendWarning(player, warning);
    }

    public GameStats getMostRecentGameStats() {
        return _mostRecentGameStats;
    }
}
