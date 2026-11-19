package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thandronen", "4_85");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion with no requirement to play; +3 only against a Nazgul
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability used only on purpose
					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area, a card of the culture that is no Elf; nothing without Legolas
					// Preparations: the player's own [dwarven] condition; its regroup ability stacks a card from his
					// discard pile there, his own Shadow cards included.  Its abilities are used only on purpose.
					put("preparations", "7_12");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its "when you play" does nothing when stacked
					put("seeking", "1_276");    // Seeking Its Master: [sauron] condition, a Shadow card that is no minion
					// Teeth of Mordor: the opponent's [sauron] condition, whose regroup ability stacks a [sauron] Orc
					// from play there.  Used only on purpose.
					put("teeth", "5_110");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; a trigger on a weapon played on him only
					put("besieger", "4_180");   // Uruk Besieger: [isengard] Uruk-hai whose own regroup ability stacks him on a site his owner controls
					// Fleet of Corsair Ships: the opponent's [men] possession in the support area, on which play stacks
					// [men] minions.  A play trigger and a regroup ability (4 tokens) only.
					put("fleet", "18_66");
					put("axeman", "11_69");     // Axeman of Harad: [men] Man; a cost of his own play only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("troop2", "1_177");
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
		 * Game Text: To play, spot an Elf.<br><b>Regroup:</b> Exert Thandronen to discard a minion stacked on a condition or stacked on a site.
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
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var finalcount = scn.GetFreepsCard("finalcount");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(thandronen, arwen);

		scn.StartGame();

		// A companion who is no Elf and an [elven] card that is no Elf in play, and no Elf.
		assertInHand(thandronen, arwen);
		assertFalse(scn.FreepsPlayAvailable(thandronen));
		scn.FreepsPlayCard(arwen);

		assertTrue(scn.FreepsPlayAvailable(thandronen));
		scn.FreepsPlayCard(thandronen);
		assertInZone(Zone.FREE_CHARACTERS, thandronen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyElfIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(thandronen);

		scn.StartGame();

		assertInHand(thandronen);
		assertTrue(scn.FreepsPlayAvailable(thandronen));
		scn.FreepsPlayCard(thandronen);
		assertInZone(Zone.FREE_CHARACTERS, thandronen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsHimToDiscardAMinionStackedOnAConditionOrOnASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var preparations = scn.GetFreepsCard("preparations");
		var runner = scn.GetFreepsCard("runner");
		var seeking = scn.GetFreepsCard("seeking");
		scn.MoveCompanionsToTable(thandronen);
		scn.MoveCardsToSupportArea(preparations);

		var teeth = scn.GetShadowCard("teeth");
		var chieftain = scn.GetShadowCard("chieftain");
		var besieger = scn.GetShadowCard("besieger");
		var fleet = scn.GetShadowCard("fleet");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToSupportArea(teeth, fleet);
		// A minion on the opponent's condition, one on the player's own condition, and one on a site the
		// opponent controls: each can be chosen.
		scn.StackCardsOn(teeth, chieftain);
		scn.StackCardsOn(preparations, runner);
		scn.StackCardsOn(site1, besieger);
		// A Shadow card that is no minion on a condition, a minion on a possession, a minion in the discard pile
		// and one in play: none can be.
		scn.StackCardsOn(preparations, seeking);
		scn.StackCardsOn(fleet, axeman);
		scn.MoveCardsToDiscard(troop);
		scn.MoveMinionsToTable(troop2);
		assertInZone(Zone.STACKED, chieftain, runner, besieger, seeking, axeman);
		assertInDiscard(troop);
		scn.SkipToAssignments();
		// Nobody is assigned, so the Troop in play lives on into the regroup phase.
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.STACKED, chieftain, runner, besieger, seeking, axeman);
		assertInDiscard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop2);
		assertEquals(0, scn.GetWoundsOn(thandronen));
		int shadowDiscard = scn.GetShadowDiscardCount();
		int freepsDiscard = scn.GetFreepsDiscardCount();

		assertTrue(scn.FreepsActionAvailable(thandronen));
		scn.FreepsUseCardAction(thandronen);
		assertEquals(1, scn.GetWoundsOn(thandronen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, runner, besieger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(seeking, axeman, troop, troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(besieger);

		// To its owner's discard pile; every other card stays where it was.
		assertInDiscard(besieger);
		assertEquals(shadowDiscard + 1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.GetStackedCards(teeth).contains(chieftain));
		assertTrue(scn.GetStackedCards(preparations).contains(runner));
		assertTrue(scn.GetStackedCards(preparations).contains(seeking));
		assertTrue(scn.GetStackedCards(fleet).contains(axeman));
		assertInZone(Zone.SHADOW_CHARACTERS, troop2);

		// Used again once the Shadow player has passed: the player's own minion off his own condition.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(thandronen));
		scn.FreepsUseCardAction(thandronen);
		assertEquals(2, scn.GetWoundsOn(thandronen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, runner));
		scn.FreepsChooseCard(runner);

		assertInDiscard(runner);
		assertEquals(freepsDiscard + 1, scn.GetFreepsDiscardCount());
		assertTrue(scn.GetStackedCards(teeth).contains(chieftain));
		assertTrue(scn.GetStackedCards(preparations).contains(seeking));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		scn.MoveCompanionsToTable(thandronen);
		var teeth = scn.GetShadowCard("teeth");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, chieftain);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(thandronen, 2);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(thandronen));
		assertInZone(Zone.STACKED, chieftain);
		// No minion in play, so the game goes from the Shadow phase to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thandronen));
		assertTrue(scn.GetStackedCards(teeth).contains(chieftain));
	}

	@Test
	public void WithNoMinionWhereItCanBeDiscardedTheRegroupAbilityExertsHimForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thandronen = scn.GetFreepsCard("thandronen");
		var preparations = scn.GetFreepsCard("preparations");
		var seeking = scn.GetFreepsCard("seeking");
		scn.MoveCompanionsToTable(thandronen);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, seeking);
		var fleet = scn.GetShadowCard("fleet");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(fleet);
		scn.StackCardsOn(fleet, axeman);

		scn.StartGame();

		assertInZone(Zone.STACKED, seeking, axeman);
		scn.SkipToPhase(Phase.REGROUP);

		// An ability with a cost is offered though its effect will find nothing (Q-0034).
		assertEquals(0, scn.GetWoundsOn(thandronen));
		assertTrue(scn.FreepsActionAvailable(thandronen));
		scn.FreepsUseCardAction(thandronen);
		assertEquals(1, scn.GetWoundsOn(thandronen));
		assertTrue(scn.GetStackedCards(preparations).contains(seeking));
		assertTrue(scn.GetStackedCards(fleet).contains(axeman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
