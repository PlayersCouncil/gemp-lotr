package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizen", "11_117");
					put("denizen2", "11_117");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, the other minion at the same sites
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizen", "11_117");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				// Site 3 of the King block's path is The Dimholt 8_117: underground and a sanctuary.
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenizenoftheBlackPitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Denizen of the Black Pit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While this minion is at an underground site, it is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denizen");

		assertEquals("Denizen of the Black Pit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2AtBalinsTombAndNotAtRivendellValleyEachCopyForItselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(denizen, denizen2, troop);

		// Rivendell Valley is no underground site: the printed numbers.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(denizen2));
		assertEquals(13, scn.GetStrength(troop));

		// The move to Balin's Tomb, underground.
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		// 10 + 2, each copy for itself: a second copy adds nothing to the first, and the Troop is untouched.
		assertEquals(12, scn.GetStrength(denizen));
		assertEquals(12, scn.GetStrength(denizen2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AtTheDimholtAnUndergroundSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(denizen, troop);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(denizen));
		assertEquals(13, scn.GetStrength(troop));

		// The move to The Dimholt.
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(12, scn.GetStrength(denizen));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
