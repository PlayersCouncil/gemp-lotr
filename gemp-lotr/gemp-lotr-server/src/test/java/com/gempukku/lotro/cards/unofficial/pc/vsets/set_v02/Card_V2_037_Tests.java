package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_037_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("evil1", "102_37");
					put("evil2", "102_37");

					// [moria] minions that are not unique.
					put("runner1", "1_178");        // Goblin Runner: Orc, strength 5, vitality 1.  Cheated in, so its text never happens.
					put("runner2", "1_178");
					put("troop", "1_177");          // Goblin Patrol Troop: Orc, strength 13, vitality 3, no text
					// Unique [moria] minions.  All three have site number 4.
					put("chieftain", "1_163");      // Ancient Chieftain: Orc, costs 4; strength +1 for each other [moria] Orc
					put("commander", "1_186");      // Guard Commander: Orc, costs 3; a skirmish ability that is only ever used on purpose
					put("balrog", "12_79");         // The Balrog, The Terror of Khazad-dum: a Balrog, costs 12, strength 17; Damage +1, and the rest of its text is for underground sites
					// Minions that are not [moria].
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability that is only ever used on purpose
					put("forger1", "3_56");         // Isengard Forger: "Shadow: Exert this minion to add (1)."
					put("forger2", "3_56");
					// Unique cards that Ancient Evil cannot itself stack, cheated onto it.
					put("lurtz", "1_127");          // Lurtz, Servant of Isengard: a unique [isengard] minion, costs 7, site number 5; his ability is for the maneuver phase
					put("longdark", "1_188");       // The Long Dark: a unique [moria] condition, costs 2, plays to the support area

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability that is only ever used on purpose
					put("pippin", "1_306");         // Pippin, Friend to Frodo: strength 3; his text is about [shire] tales
					put("dwarf", "1_7");            // Dwarf Guard: strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientEvilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Ancient Evil
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a [moria] minion wins a skirmish, you may stack it here.
		 * Shadow: Play a unique minion stacked here, it is twilight cost -1 for each other card stacked here.
		 * Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("evil1");

		assertEquals("Ancient Evil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil1");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 5 set + 1 companion + 2 for the site.
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.HAND, evil);
		assertTrue(scn.ShadowPlayAvailable(evil));

		scn.ShadowPlayCard(evil);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, evil);
		assertNotAttachedTo(evil, runner);
		assertEquals(0, scn.GetStackedCards(evil).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaMinionThatWinsASkirmishMayBeStackedOnTheCopyWhoseOfferIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, pippin);

		var evil1 = scn.GetShadowCard("evil1");
		var evil2 = scn.GetShadowCard("evil2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var soldier = scn.GetShadowCard("soldier");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(evil1, evil2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, soldier, balrog);
		scn.SkipToAssignments();
		// A Goblin Runner and a [sauron] Orc together on Aragorn; The Balrog on Pippin; the second Goblin Runner
		// fights nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{pippin, balrog});
		scn.ShadowAssignToMinions(aragorn, soldier);

		// 5 + 7 against 8: the minions win, and Aragorn is not overwhelmed.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Each copy offers once, for the Goblin Runner; nothing is offered for the Orc that won beside it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil2));
		assertEquals(2, scn.ShadowGetActionCount());
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);

		scn.ShadowChooseOptionalTrigger(evil2);
		assertInZone(Zone.STACKED, runner1);
		assertTrue(scn.GetStackedCards(evil2).contains(runner1));
		assertEquals(0, scn.GetStackedCards(evil1).size());
		// Neither the [sauron] Orc that won with it nor the Goblin Runner that fought nobody has gone anywhere.
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, runner2);

		// The other copy's offer has gone with the Runner: there is no longer a minion in play for it to stack, and
		// an offer that costs nothing and would do nothing is not made (Q-0034; withheld by the engine since Q-0224).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(evil1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(evil2));
		assertTrue(scn.GetStackedCards(evil2).contains(runner1));
		assertEquals(0, scn.GetStackedCards(evil1).size());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, runner2);

		// The Balrog, unique and no Orc, overwhelms Pippin.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil2));
		scn.ShadowChooseOptionalTrigger(evil1);
		assertInZone(Zone.STACKED, balrog);
		assertTrue(scn.GetStackedCards(evil1).contains(balrog));
		assertEquals(1, scn.GetStackedCards(evil1).size());
		assertEquals(1, scn.GetStackedCards(evil2).size());
		// Nor is the second copy's offer for The Balrog made once the first has taken it.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(evil2));

		assertInZone(Zone.SUPPORT, evil1, evil2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAMoriaMinionThatLosesOrAMinionOfAnotherCultureThatWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		scn.MoveCompanionsToTable(aragorn, dwarf);

		var evil = scn.GetShadowCard("evil1");
		var runner = scn.GetShadowCard("runner1");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{dwarf, soldier});

		// Goblin Runner (5) loses to Aragorn (8) and dies of it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Orc Soldier, a [sauron] Orc (7), beats Dwarf Guard (4).
		scn.FreepsResolveSkirmish(dwarf);
		assertEquals(0, scn.GetWoundsOn(dwarf));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(dwarf));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(0, scn.GetStackedCards(evil).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesTheMinionInPlayAndItIsOfferedAgainWhenItWinsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var evil = scn.GetShadowCard("evil1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Fierce, so that it skirmishes twice in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.SkipToAssignments();

		// 13 against 8: Goblin Patrol Troop wins without overwhelming.
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(0, scn.GetStackedCards(evil).size());

		// The fierce skirmish.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, troop);
		assertTrue(scn.GetStackedCards(evil).contains(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoMoriaMinionsThatWinOneSkirmishAreEachOfferedAndOneMayBeStackedWithoutTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var evil = scn.GetShadowCard("evil1");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.ShadowAssignToMinions(aragorn, runner2);

		// 5 + 5 against 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// One offer for each Goblin Runner.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil));
		assertEquals(2, scn.ShadowGetActionCount());

		// The game asks which of the two winners an accepted offer is for.
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2));
		scn.ShadowChooseCard(runner2);
		assertInZone(Zone.STACKED, runner2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);

		// The other offer is left.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(evil));
		assertEquals(1, scn.ShadowGetActionCount());
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertEquals(1, scn.GetStackedCards(evil).size());
		assertTrue(scn.GetStackedCards(evil).contains(runner2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowActionPlaysAChosenUniqueMinionStackedHereAtOneLessForEachOtherCardStackedHereAndDiscardsThisCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil1 = scn.GetShadowCard("evil1");
		var evil2 = scn.GetShadowCard("evil2");
		var chieftain = scn.GetShadowCard("chieftain");
		var balrog = scn.GetShadowCard("balrog");
		var longdark = scn.GetShadowCard("longdark");
		var runner = scn.GetShadowCard("runner1");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToSupportArea(evil1, evil2);
		// Two unique minions, a unique condition, and a minion that is not unique.
		scn.StackCardsOn(evil1, chieftain, balrog, longdark, runner);
		// A unique minion that is not [moria], with nothing beside it.
		scn.StackCardsOn(evil2, lurtz);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 20 set + 1 companion + 2 for the site.
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(evil1));
		assertTrue(scn.ShadowActionAvailable(evil2));

		scn.ShadowUseCardAction(evil1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, balrog));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(longdark, runner, lurtz));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(balrog);

		// The Balrog costs 12, less 3 for the three cards left beside it, plus 2 for roaming (its site number is
		// 4 and the fellowship is at site 2).
		assertEquals(23 - (12 - 3 + 2), scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertEquals(0, scn.GetWoundsOn(balrog));
		// This copy is discarded, and what was left on it goes with it.
		assertInDiscard(evil1, chieftain, longdark, runner);
		assertInZone(Zone.SUPPORT, evil2);
		assertEquals(1, scn.GetStackedCards(evil2).size());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Lurtz is alone on the other copy: his full 7, plus 2 for roaming (site number 5).
		scn.ShadowUseCardAction(evil2);
		assertEquals(12 - (7 + 2), scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, lurtz);
		assertInDiscard(evil2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AUniqueMinionStackedHereThatCannotBePaidForIsNotOneThatMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil1");
		var chieftain = scn.GetShadowCard("chieftain");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(evil);
		scn.StackCardsOn(evil, chieftain, balrog);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 companion + 2 for the site.  The Balrog would be 12 - 1 + 2.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(evil));

		// Nothing to choose: Ancient Chieftain comes out for 4, less 1 for The Balrog, plus 2 for roaming.
		scn.ShadowUseCardAction(evil);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(8 - (4 - 1 + 2), scn.GetTwilight());
		assertInDiscard(evil, balrog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedUntilAUniqueMinionStackedHereCanBePaidForNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil1 = scn.GetShadowCard("evil1");
		var evil2 = scn.GetShadowCard("evil2");
		var commander = scn.GetShadowCard("commander");
		var runner = scn.GetShadowCard("runner1");
		var longdark = scn.GetShadowCard("longdark");
		var forger1 = scn.GetShadowCard("forger1");
		var forger2 = scn.GetShadowCard("forger2");
		scn.MoveCardsToSupportArea(evil1, evil2);
		// Guard Commander, unique, alone here: 3, plus 2 for roaming.
		scn.StackCardsOn(evil1, commander);
		// No unique minion here: a Goblin Runner (1, plus 2 for roaming) and a unique condition (2).
		scn.StackCardsOn(evil2, runner, longdark);

		scn.StartGame();
		scn.MoveMinionsToTable(forger1, forger2);
		scn.FreepsPass();

		// 1 companion + 2 for the site.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(evil1));
		assertFalse(scn.ShadowActionAvailable(evil2));

		// "Shadow: Exert this minion to add (1)."  One short, and then enough.
		scn.ShadowUseCardAction(forger1);
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(evil1));
		scn.ShadowUseCardAction(forger2);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(evil1));
		assertFalse(scn.ShadowActionAvailable(evil2));

		// Not in the maneuver phase, with the pool still at 5.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(evil1));
		assertEquals(1, scn.GetStackedCards(evil1).size());
	}

	@Test
	public void ShadowActionIsOfferedOnceThePoolCoversTheReducedCostAndNotOneShortOfIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil1");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");
		var forger = scn.GetShadowCard("forger1");
		scn.MoveCardsToSupportArea(evil);
		// Ancient Chieftain costs 4, less 2 for the two cards beside him, plus 2 for roaming: 4.
		scn.StackCardsOn(evil, chieftain, runner, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.FreepsPass();

		// 1 companion + 2 for the site: one short.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(evil));

		// "Shadow: Exert this minion to add (1)."
		scn.ShadowUseCardAction(forger);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(evil));

		scn.ShadowUseCardAction(evil);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInDiscard(evil, runner, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
