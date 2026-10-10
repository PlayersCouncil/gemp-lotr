package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.VirtualTableScenario.P1;
import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_200_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_200");
					put("enquea", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDeclineScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spawn", "7_200");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl who is not fierce; her text is about [wraith] conditions
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion who could exert
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSpawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Spawn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, the Free Peoples player must exert a companion to assign this minion to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul Spawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MustExertToAssignMorgulSpawn() throws DecisionResultInvalidException, CardNotFoundException {
		// Arrange
		VirtualTableScenario scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var enquea = scn.GetShadowCard("enquea");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToHand(card);
		scn.MoveCardsToHand(enquea);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPass();

		scn.ShadowPlayCard(card);
		scn.ShadowPlayCard(enquea);
		scn.ShadowPass();

		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable(
				"Would you like to exert a companion to be able to assign Morgul Spawn to skirmish?"));

		// Act
		scn.PlayerDecided(P1, "0");

		// Assert
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsCanAssign(card));
	}

	// Morgul Spawn's price is built as a question put to the Free Peoples player as he starts his own assigning, ahead
	// of any assignment (the engine's assignmentCost).  Whether it is to be rebuilt as a cost paid at the assignment
	// is Q-0221, which is open: this test reads the game as it plays today.
	@Test
	public void DecliningToExertLeavesMorgulSpawnOutOfTheFreePeoplesPlayersAssigning() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeclineScenario();

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable(
				"Would you like to exert a companion to be able to assign Morgul Spawn to skirmish?"));
		scn.FreepsChooseNo();

		// Nobody exerts, and his assignment lists both companions and the Nazgûl, but not the Spawn.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(spawn));
		scn.FreepsDeclineAssignments();

		// The price is the Free Peoples player's alone: the Shadow player's assignment lists the Spawn, and he
		// assigns it with nobody exerting.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spawn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(lemenya));
		scn.ShadowAssignToMinions(aragorn, spawn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
