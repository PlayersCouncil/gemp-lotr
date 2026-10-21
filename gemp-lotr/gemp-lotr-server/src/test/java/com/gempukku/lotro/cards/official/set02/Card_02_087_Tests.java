package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "2_87");

					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, vitality 2; answers only a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (an Uruk-hai), vitality 2; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text at all

					// The Shadow player's draw deck, laid exactly: seven cards, so that a draw of six could be made.
					put("deck1", "1_177");
					put("deck2", "1_177");
					put("deck3", "1_177");
					put("deck4", "1_177");
					put("deck5", "1_177");
					put("deck6", "1_177");
					put("deck7", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheEyeofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: The Eye of Sauron
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Exert a [sauron] Orc and spot X burdens to draw X cards (limit 5).
		*/

		var scn = GetScenario();

		var eye = scn.GetFreepsCard("eye");

		assertEquals("The Eye of Sauron", eye.getBlueprint().getTitle());
		assertNull(eye.getBlueprint().getSubtitle());
		assertFalse(eye.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, eye.getBlueprint().getSide());
		assertEquals(Culture.SAURON, eye.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, eye.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(eye, Timeword.SHADOW));
		assertEquals(1, eye.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcOfThePlayersChoiceAndSpotsThreeBurdensToDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetShadowCard("eye");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(eye);
		assertInHand(eye);

		scn.StartGame();
		scn.SetShadowDrawDeck(scn.GetShadowCard("deck1"), scn.GetShadowCard("deck2"), scn.GetShadowCard("deck3"),
				scn.GetShadowCard("deck4"), scn.GetShadowCard("deck5"), scn.GetShadowCard("deck6"), scn.GetShadowCard("deck7"));
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(chieftain, soldier, guard, troop);
		assertEquals(3, scn.GetBurdens());
		assertEquals(7, scn.GetShadowDeckCount());
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowPlayAvailable(eye));
		scn.ShadowPlayCard(eye);

		// A [sauron] Orc of either title; not the [sauron] Uruk-hai, nor the [moria] Orc, though each could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));

		// X is the Shadow player's count of the burdens he spots: from none to the three there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		assertInDiscard(eye);
		// The burdens are spotted, not removed.
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePlayerMaySpotFewerBurdensThanThereAreAndDrawsThatMany() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetShadowCard("eye");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(eye);
		assertInHand(eye);

		scn.StartGame();
		scn.SetShadowDrawDeck(scn.GetShadowCard("deck1"), scn.GetShadowCard("deck2"), scn.GetShadowCard("deck3"),
				scn.GetShadowCard("deck4"), scn.GetShadowCard("deck5"), scn.GetShadowCard("deck6"), scn.GetShadowCard("deck7"));
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(chieftain);
		assertEquals(3, scn.GetBurdens());
		assertEquals(7, scn.GetShadowDeckCount());
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.ShadowPlayCard(eye);
		// The only [sauron] Orc exerts with no question.
		assertEquals(1, scn.GetWoundsOn(chieftain));

		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		// One burden spotted of three: one card drawn.
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetBurdens());
		assertInDiscard(eye);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixBurdensSpottedDrawsNoMoreThanFiveCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetShadowCard("eye");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(eye);
		assertInHand(eye);

		scn.StartGame();
		scn.SetShadowDrawDeck(scn.GetShadowCard("deck1"), scn.GetShadowCard("deck2"), scn.GetShadowCard("deck3"),
				scn.GetShadowCard("deck4"), scn.GetShadowCard("deck5"), scn.GetShadowCard("deck6"), scn.GetShadowCard("deck7"));
		// Frodo's resistance is 10: six burdens do not corrupt him.
		scn.AddBurdens(6);
		scn.MoveMinionsToTable(chieftain);
		assertEquals(6, scn.GetBurdens());
		assertEquals(7, scn.GetShadowDeckCount());
		scn.FreepsPass();

		scn.ShadowPlayCard(eye);
		assertEquals(1, scn.GetWoundsOn(chieftain));

		// The limit is on what X buys, not on X: all six burdens may be spotted.
		assertEquals(6, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("6");

		// Six spotted, five drawn, with seven cards in the deck to draw from.
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(6, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetShadowCard("eye");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(eye);
		assertInHand(eye);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(chieftain, guard, troop);
		// Vitality 2 with 1 wound: the Chieftain cannot exert.  The [sauron] Uruk-hai and the [moria] Orc beside him
		// could.
		scn.AddWoundsToChar(chieftain, 1);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(eye));
		assertInHand(eye);
	}
}
