package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("camp", "15_113");
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc minion, vitality 2; strength +2 only at an underground site
					put("denizen2", "11_115");
					put("goblin", "11_137");      // Scurrying Goblin: [orc] Orc minion; adds (2) only when played at an underground site
					put("halftroll", "14_12");    // Half-troll of Far Harad: [orc] minion who is no Orc, vitality 2; a start-of-assignment trigger never reached
					put("halftroll2", "14_12");
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc minion; his text answers only a weapon played on him
					put("chieftain2", "1_266");
					put("elf", "1_53");           // Lorien Elf: a Free Peoples card, here only as a card of the draw deck

					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: the [gandalf] Wizard Traveled Leader spots
					put("leader", "12_34");       // Traveled Leader: Maneuver or Regroup: spot a [gandalf] Wizard to replace a site in the fellowship's current region
					put("leader2", "12_34");
					put("leader3", "12_34");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows-block path, where the Shadow player holds the sites not yet laid and may replace the one under the
	// fellowship.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("camp", "15_113");
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc minion; strength +2 only at an underground site
					// Ulaire Nelya, Third of the Nine Riders: "Shadow: Exert Ulaire Nelya to replace the fellowship's
					// current site with a site from your adventure deck."
					put("nelya", "11_222");
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");     // Sirannon Ruins: River, and no text
					put("glade", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("road", "11_236");      // East Road: each companion is twilight cost +2, and no companion is played
					// Never laid in these tests, and never named: copies of Expanding Marshland.
					put("site4", "11_238");
					put("site5", "11_238");
					put("site6", "11_238");
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishCampStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Orkish Camp
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot an [orc] Orc, the Free Peoples player cannot replace the fellowship's current site.<br><b>Shadow:</b> Exert an [orc] minion and remove (2) to reveal the bottom card of your draw deck. If it is an [orc] Orc, take it into hand. Otherwise, discard it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("camp");

		assertEquals("Orkish Camp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileAnOrcOrcCanBeSpottedTheFreePeoplesPlayerCannotReplaceTheCurrentSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var leader2 = scn.GetFreepsCard("leader2");
		var leader3 = scn.GetFreepsCard("leader3");
		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		scn.MoveCompanionsToTable(gandalf);

		var camp = scn.GetShadowCard("camp");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var chieftain = scn.GetShadowCard("chieftain");
		var shadowSite2 = scn.GetShadowSite(2);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(leader, leader2, leader3);
		// No minion is played, so the turn goes from the move to site 3 straight to the regroup phase.  Sites 2
		// and 3 were laid by the moves, and are the other player's.  The minions stand as minions that came
		// through the skirmishes stand.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(camp);
		scn.MoveMinionsToTable(denizen, halftroll, chieftain);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, camp);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen, halftroll, chieftain);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite3, scn.GetCurrentSite());
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, site2, site3);

		// The fellowship's current site is not replaced.
		scn.FreepsPlayCard(leader);
		scn.FreepsChooseCard(shadowSite3);
		assertSame(shadowSite3, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertInDiscard(leader);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// A site of the region that is not the current one is.
		scn.ShadowPass();
		scn.FreepsPlayCard(leader2);
		scn.FreepsChooseCard(shadowSite2);
		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With the [orc] Orc gone, an [orc] minion who is no Orc, a [sauron] Orc and the Camp itself, an [orc] card,
		// stop nothing.
		scn.MoveCardsToDiscard(denizen);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPlayCard(leader3);
		scn.FreepsChooseCard(shadowSite3);
		assertSame(site3, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnotherOrcOrcStopsTheReplacementToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var site3 = scn.GetFreepsSite(3);
		scn.MoveCompanionsToTable(gandalf);

		var camp = scn.GetShadowCard("camp");
		var goblin = scn.GetShadowCard("goblin");
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(leader);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(camp);
		scn.MoveMinionsToTable(goblin);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, camp);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertSame(shadowSite3, scn.GetCurrentSite());

		scn.FreepsPlayCard(leader);
		scn.FreepsChooseCard(shadowSite3);
		assertSame(shadowSite3, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillReplaceTheCurrentSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var camp = scn.GetShadowCard("camp");
		var denizen = scn.GetShadowCard("denizen");
		var nelya = scn.GetShadowCard("nelya");
		var glade = scn.GetShadowSite("glade");
		var road = scn.GetShadowSite("road");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveCardsToSupportArea(camp);
		scn.MoveMinionsToTable(denizen, nelya);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SUPPORT, camp);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen, nelya);
		assertEquals(glade, scn.GetCurrentSite());

		// The bar is on the Free Peoples player alone.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(road);
		assertEquals(road, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsAnOrcMinionAndRemovesTwoToTakeAnOrcOrcFromTheBottomOfTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowCard("camp");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var chieftain = scn.GetShadowCard("chieftain");
		var goblin = scn.GetShadowCard("goblin");
		var denizen2 = scn.GetShadowCard("denizen2");

		scn.StartGame();
		scn.MoveCardsToSupportArea(camp);
		scn.MoveMinionsToTable(denizen, halftroll, chieftain);
		// From the bottom up: the Goblin, then a second [orc] Orc of another title above it.
		scn.MoveCardsToBottomOfDeck(denizen2, goblin);
		assertInZone(Zone.DECK, goblin, denizen2);
		assertInZone(Zone.SUPPORT, camp);
		assertEquals(goblin, scn.GetShadowBottomOfDeck());
		assertEquals(denizen2, scn.GetFromBottomOfShadowDeck(2));
		scn.SetTwilight(5);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		int pool = scn.GetTwilight();
		assertEquals(goblin, scn.GetShadowBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertTrue(scn.ShadowActionAvailable(camp));
		scn.ShadowUseCardAction(camp);

		// An [orc] Orc and an [orc] minion who is no Orc may exert; the [sauron] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain));
		scn.ShadowChooseCard(halftroll);

		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(pool - 2, scn.GetTwilight());
		// The bottom card is shown to both players.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(goblin, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, false));
		scn.DismissRevealedCards();

		// The Scurrying Goblin is an [orc] Orc: it goes to hand.
		assertInHand(goblin);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Any [orc] Orc is taken, not only the Goblin: the Denizen, now at the bottom, goes to hand too.  The
		// Half-troll is exhausted by now and the [sauron] Orc may not exert, so the other Denizen pays.
		assertEquals(denizen2, scn.GetShadowBottomOfDeck());
		scn.ShadowUseCardAction(camp);
		scn.DismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertInHand(goblin, denizen2);
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnyOtherBottomCardIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowCard("camp");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var halftroll = scn.GetShadowCard("halftroll");
		var halftroll2 = scn.GetShadowCard("halftroll2");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		var elf = scn.GetShadowCard("elf");

		scn.StartGame();
		scn.MoveCardsToSupportArea(camp);
		scn.MoveMinionsToTable(denizen, denizen2, halftroll);
		// From the bottom up: an [orc] minion who is no Orc, a [sauron] Orc, and a Free Peoples card.
		scn.MoveCardsToBottomOfDeck(elf, chieftain2, halftroll2);
		assertInZone(Zone.DECK, elf, chieftain2, halftroll2);
		assertInZone(Zone.SUPPORT, camp);
		assertEquals(halftroll2, scn.GetShadowBottomOfDeck());
		assertEquals(chieftain2, scn.GetFromBottomOfShadowDeck(2));
		assertEquals(elf, scn.GetFromBottomOfShadowDeck(3));
		scn.SetTwilight(10);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		assertEquals(halftroll2, scn.GetShadowBottomOfDeck());
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowUseCardAction(camp);
		scn.ShadowChooseCard(denizen);
		scn.DismissRevealedCards();
		assertInDiscard(halftroll2);
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		scn.ShadowUseCardAction(camp);
		scn.ShadowChooseCard(denizen2);
		scn.DismissRevealedCards();
		assertInDiscard(chieftain2);

		scn.ShadowUseCardAction(camp);
		scn.DismissRevealedCards();
		assertInDiscard(elf);
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyOrcMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowCard("camp");
		var denizen = scn.GetShadowCard("denizen");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveCardsToSupportArea(camp);
		// Vitality 2 with 1 wound: the Denizen cannot exert; the [sauron] Orc beside him could, and does not pay.
		scn.MoveMinionsToTable(denizen, chieftain);
		scn.AddWoundsToChar(denizen, 1);
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertInZone(Zone.SUPPORT, camp);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.GetTwilight() >= 2);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(camp));
	}

	@Test
	public void NotOfferedWithOneTwilightToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowCard("camp");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveCardsToSupportArea(camp);
		scn.MoveMinionsToTable(denizen);
		assertInZone(Zone.SUPPORT, camp);
		assertEquals(0, scn.GetWoundsOn(denizen));
		scn.FreepsPass();

		// One token, one short of the (2) the ability removes.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(camp));
	}

	@Test
	public void WithAnEmptyDrawDeckItIsOfferedAndTheCostsBuyNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var camp = scn.GetShadowCard("camp");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveCardsToSupportArea(camp);
		scn.MoveMinionsToTable(denizen);
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetShadowDrawDeck()).toArray(new PhysicalCardImpl[0]));
		assertInZone(Zone.SUPPORT, camp);
		scn.SetTwilight(5);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		int discard = scn.GetShadowDiscardCount();
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		// A reveal the costs buy is offered with the deck empty (Q-0163).
		assertTrue(scn.ShadowActionAvailable(camp));
		scn.ShadowUseCardAction(camp);

		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(pool - 2, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
