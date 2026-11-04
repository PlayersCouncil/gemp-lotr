package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_188_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk1", "11_188");
					put("uruk2", "11_188");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; Maneuver: exert Aragorn
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: another Uruk-hai minion, strength 5; damage +1 and no other text
					put("runner", "1_178");     // Goblin Runner: a minion of vitality 1, so always exhausted; its play trigger is not reached by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HoundingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Hounding Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you can spot an exhausted companion, this minion is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruk1");

		assertEquals("Hounding Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}


	@Test
	public void IsStrength3MoreOnceACompanionIsExhaustedAndNotForAnExhaustedAllyOrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2, savage, runner);

		// An exhausted ally (vitality 2, one wound) and an exhausted minion (vitality 1); Aragorn has 2 vitality left.
		assertEquals(2, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(1, scn.GetVitality(runner));
		assertEquals(5, scn.GetStrength(uruk1));
		assertEquals(5, scn.GetStrength(uruk2));
		assertEquals(5, scn.GetStrength(savage));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);

		// Aragorn exerted to exhaustion: each copy is 5 + 3, the bonuses not stacking; the other Uruk-hai is untouched.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(uruk1));
		assertEquals(8, scn.GetStrength(uruk2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedHobbitCompanionIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk1 = scn.GetShadowCard("uruk1");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1);
		assertEquals(5, scn.GetStrength(uruk1));

		scn.AddWoundsToChar(merry, 3);
		assertEquals(1, scn.GetVitality(merry));
		assertEquals(8, scn.GetStrength(uruk1));
	}
}
