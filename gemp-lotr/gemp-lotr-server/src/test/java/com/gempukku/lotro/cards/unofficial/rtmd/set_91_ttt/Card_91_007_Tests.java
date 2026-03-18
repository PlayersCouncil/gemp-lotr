package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_007_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mod", "91_7");
					// Ulaire Enquea, Lieutenant of Morgul: Nazgul minion (home 3)
					put("enquea", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotRoamingStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_7
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Your opponent's minions are not roaming.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_7", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void OpponentMinionsLoseRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_7: "Your opponent's minions are not roaming."
		// When Freeps has this modifier, Shadow's minions should lose roaming.

		var scn = GetScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCardsToSupportArea(freepsMod);
		scn.MoveCardsToHand(enquea);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass(); // move to site 2

		//There's only enough twilight to pay for Enquea, but not his roaming cost
		assertEquals(6, scn.GetTwilight());
		scn.ShadowPlayCard(enquea);

		// Enquea would normally be roaming at site 2
		// With the modifier, it should NOT be roaming
		assertFalse(scn.HasKeyword(enquea, Keyword.ROAMING));
	}

	@Test
	public void OwnMinionsStillRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_7: "Your opponent's minions are not roaming."
		// When Shadow has this modifier, Shadow's OWN minions should still be roaming
		// (the card says "opponent's", not "your").

		var scn = GetScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCardsToSupportArea(shadowMod);

		scn.StartGame();
		scn.FreepsPass(); // move to site 2
		scn.MoveMinionsToTable(enquea);

		// Shadow's own minions should NOT be affected by their own copy
		// (it says "opponent's minions")
		assertTrue(scn.HasKeyword(enquea, Keyword.ROAMING));
	}
}
