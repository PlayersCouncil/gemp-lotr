package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_291_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gaffer", "1_291");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; his own abilities are used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] Hobbit companion who is neither Frodo nor Sam
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheGafferStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Gaffer, Sam's Father
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Home: 1
		 * Strength: 2
		 * Vitality: 2
		 * Game Text: <b>Fellowship:</b> Exert The Gaffer to heal Frodo or Sam.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gaffer");

		assertEquals("The Gaffer", card.getBlueprint().getTitle());
		assertEquals("Sam's Father", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void FellowshipActionExertsTheGafferToHealFrodoOrSamAndIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveAlliesToTable(gaffer, bounder);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(bounder, 1);

		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsActionAvailable(gaffer));

		scn.FreepsUseCardAction(gaffer);
		// Frodo (the Ring-bearer) and Sam; not another Hobbit companion, a Hobbit ally, or The Gaffer himself.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, bounder, gaffer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(bounder));

		// Vitality 2 with 1 wound: he cannot exert again, and the action is no longer offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gaffer));
	}
}
