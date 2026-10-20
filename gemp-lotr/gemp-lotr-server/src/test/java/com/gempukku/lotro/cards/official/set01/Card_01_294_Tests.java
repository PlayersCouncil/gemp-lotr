package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_294_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("appetite", "1_294");
					put("appetite2", "1_294");
					// Sam, Son of Hamfast: Hobbit companion, vitality 4; his fellowship ability is never used
					put("sam", "1_311");
					// Pippin, Trollslayer: a Hobbit companion who is a [gondor] card, vitality 4; a skirmish ability
					put("trollslayer", "103_31");
					put("bounder", "1_286");       // Bounder: [shire] Hobbit ally, vitality 2; a skirmish ability
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used here
					put("smeagol", "103_116");

					put("runner", "1_178");        // Goblin Runner: [moria] Orc; "when played" only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitAppetiteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Hobbit Appetite
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Add (X) to heal a Hobbit companion X times.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("appetite");

		assertEquals("Hobbit Appetite", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsXToHealOneHobbitCompanionXTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var appetite = scn.GetFreepsCard("appetite");
		var appetite2 = scn.GetFreepsCard("appetite2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, trollslayer, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(appetite, appetite2);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(trollslayer, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(smeagol, 1);

		scn.StartGame();

		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(trollslayer));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(appetite));

		scn.FreepsPlayCard(appetite);
		// X has no cap: adding twilight can always be paid (shapes.md, "What bounds an X").
		assertEquals(Integer.MAX_VALUE, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(2, scn.GetTwilight());

		// The Ring-bearer, a Hobbit companion of [shire] and one of [gondor]; not a Hobbit ally, nor a [shire]
		// companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, trollslayer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sam);

		// Two of Sam's three wounds; nobody else is healed.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(trollslayer));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertInDiscard(appetite);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// X of 1: one of Pippin's two wounds.
		scn.FreepsPlayCard(appetite2);
		scn.FreepsChoose("1");
		assertEquals(3, scn.GetTwilight());
		scn.FreepsChooseCard(trollslayer);
		assertEquals(1, scn.GetWoundsOn(trollslayer));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(appetite2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHobbitCompanionWoundedItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var appetite = scn.GetFreepsCard("appetite");
		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(appetite);
		// Wounded, but neither is a Hobbit companion.
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(smeagol, 1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetTwilight());
		// An event's target is not a requirement: it is played for nothing (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(appetite));

		scn.FreepsPlayCard(appetite);
		scn.FreepsChoose("1");

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertInDiscard(appetite);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var appetite = scn.GetFreepsCard("appetite");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(appetite);
		scn.AddWoundsToChar(sam, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertInHand(appetite);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(appetite));
	}
}
