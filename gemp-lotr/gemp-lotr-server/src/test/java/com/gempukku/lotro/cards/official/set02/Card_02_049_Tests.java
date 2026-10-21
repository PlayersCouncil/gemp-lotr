package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "2_49");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7; Archer is its whole text
					put("bowman", "2_60");      // Goblin Bowman: [moria] Orc archer, strength 4; Archer is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc who is no archer, strength 5; its trigger is on being played
					// Orc Archer Troop: a [sauron] Orc archer, strength 15; its text gives fierce and an archery bonus
					// with initiative, and no strength
					put("troop", "7_294");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArcherCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Archer Commander
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.<br>Each other [moria] archer is strength +2.
		*/

		var scn = GetScenario();

		var commander = scn.GetFreepsCard("commander");

		assertEquals("Archer Commander", commander.getBlueprint().getTitle());
		assertNull(commander.getBlueprint().getSubtitle());
		assertTrue(commander.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, commander.getBlueprint().getSide());
		assertEquals(Culture.MORIA, commander.getBlueprint().getCulture());
		assertEquals(CardType.MINION, commander.getBlueprint().getCardType());
		assertEquals(Race.ORC, commander.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(commander, Keyword.ARCHER));
		assertEquals(4, commander.getBlueprint().getTwilightCost());
		assertEquals(8, commander.getBlueprint().getStrength());
		assertEquals(2, commander.getBlueprint().getVitality());
		assertEquals(4, commander.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOtherMoriaArcherIsStrengthPlus2WhileHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var marksman = scn.GetShadowCard("marksman");
		var bowman = scn.GetShadowCard("bowman");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(marksman, bowman, runner, troop);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetStrength(marksman));
		assertEquals(4, scn.GetStrength(bowman));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(15, scn.GetStrength(troop));

		scn.MoveMinionsToTable(commander);

		// Two [moria] archers of different titles; not a [moria] Orc who is no archer, nor an archer of another
		// culture, nor the Commander himself.
		assertEquals(9, scn.GetStrength(marksman));
		assertEquals(6, scn.GetStrength(bowman));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(commander));

		scn.MoveCardsToDiscard(commander);

		// With him gone, the bonus goes too.
		assertInDiscard(commander);
		assertEquals(7, scn.GetStrength(marksman));
		assertEquals(4, scn.GetStrength(bowman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
