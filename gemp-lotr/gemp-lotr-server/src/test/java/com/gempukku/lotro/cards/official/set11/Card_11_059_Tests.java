package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_59");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Buckleberry Ferry 1_330 as site 2: a river ("While only Hobbits are in the
	// fellowship, there are no assignment and skirmish phases at Buckleberry Ferry").  The Gondorian Merchant's home
	// is site 3 of the Towers path, so he has none on this one; he is the only [gondor] ally, and no [gondor] ally
	// is a ranger.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "11_59");
					put("blade2", "11_59");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no ranger; "Knight." is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gondorian Blade
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [gondor] Man.<br>While bearer is a ranger or at a river site, he or she is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gondorian Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GondorianBladeTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void BorneByAnAllyWithNoHomeSiteOnThePathItGivesNothingWithTheFellowshipAtARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var blade = scn.GetFreepsCard("blade");
		var blade2 = scn.GetFreepsCard("blade2");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, blade);
		scn.AttachCardsTo(merchant, blade2);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Buckleberry Ferry", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));

		// The companion is with the fellowship, at the river.  The Merchant's home is on another block's path: on
		// this one he is at no site (CR 4.2 "ally").
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
	}
}
