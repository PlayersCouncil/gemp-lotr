package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_059_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("sts", "51_59");
					put("legolas", "1_50");
					put("gimli", "1_13");
					put("rumil", "1_57");
					put("grimir", "1_17");

					put("runner", "1_178");
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
		* Title: Shoulder to Shoulder
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: <b>Fellowship:</b> Add (1) and exert a Dwarf to heal an Elf, or add (1) and exert an Elf to heal a Dwarf.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");

		assertFalse(sts.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sts.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, sts.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, sts.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(sts, Keyword.SUPPORT_AREA));
		assertEquals(1, sts.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionWorksOnDwarves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var rumil = scn.GetFreepsCard("rumil");
		var grimir = scn.GetFreepsCard("grimir");
		scn.FreepsMoveCharToTable(legolas, gimli);
		scn.FreepsMoveCardToSupportArea(rumil, grimir, sts);

		scn.StartGame();

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

		assertEquals(4, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(rumil);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(rumil));

		assertTrue(scn.FreepsActionAvailable("Exert a Dwarf"));
		scn.FreepsChooseAction("Exert a Dwarf");

		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(grimir);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(legolas));
	}

	@Test
	public void FellowshipActionWorksOnElves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var rumil = scn.GetFreepsCard("rumil");
		var grimir = scn.GetFreepsCard("grimir");
		scn.FreepsMoveCharToTable(legolas, gimli);
		scn.FreepsMoveCardToSupportArea(rumil, grimir, sts);

		scn.StartGame();

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

		assertEquals(4, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(legolas);
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsActionAvailable("Exert an Elf"));
		scn.FreepsChooseAction("Exert an Elf");

		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(rumil);
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(grimir));
	}

	@Test
	public void NoManeuverAction() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sts = scn.GetFreepsCard("sts");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var rumil = scn.GetFreepsCard("rumil");
		var grimir = scn.GetFreepsCard("grimir");
		scn.FreepsMoveCharToTable(legolas, gimli);
		scn.FreepsMoveCardToSupportArea(rumil, grimir, sts);

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.FreepsAnyActionsAvailable());
	}
}
