package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reaver", "102_3");

					put("card1", "102_4");
					put("card2", "102_5");
					put("card3", "102_6");
					put("card4", "102_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingReaverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Dunlending Reaver
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: While you have initiative, this minion is strength +3 and damage +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reaver");

		assertEquals("Dunlending Reaver", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DunlendingReaverNoBonusIfFreepsHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var reaver = scn.GetShadowCard("reaver");
		scn.MoveCardsToHand(reaver);

		scn.FreepsDrawCards(4);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowPlayAvailable(reaver));

		scn.ShadowPlayCard(reaver);

		assertEquals(8, scn.GetStrength(reaver));
		assertFalse(scn.HasKeyword(reaver, Keyword.DAMAGE));
	}

	@Test
	public void DunlendingReaverIsStrengthPlus3AndDamagePlus1IfShadowHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var reaver = scn.GetShadowCard("reaver");
		scn.MoveCardsToHand(reaver);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowPlayAvailable(reaver));

		scn.ShadowPlayCard(reaver);

		assertEquals(11, scn.GetStrength(reaver));
		assertTrue(scn.HasKeyword(reaver, Keyword.DAMAGE));
	}
}
