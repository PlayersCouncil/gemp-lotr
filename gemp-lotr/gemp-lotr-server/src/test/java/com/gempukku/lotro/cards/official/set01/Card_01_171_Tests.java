package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_171_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frenzy", "1_171");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, vitality 4
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man companion, vitality 3; not overwhelmed unless tripled
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4, Ring-bound by rule but not the Ring-bearer; a fellowship ability and a response used only on purpose
					put("bounder", "1_286");    // Bounder: Hobbit ally, vitality 2, home site 2 of this path, where he may be assigned

					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its text answers only its own play
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("tentacle", "101_36");  // Vile Tentacle: [moria] minion who is no Orc (a Creature)
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers only a weapon played on him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrenzyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Frenzy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: <b>Assignment:</b> Assign an exhausted companion (except the Ring-bearer) to skirmish a [moria] Orc.
		*/

		var scn = GetScenario();

		var frenzy = scn.GetFreepsCard("frenzy");

		assertEquals("Frenzy", frenzy.getBlueprint().getTitle());
		assertNull(frenzy.getBlueprint().getSubtitle());
		assertFalse(frenzy.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, frenzy.getBlueprint().getSide());
		assertEquals(Culture.MORIA, frenzy.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, frenzy.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(frenzy, Timeword.ASSIGNMENT));
		assertEquals(2, frenzy.getBlueprint().getTwilightCost());
	}

	@Test
	public void AssignsAnExhaustedCompanionWhoIsNotTheRingBearerToAMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frenzy = scn.GetShadowCard("frenzy");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var tentacle = scn.GetShadowCard("tentacle");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(aragorn, merry, boromir, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(frenzy);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(merry, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(bounder, 1);
		// Exhausted: Frodo (the Ring-bearer), Aragorn, Merry, Sam (Ring-bound, not the Ring-bearer) and the ally; Boromir is one wound short of it.
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(aragorn));
		assertEquals(1, scn.GetVitality(merry));
		assertEquals(1, scn.GetVitality(sam));
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(2, scn.GetVitality(boromir));
		scn.MoveMinionsToTable(runner, troop, tentacle, chieftain);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(frenzy));
		scn.ShadowPlayCard(frenzy);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, merry, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, boromir, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(merry);

		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tentacle, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertTrue(scn.IsCharAssignedAgainst(merry, troop));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(runner));
		assertInZone(Zone.DISCARD, frenzy);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frenzy = scn.GetShadowCard("frenzy");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(frenzy);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 3);
		assertEquals(1, scn.GetVitality(merry));
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.ShadowPlayAvailable(frenzy));
	}

	@Test
	public void WithNoExhaustedCompanionButTheRingBearerTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frenzy = scn.GetShadowCard("frenzy");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(frenzy);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(boromir, 1);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(2, scn.GetVitality(boromir));
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// CR 4.2 "effect": an event may be played though it will have no effect.
		assertTrue(scn.ShadowPlayAvailable(frenzy));
		scn.ShadowPlayCard(frenzy);

		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(boromir));
		assertFalse(scn.IsCharAssigned(runner));
		assertInZone(Zone.DISCARD, frenzy);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
