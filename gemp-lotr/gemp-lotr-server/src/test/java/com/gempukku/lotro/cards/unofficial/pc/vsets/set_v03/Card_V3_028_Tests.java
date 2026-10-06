package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("amondin", "103_28");
					// War-beacon: a [gondor] beacon, cost 1, to play spot an unbound Man; fellowship abilities never used here
					put("war1", "103_35");
					put("war2", "103_35");
					put("war3", "103_35");
					put("calenhad", "103_86");  // Northern Signal-fire, Flame of Calenhad: a [rohan] beacon; a maneuver ability never used here
					put("catapult", "8_32");    // Catapult: a [gondor] possession in the support area that is no beacon; a maneuver ability never used here
					// Gondor Calls for Aid!: a [rohan] condition that is a beacon and cannot be hindered. It also gives
					// [gondor] Men the [rohan] culture and the other way round, which nothing here turns on.
					put("gcfa", "103_83");

					put("strider", "11_54");    // Aragorn, Strider: [gondor] Man costing 1, strength 8, vitality 4; adds (1) when the fellowship moves
					put("knight", "8_39");      // Knight of Dol Amroth: [gondor] Man costing 2, vitality 3; his text needs him to win a skirmish
					put("eowyn", "5_122");      // Eowyn, Daughter of Eomund: [rohan] Man costing 2, vitality 3; a maneuver ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man costing 3, vitality 3; a skirmish ability never used here
					// Eomer, Third Marshal of Riddermark: [rohan] Man with a printed cost of 3, vitality 3. "While you
					// can spot a [rohan] Man, Eomer's twilight cost is -1."
					put("eomer", "4_267");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man costing 4, vitality 4; a maneuver ability never used here
					put("ranger", "4_122");     // Gondorian Ranger: a Ring-bound [gondor] Man costing 2, vitality 3; his text is about minions assigned
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an unbound Elf costing 2, vitality 3; stronger at sites 6 to 8
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer, so that the fellowship has an archery wound to place
					put("barliman", "1_70");    // Barliman Butterbur: a Man who is an ally, costing 0, vitality 2; a fellowship ability never used here

					put("marksman1", "1_176");  // Goblin Marksman: an archer and nothing else, one archery wound each
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("marksman4", "1_176");
					put("marksman5", "1_176");
					put("picket", "6_101");     // Gate Picket: maneuver, exerts twice to wound each companion and ally of a culture the Free Peoples player does not name
					put("grima", "4_154");      // Grima, Wormtongue: a Man who is a minion, costing 2, vitality 3; his maneuver ability needs a companion bearing 3 cards
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					// Journey Into Danger: a Shadow condition. "While you can spot 5 companions, each companion's
					// twilight cost is +2."
					put("journey", "1_253");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Amon Din
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		 * 	Response: If an unbound Man costing X is about to take a wound, spot X beacons to prevent that wound. Hinder a beacon.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("amondin");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Amon Din", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoBeaconsCanBeHinderedAndThenCostsTwoAndHindersBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(war1, calenhad, catapult);
		// A hindered beacon is no beacon while it is hindered, and the Catapult never was one.
		scn.HinderCard(calenhad);
		scn.MoveCardsToHand(amondin, war2);

		scn.StartGame();

		// One beacon that can be hindered: one short.
		assertFalse(scn.FreepsPlayAvailable(amondin));

		scn.FreepsPlayCard(war2);

		// 1 for the War-beacon
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(amondin));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war2));

		scn.FreepsPlayCard(amondin);

		// 1 + 2 for the Signal-fire. With exactly two beacons to hinder nothing is asked.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, amondin);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(catapult));
		assertFalse(scn.IsHindered(amondin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheTwoBeaconsHinderedToPlayAreChosenFromUnhinderedBeaconsOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCardsToSupportArea(war1, war2, war3, calenhad, catapult);
		scn.HinderCard(war3);
		scn.MoveCardsToHand(amondin);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(amondin);

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Two [gondor] beacons and a [rohan] one
		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, calenhad));
		// A beacon already hindered, a [gondor] support possession that is no beacon, and the card being played
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war3, catapult, amondin));

		scn.FreepsChooseCards(war1, calenhad);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, amondin);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(war2));
		assertFalse(scn.IsHindered(catapult));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponsePreventsAWoundToAnUnboundManWhoseCostTheBeaconsCoverAndThenHindersABeacon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		var knight = scn.GetFreepsCard("knight");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(knight, eowyn);
		// Three beacons to spot, a fourth that is hindered, and a possession that is no beacon.
		scn.MoveCardsToSupportArea(amondin, war1, war2, calenhad, catapult);
		scn.HinderCard(war2);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3", "marksman4");
		scn.SkipToArcheryWounds();

		// First archery wound, to a [gondor] Man costing 2: offered, and declined.
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(calenhad));

		// Second, to a [rohan] Man costing 2: offered, and used.
		assertEquals(0, scn.GetWoundsOn(eowyn));
		scn.FreepsChooseCard(eowyn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(amondin, war1, calenhad));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war2, catapult));
		scn.FreepsChooseCard(calenhad);

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(war1));

		// Third, to the Knight again: two beacons are left for his cost of 2, exactly enough.
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(amondin, war1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(calenhad, war2, catapult));
		scn.FreepsChooseCard(war1);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(war1));

		// Fourth, to the Knight: one beacon beside three hindered ones is one short, and the wound lands unasked.
		scn.FreepsChooseCard(knight);

		assertEquals(2, scn.GetWoundsOn(knight));
		assertFalse(scn.IsHindered(amondin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BeaconsNeededAreTheMansOwnCostAtFourThreeAndTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var calenhad = scn.GetFreepsCard("calenhad");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		// Four beacons to spot and a fifth that is hindered.
		scn.MoveCardsToSupportArea(amondin, war1, war2, war3, calenhad);
		scn.HinderCard(war3);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3", "marksman4", "marksman5");
		scn.SkipToArcheryWounds();

		// Aragorn costs 4 and there are four beacons.
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);
		scn.FreepsChooseCard(calenhad);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// One beacon is hindered for it, not four.
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war2));

		// Three beacons are one short of 4.
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Boromir costs 3 and there are three beacons.
		scn.FreepsChooseCard(boromir);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);
		scn.FreepsChooseCard(war2);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.IsHindered(war2));

		// Two beacons are one short of 3.
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// The Knight costs 2 and there are two beacons. The Signal-fire itself is the beacon hindered, and the
		// wound is prevented all the same.
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(amondin, war1));
		scn.FreepsChooseCard(amondin);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(war1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AManWhoseOwnTextLowersHisCostIsCountedAtTheCostPrintedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var calenhad = scn.GetFreepsCard("calenhad");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToSupportArea(amondin, war1, calenhad);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");
		scn.SkipToArcheryWounds();

		// Eomer has 3 printed on him and there are three beacons.
		assertEquals(0, scn.GetWoundsOn(eomer));
		scn.FreepsChooseCard(eomer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);
		scn.FreepsChooseCard(calenhad);
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.IsHindered(calenhad));

		// Two beacons are one short of 3. His own text makes his twilight cost 1 less while a [rohan] Man can
		// be spotted, as he himself can; the cost counted is the one printed, and the wound lands unasked.
		scn.FreepsChooseCard(eomer);

		assertEquals(1, scn.GetWoundsOn(eomer));
		assertFalse(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(war1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACostRaisedByAnotherCardLeavesTheBeaconsNeededAtTheCostPrinted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var knight = scn.GetFreepsCard("knight");
		var eowyn = scn.GetFreepsCard("eowyn");
		var boromir = scn.GetFreepsCard("boromir");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var journey = scn.GetShadowCard("journey");
		// Frodo and four more: five companions to spot.
		scn.MoveCompanionsToTable(knight, eowyn, boromir, haldir);
		scn.MoveCardsToSupportArea(amondin, war1);
		scn.MoveCardsToSupportArea(journey);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		// Journey Into Danger is in force: a companion coming into play costs 2 more.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(aragorn);
		// 4 for Aragorn + 2
		assertEquals(6, scn.GetTwilight());

		scn.MoveMinionsToTable("marksman1");
		scn.SkipToArcheryWounds();

		// The Knight is in play already, where another card's change to his cost does not reach him: two
		// beacons are enough for the 2 printed on him, as they would not be for 4.
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);
		scn.FreepsChooseCard(war1);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(amondin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ABeaconThatCannotBeHinderedIsSpottedAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var gcfa = scn.GetFreepsCard("gcfa");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(amondin, gcfa);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1");
		scn.SkipToArcheryWounds();

		// The Knight costs 2. The Signal-fire and a condition that is a beacon are the two to spot.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(gcfa));
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);

		// Nothing is asked: of the two only the Signal-fire can be hindered.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(gcfa));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OfEveryoneWoundedByOneEffectOnlyTheUnboundManIsOfferedTheResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var calenhad = scn.GetFreepsCard("calenhad");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var haldir = scn.GetFreepsCard("haldir");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		var picket = scn.GetShadowCard("picket");
		scn.MoveCompanionsToTable(knight, ranger, haldir);
		scn.MoveAlliesToTable(barliman);
		// Three beacons: enough for anyone here, none of whom costs more than 2.
		scn.MoveCardsToSupportArea(amondin, war1, calenhad);

		scn.StartGame();
		scn.MoveMinionsToTable(picket);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(picket);
		// No culture is named, so every companion and ally is wounded.
		scn.FreepsChooseCards();

		// One offer, for the Knight.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);
		scn.FreepsChooseCard(war1);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.IsHindered(war1));
		// A Ring-bound Man, an unbound Elf, a Man who is an ally, and the Ring-bearer: wounded, with two
		// beacons still to spot.
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(calenhad));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AManWhoIsAMinionIsNotOfferedTheResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var war1 = scn.GetFreepsCard("war1");
		var calenhad = scn.GetFreepsCard("calenhad");
		var knight = scn.GetFreepsCard("knight");
		var legolas = scn.GetFreepsCard("legolas");
		var grima = scn.GetShadowCard("grima");
		var marksman = scn.GetShadowCard("marksman1");
		scn.MoveCompanionsToTable(knight, legolas);
		scn.MoveCardsToSupportArea(amondin, war1, calenhad);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, marksman);
		scn.SkipToArcheryWounds();

		// The Marksman's wound, to a Man of the fellowship costing 2: offered on this board.
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(knight));

		// Legolas's wound, to a Man of the Shadow's costing 2: it lands unasked.
		assertEquals(0, scn.GetWoundsOn(grima));
		scn.ShadowChooseCard(grima);

		assertEquals(1, scn.GetWoundsOn(grima));
		assertFalse(scn.IsHindered(amondin));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(calenhad));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithItselfTheOnlyBeaconOneUsePreventsOneOfTheTwoWoundsOfALostSkirmishAndHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var amondin = scn.GetFreepsCard("amondin");
		var strider = scn.GetFreepsCard("strider");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(strider);
		scn.MoveCardsToSupportArea(amondin);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(strider, party);
		scn.FreepsPass();
		scn.ShadowPass();

		// Strength 8 loses to 9, and damage +1 makes it two wounds. Strider costs 1 and one beacon is in play.
		assertEquals(0, scn.GetWoundsOn(strider));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(amondin));
		scn.FreepsChooseOptionalTrigger(amondin);

		// The Signal-fire is the only beacon there is to hinder. Hindered, it has nothing to say about the
		// second wound.
		assertTrue(scn.IsHindered(amondin));
		assertEquals(1, scn.GetWoundsOn(strider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
