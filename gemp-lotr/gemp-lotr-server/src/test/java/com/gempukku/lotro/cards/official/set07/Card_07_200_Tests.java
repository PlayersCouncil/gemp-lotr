package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

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

	protected VirtualTableScenario GetTwoCompanionScenario() throws CardNotFoundException, DecisionResultInvalidException {
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

		// The price is paid as the Spawn is assigned, and nothing is asked ahead of it (Q-0221)
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsCanAssign(card));

		// Act
		scn.FreepsAssignToMinions(frodo, card);

		// Assert: the one companion there is exerts, and the Spawn is assigned
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, card));
		// The Nazgûl is left over for the Shadow player
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// Morgul Spawn's price is a cost of assigning (Q-0221: A): the Free Peoples player pays it as he assigns the
	// Spawn and only then.  There is no question to decline: he declines by leaving the Spawn unassigned.
	@Test
	public void LeavingMorgulSpawnUnassignedCostsNothingAndTheShadowPlayerAssignsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoCompanionScenario();

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		// Nothing is asked ahead; his assignment lists both companions, the Nazgûl and the Spawn.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		scn.FreepsDeclineAssignments();

		// He assigned nothing, so nobody exerts.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

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

	// The same table, and the price paid: with a second companion beside the Ring-bearer, which of them exerts is
	// the Free Peoples player's to choose, asked once he has assigned the Spawn and before it is assigned (Q-0221).
	@Test
	public void ExertingACompanionOfHisChoiceLetsTheFreePeoplesPlayerAssignMorgulSpawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoCompanionScenario();

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		// His assignment lists the Spawn beside the Nazgûl, and nobody has exerted.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		scn.FreepsAssignToMinions(aragorn, spawn);

		// "Exert a companion": either of the two, and one of them must, before the Spawn is assigned.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsCharAssigned(spawn));
		scn.FreepsChooseCard(aragorn);

		// One exertion was the whole price.  The Nazgûl is left over for the Shadow player to assign.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// "While you can spot a Nazgûl": with none, the Spawn is assigned like any other minion.
	@Test
	public void WithNoNazgulToSpotMorgulSpawnIsAssignedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoCompanionScenario();

		var spawn = scn.GetShadowCard("spawn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		scn.FreepsAssignToMinions(aragorn, spawn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
