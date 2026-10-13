package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_93_005_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13"); // Gimli: Dwarf companion, resistance 6, strength 6 — NOT affected
					put("merry", "11_168"); // Merry, Loyal Companion: Hobbit companion, resistance 9, strength 3 — affected (resistance <= 3 once burdened)
					put("sam", "1_311"); // Sam, Son of Hamfast: resistance 5, strength 3; exerts to remove a burden
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally, strength 2
					put("troop", "1_177"); // Goblin Patrol Troop: a minion with no text, strength 13
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_5", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13"); // Gimli: Dwarf companion, resistance 6, strength 6 — NOT affected
					put("merry", "11_168"); // Merry, Loyal Companion: Hobbit companion, resistance 9, strength 3 — affected (resistance <= 3 once burdened)
					put("sam", "1_311"); // Sam, Son of Hamfast: resistance 5, strength 3; exerts to remove a burden
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally, strength 2
					put("troop", "1_177"); // Goblin Patrol Troop: a minion with no text, strength 13
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_5"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_5
		 * Type: MetaSite
		 * Intensity: 1
		 * Game Text: Each companion with resistance 3 or less is strength -1.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_5", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getIntensity());
	}

	@Test
	public void LowResistanceCompanionLosesStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(gimli, merry);

		scn.StartGame();

		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		assertEquals(9, scn.GetResistance(merry));
		assertEquals(3, scn.GetStrength(merry));

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(4, scn.GetStrength(frodo));

		scn.AddBurdens(3);

		//Gimli is now strength -1
		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(5, scn.GetStrength(gimli));

		assertEquals(6, scn.GetResistance(merry));
		assertEquals(3, scn.GetStrength(merry));

		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(4, scn.GetStrength(frodo));

		scn.AddBurdens(3);

		assertEquals(0, scn.GetResistance(gimli));
		assertEquals(5, scn.GetStrength(gimli));

		//Merry is now strength -1
		assertEquals(3, scn.GetResistance(merry));
		assertEquals(2, scn.GetStrength(merry));

		assertEquals(4, scn.GetResistance(frodo));
		assertEquals(4, scn.GetStrength(frodo));

		scn.AddBurdens(1);

		assertEquals(0, scn.GetResistance(gimli));
		assertEquals(5, scn.GetStrength(gimli));

		//Merry is now strength -1
		assertEquals(2, scn.GetResistance(merry));
		assertEquals(2, scn.GetStrength(merry));

		assertEquals(3, scn.GetResistance(frodo));
		assertEquals(3, scn.GetStrength(frodo));
	}

	@Test
	public void AffectsOpponentCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		assertEquals(9, scn.GetResistance(merry));
		assertEquals(3, scn.GetStrength(merry));

		scn.AddBurdens(6);

		// Shadow owns the mod, but no owner-gating: merry should still be affected
		assertEquals(3, scn.GetResistance(merry));
		assertEquals(2, scn.GetStrength(merry));
	}

	@Test
	public void OnlyCompanionsAreWeakenedAndARemovedBurdenRestoresTheOneNowAtFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gimli, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.AddBurdens(3);

		// Gimli 6 - 3 burdens = 3, Sam 5 - 3 = 2: each a strength less
		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(5, scn.GetStrength(gimli));
		assertEquals(2, scn.GetResistance(sam));
		assertEquals(2, scn.GetStrength(sam));
		// An ally's resistance is zero, and he is no companion
		assertEquals(0, scn.GetResistance(bounder));
		assertEquals(2, scn.GetStrength(bounder));

		// Sam exerts to remove a burden: Gimli is at 4 and has his strength back, Sam is at 3 and has not
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(2, scn.GetStrength(sam));
		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Nor is a minion a companion
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(2, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
