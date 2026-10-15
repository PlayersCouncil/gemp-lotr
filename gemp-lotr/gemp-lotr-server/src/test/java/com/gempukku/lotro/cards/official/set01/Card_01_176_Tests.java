package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinMarksmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Marksman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Archer</b>. <helper>(Add 1 to the minion archery total.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marksman");

		assertEquals("Goblin Marksman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheReminderTextAddsNothingSoTheMarksmanAddsOneToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);

		// Site 2 is Ettenmoors, whose text adds nothing to archery, and no other archer is on the table: the
		// Marksman's archer keyword adds 1, and the words in brackets after it (descriptive only, CR 4.2's ruling on
		// this card) add nothing more.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
