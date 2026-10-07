package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_102_Tests
{

	// The Towers-block path: its site 4 is White Rocks.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("morannon", "5_102");
					// Morgul Hunter: [sauron] Orc, cost 5, site 6; stronger for each companion and nothing else
					put("hunter1", "1_256");
					put("hunter2", "1_256");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "5_102");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorannonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Morannon
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>The twilight cost of the first [sauron] Orc played at site 4[T] is -3.<br><b>Shadow:</b> Spot 2 [sauron] Orcs and discard 4 cards from hand to draw 3 cards. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morannon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnlyTheFirstSauronOrcPlayedAtSite4CostsThreeLessWhateverThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var morannon = scn.GetShadowCard("morannon");
		var hunter1 = scn.GetShadowCard("hunter1");
		var hunter2 = scn.GetShadowCard("hunter2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(morannon);
		scn.MoveCardsToHand(hunter1, hunter2);
		scn.SetTwilight(20);
		// Site 3 of the Towers block: nothing is discounted there.
		assertEquals(5, scn.GetTwilightCost(hunter1));
		scn.FreepsPass();

		assertEquals(4, (int) scn.GetCurrentSite().getSiteNumber());
		int twilight = scn.GetTwilight();
		// 5 - 3 for the first [sauron] Orc played at site 4
		assertEquals(2, scn.GetTwilightCost(hunter1));
		assertEquals(2, scn.GetTwilightCost(hunter2));

		scn.ShadowPlayCard(hunter1);

		assertInZone(Zone.SHADOW_CHARACTERS, hunter1);
		// 2 to play + 2 roaming
		assertEquals(twilight - 4, scn.GetTwilight());
		// The second [sauron] Orc costs what is printed on it,
		assertEquals(5, scn.GetTwilightCost(hunter2));

		// and still does in the later phases of the turn: it is not the first played at site 4.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(5, scn.GetTwilightCost(hunter2));
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetTwilightCost(hunter2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void MorannonTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
