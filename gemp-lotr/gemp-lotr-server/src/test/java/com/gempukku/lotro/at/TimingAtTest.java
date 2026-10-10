package com.gempukku.lotro.at;

import com.gempukku.lotro.cards.build.LotroCardBlueprintBuilder;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TimingAtTest extends AbstractAtTest {
    @Test
    public void playStartingFellowshipWithDiscount() throws DecisionResultInvalidException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        extraCards.put(P1, Arrays.asList("7_88", "6_121"));
        initializeSimplestGame(extraCards);

        // Play first character
        AwaitingDecision firstCharacterDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, firstCharacterDecision.getDecisionType());
        validateContents(new String[]{"7_88", "6_121"}, ((String[]) firstCharacterDecision.getDecisionParameters().get("blueprintId")));

        playerDecided(P1, getArbitraryCardId(firstCharacterDecision, "7_88"));

        // Play second character with discount
        AwaitingDecision secondCharacterDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, secondCharacterDecision.getDecisionType());
        validateContents(new String[]{"6_121"}, ((String[]) secondCharacterDecision.getDecisionParameters().get("blueprintId")));

        playerDecided(P1, getArbitraryCardId(secondCharacterDecision, "6_121"));
    }

    @Test
    public void playStartingFellowshipWithDiscountFromCardItself() throws DecisionResultInvalidException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        extraCards.put(P1, Arrays.asList("4_265", "4_267"));
        initializeSimplestGame(extraCards);

        // Play first character
        AwaitingDecision firstCharacterDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, firstCharacterDecision.getDecisionType());
        validateContents(new String[]{"4_265", "4_267"}, (firstCharacterDecision.getDecisionParameters().get("blueprintId")));

        playerDecided(P1, getArbitraryCardId(firstCharacterDecision, "4_265"));

        // Play second character with discount
        AwaitingDecision secondCharacterDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, secondCharacterDecision.getDecisionType());
        validateContents(new String[]{"4_267"}, ( secondCharacterDecision.getDecisionParameters().get("blueprintId")));

        playerDecided(P1, getArbitraryCardId(secondCharacterDecision, "4_267"));
    }

    @Test
    public void playStartingFellowshipWithSpotRequirement() throws DecisionResultInvalidException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        extraCards.put(P1, Arrays.asList("1_50", "1_48"));
        initializeSimplestGame(extraCards);

        // Play first character
        AwaitingDecision firstCharacterDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, firstCharacterDecision.getDecisionType());
        validateContents(new String[]{"1_50"}, ((String[]) firstCharacterDecision.getDecisionParameters().get("blueprintId")));

        playerDecided(P1, getArbitraryCardId(firstCharacterDecision, "1_50"));

        // Play second character with spot requirement
        AwaitingDecision secondCharacterDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.ARBITRARY_CARDS, secondCharacterDecision.getDecisionType());
        validateContents(new String[]{"1_48"}, ((String[]) secondCharacterDecision.getDecisionParameters().get("blueprintId")));

        playerDecided(P1, getArbitraryCardId(secondCharacterDecision, "1_48"));
    }

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
    public void playEffectFromStacked() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCard gimli = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "1_13");
        PhysicalCard letThemCome = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "1_20");
        PhysicalCard slakedThirsts = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "7_14");

        PhysicalCard gollum = _game.getGameState().createPhysicalCard(P2, _cardLibrary, "7_58");

        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, letThemCome, Zone.SUPPORT);
        _game.getGameState().stackCard(_game, slakedThirsts, letThemCome);

        skipMulligans();

        // End fellowship phase
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, gollum, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        playerDecided(P2, "");

        AwaitingDecision maneuverPhaseAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, maneuverPhaseAction.getDecisionType());
        validateContents(new String[]{"Use Slaked Thirsts"}, (String[]) maneuverPhaseAction.getDecisionParameters().get("actionText"));

        playerDecided(P1, "0");

        assertEquals(Zone.DISCARD, slakedThirsts.getZone());
        assertEquals(2, _game.getGameState().getWounds(gollum));
    }

    @Test
    public void stackedCardAffectsGame() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCard gimli = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "1_13");
        PhysicalCard letThemCome = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "1_20");
        PhysicalCard tossMe = _game.getGameState().createPhysicalCard(P1, _cardLibrary, "6_11");

        skipMulligans();

        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, letThemCome, Zone.SUPPORT);
        _game.getGameState().stackCard(_game, tossMe, letThemCome);

        assertEquals(7, _game.getModifiersQuerying().getStrength(_game, gimli));
    }

    @Test
    public void movementSiteAffecting() throws Exception {
        initializeSimplestGame();

        skipMulligans();

        LotroCardBlueprintBuilder builder = new LotroCardBlueprintBuilder();
        Map<String, LotroCardBlueprint> cards = LotroCardBlueprintLibrary.loadCardsFromFile(builder, TimingAtTest.class.getResourceAsStream("/siteTest.hjson"));

        PhysicalCardImpl moveFromSite = new PhysicalCardImpl(100, "0_1234", P1, cards.get("0_1234"));
        moveFromSite.setSiteNumber(1);
        PhysicalCardImpl moveToSite = new PhysicalCardImpl(100, "0_1235", P1, cards.get("0_1235"));
        moveToSite.setSiteNumber(2);

        _game.getGameState().removeCardsFromZone(P1, Collections.singleton(_game.getGameState().getCurrentSite()));
        _game.getGameState().addCardToZone(_game, moveFromSite, Zone.ADVENTURE_PATH);
        _game.getGameState().addCardToZone(_game, moveToSite, Zone.ADVENTURE_PATH);

        assertTrue(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.RING_TEXT_INACTIVE));
        assertFalse(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.CANT_PREVENT_WOUNDS));

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        assertFalse(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.RING_TEXT_INACTIVE));
        assertTrue(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.CANT_PREVENT_WOUNDS));

        // Pass in Regroup phase
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
        playerDecided(P2, "");

        // Decide not to move
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, getMultipleDecisionIndex(_userFeedback.getAwaitingDecision(P1), "No"));

        // Fellowship of player2
        assertTrue(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.RING_TEXT_INACTIVE));
        assertFalse(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.CANT_PREVENT_WOUNDS));

        // End fellowship phase
        playerDecided(P2, "");

        // End shadow phase
        playerDecided(P1, "");

        assertFalse(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.RING_TEXT_INACTIVE));
        assertTrue(_game.getModifiersQuerying().hasFlagActive(_game, ModifierFlag.CANT_PREVENT_WOUNDS));
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
