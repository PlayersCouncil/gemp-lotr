package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_065_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_65");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Gloom
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		* 	To play, hinder 2 twilight conditions.
		* 	Shadow: Hinder this condition and remove (1) to take a twilight card into hand from your draw deck.
		* 	Regroup: Hinder a twilight card to add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Gloom", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityNotAvailableWithZeroTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		scn.MoveCardsToSupportArea(card);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Set twilight to 0 BEFORE shadow decision is created
		scn.SetTwilight(0);
		scn.FreepsPassCurrentPhaseAction();

		// The Shadow ability costs: hinder self + remove (1).
		// With 0 twilight, the ability should not be available due to the RemoveTwilight(1) cost.
		assertFalse(scn.ShadowActionAvailable(card));
	}

	@Test
	public void ShadowAbilityAvailableWithOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		scn.MoveCardsToSupportArea(card);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Set twilight to 1 BEFORE shadow decision is created
		scn.SetTwilight(1);
		scn.FreepsPassCurrentPhaseAction();

		// With 1 twilight, the ability should be available (hinder self is free, remove 1 is paid)
		assertTrue(scn.ShadowActionAvailable(card));
	}
}
