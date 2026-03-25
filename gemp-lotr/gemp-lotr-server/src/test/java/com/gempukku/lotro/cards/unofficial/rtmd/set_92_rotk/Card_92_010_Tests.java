package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_010_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Goblin Runner (1_178): Moria Orc minion, twilight 0, strength 5, vitality 1
		put("runner", "1_178");
		// Goblin Sneak (1_183): Moria Goblin minion, twilight 2, strength 5, vitality 1
		put("sneak", "1_183");
		// Cave Troll of Moria (1_165): Moria Troll minion, twilight 10, strength 10, vitality 4
		put("troll", "1_165");
		// Orc fodder cards for discarding from hand
		put("fodder1", "1_179");
		put("fodder2", "1_183");
		put("fodder3", "1_191");
		// Extra fodder if needed
		put("fodder4", "1_192");
	}};

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_10"
		);
	}

	protected VirtualTableScenario GetOpponentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_10", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_10
		 * Type: MetaSite
		 * Game Text: Shadow: Discard 3 cards from hand to play a minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_10", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void PlaysMinionFromDiscardAfterDiscarding3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var runner = scn.GetShadowCard("runner");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder3 = scn.GetShadowCard("fodder3");
		var fodder4 = scn.GetShadowCard("fodder4");

		scn.MoveCardsToHand(fodder1, fodder2, fodder3, fodder4);
		scn.MoveCardsToDiscard(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Discard 3 cards
		scn.ShadowChooseCards(fodder1, fodder2, fodder3);

		// Should be able to choose runner from discard
		scn.ShadowChooseCardBPFromSelection(runner);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
	}

	@Test
	public void CannotChooseDiscardedCardsAsTarget() throws DecisionResultInvalidException, CardNotFoundException {
		// Per ruling on They Are Coming: the cards you discarded cannot be the minion you play.
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder3 = scn.GetShadowCard("fodder3");
		var fodder4 = scn.GetShadowCard("fodder4");

		// Put runner in hand as one of the 3 to discard, plus 2 others
		// Runner will end up in discard but should NOT be selectable
		scn.MoveCardsToHand(runner, fodder2, fodder3, fodder4);
		// fodder1 is already a minion in discard as the intended target
		scn.MoveCardsToDiscard(fodder1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(scn.GetShadowCard("mod"));

		// Discard 3 cards from hand (including runner)
		scn.ShadowChooseCards(runner, fodder2, fodder3);

		// Now selecting target: runner is in discard but was one of the discarded cards,
		// so only fodder1 should be available.  Since there's only one valid choice,
		// it should auto-select fodder1.
		assertInZone(Zone.SHADOW_CHARACTERS, fodder1);
	}

	@Test
	public void CannotPlayMinionYouCantAfford() throws DecisionResultInvalidException, CardNotFoundException {
		// Cave Troll costs 10 twilight — if pool is insufficient, it shouldn't be playable.
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		var fodder3 = scn.GetShadowCard("fodder3");

		scn.MoveCardsToHand(fodder1, fodder2, fodder3);
		scn.MoveCardsToDiscard(troll);

		scn.StartGame();
		// Don't add extra twilight — pool should be too low for a 10-cost troll
		scn.FreepsPassCurrentPhaseAction();

		// The action shouldn't be available at all since the only minion in discard
		// can't be afforded (and you can't take the action if the effect can't resolve).
		assertFalse(scn.ShadowActionAvailable("Use Race Text"));
	}

	@Test
	public void OpponentCannotUseAbility() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_8: "you play" — Shadow has the modifier, so Freeps playing a companion should not trigger it.

		var scn = GetOpponentScenario();
		var mod = scn.GetFreepsCard("mod");

		scn.MoveCardsToShadowDiscard("runner");
		scn.MoveCardsToShadowHand("fodder1", "fodder2", "fodder3");

		scn.StartGame();

		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(mod));
	}
}
