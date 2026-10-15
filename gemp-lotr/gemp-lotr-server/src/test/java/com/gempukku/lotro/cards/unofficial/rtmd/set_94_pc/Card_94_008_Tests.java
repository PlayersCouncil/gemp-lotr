package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

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

public class Card_94_008_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unique, vitality 4 - qualifies
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: unique, vitality 4 - 2nd qualifying target
					put("gimli", "1_13");       // Gimli, Son of Gloin: unique, vitality 3 - does not qualify
					put("backstabber", "1_174"); // Goblin Backstabber: minion, needed so maneuver phase isn't auto-skipped
					put("maneuverEvent", "1_103"); // Elendil's Valor: Freeps Maneuver event, no requirements
					put("shadowManeuverEvent", "1_126"); // Hunt Them Down!: Shadow Maneuver event, cost 3, no requirements
					put("sam", "1_311");        // Sam, Son of Hamfast: unique, vitality 4, Ring-bound and no Ring-bearer - qualifies
					put("saga", "1_114");       // The Saga of Elendil: a condition a [gondor] companion bears, vitality +1; a skirmish ability never used
					put("avenger", "6_27");     // Ent Avenger: a companion of vitality 4 who is not unique; no text below 3 Ents
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: a unique ally of vitality 4; a fellowship ability never used
					put("saruman", "17_116");   // Saruman, Master of the White Hand: a unique minion of vitality 4; each companion is resistance -1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_8", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unique, vitality 4 - qualifies
					put("backstabber", "1_174"); // Goblin Backstabber: minion, needed so maneuver phase isn't auto-skipped
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_8"
		);
	}

	// Sam, Bearer of Great Need (unique, vitality 4) bears the Ring: a Ring-bearer who is not Frodo.
	protected VirtualTableScenario GetSamRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: minion, needed so maneuver phase isn't auto-skipped
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing,
				"94_8", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_8
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Maneuver: If the fellowship is in region 2, place your unique companion with
		 * 4 or more vitality (except the Ring-bearer) in the dead pile to skip to the regroup phase.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_8", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void KillingAQualifyingCompanionInRegionTwoSkipsToRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer(); // vitality 4, but excluded for being the Ring-bearer
		var backstabber = scn.GetShadowCard("backstabber");
		var maneuverEvent = scn.GetFreepsCard("maneuverEvent");
		var shadowManeuverEvent = scn.GetShadowCard("shadowManeuverEvent");

		scn.MoveCompanionsToTable(aragorn, gandalf, gimli);
		scn.MoveCardsToHand(maneuverEvent, shadowManeuverEvent);
		scn.StartGame();
		scn.SkipToSite(4); // region 2
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		// The fellowship has moved on from site 4 to site 5, both in region 2.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(maneuverEvent));
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Filter specificity: only the two vitality-4 uniques qualify; Gimli (vitality 3) and Frodo
		// (vitality 4, but the Ring-bearer) do not
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertInPlay(gandalf, gimli);

		// The rest of this maneuver phase is locked out for both sides - neither eligible
		// Maneuver event can be used once the ability has been activated.  Each is read while its
		// player is the one being asked; Hunt Them Down! costs 3 and there is more than that in the pool.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.GetTwilight() >= 3);
		assertFalse(scn.ShadowPlayAvailable(shadowManeuverEvent));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(maneuverEvent));
		// Nor may Gandalf be placed in the dead pile as well.
		assertFalse(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();

		// Both players pass out of the (now action-less) maneuver phase; skipped straight
		// through archery/assignment/skirmish to regroup
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyAUniqueCompanionOfFourOrMoreVitalityWhoIsNotTheRingBearerMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var elrond = scn.GetFreepsCard("elrond");
		var frodo = scn.GetRingBearer();
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCompanionsToTable(aragorn, sam, gandalf, avenger);
		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(aragorn, saga);
		// Gandalf's printed vitality is 4; with a wound on him he has 3.
		scn.AddWoundsToChar(gandalf, 1);
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		// The fellowship has moved from site 3 to site 4, the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetVitality(aragorn));
		assertEquals(4, scn.GetVitality(sam));
		assertEquals(3, scn.GetVitality(gandalf));
		assertEquals(4, scn.GetVitality(avenger));
		assertEquals(4, scn.GetVitality(elrond));
		assertEquals(4, scn.GetVitality(saruman));
		assertTrue(scn.FreepsActionAvailable(mod));

		scn.FreepsUseCardAction(mod);

		// A [gondor] Man of 5 vitality and a Ring-bound [shire] Hobbit of exactly 4.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam));
		// Unique, but 3 vitality left; 4 vitality, but not unique; unique with 4, but an ally; unique with 4,
		// but a minion and the other player's; unique with 4, but the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertInZone(Zone.FREE_CHARACTERS, sam);

		scn.FreepsChooseCard(sam);

		assertInZone(Zone.DEAD, sam);
		assertInPlay(aragorn, gandalf, avenger, elrond, saruman);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.PassCurrentPhaseActions();

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UnavailableOutsideRegionTwo() throws DecisionResultInvalidException, CardNotFoundException {
		// Region 1 (the first move, from site 1 to site 2)
		var scn1 = GetFreepsScenario();
		var mod1 = scn1.GetFreepsCard("mod");
		var aragorn1 = scn1.GetFreepsCard("aragorn");
		var backstabber1 = scn1.GetShadowCard("backstabber");
		scn1.MoveCompanionsToTable(aragorn1);
		scn1.StartGame();
		scn1.MoveMinionsToTable(backstabber1);
		scn1.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn1.GetCurrentSiteNumber());
		assertTrue(scn1.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn1.FreepsActionAvailable(mod1));

		// Region 3 (the move from site 7 to site 8)
		var scn2 = GetFreepsScenario();
		var mod2 = scn2.GetFreepsCard("mod");
		var aragorn2 = scn2.GetFreepsCard("aragorn");
		var backstabber2 = scn2.GetShadowCard("backstabber");
		scn2.MoveCompanionsToTable(aragorn2);
		scn2.StartGame();
		scn2.SkipToSite(7);
		scn2.MoveMinionsToTable(backstabber2);
		scn2.SkipToPhase(Phase.MANEUVER);
		assertEquals(8, scn2.GetCurrentSiteNumber());
		assertTrue(scn2.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn2.FreepsActionAvailable(mod2));
	}

	@Test
	public void UnavailableWithoutAQualifyingCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(gimli);
		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void ARingBearerWhoIsNotFrodoMayNotBeChosenEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamRingBearerScenario();
		var mod = scn.GetFreepsCard("mod");
		var sam = scn.GetRingBearer();
		var backstabber = scn.GetShadowCard("backstabber");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		// The fellowship has moved from site 5 to site 6, the last site of region 2.
		assertEquals(6, scn.GetCurrentSiteNumber());
		// Sam is unique and has 4 vitality: only bearing the Ring keeps him from paying.
		assertEquals("Sam", sam.getBlueprint().getTitle());
		assertEquals(4, scn.GetVitality(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void TheOwnerAsPlayerTwoPlacesHisCompanionInTheDeadPileOnHisOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetShadowCard("aragorn");
		var backstabber = scn.GetFreepsCard("backstabber");

		scn.StartGame();
		scn.SkipToSite(3);
		// Player 2's third turn: he is the Free Peoples player, the modifier is his, and his fellowship is at site 3.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhaseInverted(Phase.MANEUVER);

		// His fellowship has moved from site 3 to site 4, the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Aragorn is the only companion of his who may pay, so nobody is asked which.
		assertInZone(Zone.DEAD, aragorn);

		// Player 1, the Shadow player this turn, is asked next; then player 2.
		scn.FreepsPass();
		scn.ShadowPass();

		assertInZone(Zone.SHADOW_CHARACTERS, backstabber);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void OwnerGatingShadowCantUseEvenInRegionTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		// Player 1 has Aragorn to pay with, but the modifier is not his.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}
}
