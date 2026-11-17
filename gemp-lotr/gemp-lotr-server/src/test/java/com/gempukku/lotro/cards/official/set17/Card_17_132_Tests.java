package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("taskmaster1", "17_132");
					put("taskmaster2", "17_132");   // not unique: a second copy is "another" for its own play
					put("butcher", "17_122");       // White Hand Butcher: an [uruk-hai] Uruk-hai, Hunter 6 is its whole text
					put("saruman", "17_116");       // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard)
					put("guard", "10_81");          // Cirith Ungol Guard: a [sauron] Uruk-hai, Damage +1 is its whole text
					put("shingle", "12_145");       // Shingle in a Storm: an [uruk-hai] condition, no minion; nothing below 6 companions
					put("poison", "8_28");          // Spider Poison: a [gollum] Shadow possession in the Shadow support area

					// Companions beside Frodo: five in all, so that up to five threats can be added.
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire], like Frodo
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor]
					put("boromir", "1_97");         // Boromir, Son of Denethor: [gondor], a second card of that culture
					put("gimli", "1_13");           // Gimli, Son of Glóin: [dwarven]
					put("cart", "1_73");            // Gandalf's Cart: a [gandalf] possession in the support area, no character
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandTaskmasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Taskmaster
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>When you play this minion, you may spot another [uruk-hai] minion to add a threat for each Free Peoples culture you can spot.
		*/

		var scn = GetScenario();

		var taskmaster = scn.GetFreepsCard("taskmaster1");

		assertEquals("White Hand Taskmaster", taskmaster.getBlueprint().getTitle());
		assertNull(taskmaster.getBlueprint().getSubtitle());
		assertFalse(taskmaster.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, taskmaster.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, taskmaster.getBlueprint().getCulture());
		assertEquals(CardType.MINION, taskmaster.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, taskmaster.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(taskmaster, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(taskmaster, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(taskmaster, Keyword.HUNTER));
		assertEquals(4, taskmaster.getBlueprint().getTwilightCost());
		assertEquals(9, taskmaster.getBlueprint().getStrength());
		assertEquals(2, taskmaster.getBlueprint().getVitality());
		assertEquals(5, taskmaster.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpottingAnotherUrukHaiItAddsAThreatForEachFreePeoplesCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn", "boromir", "gimli");
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("cart"));

		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var butcher = scn.GetShadowCard("butcher");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(poison);
		scn.MoveCardsToHand(taskmaster1);

		scn.StartGame();
		scn.MoveMinionsToTable(butcher);
		// Enough for its cost and the roaming penalty at site 2; the pool is not what this test reads.
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetThreatLimit());

		scn.ShadowPlayCard(taskmaster1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		scn.ShadowAcceptOptionalTrigger();

		// [shire] (Frodo and Sam), [gondor] (Aragorn and Boromir), [dwarven] (Gimli) and [gandalf] (a possession): 4.
		// Not the [uruk-hai] of the minions, nor the [gollum] of a Shadow card, nor a culture twice; with five
		// companions, a fifth would have shown.
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionWhoIsNoUrukHaiIsSpottedAndTwoCulturesAddTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn");

		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(taskmaster1);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());

		scn.ShadowPlayCard(taskmaster1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		scn.ShadowAcceptOptionalTrigger();

		// [shire] and [gondor]
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedBesideASauronUrukHaiAndAnUrukHaiConditionAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn");

		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var guard = scn.GetShadowCard("guard");
		var shingle = scn.GetShadowCard("shingle");
		scn.MoveCardsToSupportArea(shingle);
		scn.MoveCardsToHand(taskmaster1);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(taskmaster1);

		// The Taskmaster itself is an [uruk-hai] minion, but not "another".
		assertInZone(Zone.SHADOW_CHARACTERS, taskmaster1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheThreatsAlreadyAtTheNumberOfCompanionsNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn");

		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var butcher = scn.GetShadowCard("butcher");
		scn.MoveCardsToHand(taskmaster1);

		scn.StartGame();
		scn.MoveMinionsToTable(butcher);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());

		scn.ShadowPlayCard(taskmaster1);

		// No threat could be added: the trigger would do nothing.
		assertInZone(Zone.SHADOW_CHARACTERS, taskmaster1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningAddsNoThreatAndAnotherUrukHaiMinionPlayedAfterItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn");

		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var saruman = scn.GetShadowCard("saruman");
		var butcher = scn.GetShadowCard("butcher");
		scn.MoveCardsToHand(taskmaster1, butcher);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(taskmaster1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(butcher);
		assertInZone(Zone.SHADOW_CHARACTERS, taskmaster1, butcher);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedAfterTheFirstIsAnotherUrukHaiMinionForItsOwnOfferAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn");

		var taskmaster1 = scn.GetShadowCard("taskmaster1");
		var taskmaster2 = scn.GetShadowCard("taskmaster2");
		scn.MoveCardsToHand(taskmaster1, taskmaster2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(taskmaster1);
		// Alone, it has no other [uruk-hai] minion to spot.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(taskmaster2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(taskmaster1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(taskmaster2));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
