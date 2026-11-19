package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "17_84");
					put("scout2", "17_84");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> Exert this minion to add (X), where X is the current site's Shadow number.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("scout");

		assertEquals("Orkish Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtBalinsTombTheShadowAbilityExertsThisScoutToAdd3AndThenItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");
		var scout2 = scn.GetShadowCard("scout2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(scout, scout2);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
		scn.FreepsPass();

		// Balin's Tomb: site 4, Shadow number 3.  Neither its site number (4) nor the Shadow number of the site
		// before (0) or after it (6) is 3.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilightCost(scn.GetCurrentSite()));
		int pool = scn.GetTwilight();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);

		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertEquals(pool + 3, scn.GetTwilight());
		// Vitality 2 with 1 wound: this Scout cannot exert again; the other copy still pays for its own.
		assertFalse(scn.ShadowActionAvailable(scout));
		assertTrue(scn.ShadowActionAvailable(scout2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheBridgeOfKhazadDumTheShadowAbilityAdds6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(scout);
		assertEquals(0, scn.GetWoundsOn(scout));
		scn.FreepsPass();

		// The Bridge of Khazad-dum: site 5, Shadow number 6.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetTwilightCost(scn.GetCurrentSite()));
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);

		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(pool + 6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
