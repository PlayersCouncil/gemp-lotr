package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_104_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tower", "53_104");
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, vitality 3; Shadow: exert this minion to add (1)
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2, damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					put("filler1", "1_7");      // Dwarf Guard: cards for the Free Peoples hand
					put("filler2", "1_7");
					// More cards for the Shadow draw deck: the Shadow player draws up to 8 when he reconciles in the
					// regroup phase, and a draw at the regroup move needs cards left after that.
					put("deck1", "1_7");
					put("deck2", "1_7");
					put("deck3", "1_7");
					put("deck4", "1_7");
					put("deck5", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TowerofBaraddurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Tower of Barad-dur
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert a [sauron] minion.
		 * Each time the fellowship moves, draw 2 cards.  The Free Peoples player may discard a card from hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tower");

		assertEquals("Tower of Barad-dur", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronMinionOfEitherRaceToPlayToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(tower);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster, sentry, troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(tower));
		scn.ShadowPlayCard(tower);

		assertTrue(scn.ShadowHasCardChoiceAvailable(taskmaster, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(tower);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster, troop);
		scn.AddWoundsToChar(taskmaster, 2);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The [moria] Orc beside it could exert, but is not [sauron].
		assertTrue(scn.IsExhausted(taskmaster));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(tower));
	}

	@Test
	public void DrawsTwoEachTimeTheFellowshipMovesWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var filler = scn.GetFreepsCard("filler1");
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(filler);

		scn.StartGame();

		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPass();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move is a second move this turn.  The Shadow player has reconciled to 8 by then.
		scn.SkipToMovementDecision();
		int hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();
		assertEquals(8, hand);
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsDrawnWhenTheFellowshipStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var filler = scn.GetFreepsCard("filler1");
		scn.MoveCardsToHand(filler);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(tower);

		scn.SkipToMovementDecision();
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		scn.FreepsChooseToStay();

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}

	@Test
	public void FreePeoplesPlayerMayDiscardACardFromHandToPreventTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(filler1, filler2);

		scn.StartGame();

		int deck = scn.GetShadowDeckCount();
		scn.FreepsPass();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertTrue(scn.FreepsHasCardChoiceAvailable(filler1, filler2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(filler1);

		assertInDiscard(filler1);
		assertInHand(filler2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCardInTheFreePeoplesHandThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();

		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsPass();

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyShadowDrawDeckThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var filler = scn.GetFreepsCard("filler1");
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(filler);
		scn.MoveCardsToShadowDiscard("taskmaster", "sentry", "troop", "filler1", "filler2", "deck1", "deck2", "deck3", "deck4", "deck5");

		scn.StartGame();

		assertEquals(0, scn.GetShadowDeckCount());
		scn.FreepsPass();

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
