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

public class Card_07_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "7_15");
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

		var card = scn.GetFreepsCard("blade");

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

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "7_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion; her text needs a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an [elven] companion of another title; +2 at sites 6 to 8 only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] ally; an archery ability, not used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion; a skirmish ability, not used here
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1 and nothing else
					// Cards for the hands and the draw deck, with no text
					put("brigand1", "4_10");    // Dunlending Brigand
					put("brigand2", "4_10");
					put("brigand3", "4_10");
					put("brigand4", "4_10");
					put("brigand5", "4_10");
					put("brigand6", "4_10");
					put("brigand7", "4_10");
					put("brigand8", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlaysOnAnElvenCompanionAndNotOnAnElvenAllyOrACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, haldir, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(blade);

		scn.StartGame();

		assertInHand(blade);
		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);

		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(haldir);

		assertInZone(Zone.ATTACHED, blade);
		assertTrue(scn.GetAttachedCards(haldir).contains(blade));
		assertFalse(scn.GetAttachedCards(arwen).contains(blade));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItToDiscardAMinionAndReconcileTheFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, blade);
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertInZone(Zone.ATTACHED, blade);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion, [moria] Orc and [isengard] Uruk-hai, has the assignment ability; neither uses it, and
		// neither is assigned.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop, "Ancient Blade"));
		assertTrue(scn.ShadowActionAvailable(savage, "Ancient Blade"));
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// Three cards in each hand.
		var b1 = scn.GetFreepsCard("brigand1");
		var b2 = scn.GetFreepsCard("brigand2");
		var b3 = scn.GetFreepsCard("brigand3");
		scn.SetFreepsHand(b1, b2, b3);
		scn.SetShadowHand(scn.GetShadowCard("brigand1"), scn.GetShadowCard("brigand2"), scn.GetShadowCard("brigand3"));
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(deck >= 5);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(blade));
		scn.FreepsUseCardAction(blade);

		// The cost.
		assertInDiscard(blade);

		// "A minion": either, of two cultures and races, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertInDiscard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		// The reconcile: one card may be discarded first, then the hand is drawn up to 8.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(b1);
		assertInDiscard(b1);
		assertEquals(8, scn.GetFreepsHandCount());
		// 2 cards kept, 6 drawn.
		assertEquals(deck - 6, scn.GetFreepsDeckCount());

		// The Shadow player's hand is not reconciled.
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheRegroupAbilityStillReconcilesTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, blade);

		scn.StartGame();

		assertInZone(Zone.ATTACHED, blade);
		scn.SkipToPhase(Phase.REGROUP);

		var b1 = scn.GetFreepsCard("brigand1");
		var b2 = scn.GetFreepsCard("brigand2");
		var b3 = scn.GetFreepsCard("brigand3");
		scn.SetFreepsHand(b1, b2, b3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(blade));
		scn.FreepsUseCardAction(blade);

		// Nothing to discard; the hand is still reconciled, here with nothing discarded first.
		assertInDiscard(blade);
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 5, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, blade);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertInZone(Zone.ATTACHED, blade);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blade));
		assertTrue(scn.GetAttachedCards(arwen).contains(blade));
	}
}
