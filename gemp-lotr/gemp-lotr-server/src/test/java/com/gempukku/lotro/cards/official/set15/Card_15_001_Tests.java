package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
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

public class Card_15_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
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
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"15_1"
		);
	}

	@Test
	public void RingOfDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: The One Ring, The Ring of Doom
		 * Unique: True
		 * Side: 
		 * Culture: 
		 * Twilight Cost: 
		 * Type: Onering
		 * Subtype: 
		 * Vitality: 2
		 * Game Text: <span style="word-spacing:-0.02em;font-size:99%">While wearing The One Ring, the Ring-bearer</span> <span style="font-size:99%">gains <b>hunter 3</b>, and each time he or she is about to take a wound in a skirmish, add a burden instead.</span><br><b>Skirmish:</b> Add a burden to wear The One Ring until the regroup phase.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("The Ring of Doom", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void RingOfDoomGrantsRBHunter3WhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		//Cheat and add an ability to Frodo which puts on the One Ring
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo, VirtualTableScenario.P1);
				action.appendEffect(new PutOnTheOneRingEffect());
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();

		assertEquals(6, scn.GetVitality(frodo));
		assertEquals(3, scn.GetStrength(frodo));
		scn.FreepsUseCardAction(frodo);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(frodo, Keyword.HUNTER));
	}

	@Test
	public void RingOfDoomConvertsWoundsOnlyDuringSkirmishPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		var picket = scn.GetShadowCard("picket");
		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveMinionsToTable(marksman, soldier, picket, gollum);
		scn.MoveCardsToHand(snuffler);

		//Cheat and add an ability to Frodo which puts on the One Ring
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo, VirtualTableScenario.P1);
				action.appendEffect(new PutOnTheOneRingEffect());
				return Collections.singletonList(action);
			}
		});

		scn.StartGame();

		scn.FreepsUseCardAction(frodo);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPassCurrentPhaseAction();

		//Shadow wounds cannot convert to burdens
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.ShadowPassCurrentPhaseAction();

		//Maneuver wounds cannot convert to burdens
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		//Archery wounds cannot convert to burdens
		assertTrue(scn.RBWearingOneRing());
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));

		//Assignment phase
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(frodo, soldier);

		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(soldier);

		//We are now in a skirmish phase, so the burden should have gotten converted
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));

		scn.PassCurrentPhaseActions();

		//Regular skirmish wound converted to burden
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		//Got taken off at the start of the regroup phase
		assertFalse(scn.RBWearingOneRing());
		scn.FreepsUseCardAction(frodo);
		assertTrue(scn.RBWearingOneRing());

		//Regroup wounds cannot convert to burdens
		scn.ShadowChooseAction("wound"); //We want DaD's wounding action, not his 3 twilight action
		assertEquals(3, scn.GetBurdens());
		assertEquals(4, scn.GetWoundsOn(frodo));
	}

	@Test
	public void RingOfDoomSkirmishAbilityAddsABurdenToWearRingUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		var soldier = scn.GetShadowCard("soldier");
		scn.MoveMinionsToTable(soldier);

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
