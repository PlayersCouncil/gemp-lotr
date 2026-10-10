package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TimingAtTest extends AbstractAtTest {
    @Test
    public void playEffectFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCard gollum = _game.getGameState().createPhysicalCard(P2, _cardLibrary, "7_58");

        _game.getGameState().addCardToZone(_game, gollum, Zone.DISCARD);

        skipMulligans();

        _game.getGameState().addTwilight(10);

        playerDecided(P1, "");

        _userFeedback.getAwaitingDecision(P2);

        AwaitingDecision shadowPhaseAction = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseAction.getDecisionType());
        validateContents(new String[]{"7_58"}, (String[]) shadowPhaseAction.getDecisionParameters().get("blueprintId"));

        playerDecided(P2, "0");

        assertEquals(Zone.SHADOW_CHARACTERS, gollum.getZone());
    }

    @Test
    public void extraCostToPlay() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        skipMulligans();

        PhysicalCard balinAvenged = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "17_2");
        PhysicalCard prowlingOrc = _game.getGameState().createPhysicalCard(P2, _cardLibrary, "11_136");
        PhysicalCard dwarvenGuard1 = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "1_7");
        PhysicalCard dwarvenGuard2 = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "1_7");
        PhysicalCard prowlingOrcInDiscard = _game.getGameState().createPhysicalCard(P2, _cardLibrary, "11_136");

        _game.getGameState().addCardToZone(_game, balinAvenged, Zone.SUPPORT);
        _game.getGameState().addTokens(balinAvenged, Token.DWARVEN, 4);
        _game.getGameState().addCardToZone(_game, dwarvenGuard1, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, dwarvenGuard2, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, prowlingOrc, Zone.HAND);
        _game.getGameState().addCardToZone(_game, prowlingOrcInDiscard, Zone.DISCARD);
        _game.getGameState().addTwilight(10);

        // End fellowship phase
        playerDecided(P1, "");

        AwaitingDecision shadowDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowDecision.getDecisionType());
        validateContents(new String[]{"" + prowlingOrc.getCardId()}, (String[]) shadowDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, "0");

        assertEquals(Zone.REMOVED, prowlingOrcInDiscard.getZone());
        assertEquals(Zone.SHADOW_CHARACTERS, prowlingOrc.getZone());
    }

}
