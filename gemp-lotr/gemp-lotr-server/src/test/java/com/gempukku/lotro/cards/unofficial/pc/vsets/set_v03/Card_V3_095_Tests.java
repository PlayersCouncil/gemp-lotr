package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_095_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horror", "103_95");
					put("sky1", "103_97");        // Ominous Sky: a [sauron] twilight condition, cost 2, nothing needed to play it; its texts answer the play of an Orc or Troll and cost (2) in the Shadow or regroup phase
					put("sky2", "103_97");
					put("fear", "103_64");        // Cover of Darkness, Omen of Fear: a [ringwraith] twilight condition; its ability is an assignment action that needs 3 [ringwraith] minions
					put("servants", "1_219");     // The Nine Servants of Sauron: a [ringwraith] condition that is not twilight
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: a twilight card that is no condition; a Nazgul, vitality 3
					put("soldier", "1_271");      // Orc Soldier: a [sauron] Orc, strength 7, vitality 2; a skirmish ability that is only ever used on purpose
					put("cavetroll", "1_165");    // Cave Troll of Moria, Scourge of the Black Pit: a [moria] Troll, strength 15, vitality 4; damage +1, fierce
					put("runner", "1_178");       // Goblin Runner: a [moria] Orc of vitality 1, who can never exert
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] Uruk-hai, strength 8, vitality 2; damage +1 is its whole text
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, no skirmish ability
					put("gimli", "1_13");         // Gimli, Son of Gloin: Skirmish: exert Gimli to make him strength +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Horror
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Twilight.
		 * 	To play, hinder 2 twilight conditions.
		 * 	Skirmish: Exert an Orc or Troll and hinder a twilight condition to make that minion strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horror");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Horror", card.getBlueprint().getSubtitle());
		// Two dots: no more than two copies in play
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayHindersTwoTwilightConditionsOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(horror);
		// Twilight conditions: two that are [sauron], and a Cover of Darkness that is [ringwraith].
		// And a condition that is not twilight.
		scn.MoveCardsToSupportArea(sky1, sky2, fear, servants);

		scn.StartGame();
		// A twilight card that is no condition
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(horror));

		scn.ShadowPlayCard(horror);

		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, sky2, fear));
		assertFalse(scn.ShadowHasCardChoiceAvailable(servants));
		assertFalse(scn.ShadowHasCardChoiceAvailable(nelya));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		scn.ShadowChooseCards(sky1, sky2);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 11 - 2 to play
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, horror);
		assertFalse(scn.IsHindered(horror));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFewerThanTwoTwilightConditionsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(horror, sky2);
		// One twilight condition that can be hindered. Beside it: one that is hindered already, a condition
		// that is not twilight, and (below) a twilight minion.
		scn.MoveCardsToSupportArea(sky1, fear, servants);
		scn.HinderCard(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(horror));

		// A second twilight condition, played from hand
		scn.ShadowPlayCard(sky2);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(horror));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		// With exactly two there is nothing to choose.
		scn.ShadowPlayCard(horror);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 9 - 2 to play
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, horror);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionExertsAnOrcOrTrollAndHindersATwilightConditionToMakeThatMinionStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var fear = scn.GetShadowCard("fear");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");
		var soldier = scn.GetShadowCard("soldier");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var runner = scn.GetShadowCard("runner");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		// Twilight conditions of two cultures beside this one, one already hindered, and a condition that
		// is not twilight
		scn.MoveCardsToSupportArea(horror, fear, sky1, sky2, servants);
		scn.HinderCard(sky2);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// A [sauron] Orc and a [moria] Troll; an Orc who cannot exert; a [sauron] Uruk-hai and a Nazgul,
		// who is also a twilight card that is no condition
		scn.MoveMinionsToTable(soldier, cavetroll, runner, sentry, nelya);

		// Not an assignment action
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(horror));
		scn.ShadowPass();

		// Aragorn fights the Orc; the Troll is assigned to Gimli and waits his turn.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{gimli, cavetroll});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(horror));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(0, scn.GetWoundsOn(cavetroll));

		scn.ShadowUseCardAction(horror);

		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, cavetroll));
		assertFalse(scn.ShadowHasCardChoiceAvailable(runner));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sentry));
		assertFalse(scn.ShadowHasCardChoiceAvailable(nelya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(cavetroll);

		assertEquals(1, scn.GetWoundsOn(cavetroll));
		assertEquals(0, scn.GetWoundsOn(soldier));

		// Any twilight condition, this one included
		assertTrue(scn.ShadowHasCardChoiceAvailable(horror, fear, sky1));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sky2));
		assertFalse(scn.ShadowHasCardChoiceAvailable(servants));
		assertFalse(scn.ShadowHasCardChoiceAvailable(nelya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertFalse(scn.IsHindered(fear));
		scn.ShadowChooseCard(fear);

		assertTrue(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(horror));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(servants));
		assertEquals(17, scn.GetStrength(cavetroll));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(sentry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedAgainByHinderingItselfAndTheBonusesLastForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var soldier = scn.GetShadowCard("soldier");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToSupportArea(horror, sky1);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, cavetroll);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{gimli, cavetroll});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(15, scn.GetStrength(cavetroll));

		// The Orc, who is fighting, by hindering the other twilight condition
		scn.ShadowUseCardAction(horror);
		scn.ShadowChooseCard(soldier);
		scn.ShadowChooseCard(sky1);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(horror));
		assertEquals(9, scn.GetStrength(soldier));
		assertEquals(15, scn.GetStrength(cavetroll));

		// Again: the Orc is exhausted now, so it is the Troll, whose own skirmish is still to come; and
		// the only twilight condition left to hinder is this one.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(horror));
		scn.ShadowUseCardAction(horror);

		assertEquals(1, scn.GetWoundsOn(cavetroll));
		assertTrue(scn.IsHindered(horror));
		assertEquals(17, scn.GetStrength(cavetroll));
		assertEquals(9, scn.GetStrength(soldier));

		// Still so when the Shadow player is last asked in this skirmish
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(17, scn.GetStrength(cavetroll));
		assertEquals(9, scn.GetStrength(soldier));
		scn.ShadowPass();

		// Aragorn (8) loses to the Orc (9) and takes a wound. In the Troll's own skirmish both are back
		// to their printed strength.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(15, scn.GetStrength(cavetroll));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithOnlyAnUrukHaiAndANazgulToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sentry = scn.GetShadowCard("sentry");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(horror, sky1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Two minions able to exert, one of them [sauron] and fighting, and twilight conditions to hinder
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertFalse(scn.IsHindered(horror));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.ShadowActionAvailable(horror));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedUntilAnOrcOrTrollCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var runner = scn.GetShadowCard("runner");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToSupportArea(horror, sky1);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// An Orc of vitality 1, and a Troll of vitality 4 with 3 wounds: neither can exert.
		scn.MoveMinionsToTable(cavetroll, runner);
		scn.AddWoundsToChar(cavetroll, 3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, cavetroll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(gimli);

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(horror));

		scn.ShadowPass();
		// One wound fewer, and Gimli acts again so that the Shadow player is asked anew.
		scn.RemoveWoundsFromChar(cavetroll, 1);
		scn.FreepsUseCardAction(gimli);

		assertEquals(2, scn.GetWoundsOn(cavetroll));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertTrue(scn.ShadowActionAvailable(horror));

		// The Troll is the only one who can pay.
		scn.ShadowUseCardAction(horror);
		assertEquals(3, scn.GetWoundsOn(cavetroll));
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.ShadowChooseCard(sky1);

		assertTrue(scn.IsHindered(sky1));
		assertEquals(17, scn.GetStrength(cavetroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
