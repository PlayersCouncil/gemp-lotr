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

public class Card_04_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ransacker", "4_14");
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
					put("ransacker", "4_14");
					put("brigand", "4_10");     // Dunlending Brigand: the other [dunland] Man, with no text
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRansackerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ransacker", "4_14");
					put("ransacker2", "4_14");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text
					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man with no text
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  "May not be assigned to
					// a skirmish"; his move trigger is kept out by laying him after the move.
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man.  Strength +3 and fierce only while 2 burdens can be spotted.
					put("axeman", "4_224");
					// Hides: a [dunland] possession in the support area.  "Response: If a [dunland] Man is about to
					// take a wound, remove (2) or discard this possession to prevent that wound."
					put("hides", "4_19");
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
	public void DunlendingRansackerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Ransacker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may spot another [dunland] Man to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ransacker");

		assertEquals("Dunlending Ransacker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(ransacker, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 7 against 6: the Knight loses, and takes a wound.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(ransacker, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Ransacker has won, beside the other [dunland] Man.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void WinningASkirmishBesideADunlendingSavageAsTheOtherDunlandManTakesASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRansackerScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var savage = scn.GetShadowCard("savage");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(ransacker, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 7 against 6: the Ransacker wins.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishBesideASecondRansackerOffersOnlyTheWinnersTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRansackerScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var ransacker2 = scn.GetShadowCard("ransacker2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(ransacker, ransacker2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// The other copy is the other [dunland] Man spotted; it won nothing and offers nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ransacker2));
		scn.ShadowChooseOptionalTrigger(ransacker);

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishWithNoOtherDunlandManOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRansackerScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		// A [dunland] minion who is no Man, a Man of another culture, and a [dunland] card that is no Man; the
		// Ransacker himself is no "other".  Laid after the move, so that Saruman's move trigger has nothing to answer.
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(ransacker, saruman, axeman);
		assertInZone(Zone.SUPPORT, hides);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman, axeman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));

		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void DecliningTheTriggerTakesNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRansackerScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var brigand = scn.GetShadowCard("brigand");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(ransacker, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRansackerScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var brigand = scn.GetShadowCard("brigand");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(ransacker, brigand);
		assertInZone(Zone.SUPPORT, hides);
		// The Knight at 8 beats the Ransacker's 7 without overwhelming him (14).
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(ransacker));

		scn.PassCurrentPhaseActions();
		// Hides saves the Ransacker from the wound for losing, so that he is still in play to answer anything.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, ransacker);
		assertEquals(0, scn.GetWoundsOn(ransacker));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void WinningTheNormalAndTheFierceSkirmishTakesTwoSites() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRansackerScenario();

		var ransacker = scn.GetShadowCard("ransacker");
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
		scn.MoveMinionsToTable(ransacker, brigand, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);

		// Anger, spotting the three [dunland] minions, makes the Ransacker fierce.
		scn.FreepsPass();
		scn.ShadowPlayCard(anger);
		scn.ShadowChooseCard(ransacker);
		assertTrue(scn.HasKeyword(ransacker, Keyword.FIERCE));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// The fierce skirmish: the Ransacker wins again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, ransacker);
		scn.FreepsResolveSkirmish(knight);
		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ransacker));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
