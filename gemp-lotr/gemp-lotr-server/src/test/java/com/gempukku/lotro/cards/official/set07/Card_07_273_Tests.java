package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_273_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("garrison", "7_273");
					put("axeman", "7_271");     // Gorgoroth Axeman: [sauron] Orc besieger; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, no besieger; his text answers a weapon played on him
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc, no besieger; his texts answer a minion played and two Hobbits spotted
					put("troll", "8_102");      // Great Hill Troll: a besieger who is no Orc; his trigger is for King-block sites only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]); his text needs 2 wounds on the Ring-bearer
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai); damage +1 only
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
	public void GorgorothGarrisonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Garrison
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Regroup:</b> Discard 2 cards from hand to stack a [sauron] Orc on a site you control (or discard 1 card from hand if that Orc is a besieger).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("garrison");

		assertEquals("Gorgoroth Garrison", card.getBlueprint().getTitle());
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
	public void DiscardsOneCardFromHandToStackABesiegerOrcOnASiteHeControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetShadowCard("garrison");
		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");
		var assassin = scn.GetShadowCard("assassin");
		var troll = scn.GetShadowCard("troll");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player, sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);

		scn.SkipToPhase(Phase.REGROUP);
		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		scn.MoveMinionsToTable(garrison, axeman, chieftain, assassin, troll, hound, guard);
		scn.SetShadowHand(hand1, hand2);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(garrison, "Stack a besieger"));
		scn.ShadowUseCardAction(garrison, "Stack a besieger");

		// One card of two: the price of a besieger.
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hand1);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(hand1);

		// His two sites; not the Free Peoples player's, nor the sites still on the path.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1, site4, site5));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site2);

		// The two [sauron] Orc besiegers, the Garrison himself among them; not the [sauron] Orcs who are no
		// besiegers, the besieger who is no Orc, the [wraith] Orc or the [sauron] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(garrison, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, assassin, troll, hound, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axeman);

		assertInZone(Zone.STACKED, axeman);
		assertTrue(scn.GetStackedCards(site2).contains(axeman));
		assertTrue(scn.GetStackedCards(site3).isEmpty());
		assertInZone(Zone.SHADOW_CHARACTERS, garrison, chieftain, assassin, troll, hound, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DiscardsTwoCardsFromHandToStackASauronOrcWhoIsNoBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetShadowCard("garrison");
		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");
		var assassin = scn.GetShadowCard("assassin");
		var troll = scn.GetShadowCard("troll");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");
		var hand3 = scn.GetShadowCard("hand3");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);

		scn.SkipToPhase(Phase.REGROUP);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		scn.MoveMinionsToTable(garrison, axeman, chieftain, assassin, troll, hound, guard);
		scn.SetShadowHand(hand1, hand2, hand3);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(garrison, "Stack a non-besieger"));
		scn.ShadowUseCardAction(garrison, "Stack a non-besieger");

		// Two cards of three.
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(hand1, hand2);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(hand1, hand2);

		// His two sites, not the sites still on the path.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3, site4));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site1);

		// The [sauron] Orcs who are no besiegers; the besiegers go by the other ability, at one card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, assassin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(garrison, axeman, troll, hound, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertInZone(Zone.STACKED, chieftain);
		assertTrue(scn.GetStackedCards(site1).contains(chieftain));
		assertTrue(scn.GetStackedCards(site2).isEmpty());
		assertInZone(Zone.SHADOW_CHARACTERS, garrison, axeman, assassin, troll, hound, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCardInHandOnlyABesiegerCanBeStackedAndWithNoneNeitherIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetShadowCard("garrison");
		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");
		var hand1 = scn.GetShadowCard("hand1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(garrison, axeman, chieftain);
		scn.SetShadowHand(hand1);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowControls(site1));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(garrison, "Stack a besieger"));
		assertFalse(scn.ShadowActionAvailable(garrison, "Stack a non-besieger"));

		// His one card and his one site are taken unasked.
		scn.ShadowUseCardAction(garrison, "Stack a besieger");
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(hand1);
		scn.ShadowChooseCard(axeman);
		assertTrue(scn.GetStackedCards(site1).contains(axeman));
		assertInZone(Zone.SHADOW_CHARACTERS, garrison, chieftain);

		// The Garrison and the Chieftain are still there to be stacked, but nothing is left to pay with.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(garrison, "Stack a besieger"));
		assertFalse(scn.ShadowActionAvailable(garrison, "Stack a non-besieger"));
	}

	@Test
	public void WithNoSiteOfHisOwnTheTwoCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetShadowCard("garrison");
		var chieftain = scn.GetShadowCard("chieftain");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");

		scn.StartGame();
		scn.SkipToSite(2);
		// The one site controlled is the Free Peoples player's.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(garrison, chieftain);
		scn.SetShadowHand(hand1, hand2);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(garrison, "Stack a non-besieger"));
		scn.ShadowUseCardAction(garrison, "Stack a non-besieger");

		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(hand1, hand2);
		assertInZone(Zone.SHADOW_CHARACTERS, garrison, chieftain);
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSauronOrcWhoIsNoBesiegerInPlayTheTwoCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetShadowCard("garrison");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);

		scn.SkipToPhase(Phase.REGROUP);
		// The Garrison is a besieger, so he is not what the second ability stacks; he is the only minion.
		scn.MoveMinionsToTable(garrison);
		scn.SetShadowHand(hand1, hand2);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(garrison, "Stack a non-besieger"));
		scn.ShadowUseCardAction(garrison, "Stack a non-besieger");

		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(hand1, hand2);
		assertInZone(Zone.SHADOW_CHARACTERS, garrison);
		assertTrue(scn.GetStackedCards(site1).isEmpty());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
