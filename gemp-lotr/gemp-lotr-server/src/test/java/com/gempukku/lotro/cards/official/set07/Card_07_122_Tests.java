package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strong", "7_122");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; valiant only

					// [gondor] fortifications, all conditions; their abilities are of the maneuver and skirmish phases.
					put("fourth", "8_35");      // Fourth Level: twilight 2, not unique
					put("garrison", "6_52");    // Garrison of Osgiliath: twilight 2, unique
					put("wall", "5_33");        // City Wall: twilight 1, not unique
					put("tower", "5_39");       // Stone Tower: twilight 1, unique
					put("first", "7_92");       // First Level: twilight 1, not unique; transfers itself to a minion
					// Beside them, a fortification of another culture and a [gondor] condition that is none.
					put("slits", "5_80");       // Arrow-slits: [rohan] fortification, twilight 1; a maneuver ability spotting 2 [rohan] Men
					put("rally", "5_38");       // Rally Point: [gondor] condition, twilight 1; a regroup ability exerting a knight

					put("theoden", "4_292");    // Theoden, Son of Thengel: "Regroup: Spot a [rohan] ally and exert Theoden to liberate a site."
					put("guma", "4_277");       // Guma, Plains Farmer: the [rohan] ally (discarded only if site 4 of a Towers path is controlled)
					put("explorer", "4_250");   // Southron Explorer: "Regroup: Exert this minion and remove (5) to take control of a site."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongandOldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Strong and Old
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [gondor] Men.
		 * 	The twilight cost of each of your [gondor] fortifications is -1.
		 * 	<b>Response:</b> If an opponent is about to control a site, discard 2 [gondor] fortifications to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strong");

		assertEquals("Strong and Old", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideTwoGondorMenItMakesEachOfYourGondorFortificationsOneCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetFreepsCard("strong");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var fourth = scn.GetFreepsCard("fourth");
		var garrison = scn.GetFreepsCard("garrison");
		var slits = scn.GetFreepsCard("slits");
		var rally = scn.GetFreepsCard("rally");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(strong, fourth, garrison, slits, rally);
		assertInHand(strong, fourth, garrison, slits, rally);
		// The other player's own Fourth Level, in his hand.
		var shadowFourth = scn.GetShadowCard("fourth");
		scn.MoveCardsToHand(shadowFourth);
		assertInHand(shadowFourth);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(fourth));
		assertEquals(2, scn.GetTwilightCost(garrison));
		assertEquals(1, scn.GetTwilightCost(slits));
		assertEquals(1, scn.GetTwilightCost(rally));
		assertEquals(2, scn.GetTwilightCost(shadowFourth));
		// Two [gondor] Men, a companion and an ally.
		assertTrue(scn.FreepsPlayAvailable(strong));

		scn.FreepsPlayCard(strong);
		assertInZone(Zone.SUPPORT, strong);

		// Each of his [gondor] fortifications is 1 cheaper; not a [rohan] fortification, a [gondor] condition that
		// is no fortification, or the other player's [gondor] fortification.
		assertEquals(1, scn.GetTwilightCost(fourth));
		assertEquals(1, scn.GetTwilightCost(garrison));
		assertEquals(1, scn.GetTwilightCost(slits));
		assertEquals(1, scn.GetTwilightCost(rally));
		assertEquals(2, scn.GetTwilightCost(shadowFourth));

		var pool = scn.GetTwilight();
		scn.FreepsPlayCard(fourth);
		assertInZone(Zone.SUPPORT, fourth);
		// 2 for Fourth Level - 1
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedBesideOneGondorManAGondorHobbitAndARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetFreepsCard("strong");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveCardsToHand(strong);
		assertInHand(strong);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(strong));
	}

	@Test
	public void DiscardingTwoGondorFortificationsPreventsTheOpponentTakingControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetFreepsCard("strong");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wall = scn.GetFreepsCard("wall");
		var tower = scn.GetFreepsCard("tower");
		var first = scn.GetFreepsCard("first");
		var slits = scn.GetFreepsCard("slits");
		var rally = scn.GetFreepsCard("rally");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strong, wall, tower, slits, rally);
		assertInZone(Zone.SUPPORT, strong, wall, tower, slits, rally);

		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker and can be controlled.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn, 8 against 5, has won: the Explorer has a wound and can still exert.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(explorer));
		// A [gondor] fortification borne by a minion is one too (put on him once his skirmish is over).
		scn.AttachCardsTo(explorer, first);
		assertAttachedTo(first, explorer);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(explorer));
		scn.ShadowUseCardAction(explorer);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(strong));
		assertFalse(scn.IsSiteControlled(site1));
		scn.FreepsAcceptOptionalTrigger();

		// Two [gondor] fortifications, unique or not, in the support area or on a minion; not the [rohan]
		// fortification, nor the [gondor] condition that is no fortification.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wall, tower, first));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(slits, rally, strong));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(wall, first);

		assertInDiscard(wall, first);
		assertInZone(Zone.SUPPORT, tower, slits, rally, strong);
		// Nothing was taken, and the Explorer's costs stay paid.
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneGondorFortificationTheResponseIsNotOfferedAndTheSiteIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetFreepsCard("strong");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wall = scn.GetFreepsCard("wall");
		var slits = scn.GetFreepsCard("slits");
		var rally = scn.GetFreepsCard("rally");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(strong, wall, slits, rally);
		assertInZone(Zone.SUPPORT, strong, wall, slits, rally);

		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer);

		// One [gondor] fortification beside a [rohan] one and a [gondor] condition: the cost cannot be paid.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(strong));
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SUPPORT, strong, wall, slits, rally);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASiteLiberatedIsNoSiteAboutToBeControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strong = scn.GetFreepsCard("strong");
		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		var wall = scn.GetFreepsCard("wall");
		var tower = scn.GetFreepsCard("tower");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(strong, wall, tower);
		assertInZone(Zone.SUPPORT, strong, wall, tower);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// No response was offered: the game went on to the Shadow player's regroup actions.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(strong));
		assertInZone(Zone.SUPPORT, strong, wall, tower);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
