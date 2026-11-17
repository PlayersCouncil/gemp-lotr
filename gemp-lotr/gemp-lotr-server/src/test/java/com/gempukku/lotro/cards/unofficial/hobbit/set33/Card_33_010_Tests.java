package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_010_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// and skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chariot", "33_10");
					put("balin", "30_2");       // Balin, Brother of Dwalin: the one Balin; [dwarven] Dwarf, 7/3; "Fellowship: Exert Balin to shuffle a Free Peoples event from your discard pile into your draw deck"
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: another [dwarven] Dwarf companion, 7/3, damage +1; a maneuver ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] Dwarf ally; a fellowship ability only

					// [dwarven] events whose cost exerts a Dwarf. Battle of Azanulbizar (maneuver, cost 0): "Exert a Dwarf
					// character to wound 2 Orcs or to wound 1 Orc twice". Noble intentions (skirmish, cost 0): "Exert a
					// companion (except Bilbo) to make Bilbo strength +3".
					put("azanulbizar", "30_4");
					put("azanulbizar2", "30_4");
					put("noble", "30_14");
					// Moon-Letters: an [elven] Fellowship event; "Exert a Dwarf companion (or spot a [dwarven] possession) to
					// take into hand an [elven] or [mirkwood] card from your draw deck or discard pile"
					put("moon", "33_15");
					put("moon2", "33_15");

					// For the discard pile: skirmish events of twilight 0 (Halfling Deftness [shire], Bred for Battle
					// [isengard], Honed [dwarven], Stout Resistance [gondor]); Battle Fury, a [dwarven] skirmish event
					// of cost 1; Betsy Butterbur, a [gandalf] ally of cost 0.
					put("deftness", "1_293");
					put("bred", "1_121");
					put("honed", "8_6");
					put("stout", "7_121");
					put("fury", "30_3");
					put("betsy", "33_19");

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, 5/1; a "when you play" text, never reached
					put("runner2", "31_30");
					put("bats", "32_30");       // Dark Bats: [moria] Creature, 4/2; a Shadow ability left unused
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void IronHillsChariotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Iron Hills Chariot
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Each time you exert Balin to play a [dwarven] event, you may heal him.
		 * 	<b>Skirmish:</b> Exert Balin to make a [dwarven] companion strength +1 for each skirmish event with a twilight cost of 0 in your discard pile (limit +4).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chariot");

		assertEquals("Iron Hills Chariot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeBalinExertsToPlayADwarvenEventHeMayHealOfEitherPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var noble = scn.GetFreepsCard("noble");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(chariot);
		scn.AddWoundsToChar(balin, 1);
		scn.AddWoundsToChar(dwalin, 1);

		scn.StartGame();
		scn.MoveCardsToHand(azanulbizar, noble);
		scn.MoveMinionsToTable(runner, runner2, bats);
		assertInHand(azanulbizar, noble);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(azanulbizar);
		scn.FreepsChooseCard(balin);
		assertEquals(2, scn.GetWoundsOn(balin));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chariot));
		scn.FreepsAcceptOptionalTrigger();
		// Balin heals; Dwalin's wound is untouched.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		// Two Orcs of 1 vitality: "wound 1 Orc twice" cannot be done in full, and "wound 2 Orcs" is done unasked.
		assertInDiscard(runner, runner2, azanulbizar);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A [dwarven] event of another phase, played in a skirmish, exerting Balin again: offered again.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, bats);
		scn.FreepsResolveSkirmish(bilbo);
		scn.FreepsPlayCard(noble);
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, dwalin));
		scn.FreepsChooseCard(balin);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chariot));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertEquals(6, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DwalinPayingForADwarvenEventOffersNothingAndBalinsHealMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var azanulbizar2 = scn.GetFreepsCard("azanulbizar2");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(chariot);
		scn.AddWoundsToChar(balin, 1);

		scn.StartGame();
		scn.MoveCardsToHand(azanulbizar, azanulbizar2);
		scn.MoveMinionsToTable(runner, runner2);
		assertInHand(azanulbizar, azanulbizar2);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(azanulbizar);
		// Dwalin, a [dwarven] Dwarf who is no Balin, pays.
		scn.FreepsChooseCard(dwalin);
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chariot));
		assertInDiscard(runner, runner2);
		assertEquals(1, scn.GetWoundsOn(balin));
		scn.ShadowPass();

		// Balin pays for the second: the heal is offered, and declined.
		scn.FreepsPlayCard(azanulbizar2);
		scn.FreepsChooseCard(balin);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chariot));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dwalin));
		assertInDiscard(azanulbizar2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void BalinExertingForHisOwnAbilityOrForAnElvenEventOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var moon = scn.GetFreepsCard("moon");
		var moon2 = scn.GetFreepsCard("moon2");
		var deftness = scn.GetFreepsCard("deftness");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(chariot);

		scn.StartGame();
		scn.MoveCardsToHand(moon);
		// Moon-Letters takes the other copy from the discard pile; Balin shuffles Halfling Deftness, a Free Peoples
		// event, into the deck.
		scn.MoveCardsToDiscard(moon2, deftness);
		scn.FreepsRefreshDecision();
		assertInHand(moon);
		assertInDiscard(moon2, deftness);
		assertEquals(0, scn.GetWoundsOn(balin));

		// Balin exerts for his own Fellowship ability: no event is played.
		scn.FreepsUseCardAction(balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		scn.FreepsChooseCard(deftness);
		assertInZone(Zone.DECK, deftness);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chariot));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Balin exerts to play an [elven] event.
		scn.FreepsPlayCard(moon);
		scn.FreepsChoose("Exert a Dwarf companion");
		assertEquals(2, scn.GetWoundsOn(balin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chariot));
		// The other copy, the one [elven] card, is taken from the discard pile unasked.
		assertInHand(moon2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsBalinToMakeADwarvenCompanionPlusOneForEachSkirmishEventOfCostZeroInHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var grimir = scn.GetFreepsCard("grimir");
		var noble = scn.GetFreepsCard("noble");
		var deftness = scn.GetFreepsCard("deftness");
		var bred = scn.GetFreepsCard("bred");
		var fury = scn.GetFreepsCard("fury");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var betsy = scn.GetFreepsCard("betsy");
		var shadowNoble = scn.GetShadowCard("noble");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(chariot);

		scn.StartGame();
		// Three skirmish events of cost 0, of three cultures and both sides. Not: a skirmish event of cost 1, a
		// maneuver event of cost 0, an ally of cost 0, nor a skirmish event of cost 0 in the other player's pile.
		scn.MoveCardsToDiscard(noble, deftness, bred, fury, azanulbizar, betsy, shadowNoble);
		scn.MoveMinionsToTable(runner);
		assertInDiscard(noble, deftness, bred, fury, azanulbizar, betsy, shadowNoble);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.FreepsResolveSkirmish(balin);

		assertTrue(scn.FreepsActionAvailable(chariot));
		scn.FreepsUseCardAction(chariot);
		// Balin exerts; Dwalin, of his culture and race, does not.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));

		// [dwarven] companions, one skirmishing and one not; not a [shire] companion nor a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, dwalin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, grimir));
		scn.FreepsChooseCard(dwalin);

		// 7 + 3.
		assertEquals(10, scn.GetStrength(dwalin));
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSkirmishEventOfCostZeroInTheDiscardPileTheSkirmishActionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fury = scn.GetFreepsCard("fury");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var betsy = scn.GetFreepsCard("betsy");
		var shadowNoble = scn.GetShadowCard("noble");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(chariot);

		scn.StartGame();
		scn.MoveCardsToDiscard(fury, azanulbizar, betsy, shadowNoble);
		scn.MoveMinionsToTable(runner);
		assertInDiscard(fury, azanulbizar, betsy, shadowNoble);
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.FreepsResolveSkirmish(balin);

		assertTrue(scn.FreepsActionAvailable(chariot));
		scn.FreepsUseCardAction(chariot);
		assertEquals(1, scn.GetWoundsOn(balin));
		scn.FreepsChooseCard(dwalin);
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void FiveSkirmishEventsOfCostZeroGiveNoMoreThanPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var noble = scn.GetFreepsCard("noble");
		var deftness = scn.GetFreepsCard("deftness");
		var bred = scn.GetFreepsCard("bred");
		var honed = scn.GetFreepsCard("honed");
		var stout = scn.GetFreepsCard("stout");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(chariot);

		scn.StartGame();
		scn.MoveCardsToDiscard(noble, deftness, bred, honed, stout);
		scn.MoveMinionsToTable(runner);
		assertInDiscard(noble, deftness, bred, honed, stout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.FreepsResolveSkirmish(balin);

		scn.FreepsUseCardAction(chariot);
		assertEquals(1, scn.GetWoundsOn(balin));
		scn.FreepsChooseCard(balin);
		// 7 + 4, not 7 + 5.
		assertEquals(11, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(dwalin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheLimitIsPlusFourForTheWholeSkirmishAcrossCompanionsAndUsesAndRenewsInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var noble = scn.GetFreepsCard("noble");
		var deftness = scn.GetFreepsCard("deftness");
		var bred = scn.GetFreepsCard("bred");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(chariot);

		scn.StartGame();
		// Balin's vitality raised by hand from 3 to 5, so that he can pay four times.
		scn.ApplyAdHocModifier(new VitalityModifier(null, balin, null, 2));
		scn.MoveCardsToDiscard(noble, deftness, bred);
		scn.MoveMinionsToTable(runner, bats);
		assertInDiscard(noble, deftness, bred);
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner}, new PhysicalCardImpl[]{dwalin, bats});
		scn.FreepsResolveSkirmish(balin);

		// +3 to Balin.
		scn.FreepsUseCardAction(chariot);
		scn.FreepsChooseCard(balin);
		assertEquals(10, scn.GetStrength(balin));
		scn.ShadowPass();

		// +3 again, to Dwalin: only +1 is left of the +4.
		scn.FreepsUseCardAction(chariot);
		assertEquals(2, scn.GetWoundsOn(balin));
		scn.FreepsChooseCard(dwalin);
		assertEquals(8, scn.GetStrength(dwalin));
		assertEquals(10, scn.GetStrength(balin));
		scn.ShadowPass();

		// At the limit it is still offered; Balin exerts and nobody is stronger.
		assertTrue(scn.FreepsActionAvailable(chariot));
		scn.FreepsUseCardAction(chariot);
		assertEquals(3, scn.GetWoundsOn(balin));
		scn.FreepsChooseCard(dwalin);
		assertEquals(8, scn.GetStrength(dwalin));
		assertEquals(10, scn.GetStrength(balin));

		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// The next skirmish: the bonuses are gone, and the limit has renewed.
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(7, scn.GetStrength(balin));
		scn.FreepsUseCardAction(chariot);
		assertEquals(4, scn.GetWoundsOn(balin));
		scn.FreepsChooseCard(dwalin);
		assertEquals(10, scn.GetStrength(dwalin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileBalinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chariot = scn.GetFreepsCard("chariot");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var noble = scn.GetFreepsCard("noble");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(chariot);
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		scn.MoveCardsToDiscard(noble);
		scn.MoveMinionsToTable(runner);
		assertInDiscard(noble);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwalin, runner);
		scn.FreepsResolveSkirmish(dwalin);

		// Dwalin could exert, but he is no Balin.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(chariot));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertEquals(7, scn.GetStrength(dwalin));
	}
}
