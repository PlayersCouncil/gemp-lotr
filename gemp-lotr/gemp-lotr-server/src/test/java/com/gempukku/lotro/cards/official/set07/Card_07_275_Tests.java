package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_275_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "7_275");
					put("troop", "7_279");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Pillager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br>While this minion is stacked on a site you control, besiegers are <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pillager");

		assertEquals("Gorgoroth Pillager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GorgorothPillagerMakesBesiegersFierceWhenStackedOnSiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var site1 = scn.GetFreepsSite(1);

		var pillager = scn.GetShadowCard("pillager");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(pillager, troop);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));

		scn.MoveMinionsToTable(troop);
		scn.StackCardsOn(site1, pillager);

		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
	}
}
