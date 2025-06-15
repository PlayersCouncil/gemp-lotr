package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_154_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{

					put("soldier", "51_154");
					put("chaff1", "1_155");
					put("chaff2", "1_156");
					put("chaff3", "1_157");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Uruk Soldier
		* Unique: False
		* Side: SHADOW
		* Culture: Isengard
		* Twilight Cost: 2
		* Type: minion
		* Subtype: Uruk-hai
		* Strength: 7
		* Vitality: 1
		* Site Number: 5
		* Game Text: <b>Damage +1</b>.
		 * When you play this minion, exert a companion.  The Free Peoples player may discard the top 2 cards of their draw deck to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");

		assertFalse(soldier.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, soldier.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, soldier.getBlueprint().getCulture());
		assertEquals(CardType.MINION, soldier.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, soldier.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(soldier, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(2, soldier.getBlueprint().getTwilightCost());
		assertEquals(7, soldier.getBlueprint().getStrength());
		assertEquals(1, soldier.getBlueprint().getVitality());
		assertEquals(5, soldier.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukSoldierExertsCompanionIfNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();

		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(soldier);

		assertTrue(scn.FreepsAnyDecisionsAvailable());

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void UrukSoldierExertionCanBePreventedByDiscardingTopDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();

		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(soldier);

		assertTrue(scn.FreepsAnyDecisionsAvailable());

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
	}
}
