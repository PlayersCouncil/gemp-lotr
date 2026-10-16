package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_102");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("repairs", "7_102");
					// Three [gondor] fortifications of three blueprints, each played to the support area; their
					// abilities are never used.
					put("citadel", "5_32");    // Citadel of the Stars: for the hand
					put("wall", "5_33");       // City Wall, cost 1: for the discard pile
					put("first", "7_92");      // First Level: for the discard pile
					// Three more cards, to make the hand 4
					put("guard", "1_7");       // Dwarf Guard: [dwarven] companion
					put("armor", "1_8");       // Dwarven Armor: [dwarven] possession
					put("axe", "1_9");         // Dwarven Axe: [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HastyRepairsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hasty Repairs
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 3 [gondor] Men.<br><b>Regroup:</b> While you have only 4 cards in hand, you may discard 2 cards from hand to play a [gondor] fortification from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Hasty Repairs", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheFortificationPlayedIsNotOneOfTheCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var repairs = scn.GetFreepsCard("repairs");
		var citadel = scn.GetFreepsCard("citadel");
		var wall = scn.GetFreepsCard("wall");
		var first = scn.GetFreepsCard("first");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCardsToSupportArea(repairs);
		scn.MoveCardsToHand(citadel, guard, armor, axe);
		scn.MoveCardsToDiscard(wall, first);

		scn.StartGame();
		assertInHand(citadel, guard, armor, axe);
		assertInDiscard(wall, first);
		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(citadel, guard, armor, axe);
		assertInDiscard(wall, first);
		assertEquals(4, scn.GetFreepsHandCount());
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsActionAvailable(repairs));
		scn.FreepsUseCardAction(repairs);
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(citadel, guard);
		assertInDiscard(citadel, guard);
		assertInHand(armor, axe);

		// The two fortifications that lay in the discard pile before the cost was paid, and not the one that lies
		// there now because it paid for it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.FreepsHasCardChoiceAvailable(wall, first));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(citadel));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(wall);

		assertInZone(Zone.SUPPORT, wall);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInDiscard(citadel, first, guard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void HastyRepairsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
