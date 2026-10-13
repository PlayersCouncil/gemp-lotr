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

public class Card_01_334_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] companion; a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: a second Dwarf companion, no text in play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability only
					put("orophin", "1_56");     // Orophin, Lórien Bowman: Elf ally, home site 6; an archery ability only

					// Dwarven Armor: possession, cost 0, bearer must be a Dwarf; changes no number
					put("armor", "1_8");
					// Sting: possession (hand weapon), cost 1, bearer must be Frodo; an ability used only on purpose
					put("sting", "1_313");
					// Mithril-coat: artifact (armor), cost 2, bearer must be Frodo; its texts need minions
					put("coat", "2_105");
					// The Saga of Elendil: condition, cost 1, bearer must be a [gondor] companion; a skirmish ability only
					put("saga", "1_114");
					// Elven Bow: possession, cost 1, bearer must be an Elf (an ally may bear it); bearer is an archer
					put("bow", "1_41");
					// Gandalf's Cart: possession, cost 1, plays to the support area; a fellowship ability used only on purpose
					put("cart", "1_73");

					// Goblin Spear: Shadow possession, cost 1, bearer must be a [moria] minion; its text needs a kill
					put("spear", "1_182");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, no text

					// Cards to draw: Goblin Patrol Troops, which do nothing in a Free Peoples hand
					put("draw1", "1_177");
					put("draw2", "1_177");
					put("draw3", "1_177");
					put("draw4", "1_177");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollshawForestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Trollshaw Forest
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Forest</b>. Each time you play a possession or artifact on your companion, draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Trollshaw Forest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void EachPossessionOrArtifactPlayedOnACompanionDrawsItsPlayerOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trollshaw = scn.GetShadowSite(2);

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var sting = scn.GetFreepsCard("sting");
		var coat = scn.GetFreepsCard("coat");
		var draw1 = scn.GetFreepsCard("draw1");
		var draw2 = scn.GetFreepsCard("draw2");
		var draw3 = scn.GetFreepsCard("draw3");
		var draw4 = scn.GetFreepsCard("draw4");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SkipToSite(2);
		// The site under the fellowship is the one the other player laid.
		assertSame(trollshaw, scn.GetCurrentSite());

		scn.SetFreepsHand(armor, sting, coat);
		// draw1 is the top card, then draw2, draw3 and draw4
		scn.MoveCardsToTopOfDeck(draw4, draw3, draw2, draw1);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowHand = scn.GetShadowHandCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, draw1);

		// A possession on a companion: one card, the top one, with nothing asked.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.HAND, draw1);
		assertInZone(Zone.DECK, draw2);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// 3 less the Armor, plus the card drawn
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second possession, on another companion: again.
		scn.FreepsPlayCard(sting);
		assertAttachedTo(sting, frodo);
		assertInZone(Zone.HAND, draw2);
		assertInZone(Zone.DECK, draw3);
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An artifact, on a companion who has had one already: again.
		scn.FreepsPlayCard(coat);
		assertAttachedTo(coat, frodo);
		assertInZone(Zone.HAND, draw3);
		assertInZone(Zone.DECK, draw4);
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsHandCount());

		// The other player draws nothing.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AConditionOnACompanionAndAPossessionOnAnAllyOrInTheSupportAreaDrawNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var saga = scn.GetFreepsCard("saga");
		var bow = scn.GetFreepsCard("bow");
		var cart = scn.GetFreepsCard("cart");
		var draw1 = scn.GetFreepsCard("draw1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Trollshaw Forest", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SetFreepsHand(saga, bow, cart);
		scn.MoveCardsToTopOfDeck(draw1);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetFreepsHandCount());

		// A condition played on a companion
		scn.FreepsPlayCard(saga);
		assertAttachedTo(saga, aragorn);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());

		// A possession played on an ally
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, orophin);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());

		// A possession played to the support area
		scn.FreepsPlayCard(cart);
		assertInZone(Zone.SUPPORT, cart);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, draw1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionTransferredToACompanionDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var draw1 = scn.GetFreepsCard("draw1");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(guard, armor);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Trollshaw Forest", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SetFreepsHand();
		scn.MoveCardsToTopOfDeck(draw1);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertAttachedTo(armor, guard);
		assertTrue(scn.FreepsTransferAvailable(armor));

		// Gimli comes to bear the Armor, but it was not played on him.
		scn.FreepsTransferCard(armor);
		assertAttachedTo(armor, gimli);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, draw1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowPossessionPlayedOnAMinionDrawsNobodyACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals("Trollshaw Forest", scn.GetCurrentSite().getBlueprint().getTitle());
		// 3 set + 1 companion + 1 for Trollshaw Forest
		assertEquals(5, scn.GetTwilight());
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(spear));

		scn.ShadowPlayCard(spear);
		assertAttachedTo(spear, troop);
		assertEquals(4, scn.GetTwilight());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
