package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_03_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "3_13");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Elrond, Herald to Gil-galad
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: At the start of each of your turns, you may spot an ally whose home is site 3 and heal that ally up to 2 times.<br><b>Regroup:</b> Exert Elrond twice to heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Herald to Gil-galad", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void AtTheStartOfTurnHealAHome3AllyTwice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionToTable(elrond);

		scn.StartGame();
		scn.AddWoundsToChar(elrond, 3);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("2");

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(elrond));
	}

	@Test
	public void RegroupActionExertsElrondTwiceToHealCompanionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionToTable(elrond);

		scn.StartGame();
		scn.FreepsDeclineOptionalTrigger();
		scn.AddWoundsToChar(frodo, 3);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(frodo));
	}
}
