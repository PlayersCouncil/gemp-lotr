package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_008_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, vitality 3
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_8", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, vitality 3
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_8"
		);
	}

	// Galadriel bears the Ring (vitality 3): a Ring-bearer who is not Frodo, with no Frodo beside her.
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, cost 2; Ring-bound, and not the Ring-bearer
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing,
				"92_8", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_8
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Each time you play a companion, you must exert the Ring-bearer.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_8", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(4, card.getBlueprint().getIntensity());
	}

	@Test
	public void PlayingCompanionExertsRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		// Playing multiple companions should trigger multiple exertions.

		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToHand(aragorn, gimli);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetWoundsOn(frodo));
		// The companions played are not the ones who exert, and nothing is asked.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentPlayingCompanionDoesNotExert() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_8: "you play" — Shadow has the modifier, so Freeps playing a companion should not trigger it.

		var scn = GetShadowScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(aragorn);

		// Frodo should NOT be exerted — the modifier belongs to Shadow, not Freeps
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Nor is the Ring-bearer of the modifier's owner, who played nothing.
		assertEquals(0, scn.GetWoundsOn(scn.GetShadowRingBearer()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAnAllyDoesNotExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(bounder, gimli);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(bounder));

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(0, scn.GetWoundsOn(frodo));

		// A companion played on the same board is what exerts him.
		scn.FreepsPlayCard(gimli);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRingBearerWhoIsNotFrodoExertsUntilSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(sam, gimli, aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(galadriel));

		// Sam is Ring-bound and no Ring-bearer: it is Galadriel who exerts, not he.
		scn.FreepsPlayCard(sam);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));

		// Vitality 3 with 2 wounds: exhausted. She cannot exert, which does not stop a companion
		// being played, and an exertion is no wound: she takes none.
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
