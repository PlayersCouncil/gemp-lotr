package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prey", "8_67");

					put("attea", "8_79");       // Úlairë Attëa, Thrall of The One: enduring [wraith] Nazgûl, vitality 3; a regroup ability never used here
					put("undead", "16_6");      // Undead of Angmar: enduring [wraith] Wraith, vitality 3; its text needs another [wraith] Wraith, and its ability is never used
					put("berserker", "8_96");   // Gorgoroth Berserker: enduring [sauron] Orc, vitality 3; its maneuver abilities are never used
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl who is not enduring, vitality 3; fierce and damage +1 only at 2 burdens, and there are none

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, unbound; a maneuver ability never used here
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, unbound; valiant and nothing else
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his abilities are never used
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: an ally, no companion; a maneuver ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BetweenNazgulandPreyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Between Nazgûl and Prey
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Exert an enduring [wraith] minion twice to assign that minion to an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("prey");

		assertEquals("Between Nazgûl and Prey", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnEnduringWraithMinionTwiceToAssignItToTheUnboundCompanionChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prey = scn.GetShadowCard("prey");
		var attea = scn.GetShadowCard("attea");
		var undead = scn.GetShadowCard("undead");
		var berserker = scn.GetShadowCard("berserker");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var albert = scn.GetFreepsCard("albert");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToHand(prey);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, undead, berserker, nelya);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(undead));
		assertFalse(scn.IsCharAssigned(attea));
		assertTrue(scn.ShadowPlayAvailable(prey));
		scn.ShadowPlayCard(prey);

		// An enduring [wraith] minion of either race; not the enduring [sauron] Orc or the [wraith] Nazgûl who is
		// not enduring.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, undead));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(berserker, nelya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(attea);
		assertEquals(2, scn.GetWoundsOn(attea));

		// An unbound companion: not the Ring-bearer, the Ring-bound Sam or the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, albert));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertInDiscard(prey);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, attea));
		// The minion not chosen is neither exerted nor assigned.
		assertEquals(0, scn.GetWoundsOn(undead));
		assertFalse(scn.IsCharAssigned(undead));
		assertFalse(scn.IsCharAssigned(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyEnduringWraithMinionCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prey = scn.GetShadowCard("prey");
		var attea = scn.GetShadowCard("attea");
		var berserker = scn.GetShadowCard("berserker");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(prey);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, berserker, nelya);
		// Vitality 3 with one wound: she can exert once, not twice.  The other two could exert twice.
		scn.AddWoundsToChar(attea, 1);
		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		// The pool would pay the event's (4).
		assertTrue(scn.GetTwilight() >= 4);
		assertFalse(scn.ShadowPlayAvailable(prey));
		assertEquals(1, scn.GetWoundsOn(attea));
	}

	@Test
	public void WithNoUnboundCompanionItIsPlayedAndPaidForAndAssignsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prey = scn.GetShadowCard("prey");
		var attea = scn.GetShadowCard("attea");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(prey);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(attea));
		// Only Frodo, the Ring-bearer, and Sam, Ring-bound, are companions.
		assertTrue(scn.ShadowPlayAvailable(prey));
		scn.ShadowPlayCard(prey);

		assertInDiscard(prey);
		assertEquals(2, scn.GetWoundsOn(attea));
		assertFalse(scn.IsCharAssigned(attea));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
