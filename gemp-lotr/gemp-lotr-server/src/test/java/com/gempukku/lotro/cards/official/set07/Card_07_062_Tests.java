package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mine", "7_62");
					// Gollum, Nasty Treacherous Creature: the [gollum] Gollum, vitality 4; a regroup ability only
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum, vitality 4; his texts answer a Nazgûl played
					// and the assignment phase
					put("wraithgollum", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum
					put("shelob", "8_26");
					// Cards for the top of the Shadow draw deck, whose own texts do nothing there:
					put("notalone", "12_39");   // Not Alone: a [gollum] condition
					put("notalone2", "12_39");
					put("whelp", "7_202");      // Morgul Whelp: a [wraith] Orc minion
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ItsMineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: It's Mine
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Shadow:</b> Remove (1) and exert Gollum to discard the top card of your draw deck. If that card is a [gollum] or [wraith] card, take it into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mine");

		assertEquals("It's Mine", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityRemovesOneAndExertsGollumToDiscardTheTopCardAndTakesAGollumCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mine = scn.GetShadowCard("mine");
		var gollum = scn.GetShadowCard("gollum");
		var notalone = scn.GetShadowCard("notalone");
		var notalone2 = scn.GetShadowCard("notalone2");
		scn.MoveCardsToSupportArea(mine);
		scn.MoveMinionsToTable(gollum);
		// Another [gollum] card already in the discard pile: it is not the card taken.
		scn.MoveCardsToDiscard(notalone2);

		scn.StartGame();
		scn.MoveCardsToTopOfShadowDeck("notalone");
		var freepsTop = scn.GetFreepsTopOfDeck();
		assertEquals(notalone, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DISCARD, notalone2);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for the site
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));
		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowActionAvailable(mine));
		scn.ShadowUseCardAction(mine);

		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertInHand(notalone);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, notalone2);
		// The Free Peoples draw deck is not touched.
		assertEquals(freepsTop, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWraithGollumPaysAndAWraithCardIsTakenWhileASauronCardStaysDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mine = scn.GetShadowCard("mine");
		var gollum = scn.GetShadowCard("wraithgollum");
		var whelp = scn.GetShadowCard("whelp");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(mine);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();
		// The Whelp on top, the Soldier under it.
		scn.MoveCardsToTopOfShadowDeck("soldier", "whelp");
		assertEquals(whelp, scn.GetShadowTopOfDeck());
		assertEquals(soldier, scn.GetFromTopOfShadowDeck(2));
		scn.FreepsPass();

		// 0 + 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));
		int hand = scn.GetShadowHandCount();
		scn.ShadowUseCardAction(mine);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertInHand(whelp);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(soldier, scn.GetShadowTopOfDeck());

		scn.ShadowUseCardAction(mine);

		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertInZone(Zone.DISCARD, soldier);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithAnEmptyPoolOrWithGollumExhaustedThoughShelobCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mine = scn.GetShadowCard("mine");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(mine);
		scn.MoveMinionsToTable(gollum, shelob);

		scn.StartGame();
		scn.FreepsPass();

		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mine));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(mine));

		// Vitality 4 with 3 wounds: Gollum is exhausted, and Shelob is no Gollum.
		scn.AddWoundsToChar(gollum, 3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mine));
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mine = scn.GetShadowCard("mine");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(mine);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mine));
	}

	@Test
	public void WithAnEmptyDrawDeckTheAbilityIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mine = scn.GetShadowCard("mine");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(mine);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDrawDeck().size());
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));
		int discard = scn.GetShadowDiscardCount();
		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowActionAvailable(mine));
		scn.ShadowUseCardAction(mine);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
