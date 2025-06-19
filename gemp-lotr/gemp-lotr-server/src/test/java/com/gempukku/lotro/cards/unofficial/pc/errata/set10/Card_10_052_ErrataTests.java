package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_052_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("underfoot", "60_52");
					put("raider", "4_249");

					put("chaff1", "4_250");
					put("chaff2", "4_251");
					put("chaff3", "4_252");
					put("chaff4", "4_253");
					put("chaff5", "4_254");
					put("chaff6", "4_255");
					put("chaff7", "4_256");
					put("chaff8", "4_257");
					put("chaff9", "4_258");
					put("chaff0", "4_259");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnderFootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 10
		* Title: Under Foot
		* Unique: True
		* Side: SHADOW
		* Culture: Raider
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: <b>Shadow:</b> If you have initiative, spot a [raider] Man to reconcile your hand (limit once per phase). At the start of the regroup phase, discard this condition. 
		* 	<b>Skirmish:</b> Discard this condition to make a [raider] Man strength +2.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var underfoot = scn.GetFreepsCard("underfoot");

		assertTrue(underfoot.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, underfoot.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, underfoot.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, underfoot.getBlueprint().getCardType());;
		assertTrue(scn.HasKeyword(underfoot, Keyword.SUPPORT_AREA));
		assertEquals(1, underfoot.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityWorksOnlyOnceAndSelfDiscardsDuringRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var underfoot = scn.GetShadowCard("underfoot");
		var raider = scn.GetShadowCard("raider");
		scn.MoveCardsToHand(raider);
		scn.MoveCardsToSupportArea(underfoot);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable(underfoot));
		assertTrue(scn.ShadowHasInitiative());
		scn.ShadowPlayCard(raider);
		assertTrue(scn.ShadowActionAvailable(underfoot));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(10, scn.GetShadowDeckCount());

		scn.ShadowUseCardAction(underfoot);
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());

		//limit once per phase
		assertFalse(scn.ShadowActionAvailable(underfoot));

		scn.MoveCardsToDiscard(raider);
		assertEquals(Zone.SUPPORT, underfoot.getZone());
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(Zone.DISCARD, underfoot.getZone());
	}

	@Test
	public void SkirmishAbilitySelfDiscardsToPumpRaiderMan2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var underfoot = scn.GetShadowCard("underfoot");
		var raider = scn.GetShadowCard("raider");
		scn.MoveMinionsToTable(raider);
		scn.MoveCardsToSupportArea(underfoot);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(frodo, raider);
		scn.ShadowDeclineOptionalTrigger(); //ambush
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(underfoot));
		assertEquals(10, scn.GetStrength(raider));
		assertEquals(Zone.SUPPORT, underfoot.getZone());
		scn.ShadowUseCardAction(underfoot);
		assertEquals(12, scn.GetStrength(raider));
		assertEquals(Zone.DISCARD, underfoot.getZone());
	}
}
