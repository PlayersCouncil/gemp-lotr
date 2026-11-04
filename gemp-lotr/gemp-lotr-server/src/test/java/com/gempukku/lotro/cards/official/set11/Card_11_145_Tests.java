package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_145_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "11_145");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, cost 2; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside three [rohan]
					// companions or Éowyn).  "Each time another [rohan] Man exerts, you may exert Merry to heal that
					// Man": he is laid only where nobody exerts.
					put("merry", "103_85");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man who is an ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of [gondor], cost 4, held in hand
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					// Goblin Armory: a [moria] support-area condition; its texts answer a [moria] weapon and a [moria] Orc's wound
					put("armory", "1_173");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Éomer, Guardian of the Eastmark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While you can spot a [rohan] Man, Éomer's twilight cost is -1.<br>Each time the fellowship moves, you may exert Éomer to return a minion to its owner's hand.
		*/

		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", eomer.getBlueprint().getTitle());
		assertEquals("Guardian of the Eastmark", eomer.getBlueprint().getSubtitle());
		assertTrue(eomer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eomer.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eomer.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eomer.getBlueprint().getCardType());
		assertEquals(Race.MAN, eomer.getBlueprint().getRace());
		assertEquals(3, eomer.getBlueprint().getTwilightCost());
		assertEquals(7, eomer.getBlueprint().getStrength());
		assertEquals(3, eomer.getBlueprint().getVitality());
		assertEquals(7, eomer.getBlueprint().getResistance());
		assertNull(eomer.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForThreeBesideAManOfAnotherCultureAndARohanHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(eomer);
		scn.MoveCompanionsToTable(boromir, merry);

		scn.StartGame();

		// One [rohan] companion and no Éowyn: Merry is a Hobbit.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(eomer);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CostsOneLessOnceARohanManIsPlayedAndAnotherCompanionInHandDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, guard, aragorn);

		scn.StartGame();

		assertEquals(3, scn.GetTwilightCost(eomer));
		assertEquals(4, scn.GetTwilightCost(aragorn));

		scn.FreepsPlayCard(guard);
		assertEquals(2, scn.GetTwilight());

		// The text names this card: the other companion in hand keeps his printed 4.
		assertEquals(2, scn.GetTwilightCost(eomer));
		assertEquals(4, scn.GetTwilightCost(aragorn));

		scn.FreepsPlayCard(eomer);

		// 2 for the Guard + 3 - 1 for Éomer
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		// In play his cost is the number printed on him, though a [rohan] Man stands beside him.
		assertEquals(3, scn.GetTwilightCost(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManWhoIsAnAllyIsSpottedForTheLowerCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToHand(eomer);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(eomer));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(eomer);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveOffersToExertEomerToReturnAMinionToItsOwnersHandEvenWithNoMinionToReturn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(eomer, boromir);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(eomer));

		// The move that ends the fellowship phase: no minion is in play, and the offer is made all the same (it has a cost).
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minion, no combat: the regroup phase, where minions that lived through the skirmishes would still stand.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage, troop);
		scn.MoveCardsToSupportArea(armory);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, savage, troop);
		assertInZone(Zone.SUPPORT, armory);
		int shadowHand = scn.GetShadowHandCount();
		int freepsHand = scn.GetFreepsHandCount();

		// The second move of the turn: offered again.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(eomer));

		// "A minion" of either culture and race; not a Shadow card that is no minion, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, eomer, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// The Troop goes to the Shadow player's hand, its owner's; the Savage stays.
		assertInZone(Zone.HAND, troop);
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, armory);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferReturnsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEomerExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 2 wounds: Éomer cannot exert.
		scn.AddWoundsToChar(eomer, 2);
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		scn.SkipToMovementDecision();

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eomer));
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheMovementDecisionMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(savage);
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eomer));
		assertEquals(0, scn.GetWoundsOn(eomer));
		// The turn is over: the other player's fellowship phase.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
