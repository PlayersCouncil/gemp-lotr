package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_006_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("arwen", "102_6");
					put("aragorn", "1_89");

					put("marksman", "1_176");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Arwen, Lady of Rivendell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Site Number: 3F
		 * Game Text: Aragorn is strength +1. While Arwen is exhausted, Aragorn is defender +1.
		* 	Each time Aragorn is about to take a wound, you may wound Arwen to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Lady of Rivendell", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void ArwenMakesAragornStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCardToHand(arwen);
		scn.FreepsMoveCharToTable(aragorn);

		scn.StartGame();
		assertEquals(8, scn.getStrength(aragorn));
		scn.FreepsPlayCard(arwen);
		assertEquals(9, scn.getStrength(aragorn));
	}

	@Test
	public void ArwenMakesAragornDefenderPlus1WhileSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn, arwen);

		scn.StartGame();

		assertEquals(3, scn.GetVitality(arwen));
		assertFalse(scn.hasKeyword(aragorn, Keyword.DEFENDER));
		scn.AddWoundsToChar(arwen, 1);

		assertEquals(2, scn.GetVitality(arwen));
		assertFalse(scn.hasKeyword(aragorn, Keyword.DEFENDER));
		scn.AddWoundsToChar(arwen, 1);

		assertEquals(1, scn.GetVitality(arwen));
		assertTrue(scn.hasKeyword(aragorn, Keyword.DEFENDER));
		scn.AddWoundsToChar(arwen, 1);
		scn.PassCurrentPhaseActions(); //Death is only processed when there's change in the game process

		assertEquals(Zone.DEAD, arwen.getZone());
		assertFalse(scn.hasKeyword(aragorn, Keyword.DEFENDER));
	}

	@Test
	public void ArwenSelfWoundsToPreventWoundToAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn, arwen);

		var marksman = scn.GetShadowCard("marksman");
		scn.ShadowMoveCharToTable(marksman);

		scn.StartGame();

		scn.AddWoundsToChar(arwen, 2);

		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetVitality(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(Zone.DEAD, arwen.getZone());
		assertFalse(scn.hasKeyword(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}
}
