package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_197_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_197");
					put("card2", "7_197");
					put("enquea", "1_231");
					// Úlairë Lemenya, Lieutenant of Morgul: a second Nazgûl, strength 9, not fierce; her text is about
					// companions bearing [wraith] conditions
					put("lemenya", "1_232");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulRegimentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Regiment
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: For each Nazgûl you can spot, the Free Peoples player must exert a companion to assign this minion to a skirmish.<br><b>Skirmish:</b> Exert this minion to make a Nazgûl or [wraith] Orc strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul Regiment", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MustExertToAssignMorgulRegiment() throws DecisionResultInvalidException, CardNotFoundException {
		// Arrange
		VirtualTableScenario scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var enquea = scn.GetShadowCard("enquea");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToHand(card);
		scn.MoveCardsToHand(enquea);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPass();

		scn.ShadowPlayCard(card);
		scn.ShadowPlayCard(enquea);
		scn.ShadowPass();

		scn.SkipToAssignments();

		// The price is paid as the Regiment is assigned, and nothing is asked ahead of it (Q-0221)
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsCanAssign(card));

		// Act
		scn.FreepsAssignToMinions(frodo, card);

		// Assert: one Nazgûl, so one exertion, by the one companion there is
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, card));
		// The Nazgûl is left over for the Shadow player
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// F-0425: "For each Nazgûl you can spot, the Free Peoples player must exert a companion": a cost that cannot be
	// paid in full is not begun (CR 4.2 "cost").
	@Test
	public void WithTwoNazgulAndOneExertionLeftMorgulRegimentIsNotTheFreePeoplesPlayersToAssignAndNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("card");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, enquea, lemenya);
		// The Ring-bearer (vitality 4) is the only companion: with 2 wounds he can exert once, and two Nazgûl
		// ask for two exertions
		scn.AddWoundsToChar(frodo, 2);
		scn.SkipToAssignments();

		assertEquals(2, scn.GetVitality(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(regiment));
		assertFalse(scn.FreepsCanAssign(regiment));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, regiment));

		scn.FreepsDeclineAssignments();

		// Nothing was paid, and the Regiment is the Shadow player's to assign, for nothing
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(regiment));
		scn.ShadowAssignToMinions(frodo, regiment);

		assertTrue(scn.IsCharAssignedAgainst(frodo, regiment));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithTwoNazgulAndOneExertionLeftOnEachOfTwoCompanionsMorgulRegimentIsAssignedAndBothExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("card");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, enquea, lemenya);
		// Each of the two companions (vitality 4) can exert once: two exertions between them, for two Nazgûl
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment));
		assertTrue(scn.FreepsCanAssign(regiment));

		scn.FreepsAssignToMinions(aragorn, regiment);

		// The first exertion is his to place on either; the second is then the other's, the one companion left
		// able to exert, and is not asked
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertFalse(scn.IsCharAssigned(regiment));
		scn.FreepsChooseCard(frodo);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, regiment));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void ARegimentWhoseTwoExertionsCanNoLongerBothBePaidWhenItsTurnComesIsNotBegunAndIsLeftOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment1 = scn.GetShadowCard("card");
		var regiment2 = scn.GetShadowCard("card2");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment1, regiment2, enquea, lemenya);
		// Three exertions are left among the companions, all Aragorn's; two Nazgûl ask two for each Regiment
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToAssignments();

		// Each Regiment can be paid for by itself, so both are his to assign
		assertTrue(scn.FreepsCanAssign(regiment1));
		assertTrue(scn.FreepsCanAssign(regiment2));

		// The payments are made in the order of the minions' card ids
		var first = regiment1.getCardId() < regiment2.getCardId() ? regiment1 : regiment2;
		var second = first == regiment1 ? regiment2 : regiment1;

		scn.MarkGameLog();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, second}, new PhysicalCardImpl[]{aragorn, first});

		// Aragorn, the one companion able to, exerts twice for the first.  One exertion is left for the second,
		// which asks two: none of it is paid, and it is left over for the Shadow player.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, first));
		assertFalse(scn.IsCharAssigned(second));
		assertTrue(scn.GameLogSinceMarkSays("did not pay to assign"));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(second));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(first));
	}
}
