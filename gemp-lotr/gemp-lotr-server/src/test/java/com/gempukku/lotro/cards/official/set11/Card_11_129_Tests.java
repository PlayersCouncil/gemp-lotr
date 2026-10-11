package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_129");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Mountain Orc, and a companion of resistance 5.
	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "11_129");
					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5, vitality 4; a fellowship ability and a response to Frodo's death
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MountainOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Mountain Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to skirmish a companion who has resistance 5 or less, exert that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Mountain Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "Each time this minion is assigned to skirmish a companion who has resistance 5 or less, exert that
	// companion": no "may", so nobody is asked (Q-0017; Q-0220: G).  The definition offered the exertion to the
	// Shadow player, who could decline it.
	// F-0463.
	@Test
	public void TheCompanionIsExertedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsAssignToMinions(sam, orc);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void MountainOrcTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}
}
