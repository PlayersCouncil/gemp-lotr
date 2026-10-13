package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "6_109");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFrodoRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "6_109");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges as the Ring-bearer, beside the one Frodo who can stand there (Q-0212)
	protected VirtualTableScenario GetOtherRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("held", "6_109");
					put("fighter", "13_149");   // Frodo, Frenzied Fighter: "This version of Frodo cannot become the Ring-bearer"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Held
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be Frodo.<br>Each time Frodo is about to be killed by a wound, add a burden instead.<br>When the fellowship moves to site 9, Frodo is corrupted.<br><b>Regroup:</b> If you can spot no minions, discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Held", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BorneByAFrodoWhoIsTheRingBearerTheMoveToSite9CorruptsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoRingBearerScenario();

		var frodo = scn.GetRingBearer();
		var held = scn.GetFreepsCard("held");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.AttachCardsTo(frodo, held);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MarkGameLog();
		scn.FreepsPass();

		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
	}

	@Test
	public void BorneByAFrodoWhoIsNotTheRingBearerTheMoveToSite9CorruptsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherRingBearerScenario();

		var held = scn.GetFreepsCard("held");
		var fighter = scn.GetFreepsCard("fighter");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(fighter);
		scn.AttachCardsTo(fighter, held);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MarkGameLog();
		scn.FreepsPass();

		// "Frodo is corrupted": the Frodo who bears this is no Ring-bearer, and the Ring-bearer is no Frodo.
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());
		assertFalse(scn.GameLogSinceMarkSays("lost due to"));
		assertAttachedTo(held, fighter);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
