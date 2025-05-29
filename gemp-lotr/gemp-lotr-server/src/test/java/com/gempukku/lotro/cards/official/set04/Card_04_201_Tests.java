package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_201_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veteran", "4_201");
					put("child", "4_148"); // source of easy site control
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukVeteranStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Veteran
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Regroup:</b> Stack this minion on a site you control.<br><b>Shadow:</b> If stacked on a site you control, play this minion. Its twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veteran");

		assertEquals("Uruk Veteran", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukVeteranRegroupActionStacksSelfOnControlledSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var child = scn.GetShadowCard("child");
		scn.MoveCardsToHand(child, veteran);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.MoveCardsToSupportArea(child);
		scn.AddTokensToCard(child, 2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(child); //should take control of site

		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		var controlledSite = scn.GetFreepsSite(1);
		assertTrue(scn.IsSiteControlled(controlledSite));
		assertTrue(scn.ShadowActionAvailable(veteran));

		scn.ShadowUseCardAction(veteran);
		assertEquals(controlledSite, veteran.getStackedOn());
		assertEquals(Zone.STACKED, veteran.getZone());
	}

	@Test
	public void UrukVeteranShadowActionPlaysSelfFromControlledSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var veteran = scn.GetShadowCard("veteran");
		var child = scn.GetShadowCard("child");
		scn.MoveCardsToHand(child, veteran);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.MoveCardsToSupportArea(child);
		scn.AddTokensToCard(child, 2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(child); //should take control of site

		scn.SkipToSite(4);
		var controlledSite = scn.GetFreepsSite(1);
		assertTrue(scn.IsSiteControlled(controlledSite));
		scn.StackCardsOn(controlledSite, veteran);
		scn.SetTwilight(3);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(controlledSite, veteran.getStackedOn());
		assertEquals(Zone.STACKED, veteran.getZone());

		assertTrue(scn.ShadowActionAvailable(veteran));
		assertEquals(10, scn.GetTwilight());
		scn.ShadowUseCardAction(veteran);
		assertEquals(Zone.SHADOW_CHARACTERS, veteran.getZone());
		// Costs 3, -1 discount = 2
		assertEquals(8, scn.GetTwilight());
	}
}
