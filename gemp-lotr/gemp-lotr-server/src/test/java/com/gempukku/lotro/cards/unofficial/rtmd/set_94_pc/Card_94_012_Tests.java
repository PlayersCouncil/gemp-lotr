package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_012_Tests
{

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: minion in hand, cost choice A
					put("runner", "1_178"); // Goblin Runner: minion in hand, cost choice B (stays in hand)
					put("troop", "1_143"); // Troop of Uruk-hai: minion already in discard, valid retrieval target
					put("runner2", "1_178"); // Goblin Runner: 2nd minion already in discard - gives 2 valid retrieval
					// targets alongside troop, so the just-discarded card's exclusion is actually visible
					// (a single valid target would auto-resolve without presenting a choice)
					put("brigand", "4_10"); // Dunlending Brigand: a [dunland] Man with no text, beside the [moria] Orcs
					put("patrol", "1_177"); // Goblin Patrol Troop: no text; the minion in the other player's discard pile
					put("fear", "1_278"); // Strength Born of Fear: a [sauron] condition, a Shadow card that is no minion
					put("fear2", "1_278");
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: the Shadow player's own companion, a character that is no minion
					put("aragorn2", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_12"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: minion in hand, cost choice A
					put("runner", "1_178"); // Goblin Runner: minion in hand, cost choice B (stays in hand)
					put("troop", "1_143"); // Troop of Uruk-hai: minion already in discard, valid retrieval target
					put("runner2", "1_178"); // Goblin Runner: 2nd minion already in discard - gives 2 valid retrieval
					// targets alongside troop, so the just-discarded card's exclusion is actually visible
					// (a single valid target would auto-resolve without presenting a choice)
					put("brigand", "4_10"); // Dunlending Brigand: a [dunland] Man with no text, beside the [moria] Orcs
					put("patrol", "1_177"); // Goblin Patrol Troop: no text; the minion in the other player's discard pile
					put("fear", "1_278"); // Strength Born of Fear: a [sauron] condition, a Shadow card that is no minion
					put("fear2", "1_278");
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: the Shadow player's own companion, a character that is no minion
					put("aragorn2", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_12", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_12
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: Shadow: Discard a minion from hand to take a minion into hand from the discard pile.
		 */

		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		assertEquals("Race Text 94_12", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void DiscardAMinionFromHandToTakeOneFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var runner2 = scn.GetShadowCard("runner2");
		var fear = scn.GetShadowCard("fear");
		var fear2 = scn.GetShadowCard("fear2");
		var aragorn = scn.GetShadowCard("aragorn");
		var aragorn2 = scn.GetShadowCard("aragorn2");
		var freepsPatrol = scn.GetFreepsCard("patrol");

		scn.MoveCardsToHand(backstabber, runner, fear, aragorn);
		scn.MoveCardsToDiscard(troop, runner2, fear2, aragorn2);
		// A minion in the other player's discard pile; the Shadow player's own copy stays in his draw deck.
		scn.MoveCardsToDiscard(freepsPatrol);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		int hand = scn.GetShadowHandCount();
		int discard = scn.GetShadowDiscardCount();

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Cost: choice between the two minions in hand
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner));
		// A condition and a companion in the same hand are no minions.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(backstabber);
		assertInDiscard(backstabber);

		// Effect: the "They Are Coming" ruling - the card just discarded as the cost cannot be the
		// one retrieved, even though it is now sitting in the discard pile alongside valid targets
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(backstabber));
		// Nor the condition and the companion in the discard pile, nor the minion in the other player's.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fear2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(freepsPatrol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertInHand(troop);
		assertInDiscard(backstabber, runner2);
		assertInDiscard(fear2, aragorn2, freepsPatrol);
		assertInHand(runner, fear, aragorn);
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void UnavailableWithoutAMinionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		var aragorn = scn.GetShadowCard("aragorn");

		// A Shadow condition and a companion in hand, and no minion.
		scn.MoveCardsToHand(fear, aragorn);
		scn.MoveCardsToDiscard(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void ActionIsAvailableToShadowEvenWhenFreepsOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 94_12's gametext has no "your" - "Shadow: Discard a minion..." is a Shadow-phase ability
		// that should be usable by whichever player is playing the Shadow role, regardless of which
		// side owns this copy of the metasite.
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(backstabber, brigand);
		scn.MoveCardsToDiscard(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// A [moria] Orc or a [dunland] Man may be discarded.
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber));
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand));
		scn.ShadowChooseCard(brigand);

		// The Troop of Uruk-hai is the only other minion in his discard pile, so he is not asked which.
		assertInDiscard(brigand);
		assertInHand(troop, backstabber);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
