package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oath", "11_91");
					// Harad Standard-bearer: a [men] Man minion.  Its skirmish ability is never used.
					put("bearer", "11_84");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit.  His maneuver ability wants a
					// site he controls (none is) and his regroup ability is never reached.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion with no text.
					put("brigand", "4_10");
					// Lying in Wait: a [men] condition in the support area.  Its skirmish ability is never used, and
					// its other two sentences read only once it is borne.
					put("lying", "13_95");
					// Legion of Harad: a [men] minion with no text but toil, only ever held in a hand.
					put("legion1", "11_88");
					put("legion2", "11_88");
					put("legion3", "11_88");
					put("legion4", "11_88");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OathSwornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Oath Sworn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of the maneuver phase, if you have 2 or more cards in hand, you may spot 2 [men] minions and discard your hand to add (8).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("oath");

		assertEquals("Oath Sworn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseSpotting2MenMinionsItDiscardsTheWholeHandToAdd8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freepsLegion1 = scn.GetFreepsCard("legion1");
		var freepsLegion2 = scn.GetFreepsCard("legion2");

		var oath = scn.GetShadowCard("oath");
		var bearer = scn.GetShadowCard("bearer");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var lying = scn.GetShadowCard("lying");
		var legion1 = scn.GetShadowCard("legion1");
		var legion2 = scn.GetShadowCard("legion2");
		var legion3 = scn.GetShadowCard("legion3");
		scn.MoveCardsToSupportArea(oath, lying);
		assertInZone(Zone.SUPPORT, oath, lying);

		scn.StartGame();
		scn.MoveCardsToHand(legion1, legion2, legion3, freepsLegion1, freepsLegion2);
		assertInHand(legion1, legion2, legion3, freepsLegion1, freepsLegion2);
		scn.MoveMinionsToTable(bearer, ted, brigand);

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, oath, lying);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		int pool = scn.GetTwilight();

		// The two [men] minions spotted are a Man and a Hobbit; the [dunland] Man and the [men] condition beside
		// them make no third.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(legion1, legion2, legion3);
		assertEquals(pool + 8, scn.GetTwilight());
		// The Free Peoples hand is not touched.
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(freepsLegion1, freepsLegion2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithExactly2CardsInHandItIsOfferedAndDiscardsBothToAdd8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath = scn.GetShadowCard("oath");
		var bearer = scn.GetShadowCard("bearer");
		var ted = scn.GetShadowCard("ted");
		var legion1 = scn.GetShadowCard("legion1");
		var legion2 = scn.GetShadowCard("legion2");
		scn.MoveCardsToSupportArea(oath);
		assertInZone(Zone.SUPPORT, oath);

		scn.StartGame();
		scn.MoveCardsToHand(legion1, legion2);
		assertInHand(legion1, legion2);
		scn.MoveMinionsToTable(bearer, ted);

		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		int pool = scn.GetTwilight();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(legion1, legion2);
		assertEquals(pool + 8, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOnly1CardInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freepsLegion1 = scn.GetFreepsCard("legion1");
		var freepsLegion2 = scn.GetFreepsCard("legion2");
		var freepsLegion3 = scn.GetFreepsCard("legion3");
		var freepsLegion4 = scn.GetFreepsCard("legion4");

		var oath = scn.GetShadowCard("oath");
		var bearer = scn.GetShadowCard("bearer");
		var ted = scn.GetShadowCard("ted");
		var legion1 = scn.GetShadowCard("legion1");
		scn.MoveCardsToSupportArea(oath);
		assertInZone(Zone.SUPPORT, oath);

		scn.StartGame();
		scn.MoveCardsToHand(legion1, freepsLegion1, freepsLegion2, freepsLegion3, freepsLegion4);
		assertInHand(legion1, freepsLegion1, freepsLegion2, freepsLegion3, freepsLegion4);
		scn.MoveMinionsToTable(bearer, ted);

		// One card in the Shadow player's own hand; the four in the Free Peoples hand do not count for him.
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOnly1MenMinionToSpotItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath = scn.GetShadowCard("oath");
		var bearer = scn.GetShadowCard("bearer");
		var brigand = scn.GetShadowCard("brigand");
		var lying = scn.GetShadowCard("lying");
		var legion1 = scn.GetShadowCard("legion1");
		var legion2 = scn.GetShadowCard("legion2");
		var legion3 = scn.GetShadowCard("legion3");
		scn.MoveCardsToSupportArea(oath, lying);
		assertInZone(Zone.SUPPORT, oath, lying);

		scn.StartGame();
		scn.MoveCardsToHand(legion1, legion2, legion3);
		assertInHand(legion1, legion2, legion3);
		// One [men] minion, beside a minion of another culture and a [men] card that is no minion.
		scn.MoveMinionsToTable(bearer, brigand);

		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.SUPPORT, lying);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedItLeavesHandAndPoolAndIsNotOfferedAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath = scn.GetShadowCard("oath");
		var bearer = scn.GetShadowCard("bearer");
		var ted = scn.GetShadowCard("ted");
		var legion1 = scn.GetShadowCard("legion1");
		var legion2 = scn.GetShadowCard("legion2");
		var legion3 = scn.GetShadowCard("legion3");
		scn.MoveCardsToSupportArea(oath);
		assertInZone(Zone.SUPPORT, oath);

		scn.StartGame();
		scn.MoveCardsToHand(legion1, legion2, legion3);
		assertInHand(legion1, legion2, legion3);
		scn.MoveMinionsToTable(bearer, ted);

		assertEquals(3, scn.GetShadowHandCount());

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		int pool = scn.GetTwilight();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetShadowHandCount());
		assertInHand(legion1, legion2, legion3);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Into the archery phase, with the hand and the minions as they were.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(oath));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ItIsOfferedAgainAtTheManeuverPhaseOfTheOwnersNextTurnAsShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oath = scn.GetShadowCard("oath");
		var bearer = scn.GetShadowCard("bearer");
		var ted = scn.GetShadowCard("ted");
		var legion1 = scn.GetShadowCard("legion1");
		var legion2 = scn.GetShadowCard("legion2");
		var legion3 = scn.GetShadowCard("legion3");
		scn.MoveCardsToSupportArea(oath);
		assertInZone(Zone.SUPPORT, oath);

		scn.StartGame();
		scn.MoveCardsToHand(legion1, legion2, legion3);
		assertInHand(legion1, legion2, legion3);
		scn.MoveMinionsToTable(bearer, ted);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetShadowHandCount());

		// The minions are taken off before this turn's skirmishes, and laid again on turn 3.
		scn.MoveCardsToDiscard(bearer, ted);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.MoveMinionsToTable(bearer, ted);
		scn.MoveCardsToHand(legion1, legion2, legion3);
		assertInHand(legion1, legion2, legion3);

		int hand = scn.GetShadowHandCount();
		int discard = scn.GetShadowDiscardCount();
		assertTrue(hand >= 3);
		assertInZone(Zone.SUPPORT, oath);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		int pool = scn.GetTwilight();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(oath));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(discard + hand, scn.GetShadowDiscardCount());
		assertInDiscard(legion1, legion2, legion3);
		assertEquals(pool + 8, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
