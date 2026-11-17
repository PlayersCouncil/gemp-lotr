package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_185_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "15_185");
					put("nelya", "2_84");           // Úlairë Nelya, Ringwraith in Twilight: another Nazgûl, cost 5, site 2; fierce only at 2 burdens

					// Four companions (with Frodo) of two cultures, so that up to four threats can be added.
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire], like Frodo
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor]
					put("boromir", "1_97");         // Boromir, Son of Denethor: [gondor], a second card of that culture
					put("lord", "1_21");            // Lord of Moria: [dwarven] condition in the support area, with no Dwarf in play
					put("poison", "8_28");          // Spider Poison: a [gollum] Shadow possession in the Shadow support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Úlairë Lemenya, Eternally Threatening
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>When you play Úlairë Lemenya, you may remove (1) to add a threat for each Free Peoples culture you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Eternally Threatening", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedSheMayRemoveOneToAddAThreatForEachFreePeoplesCultureSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn", "boromir");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);

		var lemenya = scn.GetShadowCard("lemenya");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.FreepsPass();

		// 4 to play + 2 roaming at site 2, and 1 more to remove.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertEquals(4, scn.GetThreatLimit());

		scn.ShadowPlayCard(lemenya);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		// [shire] (Frodo and Sam), [gondor] (Aragorn and Boromir) and [dwarven] (a condition): 3.
		// Not her own [wraith], nor the [gollum] of a Shadow card, nor a culture twice.
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoFreePeoplesCulturesSpottedTwoThreatsAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn", "boromir");

		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(lemenya);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		// [shire] and [gondor]
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAndAnotherNazgulPlayedAfterHerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn", "boromir");

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(lemenya, nelya);

		scn.StartGame();
		scn.FreepsPass();

		// 6 for her, 5 for Nelya (site 2: no roaming), and 1 more.
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(lemenya);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, nelya);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThePoolEmptiedByHerPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn", "boromir");

		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.FreepsPass();

		// Exactly her cost: 4 + 2 roaming.
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lemenya));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
