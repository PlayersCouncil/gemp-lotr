package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "11_129");
					put("orc2", "11_129");
					put("orc3", "11_129");
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("warg", "17_92");         // Vicious Warg: [orc] mount, bearer (an [orc] Orc of strength 9 or less) is fierce; +4 strength
					put("sam", "1_311");          // Sam, Son of Hamfast: [shire] Hobbit, resistance 5, vitality 4; a fellowship ability and a response to Frodo's death
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, resistance 6, vitality 4; a maneuver ability used only on purpose
					put("guard", "13_67");        // Guard of the White Tree: [gondor] Man, resistance 5; his skirmish ability needs a [gondor] condition, and there is none
					put("bounder", "1_286");      // Bounder: a [shire] Hobbit ally (resistance 0) whose home is site 2, where these assignments are
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Mountain Orc, and a companion of resistance 5.
	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "11_129");
					put("sam", "1_311");        // Sam, Son of Hamfast: resistance 5, vitality 4; a fellowship ability and a response to Frodo's death
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MountainOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Mountain Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion is assigned to skirmish a companion who has resistance 5 or less, exert that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orc");

		assertEquals("Mountain Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "Each time this minion is assigned to skirmish a companion who has resistance 5 or less, exert that
	// companion": no "may", so nobody is asked (Q-0017; Q-0220: G).  The definition offered the exertion to the
	// Shadow player, who could decline it.
	// F-0463.
	@Test
	public void TheCompanionIsExertedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsAssignToMinions(sam, orc);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void OnlyTheCompanionOfResistance5ThisMinionIsAssignedToIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, aragorn, guard);
		scn.MoveAlliesToTable(bounder);

		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, orc2, orc3, troop);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(guard));
		assertEquals(0, scn.GetWoundsOn(sam));

		// Sam (resistance 5) and Aragorn (6) to Mountain Orcs, the Bounder (an ally, no companion) to a third, and
		// the Guard (resistance 5) to a minion that is not a Mountain Orc.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, orc}, new PhysicalCardImpl[]{aragorn, orc2},
				new PhysicalCardImpl[]{bounder, orc3}, new PhysicalCardImpl[]{guard, troop});

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToACompanionWhomABurdenBringsToResistance5ItExertsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(orc);
		scn.SkipToAssignments();
		// 6 - 1 burden
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(aragorn, orc);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnExhaustedCompanionOfResistance5IsNotKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));

		var orc = scn.GetShadowCard("orc");

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.FreepsAssignToMinions(sam, orc);

		// Vitality 4 with 3 wounds: exhausted, he cannot exert.  A wound in its place would kill him.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInPlay(sam);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAgainInTheFierceAssignmentPhaseItExertsHimAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var orc = scn.GetShadowCard("orc");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(orc);
		scn.AttachCardsTo(orc, warg);
		scn.SkipToAssignments();
		assertAttachedTo(warg, orc);
		assertTrue(scn.HasKeyword(orc, Keyword.FIERCE));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(aragorn, orc);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Aragorn's 8 against 9 + 4 = 13: he loses and takes a wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, orc);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
