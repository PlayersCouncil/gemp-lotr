package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trail", "12_76");
					put("men", "11_100");       // Strange-looking Men: [men] Man minion, strength 10, vitality 2; its ability is used only on purpose
					put("axeman", "11_69");     // Axeman of Harad: [men] Man minion, strength 11, vitality 3; a cost reduction only
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, strength 3, vitality 4; his abilities are used only on purpose
					put("easterling", "4_224"); // Easterling Axeman: [raider] Man minion; +3 and fierce only at 2 burdens, and there are none

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, resistance 6; his ability is used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: unbound [shire] Hobbit, resistance 6; his ability is used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer, resistance 5; his abilities are used only on purpose
					put("eomer", "12_112");     // Eomer, Eored Leader: unbound [rohan] Man, resistance 7; Maneuver: exert him to play a [rohan] companion from the draw deck
					put("theoden", "8_92");     // Theoden, Tall and Proud: unbound [rohan] companion, resistance 6; his texts are a discount and a death trigger
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrailOfTerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Trail of Terror
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the start of the maneuver phase, you may spot 3 [men] minions and remove (1) to make each unbound companion resistance -1 until the regroup phase.
		*/

		var scn = GetScenario();

		var trail = scn.GetFreepsCard("trail");

		assertEquals("Trail of Terror", trail.getBlueprint().getTitle());
		assertNull(trail.getBlueprint().getSubtitle());
		assertFalse(trail.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, trail.getBlueprint().getSide());
		assertEquals(Culture.MEN, trail.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, trail.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(trail, Keyword.SUPPORT_AREA));
		assertEquals(0, trail.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseRemovesOneToMakeEachUnboundCompanionResistanceMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var men = scn.GetShadowCard("men");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, merry, sam);
		scn.MoveCardsToSupportArea(trail);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trail);
		scn.MoveMinionsToTable(men, axeman, ted);
		scn.FreepsPass();
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		scn.ShadowPass();

		// Three [men] minions, two Men and a Hobbit.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(trail));
		scn.ShadowChooseOptionalTrigger(trail);
		assertEquals(0, scn.GetTwilight());
		// The unbound companions, a Man and a Hobbit; not the Ring-bearer, nor Sam, who is Ring-bound.
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(merry));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTwoMenMinionsTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var men = scn.GetShadowCard("men");
		var ted = scn.GetShadowCard("ted");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(trail);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trail);
		scn.MoveMinionsToTable(men, ted, easterling);
		scn.FreepsPass();
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		assertEquals(6, scn.GetResistance(aragorn));
		scn.ShadowPass();

		// Two [men] minions; the [raider] Man and this [men] condition do not make a third.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetTwilight());
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesEverythingAsItWasAndNothingIsOfferedAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var men = scn.GetShadowCard("men");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(trail);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trail);
		scn.MoveMinionsToTable(men, axeman, ted);
		scn.FreepsPass();
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(trail));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(3, scn.GetTwilight());
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase is no start of a maneuver phase.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetTwilight());
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var men = scn.GetShadowCard("men");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(trail);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trail);
		scn.MoveMinionsToTable(men, axeman, ted);
		scn.FreepsPass();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheOfferIsTakenAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var men = scn.GetShadowCard("men");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(trail);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trail);
		scn.MoveMinionsToTable(men, axeman, ted);
		scn.FreepsPass();
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());

		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(trail));
		scn.ShadowChooseOptionalTrigger(trail);
		assertEquals(1, scn.GetTwilight());
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ResistanceIsLowerThroughTheSkirmishesAndBackAtRegroupAndTheTriggerComesAgainAfterASecondMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var men = scn.GetShadowCard("men");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(trail);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trail);
		scn.MoveMinionsToTable(men, axeman, ted);
		// Aragorn, 8 + 20, beats the Axeman and the Men together, 11 + 10 = 21, and does not double them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 20));
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowPass();

		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));
		scn.ShadowChooseOptionalTrigger(trail);
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(merry));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, axeman},
				new PhysicalCardImpl[]{merry, ted});
		scn.ShadowAssignToMinions(aragorn, men);
		// Merry, 3, and Ted, 3: Merry loses and takes a wound.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(merry));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(merry));

		// A second move: the three [men] minions are still in play, each with a wound or none.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(trail));
		scn.ShadowChooseOptionalTrigger(trail);
		assertEquals(0, scn.GetTwilight());
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionPlayedAfterTheStartOfTheManeuverPhaseKeepsHisResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trail = scn.GetShadowCard("trail");
		var men = scn.GetShadowCard("men");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var eomer = scn.GetFreepsCard("eomer");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToSupportArea(trail);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, trail);
		assertInZone(Zone.DECK, theoden);
		scn.MoveMinionsToTable(men, axeman, ted);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowPass();

		assertEquals(7, scn.GetResistance(eomer));
		scn.ShadowChooseOptionalTrigger(trail);
		assertEquals(6, scn.GetResistance(eomer));

		// Three minions and two companions: Eomer plays Theoden from the draw deck.
		scn.FreepsUseCardAction(eomer);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(theoden);
		assertInPlay(theoden);
		assertEquals(6, scn.GetResistance(theoden));
		assertEquals(6, scn.GetResistance(eomer));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
