package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_064_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("searcher1", "102_64");
					put("searcher2", "102_64");

					// Grishnakh, Orc Captain: "The site number of each [sauron] Orc is -3."  With him in play a
					// [sauron] Orc printed 6 is at 3, one above site 2, where the Shadow phase of these tests is.
					put("grishnakh", "5_100");
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc and no tracker, printed site number 6; a skirmish ability
					put("gorgoroth", "7_278");  // Gorgoroth Soldier: a [sauron] Orc, a besieger, printed site number 5; a response that needs 2 cards in hand
					put("worker", "3_62");      // Isengard Worker: an [isengard] Orc and no tracker, printed site number 4; strength 5, vitality 3; a regroup ability
					put("mouth", "12_118");     // The Mouth of Sauron, Lieutenant of Barad-dur: a [sauron] Man, printed site number 5; an assignment ability

					put("uscout", "2_47");      // Uruk Scout: an [isengard] tracker, strength 7, vitality 2; Damage +1; a response to events that spot or exert a ranger
					put("raider", "53_74");     // Uruk Raider: an [isengard] tracker, strength 6, vitality 2; its text is about allies
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai and no tracker, strength 5, vitality 3; Damage +1 is its whole text
					put("oscout", "2_89");      // Orc Scout: a [sauron] tracker, strength 6, vitality 2; a response to events that spot or exert a ranger

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; Damage +1
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text that matters once in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A format in which a skirmish involving the Ring-bearer may be cancelled.  The default one does not allow it.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("searcher1", "102_64");
					put("raider", "53_74");     // Uruk Raider: an [isengard] tracker, strength 6, vitality 2; its text is about allies
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void OrcSearcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Orc Searcher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: Tracker.
		 * 	The site number of each [Sauron] orc is -1.
		 * 	Skirmish: Discard this minion to cancel a skirmish involving an [isengard] tracker.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("searcher1");

		assertEquals("Orc Searcher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedFromHandFor2AndTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var searcher = scn.GetShadowCard("searcher1");
		scn.MoveCardsToHand(searcher);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(searcher));

		scn.ShadowPlayCard(searcher);
		// 2 to play + 2 roaming: site number 6 at site 2
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, searcher);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SiteNumberOfEachSauronOrcIsOneLowerForEachSearcherInPlayAndHisOwnOnlyOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var searcher1 = scn.GetShadowCard("searcher1");
		var searcher2 = scn.GetShadowCard("searcher2");
		scn.MoveCardsToHand(searcher1, searcher2);

		var grishnakh = scn.GetShadowCard("grishnakh");
		var soldier = scn.GetShadowCard("soldier");
		var gorgoroth = scn.GetShadowCard("gorgoroth");
		var worker = scn.GetShadowCard("worker");
		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, soldier, gorgoroth, worker, mouth);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// Grishnakh's -3 on the [sauron] Orcs.
		assertEquals(3, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(3, scn.GetMinionSiteNumber(soldier));
		assertEquals(2, scn.GetMinionSiteNumber(gorgoroth));
		assertEquals(4, scn.GetMinionSiteNumber(worker));
		assertEquals(5, scn.GetMinionSiteNumber(mouth));
		assertTrue(scn.HasKeyword(soldier, Keyword.ROAMING));

		// As he is played the Searcher's own text does not count for him: 6 - 3 is above site 2, so he is
		// roaming.  2 to play + 2 roaming.
		scn.ShadowPlayCard(searcher1);
		assertEquals(9, scn.GetTwilight());

		// Once in play he is a [sauron] Orc like the others, and each of them is 1 lower.  The [isengard]
		// Orc and the [sauron] Man are where they were.
		assertEquals(2, scn.GetMinionSiteNumber(searcher1));
		assertEquals(2, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(2, scn.GetMinionSiteNumber(soldier));
		assertEquals(1, scn.GetMinionSiteNumber(gorgoroth));
		assertEquals(4, scn.GetMinionSiteNumber(worker));
		assertEquals(5, scn.GetMinionSiteNumber(mouth));
		assertFalse(scn.HasKeyword(soldier, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(searcher1, Keyword.ROAMING));

		// The second Searcher, still in hand, is a [sauron] Orc too: 6 - 3 - 1 is site 2, so he is not
		// roaming and costs his 2.
		assertTrue(scn.ShadowPlayAvailable(searcher2));
		scn.ShadowPlayCard(searcher2);
		assertEquals(7, scn.GetTwilight());

		// Two Searchers: 2 lower.
		assertEquals(1, scn.GetMinionSiteNumber(searcher1));
		assertEquals(1, scn.GetMinionSiteNumber(searcher2));
		assertEquals(1, scn.GetMinionSiteNumber(soldier));
		assertEquals(0, scn.GetMinionSiteNumber(gorgoroth));
		assertEquals(4, scn.GetMinionSiteNumber(worker));
		assertEquals(5, scn.GetMinionSiteNumber(mouth));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsItselfToCancelASkirmishInvolvingAnIsengardTracker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var searcher1 = scn.GetShadowCard("searcher1");
		var searcher2 = scn.GetShadowCard("searcher2");
		var uscout = scn.GetShadowCard("uscout");
		var raider = scn.GetShadowCard("raider");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(searcher1, searcher2, uscout, raider, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not an assignment action.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(searcher1));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uscout},
				new PhysicalCardImpl[]{gimli, raider});
		// The Shadow player adds the Savage to Gimli's skirmish and leaves the Searchers out.
		scn.ShadowAssignToMinions(gimli, savage);

		// Aragorn against an Uruk Scout.  Neither Searcher is in the skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, uscout));
		assertTrue(scn.ShadowActionAvailable(searcher1));
		assertTrue(scn.ShadowActionAvailable(searcher2));
		assertInZone(Zone.SHADOW_CHARACTERS, searcher1, searcher2);

		scn.ShadowUseCardAction(searcher1);
		assertInDiscard(searcher1);
		assertInZone(Zone.SHADOW_CHARACTERS, searcher2);
		// The skirmish is over with nobody having won or lost it.
		assertFalse(scn.IsCharSkirmishing(uscout));
		assertFalse(scn.IsCharAssigned(uscout));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(uscout));
		// Gimli's is untouched.
		assertTrue(scn.IsCharAssignedAgainst(gimli, raider));
		assertTrue(scn.IsCharAssignedAgainst(gimli, savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli against an Uruk Raider, a second [isengard] tracker, and an Uruk Savage, who is none: one
		// tracker among the minions is enough.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, raider));
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, savage));
		assertFalse(scn.HasKeyword(savage, Keyword.TRACKER));
		assertTrue(scn.ShadowActionAvailable(searcher2));

		scn.ShadowUseCardAction(searcher2);
		assertInDiscard(searcher2);
		assertFalse(scn.IsCharSkirmishing(raider));
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharAssigned(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(raider));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InASkirmishWithNoIsengardTrackerItIsDiscardedAndNothingIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);

		var searcher1 = scn.GetShadowCard("searcher1");
		var searcher2 = scn.GetShadowCard("searcher2");
		var oscout = scn.GetShadowCard("oscout");
		var savage = scn.GetShadowCard("savage");
		var uscout = scn.GetShadowCard("uscout");

		scn.StartGame();
		scn.MoveMinionsToTable(searcher1, searcher2, oscout, savage, uscout);
		// One wound short of dead, so that Aragorn's skirmish takes the Uruk Scout off the table.
		scn.AddWoundsToChar(uscout, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, oscout},
				new PhysicalCardImpl[]{aragorn, uscout},
				new PhysicalCardImpl[]{gimli, savage});
		scn.ShadowDeclineAssignments();

		// The Dwarf Guard against an Orc Scout, a tracker of the wrong culture, while the Uruk Scout, an
		// [isengard] tracker, waits for Aragorn.  The ability has a cost that can be paid, so it is offered.
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(guard, oscout));
		assertTrue(scn.ShadowActionAvailable(searcher1));

		scn.ShadowUseCardAction(searcher1);
		assertInDiscard(searcher1);
		assertInZone(Zone.SHADOW_CHARACTERS, searcher2);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, oscout));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, uscout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// The Guard's 4 loses to the Orc Scout's 6.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(oscout));

		// Aragorn's 8 beats the Uruk Scout's 7 and kills him: no [isengard] tracker is left in play.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(uscout);

		// Gimli against an Uruk Savage, an [isengard] minion who is no tracker.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, savage));
		assertTrue(scn.ShadowActionAvailable(searcher2));

		scn.ShadowUseCardAction(searcher2);
		assertInDiscard(searcher2);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Gimli's 6 beats the Savage's 5, and Gimli is damage +1.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InFellowshipBlockCancelsASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var frodo = scn.GetRingBearer();
		var searcher = scn.GetShadowCard("searcher1");
		var raider = scn.GetShadowCard("raider");

		scn.StartGame();
		scn.MoveMinionsToTable(searcher, raider);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, raider);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, raider));
		assertTrue(scn.ShadowActionAvailable(searcher));

		scn.ShadowUseCardAction(searcher);
		assertInDiscard(searcher);
		assertFalse(scn.IsCharSkirmishing(raider));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(raider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathIsDiscardedAndDoesNotCancelASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var searcher = scn.GetShadowCard("searcher1");
		var raider = scn.GetShadowCard("raider");

		scn.StartGame();
		scn.MoveMinionsToTable(searcher, raider);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, raider);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, raider));
		// The ability has a cost that can be paid, so it is offered; its cancel is what the format refuses.
		assertTrue(scn.ShadowActionAvailable(searcher));

		scn.ShadowUseCardAction(searcher);
		assertInDiscard(searcher);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, raider));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish is fought and has a loser: Frodo, 3 + 1 for the Ring against 6.  He is offered the
		// Ring for the wound.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(raider));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
