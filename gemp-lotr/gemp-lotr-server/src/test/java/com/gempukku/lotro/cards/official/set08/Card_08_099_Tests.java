package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "8_99");
					put("patrol2", "8_99");
					put("patrol3", "8_99");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength text only
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf for Durin's Secret; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf for Durin's Secret; an archery ability only
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("secret", "1_71");
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition that play stacks a [sauron] Orc on
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothPatrolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Patrol
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Regroup:</b> If this minion is stacked on a site you control, discard this minion to make the move limit for this turn -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patrol");

		assertEquals("Gorgoroth Patrol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StackedOnASiteItsOwnerControlsItIsDiscardedToLowerTheMoveLimitByOneForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable("gandalf", "gimli", "legolas");

		var patrol = scn.GetShadowCard("patrol");
		var patrol2 = scn.GetShadowCard("patrol2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, patrol, patrol2);
		assertInZone(Zone.STACKED, patrol, patrol2);
		scn.MoveCardsToHand(secret);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(secret);
		// The move limit never reads below 1: raised first, so that -1 and -2 read apart.
		assertEquals(3, scn.GetMoveLimit()); // 2 + 1

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.ShadowControls(site1));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(patrol));
		scn.ShadowUseCardAction(patrol);

		// 2 + 1 - 1: a penalty of 2 would read 1.  The one used is discarded; the other stays on the site.
		assertEquals(2, scn.GetMoveLimit());
		assertInDiscard(patrol);
		assertTrue(scn.GetStackedCards(site1).contains(patrol2));
		assertFalse(scn.GetStackedCards(site1).contains(patrol));

		// Still in force after the regroup phase it was used in: one move of two, so a second is offered, and in
		// that move's Shadow phase the limit is still 2.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheLimitItLowersEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, patrol);
		assertInZone(Zone.STACKED, patrol);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.ShadowUseCardAction(patrol);
		assertEquals(1, scn.GetMoveLimit());
		assertInDiscard(patrol);

		// At the move limit the turn ends with no question of another move; the next turn, the other player's,
		// has the move limit of two the rules give.
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsDeclineReconciliation();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void OnlyAPatrolStackedOnASiteItsOwnerControlsOffersItsRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var patrol2 = scn.GetShadowCard("patrol2");
		var patrol3 = scn.GetShadowCard("patrol3");
		var teeth = scn.GetShadowCard("teeth");
		var theirPatrol = scn.GetFreepsCard("patrol");

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 is the other player's (he took it as the Shadow player of an earlier turn), with a Patrol of his
		// own on it; site 2 is the Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.StackCardsOn(site1, theirPatrol);
		scn.StackCardsOn(site2, patrol);
		// A Patrol stacked on a condition.
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, patrol2);
		assertInZone(Zone.STACKED, theirPatrol, patrol, patrol2);

		scn.SkipToPhase(Phase.REGROUP);
		// A Patrol in play, one that has come through its skirmish.
		scn.MoveMinionsToTable(patrol3);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(patrol));
		assertFalse(scn.ShadowActionAvailable(patrol2));
		assertFalse(scn.ShadowActionAvailable(patrol3));
		assertFalse(scn.ShadowActionAvailable(theirPatrol));
	}
}
