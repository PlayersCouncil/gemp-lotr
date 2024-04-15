package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_234_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
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
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Title: Ulaire Nertea, Messenger of Dol Guldur
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgul
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

		scn.FreepsMoveCharToTable("comp2", "comp3", "comp4");

		var nertea = scn.GetShadowCard("nertea");
		scn.ShadowMoveCardToHand(nertea);

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

		scn.FreepsMoveCharToTable("comp2", "comp3", "comp4", "comp5");

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		scn.ShadowMoveCardToDiscard("runner", "rit", "twk", "attea");
		scn.ShadowMoveCardToDiscard(twk);
		scn.ShadowMoveCardToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseYes();
		//twk, attea, rit, and runner
		assertEquals(4, scn.GetShadowCardChoiceCount());
		assertEquals(Zone.DISCARD, twk.getZone());
		scn.ShadowChooseCardBPFromSelection(twk);
		assertEquals(Zone.SHADOW_CHARACTERS, twk.getZone());

		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	@Test
	public void NerteaPlays2MinionsIf6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.FreepsMoveCharToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToDiscard("rit", "twk", "attea");
		scn.ShadowMoveCardToDiscard(twk, runner);
		scn.ShadowMoveCardToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseYes();
		//twk, attea, rit, and runner
		assertEquals(4, scn.GetShadowCardChoiceCount());
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

		scn.FreepsMoveCharToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var twk = scn.GetShadowCard("twk");
		var attea = scn.GetShadowCard("attea");
		var nertea = scn.GetShadowCard("nertea");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCardToDiscard("rit", "twk", "attea");
		scn.ShadowMoveCardToDiscard(twk, runner);
		scn.ShadowMoveCardToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertTrue(scn.ShadowDecisionAvailable("play a minion"));
		scn.ShadowChooseNo();

		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	@Test
	public void NerteaDoesNotPromptIfNoMinionsInDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		scn.FreepsMoveCharToTable("comp2", "comp3", "comp4", "comp5", "comp6");

		var nertea = scn.GetShadowCard("nertea");
		scn.ShadowMoveCardToHand("rit", "twk", "attea", "runner");
		scn.ShadowMoveCardToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(nertea);
		assertFalse(scn.ShadowDecisionAvailable("play a minion"));
	}

	//Imported from the at tests
	@Test
	public void ulaireNerteaCantPlayMinionsOnGreatRiver() throws Exception {
		var scn = GetScenario();
		var _game = scn._game;

		for (int i=0; i<4; i++) {
			final PhysicalCardImpl pippin = scn.createCard(scn.P1, "1_306");
			_game.getGameState().addCardToZone(_game, pippin, Zone.FREE_CHARACTERS);
		}

		scn.skipMulligans();

		final PhysicalCardImpl greatRiver = scn.createCard(scn.P2, "3_118");
		greatRiver.setSiteNumber(2);
		_game.getGameState().addCardToZone(_game, greatRiver, Zone.ADVENTURE_PATH);

		final PhysicalCardImpl ulaireNertea = scn.createCard(scn.P2, "1_234");
		_game.getGameState().addCardToZone(_game, ulaireNertea, Zone.HAND);

		final PhysicalCardImpl goblinRunner = scn.createCard(scn.P2, "1_178");
		_game.getGameState().addCardToZone(_game, goblinRunner, Zone.DISCARD);

		_game.getGameState().setTwilight(20);

		// Fellowship phase
		scn.playerDecided(scn.P1, "");

		assertEquals(greatRiver, _game.getGameState().getCurrentSite());

		scn.playerDecided(scn.P2, scn.getCardActionId(scn.P2, "Play Úlairë Nertëa"));

		assertFalse(scn._userFeedback.getAwaitingDecision(scn.P2).getDecisionType() == AwaitingDecisionType.MULTIPLE_CHOICE);
	}
}
