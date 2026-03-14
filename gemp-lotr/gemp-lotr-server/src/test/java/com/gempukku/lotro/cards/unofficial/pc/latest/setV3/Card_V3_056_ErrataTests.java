package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_056_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_56");
					put("initiate", "103_47");   // Desert Wind Initiate - Southron Tracker with Ambush (3)
					put("southron1", "4_222");   // Desert Warrior - Southron (no tracker, no ambush)
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
		 * Game Text: Restore all trackers and minions with ambush.  If you restored any, make each Southron <b>ambush (1)</b> and strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

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
	public void SandcraftTrapRestoresTrackersAndGrantsSouthronsBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		// Tests: restore trackers/ambush minions, then Southrons get +1 STR and ambush (1)
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var initiate = scn.GetShadowCard("initiate");
		var southron1 = scn.GetShadowCard("southron1");
		var aragorn = scn.GetFreepsCard("aragorn");

		// Put initiate in hand to play and hinder, southron1 on table
		scn.MoveCardsToHand(card, initiate);
		scn.MoveMinionsToTable(southron1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play the Initiate and hinder it (tracker tax)
		scn.ShadowPlayCard(initiate);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.IsHindered(initiate));

		int southronStrBefore = scn.GetStrength(southron1);

		// Skip to assignment phase to play Sandcraft Trap
		scn.ShadowPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		// Play the Sandcraft Trap
		scn.ShadowPlayCard(card);

		// Initiate is a tracker and was hindered, should be restored
		assertFalse(scn.IsHindered(initiate));

		// Since we restored at least one, all Southrons get strength +1
		assertEquals(southronStrBefore + 1, scn.GetStrength(southron1));

		// Southrons should also gain ambush (1)
		assertTrue(scn.HasKeyword(southron1, Keyword.AMBUSH));
	}
}
