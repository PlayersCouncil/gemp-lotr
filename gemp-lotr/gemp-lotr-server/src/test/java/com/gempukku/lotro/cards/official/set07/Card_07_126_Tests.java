package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_126");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnexpectedVisitorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Unexpected Visitor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be a [gondor] companion.<br>Each minion gains this ability: "<b>Assignment:</b> Assign this minion to bearer of Unexpected Visitor."<br><b>Regroup:</b> Discard this condition to discard a minion and remove (4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Unexpected Visitor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("visitor", "7_126");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability, not used here
					// Ancient Blade: the same ability for its own bearer, an [elven] companion
					put("blade", "7_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: her text needs a Nazgul
					put("troop1", "1_177");     // Goblin Patrol Troop: no text at all
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The ability costs nothing, so it is not offered where it would assign nobody (Ketura on Q-0034; CR 5.0
	// "hinder": "Hindered characters cannot be assigned to skirmishes").
	@Test
	public void WithItsBearerHinderedNoMinionIsOfferedTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var visitor = scn.GetFreepsCard("visitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, visitor);
		scn.AttachCardsTo(arwen, blade);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion has the ability, in the words of this card.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.ShadowActionAvailable(troop1, "Unexpected Visitor"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Unexpected Visitor"));

		scn.HinderCard(aragorn);
		scn.ShadowRefreshDecision();

		// The condition is still on Aragorn and is not itself hindered, but its bearer cannot be assigned.
		// Ancient Blade's ability, whose bearer can be, is still each minion's to use.
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(visitor));
		assertTrue(scn.GetAttachedCards(aragorn).contains(visitor));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop1, "Unexpected Visitor"));
		assertFalse(scn.ShadowActionAvailable(troop2, "Unexpected Visitor"));
		assertTrue(scn.ShadowActionAvailable(troop1, "Ancient Blade"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Ancient Blade"));
		assertFalse(scn.IsCharAssigned(aragorn));
	}

	// CR 4.2 "assignment phase": "Many assignment actions assign a minion to a companion.  You cannot do this
	// unless both of them are unassigned."
	@Test
	public void EachMinionMayAssignItselfToTheBearerOfUnexpectedVisitorAndNotOnceEitherIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var visitor = scn.GetFreepsCard("visitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, visitor);
		scn.AttachCardsTo(arwen, blade);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1, "Unexpected Visitor"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Unexpected Visitor"));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(troop1));

		scn.ShadowUseCardAction(troop1, "Unexpected Visitor");

		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop1));
		assertFalse(scn.IsCharAssigned(arwen));
		assertFalse(scn.IsCharAssigned(troop2));

		scn.FreepsPass();

		// The assigned minion has the ability no longer, and neither has the other minion now that the bearer is
		// assigned.  Ancient Blade's, whose bearer is not, is still the unassigned minion's to use.
		assertFalse(scn.ShadowActionAvailable(troop1, "Unexpected Visitor"));
		assertFalse(scn.ShadowActionAvailable(troop2, "Unexpected Visitor"));
		assertFalse(scn.ShadowActionAvailable(troop1, "Ancient Blade"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Ancient Blade"));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
