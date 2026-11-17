package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_211_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wail", "11_211");
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, vitality 2; her text reaches only a companion or ally bearing a [wraith] condition
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his texts need 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeeningWailStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Keening Wail
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Toil 2</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot a Nazgûl to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wail");

		assertEquals("Keening Wail", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(7, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesTwoOffItsCostForEachWraithCharacterExertedAndSpottingANazgulAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wail = scn.GetShadowCard("wail");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(wail);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 7 - 2 - 2 is the least it can cost, with both [wraith] minions exerted; the [moria] Troop does not count.
		// Toil 1 would want 5, and toil 3 (or the Troop counted) would let 1 do.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wail));
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(wail));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowPlayCard(wail);
		// The pool is 2 short of 7: one exertion is owed, and the second is the player's to make or not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(hound);

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 5 - (7 - 2)
		assertEquals(0, scn.GetTwilight());
		// The one Nazgûl in play is a [wraith] one, and is spotted: a burden is added.
		assertEquals(1, scn.FreepsGetBurdens());
		assertInDiscard(wail);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wail = scn.GetShadowCard("wail");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(wail);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// No [wraith] character to exert for toil: the full 7 is paid.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowPlayAvailable(wail));
		scn.ShadowPlayCard(wail);

		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertInDiscard(wail);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAWraithMinionWhoIsNoNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wail = scn.GetShadowCard("wail");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(wail);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The pool pays the full cost without toil: only the missing Nazgûl keeps it out.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wail));
		assertEquals(0, scn.FreepsGetBurdens());
	}
}
