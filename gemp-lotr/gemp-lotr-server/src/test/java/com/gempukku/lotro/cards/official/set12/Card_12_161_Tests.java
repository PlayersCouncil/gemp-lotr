package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_161_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "12_161");
					put("rider2", "12_161");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, nothing needed to play him
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a [shire] Hobbit ally, no companion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Black Rider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>This minion is strength +1 for each companion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rider");

		assertEquals("Black Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlusOneForEachCompanionAndRisesAsOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(gimli);

		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Laid in the fellowship phase to read the count change as a companion is played.
		scn.MoveMinionsToTable(rider, rider2, troop);
		scn.FreepsRefreshDecision();

		// 8 + 2 for Frodo and Aragorn; the ally and the three minions are no companions. Each Rider counts for
		// itself, and the Troop is untouched.
		assertEquals(10, scn.GetStrength(rider));
		assertEquals(10, scn.GetStrength(rider2));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(gimli);
		// 8 + 3
		assertEquals(11, scn.GetStrength(rider));
		assertEquals(11, scn.GetStrength(rider2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
