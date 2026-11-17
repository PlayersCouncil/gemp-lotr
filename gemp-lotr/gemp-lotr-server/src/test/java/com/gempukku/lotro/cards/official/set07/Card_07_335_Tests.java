package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_335_Tests
{

	// The King path, King's Tent its site 2.  Site 1 is Edoras Hall ([rohan] possessions cost 1 less) and site 3
	// The Dimholt (a fellowship ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength text only
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf for Durin's Secret; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf for Durin's Secret; an archery ability only
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("secret", "1_71");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2; Damage +1 is its whole text
					// Orc Chieftain: [sauron] Orc, cost 2; its text answers a weapon played on it
					put("chieftain", "1_266");
					put("troop", "1_177");      // Goblin Patrol Troop: no text; here only a card in the Free Peoples hand
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingsTentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: King's Tent
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Shadow:</b> If the Shadow has initiative, spot your minion to make the move limit for this turn -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("King's Tent", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowActionLowersARaisedMoveLimitByOneForTheRestOfTheTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tent = scn.GetShadowSite(2);
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("gandalf", "gimli", "legolas");
		scn.MoveCardsToHand(secret);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit()); // 2 + 1
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		assertSame(tent, scn.GetCurrentSite());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(tent));

		scn.ShadowUseCardAction(tent);
		// 2 + 1 - 1: a penalty of 2 would read 1.
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Still in force at the end of the turn: one move of two made, so another is asked for, and declined.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		// The next turn, the other player's, has the move limit of two the rules give: the -1 ended with the turn.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void IsNotOfferedWhileTheFreePeoplesPlayerHasFourCardsAndIsOfferedAtThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tent = scn.GetShadowSite(2);
		var troop = scn.GetFreepsCard("troop");

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SetFreepsHand(troop, scn.GetFreepsCard("savage"), scn.GetFreepsCard("chieftain"), scn.GetFreepsCard("secret"));
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		assertSame(tent, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Four cards in the Free Peoples hand: he has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowActionAvailable(tent));

		scn.MoveCardsToDiscard(troop);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(tent));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTheShadowPlayerHasAMinionAndThenEndsTheTurnAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tent = scn.GetShadowSite(2);
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SetShadowHand(chieftain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(tent, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		// No minion in play.
		assertFalse(scn.ShadowActionAvailable(tent));

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowActionAvailable(tent));
		assertEquals(2, scn.GetMoveLimit());

		scn.ShadowUseCardAction(tent);
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());
		scn.PassCurrentPhaseActions();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
