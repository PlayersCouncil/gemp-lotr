package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "11_33");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, strength 1; a fellowship ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, strength 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gandalf, Leader of the Company
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: While Gandalf is in region 1, each other companion is strength +2.
		 * 	While Gandalf is in region 2, each companion is strength +1.
		 * 	While Gandalf is in region 3, he is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Leader of the Company", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void AtSite3EachOtherCompanionIsStrengthPlus2AndAtSite4EachCompanionIsPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(barliman);

		// Site 3, the last site of region 1: each companion but Gandalf is +2, and the ally is not.
		assertEquals(3, scn.GetCurrentSiteNumber());
		// Frodo 3 + 1 for The Ruling Ring + 2.
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetStrength(barliman));

		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);

		// Site 4, the first of region 2: each companion, Gandalf too, is +1 and no longer +2; the ally and the
		// minion are not.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetStrength(barliman));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite6EachCompanionIsStrengthPlus1AndAtSite7OnlyGandalfIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		// Site 6, the last site of region 2: each companion is +1, Gandalf with no +2 of his own.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(gandalf));

		scn.FreepsPass();
		// Anduin Confluence discards every ally as the fellowship moves to it: the ally and the minion come in after.
		scn.MoveAlliesToTable(barliman);
		scn.MoveMinionsToTable(runner);

		// Site 7, the first of region 3: Gandalf alone is +2.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetStrength(barliman));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
