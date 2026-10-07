package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_130_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, alone: 1 companion + 9 for the site is (10) in the pool on arriving,
	// and one threat is as many as the dead pile can hold. No minion here has a site number above 6, so none
	// roams at site 9.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, cost 2; damage +1 is its whole text
					put("dunlending", "4_17");    // Dunlending Savage: [dunland] Man, cost 4, no text
					put("hunter", "1_256");       // Morgul Hunter: [sauron] Orc, cost 5; stronger for each companion, nothing else
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("pinned", "1_192");       // Pinned Down: [moria] condition, cost 1, plays to the support area
					put("scimitar", "1_180");     // Goblin Scimitar: [moria] possession, cost 0, bearer must be a [moria] Orc

					// Watcher in the Water, Keeper of Westgate: cost 4. "While you can spot Watcher in the Water,
					// discard all other minions (except tentacles). ... is discarded if not at a marsh."
					put("watcher", "2_73");
					// Evil-smelling Fens: "Each site on the adventure path is a marsh." It keeps the Watcher in
					// play; its own ability needs Gollum.
					put("fens", "5_22");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");       // Watchers of Cirith Ungol: a skirmish ability for a [wraith] Orc, never used
					put("site9", "103_130");      // Threshold of Doom
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThresholdofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Threshold of Doom
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. <b>Shadow:</b> Remove a burden or a threat to play a minion from your discard
		 * 	pile. If it costs (5) or more, add (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Threshold of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void WithABurdenAndAThreatTheShadowPlayerRemovesTheBurdenToPlayAMinionOfHisChoiceFromHisDiscardPileAndThenTheThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var dunlending = scn.GetShadowCard("dunlending");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");
		var hunter = scn.GetShadowCard("hunter");
		var troop = scn.GetShadowCard("troop");
		var freepsTroop = scn.GetFreepsCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(1);
		scn.AddThreats(1);
		// In the Shadow player's discard pile: two minions of two cultures, and a condition and a possession.
		scn.MoveCardsToDiscard(savage, dunlending, pinned, scimitar);
		// Minions that are not in his discard pile: in his hand, on his draw deck, and (the other player's
		// copy of the one on his deck) in the other player's discard pile.
		scn.MoveCardsToHand(hunter);
		scn.MoveCardsToTopOfDeck(troop);
		scn.MoveCardsToDiscard(freepsTroop);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Threshold of Doom", site.getBlueprint().getTitle());

		// 1 companion + 9 for the site
		assertEquals(10, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Remove a burden");
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());

		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, dunlending));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned, scimitar, hunter, troop));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(dunlending);
		assertInZone(Zone.SHADOW_CHARACTERS, dunlending);
		assertInDiscard(savage, pinned, scimitar, freepsTroop);
		assertInZone(Zone.HAND, hunter);
		assertInZone(Zone.DECK, troop);
		// The Dunlending Savage's (4) is paid, and nothing is added for a minion that costs (4).
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Only the threat is left to remove, so there is no question; one minion is left to play, so there
		// is no choice.
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInDiscard(pinned, scimitar);
		// The Uruk Savage's (2)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovingTheThreatPlaysTheMinionTooAndAMinionThatCostsFiveOrMoreAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunter = scn.GetShadowCard("hunter");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(1);
		scn.AddThreats(1);
		scn.MoveCardsToDiscard(hunter, troop);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		// 1 companion + 9 for the site
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Remove a threat");
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.ShadowHasCardChoiceAvailable(hunter, troop));
		scn.ShadowChooseCardBPFromSelection(hunter);
		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		assertInDiscard(troop);
		// 10 - 5 for the Morgul Hunter + 1 because he costs exactly (5)
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Only the burden is left to remove.
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// 6 - 6 for the Goblin Patrol Troop + 1 because it costs more than (5)
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNeitherABurdenNorAThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToDiscard(savage);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		// A minion to play and (10) to pay its (2) with, and nothing to remove.
		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A burden alone makes it usable; so does a threat alone.
		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(site));

		scn.RemoveBurdens(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(site));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(site));
	}

	@Test
	public void IsNotOfferedWithNoMinionInTheDiscardPileOrNoneThePoolCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunter = scn.GetShadowCard("hunter");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(1);
		// A condition and a possession in the discard pile, and the minion in hand.
		scn.MoveCardsToDiscard(pinned, scimitar);
		scn.MoveCardsToHand(hunter);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		assertEquals(10, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(site));

		// The minion is in the discard pile now, with one twilight too few for his (5): the (1) the site
		// adds comes after he is played and does not help to pay for him.
		scn.MoveCardsToDiscard(hunter);
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(site));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		assertInDiscard(pinned, scimitar);
		// 5 - 5 + 1
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsAShadowPhaseActionAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(1);
		scn.MoveCardsToDiscard(savage);
		// A minion on the table, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(site));
		assertEquals(1, scn.GetBurdens());
		assertEquals(10, scn.GetTwilight());
		assertInDiscard(savage);
	}

	@Test
	public void TheCostThatCountsIsTheNumberPrintedOnTheMinionNotWhatWasPaidToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunter = scn.GetShadowCard("hunter");
		var dunlending = scn.GetShadowCard("dunlending");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(2);
		scn.MoveCardsToDiscard(hunter, dunlending);
		// The [sauron] minion, printed (5), is played for 1 less; the [dunland] minion, printed (4), for 1 more.
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, Culture.SAURON, null, new ConstantEvaluator(-1)));
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, Culture.DUNLAND, null, new ConstantEvaluator(1)));

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		assertEquals(10, scn.GetTwilight());
		// What each would cost to play from the discard pile.
		assertEquals(4, scn.GetTwilightCost(hunter));
		assertEquals(5, scn.GetTwilightCost(dunlending));

		scn.ShadowUseCardAction(site);
		assertEquals(1, scn.GetBurdens());
		scn.ShadowChooseCardBPFromSelection(hunter);
		assertInZone(Zone.SHADOW_CHARACTERS, hunter);
		// In play he costs the (5) printed on him: 10 - 4 paid + 1.
		assertEquals(5, scn.GetTwilightCost(hunter));
		assertEquals(7, scn.GetTwilight());

		scn.ShadowUseCardAction(site);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, dunlending);
		// In play he costs the (4) printed on him: 7 - 5 paid, and nothing added.
		assertEquals(4, scn.GetTwilightCost(dunlending));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionDiscardedAsItComesIntoPlayIsPaidForAndTheOneIsStillAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunter = scn.GetShadowCard("hunter");
		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AddBurdens(1);
		scn.MoveCardsToDiscard(hunter);
		// Every site is a marsh, so the Watcher stays; it discards every other minion that comes into play.
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);

		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(hunter);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		// His (5) stays paid, and the (1) is an effect of its own that is still carried out: 10 - 5 + 1.
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
