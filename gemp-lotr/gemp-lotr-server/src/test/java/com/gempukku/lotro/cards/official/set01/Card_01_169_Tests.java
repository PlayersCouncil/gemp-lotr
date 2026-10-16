package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_169_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("endcomes", "1_169");
					put("endcomes2", "1_169");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc; its text answers only its own play
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("tentacle", "101_36");  // Vile Tentacle: [moria] minion who is no Orc (a Creature); strength -4 away from a marsh
					put("armory", "1_173");     // Goblin Armory: [moria] support-area condition, no minion; answers only a [moria] weapon played or an Orc's wound
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers only a weapon played on him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheEndComesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The End Comes
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: <b>Assignment:</b> Spot 5 [moria] minions to make the Free Peoples player assign the Ring-bearer to a skirmish.
		*/

		var scn = GetScenario();

		var endcomes = scn.GetFreepsCard("endcomes");

		assertEquals("The End Comes", endcomes.getBlueprint().getTitle());
		assertNull(endcomes.getBlueprint().getSubtitle());
		assertFalse(endcomes.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, endcomes.getBlueprint().getSide());
		assertEquals(Culture.MORIA, endcomes.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, endcomes.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(endcomes, Timeword.ASSIGNMENT));
		assertEquals(0, endcomes.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFiveMoriaMinionsTheFreePeoplesPlayerAssignsTheRingBearerToAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var endcomes = scn.GetShadowCard("endcomes");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var troop = scn.GetShadowCard("troop");
		var tentacle = scn.GetShadowCard("tentacle");
		var armory = scn.GetShadowCard("armory");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(endcomes);

		scn.StartGame();
		// Four [moria] minions, beside a [moria] card that is no minion and a minion of another culture.
		scn.MoveMinionsToTable(runner1, runner2, runner3, tentacle, chieftain);
		scn.MoveCardsToSupportArea(armory);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(endcomes));

		// A fifth [moria] minion.
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The Free Peoples player assigns, and so chooses the minion: any minion, of any culture or race.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner1, troop, tentacle, chieftain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chieftain);

		assertTrue(scn.IsCharAssignedAgainst(frodo, chieftain));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(runner1));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(tentacle));
		assertInZone(Zone.DISCARD, endcomes);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithTheRingBearerAlreadyAssignedASecondCopyIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var endcomes = scn.GetShadowCard("endcomes");
		var endcomes2 = scn.GetShadowCard("endcomes2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var troop = scn.GetShadowCard("troop");
		var tentacle = scn.GetShadowCard("tentacle");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(endcomes, endcomes2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3, troop, tentacle);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowPlayCard(endcomes);
		scn.FreepsChooseCard(troop);
		assertTrue(scn.IsCharAssignedAgainst(frodo, troop));

		scn.FreepsPass();
		// CR 4.2 "assignment phase": an assignment action assigns only characters who are both unassigned, so
		// there is nobody left to assign; the event is still played (CR 4.2 "effect"), and does nothing.
		assertTrue(scn.ShadowPlayAvailable(endcomes2));
		scn.ShadowPlayCard(endcomes2);

		assertTrue(scn.IsCharAssignedAgainst(frodo, troop));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(runner1));
		assertFalse(scn.IsCharAssigned(tentacle));
		assertInZone(Zone.DISCARD, endcomes2);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
