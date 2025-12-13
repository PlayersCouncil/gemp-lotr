package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "102_45");
					put("eomer", "4_267");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DoNotTrusttoHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Do Not Trust to Hope
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Strength: 2
		 * Game Text: Bearer must be a [rohan] companion.
		* 	 Bearer is <b>valiant</b>.
		* 	At the start of the regroup phase, wound bearer or discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Do Not Trust to Hope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void DoNotTrusttoHopeMakesBearerValiant() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCardsToHand(hope);
		scn.MoveCompanionsToTable(eomer);

		scn.StartGame();

		assertFalse(scn.HasKeyword(eomer, Keyword.VALIANT));
		assertTrue(scn.FreepsPlayAvailable(hope));

		scn.FreepsPlayCard(hope);
		assertEquals(eomer, hope.getAttachedTo());
		assertTrue(scn.HasKeyword(eomer, Keyword.VALIANT));
	}

	@Test
	public void DoNotTrusttoHopeWoundsAtStartOfRegroupUnlessDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, hope);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsDecisionAvailable(""));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(Zone.ATTACHED, hope.getZone());

		scn.FreepsChoose("Wound");
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(Zone.ATTACHED, hope.getZone());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsDecisionAvailable(""));
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(Zone.ATTACHED, hope.getZone());

		scn.FreepsChoose("Discard");
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(Zone.DISCARD, hope.getZone());
	}
}
