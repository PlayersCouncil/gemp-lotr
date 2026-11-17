package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_317_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pack", "4_317");
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound by rule, not the Ring-bearer; abilities used only on purpose
					put("merry", "1_303");        // Merry, From O'er the Brandywine: an unbound Hobbit companion; +2 only with a weapon
					put("bounder", "1_286");      // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("smeagol", "103_116");    // Smeagol, Once Like Us: a Ring-bound [shire] companion who is no Hobbit
					put("boromir", "0_65");       // Boromir, Steward's Heir: a Ring-bound [gondor] Man, vitality 3; keywords only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamsPackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Sam's Pack
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Bearer must be a Ring-bound Hobbit.<br>At the start of each turn, you may add (2) to heal a Ring-bound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pack");

		assertEquals("Sam's Pack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARingBoundHobbitOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pack = scn.GetFreepsCard("pack");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, merry, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(pack);

		scn.StartGame();
		assertInHand(pack);
		assertTrue(scn.FreepsPlayAvailable(pack));

		scn.FreepsPlayCard(pack);
		// The Ring-bearer and Sam; not an unbound Hobbit, a Hobbit ally, nor a Ring-bound companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, bounder, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(pack, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheTurnItsOwnerMayAddTwoToHealARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pack = scn.GetFreepsCard("pack");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(sam, merry, boromir);
		scn.AttachCardsTo(frodo, pack);
		// Wounded: the Ring-bearer and a Ring-bound Man, and an unbound Hobbit; Sam, Ring-bound, has no wound.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(merry, 1);
		assertAttachedTo(pack, frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.StartGame();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pack));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir));
		// Sam has no wound to heal.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, sam));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRingBoundCompanionWoundedItIsStillOfferedAndTheTwilightIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pack = scn.GetFreepsCard("pack");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(frodo, pack);
		scn.AddWoundsToChar(merry, 1);
		assertAttachedTo(pack, frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));

		scn.StartGame();
		// It has a cost, so it is offered whatever it can heal (Q-0034).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pack));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNothingAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pack = scn.GetFreepsCard("pack");
		scn.AttachCardsTo(frodo, pack);
		scn.AddWoundsToChar(frodo, 2);
		assertAttachedTo(pack, frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pack));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertAttachedTo(pack, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheStartOfItsOwnersNextTurnAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pack = scn.GetFreepsCard("pack");
		scn.AttachCardsTo(frodo, pack);
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pack));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));

		// The other player's turn: this player's Free Peoples cards are not active.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());

		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pack));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
