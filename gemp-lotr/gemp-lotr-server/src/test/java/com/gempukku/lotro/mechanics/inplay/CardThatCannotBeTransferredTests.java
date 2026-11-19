package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card that cannot be transferred is not among the cards a card's effect offers to transfer.
 *
 * Comprehensive Rules 4.2, "transfer": "If an effect can transfer a card to another 'eligible bearer,' you must
 * obey that card's requirements on both what may bear it and when it may be transferred."  Warrior of Rohan
 * 17_104 is the card that says a card may not be: "Possessions cannot be transferred from this companion to
 * another character."
 *
 * The effect never moved such a card, but its choice of the card to transfer did not ask: a possession the Warrior
 * bears was offered, its new bearer was asked for, and nothing moved, with the cost of the action paid for nothing
 * (F-0340; Ketura, 2026-10-10: "good to fix").  The choice now asks what the effect asks.  A possession may still
 * be transferred TO him; and an action with a cost is offered though its effect will do nothing (Q-0034), so with
 * no other card to transfer the cost is paid and nobody is asked anything.
 *
 * Shown with Strange-looking Men 11_100 ("Maneuver: Exert this minion to transfer a possession or condition borne
 * by a character to another eligible bearer"), whose choice is of the card and then of its bearer, and with
 * Gamling, Dutiful Marshal 18_99 ("Skirmish: Transfer a [rohan] possession that Gamling can bear to him"), whose
 * bearer is named and not chosen.
 */
public class CardThatCannotBeTransferredTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Strange-looking Men: [men] Man, strength 10, vitality 2; the maneuver ability is its whole text
					put("men1", "11_100");
					put("men2", "11_100");
					// Warrior of Rohan: [rohan] Man, strength 7, vitality 3.  "Possessions cannot be transferred
					// from this companion to another character."  His fellowship ability is never used.
					put("warrior", "17_104");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; a maneuver ability that is never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf; a skirmish ability that is never used
					put("guard", "5_83");       // Household Guard: [rohan] Man; Valiant is its whole text
					// Gamling, Dutiful Marshal: [rohan] Man, strength 6, vitality 3, valiant.  "Skirmish: Transfer
					// a [rohan] possession that Gamling can bear to him."
					put("gamling", "18_99");
					// Elven Cloak: a possession (cloak).  "Bearer must be a companion."  The minion archery total
					// is -1, and nobody here is an archer.
					put("cloak1", "1_42");
					put("cloak2", "1_42");
					put("armor", "1_92");       // Armor: a possession (armor).  "Bearer must be a Man."
					// Rohirrim Shield: a possession (shield).  "Bearer must be a [rohan] Man."  Its regroup ability
					// is never used.
					put("shield1", "5_91");
					put("shield2", "5_91");
					put("spear", "4_288");      // Rider's Spear: a possession (hand weapon), strength +2.  "Bearer must be a [rohan] Man."
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void APossessionThatCannotBeTransferredIsNotAmongTheCardsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var warrior = scn.GetFreepsCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var warriorsCloak = scn.GetFreepsCard("cloak1");
		var gimlisCloak = scn.GetFreepsCard("cloak2");
		var armor = scn.GetFreepsCard("armor");
		var men = scn.GetShadowCard("men1");
		scn.MoveCompanionsToTable(warrior, aragorn, gimli);
		scn.AttachCardsTo(warrior, warriorsCloak);
		scn.AttachCardsTo(gimli, gimlisCloak);
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(men);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(men));
		scn.ShadowUseCardAction(men);

		assertEquals(1, scn.GetWoundsOn(men));
		// Aragorn's Armor could go to another Man, and Gimli's Cloak to Frodo or Aragorn.  The Warrior's Cloak could
		// go to Frodo or Aragorn as well, if it could be transferred from him.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, gimlisCloak));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warriorsCloak));
		scn.ShadowChooseCard(gimlisCloak);

		// One cloak to a character: the Warrior has his own, and Gimli bears this one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warrior, gimli));
		scn.ShadowChooseCard(aragorn);

		assertAttachedTo(gimlisCloak, aragorn);
		assertAttachedTo(warriorsCloak, warrior);
		assertAttachedTo(armor, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoOtherCardToTransferTheCostIsPaidAndNobodyIsAskedAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak1");
		var men = scn.GetShadowCard("men1");
		scn.MoveCompanionsToTable(warrior, gimli);
		scn.AttachCardsTo(warrior, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(men);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Frodo and Gimli are companions with no cloak.  The ability has a cost, so it is offered whatever its
		// effect will do (Q-0034).
		assertTrue(scn.ShadowActionAvailable(men));
		scn.ShadowUseCardAction(men);

		// No card is taken, so no bearer is asked for.
		assertEquals(1, scn.GetWoundsOn(men));
		assertAttachedTo(cloak, warrior);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void APossessionMayBeTransferredToTheBearerItCannotThenLeave() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var men1 = scn.GetShadowCard("men1");
		var men2 = scn.GetShadowCard("men2");
		scn.MoveCompanionsToTable(warrior, aragorn);
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(men1, men2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Armor is the one possession in play, and is not asked for.  "Bearer must be a Man" names no side
		// (Q-0058): the Men who move it are Men themselves.
		scn.ShadowUseCardAction(men1);

		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, men1, men2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(warrior);

		assertEquals(1, scn.GetWoundsOn(men1));
		assertAttachedTo(armor, warrior);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();

		// Aragorn and the Men have no armor, and the Armor cannot be transferred from the Warrior.
		assertTrue(scn.ShadowActionAvailable(men2));
		scn.ShadowUseCardAction(men2);

		assertEquals(1, scn.GetWoundsOn(men2));
		assertAttachedTo(armor, warrior);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ATransferToABearerTheTextNamesDoesNotOfferItEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var gamling = scn.GetFreepsCard("gamling");
		var warriorsShield = scn.GetFreepsCard("shield1");
		var guardsShield = scn.GetFreepsCard("shield2");
		var spear = scn.GetFreepsCard("spear");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(warrior, guard, gamling);
		scn.AttachCardsTo(warrior, warriorsShield);
		scn.AttachCardsTo(guard, guardsShield, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gamling, savage);

		assertEquals(6, scn.GetStrength(gamling));
		assertTrue(scn.FreepsActionAvailable(gamling));
		scn.FreepsUseCardAction(gamling);

		// Gamling is a [rohan] Man with no shield and no hand weapon: he could bear any of the three.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardsShield, spear));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(warriorsShield));
		scn.FreepsChooseCard(spear);

		assertAttachedTo(spear, gamling);
		assertAttachedTo(guardsShield, guard);
		assertAttachedTo(warriorsShield, warrior);
		assertEquals(8, scn.GetStrength(gamling));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
