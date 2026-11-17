package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_194_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurtz", "11_194");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a
					                            // maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4;
					                            // "Fellowship: Exert Sam to remove a burden"
					put("bounder", "1_286");    // Bounder: Hobbit ally, vitality 2; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Lurtz, Minion of the White Wizard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Archer</b>. <b>Damage +1</b>. <b>Muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>Lurtz is strength +3 for each exhausted companion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lurtz");

		assertEquals("Lurtz", card.getBlueprint().getTitle());
		assertEquals("Minion of the White Wizard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.MUSTER));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus3ForEachExhaustedCompanionAndNoOtherExhaustedCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Laid early, in the fellowship phase, to be read (Q-0228).
		scn.MoveMinionsToTable(lurtz, troop);
		assertEquals(13, scn.GetStrength(lurtz));

		// Frodo exhausted (3 wounds of 4); Aragorn wounded and not exhausted (2 of 4); an exhausted ally (1 of 2)
		// and an exhausted minion (2 of 3), which are no companions.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(2, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(1, scn.GetVitality(troop));
		assertEquals(16, scn.GetStrength(lurtz));   // 13 + 3 for Frodo
		assertEquals(13, scn.GetStrength(troop));

		// A second exhausted companion, of another culture and race.
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(19, scn.GetStrength(lurtz));   // 13 + 3 + 3
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void WithNoExhaustedCompanionHeIsAsPrintedAndACompanionExertingToExhaustionMakesHimPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AddWoundsToChar(sam, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		// Sam has 2 vitality left: wounded, not exhausted.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(13, scn.GetStrength(lurtz));

		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);

		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertEquals(16, scn.GetStrength(lurtz));   // 13 + 3 for Sam
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
