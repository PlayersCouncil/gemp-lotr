package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_282_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "4_282");
					put("charge2", "4_282");    // a second copy: a limit does not reach another copy of the card (CR 4.2 "limit")

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; Valiant is his whole text
					put("elite", "4_265");      // Elite Rider: [rohan] Man companion; his bonus needs him skirmishing a wounded minion
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally; a fellowship ability used only on purpose
					put("hlafwine", "4_281");   // Hlafwine, Village Farmhand: [rohan] Man ally; a skirmish ability used only on purpose
					// Merry, Master Holbytla: the one [rohan] character who is no Man.  He is a Man as well only while
					// 3 [rohan] companions (or Eowyn) can be spotted; nobody exerts, so his trigger never comes up.
					put("merry", "103_85");
					put("knight", "5_35");      // Gondorian Knight: a Man who is not [rohan]; Knight is his whole text
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound Dwarf companion, strength 6
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: strength 5; cheated in, so its text never comes up
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnHonorableChargeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: An Honorable Charge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an unbound companion strength +1 for each [rohan] Man you spot (limit +3).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("charge");

		assertEquals("An Honorable Charge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenUnboundCompanionStrongerByTheNumberOfRohanMenNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		// Three [rohan] Men: the Guard, and Guma and Hlafwine, who are allies.  Merry is the second [rohan]
		// companion and so no Man; the Knight is a Man of [gondor].
		scn.MoveCompanionsToTable(guard, merry, knight, gimli, sam);
		scn.MoveAlliesToTable(guma, hlafwine);
		scn.MoveCardsToHand(charge);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(charge));
		scn.FreepsPlayCard(charge);

		// The player names how many he spots: none, or up to the three there are.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// Any unbound companion, in the skirmish or not; not a Ring-bound companion, an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, merry, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, guma, hlafwine, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// Two named of the three: +2 and not +3.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(guard));
		assertInDiscard(charge);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusStopsAtThreeAndASecondCopyGivesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var charge2 = scn.GetFreepsCard("charge2");
		var guard = scn.GetFreepsCard("guard");
		var elite = scn.GetFreepsCard("elite");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		// Four [rohan] Men: two companions and two allies.
		scn.MoveCompanionsToTable(guard, elite);
		scn.MoveAlliesToTable(guma, hlafwine);
		scn.MoveCardsToHand(charge, charge2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		scn.FreepsPlayCard(charge);

		// The count itself is not capped: all four may be named.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("4");
		scn.FreepsChooseCard(guard);

		// Four named, and the bonus stops at +3.
		assertEquals(9, scn.GetStrength(guard));
		assertInDiscard(charge);

		scn.ShadowPass();
		scn.FreepsPlayCard(charge2);
		scn.FreepsChoose("4");
		scn.FreepsChooseCard(guard);

		// The second copy has a limit of its own: 6 + 3 + 3.
		assertEquals(12, scn.GetStrength(guard));
		assertInDiscard(charge2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsToTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.MoveCardsToHand(charge);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, savage},
				new PhysicalCardImpl[]{gimli, runner});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		scn.FreepsPlayCard(charge);
		// The Guard is the one [rohan] Man.
		scn.FreepsChoose("1");
		scn.FreepsChooseCard(guard);

		// Still in force at the next decision of that skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(guard));

		// 7 against 5: the Guard wins, and the bonus is gone in the next skirmish.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(charge);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(charge));

		// The skirmish phase next to it: playable.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(charge));
	}
}
