package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_066_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "53_66");
					put("aragorn", "1_89");    // Aragorn, Ranger of the North (companion, VIT 4)
					put("guard", "1_7");       // Dwarf Guard (companion, VIT 2)
					put("runner", "1_178");    // Goblin Runner
					put("demands1", "2_40");   // Demands of the Sackville-Bagginses (Isengard SA condition)
					put("demands2", "2_40");
					put("demands3", "2_40");
					put("demands4", "2_40");
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
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Maneuver:</b> Spot 4 burdens and exert Orthanc Berserker twice
		 *  to exhaust a companion (except the Ring-bearer).
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
	public void ManeuverAbilityNotAvailableWith3Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveMinionsToTable(berserker);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.AddBurdens(3 - scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(berserker));
	}

	@Test
	public void IsengardCardsNoLongerSubstituteForBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		// The previous errata also allowed spotting 5 [isengard] cards; this revision reverts to burdens only.
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var guard = scn.GetFreepsCard("guard");

		// 5 Isengard cards: berserker + 4 demands
		var demands1 = scn.GetShadowCard("demands1");
		var demands2 = scn.GetShadowCard("demands2");
		var demands3 = scn.GetShadowCard("demands3");
		var demands4 = scn.GetShadowCard("demands4");

		scn.MoveMinionsToTable(berserker);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(demands1, demands2, demands3, demands4);

		scn.StartGame();
		scn.AddBurdens(3 - scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(berserker));
	}

	@Test
	public void ManeuverAbilityExhaustsNonRingBearerCompanionWith4Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();

		scn.MoveMinionsToTable(berserker);
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.AddBurdens(4 - scn.GetBurdens());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertTrue(scn.ShadowActionAvailable(berserker));
		scn.ShadowUseCardAction(berserker);

		// Should be prompted to choose a companion to exhaust (not Frodo)
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard));
		assertFalse(scn.ShadowHasCardChoiceAvailable(frodo));

		// Choose Aragorn (VIT 4); exhausting means VIT-1 = 3 wounds
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(berserker));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(aragorn));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
