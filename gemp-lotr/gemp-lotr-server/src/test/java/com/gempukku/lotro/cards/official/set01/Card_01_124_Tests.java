package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cruel", "1_124");

					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability only ever used on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CruelCaradhrasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Cruel Caradhras
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>. <b>Weather</b>.<br><b>Maneuver:</b> Exert an [isengard] minion to make the opponent choose to either exert the Ring-bearer or add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cruel");

		assertEquals("Cruel Caradhras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.WEATHER));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionAndTheFreePeoplesPlayerChoosesToExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruel = scn.GetShadowCard("cruel");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(cruel);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, servant, guard, troop);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.ShadowPlayAvailable(cruel));

		scn.ShadowPlayCard(cruel);
		// [isengard] minions of two races; not a [sauron] Uruk-hai or a [moria] Orc, each able to exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);

		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The opponent, not the Shadow player, chooses between the two.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert the Ring-bearer"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChoose("Exert the Ring-bearer");

		assertEquals(1, scn.GetWoundsOn(frodo));
		// Sam is Ring-bound but not the Ring-bearer.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());
		assertInDiscard(cruel);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayChooseToAddABurdenInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruel = scn.GetShadowCard("cruel");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(cruel);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.ShadowPlayCard(cruel);
		// The one [isengard] minion exerts with nothing asked.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInDiscard(cruel);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheRingBearerExhaustedABurdenIsAddedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruel = scn.GetShadowCard("cruel");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(cruel);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Frodo is exhausted and cannot exert.  Sam beside him could.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowPlayCard(cruel);

		// Exerting the Ring-bearer cannot be done, so there is nothing to choose: the burden is added.
		assertEquals(0, scn.FreepsGetMultipleChoiceCount());
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(cruel);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruel = scn.GetShadowCard("cruel");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(cruel);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.AddWoundsToChar(savage, 1);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: Uruk Savage can still exert.
		assertTrue(scn.ShadowPlayAvailable(cruel));

		scn.AddWoundsToChar(savage, 1);
		scn.ShadowRefreshDecision();

		// Vitality 3 with 2 wounds: exhausted.  The [moria] minion beside it could exert.
		assertFalse(scn.ShadowPlayAvailable(cruel));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
