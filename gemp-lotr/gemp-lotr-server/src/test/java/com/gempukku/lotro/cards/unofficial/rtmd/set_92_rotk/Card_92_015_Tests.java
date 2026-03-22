package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_92_015_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Gimli, Dwarf of Erebor (1_13): Dwarven companion, strength 6, vitality 3
		put("gimli", "1_13");
		// Aragorn, Ranger of the North (1_89): Gondor companion, strength 8, vitality 4
		put("aragorn", "1_89");
		// Legolas, Greenleaf (1_50): Elven companion, strength 6, vitality 3
		put("legolas", "1_50");
		// Orthanc Berserker: Isengard Uruk-hai minion, strength 11, vitality 3, damage +1
		put("berserker", "3_66");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_15", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
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
		 * Game Text: Your Dwarves are strength +1 and cannot take more than one wound during each skirmish phase.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_15", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
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
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// Gimli lost the skirmish (7 vs 10). Damage +1 would normally mean 2 wounds.
		// But modifier caps at 1 wound per skirmish phase.
		assertEquals(1, scn.GetWoundsOn(gimli));
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

		// Legolas killed (str 6 vs 10, damage+1 = 2 wounds, already had 2 = dead)
		assertInZone(Zone.DEAD, legolas);

		// 2 threat wounds to assign. Choose Gimli for the first.
		assertTrue(scn.FreepsHasCardChoicesAvailable(gimli, aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Second threat wound — Gimli already took 1 wound this skirmish phase,
		// so he should NOT be offered as a choice. It goes to someone else.
		// Aragorn or Frodo should take it.
		assertTrue(scn.FreepsHasCardChoicesAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(gimli));
		assertEquals(1, scn.GetWoundsOn(gimli));
	}

	@Test
	public void DoesNotApplyWhenOwnedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// No strength bonus — shadow owns the modifier
		assertEquals(6, scn.GetStrength(gimli));
	}
}
