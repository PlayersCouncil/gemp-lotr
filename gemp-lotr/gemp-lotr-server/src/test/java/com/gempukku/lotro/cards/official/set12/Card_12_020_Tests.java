package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_020_Tests
{
	// Site 1 has no terrain, 2 is a forest, 3 a sanctuary and no forest, 6 a forest and a sanctuary.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orophin", "12_20");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: another Elf companion, no archer; text only against a Nazgul
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: an Elf ally, to play spot an Elf; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, no Elf; a maneuver ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");  // Trollshaw Forest: a forest alone; draws on a possession played on a companion
					put("site6", "1_352");  // Lothlorien Woods: a forest and a sanctuary; strengthens allies whose home is site 6
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrophinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orophin, Brother of Haldir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While Orophin is at a forest site and you can spot another Elf, Orophin is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orophin");

		assertEquals("Orophin", card.getBlueprint().getTitle());
		assertEquals("Brother of Haldir", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WithAnotherElfOrophinIsAnArcherAtEachForestAndNowhereElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(orophin, arwen);

		scn.StartGame();

		// Bag End: no forest.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));

		// The fellowship's own move to Trollshaw Forest, a forest with no sanctuary.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
		// The other Elf at the same forest is not made an archer.
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Rivendell Valley: a sanctuary and no forest.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Lothlorien Woods: a forest, and a sanctuary.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtAForestWithNoOtherElfOrophinIsNoArcherUntilAnElfAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var golradir = scn.GetFreepsCard("golradir");
		scn.MoveCompanionsToTable(orophin, aragorn);
		scn.MoveCardsToHand(golradir);

		scn.StartGame();
		assertInHand(golradir);
		scn.SkipToSite(2);

		// At Trollshaw Forest beside a Man and Frodo: Orophin himself is no other Elf.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInHand(golradir);
		assertFalse(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));

		// Golradir, an Elf ally, is played by spotting Orophin.
		scn.FreepsPlayCard(golradir);
		assertInZone(Zone.SUPPORT, golradir);
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
