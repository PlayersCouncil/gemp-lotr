package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength1", "103_59");
					put("strength2", "103_59");
					put("explorer1", "4_250");  // Southron Explorer: [raider] Man, strength 5; a regroup ability never used here
					put("explorer2", "4_250");
					put("soldier", "4_254");    // Southron Soldier: [raider] Man, ambush (2), no other text
					put("attacker", "7_141");   // Easterling Attacker: [raider] Man; a skirmish ability never used here
					put("sentry", "4_253");     // Southron Sentry: [raider] Man. Maneuver: exert this minion to add (2) for each site you control
					put("harad1", "4_238");     // Men of Harad: [raider] condition, support area; a Shadow ability that needs a controlled site
					put("harad2", "4_238");
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession, support area; Shadow and regroup abilities never used here
					put("armory", "1_173");     // Goblin Armory: [moria] condition, support area; answers [moria] weapons and wounds to [moria] Orcs
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text at all
					// Goblin Runner: cards to hold and to draw, which cannot be played in the maneuver phase
					put("fodder1", "1_178");
					put("fodder2", "1_178");
					put("fodder3", "1_178");
					put("fodder4", "1_178");
					put("fodder5", "1_178");
					put("fodder6", "1_178");
					put("fodder7", "1_178");
					put("fodder8", "1_178");
					put("fodder9", "1_178");
					put("fodder10", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Strength of Men
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot 2 [raider] cards to restore 2 [raider] cards (or reconcile your hand
		 *  if you can spot 5 burdens or 6 [raider] cards).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strength1");

		assertEquals("Strength of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RestoresTwoHinderedRaiderCardsOfThePlayersChoiceAtFourBurdensAndFiveRaiderCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength1");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var soldier = scn.GetShadowCard("soldier");
		var attacker = scn.GetShadowCard("attacker");
		var sentry = scn.GetShadowCard("sentry");
		var harad1 = scn.GetShadowCard("harad1");
		var harad2 = scn.GetShadowCard("harad2");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(strength);
		scn.MoveCardsToShadowHand("fodder1", "fodder2");

		scn.StartGame();
		// Five [raider] cards to spot: four minions and a condition.
		scn.MoveMinionsToTable(explorer1, soldier, attacker, sentry, explorer2, troop);
		scn.MoveCardsToSupportArea(harad1, harad2, ships, armory);
		// Hindered: a [raider] minion, condition and possession, and a [moria] condition. With the Troop,
		// that is 6 unhindered Shadow cards and 8 [raider] cards in play, of which only 5 can be spotted.
		scn.HinderCard(explorer2, harad2, ships, armory);
		scn.AddBurdens(4);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.IsHindered(harad2));
		assertTrue(scn.IsHindered(ships));
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.ShadowPlayAvailable(strength));

		scn.ShadowPlayCard(strength);

		// Every hindered [raider] card, of whatever type; not the hindered [moria] card, and not a [raider]
		// card that is not hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer2, harad2, ships));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, explorer1, harad1, troop));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(explorer2, harad2);

		assertFalse(scn.IsHindered(explorer2));
		assertFalse(scn.IsHindered(harad2));
		assertTrue(scn.IsHindered(ships));
		assertTrue(scn.IsHindered(armory));
		// The hand is not reconciled: only the event has left it and nothing is drawn.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, strength);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilTwoRaiderCardsCanBeSpottedEvenWithFiveBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var sentry = scn.GetShadowCard("sentry");
		var harad1 = scn.GetShadowCard("harad1");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(strength);

		scn.StartGame();
		// One [raider] card to spot, beside two hindered [raider] cards and two [moria] cards.
		scn.MoveMinionsToTable(sentry, explorer2, troop);
		scn.MoveCardsToSupportArea(harad1, armory);
		scn.HinderCard(explorer2, harad1);
		scn.AddBurdens(5);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strength));

		// A second [raider] card to spot, a condition. The Sentry's own action (which adds nothing with no
		// site controlled) brings the next decision.
		scn.RestoreCard(harad1);
		assertEquals(0, scn.GetWoundsOn(sentry));
		scn.ShadowUseCardAction(sentry);
		assertEquals(1, scn.GetWoundsOn(sentry));
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(strength));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFiveBurdensTheHandIsReconciledInPlaceOfRestoringTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength1");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var harad1 = scn.GetShadowCard("harad1");
		var harad2 = scn.GetShadowCard("harad2");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		scn.MoveCardsToHand(strength, fodder1, fodder2);
		scn.MoveCardsToFreepsHand("fodder1", "fodder2");

		scn.StartGame();
		// Two [raider] cards to spot, far short of 6.
		scn.MoveMinionsToTable(explorer1, explorer2, troop);
		scn.MoveCardsToSupportArea(harad1, harad2, ships, armory);
		scn.HinderCard(explorer2, harad2, ships, armory);
		scn.AddBurdens(5);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.IsHindered(harad2));
		assertTrue(scn.IsHindered(ships));
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.ShadowPlayAvailable(strength));

		scn.ShadowPlayCard(strength);

		// No two cards are asked for: the hand is reconciled, which begins by restoring every hindered card
		// this player owns, of whatever culture, and then asks for the discard a player may make.
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(explorer2));
		assertFalse(scn.IsHindered(harad2));
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(armory));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(fodder1, fodder2));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(fodder1);

		assertInZone(Zone.DISCARD, fodder1);
		// 1 card held + 7 drawn
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(deck - 7, scn.GetShadowDeckCount());
		assertInZone(Zone.HAND, fodder2);
		// It is the hand of the player who played the event.
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, strength);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSixRaiderCardsTheHandIsReconciledInPlaceOfRestoringTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength1");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var soldier = scn.GetShadowCard("soldier");
		var attacker = scn.GetShadowCard("attacker");
		var sentry = scn.GetShadowCard("sentry");
		var harad1 = scn.GetShadowCard("harad1");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder3 = scn.GetShadowCard("fodder3");
		scn.MoveCardsToHand(strength);
		scn.MoveCardsToShadowHand("fodder1", "fodder2", "fodder3", "fodder4", "fodder5",
				"fodder6", "fodder7", "fodder8", "fodder9", "fodder10");

		scn.StartGame();
		// Exactly six [raider] cards to spot: four minions, a condition and a possession.
		scn.MoveMinionsToTable(explorer1, soldier, attacker, sentry, explorer2, troop);
		scn.MoveCardsToSupportArea(harad1, ships, armory);
		scn.HinderCard(explorer2, armory);
		scn.AddBurdens(4);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(11, scn.GetShadowHandCount());
		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.ShadowPlayAvailable(strength));

		scn.ShadowPlayCard(strength);

		// The hand is reconciled: the hindered cards this player owns are restored, the [moria] one too.
		assertFalse(scn.IsHindered(explorer2));
		assertFalse(scn.IsHindered(armory));
		// Ten cards held: two have to go, and nothing is drawn.
		assertEquals(10, scn.GetShadowHandCount());
		assertTrue(scn.ShadowDecisionAvailable("discard down"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(fodder1, fodder2, fodder3));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(fodder1, fodder2);

		assertInZone(Zone.DISCARD, fodder1, fodder2);
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, strength);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void RestoresTheOnlyHinderedRaiderCardAndCanBePlayedWithNoneHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength1 = scn.GetShadowCard("strength1");
		var strength2 = scn.GetShadowCard("strength2");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var harad1 = scn.GetShadowCard("harad1");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(strength1, strength2);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer1, explorer2);
		scn.MoveCardsToSupportArea(harad1, armory);
		scn.HinderCard(explorer2, armory);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(explorer2));
		assertTrue(scn.IsHindered(armory));

		scn.ShadowPlayCard(strength1);

		// One hindered [raider] card: it is restored without a choice, and the [moria] card is not.
		assertFalse(scn.IsHindered(explorer2));
		assertTrue(scn.IsHindered(armory));
		assertInZone(Zone.DISCARD, strength1);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();

		// No hindered [raider] card is left: the event may still be played, and does nothing.
		assertTrue(scn.ShadowPlayAvailable(strength2));
		scn.ShadowPlayCard(strength2);

		assertTrue(scn.IsHindered(armory));
		assertInZone(Zone.DISCARD, strength2);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
