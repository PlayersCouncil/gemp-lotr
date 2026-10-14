package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_247_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli, Son of Glóin: a Dwarf companion, vitality 3.  His skirmish ability is not used.
					put("gimli", "1_13");
					// Grimir, Dwarven Elder: a Dwarf ally.  His fellowship ability is not used.
					put("grimir", "1_17");
					// Legolas, Greenleaf: an Elf companion.  Archery is never reached.
					put("legolas", "1_50");
					// Delving: a [dwarven] Fellowship event.  Exert a Dwarf companion to draw 3 cards.
					put("delving", "1_6");
					// Elrond, Lord of Rivendell: an Elf ally.  Fellowship: Exert Elrond to draw a card.  (At the start of
					// each of his owner's turns he heals every ally whose home is site 3: there is none wounded.)
					put("elrond", "1_40");
					// Hobbit Swords: a [shire] possession with no ability, the cards to be drawn
					put("sword1", "1_299");
					put("sword2", "1_299");
					put("sword3", "1_299");
					put("sword4", "1_299");
					// Four more, so that a deck is not emptied by the reconcile at the end of a turn
					put("sword5", "1_299");
					put("sword6", "1_299");
					put("sword7", "1_299");
					put("sword8", "1_299");
				}},
				new HashMap<>() {{
					put("guardroom", "11_247");
					// Abandoned Mine Shaft: a burden when left in the regroup phase outside region 1; never left so here
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void MoriaGuardroomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Moria Guardroom
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Underground</b>. When your fellowship moves from this site, you may spot a Dwarf to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guardroom");

		assertEquals("Moria Guardroom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void MovingOnFromTheGuardroomWithADwarfCompanionTheFellowshipMayDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);

		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");

		var guardroom = scn.GetFreepsCard("guardroom");
		var ruins = scn.GetShadowSite("site5");     // Sirannon Ruins: a river, and no other text

		scn.StartGame(guardroom);

		// Nothing is offered at the start of the turn.
		assertSame(guardroom, scn.GetCurrentSite());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The three Swords on top of the deck, the third to be drawn last.
		scn.MoveCardsToTopOfDeck(sword3, sword2, sword1);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);

		// The fellowship is still at the Guardroom as it moves from it.
		assertEquals(1, scn.FreepsGetSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(guardroom));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.HAND, sword1, sword2);
		assertInZone(Zone.DECK, sword3);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var guardroom = scn.GetFreepsCard("guardroom");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(guardroom);

		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(guardroom));
		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoDwarfOfHisOwnNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// An Elf companion, and no Dwarf of the moving player's.
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		// The other player's Gimli is on the table, and is not active on this turn.
		var shadowGimli = scn.GetShadowCard("gimli");
		scn.MoveCompanionsToTable(shadowGimli);

		var guardroom = scn.GetFreepsCard("guardroom");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(guardroom);

		assertInZone(Zone.FREE_CHARACTERS, shadowGimli);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourCardsAlreadyDrawnThisFellowshipPhaseNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var delving = scn.GetFreepsCard("delving");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(delving);

		var guardroom = scn.GetFreepsCard("guardroom");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(guardroom);

		assertEquals(1, scn.GetFreepsHandCount());

		// Gimli exerts for Delving's three cards, and Elrond exerts for a fourth: the Rule of 4 is spent.
		scn.FreepsPlayCard(delving);
		scn.FreepsUseCardAction(elrond);
		// 1 - 1 for the event + 3 + 1 drawn
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(elrond));
		int deck = scn.GetFreepsDeckCount();

		// The move is the end of the fellowship phase, and a draw there is a fifth card: nothing is offered,
		// though Gimli can still be spotted.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToTheGuardroomOffersNothingAndMovingOnFromItOnALaterTurnDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// A Dwarf ally, the only Dwarf.
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);

		var mineShaft = scn.GetFreepsSite("site2");
		// The site laid at the move is the second player's own Guardroom.
		var guardroom = scn.GetShadowSite("guardroom");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(mineShaft);

		assertEquals(0, scn.GetFreepsHandCount());

		// The move to the Guardroom: nothing is offered.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(guardroom);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(guardroom, scn.GetCurrentSite());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The first player's next turn begins at the Guardroom.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(guardroom, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		// The move on from it: the first player, whose fellowship moves, is offered, though the site is the
		// other player's card.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(guardroom));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSecondPlayerMovingOnFromTheFirstPlayersGuardroomIsOfferedTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The first player has no Dwarf.  The second player's Gimli is on the table from the start.
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		var shadowGimli = scn.GetShadowCard("gimli");
		scn.MoveCompanionsToTable(shadowGimli);

		var guardroom = scn.GetFreepsCard("guardroom");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(guardroom);

		// The first player's move: he has no Dwarf, and nobody is offered.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The second player's turn begins at site 1, the first player's Guardroom.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertSame(guardroom, scn.GetCurrentSite());

		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		// His fellowship moves from it: the offer is his, and he draws from his own deck.
		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guardroom));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(shadowHand + 2, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
