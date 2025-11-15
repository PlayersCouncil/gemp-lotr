package com.gempukku.lotro.bots;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.communication.GameStateListener;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PreGameInfo;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameStats;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class BotGameStateListener implements GameStateListener {
    private final BotPlayer botPlayer;
    private final LotroGameMediator lotroGameMediator;

    public BotGameStateListener(BotPlayer botPlayer, LotroGameMediator lotroGameMediator) {
        this.botPlayer = botPlayer;
        this.lotroGameMediator = lotroGameMediator;
    }

    @Override
    public boolean isLiveConnection() { return false; }

    @Override
    public void cardCreated(PhysicalCard card) {

    }

    @Override
    public void cardCreated(PhysicalCard card, boolean overridePlayerVisibility) {

    }

    @Override
    public void cardMoved(PhysicalCard card) {

    }

    @Override
    public void cardsFlipped(Collection<PhysicalCard> cards, boolean down) {

    }

    @Override
    public void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards) {

    }

    @Override
    public void initializeBoard(List<String> playerIds, boolean discardIsPublic) {

    }

    @Override
    public void initializePregameBoard(PreGameInfo preGameInfo) {

    }

    @Override
    public void setPlayerPosition(String playerId, int i) {

    }

    @Override
    public void setTwilight(int twilightPool) {

    }

    @Override
    public void setCurrentPlayerId(String playerId, Set<PhysicalCard> inactiveCards) {

    }

    @Override
    public String getAssignedPlayerId() {
        return null;
    }

    @Override
    public void setCurrentPhase(String currentPhase) {

    }

    @Override
    public void addAssignment(PhysicalCard fp, Set<PhysicalCard> minions) {

    }

    @Override
    public void addPendingAssignment(PhysicalCard fp, PhysicalCard minion) {

    }

    @Override
    public void removeAssignment(PhysicalCard fp) {

    }

    @Override
    public void removePendingAssignment(PhysicalCard freePeople, PhysicalCard minion) {

    }

    @Override
    public void startSkirmish(PhysicalCard fp, Set<PhysicalCard> minions) {

    }

    @Override
    public void addToSkirmish(PhysicalCard card) {

    }

    @Override
    public void removeFromSkirmish(PhysicalCard card) {

    }

    @Override
    public void finishSkirmish() {

    }

    @Override
    public void addTokens(PhysicalCard card, Token token, int count) {

    }

    @Override
    public void removeTokens(PhysicalCard card, Token token, int count) {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void setSite(PhysicalCard card) {

    }

    @Override
    public void sendGameStats(GameStats gameStats) {

    }

    @Override
    public void cardAffectedByCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCard) {

    }

    @Override
    public void eventPlayed(PhysicalCard card) {

    }

    @Override
    public void cardActivated(String playerPerforming, PhysicalCard card) {

    }

    @Override
    public void decisionRequired(String playerId, AwaitingDecision awaitingDecision) {
        if (playerId.equals(botPlayer.getName())) {
            new Thread(() -> {
                String action = botPlayer.chooseAction(lotroGameMediator.getGameState(), awaitingDecision);
                lotroGameMediator.botAnswered(botPlayer.getName(), awaitingDecision.getAwaitingDecisionId(), action);
            }).start();
        }
    }

    @Override
    public void sendWarning(String playerId, String warning) {

    }

    @Override
    public void endGame() {

    }
}
