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

public class Card_07_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beacons", "7_43");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a [gandalf] companion, strength 8, vitality 4, no text
					// Ent Avenger: a [gandalf] companion who is no Gandalf, strength 6, vitality 4; +2 only beside 3 Ents
					put("avenger", "6_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man, strength 8; a maneuver ability only
					// Pippin, Trollslayer: an unbound [gondor] Hobbit, strength 3; enduring (left unwounded), a skirmish ability only
					put("pippin", "103_31");
					put("faramir", "4_117");    // Faramir, Son of Denethor: a Ring-bound [gondor] Man, strength 7
					put("legolas", "1_50");     // Legolas, Greenleaf: an unbound [elven] companion, strength 6; an archery ability only
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] ally, strength 6; a maneuver ability only

					// Gandalf's Wisdom: a [gandalf] condition, here only cards in hand to count toward initiative
					put("wisdom1", "2_23");
					put("wisdom2", "2_23");
					put("wisdom3", "2_23");
					put("wisdom4", "2_23");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSnapshotScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beacons", "7_43");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a [gandalf] companion, vitality 4, no text
					// Éomer, Éored Leader: an unbound [rohan] Man, strength 7.  "Maneuver: If you can spot more minions than
					// companions, exert Éomer to play a [rohan] companion from your draw deck."
					put("eomer", "12_112");
					// Théoden, Tall and Proud: an unbound [rohan] Man, strength 7; valiant, a cheaper play beside a [rohan] Man
					put("theoden", "8_92");

					put("runner1", "1_178");    // Goblin Runner: four of them, more minions than the three companions
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LightTheBeaconsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Light the Beacons
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Gandalf three times (or twice if you have initiative) to make all unbound companions of one culture (except [gandalf]) strength +3 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beacons");

		assertEquals("Light the Beacons", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FromAHandOfFourWithItselfGandalfExertsThreeTimesAndEveryUnboundCompanionOfTheCultureChosenIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacons = scn.GetFreepsCard("beacons");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var faramir = scn.GetFreepsCard("faramir");
		var legolas = scn.GetFreepsCard("legolas");
		var merchant = scn.GetFreepsCard("merchant");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn, pippin, faramir, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(beacons, scn.GetFreepsCard("wisdom1"), scn.GetFreepsCard("wisdom2"), scn.GetFreepsCard("wisdom3"));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Four cards in hand, the event among them: the hand has initiative, but the event played does not count
		// toward it (CR 4.2 "playing a card"), so the price is three exertions.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(4, scn.GetStrength(frodo));     // 3 + 1 for The Ruling Ring
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.FreepsPlayAvailable(beacons));

		scn.FreepsPlayCard(beacons);

		// Gandalf, the one Gandalf, exerts three times; the [gandalf] companion who is no Gandalf does not.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// The culture is named by an unbound companion of it, any culture but [gandalf].
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin, legolas));
		// Not Gandalf's culture, not a Ring-bound companion (Faramir, the Ring-bearer); an ally is no unbound companion.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, avenger, faramir, frodo, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		// Every unbound [gondor] companion, Man and Hobbit alike: not the Ring-bound [gondor] Man, not the [gondor]
		// ally, not the companions of other cultures.
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetStrength(frodo));
		assertInDiscard(beacons);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void FromAHandOfFiveWithItselfGandalfExertsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacons = scn.GetFreepsCard("beacons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn, legolas);
		scn.MoveCardsToHand(beacons, scn.GetFreepsCard("wisdom1"), scn.GetFreepsCard("wisdom2"),
				scn.GetFreepsCard("wisdom3"), scn.GetFreepsCard("wisdom4"));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Four cards beside the event: initiative.
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsPlayCard(beacons);
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// Another culture chosen this time: [elven].
		scn.FreepsChooseCard(legolas);
		assertEquals(9, scn.GetStrength(legolas));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(beacons);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithoutInitiativeCannotBePlayedWhileGandalfCannotExertThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacons = scn.GetFreepsCard("beacons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wisdom4 = scn.GetFreepsCard("wisdom4");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveCardsToHand(beacons, scn.GetFreepsCard("wisdom1"), scn.GetFreepsCard("wisdom2"), scn.GetFreepsCard("wisdom3"));
		// Vitality 4 with 1 wound: he can exert twice but not three times.  The [gandalf] companion beside him could.
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Four cards in hand with the event: three exertions are asked, and cannot be paid.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(beacons));

		// A fifth card: initiative even without the event, and two exertions are enough.
		scn.MoveCardsToHand(wisdom4);
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(beacons));

		scn.FreepsPlayCard(beacons);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithInitiativeCannotBePlayedWhileGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacons = scn.GetFreepsCard("beacons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveCardsToHand(beacons, scn.GetFreepsCard("wisdom1"), scn.GetFreepsCard("wisdom2"),
				scn.GetFreepsCard("wisdom3"), scn.GetFreepsCard("wisdom4"));
		// Vitality 4 with 2 wounds: he can exert once but not twice.
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(beacons));
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacons = scn.GetFreepsCard("beacons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(beacons);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(beacons));
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beacons = scn.GetFreepsCard("beacons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(beacons);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(beacons);
		// Aragorn is the only unbound companion who is not [gandalf]: his culture is taken unasked.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(11, scn.GetStrength(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, runner1}, new PhysicalCardImpl[]{aragorn, runner2});

		// Gandalf's skirmish first; Aragorn still has the bonus in the skirmish after it.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(11, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// Gone at the first regroup decision.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void ACompanionOfTheCultureChosenWhoComesIntoPlayAfterwardsIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSnapshotScenario();

		var beacons = scn.GetFreepsCard("beacons");
		var gandalf = scn.GetFreepsCard("gandalf");
		var eomer = scn.GetFreepsCard("eomer");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(gandalf, eomer);
		scn.MoveCardsToHand(beacons);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("runner3"), scn.GetShadowCard("runner4"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(7, scn.GetStrength(eomer));
		scn.FreepsPlayCard(beacons);
		// Éomer is the only unbound companion who is not [gandalf]: [rohan] is taken unasked.
		assertEquals(10, scn.GetStrength(eomer));

		scn.ShadowPass();
		// Four minions against three companions: Éomer plays Théoden, a [rohan] companion, from the draw deck.
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(theoden);

		assertInZone(Zone.FREE_CHARACTERS, theoden);
		// The event made the [rohan] companions in play when it was played strength +3, and no others.
		assertEquals(7, scn.GetStrength(theoden));
		assertEquals(10, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
