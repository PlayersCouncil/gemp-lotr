package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "17_96");

					put("scout", "1_270");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Éowyn, Northwoman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While the Ring-bearer is assigned to a skirmish, Éowyn cannot take wounds while skirmishing.
		 * <b>Assignment:</b> Exert Éowyn and assign a minion to the Ring-bearer to make that minion lose
		 * all game text keywords and unable to gain game text keywords until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Northwoman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void EowynAbilityAssignsRingBearerToRemoveGameText() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionToTable(eowyn);

		var scout = scn.GetShadowCard("scout");
		scn.MoveMinionsToTable(scout);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.HasKeyword(scout, Keyword.TRACKER));
		assertTrue(scn.FreepsActionAvailable(eowyn));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(GetScenario().IsCharAssigned(scout));

		scn.FreepsUseCardAction(eowyn);
		assertFalse(scn.HasKeyword(scout, Keyword.TRACKER));
		assertFalse(scn.FreepsActionAvailable(eowyn));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.IsCharAssigned(frodo));
		assertTrue(scn.IsCharAssigned(scout));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowAnyActionsAvailable());
	}
}
