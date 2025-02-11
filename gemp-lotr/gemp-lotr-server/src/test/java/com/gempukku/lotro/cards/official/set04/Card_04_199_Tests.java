package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_199_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("trooper", "4_199");
					put("child", "4_148"); // source of easy site control
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void UrukTrooperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Trooper
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * <b>Regroup:</b> Stack this minion on a site you control.
		 * 	<b>Shadow:</b> If stacked on a site you control, play this minion. Its twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trooper");

		assertEquals("Uruk Trooper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukTrooperRegroupActionStacksSelfOnControlledSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var child = scn.GetShadowCard("child");
		scn.ShadowMoveCardToHand(child, trooper);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.ShadowMoveCardToSupportArea(child);
		scn.AddTokensToCard(child, 2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(child); //should take control of site

		scn.ShadowMoveCharToTable(trooper);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		var controlledSite = scn.GetFreepsSite(1);
		assertTrue(scn.IsSiteControlled(controlledSite));
		assertTrue(scn.ShadowActionAvailable(trooper));

		scn.ShadowUseCardAction(trooper);
		assertEquals(controlledSite, trooper.getStackedOn());
		assertEquals(Zone.STACKED, trooper.getZone());
	}

	@Test
	public void UrukTrooperShadowActionPlaysSelfFromControlledSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var trooper = scn.GetShadowCard("trooper");
		var child = scn.GetShadowCard("child");
		scn.ShadowMoveCardToHand(child, trooper);

		scn.StartGame();

		scn.SkipToSite(2);

		scn.ShadowMoveCardToSupportArea(child);
		scn.AddTokensToCard(child, 2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(child); //should take control of site

		scn.SkipToSite(4);
		var controlledSite = scn.GetFreepsSite(1);
		assertTrue(scn.IsSiteControlled(controlledSite));
		scn.StackCardsOn(controlledSite, trooper);
		scn.SetTwilight(3);

		scn.FreepsPassCurrentPhaseAction();
		assertEquals(controlledSite, trooper.getStackedOn());
		assertEquals(Zone.STACKED, trooper.getZone());

		assertTrue(scn.ShadowActionAvailable(trooper));
		assertEquals(10, scn.GetTwilight());
		scn.ShadowUseCardAction(trooper);
		assertEquals(Zone.SHADOW_CHARACTERS, trooper.getZone());
		// Trooper cost 4, but should have a -1 discount
		assertEquals(7, scn.GetTwilight());
	}
}
