package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_154_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier1", "1_154");
					put("soldier2", "1_154");   // not unique: each copy answers its own play
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2; Damage +1 is its whole text
					put("sam", "1_311");        // Sam, Son of Hamfast: here only a card for the draw deck
					put("guard1", "1_7");       // Dwarf Guard: here only cards for the draw deck
					put("guard2", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion, you may make the Free Peoples player discard the top card of his draw deck.
		*/

		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier1");

		assertEquals("Uruk Soldier", soldier.getBlueprint().getTitle());
		assertNull(soldier.getBlueprint().getSubtitle());
		assertFalse(soldier.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, soldier.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, soldier.getBlueprint().getCulture());
		assertEquals(CardType.MINION, soldier.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, soldier.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(soldier, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(2, soldier.getBlueprint().getTwilightCost());
		assertEquals(7, soldier.getBlueprint().getStrength());
		assertEquals(1, soldier.getBlueprint().getVitality());
		assertEquals(5, soldier.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayMakeTheFreePeoplesPlayerDiscardTheTopCardOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard1");
		var top3 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.SetFreepsDrawDeck(top1, top2, top3);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.SetTwilight(10);
		scn.FreepsPass();

		var shadowTop = scn.GetShadowTopOfDeck();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowChooseOptionalTrigger(soldier);

		// The one top card goes, and nothing is asked of the Free Peoples player.
		assertInZone(Zone.DISCARD, top1);
		assertEquals(top2, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		// Not the Shadow player's own deck.
		assertEquals(shadowTop, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.SetFreepsDrawDeck(top1, top2);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(soldier);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(top1, scn.GetFreepsTopOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyItsOwnPlaySetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var savage = scn.GetShadowCard("savage");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToHand(soldier2, savage);

		scn.StartGame();
		scn.SetFreepsDrawDeck(top1, top2);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.SetTwilight(20);
		scn.FreepsPass();
		// Put into play, not played: no offer.
		scn.MoveMinionsToTable(soldier1);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier1);

		// Another [isengard] Uruk-hai played while a Soldier is in play.
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsDeckCount());

		// The second copy played offers its own discard, and the first makes no offer.
		scn.ShadowPlayCard(soldier2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier1));
		scn.ShadowChooseOptionalTrigger(soldier2);
		assertInZone(Zone.DISCARD, top1);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyTheDiscardIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
