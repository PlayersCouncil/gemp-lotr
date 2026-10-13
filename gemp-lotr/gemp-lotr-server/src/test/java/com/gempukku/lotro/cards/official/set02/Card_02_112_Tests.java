package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("promise", "2_112");
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; "Fellowship: Exert Sam to remove a burden"
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSamRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("promise", "2_112");
					// Frodo, Frenzied Fighter: the one Frodo who can be in play beside another Ring-bearer.
					// Strength 3, vitality 4; his texts are about overwhelming and a skirmish ability.
					put("frodo", "13_149");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,     // Sam, Bearer of Great Need: strength 3, Ring-bearer, no Frodo beside him
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void APromiseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: A Promise
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>While you can spot Frodo and Sam and neither is exhausted, each is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("promise");

		assertEquals("A Promise", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideFrodoAndSamEachIsStrengthPlus1UntilSamExertsHimselfToExhaustion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var promise = scn.GetFreepsCard("promise");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(promise);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);
		scn.AddBurdens(1);

		// Frodo is 3 + 1 for The Ruling Ring; Sam and Merry are 3.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.IsExhausted(sam));

		scn.FreepsPlayCard(promise);
		assertInZone(Zone.SUPPORT, promise);
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));
		// Merry, another [shire] Hobbit companion, is neither Frodo nor Sam.
		assertEquals(3, scn.GetStrength(merry));

		// Sam has 4 vitality: his third wound exhausts him, and neither is strength +1 any longer.
		scn.FreepsUseCardAction(sam);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.IsExhausted(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFrodoExhaustedNeitherIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var promise = scn.GetFreepsCard("promise");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(promise);

		scn.StartGame();

		// Frodo has 4 vitality: with 2 wounds he is not exhausted yet.
		scn.AddWoundsToChar(frodo, 2);
		assertFalse(scn.IsExhausted(frodo));
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));

		scn.AddWoundsToChar(frodo, 1);
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoSamFrodoIsNotStrengthPlus1UntilSamIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var promise = scn.GetFreepsCard("promise");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(promise);
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFrodoSamIsNotStrengthPlus1UntilAFrodoIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamRingBearerScenario();

		var promise = scn.GetFreepsCard("promise");
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(promise);
		scn.MoveCardsToHand(frodo);

		scn.StartGame();

		// Sam is 3 + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(sam));

		// A Frodo who is not the Ring-bearer is Frodo all the same.
		scn.FreepsPlayCard(frodo);
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
