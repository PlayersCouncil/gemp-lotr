package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_007_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase of
	// turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "32_7");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7; a fellowship ability only
					// Thorin, Oakenshield: [dwarven] Dwarf companion, strength 7, +1 for each [dwarven] follower he bears
					// (none here); a fellowship ability only
					put("thorin", "30_19");
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] Dwarf ally; +1 to Thorin and a start-of-regroup offer
					// Óin, Knowledgeable Healer: [dwarven] follower whose Aid discards a [dwarven] card from hand; heals
					// his bearer when transferred
					put("oin", "30_16");

					// Counted in the discard pile: [dwarven] possessions and artifacts
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon
					put("spear", "32_4");       // Dwarven Spear: [dwarven] possession, hand weapon
					put("emeralds", "31_3");    // Emeralds of Girion: [dwarven] artifact
					put("orcrist", "30_17");    // Orcrist: [dwarven] artifact, hand weapon
					// Not counted: a [dwarven] card of each other kind, and items of another culture
					put("fury", "30_3");        // Battle Fury: [dwarven] skirmish event
					put("kinsfolk", "30_20");   // To me! O my kinsfolk!: [dwarven] fellowship event
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion
					put("cones", "33_20");      // Burning Fir Cones: [gandalf] possession
					put("glamdring", "30_29");  // Glamdring: [gandalf] artifact
					put("dawn", "30_24");       // Dawn Take You All!: [gandalf] event
					put("noble", "30_14");      // Noble intentions: [dwarven] skirmish event

					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("troop", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RingofThrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Ring of Thrór
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Game Text: Bearer must be a [dwarven] companion.
		 * 	Bearer is strength +1 for each [dwarven] possession and [dwarven] artifact in your discard pile (limit +3).
		 * 	<b>Maneuver:</b> Take a [dwarven] event into hand from your discard pile. Discard this artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Thrór", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnlyOnADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin, thorin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToHand(ring);

		scn.StartGame();
		assertInZone(Zone.HAND, ring);

		scn.FreepsPlayCard(ring);
		// Two [dwarven] companions; not the [shire] Ring-bearer, nor a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);

		assertAttachedTo(ring, thorin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneForEachDwarvenPossessionAndArtifactInHisOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		var emeralds = scn.GetFreepsCard("emeralds");
		var spear = scn.GetFreepsCard("spear");
		var orcrist = scn.GetFreepsCard("orcrist");
		var fury = scn.GetFreepsCard("fury");
		var bifur = scn.GetFreepsCard("bifur");
		var dwalin = scn.GetFreepsCard("dwalin");
		var cones = scn.GetFreepsCard("cones");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(balin, thorin);
		scn.AttachCardsTo(balin, ring);
		// A [dwarven] artifact in play and a [dwarven] possession in hand: neither is in the discard pile.
		scn.AttachCardsTo(thorin, orcrist);
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		// A [dwarven] possession and a [dwarven] artifact, beside an event, a follower and a companion of the
		// culture, and a possession and an artifact of another.
		scn.MoveCardsToDiscard(axe, emeralds, fury, bifur, dwalin, cones, glamdring);
		scn.FreepsRefreshDecision();

		assertAttachedTo(ring, balin);
		assertAttachedTo(orcrist, thorin);
		assertInZone(Zone.HAND, spear);
		assertInZone(Zone.DISCARD, axe, emeralds, fury, bifur, dwalin, cones, glamdring);

		// 7 printed, +2 for the Axe and the Emeralds.
		assertEquals(9, scn.GetStrength(balin));
		// Thorin does not bear the Ring: 7 printed, +2 for Orcrist.
		assertEquals(9, scn.GetStrength(thorin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarvenItemsInTheOtherPlayersDiscardPileAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var shadowSpear = scn.GetShadowCard("spear");
		var shadowEmeralds = scn.GetShadowCard("emeralds");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, ring);

		scn.StartGame();
		scn.MoveCardsToDiscard(axe);
		// A [dwarven] possession and a [dwarven] artifact in the other player's own discard pile.
		scn.MoveCardsToDiscard(shadowSpear, shadowEmeralds);
		scn.FreepsRefreshDecision();

		assertAttachedTo(ring, balin);
		assertInZone(Zone.DISCARD, axe, shadowSpear, shadowEmeralds);
		// 7 printed, +1 for the Axe in the player's own discard pile alone.
		assertEquals(8, scn.GetStrength(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusGrowsAsADwarvenPossessionIsDiscardedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var oin = scn.GetFreepsCard("oin");
		var spear = scn.GetFreepsCard("spear");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, ring);
		scn.MoveCardsToSupportArea(oin);

		scn.StartGame();
		scn.MoveCardsToHand(spear);
		// Only a [dwarven] event in the discard pile: nothing counted.
		scn.MoveCardsToDiscard(fury);
		scn.MoveMinionsToTable("troop");
		scn.FreepsRefreshDecision();

		assertAttachedTo(ring, balin);
		assertInZone(Zone.SUPPORT, oin);
		assertInZone(Zone.HAND, spear);
		assertInZone(Zone.DISCARD, fury);
		assertEquals(7, scn.GetStrength(balin));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(7, scn.GetStrength(balin));
		// Óin's Aid discards the Spear from hand, the only [dwarven] card there.
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(bilbo);

		assertInZone(Zone.DISCARD, spear);
		assertAttachedTo(oin, bilbo);
		// 7 printed, +1 for the Spear now in the discard pile.
		assertEquals(8, scn.GetStrength(balin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var spear = scn.GetFreepsCard("spear");
		var emeralds = scn.GetFreepsCard("emeralds");
		var orcrist = scn.GetFreepsCard("orcrist");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, ring);

		scn.StartGame();
		scn.MoveCardsToDiscard(axe, spear, emeralds, orcrist);
		scn.FreepsRefreshDecision();

		assertAttachedTo(ring, balin);
		assertInZone(Zone.DISCARD, axe, spear, emeralds, orcrist);
		// Four counted: 7 printed, +3 and no more.
		assertEquals(10, scn.GetStrength(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverTakesADwarvenEventIntoHandFromTheDiscardPileAndDiscardsTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var fury = scn.GetFreepsCard("fury");
		var kinsfolk = scn.GetFreepsCard("kinsfolk");
		var dawn = scn.GetFreepsCard("dawn");
		var noble = scn.GetFreepsCard("noble");
		var shadowNoble = scn.GetShadowCard("noble");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, ring);

		scn.StartGame();
		scn.MoveCardsToDiscard(axe, fury, kinsfolk, dawn);
		// A [dwarven] event in the other player's discard pile, of a title the player's own piles do not hold.
		scn.MoveCardsToDiscard(shadowNoble);
		scn.MoveCardsToHand(noble);
		scn.FreepsRefreshDecision();

		assertAttachedTo(ring, balin);
		assertInZone(Zone.DISCARD, axe, fury, kinsfolk, dawn, shadowNoble);
		assertInZone(Zone.HAND, noble);
		// 7 printed, +1 for the Axe.
		assertEquals(8, scn.GetStrength(balin));

		// A Maneuver ability: not offered in the fellowship phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));

		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		// [dwarven] events of two phases; not a [dwarven] possession, a [gandalf] event, or the other player's event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, kinsfolk));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, dawn, shadowNoble));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(fury);

		assertInZone(Zone.HAND, fury, noble);
		assertInZone(Zone.DISCARD, ring, axe, kinsfolk, dawn, shadowNoble);
		// No Ring borne: 7 printed.
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenEventInTheDiscardPileTheManeuverIsStillUsedAndTheRingDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var dawn = scn.GetFreepsCard("dawn");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, ring);

		scn.StartGame();
		scn.MoveCardsToDiscard(axe, dawn);
		scn.MoveMinionsToTable("troop");
		scn.FreepsRefreshDecision();

		assertAttachedTo(ring, balin);
		assertInZone(Zone.DISCARD, axe, dawn);
		int hand = scn.GetFreepsHandCount();

		scn.SkipToPhase(Phase.MANEUVER);
		// Its own words discard it, so it is offered with nothing to take.
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		assertInZone(Zone.DISCARD, ring, axe, dawn);
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
