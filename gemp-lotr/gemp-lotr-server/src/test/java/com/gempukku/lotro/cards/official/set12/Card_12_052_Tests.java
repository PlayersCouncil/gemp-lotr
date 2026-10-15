package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_52");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground (its Shadow number counts mounted companions).
	// Site 3, the home of the Gondorian Merchant (3T), is Barrows of Edoras 4_337, a sanctuary and no
	// battleground.  The Merchant is the only [gondor] ally, and no ally's home is a battleground.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tireless", "12_52");
					put("tireless2", "12_52");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, resistance 6; "Knight." is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T, strength 6; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, site 4 (not roaming at site 4), no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TirelessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Tireless
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a [gondor] Man strength +1 for each of the following that is true: he or she is at a battleground site; he or she has resistance 4 or more; he or she is bearing a hand weapon; he or she is skirmishing a roaming minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Tireless", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TirelessTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void AnAllyWhoseHomeIsNoBattlegroundGetsNothingForTheFellowshipAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var tireless = scn.GetFreepsCard("tireless");
		var tireless2 = scn.GetFreepsCard("tireless2");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(tireless, tireless2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		// The companion: at the battleground, with the fellowship, and resistance 6: 6 + 2.
		scn.FreepsUseCardAction(tireless);
		scn.FreepsChooseCard(knight);
		assertEquals(8, scn.GetStrength(knight));

		// The Merchant is at his home site, Barrows of Edoras (CR 4.2 "ally"), and is true of nothing else listed: 6.
		scn.ShadowPass();
		scn.FreepsUseCardAction(tireless2);
		scn.FreepsChooseCard(merchant);
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
