package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_3");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Pass of Caradhras 1_348 as site 4: a mountain (its Shadow ability needs an
	// [isengard] minion).  Site 3, the home of Thrarin (3F), is Rivendell Valley 1_341, a sanctuary and no
	// mountain.  No Dwarf ally has a home that is a mountain: each is of site 3 of the Fellowship path.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "11_3");
					put("axe2", "11_3");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; strength +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AxeofKhazaddumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Axe of Khazad-dûm
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a Dwarf.<br>While bearer is at a mountain site, he is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Axe of Khazad-dûm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void AxeofKhazaddumTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void BorneByAnAllyWhoseHomeIsNoMountainItGivesNothingWithTheFellowshipAtAMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(farin);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(farin, axe);
		scn.AttachCardsTo(thrarin, axe2);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals("Pass of Caradhras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));

		// The companion is with the fellowship, at the mountain.  Thrarin is at his home site, Rivendell Valley
		// (CR 4.2 "ally").
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
	}
}
