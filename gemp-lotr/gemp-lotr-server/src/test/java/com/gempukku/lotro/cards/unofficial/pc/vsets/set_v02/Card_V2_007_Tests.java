package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_007_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("contest", "102_7");
					put("dwarf", "1_7");
					put("gimli", "1_13");
					put("elf", "4_76");
					put("legolas", "1_50");
					put("count", "4_69");
					put("notched", "4_52");

					put("runner", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DeadlyContestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Deadly Contest
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot an [elven] companion to reinforce a [dwarven] token or spot a [dwarven] companion to reinforce an [elven] token.
		* 	If you can spot both Legolas and Gimli you may reinforce both an [elven] and [dwarven] token an additional time.  
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("contest");

		assertEquals("Deadly Contest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.hasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DeadlyContestSpotsAnElfToReinforceADwarvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var dwarf = scn.GetFreepsCard("dwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		scn.FreepsMoveCardToHand(contest);
		scn.FreepsMoveCharToTable(elf);
		scn.FreepsMoveCardToSupportArea(notched);
		scn.AddTokensToCard(notched, 1);

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(Zone.FREE_CHARACTERS, elf.getZone());
		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void DeadlyContestSpotsADwarfToReinforceAnElvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var dwarf = scn.GetFreepsCard("dwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		scn.FreepsMoveCardToHand(contest);
		scn.FreepsMoveCharToTable(dwarf);
		scn.FreepsMoveCardToSupportArea(count);
		scn.AddTokensToCard(count, 1);

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(Zone.FREE_CHARACTERS, dwarf.getZone());
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);

		assertEquals(2, scn.GetCultureTokensOn(count));
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void DeadlyContestCannotPlayIfNeitherElfNorDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var dwarf = scn.GetFreepsCard("dwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		scn.FreepsMoveCardToHand(contest);
		scn.FreepsMoveCardToSupportArea(count, notched);
		scn.AddTokensToCard(count, 1);
		scn.AddTokensToCard(notched, 1);

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsPlayAvailable(contest));
	}

	@Test
	public void DeadlyContestCanSpotLegolasAndGimliToReinforceAnother2TokensAfterReinforcingDwarvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var dwarf = scn.GetFreepsCard("dwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		scn.FreepsMoveCardToHand(contest);
		scn.FreepsMoveCharToTable(gimli, legolas);
		scn.FreepsMoveCardToSupportArea(count, notched);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(count, 1);

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);
		scn.FreepsChooseMultipleChoiceOption("Spot an"); //[Elven] companion to reinforce a [Dwarven] token

		//Reinforced initially and then again by the Legolas/Gimli clause
		assertEquals(3, scn.GetCultureTokensOn(notched));
		//Only reinforced by the Legolas/Gimli clause
		assertEquals(2, scn.GetCultureTokensOn(count));
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void DeadlyContestCanSpotLegolasAndGimliToReinforceAnother2TokensAfterReinforcingElvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var contest = scn.GetFreepsCard("contest");
		var dwarf = scn.GetFreepsCard("dwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var count = scn.GetFreepsCard("count");
		var notched = scn.GetFreepsCard("notched");
		scn.FreepsMoveCardToHand(contest);
		scn.FreepsMoveCharToTable(gimli, legolas);
		scn.FreepsMoveCardToSupportArea(count, notched);
		scn.AddTokensToCard(notched, 1);
		scn.AddTokensToCard(count, 1);

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetCultureTokensOn(notched));
		assertTrue(scn.FreepsPlayAvailable(contest));
		scn.FreepsPlayCard(contest);
		scn.FreepsChooseMultipleChoiceOption("Spot a "); //[Dwarven] companion to reinforce an [Elven] token

		//Reinforced initially and then again by the Legolas/Gimli clause
		assertEquals(3, scn.GetCultureTokensOn(count));
		//Only reinforced by the Legolas/Gimli clause
		assertEquals(2, scn.GetCultureTokensOn(notched));
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}
}
