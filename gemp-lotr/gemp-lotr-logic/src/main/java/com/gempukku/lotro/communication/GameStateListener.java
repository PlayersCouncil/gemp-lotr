package com.gempukku.lotro.communication;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.PreGameInfo;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.timing.GameStats;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface GameStateListener {
    void cardCreated(PhysicalCard card);
    void cardCreated(PhysicalCard card, boolean overridePlayerVisibility);

    void cardMoved(PhysicalCard card);
    void cardsFlipped(Collection<PhysicalCard> cards, boolean down);

    void cardsRemoved(String playerPerforming, Collection<PhysicalCard> cards);

    void initializeBoard(List<String> playerIds, boolean discardIsPublic);
    void initializePregameBoard(PreGameInfo preGameInfo);

    void setPlayerPosition(String playerId, int i);

    void setTwilight(int twilightPool);

    void setCurrentPlayerId(String playerId, Set<PhysicalCard> inactiveCards);
    String getAssignedPlayerId();

    void setCurrentPhase(String currentPhase);

    void addAssignment(PhysicalCard fp, Set<PhysicalCard> minions);

    void removeAssignment(PhysicalCard fp);

    void startSkirmish(PhysicalCard fp, Set<PhysicalCard> minions);

    void addToSkirmish(PhysicalCard card);

    void removeFromSkirmish(PhysicalCard card);

    void finishSkirmish();

    void addTokens(PhysicalCard card, Token token, int count);

    void removeTokens(PhysicalCard card, Token token, int count);

    void sendMessage(String message);

    void setSite(PhysicalCard card);

    void sendGameStats(GameStats gameStats);

    void cardAffectedByCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCard);

    void eventPlayed(PhysicalCard card);

    void cardActivated(String playerPerforming, PhysicalCard card);

    void decisionRequired(String playerId, AwaitingDecision awaitingDecision);

    void sendWarning(String playerId, String warning);

    void endGame();
}
