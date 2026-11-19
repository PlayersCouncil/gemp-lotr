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

public class Card_07_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("city", "7_83");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man, strength 5; his skirmish ability needs him mounted

					put("attacker", "7_270");   // Gorgoroth Attacker: [sauron] Orc, strength 9, vitality 2; its trigger answers a play from a controlled site only
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, strength 7, vitality 2; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; cheated in, so its text does nothing
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CityofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: City of Men
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [gondor] Man strength +2. If that Man is skirmishing a [sauron] minion, discard that minion at the end of the skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("city");

		assertEquals("City of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorManStrengthPlus2AndDiscardsTheSauronMinionHeIsSkirmishingAtTheEndOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, boromir, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(city);

		var attacker = scn.GetShadowCard("attacker");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(attacker, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, attacker},
				new PhysicalCardImpl[]{boromir, runner});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(0, scn.GetWoundsOn(attacker));
		assertTrue(scn.FreepsPlayAvailable(city));

		scn.FreepsPlayCard(city);
		// [gondor] Men: the companion in this skirmish, a companion waiting for a skirmish of his own, and the ally.
		// Not a [gondor] Hobbit, a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(merchant));
		assertInDiscard(city);
		// The skirmish is not over: the Attacker is still in play.
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 10 against 9: Aragorn wins and the Attacker, of vitality 2, lives through its wound; the end of the skirmish
		// discards it.
		scn.PassSkirmishActions();
		assertInDiscard(attacker);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void TheSauronMinionIsDiscardedThoughTheManLosesAndAnotherMinionInTheSkirmishIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(city);

		var attacker = scn.GetShadowCard("attacker");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(attacker, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attacker);
		// The Runner, left over, is put on Aragorn as well by the Shadow player.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(city);
		assertEquals(10, scn.GetStrength(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, attacker, runner);

		// 10 against 9 + 5: Aragorn loses, and takes one wound.  The [sauron] minion is discarded all the same; the
		// [moria] one stays.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(attacker);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AManWaitingForHisOwnSkirmishAgainstASauronMinionIsStrongerForThisSkirmishOnlyAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(city);

		var attacker = scn.GetShadowCard("attacker");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(attacker, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{boromir, attacker});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPlayCard(city);
		scn.FreepsChooseCard(boromir);
		// Boromir is assigned to the Attacker, but is not skirmishing it yet.
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);

		// The strength lasted the skirmish phase it was played in: in his own skirmish Boromir is 7 again.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));

		// 7 against 9: he loses, and the end of his skirmish discards nothing either.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(city);

		var attacker = scn.GetShadowCard("attacker");

		scn.StartGame();
		scn.MoveMinionsToTable(attacker);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(city);
		assertFalse(scn.FreepsPlayAvailable(city));

		// The same board one phase on.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, attacker);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(city));
	}

	@Test
	public void IsPlayedForNothingWithNoGondorManAndASauronMinionSkirmishingAnotherManIsNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(pippin, rider);
		scn.MoveCardsToHand(city);

		var attacker = scn.GetShadowCard("attacker");

		scn.StartGame();
		scn.MoveMinionsToTable(attacker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, attacker);
		scn.FreepsResolveSkirmish(rider);

		assertEquals(5, scn.GetStrength(rider));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(city));

		// A [rohan] Man skirmishing the [sauron] minion, a [gondor] Hobbit and Frodo: the event is played, and does nothing.
		scn.FreepsPlayCard(city);
		assertInDiscard(city);
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 5 against 9: the Rider loses; the Attacker is not discarded.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyOneOfTwoSauronMinionsSkirmishingHimIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var city = scn.GetFreepsCard("city");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(city);

		var attacker = scn.GetShadowCard("attacker");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.MoveMinionsToTable(attacker, lights);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attacker);
		scn.ShadowAssignToMinions(aragorn, lights);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(city);
		assertEquals(10, scn.GetStrength(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, attacker, lights);

		// 10 against 9 + 7: Aragorn loses and takes one wound.  Then one of the two [sauron] minions, an Orc and a
		// Wraith, is discarded, of the Free Peoples player's choosing, and the other stays (CR 4.2 Section Three).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(attacker, lights));
		scn.FreepsChooseCard(lights);
		assertInDiscard(lights);
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
