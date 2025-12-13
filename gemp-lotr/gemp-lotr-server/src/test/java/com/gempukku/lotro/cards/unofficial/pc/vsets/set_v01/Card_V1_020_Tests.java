
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("boromir", "101_20");
					put("sam", "1_311");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("nelya", "1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Boromir, Redeemed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Boromir is strength +2 for each minion he is skirmishing.
		* 	At the start of each assignment phase, make Boromir <b>defender +1</b> if you can spot an unbound Hobbit or another companion with the Aragorn signet.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Redeemed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void BoromirIsStrengthPlus2PerMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		PhysicalCardImpl runner1 = scn.GetShadowCard("runner1");
		PhysicalCardImpl runner2 = scn.GetShadowCard("runner2");
		PhysicalCardImpl runner3 = scn.GetShadowCard("runner3");
		scn.MoveMinionsToTable(runner1, runner2, runner3);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		assertEquals(5, scn.GetStrength(boromir));
		scn.FreepsAssignToMinions(boromir, runner1);
		scn.ShadowAssignToMinions(boromir, runner2, runner3);

		scn.FreepsResolveSkirmish(boromir);

		assertEquals(11, scn.GetStrength(boromir));
	}

	@Test
	public void AssignmentActionSpotsAragornSignetToMakeBoromirDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(boromir, sam);

		PhysicalCardImpl nelya = scn.GetShadowCard("nelya");
		scn.MoveMinionsToTable(nelya);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(boromir, nelya);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
	}
}
