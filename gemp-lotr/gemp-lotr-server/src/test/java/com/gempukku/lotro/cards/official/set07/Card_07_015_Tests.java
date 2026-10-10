package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_15");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Ancient Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be an [elven] companion.<br>Each minion gains this ability: "<b>Assignment:</b> Assign this minion to bearer of Ancient Blade."<br><b>Regroup:</b> Discard this condition to discard a minion and reconcile your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Ancient Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "7_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion; her text needs a Nazgul
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

	// The definition named Reckless Pride 7_13 in the ability it gives, twice (F-0351): with both conditions in play
	// the minion went to Reckless Pride's bearer, and with Ancient Blade alone to nobody.  The ability costs
	// nothing, so it is not offered where it would assign nobody (Ketura on Q-0034, F-0353; CR 4.2 "assignment
	// phase": "Many assignment actions assign a minion to a companion.  You cannot do this unless both of them are
	// unassigned").
	@Test
	public void EachMinionMayAssignItselfToTheBearerOfAncientBladeAndNotOnceEitherIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		var pride = scn.GetFreepsCard("pride");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, blade);
		scn.AttachCardsTo(gimli, pride);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion has the ability, in the words of this card.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1, "Ancient Blade"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Ancient Blade"));
		assertFalse(scn.IsCharAssigned(arwen));
		assertFalse(scn.IsCharAssigned(troop1));

		scn.ShadowUseCardAction(troop1, "Ancient Blade");

		// The minion is assigned to the bearer of Ancient Blade, not to the bearer of the other condition.
		assertTrue(scn.IsCharAssignedAgainst(arwen, troop1));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(troop2));

		scn.FreepsPass();

		// The assigned minion has the ability no longer, and neither has the other minion now that the bearer is
		// assigned.  Reckless Pride's, whose bearer is not, is still the unassigned minion's to use.
		assertFalse(scn.ShadowActionAvailable(troop1, "Ancient Blade"));
		assertFalse(scn.ShadowActionAvailable(troop2, "Ancient Blade"));
		assertFalse(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Reckless Pride"));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
