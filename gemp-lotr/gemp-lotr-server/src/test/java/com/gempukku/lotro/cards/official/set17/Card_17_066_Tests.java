package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_66");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Lone tests of F-0444, on a board of their own: a [men] minion to kill and to discard in the maneuver phase.
	protected VirtualTableScenario GetKilledOrDiscardedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oath", "17_66");
					// Man of Bree: [men] Man, cost 2, vitality 1, site 4; strength 4 and +1 for each card in the
					// Free Peoples player's hand
					put("bree", "11_90");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger, vitality 4
					// Defend It and Hope: [gondor] Maneuver event, cost 0.  "Exert a [gondor] Man to wound a
					// minion once (or twice if that minion is an Uruk-hai)."
					put("defend", "4_115");
					// What Are They?: [gondor] Maneuver event, cost 1.  "Spot a ranger to discard a roaming minion."
					put("whatAreThey", "1_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WildmansOathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Wildman's Oath
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Each time a [men] minion is discarded during the maneuver phase, you may stack it here.<br><b>Skirmish:</b> Discard this possession to make a [men] Man strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Wildman's Oath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Each time a [men] minion is discarded during the maneuver phase": a minion that is killed was not
	// discarded, though it is placed in the discard pile (CR 4.2 "killed": "Cards that trigger when a character is
	// killed will not trigger when a character is discarded, and vice-versa"; F-0444).
	@Test
	public void AMenMinionKilledInTheManeuverPhaseWasNotDiscardedAndIsNotOfferedToStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledOrDiscardedScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var oath = scn.GetShadowCard("oath");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SkipToPhase(Phase.MANEUVER);

		// The wound takes the last of Man of Bree's vitality: he is killed.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, bree);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMenMinionDiscardedInTheManeuverPhaseMayBeStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledOrDiscardedScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatAreThey = scn.GetFreepsCard("whatAreThey");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatAreThey);

		var oath = scn.GetShadowCard("oath");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToSupportArea(oath);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		scn.SkipToPhase(Phase.MANEUVER);

		// Man of Bree, site number 4, is roaming at site 2: the one minion to discard.
		scn.FreepsPlayCard(whatAreThey);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, bree);
		assertEquals(1, scn.GetStackedCards(oath).size());
		assertSame(bree, scn.GetStackedCards(oath).get(0));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void WildmansOathTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
