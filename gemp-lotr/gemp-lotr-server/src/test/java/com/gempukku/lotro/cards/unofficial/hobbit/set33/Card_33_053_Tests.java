package com.gempukku.lotro.cards.unofficial.hobbit.set33;

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

public class Card_33_053_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer, the one Bilbo in play.
	// The skirmishes of turn 1 are at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("worrywort", "33_53");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4, damage +1; a fellowship ability
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] Dwarf ally, home 9H; Thorin +1, and a regroup offer that wants a follower
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: [gandalf] Wizard companion; a cost modifier and a fellowship ability

					// Events for the hand. Battle Fury: a [dwarven] skirmish event (a Dwarf character +3), two copies.
					// Take up arms. Fight!: a [gandalf] skirmish event (spot Gandalf). Battle of Azanulbizar: a [dwarven]
					// maneuver event. An Invisible Ring: a [shire] skirmish event that wants The One Ring worn.
					put("fury1", "30_3");
					put("fury2", "30_3");
					put("takeup", "30_31");
					put("azanulbizar", "30_4");
					put("invisible", "31_39");

					put("spider", "31_55");     // Wicked Spider: [spider] Spider, 7/2, fierce; a skirmish ability that wants an Orc to discard
					put("bats", "32_30");       // Dark Bats: [moria] Creature, 4/2; a Shadow ability left unused
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, 5/1; a "when you play" text, never reached
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, 6/2; an Assignment ability used only on purpose
					put("gollum", "31_20");     // Gollum, Small Slimy Creature: [gollum] minion, 5/4; a win trigger and an Assignment ability
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, 8/1; a "when you play" text, never reached
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void MasterWorrywortStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Master Worrywort, Hobbit Farmer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 3
		 * Home: 1H
		 * Game Text: Each time Bilbo wins a skirmish, you may heal a [dwarven] or [shire] character.<br><b>Skirmish:</b> Play a skirmish event to make a minion (except Gollum) skirmishing Bilbo strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("worrywort");

		assertEquals("Master Worrywort", card.getBlueprint().getTitle());
		assertEquals("Hobbit Farmer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 1)));
	}

	@Test
	public void EachTimeBilboWinsASkirmishADwarvenOrShireCharacterMayBeHealedAndTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var worrywort = scn.GetFreepsCard("worrywort");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var gandalf = scn.GetFreepsCard("gandalf");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(balin, gandalf);
		scn.MoveAlliesToTable(worrywort, dain);
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(dain, 1);
		scn.AddWoundsToChar(worrywort, 1);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(bilbo, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(spider);
		// Bilbo's 3 raised to 8, to beat the fierce Spider's 7 twice.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 5));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(worrywort));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();

		// Bilbo's first win: the heal is offered and taken.
		assertEquals(1, scn.GetWoundsOn(spider));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worrywort));
		scn.FreepsAcceptOptionalTrigger();
		// [dwarven] characters (a companion and an ally) and [shire] characters (Bilbo, and Master Worrywort himself),
		// each wounded; not Gandalf, a wounded [gandalf] companion, nor the wounded Spider.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, dain, bilbo, worrywort));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, spider));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(balin);

		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(worrywort));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(spider));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassFierceSkirmishActions();

		// His second win offers it again; declined, nobody is healed.
		assertInZone(Zone.DISCARD, spider);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worrywort));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(1, scn.GetWoundsOn(worrywort));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BilboLosingASkirmishAndAnotherCompanionWinningOneOfferNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var worrywort = scn.GetFreepsCard("worrywort");
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var bats = scn.GetShadowCard("bats");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, thorin);
		scn.MoveAlliesToTable(worrywort);
		scn.AddWoundsToChar(balin, 1);
		scn.AddWoundsToChar(worrywort, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(bats, runner);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(worrywort));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, bats}, new PhysicalCardImpl[]{thorin, runner});

		// Thorin, 7, beats Goblin Runner, 5: a companion who is no Bilbo wins.
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(worrywort));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Bilbo, 3, loses to Dark Bats, 4, and lives.
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(worrywort));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(worrywort));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BilboWinningWithNoDwarvenOrShireCharacterWoundedOffersNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var worrywort = scn.GetFreepsCard("worrywort");
		var balin = scn.GetFreepsCard("balin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(balin, gandalf);
		scn.MoveAlliesToTable(worrywort);
		// Gandalf wounded: a [gandalf] companion, nothing for the heal to act on.
		scn.AddWoundsToChar(gandalf, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		// Bilbo's 3 raised to 6, to beat Dark Bats' 4.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 3));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(worrywort));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, bats);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(bats));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(worrywort));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishActionPlaysASkirmishEventToMakeAMinionSkirmishingBilboStrengthMinus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var worrywort = scn.GetFreepsCard("worrywort");
		var thorin = scn.GetFreepsCard("thorin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fury = scn.GetFreepsCard("fury1");
		var takeup = scn.GetFreepsCard("takeup");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var aggressor = scn.GetShadowCard("aggressor");
		var gollum = scn.GetShadowCard("gollum");
		var bats = scn.GetShadowCard("bats");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCompanionsToTable(thorin, gandalf);
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToHand(fury, takeup, azanulbizar);
		assertInZone(Zone.HAND, fury, takeup, azanulbizar);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, gollum, bats, footman);
		// Strength enough for Bilbo to win against three minions and live: the board is about the minions.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 20));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, aggressor}, new PhysicalCardImpl[]{thorin, footman});
		// Two more minions to Bilbo: Gollum and a Creature of another culture.
		scn.ShadowAssignToMinions(bilbo, gollum, bats);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(7, scn.GetStrength(thorin));
		assertTrue(scn.FreepsActionAvailable(worrywort));
		scn.FreepsUseCardAction(worrywort);
		// Skirmish events of two cultures; not a maneuver event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, takeup));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(azanulbizar));
		scn.FreepsChooseCard(fury);

		// Battle Fury is played, on Thorin, its only Dwarf character.
		assertInZone(Zone.DISCARD, fury);
		assertEquals(10, scn.GetStrength(thorin));
		// The Orc and the Creature skirmishing Bilbo; not Gollum beside them, nor the Orc assigned to Thorin whose
		// skirmish is still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, bats));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum, footman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aggressor);

		assertEquals(4, scn.GetStrength(aggressor));
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// Still -2 at the next decision of the same skirmish.
		scn.ShadowPass();
		assertEquals(4, scn.GetStrength(aggressor));

		// Bilbo wins; in the next skirmish, a phase of its own, the Aggressor is back at 6.
		scn.FreepsPass();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionInAnotherSkirmishWhileBilboWaitsIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var worrywort = scn.GetFreepsCard("worrywort");
		var thorin = scn.GetFreepsCard("thorin");
		var fury = scn.GetFreepsCard("fury1");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToHand(fury);
		assertInZone(Zone.HAND, fury);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, footman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, aggressor}, new PhysicalCardImpl[]{thorin, footman});
		// Thorin's skirmish first, while Bilbo is still assigned and waiting.
		scn.FreepsResolveSkirmish(thorin);

		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(7, scn.GetStrength(thorin));
		assertTrue(scn.FreepsActionAvailable(worrywort));
		scn.FreepsUseCardAction(worrywort);

		// Battle Fury is played and the ability carried out with no minion skirmishing Bilbo: the Orc Thorin is
		// fighting and the Orc waiting for Bilbo are neither of them made weaker.
		assertInZone(Zone.DISCARD, fury);
		assertEquals(10, scn.GetStrength(thorin));
		assertEquals(8, scn.GetStrength(footman));
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoSkirmishEventInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var worrywort = scn.GetFreepsCard("worrywort");
		var thorin = scn.GetFreepsCard("thorin");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var invisible = scn.GetFreepsCard("invisible");
		var fury = scn.GetFreepsCard("fury1");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(worrywort);
		scn.MoveCardsToHand(azanulbizar, invisible);
		assertInZone(Zone.HAND, azanulbizar, invisible);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, aggressor);
		scn.FreepsResolveSkirmish(bilbo);

		// A maneuver event, and a skirmish event that cannot be played with The One Ring not worn.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(worrywort));

		// With a skirmish event that can be played, it is.
		scn.MoveCardsToHand(fury);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(worrywort));
	}
}
