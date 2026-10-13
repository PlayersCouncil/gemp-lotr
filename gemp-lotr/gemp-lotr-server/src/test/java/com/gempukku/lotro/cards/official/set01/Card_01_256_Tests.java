package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_256_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunter", "1_256");
					put("hunter2", "1_256");
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
	public void MorgulHunterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Morgul Hunter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 4
		 * Site Number: 6
		 * Game Text: For each companion you can spot, this minion is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hunter");

		assertEquals("Morgul Hunter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
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

		var hunter = scn.GetShadowCard("hunter");
		var hunter2 = scn.GetShadowCard("hunter2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Laid in the fellowship phase to read the count change as a companion is played.
		scn.MoveMinionsToTable(hunter, hunter2, troop);

		// 9 + 2 for Frodo and Aragorn; the ally and the three minions are no companions. Each Hunter counts for
		// itself, and the Troop is untouched.
		assertEquals(11, scn.GetStrength(hunter));
		assertEquals(11, scn.GetStrength(hunter2));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(gimli);
		// 9 + 3
		assertEquals(12, scn.GetStrength(hunter));
		assertEquals(12, scn.GetStrength(hunter2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
