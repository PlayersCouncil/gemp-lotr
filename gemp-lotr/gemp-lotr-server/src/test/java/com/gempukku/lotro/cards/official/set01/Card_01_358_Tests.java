package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_358_Tests
{

	// Pillars of the Kings is site 8; the rest of the path is that of FellowshipSites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability never used
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, vitality 1; a maneuver ability never used
					put("cloak", "15_68");      // Ranger's Cloak: [gondor] possession on a [gondor] Man, vitality +1, so the Merchant can carry a wound
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion card, held in hand
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition, held in hand
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, held in hand; no text but its bearer
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion, held in hand; no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site8", "1_358");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PillarsoftheKingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pillars of the Kings
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 8
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. <b>Fellowship:</b> Discard a [gondor] card from hand to heal a [gondor] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Pillars of the Kings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void FellowshipActionDiscardsAGondorCardFromHandToHealAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetFreepsCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		var pillars = scn.GetCurrentSite();
		assertEquals("Pillars of the Kings", pillars.getBlueprint().getTitle());

		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, cloak);
		scn.MoveCardsToHand(boromir, saga, sword, troop);
		// Aragorn carries two wounds, so one heal is told from two.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(merchant, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, merchant);
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsActionAvailable(pillars));

		scn.FreepsUseCardAction(pillars);

		// A [gondor] card of any type may be discarded; a [shire] card and a [moria] card may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, saga));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(saga);
		assertInZone(Zone.DISCARD, saga);
		assertInZone(Zone.HAND, boromir, sword, troop);
		assertEquals(hand - 1, scn.GetFreepsHandCount());

		// A wounded [gondor] Man and a wounded [gondor] Hobbit, both companions, may be healed;
		// the wounded [gondor] ally and the wounded [shire] Ring-bearer may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGondorCardInHandTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetFreepsCard("troop");
		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var cloak = scn.GetFreepsCard("cloak");

		scn.StartGame();
		scn.SkipToSite(8);
		var pillars = scn.GetCurrentSite();
		assertEquals("Pillars of the Kings", pillars.getBlueprint().getTitle());

		scn.MoveCompanionsToTable(aragorn);
		// Only a [shire] card and a [moria] card in hand, and a wounded [gondor] companion to heal.
		scn.MoveCardsToHand(sword, troop);
		// The [gondor] cards the turns on the way may have drawn are put out of the hand.
		scn.MoveCardsToDiscard(boromir, saga, pippin, merchant, cloak);
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.HAND, sword, troop);
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(pillars));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
