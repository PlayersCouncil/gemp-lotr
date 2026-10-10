package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_105");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defiled", "13_105");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the companion in the skirmish, strength 8
					// Howling Orc: an [orc] lurker minion, strength 8; his trigger answers an [orc] minion killed
					// in a skirmish
					put("howler", "13_109");

					// [orc] cards of four types for the discard pile, where none of them does anything
					put("underling", "11_125");     // Isengard Underling: minion
					put("demoralized", "11_114");   // Demoralized: condition
					put("spear", "12_96");          // Orc Spear: possession
					put("strength", "11_140");      // Strength in Shadows: event
					put("hordes", "11_123");        // Goblin Hordes: condition
					put("runner", "1_178");         // Goblin Runner: an Orc of the [moria] culture
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DefiledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Defiled
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 [orc] cards in your discard pile instead of paying the twilight cost for this card.<br>Make your [orc] lurker minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Defiled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAnEmptyPoolItIsPlayedByRemovingFourOrcCardsInTheDiscardPileFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var howler = scn.GetShadowCard("howler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var spear = scn.GetShadowCard("spear");
		var strength = scn.GetShadowCard("strength");
		var hordes = scn.GetShadowCard("hordes");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(defiled);
		scn.MoveCardsToDiscard(underling, demoralized, spear, strength, hordes, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(howler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, howler);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		// Nothing in the pool for an event of cost 2, and five [orc] cards in the discard pile.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(defiled));
		assertEquals(8, scn.GetStrength(howler));

		// Exactly four of the five are to be removed; the Orc of another culture is no candidate.
		scn.ShadowPlayCard(defiled);
		assertTrue(scn.ShadowHasCardChoiceAvailable(underling, demoralized, spear, strength, hordes));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(underling, demoralized, spear, strength);

		assertInZone(Zone.REMOVED, underling, demoralized, spear, strength);
		assertInDiscard(hordes, runner);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(defiled);
		assertEquals(10, scn.GetStrength(howler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolAndThreeOrcCardsInTheDiscardPileItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defiled = scn.GetShadowCard("defiled");
		var howler = scn.GetShadowCard("howler");
		var underling = scn.GetShadowCard("underling");
		var demoralized = scn.GetShadowCard("demoralized");
		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(defiled);
		// Four cards, three of them [orc].
		scn.MoveCardsToDiscard(underling, demoralized, spear, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(howler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, howler);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(defiled));

		// With its cost in the pool it is offered, and paid for there: nothing is asked.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(defiled));
		assertEquals(8, scn.GetStrength(howler));

		scn.ShadowPlayCard(defiled);
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(defiled, underling, demoralized, spear, runner);
		assertEquals(10, scn.GetStrength(howler));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
