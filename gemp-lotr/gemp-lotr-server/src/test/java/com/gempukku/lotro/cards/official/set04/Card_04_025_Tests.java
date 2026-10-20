package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tribe", "4_25");
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
					put("tribe", "4_25");
					put("brigand", "4_10");     // Dunlending Brigand: the other [dunland] minion, with no text
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTribeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tribe", "4_25");
					put("tribe2", "4_25");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text
					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man with no text
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  "May not be assigned to
					// a skirmish"; his move trigger is kept out by laying him after the move.
					put("saruman", "4_33");
					// Hides: a [dunland] possession in the support area.  "Response: If a [dunland] Man is about to
					// take a wound, remove (2) or discard this possession to prevent that wound."
					put("hides", "4_19");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					// Anger: [dunland] Skirmish event, "Spot 3 [dunland] minions or a site you control to make a
					// [dunland] Man fierce until the regroup phase."
					put("anger", "4_3");
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HillmanTribeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hillman Tribe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may spot another [dunland] minion to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tribe");

		assertEquals("Hillman Tribe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var tribe = scn.GetShadowCard("tribe");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(tribe, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 9 against 6: the Knight loses, and takes a wound.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tribe));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var tribe = scn.GetShadowCard("tribe");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(tribe, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Tribe has won, beside the other [dunland] minion.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tribe));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void WinningASkirmishBesideSarumanRabbleRouserAsTheOtherDunlandMinionTakesASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTribeScenario();

		var tribe = scn.GetShadowCard("tribe");
		var saruman = scn.GetShadowCard("saruman");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		// Laid after the move, so that Saruman's move trigger has nothing to answer.
		scn.MoveMinionsToTable(tribe, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 9 against 6: the Tribe wins.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tribe));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishBesideASecondHillmanTribeOffersOnlyTheWinnersTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTribeScenario();

		var tribe = scn.GetShadowCard("tribe");
		var tribe2 = scn.GetShadowCard("tribe2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(tribe, tribe2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// The other copy is the other [dunland] minion spotted; it won nothing and offers nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tribe));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tribe2));
		scn.ShadowChooseOptionalTrigger(tribe);

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheTriggerTakesNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTribeScenario();

		var tribe = scn.GetShadowCard("tribe");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(tribe, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tribe));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishWithNoOtherDunlandMinionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTribeScenario();

		var tribe = scn.GetShadowCard("tribe");
		var hides = scn.GetShadowCard("hides");
		var troop = scn.GetShadowCard("troop");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// A [dunland] card that is no minion, and a minion of another culture.
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(tribe, troop);
		assertInZone(Zone.SUPPORT, hides);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, hides);

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tribe));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTribeScenario();

		var tribe = scn.GetShadowCard("tribe");
		var brigand = scn.GetShadowCard("brigand");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(tribe, brigand);
		assertInZone(Zone.SUPPORT, hides);
		// The Knight at 12 beats the Tribe's 9 without overwhelming him (18).
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 6));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(tribe));
		assertInZone(Zone.SUPPORT, hides);

		scn.PassCurrentPhaseActions();
		// Hides saves the Tribe from the wound for losing, so that he is still in play to answer anything.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tribe));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, tribe);
		assertEquals(0, scn.GetWoundsOn(tribe));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(tribe));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void WinningTheNormalAndTheFierceSkirmishTakesTwoSites() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTribeScenario();

		var tribe = scn.GetShadowCard("tribe");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var anger = scn.GetShadowCard("anger");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(anger);

		scn.StartGame();
		// Player 1's third turn: the move takes him to site 4 with player 2's marker on site 3, so sites 1 and 2
		// are behind every marker.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(tribe, brigand, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		// Anger, spotting the three [dunland] minions, makes the Tribe fierce.
		scn.FreepsPass();
		scn.ShadowPlayCard(anger);
		scn.ShadowChooseCard(tribe);
		assertTrue(scn.HasKeyword(tribe, Keyword.FIERCE));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tribe));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// The fierce skirmish: the Tribe wins again.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(knight, tribe);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tribe));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
