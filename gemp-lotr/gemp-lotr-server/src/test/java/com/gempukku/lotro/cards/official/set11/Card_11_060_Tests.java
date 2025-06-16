package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quality", "11_60");
					put("man1", "14_7");
					put("man2", "14_8");
					put("man3", "14_9");

					put("sauron", "9_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheHighestQualityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The Highest Quality
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert any number of [gondor] companions who have total resistance 12
		 * or more to make a minion skirmishing a [gondor] companion strength -3 for each
		 * companion exerted this way.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quality");

		assertEquals("The Highest Quality", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheHighestQualityExerts12ResWorthOfMenToDebuffAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var quality = scn.GetFreepsCard("quality");
		var man1 = scn.GetFreepsCard("man1");
		var man2 = scn.GetFreepsCard("man2");
		var man3 = scn.GetFreepsCard("man3");
		scn.MoveCardsToHand(quality);
		scn.MoveCompanionToTable(man1, man2, man3);

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();
		scn.RemoveBurdens(1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(man1, sauron);
		scn.FreepsResolveSkirmish(man1);

		assertEquals(5, scn.GetResistance(man1));
		assertEquals(5, scn.GetResistance(man2));
		assertEquals(6, scn.GetResistance(man3));
		assertEquals(0, scn.GetWoundsOn(man1));
		assertEquals(0, scn.GetWoundsOn(man2));
		assertEquals(0, scn.GetWoundsOn(man3));
		assertEquals(24, scn.GetStrength(sauron));
		assertTrue(scn.FreepsPlayAvailable(quality));

		scn.FreepsPlayCard(quality);
		scn.FreepsChooseCards(man1, man2, man3);
		assertEquals(1, scn.GetWoundsOn(man1));
		assertEquals(1, scn.GetWoundsOn(man2));
		assertEquals(1, scn.GetWoundsOn(man3));
		assertEquals(15, scn.GetStrength(sauron));

	}
}
