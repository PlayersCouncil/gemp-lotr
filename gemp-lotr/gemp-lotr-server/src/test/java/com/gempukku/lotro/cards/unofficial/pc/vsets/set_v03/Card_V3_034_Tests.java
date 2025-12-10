package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_034_Tests
{

// ----------------------------------------
// TORMENTED WARRIOR TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "103_34");     // Tormented Warrior
					put("spectre", "103_33");     // Tormented Spectre - Wraith companion
					put("aragorn", "1_89");       // Aragorn - non-Wraith companion
					put("legolas", "1_50");       // Legolas - non-Wraith companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TormentedWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Tormented Warrior
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 6
		 * Vitality: 2
		 * Resistance: 6
		 * Game Text: Enduring.
		* 	To play, add a threat.
		* 	This companion is strength +1 for each companion in the dead pile.
		* 	This companion is strength +1 for each Wraith in the dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Tormented Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}



	@Test
	public void TormentedWarriorAdds1ThreatToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		scn.MoveCardsToHand(warrior);

		scn.StartGame();

		int threatsBefore = scn.GetThreats();

		scn.FreepsPlayCard(warrior);

		assertEquals(threatsBefore + 1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, warrior);
	}

	@Test
	public void TormentedWarriorGainsStrengthFromDeadPileWithWraithsCountingTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var spectre = scn.GetFreepsCard("spectre");   // Wraith companion
		var aragorn = scn.GetFreepsCard("aragorn");   // Non-Wraith companion
		var legolas = scn.GetFreepsCard("legolas");   // Non-Wraith companion
		scn.MoveCompanionsToTable(warrior);

		scn.StartGame();

		int baseStrength = scn.GetStrength(warrior);

		// Add non-Wraith companion to dead pile: +1 (companion only)
		scn.MoveCardsToDeadPile(aragorn);
		assertEquals(baseStrength + 1, scn.GetStrength(warrior));

		// Add another non-Wraith companion: +2 total
		scn.MoveCardsToDeadPile(legolas);
		assertEquals(baseStrength + 2, scn.GetStrength(warrior));

		// Add Wraith companion to dead pile: +2 more (companion + Wraith)
		scn.MoveCardsToDeadPile(spectre);
		assertEquals(baseStrength + 4, scn.GetStrength(warrior));
	}
}
