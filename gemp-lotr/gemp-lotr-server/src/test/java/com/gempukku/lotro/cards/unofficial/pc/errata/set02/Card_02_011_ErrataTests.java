package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_02_011_ErrataTests
{

// ----------------------------------------
// MAKE LIGHT OF BURDENS TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("burdens", "52_11");      // Make Light of Burdens
					put("gimli", "1_13");         // Gimli, Son of Gloin
					put("armor", "1_8");          // Dwarven Armor

					put("chill", "1_134");        // Saruman's Chill - weather condition
					put("bladetip", "1_209");     // Blade Tip - Shadow condition
					put("orc", "1_271");          // Orc Soldier - bait
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MakeLightofBurdensStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Make Light of Burdens
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Maneuver: Exert a Dwarf companion and hinder an item on that Dwarf to hinder a Shadow condition (if it is a weather or attached to a Dwarf, discard it instead).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("burdens");

		assertEquals("Make Light of Burdens", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void MakeLightOfBurdensCanBeActivatedWithValidSetup() throws DecisionResultInvalidException, CardNotFoundException {
		// Basic activation test - can we even use it?
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var bladetip = scn.GetShadowCard("bladetip");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(burdens, bladetip);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// This is the failing case - can Freeps even activate this?
		assertTrue(scn.FreepsActionAvailable(burdens));
	}

	@Test
	public void MakeLightOfBurdensHindersShadowConditionInSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var bladetip = scn.GetShadowCard("bladetip");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(burdens, bladetip);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(bladetip));

		scn.FreepsUseCardAction(burdens);
		// Gimli auto-selected as only valid Dwarf with item
		// Armor auto-selected as only item on Gimli
		// Bladetip auto-selected as only Shadow condition

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsHindered(armor));
		assertTrue(scn.IsHindered(bladetip)); // Support area condition = hindered, not discarded
	}

	@Test
	public void MakeLightOfBurdensDiscardsWeatherCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var chill = scn.GetShadowCard("chill");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(burdens);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(scn.GetCurrentSite(), chill); // Weather attaches to site
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(burdens);
		// Gimli auto-selected
		// Armor auto-selected
		// Chill auto-selected as only Shadow condition

		// Weather should be discarded, not hindered
		assertInZone(Zone.DISCARD, chill);
	}

	@Test
	public void MakeLightOfBurdensDiscardsConditionOnDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var bladetip = scn.GetShadowCard("bladetip");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(burdens);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, bladetip); // Blade Tip attached to Gimli
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(burdens);
		// Gimli auto-selected
		// Need to choose which item to hinder (armor is the only one, but bladetip is a condition not item)
		// Armor auto-selected
		// Bladetip auto-selected as only Shadow condition

		// Condition on Dwarf should be discarded, not hindered
		assertInZone(Zone.DISCARD, bladetip);
	}

	@Test
	public void MakeLightOfBurdensNotAvailableWithoutDwarfWithItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var bladetip = scn.GetShadowCard("bladetip");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(burdens, bladetip);
		scn.MoveCompanionsToTable(gimli);
		// No armor attached!
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Should NOT be available - no Dwarf with item
		assertFalse(scn.FreepsActionAvailable(burdens));
	}

}
