package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_020_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("golradir", "53_20");
					put("erestor", "53_14");
					put("saelbeth", "53_25");
					put("arwen", "3_7");

					put("nazgul", "12_161");
					put("uruk", "1_151");
					put("orc", "1_178");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GolradirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Golradir, Councilor of Imladris
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot an Elf.<br><b>Skirmish:</b> Exert Golradir to make a minion strength -1 (or -1 for each Elf you can spot if that minion is an Orc).  
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("golradir");

		assertEquals("Golradir", card.getBlueprint().getTitle());
		assertEquals("Councilor of Imladris", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void GolradirSpotsAnElfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var erestor = scn.GetFreepsCard("erestor");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToHand(arwen, golradir);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(golradir));
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(golradir));
	}

	@Test
	public void GolradirExertsToMakeANonOrcStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var erestor = scn.GetFreepsCard("erestor");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen, golradir);

		var nazgul = scn.GetShadowCard("nazgul");
		scn.MoveMinionsToTable(nazgul);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, nazgul);

		assertEquals(0, scn.GetWoundsOn(golradir));
		assertEquals(10, scn.GetStrength(nazgul));
		assertTrue(scn.FreepsActionAvailable(golradir));

		scn.FreepsUseCardAction(golradir);
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(9, scn.GetStrength(nazgul));
	}

	@Test
	public void GolradirExertsToMakeAnOrcStrengthMinus1PerElf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var golradir = scn.GetFreepsCard("golradir");
		var erestor = scn.GetFreepsCard("erestor");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(golradir, erestor, saelbeth);

		var orc = scn.GetShadowCard("orc");
		scn.MoveMinionsToTable(orc);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(arwen, orc);

		assertEquals(0, scn.GetWoundsOn(golradir));
		assertEquals(5, scn.GetStrength(orc));
		assertTrue(scn.FreepsActionAvailable(golradir));

		scn.FreepsUseCardAction(golradir);
		assertEquals(1, scn.GetWoundsOn(golradir));
		// -4 for 4 elves
		assertEquals(1, scn.GetStrength(orc));
	}


}
