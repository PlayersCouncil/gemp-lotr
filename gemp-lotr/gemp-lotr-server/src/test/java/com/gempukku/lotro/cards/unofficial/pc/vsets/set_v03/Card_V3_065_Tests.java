package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_065_Tests
{

// ----------------------------------------
// COVER OF DARKNESS, OMEN OF GLOOM TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gloom", "103_65");       // Cover of Darkness, Omen of Gloom
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");        // Second copy
					put("sky3", "103_97");        // Third copy (in deck for fetching)
					put("marshwight", "102_61");  // Marsh Wight - Twilight [Sauron] Wraith minion
					put("rwintwilight", "101_40"); // Ringwraith in Twilight - Twilight [Ringwraith] Nazgul minion
					put("orc", "1_271");          // Orc Soldier - non-Twilight minion
					put("hollowing", "3_54");     // Hollowing of Isengard - non-Twilight condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Gloom
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		* 	To play, hinder 2 twilight conditions.
		* 	Shadow: Hinder this condition to take a twilight card into hand from your draw deck.
		* 	Regroup: Hinder a twilight card to add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gloom");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Gloom", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



// ========================================
// EXTRA COST TESTS
// ========================================

	@Test
	public void OmenOfGloomRequiresAndHinders2TwilightConditionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToHand(gloom, sky1, sky2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// No twilight conditions in play - can't play
		assertFalse(scn.ShadowPlayAvailable(gloom));

		// One twilight condition - still can't play
		scn.ShadowPlayCard(sky1);
		assertFalse(scn.ShadowPlayAvailable(gloom));

		// Two twilight conditions - now can play
		scn.ShadowPlayCard(sky2);
		assertTrue(scn.ShadowPlayAvailable(gloom));

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		scn.ShadowPlayCard(gloom);
		// Only 2 twilight conditions, so auto-selected

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertInZone(Zone.SUPPORT, gloom);
	}

// ========================================
// SHADOW ABILITY TESTS - Fetch Twilight Card
// ========================================

	@Test
	public void OmenOfGloomShadowAbilityHindersSelfToFetchTwilightCardFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3"); // In deck
		var marshwight = scn.GetShadowCard("marshwight"); // Twilight minion in deck
		var hollowing = scn.GetShadowCard("hollowing"); // Non-twilight in deck
		scn.MoveCardsToSupportArea(gloom, sky1, sky2);
		scn.HinderCard(sky1, sky2);
		// sky3, marshwight, hollowing remain in deck

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.IsHindered(gloom));
		assertInZone(Zone.DECK, sky3);
		assertInZone(Zone.DECK, marshwight);

		assertTrue(scn.ShadowActionAvailable(gloom));
		scn.ShadowUseCardAction(gloom);
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.IsHindered(gloom));

		// Should see twilight cards available (sky3 and marshwight), but not hollowing
		assertTrue(scn.ShadowHasCardChoicesAvailable(sky3, marshwight));
		assertFalse(scn.ShadowHasCardChoiceAvailable(hollowing));

		scn.ShadowChooseCardBPFromSelection(marshwight);

		assertInZone(Zone.HAND, marshwight);
		assertInZone(Zone.DECK, sky3);
	}

	@Test
	public void OmenOfGloomShadowAbilityCannotBeUsedIfSelfAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToSupportArea(gloom, sky1, sky2);
		scn.HinderCard(sky1, sky2, gloom); // Gloom already hindered

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.ShadowActionAvailable(gloom));
	}


// ========================================
// REGROUP ABILITY TESTS - Hinder Twilight to Add Twilight
// ========================================

	@Test
	public void OmenOfGloomRegroupAbilityCanHinderTwilightConditionsAndMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloom = scn.GetShadowCard("gloom");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var marshwight = scn.GetShadowCard("marshwight");
		scn.MoveCardsToSupportArea(gloom, sky1, sky2);
		scn.MoveMinionsToTable(marshwight);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		// First use - hinder sky1
		scn.ShadowUseCardAction(gloom);
		scn.ShadowChooseCard(sky1);
		assertEquals(twilightBefore + 1, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();

		// Second use - hinder sky2
		scn.ShadowUseCardAction(gloom);
		scn.ShadowChooseCard(sky2);
		assertEquals(twilightBefore + 2, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();

		// Third use - hinder marshwight (twilight minion)
		scn.ShadowUseCardAction(gloom);
		scn.ShadowChooseCard(marshwight);
		assertEquals(twilightBefore + 3, scn.GetTwilight());

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(gloom);
		//gloom auto-selected as only remaining unhindered twilight card
		assertEquals(twilightBefore + 4, scn.GetTwilight());

		// Fourth use - hinder gloom itself

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(gloom));
		assertTrue(scn.IsHindered(marshwight));
	}
}
