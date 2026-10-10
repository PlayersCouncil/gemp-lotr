package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_323_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_323");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Green Hill Country is site 1, where the first turn of the game begins; the rest is the usual Fellowship path.
	protected VirtualTableScenario GetFirstTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit of cost 1
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit of cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: no Hobbit, cost 4
				}},
				new HashMap<>() {{
					put("site1", "1_323");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreenHillCountryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Green Hill Country
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 
		 * Type: Site
		 * Subtype: 
		 * Site Number: 1
		 * Game Text: The twilight cost of each Hobbit is -1.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(1);
		var card = scn.GetFreepsCard("card");

		assertEquals("Green Hill Country", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AHobbitPlayedInTheFirstFellowshipPhaseOfTheGameCostsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFirstTurnScenario();

		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(merry, sam, aragorn);

		scn.StartGame();

		// The first fellowship phase of the game, at the first player's own site 1.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("Green Hill Country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);
		// 1 - 1
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, merry);

		scn.FreepsPlayCard(sam);
		// 2 - 1: one less, not nothing
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(aragorn);
		// No Hobbit: his 4 in full
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
