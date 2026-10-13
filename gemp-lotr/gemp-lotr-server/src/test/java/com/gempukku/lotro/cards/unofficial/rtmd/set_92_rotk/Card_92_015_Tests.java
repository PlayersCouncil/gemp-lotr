package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_92_015_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6, vitality 3; Damage +1
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, strength 3, vitality 3, home site 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, strength 6, vitality 3; Archer
					// One Whom Men Would Follow: Maneuver event, cost 0. Exert Aragorn, and an ally is strength +2
					// and takes part in skirmishes until the regroup phase.
					put("follow", "1_109");
					put("berserker", "3_66");   // Orthanc Berserker: [isengard] Uruk-hai, strength 11, vitality 3; Damage +1
					put("party1", "1_158");     // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; Damage +1 is its whole text
					put("party2", "1_158");
					put("marksman1", "1_176");  // Goblin Marksman: [moria] Orc, strength 7, vitality 1; Archer is its whole text
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_15", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6, vitality 3; Damage +1
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, strength 3, vitality 3, home site 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, strength 6, vitality 3; Archer
					// One Whom Men Would Follow: Maneuver event, cost 0. Exert Aragorn, and an ally is strength +2
					// and takes part in skirmishes until the regroup phase.
					put("follow", "1_109");
					put("berserker", "3_66");   // Orthanc Berserker: [isengard] Uruk-hai, strength 11, vitality 3; Damage +1
					put("party1", "1_158");     // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; Damage +1 is its whole text
					put("party2", "1_158");
					put("marksman1", "1_176");  // Goblin Marksman: [moria] Orc, strength 7, vitality 1; Archer is its whole text
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_15"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_15
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Your Dwarves are strength +1 and cannot take more than one wound during each skirmish phase.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_15", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void DwarfGetsStrengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// Gimli base 6 + 1 from modifier = 7
		assertEquals(7, scn.GetStrength(gimli));
	}

	@Test
	public void DamagePlusOneOnlyInflictsOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		// Orthanc Berserker is damage +1 (normally 2 wounds on skirmish win).
		// Modifier should cap Gimli at 1 wound per skirmish.
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var berserker = scn.GetShadowCard("berserker");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(berserker);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, berserker);
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// Gimli lost the skirmish (7 vs 11, not overwhelmed). Damage +1 would normally mean 2 wounds.
		// But modifier caps at 1 wound per skirmish phase.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThreatWoundsCappedAtOnePerSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		// 3 companions + Frodo. Kill one in skirmish. 2 threats = 2 wounds to distribute.
		// Gimli can take the first threat wound but not the second.
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var berserker = scn.GetShadowCard("berserker");

		scn.MoveCompanionsToTable(gimli, legolas, aragorn);
		scn.MoveMinionsToTable(berserker);

		scn.StartGame();
		scn.AddThreats(2);
		// Wound legolas to 2 wounds so berserker's damage+1 kill will go through
		scn.AddWoundsToChar(legolas, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, berserker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(legolas);
		scn.PassCurrentPhaseActions();

		// Legolas killed (str 6 vs 11, already had 2 wounds of his vitality 3 = dead)
		assertInZone(Zone.DEAD, legolas);

		// 2 threat wounds to assign. Choose Gimli for the first.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Second threat wound — Gimli already took 1 wound this skirmish phase,
		// so he should NOT be offered as a choice. It goes to someone else.
		// Aragorn or Frodo should take it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DoesNotApplyWhenOwnedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var berserker = scn.GetShadowCard("berserker");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(berserker);

		scn.StartGame();

		// No strength bonus — shadow owns the modifier
		assertEquals(6, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, berserker);
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// And no cap: Gimli lost (6 vs 11, not overwhelmed) and Damage +1 makes it 2 wounds.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyIsStrengthPlusOneAndAManAndAnElfAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");

		scn.MoveCompanionsToTable(gimli, aragorn, legolas);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		// A Dwarf companion and a Dwarf ally: Gimli 6 + 1, Grimir 3 + 1.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(grimir));
		// The Man and the Elf keep their printed strength.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoWoundsInTheArcheryPhaseAreBothTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);

		// Two Shadow archers: a minion archery total of 2, and the archery phase is no skirmish phase.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ADwarfAllyInASkirmishTakesOneWoundAndAManTakesBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(follow);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);

		// One Whom Men Would Follow: Aragorn exerts, and Grimir fights until the regroup phase
		// at strength 3 + 1 from the modifier + 2 from the event.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(grimir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{grimir, party1}, new PhysicalCardImpl[]{aragorn, party2});

		// Each loses to a Raiding Party's 9 without being overwhelmed, and Damage +1 makes it 2 wounds.
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.FreepsResolveSkirmish(grimir);
		scn.PassCurrentPhaseActions();
		// The Dwarf ally takes the first and not the second.
		assertEquals(1, scn.GetWoundsOn(grimir));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		// The Man, a companion who is no Dwarf, takes both: 1 from his exertion + 2.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheCapRenewsInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var berserker = scn.GetShadowCard("berserker");

		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		// The Berserker made fierce, so that Gimli loses to it in two skirmish phases of one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, berserker, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, berserker);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, berserker);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// One wound in each skirmish phase. A cap for the whole turn would leave him at 1, and with no cap
		// the two skirmishes' four wounds would have killed him (vitality 3).
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
