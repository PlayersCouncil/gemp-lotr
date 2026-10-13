package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "101_48");
					put("troop2", "101_48");
					put("soldier", "1_271");    // Orc Soldier: another [sauron] Orc, strength 7; a skirmish ability that is never used

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; "Maneuver: Exert Aragorn to make him defender +1"
					put("guard1", "1_7");       // Dwarf Guard: vitality 2, and nothing to do once in play
					put("guard2", "1_7");
					put("gaffer", "1_291");     // The Gaffer: an ally, vitality 2; a fellowship ability that is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcAmbushTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Orc Ambush Troop
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: Tracker.
		 * While you can spot an exhausted companion, this minion is an archer.
		 * While you can spot 3 exhausted companions, this minion is strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Orc Ambush Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsAnArcherWhileAnExhaustedCompanionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(gaffer);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2, soldier);
		// What is nearly an exhausted companion: a companion with wounds and 2 vitality left, an exhausted ally
		// and an exhausted minion (the second copy of the Troop itself).
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gaffer, 1);
		scn.AddWoundsToChar(troop2, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsExhausted(aragorn));
		assertTrue(scn.IsExhausted(gaffer));
		assertTrue(scn.IsExhausted(troop2));
		assertFalse(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop2, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(soldier, Keyword.ARCHER));
		assertEquals(8, scn.GetStrength(troop));

		// Aragorn exerts for his own ability and has 1 vitality left.
		scn.FreepsUseCardAction(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop2, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(soldier, Keyword.ARCHER));

		// With Aragorn no longer exhausted the keyword is gone, and an exhausted Ring-bearer brings it back.
		scn.RemoveWoundsFromChar(aragorn, 1);
		assertFalse(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop2, Keyword.ARCHER));

		scn.AddWoundsToChar(frodo, 3);
		assertTrue(scn.IsExhausted(frodo));
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop2, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(soldier, Keyword.ARCHER));
		// One exhausted companion is not three.
		assertEquals(8, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsStrengthPlusThreeWhileThreeExhaustedCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var gaffer = scn.GetFreepsCard("gaffer");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(aragorn, guard1, guard2);
		scn.MoveAlliesToTable(gaffer);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2, soldier);
		// Two exhausted companions, and beside them what is nearly a third: a companion with wounds and 2
		// vitality left, an exhausted ally and an exhausted minion.
		scn.AddWoundsToChar(guard1, 1);
		scn.AddWoundsToChar(guard2, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gaffer, 1);
		scn.AddWoundsToChar(troop2, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsExhausted(guard1));
		assertTrue(scn.IsExhausted(guard2));
		assertFalse(scn.IsExhausted(aragorn));
		assertTrue(scn.IsExhausted(gaffer));
		assertTrue(scn.IsExhausted(troop2));
		assertEquals(8, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(troop2));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));

		// Aragorn exerts for his own ability and is the third.
		scn.FreepsUseCardAction(aragorn);
		assertTrue(scn.IsExhausted(aragorn));
		// 8 + 3
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(11, scn.GetStrength(troop2));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));

		// A fourth adds nothing more.
		scn.AddWoundsToChar(frodo, 3);
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(11, scn.GetStrength(troop2));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
