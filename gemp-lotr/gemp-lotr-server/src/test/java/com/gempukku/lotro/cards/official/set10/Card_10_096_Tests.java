package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rank", "10_96");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2, site 6; its text answers a weapon played on it
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, strength 8, vitality 3, site 6; a Shadow ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2, site 6; Damage +1 only
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, strength 7, vitality 2, site 4; a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, site 5; Damage +1 only
					put("miscreant", "11_131"); // Orc Miscreant: [orc] Orc, vitality 3; a regroup ability used only on purpose
					put("towers", "7_281");     // Great Siege-towers: [sauron] condition that takes [sauron] tokens of its own; abilities used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, Damage +1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced") in
	// place of a site no test reaches; the test lays it as the site after the first.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rank", "10_96");
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, strength 8, vitality 3, site 6
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RankandFileStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Rank and File
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a roaming [sauron] minion takes a wound, you may add a [sauron] token here.<br><b>Regroup:</b> Exert an Uruk-hai or discard a [sauron] minion to add a [sauron] token here.<br><b>Skirmish:</b> Remove 2 [sauron] tokens from here to make a [sauron] minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rank");

		assertEquals("Rank and File", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachWoundOnARoamingSauronMinionOffersATokenAndNoOtherWoundDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rank = scn.GetShadowCard("rank");
		var towers = scn.GetShadowCard("towers");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var lights = scn.GetShadowCard("lights");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(rank, towers);
		scn.AddTokensToCard(towers, 1);
		assertEquals(1, scn.GetCultureTokensOn(towers));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(chieftain, guard, lights, savage);
		// The four minions together have strength 7 + 9 + 7 + 5 = 28; Aragorn at 8 + 25 = 33 beats them and does
		// not overwhelm them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 25));
		scn.SkipToAssignments();

		// Site 4: the site 6 Orc and Uruk-hai and the site 5 [isengard] Uruk-hai are roaming; the site 4 Wraith is not.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lights, Keyword.ROAMING));

		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowAssignToMinions(aragorn, guard, lights, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetCultureTokensOn(rank));
		assertEquals(1, scn.GetCultureTokensOn(towers));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.PassSkirmishActions();
		// Each of the two roaming [sauron] minions' wounds is offered, once.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));
		scn.ShadowChooseOptionalTrigger(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));
		scn.ShadowChooseOptionalTrigger(rank);
		assertEquals(2, scn.GetCultureTokensOn(rank));

		// Every loser took his wound; the Wraith that is not roaming and the [isengard] Uruk-hai set off nothing.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(1, scn.GetCultureTokensOn(towers));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoWoundsOnOneMinionAreTwoOffersAndADeclineAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var rank = scn.GetShadowCard("rank");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToSupportArea(rank);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		// Gimli at 6 + 3 = 9 beats the Taskmaster's 8; his damage +1 makes it two wounds, which vitality 3 survives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 3));
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(taskmaster, Keyword.ROAMING));
		scn.FreepsAssignToMinions(gimli, taskmaster);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetCultureTokensOn(rank));
		assertEquals(0, scn.GetWoundsOn(taskmaster));

		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(rank));

		// The second wound on the same minion is offered again.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rank));
		scn.ShadowChooseOptionalTrigger(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokenIsNotOfferedWhereNoTokenCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rank = scn.GetShadowCard("rank");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToSupportArea(rank);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.MoveMinionsToTable(taskmaster);
		// Aragorn at 8 + 5 = 13 beats the Taskmaster's 8 and does not overwhelm him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(taskmaster, Keyword.ROAMING));
		scn.FreepsAssignToMinions(aragorn, taskmaster);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(taskmaster));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rank));
		assertEquals(0, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsAnUrukHaiOrDiscardsASauronMinionToAddAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rank = scn.GetShadowCard("rank");
		var towers = scn.GetShadowCard("towers");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var chieftain = scn.GetShadowCard("chieftain");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToSupportArea(rank, towers);
		assertInZone(Zone.SUPPORT, towers);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, chieftain, lights);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(rank));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertTrue(scn.ShadowActionAvailable(rank));

		scn.ShadowUseCardAction(rank);
		// Both halves can be paid: the Shadow player is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Exert an Uruk-hai"));
		assertTrue(scn.ShadowChoiceAvailable("minion"));
		scn.ShadowChooseOption("Exert an Uruk-hai");

		// Uruk-hai of two cultures; not the Orc or the Wraith.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, lights));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetCultureTokensOn(rank));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(rank));
		scn.ShadowUseCardAction(rank);
		scn.ShadowChooseOption("minion");

		// [sauron] minions of three races; not the [isengard] Uruk-hai, nor the [sauron] condition.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, chieftain, lights));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, towers));
		scn.ShadowChooseCard(chieftain);
		assertInDiscard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, guard, lights, savage);
		assertInZone(Zone.SUPPORT, towers);
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachHalfOfTheRegroupCostIsPaidAloneWhenTheOtherCannotBe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rank = scn.GetShadowCard("rank");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(rank);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetCultureTokensOn(rank));
		assertEquals(0, scn.GetWoundsOn(savage));

		// No [sauron] minion: the Uruk-hai exerts, with no question of which half.
		scn.ShadowUseCardAction(rank);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(rank));

		// Vitality 3 with 2 wounds: the only Uruk-hai is exhausted, and a [sauron] Orc is there to discard.
		scn.FreepsPass();
		scn.AddWoundsToChar(savage, 1);
		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowActionAvailable(rank));

		scn.ShadowUseCardAction(rank);
		assertInDiscard(chieftain);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoUrukHaiToExertAndNoSauronMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rank = scn.GetShadowCard("rank");
		var towers = scn.GetShadowCard("towers");
		var savage = scn.GetShadowCard("savage");
		var miscreant = scn.GetShadowCard("miscreant");
		scn.MoveCardsToSupportArea(rank, towers);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, miscreant);
		// Vitality 3 with 2 wounds: the only Uruk-hai is exhausted; the [orc] Orc and the [sauron] condition
		// are neither half.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(miscreant));
		assertFalse(scn.ShadowActionAvailable(rank));
		assertEquals(0, scn.GetCultureTokensOn(rank));
	}

	@Test
	public void SkirmishAbilityRemovesTwoTokensHereToMakeASauronMinionStrongerForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var rank = scn.GetShadowCard("rank");
		var towers = scn.GetShadowCard("towers");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(rank, towers);
		scn.AddTokensToCard(rank, 3);
		scn.AddTokensToCard(towers, 2);
		assertEquals(3, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(towers));

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, guard, taskmaster, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{gimli, guard});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(towers));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(taskmaster));
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(rank));
		scn.ShadowUseCardAction(rank);
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(towers));

		// [sauron] minions in this skirmish, in one still to come and in none; not the [isengard] Uruk-hai, nor
		// a [sauron] condition.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, guard, taskmaster));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, towers, rank));
		scn.ShadowChooseCard(chieftain);
		assertEquals(9, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(taskmaster));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 9: Aragorn loses.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);

		// The next skirmish is a phase of its own: the bonus is gone.  One token here, though Siege-towers
		// holds two, does not pay the cost.
		assertEquals(7, scn.GetStrength(chieftain));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(towers));
		assertFalse(scn.ShadowActionAvailable(rank));
	}
}
