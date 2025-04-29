package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_V2_052_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("ruin", "102_52");
					put("eowyn", "13_124");
					put("vhaldir", "102_8");
					put("vgimli", "55_7");

					put("arrowslits", "5_80");
					put("possession", "102_48");
					put("maneuver", "7_238");
					put("archery", "18_101");
					put("skirmish", "4_273");
					put("regroup", "12_114");

					put("runner", "1_178");
					put("sauron", "9_48");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NowforRuinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Now for Ruin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a minion is killed, spot 2 valiant companions to add a [rohan] token here.
		* 	Each time there are 3 [rohan] tokens here, remove all tokens here to play a [rohan] condition or event from your discard pile. The Shadow player may then play a minion from their discard pile. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruin");

		assertEquals("Now for Ruin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NowforRuinAddsNoTokenToSelfWhenMinionKilledIf0ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		scn.FreepsMoveCardToSupportArea(ruin);

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		//Cheating and adding an all-phases action on frodo to wound the Goblin Runner directly
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new WoundCharactersEffect(frodo, runner));
				action.setText("Wound Runner");
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		scn.FreepsUseCardAction(frodo);

		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(0, scn.GetCultureTokensOn(ruin));
	}

	@Test
	public void NowforRuinAddsNoTokenToSelfWhenMinionKilledIf1ValiantCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCharToTable("vhaldir");

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		//Cheating and adding an all-phases action on frodo to wound the Goblin Runner directly
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new WoundCharactersEffect(frodo, runner));
				action.setText("Wound Runner");
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		scn.FreepsUseCardAction(frodo);

		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(0, scn.GetCultureTokensOn(ruin));
	}

	@Test
	public void NowforRuinAdds1TokenToSelfWhenMinionKilledIf2ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCharToTable("vhaldir", "vgimli");

		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(runner);

		//Cheating and adding an all-phases action on frodo to wound the Goblin Runner directly
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new WoundCharactersEffect(frodo, runner));
				action.setText("Wound Runner");
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		scn.FreepsUseCardAction(frodo);

		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(1, scn.GetCultureTokensOn(ruin));
	}


	@Test
	public void NowforRuinPermitsPlayOfEitherRohanConditionOrManeuverEventAndShadowMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var maneuver = scn.GetFreepsCard("maneuver");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCharToTable("vhaldir", "vgimli");
		scn.FreepsMoveCardToDiscard(arrowslits, maneuver);

		scn.AddTokensToCard(ruin, 2);

		var runner = scn.GetShadowCard("runner");
		var sauron = scn.GetShadowCard("sauron");
		scn.ShadowMoveCharToTable(runner);
		scn.ShadowMoveCardToDiscard(sauron);

		//Cheating and adding an all-phases action on frodo to wound the Goblin Runner directly
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new WoundCharactersEffect(frodo, runner));
				action.setText("Wound Runner");
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();
		scn.SetTwilight(20);

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.DISCARD, sauron.getZone());
		assertEquals(Zone.DISCARD, arrowslits.getZone());
		assertEquals(Zone.DISCARD, maneuver.getZone());
		assertEquals(2, scn.GetCultureTokensOn(ruin));
		scn.FreepsUseCardAction(frodo);

		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(arrowslits, maneuver));
		scn.FreepsChooseCard(arrowslits);
		assertEquals(Zone.SUPPORT, arrowslits.getZone());

		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoicesAvailable(runner, sauron));
		scn.ShadowChooseCard(sauron);
		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, sauron.getZone());
	}

	@Test
	public void NowforRuinPermitsPlayOfEitherRohanConditionOrArcheryEventAndShadowMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var archery = scn.GetFreepsCard("archery");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCharToTable("vhaldir", "vgimli");
		scn.FreepsMoveCardToSupportArea("possession"); //needed prereq for the archery event
		scn.FreepsMoveCardToDiscard(arrowslits, archery);

		scn.AddTokensToCard(ruin, 2);

		var runner = scn.GetShadowCard("runner");
		var sauron = scn.GetShadowCard("sauron");
		scn.ShadowMoveCharToTable(runner);
		scn.ShadowMoveCardToDiscard(sauron);

		//Cheating and adding an all-phases action on frodo to wound the Goblin Runner directly
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new WoundCharactersEffect(frodo, runner));
				action.setText("Wound Runner");
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();
		scn.SetTwilight(20);

		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.DISCARD, sauron.getZone());
		assertEquals(Zone.DISCARD, arrowslits.getZone());
		assertEquals(Zone.DISCARD, archery.getZone());
		assertEquals(2, scn.GetCultureTokensOn(ruin));
		scn.FreepsUseCardAction(frodo);

		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(arrowslits, archery));
		scn.FreepsChooseCard(arrowslits);
		assertEquals(Zone.SUPPORT, arrowslits.getZone());

		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoicesAvailable(runner, sauron));
		scn.ShadowChooseCard(sauron);
		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, sauron.getZone());
	}

	@Test
	public void NowforRuinPermitsPlayOfEitherRohanConditionOrSkirmishEventAndShadowMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var skirmish = scn.GetFreepsCard("skirmish");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCharToTable("vhaldir", "vgimli", "eowyn");
		scn.FreepsMoveCardToDiscard(arrowslits, skirmish);

		scn.AddTokensToCard(ruin, 2);

		var runner = scn.GetShadowCard("runner");
		var sauron = scn.GetShadowCard("sauron");
		scn.ShadowMoveCharToTable(runner);
		scn.ShadowMoveCardToDiscard(sauron);

		//Cheating and adding an all-phases action on frodo to wound the Goblin Runner directly
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new WoundCharactersEffect(frodo, runner));
				action.setText("Wound Runner");
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();
		scn.SetTwilight(20);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.DISCARD, sauron.getZone());
		assertEquals(Zone.DISCARD, arrowslits.getZone());
		assertEquals(Zone.DISCARD, skirmish.getZone());
		assertEquals(2, scn.GetCultureTokensOn(ruin));
		scn.FreepsUseCardAction(frodo);

		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(arrowslits, skirmish));
		scn.FreepsChooseCard(arrowslits);
		assertEquals(Zone.SUPPORT, arrowslits.getZone());

		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoicesAvailable(runner, sauron));
		scn.ShadowChooseCard(sauron);
		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, sauron.getZone());
	}

	@Test
	public void NowforRuinPermitsPlayOfEitherRohanConditionOrRegroupEventAndShadowMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		var regroup = scn.GetFreepsCard("regroup");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCharToTable("vhaldir", "vgimli", "eowyn");
		scn.FreepsMoveCardToDiscard(arrowslits, regroup);

		scn.AddTokensToCard(ruin, 2);

		var runner = scn.GetShadowCard("runner");
		var sauron = scn.GetShadowCard("sauron");
		scn.ShadowMoveCharToTable(runner);
		scn.ShadowMoveCardToDiscard(sauron);

		//Cheating and adding an all-phases action on frodo to wound the Goblin Runner directly
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new WoundCharactersEffect(frodo, runner));
				action.setText("Wound Runner");
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();
		scn.SetTwilight(20);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.DISCARD, sauron.getZone());
		assertEquals(Zone.DISCARD, arrowslits.getZone());
		assertEquals(Zone.DISCARD, regroup.getZone());
		assertEquals(2, scn.GetCultureTokensOn(ruin));
		scn.FreepsUseCardAction(frodo);

		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoicesAvailable(arrowslits, regroup));
		scn.FreepsChooseCard(arrowslits);
		assertEquals(Zone.SUPPORT, arrowslits.getZone());

		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoicesAvailable(runner, sauron));
		scn.ShadowChooseCard(sauron);
		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, sauron.getZone());
	}

	@Test
	public void NowforRuinTriggersOnReinforceOfSecondToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var arrowslits = scn.GetFreepsCard("arrowslits");
		scn.FreepsMoveCardToSupportArea(ruin);
		scn.FreepsMoveCardToDiscard(arrowslits);
		scn.FreepsMoveCharToTable("eowyn"); // On a regroup move, reinforces a Rohan token

		scn.StartGame();
		scn.AddTokensToCard(ruin, 2);
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCultureTokensOn(ruin));
		assertEquals(Zone.DISCARD, arrowslits.getZone());
		scn.FreepsChooseToMove();
		scn.FreepsAcceptOptionalTrigger(); //Eowyn's reinforcement
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertEquals(Zone.SUPPORT, arrowslits.getZone());
	}
}
