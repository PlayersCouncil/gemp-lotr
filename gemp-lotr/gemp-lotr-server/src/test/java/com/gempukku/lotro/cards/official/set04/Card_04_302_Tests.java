package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_302_Tests
{

	// Frodo, Tired Traveller is the table's Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound by rule, cost 2; a fellowship ability used only on purpose
					put("sam2", "1_311");
					put("faramir", "4_117");    // Faramir, Son of Denethor: [gondor] Man, Ring-bound, cost 3; his text reads only skirmishes
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound [shire] Hobbit companion, cost 1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				"4_302",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Frodo, Tired Traveller
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bearer (resistance 10)</b>. <b>Ring-bound</b>.
		 * 	<b>Fellowship:</b> Play a Ring-bound companion to remove a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Tired Traveller", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void FellowshipActionPlaysARingBoundCompanionFromHandToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var faramir = scn.GetFreepsCard("faramir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(sam, faramir, merry);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertInHand(sam, faramir, merry);
		assertTrue(scn.FreepsActionAvailable(frodo));

		scn.FreepsUseCardAction(frodo);
		// Ring-bound companions of two cultures and races; not Merry, a Hobbit companion who is not Ring-bound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, faramir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(faramir);

		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertInHand(sam, merry);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyRingBoundCompanionInHandCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sam2 = scn.GetFreepsCard("sam2");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sam2, merry);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertInHand(sam2, merry);

		// Sam is unique and in play, so the second Sam cannot be played; Merry can, and is no Ring-bound companion.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sam2));
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertFalse(scn.FreepsActionAvailable(frodo));
		assertEquals(1, scn.GetBurdens());
	}
}
