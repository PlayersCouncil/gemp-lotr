package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_10_012_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glimpse", "60_12");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2
					put("bow", "1_41");         // Elven Bow: [elven] possession, bearer must be an Elf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, cost 2
					put("smeagol", "6_45");     // Smeagol, Poor Creature: "Skirmish: Discard a card from hand to make Smeagol strength +1."
					put("ranger", "7_116");     // Ranger of Osgiliath: "Regroup: Discard 2 cards from hand to exhaust a minion."

					// Elendil's Valor: Maneuver event, cost 0: "Make a [gondor] companion defender +1 until the regroup phase."
					put("valor1", "1_103");
					put("valor2", "1_103");
					put("valor3", "1_103");
					// Dwarven Armor: cards to hold in hand and nothing else.
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");
					put("filler4", "1_8");
					put("filler5", "1_8");
					put("filler6", "1_8");

					put("savage", "1_151");     // Uruk Savage: [isengard], strength 5, vitality 3
					put("troop", "1_177");      // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlimpseOfFateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Glimpse of Fate
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 Elves.
		 * When you lose initiative, you may make a minion strength -3 until the regroup phase (limit once per
		 * turn).
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
	public void CannotBePlayedUntilTwoElvesAreSpottedAndThenGoesToTheSupportAreaForNothing() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(glimpse));

		scn.FreepsPlayCard(glimpse);
		assertInZone(Zone.SUPPORT, glimpse);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LosingInitiativeByPlayingAnEventMayMakeAMinionStrengthMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor1 = scn.GetFreepsCard("valor1");
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

		// Three cards to two: the Shadow has initiative already, so none is lost and nothing is offered.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Three more cards make five: the Free Peoples player has initiative from the next answer on.
		scn.MoveCardsToHand(filler1, filler2, filler3);
		scn.ShadowPass();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Five cards to four: initiative is kept, and nothing is offered.
		scn.FreepsPlayCard(valor2);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		// Four cards to three: initiative is lost.
		scn.FreepsPlayCard(valor3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(troop);
		// 13 - 3
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningChangesNothingAndTheNextLossOfInitiativeThatTurnOffersAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor1 = scn.GetFreepsCard("valor1");
		var valor2 = scn.GetFreepsCard("valor2");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(valor1, valor2, filler1, filler2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A fourth card gives initiative back. Declining was no use of the limit: losing it again offers again.
		scn.MoveCardsToHand(filler3);
		scn.ShadowPass();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(valor2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(savage);
		// 5 - 3
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NothingIsOfferedWithNoMinionAndTheLimitIsNotSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var valor1 = scn.GetFreepsCard("valor1");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(gimli, valor1, filler1, filler2);

		scn.StartGame();

		// Four cards to three in the fellowship phase, where there is no minion to choose.
		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(gimli);
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Later that turn, with minions on the table and four cards in hand again.
		scn.MoveMinionsToTable(savage, troop);
		scn.MoveCardsToHand(filler3);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.FreepsPlayCard(valor1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void LosingInitiativeByADiscardWeakensAMinionUntilTheRegroupPhaseAndOnlyOnceThatTurn() throws DecisionResultInvalidException, CardNotFoundException {
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

		// Smeagol's discard takes the hand from four cards to three.
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(filler1);
		assertInZone(Zone.DISCARD, filler1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));
		scn.FreepsAcceptOptionalTrigger();
		// The minion in this skirmish and the one assigned to the skirmish still to come; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, smeagol));

		scn.FreepsChooseCard(troop);
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));

		// Aragorn 8 beats the Uruk Savage 5.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// The next skirmish, a phase of its own: the Goblin Patrol Troop is still 3 down.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(troop));
		assertEquals(10, scn.GetStrength(troop));

		// Two cards more, and a discard from five to four, give the Free Peoples player initiative again.
		scn.MoveCardsToHand(filler5, filler6);
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(filler2);
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// Four to three again: initiative is lost a second time this turn, in another phase, with minions
		// still to choose, and nothing is offered.
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(filler3);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gimli 6 loses to 10 and takes a wound; at 13 the Troop would have overwhelmed him.
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The regroup phase: the Troop is 13 again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void TheLimitRenewsOnTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glimpse = scn.GetFreepsCard("glimpse");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor1 = scn.GetFreepsCard("valor1");
		var valor2 = scn.GetFreepsCard("valor2");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(glimpse);
		scn.MoveCardsToHand(valor1, filler1, filler2, filler3);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(valor1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(troop);
		assertEquals(10, scn.GetStrength(troop));

		// The rest of this turn and the whole of the other player's.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The reconciles have refilled the hand: it is cut back to these four cards.
		scn.SetFreepsHand(valor2, filler1, filler2, filler3);
		assertEquals(4, scn.GetFreepsHandCount());
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(valor2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
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

		// Ranger of Osgiliath discards 2 cards from hand, five cards to three, and exhausts the one minion.
		scn.FreepsUseCardAction(ranger);
		scn.FreepsChooseCards(filler1, filler2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glimpse));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(2, scn.GetWoundsOn(troop));

		// The regroup phase this was done in has already begun; the next one is after another move.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(troop));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
