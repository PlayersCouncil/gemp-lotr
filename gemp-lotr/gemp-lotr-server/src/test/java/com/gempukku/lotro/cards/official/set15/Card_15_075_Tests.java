package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("easterling", "15_75");
					put("easterling2", "15_75");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability only ever used on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability only ever used on purpose
					// Man of Bree: [men] Man, cost 2, site 4; strength +1 for each card in the Free Peoples hand
					put("bree", "11_90");
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), cost 4, site 4; his +2 wants a
					// [men] minion bearing a possession, and his skirmish ability a [men] possession in the pile: neither here
					put("saruman", "17_48");
					// Hemmed In: a [men] condition, no minion; its texts want six companions or a maneuver phase
					put("hemmed", "12_70");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					// Rampaging Easterling: a [men] Man; laid in the Free Peoples player's discard pile only
					put("rampager", "11_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CourageousEasterlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Courageous Easterling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> If this minion is skirmishing, discard this minion to play a [men] minion from your discard pile. That minion is <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");

		assertEquals("Courageous Easterling", easterling.getBlueprint().getTitle());
		assertNull(easterling.getBlueprint().getSubtitle());
		assertFalse(easterling.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, easterling.getBlueprint().getSide());
		assertEquals(Culture.MEN, easterling.getBlueprint().getCulture());
		assertEquals(CardType.MINION, easterling.getBlueprint().getCardType());
		assertEquals(Race.MAN, easterling.getBlueprint().getRace());
		assertEquals(1, easterling.getBlueprint().getTwilightCost());
		assertEquals(5, easterling.getBlueprint().getStrength());
		assertEquals(1, easterling.getBlueprint().getVitality());
		assertEquals(4, easterling.getBlueprint().getSiteNumber());
	}

	@Test
	public void InItsOwnSkirmishItDiscardsItselfToPlayAMenMinionFromTheDiscardPileWhichIsMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var easterling2 = scn.GetShadowCard("easterling2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		var hemmed = scn.GetShadowCard("hemmed");
		var brigand = scn.GetShadowCard("brigand");
		var rampager = scn.GetFreepsCard("rampager");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, easterling2);
		scn.MoveCardsToDiscard(bree, saruman, hemmed, brigand, rampager);
		assertInZone(Zone.SHADOW_CHARACTERS, easterling, easterling2);
		assertInDiscard(bree, saruman, hemmed, brigand, rampager);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, easterling}, new PhysicalCardImpl[]{gimli, easterling2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The copy assigned to Gimli is not skirmishing yet: only the one in this skirmish is offered.
		assertTrue(scn.IsCharSkirmishing(easterling));
		assertTrue(scn.IsCharAssigned(easterling2));
		assertFalse(scn.ShadowActionAvailable(easterling2));
		assertTrue(scn.ShadowActionAvailable(easterling));

		scn.ShadowUseCardAction(easterling);
		assertInDiscard(easterling);
		assertInZone(Zone.SHADOW_CHARACTERS, easterling2);

		// A [men] Man and a [men] Wizard; not a [men] condition, a [dunland] Man, or the Free Peoples player's own
		// [men] Man in his discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bree, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hemmed, brigand, rampager));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(saruman);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertInDiscard(bree);
		assertTrue(scn.HasKeyword(saruman, Keyword.FIERCE));
		// The other [men] minion in play is not made fierce.
		assertFalse(scn.HasKeyword(easterling2, Keyword.FIERCE));
		// With its only minion gone, Aragorn's skirmish is over: Gimli's is next.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotOfferedWhileNoMenMinionInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bree = scn.GetShadowCard("bree");
		var hemmed = scn.GetShadowCard("hemmed");
		var brigand = scn.GetShadowCard("brigand");
		var rampager = scn.GetFreepsCard("rampager");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling);
		scn.MoveCardsToDiscard(hemmed, brigand, rampager);
		assertInZone(Zone.SHADOW_CHARACTERS, easterling);
		assertInDiscard(hemmed, brigand, rampager);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, easterling);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The play is a cost (Q-0062): with no [men] minion of his own in the pile it cannot be paid.
		assertTrue(scn.IsCharSkirmishing(easterling));
		assertFalse(scn.ShadowActionAvailable(easterling));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Man of Bree at site 2 costs 2 + 2 roaming: a pool of 3 cannot pay for him, a pool of 4 can.
		scn.MoveCardsToDiscard(bree);
		assertInDiscard(bree);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(easterling));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(easterling));
		assertInZone(Zone.SHADOW_CHARACTERS, easterling);
	}

	@Test
	public void TheMinionPlayedIsFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var easterling2 = scn.GetShadowCard("easterling2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling, easterling2);
		// Two [men] minions in the pile, so that the choice is asked.
		scn.MoveCardsToDiscard(bree, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, easterling, easterling2);
		assertInDiscard(bree, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, easterling}, new PhysicalCardImpl[]{gimli, easterling2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertFalse(scn.HasKeyword(bree, Keyword.FIERCE));
		scn.ShadowUseCardAction(easterling);
		scn.ShadowChooseCardBPFromSelection(bree);
		assertInZone(Zone.SHADOW_CHARACTERS, bree);
		assertTrue(scn.HasKeyword(bree, Keyword.FIERCE));

		// With its only minion gone, Aragorn's skirmish is over at once.
		scn.FreepsResolveSkirmish(gimli);
		// The next skirmish, a phase of its own: still fierce.
		assertTrue(scn.IsCharSkirmishing(easterling2));
		assertTrue(scn.HasKeyword(bree, Keyword.FIERCE));

		scn.PassSkirmishActions();
		// Gimli (6) beats the second copy (5), which dies; Man of Bree, fierce, brings on the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.HasKeyword(bree, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, bree);
		assertFalse(scn.HasKeyword(bree, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheEasterlingDiscardedForTheCostIsNotTheMinionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(easterling);
		scn.MoveCardsToDiscard(bree, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, easterling);
		assertInDiscard(bree, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, easterling);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(easterling);
		assertInDiscard(easterling);
		// The card the cost discarded is not the card played (Q-0192, Q-0320, Q-0331).
		assertTrue(scn.ShadowHasCardChoiceAvailable(bree, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling));
		scn.ShadowChooseCardBPFromSelection(bree);

		assertInZone(Zone.SHADOW_CHARACTERS, bree);
		assertInDiscard(easterling, saruman);
		// Aragorn's skirmish, the only one, is over with its minion gone; Man of Bree, made fierce, brings on the
		// fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
