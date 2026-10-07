package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_3");
					put("gimli", "8_5");
					put("greed1", "1_125");     // Greed: an [isengard] condition in the Shadow support area, not unique
					put("greed2", "1_125");
					put("greed3", "1_125");
					put("greed4", "1_125");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodRunsChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Blood Runs Chill
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot a Dwarf who is damage +X and exert that Dwarf twice to make an opponent discard X Shadow cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Blood Runs Chill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BloodRunsChillTest1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(card);

		scn.StartGame();
	scn.SkipToPhase(Phase.REGROUP);

		int twilightBeforePlay = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(card);
		assertEquals(twilightBeforePlay + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gimli));
	}
	@Test
	public void HinderedShadowCardsMayBeAmongThoseDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(card);

		var hindered1 = scn.GetShadowCard("greed1");
		var hindered2 = scn.GetShadowCard("greed2");
		var greed3 = scn.GetShadowCard("greed3");
		var greed4 = scn.GetShadowCard("greed4");
		scn.MoveCardsToSupportArea(hindered1, hindered2, greed3, greed4);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098).
		scn.HinderCard(hindered1, hindered2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(card);

		// Gimli, damage +1, is damage +1 more for each of the two wounds: X is 3, of the four Shadow cards.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasCardChoiceAvailable(hindered1, hindered2, greed3, greed4));
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(hindered1, hindered2, greed3);

		assertEquals(Zone.DISCARD, hindered1.getZone());
		assertEquals(Zone.DISCARD, hindered2.getZone());
		assertEquals(Zone.DISCARD, greed3.getZone());
		assertEquals(Zone.SUPPORT, greed4.getZone());
	}
}
