package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_092_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("theoden", "8_92");
					put("rider", "4_286");
					put("eomer", "4_266");

					put("sauron", "9_48");
					put("bowman", "2_60");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Théoden, Tall and Proud
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Valiant</b>.
		 * While you can spot a [rohan] Man, Théoden's twilight cost is -1.
		 * When Théoden is killed, you may play a [rohan] companion from your discard pile or draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("Tall and Proud", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void TheodenPlaysRohanCompanionFromDeckWhenDyingFromWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		scn.FreepsMoveCharToTable(theoden);
		scn.FreepsMoveCardsToTopOfDeck(eomer, rider);

		var bowman = scn.GetShadowCard("bowman");
		scn.ShadowMoveCharToTable(bowman);

		scn.StartGame();

		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertEquals(Zone.FREE_CHARACTERS, theoden.getZone());
		assertEquals(Zone.DECK, eomer.getZone());
		assertEquals(Zone.DECK, rider.getZone());

		//arrow
		scn.FreepsChooseCard(theoden);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(rider);

		assertEquals(Zone.DEAD, theoden.getZone());
		assertEquals(Zone.DECK, eomer.getZone());
		assertEquals(Zone.FREE_CHARACTERS, rider.getZone());
	}

	@Test
	public void TheodenPlaysRohanCompanionFromDiscardWhenDyingFromWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		scn.FreepsMoveCharToTable(theoden);
		scn.FreepsMoveCardToDiscard(eomer, rider);

		var bowman = scn.GetShadowCard("bowman");
		scn.ShadowMoveCharToTable(bowman);

		scn.StartGame();

		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertEquals(Zone.FREE_CHARACTERS, theoden.getZone());
		assertEquals(Zone.DISCARD, eomer.getZone());
		assertEquals(Zone.DISCARD, rider.getZone());

		//arrow
		scn.FreepsChooseCard(theoden);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(rider);

		assertEquals(Zone.DEAD, theoden.getZone());
		assertEquals(Zone.DISCARD, eomer.getZone());
		assertEquals(Zone.FREE_CHARACTERS, rider.getZone());
	}

	@Test
	public void TheodenOffersChoiceWhenValidOptionsInBothDrawDeckAndDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		scn.FreepsMoveCharToTable(theoden);
		scn.FreepsMoveCardsToTopOfDeck(eomer);
		scn.FreepsMoveCardToDiscard(rider);

		var bowman = scn.GetShadowCard("bowman");
		scn.ShadowMoveCharToTable(bowman);

		scn.StartGame();

		scn.AddWoundsToChar(theoden, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertEquals(Zone.FREE_CHARACTERS, theoden.getZone());
		assertEquals(Zone.DECK, eomer.getZone());
		assertEquals(Zone.DISCARD, rider.getZone());

		//arrow
		scn.FreepsChooseCard(theoden);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsChoiceAvailable("draw deck"));
		assertTrue(scn.FreepsChoiceAvailable("discard pile"));
	}

	@Test
	public void TheodenTriggersWhenKilledByThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		scn.FreepsMoveCharToTable(theoden, rider);
		scn.FreepsMoveCardsToTopOfDeck(eomer);

		var bowman = scn.GetShadowCard("bowman");
		scn.ShadowMoveCharToTable(bowman);

		scn.StartGame();

		scn.AddThreats(3);
		scn.AddWoundsToChar(theoden, 2);
		scn.AddWoundsToChar(rider, 2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertEquals(Zone.FREE_CHARACTERS, theoden.getZone());
		assertEquals(Zone.DECK, eomer.getZone());
		assertEquals(Zone.FREE_CHARACTERS, rider.getZone());

		//arrow
		scn.FreepsChooseCard(rider);
		//threats
		scn.FreepsChooseCard(theoden);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(Zone.DEAD, theoden.getZone());
		assertEquals(Zone.FREE_CHARACTERS, eomer.getZone());
		assertEquals(Zone.DEAD, rider.getZone());
	}
}
