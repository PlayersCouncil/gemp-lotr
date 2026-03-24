package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_056_Tests
{

// ----------------------------------------
// SANDCRAFT TRAP TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trap", "103_56");        // Sandcraft Trap
					put("whisper", "103_50");     // Desert Wind Whisper - Tracker
					put("stalker", "103_49");     // Desert Wind Stalker - Tracker, grants ambush
					put("initiate", "103_47");    // Desert Wind Initiate - Tracker, Ambush (3)
					put("scout", "103_48");       // Desert Wind Scout - Tracker, Ambush (2)
					put("southron", "4_222");     // Desert Warrior - Southron but NOT Tracker
					put("southron2", "4_222");     // Desert Warrior - Southron but NOT Tracker
					put("orc", "1_271");          // Orc Soldier - not Tracker, no ambush
					put("isengard_tracker", "4_193");
					put("raider_tracker", "103_47");
					put("ambush_southron", "4_252"); //Southron Scout - Ambush (2)
					put("ambush_horror", "14_13");
					put("runner", "1_178");

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SandcraftTrapStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sandcraft Trap
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Restore all trackers and minions with ambush.  If you restored any, make each Southron gain ambush (1) until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trap");

		assertEquals("Sandcraft Trap", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SandcraftTrapRestoresTrackersAndNativeAmbushMinions() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var isengard_tracker = scn.GetShadowCard("isengard_tracker");
		var raider_tracker = scn.GetShadowCard("raider_tracker");
		var ambush_southron = scn.GetShadowCard("ambush_southron");
		var ambush_horror = scn.GetShadowCard("ambush_horror");
		var southron = scn.GetShadowCard("southron");
		var southron2 = scn.GetShadowCard("southron2");
		var soldier = scn.GetShadowCard("orc");
		var runner = scn.GetShadowCard("runner"); // Because if there's no unhindered minions we skip assignment

		scn.MoveCardsToHand(trap);
		scn.MoveMinionsToTable(isengard_tracker, raider_tracker, ambush_southron, ambush_horror, southron, southron2, soldier, runner);
		scn.HinderCard(isengard_tracker, raider_tracker, ambush_southron, ambush_horror, southron, soldier);

		scn.StartGame();
		
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(isengard_tracker));
		assertTrue(scn.IsHindered(raider_tracker));
		assertTrue(scn.IsHindered(ambush_southron));
		assertTrue(scn.IsHindered(ambush_southron));
		assertTrue(scn.IsHindered(ambush_horror));
		assertTrue(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(runner));

		assertTrue(scn.ShadowPlayAvailable(trap));
		scn.ShadowPlayCard(trap);

		//Trackers of all cultures restored
		assertFalse(scn.IsHindered(isengard_tracker));
		assertFalse(scn.IsHindered(raider_tracker));
		//Minions of all cultures with trap restored
		assertFalse(scn.IsHindered(ambush_southron));
		assertFalse(scn.IsHindered(ambush_horror));

		//Non-ambush southron still hindered
		assertTrue(scn.IsHindered(southron));
		//Non-tracker non-trap minion still hindered
		assertTrue(scn.IsHindered(soldier));

		// All Southrons gain Ambush +1
		// Isengard Tracker got no bonus
		assertEquals(0, scn.GetKeywordCount(isengard_tracker, Keyword.AMBUSH));
		// Horror of Harad got no bonus besides his native (1)
		assertEquals(1, scn.GetKeywordCount(ambush_horror, Keyword.AMBUSH));
		// Regular Southron: gains Ambush (1)
		assertEquals(1, scn.GetKeywordCount(southron2, Keyword.AMBUSH));
		// Ambush Southron: gains Ambush (2) + (1) = (3)
		assertEquals(3, scn.GetKeywordCount(ambush_southron, Keyword.AMBUSH));
	}

	@Test
	public void SandcraftTrapDoesNotGrantBonusIfNothingRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trap = scn.GetShadowCard("trap");
		var southron = scn.GetShadowCard("southron");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(trap);
		scn.MoveMinionsToTable(southron, orc); // No trackers, no ambush minions
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertFalse(scn.HasKeyword(southron, Keyword.AMBUSH));

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(trap);

		// Nothing to restore, so no ambush bonus
		assertFalse(scn.HasKeyword(southron, Keyword.AMBUSH));
	}

}
