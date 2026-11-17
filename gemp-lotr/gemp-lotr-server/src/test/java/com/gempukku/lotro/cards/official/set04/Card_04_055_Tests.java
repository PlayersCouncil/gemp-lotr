package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
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

public class Card_04_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "4_55");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability only
					// Thrarin, Dwarven Smith: a Dwarf ally, vitality 2, home 3F; a maneuver ability only
					put("thrarin", "1_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, no Dwarf; a maneuver ability only
					// Here Is Good Rock: [dwarven] support-area condition that play stacks Free Peoples cards on; a skirmish ability only
					put("rock", "4_50");
					// From the Armory: [dwarven] support-area condition; a skirmish ability only
					put("armory", "4_47");
					// Endurance of Dwarves: [dwarven] condition borne by a Dwarf, vitality +1; a skirmish ability only
					put("endurance", "2_4");
					// Dwarven Axe: a [dwarven] possession, no condition; its trigger is on a skirmish lost to its bearer
					put("dwaxe", "1_9");
					// Ithilien Trap: a [gondor] support-area condition; its trigger is on a [gondor] Man winning a skirmish
					put("trap", "4_126");
					put("guard", "1_7");        // Dwarf Guard: a Free Peoples card stacked on Here Is Good Rock
					put("sword", "1_299");      // Hobbit Sword: a second
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its play trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RestlessAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Restless Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a Dwarf to shuffle a [dwarven] condition (and all cards stacked on it) into your draw deck. Exert a minion for each card shuffled into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Restless Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfToShuffleAConditionAndItsStackedCardsInAndExertsAMinionForEachCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rock = scn.GetFreepsCard("rock");
		var armory = scn.GetFreepsCard("armory");
		var endurance = scn.GetFreepsCard("endurance");
		var dwaxe = scn.GetFreepsCard("dwaxe");
		var trap = scn.GetFreepsCard("trap");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(rock, armory, trap);
		scn.AttachCardsTo(gimli, endurance, dwaxe);
		scn.StackCardsOn(rock, guard, sword);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, rock, armory, trap);
		assertInZone(Zone.ATTACHED, endurance, dwaxe);
		scn.MoveMinionsToTable(savage, troop, runner);
		// No minion is assigned: the game reaches the regroup phase with all three in play.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.STACKED, guard, sword);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int freepsDeck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);

		// The cost: a Dwarf, companion or ally; not a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// One [dwarven] condition, in the support area or borne; not a [dwarven] possession or a [gondor] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rock, armory, endurance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwaxe, trap));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(rock);

		// The condition and the two cards stacked on it: three cards in, in one shuffle.
		assertInZone(Zone.DECK, rock, guard, sword);
		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.SUPPORT, armory, trap);
		assertInZone(Zone.ATTACHED, endurance, dwaxe);

		// Three exertions, one minion at a time and the same minion more than once (CR 4.2 "for each"); the
		// Runner, exhausted at vitality 1, is never offered, nor is any companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, gimli, aragorn));
		scn.FreepsChooseCard(savage);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(savage);
		// The Savage is now exhausted: the third exertion goes to the Troop unasked.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(axe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyPaysAndAConditionWithNothingStackedOnItExertsOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetFreepsCard("armory");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, armory);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		int freepsDeck = scn.GetFreepsDeckCount();

		// Thrarin, the one Dwarf, pays unasked, and the one [dwarven] condition is shuffled in unasked.
		scn.FreepsPlayCard(axe);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.DECK, armory);
		assertEquals(freepsDeck + 1, scn.GetFreepsDeckCount());

		// One card shuffled in: one exertion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(axe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheConditionChosenGoesIntoTheDrawDeckWithTheCardsStackedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		var armory = scn.GetFreepsCard("armory");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock, armory);
		scn.StackCardsOn(rock, guard, sword);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, rock, armory);
		assertInZone(Zone.STACKED, guard, sword);
		// No minion in play: the game goes straight to regroup, and nothing is there to exert.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(gimli));
		int freepsDeck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(axe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(rock, armory));
		scn.FreepsChooseCard(rock);

		assertInZone(Zone.DECK, rock, guard, sword);
		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SUPPORT, armory);
		assertInDiscard(axe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetFreepsCard("armory");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(axe);
		// Gimli, vitality 3, exhausted; Aragorn could exert, but is no Dwarf.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(axe));
		assertInHand(axe);
		assertInZone(Zone.SUPPORT, armory);
	}

	@Test
	public void WithNoDwarvenConditionTheEventIsStillPlayedAndOnlyTheDwarfExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var dwaxe = scn.GetFreepsCard("dwaxe");
		var trap = scn.GetFreepsCard("trap");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, dwaxe);
		scn.MoveCardsToSupportArea(trap);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trap);
		assertInZone(Zone.ATTACHED, dwaxe);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(gimli));
		int freepsDeck = scn.GetFreepsDeckCount();

		// An event's target is no requirement (CR 4.2 "effect"): played, paid for, and nothing is shuffled in.
		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertInZone(Zone.SUPPORT, trap);
		assertInZone(Zone.ATTACHED, dwaxe);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(axe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
