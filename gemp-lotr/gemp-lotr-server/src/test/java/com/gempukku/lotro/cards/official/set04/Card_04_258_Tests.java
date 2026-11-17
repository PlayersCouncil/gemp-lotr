package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_258_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wanderer1", "4_258");
					put("wanderer2", "4_258");  // a second copy: not unique

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit; a modifier while he bears a weapon
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally; a fellowship ability only
					put("pride", "7_13");       // Reckless Pride: on a [dwarven] companion, each minion gains "Assignment: Assign this minion to bearer of Reckless Pride"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronWandererStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Wanderer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (1)</b>.<br><b>Assignment:</b> Exert this minion and spot 5 companions to assign this minion to the Ring-bearer. The Free Peoples player may add (4) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wanderer1");

		assertEquals("Southron Wanderer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignmentAbilityExertsThisMinionToAssignItToTheRingBearerWhenThePreventionIsDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wanderer1 = scn.GetShadowCard("wanderer1");
		var wanderer2 = scn.GetShadowCard("wanderer2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		// Exactly five companions of four cultures, Ring-bound and unbound, with an ally beside them.
		scn.MoveCompanionsToTable(sam);
		scn.MoveCompanionsToTable("aragorn", "gimli", "merry");
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveMinionsToTable(wanderer1, wanderer2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(wanderer1));
		assertEquals(0, scn.GetWoundsOn(wanderer2));
		assertFalse(scn.IsCharAssigned(wanderer1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wanderer1));

		scn.ShadowUseCardAction(wanderer1);
		// The copy used exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(wanderer1));
		assertEquals(0, scn.GetWoundsOn(wanderer2));
		assertTrue(scn.FreepsDecisionAvailable("add (4)"));
		scn.FreepsChooseNo();

		// The copy used is assigned to the Ring-bearer; Sam and the other copy are not assigned.
		assertTrue(scn.IsCharAssignedAgainst(frodo, wanderer1));
		assertFalse(scn.IsCharAssigned(wanderer2));
		assertFalse(scn.IsCharAssigned(sam));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddFourTwilightToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wanderer1 = scn.GetShadowCard("wanderer1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry");

		scn.StartGame();
		scn.MoveMinionsToTable(wanderer1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(wanderer1));
		assertTrue(scn.ShadowActionAvailable(wanderer1));

		scn.ShadowUseCardAction(wanderer1);
		assertEquals(1, scn.GetWoundsOn(wanderer1));
		assertTrue(scn.FreepsDecisionAvailable("add (4)"));
		scn.FreepsChooseYes();

		// The Free Peoples player adds (4); the assignment is prevented, and the exertion stays paid.
		assertEquals(twilight + 4, scn.GetTwilight());
		assertFalse(scn.IsCharAssigned(wanderer1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(1, scn.GetWoundsOn(wanderer1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithFourCompanionsAndAnAllyTheAssignmentAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wanderer1 = scn.GetShadowCard("wanderer1");
		var merry = scn.GetFreepsCard("merry");
		// Four companions, and an ally, a character who is no companion.
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli");
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveMinionsToTable(wanderer1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(wanderer1));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wanderer1));

		// A fifth companion.
		scn.MoveCompanionsToTable(merry);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(wanderer1));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wanderer1 = scn.GetShadowCard("wanderer1");
		var wanderer2 = scn.GetShadowCard("wanderer2");
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry");

		scn.StartGame();
		scn.MoveMinionsToTable(wanderer1, wanderer2);
		// Vitality 2 with 1 wound: exhausted, with five companions to spot.
		scn.AddWoundsToChar(wanderer1, 1);
		assertEquals(1, scn.GetWoundsOn(wanderer1));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(wanderer1));
		assertEquals(0, scn.GetWoundsOn(wanderer2));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wanderer1));
		// The other copy, able to exert, is offered its own.
		assertTrue(scn.ShadowActionAvailable(wanderer2));

		scn.RemoveWoundsFromChar(wanderer1, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(wanderer1));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithTheRingBearerAssignedTheAbilityIsPaidForNothingAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wanderer1 = scn.GetShadowCard("wanderer1");
		var wanderer2 = scn.GetShadowCard("wanderer2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry");

		scn.StartGame();
		scn.MoveMinionsToTable(wanderer1, wanderer2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(wanderer2);
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(frodo, wanderer2));
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(wanderer1));
		assertFalse(scn.IsCharAssigned(wanderer1));
		// The ability has a cost, so it is offered with the Ring-bearer already assigned.
		assertTrue(scn.ShadowActionAvailable(wanderer1));
		scn.ShadowUseCardAction(wanderer1);

		// The exertion is paid; nobody is assigned, and the Free Peoples player is asked nothing.
		assertEquals(1, scn.GetWoundsOn(wanderer1));
		assertFalse(scn.IsCharAssigned(wanderer1));
		assertFalse(scn.FreepsDecisionAvailable("add (4)"));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceThisMinionIsAssignedElsewhereTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wanderer1 = scn.GetShadowCard("wanderer1");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("sam", "aragorn", "merry");
		scn.AttachCardsTo(gimli, pride);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, pride);
		scn.MoveMinionsToTable(wanderer1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Assigned to Gimli by the ability Reckless Pride gives it.
		scn.ShadowUseCardAction(wanderer1, "Reckless Pride");
		assertTrue(scn.IsCharAssignedAgainst(gimli, wanderer1));
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(wanderer1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.ShadowActionAvailable(wanderer1, "Use Southron Wanderer"));
		scn.ShadowUseCardAction(wanderer1, "Use Southron Wanderer");

		// The exertion is paid; it stays Gimli's, and the Ring-bearer is not assigned.
		assertEquals(1, scn.GetWoundsOn(wanderer1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, wanderer1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.FreepsDecisionAvailable("add (4)"));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
