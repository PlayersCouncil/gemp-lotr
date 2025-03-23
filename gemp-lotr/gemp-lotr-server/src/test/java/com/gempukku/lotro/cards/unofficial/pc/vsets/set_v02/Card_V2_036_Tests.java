package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_036_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("pit", "102_36");
					put("warg1", "5_65");
					put("warg2", "5_64");
					put("warg3", "5_59");
					put("rider1", "5_67");
					put("rider2", "5_67");
					put("uruk", "1_151");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void WolfPitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Wolf Pit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: When you play this possession, you may stack two [isengard] mounts here from your draw deck or discard pile.
		* 	Shadow: Exert an [Isengard] minion to play an [Isengard] mount stacked here as if from hand. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pit");

		assertEquals("Wolf Pit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WolfPitCanStack2MountsFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var warg1 = scn.GetShadowCard("warg1");
		var warg2 = scn.GetShadowCard("warg2");
		var warg3 = scn.GetShadowCard("warg3");
		scn.ShadowMoveCardToHand(pit);
		scn.ShadowMoveCardToDiscard(warg1, warg2, warg3);

		scn.StartGame();

		scn.SkipToPhase(Phase.SHADOW);
		assertTrue(scn.ShadowPlayAvailable(pit));
		scn.ShadowPlayCard(pit);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("discard");

		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(Zone.DISCARD, warg1.getZone());
		scn.ShadowChooseCardBPFromSelection(warg1);
		assertEquals(Zone.STACKED, warg1.getZone());
		assertSame(pit, warg1.getStackedOn());

		//We now optionally do it all again a second time
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("discard");

		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(Zone.DISCARD, warg2.getZone());
		scn.ShadowChooseCardBPFromSelection(warg2);
		assertEquals(Zone.STACKED, warg2.getZone());
		assertSame(pit, warg2.getStackedOn());

		assertEquals(Zone.DISCARD, warg3.getZone());
	}

	@Test
	public void WolfPitCanStack2MountsFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var warg1 = scn.GetShadowCard("warg1");
		var warg2 = scn.GetShadowCard("warg2");
		var warg3 = scn.GetShadowCard("warg3");
		scn.ShadowMoveCardToHand(pit);
		scn.ShadowMoveCardsToTopOfDeck(warg1, warg2, warg3);

		scn.StartGame();

		scn.SkipToPhase(Phase.SHADOW);
		assertTrue(scn.ShadowPlayAvailable(pit));
		scn.ShadowPlayCard(pit);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("deck");
		scn.ShadowDismissRevealedCards();

		assertEquals(3, scn.ShadowGetSelectableCount());
		assertEquals(Zone.DECK, warg1.getZone());
		scn.ShadowChooseCardBPFromSelection(warg1);
		assertEquals(Zone.STACKED, warg1.getZone());
		assertSame(pit, warg1.getStackedOn());

		//We now optionally do it all again a second time
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("deck");
		scn.ShadowDismissRevealedCards();

		assertEquals(2, scn.ShadowGetSelectableCount());
		assertEquals(Zone.DECK, warg2.getZone());
		scn.ShadowChooseCardBPFromSelection(warg2);
		assertEquals(Zone.STACKED, warg2.getZone());
		assertSame(pit, warg2.getStackedOn());

		assertEquals(Zone.DECK, warg3.getZone());
	}

	@Test
	public void WolfPitCanStack1MountFromDrawDeckAnd1FromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var warg1 = scn.GetShadowCard("warg1");
		var warg2 = scn.GetShadowCard("warg2");
		var warg3 = scn.GetShadowCard("warg3");
		scn.ShadowMoveCardToHand(pit);
		scn.ShadowMoveCardsToTopOfDeck(warg1);
		scn.ShadowMoveCardToDiscard(warg2, warg3);

		scn.StartGame();

		scn.SkipToPhase(Phase.SHADOW);
		assertTrue(scn.ShadowPlayAvailable(pit));
		scn.ShadowPlayCard(pit);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("deck");
		scn.ShadowDismissRevealedCards();

		assertEquals(1, scn.ShadowGetSelectableCount());
		assertEquals(Zone.DECK, warg1.getZone());
		scn.ShadowChooseCardBPFromSelection(warg1);
		assertEquals(Zone.STACKED, warg1.getZone());
		assertSame(pit, warg1.getStackedOn());

		//We now optionally do it all again a second time
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowChooseMultipleChoiceOption("discard");

		assertEquals(2, scn.ShadowGetSelectableCount());
		assertEquals(Zone.DISCARD, warg2.getZone());
		scn.ShadowChooseCardBPFromSelection(warg2);
		assertEquals(Zone.STACKED, warg2.getZone());
		assertSame(pit, warg2.getStackedOn());

		assertEquals(Zone.DISCARD, warg3.getZone());
	}

	@Test
	public void WolfPitShadowExertIsengardMinionToPlayIsenMountFromStack() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var warg1 = scn.GetShadowCard("warg1");
		var warg2 = scn.GetShadowCard("warg2");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCardToSupportArea(pit);
		scn.ShadowMoveCharToTable(rider1, rider2, uruk);
		scn.StackCardsOn(pit, warg1, warg2);

		scn.StartGame();

		scn.SetTwilight(50);

		scn.SkipToPhase(Phase.SHADOW);
		assertTrue(scn.ShadowActionAvailable(pit));
		scn.ShadowUseCardAction(pit);
		assertEquals(Zone.STACKED, warg1.getZone());
		//choosing exert cost: 2 warg-riders and an uruk
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(3, scn.GetVitality(uruk));
		scn.ShadowChooseCard(uruk);
		assertEquals(2, scn.GetVitality(uruk));

		//choosing the mount to play that's stacked on pit
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(warg1);

		//choosing the warg-rider for it to go on
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(rider1);
		assertEquals(Zone.ATTACHED, warg1.getZone());
		assertSame(rider1, warg1.getAttachedTo());
	}
}
