package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_098_Tests
{

	// Site 2 is Ettenmoors 1_331, a plains and nothing else; site 3 is Rivendell Valley 1_341, a sanctuary.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fallback", "18_98");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, vitality 3; valiant is its whole text
					// Merry, Master Holbytla: a [rohan] Hobbit companion; his trigger answers ANOTHER [rohan] Man exerting
					put("merry", "103_85");
					// Rohirrim Herdsman: a [rohan] Man ally; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its play trigger does not fire when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Uruk Camp 4_335, a battleground and a plains: "each time an Uruk-hai is played, that minion must
	// exert", and none is played.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fallback", "18_98");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, vitality 3; valiant is its whole text
					// Merry, Master Holbytla: a [rohan] Hobbit companion; his trigger answers ANOTHER [rohan] Man exerting
					put("merry", "103_85");
					// Rohirrim Herdsman: a [rohan] Man ally; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its play trigger does not fire when cheated in
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FallBacktoHelmsDeepStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Fall Back to Helm's Deep
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: If the fellowship is at a plains site, exert a [rohan] companion to return a minion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fallback");

		assertEquals("Fall Back to Helm's Deep", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtAPlainsSiteExertsARohanCompanionToReturnAMinionToItsOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fallback = scn.GetFreepsCard("fallback");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(fallback);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(fallback));

		scn.FreepsPlayCard(fallback);
		// [rohan] companions, a Man and a Hobbit; not the [rohan] ally, not Aragorn or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// Merry pays: his trigger answers another [rohan] Man's exertion, not his own.
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Minions of two cultures and races; not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertInZone(Zone.HAND, runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.DISCARD, fallback);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PlayableAtAPlainsSiteThatIsAlsoABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var fallback = scn.GetFreepsCard("fallback");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(fallback);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(fallback));

		// One [rohan] companion and one minion: neither choice is asked.
		scn.FreepsPlayCard(fallback);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.HAND, savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedAtASiteThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fallback = scn.GetFreepsCard("fallback");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(fallback);
		scn.MoveMinionsToTable(savage);

		// The maneuver phase at Rivendell Valley, site 3.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInHand(fallback);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fallback));
		assertEquals(0, scn.GetWoundsOn(guard));
	}

	@Test
	public void CannotBePlayedWhileTheOnlyRohanCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fallback = scn.GetFreepsCard("fallback");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToHand(fallback);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3, 2 wounds: the Guard cannot exert.  Aragorn, who could, is no [rohan] companion.
		scn.AddWoundsToChar(guard, 2);
		assertEquals(2, scn.GetWoundsOn(guard));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fallback));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
