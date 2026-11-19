package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_93_002_Tests
{
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing,
				"93_2", null
		);
	}

	protected VirtualTableScenario GetFrodoScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_2", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing,
				null, "93_2"
		);
	}

	// Bilbo, Bearer of Things Burgled bears the Ring: a Ring-bound [shire] Hobbit of resistance 8 who is
	// neither Frodo nor Sam.
	protected VirtualTableScenario GetBilboScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6
				}},
				VirtualTableScenario.FellowshipSites,
				"9_49",
				VirtualTableScenario.RulingRing,
				"93_2", null
		);
	}

	// Sam, Bearer of Great Need bears the Ring: resistance 5, and +1 for each Hobbit he can spot.
	protected VirtualTableScenario GetSamScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing,
				"93_2", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_2
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Your Ring-bearer (except Frodo or Sam) is resistance +3.
		 */
		var scn = GetGimliScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_2", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void GimliRBGetsResistanceBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();
		var gimli = scn.GetRingBearer();

		scn.StartGame();

		// Gimli RB: 4 base resistance + 1 (damage +1 → resistance +1) = 5
		// With mod: 5 + 3 = 8
		assertEquals(8, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FrodoDoesNotGetResistanceBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();

		// Frodo is excluded — base resistance 10, no bonus
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DoesNotApplyWhenShadowOwns() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var gimli = scn.GetRingBearer();

		scn.StartGame();

		// Shadow owns the mod — Gimli should not get the bonus
		// Base resistance: 4+1
		assertEquals(5, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BilboAsRingBearerGetsResistanceBonusAndACompanionWhoBearsNoRingDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBilboScenario();

		var bilbo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Bilbo is a [shire] Hobbit and Ring-bound, as Frodo and Sam are, but the exception is by name: 8 + 3.
		assertEquals(11, scn.GetResistance(bilbo));
		// Aragorn is neither Frodo nor Sam, and no Ring-bearer.
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SamDoesNotGetResistanceBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamScenario();

		var sam = scn.GetRingBearer();

		scn.StartGame();

		// Sam is excluded: 5 printed + 1 by his own text for the one Hobbit he can spot, himself, and no bonus
		assertEquals(6, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
