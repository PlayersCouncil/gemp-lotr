package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_301_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("proudfoot", "1_301");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; only ever a card in the deck or hand
					put("smeagol", "103_116");
					put("appetite", "1_294");   // Hobbit Appetite: a [shire] Fellowship event; never played
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession; never played
					// Pippin, Trollslayer: a Hobbit companion who is [gondor], not [shire]; never played
					put("pippin", "103_31");
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, a Shadow card in the deck
					put("sleep", "1_84");       // Sleep, Caradhras: a [gandalf] event, a Free Peoples card of another culture; never played
					put("axe", "1_3");          // Axe Strike: a [dwarven] event, the fourth card of the deck; never played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MasterProudfootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Master Proudfoot, Distant Relative of Bilbo
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: <b>Fellowship:</b> If the twilight pool has fewer than 3 twilight tokens, add (2) to reveal the top 3 cards of your draw deck. Take all [shire] cards revealed into hand and discard the rest.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("proudfoot");

		assertEquals("Master Proudfoot", card.getBlueprint().getTitle());
		assertEquals("Distant Relative of Bilbo", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void AtTwoTwilightAddsTwoToRevealTheTop3AndTakesTheShireCardsIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proudfoot = scn.GetFreepsCard("proudfoot");
		var smeagol = scn.GetFreepsCard("smeagol");
		var appetite = scn.GetFreepsCard("appetite");
		var pippin = scn.GetFreepsCard("pippin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveAlliesToTable(proudfoot);

		scn.StartGame();
		// From the top: Smeagol, Hobbit Appetite, Pippin, then Axe Strike.
		scn.MoveCardsToTopOfDeck(axe, pippin, appetite, smeagol);
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, smeagol, appetite, pippin, axe);
		assertSame(smeagol, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(proudfoot));

		scn.FreepsUseCardAction(proudfoot);
		assertEquals(4, scn.GetTwilight());
		// The top 3 are shown to each player, and the fourth card is not.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(appetite, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(axe, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(smeagol, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(appetite, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(pippin, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(axe, false));
		scn.DismissRevealedCards();

		// The [shire] cards, a companion who is no Hobbit and an event, are taken with nothing asked; the
		// Hobbit who is not [shire] is discarded.
		assertInHand(smeagol, appetite);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(pippin);
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		// A look at the top of the deck is no search of it: no shuffle follows (Q-0013).
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakesAShirePossessionAndDiscardsAShadowCardAndAFreePeoplesCardOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proudfoot = scn.GetFreepsCard("proudfoot");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetFreepsCard("runner");
		var sleep = scn.GetFreepsCard("sleep");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveAlliesToTable(proudfoot);

		scn.StartGame();
		// From the top: Goblin Runner, Hobbit Sword, Sleep Caradhras, then Axe Strike.
		scn.MoveCardsToTopOfDeck(axe, sleep, sword, runner);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, runner, sword, sleep, axe);
		assertSame(runner, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(proudfoot);
		assertEquals(2, scn.GetTwilight());
		scn.DismissRevealedCards();

		assertInHand(sword);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInDiscard(runner, sleep);
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertSame(axe, scn.GetFreepsTopOfDeck());
		// Two tokens in the pool: still fewer than 3.
		assertTrue(scn.FreepsActionAvailable(proudfoot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShireCardRevealedTheTwoAreAddedAndAllThreeDiscardedAndAtThreeItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proudfoot = scn.GetFreepsCard("proudfoot");
		var pippin = scn.GetFreepsCard("pippin");
		var runner = scn.GetFreepsCard("runner");
		var sleep = scn.GetFreepsCard("sleep");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveAlliesToTable(proudfoot);

		scn.StartGame();
		// From the top: Pippin, Goblin Runner, Sleep Caradhras, then Axe Strike: no [shire] card among the three.
		scn.MoveCardsToTopOfDeck(axe, sleep, runner, pippin);
		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, pippin, runner, sleep, axe);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(proudfoot));

		scn.FreepsUseCardAction(proudfoot);
		assertEquals(3, scn.GetTwilight());
		scn.DismissRevealedCards();

		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(pippin, runner, sleep);
		assertSame(axe, scn.GetFreepsTopOfDeck());
		// Three tokens in the pool: no longer fewer than 3, so the ability is not offered again.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(proudfoot));
	}
}
