package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_251_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Legolas, Greenleaf: an Elf companion, vitality 3.  Archery is never reached.
					put("legolas", "1_50");
					// Arwen, Daughter of Elrond: an Elf companion, vitality 3; strength +3 against a Nazgûl only
					put("arwen", "1_30");
					// Calaglin, Elf of Lórien: an Elf ally, vitality 3.  His regroup ability is not used.
					put("calaglin", "3_10");
					// Gimli, Son of Glóin: a Dwarf companion, vitality 3.  His skirmish ability is not used.
					put("gimli", "1_13");
				}},
				new HashMap<>() {{
					put("road", "11_251");
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void OldForestRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Old Forest Road
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Forest</b>. At the start of your fellowship phase, you may heal 2 Elves.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("road");

		assertEquals("Old Forest Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void TwoOfThreeWoundedElvesAreHealedOnceEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var calaglin = scn.GetFreepsCard("calaglin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, arwen, gimli);
		scn.MoveAlliesToTable(calaglin);
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(calaglin, 2);
		scn.AddWoundsToChar(gimli, 2);

		var road = scn.GetFreepsCard("road");

		scn.StartGame(road);

		assertSame(road, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();

		// Two of the three wounded Elves, companions and an ally alike.  A wounded Dwarf is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, calaglin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(arwen, calaglin);

		// One wound off each of the two chosen, none off the third Elf or the Dwarf.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoElfWoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var calaglin = scn.GetFreepsCard("calaglin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(calaglin);
		// Only the Dwarf is wounded.
		scn.AddWoundsToChar(gimli, 2);

		var road = scn.GetFreepsCard("road");

		scn.StartGame(road);

		assertSame(road, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(calaglin));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningHealsNobodyAndTheOtherPlayerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(arwen, 1);

		// The second player has a wounded Elf of his own on the table.
		var shadowLegolas = scn.GetShadowCard("legolas");
		scn.MoveCompanionsToTable(shadowLegolas);
		scn.AddWoundsToChar(shadowLegolas, 1);

		var road = scn.GetFreepsCard("road");

		scn.StartGame(road);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(shadowLegolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipIsAtTheRoadAndNotOnceItHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(calaglin);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(calaglin, 1);

		var shadowArwen = scn.GetShadowCard("arwen");
		var shadowCalaglin = scn.GetShadowCard("calaglin");
		scn.MoveCompanionsToTable(shadowArwen);
		scn.MoveAlliesToTable(shadowCalaglin);
		scn.AddWoundsToChar(shadowArwen, 1);
		scn.AddWoundsToChar(shadowCalaglin, 1);

		var road = scn.GetFreepsCard("road");
		var ruins = scn.GetShadowSite("site5");     // Sirannon Ruins: a river, and no other text

		scn.StartGame(road);

		// The first player's turn: he is offered and declines.  The second player, whose own Elves are wounded,
		// is offered nothing on a turn that is not his.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// He moves on to site 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(2, scn.FreepsGetSiteNumber());

		// The second player's turn begins at site 1, which is the first player's card: the offer is the
		// second player's, and his own two wounded Elves are healed.  The first player's are not.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertSame(road, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(1, scn.GetWoundsOn(shadowArwen));
		assertEquals(1, scn.GetWoundsOn(shadowCalaglin));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(shadowArwen));
		assertEquals(0, scn.GetWoundsOn(shadowCalaglin));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(calaglin));

		// The first player's second turn begins at site 2, with his two Elves still wounded.  The Road lies
		// behind him, and nothing is offered.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(road, scn.GetSite(1));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
