package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_039_Tests
{

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain (its Shadow ability needs an [isengard]
	// minion, and none is laid).  Site 3, Rivendell Valley 1_341, is no mountain and not underground; site 5,
	// The Bridge of Khazad-dum 1_349, is underground and nothing more (its Balrog play is never used).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alone", "12_39");

					// For the hand.
					put("look", "5_26");        // Look at Him: a Shadow [gollum] event
					put("soon", "5_21");        // Be Back Soon: a Free Peoples [gollum] event
					put("stole", "6_46");       // They Stole It: a [gollum] condition, no event
					put("veils", "1_204");      // All Veils Removed: a [wraith] event

					// For the discard pile.
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc minion
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion
					put("tip", "1_209");        // Blade Tip: a [wraith] condition, no minion
					put("troop", "1_177");      // Goblin Patrol Troop: a minion, laid in the other player's pile only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The plain Fellowship path: site 5, The Bridge of Khazad-dum 1_349, is underground; site 6, Galadriel's Glade
	// 1_351, is a sanctuary and neither a mountain nor underground.
	protected VirtualTableScenario GetFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alone", "12_39");
					put("look", "5_26");        // Look at Him: a Shadow [gollum] event
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path: site 3, The Dimholt 8_117, is underground and a sanctuary.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alone", "12_39");
					put("look", "5_26");        // Look at Him: a Shadow [gollum] event
					put("tip", "1_209");        // Blade Tip: a [wraith] condition, no minion
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotAloneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Not Alone
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time the fellowship moves to a mountain or underground site, you may discard a [gollum] event from hand to take a minion from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alone");

		assertEquals("Not Alone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMoveToAMountainOrUndergroundSiteOffersAGollumEventDiscardedForAMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetShadowCard("alone");
		var look = scn.GetShadowCard("look");
		var soon = scn.GetShadowCard("soon");
		var stole = scn.GetShadowCard("stole");
		var veils = scn.GetShadowCard("veils");
		var hound = scn.GetShadowCard("hound");
		var savage = scn.GetShadowCard("savage");
		var tip = scn.GetShadowCard("tip");
		var troop = scn.GetFreepsCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(alone);
		scn.SetShadowHand(look, soon, stole, veils);
		scn.MoveCardsToDiscard(hound, savage, tip, troop);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, alone);
		assertInHand(look, soon, stole, veils);
		assertEquals(4, scn.GetShadowHandCount());
		assertInDiscard(hound, savage, tip, troop);
		// The turns played to reach site 3 may have sent cards of the Shadow deck to the pile already.
		int discard = scn.GetShadowDiscardCount();

		// The move to Pass of Caradhras, a mountain.
		scn.FreepsPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(alone));
		scn.ShadowAcceptOptionalTrigger();

		// Either side's [gollum] event pays; a [gollum] card that is no event, and an event of another culture, do not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(look, soon));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stole, veils));
		scn.ShadowChooseCard(look);
		assertInDiscard(look);

		// Either minion of the player's own pile; not the condition there, nor the other player's minion in his.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tip, troop));
		scn.ShadowChooseCardBPFromSelection(hound);

		assertInHand(hound);
		assertInHand(soon, stole, veils);
		assertEquals(4, scn.GetShadowHandCount());
		assertInDiscard(look, savage, tip);
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertInDiscard(troop);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second move in the same turn, to The Bridge of Khazad-dum, underground: offered again, and declined.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// (The Shadow player's reconcile ahead of the move may have changed his hand and pile: read them here.)
		int hand = scn.GetShadowHandCount();
		discard = scn.GetShadowDiscardCount();
		assertInHand(soon);
		assertInDiscard(savage);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(alone));
		scn.ShadowDeclineOptionalTrigger();

		assertInHand(soon);
		assertEquals(hand, scn.GetShadowHandCount());
		assertInDiscard(savage);
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoveFromAnUndergroundSiteToASiteThatIsNeitherOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var alone = scn.GetShadowCard("alone");
		var look = scn.GetShadowCard("look");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(alone);
		scn.SetShadowHand(look);
		scn.MoveCardsToDiscard(hound);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, alone);
		assertInHand(look);
		assertInDiscard(hound);

		// From The Bridge of Khazad-dum, underground, to Galadriel's Glade, neither.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(alone));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(look);
		assertInDiscard(hound);
	}

	@Test
	public void WithNoGollumEventInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alone = scn.GetShadowCard("alone");
		var stole = scn.GetShadowCard("stole");
		var veils = scn.GetShadowCard("veils");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(alone);
		scn.SetShadowHand(stole, veils);
		scn.MoveCardsToDiscard(hound);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, alone);
		assertInHand(stole, veils);
		assertInDiscard(hound);

		// The move to Pass of Caradhras, a mountain, with a minion to take.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(alone));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(stole, veils);
		assertInDiscard(hound);
	}

	@Test
	public void AMoveToAnUndergroundSanctuaryIsAnswerToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var alone = scn.GetShadowCard("alone");
		var look = scn.GetShadowCard("look");
		var tip = scn.GetShadowCard("tip");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(alone);
		scn.SetShadowHand(look);
		scn.MoveCardsToDiscard(tip);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, alone);
		assertInHand(look);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(tip);
		int discard = scn.GetShadowDiscardCount();

		// The move to The Dimholt, underground and a sanctuary, with no minion in the discard pile: the event is
		// paid for nothing.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(alone));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(look, tip);
		assertEquals(discard + 1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
