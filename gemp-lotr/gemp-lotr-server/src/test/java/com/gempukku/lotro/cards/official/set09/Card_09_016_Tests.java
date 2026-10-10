package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "9_16");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glorfindel", "9_16");
					put("bow", "1_41");         // Elven Bow: twilight cost 1, here only as the card on top of the draw deck
					// Úlairë Cantëa, Lieutenant of Dol Guldur: Nazgûl, strength 10; fierce, and an ability that needs a weapon to discard
					put("cantea", "1_230");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlorfindelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Glorfindel, Revealed in Wrath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 9
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: When Glorfindel is in your starting fellowship, his twilight cost is -2.<br><b>Skirmish:</b> Reveal the top card of your draw deck. You may exert Glorfindel to make a Nazgûl he is skirmishing strength -X, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Glorfindel", card.getBlueprint().getTitle());
		assertEquals("Revealed in Wrath", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExhaustedHeStillRevealsTheTopCardAndIsNotAskedToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var glorfindel = scn.GetFreepsCard("glorfindel");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(glorfindel);

		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		// The card on top costs 1 and not 0, or a penalty wrongly given would not show in the Nazgûl's strength.
		scn.MoveCardsToTopOfDeck(bow);
		// Vitality 3: with 1 wound he can exert once more.
		scn.AddWoundsToChar(glorfindel, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(glorfindel, cantea);
		scn.FreepsResolveSkirmish(glorfindel);

		// The control: able to exert, he is asked, and exerting makes the Nazgûl 10 - 1.
		assertEquals(10, scn.GetStrength(cantea));
		scn.FreepsUseCardAction(glorfindel);
		scn.DismissRevealedCards();
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetWoundsOn(glorfindel));
		assertEquals(9, scn.GetStrength(cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// With 2 wounds he is exhausted.  The reveal has no price, so the ability is still his to use.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(glorfindel));
		scn.FreepsUseCardAction(glorfindel);

		// The top card is shown to both players.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, false));
		scn.DismissRevealedCards();

		// No question follows: he is not exerted (a third wound would have killed him) and the Nazgûl is not
		// weakened a second time (9, where another exertion would have read 9 - 1).
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.GetWoundsOn(glorfindel));
		assertInZone(Zone.FREE_CHARACTERS, glorfindel);
		assertEquals(9, scn.GetStrength(cantea));
		assertEquals(bow, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
