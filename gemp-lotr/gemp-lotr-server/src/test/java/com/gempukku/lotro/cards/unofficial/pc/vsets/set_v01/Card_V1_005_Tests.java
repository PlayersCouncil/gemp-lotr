
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("onedwarf", "101_5");
					put("gimli", "1_13");
					put("guard", "1_7");
					put("handaxe1", "2_10");
					put("handaxe2", "2_10");
					put("armor", "1_8");
					put("bracers", "2_3");
					put("ring", "9_6");

					put("runner", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("runner5", "1_178");
					put("runner6", "1_178");
				}}
		);
	}

	@Test
	public void OneDwarfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: One Dwarf in Moria
		* Side: Free Peoples
		* Culture: dwarven
		* Twilight Cost: 1
		* Type: event
		* Subtype: Maneuver
		* Game Text: Spot a Dwarf bearing 2 possessions to make that Dwarf defender +1 until the regroup phase. 
		*/

		//Pre-game setup
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");

		assertEquals(1, onedwarf.getBlueprint().getTwilightCost());
		assertEquals(CardType.EVENT, onedwarf.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(onedwarf, Timeword.MANEUVER));
		assertEquals(Culture.DWARVEN, onedwarf.getBlueprint().getCulture());
		assertEquals(Side.FREE_PEOPLE, onedwarf.getBlueprint().getSide());
	}

	@Test
	public void OneDwarfHeals() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(onedwarf);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("guard");
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("handaxe1"), scn.GetFreepsCard("handaxe2"));

		scn.MoveMinionsToTable("runner");

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable("One Dwarf"));

		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(onedwarf);
		assertEquals(1, scn.GetWoundsOn(gimli));
	}

	@Test
	public void OneDwarfDiscardsVariableNumberOfItemsToWoundMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(onedwarf);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("guard");
		scn.AttachCardsTo(gimli,
				scn.GetFreepsCard("handaxe1"),
				scn.GetFreepsCard("handaxe2"),
				scn.GetFreepsCard("armor"),
				scn.GetFreepsCard("bracers"),
				scn.GetFreepsCard("ring"));

		scn.MoveMinionsToTable("runner", "runner2", "runner3", "runner4", "runner5", "runner6");

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable("One Dwarf"));

		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(onedwarf);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to return to hand"));
		assertEquals(5, scn.FreepsGetCardChoiceCount()); // There are 5 items on gimli
		String[] choices = scn.FreepsGetCardChoices().toArray(new String[0]);
		scn.FreepsChoose(choices[0], choices[1], choices[2], choices[3], choices[4]);

		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		choices = scn.FreepsGetCardChoices().toArray(new String[0]);
		scn.FreepsChoose(choices[0], choices[1], choices[2], choices[3], choices[4]);

		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[0]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[1]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[2]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[3]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[4]).getZone());

		assertEquals(Zone.SHADOW_CHARACTERS, scn.GetShadowCardByID(choices[5]).getZone());
	}

	@Test
	public void OneDwarfDiscardsAndWoundsEvenIfDwarfIsFullyHealed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(onedwarf);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("guard");
		scn.AttachCardsTo(gimli,
				scn.GetFreepsCard("handaxe1"),
				scn.GetFreepsCard("handaxe2"),
				scn.GetFreepsCard("armor"),
				scn.GetFreepsCard("bracers"),
				scn.GetFreepsCard("ring"));

		scn.MoveMinionsToTable("runner", "runner2", "runner3", "runner4", "runner5", "runner6");

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable("One Dwarf"));

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(onedwarf);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to return to hand"));
		assertEquals(5, scn.FreepsGetCardChoiceCount()); // There are 5 items on gimli
		String[] choices = scn.FreepsGetCardChoices().toArray(new String[0]);
		scn.FreepsChoose(choices[0], choices[1], choices[2], choices[3], choices[4]);

		assertTrue(scn.FreepsDecisionAvailable("Choose cards to wound"));
		choices = scn.FreepsGetCardChoices().toArray(new String[0]);
		scn.FreepsChoose(choices[0], choices[1], choices[2], choices[3], choices[4]);

		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[0]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[1]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[2]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[3]).getZone());
		assertEquals(Zone.DISCARD, scn.GetShadowCardByID(choices[4]).getZone());

		assertEquals(Zone.SHADOW_CHARACTERS, scn.GetShadowCardByID(choices[5]).getZone());
	}

	@Test
	public void OneDwarfCantBePlayedIfNoDwarvesWithTwoItems() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl onedwarf = scn.GetFreepsCard("onedwarf");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(onedwarf);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("guard");
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("handaxe1"));

		scn.MoveMinionsToTable("runner");

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsActionAvailable("One Dwarf"));

	}
}
