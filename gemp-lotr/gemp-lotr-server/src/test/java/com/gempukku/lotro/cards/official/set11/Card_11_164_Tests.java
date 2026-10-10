package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_11_164_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_164");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_7");        // Dwarf Guard and Axe Strike: the two cards in hand
					put("strike", "1_3");
					put("armor", "1_8");        // Dwarven Armor: the top card of the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				"11_164",                       // Frodo, Protected by Many, as the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Frodo, Protected by Many
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Game Text: <b>Ring-bearer</b>. <b>Ring-bound</b>.<br>At the start of the regroup phase, you may exert Frodo to make him gain <b>muster</b> until the end of the regroup phase. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Protected by Many", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
        assertTrue(card.getBlueprint().canStartWithRing());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
	}

	// CR 4.2 "muster" acts "at the start of the regroup phase", which is when Frodo gains it: the keyword he has
	// just gained must still be offered in that phase, or his text does nothing.
	@Test
	public void MusterGainedAtTheStartOfRegroupIsUsedInThatSameRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var strike = scn.GetFreepsCard("strike");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(guard, strike);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		// No minion is in play: nothing is done between the fellowship phase and the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		// Frodo's own offer, and none of muster yet: he has not the keyword.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo, "Use Muster"));
		scn.FreepsAcceptOptionalTrigger();
		// One muster character: no card or one.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, strike));

		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.HAND, strike, armor);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
