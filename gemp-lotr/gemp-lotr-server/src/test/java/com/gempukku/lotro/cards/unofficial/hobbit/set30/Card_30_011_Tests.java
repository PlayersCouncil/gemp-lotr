package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_30_011_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gloin", "30_11");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7; a fellowship ability only
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] Dwarf companion, strength 7; a maneuver ability only
					put("runner1", "31_30");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its when-played text is not used (cheated in)
					put("runner2", "31_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GloinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Glóin, Father of Gimli
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Skirmish:</b> Exert Glóin to make him strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gloin");

		assertEquals("Glóin", card.getBlueprint().getTitle());
		assertEquals("Father of Gimli", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishActionExertsGloinToMakeHimAloneStrengthPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gloin, balin);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, runner1);
		scn.FreepsResolveSkirmish(gloin);

		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(6, scn.GetStrength(gloin));
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.FreepsActionAvailable(gloin));

		scn.FreepsUseCardAction(gloin);
		assertEquals(1, scn.GetWoundsOn(gloin));
		assertEquals(9, scn.GetStrength(gloin));
		// Another companion is not strengthened.
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsOfferedOutsideHisOwnSkirmishAndTheBonusLastsOnlyThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(gloin, balin, nori);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		// Glóin is left unassigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner1}, new PhysicalCardImpl[]{nori, runner2});

		scn.FreepsResolveSkirmish(balin);
		assertFalse(scn.IsCharAssigned(gloin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(6, scn.GetStrength(gloin));
		// The text names no skirmish of his: the ability is offered while he is in none.
		assertTrue(scn.FreepsActionAvailable(gloin));

		scn.FreepsUseCardAction(gloin);
		assertEquals(1, scn.GetWoundsOn(gloin));
		assertEquals(9, scn.GetStrength(gloin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Each skirmish is a phase of its own (CR 4.2 "skirmish phase(s)"): the bonus is gone in the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(nori);
		assertTrue(scn.IsCharSkirmishingAgainst(nori, runner2));
		assertEquals(6, scn.GetStrength(gloin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileGloinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		scn.MoveCompanionsToTable(gloin);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(gloin, 2);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gloin));
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gloin, runner1);
		scn.FreepsResolveSkirmish(gloin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gloin));
		assertEquals(2, scn.GetWoundsOn(gloin));
		assertEquals(6, scn.GetStrength(gloin));
	}
}
