package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_202_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whelp", "7_202");
					put("whelp2", "7_202");
					// Morgul Creeper: a [wraith] Orc, strength 8; its text needs 3 burdens
					put("creeper", "8_75");
					// Goblin Marksman: an archer minion with no other text, strength 7; adds 1 to the minion archery total
					put("marksman", "1_176");

					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; his abilities are not used
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulWhelpStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Whelp
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 3
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: While you can spot a wound on the Ring-bearer, this minion is strength +6.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("whelp");

		assertEquals("Morgul Whelp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhelpIsStrengthPlusSixOnceTheRingBearerTakesAWoundAndNotForAWoundOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whelp = scn.GetShadowCard("whelp");
		var whelp2 = scn.GetShadowCard("whelp2");
		var creeper = scn.GetShadowCard("creeper");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 1);
		scn.MoveMinionsToTable(whelp, whelp2, creeper, marksman);

		// A wound on Sam, who is Ring-bound but not the Ring-bearer, is not a wound on the Ring-bearer.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetStrength(whelp));
		assertEquals(3, scn.GetStrength(whelp2));
		assertEquals(8, scn.GetStrength(creeper));

		// The Marksman's archery wound, placed on Frodo.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Each Whelp is 3 + 6 for itself, not +12 for the two of them; the Creeper is untouched.
		assertEquals(9, scn.GetStrength(whelp));
		assertEquals(9, scn.GetStrength(whelp2));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(7, scn.GetStrength(marksman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
