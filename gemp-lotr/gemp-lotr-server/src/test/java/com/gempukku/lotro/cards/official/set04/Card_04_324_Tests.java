package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_324_Tests
{

	// The Towers path with Eastemnet Downs as its site 1.  Site 2 is Eastfold (a Shadow action that needs 3
	// [isengard] trackers) and site 3 Barrows of Edoras (a Fellowship action used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a Maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a Skirmish ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally; an Archery ability only
					// Hobbit Sword: a possession with no text but its bearer, the cards there are to discard
					put("sword1", "1_299");
					put("sword2", "1_299");
					put("sword3", "1_299");
					put("sword4", "1_299");
					put("sword5", "1_299");
				}},
				new HashMap<>()
				{{
					put("site1", "4_324");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EastemnetDownsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Eastemnet Downs
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1T
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Each time you play a companion here, you must discard 2 cards from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Eastemnet Downs", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachCompanionPlayedHereHasHisPlayerDiscard2CardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site = scn.GetFreepsSite(1);
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var sword4 = scn.GetFreepsCard("sword4");
		var sword5 = scn.GetFreepsCard("sword5");

		scn.MoveCardsToHand(aragorn, gimli, sword1, sword2, sword3, sword4, sword5);
		scn.StartGame();

		assertEquals(site, scn.GetCurrentSite());
		assertInHand(aragorn, gimli, sword1, sword2, sword3, sword4, sword5);
		assertEquals(7, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// Exactly 2 of the 6 cards left in hand, Gimli among them.
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertEquals(6, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sword1, sword2);

		assertInDiscard(sword1, sword2);
		assertInHand(gimli, sword3, sword4, sword5);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second companion, of another culture and race, at the same site: 2 more.
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCards(sword3, sword4);

		assertInDiscard(sword3, sword4);
		assertInHand(sword5);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyOrAPossessionPlayedHereAsksForNoDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site = scn.GetFreepsSite(1);
		var frodo = scn.GetRingBearer();
		var orophin = scn.GetFreepsCard("orophin");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");

		scn.MoveCardsToHand(orophin, sword1, sword2, sword3);
		scn.StartGame();

		assertEquals(site, scn.GetCurrentSite());
		assertInHand(orophin, sword1, sword2, sword3);
		assertEquals(4, scn.GetFreepsHandCount());

		// An ally is a character, and no companion.
		scn.FreepsPlayCard(orophin);
		assertInZone(Zone.SUPPORT, orophin);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(sword1);
		assertAttachedTo(sword1, frodo);
		assertInHand(sword2, sword3);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnItIsHeWhoPlaysTheCompanionHereWhoDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site = scn.GetFreepsSite(1);
		var aragorn = scn.GetShadowCard("aragorn");
		var sword1 = scn.GetShadowCard("sword1");
		var sword2 = scn.GetShadowCard("sword2");
		var sword3 = scn.GetShadowCard("sword3");
		var p1sword1 = scn.GetFreepsCard("sword1");
		var p1sword2 = scn.GetFreepsCard("sword2");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(aragorn, sword1, sword2, sword3, p1sword1, p1sword2);

		// The second player's fellowship is at site 1, the first player's Eastemnet Downs.
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(site, scn.GetCurrentSite());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInHand(aragorn, sword1, sword2, sword3, p1sword1, p1sword2);
		int p1hand = scn.GetFreepsHandCount();
		int p1discard = scn.GetFreepsDiscardCount();
		int p2hand = scn.GetShadowHandCount();
		int p2discard = scn.GetShadowDiscardCount();

		scn.ShadowPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.ShadowDecisionAvailable("Choose cards from hand to discard"));
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(sword1, sword2);

		assertInDiscard(sword1, sword2);
		assertInHand(sword3);
		assertEquals(p2hand - 3, scn.GetShadowHandCount());
		assertEquals(p2discard + 2, scn.GetShadowDiscardCount());
		// The site's owner, who played nothing, keeps his hand.
		assertInHand(p1sword1, p1sword2);
		assertEquals(p1hand, scn.GetFreepsHandCount());
		assertEquals(p1discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
