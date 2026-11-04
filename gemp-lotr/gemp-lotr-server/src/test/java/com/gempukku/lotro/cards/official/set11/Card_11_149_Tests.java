package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_149_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hall", "11_149");
					put("hall2", "11_149");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally; a fellowship ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, no Man without Eowyn
					                            //   or 3 [rohan] companions
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ProtectingtheHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Protecting the Hall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Toil 2</b>. <helper>(For each [rohan] character you exert when playing this, its twilight cost is -2.)</helper><br>Bearer must be a [rohan] Man.<br><b>Assignment:</b> Discard this condition from play to make bearer <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hall");

		assertEquals("Protecting the Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARohanManAndEachRohanCharacterExertedForToilTakesTwoOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hall = scn.GetFreepsCard("hall");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(hall);

		assertInHand(hall);
		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsPlayAvailable(hall));
		scn.FreepsPlayCard(hall);

		// The bearer: a [rohan] Man, companion or ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		scn.FreepsChooseCard(guard);

		// Toil: any [rohan] character may be exerted, and none need be.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(merry);

		assertAttachedTo(hall, guard);
		assertEquals(1, scn.GetWoundsOn(merry));
		// 4 - 2 for Merry's exertion
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AssignmentActionDiscardsThisConditionToMakeItsBearerDefenderPlusOneForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hall = scn.GetFreepsCard("hall");
		var hall2 = scn.GetFreepsCard("hall2");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, hall);
		scn.AttachCardsTo(guma, hall2);

		var savage = scn.GetShadowCard("savage");

		assertAttachedTo(hall, guard);
		assertAttachedTo(hall2, guma);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(hall));
		scn.FreepsUseCardAction(hall);

		assertInDiscard(hall);
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		// The other copy and its bearer are untouched.
		assertAttachedTo(hall2, guma);
		assertEquals(0, scn.GetKeywordCount(guma, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		// The defender +1 lasts the assignment phase.
		scn.PassAssignmentActions();
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
	}

	@Test
	public void AssignmentActionIsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hall = scn.GetFreepsCard("hall");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, hall);

		var savage = scn.GetShadowCard("savage");

		assertAttachedTo(hall, guard);
		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hall));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hall));
		assertAttachedTo(hall, guard);
	}
}
