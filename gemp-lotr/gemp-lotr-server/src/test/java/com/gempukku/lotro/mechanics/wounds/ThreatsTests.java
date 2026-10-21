package com.gempukku.lotro.mechanics.wounds;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Threats.
 *
 * Comprehensive Rules 4.2, "threats": "A threat is a token placed on the dead pile by the Free Peoples player. ...
 * However, threats cannot be added if the number of threats on the dead pile is equal to or greater than the
 * number of companions in play."  Its example: "If the dead pile has four threats and there are four companions
 * in play, no more threats may be added.  Sometimes a companion will be discarded, and there will be more threats
 * than companions in play."
 *
 * "When a companion or ally is killed and that card is placed in the dead pile, the Free Peoples player counts the
 * number of threats on the dead pile and then removes them.  Then the Free Peoples player must assign a number of
 * wounds equal to the number of threats removed to his companions in any way he wishes."  Its example: "If there
 * are three threats on the dead pile when an ally is killed, the Free Peoples player must remove those tokens and
 * assign three wounds to his companions (not allies).  This may be three wounds to one companion, one wound each
 * to three different companions, or any other possible combination."
 *
 * "Killed": "Cards that trigger when a character is killed will not trigger when a character is discarded", and a
 * killed minion goes to the discard pile, not the dead pile: neither sets the threats off.  "Dead pile": "When game
 * text instructs you to place a character in the dead pile, that character has been killed", and that does.
 *
 * That a hindered companion still counts toward the limit is in HinderedCardStillRestrictsPlayTests.
 */
public class ThreatsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					// Aragorn, Thorongil: strength 8, vitality 4.  "Maneuver: Add a threat to exert a non-
					// hunter minion with strength less than Aragorn."  Never on a table beside the other Aragorn.
					put("thorongil", "15_55");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; his text is about being played
					// Elrond, Lord of Rivendell: ally, vitality 4.  He heals allies of site 3 at the start of
					// the turn, and has a fellowship ability, not used here.
					put("elrond", "1_40");
					put("bounder", "1_286");    // Bounder: ally, vitality 2; a skirmish ability, not used here
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4; a fellowship ability, not used here
					// Saved From the Fire: [gandalf] Fellowship event, cost 3.  "Spot Gandalf and place a
					// companion (except the Ring-bearer) in the dead pile to take up to 3 cards from that
					// companion's culture into hand from your draw deck.  Shuffle your draw deck."
					put("saved", "8_20");

					// War Towers: [raider] Maneuver event, cost 1.  "Spot a [raider] minion to add a threat.
					// Add an additional threat for each companion over 4."
					put("towers1", "7_173");
					put("towers2", "7_173");
					// Easterling Axeman: [raider] Man, strength 6, vitality 2; its text needs 2 burdens.
					put("axeman1", "4_224");
					put("axeman2", "4_224");
					// Fierce in Despair: [raider] Regroup event, cost 7.  "Spot 2 [raider] Men to discard a
					// companion (except the Ring-bearer)."
					put("despair", "7_148");
					// Dark Tidings: [raider] support-area condition.  "Regroup: Spot 2 [raider] Men and
					// remove (3) to add a threat (limit 1)."
					put("tidings", "7_130");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
					// Úlairë Lemenya: Nazgûl, strength 9, vitality 2.  "Each companion or ally who bears a [wraith]
					// condition is strength -2": nobody bears one here.
					put("lemenya", "1_232");
					// Drawn to Its Power: [wraith] support-area condition.  "Each time a companion is killed in a
					// skirmish involving a Nazgûl, add a burden": a required trigger on the same kill as the threats.
					put("drawn", "1_211");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WithAsManyThreatsAsCompanionsNoMoreCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorongil = scn.GetFreepsCard("thorongil");
		// With the Ring-bearer, four companions
		scn.MoveCompanionsToTable(thorongil, scn.GetFreepsCard("boromir"), scn.GetFreepsCard("guard"));

		var axeman = scn.GetShadowCard("axeman1");
		var towers1 = scn.GetShadowCard("towers1");
		var towers2 = scn.GetShadowCard("towers2");
		scn.MoveCardsToHand(towers1, towers2);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.AddThreats(3);
		scn.SkipToPhase(Phase.MANEUVER);

		// Three threats and four companions: there is room for one, as a cost or as an effect.
		assertEquals(4, scn.GetThreatLimit());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(thorongil));

		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(towers1);
		assertEquals(4, scn.GetThreats());
		assertEquals(twilight - 1, scn.GetTwilight());

		// The rules' own example: "If the dead pile has four threats and there are four companions in play, no
		// more threats may be added."  A cost of a threat cannot be paid ...
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thorongil));

		// ... and an effect that adds one adds none.  The event is played and paid for all the same.
		scn.FreepsPass();
		scn.ShadowPlayCard(towers2);
		assertInZone(Zone.DISCARD, towers2);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(4, scn.GetThreats());
		assertEquals(4, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionDiscardedLeavesMoreThreatsThanCompanionsAndSetsOffNoThreatWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		// With the Ring-bearer, three companions
		scn.MoveCompanionsToTable(boromir, guard);

		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		var despair = scn.GetShadowCard("despair");
		var tidings = scn.GetShadowCard("tidings");
		scn.MoveCardsToSupportArea(tidings);
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.AddThreats(3);
		// Nobody is assigned to the two Men, who are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		// 7 for Fierce in Despair + 3 for Dark Tidings
		scn.SetTwilight(10);

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(3, scn.GetThreats());

		scn.FreepsPass();
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(guard);

		// The Guard is discarded, not killed: he is in the discard pile, the threats stay where they are, and
		// the Free Peoples player is asked for no wound.  "There will be more threats than companions in play."
		assertInZone(Zone.DISCARD, guard);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(3, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// "Equal to or greater than": above the number of companions, as at it, none is added.  The (3) is
		// removed for nothing.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(tidings));
		scn.ShadowUseCardAction(tidings);
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenACompanionIsKilledTheThreatsAreRemovedAndThatManyWoundsArePlacedOnCompanionsAsTheFreePeoplesPlayerWishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		// With the Ring-bearer, four companions
		scn.MoveCompanionsToTable(aragorn, boromir, guard);
		scn.MoveAlliesToTable(elrond);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.AddThreats(3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{guard, troop});

		// Aragorn, 8, beats the Runner, 5, and kills it.  A minion killed is no companion or ally, and goes to
		// the discard pile: the threats stay and nothing is asked but the next skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The Guard, 4, is overwhelmed by the Troop, 13, and killed.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);

		// The threats are counted and removed, and then a wound is asked for each of the three: any companion,
		// and not the ally.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// "In any way he wishes": the companion just wounded may take the next one too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		scn.FreepsChooseCard(boromir);

		// Two and one: three wounds for three threats, and no fourth.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenAnAllyIsKilledTheWoundsGoToCompanionsAndMayAllGoToOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var bounder = scn.GetFreepsCard("bounder");
		// With the Ring-bearer, three companions
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond, bounder);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		// The rules' own example: three threats on the dead pile when an ally is killed.  Elrond, vitality 4,
		// has one wound left to take.
		scn.AddThreats(3);
		scn.AddWoundsToChar(elrond, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetWoundsOn(elrond));

		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, bounder));
		scn.ShadowChooseCard(elrond);
		assertInZone(Zone.DEAD, elrond);

		// "Assign three wounds to his companions (not allies)."
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());

		// "This may be three wounds to one companion."  Aragorn, vitality 4, lives through them.
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		scn.FreepsChooseCard(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionPlacedInTheDeadPileByACardsTextIsKilledAndTheWoundsMayGoOneToEachCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var saved = scn.GetFreepsCard("saved");
		// With the Ring-bearer, four companions
		scn.MoveCompanionsToTable(gandalf, aragorn, guard);
		scn.MoveCardsToHand(saved);

		scn.StartGame();
		scn.AddThreats(3);

		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());

		// The Guard is the only [dwarven] card of this deck, so the event will find nothing to take.
		scn.FreepsPlayCard(saved);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetWoundsOn(guard));

		// Placed there by a card and by no wound, he has been killed all the same: the threats are removed
		// and three wounds are to be placed.  "One wound each to three different companions."
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		scn.FreepsChooseCard(gandalf);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());

		// Then the event goes on: the draw deck is shown, with no [dwarven] card in it to take.
		scn.FreepsDismissRevealedCards();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionKilledWithNoThreatsOnTheDeadPilePutsNothingOfTheThreatsToTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var lemenya = scn.GetShadowCard("lemenya");
		var drawn = scn.GetShadowCard("drawn");
		scn.MoveCardsToSupportArea(drawn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lemenya);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());

		// The Guard, 4, is overwhelmed by Lemenya, 9, and killed.  With no threat on the dead pile there is no
		// threat to remove and no wound to place: the rule puts nothing to the Free Peoples player, so Drawn to
		// Its Power's trigger, the one thing left that answers the kill, is carried out with nothing to order.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.FreepsDecisionAvailable("Required responses"));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledWithThreatsOnTheDeadPileListsTheThreatWoundsByNameAmongTheRequiredResponses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var lemenya = scn.GetShadowCard("lemenya");
		var drawn = scn.GetShadowCard("drawn");
		scn.MoveCardsToSupportArea(drawn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lemenya);

		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);

		// Two things answer the kill: the threats, and Drawn to Its Power.  The Free Peoples player orders them,
		// and the rule's entry says what it does.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		var entries = scn.FreepsGetAvailableActions();
		assertEquals(2, entries.size());
		assertTrue(entries.contains("Threat wounds: remove every threat, then wound a companion for each"));
		assertFalse(entries.contains(null));
		assertFalse(entries.contains("null"));

		// Taken first, it removes the threats and asks for the two wounds before the burden is added.
		scn.FreepsResolveActionOrder("Threat wounds");
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
