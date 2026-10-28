package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cirion", "7_82");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: another [gondor] Man, strength 8; a maneuver ability used only on purpose

					// Man the Walls: [gondor] support condition, cost 1, nothing to play it; cards to play from hand
					put("walls1", "7_111");
					put("walls2", "7_111");
					put("walls3", "7_111");
					// Cards only held in hand
					put("legolas", "1_50");         // Legolas, Greenleaf
					put("gandalf", "1_72");         // Gandalf, Friend of the Shirefolk
					put("boromir", "1_97");         // Boromir, Son of Denethor
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Cirion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you have only 4 or 5 cards in hand, Cirion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cirion");

		assertEquals("Cirion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void StrengthIsPlusTwoOnlyWhileYourHandHoldsFourOrFiveCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cirion = scn.GetFreepsCard("cirion");
		var aragorn = scn.GetFreepsCard("aragorn");
		var walls1 = scn.GetFreepsCard("walls1");
		var walls2 = scn.GetFreepsCard("walls2");
		var walls3 = scn.GetFreepsCard("walls3");
		scn.MoveCompanionsToTable(cirion, aragorn);

		scn.StartGame();

		scn.MoveCardsToHand(walls1, walls2, walls3, scn.GetFreepsCard("legolas"), scn.GetFreepsCard("gandalf"),
				scn.GetFreepsCard("boromir"));
		// The other player's hand holds 4 throughout: it is not "your" hand.
		scn.MoveCardsToHand(scn.GetShadowCard("walls1"), scn.GetShadowCard("walls2"), scn.GetShadowCard("walls3"),
				scn.GetShadowCard("legolas"));
		scn.FreepsRefreshDecision();

		// Six cards: one too many.
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(5, scn.GetStrength(cirion));
		assertEquals(8, scn.GetStrength(aragorn));

		// Five: 5 + 2.
		scn.FreepsPlayCard(walls1);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetStrength(cirion));
		assertEquals(8, scn.GetStrength(aragorn));

		// Four: 5 + 2.
		scn.FreepsPlayCard(walls2);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetStrength(cirion));
		assertEquals(8, scn.GetStrength(aragorn));

		// Three: one too few, with the other player still holding 4.
		scn.FreepsPlayCard(walls3);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(5, scn.GetStrength(cirion));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
