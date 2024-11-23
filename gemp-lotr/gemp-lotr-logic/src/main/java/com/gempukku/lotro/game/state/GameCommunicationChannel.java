package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameStats;
import com.gempukku.polling.LongPollableResource;
import com.gempukku.polling.WaitingRequest;

import java.util.*;

import static com.gempukku.lotro.game.state.GameEvent.Type.*;

public class GameCommunicationChannel implements GameStateListener, LongPollableResource {
    private List<GameEvent> _events = Collections.synchronizedList(new LinkedList<>());
    private final String _self;
    private long _lastConsumed = System.currentTimeMillis();
    private final int _channelNumber;
    private volatile WaitingRequest _waitingRequest;

    private final LotroFormat _format;

    public GameCommunicationChannel(String self, int channelNumber, LotroFormat format) {
        _self = self;
        _channelNumber = channelNumber;
        _format = format;
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    @Override
    public void initializeBoard(List<String> participants, boolean discardIsPublic) {
        List<String> participantIds = new LinkedList<>();
        participantIds.addAll(participants);
        appendEvent(new GameEvent(PARTICIPANTS)
                .participantId(_self)
                .allParticipantIds(participantIds)
                .discardPublic(discardIsPublic)
        );
    }

    @Override
    public void initializePregameBoard(PreGameInfo preGameInfo) {
        List<String> participantIds = new LinkedList<>(preGameInfo.participants());
        appendEvent(new GameEvent(PRE_GAME_SETUP)
                .participantId(_self)
                .allParticipantIds(participantIds)
                .preGameInfo(preGameInfo)
        );
    }

    @Override
    public synchronized void deregisterRequest(WaitingRequest waitingRequest) {
        _waitingRequest = null;
    }

    @Override
    public synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (_events.size()>0)
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    private synchronized void appendEvent(GameEvent event) {
        _events.add(event);
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    private int[] getCardIds(Collection<PhysicalCard> cards) {
        int[] result = new int[cards.size()];
        int index = 0;
        for (PhysicalCard card : cards) {
            result[index] = card.getCardId();
            index++;
        }
        return result;
    }

    @Override
    public void addAssignment(PhysicalCard freePeople, Set<PhysicalCard> minions) {
        appendEvent(new GameEvent(ADD_ASSIGNMENT).cardId(freePeople.getCardId()).otherCardIds(getCardIds(minions)));
    }

    @Override
    public void removeAssignment(PhysicalCard freePeople) {
        appendEvent(new GameEvent(REMOVE_ASSIGNMENT).cardId(freePeople.getCardId()));
    }

    @Override
    public void startSkirmish(PhysicalCard freePeople, Set<PhysicalCard> minions) {
        GameEvent gameEvent = new GameEvent(START_SKIRMISH).otherCardIds(getCardIds(minions));
        if (freePeople != null)
            gameEvent.cardId(freePeople.getCardId());
        appendEvent(gameEvent);
    }

    @Override
    public void addToSkirmish(PhysicalCard card) {
        appendEvent(new GameEvent(ADD_TO_SKIRMISH).card(card));
    }

    @Override
    public void removeFromSkirmish(PhysicalCard card) {
        appendEvent(new GameEvent(REMOVE_FROM_SKIRMISH).card(card));
    }

    @Override
    public void finishSkirmish() {
        appendEvent(new GameEvent(END_SKIRMISH));
    }

    @Override
    public void setCurrentPhase(String phase) {
        appendEvent(new GameEvent(GAME_PHASE_CHANGE).phase(phase));
    }

    @Override
    public void cardCreated(PhysicalCard card) {
        boolean publicDiscard = card.getZone() == Zone.DISCARD && _format.discardPileIsPublic();
        if (card.getZone().isPublic() || publicDiscard || (card.getZone().isVisibleByOwner() && card.getOwner().equals(_self)))
            appendEvent(new GameEvent(PUT_CARD_INTO_PLAY).card(card));
    }

    @Override
    public void cardCreated(PhysicalCard card, boolean overridePlayerVisibility) {
        boolean publicDiscard = card.getZone() == Zone.DISCARD && _format.discardPileIsPublic();
        if (card.getZone().isPublic() || publicDiscard || ((overridePlayerVisibility || card.getZone().isVisibleByOwner()) && card.getOwner().equals(_self)))
            appendEvent(new GameEvent(PUT_CARD_INTO_PLAY).card(card));
    }

    @Override
    public void cardMoved(PhysicalCard card) {
        appendEvent(new GameEvent(MOVE_CARD_IN_PLAY).card(card));
    }

    @Override
    public void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards) {
        Set<PhysicalCard> removedCardsVisibleByPlayer = new HashSet<>();
        for (PhysicalCard card : cards) {
            boolean publicDiscard = card.getZone() == Zone.DISCARD && _format.discardPileIsPublic();
            if (card.getZone().isPublic() || publicDiscard || (card.getZone().isVisibleByOwner() && card.getOwner().equals(_self)))
                removedCardsVisibleByPlayer.add(card);
        }
        if (removedCardsVisibleByPlayer.size() > 0)
            appendEvent(new GameEvent(REMOVE_CARD_FROM_PLAY).otherCardIds(getCardIds(removedCardsVisibleByPlayer)).participantId(playerPerforming));
    }

    @Override
    public void setPlayerPosition(String participant, int position) {
        appendEvent(new GameEvent(PLAYER_POSITION).participantId(participant).index(position));
    }

    @Override
    public void setTwilight(int twilightPool) {
        appendEvent(new GameEvent(TWILIGHT_POOL_UPDATE).count(twilightPool));
    }

    @Override
    public void setCurrentPlayerId(String currentPlayerId, Set<PhysicalCard> inactiveCards) {
        appendEvent(new GameEvent(TURN_CHANGE)
                .participantId(currentPlayerId)
                .otherCardIds(getCardIds(inactiveCards)));
    }

    @Override
    public String getAssignedPlayerId() {
        return _self;
    }

    @Override
    public void addTokens(PhysicalCard card, Token token, int count) {
        appendEvent(new GameEvent(ADD_TOKENS).card(card).token(token).count(count));
    }

    @Override
    public void removeTokens(PhysicalCard card, Token token, int count) {
        appendEvent(new GameEvent(REMOVE_TOKENS).card(card).token(token).count(count));
    }

    @Override
    public void sendMessage(String message) {
        appendEvent(new GameEvent(SEND_MESSAGE).message(message));
    }

    @Override
    public void setSite(PhysicalCard card) {
        appendEvent(new GameEvent(PUT_CARD_INTO_PLAY).card(card).index(card.getSiteNumber()));
    }

    @Override
    public void sendGameStats(GameStats gameStats) {
        appendEvent(new GameEvent(GAME_STATS).gameStats(gameStats.makeACopy()));
    }

    @Override
    public void cardAffectedByCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCards) {
        appendEvent(new GameEvent(CARD_AFFECTED_BY_CARD).card(card).participantId(playerPerforming).otherCardIds(getCardIds(affectedCards)));
    }

    @Override
    public void eventPlayed(PhysicalCard card) {
        appendEvent(new GameEvent(SHOW_CARD_ON_SCREEN).card(card));
    }

    @Override
    public void cardActivated(String playerPerforming, PhysicalCard card) {
        appendEvent(new GameEvent(FLASH_CARD_IN_PLAY).card(card).participantId(playerPerforming));
    }

    public void decisionRequired(String playerId, AwaitingDecision decision) {
        if (playerId.equals(_self))
            appendEvent(new GameEvent(DECISION).awaitingDecision(decision).participantId(playerId));
    }

    @Override
    public void sendWarning(String playerId, String warning) {
        if (playerId.equals(_self))
            appendEvent(new GameEvent(SEND_WARNING).message(warning));
    }

    public List<GameEvent> consumeGameEvents() {
        updateLastAccess();
        List<GameEvent> result = _events;
        _events = Collections.synchronizedList(new LinkedList<>());
        return result;
    }

    public boolean hasGameEvents() {
        updateLastAccess();
        return _events.size()>0;
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public long getLastAccessed() {
        return _lastConsumed;
    }

    @Override
    public void endGame() {
        appendEvent(new GameEvent(GAME_ENDED));
    }
}
