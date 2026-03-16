package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "103_48");       // Desert Wind Scout
					put("initiate", "103_47");    // Desert Wind Initiate - another Raider
					put("southron", "4_222");     // Desert Warrior - another Raider
					put("orc", "1_271");          // Orc Soldier - not Raider

					put("aragorn", "1_89");
					put("gandalf", "1_364");
					put("enterhere", "103_9");    // You Cannot Enter Here - hinders Shadow card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Scout
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker.  Ambush (2).
		* 	When you play this minion, remove (3) or hinder this minion.
		* 	Each time this minion is hindered, you may hinder or restore another [raider] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout");

		assertEquals("Desert Wind Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DesertWindScoutPayTaxOrHinderOnPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(scout);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowPlayCard(scout);
		scn.ShadowChoose("Remove"); // Pay tax

		// 2 to play + 2 roaming + 3 tax = 7 total
		assertEquals(twilightBefore - 7, scn.GetTwilight());
		assertFalse(scn.IsHindered(scout));
	}

	@Test
	public void DesertWindScoutCanHinderAnotherRaiderWhenSelfHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var initiate = scn.GetShadowCard("initiate");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(scout);
		scn.MoveMinionsToTable(initiate, orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.IsHindered(initiate));
		assertFalse(scn.IsHindered(orc));

		scn.ShadowPlayCard(scout);
		scn.ShadowChoose("Hinder"); // Hinder self

		// Optional trigger to hinder another Raider
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Should only offer Raider minions, not the Orc
		assertTrue(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(initiate));
		assertFalse(scn.IsHindered(orc));
	}

	@Test
	public void DesertWindScoutTriggerFiresWhenHinderedByOpponent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var southron = scn.GetShadowCard("southron");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enterhere = scn.GetFreepsCard("enterhere");
		scn.MoveMinionsToTable(scout, southron);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enterhere);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(scout));
		assertFalse(scn.IsHindered(southron));

		// Freeps hinders Scout
		scn.FreepsPlayCard(enterhere);
		scn.FreepsChooseCard(scout);

		// Scout's trigger fires - can drag Southron with it
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// Southron auto-selected as only other Raider minion

		assertTrue(scn.IsHindered(scout));
		assertTrue(scn.IsHindered(southron));
	}

	@Test
	public void DesertWindScoutCanDeclineToHinderAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(scout);
		scn.MoveMinionsToTable(initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(scout);
		scn.ShadowChoose("Hinder");

		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.IsHindered(scout));
		assertFalse(scn.IsHindered(initiate)); // Not dragged along
	}

	@Test
	public void DesertWindScoutCanRestoreAnotherRaiderWhenHindered() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata changes: when hindered, offers choice of hinder OR restore another [raider] minion
		// (original only offered hinder)
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var initiate = scn.GetShadowCard("initiate");
		var southron = scn.GetShadowCard("southron");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(scout, initiate);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(southron);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// First play the Initiate and hinder it (so we have a hindered raider to restore)
		scn.ShadowPlayCard(initiate);
		scn.ShadowChoose("Hinder");
		assertTrue(scn.IsHindered(initiate));

		// Now play the Scout and choose to hinder self
		scn.ShadowPlayCard(scout);
		scn.ShadowChoose("Hinder");

		// The AboutToHinder trigger fires - offering choice of hinder or restore
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Errata: choose to restore the hindered initiate, or hinder the other southron
		scn.ShadowChoose("Restore");

		// Initiate should now be restored
		assertFalse(scn.IsHindered(initiate));
		// Scout should still be hindered
		assertTrue(scn.IsHindered(scout));
	}
}
