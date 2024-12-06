package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.PutOnTheOneRingEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_01_001_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("sword", "1_299");
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
				"1_1"
		);
	}

	@Test
	public void IsildursBaneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The One Ring, Isildur's Bane
		 * Unique: True
		 * Side: 
		 * Culture: 
		 * Twilight Cost: 
		 * Type: Onering
		 * Subtype: 
		 * Strength: 1
		 * Vitality: 1
		 * Game Text: <b>Response:</b> If bearer is about to take a wound, he wears The One Ring until the regroup phase.<br>While wearing The One Ring, each time the Ring-bearer is about to take a wound, add 2 burdens instead.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetFreepsRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("Isildur's Bane", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsildursBaneConvertsWoundsTo2BurdensDuringAnyPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();

		scn.FreepsAttachCardsTo(frodo, "sword");

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
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowPassCurrentPhaseAction();

		//Maneuver wounds can convert to burdens
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		//Archery wounds can convert to burdens
		assertTrue(scn.RBWearingOneRing());
		scn.PassCurrentPhaseActions();

		assertEquals(7, scn.GetBurdens());
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
		assertEquals(9, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		//Have to cheat because we're out of resistance haha
		scn.RemoveBurdens(9);

		scn.PassCurrentPhaseActions();

		//Regular skirmish wound converted to burden
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		//Got taken off at the start of the regroup phase
		assertFalse(scn.RBWearingOneRing());
		scn.FreepsUseCardAction(frodo);
		assertTrue(scn.RBWearingOneRing());

		//Regroup wounds cannot convert to burdens
		scn.ShadowChooseAction("wound"); //We want DaD's wounding action, not his 3 twilight action
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
	}

	@Test
	public void IsildursBaneRespondsToAnyWoundToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();

		var picket = scn.GetShadowCard("picket");
		scn.ShadowMoveCharToTable(picket);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.RBWearingOneRing());

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineOptionalTrigger(); //Declining the Picket's culture selection.

		//One Ring response
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.RBWearingOneRing());
	}
}
