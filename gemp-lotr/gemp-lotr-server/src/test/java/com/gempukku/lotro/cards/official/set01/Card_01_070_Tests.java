package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barliman", "1_70");

					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: an ally to stand beside Barliman
					put("folly", "1_77");         // Let Folly Be Our Cloak: [gandalf] Fellowship event, no spell
					put("wizard", "1_78");        // Mysterious Wizard: [gandalf] Skirmish event, a spell
					put("sleep", "1_84");         // Sleep, Caradhras: [gandalf] Fellowship event, left in the draw deck
					put("servant", "1_83");       // Servant of the Secret Fire: [gandalf] event, the other player's in his discard pile
					put("cart", "1_73");          // Gandalf's Cart: a [gandalf] possession, no event
					put("defiance", "1_37");      // Defiance: an [elven] Skirmish event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarlimanButterburStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Barliman Butterbur, Prancing Pony Proprietor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 1
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: <b>Fellowship:</b> Exert Barliman Butterbur to take a [gandalf] event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("barliman");

		assertEquals("Barliman Butterbur", card.getBlueprint().getTitle());
		assertEquals("Prancing Pony Proprietor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void FellowshipActionExertsBarlimanToTakeAGandalfEventFromYourDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var celeborn = scn.GetFreepsCard("celeborn");
		var folly = scn.GetFreepsCard("folly");
		var wizard = scn.GetFreepsCard("wizard");
		var sleep = scn.GetFreepsCard("sleep");
		var cart = scn.GetFreepsCard("cart");
		var defiance = scn.GetFreepsCard("defiance");
		var shadowServant = scn.GetShadowCard("servant");
		scn.MoveAlliesToTable(barliman, celeborn);
		// Two [gandalf] events of his own, of different phases, one a spell and one not; a [gandalf] card that
		// is no event and an event of another culture beside them; a [gandalf] event left in his draw deck; and
		// one in the other player's discard pile.  Each is of a title of its own, since a choice from a pile
		// lists its cards by blueprint.
		scn.MoveCardsToDiscard(folly, wizard, cart, defiance, shadowServant);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(barliman));

		scn.FreepsUseCardAction(barliman);
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(folly, wizard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(cart, defiance, sleep, shadowServant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(wizard);

		assertInHand(wizard);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(folly, cart, defiance, shadowServant);
		assertInZone(Zone.DECK, sleep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfEventInTheDiscardPileItIsStillOfferedAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var cart = scn.GetFreepsCard("cart");
		var defiance = scn.GetFreepsCard("defiance");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(cart, defiance);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetFreepsHandCount());
		// The action has a cost, so it is offered whatever the effect can find (Q-0034).
		assertTrue(scn.FreepsActionAvailable(barliman));

		scn.FreepsUseCardAction(barliman);

		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(cart, defiance);
		assertInZone(Zone.DECK, sleep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileBarlimanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var folly = scn.GetFreepsCard("folly");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(folly);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(barliman));

		scn.RemoveWoundsFromChar(barliman, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
