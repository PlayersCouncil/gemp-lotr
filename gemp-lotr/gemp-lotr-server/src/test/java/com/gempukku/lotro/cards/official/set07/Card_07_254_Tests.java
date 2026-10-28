package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_254_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "7_254");
					put("sonofthengel", "4_292"); // Théoden, Son of Thengel: another Théoden, cost 2, held in hand only
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, cost 2; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside three [rohan] companions or
					// Éowyn), vitality 4.  "Each time another [rohan] Man exerts, you may exert Merry to heal that Man": only
					// Merry himself exerts in these tests, so it never comes up.
					put("merry", "103_85");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man who is an ally
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] Man ally; an archery ability that needs a villager
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man and a companion, not [rohan], cost 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion; a skirmish ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its play trigger is never met
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Théoden, Leader of Spears
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Valiant</b>. While you can spot a [rohan] Man, Théoden's twilight cost is -1.<br>Each time the fellowship moves, you may exert a [rohan] companion to exert a minion.
		*/

		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", theoden.getBlueprint().getTitle());
		assertEquals("Leader of Spears", theoden.getBlueprint().getSubtitle());
		assertTrue(theoden.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, theoden.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, theoden.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, theoden.getBlueprint().getCardType());
		assertEquals(Race.MAN, theoden.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(theoden, Keyword.VALIANT));
		assertEquals(3, theoden.getBlueprint().getTwilightCost());
		assertEquals(7, theoden.getBlueprint().getStrength());
		assertEquals(3, theoden.getBlueprint().getVitality());
		assertEquals(6, theoden.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, theoden.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForThreeBesideAManOfAnotherCultureAndARohanHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(theoden);
		// Merry is a Man only beside three [rohan] companions or Éowyn: here he is a [rohan] Hobbit.
		scn.MoveCompanionsToTable(aragorn, merry);

		scn.StartGame();

		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(theoden));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(theoden));
		scn.FreepsPlayCard(theoden);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CostsOneLessOnceARohanManIsPlayedAndAnotherTheodenInHandDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var sonOfThengel = scn.GetFreepsCard("sonofthengel");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(theoden, sonOfThengel, guard);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(3, scn.GetTwilightCost(theoden));
		assertEquals(2, scn.GetTwilightCost(sonOfThengel));

		scn.FreepsPlayCard(guard);
		assertEquals(2, scn.GetTwilight());

		// The text names this card: the other Théoden keeps his printed 2.
		assertEquals(2, scn.GetTwilightCost(theoden));
		assertEquals(2, scn.GetTwilightCost(sonOfThengel));

		scn.FreepsPlayCard(theoden);

		// 2 for the Guard + 3 - 1 for Théoden
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		// In play his cost is the number printed on him, though he is a [rohan] Man himself beside another.
		assertEquals(3, scn.GetTwilightCost(theoden));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManWhoIsAnAllyIsSpottedForTheLowerCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToHand(theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(theoden));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(theoden);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveOffersToExertARohanCompanionToExertAMinionEvenWithNoMinionToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var aldor = scn.GetFreepsCard("aldor");
		scn.MoveCompanionsToTable(theoden, merry, boromir);
		scn.MoveAlliesToTable(aldor);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(merry));
		// Two [rohan] companions and no Éowyn: Merry is a Hobbit.
		assertFalse(scn.IsRace(merry, Race.MAN));

		// The move that ends the fellowship phase: no minion is in play, and the offer is made all the same (it has a cost).
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsAcceptOptionalTrigger();

		// "A [rohan] companion": a Man (Théoden himself) and a Hobbit; not a [rohan] ally, nor a companion of [gondor].
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(theoden));
		// Nothing to exert: the Shadow phase begins at site 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minion, no combat: the regroup phase, where minions that lived through the skirmishes would still stand.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));

		// The second move of the turn: offered again.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(merry);
		assertEquals(2, scn.GetWoundsOn(merry));

		// "A minion" of either culture and race; not the exhausted Runner, who cannot exert, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, theoden, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theoden));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheOnlyRohanCompanionExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var boromir = scn.GetFreepsCard("boromir");
		var aldor = scn.GetFreepsCard("aldor");
		scn.MoveCompanionsToTable(theoden, boromir);
		scn.MoveAlliesToTable(aldor);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Théoden cannot exert.  Boromir, of [gondor], and Aldor, an ally, could.
		scn.AddWoundsToChar(theoden, 2);
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aldor));

		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(theoden));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheMovementDecisionMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(theoden));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(savage));
		// The turn is over: the other player's fellowship phase.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
