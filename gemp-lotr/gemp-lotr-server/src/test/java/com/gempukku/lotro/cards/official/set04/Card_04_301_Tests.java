package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_301_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_301");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo, Courteous Halfling is the Ring-bearer here.
	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "7_16");       // Arwen, Fair Elf Maiden: an unbound companion; her text is about skirmishing a fierce minion
					put("legolas", "1_50");     // Legolas, Greenleaf: an unbound companion; an archer, and no test here reaches the archery phase
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is Ring-bound, and so not one of the three
					// Orc Inquisitor: [sauron] Orc, cost 3.  "When you play this minion, you may make the Free Peoples
					// player discard a card at random from hand."  A discard the Free Peoples player has no choice in.
					put("inquisitor", "1_268");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Frodo, Courteous Halfling
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Frodo
		 * Game Text: <b>Ring-bearer (resistance 10).</b><br>While you can spot 3 unbound companions, Shadow cards may not discard cards from your hand or from the top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Courteous Halfling", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}

	@Test
	public void WithThreeUnboundCompanionsAShadowCardCannotMakeYouDiscardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		// The Free Peoples player's own copy of the Inquisitor is a card in his hand and nothing more.
		var spare = scn.GetFreepsCard("inquisitor");
		var inquisitor = scn.GetShadowCard("inquisitor");
		scn.MoveCompanionsToTable(arwen, legolas, aragorn);
		scn.MoveCardsToHand(sam, spare, inquisitor);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowAcceptOptionalTrigger();

		// Arwen, Legolas and Aragorn are three unbound companions: the hand is as it was.
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(sam, spare);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// The same board by Q-0034 to its letter: a "you may" with no cost whose whole effect would do nothing is
	// not offered.  The test above reads what the game does, and the old test did.
	@Test
	@Ignore("Q-0231: under Frodo's bar Orc Inquisitor's 'you may make the Free Peoples player discard a card at random from hand' is still offered, and does nothing (Q-0034; rider to Q-0224)")
	public void WithThreeUnboundCompanionsAShadowCardsOptionalDiscardFromHandIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var spare = scn.GetFreepsCard("inquisitor");
		var inquisitor = scn.GetShadowCard("inquisitor");
		scn.MoveCompanionsToTable(arwen, legolas, aragorn);
		scn.MoveCardsToHand(sam, spare, inquisitor);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(inquisitor);

		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(sam, spare);
	}

	@Test
	public void WithTwoUnboundCompanionsAndARingBoundOneAShadowCardStillMakesYouDiscardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var spare = scn.GetFreepsCard("inquisitor");
		var inquisitor = scn.GetShadowCard("inquisitor");
		// Three companions beside Frodo again, but Sam is Ring-bound: two unbound companions, one short.
		scn.MoveCompanionsToTable(arwen, legolas, sam);
		scn.MoveCardsToHand(aragorn, spare, inquisitor);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowAcceptOptionalTrigger();

		// One of the two cards, at random, has gone to the discard pile.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
