package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_263_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "1_263");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("warband", "1_272");    // Orc War Band: [sauron] Orc, strength 10, vitality 2; a skirmish ability used only on purpose
					put("pillager1", "10_92");  // Mordor Pillager: [sauron] Orc, strength 9, vitality 2, printed fierce; a Shadow ability used only on purpose
					put("pillager2", "10_92");
					put("officer", "7_274");    // Gorgoroth Officer: [sauron] Orc, strength 12; "Skirmish: Discard 2 cards from hand to play a [sauron] Orc
					                            // stacked on a site you control. That Orc is fierce and strength +6 until the regroup phase."
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai (the culture without the race), strength 9, damage +1, no other text
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc (the race without the culture), strength 5; +4 and fierce only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; his texts are used only on purpose or answer Frodo's death
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcBannerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Banner
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each time a companion or ally loses a skirmish involving a [sauron] Orc, each [sauron] Orc is strength +1 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Orc Banner", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenACompanionLosesToASauronOrcEachSauronOrcIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var cuguard = scn.GetShadowCard("cuguard");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(banner);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, warband, cuguard, hound);
		assertInZone(Zone.SUPPORT, banner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warband}, new PhysicalCardImpl[]{gimli, soldier});
		// The Uruk-hai and the [wraith] Orc stay out of the skirmishes.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(warband));
		assertEquals(9, scn.GetStrength(cuguard));
		assertEquals(5, scn.GetStrength(hound));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// Gimli loses, 7 against 6.  The Orc he lost to and the [sauron] Orc still waiting for its skirmish are each +1;
		// the [sauron] Uruk-hai and the [wraith] Orc are not.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(11, scn.GetStrength(warband));
		assertEquals(9, scn.GetStrength(cuguard));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void LossesToASauronUrukHaiOrAWraithOrcAndASauronOrcLosingGiveNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var cuguard = scn.GetShadowCard("cuguard");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveCardsToSupportArea(banner);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, warband, cuguard, hound);
		assertInZone(Zone.SUPPORT, banner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{gimli, cuguard},
				new PhysicalCardImpl[]{sam, hound});
		// The War Band stays out of the skirmishes, to be read.
		scn.ShadowDeclineAssignments();

		// Gimli loses to the [sauron] Uruk-hai, 9 against 6, and takes 2 (damage +1).
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(10, scn.GetStrength(warband));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Sam loses to the [wraith] Orc, 5 against 3.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(10, scn.GetStrength(warband));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The [sauron] Orc loses to Aragorn, 7 against 8, and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(warband));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyWhoLosesToASauronOrcSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(banner);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, warband);
		assertInZone(Zone.SUPPORT, banner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// Aragorn's skirmish waits, so that the bonus is read in the skirmish phase.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, soldier}, new PhysicalCardImpl[]{aragorn, warband});
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(warband));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// 7 against 4: the ally loses to a [sauron] Orc, and the wound kills him.
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(11, scn.GetStrength(warband));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void EachLossToASauronOrcAddsAnotherPlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var pillager1 = scn.GetShadowCard("pillager1");
		var pillager2 = scn.GetShadowCard("pillager2");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(banner);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager1, pillager2, soldier);
		assertInZone(Zone.SUPPORT, banner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pillager1}, new PhysicalCardImpl[]{gimli, pillager2});
		// The Soldier, who is not fierce, stays out of the skirmishes, to be read.
		scn.ShadowDeclineAssignments();
		assertEquals(9, scn.GetStrength(pillager1));
		assertEquals(7, scn.GetStrength(soldier));

		// 9 against 8: Aragorn loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(pillager1));
		assertEquals(10, scn.GetStrength(pillager2));
		assertEquals(8, scn.GetStrength(soldier));

		// 10 against 6: Gimli loses.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The fierce round, later phases of the same turn: both bonuses are still there.
		assertTrue(scn.InFierceRound());
		assertEquals(11, scn.GetStrength(pillager1));
		assertEquals(11, scn.GetStrength(pillager2));
		assertEquals(9, scn.GetStrength(soldier));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pillager1}, new PhysicalCardImpl[]{gimli, pillager2});

		// 11 against 8: Aragorn loses a second time, and each [sauron] Orc is +1 more.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(12, scn.GetStrength(pillager1));
		assertEquals(12, scn.GetStrength(pillager2));
		assertEquals(10, scn.GetStrength(soldier));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 12 against 6 overwhelms Gimli; then the regroup phase, and every bonus is over.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(pillager1));
		assertEquals(9, scn.GetStrength(pillager2));
		assertEquals(7, scn.GetStrength(soldier));
	}

	@Test
	public void ASauronOrcPlayedAfterTheLossIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var warband = scn.GetShadowCard("warband");
		var officer = scn.GetShadowCard("officer");
		var soldier = scn.GetShadowCard("soldier");
		// Any two cards in the Shadow hand pay the Officer's cost.
		var discard1 = scn.GetShadowCard("sam");
		var discard2 = scn.GetShadowCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(banner);

		scn.StartGame();
		var site1 = scn.GetCurrentSite();
		// Player 1's second turn: each player has moved to site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.StackCardsOn(site1, soldier);
		scn.MoveMinionsToTable(warband, officer);
		scn.MoveCardsToHand(discard1, discard2);
		scn.SetTwilight(10);
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertInZone(Zone.STACKED, soldier);
		assertInZone(Zone.SUPPORT, banner);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warband}, new PhysicalCardImpl[]{gimli, officer});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// 10 against 8: Aragorn loses, and the two [sauron] Orcs in play are +1.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(11, scn.GetStrength(warband));
		assertEquals(13, scn.GetStrength(officer));

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(officer));
		scn.ShadowUseCardAction(officer);
		scn.ShadowChooseCards(discard1, discard2);

		// The Orc played from the stack is 7 + 6 by the Officer's text, and not +1 more: the trigger reached only
		// the [sauron] Orcs that were in play when it was carried out.
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(13, scn.GetStrength(soldier));
		assertEquals(11, scn.GetStrength(warband));
		assertEquals(13, scn.GetStrength(officer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
