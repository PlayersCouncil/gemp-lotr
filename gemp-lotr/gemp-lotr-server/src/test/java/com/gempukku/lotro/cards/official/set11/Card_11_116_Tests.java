package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizen", "11_116");
					put("denizen2", "11_116");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its (2) only when played
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
					put("denizen", "11_116");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its (2) only when played
				}},
				VirtualTableScenario.KingSites,   // site 3, The Dimholt 8_117: underground and a sanctuary
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenizenofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Denizen of Moria
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: While this minion is at an underground site, it is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denizen");

		assertEquals("Denizen of Moria", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAnUndergroundSiteEachDenizenIsStrengthPlus3AndNoOtherOrcIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		// Rivendell Valley, site 3, is no underground site.
		scn.MoveMinionsToTable(denizen, denizen2, runner);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(denizen));
		assertEquals(7, scn.GetStrength(denizen2));
		assertEquals(5, scn.GetStrength(runner));

		// The fellowship moves to Balin's Tomb, site 4, an underground site.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// Each copy +3 for itself, and no more; the other Orc as printed.
		assertEquals(10, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(denizen2));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAnUndergroundSanctuaryTheDenizenIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var denizen = scn.GetShadowCard("denizen");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		// King's Tent, site 2, is no underground site.
		scn.MoveMinionsToTable(denizen, runner);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(denizen));
		assertEquals(5, scn.GetStrength(runner));

		// The fellowship moves to The Dimholt, site 3: underground, and a sanctuary.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(denizen));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
