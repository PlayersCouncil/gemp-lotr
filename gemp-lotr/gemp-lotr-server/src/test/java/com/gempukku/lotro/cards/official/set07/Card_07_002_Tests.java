package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.PutOnTheOneRingEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_07_002_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					//Shadow wounding
					put("snuffler", "13_52");
					//Maneuver wounding
					put("picket", "6_101");
					//Archery wounding
					put("marksman", "1_176");
					//Skirmish wounding
					put("soldier", "1_271");
					//Regroup wounding
					put("gollum", "9_28");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				"7_2"
		);
	}

	@Test
	public void SuchAWeightToCarryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: The One Ring, Such a Weight to Carry
		 * Unique: True
		 * Side: 
		 * Culture: 
		 * Twilight Cost: 
		 * Type: Onering
		 * Subtype: 
		 * Strength: 2
		 * Game Text: <b>Maneuver:</b> Add a burden to wear The One Ring until the regroup phase.<br>While wearing The One Ring, each time the Ring-bearer is about to take a wound, add a burden instead.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetFreepsRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("Such a Weight to Carry", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void SuchAWeightToCarryConvertsWoundsDuringAnyPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();

		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		var picket = scn.GetShadowCard("picket");
		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		scn.ShadowMoveCharToTable(marksman, soldier, picket, gollum);
		scn.ShadowMoveCardToHand(snuffler);

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

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPassCurrentPhaseAction();

		//Shadow wounds can convert to burdens
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPassCurrentPhaseAction();

		//Maneuver wounds can convert to burdens
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		//Archery wounds can convert to burdens
		assertTrue(scn.RBWearingOneRing());
		scn.PassCurrentPhaseActions();

		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		//Assignment phase
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(frodo, soldier);

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(soldier);

		//We are now in a skirmish phase, so the burden should have gotten converted
		assertTrue(scn.RBWearingOneRing());
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		//Regular skirmish wound converted to burden
		assertEquals(6, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		//Got taken off at the start of the regroup phase
		assertFalse(scn.RBWearingOneRing());
		scn.FreepsUseCardAction(frodo);
		assertTrue(scn.RBWearingOneRing());

		//Regroup wounds cannot convert to burdens
		scn.ShadowChooseAction("wound"); //We want DaD's wounding action, not his 3 twilight action
		assertEquals(7, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void SuchAWeightToCarryManeuverAbilityAddsABurdenToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();

		var soldier = scn.GetShadowCard("soldier");
		scn.ShadowMoveCharToTable(soldier);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.RBWearingOneRing());
	}
}
