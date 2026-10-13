package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_10_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glimpse", "10_12");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion
					put("bow", "1_41");         // Elven Bow: [elven] possession, bearer must be an Elf
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability, never used
					// Smeagol, Poor Creature: "Skirmish: Discard a card from hand to make Smeagol strength +1."
					put("smeagol", "6_45");
					// Ranger of Osgiliath: "Regroup: Discard 2 cards from hand to exhaust a minion."
					put("ranger", "7_116");
					// Elendil's Valor: Maneuver event, cost 0: "Make a [gondor] companion defender +1 until the regroup phase."
					put("valor", "1_103");
					put("valor2", "1_103");
					put("valor3", "1_103");
					// Dwarven Armor: cards to hold in hand and nothing else.
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");
					put("filler4", "1_8");
					put("filler5", "1_8");
					put("filler6", "1_8");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlimpseofFateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Glimpse of Fate
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 Elves.<br>Each time you lose initiative, make a minion strength -4 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glimpse");

		assertEquals("Glimpse of Fate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void LosingInitiativeMakesAMinionStrengthMinusFourUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(valor, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		// Four cards to three: initiative is lost with the play of the event.  The text has no "may": the choice
		// of a minion is put at once, and one must be chosen.
		scn.FreepsPlayCard(valor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// 13 - 4
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// It lasts through the phases that follow: read in the assignment phase and in a skirmish the minion is
		// no part of.
		scn.SkipToAssignments();
		assertEquals(9, scn.GetStrength(troop));
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(9, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// And it is over at the first decision of the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
	}
	@Test
	public void CannotBePlayedUntilTwoElvesAreSpottedAndThenGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var legolas = scn.GetFreepsCard("legolas");
		var celeborn = scn.GetFreepsCard("celeborn");
		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		// One Elf, who bears an [elven] card that is no Elf, and a companion who is no Elf.
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(glimpse, celeborn);

		scn.StartGame();

		assertAttachedTo(bow, legolas);
		assertFalse(scn.FreepsPlayAvailable(glimpse));

		// An Elf ally beside the Elf companion makes two.
		scn.FreepsPlayCard(celeborn);
		assertTrue(scn.FreepsPlayAvailable(glimpse));

		scn.FreepsPlayCard(glimpse);
		assertInZone(Zone.SUPPORT, glimpse);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHandGoingFromThreeToTwoOrFromFiveToFourLosesNoInitiativeAndAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor1 = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var valor3 = scn.GetFreepsCard("valor3");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(valor1, valor2, valor3);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Three cards to two: the Shadow has initiative already, so none is lost.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor1);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Three more cards make five: the Free Peoples player has initiative from the next answer on.
		scn.MoveCardsToHand(filler1, filler2, filler3);
		scn.ShadowPass();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Five cards to four: initiative is kept.
		scn.FreepsPlayCard(valor2);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		// Four cards to three: initiative is lost, and a minion must be chosen.
		scn.FreepsPlayCard(valor3);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli));
		scn.FreepsChooseCard(savage);
		// 5 - 4
		assertEquals(1, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachLossOfInitiativeWeakensAMinionAgainAndItLastsIntoTheNextSkirmishUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var smeagol = scn.GetFreepsCard("smeagol");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var filler5 = scn.GetFreepsCard("filler5");
		var filler6 = scn.GetFreepsCard("filler6");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli, smeagol);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(filler1, filler2, filler3, filler4);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, troop});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(13, scn.GetStrength(troop));

		// Smeagol's discard takes the hand from four cards to three: a loss of initiative that is no play.
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(filler1);
		assertInZone(Zone.DISCARD, filler1);
		assertFalse(scn.FreepsHasInitiative());
		// The minion in this skirmish and the one assigned to the skirmish still to come; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(troop);
		// 13 - 4
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));

		// Aragorn 8 beats the Uruk Savage 5.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// The next skirmish, a phase of its own: the Goblin Patrol Troop is still 4 down.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(troop));
		assertEquals(9, scn.GetStrength(troop));

		// Two cards more, and a discard from five to four: the Free Peoples player has initiative again, and
		// the Shadow losing it asks nothing.
		scn.MoveCardsToHand(filler5, filler6);
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(filler2);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// Four to three again: initiative is lost a second time, and the same minion is weakened again.
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(filler3);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);
		// 13 - 4 - 4
		assertEquals(5, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gimli 6 beats the Troop 5: one wound and one more for his damage +1.
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(troop));

		// The regroup phase: the Troop is 13 again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void LostInTheRegroupPhaseTheMinionStaysWeakenedThroughAnotherMoveUntilTheNextRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var ranger = scn.GetFreepsCard("ranger");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var filler5 = scn.GetFreepsCard("filler5");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(filler1, filler2, filler3, filler4, filler5);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(13, scn.GetStrength(troop));

		// Ranger of Osgiliath discards 2 cards from hand, five cards to three, and exhausts the one minion, which
		// is then the one minion to weaken: nobody is asked.
		scn.FreepsUseCardAction(ranger);
		scn.FreepsChooseCards(filler1, filler2);
		assertFalse(scn.FreepsHasInitiative());
		// 13 - 4
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(2, scn.GetWoundsOn(troop));

		// The regroup phase this was done in has already begun; the next one is after another move (Q-0160).
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetStrength(troop));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
