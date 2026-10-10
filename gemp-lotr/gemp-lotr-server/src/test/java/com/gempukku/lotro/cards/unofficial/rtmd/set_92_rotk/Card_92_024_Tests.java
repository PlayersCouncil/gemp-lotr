package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_024_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Enquea, Lieutenant of Morgul: a unique [wraith] Nazgul, cost 6
					put("enquea1", "1_231");
					put("enquea2", "1_231");
					put("enquea3", "1_231");
					put("enquea4", "1_231");
					// Ulaire Attea, Khamul of the East: a unique Nazgul who is [raider], cost 6
					put("khamul1", "103_61");
					put("khamul2", "103_61");
					// Orthanc Berserker: a unique [isengard] Uruk-hai, cost 5
					put("berserker1", "3_66");
					put("berserker2", "3_66");
					// Was He Dear to Thee?: [sauron] Maneuver event, cost 3: exert a unique [sauron] or [wraith] minion
					// and hinder the Ring-bearer to make each minion strength +2
					put("washe", "103_104");
					// Ulaire Nertea, Messenger of Dol Guldur (errata): a Nazgul, cost 4; when played, for each
					// companion over 4 you may play a unique [wraith] minion from your discard pile
					put("nertea", "51_234");
					// Gothmog, Morgul Commander: a unique [wraith] minion who is an Orc and no Nazgul, cost 7
					put("gothmog", "8_72");
					// Dwarf Guard: a companion who is not unique
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_24"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Enquea, Lieutenant of Morgul: a unique [wraith] Nazgul, cost 6
					put("enquea1", "1_231");
					put("enquea2", "1_231");
					put("enquea3", "1_231");
					put("enquea4", "1_231");
					// Ulaire Attea, Khamul of the East: a unique Nazgul who is [raider], cost 6
					put("khamul1", "103_61");
					put("khamul2", "103_61");
					// Orthanc Berserker: a unique [isengard] Uruk-hai, cost 5
					put("berserker1", "3_66");
					put("berserker2", "3_66");
					// Was He Dear to Thee?: [sauron] Maneuver event, cost 3: exert a unique [sauron] or [wraith] minion
					// and hinder the Ring-bearer to make each minion strength +2
					put("washe", "103_104");
					// Ulaire Nertea, Messenger of Dol Guldur (errata): a Nazgul, cost 4; when played, for each
					// companion over 4 you may play a unique [wraith] minion from your discard pile
					put("nertea", "51_234");
					// Gothmog, Morgul Commander: a unique [wraith] minion who is an Orc and no Nazgul, cost 7
					put("gothmog", "8_72");
					// Dwarf Guard: a companion who is not unique
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_24", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_24
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Your Nazgul are non-unique.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_24", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void CanPlayDuplicateNazgulWhenModifierActive() throws DecisionResultInvalidException, CardNotFoundException {
		// With the modifier, three copies of Enquëa are in play and a fourth is played beside them:
		// "non-unique" is no cap of two or three. And two of Khamul, the Nazgul who is not [wraith]
		var scn = GetShadowScenario();

		var enquea1 = scn.GetShadowCard("enquea1");
		var enquea2 = scn.GetShadowCard("enquea2");
		var enquea3 = scn.GetShadowCard("enquea3");
		var enquea4 = scn.GetShadowCard("enquea4");
		var khamul1 = scn.GetShadowCard("khamul1");
		var khamul2 = scn.GetShadowCard("khamul2");

		scn.MoveMinionsToTable(enquea1, enquea2, enquea3, khamul1);
		scn.MoveCardsToHand(enquea4, khamul2);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.SkipToPhase(Phase.SHADOW);

		assertInZone(Zone.SHADOW_CHARACTERS, enquea1);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea2);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea3);
		assertInZone(Zone.HAND, enquea4);
		assertTrue(scn.ShadowPlayAvailable(enquea4));
		scn.ShadowPlayCard(enquea4);

		assertInZone(Zone.SHADOW_CHARACTERS, enquea1);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea2);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea3);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea4);

		assertInZone(Zone.SHADOW_CHARACTERS, khamul1);
		assertInZone(Zone.HAND, khamul2);
		assertTrue(scn.ShadowPlayAvailable(khamul2));
		scn.ShadowPlayCard(khamul2);

		assertInZone(Zone.SHADOW_CHARACTERS, khamul1);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotPlayDuplicateNazgulWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// Without the modifier (Freeps owns it), uniqueness is enforced normally
		var scn = GetFreepsScenario();

		var enquea1 = scn.GetShadowCard("enquea1");
		var enquea2 = scn.GetShadowCard("enquea2");

		scn.MoveMinionsToTable(enquea1);
		scn.MoveCardsToHand(enquea2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertInZone(Zone.SHADOW_CHARACTERS, enquea1);
		assertInZone(Zone.HAND, enquea2);
		assertFalse(scn.ShadowPlayAvailable(enquea2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NonNazgulUniquenessUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Orthanc Berserker is a unique minion who is no Nazgul: he stays unique,
		// and his second copy cannot be played.
		var scn = GetShadowScenario();

		var berserker1 = scn.GetShadowCard("berserker1");
		var berserker2 = scn.GetShadowCard("berserker2");

		scn.MoveMinionsToTable(berserker1);
		scn.MoveCardsToHand(berserker2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertInZone(Zone.SHADOW_CHARACTERS, berserker1);
		assertInZone(Zone.HAND, berserker2);
		assertFalse(scn.ShadowPlayAvailable(berserker2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WasHeDearCannotExertNonUniqueNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		// Was He Dear to Thee? requires exerting a unique [Sauron] or [Ringwraith] minion.
		// With 92_24 active, Enquëa is no longer unique, so the event should not be playable
		// if he's the only qualifying minion.
		var scn = GetShadowScenario();

		var enquea1 = scn.GetShadowCard("enquea1");
		var washe = scn.GetShadowCard("washe");

		scn.MoveMinionsToTable(enquea1);
		scn.MoveCardsToHand(washe);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.HAND, washe);
		assertEquals(0, scn.GetWoundsOn(enquea1));
		assertFalse(scn.ShadowPlayAvailable(washe));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WasHeDearCanExertTheNazgulWhileTheModifierIsTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		// The same board with the modifier across the table: Enquëa is unique, and the event can be played
		var scn = GetFreepsScenario();

		var enquea1 = scn.GetShadowCard("enquea1");
		var washe = scn.GetShadowCard("washe");

		scn.MoveMinionsToTable(enquea1);
		scn.MoveCardsToHand(washe);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.HAND, washe);
		assertEquals(0, scn.GetWoundsOn(enquea1));
		assertTrue(scn.ShadowPlayAvailable(washe));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NerteaPlaysGothmogButNotEnqueaFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		// Nertëa's trigger plays a unique [wraith] minion from discard.
		// With 92_24 active, Enquëa is non-unique and should not be a valid target.
		// Gothmog is a unique [Ringwraith] Orc (not a Nazgul), so he should still be unique
		// and auto-chosen as the only valid target. Four Dwarf Guards and Frodo are the five companions.
		var scn = GetShadowScenario();

		var nertea = scn.GetShadowCard("nertea");
		var enquea1 = scn.GetShadowCard("enquea1");
		var gothmog = scn.GetShadowCard("gothmog");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");

		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4);
		scn.MoveCardsToDiscard(enquea1, gothmog);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.SkipToPhase(Phase.SHADOW);

		assertInZone(Zone.DISCARD, gothmog);
		assertInZone(Zone.DISCARD, enquea1);

		// Play Nertëa — triggers the "play unique wraith minion from discard" effect
		scn.ShadowPlayCard(nertea);
		// Accept the optional trigger (one offer: since Q-0121 the play is not asked a second time)
		scn.ShadowAcceptOptionalTrigger();
		// Gothmog should auto-play as the only valid unique wraith minion in discard
		assertInZone(Zone.SHADOW_CHARACTERS, gothmog);
		// Enquëa should still be in discard — no longer unique
		assertInZone(Zone.DISCARD, enquea1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
