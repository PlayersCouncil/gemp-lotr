package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_215_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("riders", "11_215");
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9; her text reaches only a companion or ally bearing a [wraith] condition
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("mongrel", "7_194");    // Morgul Mongrel: [wraith] Orc; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: a [wraith] Nazgûl, here only in a hand
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his texts need 3 burdens
					put("khamul2", "103_61");   // a second copy, for the hand alone
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, strength 13
					put("troop2", "1_177");
					put("found", "2_78");       // It Wants to be Found: a [wraith] event
					put("steed", "1_208");      // Black Steed: a [wraith] possession
					put("bladetip", "1_209");   // Blade Tip: a [wraith] condition in the support area; its response answers a Nazgûl's win, and none wins here

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RidersinBlackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Riders in Black
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Reveal your hand to make a [wraith] minion strength +1 for each [wraith] minion in your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("riders");

		assertEquals("Riders in Black", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RevealsTheHandAndMakesAWraithMinionStrengthPlus1ForEachOfTheTwoWraithMinionsInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetShadowCard("riders");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var troop = scn.GetShadowCard("troop");
		var bladetip = scn.GetShadowCard("bladetip");
		// In the hand: two [wraith] minions (an Orc and a Nazgûl), beside a [wraith] event, a [wraith] possession,
		// a [moria] minion and a [raider] Nazgûl.
		var mongrel = scn.GetShadowCard("mongrel");
		var witchking = scn.GetShadowCard("witchking");
		var found = scn.GetShadowCard("found");
		var steed = scn.GetShadowCard("steed");
		var troop2 = scn.GetShadowCard("troop2");
		var khamul2 = scn.GetShadowCard("khamul2");
		scn.MoveCardsToHand(riders, mongrel, witchking, found, steed, troop2, khamul2);
		scn.MoveCardsToSupportArea(bladetip);
		// [wraith] minions in the other player's hand are not "your hand": three of them, so that his hand would
		// read otherwise.
		scn.MoveCardsToHand(scn.GetFreepsCard("mongrel"), scn.GetFreepsCard("witchking"), scn.GetFreepsCard("lemenya"));

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound, khamul, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, troop});
		// The Hound and Khamûl stay out of any skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.ShadowPlayAvailable(riders));
		scn.ShadowPlayCard(riders);

		// The hand is shown to the other player, each card and not to be chosen; its owner is shown nothing.
		assertEquals(6, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(mongrel, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(witchking, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(found, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(steed, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop2, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(khamul2, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// A [wraith] minion: the Nazgûl in the skirmish and the Orc in none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, troop, bladetip));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		// Two [wraith] minions in hand: 9 + 1 + 1.
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(khamul));
		assertInDiscard(riders);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneWraithMinionInHandAMinionInNoSkirmishYetIsPlus1UntilTheSkirmishPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetShadowCard("riders");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCardsToHand(riders, mongrel);

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, hound});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowPlayAvailable(riders));
		scn.ShadowPlayCard(riders);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		scn.ShadowChooseCard(hound);

		// One [wraith] minion in hand: 5 + 1, on the minion whose skirmish with Boromir is still to come.
		assertEquals(6, scn.GetStrength(hound));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 loses to 9.  Each skirmish is a phase of its own, and in Boromir's the Hound is 5 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetShadowCard("riders");
		var lemenya = scn.GetShadowCard("lemenya");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCardsToHand(riders, mongrel);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(riders));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(riders));
	}

	@Test
	public void WithNoWraithMinionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetShadowCard("riders");
		var troop = scn.GetShadowCard("troop");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCardsToHand(riders, mongrel);
		assertInHand(mongrel);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowPlayAvailable(riders));
		scn.ShadowPlayCard(riders);
		// The hand is still revealed: it is the cost.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(mongrel, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(riders);
		assertInHand(mongrel);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherCardInHandItIsPlayedAndTheMinionGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var riders = scn.GetShadowCard("riders");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(riders);

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, hound});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(riders));
		scn.ShadowPlayCard(riders);
		// An empty hand is revealed: nobody is shown anything.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		// The choice of minion is still made.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		scn.ShadowChooseCard(lemenya);

		// No [wraith] minion in hand: 9 + 0.

		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(riders);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
