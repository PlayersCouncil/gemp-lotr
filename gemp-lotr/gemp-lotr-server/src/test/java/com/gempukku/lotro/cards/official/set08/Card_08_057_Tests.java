package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_057_Tests
{
	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("marauder", "8_57");
                    put("corsair", "8_56");

                    put("cart", "1_73");

                    put("blacksails1", "8_50");
                    put("blacksails2", "8_50");
                }}
		);
	}

	@Test
	public void CorsairMarauderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Marauder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>When you play this minion, if you can spot another corsair, you may discard a possession to add 2 [raider] tokens to a card that already has a [raider] token on it.
		*/

		var scn = GetScenario();

        var card = scn.GetFreepsCard("marauder");

		assertEquals("Corsair Marauder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.CORSAIR));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

    @Test
    public void IfYouCannotSpotAnotherCorsairOnPlayNothingHappens() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = GetScenario();

        var marauder = scn.GetShadowCard("marauder");
        var corsair = scn.GetShadowCard("corsair");
        var blacksails1 = scn.GetShadowCard("blacksails1");
        var blacksails2 = scn.GetShadowCard("blacksails2");
        scn.ShadowMoveCardToHand(marauder, corsair, blacksails1, blacksails2);

        PhysicalCardImpl cart = scn.GetFreepsCard("cart");
        scn.FreepsMoveCardToSupportArea(cart);

        scn.StartGame();

        scn.SetTwilight(10);

        scn.FreepsPassCurrentPhaseAction();

        scn.ShadowPlayCard(blacksails1);
        scn.ShadowAcceptOptionalTrigger();

        assertEquals(1, scn.GetCultureTokensOn(blacksails1));
        assertEquals(Zone.SUPPORT, blacksails1.getZone());
        assertEquals(Zone.SUPPORT, cart.getZone());

        scn.ShadowPlayCard(marauder);

        assertEquals(1, scn.GetCultureTokensOn(blacksails1));
        assertEquals(Zone.SUPPORT, blacksails1.getZone());
        assertEquals(Zone.SUPPORT, cart.getZone());
    }

    @Test
    public void IfAnotherCorsairOnPlayCanDiscardAPossessionToAdd2Tokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

        var marauder = scn.GetShadowCard("marauder");
        var corsair = scn.GetShadowCard("corsair");
        var blacksails1 = scn.GetShadowCard("blacksails1");
        var blacksails2 = scn.GetShadowCard("blacksails2");
        scn.ShadowMoveCardToHand(marauder, blacksails1, blacksails2);
        scn.ShadowMoveCharToTable(corsair);

        var cart = scn.GetFreepsCard("cart");
        scn.FreepsMoveCardToSupportArea(cart);

		scn.StartGame();

        scn.SetTwilight(10);

        scn.FreepsPassCurrentPhaseAction();

        scn.ShadowPlayCard(blacksails1);
        scn.ShadowAcceptOptionalTrigger();
        scn.ShadowPlayCard(blacksails2);
        scn.ShadowDeclineOptionalTrigger();

        assertEquals(1, scn.GetCultureTokensOn(blacksails1));
        assertEquals(0, scn.GetCultureTokensOn(blacksails2));
        assertEquals(Zone.SUPPORT, blacksails1.getZone());
        assertEquals(Zone.SUPPORT, blacksails2.getZone());
        assertEquals(Zone.SUPPORT, cart.getZone());

        scn.ShadowPlayCard(marauder);

        scn.ShadowAcceptOptionalTrigger();
        assertTrue(scn.ShadowDecisionAvailable("Choose cards to discard"));
        assertEquals(3, scn.ShadowGetCardChoiceCount()); //1x Gandalf's Cart, 2x Black Sails

        scn.ShadowChooseCard(cart);

        assertEquals(3, scn.GetCultureTokensOn(blacksails1)); // Only valid choice
        assertEquals(0, scn.GetCultureTokensOn(blacksails2));
        assertEquals(Zone.SUPPORT, blacksails1.getZone());
        assertEquals(Zone.SUPPORT, blacksails2.getZone());
        assertEquals(Zone.DISCARD, cart.getZone());
	}
}
