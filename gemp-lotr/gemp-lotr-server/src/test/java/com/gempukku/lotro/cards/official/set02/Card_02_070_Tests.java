package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("power", "2_70");
					put("balrog", "6_76");

					put("orc1", "1_178");
					put("orc2", "1_179");
					put("orc3", "1_181");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PowerandTerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Power and Terror
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Reveal any number of [moria] Orcs from your hand to play The Balrog. Its twilight cost is -2 for each Orc revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("power");

		assertEquals("Power and Terror", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PowerandTerrorPlaysTheBalrogEvenWhenOnlyPlayableViaDisount() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var balrog = scn.GetShadowCard("balrog");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		scn.MoveCardsToHand(power, balrog, orc1, orc2, orc3);

		scn.StartGame();
		scn.SetTwilight(7);

		scn.FreepsPassCurrentPhaseAction();

		// Starting 7 + 1 companion + 2 from site = 10
		assertEquals(10, scn.GetTwilight());
		assertEquals(14, balrog.getBlueprint().getTwilightCost());

		// With 3 moria orcs in hand, the maximum discount that Power and Terror can give is -6.
		// 14 - 6 + 2 (roaming) = 10, which is exactly what's on the table.  P&T should thus permit the play.
		assertTrue(scn.ShadowPlayAvailable(power));

		assertEquals(Zone.HAND, balrog.getZone());
		scn.ShadowPlayCard(power);
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(orc1, orc2, orc3);
		scn.FreepsDismissRevealedCards();
		assertEquals(Zone.SHADOW_CHARACTERS, balrog.getZone());
	}
}
