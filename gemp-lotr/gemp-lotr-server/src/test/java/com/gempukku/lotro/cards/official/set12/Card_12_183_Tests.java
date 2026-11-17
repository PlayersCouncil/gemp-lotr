package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_183_Tests
{

	// Frodo, Son of Drogo 1_290 is the Ring-bearer: vitality 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "12_183");
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul minion, cost 5; strength +1 for each companion, nothing about costs
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; his abilities are used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, nothing on his play
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3, nothing on his play
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1, nothing on his play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Witch-king, Black Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>The Witch-king is twilight cost -1 for each wound on the Ring-bearer.<br>Each time a companion is played, you may reveal this card from hand to exert the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Black Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsTwilightCostMinusOneForEachWoundOnTheRingBearerAndForNoOtherWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(witchking, rider);
		assertInHand(witchking, rider);

		// Wounds on Sam, who is not the Ring-bearer, count for nothing.
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(8, scn.GetTwilightCost(witchking));
		assertEquals(5, scn.GetTwilightCost(rider));

		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(7, scn.GetTwilightCost(witchking));
		assertEquals(5, scn.GetTwilightCost(rider));

		scn.AddWoundsToChar(frodo, 2);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetTwilightCost(witchking));
		assertEquals(5, scn.GetTwilightCost(rider));

		// To The Bridge of Khazad-dum, site 5: the Witch-king (site 3) is not roaming there.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(witchking);

		// 8 - 3 wounds on the Ring-bearer
		assertEquals(pool - 5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeACompanionIsPlayedItMayBeRevealedToExertTheRingBearerButNotForAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam);

		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.MoveCardsToHand(gimli, boromir, bounder);
		scn.MoveCardsToHand(witchking, rider);
		assertInHand(witchking, rider);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();

		// The Witch-king is shown to the Free Peoples player alone, and nothing else in the hand is.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(witchking, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The Ring-bearer exerts; Sam, Ring-bound, does not.  The Witch-king stays in hand.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInHand(witchking, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second companion, of another culture and race: again.
		scn.FreepsPlayCard(boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An ally is no companion.
		scn.FreepsPlayCard(bounder);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningShowsNothingAndExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveCardsToHand(gimli);
		scn.MoveCardsToHand(witchking);
		assertInHand(witchking);
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInHand(witchking);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheRingBearerExhaustedItIsStillOfferedAndRevealedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam);

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveCardsToHand(gimli);
		scn.MoveCardsToHand(witchking);
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInHand(witchking);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(gimli);
		// The reveal is a cost, and can be paid (Q-0034).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(witchking, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItsOwnersOwnCompanionPlayedOnHisOwnTurnDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var witchking = scn.GetFreepsCard("witchking");

		scn.StartGame();
		scn.MoveCardsToHand(gimli, witchking);
		assertInHand(witchking);
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsRefreshDecision();

		// On his own turn he is the Free Peoples player, and a Shadow card in his hand is not his to use then.
		scn.FreepsPlayCard(gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(witchking));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInHand(witchking);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
