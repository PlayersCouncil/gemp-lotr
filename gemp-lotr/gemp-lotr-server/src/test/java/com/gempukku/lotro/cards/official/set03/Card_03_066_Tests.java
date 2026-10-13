package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_066_Tests
{

	// Frodo, Son of Drogo is the Ring-bearer: resistance 10, so 5 burdens do not corrupt him.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "3_66");
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: companion, vitality 4
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer, vitality 4
					put("guard", "1_7");       // Dwarf Guard: companion, vitality 2
					put("silinde", "1_60");    // Silinde, Elf of Mirkwood: ally, vitality 2
					put("troop", "1_177");     // Goblin Patrol Troop: minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrthancBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orthanc Berserker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Maneuver:</b> Spot 5 burdens and exert Orthanc Berserker twice to exhaust a companion (except the
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
		scn.AddBurdens(5 - scn.GetBurdens());
		// Vitality 2 with 1 wound: already exhausted.
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(5, scn.GetBurdens());
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
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWith4BurdensBesideAWoundOnTheRingBearerAndAThreatButOfferedWithAFifthBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		// Nearly counting: a wound on the Ring-bearer and a threat beside the burdens.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddThreats(1);
		scn.AddBurdens(4 - scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserker));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();

		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(berserker));
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
		scn.AddBurdens(5 - scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(berserker));
		assertFalse(scn.IsExhausted(berserker));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserker));
	}

	@Test
	public void AbilityNotOfferedInTheShadowPhaseButInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		scn.AddBurdens(5 - scn.GetBurdens());
		scn.FreepsPass();

		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserker));

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(berserker));
	}
}
