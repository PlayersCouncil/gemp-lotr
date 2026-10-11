package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_014_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Southron Assassin: [raider] Man minion, twilight 4, site 4; answers only a lost skirmish
					put("assassin", "4_246");
					// Easterling Assailant: [raider] Man minion, twilight 4, site 4; a skirmish ability only
					put("assailant", "7_140");
					// Corsair Gunners: [raider] Man minion, twilight 5, site 4; a skirmish ability only
					put("gunners", "8_55");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul, twilight 6, site 3;
					// his text does nothing below 3 burdens
					put("khamul", "103_61");
					// Goblin Runner: [moria] Orc minion, twilight 1 — NOT a raider
					put("runner", "1_178");
					// Dunlending Brigand: a Man minion who is not [raider] ([dunland]), twilight 2, no text
					put("brigand", "4_10");
					// Raider Halberd: a [raider] card that is no minion (possession, twilight 0, on a [raider] Man)
					put("halberd", "7_156");
					// Sam, Son of Hamfast: [shire] Hobbit companion
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: another [shire] Hobbit companion; stronger with a weapon
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_14"
		);
	}

	protected VirtualTableScenario GetSamScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Southron Assassin: [raider] Man minion, twilight 4, site 4; answers only a lost skirmish
					put("assassin", "4_246");
					// Easterling Assailant: [raider] Man minion, twilight 4, site 4; a skirmish ability only
					put("assailant", "7_140");
					// Corsair Gunners: [raider] Man minion, twilight 5, site 4; a skirmish ability only
					put("gunners", "8_55");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul, twilight 6, site 3;
					// his text does nothing below 3 burdens
					put("khamul", "103_61");
					// Goblin Runner: [moria] Orc minion, twilight 1 — NOT a raider
					put("runner", "1_178");
					// Dunlending Brigand: a Man minion who is not [raider] ([dunland]), twilight 2, no text
					put("brigand", "4_10");
					// Raider Halberd: a [raider] card that is no minion (possession, twilight 0, on a [raider] Man)
					put("halberd", "7_156");
					// Sam, Son of Hamfast: [shire] Hobbit companion
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: another [shire] Hobbit companion; stronger with a weapon
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing,
				null, "92_14"
		);
	}

	protected VirtualTableScenario GetOpponentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Southron Assassin: [raider] Man minion, twilight 4, site 4; answers only a lost skirmish
					put("assassin", "4_246");
					// Easterling Assailant: [raider] Man minion, twilight 4, site 4; a skirmish ability only
					put("assailant", "7_140");
					// Corsair Gunners: [raider] Man minion, twilight 5, site 4; a skirmish ability only
					put("gunners", "8_55");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul, twilight 6, site 3;
					// his text does nothing below 3 burdens
					put("khamul", "103_61");
					// Goblin Runner: [moria] Orc minion, twilight 1 — NOT a raider
					put("runner", "1_178");
					// Dunlending Brigand: a Man minion who is not [raider] ([dunland]), twilight 2, no text
					put("brigand", "4_10");
					// Raider Halberd: a [raider] card that is no minion (possession, twilight 0, on a [raider] Man)
					put("halberd", "7_156");
					// Sam, Son of Hamfast: [shire] Hobbit companion
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: another [shire] Hobbit companion; stronger with a weapon
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_14", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_14
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Shadow: If 3 or more [Raider] minions have been played this phase, discard Sam.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_14", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void DiscardsSamAfterThreeRaidersPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var assassin = scn.GetShadowCard("assassin");
		var assailant = scn.GetShadowCard("assailant");
		var gunners = scn.GetShadowCard("gunners");
		var runner = scn.GetShadowCard("runner");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToHand(assassin, assailant, gunners, runner);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// Not yet available — no raiders played this phase
		assertFalse(scn.ShadowActionAvailable(mod));

		// 1 Raider played
		scn.ShadowPlayCard(assassin);
		assertFalse(scn.ShadowActionAvailable(mod));

		// 2 Raiders played
		scn.ShadowPlayCard(assailant);
		assertFalse(scn.ShadowActionAvailable(mod));

		// 2 Raiders + another minion played
		scn.ShadowPlayCard(runner);
		assertFalse(scn.ShadowActionAvailable(mod));

		// Now 3 raiders played this phase — ability should be available
		scn.ShadowPlayCard(gunners);
		assertTrue(scn.ShadowActionAvailable(mod));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		scn.ShadowUseCardAction(mod);

		assertInZone(Zone.DISCARD, sam);
		assertInZone(Zone.FREE_CHARACTERS, scn.GetRingBearer());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderNazgulCountsAndAManOfAnotherCultureAndARaiderPossessionDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var khamul = scn.GetShadowCard("khamul");
		var assassin = scn.GetShadowCard("assassin");
		var assailant = scn.GetShadowCard("assailant");
		var brigand = scn.GetShadowCard("brigand");
		var halberd = scn.GetShadowCard("halberd");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToHand(khamul, assassin, assailant, brigand, halberd);
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.SkipToPhase(Phase.SHADOW);

		// Two [raider] minions, and beside them a Man minion of another culture and a [raider] possession
		scn.ShadowPlayCard(khamul);
		scn.ShadowPlayCard(assassin);
		scn.ShadowPlayCard(brigand);
		scn.ShadowPlayCard(halberd);
		assertAttachedTo(halberd, assassin);
		assertFalse(scn.ShadowActionAvailable(mod));

		// The third [raider] minion: a Nazgul and two Men
		scn.ShadowPlayCard(assailant);
		assertTrue(scn.ShadowActionAvailable(mod));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		scn.ShadowUseCardAction(mod);

		// Sam, and no other Hobbit of the Shire: nothing is asked
		assertInZone(Zone.DISCARD, sam);
		assertInZone(Zone.FREE_CHARACTERS, merry, frodo);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CountResetsAcrossShadowPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var assassin = scn.GetShadowCard("assassin");
		var assailant = scn.GetShadowCard("assailant");
		var gunners = scn.GetShadowCard("gunners");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToHand(assassin, assailant, gunners);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// Play 2 raiders this phase
		scn.ShadowPlayCard(assassin);
		scn.ShadowPlayCard(assailant);
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves again: a second Shadow phase of the same turn
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// New shadow phase — count should have reset: the third raider of the turn is the first of this phase
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		scn.ShadowPlayCard(gunners);
		assertInZone(Zone.SHADOW_CHARACTERS, assassin, assailant, gunners);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DoesNotDiscardRingBearerSam() throws DecisionResultInvalidException, CardNotFoundException {
		// If Sam is the ring-bearer, he should not be a valid target
		var scn = GetSamScenario();

		var mod = scn.GetShadowCard("mod");
		var assassin = scn.GetShadowCard("assassin");
		var assailant = scn.GetShadowCard("assailant");
		var gunners = scn.GetShadowCard("gunners");
		var sam = scn.GetRingBearer();

		scn.MoveCardsToHand(assassin, assailant, gunners);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(assassin);
		scn.ShadowPlayCard(assailant);
		scn.ShadowPlayCard(gunners);

		// Sam is ring-bearer — ability should not be available
		assertFalse(scn.ShadowActionAvailable(mod));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OpponentCanUseAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOpponentScenario();

		var mod = scn.GetFreepsCard("mod");
		var assassin = scn.GetShadowCard("assassin");
		var assailant = scn.GetShadowCard("assailant");
		var gunners = scn.GetShadowCard("gunners");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToHand(assassin, assailant, gunners);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(assassin);
		scn.ShadowPlayCard(assailant);
		scn.ShadowPlayCard(gunners);

		assertTrue(scn.ShadowActionAvailable(mod));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		scn.ShadowUseCardAction(mod);

		assertInZone(Zone.DISCARD, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
