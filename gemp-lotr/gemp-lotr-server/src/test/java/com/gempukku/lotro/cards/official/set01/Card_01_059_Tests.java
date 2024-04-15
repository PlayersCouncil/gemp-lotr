package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_059_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("sts", "1_59");
					put("legolas", "1_50");
					put("gimli", "1_13");
					put("rumil", "1_57");
					put("grimir", "1_17");

					put("runner", "1_178");

					put("finalstrike", "10_20");
					put("gollum", "5_24");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ShouldertoShoulderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Shoulder to Shoulder
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Add (1) and exert a Dwarf to heal an Elf, or add (1) and exert an Elf to heal a Dwarf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sts");

		assertEquals("Shoulder to Shoulder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverActionWorksOnDwarves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var rumil = scn.GetFreepsCard("rumil");
		var grimir = scn.GetFreepsCard("grimir");
		scn.FreepsMoveCharToTable(legolas, gimli);
		scn.FreepsMoveCardToSupportArea(rumil, grimir, sts);

		scn.ShadowMoveCharToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);

		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(grimir, 1);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(grimir));

		assertTrue(scn.FreepsActionAvailable("Exert a Dwarf"));
		scn.FreepsChooseAction("Exert a Dwarf");

		assertEquals(4, scn.GetFreepsCardChoiceCount());
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(rumil);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(rumil));

		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsActionAvailable("Exert a Dwarf"));
		scn.FreepsChooseAction("Exert a Dwarf");

		//Legolas, Rumil, and Grimir
		assertEquals(3, scn.GetFreepsCardChoiceCount());
		scn.FreepsChooseCard(grimir);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(legolas));
	}

	@Test
	public void ManeuverActionWorksOnElves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var rumil = scn.GetFreepsCard("rumil");
		var grimir = scn.GetFreepsCard("grimir");
		scn.FreepsMoveCharToTable(legolas, gimli);
		scn.FreepsMoveCardToSupportArea(rumil, grimir, sts);

		scn.ShadowMoveCharToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);

		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(rumil, 1);
		scn.AddWoundsToChar(grimir, 1);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(grimir));

		assertTrue(scn.FreepsActionAvailable("Exert an Elf"));
		scn.FreepsChooseAction("Exert an Elf");

		//Initially either dwarves or elves can be selected
		assertEquals(4, scn.GetFreepsCardChoiceCount());
		scn.FreepsChooseCard(legolas);

		//Once an Elf has been selected, only Dwarves can be selected as a target
		assertEquals(2, scn.GetFreepsCardChoiceCount());
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsActionAvailable("Exert an Elf"));
		scn.FreepsChooseAction("Exert an Elf");

		scn.FreepsChooseCard(rumil);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(grimir));
	}

	@Test
	public void STSIsProperlyBlockedByFinalStrike() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCharToTable(legolas, gimli);
		scn.FreepsMoveCardToSupportArea(sts);

		var gollum = scn.GetShadowCard("gollum");
		var finalstrike = scn.GetShadowCard("finalstrike");
		scn.ShadowMoveCardToSupportArea(finalstrike);
		scn.ShadowMoveCharToTable(gollum);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);

		scn.AddWoundsToChar(legolas, 1);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gollum));

		scn.FreepsChooseAction("Exert a Dwarf");
		scn.FreepsChooseCard(gimli);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		//Final Strike is a "Response:" action, so it can technically be repeated (for some reason)
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.ShadowDecisionAvailable("Maneuver action"));

		// The costs of STS should have still been paid, meaning that Gimli should have exerted
		// and the twilight been added, but the effect (healing Legolas) should have been blocked
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(gollum));
	}
}
