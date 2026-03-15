package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breeder", "18_87");
					put("ugluk", "15_172");
					put("sentry", "15_170");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishBreederStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Breeder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>
		 * 	<br><b>Shadow:</b> Exert this minion to play an [uruk-hai] hunter at twilight cost -2.
		*/

		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");

		assertEquals("Orkish Breeder", breeder.getBlueprint().getTitle());
		assertNull(breeder.getBlueprint().getSubtitle());
		assertFalse(breeder.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, breeder.getBlueprint().getSide());
		assertEquals(Culture.ORC, breeder.getBlueprint().getCulture());
		assertEquals(CardType.MINION, breeder.getBlueprint().getCardType());
		assertEquals(Race.ORC, breeder.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(breeder, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(breeder, Keyword.HUNTER));
		assertEquals(4, breeder.getBlueprint().getTwilightCost());
		assertEquals(9, breeder.getBlueprint().getStrength());
		assertEquals(3, breeder.getBlueprint().getVitality());
		assertEquals(4, breeder.getBlueprint().getSiteNumber());
	}

	@Test
	public void OrkishBreederAbilityPlaysUrukHunterWithDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");
		var ugluk = scn.GetShadowCard("ugluk");

		scn.MoveMinionsToTable(breeder);
		scn.MoveCardsToHand(ugluk);

		scn.StartGame();
		scn.SetTwilight(6);
		scn.SkipToPhase(Phase.SHADOW);

		int twilightBefore = scn.GetTwilight();

		// Use Orkish Breeder's ability
		assertTrue(scn.ShadowActionAvailable(breeder));
		scn.ShadowUseCardAction(breeder);

		// Ugluk costs 4 base + 2 roaming (home site 5, current site 1) - 2 discount = 4
		assertEquals(twilightBefore - 4, scn.GetTwilight());
	}

	@Test
	public void OrkishBreederAbilityAvailableWithExactDiscountedTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// Bug: ability not available when pool has exactly enough for discounted cost
		// Ugluk costs 4, discount -2 = 2 needed. With exactly 2 twilight, should work.
		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");
		var ugluk = scn.GetShadowCard("ugluk");

		scn.MoveMinionsToTable(breeder);
		scn.MoveCardsToHand(ugluk);

		scn.StartGame();
		// Ugluk costs 4 base + 2 roaming (home site 5, current site 1) - 2 discount = 4
		scn.SetTwilight(1);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(4, scn.GetTwilight());

		// Ability SHOULD be available — discounted cost of Ugluk is 4
		assertTrue(scn.ShadowActionAvailable(breeder));
		scn.ShadowUseCardAction(breeder);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void OrkishBreederAbilityNotAvailableWithInsufficientTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");
		var ugluk = scn.GetShadowCard("ugluk");

		scn.MoveMinionsToTable(breeder);
		scn.MoveCardsToHand(ugluk);

		scn.StartGame();
		// Ugluk costs 4 base + 2 roaming (home site 5, current site 1) - 2 discount = 4
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(3, scn.GetTwilight());

		// Discounted cost is 4, but only 3 twilight available
		assertFalse(scn.ShadowActionAvailable(breeder));
	}
}
