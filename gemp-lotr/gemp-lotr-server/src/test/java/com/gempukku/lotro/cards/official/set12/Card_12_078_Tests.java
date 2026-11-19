package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_078_Tests
{

	// The King-block path: site 2, King's Tent, has no terrain; site 3, The Dimholt, is underground and a
	// sanctuary.  Neither is a plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "12_78");
					put("hillman2", "12_78");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship-block path: site 2, Ettenmoors, is a plains and nothing else.
	protected VirtualTableScenario GetEttenmoorsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "12_78");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Towers-block path whose site 2 is Uruk Camp, a battleground as well as a plains.  Its text answers only an
	// Uruk-hai played.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "12_78");
					put("aragorn", "1_89");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_335");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WrathfulHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Wrathful Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to a skirmish, if it is not at a plains site, remove (2) or discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hillman");

		assertEquals("Wrathful Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedAtASiteThatIsNoPlainsItMakesTheShadowPlayerRemoveTwoAndTheUnassignedCopyAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman = scn.GetShadowCard("hillman");
		var hillman2 = scn.GetShadowCard("hillman2");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, hillman2);

		// The fellowship has moved to King's Tent, site 2.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, hillman);

		// Both can be done in full, so the Shadow player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		assertTrue(scn.ShadowChoiceAvailable("Discard this minion"));
		scn.ShadowChoose("Remove 2 twilight");

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, hillman, hillman2);

		// The second copy is left over and the Shadow player does not assign it: nothing more is asked of him.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowDeclineAssignments();

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, hillman, hillman2);
	}

	@Test
	public void TheShadowPlayerMayDiscardItInsteadOfRemovingTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);

		scn.SkipToAssignments();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, hillman);
		assertTrue(scn.ShadowChoiceAvailable("Remove 2 twilight"));
		scn.ShadowChoose("Discard this minion");

		assertInZone(Zone.DISCARD, hillman);
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.IsCharAssigned(aragorn));
		// With no minion left there is no skirmish to fight.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithLessThanTwoTwilightInThePoolItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);

		scn.SkipToAssignments();
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		// (2) cannot be removed in full from a pool of 1, so the discard is all there is to do.
		scn.FreepsAssignToMinions(aragorn, hillman);

		assertInZone(Zone.DISCARD, hillman);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.IsCharAssigned(aragorn));
		// With no minion left there is no skirmish to fight.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AssignedAtEttenmoorsAPlainsNothingIsAskedOrPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEttenmoorsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);

		// The fellowship has moved to Ettenmoors, site 2.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, hillman);

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, hillman));
	}

	@Test
	public void AssignedAtUrukCampABattlegroundPlainsNothingIsAskedOrPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);

		// The fellowship has moved to Uruk Camp, site 2.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, hillman);

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, hillman));
	}

	@Test
	public void AssignedAgainInTheSameTurnByTheShadowPlayerItAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);

		scn.SkipToAssignments();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		// The first time, by the Free Peoples player's assignment at King's Tent.
		scn.FreepsAssignToMinions(aragorn, hillman);
		scn.ShadowChoose("Remove 2 twilight");
		assertEquals(3, scn.GetTwilight());

		// 12 against 8: Aragorn loses and takes a wound; the Hillman stays in play.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);

		// On to The Dimholt, site 3, in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// The second time, by the Shadow player's assignment of the minion left over.
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(aragorn, hillman);

		assertTrue(scn.ShadowChoiceAvailable("Discard this minion"));
		scn.ShadowChoose("Remove 2 twilight");

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, hillman);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
