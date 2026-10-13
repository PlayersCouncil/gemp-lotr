package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "5_30");
					// put other cards in here as needed for the test case

					// Gollums, each a unique minion of cost 2 and site number 3 (2 + 2 roaming at Ettenmoors, site 2)
					put("villain", "6_40");     // Gollum, Old Villain: a skirmish ability only
					put("wraith", "103_66");    // Gollum, Half a Wraith Himself: answers a Nazgul played, and an assignment ability
					put("nasty", "5_24");       // Gollum, Nasty Treacherous Creature: a regroup ability only
					put("vile", "7_59");        // Gollum, Vile Creature: a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeMustHaveItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: We Must Have It
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow, Skirmish
		 * Game Text: <b>Shadow:</b> Play Gollum from your draw deck or discard pile.<br><b>Skirmish:</b> Discard a card at random from hand to make Gollum strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("We Must Have It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADrawDeckLookedThroughForAGollumNotPlayedIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		// The play is the effect of a costToEffect inside the event's cost, so it has always been carried out as an
		// effect and the shuffle after it reached (F-0494 was Captured by the Ring 7_53's, whose play is the cost).
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		// Old Villain and Half a Wraith Himself stay in the draw deck; the other two Gollums are in hand, so the
		// discard pile has none and no pile is asked for.
		scn.MoveCardsToHand(card, scn.GetShadowCard("nasty"), scn.GetShadowCard("vile"));

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, villain, wraith);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(card);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(villain, wraith));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		// CR 4.2 "draw deck": "When you finish looking through it, reshuffle it" (Q-0013: always), once, whether
		// or not a card is played from it. The other player's deck is not shuffled.
		assertInZone(Zone.DECK, villain, wraith);
		assertInZone(Zone.DISCARD, card);
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void WeMustHaveItTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
