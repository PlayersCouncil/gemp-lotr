package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaughterer1", "10_95");
					put("slaughterer2", "10_95");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("rumil", "1_57");       // Rumil, Elven Protector: Elf ally, vitality 3; "Archery: Exert Rumil to wound an Orc."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSlaughtererStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Orc Slaughterer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Enduring</b>.
		 * For each wound on this minion, it is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slaughterer1");

		assertEquals("Orc Slaughterer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachWoundOnItMakesItStrengthTwoMoreBesideEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var slaughterer1 = scn.GetShadowCard("slaughterer1");
		var slaughterer2 = scn.GetShadowCard("slaughterer2");
		var troop = scn.GetShadowCard("troop");

		scn.MoveAlliesToTable(rumil);

		scn.StartGame();
		scn.MoveMinionsToTable(slaughterer1, slaughterer2, troop);
		scn.AddWoundsToChar(troop, 1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(slaughterer1));
		assertEquals(0, scn.GetWoundsOn(slaughterer2));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(10, scn.GetStrength(slaughterer1));
		assertEquals(10, scn.GetStrength(slaughterer2));
		// A wound on another Orc does nothing for either Slaughterer, nor does their text reach the Troop.
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsUseCardAction(rumil);
		scn.FreepsChooseCard(slaughterer1);
		assertEquals(1, scn.GetWoundsOn(slaughterer1));
		// 10 printed + 2 for enduring + 2 for its own text
		assertEquals(14, scn.GetStrength(slaughterer1));
		assertEquals(10, scn.GetStrength(slaughterer2));
		assertEquals(13, scn.GetStrength(troop));

		scn.ShadowPass();
		scn.FreepsUseCardAction(rumil);
		scn.FreepsChooseCard(slaughterer1);
		assertEquals(2, scn.GetWoundsOn(slaughterer1));
		// 10 printed + 4 for enduring + 4 for its own text
		assertEquals(18, scn.GetStrength(slaughterer1));
		assertEquals(10, scn.GetStrength(slaughterer2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
