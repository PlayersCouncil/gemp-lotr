package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("notched", "4_52");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; his skirmish ability is used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion who is not Gimli, strength 5, +2 while skirmishing an Orc
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion who is not Gimli, strength 4, no text in play
					// Final Count: [elven] support-area condition that takes an [elven] token each time Legolas wins a skirmish.
					put("finalcount", "4_69");
					// Strength of Arms: [elven] support-area condition that takes an [elven] token each time an Elf wins a
					// skirmish; its skirmish ability is used only on purpose.
					put("strengthofarms", "4_82");
					// Stout and Strong: [dwarven] support-area condition that takes a [dwarven] token each time a Dwarf wins
					// a skirmish; its skirmish ability is used only on purpose.
					put("stout", "4_57");

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("soldier", "1_271");    // Orc Soldier: strength 7, vitality 2; beats Gimli without overwhelming him
					// Uruk Pursuer: fierce, strength 5, vitality 3: Gimli beats him in the skirmish (2 wounds) and again in
					// the fierce skirmish.
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MyAxeIsNotchedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: My Axe Is Notched
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 		Each time Gimli wins a skirmish, place a [dwarven] token on this card.
		 * 		While you can spot X [dwarven] tokens on this card and the same number of [elven] tokens on Final Count, Gimli is strength +X (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("notched");

		assertEquals("My Axe Is Notched", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnlyGimliWinningASkirmishPlacesADwarvenTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(notched);
		scn.MoveCompanionsToTable(gimli, farin);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{farin, runner1}, new PhysicalCardImpl[]{gimli, runner2});

		// Farin, a Dwarf who is not Gimli, 5 + 2 against an Orc, beats the Runner.
		scn.FreepsResolveSkirmish(farin);
		assertEquals(0, scn.GetCultureTokensOn(notched));
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertEquals(0, scn.GetCultureTokensOn(notched));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Gimli, 6, beats the other Runner: the token is placed, with nothing asked.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		// A required trigger, ordered with the wound for losing: neither is carried out until the order is chosen.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(notched));
		scn.FreepsResolveRuleFirst();
		assertInDiscard(runner2);
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoTokenIsPlacedWhenGimliLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(notched);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, soldier);

		assertEquals(0, scn.GetCultureTokensOn(notched));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// 6 against 7: Gimli loses and takes a wound.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetCultureTokensOn(notched));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinPlacesAnotherTokenAndGimliGainsStrengthAsTheyArePlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var finalcount = scn.GetFreepsCard("finalcount");
		var gimli = scn.GetFreepsCard("gimli");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(notched, finalcount);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.AddTokensToCard(finalcount, 2);
		assertEquals(2, scn.GetCultureTokensOn(finalcount));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, pursuer);

		assertEquals(0, scn.GetCultureTokensOn(notched));
		assertEquals(6, scn.GetStrength(gimli));
		// 6 against 5: Gimli wins, and the Pursuer lives with 2 wounds of 3.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetCultureTokensOn(notched));
		// One [dwarven] token here and two [elven] on Final Count: +1.
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.InFierceRound());

		// The fierce skirmish: Gimli wins again, and the second token makes it +2.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, pursuer);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInDiscard(pursuer);
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GimliIsStrengthPlusTheNumberOfTokensMatchedOnBothCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var finalcount = scn.GetFreepsCard("finalcount");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(notched, finalcount);
		scn.MoveCompanionsToTable(gimli, guard);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(finalcount, 1);
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertEquals(1, scn.GetCultureTokensOn(finalcount));
		assertEquals(7, scn.GetStrength(gimli));
		// Another Dwarf companion, who is not Gimli, gains nothing.
		assertEquals(4, scn.GetStrength(guard));

		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(finalcount, 1);
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(2, scn.GetCultureTokensOn(finalcount));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
	}

	@Test
	public void NoBonusWithoutElvenTokensOnFinalCountEvenWithElvenTokensElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var finalcount = scn.GetFreepsCard("finalcount");
		var strengthofarms = scn.GetFreepsCard("strengthofarms");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(notched, finalcount, strengthofarms);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(gimli));
		scn.AddTokensToCard(notched, 2);
		// [elven] tokens on an [elven] condition that is not Final Count.
		scn.AddTokensToCard(strengthofarms, 2);
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(2, scn.GetCultureTokensOn(strengthofarms));
		assertEquals(0, scn.GetCultureTokensOn(finalcount));
		assertEquals(6, scn.GetStrength(gimli));

		// The control: the same number on Final Count.
		scn.AddTokensToCard(finalcount, 2);
		assertEquals(8, scn.GetStrength(gimli));
	}

	@Test
	public void DwarvenTokensOnAnotherCardDoNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var finalcount = scn.GetFreepsCard("finalcount");
		var stout = scn.GetFreepsCard("stout");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(notched, finalcount, stout);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(gimli));
		scn.AddTokensToCard(finalcount, 2);
		// [dwarven] tokens on a [dwarven] condition that is not this one.
		scn.AddTokensToCard(stout, 2);
		assertEquals(2, scn.GetCultureTokensOn(finalcount));
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(notched));
		assertEquals(6, scn.GetStrength(gimli));

		// The control: the same number here.
		scn.AddTokensToCard(notched, 2);
		assertEquals(8, scn.GetStrength(gimli));
	}

	@Test
	public void TheBonusStopsAtPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var finalcount = scn.GetFreepsCard("finalcount");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(notched, finalcount);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(gimli));
		scn.AddTokensToCard(notched, 3);
		scn.AddTokensToCard(finalcount, 3);
		assertEquals(9, scn.GetStrength(gimli));

		// Four of each: still +3.
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(finalcount, 1);
		assertEquals(4, scn.GetCultureTokensOn(notched));
		assertEquals(4, scn.GetCultureTokensOn(finalcount));
		assertEquals(9, scn.GetStrength(gimli));
	}

	@Test
	public void TheBonusIsTheLowerOfTheTwoCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var notched = scn.GetFreepsCard("notched");
		var finalcount = scn.GetFreepsCard("finalcount");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(notched, finalcount);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertEquals(6, scn.GetStrength(gimli));
		// The ruling's own example: two [elven] tokens on Final Count and three [dwarven] tokens here.
		scn.AddTokensToCard(notched, 3);
		scn.AddTokensToCard(finalcount, 2);
		assertEquals(3, scn.GetCultureTokensOn(notched));
		assertEquals(2, scn.GetCultureTokensOn(finalcount));
		assertEquals(8, scn.GetStrength(gimli));

		// The other way round: three on Final Count and two here.
		scn.RemoveTokensFromCard(notched, 1);
		scn.AddTokensToCard(finalcount, 1);
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertEquals(3, scn.GetCultureTokensOn(finalcount));
		assertEquals(8, scn.GetStrength(gimli));
	}
}
