package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hate", "3_53");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and no other text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 and no other text
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, no Uruk-hai; a Shadow ability used only on purpose
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll; its text is about playing it, and it is cheated in
					put("armory", "1_173");     // Goblin Armory: [moria] condition, no minion; it answers [moria] weapons and wounds only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion; it answers a weapon played on it only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1; when played, adds (2)
					// Left in the Shadow player's draw deck, so that there is always more to draw than 3.
					put("filler", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HateandAngerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hate and Anger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot an Uruk-hai and a [moria] minion to draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hate");

		assertEquals("Hate and Anger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnUrukHaiAndAMoriaMinionDrawsThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hate, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An Uruk-hai and no [moria] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hate));

		// The Goblin Runner is a [moria] Orc (it adds its (2) without asking).
		scn.ShadowPlayCard(runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hate));

		int deck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(hate);

		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertInDiscard(hate);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaConditionOrAMinionOfAnotherCultureIsNoMoriaMinionButAMoriaTrollIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var sentry = scn.GetShadowCard("sentry");
		var armory = scn.GetShadowCard("armory");
		var forger = scn.GetShadowCard("forger");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, forger, chieftain);
		scn.MoveCardsToSupportArea(armory);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [sauron] Uruk-hai; a [moria] condition and two minions of other cultures, and no [moria] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hate));

		scn.MoveMinionsToTable(troll);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(hate));

		int deck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(hate);
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var runner = scn.GetShadowCard("runner");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, forger);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [moria] minion, and an [isengard] Orc, of the culture of most Uruk-hai, but no Uruk-hai.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hate));

		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(hate));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
