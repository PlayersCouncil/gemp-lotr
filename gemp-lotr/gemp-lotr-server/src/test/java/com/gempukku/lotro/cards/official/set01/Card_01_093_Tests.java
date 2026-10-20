package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fate", "1_93");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4
					put("rohanaragorn", "17_93");   // Aragorn, Defender of Rohan: the [rohan] Aragorn, strength 8, vitality 4
					put("arwen", "1_30");           // Arwen, Daughter of Elrond: companion, strength 6, vitality 3 (+3 only against a Nazgul)
					// Arwen, Lady of Rivendell: the Arwen who is an ally, strength 6, vitality 3; "Aragorn is strength +1"
					put("arwenally", "102_6");
					put("boromir", "1_97");         // Boromir, Son of Denethor: a [gondor] companion who is no Aragorn, strength 7
					put("lorienelf", "1_53");       // Lorien Elf: an [elven] companion who is no Arwen, strength 4, no archer

					put("savage1", "1_151");        // Uruk Savage: strength 5, vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwensFateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Arwen's Fate
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert Aragorn to make Arwen strength +3, or exert Arwen to make Aragorn strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fate");

		assertEquals("Arwen's Fate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingAragornMakesArwenStrengthPlus3UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir, lorienelf);
		scn.MoveCardsToHand(fate);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes, in which the Free Peoples player is asked for actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fate));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{arwen, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fate));

		scn.FreepsPlayCard(fate);

		// Both can exert, so both halves are offered.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Aragorn"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen"));
		scn.FreepsChoose("Exert Aragorn");

		// Arwen, who is in a skirmish still to come and not in this one, is strength +3.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertInZone(Zone.DISCARD, fate);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(arwen);

		// Each skirmish is a phase of its own: in Arwen's own skirmish the bonus is gone.
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAragornExhaustedArwenExertsUnaskedToMakeAragornStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveCardsToHand(fate);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.  Boromir, a [gondor] Man who is no Aragorn, can.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsPlayAvailable(fate));

		scn.FreepsPlayCard(fate);

		// "Exert Aragorn" cannot be paid, so the other half is carried out with no question.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheArwenWhoIsAnAllyMayExertToMakeTheRohanAragornStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var arwen = scn.GetFreepsCard("arwenally");
		var boromir = scn.GetFreepsCard("boromir");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(arwen);
		scn.MoveCardsToHand(fate);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{boromir, savage2});
		// Aragorn is not in the skirmish being resolved: the text names no skirmish.
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		// 8, +1 from the ally Arwen's own text.
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsPlayAvailable(fate));

		scn.FreepsPlayCard(fate);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Aragorn"));
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen"));
		scn.FreepsChoose("Exert Arwen");

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(boromir));
		assertInZone(Zone.DISCARD, fate);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(aragorn);

		// Each skirmish is a phase of its own: in Aragorn's own skirmish the bonus is gone.
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheArwenAllyExhaustedTheRohanAragornExertsUnaskedToMakeHerStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var arwen = scn.GetFreepsCard("arwenally");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn, lorienelf);
		scn.MoveAlliesToTable(arwen);
		scn.MoveCardsToHand(fate);
		// Vitality 3 with 2 wounds: the ally Arwen cannot exert.  Lorien Elf, an Elf who is no Arwen, can.
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertTrue(scn.FreepsPlayAvailable(fate));

		scn.FreepsPlayCard(fate);

		// "Exert Arwen" cannot be paid, so the other half is carried out with no question.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoArwenInPlayAragornStillExertsAndNobodyIsStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn, lorienelf);
		scn.MoveCardsToHand(fate);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(lorienelf));
		// The halves go by their costs: "Exert Aragorn" can be paid, though there is no Arwen to strengthen.
		assertTrue(scn.FreepsPlayAvailable(fate));

		scn.FreepsPlayCard(fate);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertInZone(Zone.DISCARD, fate);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNeitherAragornNorArwenCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir, lorienelf);
		scn.MoveCardsToHand(fate);
		// Both exhausted; a Man who is no Aragorn and an Elf who is no Arwen could exert.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// CR 4.2 "cost": neither half's cost can be paid, so the event cannot be played.
		assertFalse(scn.FreepsPlayAvailable(fate));
	}
}
