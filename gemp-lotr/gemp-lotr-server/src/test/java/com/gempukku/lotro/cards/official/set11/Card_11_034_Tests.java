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

public class Card_11_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "11_34");
					put("gandalf", "8_15");     // Gandalf, Leader of Men: [gandalf] Wizard companion, strength 7, vitality 4; text only for a starting fellowship
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ALLY (Hobbit set, Q-0312); a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; +2 only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1 (exhausted)
					put("saruman", "4_173");    // Saruman, Black Traitor: [isengard] Wizard minion, vitality 4, may not be assigned to a skirmish
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("pursuer", "4_190");    // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gandalf's Staff, Ash-staff
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: +1
		 * Game Text: Bearer must be a Wizard.
		 * 	If bearer is Gandalf, he gains <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		 * 	Each time bearer wins a skirmish, choose a Shadow player who must wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Gandalf's Staff", card.getBlueprint().getTitle());
		assertEquals("Ash-staff", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedOnGandalfHeGainsMusterAndNoOtherCharacterDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(staff);

		scn.StartGame();

		assertInHand(staff);
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertTrue(scn.FreepsPlayAvailable(staff));

		scn.FreepsPlayCard(staff);

		// A Wizard companion and a Wizard ally; not a [gandalf] companion who is no Wizard, nor others.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(staff, gandalf);
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(radagast, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnAnotherWizardNeitherHeNorGandalfGainsMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(staff);

		scn.StartGame();

		assertInHand(staff);
		scn.FreepsPlayCard(staff);
		scn.FreepsChooseCard(radagast);

		assertAttachedTo(staff, radagast);
		assertFalse(scn.HasKeyword(radagast, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerWinsASkirmishTheShadowPlayerWoundsAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, savage}, new PhysicalCardImpl[]{aragorn, runner});

		assertAttachedTo(staff, gandalf);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(saruman));

		// Gandalf (7) beats the Savage (5).
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage));

		// The Shadow player wounds any minion: the one beaten, an exhausted one whose skirmish is still to come, and
		// one in no skirmish; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, runner, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		// A wound, not an exertion: the Runner, at 1 vitality, is killed.
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesASkirmishNoMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);

		assertAttachedTo(staff, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(saruman));

		// Gandalf (7) loses to the Troop (13) and lives, with 5 vitality under the staff.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenAWizardWhoDoesNotBearItWinsNoMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast);
		scn.AttachCardsTo(radagast, staff);
		assertAttachedTo(staff, radagast);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);

		assertAttachedTo(staff, radagast);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(saruman));

		// Gandalf beats the Savage, but the staff is Radagast's.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsTheShadowPlayerWoundsAMinionInTheFierceSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pursuer = scn.GetShadowCard("pursuer");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, pursuer);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.ShadowHasCardChoiceAvailable(pursuer, saruman));
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));

		// The Pursuer, fierce, is fought again, and Gandalf wins again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.ShadowHasCardChoiceAvailable(pursuer, saruman));
		scn.ShadowChooseCard(saruman);

		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
