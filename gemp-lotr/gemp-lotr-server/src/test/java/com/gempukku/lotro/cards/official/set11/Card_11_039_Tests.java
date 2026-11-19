package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("struggle", "11_39");
					// Gandalf, Leader of the Company: [gandalf] Wizard companion, resistance 7, vitality 4.  In region 1
					// each other companion is strength +2, which decides no skirmish read here.
					put("gandalf", "11_33");
					// Saruman, The White: a [gandalf] Wizard who is an ally (a Hobbit-set card, laid under Q-0312 as the
					// one card that tells "a [gandalf] Wizard" from a [gandalf] Wizard companion).  Gandalf is
					// defender +1 beside him; his abilities are never used.
					put("sarumanally", "32_24");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard, resistance 6
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: resistance 6; a maneuver ability never used
					put("blacktraitor", "4_173"); // Saruman, Black Traitor: an [isengard] Wizard minion who may not be assigned

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); cheated in, so its play trigger never fires
					put("detachment", "11_74"); // Detachment of Haradrim: [men] Man, strength 12, vitality 3; a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ProlongedStruggleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Prolonged Struggle
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a [gandalf] Wizard and spot 2 companions who each have resistance 7 or more to wound a minion that Wizard is skirmishing twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("struggle");

		assertEquals("Prolonged Struggle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAGandalfWizardToWoundAMinionHeIsSkirmishingTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var struggle = scn.GetFreepsCard("struggle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sarumanally = scn.GetFreepsCard("sarumanally");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(sarumanally);
		scn.MoveCardsToHand(struggle);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var detachment = scn.GetShadowCard("detachment");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, detachment);
		scn.SkipToAssignments();
		// Gandalf is defender +1 beside the ally, and takes two minions.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop, runner}, new PhysicalCardImpl[]{aragorn, detachment});
		scn.FreepsResolveSkirmish(gandalf);

		// Exactly two companions have resistance 7 or more: Frodo (10) and Gandalf himself (7).
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(7, scn.GetResistance(gandalf));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(avenger));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(struggle));
		scn.FreepsPlayCard(struggle);

		// Either [gandalf] Wizard may pay, the ally among them; not the [gandalf] companion who is no Wizard.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, sarumanally));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(sarumanally));

		// The two minions he is skirmishing, the exhausted one among them (a wound, not an exertion); not the
		// minion assigned to Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(detachment));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsChooseCard(troop);

		// Twice, on the one minion chosen.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(detachment));
		assertInDiscard(struggle);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenABurdenLeavesOnlyOneCompanionWithResistance7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var struggle = scn.GetFreepsCard("struggle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(struggle);
		assertInZone(Zone.HAND, struggle);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.FreepsGetBurdens());
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		// Each burden lowers every companion's resistance: Frodo 9, Gandalf 6, Aragorn 5.
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(gandalf));
		assertEquals(5, scn.GetResistance(aragorn));
		assertFalse(scn.FreepsPlayAvailable(struggle));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheGandalfWizardIsExhaustedBesideAWizardMinionAndAGandalfCompanionWhoCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var struggle = scn.GetFreepsCard("struggle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(struggle);
		scn.AddWoundsToChar(gandalf, 3);
		assertInZone(Zone.HAND, struggle);
		assertEquals(3, scn.GetWoundsOn(gandalf));

		var troop = scn.GetShadowCard("troop");
		var blacktraitor = scn.GetShadowCard("blacktraitor");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, blacktraitor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		// The two companions are still spotted: Frodo 10 and Gandalf 7.  Only the exertion cannot be paid.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(7, scn.GetResistance(gandalf));
		assertEquals(1, scn.GetVitality(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertFalse(scn.FreepsPlayAvailable(struggle));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInAnotherSkirmishItWoundsNeitherTheMinionThereNorTheOneWaitingForTheWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var struggle = scn.GetFreepsCard("struggle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(struggle);

		var troop = scn.GetShadowCard("troop");
		var detachment = scn.GetShadowCard("detachment");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, detachment);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{aragorn, detachment});
		// Aragorn's skirmish first; Gandalf's with the Troop waits.
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(struggle));
		scn.FreepsPlayCard(struggle);

		// Gandalf, the one Wizard, pays; he is skirmishing nobody yet, so nothing more happens.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(detachment));
		assertInDiscard(struggle);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
