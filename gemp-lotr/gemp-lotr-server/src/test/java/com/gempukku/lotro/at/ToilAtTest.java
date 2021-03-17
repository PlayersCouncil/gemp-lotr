package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class ToilAtTest extends AbstractAtTest {
    @Test
    public void cantPlayIfNotEnoughAndCantExertAnything() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addTwilight(5);

        playerDecided(P1, "");

        assertEquals(7, _game.getGameState().getTwilightPool());

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[0], (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));
    }

    @Test
    public void canPlayIfNotEnoughAndCantExertAnything() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addTwilight(6);

        playerDecided(P1, "");

        assertEquals(8, _game.getGameState().getTwilightPool());

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + legionOfHarad.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, "0");

        assertEquals(Zone.SHADOW_CHARACTERS, legionOfHarad.getZone());
        assertEquals(0, _game.getGameState().getTwilightPool());
    }

    @Test
    public void cantPlayIfNotEnoughAndCanExertOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));
        PhysicalCardImpl corpsOfHarad = new PhysicalCardImpl(101, "11_73", P2, _library.getLotroCardBlueprint("11_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corpsOfHarad, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTwilight(3);

        playerDecided(P1, "");

        assertEquals(5, _game.getGameState().getTwilightPool());

        PhysicalCard kingsTent = _game.getGameState().getSite(2);

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + kingsTent.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));
    }

    @Test
    public void canPlayIfNotEnoughAndCanExertOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));
        PhysicalCardImpl corpsOfHarad = new PhysicalCardImpl(101, "11_73", P2, _library.getLotroCardBlueprint("11_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corpsOfHarad, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTwilight(4);

        playerDecided(P1, "");

        assertEquals(6, _game.getGameState().getTwilightPool());

        PhysicalCard kingsTent = _game.getGameState().getSite(2);

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + kingsTent.getCardId(), "" + legionOfHarad.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, getCardActionId(shadowPhaseDecision, "Play "));

        assertEquals(Zone.SHADOW_CHARACTERS, legionOfHarad.getZone());
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad));
        assertEquals(0, _game.getGameState().getTwilightPool());
    }

    @Test
    public void canPlayIfEnoughAndCanExertOneMinionExertingLowers() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));
        PhysicalCardImpl corpsOfHarad = new PhysicalCardImpl(101, "11_73", P2, _library.getLotroCardBlueprint("11_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corpsOfHarad, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTwilight(6);

        playerDecided(P1, "");

        assertEquals(8, _game.getGameState().getTwilightPool());

        PhysicalCard kingsTent = _game.getGameState().getSite(2);

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + kingsTent.getCardId(), "" + legionOfHarad.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, getCardActionId(shadowPhaseDecision, "Play "));

        AwaitingDecision toilExertion = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, toilExertion.getDecisionType());
        assertEquals("0", toilExertion.getDecisionParameters().get("min")[0]);
        assertEquals("1", toilExertion.getDecisionParameters().get("max")[0]);
        validateContents(new String[]{"" + corpsOfHarad.getCardId()}, (String[]) toilExertion.getDecisionParameters().get("cardId"));

        playerDecided(P2, "" + corpsOfHarad.getCardId());

        assertEquals(Zone.SHADOW_CHARACTERS, legionOfHarad.getZone());
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad));
        assertEquals(2, _game.getGameState().getTwilightPool());
    }

    @Test
    public void canPlayIfEnoughAndCanExertOneMinionNotExertingDoesntLower() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));
        PhysicalCardImpl corpsOfHarad = new PhysicalCardImpl(101, "11_73", P2, _library.getLotroCardBlueprint("11_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corpsOfHarad, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTwilight(6);

        playerDecided(P1, "");

        assertEquals(8, _game.getGameState().getTwilightPool());

        PhysicalCard kingsTent = _game.getGameState().getSite(2);

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + kingsTent.getCardId(), "" + legionOfHarad.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, getCardActionId(shadowPhaseDecision, "Play "));

        AwaitingDecision toilExertion = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, toilExertion.getDecisionType());
        assertEquals("0", toilExertion.getDecisionParameters().get("min")[0]);
        assertEquals("1", (String) toilExertion.getDecisionParameters().get("max")[0]);
        validateContents(new String[]{"" + corpsOfHarad.getCardId()}, (String[]) toilExertion.getDecisionParameters().get("cardId"));

        playerDecided(P2, "");

        assertEquals(Zone.SHADOW_CHARACTERS, legionOfHarad.getZone());
        assertEquals(0, _game.getGameState().getWounds(corpsOfHarad));
        assertEquals(0, _game.getGameState().getTwilightPool());
    }

    @Test
    public void canPlayIfEnoughAndCanExertTwoMinionsExertingBothLowers() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));
        PhysicalCardImpl corpsOfHarad1 = new PhysicalCardImpl(101, "11_73", P2, _library.getLotroCardBlueprint("11_73"));
        PhysicalCardImpl corpsOfHarad2 = new PhysicalCardImpl(102, "11_73", P2, _library.getLotroCardBlueprint("11_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corpsOfHarad1, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, corpsOfHarad2, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTwilight(6);

        playerDecided(P1, "");

        assertEquals(8, _game.getGameState().getTwilightPool());

        PhysicalCard kingsTent = _game.getGameState().getSite(2);

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + kingsTent.getCardId(), "" + legionOfHarad.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, getCardActionId(shadowPhaseDecision, "Play "));

        AwaitingDecision toilExertion = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, toilExertion.getDecisionType());
        assertEquals("0", (String) toilExertion.getDecisionParameters().get("min")[0]);
        assertEquals("2", (String) toilExertion.getDecisionParameters().get("max")[0]);
        validateContents(new String[]{"" + corpsOfHarad1.getCardId(), "" + corpsOfHarad2.getCardId()}, (String[]) toilExertion.getDecisionParameters().get("cardId"));

        playerDecided(P2, corpsOfHarad1.getCardId() + "," + corpsOfHarad2.getCardId());

        assertEquals(Zone.SHADOW_CHARACTERS, legionOfHarad.getZone());
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad1));
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad2));
        assertEquals(4, _game.getGameState().getTwilightPool());
    }

    @Test
    public void canPlayIfEnoughAndCanExertTwoMinionsExertingOneLowers() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));
        PhysicalCardImpl corpsOfHarad1 = new PhysicalCardImpl(101, "11_73", P2, _library.getLotroCardBlueprint("11_73"));
        PhysicalCardImpl corpsOfHarad2 = new PhysicalCardImpl(102, "11_73", P2, _library.getLotroCardBlueprint("11_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corpsOfHarad1, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, corpsOfHarad2, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTwilight(6);

        playerDecided(P1, "");

        assertEquals(8, _game.getGameState().getTwilightPool());

        PhysicalCard kingsTent = _game.getGameState().getSite(2);

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + kingsTent.getCardId(), "" + legionOfHarad.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, getCardActionId(shadowPhaseDecision, "Play "));

        AwaitingDecision toilExertion = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, toilExertion.getDecisionType());
        assertEquals("0", (String) toilExertion.getDecisionParameters().get("min")[0]);
        assertEquals("2", (String) toilExertion.getDecisionParameters().get("max")[0]);
        validateContents(new String[]{"" + corpsOfHarad1.getCardId(), "" + corpsOfHarad2.getCardId()}, (String[]) toilExertion.getDecisionParameters().get("cardId"));

        playerDecided(P2, "" + corpsOfHarad1.getCardId());

        assertEquals(Zone.SHADOW_CHARACTERS, legionOfHarad.getZone());
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad1));
        assertEquals(0, _game.getGameState().getWounds(corpsOfHarad2));
        assertEquals(2, _game.getGameState().getTwilightPool());
    }

    @Test
    public void canPlayIfEnoughAndCanExertMinionsBelowZero() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<String, Collection<String>>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl legionOfHarad = new PhysicalCardImpl(100, "11_88", P2, _library.getLotroCardBlueprint("11_88"));
        PhysicalCardImpl corpsOfHarad1 = new PhysicalCardImpl(101, "11_73", P2, _library.getLotroCardBlueprint("11_73"));
        PhysicalCardImpl corpsOfHarad2 = new PhysicalCardImpl(102, "11_73", P2, _library.getLotroCardBlueprint("11_73"));
        PhysicalCardImpl corpsOfHarad3 = new PhysicalCardImpl(103, "11_73", P2, _library.getLotroCardBlueprint("11_73"));
        PhysicalCardImpl corpsOfHarad4 = new PhysicalCardImpl(104, "11_73", P2, _library.getLotroCardBlueprint("11_73"));
        PhysicalCardImpl corpsOfHarad5 = new PhysicalCardImpl(105, "11_73", P2, _library.getLotroCardBlueprint("11_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, legionOfHarad, Zone.HAND);
        _game.getGameState().addCardToZone(_game, corpsOfHarad1, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, corpsOfHarad2, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, corpsOfHarad3, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, corpsOfHarad4, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, corpsOfHarad5, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTwilight(6);

        playerDecided(P1, "");

        assertEquals(8, _game.getGameState().getTwilightPool());

        PhysicalCard kingsTent = _game.getGameState().getSite(2);

        AwaitingDecision shadowPhaseDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowPhaseDecision.getDecisionType());
        validateContents(new String[]{"" + kingsTent.getCardId(), "" + legionOfHarad.getCardId()}, (String[]) shadowPhaseDecision.getDecisionParameters().get("cardId"));

        playerDecided(P2, getCardActionId(shadowPhaseDecision, "Play "));

        AwaitingDecision toilExertion = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_SELECTION, toilExertion.getDecisionType());
        assertEquals("0", (String) toilExertion.getDecisionParameters().get("min")[0]);
        assertEquals("5", (String) toilExertion.getDecisionParameters().get("max")[0]);
        validateContents(new String[]{"" + corpsOfHarad1.getCardId(), "" + corpsOfHarad2.getCardId(), "" + corpsOfHarad3.getCardId(), "" + corpsOfHarad4.getCardId(), "" + corpsOfHarad5.getCardId()}, (String[]) toilExertion.getDecisionParameters().get("cardId"));

        playerDecided(P2, corpsOfHarad1.getCardId() + "," + corpsOfHarad2.getCardId() + "," + corpsOfHarad3.getCardId() + "," + corpsOfHarad4.getCardId() + "," + corpsOfHarad5.getCardId());

        assertEquals(Zone.SHADOW_CHARACTERS, legionOfHarad.getZone());
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad1));
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad2));
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad3));
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad4));
        assertEquals(1, _game.getGameState().getWounds(corpsOfHarad5));
        // It's 6 not 8, because of roaming penalty
        assertEquals(6, _game.getGameState().getTwilightPool());
    }
}
