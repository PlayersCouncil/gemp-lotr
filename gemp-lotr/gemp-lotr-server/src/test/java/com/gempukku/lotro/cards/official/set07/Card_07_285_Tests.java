package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_285_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defender", "7_285");
					put("defender2", "7_285");
					// Cirith Ungol Sentry: a [sauron] minion who is no Orc (an Uruk-hai), damage +1 and nothing else
					put("sentry", "10_84");
					// Orc Chieftain: a [sauron] Orc; his text answers only a weapon played on him
					put("chieftain", "1_266");
					// Goblin Runner: a [moria] Orc, a minion of another culture; its text answers its own play only
					put("runner", "1_178");
					// Strength Born of Fear: a [sauron] card that is no minion; its skirmish ability is never used
					put("fear", "1_278");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorDefenderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Defender
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Exert this minion and spot another [sauron] minion to add a threat.
		*/

		var scn = GetScenario();

		var defender = scn.GetFreepsCard("defender");

		assertEquals("Mordor Defender", defender.getBlueprint().getTitle());
		assertNull(defender.getBlueprint().getSubtitle());
		assertFalse(defender.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, defender.getBlueprint().getSide());
		assertEquals(Culture.SAURON, defender.getBlueprint().getCulture());
		assertEquals(CardType.MINION, defender.getBlueprint().getCardType());
		assertEquals(Race.ORC, defender.getBlueprint().getRace());
		assertEquals(2, defender.getBlueprint().getTwilightCost());
		assertEquals(7, defender.getBlueprint().getStrength());
		assertEquals(2, defender.getBlueprint().getVitality());
		assertEquals(6, defender.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsTheDefenderToAddAThreatWhileAnotherSauronMinionWhoIsNoOrcCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, sentry);
		scn.SkipToAssignments();
		// The Sentry is left unassigned: it is only spotted.
		scn.FreepsAssignToMinions(aragorn, defender);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(defender));

		scn.ShadowUseCardAction(defender);
		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedToTheDefenderOutOfAnySkirmishBesideASauronOrcWhoIsInOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, chieftain);
		scn.SkipToAssignments();
		// The Defender is left unassigned: the text asks for no skirmish of his own.
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(defender));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(defender));

		scn.ShadowUseCardAction(defender);
		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedBesideAMinionOfAnotherCultureAndASauronConditionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, defender);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Defender can exert, but nothing else he can spot is a [sauron] minion: not himself, not a [moria]
		// Orc, not a [sauron] condition.
		assertEquals(0, scn.GetWoundsOn(defender));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(defender));

		// The same board with a [sauron] minion added.
		scn.MoveMinionsToTable(sentry);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(defender));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheDefenderIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, sentry);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(defender, 1);
		assertEquals(1, scn.GetWoundsOn(defender));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, defender);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(defender));
	}

	@Test
	public void WithTwoCopiesInOneSkirmishEachSpotsTheOtherAndOnlyTheOneUsedExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var defender2 = scn.GetShadowCard("defender2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, defender2);
		scn.SkipToAssignments();
		// Both copies skirmish Aragorn: the second is the leftover minion the Shadow player assigns.
		scn.FreepsAssignToMinions(aragorn, defender);
		scn.ShadowAssignToMinions(aragorn, defender2);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, defender));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, defender2));
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(defender2));
		assertEquals(0, scn.GetThreats());
		// No other [sauron] minion: each copy is the other's "another [sauron] minion".
		assertTrue(scn.ShadowActionAvailable(defender));
		assertTrue(scn.ShadowActionAvailable(defender2));

		scn.ShadowUseCardAction(defender);
		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(defender2));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
