package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Archery fire: the two archery totals, and who places whose wounds.
 *
 * Comprehensive Rules 4.2, "archery phase": "All Shadow players count the number of all their minions with the
 * keyword archer to determine the 'minion archery total.' ...  As the Free Peoples player, you also count the
 * number of your Free Peoples archer companions to determine the 'fellowship archery total.' ...  The Free Peoples
 * player then assigns a number of wounds equal to the minion archery total to his companions (and participating
 * allies), in any way he wishes.  He then chooses one Shadow player who must assign a number of wounds equal to the
 * fellowship archery total to his minions, in any way he wishes. ...  There is always a 'default' archery total of
 * zero for each side.  A card may add to your archery total ...  When all archery wounds have been assigned,
 * proceed to the assignment phase."
 *
 * Which allies take part, and what happens while allies take the wounds "instead of companions", is in
 * AlliesTakeArcheryWoundsTests.
 */
public class ArcheryFireTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Legolas, Prince of Mirkwood: Elf companion, archer, vitality 3; his other text needs a Nazgul
					put("legolas", "1_51");
					// Double Shot: "Archery: Spot an Elf archer companion to make the fellowship archery total +1."
					put("doubleshot", "1_38");
					put("assassin", "1_131");   // Orthanc Assassin: "Archer. Damage +1." is its whole text; vitality 3
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text at all, so no archer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EachSidesArchersWoundTheOtherSideOnceForEachArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var assassin = scn.GetShadowCard("assassin");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// One archer a side; the companion and the minion who are no archers count for nothing.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.PassArcheryActions();

		// The minion archery total's wound is the Free Peoples player's to place, on either companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		// The fellowship archery total's wound is the Shadow player's to place, on either minion, archer or not.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ABonusToTheFellowshipArcheryTotalAddsNothingToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var doubleshot = scn.GetFreepsCard("doubleshot");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(doubleshot);

		var assassin = scn.GetShadowCard("assassin");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.FreepsPlayCard(doubleshot);

		assertInZone(Zone.DISCARD, doubleshot);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.PassCurrentPhaseActions();

		// The fellowship takes one wound, as before the event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, legolas));
		scn.FreepsChooseCard(legolas);
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// The minions take two, each placed where the Shadow player likes.
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, troop));
		scn.ShadowChooseCard(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(assassin, troop));
		scn.ShadowChooseCard(assassin);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
