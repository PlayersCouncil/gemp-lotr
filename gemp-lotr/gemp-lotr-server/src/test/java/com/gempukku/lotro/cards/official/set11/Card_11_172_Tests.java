package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "11_172");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6; a maneuver ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally (resistance 0); a fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; its play trigger is not set off by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Sam, Steadfast Friend
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Game Text: <b>Ring-bound</b>.
		 * Each other companion is resistance +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Steadfast Friend", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
	}

	@Test
	public void EachOtherCompanionIsResistancePlus1AndNeitherSamNorAnAllyNorAMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		// The minion is laid early, to stand on the table beside the fellowship as Sam comes in.
		scn.MoveMinionsToTable(runner);

		// Without Sam: printed resistance.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(barliman));
		assertEquals(0, scn.GetResistance(runner));

		scn.MoveCompanionsToTable(sam);

		// A [shire] Hobbit and a [gondor] Man, each +1; Sam himself, the ally and the minion are not.
		assertEquals(11, scn.GetResistance(frodo));
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(10, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(barliman));
		assertEquals(0, scn.GetResistance(runner));
	}
}
