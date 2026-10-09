package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_010_Tests
{

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: companion to open a 2nd skirmish (vs backstabber)
					put("urukFighter", "1_146"); // Uruk Fighter: damage+1, twilight 3 - the "damaged" cost target
					put("butcher", "6_60"); // Berserk Butcher: damage+1, twilight 6 - 2nd valid cost target, and the
					// sacrificial skirmish opponent (a skirmish must be assigned or the engine skips the skirmish phase)
					put("backstabber", "1_174"); // Goblin Backstabber: no damage keyword - invalid cost target, valid
					// effect target, and the 2nd skirmish's opponent (fights the Ring-bearer)
					put("berserker", "10_40"); // Easterling Berserker: a [raider] Man who is damage +2 while unwounded; his skirmish ability needs 2 other [raider] Men
					put("patrol", "1_177"); // Goblin Patrol Troop: strength 13, no text and no damage bonus
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_10"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North: strength 8, the companion in the skirmish
					put("urukFighter", "1_146"); // Uruk Fighter: damage+1 - a minion that could pay
					put("backstabber", "1_174"); // Goblin Backstabber: no damage keyword - a minion to be made stronger
					put("berserker", "10_40"); // Easterling Berserker: a [raider] Man who is damage +2 while unwounded; his skirmish ability needs 2 other [raider] Men
					put("patrol", "1_177"); // Goblin Patrol Troop: strength 13, no text and no damage bonus
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_10", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_10
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Skirmish: Make your minion damage -X until the regroup phase to make another
		 * minion strength +X until the regroup phase.
		 */

		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		assertEquals("Race Text 94_10", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void TradesDamageForStrengthUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var urukFighter = scn.GetShadowCard("urukFighter");
		var butcher = scn.GetShadowCard("butcher");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(urukFighter, butcher, backstabber);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, backstabber},
				new PhysicalCardImpl[]{aragorn, butcher}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		int backstabberStr = scn.GetStrength(backstabber);

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Cost filter: only damage-carrying minions are offered
		assertTrue(scn.ShadowHasCardChoiceAvailable(urukFighter));
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(backstabber));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(urukFighter);

		// X is bounded by the chosen minion's actual damage bonus: damage +1, so X can only be 1 and
		// the game takes it without asking.

		// Effect target: any other minion (butcher or backstabber), not the damaged one
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher));
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(urukFighter));
		// A companion is no minion, in the skirmish or out of it.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(backstabber);

		assertEquals(0, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(backstabberStr + 1, scn.GetStrength(backstabber));
		// The Butcher, who could have paid and did not, keeps his bonus.
		assertEquals(1, scn.GetKeywordCount(butcher, Keyword.DAMAGE));

		// Conclude both skirmishes (aragorn/butcher, then frodo/backstabber) before checking
		// expiration - with nothing else pending in either, the engine cascades straight through
		// both - so this crosses an actual 2nd skirmish first, rather than going directly from the
		// skirmish the mod was used in to regroup with no intervening skirmish at all
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(backstabberStr + 1, scn.GetStrength(backstabber));
		scn.BothPass();
		scn.FreepsDeclineOptionalTrigger(); //Ring

		// Duration: both modifications expire at regroup
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(backstabberStr, scn.GetStrength(backstabber));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UnavailableWithoutADamagedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, backstabber);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OwnerGatingShadowCantUseWhenFreepsOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 94_10's ability says "your minion" - a Shadow minion is never "your" for a Freeps-owned
		// mod, so the ability must be absent for the non-owner (Shadow).
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var urukFighter = scn.GetShadowCard("urukFighter");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(urukFighter, backstabber);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, urukFighter);
		assertFalse(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();

		// The Uruk Fighter is player 2's and has a damage bonus to give up, but the modifier is player 1's.
		assertEquals(1, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void XOfTwoTakesBothDamageBonusesOfTheChosenMinionAndGivesStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var berserker = scn.GetShadowCard("berserker");
		var urukFighter = scn.GetShadowCard("urukFighter");
		var patrol = scn.GetShadowCard("patrol");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(berserker, urukFighter, patrol);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, patrol);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(2, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(patrol, Keyword.DAMAGE));
		assertEquals(13, scn.GetStrength(patrol));
		assertEquals(8, scn.GetStrength(urukFighter));
		assertEquals(11, scn.GetStrength(berserker));

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// A [raider] Man and an [isengard] Uruk-hai, neither of them in the skirmish, may pay; the Patrol Troop has no bonus to give up.
		assertTrue(scn.ShadowHasCardChoiceAvailable(berserker));
		assertTrue(scn.ShadowHasCardChoiceAvailable(urukFighter));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(patrol));
		scn.ShadowChooseCard(berserker);

		// X is 1 or 2: the Berserker's own damage +2, not the 3 the two minions have between them.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertTrue(scn.ShadowHasCardChoiceAvailable(urukFighter));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(berserker));
		scn.ShadowChooseCard(patrol);

		assertEquals(0, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		// 13 + 2
		assertEquals(15, scn.GetStrength(patrol));
		assertEquals(8, scn.GetStrength(urukFighter));
		assertEquals(11, scn.GetStrength(berserker));

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerOneGivesUpOneOfTwoDamageBonusesOnPlayerTwosTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetShadowCard("aragorn");
		var berserker = scn.GetFreepsCard("berserker");
		var patrol = scn.GetFreepsCard("patrol");

		scn.StartGame();
		// Player 2's turn: player 1, who owns the modifier, is the Shadow player and the minions are his.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(berserker, patrol);
		scn.SkipToPhaseInverted(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.ShadowAssignToMinions(aragorn, patrol);
		scn.FreepsDeclineAssignments();
		scn.ShadowResolveSkirmish(aragorn);
		scn.ShadowPass();

		assertEquals(2, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		assertEquals(13, scn.GetStrength(patrol));
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// The Berserker is the only minion with a bonus to give up, so nobody is asked which; X is 1 or 2.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// The Patrol Troop is the only other minion.
		assertEquals(1, scn.GetKeywordCount(berserker, Keyword.DAMAGE));
		// 13 + 1
		assertEquals(14, scn.GetStrength(patrol));
		assertEquals(11, scn.GetStrength(berserker));

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
