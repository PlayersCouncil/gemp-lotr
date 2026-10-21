package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("malice", "3_79");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text
					put("runner", "1_178");       // Goblin Runner: [moria] Orc; its trigger is on being played
					put("bowman", "2_60");        // Goblin Bowman: [moria] Orc, Archer and nothing else
					put("troll", "1_165");        // Cave Troll of Moria: [moria] Troll, damage +1 and fierce; its other texts are about playing it: a [moria] minion that is no Orc
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("taskmaster", "2_90");    // Orc Taskmaster: [sauron] Orc, a Shadow ability used only on purpose
					put("band", "1_270");         // Orc Scouting Band: [sauron] Orc tracker; a roaming discount and a skirmish ability
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, Damage +1 and nothing else: a [sauron] minion that is no Orc
					put("forger", "3_56");        // Isengard Forger: [isengard] Orc, a Shadow ability used only on purpose

					put("legolas", "1_50");       // Legolas, Greenleaf: Archer companion, vitality 3; his archery ability is used only on purpose
					put("orophin", "1_56");       // Orophin, Lorien Bowman: Archer ally; his archery ability is used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion who is no archer; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MaliceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Malice
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Spot a [moria] Orc and a [sauron] Orc to wound an archer.
		*/

		var scn = GetScenario();

		var malice = scn.GetFreepsCard("malice");

		assertEquals("Malice", malice.getBlueprint().getTitle());
		assertNull(malice.getBlueprint().getSubtitle());
		assertFalse(malice.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, malice.getBlueprint().getSide());
		assertEquals(Culture.MORIA, malice.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, malice.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(malice, Timeword.ARCHERY));
		assertEquals(0, malice.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableWithoutASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var troop = scn.GetShadowCard("troop");
		var forger = scn.GetShadowCard("forger");
		var guard = scn.GetShadowCard("guard");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(malice);

		scn.StartGame();
		// A [moria] Orc, an Orc of another culture, and a [sauron] minion that is no Orc: Cirith Ungol Guard, an
		// Uruk-hai. Legolas is an archer to wound.
		scn.MoveMinionsToTable(troop, forger, guard);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(malice));

		scn.MoveMinionsToTable(taskmaster);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(malice));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotPlayableWithoutAMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var forger = scn.GetShadowCard("forger");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(malice);

		scn.StartGame();
		// A [sauron] Orc, an Orc of another culture, and a [moria] minion that is no Orc: the Cave Troll, cheated in
		// (the [moria] Orc it was played beside is gone).
		scn.MoveMinionsToTable(taskmaster, forger, troll);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(malice));

		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(malice));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WoundsAnArcherOfEitherSideAndAnExhaustedArcherIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var troop = scn.GetShadowCard("troop");
		var bowman = scn.GetShadowCard("bowman");
		var soldier = scn.GetShadowCard("soldier");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(malice);
		// Legolas has vitality 3: with two wounds he is exhausted, and a wound kills him where an exertion could not.
		scn.AddWoundsToChar(legolas, 2);
		assertEquals(2, scn.GetWoundsOn(legolas));

		scn.StartGame();
		scn.MoveMinionsToTable(troop, bowman, soldier);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(bowman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.ShadowPlayAvailable(malice));

		scn.ShadowPlayCard(malice);
		// An archer companion, an archer ally and the Shadow player's own archer minion; a companion and minions who
		// are no archers are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, orophin, bowman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo, troop, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(legolas);

		assertInZone(Zone.DEAD, legolas);
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(bowman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInDiscard(malice);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WoundsAnArcherWhoIsNotExhaustedOnceAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(malice);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));

		scn.ShadowPlayCard(malice);
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, orophin));
		scn.ShadowChooseCard(orophin);

		// Orophin has vitality 3: one wound, not two, and the archer not chosen is untouched.
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertInDiscard(malice);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithNoArcherOnTheTableItIsPlayedAndNobodyIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var runner = scn.GetShadowCard("runner");
		var band = scn.GetShadowCard("band");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(malice);

		scn.StartGame();
		// A [moria] Orc and a [sauron] Orc of other titles; nobody on the table is an archer.
		scn.MoveMinionsToTable(runner, band);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(band));
		// An event may be played though its target is missing.
		assertTrue(scn.ShadowPlayAvailable(malice));

		scn.ShadowPlayCard(malice);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(band));
		assertInDiscard(malice);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
