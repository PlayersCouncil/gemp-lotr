package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "5_55");
					put("scout2", "5_55");
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a Regroup ability only
					put("party", "1_158");      // Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, vitality 3, damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, strength 13, vitality 3

					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3, vitality 4 (+2 only with a weapon)
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; a Fellowship ability and a Response to Frodo's death
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a Maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3, damage +1; a Skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardScoutTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Isengard Scout Troop
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>.
		 * While this minion is not exhausted, it is <b>fierce</b>.
		 * Each time an [isengard] Orc wins a skirmish, you may exert this minion to control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout");

		assertEquals("Isengard Scout Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachIsengardOrcWinExertsItToControlASiteUntilItIsExhaustedAndNoLongerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var worker = scn.GetShadowCard("worker");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, sam);

		scn.StartGame();
		// Player 1's third turn: the fellowship moves on to site 4, with sites 1 and 2 behind both markers.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(scout, worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, worker}, new PhysicalCardImpl[]{sam, scout});

		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.HasKeyword(scout, Keyword.FIERCE));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// 5 against 3: the Worker, an [isengard] Orc of another title, wins.
		scn.FreepsResolveSkirmish(merry);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout));
		scn.ShadowAcceptOptionalTrigger();

		// Exerted, and site 1, the lowest behind every marker, is the Shadow player's.
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		// Vitality 3 with 1 wound: 2 left, not exhausted, still fierce.
		assertFalse(scn.IsExhausted(scout));
		assertTrue(scn.HasKeyword(scout, Keyword.FIERCE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The Troop's own win: offered again.
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		// Its own exertion has exhausted it: no longer fierce, so no fierce round follows.
		assertTrue(scn.IsExhausted(scout));
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedItExertsNothingAndItsOwnFierceWinOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, scout);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(scout));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Unwounded and fierce, it fights again, and wins again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, scout);
		scn.FreepsResolveSkirmish(merry);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnIsengardOrcLosesNorWhenAnIsengardUrukHaiOrAMoriaOrcWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var worker = scn.GetShadowCard("worker");
		var party = scn.GetShadowCard("party");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, knight, merry);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(scout, worker, party, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, worker}, new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{knight, party}, new PhysicalCardImpl[]{merry, scout});

		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// 5 against 6: the Worker loses, and lives with 2 wounds of 3 (Gimli is damage +1).
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(worker));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scout));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 13 against 8: the [moria] Orc wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scout));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// 9 against 6: the [isengard] Uruk-hai wins.
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scout));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// The Troop's own win is an [isengard] Orc's.
		scn.FreepsResolveSkirmish(merry);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ExhaustedItIsNotFierceAndCannotPayWhileASecondCopyPaysForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var scout2 = scn.GetShadowCard("scout2");
		var worker = scn.GetShadowCard("worker");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(scout, scout2, worker);
		// Vitality 3: with 1 wound it has 2 left and is fierce; with 2 it is exhausted and is not.
		scn.AddWoundsToChar(scout, 1);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertFalse(scn.IsExhausted(scout));
		assertTrue(scn.HasKeyword(scout, Keyword.FIERCE));
		scn.AddWoundsToChar(scout, 1);
		assertEquals(2, scn.GetWoundsOn(scout));
		assertTrue(scn.IsExhausted(scout));
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		// The other copy goes by its own state; the Worker, not exhausted, is not fierce.
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertTrue(scn.HasKeyword(scout2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(worker, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, worker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);
		scn.PassCurrentPhaseActions();

		// The Worker has won: the exhausted copy cannot exert, and only the other is offered.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scout));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout2));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(scout2));
		assertEquals(2, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledItIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		// Turn 1: the Shadow player's marker is still on site 1, so no site is behind every marker.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, scout);
		scn.FreepsResolveSkirmish(merry);

		var site1 = scn.GetSite(1);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(scout));
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(scn.GetSite(2)));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
