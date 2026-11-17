package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("underling", "11_125");
					put("underling2", "11_125");

					put("halls", "11_112");       // Conquered Halls: an [orc] Skirmish event
					put("bound", "11_110");       // Bound to its Fate: an [orc] Shadow event
					put("beckoning", "12_83");    // The Beckoning Shadow: an [orc] card that is no event (a condition)
					put("forces", "1_248");       // Forces of Mordor: a [sauron] event
					// Strength in Shadows: an [orc] event, laid only in the Free Peoples player's discard pile
					put("strength", "11_140");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardUnderlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Isengard Underling
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may take an [orc] event from your discard pile into hand.
		*/

		var scn = GetScenario();

		var underling = scn.GetFreepsCard("underling");

		assertEquals("Isengard Underling", underling.getBlueprint().getTitle());
		assertNull(underling.getBlueprint().getSubtitle());
		assertFalse(underling.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, underling.getBlueprint().getSide());
		assertEquals(Culture.ORC, underling.getBlueprint().getCulture());
		assertEquals(CardType.MINION, underling.getBlueprint().getCardType());
		assertEquals(Race.ORC, underling.getBlueprint().getRace());
		assertEquals(3, underling.getBlueprint().getTwilightCost());
		assertEquals(7, underling.getBlueprint().getStrength());
		assertEquals(2, underling.getBlueprint().getVitality());
		assertEquals(4, underling.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayTakeAnOrcEventFromTheDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var halls = scn.GetShadowCard("halls");
		var bound = scn.GetShadowCard("bound");
		var beckoning = scn.GetShadowCard("beckoning");
		var forces = scn.GetShadowCard("forces");
		var freepsStrength = scn.GetFreepsCard("strength");
		scn.MoveCardsToHand(underling);
		scn.MoveCardsToDiscard(halls, bound, beckoning, forces, freepsStrength);
		assertInDiscard(halls, bound, beckoning, forces, freepsStrength);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(underling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(underling));
		assertInDiscard(halls, bound, beckoning, forces, freepsStrength);
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowAcceptOptionalTrigger();
		// [orc] events of two phases are offered; an [orc] card that is no event, a [sauron] event and the other
		// player's [orc] event are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(halls, bound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beckoning, forces, freepsStrength));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(bound);
		assertInHand(bound);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(halls, beckoning, forces, freepsStrength);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingMovesAndASecondCopyPlayedAfterItOffersOnlyItsOwnTake() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var underling2 = scn.GetShadowCard("underling2");
		var halls = scn.GetShadowCard("halls");
		var bound = scn.GetShadowCard("bound");
		scn.MoveCardsToHand(underling, underling2);
		scn.MoveCardsToDiscard(halls, bound);
		assertInDiscard(halls, bound);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(underling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(underling));
		assertInDiscard(halls, bound);
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(halls, bound);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second copy, an [orc] Orc played while the first is in play with [orc] events in the discard pile:
		// it offers its own take, and the first answers its own play only.
		scn.ShadowPlayCard(underling2);
		assertInZone(Zone.SHADOW_CHARACTERS, underling2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(underling2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(underling));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(halls);
		// One card taken, the second copy's.
		assertInHand(halls);
		assertInDiscard(bound);
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoOrcEventInHisOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underling = scn.GetShadowCard("underling");
		var beckoning = scn.GetShadowCard("beckoning");
		var forces = scn.GetShadowCard("forces");
		var freepsStrength = scn.GetFreepsCard("strength");
		scn.MoveCardsToHand(underling);
		scn.MoveCardsToDiscard(beckoning, forces, freepsStrength);
		assertInDiscard(beckoning, forces, freepsStrength);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(underling);
		assertInZone(Zone.SHADOW_CHARACTERS, underling);
		// The take would do nothing: withheld (Q-0224).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(beckoning, forces, freepsStrength);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
