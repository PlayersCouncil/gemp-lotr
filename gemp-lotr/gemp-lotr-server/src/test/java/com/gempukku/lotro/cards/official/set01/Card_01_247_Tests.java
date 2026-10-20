package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_247_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foe", "1_247");

					put("inquisitor", "1_268");   // Orc Inquisitor: [sauron] Orc, strength 9, vitality 3; its text acts only as it is played
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] minion who is an Uruk-hai, strength 8; Damage +1 is its whole text
					put("runner", "1_178");       // Goblin Runner: a [moria] Orc, strength 5; its text acts only as it is played

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("merry", "1_303");        // Merry, From O'er the Brandywine: strength 3, vitality 4; his text needs a weapon
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnheartenedFoeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Enheartened Foe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If a [sauron] Orc wins a skirmish, make that Orc <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foe");

		assertEquals("Enheartened Foe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ASauronOrcWhoWinsASkirmishIsMadeFierceFightsAgainAndIsFierceNoLongerInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foe = scn.GetShadowCard("foe");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var soldier = scn.GetShadowCard("soldier");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(foe);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor, soldier);
		scn.SkipToAssignments();
		// The Orc Soldier is a second [sauron] Orc, in a skirmish of his own that has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, inquisitor}, new PhysicalCardImpl[]{aragorn, soldier});

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(foe));

		// 9 against 6: the Inquisitor wins.
		scn.ShadowPass();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowPlayAvailable(foe));
		assertFalse(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());

		scn.ShadowPlayCard(foe);
		assertTrue(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, foe);

		// 7 against 8: the Soldier loses his own skirmish to Aragorn.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(soldier));

		// A fierce minion is to be defended against a second time, and the Inquisitor alone is one.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(inquisitor));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(soldier));
		scn.FreepsAssignToMinions(aragorn, inquisitor);

		// The fierce skirmish is the last thing before the regroup phase: he is fierce through it.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		// 9 against 8: Aragorn loses.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(inquisitor, Keyword.FIERCE));
	}

	@Test
	public void WithTwoSauronOrcsWinningOneSkirmishTheOneChosenIsMadeFierceAndTheOtherIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foe = scn.GetShadowCard("foe");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var runner = scn.GetShadowCard("runner");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// 8 + 10 = 18, so that the four minions together (27) beat him without being more than double him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.MoveCardsToHand(foe);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, inquisitor, runner, sentry);
		scn.SkipToAssignments();
		// Aragorn takes the Soldier and the Shadow player gives him the Chieftain, a Goblin Runner and a Cirith
		// Ungol Sentry as well, so that they all win with the two [sauron] Orcs; the Inquisitor is a third [sauron]
		// Orc, assigned to Gimli, whose skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{gimli, inquisitor});
		scn.ShadowAssignToMinions(aragorn, chieftain, runner, sentry);

		// 7 + 7 + 5 + 8 against 18: all four win, and the Sentry's damage +1 makes it two wounds.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(foe));

		scn.ShadowPlayCard(foe);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		// A [moria] Orc and a [sauron] Uruk-hai who won this very skirmish, and a [sauron] Orc who did not
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, sentry, inquisitor));
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));

		scn.ShadowChooseCard(chieftain);
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(inquisitor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, foe);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void IsNotOfferedWhenASauronOrcLosesNorWhenAnOrcOfAnotherCultureOrASauronMinionWhoIsNoOrcWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foe = scn.GetShadowCard("foe");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, merry);
		scn.MoveCardsToHand(foe);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner},
				new PhysicalCardImpl[]{gimli, sentry},
				new PhysicalCardImpl[]{aragorn, soldier});

		// 5 against 3: a Goblin Runner, a [moria] Orc, wins.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 8 against 6: a Cirith Ungol Sentry, a [sauron] Uruk-hai, wins, and wounds twice for his damage +1.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 7 against 8: an Orc Soldier, a [sauron] Orc, loses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInZone(Zone.HAND, foe);
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
