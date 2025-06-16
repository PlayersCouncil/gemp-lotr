package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("darkwaters", "101_34");
					put("tentacle1", "2_58");
					put("tentacle2", "2_66");
					put("song", "3_5");
					put("greenleaf", "1_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OutofDarkWatersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Out of Dark Waters
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a tentacle takes a wound, you may stack that tentacle here.
		* 	Shadow: Play a tentacle from here as if from hand.
		* 	Response: If this condition is about to be discarded, discard 2 tentacles stacked here to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("darkwaters");

		assertEquals("Out of Dark Waters", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DarkWatersStacksWoundedTentacles() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var darkwaters = scn.GetShadowCard("darkwaters");
		var tentacle2 = scn.GetShadowCard("tentacle2");
		scn.MoveCardsToSupportArea(darkwaters);
		scn.MoveCardsToHand(tentacle2);

		scn.MoveCompanionToTable("greenleaf");

		scn.StartGame();
		scn.SetTwilight(4);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(tentacle2);

		assertEquals(0, scn.GetWoundsOn(tentacle2));
		assertEquals(Zone.SHADOW_CHARACTERS, tentacle2.getZone());

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(tentacle2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(Zone.STACKED, tentacle2.getZone());
		assertEquals(darkwaters, tentacle2.getStackedOn());
	}

	@Test
	public void DarkWatersStacksTentaclesKilledByWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var darkwaters = scn.GetShadowCard("darkwaters");
		var tentacle1 = scn.GetShadowCard("tentacle1");
		scn.MoveCardsToSupportArea(darkwaters);
		scn.MoveCardsToHand(tentacle1);

		scn.MoveCompanionToTable("greenleaf");

		scn.StartGame();
		scn.SetTwilight(4);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.siteNumber(2), null, Keyword.MARSH));
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(tentacle1);

		assertEquals(Zone.SHADOW_CHARACTERS, tentacle1.getZone());

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(Zone.STACKED, tentacle1.getZone());
		assertEquals(darkwaters, tentacle1.getStackedOn());
	}


	@Test
	public void DarkWatersCanBurn2AStackedTentaclesToPreventSelfDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		scn.MoveCardsToSupportArea(song);

		var darkwaters = scn.GetShadowCard("darkwaters");
		var tentacle1 = scn.GetShadowCard("tentacle1");
		var tentacle2 = scn.GetShadowCard("tentacle2");
		scn.MoveCardsToSupportArea(darkwaters);
		//scn.MoveCardsToHand(tentacle1, tentacle2);

		scn.StartGame();
		scn.StackCardsOn(darkwaters, tentacle1, tentacle2);

		scn.FreepsUseCardAction(song);
		scn.FreepsChooseCard(darkwaters);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.DISCARD, tentacle1.getZone());
		assertEquals(Zone.DISCARD, tentacle2.getZone());
		assertEquals(Zone.SUPPORT, darkwaters.getZone());

	}
}
