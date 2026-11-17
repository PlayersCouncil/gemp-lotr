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

public class Card_04_274_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("firefoot", "4_274");
					put("eomer", "4_266");      // Éomer, Sister-son of Théoden: [rohan] Man companion, strength 7, vitality 3; a start-of-turn heal of a [rohan] ally
					put("eomer2", "7_227");     // Éomer, Skilled Tactician: [rohan] Man companion; a Fellowship ability used only on purpose
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] Man companion who is no Éomer; a Regroup ability only
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, home 4T; a Fellowship ability only
					// Merry, Master Holbytla: a [rohan] Hobbit, and a Man only while 3 [rohan] companions (or Éowyn) can be
					// spotted; beside one other [rohan] companion he is no Man
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a Maneuver ability only
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; its play trigger is not reached
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FirefootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Firefoot
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.<br>If bearer is Éomer, he is <b>defender +1</b>.<br>At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("firefoot");

		assertEquals("Firefoot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FirefootIsPlayedOnlyOnARohanManAndMakesEomerDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eomer, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(firefoot);

		scn.StartGame();

		assertInHand(firefoot);
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(firefoot));
		scn.FreepsPlayCard(firefoot);

		// A [rohan] companion and a [rohan] ally, both Men; not Merry, a [rohan] Hobbit (two [rohan] companions:
		// no Man), nor Aragorn, a Man of another culture, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eomer);

		assertAttachedTo(firefoot, eomer);
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnotherRohanManFirefootMakesNobodyDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer2");
		scn.MoveCompanionsToTable(theoden, eomer);
		scn.AttachCardsTo(theoden, firefoot);

		scn.StartGame();

		// Borne by a [rohan] Man who is not Éomer, it gives him nothing, nor the Éomer beside him.
		assertAttachedTo(firefoot, theoden);
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheBearersSkirmishEachMinionSkirmishingHimExertsAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(eomer, aragorn);
		scn.AttachCardsTo(eomer, firefoot);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, scout, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, savage}, new PhysicalCardImpl[]{aragorn, troop});
		// The Moria Scout is left over: the Shadow player puts it on Éomer as a second minion in his skirmish.
		scn.ShadowAssignToMinions(eomer, scout);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsResolveSkirmish(eomer);

		// Both minions skirmishing the bearer, of two cultures and races, exert; the Troop, assigned to Aragorn
		// whose skirmish comes later, does not.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);

		// The start of a skirmish that does not involve the bearer: nobody exerts.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ASecondSkirmishOfTheBearerInTheSameTurnExertsTheMinionAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firefoot = scn.GetFreepsCard("firefoot");
		var eomer = scn.GetFreepsCard("eomer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, firefoot);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, troop);
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsResolveSkirmish(eomer);
		assertEquals(1, scn.GetWoundsOn(troop));

		// The Troop (13) beats Éomer (7) without doubling him: one wound, and both are still in play.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertInPlay(troop);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.FreepsResolveSkirmish(eomer);

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
