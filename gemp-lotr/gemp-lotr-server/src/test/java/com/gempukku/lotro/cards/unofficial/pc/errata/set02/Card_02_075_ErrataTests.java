package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_075_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FOTRFrodo);
	}

	// Galadriel as Ring-bearer, so that no Hobbit is in play unless a test puts one there.
	protected VirtualTableScenario GetNoHobbitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.GaladrielRB);
	}

	protected VirtualTableScenario GetScenario(String ringBearer) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ferny", "52_75");
					put("ferny2", "52_75");

					put("rider1", "12_161");    // Black Rider: [ringwraith] Nazgul, not unique, cost 5, site 3; fierce
					put("rider2", "12_161");
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgul, cost 6, site 3
					put("faces", "16_4");       // Dead Faces: [ringwraith] Wraith and not a Nazgul, cost 2, site 3
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Hobbit
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a Hobbit ally, home site 3
					put("sam", "1_310");        // Sam, Faithful Companion: a Hobbit companion, strength 3
				}},
				VirtualTableScenario.FellowshipSites,
				ringBearer,
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
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Race: Man
		 * Strength: 4
		 * Vitality: 1
		 * Site: 2
		 * Game Text: Nazgul are not roaming.
		 * While you can spot a Hobbit, the Free Peoples player may not assign a character to skirmish Bill Ferny.
		 * Discard Bill Ferny if underground.
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
	public void NazgulPlayedWhileBillIsInPlayAreNotRoamingAndOtherMinionsStillAre() throws DecisionResultInvalidException, CardNotFoundException {
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
	public void WhileAHobbitCanBeSpottedTheFreePeoplesPlayerCannotAssignAnyCharacterToBillButTheShadowPlayerCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var ferny = scn.GetShadowCard("ferny");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(ferny, troop);

		// The move to site 3 takes the fellowship to Bilbo's home, so he can be assigned too.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(bilbo));

		// Bill is not the Free Peoples player's to assign: the game's rule says so, and he is not among the
		// minions that player is offered, so no character can be assigned to him, Hobbit, Man or ally at home.
		assertFalse(scn.FreepsCanAssign(ferny));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(ferny));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		// It is Bill who is barred, and to that player: the other minion is offered, every character is still
		// offered for it, and the Shadow player may assign Bill.
		assertTrue(scn.FreepsCanAssign(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowCanAssign(ferny));
		assertFalse(scn.IsCharAssigned(ferny));

		scn.FreepsAssignToMinions(aragorn, troop);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertFalse(scn.IsCharAssigned(ferny));

		// What the Free Peoples player could not assign, the Shadow player may: Bill is on his list.
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(ferny));
		scn.ShadowAssignToMinions(bilbo, ferny);
		assertTrue(scn.IsCharAssignedAgainst(bilbo, ferny));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.FreepsDecisionAvailable("skirmish"));
	}

	@Test
	public void WhenTheLastHobbitDiesTheFreePeoplesPlayerMayAssignToBill() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoHobbitScenario();

		var galadriel = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCompanionsToTable(aragorn, sam);

		// Bill is made fierce and strong enough to overwhelm Sam, so that Sam dies by play and a fierce
		// assignment follows with no Hobbit left.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.name("Bill Ferny"), null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Bill Ferny"), null, 5));
		// Aragorn is of the [shire] culture but no Hobbit: the text names a race (F-0019).
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.SHIRE));

		scn.StartGame();
		scn.MoveMinionsToTable(ferny);

		scn.SkipToAssignments();
		// Sam is the one Hobbit: Galadriel and Aragorn are not. While he lives Bill is not the Free Peoples
		// player's to assign, and with no other minion that player is asked nothing: the Shadow player assigns.
		assertFalse(scn.FreepsCanAssign(ferny));
		assertFalse(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(ferny));
		scn.ShadowAssignToMinions(sam, ferny);

		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		// 9 against 3: overwhelmed.
		assertInZone(Zone.DEAD, sam);

		// The fierce round, with Bill still in play and no Hobbit left to spot.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		// Bill is the Free Peoples player's to assign again, and on his list, with the two who are no Hobbits.
		assertTrue(scn.FreepsCanAssign(ferny));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(ferny));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(galadriel));
		scn.FreepsAssignToMinions(aragorn, ferny);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, ferny));
		assertTrue(scn.FreepsDecisionAvailable("skirmish"));
	}

	@Test
	public void AHobbitAllyPlayedAwayFromHomeStopsTheFreePeoplesPlayerAssigningToBill() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoHobbitScenario();

		var galadriel = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var ferny = scn.GetShadowCard("ferny");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(bilbo);

		scn.StartGame();
		scn.MoveMinionsToTable(ferny, troop);

		// With no Hobbit to spot, Bill is the Free Peoples player's to assign as any minion is.
		assertTrue(scn.FreepsCanAssign(ferny));

		scn.FreepsPlayCard(bilbo);
		assertInZone(Zone.SUPPORT, bilbo);
		assertFalse(scn.FreepsCanAssign(ferny));

		// At site 2 Bilbo, whose home is site 3, cannot skirmish, but he can be spotted: Bill is not offered
		// to the Free Peoples player, and the other minion is.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(bilbo));
		assertFalse(scn.FreepsCanAssign(ferny));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(ferny));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(galadriel));
		assertFalse(scn.IsCharAssigned(ferny));

		scn.FreepsAssignToMinions(aragorn, troop);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertFalse(scn.IsCharAssigned(ferny));
		// Bill is left for the Shadow player to assign.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(ferny));
	}

	@Test
	public void BillIsDiscardedWhenTheFellowshipMovesUndergroundAndWhenPlayedThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferny = scn.GetShadowCard("ferny");
		var ferny2 = scn.GetShadowCard("ferny2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(ferny, ferny2);

		scn.StartGame();
		scn.SkipToSite(2);
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
