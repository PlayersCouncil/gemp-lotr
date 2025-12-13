package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_049_Tests
{

// ----------------------------------------
// DESERT WIND STALKER TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalker1", "103_49");    // Desert Wind Stalker
					put("stalker2", "103_49");    // Second copy (unique 2)
					put("initiate", "103_47");    // Desert Wind Initiate - Ambush 3
					put("southron", "4_222");     // Desert Warrior - Southron without ambush
					put("orc", "1_271");          // Orc Soldier - not Southron

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindStalkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Stalker
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker. 
		* 	When you play this minion, remove (3) or hinder this minion.
		* 	Southrons gain <b>ambush (1)</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stalker1");

		assertEquals("Desert Wind Stalker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}



	@Test
	public void DesertWindStalkerPayTaxOrHinderOnPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(stalker);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowPlayCard(stalker);
		scn.ShadowChoose("Remove");

		// 3 to play + 2 roaming + 3 tax = 8 total
		assertEquals(twilightBefore - 8, scn.GetTwilight());
		assertFalse(scn.IsHindered(stalker));
	}

	@Test
	public void DesertWindStalkerGrantsAmbushToSouthronsIncludingSelf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var southron = scn.GetShadowCard("southron");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(southron, orc);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stalker);

		scn.StartGame();

		// Before Stalker - no ambush
		assertFalse(scn.HasKeyword(southron, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(orc, Keyword.AMBUSH));

		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(stalker);
		scn.ShadowChoose("Remove"); // Stay active

		// Stalker grants ambush (1) to all Southrons
		assertTrue(scn.HasKeyword(stalker, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(stalker, Keyword.AMBUSH));

		assertTrue(scn.HasKeyword(southron, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(southron, Keyword.AMBUSH));

		// Orc is not Southron - no ambush
		assertFalse(scn.HasKeyword(orc, Keyword.AMBUSH));
	}

	@Test
	public void DesertWindStalkerAmbushBonusStacksWithPrintedAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(initiate);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stalker);

		scn.StartGame();

		// Initiate has printed Ambush (3)
		assertEquals(3, scn.GetKeywordCount(initiate, Keyword.AMBUSH));

		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(stalker);
		scn.ShadowChoose("Remove");

		// Initiate now has Ambush (3) + (1) from Stalker = Ambush (4)
		assertEquals(4, scn.GetKeywordCount(initiate, Keyword.AMBUSH));
	}

	@Test
	public void DesertWindStalkerMultipleCopiesStackAmbushBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker1 = scn.GetShadowCard("stalker1");
		var stalker2 = scn.GetShadowCard("stalker2");
		var southron = scn.GetShadowCard("southron");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(southron);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stalker1, stalker2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(stalker1);
		scn.ShadowChoose("Remove");

		assertEquals(1, scn.GetKeywordCount(southron, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(stalker1, Keyword.AMBUSH));

		scn.ShadowPlayCard(stalker2);
		scn.ShadowChoose("Remove");

		// Two Stalkers = Ambush (2) on all Southrons
		assertEquals(2, scn.GetKeywordCount(southron, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(stalker1, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(stalker2, Keyword.AMBUSH));
	}

	@Test
	public void DesertWindStalkerHinderedDoesNotGrantAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stalker = scn.GetShadowCard("stalker1");
		var southron = scn.GetShadowCard("southron");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveMinionsToTable(southron);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stalker);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(stalker);
		scn.ShadowChoose("Hinder"); // Deploy hindered

		assertTrue(scn.IsHindered(stalker));

		// Hindered Stalker loses its modifier text - no ambush bonus
		assertFalse(scn.HasKeyword(southron, Keyword.AMBUSH));
	}
}
