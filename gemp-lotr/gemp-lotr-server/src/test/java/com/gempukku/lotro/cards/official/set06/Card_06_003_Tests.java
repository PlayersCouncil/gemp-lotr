package com.gempukku.lotro.cards.official.set06;

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

public class Card_06_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("footmen1", "6_3");
					put("footmen2", "6_3");
					// Hill Clan: [dunland] Man, strength 17.  "Each time this minion wins a skirmish, you may play a
					// [dunland] Man from hand", with no discount of its own: the play in a skirmish phase.
					put("clan", "6_6");
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, cost 4, site 3; no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingFootmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Dunlending Footmen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: The twilight cost of this minion during a skirmish phase is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("footmen1");

		assertEquals("Dunlending Footmen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DuringASkirmishPhaseItCostsTwoLessAndHillClanPlaysItFromHandForThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footmen1 = scn.GetShadowCard("footmen1");
		var footmen2 = scn.GetShadowCard("footmen2");
		var clan = scn.GetShadowCard("clan");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Strength 10 by hand, so that Hill Clan's 17 beats Aragorn without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(clan);
		scn.SetShadowHand(footmen1, footmen2, savage);
		assertInHand(footmen1, footmen2, savage);

		scn.SkipToAssignments();
		// Balin's Tomb, site 4: no minion of site 3 is roaming.  The assignment phase: printed cost.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilightCost(footmen1));
		assertEquals(5, scn.GetTwilightCost(footmen2));
		assertEquals(4, scn.GetTwilightCost(savage));
		scn.FreepsAssignToMinions(aragorn, clan);
		scn.FreepsResolveSkirmish(aragorn);

		// The skirmish phase: 5 - 2 for each Footmen, and the Savage, another [dunland] Man, at his printed 4.
		assertEquals(3, scn.GetTwilightCost(footmen1));
		assertEquals(3, scn.GetTwilightCost(footmen2));
		assertEquals(4, scn.GetTwilightCost(savage));
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());

		// Hill Clan beats Aragorn, 17 to 10.  Out of a pool of 3, either Footmen can be played, and not the Savage.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(clan));
		scn.ShadowChooseOptionalTrigger(clan);
		assertTrue(scn.ShadowHasCardChoiceAvailable(footmen1, footmen2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		scn.ShadowChooseCard(footmen1);

		assertInZone(Zone.SHADOW_CHARACTERS, footmen1);
		assertInHand(footmen2, savage);
		// 3 - (5 to play - 2)
		assertEquals(0, scn.GetTwilight());

		// The fierce assignment phase the Footmen played brings about (Hill Clan made him fierce): printed cost again.
		assertTrue(scn.InFierceRound());
		assertEquals(5, scn.GetTwilightCost(footmen2));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// And in the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetTwilightCost(footmen2));
		assertEquals(4, scn.GetTwilightCost(savage));
	}

	@Test
	public void InTheShadowPhaseItCostsItsPrintedFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footmen = scn.GetShadowCard("footmen1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(footmen);
		assertInHand(footmen);
		// The move to Balin's Tomb, site 4: no minion of site 3 is roaming.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilightCost(footmen));

		// Not a skirmish phase: the full 5.  With a pool of 4 it cannot be played; with 5 it can, and takes it all.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(footmen));
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(footmen));
		scn.ShadowPlayCard(footmen);
		assertInZone(Zone.SHADOW_CHARACTERS, footmen);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
