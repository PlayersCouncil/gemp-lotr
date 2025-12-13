package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_053_Tests
{

// ----------------------------------------
// HONOR OF THE DESERT WARRIOR TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("honor", "103_53");       // Honor of the Desert Warrior
					put("explorer", "4_250");     // Southron Explorer
					put("southron", "4_222");     // Desert Warrior - another Southron
					put("orc", "1_271");          // Orc Soldier - non-Southron minion

					put("aragorn", "1_89");       // Unbound companion
					put("sam", "1_311");          // Ring-bound companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HonoroftheDesertWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Honor of the Desert Warrior
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Exert a Southron and hinder all other minions to assign that Southron to an unbound companion.
		 * 		The Free Peoples player may make that Southron strength +3, <b>ambush (4)</b>, and <b>fierce</b> until
		 * 		the regroup phase to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("honor");

		assertEquals("Honor of the Desert Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void HonorForcesAssignmentWhenFreepsAllowsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honor = scn.GetShadowCard("honor");
		var explorer = scn.GetShadowCard("explorer");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam"); // Ring-bound, won't be a valid target
		scn.MoveCardsToHand(honor);
		scn.MoveMinionsToTable(explorer, orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int explorerWoundsBefore = scn.GetWoundsOn(explorer);
		assertFalse(scn.IsHindered(orc));

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(honor);

		// Southron is the only option, and Aragorn is the only unbound companion

		assertEquals(explorerWoundsBefore + 1, scn.GetWoundsOn(explorer));
		assertTrue(scn.IsHindered(orc)); // Other minions hindered

		// Freeps offered prevention
		assertTrue(scn.FreepsDecisionAvailable("Would you like to make"));
		scn.FreepsChooseNo(); // Allow the assignment

		// Explorer assigned to Aragorn
		assertTrue(scn.IsCharAssignedAgainst(aragorn, explorer));

		// No buffs since FP didn't prevent
		assertFalse(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
	}

	@Test
	public void HonorBuffsSouthronWhenFreepsPreventsAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honor = scn.GetShadowCard("honor");
		var explorer = scn.GetShadowCard("explorer");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(honor);
		scn.MoveMinionsToTable(explorer, orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int explorerStrengthBefore = scn.GetStrength(explorer);
		assertFalse(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(explorer, Keyword.AMBUSH));

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(honor);

		// Freeps prevents
		scn.FreepsChooseYes();

		// Assignment prevented
		assertFalse(scn.IsCharAssignedAgainst(aragorn, explorer));

		// But Explorer is now buffed
		assertEquals(explorerStrengthBefore + 3, scn.GetStrength(explorer));
		assertTrue(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertEquals(4, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
	}
}
