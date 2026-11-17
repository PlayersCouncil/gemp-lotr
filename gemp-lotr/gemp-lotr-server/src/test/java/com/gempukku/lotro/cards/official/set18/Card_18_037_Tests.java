package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("promise", "18_37");
					put("homestead", "13_49");  // Gladden Homestead: unique [gollum] support condition that holds [gollum] tokens; a maneuver ability only
					put("fish", "15_46");       // Nice Fish: [gollum] support condition that holds [gollum] tokens; a regroup ability only
					put("heirs", "13_69");      // Heirs of Gondor: [gondor] support condition that holds [gondor] tokens; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, for a skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrustedPromiseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Trusted Promise
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Reinforce a [gollum] token. Each Shadow player may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("promise");

		assertEquals("Trusted Promise", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ReinforcesAGollumTokenOfTheChosenCardAndTheShadowPlayerMayDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var promise = scn.GetFreepsCard("promise");
		var homestead = scn.GetFreepsCard("homestead");
		var fish = scn.GetFreepsCard("fish");
		var heirs = scn.GetFreepsCard("heirs");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(homestead, fish, heirs);
		scn.MoveCardsToHand(promise);

		scn.StartGame();
		scn.AddTokensToCard(homestead, 2);
		scn.AddTokensToCard(fish, 1);
		scn.AddTokensToCard(heirs, 1);
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		int freepsHand = scn.GetFreepsHandCount();
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, shadowHand);
		assertTrue(scn.FreepsPlayAvailable(promise));
		scn.FreepsPlayCard(promise);

		// Each card with a [gollum] token, of either kind and title; not Heirs of Gondor, whose token is [gondor].
		assertTrue(scn.FreepsHasCardChoiceAvailable(homestead, fish));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heirs));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(fish);

		assertEquals(2, scn.GetCultureTokensOn(fish));
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		// The Shadow player is the one asked whether to draw; the Free Peoples player draws nothing.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("draw a card"));
		scn.ShadowChooseYes();

		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(freepsHand - 1, scn.GetFreepsHandCount()); // the event left the hand, and nothing came in
		assertInDiscard(promise);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGollumTokenInPlayItIsPlayedForNothingAndTheShadowPlayerMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var promise = scn.GetFreepsCard("promise");
		var heirs = scn.GetFreepsCard("heirs");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(heirs);
		scn.MoveCardsToHand(promise);

		scn.StartGame();
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsPlayAvailable(promise));
		scn.FreepsPlayCard(promise);

		// Nothing to reinforce: the [gondor] token is left as it was, and the draw is still offered.
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.ShadowDecisionAvailable("draw a card"));
		scn.ShadowChooseNo();

		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInDiscard(promise);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var promise = scn.GetFreepsCard("promise");
		var fish = scn.GetFreepsCard("fish");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(fish);
		scn.MoveCardsToHand(promise);

		scn.StartGame();
		scn.AddTokensToCard(fish, 1);
		assertEquals(1, scn.GetCultureTokensOn(fish));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.HAND, promise);
		assertFalse(scn.FreepsPlayAvailable(promise));
		assertEquals(1, scn.GetCultureTokensOn(fish));
	}
}
