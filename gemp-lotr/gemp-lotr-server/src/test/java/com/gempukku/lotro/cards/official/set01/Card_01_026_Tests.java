package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_26");
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
					put("halls", "1_26");
					put("halls2", "1_26");
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
	public void TheirHallsofStoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Their Halls of Stone
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Dwarf strength +2 (or +4 if at an underground site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Their Halls of Stone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TheirHallsofStoneTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void AnAllyWhoseHomeIsNotUndergroundIsPlus2WithTheFellowshipUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var halls = scn.GetFreepsCard("halls");
		var halls2 = scn.GetFreepsCard("halls2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(halls, halls2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// The companion is with the fellowship, underground: 5 + 4.
		scn.FreepsPlayCard(halls);
		scn.FreepsChooseCard(farin);
		assertEquals(9, scn.GetStrength(farin));

		// Thrarin is at his home site, Rivendell Valley (CR 4.2 "ally"): 4 + 2.
		scn.ShadowPass();
		scn.FreepsPlayCard(halls2);
		scn.FreepsChooseCard(thrarin);
		assertEquals(6, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
