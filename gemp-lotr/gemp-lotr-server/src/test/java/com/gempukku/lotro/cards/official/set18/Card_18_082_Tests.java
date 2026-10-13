package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_82");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Free Peoples player's draw deck is his copy of every card named here: three Free Peoples cards of
	// three types, and his own Grond, a Shadow card.
	protected VirtualTableScenario GetRegroupMoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grond", "18_82");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion
					put("armor", "1_8");         // Dwarven Armor: [dwarven] possession
					put("sleep", "1_84");        // Sleep, Caradhras: [gandalf] Fellowship event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Grond, Forged With Black Steel
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: To play, spot an [orc] minion.<br>Each time the fellowship moves in the regroup phase, you may discard this possession to search the Free Peoples player's draw deck and choose 2 Free Peoples cards found there. Remove those cards from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Grond", card.getBlueprint().getTitle());
		assertEquals("Forged With Black Steel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveLetsGrondBeDiscardedToRemoveTwoFreePeoplesCardsInTheFreePeoplesDrawDeckFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupMoveScenario();

		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);

		// All four are left in the Free Peoples player's draw deck.
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var hisGrond = scn.GetFreepsCard("grond");

		scn.StartGame();

		// The move of the fellowship phase does not offer it.
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grond));

		assertInZone(Zone.SUPPORT, grond);
		assertEquals(4, scn.GetFreepsDeckCount());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(grond);

		// The Shadow player, and nobody else, is shown that deck, the Shadow card in it with the rest.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(hisGrond, false));
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// He then chooses exactly two Free Peoples cards from it, of any type; the Shadow card is not offered.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, armor, sleep));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hisGrond));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(aragorn, sleep);
		assertInZone(Zone.REMOVED, aragorn, sleep);
		assertInZone(Zone.DECK, armor, hisGrond);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// The deck that was looked through is shuffled once more; the Shadow player's own was never looked at.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
