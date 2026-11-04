package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("headsman", "18_89");
					put("headsman2", "18_89");
					put("patrol", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion with no text, strength 13

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf companion
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] Dwarf companion, strength 4, vitality 2
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an [elven] Elf companion, left in play
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishHeadsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Headsman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>
		 * This minion is strength +3 for each companion in the dead pile (limit +6).
		*/

		var scn = GetScenario();

		var headsman = scn.GetFreepsCard("headsman");

		assertEquals("Orkish Headsman", headsman.getBlueprint().getTitle());
		assertNull(headsman.getBlueprint().getSubtitle());
		assertFalse(headsman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, headsman.getBlueprint().getSide());
		assertEquals(Culture.ORC, headsman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, headsman.getBlueprint().getCardType());
		assertEquals(Race.ORC, headsman.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(headsman, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(headsman, Keyword.HUNTER));
		assertEquals(5, headsman.getBlueprint().getTwilightCost());
		assertEquals(10, headsman.getBlueprint().getStrength());
		assertEquals(2, headsman.getBlueprint().getVitality());
		assertEquals(4, headsman.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus3ForEachCompanionInTheFreePeoplesDeadPileToALimitOfPlus6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var headsman = scn.GetShadowCard("headsman");
		var headsman2 = scn.GetShadowCard("headsman2");
		var patrol = scn.GetShadowCard("patrol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		var merchant = scn.GetFreepsCard("merchant");
		var theirAragorn = scn.GetShadowCard("aragorn");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(headsman, headsman2, patrol);
		assertEquals(10, scn.GetStrength(headsman));
		assertEquals(10, scn.GetStrength(headsman2));
		assertEquals(13, scn.GetStrength(patrol));

		// Not counted: an ally in the dead pile, a companion in the discard pile, a companion in the Shadow player's own
		// dead pile (the dead pile is the Free Peoples player's, Q-0131), and a companion in play.
		scn.MoveCardsToDeadPile(merchant, theirAragorn);
		scn.MoveCardsToDiscard(gimli);
		assertInZone(Zone.DEAD, merchant);
		assertInZone(Zone.DEAD, theirAragorn);
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertEquals(10, scn.GetStrength(headsman));
		assertEquals(10, scn.GetStrength(headsman2));

		scn.MoveCardsToDeadPile(aragorn);
		// 10 + 3, on each copy for itself; the other minion is untouched.
		assertEquals(13, scn.GetStrength(headsman));
		assertEquals(13, scn.GetStrength(headsman2));
		assertEquals(13, scn.GetStrength(patrol));

		// A companion of another culture and race.
		scn.MoveCardsToDeadPile(gimli);
		// 10 + 6
		assertEquals(16, scn.GetStrength(headsman));
		assertEquals(16, scn.GetStrength(headsman2));

		scn.MoveCardsToDeadPile(guard);
		assertEquals(4, scn.GetFreepsDeadCount());
		// Three companions would be +9: the limit holds each copy to +6.
		assertEquals(16, scn.GetStrength(headsman));
		assertEquals(16, scn.GetStrength(headsman2));
		assertEquals(13, scn.GetStrength(patrol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionItKillsInASkirmishMakesItStrongerBy3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var headsman = scn.GetShadowCard("headsman");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(headsman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, headsman);
		scn.FreepsResolveSkirmish(guard);

		// 10 + hunter 1 against a companion who is no hunter, nobody in the dead pile.
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(11, scn.GetStrength(headsman));
		assertEquals(4, scn.GetStrength(guard));

		// 11 against 4 is more than double: the Dwarf Guard is overwhelmed and killed.
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard);
		assertEquals(1, scn.GetFreepsDeadCount());
		// Out of the skirmish, so no hunter bonus: 10 + 3.
		assertEquals(13, scn.GetStrength(headsman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
