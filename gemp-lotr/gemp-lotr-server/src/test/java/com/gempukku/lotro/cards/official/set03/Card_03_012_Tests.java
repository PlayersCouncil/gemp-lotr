package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dinendal", "3_12");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a regroup phase to reach
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DinendalStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Dínendal, Silent Scout
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Archer</b>.<br><b>Regroup:</b> Exert Dínendal to remove (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dinendal");

		assertEquals("Dínendal", card.getBlueprint().getTitle());
		assertEquals("Silent Scout", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void RegroupActionExertsDinendalToRemoveTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dinendal = scn.GetFreepsCard("dinendal");
		scn.MoveAlliesToTable(dinendal);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);
		// Three in the pool, so that removing 2 is told from removing 1 and from removing all.
		scn.SetTwilight(3);

		// At site 2, far from his home at site 6: an ally's special ability is used wherever the fellowship is.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(dinendal));
		assertTrue(scn.FreepsActionAvailable(dinendal));

		scn.FreepsUseCardAction(dinendal);
		assertEquals(1, scn.GetWoundsOn(dinendal));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ExhaustedDinendalCannotUseIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dinendal = scn.GetFreepsCard("dinendal");
		scn.MoveAlliesToTable(dinendal);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(dinendal, 2);
		assertEquals(2, scn.GetWoundsOn(dinendal));
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		// Vitality 3 with 2 wounds: he cannot exert, though there is twilight to remove.
		assertEquals(2, scn.GetWoundsOn(dinendal));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dinendal));
	}
}
