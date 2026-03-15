package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_096_Tests
{

// ----------------------------------------
// ENDLESS NIGHT TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("night", "103_96");       // Endless Night
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");
					put("sky3", "103_97");
					put("sky4", "103_97");
					put("darkness", "103_93");

					put("orc", "1_271");          // Orc Soldier - Orc
					put("troll", "6_106");        // Troll of Udun - Troll
					put("witchking", "8_84");     // The Witch-king - Nazgul
					put("southron", "4_250");     // Southron Explorer - Man
					put("uruk", "1_151");         // Uruk Savage - Uruk-hai

					put("sauron", "9_48");        // Sauron, The Lord of the Rings - cost 18
					put("hollowing", "3_54");     // Hollowing of Isengard - Shadow condition
					put("ithilstone", "9_47");    // Ithil Stone - Shadow artifact

					put("gandalf", "1_364");      // Gandalf, The Grey Wizard
					put("sleep", "51_84");        // Sleep, Caradhras - discards Shadow condition, hinders all conditions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EndlessNightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Endless Night
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight. To play, hinder 4 twilight conditions. This cannot be discarded or hindered. Orcs gain <b>fierce</b>. Trolls gain <b>enduring</b>. Nazgul gain <b>damage +1</b>. Your Men and Uruk-hai gain <b>archer</b>. 
		* 	Shadow: Hinder X of your other Shadow support cards to play Sauron from your hand or discard pile; he is twilight cost -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("night");

		assertEquals("Endless Night", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}



// ========================================
// EXTRA COST TESTS
// ========================================

	@Test
	public void EndlessNightRequiresAndHinders4TwilightConditionsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var darkness = scn.GetShadowCard("darkness");
		scn.MoveCardsToHand(night, sky1, sky2, sky3);
		scn.MoveCardsToSupportArea(darkness); //cheating this one in

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		// 1 twilight condition in play - can't play
		assertFalse(scn.ShadowPlayAvailable(night));

		// Play twilight conditions one by one
		scn.ShadowPlayCard(sky1);
		assertFalse(scn.ShadowPlayAvailable(night));

		scn.ShadowPlayCard(sky2);
		assertFalse(scn.ShadowPlayAvailable(night));

		scn.ShadowPlayCard(sky3);

		// Four twilight conditions - now can play
		assertTrue(scn.ShadowPlayAvailable(night));

		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(sky3));
		assertFalse(scn.IsHindered(darkness));

		scn.ShadowPlayCard(night);
		// Only 4 twilight conditions, so auto-selected

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(sky3));
		assertTrue(scn.IsHindered(darkness));
		assertInZone(Zone.SUPPORT, night);
	}

// ========================================
// PROTECTION MODIFIER TESTS
// ========================================

	@Test
	public void EndlessNightCannotBeDiscardedOrHindered() throws DecisionResultInvalidException, CardNotFoundException {
		// Sleep, Caradhras: "Exert Gandalf to discard a Shadow condition and then hinder all conditions"
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		var hollowing = scn.GetShadowCard("hollowing");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToSupportArea(night, sky1, sky2, sky3, sky4, hollowing);
		scn.HinderCard(sky1, sky2, sky3, sky4);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, night);
		assertInZone(Zone.SUPPORT, hollowing);
		assertFalse(scn.IsHindered(night));
		assertFalse(scn.IsHindered(hollowing));

		scn.FreepsPlayCard(sleep);

		scn.FreepsChooseCard(hollowing);

		// Hollowing discarded automatically as the only valid target, night still in play
		assertInDiscard(hollowing);
		assertInZone(Zone.SUPPORT, night);

		// Now all conditions get hindered - but night should be protected
		// sky1-4 already hindered, night should remain unhindered
		assertFalse(scn.IsHindered(night));
	}

// ========================================
// KEYWORD MODIFIER TESTS
// ========================================

	@Test
	public void EndlessNightGrantsKeywordsToMinionTypes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		var orc = scn.GetShadowCard("orc");
		var troll = scn.GetShadowCard("troll");
		var witchking = scn.GetShadowCard("witchking");
		var southron = scn.GetShadowCard("southron");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(orc, troll, witchking, southron, uruk);

		scn.StartGame();

		// Verify minions don't have keywords before Endless Night is in play
		assertFalse(scn.HasKeyword(orc, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troll, Keyword.ENDURING));
		int witchkingDamageBefore = scn.GetKeywordCount(witchking, Keyword.DAMAGE);
		assertFalse(scn.HasKeyword(southron, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(uruk, Keyword.ARCHER));

		// Put Endless Night into play (cheat it in since we're testing modifiers, not play cost)
		scn.MoveCardsToSupportArea(night, sky1, sky2, sky3, sky4);
		scn.HinderCard(sky1, sky2, sky3, sky4);

		// Now verify keywords are granted
		assertTrue(scn.HasKeyword(orc, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(troll, Keyword.ENDURING));
		assertEquals(witchkingDamageBefore + 1, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(southron, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(uruk, Keyword.ARCHER));
	}

// ========================================
// SHADOW ABILITY TESTS - Play Sauron with Discount
// ========================================

	@Test
	public void EndlessNightShadowAbilityPlaysFromHandWithDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		var sauron = scn.GetShadowCard("sauron");
		var hollowing = scn.GetShadowCard("hollowing");
		var ithilstone = scn.GetShadowCard("ithilstone");
		scn.MoveCardsToSupportArea(night, sky1, sky2, sky3, sky4, hollowing, ithilstone);
		scn.HinderCard(sky1, sky2, sky3, sky4);
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		// Sauron costs 18, we need enough twilight even with discount
		// If we hinder all 4 unhindered support cards (night can't be hindered, so: hollowing, ithilstone, and night itself?)
		// Wait - night can't be hindered. So valid targets are: hollowing, ithilstone
		// That's only 2 discount, so we need 18 - 2 = 16 twilight minimum
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowDeclineOptionalTrigger(); //Ithil Stone's draw

		int twilightBefore = scn.GetTwilight();
		assertInZone(Zone.HAND, sauron);
		assertFalse(scn.IsHindered(hollowing));
		assertFalse(scn.IsHindered(ithilstone));

		assertTrue(scn.ShadowActionAvailable(night));
		scn.ShadowUseCardAction(night);

		// Choose how many cards to hinder (0 to X)
		// Should be able to select hollowing and ithilstone, but NOT night (can't be hindered)
		// and NOT already-hindered sky cards
		assertTrue(scn.ShadowHasCardChoicesAvailable(hollowing, ithilstone));
		assertFalse(scn.ShadowHasCardChoiceAvailable(night));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sky1)); // Already hindered

		scn.ShadowChooseCards(hollowing, ithilstone); // Hinder both for -2 discount

		// Sauron costs 18 - 2 discount + 2 for roaming = 18
		assertTrue(scn.IsHindered(hollowing));
		assertTrue(scn.IsHindered(ithilstone));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(twilightBefore - 16 - 2, scn.GetTwilight());
	}

	@Test
	public void EndlessNightShadowAbilityPlaysFromDiscardWithDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		var sauron = scn.GetShadowCard("sauron");
		var hollowing = scn.GetShadowCard("hollowing");
		var ithilstone = scn.GetShadowCard("ithilstone");
		scn.MoveCardsToSupportArea(night, sky1, sky2, sky3, sky4, hollowing, ithilstone);
		scn.HinderCard(sky1, sky2, sky3, sky4);
		scn.MoveCardsToDiscard(sauron);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowDeclineOptionalTrigger(); //Ithil Stone's draw

		int twilightBefore = scn.GetTwilight();
		assertInZone(Zone.DISCARD, sauron);

		scn.ShadowUseCardAction(night);
		scn.ShadowChooseCards(hollowing, ithilstone);

		assertTrue(scn.IsHindered(hollowing));
		assertTrue(scn.IsHindered(ithilstone));
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(twilightBefore - 16 - 2, scn.GetTwilight());
	}

	@Test
	public void EndlessNightShadowAbilityCanHinderZeroCardsIfEnoughTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(night, sky1, sky2, sky3, sky4);
		scn.HinderCard(sky1, sky2, sky3, sky4);
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.SetTwilight(25); // Enough for full 18 + 2 cost
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowUseCardAction(night);

		// Choose to hinder 0 cards (decline selection)
		scn.ShadowDeclineChoosing();

		// Sauron costs full 18 + 2 roaming
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertEquals(twilightBefore - 18 - 2, scn.GetTwilight());
	}

	@Test
	public void EndlessNightShadowAbilityNotAvailableIfSauronNotPlayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(night, sky1, sky2, sky3, sky4);
		scn.HinderCard(sky1, sky2, sky3, sky4);
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.SetTwilight(10); // Not enough for Sauron even with max discount (18 - 0 hindered = 18)
		scn.FreepsPassCurrentPhaseAction();

		// Ability should not be available - can't afford Sauron
		assertFalse(scn.ShadowActionAvailable(night));
	}

	@Test
	public void EndlessNightShadowAbilityNotAvailableIfSauronNotInHandOrDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var night = scn.GetShadowCard("night");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(night, sky1, sky2, sky3, sky4);
		scn.HinderCard(sky1, sky2, sky3, sky4);
		// Sauron stays in deck

		scn.StartGame();
		scn.SetTwilight(25);
		scn.FreepsPassCurrentPhaseAction();

		assertInZone(Zone.DECK, sauron);
		assertFalse(scn.ShadowActionAvailable(night));
	}
}
