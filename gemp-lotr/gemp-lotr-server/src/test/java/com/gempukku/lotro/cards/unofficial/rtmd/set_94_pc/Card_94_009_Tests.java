package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_009_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_143"); // Troop of Uruk-hai: twilight 5, qualifies
					put("butcher", "6_60"); // Berserk Butcher: twilight 6, 2nd qualifying target
					put("backstabber", "1_174"); // Goblin Backstabber: twilight 1, does not qualify
					put("raiders", "1_158"); // Uruk-hai Raiding Party: twilight 4, one short; damage +1 is its whole text
					put("matriarch", "103_42"); // Bladetusk Matriarch: a [raider] artifact of twilight 5 that lies in the support area; a Shadow card of the cost that is no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_9"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_143"); // Troop of Uruk-hai: twilight 5, qualifies
					put("patrol", "1_177"); // Goblin Patrol Troop: a [moria] Orc of twilight 6 with no text
					put("raiders", "1_158"); // Uruk-hai Raiding Party: twilight 4, one short; damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_9", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_9
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Regroup: Discard your minion of twilight cost 5 or more to add a burden.
		 */

		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		assertEquals("Race Text 94_9", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void DiscardingAHeavyMinionAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var troop = scn.GetShadowCard("troop");
		var butcher = scn.GetShadowCard("butcher");
		var backstabber = scn.GetShadowCard("backstabber");
		var raiders = scn.GetShadowCard("raiders");
		var matriarch = scn.GetShadowCard("matriarch");

		scn.MoveMinionsToTable(troop, butcher, backstabber, raiders);
		scn.MoveCardsToSupportArea(matriarch);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass(); // Freeps acts first in the back-and-forth regroup actions

		int b = scn.GetBurdens();
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Filter specificity: only the twilight 5+ minions qualify
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(backstabber));
		// Twilight 4 is one short, and the Matriarch costs 5 but is an artifact.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(matriarch));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertInDiscard(troop);
		assertEquals(b + 1, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, butcher, backstabber, raiders);
		assertInZone(Zone.SUPPORT, matriarch);

		// Control alternates to Freeps (who has nothing to do) and back to Shadow
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void UnavailableWithoutAQualifyingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var raiders = scn.GetShadowCard("raiders");

		// The Raiding Party's twilight cost is 4. At site 1 it is roaming and would take 6 to play,
		// which is no part of its twilight cost.
		scn.MoveMinionsToTable(backstabber, raiders);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OwnerGatingFreepsCantUseEvenWithAQualifyingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var troop = scn.GetShadowCard("troop");

		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();

		// The Troop is player 2's minion and would pay, but the modifier is player 1's.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
	}

	@Test
	public void TheOwnerAsPlayerOneDiscardsHisMinionOnPlayerTwosTurnAndPlayerTwoTakesTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var patrol = scn.GetFreepsCard("patrol");
		var raiders = scn.GetFreepsCard("raiders");

		scn.StartGame();
		// Player 2's turn: player 1, who owns the modifier, is the Shadow player and the minions are his.
		scn.SkipToOtherPlayersTurn();
		scn.MoveMinionsToTable(patrol, raiders);
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.ShadowPass();

		// The burdens read are those of player 2, whose fellowship it is.
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// The Patrol Troop (twilight 6) is the only minion of his that may pay, so nobody is asked which.
		assertInDiscard(patrol);
		assertInZone(Zone.SHADOW_CHARACTERS, raiders);
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
