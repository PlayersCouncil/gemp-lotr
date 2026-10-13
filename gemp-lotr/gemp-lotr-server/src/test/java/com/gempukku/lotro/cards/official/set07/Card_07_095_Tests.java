package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_95");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFortificationScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stands", "7_95");
					put("citadel", "5_32");     // Citadel of the Stars: a [gondor] fortification
					put("wall", "5_33");        // City Wall: a [gondor] fortification
					put("spear", "7_108");      // Knight's Spear: a [gondor] possession, and no fortification
					put("guard", "1_7");        // Dwarf Guard: the one card of the Free Peoples draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorStillStandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gondor Still Stands
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 3 [gondor] companions.<br>While no opponent controls a site, wound a minion at the start of each regroup phase.<br><b>Regroup:</b> Discard this condition to place a [gondor] fortification from your discard pile on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gondor Still Stands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsItselfToPlaceAGondorFortificationFromYourDiscardPileOnTopOfYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFortificationScenario();

		var stands = scn.GetFreepsCard("stands");
		var citadel = scn.GetFreepsCard("citadel");
		var wall = scn.GetFreepsCard("wall");
		var spear = scn.GetFreepsCard("spear");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(stands);
		scn.MoveCardsToDiscard(citadel, wall, spear);

		scn.StartGame();
		// No minion is in play, so the wound of its own trigger at the start of the regroup phase falls on nobody.
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SUPPORT, stands);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertTrue(scn.FreepsActionAvailable(stands));

		scn.FreepsUseCardAction(stands);
		assertInZone(Zone.DISCARD, stands);
		// The two fortifications; not the [gondor] possession, nor Gondor Still Stands itself, which lies there now.
		assertTrue(scn.FreepsHasCardChoiceAvailable(citadel, wall));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spear, stands));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(citadel);

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(citadel, scn.GetFreepsTopOfDeck());
		assertEquals(guard, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DISCARD, wall, spear, stands);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
