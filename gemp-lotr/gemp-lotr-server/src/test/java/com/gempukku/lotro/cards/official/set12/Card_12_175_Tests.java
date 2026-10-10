package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_175");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "12_175");
					// Minas Morgul Answers: [wraith] Shadow event, cost 4.  "Toil 2.  Spot a Nazgûl to discard a
					// Free Peoples condition from play."
					put("answers", "12_167");
					// Sword Rack: a [rohan] support-area condition for the event to discard; its response answers a
					// [rohan] possession discarded from play, and there is none
					put("rack", "11_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Úlairë Enquëa, Black Threat
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>. <b>Toil 1</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -1.)</helper><br><b>Response:</b> If you are playing a [wraith] event that has toil X, exert Úlairë Enquëa to reduce that event's twilight cost by X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Black Threat", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ResponseExertsEnqueaToTakeTheToilNumberOffAWraithEventAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var enquea = scn.GetShadowCard("enquea");
		var answers = scn.GetShadowCard("answers");
		var rack = scn.GetFreepsCard("rack");
		scn.MoveCardsToSupportArea(rack);
		scn.MoveCardsToHand(answers);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(answers));
		assertEquals(0, scn.GetWoundsOn(enquea));

		// The response answers the event being played: it is offered before anything has been paid.
		scn.ShadowPlayCard(answers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertNotInZone(Zone.DISCARD, answers);
		assertEquals(2, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// He may answer the same play again (CR 4.2 "response"), and can still exert: declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();

		// Then toil is asked.  The event now costs 4 - 2, which the pool covers, so nobody need be exerted for
		// it; Enquëa is exerted all the same.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(enquea);
		assertEquals(2, scn.GetWoundsOn(enquea));

		// 4 - 2 for Enquëa's response - 2 for toil = 0: nothing leaves the pool.
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(answers);
		assertInDiscard(rack);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseAloneTakesTwoOffAToilTwoEventWithNobodyExertedForToil() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var enquea = scn.GetShadowCard("enquea");
		var answers = scn.GetShadowCard("answers");
		var rack = scn.GetFreepsCard("rack");
		scn.MoveCardsToSupportArea(rack);
		scn.MoveCardsToHand(answers);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.FreepsPass();

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(enquea));

		scn.ShadowPlayCard(answers);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));
		scn.ShadowDeclineOptionalTrigger();

		// The response has taken the event's own toil number off, 4 - 2, and not Enquëa's Toil 1: with 3 still
		// to pay from a pool of 2, an exertion for toil would be owed.  None is.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// 4 - 2 for Enquëa's response = 2, paid from the pool.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(answers);
		assertInDiscard(rack);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
