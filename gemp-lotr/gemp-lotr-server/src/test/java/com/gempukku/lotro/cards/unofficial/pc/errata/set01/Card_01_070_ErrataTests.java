package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_070_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barliman", "51_70");

					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: an ally to stand beside Barliman
					put("sleep", "1_84");         // Sleep, Caradhras: [gandalf] Spell event
					put("wizard", "1_78");        // Mysterious Wizard: [gandalf] Spell event, left in the draw deck
					put("servant", "1_83");       // Servant of the Secret Fire: [gandalf] Spell event, the other player's in his discard pile
					put("chill", "1_134");        // Saruman's Chill: [isengard] Spell condition, a Shadow card
					put("folly", "1_77");         // Let Folly Be Our Cloak: [gandalf] event, no Spell
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
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 1
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: <b>Fellowship:</b> Exert Barliman Butterbur to take a spell into hand from your discard pile.
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
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void FellowshipActionExertsBarlimanToTakeASpellFromYourDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var celeborn = scn.GetFreepsCard("celeborn");
		var sleep = scn.GetFreepsCard("sleep");
		var wizard = scn.GetFreepsCard("wizard");
		var chill = scn.GetFreepsCard("chill");
		var folly = scn.GetFreepsCard("folly");
		var shadowServant = scn.GetShadowCard("servant");
		scn.MoveAlliesToTable(barliman, celeborn);
		// A [gandalf] spell event and an [isengard] spell condition of his own; a [gandalf] event that is no
		// spell beside them; a spell left in his draw deck; and a spell in the other player's discard pile, each
		// of a title of its own, since a choice from a pile lists its cards by blueprint.
		scn.MoveCardsToDiscard(sleep, chill, folly, shadowServant);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(barliman));

		scn.FreepsUseCardAction(barliman);
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, chill));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(folly, wizard, shadowServant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chill);

		assertInHand(chill);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(sleep, folly, shadowServant);
		assertInZone(Zone.DECK, wizard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoSpellInTheDiscardPileItIsStillOfferedAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var folly = scn.GetFreepsCard("folly");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(folly);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetFreepsHandCount());
		// The action has a cost, so it is offered whatever the effect can find (Q-0034).
		assertTrue(scn.FreepsActionAvailable(barliman));

		scn.FreepsUseCardAction(barliman);

		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(folly);
		assertInZone(Zone.DECK, wizard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileBarlimanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(sleep);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(barliman));

		scn.RemoveWoundsFromChar(barliman, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
