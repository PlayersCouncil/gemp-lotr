package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_012_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Thranduil's home is
	// site 5; the tests are at The Troll Hoard 30_50 (site 2) and Rivendell 30_51 (site 3), away from it, where
	// he takes part in nothing unless a card allows it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thranduil", "31_12");
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, vitality 4, damage +1; no follower, his ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion who is not Thorin, vitality 4; a Fellowship ability, not used
					put("arkenstone", "31_7");  // The Arkenstone: [dwarven] artifact in the support area; Fellowship and Regroup abilities, not used
					put("coat", "31_5");        // Mithril Coat: [dwarven] artifact borne by Bilbo; the minion archery total -1
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession (no artifact), on a [dwarven] character; damage +1 at a mountain
					put("sting", "30_47");      // Sting: a [shire] artifact on Bilbo; a skirmish-win trigger
					put("bats", "32_30");       // Dark Bats: [moria] minion, strength 4, vitality 2; a Shadow ability, not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability, not used
					put("spider", "31_51");     // Fat Spider: fierce [spider] minion, strength 8, vitality 2; a Regroup ability, not used
					put("archer", "33_38");     // Goblin Archer: [moria] archer minion, vitality 1; a "when you play" trigger only
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThranduilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Thranduil, Elven King
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 5H
		 * Strength: 9
		 * Vitality: 4
		 * Game Text: <b>Maneuver:</b> Exert Thorin twice or discard a [dwarven] artifact to allow Thranduil to participate in skirmishes until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thranduil");

		assertEquals("Thranduil", card.getBlueprint().getTitle());
		assertEquals("Elven King", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 5)));
	}

	@Test
	public void WithBothCostsPayableTheChoiceIsAskedAndExertingThorinTwiceLetsThranduilBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thranduil = scn.GetFreepsCard("thranduil");
		var thorin = scn.GetFreepsCard("thorin");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(thranduil);
		scn.MoveCardsToSupportArea(arkenstone);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(thorin));
		assertInZone(Zone.SUPPORT, arkenstone);
		assertFalse(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.FreepsActionAvailable(thranduil));
		scn.FreepsUseCardAction(thranduil);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert Thorin twice"));
		assertTrue(scn.FreepsChoiceAvailable("artifact"));
		scn.FreepsChooseOption("Exert Thorin twice");

		assertEquals(2, scn.GetWoundsOn(thorin));
		assertInZone(Zone.SUPPORT, arkenstone);
		assertTrue(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(thranduil));
		scn.FreepsAssignToMinions(thranduil, bats);
		assertTrue(scn.IsCharAssignedAgainst(thranduil, bats));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DiscardingADwarvenArtifactLetsThranduilBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thranduil = scn.GetFreepsCard("thranduil");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var coat = scn.GetFreepsCard("coat");
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(thranduil);
		scn.MoveCardsToSupportArea(arkenstone);
		scn.AttachCardsTo(bilbo, coat, sting);
		scn.AttachCardsTo(balin, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, arkenstone);
		assertAttachedTo(coat, bilbo);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(axe, balin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertFalse(scn.FreepsCanAssign(thranduil));
		scn.FreepsUseCardAction(thranduil);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("artifact");

		// A [dwarven] artifact in the support area and one borne; not a [dwarven] possession, nor a [shire] artifact.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arkenstone, coat));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, sting));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(coat);

		assertInDiscard(coat);
		assertInZone(Zone.SUPPORT, arkenstone);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(axe, balin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithThorinUnableToExertTwiceTheArtifactIsDiscardedWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thranduil = scn.GetFreepsCard("thranduil");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(thranduil);
		scn.MoveCardsToSupportArea(arkenstone);
		// Vitality 4 with 2 wounds: Thorin can exert once, not twice.
		scn.AddWoundsToChar(thorin, 2);
		assertEquals(2, scn.GetWoundsOn(thorin));

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertInZone(Zone.SUPPORT, arkenstone);
		assertTrue(scn.FreepsActionAvailable(thranduil));
		scn.FreepsUseCardAction(thranduil);

		// No choice of cost: the one artifact goes.  Balin, a Dwarf who is not Thorin, does not exert.
		assertInDiscard(arkenstone);
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenArtifactThorinExertsTwiceWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thranduil = scn.GetFreepsCard("thranduil");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(thranduil);
		scn.AttachCardsTo(bilbo, sting);
		scn.AttachCardsTo(balin, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(sting, bilbo);
		assertAttachedTo(axe, balin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.FreepsUseCardAction(thranduil);

		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(axe, balin);
		assertTrue(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenNeitherCostCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thranduil = scn.GetFreepsCard("thranduil");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var bilbo = scn.GetRingBearer();
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(thranduil);
		scn.AttachCardsTo(bilbo, sting);
		scn.AttachCardsTo(balin, axe);
		scn.AddWoundsToChar(thorin, 2);
		assertEquals(2, scn.GetWoundsOn(thorin));

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.MANEUVER);

		// Thorin cannot exert twice, and there is no [dwarven] artifact; Balin could exert twice but is not Thorin.
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(axe, balin);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thranduil));
		assertFalse(scn.FreepsCanAssign(thranduil));
	}

	@Test
	public void ThranduilTakesPartInSkirmishesUntilTheRegroupPhaseAndNotAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thranduil = scn.GetFreepsCard("thranduil");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		var aggressor = scn.GetShadowCard("aggressor");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(thranduil);

		scn.StartGame();
		scn.MoveMinionsToTable(bats, aggressor, spider);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(thorin));
		scn.FreepsUseCardAction(thranduil);
		assertEquals(2, scn.GetWoundsOn(thorin));

		// The fierce Fat Spider is left out of the first assignment.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thorin, aggressor},
				new PhysicalCardImpl[]{thranduil, bats});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thranduil);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsCanAssign(thranduil));
		scn.PassSkirmishActions();

		// The fierce assignment phase, still ahead of the regroup phase: he may be assigned again.
		assertInZone(Zone.SHADOW_CHARACTERS, spider);
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(thranduil));
		scn.FreepsAssignAndResolve(thranduil, spider);
		assertTrue(scn.IsCharSkirmishingAgainst(thranduil, spider));
		scn.PassSkirmishActions();

		// The regroup phase: no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(thranduil));

		// A second move, to site 3, and its assignment phase against the minions still standing: he is not to be
		// assigned there.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void ThranduilTakesNoPartInArcheryFire() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thranduil = scn.GetFreepsCard("thranduil");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var archer = scn.GetShadowCard("archer");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(thranduil);

		scn.StartGame();
		scn.MoveMinionsToTable(archer, bats);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(thorin));
		scn.FreepsUseCardAction(thranduil);
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertTrue(scn.FreepsCanAssign(thranduil));

		// The Goblin Archer's archery wound may go on Bilbo or Thorin, not on Thranduil.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thranduil));
		scn.FreepsChooseCard(thorin);
		assertEquals(3, scn.GetWoundsOn(thorin));

		scn.SkipToAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}
}
