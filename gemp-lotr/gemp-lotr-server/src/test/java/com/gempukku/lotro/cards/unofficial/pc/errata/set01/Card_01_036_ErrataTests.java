package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_036_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("feet", "51_36");

					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, vitality 3
					put("lorien", "1_53");        // Lorien Elf: Elf companion, cost 1, vitality 2; to play, spot an Elf
					put("silinde", "1_60");       // Silinde, Elf of Mirkwood: Elf ally, vitality 2
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man

					put("runner1", "1_178");      // Goblin Runner: [moria] Orc
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3
					put("troll", "1_165");        // Cave Troll of Moria: [moria] Troll
					put("chaff1", "1_3");         // Axe Strike: a Free Peoples event
					put("chaff2", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CurseTheirFoulFeetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Curse Their Foul Feet!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert an Elf to reveal 4 cards at random from an opponent's hand.  Make the fellowship
		 * archery total +X until the regroup phase, where X is the number of Orcs revealed.  That player may
		 * discard X cards from hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("feet");

		assertEquals("Curse Their Foul Feet!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfToRevealFourCardsAndTheArcheryTotalIsPlusOneForEachOrcAmongThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		// The Shadow player's hand: two Orcs of two cultures, an Uruk-hai, and a [moria] minion that is no Orc.
		var runner = scn.GetShadowCard("runner1");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		// Stays in his draw deck, with the other Orcs of his deck.
		var chaff = scn.GetShadowCard("chaff1");
		scn.MoveCompanionsToTable(arwen, aragorn, boromir);
		scn.MoveAlliesToTable(silinde);
		// An Elf of the [gondor] culture, and a Man of the [elven] culture who is still no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.ELF));
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, boromir, null, Culture.ELVEN));
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(runner, soldier, savage, troll);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(feet));

		scn.MarkGameLog();
		scn.FreepsPlayCard(feet);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, silinde, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(silinde);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetTwilight());

		// The four cards are shown to the player of the event; the owner of the hand is told by the game log.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troll, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(chaff, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner, soldier, savage, troll));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(chaff));

		// 2 Orcs: the Goblin Runner and the Orc Soldier.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowDecisionAvailable("Discard 2 cards from hand to prevent"));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.ShadowChooseNo();
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(4, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, runner, soldier, savage, troll);
		assertInZone(Zone.DISCARD, feet);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, boromir);
		// Arwen, vitality 3, is exhausted; a healthy Man of the [elven] culture stands beside her.
		scn.AddWoundsToChar(arwen, 2);
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, boromir, null, Culture.ELVEN));
		scn.MoveCardsToHand(feet, lorien);
		scn.MoveCardsToHand(savage);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(feet));

		scn.FreepsPlayCard(lorien);
		assertTrue(scn.FreepsPlayAvailable(feet));
		assertEquals(1, scn.GetTwilight());

		// The Lorien Elf is the only one who can exert.
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		// 1 for the Lorien Elf + 2 for the event
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOwnerOfTheHandMayDiscardOneCardOfHisChoiceToPreventABonusOfOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var ownSoldier = scn.GetFreepsCard("soldier");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		var chaff = scn.GetShadowCard("chaff1");
		scn.MoveCompanionsToTable(arwen);
		// The Free Peoples player holds an Orc of his own, which is not in "an opponent's hand".
		scn.MoveCardsToHand(feet, ownSoldier);
		scn.MoveCardsToHand(runner, savage, troll, chaff);

		scn.StartGame();
		scn.FreepsPlayCard(feet);
		scn.FreepsDismissRevealedCards();
		// 1 Orc: the Goblin Runner.
		assertTrue(scn.ShadowDecisionAvailable("Discard 1 cards from hand to prevent"));
		assertEquals(4, scn.GetShadowHandCount());

		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, savage, troll, chaff));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ownSoldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(savage);
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, savage);
		assertInZone(Zone.HAND, runner, troll, chaff);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		// The cost stays paid.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, feet);
		assertInZone(Zone.HAND, ownSoldier);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCardsOfHisChoicePreventABonusOfTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner1");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var chaff = scn.GetShadowCard("chaff1");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(runner, soldier, savage, chaff);

		scn.StartGame();
		scn.FreepsPlayCard(feet);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowDecisionAvailable("Discard 2 cards from hand to prevent"));
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, soldier, savage, chaff));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		// One of the Orcs revealed and a card that is no Orc.
		scn.ShadowChooseCards(soldier, chaff);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, soldier, chaff);
		assertInZone(Zone.HAND, runner, savage);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FromAHandOfFiveOrcsFourAreRevealedAndTheBonusIsFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(runner1, runner2, runner3, runner4, soldier);

		scn.StartGame();
		scn.FreepsPlayCard(feet);
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());

		// Whichever four they were, all are Orcs: X is 4, not the 5 he holds.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowDecisionAvailable("Discard 4 cards from hand to prevent"));

		scn.ShadowChooseNo();
		assertEquals(4, scn.GetFreepsArcheryTotal());
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusLastsThroughTheArcheryPhaseAndTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner1");
		var soldier = scn.GetShadowCard("soldier");
		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(runner, soldier, chaff1, chaff2);

		scn.StartGame();
		scn.FreepsPlayCard(feet);
		scn.FreepsDismissRevealedCards();
		scn.ShadowChooseNo();
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Two minions for the archers to shoot at and then to fight.  The fellowship has no archer: the 2 is all the event's.
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		// The Shadow player puts both archery wounds on the Troop.
		scn.PassArcheryActions();
		scn.ShadowChooseCard(troop);
		scn.ShadowChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Arwen, 6, beats the Savage, 5.  In the skirmish after hers the bonus is still there.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// Aragorn, 8, loses to the Troop, 13, and the turn comes to the regroup phase.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void WithNoOrcRevealedThereIsNoBonusAndNothingToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		// An Orc in play is not an Orc revealed; nor are the Orcs of his draw deck.
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(savage, troll, chaff1, chaff2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPlayCard(feet);
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FromAHandOfTwoBothCardsAreRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);
		scn.MoveCardsToHand(runner, savage);

		scn.StartGame();
		scn.FreepsPlayCard(feet);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowDecisionAvailable("Discard 1 cards from hand to prevent"));

		scn.ShadowChooseNo();
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AgainstAnEmptyHandItMayStillBePlayedAndOnlyItsCostIsPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feet = scn.GetFreepsCard("feet");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(feet);

		scn.StartGame();

		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(feet));

		scn.MarkGameLog();
		scn.FreepsPlayCard(feet);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, feet);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		// Nobody is shown anything or asked anything, and the game log says that there was nothing to reveal.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
	}
}
