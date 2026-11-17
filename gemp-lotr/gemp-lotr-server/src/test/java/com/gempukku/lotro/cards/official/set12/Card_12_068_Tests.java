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

public class Card_12_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "12_68");

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
					// Merry, From O'er the Brandywine: a [shire] Hobbit, strength 3, vitality 4; +2 only while he bears
					// a weapon, and he bears none.
					put("merry", "1_303");
					// Dwarf Guard: a [dwarven] Dwarf, strength 4, vitality 2, no text but a requirement to play.
					put("guard", "1_7");
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
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Gríma, Betrayer of Rohan
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Each time a companion loses a skirmish involving a [men] minion, you may exert Gríma to exert a companion.
		*/

		var scn = GetScenario();

		var grima = scn.GetFreepsCard("grima");

		assertEquals("Gríma", grima.getBlueprint().getTitle());
		assertEquals("Betrayer of Rohan", grima.getBlueprint().getSubtitle());
		assertTrue(grima.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, grima.getBlueprint().getSide());
		assertEquals(Culture.MEN, grima.getBlueprint().getCulture());
		assertEquals(CardType.MINION, grima.getBlueprint().getCardType());
		assertEquals(Race.MAN, grima.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(grima, Keyword.LURKER));
		assertEquals(2, grima.getBlueprint().getTwilightCost());
		assertEquals(4, grima.getBlueprint().getStrength());
		assertEquals(3, grima.getBlueprint().getVitality());
		assertEquals(3, grima.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenACompanionLosesToAMenMinionGrimaMayExertToExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);
		scn.MoveAlliesToTable(thrarin);
		// Vitality 2 with 1 wound: the Guard is exhausted.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));

		var grima = scn.GetShadowCard("grima");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, axeman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		// The Axeman's 11 beats Gimli's 6: one wound, and he lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowAcceptOptionalTrigger();

		// Gríma exerts, and not the Axeman.
		assertEquals(1, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(axeman));

		// Any companion who can exert, the loser among them; not the exhausted Guard, the ally or a minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, thrarin, grima, axeman));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ALossToAMenWizardIsOfferedAndDeclinedAndALossToGrimaHimselfIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gimli, merry);

		var grima = scn.GetShadowCard("grima");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, saruman},
				new PhysicalCardImpl[]{merry, grima});

		// Gríma is a lurker: Gimli's skirmish comes first.  Saruman's 8 beats Gimli's 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merry));

		// Gríma's own 4 beats Merry's 3: offered again.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(grima));
		scn.ShadowChooseCard(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedForACompanionWhoWinsForALossToAMinionOfAnotherCultureOrForAnAllyWhoLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var grima = scn.GetShadowCard("grima");
		var swarthy = scn.GetShadowCard("swarthy");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, swarthy, brigand, axeman);
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
		scn.PassSkirmishActions();
		assertInDiscard(swarthy);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));

		// The Brigand's 8 beats Gimli's 6: a loss to a [dunland] Man.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));

		// The Axeman's 11 beats Thrarin's 4, a [men] minion's win over an ally: he is overwhelmed.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedGrimaIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var grima = scn.GetShadowCard("grima");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, axeman);
		// Vitality 3 with 2 wounds: Gríma cannot exert.
		scn.AddWoundsToChar(grima, 2);
		assertEquals(2, scn.GetWoundsOn(grima));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, axeman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
