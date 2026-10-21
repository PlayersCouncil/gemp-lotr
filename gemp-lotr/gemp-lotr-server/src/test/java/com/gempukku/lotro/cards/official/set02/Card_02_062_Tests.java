package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pursuer1", "2_62");
					put("pursuer2", "2_62");
					// Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinPursuerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Goblin Pursuer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: If the fellowship has moved more than once this turn, this minion is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pursuer1");

		assertEquals("Goblin Pursuer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachPursuerIsStrengthPlusThreeOnceTheFellowshipHasMovedTwiceThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer1 = scn.GetShadowCard("pursuer1");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass(); // the fellowship's first move of the turn, to site 2
		scn.MoveMinionsToTable(pursuer1, pursuer2, troop);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(6, scn.GetStrength(pursuer1));
		assertEquals(6, scn.GetStrength(pursuer2));
		assertEquals(13, scn.GetStrength(troop));

		// Nobody is assigned, so the minions are still in play when the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(2, scn.GetMoveCount());
		// Each copy is +3 for itself alone (two copies giving each other +3 would read 12);
		// the other [moria] Orc is untouched.
		assertEquals(9, scn.GetStrength(pursuer1));
		assertEquals(9, scn.GetStrength(pursuer2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
