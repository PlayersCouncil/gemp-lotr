package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_179_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eyrie", "13_179");
					put("eyrie2", "13_179");
					put("rider", "12_161");      // Black Rider: [wraith] Nazgûl, fierce, strength 8 +1 for each companion you can spot
					put("rider2", "12_161");
					put("khamul", "103_61");     // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl, fierce, strength 12; the rest needs 3 burdens
					put("hound", "7_192");       // Morgul Hound: [wraith] Orc, strength 9 and fierce while the Ring-bearer has 2 wounds

					// [wraith] cards for the discard pile, of each kind, where none of them does anything there
					put("mongrel", "7_194");     // Morgul Mongrel: [wraith] minion
					put("blade", "12_168");      // Nazgûl Blade: [wraith] possession
					put("destroyed", "1_220");   // Not Easily Destroyed: [wraith] condition
					put("wail", "11_211");       // Keening Wail: [wraith] event
					put("swooping", "10_58");    // Dark Swooping Shadows: [wraith] event, laid only in the Free Peoples player's pile

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8; a maneuver ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: strength 3; a fellowship ability and a response when Frodo dies
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FromHideousEyrieStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: From Hideous Eyrie
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 [wraith] cards in your discard pile instead of paying the twilight cost for this card.<br>Make your Nazgûl in a fierce skirmish strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eyrie");

		assertEquals("From Hideous Eyrie", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesANazgulInTheFierceSkirmishStrengthPlus2UntilThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		var frodo = scn.GetRingBearer();

		var eyrie = scn.GetShadowCard("eyrie");
		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(eyrie);

		scn.StartGame();
		// Two wounds on the Ring-bearer make the Morgul Hound fierce.
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInHand(eyrie);
		scn.MoveMinionsToTable(rider, rider2, khamul, hound);
		scn.SkipToAssignments();
		// Nobody is assigned in the first round: every minion here is fierce, so the fierce round follows.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider}, new PhysicalCardImpl[]{sam, rider2});
		scn.ShadowAssignToMinions(aragorn, khamul, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(eyrie));
		scn.ShadowPlayCard(eyrie);

		// Either Nazgûl of Aragorn's fierce skirmish, the [raider] one as well; not the [wraith] Orc beside them, nor the
		// Nazgûl assigned to Sam, whose fierce skirmish has not begun.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, rider2, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(12, scn.GetStrength(khamul));
		// Three companions: 8 + 3.
		assertEquals(11, scn.GetStrength(rider));
		scn.ShadowChooseCard(khamul);

		assertEquals(14, scn.GetStrength(khamul));
		assertEquals(11, scn.GetStrength(rider));
		assertEquals(11, scn.GetStrength(rider2));
		assertEquals(9, scn.GetStrength(hound));
		assertInDiscard(eyrie);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's skirmish resolves; in Sam's, a phase of its own, the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InANormalSkirmishItIsPlayedForNothingAndInTheFierceAssignmentPhaseItIsNotPlayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var eyrie = scn.GetShadowCard("eyrie");
		var eyrie2 = scn.GetShadowCard("eyrie2");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(eyrie, eyrie2);

		scn.StartGame();
		assertInHand(eyrie, eyrie2);
		scn.MoveMinionsToTable(khamul);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// A fierce Nazgûl in a skirmish that is not a fierce one: the event is played, and he is not strengthened.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(eyrie));
		assertEquals(12, scn.GetStrength(khamul));
		scn.ShadowPlayCard(eyrie);
		assertInDiscard(eyrie);
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The fierce assignment phase: not a skirmish phase, with the pool able to pay and the fierce Nazgûl there.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.InFierceRound());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(eyrie2));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(eyrie2));
		scn.ShadowPlayCard(eyrie2);
		assertEquals(14, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheTwilightAndFourWraithCardsBothWaysToPayAreAskedAndTheCardsAreRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsSwooping = scn.GetFreepsCard("swooping");
		scn.MoveCompanionsToTable(aragorn);

		var eyrie = scn.GetShadowCard("eyrie");
		var rider = scn.GetShadowCard("rider");
		var mongrel = scn.GetShadowCard("mongrel");
		var blade = scn.GetShadowCard("blade");
		var destroyed = scn.GetShadowCard("destroyed");
		var wail = scn.GetShadowCard("wail");
		var rider2 = scn.GetShadowCard("rider2");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(eyrie);
		// Five [wraith] cards (a minion, a Nazgûl, a possession, a condition and an event: one more than the four to be
		// removed), and the [raider] Nazgûl.
		scn.MoveCardsToDiscard(mongrel, rider2, blade, destroyed, wail, khamul);
		// The other player's [wraith] card, in his own discard pile: a title the Shadow player's pile does not hold,
		// since a choice from a pile is read by blueprint.
		scn.MoveCardsToDiscard(freepsSwooping);

		scn.StartGame();
		assertInDiscard(mongrel, rider2, blade, destroyed, wail, khamul, freepsSwooping);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		// Two companions: 8 + 2.
		assertEquals(10, scn.GetStrength(rider));
		scn.ShadowPlayCard(eyrie);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseYes();

		// Exactly four of the five [wraith] cards, of any type; not the [raider] Nazgûl, nor the other player's card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mongrel, rider2, blade, destroyed, wail));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, freepsSwooping));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(mongrel, blade, destroyed, wail);

		assertInZone(Zone.REMOVED, mongrel, blade, destroyed, wail);
		assertInDiscard(rider2, khamul, freepsSwooping);
		assertEquals(2, scn.GetTwilight());

		// The lone Nazgûl is chosen unasked.
		assertEquals(12, scn.GetStrength(rider));
		assertInDiscard(eyrie);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheRemovalPaysTheTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var eyrie = scn.GetShadowCard("eyrie");
		var rider = scn.GetShadowCard("rider");
		var mongrel = scn.GetShadowCard("mongrel");
		var blade = scn.GetShadowCard("blade");
		var destroyed = scn.GetShadowCard("destroyed");
		var wail = scn.GetShadowCard("wail");
		scn.MoveCardsToHand(eyrie);
		scn.MoveCardsToDiscard(mongrel, blade, destroyed, wail);

		scn.StartGame();
		assertInDiscard(mongrel, blade, destroyed, wail);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(eyrie));
		assertEquals(10, scn.GetStrength(rider));
		scn.ShadowPlayCard(eyrie);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseNo();

		// The twilight cost is paid in place of the removal; the lone Nazgûl is chosen unasked.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(mongrel, blade, destroyed, wail, eyrie);
		assertEquals(12, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithThreeWraithCardsTheTwilightIsPaidUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var eyrie = scn.GetShadowCard("eyrie");
		var rider = scn.GetShadowCard("rider");
		var mongrel = scn.GetShadowCard("mongrel");
		var blade = scn.GetShadowCard("blade");
		var destroyed = scn.GetShadowCard("destroyed");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(eyrie);
		// Four cards, three of them [wraith].
		scn.MoveCardsToDiscard(mongrel, blade, destroyed, khamul);

		scn.StartGame();
		assertInDiscard(mongrel, blade, destroyed, khamul);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(eyrie));
		scn.ShadowPlayCard(eyrie);
		assertFalse(scn.ShadowDecisionAvailable("instead of paying twilight cost"));
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(mongrel, blade, destroyed, khamul, eyrie);
		assertEquals(12, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolItIsPlayedOnlyWithFourWraithCardsToRemoveAndTheyAreRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var eyrie = scn.GetShadowCard("eyrie");
		var rider = scn.GetShadowCard("rider");
		var mongrel = scn.GetShadowCard("mongrel");
		var blade = scn.GetShadowCard("blade");
		var destroyed = scn.GetShadowCard("destroyed");
		var wail = scn.GetShadowCard("wail");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(eyrie);
		// Four cards, three of them [wraith].
		scn.MoveCardsToDiscard(mongrel, blade, destroyed, khamul);

		scn.StartGame();
		assertInDiscard(mongrel, blade, destroyed, khamul);
		scn.MoveMinionsToTable(rider);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(eyrie));

		// A fourth [wraith] card: the removal is the only way to pay, and it is taken with no question.
		scn.MoveCardsToDiscard(wail);
		assertInDiscard(wail);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(eyrie));
		assertEquals(10, scn.GetStrength(rider));
		scn.ShadowPlayCard(eyrie);

		assertFalse(scn.ShadowDecisionAvailable("instead of paying twilight cost"));
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to remove"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(mongrel, blade, destroyed, wail));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul));
		scn.ShadowChooseCards(mongrel, blade, destroyed, wail);
		assertInZone(Zone.REMOVED, mongrel, blade, destroyed, wail);
		assertInDiscard(khamul, eyrie);
		assertEquals(0, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
