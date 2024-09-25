package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.effects.PutOnTheOneRingEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_04_001_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("marksman", "1_176");
					put("soldier", "1_271");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				"4_1"
		);
	}

	@Test
	public void AnswerToAllRiddlesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: The One Ring, Answer To All Riddles
		 * Unique: True
		 * Side: 
		 * Culture: 
		 * Twilight Cost: 
		 * Type: The One Ring
		 * Subtype: 
		 * Vitality: 2
		 * Game Text: While wearing The One Ring, the Ring-bearer is strength +2, and each time he is about to take
		 * a wound in a skirmish, add a burden instead.
		 * <b>Skirmish:</b> Add a burden to wear The One Ring until the regroup phase.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetFreepsRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("Answer To All Riddles", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void AnswerToAllRiddlesPumpsRBPlus2WhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();

		//Cheat and add an ability to Frodo which puts on the One Ring
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo);
				action.appendEffect(new PutOnTheOneRingEffect());
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();

		assertEquals(3, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(frodo);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(5, scn.GetStrength(frodo));
	}

	@Test
	public void AnswerToAllRiddlesConvertsWoundsDuringSkirmishPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();

		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		scn.ShadowMoveCharToTable(marksman, soldier);

		//Cheat and add an ability to Frodo which puts on the One Ring
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo);
				action.appendEffect(new PutOnTheOneRingEffect());
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();

		scn.FreepsUseCardAction(frodo);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.RBWearingOneRing());

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		//Assigned archery wounds
		assertEquals(1, scn.GetBurdens());
		//No automatic burden conversion, as we are not in the skirmish phase
		assertEquals(1, scn.GetWoundsOn(frodo));

		//Assignment phase
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(frodo, soldier);

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(soldier);

		//We are now in a skirmish phase, so the burden should have gotten converted
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		//Regular skirmish wound converted to burden
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void AnswerToAllRiddlesSkirmishAbilityAddsABurdenToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();

		var soldier = scn.GetShadowCard("soldier");
		scn.ShadowMoveCharToTable(soldier);

		scn.StartGame();

		//Cheat and make orcs fierce
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.changeToFilter(Race.ORC), null, Keyword.FIERCE));

		scn.SkipToAssignments();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		//We are now in a skirmish phase, so the burden should have gotten converted
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		//Fierce skirmish
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.RBWearingOneRing());

		scn.PassCurrentPhaseActions();
		assertEquals(4, scn.GetBurdens()); // 1 more from losing skirmish
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.RBWearingOneRing());
	}
}
