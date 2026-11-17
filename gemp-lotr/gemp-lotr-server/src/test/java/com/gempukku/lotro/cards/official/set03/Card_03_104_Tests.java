package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tower", "3_104");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability never used
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2; damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					// Cards for the Free Peoples hand, one of each kind.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("sleep", "1_84");       // Sleep, Caradhras: an event
					put("runner", "1_178");     // Goblin Runner: a Shadow card
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession in play, on Frodo; no text but its bearer
					put("troll", "1_165");      // Cave Troll of Moria: a card for the Shadow hand only
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
		 * Name: Tower of Barad-dûr
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert a [sauron] minion. Plays to your support area.
		 * 	Each time the fellowship moves, the Free Peoples player must discard a card from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tower");

		assertEquals("Tower of Barad-dûr", card.getBlueprint().getTitle());
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
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(tower);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, troop);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(tower));
		scn.ShadowPlayCard(tower);

		// A [sauron] Orc and a [sauron] Uruk-hai are offered; the [moria] Orc is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, tower);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(tower);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, troop);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(sentry, 1);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The [moria] Orc beside it could exert, but is not [sauron].
		assertTrue(scn.IsExhausted(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(tower));

		// A [sauron] minion able to exert makes it playable.
		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(tower));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheFellowshipMovesTheFreePeoplesPlayerDiscardsACardOfHisChoiceFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var tower = scn.GetShadowCard("tower");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var runner = scn.GetFreepsCard("runner");
		var sword = scn.GetFreepsCard("sword");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(tower);
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.MoveCardsToHand(aragorn, armor, sleep, runner, troll);
		assertInHand(aragorn, armor, sleep, runner, troll);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.ATTACHED, sword);
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPass();

		// A companion, a possession, an event and a Shadow card in his hand are offered; the possession in play and
		// the Shadow player's card are not.  He must discard one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, armor, sleep, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, troll));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sleep);

		assertInDiscard(sleep);
		assertInHand(aragorn, armor, runner);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.ATTACHED, sword);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRegroupMoveOfTheSameTurnDiscardsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCardsToSupportArea(tower);

		scn.StartGame();
		scn.MoveCardsToHand(armor, sleep);
		assertInHand(armor, sleep);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsPass();
		scn.FreepsChooseCard(armor);
		assertInDiscard(armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.MoveCardsToHand(aragorn);
		assertInHand(aragorn, sleep);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sleep));
		scn.FreepsChooseCard(aragorn);

		assertInDiscard(aragorn, armor);
		assertInHand(sleep);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");

		scn.StartGame();
		scn.FreepsPass();
		// Put down after the turn's first move, which it would otherwise answer.
		scn.MoveCardsToSupportArea(tower);
		assertInZone(Zone.SUPPORT, tower);

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.SetFreepsHand(armor, sleep);
		assertEquals(2, scn.GetFreepsHandCount());
		int discards = scn.GetFreepsDiscardCount();

		scn.FreepsChooseToStay();
		// He is asked to reconcile, with his hand as it was.
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertInHand(armor, sleep);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(discards, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetMoveCount());
	}

	@Test
	public void WithNoCardInTheFreePeoplesHandTheMoveAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowCard("tower");
		scn.MoveCardsToSupportArea(tower);
		assertInZone(Zone.SUPPORT, tower);

		scn.StartGame();
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsPass();
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
