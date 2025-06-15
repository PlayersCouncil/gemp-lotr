
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("memorial", "101_22");
					put("aragorn", "1_89");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GilraensMemorialStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Gilraen’s Memorial
		* Side: Free Peoples
		* Culture: gondor
		* Twilight Cost: 1
		* Type: artifact
		* Subtype: Support Area
		* Game Text: To play, exert Aragorn.
		* 	When Aragorn dies, discard this artifact.
		* 	Fellowship: Exert a companion with the Aragorn signet to remove a burden.  Then exert Aragorn or discard this artifact.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl memorial = scn.GetFreepsCard("memorial");

		assertTrue(memorial.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, memorial.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, memorial.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, memorial.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, memorial.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(memorial, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, memorial.getBlueprint().getTwilightCost());
		//assertEquals(, memorial.getBlueprint().getStrength());
		//assertEquals(, memorial.getBlueprint().getVitality());
		//assertEquals(, memorial.getBlueprint().getResistance());
		//assertEquals(Signet., memorial.getBlueprint().getSignet());
		//assertEquals(, memorial.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void MemorialExertsAragornToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl memorial = scn.GetFreepsCard("memorial");
		scn.MoveCardsToHand(aragorn, memorial);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(memorial));
		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(memorial));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(memorial);
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void MemorialSelfDiscardsWhenAragornDies() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl memorial = scn.GetFreepsCard("memorial");
		scn.MoveCompanionToTable(aragorn);
		scn.MoveCardsToSupportArea(memorial);

		scn.StartGame();

		assertEquals(Zone.SUPPORT, memorial.getZone());
		scn.AddWoundsToChar(aragorn, 4);

		scn.PassCurrentPhaseActions();

		assertEquals(Zone.DEAD, aragorn.getZone());
		//There is a tie between evaluating a rule (threat rule?) and Gilraen's automatic trigger. Freeps chooses
		// to evaluate the rule first (it shouldn't matter for our purposes).
		scn.FreepsResolveRuleFirst();
		assertEquals(Zone.DISCARD, memorial.getZone());
	}

	@Test
	public void FellowshipActionExertsToRemoveBurdensAndExertsOrSelfDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");
		PhysicalCardImpl memorial = scn.GetFreepsCard("memorial");
		scn.MoveCompanionToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(memorial);

		scn.StartGame();

		//There is already 1 burden from bidding, we add enough for 2 actions
		scn.AddBurdens(1);

		assertTrue(scn.FreepsActionAvailable(memorial));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(memorial);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(sam));

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		String[] choices = scn.FreepsGetMultipleChoices().toArray(new String[0]);
		assertEquals(2, choices.length);
		assertEquals("Exert Aragorn", choices[0]);
		assertEquals("Discard Gilraen's Memorial", choices[1]);

		scn.FreepsChooseOption("Exert");
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsUseCardAction(memorial);
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(sam));
		scn.FreepsChooseOption("Discard");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.DISCARD, memorial.getZone());
	}
}
