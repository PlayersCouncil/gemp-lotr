package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_317_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale", "1_317");
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion (cheated in)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a Hobbit minion; abilities used only on purpose

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Hobbit companion who comes into play after the ability is used: Éomer, Éored Leader plays a [rohan]
	// companion from the draw deck in the maneuver phase, and Merry, Master Holbytla is a [rohan] Hobbit.
	protected VirtualTableScenario GetLateHobbitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale", "1_317");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit companion; protects [shire] tales from the opponent
					put("eomer", "12_112");     // Éomer, Éored Leader: "Maneuver: If you can spot more minions than companions, exert Éomer to play a [rohan] companion from your draw deck"
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion, strength 3

					put("runner1", "1_178");    // Goblin Runners: four minions to the three companions
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThereandBackAgainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: There and Back Again
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Tale</b>. Bearer must be a Hobbit companion.
		 * <b>Maneuver:</b> Discard this condition to make each Hobbit companion strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tale");

		assertEquals("There and Back Again", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedOnAHobbitCompanionAndNotOnAnotherCompanionOrAHobbitAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(tale);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(tale));
		scn.FreepsPlayCard(tale);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, bounder));
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(tale, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsItToMakeEachHobbitCompanionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(merry, tale);

		scn.StartGame();
		scn.MoveMinionsToTable(ted);

		scn.SkipToPhase(Phase.MANEUVER);
		// Frodo 3 +1 from The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.FreepsActionAvailable(tale));

		scn.FreepsUseCardAction(tale);

		assertInDiscard(tale);
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(5, scn.GetStrength(pippin));
		// No Hobbit, a Hobbit who is no companion, and a Hobbit minion.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishesAndEndsAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, tale);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(tale);
		assertEquals(5, scn.GetStrength(merry));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{merry, runner2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Merry's skirmish, after Aragorn's has been resolved.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishing(merry));
		assertEquals(5, scn.GetStrength(merry));
		// Frodo 3 +1 from The Ruling Ring +2.
		assertEquals(6, scn.GetStrength(frodo));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(frodo));
	}

	@Test
	public void NotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, tale);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(tale));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tale));
		assertAttachedTo(tale, merry);
	}

	@Test
	public void AHobbitCompanionWhoComesIntoPlayAfterwardsIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLateHobbitScenario();

		var tale = scn.GetFreepsCard("tale");
		var pippin = scn.GetFreepsCard("pippin");
		var eomer = scn.GetFreepsCard("eomer");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, eomer);
		scn.AttachCardsTo(pippin, tale);

		scn.StartGame();
		scn.MoveMinionsToTable("runner1", "runner2", "runner3", "runner4");

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(tale);
		assertEquals(5, scn.GetStrength(pippin));
		// Frodo 3 +1 from The Ruling Ring +2.
		assertEquals(6, scn.GetStrength(frodo));

		scn.ShadowPass();
		// Four minions to three companions: Éomer plays Merry from the draw deck.
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
