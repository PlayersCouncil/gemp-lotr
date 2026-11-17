package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("visitor", "7_126");
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

		var card = scn.GetFreepsCard("visitor");

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

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("visitor", "7_126");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability, not used here
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability, not used here
					put("pippin", "103_31");
					// Gondorian Merchant: the [gondor] ally (a Man); a maneuver ability, not used here
					put("merchant", "7_97");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlaysOnAGondorCompanionAndNotOnAGondorAllyOrACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var frodo = scn.GetRingBearer();
		var visitor = scn.GetFreepsCard("visitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(visitor);

		scn.StartGame();

		assertInHand(visitor);
		assertTrue(scn.FreepsPlayAvailable(visitor));
		scn.FreepsPlayCard(visitor);

		// [gondor] companions, a Man and a Hobbit; not the [gondor] ally, a [rohan] companion or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(visitor, pippin);
		assertNotAttachedTo(visitor, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItToDiscardAMinionAndRemoveFourTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var visitor = scn.GetFreepsCard("visitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(aragorn, visitor);
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(visitor, aragorn);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion, [moria] Orc and [isengard] Uruk-hai, has the assignment ability; neither uses it, and
		// neither is assigned.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop, "Unexpected Visitor"));
		assertTrue(scn.ShadowActionAvailable(savage, "Unexpected Visitor"));
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(6);
		scn.FreepsRefreshDecision();
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertTrue(scn.FreepsActionAvailable(visitor));
		scn.FreepsUseCardAction(visitor);

		// The cost.
		assertInDiscard(visitor);

		// "A minion": either, of two cultures and races, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertInDiscard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// 6 - 4.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheRegroupAbilityStillRemovesWhatTwilightThereIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var visitor = scn.GetFreepsCard("visitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		scn.StartGame();

		assertAttachedTo(visitor, aragorn);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(visitor));
		scn.FreepsUseCardAction(visitor);

		// The ability has a cost, so it is offered with nothing to discard; of the (4), the 2 there are removed.
		assertInDiscard(visitor);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var visitor = scn.GetFreepsCard("visitor");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertAttachedTo(visitor, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(visitor));
		assertAttachedTo(visitor, aragorn);
	}
}
