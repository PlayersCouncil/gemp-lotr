package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cutthroat", "5_104");

					// Orc Chaser: a [sauron] Orc and tracker, site 6.  "The site number of each [sauron] Orc is -1."
					// Its other sentence makes it stronger while the Shadow player has initiative.
					put("chaser", "7_298");
					// Orc Soldier: a [sauron] Orc who is not unique, cost 2, vitality 2, site 6.  A skirmish
					// ability that is never used.
					put("soldier1", "1_271");
					put("soldier2", "1_271");
					// Gorgoroth Axeman: a [sauron] Orc and besieger, site 5.  A skirmish ability never used.
					put("axeman", "7_271");
					// Goblin Patrol Troop: a [moria] Orc, site 4, no text.
					put("troop", "1_177");
					// Corpse Lights: a [sauron] Wraith (twilight), site 4.  A skirmish ability never used.
					put("lights", "6_99");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					// Thrarin, Dwarven Smith: a Dwarf ally, vitality 2.  "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcCutthroatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Orc Cutthroat
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Tracker.
		 * 	The site number of each [sauron] Orc is -3.
		 * 	Skirmish: Discard another [sauron] Orc and exert Orc Cutthroat to wound a companion he is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cutthroat");

		assertEquals("Orc Cutthroat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedAtHisOwnSiteNumberAndOnceInPlayMakesEachSauronOrcSiteNumberMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cutthroat = scn.GetShadowCard("cutthroat");
		var chaser = scn.GetShadowCard("chaser");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToHand(cutthroat, soldier2);

		scn.StartGame();
		scn.MoveMinionsToTable(chaser, soldier1, axeman, troop, lights);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 5 set + 1 companion + 2 for the site
		assertEquals(8, scn.GetTwilight());
		// The Orc Chaser's -1 is on every [sauron] Orc, itself included.
		assertEquals(5, scn.GetMinionSiteNumber(chaser));
		assertEquals(5, scn.GetMinionSiteNumber(soldier1));
		assertEquals(4, scn.GetMinionSiteNumber(axeman));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(4, scn.GetMinionSiteNumber(lights));
		assertTrue(scn.HasKeyword(chaser, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(soldier1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(axeman, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(lights, Keyword.ROAMING));
		assertTrue(scn.ShadowPlayAvailable(cutthroat));

		scn.ShadowPlayCard(cutthroat);
		// 3 to play + 2 roaming.  His own text is not active while he is played (CR 4.2 "modifier", the
		// ruling on Grishnákh's same sentence): his site number is 6 - 1 for the Chaser, at site 2.  With his
		// -3 he would have been site number 2, not roaming, and cost 3.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, cutthroat);

		// Once in play it applies to each [sauron] Orc, himself included.
		assertEquals(2, scn.GetMinionSiteNumber(cutthroat));     // 6 - 1 - 3
		assertEquals(2, scn.GetMinionSiteNumber(chaser));
		assertEquals(2, scn.GetMinionSiteNumber(soldier1));
		assertEquals(1, scn.GetMinionSiteNumber(axeman));        // 5 - 1 - 3
		assertFalse(scn.HasKeyword(cutthroat, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(chaser, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(soldier1, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(axeman, Keyword.ROAMING));
		// The [moria] Orc and the [sauron] Wraith are where they were.
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(4, scn.GetMinionSiteNumber(lights));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(lights, Keyword.ROAMING));

		// The Orc Soldier in hand is a [sauron] Orc too.  It would have cost 2 + 2 roaming before the
		// Cutthroat; now it costs 2, and 1 is left.
		assertTrue(scn.ShadowPlayAvailable(soldier2));
		scn.ShadowPlayCard(soldier2);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2);
		assertEquals(2, scn.GetMinionSiteNumber(soldier2));
		assertFalse(scn.HasKeyword(soldier2, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsAnotherSauronOrcAndExertsHimToWoundTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var cutthroat = scn.GetShadowCard("cutthroat");
		var chaser = scn.GetShadowCard("chaser");
		var soldier = scn.GetShadowCard("soldier1");
		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 1);
		scn.MoveMinionsToTable(cutthroat, chaser, soldier, troop, lights);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, cutthroat }, new PhysicalCardImpl[] { boromir, troop });
		// The Orc Soldier joins Boromir's skirmish; the Chaser and Corpse Lights are left unassigned.
		scn.ShadowAssignToMinions(boromir, soldier);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cutthroat));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowActionAvailable(cutthroat));
		scn.ShadowUseCardAction(cutthroat);

		// Another [sauron] Orc: the Soldier in another skirmish and the Chaser, unassigned.  Not the [moria] Orc,
		// not the [sauron] Wraith, and not the Cutthroat himself.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chaser));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, lights, cutthroat));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chaser);

		assertInZone(Zone.DISCARD, chaser);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(1, scn.GetWoundsOn(cutthroat));
		// Aragorn, whom he is skirmishing, takes the wound; Boromir, in another skirmish, does not.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedBeforeHisOwnSkirmishBeginsHeWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var cutthroat = scn.GetShadowCard("cutthroat");
		var chaser = scn.GetShadowCard("chaser");
		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 1);
		scn.MoveMinionsToTable(cutthroat, chaser, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, cutthroat }, new PhysicalCardImpl[] { boromir, soldier });
		scn.ShadowDeclineAssignments();
		// Boromir's skirmish against the Soldier first: Aragorn is assigned to the Cutthroat but not yet
		// skirmishing him.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(boromir, soldier));
		assertEquals(0, scn.GetWoundsOn(cutthroat));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowActionAvailable(cutthroat));
		scn.ShadowUseCardAction(cutthroat);
		scn.ShadowChooseCard(chaser);

		// The costs are paid, and no companion is wounded.
		assertInZone(Zone.DISCARD, chaser);
		assertEquals(1, scn.GetWoundsOn(cutthroat));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnAllyHeWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var cutthroat = scn.GetShadowCard("cutthroat");
		var chaser = scn.GetShadowCard("chaser");
		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(cutthroat, chaser, soldier);

		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin's own ability lets him take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { thrarin, cutthroat }, new PhysicalCardImpl[] { aragorn, soldier });
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, cutthroat));
		assertEquals(0, scn.GetWoundsOn(cutthroat));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(cutthroat));
		scn.ShadowUseCardAction(cutthroat);
		scn.ShadowChooseCard(chaser);

		// The costs are paid; the character he is skirmishing is an ally, no companion, and is not wounded.
		assertInZone(Zone.DISCARD, chaser);
		assertEquals(1, scn.GetWoundsOn(cutthroat));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoOtherSauronOrcToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var cutthroat = scn.GetShadowCard("cutthroat");
		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.MoveMinionsToTable(cutthroat, troop, lights);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cutthroat);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Beside him, a [moria] Orc and a [sauron] Wraith: neither is another [sauron] Orc.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cutthroat));
		assertEquals(0, scn.GetWoundsOn(cutthroat));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cutthroat));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var cutthroat = scn.GetShadowCard("cutthroat");
		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(cutthroat, soldier);
		scn.AddWoundsToChar(cutthroat, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cutthroat);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: he cannot exert, though the Orc Soldier could be discarded.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cutthroat));
		assertEquals(2, scn.GetWoundsOn(cutthroat));
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cutthroat));
	}
}
