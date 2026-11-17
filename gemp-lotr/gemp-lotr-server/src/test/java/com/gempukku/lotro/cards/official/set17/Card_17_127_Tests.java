package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("intruder", "17_127");
					put("intruder2", "17_127");
					put("hunt", "1_126");        // Hunt Them Down!: [isengard] Maneuver event, cost 3; make an Uruk-hai fierce until the regroup phase
					put("knight", "5_35");       // Gondorian Knight: strength 6, vitality 3, no text
					put("knight2", "5_35");
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
					put("intruder", "17_127");
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandIntruderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Intruder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion wins a skirmish, if you do not control a site, you may take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("intruder");

		assertEquals("White Hand Intruder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var intruder = scn.GetShadowCard("intruder");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(intruder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 9 against 6: the Knight loses, and takes 2 wounds for damage +1.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var intruder = scn.GetShadowCard("intruder");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(intruder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Intruder has won, and the Shadow player controls no site.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void WhileOnlyTheFreePeoplesPlayerControlsASiteAWinStillOffersItAndOnlyTheWinnerOffers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intruder = scn.GetShadowCard("intruder");
		var intruder2 = scn.GetShadowCard("intruder2");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's third turn: sites 1 and 2 lie behind both markers once the fellowship moves on.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// Site 1 is the Free Peoples player's, as if taken on one of the other player's turns.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(intruder, intruder2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		// The second copy is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site2));

		scn.PassSkirmishActions();
		// 9 against 6: the Knight loses and takes 2 wounds for damage +1.
		assertEquals(2, scn.GetWoundsOn(knight));
		// "If you do not control a site": the other player's site does not count against it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(intruder2));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site2));
		assertInZone(Zone.SUPPORT, site2);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithASiteOfHisOwnAWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intruder = scn.GetShadowCard("intruder");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(intruder);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));

		scn.PassSkirmishActions();
		// It has won, and site 2 could still be taken.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intruder = scn.GetShadowCard("intruder");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(intruder);
		// Strength 11 against 9: the Intruder loses and lives through the one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(intruder));
		assertEquals(11, scn.GetStrength(knight));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(intruder));
		assertInZone(Zone.SHADOW_CHARACTERS, intruder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedOnTheFirstWinItIsOfferedAgainOnTheFierceWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intruder = scn.GetShadowCard("intruder");
		var hunt = scn.GetShadowCard("hunt");
		var knight = scn.GetFreepsCard("knight");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCardsToHand(hunt);
		assertInHand(hunt);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight, knight2);
		scn.MoveMinionsToTable(intruder);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// The Intruder is the one Uruk-hai, chosen unasked.
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(intruder, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, intruder);
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.IsSiteControlled(site1));

		// The fierce round: a second win the same turn.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight2, intruder);
		scn.FreepsResolveSkirmish(knight2);
		assertEquals(0, scn.GetWoundsOn(knight2));
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(knight2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
