package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_085_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thandronen", "54_85");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion and archer, no requirement to play; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion: a companion who is no Elf
					put("mallorn", "1_54");     // Mallorn-trees: [elven] condition in the support area; a fellowship ability used only on purpose
					put("seeking", "1_276");    // Seeking Its Master: [sauron] condition in the support area; a Shadow ability used only on purpose
					put("seeking2", "1_276");   // a second copy, stacked as a Shadow card that is no minion
					put("toby", "1_305");       // Old Toby: [shire] possession in the support area; its when-played draw does nothing when cheated in
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; its when-played (2) does nothing when stacked
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, damage +1 and nothing else
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion with no text, of the culture that stacks itself on sites
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThandronenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Thandronen, Veteran Protector
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Valiant.
		 * To play, spot an Elf.
		 * Regroup: Exert Thandronen to discard a minion stacked on a condition or stacked on a site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thandronen");

		assertEquals("Thandronen", card.getBlueprint().getTitle());
		assertEquals("Veteran Protector", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndThenPlaysForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(thandronen, legolas);

		scn.StartGame();

		// A companion who is no Elf and an [elven] condition in play, and no Elf.
		assertFalse(scn.FreepsPlayAvailable(thandronen));
		scn.FreepsPlayCard(legolas);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(thandronen));
		scn.FreepsPlayCard(thandronen);
		assertInZone(Zone.FREE_CHARACTERS, thandronen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyElfIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(thandronen);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(thandronen));
		scn.FreepsPlayCard(thandronen);
		assertInZone(Zone.FREE_CHARACTERS, thandronen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsHimToDiscardAMinionStackedOnAConditionAndThenOneStackedOnASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var mallorn = scn.GetFreepsCard("mallorn");
		var toby = scn.GetFreepsCard("toby");
		var seeking = scn.GetShadowCard("seeking");
		var seeking2 = scn.GetShadowCard("seeking2");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveCompanionsToTable(thandronen);
		scn.MoveCardsToSupportArea(mallorn, toby);

		scn.StartGame();

		var site1 = scn.GetCurrentSite();
		scn.MoveCardsToSupportArea(seeking);
		// A minion on each player's condition and one on a site: all three can be chosen.
		scn.StackCardsOn(mallorn, runner);
		scn.StackCardsOn(seeking, savage);
		scn.StackCardsOn(site1, brigand);
		// A minion stacked on a possession, a Shadow card that is no minion stacked on a condition, a minion in the
		// discard pile: none can be chosen.
		scn.StackCardsOn(toby, troop);
		scn.StackCardsOn(mallorn, seeking2);
		scn.MoveCardsToDiscard(troop2);

		// No minion in play, so the game goes from the Shadow phase to regroup.
		scn.SkipToPhase(Phase.REGROUP);
		// A minion in play cannot be chosen either.
		scn.MoveMinionsToTable(troop3);

		assertEquals(0, scn.GetWoundsOn(thandronen));
		assertTrue(scn.FreepsActionAvailable(thandronen));
		scn.FreepsUseCardAction(thandronen);
		assertEquals(1, scn.GetWoundsOn(thandronen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, seeking2, troop2, troop3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(savage);
		assertEquals(0, scn.GetStackedCards(seeking).size());
		assertTrue(scn.GetStackedCards(mallorn).contains(runner));
		assertTrue(scn.GetStackedCards(mallorn).contains(seeking2));
		assertTrue(scn.GetStackedCards(site1).contains(brigand));
		assertTrue(scn.GetStackedCards(toby).contains(troop));
		assertInZone(Zone.SHADOW_CHARACTERS, troop3);

		// Freeps acts first in regroup; the Shadow player passes, and the Free Peoples player uses it again.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(thandronen));
		scn.FreepsUseCardAction(thandronen);
		assertEquals(2, scn.GetWoundsOn(thandronen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, brigand));
		scn.FreepsChooseCard(brigand);

		assertInDiscard(brigand);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.GetStackedCards(mallorn).contains(runner));
		assertTrue(scn.GetStackedCards(toby).contains(troop));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedOutsideTheRegroupPhaseNorWhenHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var mallorn = scn.GetFreepsCard("mallorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thandronen);
		scn.MoveCardsToSupportArea(mallorn);
		scn.StackCardsOn(mallorn, runner);

		scn.StartGame();

		// The fellowship phase: he is fresh and a minion is stacked on a condition.
		assertFalse(scn.FreepsActionAvailable(thandronen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(thandronen, 2);
		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsActionAvailable(thandronen));
		assertTrue(scn.GetStackedCards(mallorn).contains(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfferedWithNoMinionWhereItCanBeDiscardedAndExertsHimForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var toby = scn.GetFreepsCard("toby");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(thandronen);
		scn.MoveCardsToSupportArea(toby);
		scn.StackCardsOn(toby, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// An ability with a cost is offered though its effect will find nothing (Q-0034).
		assertEquals(0, scn.GetWoundsOn(thandronen));
		assertTrue(scn.FreepsActionAvailable(thandronen));
		scn.FreepsUseCardAction(thandronen);
		assertEquals(1, scn.GetWoundsOn(thandronen));
		assertTrue(scn.GetStackedCards(toby).contains(troop));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
