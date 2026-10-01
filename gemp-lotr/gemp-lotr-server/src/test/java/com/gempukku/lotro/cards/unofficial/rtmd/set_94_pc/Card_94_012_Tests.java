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

public class Card_94_012_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("backstabber", "1_174"); // Goblin Backstabber: minion in hand, cost choice A
		put("runner", "1_178"); // Goblin Runner: minion in hand, cost choice B (stays in hand)
		put("troop", "1_143"); // Troop of Uruk-hai: minion already in discard, valid retrieval target
		put("runner2", "1_178"); // Goblin Runner: 2nd minion already in discard - gives 2 valid retrieval
		// targets alongside troop, so the just-discarded card's exclusion is actually visible
		// (a single valid target would auto-resolve without presenting a choice)
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_12"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_12", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_12
		 * Type: MetaSite
		 * Game Text: Shadow: Discard a minion from hand to take a minion into hand from the discard pile.
		 */

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

		scn.MoveCardsToHand(backstabber, runner);
		scn.MoveCardsToDiscard(troop, runner2);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		int hand = scn.GetShadowHandCount();
		int discard = scn.GetShadowDiscardCount();

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Cost: choice between the two minions in hand
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner));
		scn.ShadowChooseCard(backstabber);
		assertInDiscard(backstabber);

		// Effect: the "They Are Coming" ruling - the card just discarded as the cost cannot be the
		// one retrieved, even though it is now sitting in the discard pile alongside valid targets
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(backstabber));
		scn.ShadowChooseCard(troop);

		assertInHand(troop);
		assertInDiscard(backstabber, runner2);
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void UnavailableWithoutAMinionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToDiscard(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

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

		scn.MoveCardsToHand(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowActionAvailable(mod));
	}
}
