package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.logic.PlayerOrder;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.GameStats;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameState {
    private static final Logger _log = LogManager.getLogger(GameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 15;
    private PlayerOrder _playerOrder;

    private LotroFormat _format;

    private final Map<String, List<PhysicalCardImpl>> _adventureDecks = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _decks = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _hands = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _discards = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _deadPiles = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _stacked = new HashMap<>();

    private final Map<String, List<PhysicalCardImpl>> _voids = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _voidsFromHand = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _removed = new HashMap<>();

    private final List<PhysicalCardImpl> _inPlay = new LinkedList<>();

    private final Map<Integer, PhysicalCardImpl> _allCards = new HashMap<>();

    private String _currentPlayerId;
    private String _firstPlayerId;
    private Phase _currentPhase = Phase.PUT_RING_BEARER;
    private int _twilightPool;

    private int _moveCount;
    private int turnNumber;
    private boolean _fierceSkirmishes;
    private boolean _extraSkirmishes;

    private boolean _wearingRing;
    private boolean _consecutiveAction;

    private final Map<String, Integer> _playerPosition = new HashMap<>();
    private final Map<String, Integer> _playerThreats = new HashMap<>();

    private final Map<PhysicalCard, Map<Token, Integer>> _cardTokens = new HashMap<>();

    private final Map<String, PhysicalCard> _ringBearers = new HashMap<>();
    private final Map<String, PhysicalCard> _rings = new HashMap<>();
    private final Map<String, PhysicalCard> _maps = new HashMap<>();

    private final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();

    private final List<Assignment> _assignments = new LinkedList<>();
    private Skirmish _skirmish = null;

    private final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    private final LinkedList<String> _lastMessages = new LinkedList<>();

    private PreGameInfo _preGameInfo;

    private int _nextCardId = 0;

    private GameStats _mostRecentGameStats = null;
    private boolean _isInit = false;
    private Map<String, LotroDeck> _lotroDecks;

    private int nextCardId() {
        return _nextCardId++;
    }

    //This happens before the bidding, so it has to be done separately from init
    public void initPreGame(PreGameInfo preGameInfo, Map<String, LotroDeck> decks) {
        _preGameInfo = preGameInfo;
        _lotroDecks = decks;
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializePregameBoard(preGameInfo);
        }
    }

    public boolean isInit() {
        return _isInit;
    }

    public void init(PlayerOrder playerOrder, String firstPlayer, Map<String, List<String>> cards,
            Map<String, String> ringBearers, Map<String, String> rings, Map<String, String> maps,
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
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.endGame();
        }

        if(_playerOrder == null || _playerOrder.getAllPlayers() == null)
            return;

        for (String playerId : _playerOrder.getAllPlayers()) {
            for(var card : getDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(card, true);
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

    public PhysicalCard createPhysicalCard(String ownerPlayerId, LotroCardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        return createPhysicalCardImpl(ownerPlayerId, library, blueprintId);
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
        _gameStateListeners.add(gameStateListener);
        sendStateToPlayer(playerId, gameStateListener, gameStats);
        _mostRecentGameStats = gameStats;
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameStateListeners.remove(gameStateListener);
    }

    private Collection<GameStateListener> getAllGameStateListeners() {
        return Collections.unmodifiableSet(_gameStateListeners);
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

            for (Assignment assignment : _assignments)
                listener.addAssignment(assignment.getFellowshipCharacter(), assignment.getShadowCharacters());

            if (_skirmish != null)
                listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());

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

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        if (awaitingDecision != null)
            listener.decisionRequired(playerId, awaitingDecision);
    }

    public void sendMessage(String message) {
        _lastMessages.add(message);
        if (_lastMessages.size() > LAST_MESSAGE_STORED_COUNT)
            _lastMessages.removeFirst();
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

        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardsRemoved(playerPerforming, cards);

        for (PhysicalCard card : cards) {
            ((PhysicalCardImpl) card).setZone(null);
        }
    }

    public void addCardToZone(LotroGame game, PhysicalCard card, Zone zone) {
        addCardToZone(game, card, zone, true);
    }

    private void addCardToZone(LotroGame game, PhysicalCard card, Zone zone, boolean end) {
        if (zone == Zone.DISCARD && game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        //Hindered cards that are being discarded, banished, moved to a deck, or killed do not remain hindered
        if(card.isFlipped() && (zone == Zone.REMOVED || zone == Zone.DISCARD || zone == Zone.DEAD
                || zone == Zone.DECK || zone == Zone.ADVENTURE_DECK)) {
            card.setFlipped(false);
        }

        if (zone.isInPlay()) {
            assignNewCardId(card);
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
            for (GameStateListener listener : getAllGameStateListeners()) {
                listener.cardCreated(card);
            }
        }

        if (_currentPhase.isCardsAffectGame()) {
            if (zone.isInPlay()) {
                if (card.getBlueprint().getCardType() != CardType.SITE || (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && getCurrentSite() == card)) {
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
     * Replaces a character on table with another character.  The calling function should handle discarding the old one.
     * @param oldCard the old card
     * @param newCard the new card
     */
    public void replaceCharacterOnTable(LotroGame game,  PhysicalCard oldCard, PhysicalCard newCard) {
        int oldCardId = oldCard.getCardId();

        // Remove new card from zone
        removeCardsFromZone(null, Collections.singleton(newCard));

        //Skirmish assignments will get bulldozed when we remove the old card from its zone, so we back them up first.
        var assignment = findAssignment(oldCard);
        var skirmish = findSkirmish(oldCard);
        var tokens = new HashMap<>(getTokens(oldCard));

        // Put the card where the old card was
        newCard.copyCardStats(oldCard);
        attachCard(game, newCard, oldCard.getAttachedTo());

        // Remove old card from zone
        removeCardsFromZone(null, Collections.singleton(oldCard), true);
        addCardToZone(game, oldCard, Zone.VOID);
        assignNewCardId(oldCard);

        // Give new card the card ID of the replaced card
        reassignCardId(newCard, oldCardId);

        // Add new card to zone
        getZoneCards(newCard.getOwner(), newCard.getZone()).add((PhysicalCardImpl) newCard);

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


        var attachedCardList = getAttachedCards(oldCard);
        var stackedCardList = getStackedCards(oldCard);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardCreated(newCard);

        // Transfer attached cards to the new card
        for (PhysicalCard attachedCard : attachedCardList) {
            attachCard(game, attachedCard, newCard);
        }

        // Transfer stacked cards to the new card
        for (PhysicalCard stackedCard : stackedCardList) {
            stackCard(game, stackedCard, newCard);
        }

        startAffecting(game, newCard);
        for (PhysicalCard attachedCard : getAttachedCards(newCard)) {
            reapplyAffectingForCard(game, attachedCard);
        }
    }

    public void replaceInSkirmish(PhysicalCard card) {
        _skirmish.setFellowshipCharacter(card);
        for (GameStateListener gameStateListener : getAllGameStateListeners()) {
            gameStateListener.finishSkirmish();
            gameStateListener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
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
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.finishSkirmish();
            listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
        }
    }

    public void removeFromSkirmish(PhysicalCard card) {
        removeFromSkirmish(card, true);
    }
    private void removeFromSkirmish(PhysicalCard card, boolean notify) {
        if (_skirmish == null)
            return;

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
    }

    public void removeFromAssignment(PhysicalCard card) {
        if( _assignments.isEmpty())
            return;

        for (var assignment : new LinkedList<>(_assignments)) {
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
                if (assignment.getShadowCharacters().remove(minion))
                    if (assignment.getShadowCharacters().isEmpty())
                        removeAssignment(assignment);
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
        removeAssignment(assignment);
        assignToSkirmishes(assignment.getFellowshipCharacter(), assignment.getShadowCharacters());
    }

    public void removeAssignment(Assignment assignment) {
        _assignments.remove(assignment);
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.removeAssignment(assignment.getFellowshipCharacter());
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
            listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
    }

    public void restartSkirmish(Skirmish skirmish) {
        _skirmish = skirmish;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
    }

    public Skirmish getSkirmish() {
        return _skirmish;
    }

    public void finishSkirmish() {
        if (_skirmish != null) {
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
        }

        shuffleDeck(playerId);
    }

    public void putCardOnBottomOfDeck(PhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, true);
    }

    public void putCardOnTopOfDeck(PhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, false);
    }
    public boolean iterateActiveCards(PhysicalCardVisitor visitor) { return iterateActiveCards(visitor, SpotOverride.NONE); }
    public boolean iterateActiveCards(PhysicalCardVisitor physicalCardVisitor, Map<InactiveReason, Boolean> spotOverrides) {

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

        for (PhysicalCardImpl physicalCard : _inPlay) {
            // Check if the card can be spotted as "active" and include it if it can be.
            if (isCardInPlayActive(physicalCard, includeOutOfTurn, includeAttachedToInactive, includeStacked, includeHindered)) {
                if (physicalCardVisitor.visitPhysicalCard(physicalCard)) {
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
        startAffecting(game, getCurrentSite());
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
        return getTokenCount(physicalCard, Token.WOUND);
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

    public void removeBurdens(int burdens) {
        removeTokens(_ringBearers.get(getCurrentPlayerId()), Token.BURDEN, Math.max(0, burdens));
    }

    public void addWound(PhysicalCard card) {
        addTokens(card, Token.WOUND, 1);
    }

    public void removeWound(PhysicalCard card) {
        removeTokens(card, Token.WOUND, 1);
    }

    public void hinder(Collection<PhysicalCard> cards) {
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
        if(zone == Zone.HAND)
            return true;

        //Cards stacked on active cards can be hindered
        if(zone == Zone.STACKED && isCardInPlayActive(card.getStackedOn()))
            return true;

        //Cards can be intercepted on being played and preemptively hindered
        if(zone == Zone.VOID)
            return true;

        var bp = card.getBlueprint();
        //An event card that is in the process of being resolved can be hindered (to cancel its effect)
        if(bp.getCardType() == CardType.EVENT)
            return zone == Zone.VOID_FROM_HAND;

        if(bp.getCardType() == CardType.SITE)
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

    public void startPlayerTurn(String playerId, boolean realTurn) {
        _currentPlayerId = playerId;
        if (realTurn) {
            turnNumber++;
        }
        setTwilight(0);
        _moveCount = 0;
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

    public void setFierceSkirmishes(boolean value) {
        _fierceSkirmishes = value;
    }

    public boolean isFierceSkirmishes() {
        return _fierceSkirmishes;
    }

    public boolean isNormalSkirmishes() {
        return !_fierceSkirmishes && !_extraSkirmishes;
    }

    public boolean isCardInPlayActive(PhysicalCard card) { return isCardInPlayActive(card, false, false, false, false); }
    public boolean isCardInPlayActive(PhysicalCard card, boolean includeOutOfTurn, boolean includeAttachedToInactive,
            boolean includeStacked, boolean includeHindered) {
        Side side = card.getBlueprint().getSide();

        //Hindered cards do not count as active for most purposes
        if(!includeHindered && card.isFlipped())
            return false;

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
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE)
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
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE)
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
        Collections.shuffle(deck, ThreadLocalRandom.current());
    }

    public void sendGameStats(GameStats gameStats) {
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
