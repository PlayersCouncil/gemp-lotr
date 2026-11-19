package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("business", "18_65");
					put("business2", "18_65");

					put("corps", "11_73");      // Corps of Harad: a [men] minion, held in hand
					put("stragglers", "13_99"); // Stragglers: a [men] condition, held in hand
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] minion with no text, held in hand

					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2, no play requirement
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3, no play requirement
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ALLY, cost 1, no play requirement
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeclinedBusinessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Declined Business
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the Free Peoples player plays a companion, discard a [men] card from hand to return that companion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("business");

		assertEquals("Declined Business", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenACompanionIsPlayedDiscardingAMenCardReturnsThatCompanionToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var business = scn.GetShadowCard("business");
		var corps = scn.GetShadowCard("corps");
		var stragglers = scn.GetShadowCard("stragglers");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveCardsToHand(gimli, boromir, business, corps, stragglers, brigand);
		assertInZone(Zone.HAND, gimli, boromir, business, corps, stragglers, brigand);
		scn.FreepsRefreshDecision();

		// A [gondor] Man companion played: offered; declined, he stays.
		scn.FreepsPlayCard(boromir);
		assertTrue(scn.ShadowPlayAvailable(business));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [dwarven] Dwarf companion played: offered again, and played.
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.ShadowPlayAvailable(business));
		scn.ShadowPlayCard(business);

		// The cost: a [men] card of either type from hand; not the card of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, stragglers));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stragglers);

		assertInZone(Zone.DISCARD, stragglers, business);
		assertInZone(Zone.HAND, corps, brigand);
		// Gimli is back in the Free Peoples player's hand; Boromir, played before, is untouched.
		assertTrue(scn.GetFreepsHand().contains(gimli));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheFreePeoplesPlayerPlaysAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var business = scn.GetShadowCard("business");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveCardsToHand(gimli, bounder, business, corps);
		assertInZone(Zone.HAND, gimli, bounder, business, corps);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(bounder);
		// No response window for the Shadow player: the Free Peoples player is asked again at once.
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, business);

		// The control: a companion played on the same board is answered.
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.ShadowPlayAvailable(business));
		assertTrue(scn.ShadowDecisionAvailable("Optional responses"));
	}

	@Test
	public void NotOfferedWithNoOtherMenCardInHandToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var business = scn.GetShadowCard("business");
		var business2 = scn.GetShadowCard("business2");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveCardsToHand(gimli, boromir, business, brigand);
		assertInZone(Zone.HAND, gimli, boromir, business, brigand);
		scn.FreepsRefreshDecision();

		// Declined Business cannot pay with itself (CR 4.2 "playing a card"), and the Brigand is no [men] card.
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second copy in hand is a [men] card that can pay.
		scn.MoveCardsToHand(business2);
		assertInZone(Zone.HAND, business2);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(boromir);
		assertTrue(scn.ShadowPlayAvailable(business));
		assertTrue(scn.ShadowDecisionAvailable("Optional responses"));
	}
}
