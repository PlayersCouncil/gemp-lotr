package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_276_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seeking", "1_276");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; adds (2) only when a weapon is played on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, an Orc who is not [sauron]; no text
					put("drawn", "1_272");      // Orc War Band: left in the draw deck, the card to draw
					put("spare", "1_272");      // a second Orc War Band beneath it, so that a draw of two would show
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeekingItsMasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Seeking Its Master
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	Shadow: Exert a [sauron] Orc and remove (2) to draw a card.
		*/

		var scn = GetScenario();

		var seeking = scn.GetFreepsCard("seeking");

		assertEquals("Seeking Its Master", seeking.getBlueprint().getTitle());
		assertNull(seeking.getBlueprint().getSubtitle());
		assertFalse(seeking.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, seeking.getBlueprint().getSide());
		assertEquals(Culture.SAURON, seeking.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, seeking.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(seeking, Keyword.SUPPORT_AREA));
		assertEquals(1, seeking.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityExertsASauronOrcAndRemovesTwoToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var drawn = scn.GetShadowCard("drawn");
		var spare = scn.GetShadowCard("spare");
		scn.MoveCardsToSupportArea(seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, guard, troop);
		scn.MoveCardsToTopOfDeck(spare, drawn);
		assertInZone(Zone.DECK, drawn, spare);

		scn.FreepsPass();
		// Exactly the (2) the ability removes.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(seeking));
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());

		scn.ShadowUseCardAction(seeking);
		// The exertion: either [sauron] Orc; not the [sauron] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetTwilight());
		// One card drawn, the top one; the second stays in the deck.
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertInHand(drawn);
		assertInZone(Zone.DECK, spare);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard, troop);
		// Vitality 2: one wound exhausts the only [sauron] Orc.  The Uruk-hai and the [moria] Orc could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(seeking));
	}

	@Test
	public void IsNotOfferedWithOneTwilightInThePoolAndIsWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeking = scn.GetShadowCard("seeking");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(seeking));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(seeking));
	}
}
