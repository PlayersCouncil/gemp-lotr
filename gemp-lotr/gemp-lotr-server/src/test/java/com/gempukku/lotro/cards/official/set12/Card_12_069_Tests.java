package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "12_69");

					// Saruman, Coldly Still: a [men] minion who is no Man (Wizard), strength 8, vitality 4.  His +2 is
					// for [men] minions bearing a possession, and none does; his skirmish ability is never used.
					put("saruman", "17_48");
					// Axeman of Harad: a [men] Man, strength 11, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Swarthy Hillman: a [men] Man, strength 7, vitality 1.  An archer only while its player controls a
					// site, and none is controlled.
					put("swarthy", "15_93");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text.
					put("brigand", "4_10");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, vitality 3.  A skirmish ability, never used.
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2.  His maneuver ability lets him take
					// part in skirmishes until the regroup phase.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HarryingHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Harrying Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Each time a companion loses a skirmish involving a [men] minion, add (2).
		*/

		var scn = GetScenario();

		var hillman = scn.GetFreepsCard("hillman");

		assertEquals("Harrying Hillman", hillman.getBlueprint().getTitle());
		assertNull(hillman.getBlueprint().getSubtitle());
		assertFalse(hillman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hillman.getBlueprint().getSide());
		assertEquals(Culture.MEN, hillman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, hillman.getBlueprint().getCardType());
		assertEquals(Race.MAN, hillman.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(hillman, Keyword.LURKER));
		assertEquals(3, hillman.getBlueprint().getTwilightCost());
		assertEquals(9, hillman.getBlueprint().getStrength());
		assertEquals(2, hillman.getBlueprint().getVitality());
		assertEquals(4, hillman.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachTimeACompanionLosesToAMenMinionTwoTwilightAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var hillman = scn.GetShadowCard("hillman");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, hillman},
				new PhysicalCardImpl[]{gimli, saruman});

		// The Hillman is a lurker: Gimli's skirmish is resolved first.
		scn.FreepsResolveSkirmish(gimli);
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Saruman's 8 beats Gimli's 6: a companion loses to a [men] minion who is no Man.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(pool + 2, scn.GetTwilight());

		// The Hillman's own 9 beats Aragorn's 8: the second loss adds (2) again.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(pool + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsAddedForACompanionWhoWinsForALossToAMinionOfAnotherCultureOrForAnAllyWhoLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var hillman = scn.GetShadowCard("hillman");
		var swarthy = scn.GetShadowCard("swarthy");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, swarthy, brigand, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, swarthy},
				new PhysicalCardImpl[]{gimli, brigand},
				new PhysicalCardImpl[]{thrarin, axeman});
		scn.ShadowDeclineAssignments();

		// Aragorn's 8 beats the Swarthy Hillman's 7, a [men] minion: a companion who wins.
		scn.FreepsResolveSkirmish(aragorn);
		int pool = scn.GetTwilight();
		scn.PassSkirmishActions();
		assertInDiscard(swarthy);
		assertEquals(pool, scn.GetTwilight());

		// The Brigand's 8 beats Gimli's 6: a loss to a [dunland] Man.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(pool, scn.GetTwilight());

		// The Axeman's 11 beats Thrarin's 4, a [men] minion's win over an ally: he is overwhelmed.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
