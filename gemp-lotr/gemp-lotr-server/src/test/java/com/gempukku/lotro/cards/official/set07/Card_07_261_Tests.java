package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_261_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "7_261");
					put("strength2", "7_261");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, vitality 3; a skirmish ability used only on purpose
					put("household", "5_83");   // Household Guard: a [rohan] Man companion of another title, vitality 3; valiant is its whole text
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, vitality 4; his heal answers a [rohan]
					// Man exerting, and nobody exerts here
					put("merry", "103_85");
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] ally, vitality 2; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion, vitality 3; a skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithStrengthtoFightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: With Strength to Fight
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [rohan] companion.
		 * 	Each time the fellowship moves, add a threat or discard this condition.
		 * 	<b>Regroup:</b> Discard this condition to heal up to 2 [rohan] companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strength");

		assertEquals("With Strength to Fight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilARohanCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var merry = scn.GetFreepsCard("merry");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(strength, merry);
		assertInHand(strength, merry);

		scn.StartGame();

		// A [rohan] ally and a [gondor] companion: no [rohan] companion to spot.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(strength));

		// Merry, a [rohan] companion who is a Hobbit.
		scn.FreepsPlayCard(merry);
		assertTrue(scn.FreepsPlayAvailable(strength));

		scn.FreepsPlayCard(strength);
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManCompanionIsARohanCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(strength);
		assertInHand(strength);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(strength));
		scn.FreepsPlayCard(strength);
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveOfTheFellowshipAsksForAThreatOrItsDiscardAndAThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var boromir = scn.GetFreepsCard("boromir");
		// Frodo and Boromir: room for two threats.
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(strength);
		assertInZone(Zone.SUPPORT, strength);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// Moving from site 1 to site 2.
		scn.FreepsPass();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, strength);

		// No minion: on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAMoveItMayBeDiscardedInsteadOfAddingAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(strength);
		assertInZone(Zone.SUPPORT, strength);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		scn.FreepsChooseOption("Discard this condition");

		assertInDiscard(strength);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheMoveDiscardsItUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		scn.MoveCardsToSupportArea(strength);
		assertInZone(Zone.SUPPORT, strength);

		scn.StartGame();
		// Frodo is the only companion: one threat is all there may be.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());

		scn.FreepsPass();

		// A threat cannot be added, so discarding is all that can be done.
		assertFalse(scn.FreepsChoiceAvailable("Add a threat"));
		assertInDiscard(strength);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnAMoveAndDiscardingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var strength2 = scn.GetFreepsCard("strength2");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(strength, strength2);
		assertInZone(Zone.SUPPORT, strength, strength2);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// The copy laid first answers first: discarding it discards that copy alone.
		scn.FreepsPass();
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(strength);
		assertInZone(Zone.SUPPORT, strength2);

		// The second copy asks for itself.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, strength2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingInTheRegroupPhaseAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToMovementDecision();
		// Put down after the first move of the turn, so that the regroup decision is the first it sees.
		scn.MoveCardsToSupportArea(strength);
		assertInZone(Zone.SUPPORT, strength);
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void RegroupAbilityDiscardsItToHealUpToTwoRohanCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var strength2 = scn.GetFreepsCard("strength2");
		var rider = scn.GetFreepsCard("rider");
		var household = scn.GetFreepsCard("household");
		var merry = scn.GetFreepsCard("merry");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(rider, household, merry, boromir);
		scn.MoveAlliesToTable(aldor);

		scn.StartGame();
		// No minion in the Shadow phase: straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// Laid after the move from site 1, which would otherwise ask each copy for a threat or a discard.
		scn.MoveCardsToSupportArea(strength, strength2);
		scn.AddWoundsToChar(rider, 2);
		scn.AddWoundsToChar(household, 1);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(aldor, 1);
		scn.AddWoundsToChar(boromir, 1);
		assertInZone(Zone.SUPPORT, strength, strength2);
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(household));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(strength));
		scn.FreepsUseCardAction(strength);

		// The cost: this copy is discarded, the other stays.
		assertInDiscard(strength);
		assertInZone(Zone.SUPPORT, strength2);

		// Three wounded [rohan] companions, two Men and a Hobbit; not the [rohan] ally, nor the [gondor] companion.
		// Up to 2: from none to two.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, household, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor, boromir));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(rider, merry);

		// One wound each.
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(household));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedRohanCompanionTheRegroupAbilityDiscardsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(rider, boromir);
		scn.MoveAlliesToTable(aldor);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(strength);
		scn.AddWoundsToChar(aldor, 1);
		scn.AddWoundsToChar(boromir, 1);
		assertInZone(Zone.SUPPORT, strength);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.FreepsRefreshDecision();

		// The Rider is unwounded; the wounded ones are no [rohan] companions.
		assertTrue(scn.FreepsActionAvailable(strength));
		scn.FreepsUseCardAction(strength);

		assertInDiscard(strength);
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetFreepsCard("strength");
		var rider = scn.GetFreepsCard("rider");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rider);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(strength);
		scn.AddWoundsToChar(rider, 1);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, strength);
		assertEquals(1, scn.GetWoundsOn(rider));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(strength));
		assertInZone(Zone.SUPPORT, strength);
	}
}
