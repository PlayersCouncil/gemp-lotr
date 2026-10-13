package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "1_34");
					put("orophin", "1_56");     // Orophin, Lórien Bowman: [elven] Elf ally, home 6, vitality 3; archery ability only
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf ally, home 3, vitality 2; takes on site 3's text, not in play here
					put("legolas", "11_21");    // Legolas, Companion of the Ring: an [elven] Elf COMPANION; text only at a battleground or forest
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] Dwarf ally; fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CelebornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Celeborn, Lord of Lórien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Fellowship:</b> Exert Celeborn to heal an [elven] ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("celeborn");

		assertEquals("Celeborn", card.getBlueprint().getTitle());
		assertEquals("Lord of Lórien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void FellowshipActionExertsCelebornToHealAnElvenAllyUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(celeborn, orophin, silinde, grimir);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(orophin, 2);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(grimir, 1);

		assertTrue(scn.FreepsActionAvailable(celeborn));
		scn.FreepsUseCardAction(celeborn);
		assertEquals(2, scn.GetWoundsOn(celeborn));

		// Every [elven] ally, Celeborn himself among them; not the [elven] companion, nor the ally of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, orophin, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(orophin);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(2, scn.GetWoundsOn(celeborn));

		// Vitality 3 with 2 wounds: Celeborn is exhausted and cannot exert, with wounded [elven] allies still to heal.
		assertFalse(scn.FreepsActionAvailable(celeborn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
