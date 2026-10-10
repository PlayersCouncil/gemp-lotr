package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RoamingAtTest extends AbstractAtTest {
    @Test
    public void payingForRoamingMinionWithoutRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl orcChieftain = new PhysicalCardImpl(100, "1_266", P2, _cardLibrary.getLotroCardBlueprint("1_266"));
        PhysicalCardImpl sauronsHatred = new PhysicalCardImpl(100, "7_310", P2, _cardLibrary.getLotroCardBlueprint("7_310"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, sauronsHatred, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, orcChieftain, Zone.HAND);
        _game.getGameState().addThreats(P1, 1);
        // End fellowship phase
        playerDecided(P1, "");

        assertEquals(2, _game.getGameState().getTwilightPool());
        // Can't play the Orc Chieftain but can use the Sauron's Hatred
        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + sauronsHatred.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, "0");

        // Orc Chieftain in play
        assertEquals(Zone.SHADOW_CHARACTERS, orcChieftain.getZone());
        assertEquals(0, _game.getGameState().getTwilightPool());
        assertTrue(_game.getModifiersQuerying().hasKeyword(_game, orcChieftain, Keyword.ROAMING));
    }
}
