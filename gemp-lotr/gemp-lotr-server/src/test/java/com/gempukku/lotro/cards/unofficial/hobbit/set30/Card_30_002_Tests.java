package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_002_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balin", "30_2");

					// Three Free Peoples events of three cultures and two phases.
					put("kinsfolk", "30_20");   // To me! O my kinsfolk!: [dwarven] Fellowship event
					put("wizard", "30_23");     // A Wizard Is Never Late: [gandalf] Fellowship event
					put("invisible", "31_39");  // An Invisible Ring: [shire] Skirmish event
					// Beside them in the discard pile: a Free Peoples card that is no event, and a Shadow event.
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession
					put("host", "30_35");       // Host of Thousands: [gundabad] Shadow event
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BalinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Balin, Brother of Dwalin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Fellowship:</b> Exert Balin to shuffle a Free Peoples event from your discard pile into your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balin");

		assertEquals("Balin", card.getBlueprint().getTitle());
		assertEquals("Brother of Dwalin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertsBalinToShuffleAChosenFreePeoplesEventFromHisDiscardPileIntoHisDrawDeckUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var kinsfolk = scn.GetFreepsCard("kinsfolk");
		var wizard = scn.GetFreepsCard("wizard");
		var invisible = scn.GetFreepsCard("invisible");
		var axe = scn.GetFreepsCard("axe");
		var host = scn.GetFreepsCard("host");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToDiscard(kinsfolk, wizard, invisible, axe, host);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertInZone(Zone.DISCARD, kinsfolk, wizard, invisible, axe, host);
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsActionAvailable(balin));

		scn.FreepsUseCardAction(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		// The three Free Peoples events; not the possession, and not his own Shadow event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(kinsfolk, wizard, invisible));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, host));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(invisible);
		assertInZone(Zone.DECK, invisible);
		assertInZone(Zone.DISCARD, kinsfolk, wizard, axe, host);
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.FreepsActionAvailable(balin));

		scn.FreepsUseCardAction(balin);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(kinsfolk, wizard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, host));

		scn.FreepsChooseCardBPFromSelection(wizard);
		assertInZone(Zone.DECK, invisible, wizard);
		assertInZone(Zone.DISCARD, kinsfolk, axe, host);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());

		// Vitality 3 with 2 wounds: exhausted, with a Free Peoples event still in the discard pile.
		assertFalse(scn.FreepsActionAvailable(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesEventInHisDiscardPileBalinExertsAndNoCardLeavesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var host = scn.GetFreepsCard("host");
		scn.MoveCompanionsToTable(balin);
		// Everything that is nearly a Free Peoples event in his discard pile.
		scn.MoveCardsToDiscard(axe, host);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DISCARD, axe, host);
		assertEquals(0, scn.GetWoundsOn(balin));
		// The exertion can be paid, so the ability is on offer though there is no event to shuffle in.
		assertTrue(scn.FreepsActionAvailable(balin));

		scn.FreepsUseCardAction(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertInZone(Zone.DISCARD, axe, host);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		// A shuffle into a draw deck is made though nothing is shuffled in.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
