package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_234_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("comp2", "1_53");
					put("comp3", "1_53");
					put("comp4", "1_53");
					put("comp5", "1_53");
					put("comp6", "1_53");

					put("nertea", "1_234");
					put("runner", "1_178");
					put("twk", "2_85");
					put("attea", "1_229");
					put("rit", "101_40");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "3_118"); //The Great River
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Nertëa, Messenger of Dol Guldur
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: When you play Úlairë Nertëa, for each companion over 4, you may play 1 minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Messenger of Dol Guldur", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());

	}

	@Test
	public void NerteaDoesNotTriggerWith4Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4");

		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void NerteaPlays1MinionIf5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCardsToShadowDiscard("runner", "rit", "twk", "attea");
		scn.MoveCardsToDiscard(twk);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseYes();
		//twk, attea, rit, and runner
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertEquals(Zone.DISCARD, twk.getZone());
		scn.ShadowChooseCardBPFromSelection(twk);
		assertEquals(Zone.SHADOW_CHARACTERS, twk.getZone());

		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	@Test
	public void NerteaPlays2MinionsIf6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToShadowDiscard("rit", "twk", "attea");
		scn.MoveCardsToDiscard(twk, runner);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseYes();
		//twk, attea, rit, and runner
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertEquals(Zone.DISCARD, twk.getZone());
		scn.ShadowChooseCardBPFromSelection(twk);
		assertEquals(Zone.SHADOW_CHARACTERS, twk.getZone());

		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseYes();
		assertEquals(Zone.DISCARD, runner.getZone());
		scn.ShadowChooseCardBPFromSelection(runner);
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());

		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	@Test
	public void CancelingNerteaAfterFirstMinionDoesntAskAgain() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToShadowDiscard("rit", "twk", "attea");
		scn.MoveCardsToDiscard(twk, runner);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseNo();

		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	@Test
	public void NerteaStopsPlayingMinionsIfUserDeclinesPartwayThrough() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCardsToShadowDiscard("runner", "rit", "twk", "attea");
		scn.MoveCardsToDiscard(twk);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseYes();
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertEquals(Zone.DISCARD, twk.getZone());
		scn.ShadowChooseCardBPFromSelection(twk);
		assertEquals(Zone.SHADOW_CHARACTERS, twk.getZone());

		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		assertEquals(Zone.DISCARD, attea.getZone());
		scn.ShadowChooseNo();
		assertEquals(Zone.DISCARD, attea.getZone());

		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	@Test
	public void NerteaDoesNotPromptIfNoMinionsInDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCardsToShadowHand("rit", "twk", "attea", "runner");
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	// Converted from legacy AT test
	@Test
	public void NerteaCannotPlayFromDiscardAtTheGreatRiver() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.MoveCompanionsToTable("comp2", "comp3", "comp4", "comp5");

		var greatriver = scn.GetShadowSite(7);

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCardsToShadowDiscard("runner", "rit", "twk", "attea");
		scn.MoveCardsToDiscard(twk);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();

		scn.SkipToSite(6);
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(greatriver, scn.GetCurrentSite());

		scn.ShadowPlayCard(nertea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
