package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_328_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_328");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SlowkindledCourageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Slow-kindled Courage
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be Merry or Pippin.<br>Each minion gains this ability: "<b>Assignment:</b> Assign this minion to bearer of Slow-kindled Courage."<br><b>Regroup:</b> Discard this condition to discard a minion and remove 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Slow-kindled Courage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("courage", "7_328");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: no weapon here, so no text
					// Reckless Pride: the same ability for its own bearer, a [dwarven] companion
					put("pride", "7_13");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability, not used here
					put("troop1", "1_177");     // Goblin Patrol Troop: no text at all
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwoMinionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("courage", "7_328");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: no weapon here, so no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who does not bear the condition
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text at all
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The ability costs nothing, so it is not offered where it would assign nobody (Ketura on Q-0034, F-0353;
	// CR 4.2 "assignment phase": "Many assignment actions assign a minion to a companion.  You cannot do this
	// unless both of them are unassigned").
	@Test
	public void EachMinionMayAssignItselfToTheBearerOfSlowKindledCourageAndNotOnceEitherIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var courage = scn.GetFreepsCard("courage");
		var merry = scn.GetFreepsCard("merry");
		var pride = scn.GetFreepsCard("pride");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(merry, gimli);
		scn.AttachCardsTo(merry, courage);
		scn.AttachCardsTo(gimli, pride);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion has the ability, in the words of this card.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1, "Slow-kindled Courage"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Slow-kindled Courage"));
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(troop1));

		scn.ShadowUseCardAction(troop1, "Slow-kindled Courage");

		assertTrue(scn.IsCharAssignedAgainst(merry, troop1));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(troop2));

		scn.FreepsPass();

		// The assigned minion has the ability no longer, and neither has the other minion now that the bearer is
		// assigned.  Reckless Pride's, whose bearer is not, is still the unassigned minion's to use.
		assertFalse(scn.ShadowActionAvailable(troop1, "Slow-kindled Courage"));
		assertFalse(scn.ShadowActionAvailable(troop2, "Slow-kindled Courage"));
		assertFalse(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Reckless Pride"));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void EachMinionGainsAnAssignmentAbilityThatAssignsItToTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoMinionsScenario();

		var courage = scn.GetFreepsCard("courage");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, courage);

		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// A Free Peoples card gives the ability, and it is the minions': their owner's to use, not the bearer's.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(raiders));
		assertFalse(scn.FreepsActionAvailable(troop));
		scn.FreepsPass();

		// Each minion has it, whatever its culture or race.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(raiders, "Slow-kindled Courage"));
		assertTrue(scn.ShadowActionAvailable(troop, "Slow-kindled Courage"));
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(raiders));

		scn.ShadowUseCardAction(raiders, "Slow-kindled Courage");

		// That minion is assigned to the bearer; the other companion and the other minion are not assigned.
		assertTrue(scn.IsCharAssignedAgainst(merry, raiders));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
