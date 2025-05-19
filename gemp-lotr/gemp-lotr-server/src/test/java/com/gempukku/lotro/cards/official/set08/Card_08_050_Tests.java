package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blacksails", "8_50");
					put("corsair", "8_53");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackSailsofUmbarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Black Sails of Umbar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: When you play this possession, you may add a [raider] token here.<br><b>Shadow:</b> Remove X [raider] tokens here to play a corsair from your discard pile; its twilight cost is -X. Discard this possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blacksails");

		assertEquals("Black Sails of Umbar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BlackSailsOnPlayAdds1Token() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		scn.MoveCardsToHand(blacksails);

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(blacksails));
		scn.ShadowPlayCard(blacksails);

		assertEquals(0, scn.GetCultureTokensOn(blacksails));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(blacksails));
	}

	@Test
	public void BlackSailsAbilityCannotActivateIfNoCorsairInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToTopOfDeck(corsair);

		scn.StartGame();
		scn.SetTwilight(50);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(Zone.DECK, corsair.getZone());
		assertFalse(scn.ShadowActionAvailable(blacksails));
	}

	@Test
	public void BlackSailsAbilityCanRemove0TokensToDiscount0() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToDiscard(corsair);

		scn.StartGame();
		scn.SetTwilight(47);
		scn.AddTokensToCard(blacksails, 5);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, corsair.getZone());
		assertTrue(scn.ShadowActionAvailable(blacksails));
		scn.ShadowUseCardAction(blacksails);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("0");

		assertEquals(Zone.SHADOW_CHARACTERS, corsair.getZone());
		//Buccaneer costs 3 + 2 for roaming, with no discount = 5
		assertEquals(45, scn.GetTwilight());
	}

	@Test
	public void BlackSailsAbilityCanRemove1TokensToDiscount1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToDiscard(corsair);

		scn.StartGame();
		scn.SetTwilight(47);
		scn.AddTokensToCard(blacksails, 5);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, corsair.getZone());
		assertTrue(scn.ShadowActionAvailable(blacksails));
		scn.ShadowUseCardAction(blacksails);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("1");

		assertEquals(Zone.SHADOW_CHARACTERS, corsair.getZone());
		//Buccaneer costs 3 + 2 for roaming, with a -1 discount = 4
		assertEquals(46, scn.GetTwilight());
	}

	@Test
	public void BlackSailsAbilityCanRemove2TokensToDiscount2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToDiscard(corsair);

		scn.StartGame();
		scn.SetTwilight(47);
		scn.AddTokensToCard(blacksails, 5);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, corsair.getZone());
		assertTrue(scn.ShadowActionAvailable(blacksails));
		scn.ShadowUseCardAction(blacksails);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("2");

		assertEquals(Zone.SHADOW_CHARACTERS, corsair.getZone());
		//Buccaneer costs 3 + 2 for roaming, with a -2 discount = 3
		assertEquals(47, scn.GetTwilight());
	}

	@Test
	public void BlackSailsAbilityCanRemove3TokensToDiscount3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToDiscard(corsair);

		scn.StartGame();
		scn.SetTwilight(47);
		scn.AddTokensToCard(blacksails, 5);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, corsair.getZone());
		assertTrue(scn.ShadowActionAvailable(blacksails));
		scn.ShadowUseCardAction(blacksails);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("3");

		assertEquals(Zone.SHADOW_CHARACTERS, corsair.getZone());
		//Buccaneer costs 3 + 2 for roaming, with a -3 discount = 2
		assertEquals(48, scn.GetTwilight());
	}

	@Test
	public void BlackSailsAbilityRemovingTooManyTokensCapsAtZeroCost() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToDiscard(corsair);

		scn.StartGame();
		scn.SetTwilight(47);
		scn.AddTokensToCard(blacksails, 5);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, corsair.getZone());
		assertTrue(scn.ShadowActionAvailable(blacksails));
		scn.ShadowUseCardAction(blacksails);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("5");

		assertEquals(Zone.SHADOW_CHARACTERS, corsair.getZone());
		//Buccaneer costs 3 + 2 for roaming, with a -3 discount = 2, even tho 5 was removed
		assertEquals(48, scn.GetTwilight());
	}

	@Test
	public void BlackSailsAbilityUsableWhenDiscountRequired() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToDiscard(corsair);

		scn.StartGame();
		scn.AddTokensToCard(blacksails, 5);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(3, scn.GetTwilight());
		assertEquals(Zone.DISCARD, corsair.getZone());
		assertTrue(scn.ShadowActionAvailable(blacksails));
		scn.ShadowUseCardAction(blacksails);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("2");

		assertEquals(Zone.SHADOW_CHARACTERS, corsair.getZone());
		//Buccaneer costs 3 + 2 for roaming, with a -2 discount = 3
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void BlackSailsAbilityUsableWhenPlayerFumblesAmounts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blacksails = scn.GetShadowCard("blacksails");
		var corsair = scn.GetShadowCard("corsair");
		scn.MoveCardsToSupportArea(blacksails);
		scn.MoveCardsToDiscard(corsair);

		scn.StartGame();
		scn.AddTokensToCard(blacksails, 5);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(3, scn.GetTwilight());
		assertEquals(Zone.DISCARD, corsair.getZone());
		assertTrue(scn.ShadowActionAvailable(blacksails));
		scn.ShadowUseCardAction(blacksails);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("1");

		assertEquals(Zone.DISCARD, corsair.getZone());
		//Buccaneer costs 3 + 2 for roaming, with a -1 discount = 4....which is not enough to have played the buccaneer.
		assertEquals(3, scn.GetTwilight());
	}
}
