package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.TakeOffTheOneRingEffect;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_V2_038_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("balrog", "102_38");
					put("runner", "1_178");
					put("drums", "1_168");
					put("dark", "1_188");

					put("sting", "5_116");
					put("severed", "4_319");
					put("companion1", "3_7");
					put("companion2", "3_122");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331"); //Ettenmoors for the site ability
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.ATARRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: The Balrog, Lieutenant of Morgoth
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 14
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: Fierce. Damage +1.
		* 	Each time a minion takes a wound, you may remove (1) to add a threat.
		* 	Each time the Free Peoples player uses a skirmish special ability during a skirmish involving the Balrog, you may wound a companion it is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balrog");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("Lieutenant of Morgoth", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(14, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void BalrogRemoves1ToAddThreatEachTimeAnyMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(balrog, runner);

		//Added to increase threat threshold to 3
		scn.FreepsMoveCharToTable("companion1");
		scn.FreepsMoveCharToTable("companion2");

		//cheating to add fellowship archery
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, null, 3));

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(2);

		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());

		//Wounding Balrog triggers ability
		scn.ShadowChooseCard(balrog);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(balrog));

		//Wounding something other than the balrog triggers ability
		scn.ShadowChooseCard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertEquals(Zone.DISCARD, runner.getZone());

		//Third arrow wounding without enough twilight causes ability to not trigger
		assertEquals(2, scn.GetWoundsOn(balrog));
	}

	@Test
	public void BalrogWoundsOpponentWhenFreepsUsesSpecialAbilityFromAnySource() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var drums = scn.GetShadowCard("drums");
		var dark = scn.GetShadowCard("dark");
		scn.ShadowMoveCharToTable(balrog);
		scn.ShadowMoveCardToSupportArea(dark);
		scn.ShadowMoveCardToHand(drums);

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsRing();
		var sting = scn.GetFreepsCard("sting");
		var severed = scn.GetFreepsCard("severed");
		scn.AttachCardsTo(frodo, sting);
		scn.FreepsMoveCardToHand(severed);

		var ettenmoors = scn.GetShadowSite(2);

		scn.StartGame();

		//Cheat and add an ability to Frodo which takes off the One Ring
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo, AbstractAtTest.P1);
				action.appendEffect(new TakeOffTheOneRingEffect());
				return Collections.singletonList(action);
			}
		});

		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(frodo, balrog);
		scn.FreepsResolveSkirmish(frodo);

		//5 skirmish action options: Ring, Sting, Site, Event (and cheated ring-removal ability).
		// The first 3 should trigger the Balrog's ability, the event should not.
		assertEquals(5, scn.GetFreepsCardChoiceCount());
		assertEquals(0, scn.GetWoundsOn(frodo));

		//Regular Free Peoples ability
		scn.FreepsUseCardAction(sting);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo)); //1 exertion from sting, 1 wound from balrog
		scn.FreepsChoose("0"); //Have to pick whether to buff smeagol or debuff gollum

		//One Ring ability used by the Free Peoples player
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(frodo));

		//Cheat and take the Ring off so we don't have to count burdens
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(frodo);
		scn.ShadowDeclineOptionalTrigger(); //Ironically, the balrog's ability responds to our cheating

		//Site ability used by the Free Peoples player
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(ettenmoors);
		assertEquals(4, scn.GetWoundsOn(frodo)); // 1 exertion from site
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(5, scn.GetWoundsOn(frodo)); //1 exertion from site, 1 wound from balrog

		//Free Peoples event should not trigger
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPlayCard(severed);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(5, scn.GetWoundsOn(frodo));

		//None of the equivalent Shadow abilities should activate the Balrog's ability
		//Shadow ability
		scn.ShadowUseCardAction(dark);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(5, scn.GetWoundsOn(frodo));

		//Shadow use of site
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(ettenmoors);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(5, scn.GetWoundsOn(frodo));

		//Shadow event
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(drums);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(5, scn.GetWoundsOn(frodo));
	}
}
