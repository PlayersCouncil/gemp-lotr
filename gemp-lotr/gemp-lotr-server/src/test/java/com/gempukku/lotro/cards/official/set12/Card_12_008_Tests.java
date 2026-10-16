package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_8");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path: site 4 is Balin's Tomb 1_343, underground (its maneuver ability needs a tale).  Thrarin's
	// home, site 3, is Rivendell Valley 1_341, a sanctuary and no more.  No Dwarf ally has a home that is
	// underground: each is of site 3 of the Fellowship path.  Aragorn fights the skirmish; the Dwarves are in none.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "12_8");
					put("charge2", "12_8");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5, resistance 6; +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F, strength 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisFathersChargeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: His Father's Charge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a Dwarf strength +1 for each of the following that is true: he is at a mountain or underground site; he has resistance 3 or more; he is bearing a possession; he is skirmishing a fierce minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("His Father's Charge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void HisFathersChargeTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void AnAllyWhoseHomeIsNotUndergroundGetsNothingForTheFellowshipUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var charge = scn.GetFreepsCard("charge");
		var charge2 = scn.GetFreepsCard("charge2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(charge, charge2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// The companion: underground, with the fellowship, and resistance 6: 5 + 2.
		scn.FreepsUseCardAction(charge);
		scn.FreepsChooseCard(farin);
		assertEquals(7, scn.GetStrength(farin));

		// Thrarin is at his home site, Rivendell Valley (CR 4.2 "ally"), and is true of nothing else listed: 4.
		scn.ShadowPass();
		scn.FreepsUseCardAction(charge2);
		scn.FreepsChooseCard(thrarin);
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
