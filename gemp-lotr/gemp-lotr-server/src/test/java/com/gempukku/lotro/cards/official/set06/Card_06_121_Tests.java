package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "6_121");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStartingFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "6_121");
					// Three companions whose texts touch no card's cost
					put("gimli", "1_13");       // Gimli, Son of Glóin: cost 2
					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who costs 3, as Faramir is printed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Faramir, Ithilien Ranger
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>.<br>If Faramir is in your starting fellowship, his twilight cost is -1.<br><b>Fellowship:</b> Play a ranger to heal a Ring-bound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Ithilien Ranger", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void InTheStartingFellowshipFaramirCostsOneLessAndSoFitsBesideACompanionCostingTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");

		// The table is made with the first player's starting fellowship still to choose, from his draw deck.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, faramir, gimli, pippin, boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, gimli, pippin, boromir));
		// Printed 3; what he would cost to play now is 3 - 1.
		assertEquals(2, scn.GetTwilightCost(faramir));
		// While a player chooses, the game keeps the total of what he has played in the twilight pool.
		assertEquals(0, scn.GetTwilight());

		scn.FreepsChooseCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetTwilight());
		// A starting fellowship may cost 4.  2 + 3 would be 5, and Boromir is no longer offered; 2 + (3 - 1) is
		// the 4, and Faramir still is.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));

		scn.FreepsChooseCard(faramir);

		assertInZone(Zone.FREE_CHARACTERS, faramir);
		// The total stands at 4 and not at 3: Pippin's 1 no longer fits, nobody is left to offer and the choice
		// ends.  The other player is choosing now.
		assertInZone(Zone.DECK, pippin, boromir);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}
}
