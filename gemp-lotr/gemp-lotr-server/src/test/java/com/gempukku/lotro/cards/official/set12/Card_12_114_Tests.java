package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mark", "12_114");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, resistance 6, valiant and nothing else
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man companion, resistance 6; a skirmish ability that needs a mount
					put("soldier", "17_101");   // Soldier of Rohan: a [rohan] Man companion, resistance 5; stronger while the Ring-bearer is assigned
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY (resistance 0); a fellowship ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion, resistance 6, a Man only while 3 [rohan]
					// companions or Eowyn can be spotted; each time another [rohan] Man exerts he may exert to heal him.
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, resistance 6; a maneuver ability used only on purpose
					put("armor", "1_92");       // Armor: a [gondor] possession, "Bearer must be a Man"
					put("toby", "1_305");       // Old Toby: a [shire] possession in the support area; a draw on its own play only
					put("barahir", "6_55");     // Ring of Barahir: a [gondor] ARTIFACT on Aragorn; an optional start-of-turn heal
					put("stored", "4_296");     // Well Stored: a [rohan] support-area CONDITION; an optional start-of-fellowship heal of a villager
					put("scout", "1_191");      // Moria Scout: a [moria] Orc; its trigger is on being played
					put("axe", "1_190");        // Moria Axe: a Shadow possession on a [moria] Orc; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a minion whose only text is on being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FortheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: For the Mark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot a [rohan] Man to discard a possession from play.<br>You may exert a [rohan] Man who has resistance 3 or more to play this event during the maneuver phase.
		*/

		var scn = GetScenario();

		var mark = scn.GetFreepsCard("mark");

		assertEquals("For the Mark", mark.getBlueprint().getTitle());
		assertNull(mark.getBlueprint().getSubtitle());
		assertFalse(mark.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, mark.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, mark.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, mark.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(mark, Timeword.REGROUP));
		assertEquals(0, mark.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheManeuverPhaseARohanManOfResistance3ExertsToPlayItAndAnyPossessionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mark = scn.GetFreepsCard("mark");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		var armor = scn.GetFreepsCard("armor");
		var toby = scn.GetFreepsCard("toby");
		var barahir = scn.GetFreepsCard("barahir");
		var stored = scn.GetFreepsCard("stored");
		var scout = scn.GetShadowCard("scout");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToHand(mark);
		scn.MoveCompanionsToTable(guard, rider, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.MoveCardsToSupportArea(stored);
		scn.MoveMinionsToTable(scout);
		scn.AttachCardsTo(scout, axe);
		scn.AddBurdens(3);
		assertInZone(Zone.ATTACHED, armor, barahir, axe);
		assertInZone(Zone.SUPPORT, toby, stored);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetBurdens());
		// Resistance 6 less 3 burdens on each companion; an ally has none.
		assertEquals(3, scn.GetResistance(guard));
		assertEquals(3, scn.GetResistance(rider));
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(guma));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(mark));

		scn.FreepsPlayCard(mark);
		// [rohan] Man companions of resistance 3; not the [gondor] Man, nor the [rohan] Man ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guma));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(rider));

		// Possessions borne by a companion, in the support area and borne by a minion; not an artifact, nor a
		// condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, toby, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barahir, stored));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertInZone(Zone.DISCARD, axe, mark);
		assertInZone(Zone.ATTACHED, armor, barahir);
		assertInZone(Zone.SUPPORT, toby, stored);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseARohanManOfResistance2CannotPayAndOfResistance3Can() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mark = scn.GetFreepsCard("mark");
		var soldier = scn.GetFreepsCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(mark);
		scn.MoveCompanionsToTable(soldier, aragorn);
		scn.AttachCardsTo(soldier, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.AddBurdens(3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetBurdens());
		// 5 less 3: one short. The [gondor] Man beside him has 3.
		assertEquals(2, scn.GetResistance(soldier));
		assertEquals(3, scn.GetResistance(aragorn));
		assertInZone(Zone.ATTACHED, armor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(mark));

		// One burden fewer: the Soldier has resistance 3, and exerts; nobody else could, so nothing is asked.
		scn.RemoveBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetResistance(soldier));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(mark));
		scn.FreepsPlayCard(mark);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, armor, mark);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseAnExhaustedRohanManARohanHobbitAndAGondorManCannotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mark = scn.GetFreepsCard("mark");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(mark);
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();
		// Vitality 3 with 2 wounds: the Guard is exhausted.
		scn.AddWoundsToChar(guard, 2);
		scn.MoveMinionsToTable(runner);
		scn.AddBurdens(3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(3, scn.GetBurdens());
		// Each of resistance 3 (6 less 3); Merry, beside one other [rohan] companion, is no Man.
		assertEquals(3, scn.GetResistance(guard));
		assertEquals(3, scn.GetResistance(merry));
		assertEquals(3, scn.GetResistance(aragorn));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertInZone(Zone.ATTACHED, armor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(mark));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void InTheRegroupPhaseARohanManAllyIsSpottedAndNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mark = scn.GetFreepsCard("mark");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(mark);
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// A [rohan] companion who is no Man, and a Man who is not [rohan]: nothing to spot.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertInZone(Zone.ATTACHED, armor);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(mark));

		scn.MoveAlliesToTable(guma);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.FreepsPlayAvailable(mark));
		scn.FreepsPlayCard(mark);

		// The only possession goes; the ally is spotted and not exerted.
		assertInZone(Zone.DISCARD, armor, mark);
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void InTheRegroupPhaseWithNoPossessionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mark = scn.GetFreepsCard("mark");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(mark);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(mark));
		scn.FreepsPlayCard(mark);

		// Nothing to discard, and the Guard, who could exert, is not asked to.
		assertInZone(Zone.DISCARD, mark);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
