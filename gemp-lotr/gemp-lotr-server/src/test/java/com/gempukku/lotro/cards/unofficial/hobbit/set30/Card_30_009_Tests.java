package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_30_009_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dwalin", "30_9");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7; a fellowship ability only
					put("runner1", "31_30");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its when-played text is not used (cheated in)
					put("runner2", "31_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DwalinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Dwalin, Brother of Balin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Maneuver:</b> Exert Dwalin to make him <b>defender +1</b> until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dwalin");

		assertEquals("Dwalin", card.getBlueprint().getTitle());
		assertEquals("Brother of Balin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ManeuverActionExertsDwalinToMakeHimAloneDefenderPlusOneEachTimeItIsUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dwalin = scn.GetFreepsCard("dwalin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(dwalin, balin);

		scn.StartGame();
		scn.MoveMinionsToTable("runner1");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetKeywordCount(dwalin, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(dwalin));
		scn.FreepsUseCardAction(dwalin);

		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(1, scn.GetKeywordCount(dwalin, Keyword.DEFENDER));
		// Another companion is not made a defender.
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The text has no limit, and a second defender +1 is added to the first (CR 4.2 "defender +X").
		scn.ShadowPass();
		scn.FreepsUseCardAction(dwalin);
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(2, scn.GetKeywordCount(dwalin, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(balin, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWhileDwalinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(dwalin);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(dwalin, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(dwalin));
		scn.MoveMinionsToTable("runner1");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dwalin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetKeywordCount(dwalin, Keyword.DEFENDER));
	}

	@Test
	public void DefenderLastsThroughTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dwalin = scn.GetFreepsCard("dwalin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(dwalin, balin);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(dwalin);
		assertEquals(1, scn.GetKeywordCount(dwalin, Keyword.DEFENDER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner1}, new PhysicalCardImpl[]{dwalin, runner2});

		// Balin's skirmish first; Dwalin's follows it.
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertTrue(scn.IsCharSkirmishingAgainst(dwalin, runner2));
		assertEquals(1, scn.GetKeywordCount(dwalin, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(dwalin, Keyword.DEFENDER));
	}
}
