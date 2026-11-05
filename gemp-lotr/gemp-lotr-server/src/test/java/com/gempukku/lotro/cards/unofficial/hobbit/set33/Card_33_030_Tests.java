package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_030_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// and skirmishes are at The Troll Hoard 30_50 (site 2, a forest and underground); Goblin Town 30_52 (site 4) is
	// underground and no forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("borders", "33_30");

					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc, vitality 3; a Shadow ability not used
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, vitality 2; a Maneuver ability not used
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; may not be assigned to Bilbo
					// Assaulting Goblin: a [moria] Orc, vitality 2; a Response only at a battleground
					put("goblin", "32_27");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BordersoftheForestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Borders of the Forest
		 * Unique: False
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the end of your Shadow phase, you may heal a [gundabad] Orc.
		 * 	Assignment: At a forest, assign a [gundabad] Orc to skirmish Bilbo. The Free Peoples player may add a doubt to prevent this and assign that Orc.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("borders");

		assertEquals("Borders of the Forest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheEndOfTheShadowPhaseMayHealAGundabadOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borders = scn.GetShadowCard("borders");
		var watchful = scn.GetShadowCard("watchful");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToSupportArea(borders);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, borders);
		scn.FreepsPass();
		scn.MoveMinionsToTable(watchful, yazneg, ferny, goblin);
		scn.AddWoundsToChar(watchful, 2);
		scn.AddWoundsToChar(yazneg, 1);
		scn.AddWoundsToChar(ferny, 1);
		scn.AddWoundsToChar(goblin, 1);
		assertEquals(2, scn.GetWoundsOn(watchful));
		assertEquals(1, scn.GetWoundsOn(yazneg));
		assertEquals(1, scn.GetWoundsOn(ferny));
		assertEquals(1, scn.GetWoundsOn(goblin));

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(borders));
		scn.ShadowAcceptOptionalTrigger();
		// [gundabad] Orcs of two titles; not a [gundabad] Man, not a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(watchful, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, goblin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(watchful);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(watchful));
		assertEquals(1, scn.GetWoundsOn(yazneg));
		assertEquals(1, scn.GetWoundsOn(ferny));
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedItIsOfferedAgainAtTheEndOfASecondShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borders = scn.GetShadowCard("borders");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToSupportArea(borders);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, borders);
		scn.FreepsPass();
		scn.MoveMinionsToTable(yazneg);
		scn.AddWoundsToChar(yazneg, 1);
		assertEquals(1, scn.GetWoundsOn(yazneg));

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(borders));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(yazneg));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Yazneg is left unassigned and stands into the regroup phase; the fellowship moves again, to Rivendell.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(yazneg));

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(borders));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NothingIsOfferedAtTheEndOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borders = scn.GetShadowCard("borders");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToSupportArea(borders);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, borders);
		scn.FreepsPass();
		scn.MoveMinionsToTable(yazneg);
		scn.AddWoundsToChar(yazneg, 1);
		assertEquals(1, scn.GetWoundsOn(yazneg));
		scn.ShadowPass();
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(yazneg));

		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(yazneg));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWithNoWoundedGundabadOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borders = scn.GetShadowCard("borders");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToSupportArea(borders);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, borders);
		scn.FreepsPass();
		// Yazneg unwounded; the wounded minions are a [gundabad] Man and a [moria] Orc.
		scn.MoveMinionsToTable(yazneg, ferny, goblin);
		scn.AddWoundsToChar(ferny, 1);
		scn.AddWoundsToChar(goblin, 1);
		assertEquals(0, scn.GetWoundsOn(yazneg));
		assertEquals(1, scn.GetWoundsOn(ferny));
		assertEquals(1, scn.GetWoundsOn(goblin));

		scn.ShadowPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(ferny));
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtAForestAssignsAGundabadOrcToBilboWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borders = scn.GetShadowCard("borders");
		var watchful = scn.GetShadowCard("watchful");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var goblin = scn.GetShadowCard("goblin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(borders);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, borders);
		scn.MoveMinionsToTable(watchful, yazneg, ferny, goblin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("The Troll Hoard", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(borders));
		scn.ShadowUseCardAction(borders);
		// Bilbo is the one Bilbo and is taken unasked; the Orc is chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(watchful, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, goblin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(yazneg);

		assertTrue(scn.FreepsDecisionAvailable("add a burden"));
		scn.FreepsChooseNo();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(bilbo, yazneg));
		assertFalse(scn.IsCharAssigned(balin));
		assertFalse(scn.IsCharAssigned(watchful));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddADoubtToPreventItAndAssignThatOrcHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borders = scn.GetShadowCard("borders");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(borders);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, borders);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowUseCardAction(borders);
		// Yazneg is the one [gundabad] Orc, and is chosen unasked.
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetBurdens());
		// The Free Peoples player then assigns that Orc, to any of his companions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, bilbo));
		scn.FreepsChooseCard(balin);
		assertTrue(scn.IsCharAssignedAgainst(balin, yazneg));
		assertFalse(scn.IsCharAssigned(bilbo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedAwayFromAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var borders = scn.GetShadowCard("borders");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(borders);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.SUPPORT, borders);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Goblin Town", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(borders));
		assertFalse(scn.IsCharAssigned(yazneg));
	}
}
