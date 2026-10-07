package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_067_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "53_67");
					put("troop", "1_143");      // Troop of Uruk-hai: an [isengard] minion, strength 9, vitality 4
					put("aragorn", "1_89");     // Free Peoples cards for the Free Peoples player's hand
					put("sam", "1_311");
					put("runner", "1_178");     // and a Shadow card beside them
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePalantirofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Palantir of Orthanc
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Palantir
		 * Game Text: Plays to your support area.
		* 	<b>Shadow:</b> Exert an [isengard] minion.  The Free Peoples player may place any number of cards from hand on top of their draw deck, then reveals their hand.  For each Free Peoples card revealed, make that minion strength +1 until the regroup phase.  Limit once per phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("The Palantir of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PALANTIR));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ThePalantirofOrthancTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveCompanionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		//var card = scn.GetShadowCard("card");
		scn.MoveCardsToHand(card);
		scn.MoveMinionsToTable(card);
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToDiscard(card);
		scn.MoveCardsToTopOfDeck(card);

		scn.StartGame();
		
		assertFalse(true);
	}

	@Test
	public void ShadowAbilityAtItsLimitStillExertsAMinionAndDoesNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(card);
		// Two Free Peoples cards and a Shadow card in the Free Peoples player's hand.
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"), scn.GetFreepsCard("runner"));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(troop));
		assertEquals("false", scn.ShadowActionNoEffect(card));
		scn.ShadowUseCardAction(card);
		scn.FreepsChooseNo();
		scn.ShadowDismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(11, scn.GetStrength(troop));

		// Limit once per phase: the minion can still exert, so the ability is still offered, flagged as
		// doing nothing (Ketura on Q-0041).
		assertTrue(scn.ShadowActionAvailable(card));
		assertEquals("limit", scn.ShadowActionNoEffect(card));

		// Used anyway: the minion exerts, the Free Peoples player is asked nothing and shows no hand.
		scn.ShadowUseCardAction(card);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
