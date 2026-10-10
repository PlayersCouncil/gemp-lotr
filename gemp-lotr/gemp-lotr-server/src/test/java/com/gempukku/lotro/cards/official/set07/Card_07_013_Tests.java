package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_13");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RecklessPrideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Reckless Pride
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be a [dwarven] companion.<br>Each minion gains this ability: "<b>Assignment:</b> Assign this minion to bearer of Reckless Pride."<br><b>Regroup:</b> Discard this condition to draw 4 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Reckless Pride", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pride", "7_13");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion; a skirmish ability, not used here
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

	// The ability costs nothing, so it is not offered where it would assign nobody (Ketura on Q-0034, F-0353;
	// CR 4.2 "assignment phase": "Many assignment actions assign a minion to a companion.  You cannot do this
	// unless both of them are unassigned").
	@Test
	public void EachMinionMayAssignItselfToTheBearerOfRecklessPrideAndNotOnceEitherIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var pride = scn.GetFreepsCard("pride");
		var gimli = scn.GetFreepsCard("gimli");
		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(gimli, arwen);
		scn.AttachCardsTo(gimli, pride);
		scn.AttachCardsTo(arwen, blade);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion has the ability, in the words of this card.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Reckless Pride"));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(troop1));

		scn.ShadowUseCardAction(troop1, "Reckless Pride");

		assertTrue(scn.IsCharAssignedAgainst(gimli, troop1));
		assertFalse(scn.IsCharAssigned(arwen));
		assertFalse(scn.IsCharAssigned(troop2));

		scn.FreepsPass();

		// The assigned minion has the ability no longer, and neither has the other minion now that the bearer is
		// assigned.  Ancient Blade's, whose bearer is not, is still the unassigned minion's to use.
		assertFalse(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
		assertFalse(scn.ShadowActionAvailable(troop2, "Reckless Pride"));
		assertFalse(scn.ShadowActionAvailable(troop1, "Ancient Blade"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Ancient Blade"));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	protected VirtualTableScenario GetDefenderScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pride", "7_13");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion, vitality 3; a skirmish ability, not used here
					// Disquiet of Our People: "Maneuver: Exert a Dwarf to make that Dwarf defender +1 (or defender +2
					// if you spot an Orc) until the regroup phase."
					put("disquiet", "2_2");
					put("savage1", "1_151");    // Uruk Savage: an Uruk-hai, so no Orc is spotted; damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// CR 4.2 "assignment phase": an assignment action assigns a minion to a companion only if "both of them are
	// unassigned"; "defender +X" is the Free Peoples player's own to use when he assigns ("you may assign that
	// character to one additional unassigned minion").  So once the bearer has a minion the ability would assign
	// nobody, whatever his defender bonus, and by Q-0034 it is not offered.
	@Test
	@Ignore("F-0355: with its bearer defender +1 and already assigned, the ability is still offered to another minion (and not flagged); used, it assigns nobody. The word assignableToSkirmishAgainst counts a defender bonus that an assignment action cannot use.")
	public void WithItsBearerAssignedTheAbilityIsNotOfferedThoughHeHasDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDefenderScenario();

		var pride = scn.GetFreepsCard("pride");
		var gimli = scn.GetFreepsCard("gimli");
		var disquiet = scn.GetFreepsCard("disquiet");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, pride);
		scn.MoveCardsToHand(disquiet);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Gimli is the only Dwarf, so he is the one exerted.
		scn.FreepsPlayCard(disquiet);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(savage1, "Reckless Pride"));
		assertTrue(scn.ShadowActionAvailable(savage2, "Reckless Pride"));
		scn.ShadowUseCardAction(savage1, "Reckless Pride");
		assertTrue(scn.IsCharAssignedAgainst(gimli, savage1));

		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(savage2, "Reckless Pride"));
		assertFalse(scn.IsCharAssigned(savage2));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
