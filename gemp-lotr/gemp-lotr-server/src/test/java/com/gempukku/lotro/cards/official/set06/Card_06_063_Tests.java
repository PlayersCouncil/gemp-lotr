package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_063_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("gnawing", "6_63");
					put("artisan", "6_65");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GnawingBitingHackingBurningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gnawing, Biting, Hacking, Burning
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each time a regroup action discards an [isengard] Orc, you may stack that Orc on this card.<br><b>Shadow:</b> Discard 2 cards stacked here and remove (1) to play an [isengard] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gnawing");

		assertEquals("Gnawing, Biting, Hacking, Burning", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GnawingBitingHackingBurningCatchesRegroupActionDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		var artisan = scn.GetShadowCard("artisan");
		scn.ShadowMoveCharToTable(artisan);
		scn.ShadowMoveCardToSupportArea(gnawing);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(artisan));
		scn.ShadowUseCardAction(artisan);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.STACKED, artisan.getZone());
		assertEquals(gnawing, artisan.getStackedOn());
	}

	@Test
	public void GnawingBitingHackingBurningDoesNotCatchTurnEndDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		var artisan = scn.GetShadowCard("artisan");
		scn.ShadowMoveCharToTable(artisan);
		scn.ShadowMoveCardToSupportArea(gnawing);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(Phase.FELLOWSHIP, scn.getPhase());
	}
}
