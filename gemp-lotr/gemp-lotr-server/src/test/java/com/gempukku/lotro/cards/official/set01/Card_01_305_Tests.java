package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_305_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toby", "1_305");
					put("toby2", "1_305");
					put("axe", "1_3");          // Axe Strike: a [dwarven] skirmish event, only ever a card in the deck or hand
					put("axe2", "1_3");
					put("axe3", "1_3");
					put("axe4", "1_3");
					put("axe5", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRuleOfFourScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toby", "1_305");
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf companion who exerts for Delving; a skirmish ability used only on purpose
					put("delving", "1_6");      // Delving: "Fellowship: Exert a Dwarf companion to draw 3 cards."
					// Elrond, Lord of Rivendell: ally, "Fellowship: Exert Elrond to draw a card"; his start-of-turn heal
					// finds no wounded ally
					put("elrond", "1_40");
					put("axe", "1_3");          // Axe Strike: a [dwarven] skirmish event, only ever a card in the deck or hand
					put("axe2", "1_3");
					put("axe3", "1_3");
					put("axe4", "1_3");
					put("axe5", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OldTobyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Old Toby
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Game Text: <b>Pipeweed</b>. Plays to your support area.<br>When you play this possession, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toby");

		assertEquals("Old Toby", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandItGoesToTheSupportAreaAndMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToHand(toby);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axe);

		assertInHand(toby);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, axe);
		assertSame(axe, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(toby);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));

		scn.FreepsAcceptOptionalTrigger();
		// The top card of the deck is drawn.
		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheDrawLeavesHandAndDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCardsToHand(toby);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axe);

		assertEquals(1, scn.GetFreepsHandCount());
		assertSame(axe, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(toby);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherOldTobyPlayedLaterDoesNotSetOffTheFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		scn.MoveCardsToHand(toby, toby2);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axe2, axe);

		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, axe, axe2);
		assertSame(axe, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(toby);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));
		scn.FreepsAcceptOptionalTrigger();
		assertInHand(axe);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());

		// The second copy offers its own draw; the first, already in play, offers none.
		scn.FreepsPlayCard(toby2);
		assertInZone(Zone.SUPPORT, toby, toby2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(toby));

		scn.FreepsChooseOptionalTrigger(toby2);
		assertInHand(axe2);
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		// One draw for the second copy, and no other offer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFourCardsDrawnThisFellowshipPhaseTheDrawIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRuleOfFourScenario();

		var toby = scn.GetFreepsCard("toby");
		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(toby, delving);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(elrond));

		// Three cards drawn by Delving (Gimli the only Dwarf companion exerts), and a fourth by Elrond.
		scn.FreepsPlayCard(delving);
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		// 2 - Delving + 3 + 1
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());

		// The Rule of 4 is spent: Old Toby's draw would draw nothing, and it is not offered (Q-0262).
		scn.FreepsPlayCard(toby);
		assertInZone(Zone.SUPPORT, toby);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
	}
}
