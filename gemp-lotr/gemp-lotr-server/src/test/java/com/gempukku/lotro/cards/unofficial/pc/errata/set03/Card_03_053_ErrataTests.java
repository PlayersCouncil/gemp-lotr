package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_053_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hate", "53_53");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and no other text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc; a Shadow ability used only on purpose
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll; its text is about playing it, and it is cheated in

					put("filler1", "1_178");    // Goblin Runner: [moria] Orc, cost 1; when played, adds (2)
					// Left in the Shadow player's draw deck, so that there is always more to draw than 3.
					put("filler2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HateAndAngerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hate and Anger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot an Uruk-hai and an Orc to draw 3 cards.
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
	public void SpottingAnUrukHaiAndAnOrcDrawsThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("filler1");
		scn.MoveCardsToHand(hate, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An Uruk-hai and no Orc.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hate));

		// The Goblin Runner is an Orc (it adds its (2) without asking).
		scn.ShadowPlayCard(runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hate));

		int twilight = scn.GetTwilight();
		int deck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(hate);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertInDiscard(hate);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOrATrollIsNoOrcButAnIsengardOrcIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var troll = scn.GetShadowCard("troll");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry, troll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Two Uruk-hai and a [moria] Troll.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hate));

		scn.MoveMinionsToTable(forger);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(hate));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardOrcIsNoUrukHaiButASauronUrukHaiIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var troop = scn.GetShadowCard("troop");
		var forger = scn.GetShadowCard("forger");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, forger);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Two Orcs, one of them [isengard], and no Uruk-hai.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hate));

		scn.MoveMinionsToTable(sentry);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(hate));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
