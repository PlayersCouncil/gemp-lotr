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
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Enquea, Lieutenant of Morgul: Nazgul minion (home 3)
					put("enquea", "1_231");
					// Goblin Patrol Troop: a [moria] Orc with no text, cost 6, home 4
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Enquea, Lieutenant of Morgul: Nazgul minion (home 3)
					put("enquea", "1_231");
					// Goblin Patrol Troop: a [moria] Orc with no text, cost 6, home 4
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_7"
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

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_7", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getIntensity());
	}

	@Test
	public void OpponentMinionsLoseRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_7: "Your opponent's minions are not roaming."
		// When Freeps has this modifier, Shadow's minions should lose roaming.

		var scn = GetFreepsScenario();

		var enquea = scn.GetShadowCard("enquea");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(enquea);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass(); // move to site 2

		//There's only enough twilight to pay for Enquea, but not his roaming cost
		// 3 set + 1 companion + 2 for the site
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(enquea));
		scn.ShadowPlayCard(enquea);

		// Enquea would normally be roaming at site 2
		// With the modifier, it should NOT be roaming
		assertFalse(scn.HasKeyword(enquea, Keyword.ROAMING));
		// His printed 6 and no more
		assertEquals(0, scn.GetTwilight());

		// Nor is a minion of another culture and race, home site 4
		scn.MoveMinionsToTable(troop);
		assertFalse(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OwnMinionsStillRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_7: "Your opponent's minions are not roaming."
		// When Shadow has this modifier, Shadow's OWN minions should still be roaming.

		var scn = GetShadowScenario();

		var enquea = scn.GetShadowCard("enquea");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass(); // move to site 2
		scn.MoveMinionsToTable(enquea, troop);

		// Shadow's own minions should NOT be affected by their own copy
		assertTrue(scn.HasKeyword(enquea, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnersOpponentsMinionsAreNotRoamingOnTheOwnersTurnToo() throws DecisionResultInvalidException, CardNotFoundException {
		// Player 2 owns the modifier; on his own turn player 1 is the Shadow player and the opponent.
		var scn = GetShadowScenario();

		var enquea = scn.GetFreepsCard("enquea");
		var troop = scn.GetFreepsCard("troop");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(enquea);
		scn.SetTwilight(3);

		scn.ShadowPass(); // player 2 moves to site 2

		// There's only enough twilight to pay for Enquea, but not his roaming cost
		// 3 set + 1 companion + 2 for the site
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(enquea));
		scn.FreepsPlayCard(enquea);

		assertFalse(scn.HasKeyword(enquea, Keyword.ROAMING));
		// His printed 6 and no more
		assertEquals(0, scn.GetTwilight());

		// Nor is a minion of another culture and race, home site 4
		scn.MoveMinionsToTable(troop);
		assertFalse(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
