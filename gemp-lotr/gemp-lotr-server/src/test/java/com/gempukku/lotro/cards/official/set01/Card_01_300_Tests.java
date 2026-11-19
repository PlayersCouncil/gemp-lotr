package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_300_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf", "1_300");
					put("leaf2", "1_300");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: a Free Peoples companion, held in hand
					put("runner", "1_178");        // Goblin Runner: a Shadow minion, held in hand
					put("sword", "1_299");         // Hobbit Sword: a [shire] possession with no trigger, held in hand or played on Frodo
					put("deftness", "1_293");      // Halfling Deftness: a Skirmish event, held in hand (neither character nor item)
					// Old Toby: a [shire] pipeweed possession in the support area (a card in play is not in hand)
					put("toby", "1_305");
					// Goblin Patrol Troop: no text; the Shadow player's card in his own hand, of a title the
					// Free Peoples hand does not hold
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongbottomLeafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Longbottom Leaf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Pipeweed</b>. Plays to your support area.<br>When you play this possession, you may discard up to 2 cards from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaf");

		assertEquals("Longbottom Leaf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedUpToTwoCardsOfAnyKindMayBeDiscardedFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var sword = scn.GetFreepsCard("sword");
		var deftness = scn.GetFreepsCard("deftness");
		var toby = scn.GetFreepsCard("toby");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(leaf, aragorn, runner, sword, deftness);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToShadowHand("troop");

		scn.StartGame();

		assertInHand(leaf, aragorn, runner, sword, deftness);
		assertInZone(Zone.SUPPORT, toby);
		assertInHand(troop);
		assertEquals(5, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(leaf));
		scn.FreepsAcceptOptionalTrigger();

		// A companion, a Shadow minion, a possession and an event in hand; not a pipeweed in play, nor the Shadow
		// player's card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, runner, sword, deftness));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(toby, troop));
		// "Up to 2": none may be chosen once the offer is taken (F-0385, as Ketura wants it), and no more than 2.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, runner);

		assertInDiscard(aragorn, runner);
		assertInHand(sword, deftness);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, toby);
		assertInHand(troop);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferMayBeDeclinedOrTakenWithNothingDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var leaf2 = scn.GetFreepsCard("leaf2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(leaf, leaf2, aragorn, runner);

		scn.StartGame();

		assertInHand(leaf, leaf2, aragorn, runner);
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(leaf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(leaf));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, leaf);
		assertInHand(leaf2, aragorn, runner);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second copy makes its own offer; taken, it may still discard nothing.
		scn.FreepsPlayCard(leaf2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(leaf2));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.SUPPORT, leaf, leaf2);
		assertInHand(aragorn, runner);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherPossessionPlayedAfterItAndASecondCopyPlayedMakeNoFurtherOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var leaf2 = scn.GetFreepsCard("leaf2");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(leaf, leaf2, sword, aragorn);

		scn.StartGame();

		assertInHand(leaf, leaf2, sword, aragorn);

		scn.FreepsPlayCard(leaf);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another possession played once the Leaf is in play: no offer.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second copy's play brings its own offer, and the first copy makes none.
		scn.FreepsPlayCard(leaf2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(leaf2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(leaf));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, leaf, leaf2);
		assertInHand(aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoOtherCardInHandNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToHand(leaf);

		scn.StartGame();

		assertInHand(leaf);
		assertEquals(1, scn.GetFreepsHandCount());

		// The Leaf was the last card in hand: once it is played there is nothing to discard (Q-0224).
		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(leaf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
