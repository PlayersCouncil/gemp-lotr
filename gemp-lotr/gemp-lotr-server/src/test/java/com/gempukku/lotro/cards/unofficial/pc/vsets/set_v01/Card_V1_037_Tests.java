
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("fell", "101_37");
					put("fell2", "101_37");
					put("nazgul", "1_232");
					put("blade", "1_216");
					put("ring", "9_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGreatRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("fell", "101_37");
					put("nazgul", "1_232");
					put("blade", "1_216");
					put("ring", "9_44");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "3_118"); // The Great River: cards may not be played from draw decks or discard piles
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FellVoicesCallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Fell Voices Call
		* Side: Free Peoples
		* Culture: ringwraith
		* Twilight Cost: 0
		* Type: event
		* Subtype: Shadow
		* Game Text: Play a [ringwraith] item from your draw deck or discard pile.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl fell = scn.GetFreepsCard("fell");

		assertFalse(fell.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fell.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, fell.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fell.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, fell.getBlueprint().getRace());
        assertTrue(scn.HasTimeword(fell, Timeword.SHADOW)); // test for keywords as needed
		assertEquals(0, fell.getBlueprint().getTwilightCost());
		//assertEquals(, fell.getBlueprint().getStrength());
		//assertEquals(, fell.getBlueprint().getVitality());
		//assertEquals(, fell.getBlueprint().getResistance());
		//assertEquals(Signet., fell.getBlueprint().getSignet());
		//assertEquals(, fell.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void FellVoicesCallPullsItemsFromDiscardOrDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl fell = scn.GetShadowCard("fell");
		PhysicalCardImpl fell2 = scn.GetShadowCard("fell2");
		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");
		PhysicalCardImpl blade = scn.GetShadowCard("blade");
		PhysicalCardImpl ring = scn.GetShadowCard("ring");
		scn.MoveCardsToHand(fell, fell2);
		scn.MoveCompanionsToTable(nazgul);
		scn.MoveCardsToDiscard(ring);
		scn.MoveCardsToTopOfDeck(blade);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(fell));
		assertEquals(Zone.DISCARD, ring.getZone());
		assertEquals(Zone.DECK, blade.getZone());
		scn.ShadowPlayCard(fell);

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.ShadowGetMultipleChoices().size());
		scn.ShadowChooseOption("discard");
		assertEquals(Zone.ATTACHED, ring.getZone());
		assertEquals(nazgul, ring.getAttachedTo());

		assertTrue(scn.ShadowPlayAvailable(fell2));
		scn.ShadowPlayCard(fell2);
		scn.ShadowDismissRevealedCards();

        scn.ShadowChooseCardBPFromSelection(blade);
		assertEquals(Zone.ATTACHED, blade.getZone());
		assertEquals(nazgul, blade.getAttachedTo());

	}

	@Test
	public void FellVoicesCallCannotBePlayedAtTheGreatRiver() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1080: both branches play from a forbidden zone, but the event was still offered and the
		// Choice fallback let the player pick one anyway.
		VirtualTableScenario scn = GetGreatRiverScenario();

		PhysicalCardImpl fell = scn.GetShadowCard("fell");
		PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");
		PhysicalCardImpl blade = scn.GetShadowCard("blade");
		PhysicalCardImpl ring = scn.GetShadowCard("ring");
		scn.MoveCardsToDiscard(ring);

		scn.StartGame();
		scn.SkipToSite(6);
		// Placed after the traversal so the intervening draws and discards do not disturb them
		scn.MoveCardsToHand(fell);
		scn.MoveCardsToTopOfDeck(blade);
		scn.MoveMinionsToTable(nazgul);
		scn.FreepsPassCurrentPhaseAction();          // move 6 -> 7
		if (scn.ShadowDecisionAvailable("Choose site to play"))
			scn.ShadowChooseAnyCard();
		scn.SetTwilight(10);

		assertEquals("The Great River", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(Zone.DISCARD, ring.getZone());
		assertEquals(Zone.DECK, blade.getZone());
		assertFalse(scn.ShadowPlayAvailable(fell));
	}
}
