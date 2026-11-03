package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "5_77");
					put("strength2", "5_77");

					// Southron Soldier: [raider] Southron Man, cost 2, "Southron. Ambush (2)" and nothing else
					put("soldier", "4_254");
					// Fearsome Dunlending: [men] Man, ambush (1), no Southron; its when-played offer is at a plains site only
					put("dunlending", "11_79");
					// Desert Nomad: [raider] Southron Man with no ambush; damage +1 at 2 threats only
					put("nomad", "7_132");
					// Horror of Harad: unique [orc] Half-troll with ambush (1), damage +1, fierce, lurker, muster and toil 2
					put("horror", "14_13");
					// Easterling Axeman: [raider] Man, no Southron and no ambush; more only at 2 burdens
					put("axeman", "4_224");
					// Bladetusk Rearguard: [raider] artifact; "Maneuver: Remove (5) to make this artifact a fierce
					// mounted Southron minion until the end of the turn ..." (no Man)
					put("rearguard", "103_43");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthinNumbersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Strength in Numbers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Southron. Plays to your support area.<br><b>Maneuver:</b> Discard this condition to add (1) for each Man with ambush you spot.
		*/

		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");

		assertEquals("Strength in Numbers", strength.getBlueprint().getTitle());
		assertNull(strength.getBlueprint().getSubtitle());
		assertFalse(strength.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, strength.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, strength.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, strength.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(strength, Keyword.SUPPORT_AREA));
		assertEquals(0, strength.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnceASouthronIsInPlayAndNotWhileOnlyAnEasterlingIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(strength, soldier);

		scn.StartGame();
		assertInZone(Zone.HAND, strength);
		assertInZone(Zone.HAND, soldier);
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Only the Easterling is in play; the Soldier in hand is not spotted.
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strength));

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		assertTrue(scn.ShadowPlayAvailable(strength));
		scn.ShadowPlayCard(strength);
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASouthronWhoIsNoManLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var rearguard = scn.GetShadowCard("rearguard");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(strength);
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		assertInZone(Zone.HAND, strength);
		// The Axeman, no Southron, is there so that the game has a maneuver phase.
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		scn.ShadowUseCardAction(rearguard);
		// A Southron minion until the end of the turn, and no Man.
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		// A second move, and so a second Shadow phase in the same turn, with the Rearguard still a minion.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.ShadowPlayAvailable(strength));
		scn.ShadowPlayCard(strength);
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardingItAddsOneForEachManWithAmbushSpottedAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var strength2 = scn.GetShadowCard("strength2");
		var soldier = scn.GetShadowCard("soldier");
		var dunlending = scn.GetShadowCard("dunlending");
		var nomad = scn.GetShadowCard("nomad");
		var horror = scn.GetShadowCard("horror");
		scn.MoveCardsToSupportArea(strength, strength2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, strength);
		assertInZone(Zone.SUPPORT, strength2);
		scn.MoveMinionsToTable(soldier, dunlending, nomad, horror);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, strength);
		assertInZone(Zone.SUPPORT, strength2);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(strength));
		scn.ShadowUseCardAction(strength);

		// The cost: this copy, and not the other.
		assertInZone(Zone.DISCARD, strength);
		assertInZone(Zone.SUPPORT, strength2);
		// The Soldier and the Dunlending are Men with ambush, the Soldier's of (2) counting once; the Nomad is a
		// Man with no ambush and the Horror has ambush and is no Man.  The number comes filled in with all of them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMaySpotFewerThanThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var soldier = scn.GetShadowCard("soldier");
		var dunlending = scn.GetShadowCard("dunlending");
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, strength);
		scn.MoveMinionsToTable(soldier, dunlending);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, strength);
		assertEquals(0, scn.GetTwilight());
		scn.ShadowUseCardAction(strength);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertInZone(Zone.DISCARD, strength);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoManWithAmbushItIsOfferedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var nomad = scn.GetShadowCard("nomad");
		var horror = scn.GetShadowCard("horror");
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, strength);
		scn.MoveMinionsToTable(nomad, horror);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, strength);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(strength));
		scn.ShadowUseCardAction(strength);

		// Paid for nothing: the condition is gone and no twilight is added.
		assertInZone(Zone.DISCARD, strength);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedInTheShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, strength);
		scn.MoveMinionsToTable(soldier);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, strength);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(strength));
	}
}
