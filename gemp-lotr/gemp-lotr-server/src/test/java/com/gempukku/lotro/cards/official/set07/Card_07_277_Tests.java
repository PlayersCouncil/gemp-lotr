package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_277_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sapper", "7_277");
					put("axeman", "7_271");     // Gorgoroth Axeman: [sauron] Orc besieger, cost 3, site 5; a skirmish ability only
					put("patrol", "8_99");      // Gorgoroth Patrol: [sauron] Orc besieger, cost 4, site 5; a regroup ability only while stacked
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, no besieger, cost 2, site 6; his text answers a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, no besieger; a skirmish ability only
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc, no besieger, cost 2, site 6; his texts answer a minion played and two Hobbits spotted
					put("troll", "8_102");      // Great Hill Troll: a besieger who is no Orc, cost 9; his trigger is for King-block sites only
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), cost 3; damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), cost 2; his text needs 2 wounds on the Ring-bearer
					put("keeper", "10_86");     // Gorgoroth Keeper: [sauron] Orc besieger; a cost text and a regroup ability
					put("breaker", "8_97");     // Gorgoroth Breaker: [sauron] Orc besieger; an archery ability only
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition that play stacks a [sauron] Orc on
					put("hand1", "1_7");        // Dwarf Guard: cards for the Shadow hand to pay with
					put("hand2", "1_7");
					put("hand3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothSapperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Sapper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Shadow:</b> Discard 2 cards from hand to play a [sauron] Orc stacked on a site you control (discard 1 card from hand instead if that Orc is a besieger).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sapper");

		assertEquals("Gorgoroth Sapper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardsOneCardFromHandToPlayABesiegerOrcStackedOnHisSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var axeman = scn.GetShadowCard("axeman");
		var patrol = scn.GetShadowCard("patrol");
		var chieftain = scn.GetShadowCard("chieftain");
		var assassin = scn.GetShadowCard("assassin");
		var troll = scn.GetShadowCard("troll");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var keeper = scn.GetShadowCard("keeper");
		var teeth = scn.GetShadowCard("teeth");
		var theirBreaker = scn.GetFreepsCard("breaker");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");
		var hand3 = scn.GetShadowCard("hand3");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player (with a besieger of his own on it), sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.StackCardsOn(site1, theirBreaker);
		scn.StackCardsOn(site2, axeman, patrol, chieftain, assassin, troll, guard, hound);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, keeper);
		assertInZone(Zone.STACKED, theirBreaker, axeman, patrol, chieftain, assassin, troll, guard, hound, keeper);
		scn.MoveMinionsToTable(sapper);
		scn.SetShadowHand(hand1, hand2, hand3);
		assertEquals(3, scn.GetShadowHandCount());

		scn.FreepsPass();
		// Enough twilight for any of the stacked cards, so that only the text keeps one out.
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(sapper, "Play a besieger"));
		scn.ShadowUseCardAction(sapper, "Play a besieger");

		// One card of three: the price of a besieger, though he could pay two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hand1);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(hand1);

		// The [sauron] Orc besiegers on his site; not the [sauron] Orcs who are no besiegers, the besieger who is no
		// Orc, the [sauron] Uruk-hai, the [wraith] Orc, the besieger on his condition or the one on the other
		// player's site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, patrol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, assassin, troll, guard, hound, keeper, theirBreaker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axeman);

		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertInZone(Zone.STACKED, patrol, chieftain, assassin, troll, guard, hound, keeper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsTwoCardsFromHandToPlayASauronOrcWhoIsNoBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var axeman = scn.GetShadowCard("axeman");
		var patrol = scn.GetShadowCard("patrol");
		var chieftain = scn.GetShadowCard("chieftain");
		var assassin = scn.GetShadowCard("assassin");
		var troll = scn.GetShadowCard("troll");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var soldier = scn.GetShadowCard("soldier");
		var teeth = scn.GetShadowCard("teeth");
		var theirChieftain = scn.GetFreepsCard("chieftain");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");
		var hand3 = scn.GetShadowCard("hand3");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player (with an Orc of his own on it), sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.StackCardsOn(site1, theirChieftain);
		scn.StackCardsOn(site2, axeman, patrol, chieftain, assassin, troll, guard, hound);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, soldier);
		assertInZone(Zone.STACKED, theirChieftain, axeman, patrol, chieftain, assassin, troll, guard, hound, soldier);
		scn.MoveMinionsToTable(sapper);
		scn.SetShadowHand(hand1, hand2, hand3);
		assertEquals(3, scn.GetShadowHandCount());

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowActionAvailable(sapper, "Play a non-besieger"));
		scn.ShadowUseCardAction(sapper, "Play a non-besieger");

		// Two cards of three.
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(hand1, hand2);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(hand1, hand2);

		// The [sauron] Orcs who are no besiegers on his site; the besiegers go by the other ability, at one card; not
		// the Orc on his condition or the one on the other player's site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, assassin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, patrol, troll, guard, hound, soldier, theirChieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.STACKED, axeman, patrol, assassin, troll, guard, hound, soldier, theirChieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneCardInHandOnlyABesiegerCanBePlayedAndWithNoneNeitherIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");
		var hand1 = scn.GetShadowCard("hand1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		// Only an Orc who is no besieger on his site to begin with.
		scn.StackCardsOn(site1, chieftain);
		assertInZone(Zone.STACKED, chieftain);
		scn.MoveMinionsToTable(sapper);
		scn.SetShadowHand(hand1);
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sapper, "Play a non-besieger"));
		assertFalse(scn.ShadowActionAvailable(sapper, "Play a besieger"));

		// A besieger on his site: one card pays for him.
		scn.StackCardsOn(site1, axeman);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.STACKED, axeman);
		assertTrue(scn.ShadowActionAvailable(sapper, "Play a besieger"));
		assertFalse(scn.ShadowActionAvailable(sapper, "Play a non-besieger"));

		// With no card in hand, neither.
		scn.SetShadowHand();
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sapper, "Play a besieger"));
		assertFalse(scn.ShadowActionAvailable(sapper, "Play a non-besieger"));
	}

	@Test
	public void IsNotOfferedWithTwilightOneShortOfTheBesiegersCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapper = scn.GetShadowCard("sapper");
		var axeman = scn.GetShadowCard("axeman");
		var hand1 = scn.GetShadowCard("hand1");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, axeman);
		assertInZone(Zone.STACKED, axeman);
		scn.MoveMinionsToTable(sapper);
		scn.SetShadowHand(hand1);
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPass();
		// The Axeman costs 3 at site 5, his own site number: no roaming.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sapper, "Play a besieger"));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(sapper, "Play a besieger"));
	}
}
