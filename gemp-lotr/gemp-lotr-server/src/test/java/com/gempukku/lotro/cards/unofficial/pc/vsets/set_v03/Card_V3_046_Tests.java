package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_046_Tests
{

// ----------------------------------------
// CORSAIR HELMSMAN TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helmsman", "103_46");    // Corsair Helmsman
					put("ships", "8_65");         // Ships of Great Draught - [Raider] possession
					put("southron", "4_222");     // Desert Warrior - [Raider] minion
					put("battlelines", "103_58"); // Shifting Battle-lines - [Raider] condition
					put("orc", "1_271");          // Orc Soldier - [Sauron], not Raider

					put("gandalf", "1_364");
					put("enterhere", "103_9");    // You Cannot Enter Here - hinders Shadow card
					put("thunder", "4_99");       // Roll of Thunder - discards Shadow possession/artifact
					put("sleep", "51_84");        // Sleep, Caradhras - discards Shadow condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairHelmsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Corsair Helmsman
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair.</b>
		* 	Response: If your [raider] card is about to be hindered or discarded by a card effect, exert or hinder this minion to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helmsman");

		assertEquals("Corsair Helmsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}



	@Test
	public void CorsairHelmsmanCanExertOrHinderSelfToPreventEffect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman");
		var ships = scn.GetShadowCard("ships");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thunder = scn.GetFreepsCard("thunder");
		scn.MoveMinionsToTable(helmsman);
		scn.MoveCardsToSupportArea(ships);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(helmsman));
		int helmsmanWoundsBefore = scn.GetWoundsOn(helmsman);

		// Freeps plays Roll of Thunder targeting Ships
		scn.FreepsPlayCard(thunder);

		// Shadow should have response available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Should be offered choice between exert and hinder
		assertTrue(scn.ShadowChoiceAvailable("Exert"));
		scn.ShadowChoose("Exert");

		// Ships auto-selected as only Raider being affected
		assertEquals(helmsmanWoundsBefore + 1, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(helmsman));
		assertInZone(Zone.SUPPORT, ships); // Protected from discard
	}

	@Test
	public void CorsairHelmsmanCanHinderSelfAsAlternativeCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman");
		var ships = scn.GetShadowCard("ships");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thunder = scn.GetFreepsCard("thunder");
		scn.MoveMinionsToTable(helmsman);
		scn.MoveCardsToSupportArea(ships);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(helmsman));

		scn.FreepsPlayCard(thunder);

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("Hinder");

		assertTrue(scn.IsHindered(helmsman));
		assertInZone(Zone.SUPPORT, ships); // Protected from discard
	}

	@Test
	public void CorsairHelmsmanProtectsRaiderMinionFromHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman");
		var southron = scn.GetShadowCard("southron");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enterhere = scn.GetFreepsCard("enterhere");
		scn.MoveMinionsToTable(helmsman, southron);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enterhere);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(southron));

		// Freeps plays You Cannot Enter Here targeting Southron
		scn.FreepsPlayCard(enterhere);
		scn.FreepsChooseCard(southron);

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("Exert");

		assertFalse(scn.IsHindered(southron)); // Protected
	}

	@Test
	public void CorsairHelmsmanProtectsRaiderConditionFromHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman");
		var battlelines = scn.GetShadowCard("battlelines");
		var ships = scn.GetShadowCard("ships"); // Needed for Sleep's discard target
		var gandalf = scn.GetFreepsCard("gandalf");
		var enterhere = scn.GetFreepsCard("enterhere");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveMinionsToTable(helmsman);
		scn.MoveCardsToSupportArea(battlelines, ships);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enterhere, sleep);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Test hinder protection
		scn.FreepsPlayCard(enterhere);
		scn.FreepsChooseCard(battlelines);

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("Exert");

		assertFalse(scn.IsHindered(battlelines));
		assertInZone(Zone.SUPPORT, battlelines);
	}

	@Test
	public void CorsairHelmsmanProtectsRaiderConditionFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman");
		var battlelines = scn.GetShadowCard("battlelines");
		var ships = scn.GetShadowCard("ships"); // Needed for Sleep's discard target
		var gandalf = scn.GetFreepsCard("gandalf");
		var enterhere = scn.GetFreepsCard("enterhere");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveMinionsToTable(helmsman);
		scn.MoveCardsToSupportArea(battlelines, ships);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enterhere, sleep);

		scn.StartGame();

		// Test discard protection with Sleep, Caradhras
		// Sleep discards a Shadow condition then hinders all conditions
		scn.FreepsPlayCard(sleep);

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("Exert");

		assertInZone(Zone.SUPPORT, battlelines); // Protected from discard
	}

	@Test
	public void CorsairHelmsmanDoesNotProtectNonRaiderCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman");
		var orc = scn.GetShadowCard("orc");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enterhere = scn.GetFreepsCard("enterhere");
		scn.MoveMinionsToTable(helmsman, orc);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enterhere);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Freeps hinders the Orc (Sauron culture, not Raider)
		scn.FreepsPlayCard(enterhere);
		scn.FreepsChooseCard(orc);

		// Response should NOT be available - Orc is not Raider
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.IsHindered(orc));
	}
}
