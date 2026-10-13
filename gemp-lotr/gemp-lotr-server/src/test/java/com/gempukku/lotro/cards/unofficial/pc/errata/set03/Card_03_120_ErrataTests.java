package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_120_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: ally

					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, cost 3; a regroup ability
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, cost 2; a regroup ability
					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc, cost 4; a regroup ability
					put("overseer", "3_65");    // Orc Overseer: unique [isengard] Orc, cost 5; a regroup ability
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, damage +1
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, cost 3; Shadow: exert to add (1)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "53_120");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WastesOfEmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Wastes of Emyn Muil
		 * Unique: False
		 * Side:
		 * Culture:
		 * Shadow Number: 9
		 * Type: Site
		 * Site Number: 9
		 * Block: Fellowship
		 * Game Text: Shadow: Heal a companion to play an [Isengard] Orc from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Wastes of Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void ShadowActionHealsACompanionToPlayAnIsengardOrcFromTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		// A choice from a pile is listed by blueprint, so the [isengard] Orcs in the Shadow player's hand and in the
		// other player's discard pile are of other titles than those in his own discard pile.
		var freepsRetainer = scn.GetFreepsCard("retainer");
		var servant = scn.GetShadowCard("servant");
		var worker = scn.GetShadowCard("worker");
		var overseer = scn.GetShadowCard("overseer");
		var savage = scn.GetShadowCard("savage");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToDiscard(servant, overseer, savage, taskmaster, freepsRetainer);
		scn.MoveCardsToHand(worker);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var wastes = scn.GetCurrentSite();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals("Wastes of Emyn Muil", wastes.getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(troop, 1);
		scn.ShadowRefreshDecision();

		// 9 for the site + 2 companions
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(wastes));

		scn.ShadowUseCardAction(wastes);
		// The companions are offered, whoever they are; a wounded ally and a wounded minion are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(troop));

		// [isengard] Orcs in his discard pile are offered; an [isengard] Uruk-hai and a [sauron] Orc there are not,
		// nor an [isengard] Orc in his hand or in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(servant, overseer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, taskmaster, worker, freepsRetainer));
		scn.ShadowChooseCard(overseer);

		assertInZone(Zone.SHADOW_CHARACTERS, overseer);
		// 11 - 5 for the Orc Overseer
		assertEquals(6, scn.GetTwilight());
		assertInDiscard(servant, savage, taskmaster);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow ability: not in the maneuver phase, with a wounded companion and an [isengard] Orc in the
		// discard pile still there.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wastes));
	}

	@Test
	public void NotOfferedWhenNoCompanionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToDiscard(servant);
		scn.FreepsPass();

		var wastes = scn.GetCurrentSite();
		scn.MoveMinionsToTable(troop);
		// Only an ally and a minion are wounded.
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(troop, 1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(wastes));

		scn.AddWoundsToChar(aragorn, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(wastes));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedUnlessAnIsengardOrcInTheDiscardPileCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var overseer = scn.GetShadowCard("overseer");
		var savage = scn.GetShadowCard("savage");
		var taskmaster = scn.GetShadowCard("taskmaster");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(savage, taskmaster);
		scn.FreepsPass();

		var wastes = scn.GetCurrentSite();
		scn.AddWoundsToChar(aragorn, 1);
		scn.ShadowRefreshDecision();
		// An [isengard] Uruk-hai and a [sauron] Orc in the discard pile, and the pool could pay for either.
		assertTrue(scn.GetTwilight() >= 3);
		assertFalse(scn.ShadowActionAvailable(wastes));

		// The Orc Overseer costs 5.
		scn.MoveCardsToDiscard(overseer);
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(wastes));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(wastes));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
