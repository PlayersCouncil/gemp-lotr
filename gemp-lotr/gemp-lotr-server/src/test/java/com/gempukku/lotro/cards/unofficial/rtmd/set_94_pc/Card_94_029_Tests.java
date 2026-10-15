package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_029_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Backstabber: [moria] Orc, not fierce; damage +2 only in a skirmish beside
					// another [moria] Orc
					put("backstabber", "1_174");
					// Uruk Savage: [isengard] Uruk-hai; Damage +1 is its whole text
					put("savage", "1_151");
					// Strength Born of Fear: [sauron] support-area condition, a Shadow card that is no
					// minion; its skirmish ability costs (3)
					put("fear", "1_278");
					// Gate Troll: [sauron] Troll that prints fierce, to read the keyword's count with
					// the modifier's fierce beside its own
					put("gateTroll", "6_103");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_29", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: [moria] Orc, not fierce
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, not fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_29"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_29
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: Your opponent's minions are fierce.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_29", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(5, mod.getBlueprint().getIntensity());
	}

	// Because modifiers run as the card's owner, a Freeps-owned copy makes the Shadow player's
	// minions fierce, and a Shadow-owned copy has no effect on that turn.

	@Test
	public void OpponentsMinionsBecomeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var backstabber = scn.GetShadowCard("backstabber");
		var savage = scn.GetShadowCard("savage");
		var fear = scn.GetShadowCard("fear");
		var frodo = scn.GetRingBearer();

		scn.MoveMinionsToTable(backstabber, savage);
		scn.MoveCardsToSupportArea(fear);
		scn.StartGame();

		assertTrue(scn.HasKeyword(backstabber, Keyword.FIERCE));
		// "Minions": an Uruk-hai of another culture
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		// The opponent's Shadow card that is no minion, and the owner's own character, are not
		assertFalse(scn.HasKeyword(fear, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(frodo, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentsMinionsBecomeFierceOnPlayerTwosTurnWhenPlayerTwoOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		// Player 2 owns the mod, and on his own turn the minions are player 1's: his opponent's
		var backstabber = scn.GetFreepsCard("backstabber");
		var savage = scn.GetFreepsCard("savage");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.MoveMinionsToTable(backstabber, savage);

		assertTrue(scn.HasKeyword(backstabber, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void RunnersOwnMinionsAreNotAffectedWhenShadowOwnsMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var backstabber = scn.GetShadowCard("backstabber");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();

		scn.MoveMinionsToTable(backstabber, savage);
		scn.StartGame();

		// not(your) never matches the runner's own minion
		assertFalse(scn.HasKeyword(backstabber, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		// Player 1's Ring-bearer is the mod owner's opponent's, and no minion
		assertFalse(scn.HasKeyword(frodo, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AlreadyFierceMinionKeywordCountIsRecorded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gateTroll = scn.GetShadowCard("gateTroll");

		scn.MoveMinionsToTable(gateTroll);
		scn.StartGame();

		assertTrue(scn.HasKeyword(gateTroll, Keyword.FIERCE));
		// Pinned: the mod's Fierce stacks with the printed one rather than being deduplicated.
		assertEquals(2, scn.GetKeywordCount(gateTroll, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
