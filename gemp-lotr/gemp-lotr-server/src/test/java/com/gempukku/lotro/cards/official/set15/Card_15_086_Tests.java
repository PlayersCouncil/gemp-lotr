package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_15_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gats", "15_86");
					put("pavise", "11_94");

					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mûmak Commander, Giant Among the Swertings
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Exert Mûmak Commander twice to exert a companion twice (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gats");

		assertEquals("Mûmak Commander", card.getBlueprint().getTitle());
		assertEquals("Giant Among the Swertings", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MumakCommanderCanExertCompanionWith1Vitality() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gats = scn.GetShadowCard("gats");
		scn.MoveMinionsToTable(gats);
		scn.AttachCardsTo(gats, scn.GetShadowCard("pavise"));

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(gats));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowUseCardAction(gats);
		assertEquals(2, scn.GetWoundsOn(gats));
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(gats);
		assertEquals(4, scn.GetWoundsOn(gats));
		assertEquals(3, scn.GetWoundsOn(sam));

	}
}
