package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_033_Tests
{

// ----------------------------------------
// TORMENTED SPECTRE TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spectre", "103_33");     // Tormented Spectre
					put("sky1", "103_97");        // Ominous Sky - can be hindered
					put("sky2", "103_97");
					put("sky3", "103_97");
					put("orc", "1_271");          // Orc Soldier - minion that can be hindered
					put("runner", "1_178");       // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TormentedSpectreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Tormented Spectre
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 5
		 * Vitality: 2
		 * Resistance: 6
		 * Game Text: Enduring.
		* 	To play, add a threat.
		* 	This companion is strength +1 for each hindered card you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spectre");

		assertEquals("Tormented Spectre", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}



	@Test
	public void TormentedSpectreAdds1ThreatToPlayAndGainsStrengthFromHinderedCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spectre = scn.GetFreepsCard("spectre");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(spectre);
		scn.MoveCardsToSupportArea(sky1, sky2, sky3);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();

		int threatsBefore = scn.GetThreats();

		// Play Spectre - adds 1 threat
		scn.FreepsPlayCard(spectre);
		assertEquals(threatsBefore + 1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, spectre);

		// No hindered cards yet - base strength
		int baseStrength = scn.GetStrength(spectre);

		// Hinder one condition
		scn.HinderCard(sky1);
		assertEquals(baseStrength + 1, scn.GetStrength(spectre));

		// Hinder two more conditions
		scn.HinderCard(sky2, sky3);
		assertEquals(baseStrength + 3, scn.GetStrength(spectre));

		// Hinder the minion too
		scn.HinderCard(orc);
		assertEquals(baseStrength + 4, scn.GetStrength(spectre));
	}
}
