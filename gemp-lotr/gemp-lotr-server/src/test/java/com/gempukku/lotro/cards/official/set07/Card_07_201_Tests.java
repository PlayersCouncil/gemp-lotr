package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

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

		scn.FreepsPass();

		scn.ShadowPlayCard(card);
		scn.ShadowPlayCard(enquea);
		scn.ShadowPass();

		scn.SkipToAssignments();

		// The price is paid as the Spearman is assigned, and nothing is asked ahead of it (Q-0221)
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsCanAssign(card));

		// Act
		scn.FreepsAssignToMinions(frodo, card);

		// Assert: the one companion there is exerts, and the Spearman is assigned
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, card));
		// The Nazgûl is left over for the Shadow player
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
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

	// Issue #1090: the assignment cost is per-minion.  Since Q-0221 it is paid as each minion is assigned, so
	// each Morgul Spearman the Free Peoples player assigns is paid for by itself, and one he leaves alone costs
	// him nothing.
	@Test
	public void EachSpearmanTheFreePeoplesPlayerAssignsIsPaidForByItself() throws DecisionResultInvalidException, CardNotFoundException {
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

		// Nothing is asked ahead, and both spearmen are his to assign beside the Nazgûl.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsCanAssign(spearman1));
		assertTrue(scn.FreepsCanAssign(spearman2));
		assertTrue(scn.FreepsCanAssign(enquea));
		var targets = scn.FreepsGetShadowAssignmentTargets();
		assertTrue(targets.contains(String.valueOf(spearman1.getCardId())));
		assertTrue(targets.contains(String.valueOf(spearman2.getCardId())));
		assertTrue(targets.contains(String.valueOf(enquea.getCardId())));

		// He assigns one of them: one exertion, of his choice, and no more.
		scn.FreepsAssignToMinions(aragorn, spearman2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman2));

		// The spearman he left alone is still there for the Shadow player to assign, for nothing.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowGetShadowAssignmentTargets().contains(String.valueOf(spearman1.getCardId())));
		scn.ShadowAssignToMinions(frodo, spearman1);
		assertTrue(scn.IsCharAssignedAgainst(frodo, spearman1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AssigningBothSpearmenCostsAnExertionForEach() throws DecisionResultInvalidException, CardNotFoundException {
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

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman1}, new PhysicalCardImpl[]{gimli, spearman2});

		// One exertion for each, asked for one after the other; neither is assigned until both are settled.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertFalse(scn.IsCharAssigned(spearman1));
		assertFalse(scn.IsCharAssigned(spearman2));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertFalse(scn.IsCharAssigned(spearman1));
		assertFalse(scn.IsCharAssigned(spearman2));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, spearman2));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void PayingForSpearmanDoesNotPayForMorgulRegiment() throws DecisionResultInvalidException, CardNotFoundException {
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

		// Nothing is asked ahead (Q-0221); he assigns the Spearman and leaves the Regiment.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsCanAssign(spearman));
		assertTrue(scn.FreepsCanAssign(regiment));
		scn.FreepsAssignToMinions(aragorn, spearman);

		// The Spearman's one exertion, and nothing for the Regiment he did not assign.
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman));
		assertFalse(scn.IsCharAssigned(regiment));

		// The Regiment is the Shadow player's to assign, with the Nazgûl.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowGetShadowAssignmentTargets().contains(String.valueOf(regiment.getCardId())));
		assertTrue(scn.ShadowGetShadowAssignmentTargets().contains(String.valueOf(enquea.getCardId())));
	}
}
