package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_91_009_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, vitality 3, cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, vitality 4, cost 4
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2, cost 1; a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: a minion, vitality 3, cost 2, site 5; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_9", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, vitality 3, cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, vitality 4, cost 4
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2, cost 1; a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: a minion, vitality 3, cost 2, site 5; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_9"
		);
	}

	protected VirtualTableScenario GetOutOfSightScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: cost 2, vitality 3; played at site 1 as the control
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: cost 4, vitality 4
					// Out of Sight and Shot: "Each time a companion is played, the Free Peoples player must exert a
					// companion."
					put("outofsight", "7_204");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_9", null
		);
	}

	@Test
	public void ExhaustedOnPlayStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_9
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: Each time a companion is played, that companion comes into play exhausted (except at site 1).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_9", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(5, card.getBlueprint().getIntensity());
	}

	@Test
	public void CompanionExhaustedWhenPlayedAfterSite1() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_9: Playing a companion at site 2+ should exhaust them.

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(gimli, aragorn);

		scn.StartGame();
		scn.SkipToSite(2);

		// Now at site 2, play Gimli
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(gimli);

		// Gimli should be exhausted (wounds = vitality - 1)
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsExhausted(gimli));
		// Only the companion played: the one already in play is not touched
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Each time: the next companion too, and by his own vitality of 4, not by a fixed two wounds
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CompanionNotExhaustedWhenPlayedAtSite1() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_9: Exception — companions played at site 1 should NOT be exhausted.

		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		scn.StartGame();

		// At site 1, play Gimli
		assertEquals(1, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(gimli);

		// Gimli should NOT be exhausted at site 1
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.IsExhausted(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowCopyAffectsFreePeoples() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_9: Uses generic "a companion" — Shadow's copy DOES affect FP.

		var scn = GetShadowScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		scn.SkipToSite(2);

		// Now at site 2, play Gimli
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(gimli);

		// Gimli should be exhausted (wounds = vitality - 1)
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsExhausted(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyAndAMinionPlayedAwayFromSite1AreNotExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(gimli, bounder);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(savage);

		// A companion, an ally and a minion are played on one board away from site 1
		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// "Comes into play exhausted": exhausted as the companion is placed, so it is exhausted already when its play is
	// answered (Q-0302).  Out of Sight and Shot answers each companion's play with an exertion the Free Peoples
	// player chooses: a companion played fresh at site 1 is among the candidates, and the one played at site 2 is
	// not.  The modifier no longer answers the play, so nobody orders it against Out of Sight and Shot.
	@Test
	public void TheCompanionIsExhaustedAsItComesIntoPlaySoItCannotBeChosenToExertForOutOfSightAndShot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOutOfSightScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var outofsight = scn.GetShadowCard("outofsight");
		scn.MoveCardsToSupportArea(outofsight);
		scn.MoveCardsToHand(gimli, aragorn);

		scn.StartGame();

		// The control: at site 1 Gimli comes in fresh, and is one of the companions who may exert.
		scn.FreepsPlayCard(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToSite(2);
		scn.FreepsPlayCard(aragorn);

		// Aragorn is in play exhausted by the time his play is answered: Frodo and Gimli may exert, and he may not.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
