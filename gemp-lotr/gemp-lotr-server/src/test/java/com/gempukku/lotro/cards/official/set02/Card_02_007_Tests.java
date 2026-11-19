package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gloin", "2_7");

					// Great Works Begun There: [dwarven] tale, support area, cost 1; its trigger answers playing a Dwarf
					put("tale1", "2_9");
					put("tale2", "2_9");
					put("tale3", "2_9");
					put("tale4", "2_9");
					put("tale5", "2_9");
					put("shiretale", "2_108");  // O Elbereth! Gilthoniel!: [shire] tale, cost 1, on the Ring-bearer; a skirmish ability never used

					put("tilldurin", "2_14");   // Till Durin Wakes Again: [dwarven] tale, support area, cost 0, heals on leaving underground sites
					put("book", "3_1");         // Book of Mazarbul: [dwarven] tale that is a possession, bearer must be a Dwarf
					put("luthien", "1_100");    // The Choice of Luthien: [gondor] tale, support area, a maneuver ability
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession that is not a tale
					put("guard", "1_7");        // Dwarf Guard: another [dwarven] Dwarf companion, strength 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GloinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Glóin, Friend to Thorin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: For each [dwarven] tale you can spot, Glóin is strength +1 (limit +4).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gloin");

		assertEquals("Glóin", card.getBlueprint().getTitle());
		assertEquals("Friend to Thorin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void GloinIsStrengthPlus1PerDwarvenTaleUpToPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		scn.MoveCompanionsToTable(gloin);
		var tale1 = scn.GetFreepsCard("tale1");
		var tale2 = scn.GetFreepsCard("tale2");
		var tale3 = scn.GetFreepsCard("tale3");
		var tale4 = scn.GetFreepsCard("tale4");
		var tale5 = scn.GetFreepsCard("tale5");
		var shiretale = scn.GetFreepsCard("shiretale");
		scn.MoveCardsToHand(tale1, tale2, tale3, tale4, tale5, shiretale);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(gloin));

		// No effect from a tale of another culture
		scn.FreepsPlayCard(shiretale);
		assertEquals(5, scn.GetStrength(gloin));

		// +1 per [dwarven] tale
		scn.FreepsPlayCard(tale1);
		assertEquals(6, scn.GetStrength(gloin));
		scn.FreepsPlayCard(tale2);
		assertEquals(7, scn.GetStrength(gloin));
		scn.FreepsPlayCard(tale3);
		assertEquals(8, scn.GetStrength(gloin));
		scn.FreepsPlayCard(tale4);
		assertEquals(9, scn.GetStrength(gloin));

		// The fifth [dwarven] tale does nothing: the limit is +4.
		scn.FreepsPlayCard(tale5);
		assertInZone(Zone.SUPPORT, tale5);
		assertEquals(9, scn.GetStrength(gloin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CountsOnlyDwarvenTalesOfEitherCardTypeAndStrengthensOnlyGloin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var book = scn.GetFreepsCard("book");
		var tilldurin = scn.GetFreepsCard("tilldurin");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(gloin, guard);
		// A [dwarven] possession that is not a tale, and a tale that is not [dwarven].
		scn.AttachCardsTo(gloin, armor);
		scn.MoveCardsToSupportArea(luthien);

		scn.StartGame();

		assertAttachedTo(armor, gloin);
		assertInZone(Zone.SUPPORT, luthien);
		assertEquals(5, scn.GetStrength(gloin));
		assertEquals(4, scn.GetStrength(guard));

		// A [dwarven] tale that is a possession, borne by another Dwarf.
		scn.AttachCardsTo(guard, book);
		assertEquals(6, scn.GetStrength(gloin));

		// A [dwarven] tale that is a condition in the support area.
		scn.MoveCardsToSupportArea(tilldurin);
		// 5 + 2: under "tale" alone it would be 8, under "[dwarven]" alone 9 (five [dwarven] cards, capped at +4).
		assertEquals(7, scn.GetStrength(gloin));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
