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

public class Card_15_154_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breakfast", "15_154");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3; +2 only with a weapon
					put("merry", "1_303");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1: no hunter
					put("savage", "1_151");
					// Goblin Runner: [moria] Orc, strength 5, vitality 1 (a wound kills him); his "When you play"
					// trigger does not fire when he is cheated in
					put("runner", "1_178");
					// Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3; Hunter 1 is its whole text
					put("tracker", "15_171");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: [shire], strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breakfast", "15_154");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1: no hunter
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: an [elven] Ring-bearer, vitality 3; her only text changes her own
				// resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SecondBreakfastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Second Breakfast
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Add a threat and exert your Ring-bearer to wound each non-hunter minion skirmishing your [shire] Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breakfast");

		assertEquals("Second Breakfast", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsAThreatAndExertsTheRingBearerToWoundEachNonHunterMinionSkirmishingHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var breakfast = scn.GetFreepsCard("breakfast");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var tracker = scn.GetShadowCard("tracker");
		scn.MoveCardsToHand(breakfast);

		scn.StartGame();
		assertInZone(Zone.HAND, breakfast);
		scn.MoveMinionsToTable(savage, runner, tracker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		// The other two minions are the Shadow player's to put on Frodo.
		scn.ShadowAssignToMinions(frodo, runner, tracker);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, tracker));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(tracker));
		assertInZone(Zone.HAND, breakfast);
		assertTrue(scn.FreepsPlayAvailable(breakfast));

		scn.FreepsPlayCard(breakfast);

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The [isengard] Uruk-hai is wounded, and the [moria] Orc, of vitality 1, is wounded and killed.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(runner);
		// The hunter in the same skirmish is not wounded.
		assertEquals(0, scn.GetWoundsOn(tracker));
		assertInDiscard(breakfast);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedInAnotherCompanionsSkirmishItWoundsNeitherHisMinionNorOneWaitingForTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var breakfast = scn.GetFreepsCard("breakfast");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(breakfast);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage}, new PhysicalCardImpl[]{frodo, runner});
		scn.FreepsResolveSkirmish(merry);

		// The Savage skirmishes Merry, who is no Ring-bearer; the Runner is assigned to Frodo, whose skirmish
		// has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage));
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(breakfast));

		scn.FreepsPlayCard(breakfast);

		// Played and paid for, for nothing.
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInDiscard(breakfast);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithARingBearerWhoIsNotShireItIsPlayedAndWoundsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var breakfast = scn.GetFreepsCard("breakfast");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(breakfast);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertEquals(Culture.ELVEN, galadriel.getBlueprint().getCulture());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, savage);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(breakfast));

		scn.FreepsPlayCard(breakfast);

		// Her exertion and the threat are paid; the minion skirmishing her is not wounded.
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(breakfast);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breakfast = scn.GetFreepsCard("breakfast");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(breakfast);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInZone(Zone.HAND, breakfast);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(breakfast));
	}

	@Test
	public void CannotBePlayedWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var breakfast = scn.GetFreepsCard("breakfast");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(breakfast);

		scn.StartGame();
		// One companion: one threat is the most there can be.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, savage);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(breakfast));
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void CannotBePlayedWithTheRingBearerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var breakfast = scn.GetFreepsCard("breakfast");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(breakfast);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.  Two companions, so a threat could be added.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, savage);

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(breakfast));
		assertEquals(3, scn.GetWoundsOn(frodo));
	}
}
