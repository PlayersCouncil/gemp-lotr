package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "10_16");
					put("gandalf", "1_364");

					put("two1", "1_184");
					put("two2", "1_185");
					put("one1", "1_174");
					put("three1", "1_179");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatheringWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gathering Wind
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert your Wizard to choose a number. Make each minion with that twilight cost strength -2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Gathering Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GatheringWindReducesStrengthOfChosenTwilightMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wind = scn.GetFreepsCard("wind");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(wind);
		scn.MoveCompanionsToTable(gandalf);

		var one1 = scn.GetShadowCard("one1");
		var two1 = scn.GetShadowCard("two1");
		var two2 = scn.GetShadowCard("two2");
		var three1 = scn.GetShadowCard("three1");
		scn.MoveMinionsToTable(one1, two1, two2, three1);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetStrength(one1));
		assertEquals(6, scn.GetStrength(two1));
		assertEquals(6, scn.GetStrength(two2));
		assertEquals(8, scn.GetStrength(three1));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(wind));

		scn.FreepsPlayCard(wind);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.FreepsChoose("2");

		assertEquals(5, scn.GetStrength(one1));
		assertEquals(4, scn.GetStrength(two1));
		assertEquals(4, scn.GetStrength(two2));
		assertEquals(8, scn.GetStrength(three1));
	}
}
