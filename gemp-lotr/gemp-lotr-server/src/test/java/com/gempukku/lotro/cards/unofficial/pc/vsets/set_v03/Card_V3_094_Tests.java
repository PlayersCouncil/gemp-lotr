package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_094_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dread", "103_94");
					put("dread2", "103_94");
					put("sky1", "103_97");        // Ominous Sky: a [sauron] twilight condition, cost 2, nothing needed to play it; its texts answer the play of an Orc or Troll and cost (2) in the Shadow or regroup phase
					put("sky2", "103_97");
					put("fear", "103_64");        // Cover of Darkness, Omen of Fear: a [ringwraith] twilight condition; its ability is an assignment action that needs 3 [ringwraith] minions
					put("servants", "1_219");     // The Nine Servants of Sauron: a [ringwraith] condition that is not twilight
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: a twilight card that is no condition
					put("soldier", "1_271");      // Orc Soldier: a [sauron] Orc; a skirmish ability that is only ever used on purpose
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc with no text
					put("squealer", "8_77");      // Morgul Squealer: a [ringwraith] Orc; Shadow-phase abilities only
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] Uruk-hai, strength 8; damage +1 is its whole text
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the only [gondor] Man
					put("gimli", "1_13");         // Gimli, Son of Gloin: a third companion, so that 3 threats can be added
					put("power1", "1_308");       // Power According to His Stature: Skirmish: add a burden to wound each minion skirmishing the Ring-bearer
					put("power2", "1_308");
					put("stout1", "7_121");       // Stout Resistance: a skirmish event: add a threat to make a [gondor] Man strength +3
					put("stout2", "7_121");
					put("ninefingered1", "10_112"); // Nine-fingered Frodo and the Ring of Doom: a skirmish event: exert a [shire] character to remove a burden or 2 threats
					put("ninefingered2", "10_112");
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
		 * Name: Cover of Darkness, Omen of Dread
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Twilight.
		 * 	To play, hinder 2 twilight conditions.
		 * 	Response: If a burden is added, hinder an Orc and this condition to add a threat.
		 * 	Response: If a threat is added, hinder an Orc and this condition to add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dread");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Dread", card.getBlueprint().getSubtitle());
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

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(dread);
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
		assertTrue(scn.ShadowPlayAvailable(dread));

		scn.ShadowPlayCard(dread);

		assertTrue(scn.ShadowHasCardChoiceAvailable(sky1, sky2, fear));
		assertFalse(scn.ShadowHasCardChoiceAvailable(servants));
		assertFalse(scn.ShadowHasCardChoiceAvailable(nelya));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertFalse(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(fear));

		scn.ShadowChooseCards(sky2, fear);

		assertTrue(scn.IsHindered(sky2));
		assertTrue(scn.IsHindered(fear));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 11 - 2 to play
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, dread);
		assertFalse(scn.IsHindered(dread));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFewerThanTwoTwilightConditionsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");

		scn.MoveCardsToHand(dread, sky2);
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
		assertFalse(scn.ShadowPlayAvailable(dread));

		// A second twilight condition, played from hand
		scn.ShadowPlayCard(sky2);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(dread));
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		// With exactly two there is nothing to choose.
		scn.ShadowPlayCard(dread);

		assertTrue(scn.IsHindered(sky1));
		assertTrue(scn.IsHindered(sky2));
		assertFalse(scn.IsHindered(servants));
		assertFalse(scn.IsHindered(nelya));
		// 9 - 2 to play
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, dread);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BurdenAddedMayBeAnsweredByHinderingAnOrcAndThisConditionToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var squealer = scn.GetShadowCard("squealer");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var power1 = scn.GetFreepsCard("power1");

		scn.MoveCardsToSupportArea(dread);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(power1);

		scn.StartGame();
		// Orcs of two cultures, an Orc already hindered, and a [sauron] minion that is no Orc
		scn.MoveMinionsToTable(sentry, soldier, troop, squealer);
		scn.HinderCard(squealer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(dread));

		// The Free Peoples player adds a burden to play an event.
		scn.FreepsPlayCard(power1);

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		// One response of the two
		assertEquals(1, scn.ShadowGetActionCount());
		scn.ShadowChooseOptionalTrigger(dread);

		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, troop));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sentry));
		assertFalse(scn.ShadowHasCardChoiceAvailable(squealer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(soldier);

		assertTrue(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(sentry));
		assertTrue(scn.IsHindered(dread));
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThreatAddedMayBeAnsweredByHinderingAnOrcAndThisConditionToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var squealer = scn.GetShadowCard("squealer");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stout1 = scn.GetFreepsCard("stout1");

		scn.MoveCardsToSupportArea(dread);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(stout1);

		scn.StartGame();
		// Orcs of two cultures, an Orc already hindered, and a [sauron] minion that is no Orc
		scn.MoveMinionsToTable(sentry, soldier, troop, squealer);
		scn.HinderCard(squealer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(dread));

		// The Free Peoples player adds a threat to play an event.
		scn.FreepsPlayCard(stout1);

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		// One response of the two
		assertEquals(1, scn.ShadowGetActionCount());
		scn.ShadowChooseOptionalTrigger(dread);

		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, troop));
		assertFalse(scn.ShadowHasCardChoiceAvailable(sentry));
		assertFalse(scn.ShadowHasCardChoiceAvailable(squealer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(troop);

		assertTrue(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(sentry));
		assertTrue(scn.IsHindered(dread));
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SecondCopyAnswersTheThreatTheFirstAddedWithABurdenAndThereItEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var dread2 = scn.GetShadowCard("dread2");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var power1 = scn.GetFreepsCard("power1");

		scn.MoveCardsToSupportArea(dread, dread2);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(power1);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, soldier, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(power1);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		// Each copy makes its own offer, and only the one.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread2));
		assertEquals(2, scn.ShadowGetActionCount());
		scn.ShadowChooseOptionalTrigger(dread);
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.IsHindered(dread));
		assertFalse(scn.IsHindered(dread2));
		assertFalse(scn.IsHindered(troop));

		// That threat was added by a Shadow card. The copy that added it is hindered and answers nothing;
		// the other answers it, with the one Orc left.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dread));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread2));
		assertEquals(1, scn.ShadowGetActionCount());
		scn.ShadowChooseOptionalTrigger(dread2);

		assertTrue(scn.IsHindered(dread2));
		assertTrue(scn.IsHindered(troop));
		// 1 from the event + 1 from the second copy
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SecondCopyAnswersTheBurdenTheFirstAddedWithAThreatAndThereItEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var dread2 = scn.GetShadowCard("dread2");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stout1 = scn.GetFreepsCard("stout1");

		scn.MoveCardsToSupportArea(dread, dread2);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(stout1);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, soldier, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(stout1);

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		// Each copy makes its own offer, and only the one.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread2));
		assertEquals(2, scn.ShadowGetActionCount());
		scn.ShadowChooseOptionalTrigger(dread2);
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.IsHindered(dread2));
		assertFalse(scn.IsHindered(dread));
		assertFalse(scn.IsHindered(soldier));

		// That burden was added by a Shadow card. The copy that added it is hindered and answers nothing;
		// the other answers it, with the one Orc left.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dread2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		assertEquals(1, scn.ShadowGetActionCount());
		scn.ShadowChooseOptionalTrigger(dread);

		assertTrue(scn.IsHindered(dread));
		assertTrue(scn.IsHindered(soldier));
		// 1 from the event + 1 from the second copy; 3 companions, so a second threat can be added
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NeitherResponseIsOfferedWithNoOrcToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var soldier = scn.GetShadowCard("soldier");
		var squealer = scn.GetShadowCard("squealer");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var power1 = scn.GetFreepsCard("power1");
		var power2 = scn.GetFreepsCard("power2");
		var stout1 = scn.GetFreepsCard("stout1");

		scn.MoveCardsToSupportArea(dread);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(power1, power2, stout1);

		scn.StartGame();
		// A [sauron] minion that is no Orc, and an Orc that is hindered already
		scn.MoveMinionsToTable(sentry, squealer);
		scn.HinderCard(squealer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(power1);
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		scn.FreepsPlayCard(stout1);
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.IsHindered(dread));

		// With an Orc on the table the next burden is answered.
		scn.MoveMinionsToTable(soldier);
		scn.ShadowPass();
		scn.FreepsPlayCard(power2);

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		scn.ShadowChooseOptionalTrigger(dread);

		assertTrue(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(dread));
		assertFalse(scn.IsHindered(sentry));
		// 1 from the event + 1 from the response
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BurdenOrThreatsRemovedAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var dread = scn.GetShadowCard("dread");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var ninefingered1 = scn.GetFreepsCard("ninefingered1");
		var ninefingered2 = scn.GetFreepsCard("ninefingered2");
		var power1 = scn.GetFreepsCard("power1");

		scn.MoveCardsToSupportArea(dread);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(ninefingered1, ninefingered2, power1);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, soldier);
		scn.AddBurdens(2);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Frodo is the only [shire] character and exerts for each event.
		scn.FreepsPlayCard(ninefingered1);
		scn.FreepsChoose("burden");
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		scn.FreepsPlayCard(ninefingered2);
		scn.FreepsChoose("threats");
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.IsHindered(dread));
		assertFalse(scn.IsHindered(soldier));
		scn.ShadowPass();

		// A burden added on the same board is.
		scn.FreepsPlayCard(power1);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
	}

	@Test
	public void ResponseDeclinedHindersNothingAndTheConditionAnswersTheNextThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dread = scn.GetShadowCard("dread");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var power1 = scn.GetFreepsCard("power1");
		var stout1 = scn.GetFreepsCard("stout1");

		scn.MoveCardsToSupportArea(dread);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(power1, stout1);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(power1);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(dread));
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		scn.FreepsPlayCard(stout1);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		scn.ShadowChooseOptionalTrigger(dread);

		// The only Orc is hindered without a choice.
		assertTrue(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(dread));
		// 1 from the first event + 1 from the response
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThreatAnsweredWhileTheRingBearerIsHinderedAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var dread = scn.GetShadowCard("dread");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stout1 = scn.GetFreepsCard("stout1");

		scn.MoveCardsToSupportArea(dread);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(stout1);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// While the Ring-bearer is hindered, burdens cannot be added.
		scn.HinderCard(frodo);

		assertTrue(scn.IsHindered(frodo));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsHindered(soldier));
		assertFalse(scn.IsHindered(dread));

		scn.FreepsPlayCard(stout1);

		assertEquals(1, scn.GetThreats());
		// The response has a cost, so it is offered though its burden cannot be added.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dread));
		scn.ShadowChooseOptionalTrigger(dread);

		// The costs are paid, the only Orc hindered without a choice, and no burden is added.
		assertTrue(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(dread));
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
