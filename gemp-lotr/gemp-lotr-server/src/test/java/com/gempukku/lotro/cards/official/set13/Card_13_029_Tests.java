package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dasron", "13_29");
					// Garrison of Osgiliath: [gondor] support-area condition that takes 2 [gondor] tokens as it is
					// played beside a [gondor] Man; a skirmish ability used only on purpose
					put("garrison", "6_52");
					// Suspended Palaces: [dwarven] support-area condition that takes 2 [dwarven] tokens as it is
					// played beside a Dwarf; a fellowship ability used only on purpose
					put("palaces", "6_10");
					// Ships of Great Draught: the Shadow player's [raider] support-area possession that takes [raider]
					// tokens by its Shadow ability; its abilities are used only on purpose
					put("ships", "8_65");
					put("wizard", "1_78");      // Mysterious Wizard: [gandalf] Skirmish event, a spell
					put("folly", "1_77");       // Let Folly Be Our Cloak: [gandalf] Fellowship event, no spell
					put("walkers", "1_79");     // The Nine Walkers: a [gandalf] card that is no event (a condition)
					put("defenses", "1_32");    // Border Defenses: an event that is not [gandalf] ([elven])
					put("servant", "1_83");     // Servant of the Secret Fire: [gandalf] event, laid in the Shadow player's discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DasronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Dasron, Merchant From Dorwinion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: At the start of the fellowship phase, you may remove 2 Free Peoples culture tokens to take a [gandalf] event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var dasron = scn.GetFreepsCard("dasron");

		assertEquals("Dasron", dasron.getBlueprint().getTitle());
		assertEquals("Merchant From Dorwinion", dasron.getBlueprint().getSubtitle());
		assertTrue(dasron.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, dasron.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, dasron.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, dasron.getBlueprint().getCardType());
		assertEquals(Race.MAN, dasron.getBlueprint().getRace());
		assertEquals(2, dasron.getBlueprint().getTwilightCost());
		assertEquals(6, dasron.getBlueprint().getStrength());
		assertEquals(3, dasron.getBlueprint().getVitality());
		assertEquals(6, dasron.getBlueprint().getResistance());
	}

	@Test
	public void RemovesAGondorAndADwarvenTokenToTakeAGandalfEventFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dasron = scn.GetFreepsCard("dasron");
		var garrison = scn.GetFreepsCard("garrison");
		var palaces = scn.GetFreepsCard("palaces");
		var wizard = scn.GetFreepsCard("wizard");
		var folly = scn.GetFreepsCard("folly");
		var walkers = scn.GetFreepsCard("walkers");
		var defenses = scn.GetFreepsCard("defenses");
		scn.MoveCompanionsToTable(dasron);
		scn.MoveCardsToSupportArea(garrison, palaces);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(palaces, 1);
		scn.MoveCardsToDiscard(wizard, folly, walkers, defenses);

		var ships = scn.GetShadowCard("ships");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 2);
		scn.MoveCardsToDiscard(servant);

		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertInDiscard(wizard, folly, walkers, defenses, servant);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dasron));
		scn.FreepsAcceptOptionalTrigger();

		// The tokens of two Free Peoples cultures, one at a time; not the Shadow player's [raider] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, palaces));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ships));
		scn.FreepsChooseCard(garrison);
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(2, scn.GetCultureTokensOn(ships));

		// Either [gandalf] event in his own discard pile; no [gandalf] condition, no [elven] event, nothing of the
		// Shadow player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wizard, folly));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(walkers, defenses, servant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(wizard);
		assertInHand(wizard);
		assertInDiscard(folly, walkers, defenses, servant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneFreePeoplesTokenBesideTwoRaiderTokensItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dasron = scn.GetFreepsCard("dasron");
		var garrison = scn.GetFreepsCard("garrison");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveCompanionsToTable(dasron);
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 1);
		scn.MoveCardsToDiscard(wizard);

		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 2);

		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertInDiscard(wizard);

		scn.StartGame();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dasron));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertInDiscard(wizard);
	}

	@Test
	public void WithNoGandalfEventInTheDiscardPileTheTokensAreRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dasron = scn.GetFreepsCard("dasron");
		var garrison = scn.GetFreepsCard("garrison");
		var palaces = scn.GetFreepsCard("palaces");
		var walkers = scn.GetFreepsCard("walkers");
		var defenses = scn.GetFreepsCard("defenses");
		scn.MoveCompanionsToTable(dasron);
		scn.MoveCardsToSupportArea(garrison, palaces);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(palaces, 1);
		scn.MoveCardsToDiscard(walkers, defenses);

		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToDiscard(servant);

		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertInDiscard(walkers, defenses, servant);

		scn.StartGame();
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dasron));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(garrison);

		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertInDiscard(walkers, defenses, servant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedItIsNotOfferedAtTheStartOfTheRegroupPhaseAndIsOfferedAgainNextFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dasron = scn.GetFreepsCard("dasron");
		var garrison = scn.GetFreepsCard("garrison");
		var palaces = scn.GetFreepsCard("palaces");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveCompanionsToTable(dasron);
		scn.MoveCardsToSupportArea(garrison, palaces);
		scn.AddTokensToCard(garrison, 1);
		scn.AddTokensToCard(palaces, 1);
		scn.MoveCardsToDiscard(wizard);

		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertInDiscard(wizard);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dasron));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertInDiscard(wizard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The start of another phase of the same turn: no offer.  (No minion: the turn goes from the Shadow phase to
		// the regroup phase.)
		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(dasron));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// His next fellowship phase, turn 3: offered again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dasron));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertInDiscard(wizard);
	}
}
