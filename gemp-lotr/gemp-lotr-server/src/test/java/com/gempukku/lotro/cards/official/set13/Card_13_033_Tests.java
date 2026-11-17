package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_033_Tests
{

	// Gandalf is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("legolas", "1_50");      // Legolas, Greenleaf: [elven] Elf; an archery ability used only on purpose
					put("bounder", "1_286");     // Bounder: a [shire] ally, a character who is no companion; a skirmish ability only
					put("sleep", "1_84");        // Sleep, Caradhras: a [gandalf] event
					put("avenger", "6_27");      // Ent Avenger: a [gandalf] companion
					put("alatar", "13_28");      // Alatar, Final Envoy: a [gandalf] follower
					put("bounder2", "1_286");    // a second Bounder: a card in hand that is not [gandalf]
				}},
				VirtualTableScenario.FellowshipSites,
				"13_33",
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo is the Ring-bearer, and Gandalf a companion like any other.
	protected VirtualTableScenario GetFrodoScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "13_33");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("sleep", "1_84");
					put("avenger", "6_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Gandalf, Bearer of Obligation
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Race: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 5
		 * Can start with the Ring: true
		 * Game Text: While Gandalf is the Ring-bearer, each time the fellowship moves, add 3 burdens unless you spot
		 * 	2 other companions and discard 2 [gandalf] cards from hand.
		 */

		var scn = GetFrodoScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Bearer of Obligation", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(card.getBlueprint().canStartWithRing());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void AsRingBearerEachMoveAddsThreeBurdensWhenTheDiscardIsNotChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sleep = scn.GetFreepsCard("sleep");
		var avenger = scn.GetFreepsCard("avenger");
		var bounder2 = scn.GetFreepsCard("bounder2");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToHand(sleep, avenger, bounder2);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		scn.FreepsPass();

		// Two other companions to spot and two [gandalf] cards in hand: either may be done.
		assertTrue(scn.FreepsChoiceAvailable("Add 3 burdens"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		scn.FreepsChoose("Add 3 burdens");

		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(sleep, avenger, bounder2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoGandalfCardsDiscardedSpareTheBurdensAndTheNextMoveWithTooFewAddsThemUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sleep = scn.GetFreepsCard("sleep");
		var avenger = scn.GetFreepsCard("avenger");
		var alatar = scn.GetFreepsCard("alatar");
		var bounder2 = scn.GetFreepsCard("bounder2");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToHand(sleep, avenger, alatar, bounder2);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.FreepsChoiceAvailable("Add 3 burdens"));
		scn.FreepsChoose("Discard");

		// An event, a companion and a follower of [gandalf] are offered; the Bounder is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, avenger, alatar));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder2));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sleep, avenger);

		assertInDiscard(sleep, avenger);
		assertInHand(alatar, bounder2);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minion: on to the regroup phase, and a second move with one [gandalf] card left in hand.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetBurdens());
		assertInHand(alatar, bounder2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyOneOtherCompanionBesideAnAllyTheBurdensAreAddedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sleep = scn.GetFreepsCard("sleep");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sleep, avenger);

		scn.StartGame();

		// Gandalf and Aragorn are two companions, but Aragorn is the only OTHER one; the Bounder is an ally.
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertInHand(sleep, avenger);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileFrodoIsTheRingBearerAMoveAddsNoBurdenAndAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var sleep = scn.GetFreepsCard("sleep");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, aragorn, legolas);
		scn.MoveCardsToHand(sleep, avenger);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertInHand(sleep, avenger);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
