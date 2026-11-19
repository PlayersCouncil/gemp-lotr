package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_11_164_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "11_164");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_7");        // Dwarf Guard and Axe Strike: the two cards in hand
					put("strike", "1_3");
					put("armor", "1_8");        // Dwarven Armor: the top card of the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				"11_164",                       // Frodo, Protected by Many, as the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFrodoScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: another Hobbit companion; his abilities are used only on purpose
					put("guard", "1_7");        // Dwarf Guard: a card in hand, so that muster would have something to discard
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				"11_164",                       // Frodo, Protected by Many, as the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPhialScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "13_155");     // Phial of Galadriel: bearer, a Ring-bound Hobbit, has muster in region 1
					put("guard", "1_7");        // Dwarf Guard: a card in hand, so that muster would have something to discard
				}},
				VirtualTableScenario.FellowshipSites,
				"11_164",                       // Frodo, Protected by Many, as the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Frodo, Protected by Many
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Game Text: <b>Ring-bearer</b>. <b>Ring-bound</b>.
		 * At the start of the regroup phase, you may exert Frodo to make him gain <b>muster</b> until the end of the regroup phase. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("frodo");

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Protected by Many", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BEARER));
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(card.getBlueprint().canStartWithRing());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
	}

	// CR 4.2 "muster" acts "at the start of the regroup phase", which is when Frodo gains it: the keyword he has
	// just gained must still be offered in that phase, or his text does nothing.
	@Test
	public void MusterGainedAtTheStartOfRegroupIsUsedInThatSameRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var strike = scn.GetFreepsCard("strike");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(guard, strike);
		assertInHand(guard, strike);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		assertInZone(Zone.DECK, armor);
		// No minion is in play: nothing is done between the fellowship phase and the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		// Frodo's own offer, and none of muster yet: he has not the keyword.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo, "Use Muster"));
		scn.FreepsAcceptOptionalTrigger();
		// One muster character: no card or one.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, strike));

		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.HAND, strike, armor);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyFrodoGainsMusterAndHeKeepsItThroughTheRegroupPhaseButNotAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(guard);
		assertInHand(guard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(sam, Keyword.MUSTER));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();

		// Frodo exerts and gains muster; Sam, the other Hobbit companion, does not.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(sam, Keyword.MUSTER));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo, "Use Muster"));
		scn.FreepsDeclineOptionalTrigger();
		assertInHand(guard);

		// Still his at the regroup actions, the last decisions of the phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));

		// The regroup move ends the regroup phase: in the Shadow phase at site 3 he has muster no more (a gain
		// lasting the turn would still be there).
		scn.SkipToMovementDecision();
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesFrodoUnexertedAndWithoutMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(guard);
		assertInHand(guard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		// With a card in hand, muster would be offered if he had it.
		assertInHand(guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileFrodoIsExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(guard);
		assertInHand(guard);

		scn.StartGame();
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
	}

	@Test
	public void NothingIsOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();

		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// The phases are passed by hand: a skip would decline an offer made on the way.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void TheOfferIsMadeAgainAtTheStartOfHisNextRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The other player's turn, whose own Frodo (both decks hold one) makes him the same offer: declined. Then
		// the first player's next turn.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToOtherPlayersTurn();
		assertEquals(VirtualTableScenario.P1, scn.GetCurrentPlayer());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		// The hand drawn on the way gives muster something to discard.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo, "Use Muster"));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAndTheExertionPaidWhenFrodoHasMusterAlready() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPhialScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var guard = scn.GetFreepsCard("guard");
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToHand(guard);
		assertAttachedTo(phial, frodo);
		assertInHand(guard);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Site 2 is in region 1, where the Phial gives its bearer muster: Frodo has the keyword before his text acts.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsChooseOptionalTrigger(frodo, "Optional trigger");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.MUSTER));
		// His muster, held from the Phial, is then the other thing offered at the start of the phase.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo, "Use Muster"));
		scn.FreepsDeclineOptionalTrigger();
		assertInHand(guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
