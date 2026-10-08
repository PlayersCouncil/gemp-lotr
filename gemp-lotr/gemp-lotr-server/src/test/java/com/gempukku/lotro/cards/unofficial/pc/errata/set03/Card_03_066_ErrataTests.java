package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_066_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "53_66");
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: companion, vitality 4
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer, vitality 4
					put("guard", "1_7");       // Dwarf Guard: companion, vitality 2
					put("silinde", "1_60");    // Silinde, Elf of Mirkwood: ally, vitality 2
					put("troop", "1_177");     // Goblin Patrol Troop: minion, no text
					put("demands1", "2_40");   // Demands of the Sackville-Bagginses: [isengard] condition, support area
					put("demands2", "2_40");
					put("demands3", "2_40");
					put("demands4", "2_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Sam, Bearer of Great Need as the Ring-bearer (resistance 5, 6 with a Hobbit spotted, so 4 burdens do not
	// corrupt him), and Frodo, Son of Drogo a companion who is not.
	protected VirtualTableScenario GetSamRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "53_66");
					put("frodo", "1_290");     // Frodo, Son of Drogo: cheated in as an ordinary companion, vitality 4
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: companion, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrthancBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orthanc Berserker
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Maneuver:</b> Spot 4 burdens and exert Orthanc Berserker twice to exhaust a companion (except the
		 * 	Ring-bearer).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("berserker");

		assertEquals("Orthanc Berserker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandAndAbilityNotOfferedInTheShadowPhaseButInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(berserker);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(4 - scn.GetBurdens());
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 2 companions + 2 for Ettenmoors
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(berserker));
		scn.ShadowPlayCard(berserker);
		// 5 to play + 2 roaming
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);

		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertFalse(scn.ShadowActionAvailable(berserker));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(berserker));
	}

	@Test
	public void ExertsTwiceToExhaustACompanionOtherThanTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var silinde = scn.GetFreepsCard("silinde");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam, guard);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, troop);
		scn.AddBurdens(4 - scn.GetBurdens());
		// Vitality 2 with 1 wound: already exhausted.
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowActionAvailable(berserker));

		scn.ShadowUseCardAction(berserker);
		assertEquals(2, scn.GetWoundsOn(berserker));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, guard, silinde, troop, berserker));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);
		// Vitality 4: exhausted at 3 wounds.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExhaustsAFrodoWhoIsNotTheRingBearerButNeverTheRingBearerWhoeverThatIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamRingBearerScenario();

		var berserker = scn.GetShadowCard("berserker");
		var frodo = scn.GetFreepsCard("frodo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var samRB = scn.GetRingBearer();
		scn.MoveCompanionsToTable(frodo, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		scn.AddBurdens(4 - scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(samRB));
		assertTrue(scn.ShadowActionAvailable(berserker));

		scn.ShadowUseCardAction(berserker);
		assertEquals(2, scn.GetWoundsOn(berserker));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(samRB));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(frodo);
		// Vitality 4: exhausted at 3 wounds.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(samRB));
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWith3BurdensBesideAWoundOnTheRingBearerAndFiveIsengardCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var guard = scn.GetFreepsCard("guard");
		var demands1 = scn.GetShadowCard("demands1");
		var demands2 = scn.GetShadowCard("demands2");
		var demands3 = scn.GetShadowCard("demands3");
		var demands4 = scn.GetShadowCard("demands4");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		// Nearly counting: a wound on the Ring-bearer beside the burdens, and five [isengard] cards in play
		// (the berserker and four conditions), which the previous revision accepted in place of burdens.
		scn.MoveCardsToSupportArea(demands1, demands2, demands3, demands4);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddBurdens(3 - scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserker));
	}

	@Test
	public void NotOfferedWhenTheBerserkerCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		// Vitality 3 with 1 wound: one exertion left in him, not two.
		scn.AddWoundsToChar(berserker, 1);
		scn.AddBurdens(4 - scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(berserker));
		assertFalse(scn.IsExhausted(berserker));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserker));
	}
}
