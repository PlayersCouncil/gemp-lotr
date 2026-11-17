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

public class Card_06_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reserve1", "6_5");
					put("reserve2", "6_5");
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
	public void DunlendingReserveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Dunlending Reserve
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: The twilight cost of this minion during a skirmish phase is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reserve1");

		assertEquals("Dunlending Reserve", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DuringASkirmishPhaseItCostsTwoLessAndHillClanPlaysItFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reserve1 = scn.GetShadowCard("reserve1");
		var reserve2 = scn.GetShadowCard("reserve2");
		var clan = scn.GetShadowCard("clan");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Strength 10 by hand, so that Hill Clan's 17 beats Aragorn without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(clan);
		scn.SetShadowHand(reserve1, reserve2, savage);
		assertInHand(reserve1, reserve2, savage);

		scn.SkipToAssignments();
		// Balin's Tomb, site 4: no minion of site 3 is roaming.  The assignment phase: printed cost.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilightCost(reserve1));
		assertEquals(4, scn.GetTwilightCost(reserve2));
		assertEquals(4, scn.GetTwilightCost(savage));
		scn.FreepsAssignToMinions(aragorn, clan);
		scn.FreepsResolveSkirmish(aragorn);

		// The skirmish phase: 4 - 2 for each Reserve, and the Savage, another [dunland] Man, at his printed 4.
		assertEquals(2, scn.GetTwilightCost(reserve1));
		assertEquals(2, scn.GetTwilightCost(reserve2));
		assertEquals(4, scn.GetTwilightCost(savage));
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());

		// Hill Clan beats Aragorn, 17 to 10.  Out of a pool of 2, either Reserve can be played, and not the Savage.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(clan));
		scn.ShadowChooseOptionalTrigger(clan);
		assertTrue(scn.ShadowHasCardChoiceAvailable(reserve1, reserve2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		scn.ShadowChooseCard(reserve1);

		assertInZone(Zone.SHADOW_CHARACTERS, reserve1);
		assertInHand(reserve2, savage);
		// 2 - (4 to play - 2)
		assertEquals(0, scn.GetTwilight());

		// The fierce assignment phase the Reserve played brings about (Hill Clan made him fierce): printed cost again.
		assertTrue(scn.InFierceRound());
		assertEquals(4, scn.GetTwilightCost(reserve2));
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// And in the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetTwilightCost(reserve2));
		assertEquals(4, scn.GetTwilightCost(savage));
	}

	@Test
	public void InTheShadowPhaseItCostsItsPrintedFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reserve = scn.GetShadowCard("reserve1");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(reserve);
		assertInHand(reserve);
		// The move to Balin's Tomb, site 4: no minion of site 3 is roaming.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilightCost(reserve));

		// Not a skirmish phase: the full 4.  With a pool of 3 it cannot be played; with 4 it can, and takes it all.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(reserve));
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(reserve));
		scn.ShadowPlayCard(reserve);
		assertInZone(Zone.SHADOW_CHARACTERS, reserve);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
