package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_333_Tests
{

	// The King path with Sleeping Quarters as its site 1. Site 2 is King's Tent 7_335 (Shadow number 1), site 3
	// The Dimholt 8_117 (Shadow number 1).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Sméagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.KingSites);
					put("site1", "7_333");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SleepingQuartersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sleeping Quarters
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: When the fellowship moves from this site, the Free Peoples player may spot 3 Hobbits to remove (2).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Sleeping Quarters", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingOnWithThreeHobbitsTheFreePeoplesPlayerMayRemoveTwoAndTheNextMoveAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quarters = scn.GetFreepsSite(1);
		// Frodo, the Ring-bearer, a [gondor] Hobbit companion and a Hobbit ally.
		scn.MoveCompanionsToTable("pippin");
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();
		assertSame(quarters, scn.GetCurrentSite());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(quarters));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 - 2 + 1 for King's Tent + 2 companions
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move from site 2: Sleeping Quarters is behind the fellowship, and nothing is asked.
		scn.SkipToMovementDecision();
		int pool = scn.GetTwilight();
		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// + 1 for The Dimholt + 2 companions
		assertEquals(pool + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quarters = scn.GetFreepsSite(1);
		scn.MoveCompanionsToTable("pippin");
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(quarters));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 + 1 for King's Tent + 2 companions
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoHobbitsAndAShireCompanionWhoIsNoHobbitNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("smeagol");
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 + 1 for King's Tent + 2 companions
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeHobbitsAndNothingInThePoolNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("pippin");
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 0 + 1 for King's Tent + 2 companions
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnHeIsTheOneOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quarters = scn.GetFreepsSite(1);
		// The first player has three Hobbits too: the site owner, and the Shadow player on the second turn.
		scn.MoveCompanionsToTable("pippin");
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();

		// The second player's turn: his fellowship stands on the first player's Sleeping Quarters.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertSame(quarters, scn.GetCurrentSite());
		scn.MoveCompanionsToTable(scn.GetShadowCard("pippin"));
		scn.MoveAlliesToTable(scn.GetShadowCard("bounder"));
		scn.SetTwilight(3);
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(quarters));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 - 2 + 1 for King's Tent + 2 companions
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
