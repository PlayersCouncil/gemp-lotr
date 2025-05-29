package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raider", "3_74");
					put("demands", "2_40");

					put("celeborn", "1_34");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRaiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Uruk Raider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While an ally is in the dead pile, this minion is strength +3 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raider");

		assertEquals("Uruk Raider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukRaiderIsStrengthPlus3AndFierceWhileAllyIsInDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCardsToHand(celeborn);

		var raider = scn.GetShadowCard("raider");
		scn.MoveMinionsToTable(raider);

		scn.StartGame();

		assertEquals(Zone.HAND,  celeborn.getZone());
		assertEquals(6, scn.GetStrength(raider));
		assertFalse(scn.HasKeyword(raider, Keyword.FIERCE));

		scn.MoveCardsToDeadPile(celeborn);

		assertEquals(Zone.DEAD,  celeborn.getZone());
		assertEquals(9, scn.GetStrength(raider));
		assertTrue(scn.HasKeyword(raider, Keyword.FIERCE));
	}
}
