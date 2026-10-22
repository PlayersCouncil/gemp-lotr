package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "10_7");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, strength 6; a skirmish ability used only on purpose
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; harmless when cheated in
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits

					// Cards laid on top of the draw deck; only their culture is read.
					put("elfcard", "1_53");     // Lorien Elf: an [elven] companion
					put("count", "4_69");       // Final Count: an [elven] condition
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CelebornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Celeborn, Lord of the Galadhrim
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: At the start of each skirmish involving Celeborn, you may reveal the top card of your draw deck. If it is an [elven] card, you may discard it to wound each minion in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("celeborn");

		assertEquals("Celeborn", card.getBlueprint().getTitle());
		assertEquals("Lord of the Galadhrim", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfHisSkirmishAnElvenCardRevealedMayBeDiscardedToWoundEachMinionInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var haldir = scn.GetFreepsCard("haldir");
		var elfcard = scn.GetFreepsCard("elfcard");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(celeborn, gimli, haldir);

		scn.StartGame();
		// The Lorien Elf on top, the Dwarven Armor under it.
		scn.MoveCardsToTopOfDeck(armor, elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, elfcard, armor);
		int deck = scn.GetFreepsDeckCount();

		scn.MoveMinionsToTable(troop, savage, soldier, runner);
		// Vitality 3 with 2 wounds: the Savage is exhausted, and a wound kills it.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{celeborn, troop},
				new PhysicalCardImpl[]{gimli, soldier},
				new PhysicalCardImpl[]{haldir, runner}
		);
		scn.ShadowAssignToMinions(celeborn, savage);

		// Gimli's skirmish, which does not involve Celeborn: nothing is offered.
		scn.FreepsResolveSkirmish(gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertSame(elfcard, scn.GetFreepsTopOfDeck());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsAcceptOptionalTrigger();

		// The top card is shown to both players.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elfcard, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elfcard, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// An [elven] card: he is asked whether to discard it.
		assertTrue(scn.FreepsDecisionAvailable("Discard"));
		scn.FreepsChooseYes();

		assertInDiscard(elfcard);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(armor, scn.GetFreepsTopOfDeck());
		// Each minion in his skirmish is wounded, the exhausted Savage killed; the Runner, assigned to Haldir
		// and waiting, and the Soldier, whose skirmish is over, are not.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(savage);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedAtTheFirstSkirmishItIsOfferedAgainAtTheStartOfTheFierceOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var elfcard = scn.GetFreepsCard("elfcard");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(celeborn);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, elfcard);
		int deck = scn.GetFreepsDeckCount();

		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(celeborn, pursuer);
		scn.FreepsResolveSkirmish(celeborn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: nothing is shown, the top card stays, nobody is wounded.
		assertFalse(scn.BeingShownCards(scn.P1));
		assertFalse(scn.BeingShownCards(scn.P2));
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Celeborn (6) beats the Pursuer (5), which takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(celeborn, pursuer);
		scn.FreepsResolveSkirmish(celeborn);
		assertTrue(scn.InFierceRound());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		scn.FreepsChooseYes();

		assertInDiscard(elfcard);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACardThatIsNotElvenIsRevealedAndStaysOnTopWithNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(celeborn);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor);
		int deck = scn.GetFreepsDeckCount();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(celeborn, troop);
		scn.FreepsResolveSkirmish(celeborn);

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		scn.DismissRevealedCards();

		// A [dwarven] card: no question, the card stays on top, nobody is wounded.
		assertFalse(scn.FreepsDecisionAvailable("Discard"));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnElvenCardNotDiscardedStaysOnTopAndNoMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var count = scn.GetFreepsCard("count");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(celeborn);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(count);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, count);
		int deck = scn.GetFreepsDeckCount();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(celeborn, troop);
		scn.FreepsResolveSkirmish(celeborn);

		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		// An [elven] condition: the question is asked, and answered no.
		assertTrue(scn.FreepsDecisionAvailable("Discard"));
		scn.FreepsChooseNo();

		assertSame(count, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheRevealIsStillOfferedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(celeborn);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(celeborn, troop);
		scn.FreepsResolveSkirmish(celeborn);

		// As an optional look through an empty draw deck is (Q-0104).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(celeborn));
		scn.FreepsAcceptOptionalTrigger();
		assertFalse(scn.FreepsDecisionAvailable("Discard"));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
