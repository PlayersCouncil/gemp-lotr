package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_152_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shaman", "51_152");
					put("uruk", "1_151");  // Uruk Lieutenant (Isengard Uruk-hai)
					put("orc", "1_189");   // Goblin Sneak (Moria Goblin, not Isengard)
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukShamanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Shaman
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Maneuver:</b> Remove (2) to heal an [Isengard] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shaman");

		assertEquals("Uruk Shaman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukShamanHealsIsengardMinionNotJustUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shaman = scn.GetShadowCard("shaman");
		var uruk = scn.GetShadowCard("uruk");
		var orc = scn.GetShadowCard("orc");

		scn.MoveMinionsToTable(shaman, uruk, orc);
		scn.AddWoundsToChar(uruk, 1);
		scn.AddWoundsToChar(orc, 1);

		scn.StartGame();

		// Add twilight for the cost
		scn.AddTwilight(4);

		scn.SkipToPhase(Phase.MANEUVER);

		// Shaman ability should be available
		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);

		// Only the Uruk Lieutenant is an [Isengard] minion. The Goblin Sneak is [Moria].
		// Auto-selected since there's only one valid target.
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(orc));
	}
}
