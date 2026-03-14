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
					put("guard", "1_7");       // Dwarf Guard (companion, STR 4, VIT 2)
					put("runner", "1_178");    // Goblin Runner
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
		 * Game Text: <b>Damage +1</b>.<br><b>Maneuver:</b> Spot 5 burdens or 5 [isengard] cards
		 *  and exert Orthanc Berserker twice to exhaust a companion (except the Ring-bearer).
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
	public void OrthancBerserkerManeuverAbilityExhaustsCompanionWith5Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveMinionsToTable(berserker);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.AddBurdens(5);

		scn.SkipToPhase(Phase.MANEUVER);

		// Guard starts unwounded (VIT 2)
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(berserker));

		// Shadow uses Orthanc Berserker's maneuver ability
		assertTrue(scn.ShadowActionAvailable(berserker));
		scn.ShadowUseCardAction(berserker);

		// Exert twice is auto-applied as cost
		// Guard (only non-Ring-bearer companion) is auto-selected to exhaust

		// Berserker should have 2 wounds (exerted twice, VIT 3 -> 1 remaining)
		assertEquals(2, scn.GetWoundsOn(berserker));
		// Guard should be exhausted (VIT 2, so 1 wound = exhausted)
		assertTrue(scn.IsExhausted(guard));
	}
}
