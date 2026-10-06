package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nardol", "103_30");
					// War-beacon: a [gondor] possession that is a beacon, cost 1, to play spot an unbound Man; fellowship abilities never used here
					put("war1", "103_35");
					put("war2", "103_35");
					put("war3", "103_35");
					put("calenhad", "103_86");   // Northern Signal-fire, Flame of Calenhad: a [rohan] possession that is a beacon; a maneuver ability never used here

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man to bear things; a maneuver ability never used here
					put("legolas", "1_50");      // Legolas, Greenleaf: an archer, for a fellowship archery total of 1; his archery ability is never used here

					put("catapult", "8_32");     // Catapult: a [gondor] possession in the support area that is no beacon; a maneuver ability never used here
					put("walls", "7_111");       // Man the Walls: a [gondor] condition in the support area; its text is about minions in a skirmish
					put("sword", "1_112");       // Ranger's Sword: a [gondor] possession Aragorn bears
					put("arrowslits", "5_80");   // Arrow-slits: a [rohan] condition in the support area; its maneuver ability needs 2 [rohan] Men
					put("horse", "4_283");       // Horse of Rohan: a [rohan] possession a Man bears; its text is about its bearer's skirmish
					put("shards", "3_44");       // The Shards of Narsil: a [gondor] artifact in the support area; fellowship abilities only
					put("alliance", "1_49");     // The Last Alliance of Elves and Men: an [elven] condition a [gondor] Man bears; a strength bonus only
					put("sting", "1_313");       // Sting: a [shire] possession Frodo bears; fellowship and regroup ability only

					// Goblin Patrol Troop: vitality 3, no text
					put("troop1", "1_177");
					put("troop2", "1_177");
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
		 * Name: Northern Signal-fire, Flame of Nardol
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		 * 	Archery: Hinder X beacons and discard X [Gondor] or [Rohan] conditions or possessions to make the fellowship archery total +X.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nardol");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Nardol", card.getBlueprint().getSubtitle());
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

		var nardol = scn.GetFreepsCard("nardol");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(war1, calenhad, catapult);
		// A hindered beacon is no beacon while it is hindered, and the Catapult never was one.
		scn.HinderCard(calenhad);
		scn.MoveCardsToHand(nardol, war2);

		scn.StartGame();

		// One beacon that can be hindered: one short.
		assertFalse(scn.FreepsPlayAvailable(nardol));

		scn.FreepsPlayCard(war2);

		// 1 for the War-beacon
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(nardol));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war2));

		scn.FreepsPlayCard(nardol);

		// 1 + 2 for the Signal-fire. With exactly two beacons to hinder nothing is asked.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, nardol);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(catapult));
		assertFalse(scn.IsHindered(nardol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheTwoBeaconsHinderedToPlayAreChosenFromUnhinderedBeaconsOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCardsToSupportArea(war1, war2, war3, calenhad, catapult);
		scn.HinderCard(war3);
		scn.MoveCardsToHand(nardol);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(nardol);

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Two [gondor] beacons and a [rohan] one
		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, calenhad));
		// A beacon already hindered, a [gondor] support possession that is no beacon, and the card being played
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war3, catapult, nardol));

		scn.FreepsChooseCards(war1, war2);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, nardol);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertFalse(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(catapult));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityHindersTwoBeaconsAndDiscardsTwoGondorOrRohanConditionsOrPossessionsForTwoMoreArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		var walls = scn.GetFreepsCard("walls");
		var sword = scn.GetFreepsCard("sword");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var horse = scn.GetFreepsCard("horse");
		var shards = scn.GetFreepsCard("shards");
		var alliance = scn.GetFreepsCard("alliance");
		var sting = scn.GetFreepsCard("sting");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword, horse, alliance);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(nardol, war1, war2, calenhad, catapult, walls, arrowslits, shards);
		scn.HinderCard(war2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		// The phase before
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(nardol));

		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(nardol));
		// No archers
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(nardol);

		// X is at least 1, and here at most the three beacons there are to hinder.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		// Itself, a [gondor] beacon and a [rohan] one
		assertTrue(scn.FreepsHasCardChoiceAvailable(nardol, war1, calenhad));
		// A beacon already hindered, and a [gondor] possession that is no beacon
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war2, catapult));
		scn.FreepsChooseCards(war1, calenhad);

		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(nardol));

		// X is 2
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// A [gondor] condition and a [rohan] one in the support area, a [gondor] possession and a [rohan] one
		// on a companion, and two [gondor] possessions in the support area, this card among them
		assertTrue(scn.FreepsHasCardChoiceAvailable(walls, arrowslits, sword, horse, catapult, nardol));
		// The three hindered beacons, a [gondor] artifact, an [elven] condition, a [shire] possession, a [gondor] companion
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war1, calenhad, war2, shards, alliance, sting, aragorn));
		scn.FreepsChooseCards(walls, horse);

		assertInDiscard(walls, horse);
		assertInZone(Zone.SUPPORT, nardol, arrowslits, catapult, shards);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(alliance, aragorn);
		assertFalse(scn.IsHindered(nardol));
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Both pass, and the Shadow player has two archery wounds to place.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(troop1));
		scn.ShadowChooseCard(troop1);
		scn.ShadowChooseCard(troop1);

		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		// The archery phase is over, and the total is what it was. In this phase too the ability is not on
		// offer, with this card not hindered and cards still there to discard.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertFalse(scn.IsHindered(nardol));
		assertFalse(scn.FreepsActionAvailable(nardol));
	}

	@Test
	public void WithXOfOneItMayHinderItselfAndAddsOneToAnArchersTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var war1 = scn.GetFreepsCard("war1");
		var walls = scn.GetFreepsCard("walls");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(nardol, war1, walls, arrowslits);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(nardol);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(nardol);

		assertTrue(scn.IsHindered(nardol));
		assertFalse(scn.IsHindered(war1));

		// X is 1. The other beacon, not hindered, is a [gondor] possession still; this card, hindered, is not.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(walls, arrowslits, war1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nardol));
		scn.FreepsChooseCard(arrowslits);

		assertInDiscard(arrowslits);
		assertInZone(Zone.SUPPORT, nardol, war1, walls);
		// 1 for Legolas + 1
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithOneOtherBeaconAndNothingElseOneIsHinderedAndTheOtherIsThePossessionDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var war1 = scn.GetFreepsCard("war1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(nardol, war1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(nardol));

		scn.FreepsUseCardAction(nardol);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(nardol, war1));
		scn.FreepsChooseCard(war1);

		// The only [gondor] possession left to discard is this card, so nothing is asked.
		assertTrue(scn.IsHindered(war1));
		assertInDiscard(nardol);
		assertInZone(Zone.SUPPORT, war1);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void XIsNoMoreThanCanBeBothHinderedAndDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var walls = scn.GetFreepsCard("walls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(nardol, war1, war2, walls);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(nardol);

		// Three beacons and a condition: four cards, and each can pay for one half only (a hindered beacon is
		// no [gondor] possession). Two can be hindered and two discarded; three and three cannot.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(war1, war2);

		// What is left to discard is this card and the condition.
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertInDiscard(nardol, walls);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhenThereIsNothingToDiscardOnceABeaconIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var walls = scn.GetFreepsCard("walls");
		var shards = scn.GetFreepsCard("shards");
		var alliance = scn.GetFreepsCard("alliance");
		var sting = scn.GetFreepsCard("sting");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, alliance);
		scn.AttachCardsTo(frodo, sting);
		// The one beacon is this card, which cannot be both the beacon hindered and the possession discarded.
		// Beside it: a [gondor] condition that is hindered, a [gondor] artifact, an [elven] condition and a
		// [shire] possession.
		scn.MoveCardsToSupportArea(nardol, walls, shards);
		scn.HinderCard(walls);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(nardol));
	}
}
