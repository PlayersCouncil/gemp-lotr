package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_312_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sorry", "1_312");
					// Pippin, Trollslayer: a Hobbit companion who is [gondor]; his skirmish ability is used only on purpose
					put("pippin", "103_31");
					// Bounder: [shire] Hobbit ally, vitality 2; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; his assignment ability is used only on purpose
					put("smeagol", "103_116");
					// Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: [shire] Hobbit, vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SorryAboutEverythingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sorry About Everything
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert a Hobbit companion to remove a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sorry");

		assertEquals("Sorry About Everything", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAChosenHobbitCompanionToRemoveOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sorry = scn.GetFreepsCard("sorry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sorry);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsPlayAvailable(sorry));

		scn.FreepsPlayCard(sorry);
		// Hobbit companions of either culture; not a Hobbit ally, nor a [shire] companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		// One burden of the two.
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, sorry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBurdenItIsStillPlayedAndTheHobbitExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sorry = scn.GetFreepsCard("sorry");
		scn.MoveCardsToHand(sorry);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The burden is the effect's target, no requirement: the cost can be paid, so the event can be played.
		assertTrue(scn.FreepsPlayAvailable(sorry));

		scn.FreepsPlayCard(sorry);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, sorry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyHobbitCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sorry = scn.GetFreepsCard("sorry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sorry);
		// Vitality 4 with 3 wounds: Frodo is exhausted; the others could exert, but none is a Hobbit companion.
		scn.AddWoundsToChar(frodo, 3);

		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.StartGame();
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertInHand(sorry);
		assertFalse(scn.FreepsPlayAvailable(sorry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
