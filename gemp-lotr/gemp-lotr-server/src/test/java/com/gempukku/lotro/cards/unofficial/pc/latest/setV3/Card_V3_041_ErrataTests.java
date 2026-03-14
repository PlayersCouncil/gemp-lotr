package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_041_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_41");
					put("southron1", "4_222");  // Desert Warrior - Southron Man
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("orc", "1_271");        // Orc Soldier - not Southron, to keep in play
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskChargerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Charger
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Support area
		 * Strength: 4
		 * Vitality: 4
		 * Game Text: Maneuver: Stack 1 or more Southron Men here to make this a <b>fierce</b> mounted Southron minion until the end of the turn that is strength +4 and <b>ambush (1)</b> for each Southron stacked here. At the start of each skirmish involving this minion, add a threat (or 2 threats if there are 4 Southrons stacked here).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Bladetusk Charger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
	}

	@Test
	public void BladetuskChargerStrengthMultiplierIs4PerStackedSouthron() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(card);
		scn.MoveMinionsToTable(southron1, southron2, southron3, orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Use the maneuver action - cost is stacking Southron Men from play
		scn.ShadowUseCardAction(card);

		// Stack all 3 Southron Men as cost
		scn.ShadowChooseCards(southron1, southron2, southron3);

		// Verify transformation
		assertInZone(Zone.SHADOW_CHARACTERS, card);
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));

		// Errata: multiplier is 4 (was 3 in original)
		// Base strength 4 + (3 Southrons * 4) = 16
		assertEquals(16, scn.GetStrength(card));

		// Ambush (3) for 3 stacked Southrons
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.AMBUSH));
	}
}
