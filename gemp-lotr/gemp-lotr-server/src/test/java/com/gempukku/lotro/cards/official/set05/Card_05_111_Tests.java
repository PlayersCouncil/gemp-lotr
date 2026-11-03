package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "5_111");
					// Sméagol, Poor Creature: [gollum] companion, Ring-bound, cost 0. "To play, add a burden"; a skirmish
					// ability used only on purpose
					put("poorcreature", "6_45");
					// Smeagol, Once Like Us: [shire] companion, Ring-bound, cost 1. "To play, add a burden"; an
					// assignment ability used only on purpose
					put("oncelikeus", "103_116");
					// Sméagol, Slippery Sneak: [gollum] companion, cost 0, "To play, add a burden"; laid only in the
					// Shadow player's discard pile
					put("sneak", "8_27");
					put("gollum", "5_24");         // Gollum, Nasty Treacherous Creature: [gollum] minion, a [gollum] card of another title
				}},
				VirtualTableScenario.FellowshipSites,
				"5_111",                         // Frodo, Master of the Precious as the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Frodo, Master of the Precious
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Aragorn
		 * Game Text: <b>Ring-bearer (resistance 10).</b><br><b>Fellowship:</b> Add a burden to play Sméagol from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("frodo");

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Master of the Precious", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
		// Not printed: every version of Frodo is Ring-bound by rule.
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
	}

	@Test
	public void FellowshipActionAddsABurdenToPlaySmeagolFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var poorcreature = scn.GetFreepsCard("poorcreature");
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		var gollum = scn.GetFreepsCard("gollum");
		var sneak = scn.GetShadowCard("sneak");
		scn.MoveCardsToDiscard(poorcreature, oncelikeus, gollum, sneak);

		scn.StartGame();

		assertInZone(Zone.DISCARD, poorcreature, oncelikeus, gollum, sneak);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(frodo));
		scn.FreepsUseCardAction(frodo);

		assertEquals(1, scn.FreepsGetChoiceMin());
		// A Sméagol of each culture; Gollum is of another title, and the Slippery Sneak is in the other
		// player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(poorcreature, oncelikeus));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum, sneak));
		scn.FreepsChooseCard(poorcreature);

		// Frodo's burden, and the Poor Creature's own "To play, add a burden": he is played as from hand.
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, poorcreature);
		assertInZone(Zone.DISCARD, oncelikeus, gollum, sneak);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other Sméagol cannot be played while one is in play, so the action is no longer offered.
		assertFalse(scn.FreepsActionAvailable(frodo));
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoSmeagolInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gollum = scn.GetFreepsCard("gollum");
		var sneak = scn.GetShadowCard("sneak");
		scn.MoveCardsToDiscard(gollum, sneak);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gollum, sneak);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
	}
}
