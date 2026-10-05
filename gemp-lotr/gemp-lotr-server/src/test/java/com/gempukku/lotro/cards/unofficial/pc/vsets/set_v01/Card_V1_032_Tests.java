package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("terror", "101_32");

					// Two cards titled The Balrog.  Both cost far more than any pool here, so neither is ever played.
					put("bane", "2_51");        // The Balrog, Durin's Bane: cost 12
					put("flame", "2_52");       // The Balrog, Flame of Udun: cost 14

					// The Balrog's Sword: a [moria] artifact whose bearer must be The Balrog.  Its title begins with
					// "The Balrog" and it is not The Balrog.
					put("sword", "2_50");
					// Goblin Patrol Troop: a [moria] minion with no text, cost 6.
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Terror at Its Coming
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time the fellowship moves, you may reveal The Balrog from your hand to add (2).
		 */

		var scn = GetScenario();

		var terror = scn.GetFreepsCard("terror");

		assertEquals("Terror at Its Coming", terror.getBlueprint().getTitle());
		assertNull(terror.getBlueprint().getSubtitle());
		assertTrue(terror.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, terror.getBlueprint().getSide());
		assertEquals(Culture.MORIA, terror.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, terror.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(terror, Keyword.SUPPORT_AREA));
		assertEquals(0, terror.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForNoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		scn.MoveCardsToHand(terror);

		scn.StartGame();
		scn.FreepsPass();

		// 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, terror);
		assertTrue(scn.ShadowPlayAvailable(terror));

		scn.ShadowPlayCard(terror);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, terror);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RevealingTheBalrogFromHandWhenTheFellowshipMovesAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var bane = scn.GetShadowCard("bane");
		scn.MoveCardsToSupportArea(terror);
		scn.MoveCardsToHand(bane);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPass();

		// "When the fellowship moves" comes before the move's own twilight is added.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(terror));
		assertEquals(0, scn.GetTwilight());
		scn.ShadowAcceptOptionalTrigger();

		// The Balrog is shown to the other player.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bane, false));
		scn.FreepsDismissRevealedCards();

		// 1 companion + 2 for site 2 + 2 for Terror
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, bane);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningRevealsNothingAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var bane = scn.GetShadowCard("bane");
		scn.MoveCardsToSupportArea(terror);
		scn.MoveCardsToHand(bane);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(terror));
		scn.ShadowDeclineOptionalTrigger();

		// The Free Peoples player is shown nothing, and the pool is only what the move put there:
		// 1 companion + 2 for site 2
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, bane);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutTheBalrogInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var bane = scn.GetShadowCard("bane");
		var sword = scn.GetShadowCard("sword");
		var troop = scn.GetShadowCard("troop");
		var freepsFlame = scn.GetFreepsCard("flame");
		scn.MoveCardsToSupportArea(terror);
		// In the Shadow player's hand: a [moria] minion that is not The Balrog, and The Balrog's Sword.
		scn.MoveCardsToHand(troop, sword);
		// The Balrog itself is in his discard pile, and another is in the other player's hand.
		scn.MoveCardsToDiscard(bane);
		scn.MoveCardsToHand(freepsFlame);

		scn.StartGame();

		assertInZone(Zone.DISCARD, bane);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		// 1 companion + 2 for site 2
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnyCardTitledTheBalrogInHandMayBeRevealedAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var bane = scn.GetShadowCard("bane");
		var flame = scn.GetShadowCard("flame");
		var sword = scn.GetShadowCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(terror);
		scn.MoveCardsToHand(bane, flame, sword, troop);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(terror));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(bane, flame));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, troop));
		scn.ShadowChooseCard(flame);

		// The one chosen is the one shown.
		assertTrue(scn.FreepsHasCardChoiceAvailable(flame, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(bane, false));
		scn.FreepsDismissRevealedCards();

		// 1 companion + 2 for site 2 + 2 for Terror
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, bane, flame, sword, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedAgainWhenTheFellowshipMovesASecondTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var bane = scn.GetShadowCard("bane");
		scn.MoveCardsToSupportArea(terror);
		scn.MoveCardsToHand(bane);
		// The Shadow player reconciles before the second move.  With his draw deck empty he draws nothing, and
		// Durin's Bane is still the only Balrog in his hand.
		scn.MoveCardsToDiscard(scn.GetShadowCard("flame"), scn.GetShadowCard("sword"), scn.GetShadowCard("troop"));

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(terror));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// 1 companion + 2 for site 2 + 2 for Terror
		assertEquals(5, scn.GetTwilight());

		// With no minion in play the turn goes straight to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, bane);
		assertEquals(1, scn.GetShadowHandCount());
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(terror));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bane, false));
		scn.FreepsDismissRevealedCards();

		// 5 already there + 1 companion + 0 for site 3 + 2 for Terror
		assertEquals(8, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheFellowshipStaysWhereItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var bane = scn.GetShadowCard("bane");
		scn.MoveCardsToSupportArea(terror);
		scn.MoveCardsToHand(bane);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(terror));
		scn.ShadowDeclineOptionalTrigger();

		scn.SkipToMovementDecision();
		assertInZone(Zone.HAND, bane);
		assertInZone(Zone.SUPPORT, terror);
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsChooseToStay();

		// Nothing is offered: the turn ends, and the next thing asked is the other player's fellowship phase.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
