package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legions", "103_68");
					put("witchking", "1_237");
					put("squealer1", "8_77");   // [Ringwraith] Orc
					put("squealer2", "8_77");
					put("axe1", "7_186");        // [Ringwraith] possession on Morgul Orcs
					put("axe2", "7_186");
					put("sword", "1_218");       // [Ringwraith] possession on Nazgul
					put("runner", "1_178");      // [Moria] Orc - NOT ringwraith

					put("mount", "4_287");       // FP possession
					put("lance", "103_91");      // FP possession
					put("rider", "4_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulLegionsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Morgul Legions
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you play a [ringwraith] Orc, the Free Peoples player must hinder one of their possessions.
		* 	Shadow: Spot a Nazgul to play any number of possessions on your [ringwraith] Orcs from your discard pile.  Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legions");

		assertEquals("Morgul Legions", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}



// ======== TRIGGER TESTS ========

	@Test
	public void MorgulLegionsTriggersFPHinderPossessionOnRingwraithOrcPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var squealer = scn.GetShadowCard("squealer1");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");

		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(squealer);
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.IsHindered(mount));

		scn.ShadowPlayCard(squealer);

		assertTrue(scn.IsHindered(mount));
	}

	@Test
	public void MorgulLegionsDoesNotTriggerOnNonRingwraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var runner = scn.GetShadowCard("runner");  // [Moria] Orc
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");

		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(runner);
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(runner);

		// Should NOT trigger - runner is [Moria], not [Ringwraith]
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.IsHindered(mount));
	}

	@Test
	public void MorgulLegionsFPChoosesWhichPossessionToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var squealer = scn.GetShadowCard("squealer1");
		var mount = scn.GetFreepsCard("mount");
		var lance = scn.GetFreepsCard("lance");
		var rider = scn.GetFreepsCard("rider");

		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(squealer);
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount, lance);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(squealer);

		// FP player should have choice of both possessions
		assertTrue(scn.FreepsHasCardChoicesAvailable(mount, lance));

		// FP chooses mount
		scn.FreepsChooseCard(mount);

		assertTrue(scn.IsHindered(mount));
		assertFalse(scn.IsHindered(lance));
	}

// ======== SHADOW ABILITY TESTS ========

	@Test
	public void MorgulLegionsAbilityRequiresNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var squealer = scn.GetShadowCard("squealer1");
		var axe = scn.GetShadowCard("axe1");

		scn.MoveCardsToSupportArea(legions);
		scn.MoveMinionsToTable(squealer);
		scn.MoveCardsToDiscard(axe);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// No Nazgul - ability should not be available
		assertFalse(scn.ShadowActionAvailable(legions));
	}

	@Test
	public void MorgulLegionsAbilityCanPlayMultiplePossessions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var witchking = scn.GetShadowCard("witchking");
		var squealer1 = scn.GetShadowCard("squealer1");
		var squealer2 = scn.GetShadowCard("squealer2");
		var axe1 = scn.GetShadowCard("axe1");
		var axe2 = scn.GetShadowCard("axe2");
		var sword = scn.GetShadowCard("sword");  // Goes on Nazgul, not Orcs

		scn.MoveCardsToSupportArea(legions);
		scn.MoveMinionsToTable(witchking, squealer1, squealer2);
		scn.MoveCardsToDiscard(axe1, axe2, sword);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(legions);

		assertTrue(scn.ShadowHasCardChoicesAvailable(axe1, axe2));
		// Sword should NOT be offered - it goes on Nazgul, not [ringwraith] Orcs
		assertFalse(scn.ShadowHasCardChoiceAvailable(sword));

		// Play first axe
		scn.ShadowChooseCard(axe1);
		scn.ShadowChooseCard(squealer1);

		// Should be offered to play another
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe2));
		scn.ShadowChooseCard(axe2);
		// auto attack to squealer 2 as 1 is already holding a hand weapon
		//scn.ShadowChooseCard(squealer2);

		assertAttachedTo(axe1, squealer1);
		assertAttachedTo(axe2, squealer2);
		assertInDiscard(legions);

		assertTrue(scn.AwaitingShadowPhaseActions());
	}


	@Test
	public void MorgulLegionsAbilityDiscardsEvenIfNoPossessionsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var witchking = scn.GetShadowCard("witchking");
		var squealer = scn.GetShadowCard("squealer1");
		var axe = scn.GetShadowCard("axe1");

		scn.MoveCardsToSupportArea(legions);
		scn.MoveMinionsToTable(witchking, squealer);
		scn.MoveCardsToDiscard(axe);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(legions);

		// Decline to play axe
		scn.ShadowDeclineOptionalTrigger();

		// Legions still self-discards
		assertInDiscard(legions);
		// Axe still in discard
		assertInDiscard(axe);
	}
}
