package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marshal", "18_127");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marshal", "18_127");
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandMarshalStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Marshal
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion wins a skirmish, you may control a site.
		*/

		var scn = GetScenario();

		var marshal = scn.GetFreepsCard("marshal");

		assertEquals("White Hand Marshal", marshal.getBlueprint().getTitle());
		assertNull(marshal.getBlueprint().getSubtitle());
		assertTrue(marshal.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, marshal.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, marshal.getBlueprint().getCulture());
		assertEquals(CardType.MINION, marshal.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, marshal.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(marshal, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(marshal, Keyword.DAMAGE));
		assertEquals(5, marshal.getBlueprint().getTwilightCost());
		assertEquals(11, marshal.getBlueprint().getStrength());
		assertEquals(3, marshal.getBlueprint().getVitality());
		assertEquals(5, marshal.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var marshal = scn.GetShadowCard("marshal");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(marshal);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, marshal);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 11 against 6: the Knight loses, and takes 2 wounds for damage +1.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marshal));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var marshal = scn.GetShadowCard("marshal");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(marshal);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, marshal);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Marshal has won.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(marshal));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void DecliningTheOfferLeavesTheSiteOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(marshal);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, marshal);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 11 against 8: Aragorn loses, and takes 2 wounds for damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marshal));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(marshal);
		// Only so that Aragorn wins: 8 + 4 against 11.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, marshal);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(marshal));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// The Marshal loses and takes 1 wound of his 3 vitality, with site 1 there to take.
		assertEquals(1, scn.GetWoundsOn(marshal));
		assertInZone(Zone.SHADOW_CHARACTERS, marshal);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachSkirmishWonInOneTurnOffersASiteAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marshal = scn.GetShadowCard("marshal");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Both markers on site 4: at sites 5 and 6, sites 1 to 3 are behind both.
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(marshal);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, marshal);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marshal));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// The regroup move to site 6, and a second assignment phase with the Marshal still in play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, marshal);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassCurrentPhaseActions();

		// 11 against 6: Gimli loses and takes 2 wounds; the next site behind every marker is taken.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marshal));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
