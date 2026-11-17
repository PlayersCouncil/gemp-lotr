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

public class Card_11_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haradrim", "11_86");
					// A second copy, assigned by the Shadow player.
					put("haradrim2", "11_86");
					// Axeman of Harad: another [men] Man minion, strength 11.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");

					// Aragorn, Ranger of the North: resistance 6, strength 8.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: resistance exactly 5.  His abilities are never used.
					put("sam", "1_311");
					// Barliman Butterbur: a [gandalf] Man ally, resistance 0 (CR 4.2 "resistance").  His fellowship
					// ability is never used.
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InvadingHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Invading Haradrim
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time the Free Peoples player assigns this minion to a skirmish, add (1) for each Free Peoples character who has resistance 5 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haradrim");

		assertEquals("Invading Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerItAddsOneForEachFreePeoplesCharacterOfResistanceFiveOrLessAndNothingWhenTheShadowPlayerAssignsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);

		var haradrim = scn.GetShadowCard("haradrim");
		var haradrim2 = scn.GetShadowCard("haradrim2");

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim, haradrim2);

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(barliman));

		scn.SkipToAssignments();
		int pool = scn.GetTwilight();

		scn.FreepsAssignToMinions(aragorn, haradrim);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, haradrim));
		// Sam (5) and the ally Barliman (0); not Aragorn (6), Frodo (10), nor the two Shadow characters on the
		// table.  A trigger that answered either copy's assignment would add 4.
		assertEquals(pool + 2, scn.GetTwilight());

		// The Shadow player assigns the other copy, which is left over: nothing is added.
		scn.ShadowAssignToMinions(sam, haradrim2);

		assertTrue(scn.IsCharAssignedAgainst(sam, haradrim2));
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ResistanceIsReadAsItStandsAndAnotherMenMinionAssignedBesideItAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);

		var haradrim = scn.GetShadowCard("haradrim");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim, axeman);
		scn.AddBurdens(1);

		// One burden takes 1 off every companion's resistance; the ally's stays 0.
		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(barliman));

		scn.SkipToAssignments();
		int pool = scn.GetTwilight();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, haradrim},
				new PhysicalCardImpl[]{sam, axeman});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, haradrim));
		assertTrue(scn.IsCharAssignedAgainst(sam, axeman));
		// Aragorn (5), Sam (4) and Barliman (0).  Had the Axeman's assignment set it off too, 6.
		assertEquals(pool + 3, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAgainInASecondAssignmentPhaseOfTheTurnItAddsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var haradrim = scn.GetShadowCard("haradrim");

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim);

		assertEquals(5, scn.GetResistance(sam));

		scn.SkipToAssignments();
		int pool = scn.GetTwilight();

		scn.FreepsAssignToMinions(aragorn, haradrim);
		// Sam alone.
		assertEquals(pool + 1, scn.GetTwilight());

		// 8 against 8: the minion wins, and both live.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, haradrim);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		int pool2 = scn.GetTwilight();

		scn.FreepsAssignToMinions(aragorn, haradrim);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, haradrim));
		assertEquals(pool2 + 1, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
