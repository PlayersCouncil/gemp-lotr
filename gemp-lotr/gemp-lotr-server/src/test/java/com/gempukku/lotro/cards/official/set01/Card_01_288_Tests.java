package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_288_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("maggot", "1_288");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion; +2 only while he bears a weapon
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Pippin; a skirmish ability used only on purpose
					put("rohanmerry", "103_85");  // Merry, Master Holbytla: a [rohan] Merry; his texts need [rohan] companions or a skirmish
					put("shirepippin", "1_307");  // Pippin, Hobbit of Some Intelligence: [shire]; his text answers only his own play
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion who is neither Merry nor Pippin
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarmerMaggotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Farmer Maggot, Chaser of Rascals
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Home: 1
		 * Strength: 2
		 * Vitality: 3
		 * Game Text: <b>Fellowship:</b> Exert Farmer Maggot to heal Merry or Pippin.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("maggot");

		assertEquals("Farmer Maggot", card.getBlueprint().getTitle());
		assertEquals("Chaser of Rascals", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void FellowshipActionExertsFarmerMaggotToHealMerryOrPippin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var maggot = scn.GetFreepsCard("maggot");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry, pippin, sam);
		scn.MoveAlliesToTable(maggot, bounder);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(bounder, 1);

		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsActionAvailable(maggot));

		scn.FreepsUseCardAction(maggot);
		// A [shire] Merry and a [gondor] Pippin; not the Ring-bearer, another Hobbit companion, a Hobbit ally, or
		// Farmer Maggot himself, wounded by his own exertion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, maggot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealsAMerryOfAnotherCultureAndIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var maggot = scn.GetFreepsCard("maggot");
		var merry = scn.GetFreepsCard("rohanmerry");
		var pippin = scn.GetFreepsCard("shirepippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.AddWoundsToChar(maggot, 1);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 2);

		assertEquals(1, scn.GetWoundsOn(maggot));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(maggot));

		scn.FreepsUseCardAction(maggot);
		// A [rohan] Merry and a [shire] Pippin.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		scn.FreepsChooseCard(merry);

		assertEquals(2, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));

		// Vitality 3 with 2 wounds: he cannot exert again, and the action is no longer offered, with both
		// Hobbits still wounded.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(maggot));
	}
}
