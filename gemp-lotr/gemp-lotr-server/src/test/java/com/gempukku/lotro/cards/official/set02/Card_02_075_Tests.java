package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ferny", "2_75");
					put("ferny2", "2_75");

					put("rider1", "12_161");    // Black Rider: [ringwraith] Nazgul, not unique, cost 5, site 3; fierce
					put("rider2", "12_161");
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgul, cost 6, site 3
					put("faces", "16_4");       // Dead Faces: [ringwraith] Wraith and not a Nazgul, cost 2, site 3
					put("runner", "1_178");     // Goblin Runner: its text acts only when it is played
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text

					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ally whose home is site 2; a skirmish ability only
					// Eowyn, Northwoman: "Assignment: Exert Eowyn and assign a minion to the Ring-bearer to make that
					// minion lose all game text keywords ..."
					put("eowyn", "17_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BillFernyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Bill Ferny, Swarthy Sneering Fellow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 2
		 * Game Text: Nazgûl are not roaming.
		 * The Free Peoples player may not assign a character to skirmish Bill Ferny. Discard Bill Ferny if underground.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ferny");

		assertEquals("Bill Ferny", card.getBlueprint().getTitle());
		assertEquals("Swarthy Sneering Fellow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void NazgulAreNotRoamingWhileBillIsInPlayAndOtherMinionsStillAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferny = scn.GetShadowCard("ferny");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var attea = scn.GetShadowCard("attea");
		var faces = scn.GetShadowCard("faces");
		scn.MoveCardsToHand(ferny, rider1, rider2, attea, faces);

		scn.StartGame();
		scn.SetTwilight(25);
		scn.FreepsPass();

		// 25 set + 1 companion + 2 for Ettenmoors
		assertEquals(28, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Before Bill is in play, a site-3 Nazgul at site 2 is roaming: 5 + 2 roaming.
		scn.ShadowPlayCard(rider1);
		assertEquals(21, scn.GetTwilight());
		assertTrue(scn.HasKeyword(rider1, Keyword.ROAMING));

		// Bill himself is a site-2 minion at site 2: 2 and no roaming.
		scn.ShadowPlayCard(ferny);
		assertEquals(19, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertFalse(scn.HasKeyword(ferny, Keyword.ROAMING));
		// The Nazgul already in play is roaming no longer.
		assertFalse(scn.HasKeyword(rider1, Keyword.ROAMING));

		// A [ringwraith] Nazgul: 5, no roaming.
		scn.ShadowPlayCard(rider2);
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.HasKeyword(rider2, Keyword.ROAMING));

		// A [raider] Nazgul: 6, no roaming.
		scn.ShadowPlayCard(attea);
		assertEquals(8, scn.GetTwilight());
		assertFalse(scn.HasKeyword(attea, Keyword.ROAMING));

		// A [ringwraith] Wraith who is not a Nazgul is still roaming: 2 + 2 roaming.
		scn.ShadowPlayCard(faces);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.HasKeyword(faces, Keyword.ROAMING));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BillFernyCannotBeAssignedByFreepsWhenAlone() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(ferny);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Eowyn could exert, and her ability has the Free Peoples player assign a minion to the Ring-bearer:
		// with Bill the only minion there is none that player may assign.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertFalse(scn.FreepsActionAvailable(eowyn));
		scn.FreepsPass();
		scn.ShadowPass();

		// Bill is not the Free Peoples player's to assign, and with no other minion that player is asked
		// nothing: the game goes straight to the Shadow player, who may assign him.
		assertFalse(scn.FreepsCanAssign(ferny));
		assertFalse(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(ferny));

		scn.ShadowAssignToMinions(frodo, ferny);
		assertTrue(scn.IsCharAssignedAgainst(frodo, ferny));
		assertTrue(scn.FreepsDecisionAvailable("skirmish"));
	}

	@Test
	public void BillFernyCannotBeAssignedByFreepsWhenWithOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);

		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(ferny, runner);

		scn.SkipToAssignments();
		// Ettenmoors, site 2, is the Bounder's home, so he may be assigned as the companions may.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));

		// Bill is not among the minions the Free Peoples player is offered, so no character can be assigned to
		// him by that player: Ring-bearer, companion or ally.
		assertFalse(scn.FreepsCanAssign(ferny));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(ferny));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		// It is Bill who is barred, and to that player: the other minion is offered, every character is offered
		// for it, and the Shadow player may assign Bill.
		assertTrue(scn.FreepsCanAssign(runner));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(gimli));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(bounder));
		assertTrue(scn.ShadowCanAssign(ferny));

		scn.FreepsAssignToMinions(frodo, runner);
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertFalse(scn.IsCharAssigned(ferny));

		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(ferny));

		scn.ShadowAssignToMinions(frodo, ferny);
		assertTrue(scn.IsCharAssignedAgainst(frodo, ferny));
		assertTrue(scn.FreepsDecisionAvailable("skirmish"));
	}

	@Test
	public void AMinionAssignedByTheFreePeoplesPlayersCardIsNeverBill() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(ferny, runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(eowyn));

		// "Exert Eowyn and assign a minion to the Ring-bearer": of the two minions only the Runner is the Free
		// Peoples player's to assign, so it is assigned and no choice is put.
		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertFalse(scn.IsCharAssigned(ferny));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void BillIsDiscardedWhenTheFellowshipMovesUndergroundAndWhenPlayedThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferny = scn.GetShadowCard("ferny");
		var ferny2 = scn.GetShadowCard("ferny2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(ferny, ferny2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Rivendell Valley, site 3, is not underground: Bill is played and stays.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		// A minion beside him, which underground sites do not touch.
		scn.MoveMinionsToTable(troop);

		scn.SkipToMovementDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));

		// Balin's Tomb, site 4, is underground.
		scn.FreepsChooseToMove();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInDiscard(ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// The second copy, played at Balin's Tomb, is discarded as it comes into play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(ferny2);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInDiscard(ferny, ferny2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
