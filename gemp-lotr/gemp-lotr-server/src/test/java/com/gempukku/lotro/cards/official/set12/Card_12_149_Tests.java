package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_149_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("common", "12_149");
					put("common2", "12_149");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability never used here
				}},
				// Site 2 is Eastfold 4_331 (plains, no battleground), site 3 Barrows of Edoras 4_337 (sanctuary, no
				// battleground) and site 4 White Rocks 4_346 (battleground only).
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("common", "12_149");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					// Uruk Camp: a battleground that is a plains as well.  Its trigger answers an Uruk-hai played, and
					// Uruk Common is cheated onto the table.
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCommonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Common
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion is assigned to a skirmish, if it is not at a battleground site, remove (2) or discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("common");

		assertEquals("Uruk Common", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedAwayFromABattlegroundTheShadowPlayerMayRemoveTwoTwilightToKeepIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var common = scn.GetShadowCard("common");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(common);
		assertInZone(Zone.SHADOW_CHARACTERS, common);

		scn.FreepsPass();
		scn.SkipToAssignments();
		// Eastfold, site 2: a plains, and no battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, common);
		// Both can be done: the Shadow player is asked which.
		assertEquals(List.of("Remove 2 twilight", "Discard this minion"), scn.ShadowGetMultipleChoices());
		scn.ShadowChooseOption("Remove 2 twilight");

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, common);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, common));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AssignedAwayFromABattlegroundTheShadowPlayerMayDiscardItInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var common = scn.GetShadowCard("common");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(common);
		assertInZone(Zone.SHADOW_CHARACTERS, common);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, common);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Discard this minion");

		// Discarded, not killed, and nothing removed.
		assertInDiscard(common);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void WithOneTwilightInThePoolItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var common = scn.GetShadowCard("common");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(common);
		assertInZone(Zone.SHADOW_CHARACTERS, common);

		scn.FreepsPass();
		scn.SkipToAssignments();
		// Removing (2) cannot be done in full, so the discard is all that is left.
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, common);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));

		assertInDiscard(common);
		assertEquals(1, scn.GetTwilight());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void AtABattlegroundNothingHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var common = scn.GetShadowCard("common");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(common);
		assertInZone(Zone.SHADOW_CHARACTERS, common);

		scn.FreepsPass();
		scn.SkipToAssignments();
		// White Rocks, site 4: a battleground.
		assertEquals(4, (int) scn.GetCurrentSite().getSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, common);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));

		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, common);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, common));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AtABattlegroundThatIsAlsoAPlainsNothingHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var common = scn.GetShadowCard("common");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(common);
		assertInZone(Zone.SHADOW_CHARACTERS, common);

		scn.FreepsPass();
		scn.SkipToAssignments();
		// Uruk Camp, site 2: a battleground and a plains.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, common);
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));

		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, common);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, common));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void EachCopyAnswersItsOwnAssignmentWhicheverPlayerMakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var common = scn.GetShadowCard("common");
		var common2 = scn.GetShadowCard("common2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveMinionsToTable(common, common2);
		assertInZone(Zone.SHADOW_CHARACTERS, common, common2);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		// The Free Peoples player assigns the first copy and leaves the second over.
		scn.FreepsAssignToMinions(aragorn, common);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Discard this minion");

		assertInDiscard(common);
		assertInZone(Zone.SHADOW_CHARACTERS, common2);
		assertEquals(5, scn.GetTwilight());

		// The Shadow player assigns the one left over: its own trigger answers.
		scn.ShadowAssignToMinions(gimli, common2);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Remove 2 twilight");

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, common2);
		assertTrue(scn.IsCharAssignedAgainst(gimli, common2));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseItAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var common = scn.GetShadowCard("common");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(common);
		assertInZone(Zone.SHADOW_CHARACTERS, common);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, common);
		scn.ShadowChooseOption("Remove 2 twilight");
		assertEquals(3, scn.GetTwilight());

		// Strength 11 against Aragorn's 8: Aragorn takes 2 wounds (damage +1) and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, common);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		// Barrows of Edoras, site 3: a sanctuary, and no battleground.
		assertEquals(3, (int) scn.GetCurrentSite().getSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.FreepsAssignToMinions(aragorn, common);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Remove 2 twilight");

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, common));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
