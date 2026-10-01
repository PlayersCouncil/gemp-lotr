package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.VirtualTableScenario.P1;
import static org.junit.Assert.*;

public class Card_07_201_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_201");
					put("enquea", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSpearmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Spearman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, the Free Peoples player must exert a companion to assign this minion to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Morgul Spearman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MustExertToAssignMorgulSpearman() throws DecisionResultInvalidException, CardNotFoundException {
		// Arrange
		VirtualTableScenario scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var enquea = scn.GetShadowCard("enquea");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToHand(card);
		scn.MoveCardsToHand(enquea);

		scn.StartGame();

		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(card);
		scn.ShadowPlayCard(enquea);
		scn.ShadowPassCurrentPhaseAction();

		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable(
				"Would you like to exert a companion to be able to assign Morgul Spearman to skirmish?"));

		// Act
		scn.PlayerDecided(P1, "0");

		// Assert
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsCanAssign(card));
	}

	protected VirtualTableScenario GetTwoSpearmenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					put("gimli", "1_13");

					put("spearman1", "7_201");
					put("spearman2", "7_201");
					put("enquea", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Issue #1090: the assignment cost is per-minion.  Paying it for one Morgul Spearman must not
	// make every other Morgul Spearman (or any other assignment-cost minion) assignable as well.
	@Test
	public void PayingForOneSpearmanDoesNotLetFreepsAssignAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoSpearmenScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var spearman1 = scn.GetShadowCard("spearman1");
		var spearman2 = scn.GetShadowCard("spearman2");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman1, spearman2, enquea);

		scn.SkipToAssignments();

		// Both spearmen ask for their own cost; the Shadow player orders the two required triggers.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert a companion to be able to assign Morgul Spearman to skirmish?"));
		scn.FreepsChooseNo();

		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert a companion to be able to assign Morgul Spearman to skirmish?"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssign(spearman1));
		assertTrue(scn.FreepsCanAssign(spearman2));
		assertTrue(scn.FreepsCanAssign(enquea));
		var targets = scn.FreepsGetShadowAssignmentTargets();
		assertFalse(targets.contains(String.valueOf(spearman1.getCardId())));
		assertTrue(targets.contains(String.valueOf(spearman2.getCardId())));
		assertTrue(targets.contains(String.valueOf(enquea.getCardId())));

		// The unpaid spearman is still available for the Shadow player to assign.
		scn.FreepsAssignToMinions(aragorn, spearman2);
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowGetShadowAssignmentTargets().contains(String.valueOf(spearman1.getCardId())));
		scn.ShadowAssignToMinions(frodo, spearman1);
		assertTrue(scn.IsCharAssignedAgainst(frodo, spearman1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman2));
	}

	@Test
	public void PayingForSpearmanDoesNotLetFreepsAssignAnUnpaidMorgulRegiment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
					put("gimli", "1_13");

					put("spearman", "7_201");
					put("regiment", "7_197");
					put("enquea", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var spearman = scn.GetShadowCard("spearman");
		var regiment = scn.GetShadowCard("regiment");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, regiment, enquea);

		scn.SkipToAssignments();

		// The Free Peoples player orders the two different required triggers; resolve the Regiment's first.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsChooseAction("blueprintId", "7_197");
		assertTrue(scn.FreepsDecisionAvailable("assign Morgul Regiment"));
		scn.FreepsChooseNo();

		assertTrue(scn.FreepsDecisionAvailable("assign Morgul Spearman"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsCanAssign(spearman));
		assertFalse(scn.FreepsCanAssign(regiment));
		assertFalse(scn.FreepsGetShadowAssignmentTargets().contains(String.valueOf(regiment.getCardId())));
	}
}
