package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_091_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("spies", "52_91");
					put("troop", "101_48");
					put("goblinarcher", "1_176");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.IsildursBaneRing
		);
	}

	@Test
	public void SouthernSpiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: Southern Spies
		* Unique: False
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 0
		* Type: event
		* Subtype: Response
		* Game Text: If the Ring-bearer puts on The One Ring, spot a [sauron] minion to add a burden for each card
		 * in the Free Peoples player's hand. The Free Peoples player may discard their hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var spies = scn.GetFreepsCard("spies");

		assertFalse(spies.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spies.getBlueprint().getSide());
		assertEquals(Culture.SAURON, spies.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, spies.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(spies, Timeword.RESPONSE));
		assertEquals(0, spies.getBlueprint().getTwilightCost());
	}

	@Test
	public void PuttingOnRingWithNoSauronMinionDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var spies = scn.GetShadowCard("spies");
		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.ShadowMoveCardToHand(spies);
		scn.ShadowMoveCharToTable(goblinarcher);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable()); // IB One Ring
		scn.FreepsAcceptOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable()); // Should have no response from Shadow
	}

	@Test
	public void PuttingOnRingWithSauronMinionAdds1BurdenPerCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var spies = scn.GetShadowCard("spies");
		var troop = scn.GetShadowCard("troop");
		scn.ShadowMoveCardToHand(spies);
		scn.ShadowMoveCharToTable(troop);

		scn.StartGame();

		//need cards in hand
		scn.FreepsDrawCards(3);
		//troop needs an exhausted comp to be an archer
		scn.AddWoundsToChar(frodo, 4);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable()); // IB One Ring
		assertEquals(1, scn.GetBurdens()); // 1 for bid
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsAcceptOptionalTrigger();
		scn.ShadowAcceptOptionalTrigger();

		//Freeps decline prevention
		scn.FreepsChooseNo();
		assertEquals(6, scn.GetBurdens()); // 1 for bid + 2 for arrow conversion + 3 from spies
		assertEquals(3, scn.GetFreepsHandCount());
	}

	@Test
	public void BurdensCanBePreventedByDiscardingHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var spies = scn.GetShadowCard("spies");
		var troop = scn.GetShadowCard("troop");
		scn.ShadowMoveCardToHand(spies);
		scn.ShadowMoveCharToTable(troop);

		scn.StartGame();

		//need cards in hand
		scn.FreepsDrawCards(3);
		//troop needs an exhausted comp to be an archer
		scn.AddWoundsToChar(frodo, 4);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable()); // IB One Ring
		assertEquals(1, scn.GetBurdens()); // 1 for bid
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsAcceptOptionalTrigger();
		scn.ShadowAcceptOptionalTrigger();

		//Freeps accept prevention
		scn.FreepsChooseYes();
		assertEquals(3, scn.GetBurdens()); // 1 for bid + 2 for arrow conversion, none from spies
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
	}
}
