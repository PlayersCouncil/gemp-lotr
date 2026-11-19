package com.gempukku.lotro.cards.unofficial.hobbit.set30;

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

public class Card_30_025_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "30_25");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					put("dain", "32_2");        // Dáin Ironfoot: [dwarven] Dwarf ally; a regroup trigger only
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower, a Dwarf; a fierce-win trigger only
					put("roac", "32_8");        // Roäc, Son of Carc: [dwarven] follower, a Bird; a maneuver ability only
					put("glamdring", "30_29");  // Glamdring: [gandalf] artifact, so that No Finer Blade has a branch to play
					// [gandalf] skirmish events: Take up arms. Fight! (a spell: "Spot Gandalf to make a minion
					// strength -3") and No Finer Blade (no spell); Former Herald is a [gandalf] maneuver event
					put("take", "30_31");
					put("blade", "33_21");
					put("herald", "32_21");
					// [dwarven] skirmish events: Battle Fury ("Make a Dwarf character strength +3") and Noble
					// intentions; Lore of Imladris is a [dwarven] maneuver event
					put("fury", "30_3");
					put("noble", "30_14");
					put("lore", "30_13");
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an assignment ability only
					put("sneak", "31_32");      // Goblin Sneak: [moria] Orc, strength 5, vitality 1; a play trigger only
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a play trigger only
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, strength 7, fierce; a skirmish ability only
					// Dark Sorcery: [wraith] condition, "The first sentence of Gandalf's game text does not apply"
					put("sorcery", "33_61");
					// Goblin Patrol Troop: a card with no text, for hands and a deck to draw from
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
					put("card5", "1_177");
					put("card6", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Gandalf, Leader of Dwarves
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Wise</b>.<br>Each time you reconcile, you may draw 2 cards.<br><b>Skirmish:</b> Play a [gandalf] skirmish event and add a doubt to play a Dwarf from your draw deck.<br><b>Skirmish:</b> Play a [dwarven] skirmish event to make an Orc skirmishing Bilbo strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Leader of Dwarves", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void ReconcilingOffersTwoCardsAndTakingThemDrawsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"),
				scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"));

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf);

		// No minion is played: the regroup phase follows the Shadow phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();

		// The Shadow player has reconciled, and that is not Gandalf's owner reconciling: nothing is offered.
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsChooseToStay();

		assertEquals(6, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsDeclineReconciliation();

		// He has reconciled, a hand of 6 made up to 8, and now Gandalf makes his offer.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(10, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());

		// The turn is over: the other player's begins.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void EachReconcileOffersTheDrawAndDecliningLeavesTheHandAsReconciled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"),
				scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"));

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf);

		// Turn 1: the reconcile's offer, declined.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		scn.FreepsChooseToStay();
		assertEquals(6, scn.GetFreepsHandCount());
		scn.FreepsDeclineReconciliation();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(8, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Turn 3, his owner's next turn: he reconciles again and is offered again; taken.
		scn.SkipToOtherPlayersTurn();
		// Player 1's fellowship at site 2 again (The Troll Hoard makes no offer: no hand weapon in the deck).
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		// The Shadow player, with a hand of his own by now, reconciles first.
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(8, scn.GetFreepsHandCount());
		deck = scn.GetFreepsDeckCount();
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(10, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		// The turn is over: the other player's begins, at The Troll Hoard, which offers nothing with no hand
		// weapon in that deck (F-0800).
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void GandalfSkirmishAbilityPlaysAGandalfSkirmishEventAndAddsADoubtToPlayADwarfFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();
		var take = scn.GetFreepsCard("take");
		var blade = scn.GetFreepsCard("blade");
		var herald = scn.GetFreepsCard("herald");
		var fury = scn.GetFreepsCard("fury");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(take, blade, herald, fury);
		assertInZone(Zone.HAND, take, blade, herald, fury);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.DECK, balin, dain, bifur, roac);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(runner));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(gandalf, "[gandalf]"));
		scn.FreepsUseCardAction(gandalf, "[gandalf]");
		// [gandalf] skirmish events, a spell and one that is none; not a [gandalf] maneuver event, nor a
		// [dwarven] skirmish event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(take, blade));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herald, fury));
		scn.FreepsChooseCard(take);

		// Take up arms is played (the Runner is its only minion), and the doubt is added.
		assertInZone(Zone.DISCARD, take);
		assertEquals(2, scn.GetStrength(runner));
		assertEquals(1, scn.GetBurdens());

		// The deck is looked through: a Dwarf companion, a Dwarf ally and a Dwarf follower may be played, the
		// [dwarven] Bird follower not.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, dain, bifur));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(roac));
		scn.FreepsChooseCardBPFromSelection(balin);

		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.DECK, dain, bifur, roac);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void GandalfSkirmishAbilityIsNotOfferedWithNoDwarfInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();
		var take = scn.GetFreepsCard("take");
		var roac = scn.GetFreepsCard("roac");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(take);
		assertInZone(Zone.HAND, take);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(runner);
		// No Dwarf in the draw deck: Roäc is a [dwarven] follower and no Dwarf.
		scn.SetFreepsDrawDeck(roac, scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"));
		assertInZone(Zone.DECK, roac);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		// A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on
		// Q-0406 and Q-0418): no doubt can be paid, nor a skirmish event spent, for a look that finds nothing.
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf, "[gandalf]"));
		assertInZone(Zone.HAND, take);
		assertInZone(Zone.DECK, roac);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void GandalfSkirmishAbilityIsNotOfferedWithNoGandalfSkirmishEventInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var herald = scn.GetFreepsCard("herald");
		var fury = scn.GetFreepsCard("fury");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(herald, fury);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf, "[gandalf]"));
		// The other ability, paid with the [dwarven] skirmish event in hand, is offered.
		assertTrue(scn.FreepsActionAvailable(gandalf, "[dwarven]"));
	}

	@Test
	public void DwarvenSkirmishAbilityPlaysADwarvenSkirmishEventToMakeAnOrcSkirmishingBilboStrengthMinus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var fury = scn.GetFreepsCard("fury");
		var noble = scn.GetFreepsCard("noble");
		var lore = scn.GetFreepsCard("lore");
		var take = scn.GetFreepsCard("take");
		var aggressor = scn.GetShadowCard("aggressor");
		var sneak = scn.GetShadowCard("sneak");
		var spider = scn.GetShadowCard("spider");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(fury, noble, lore, take);
		assertInZone(Zone.HAND, fury, noble, lore, take);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveMinionsToTable(aggressor, sneak, spider, runner);
		// Strength enough for Bilbo to win against three minions and live: the board is about the Orcs.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 20));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, aggressor}, new PhysicalCardImpl[]{gandalf, runner});
		// Two more minions to Bilbo: an Orc of another culture and a Spider.
		scn.ShadowAssignToMinions(bilbo, sneak, spider);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(5, scn.GetStrength(sneak));
		assertEquals(7, scn.GetStrength(balin));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(gandalf, "[dwarven]"));
		scn.FreepsUseCardAction(gandalf, "[dwarven]");
		// [dwarven] skirmish events, not a [dwarven] maneuver event nor a [gandalf] skirmish event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, noble));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lore, take));
		scn.FreepsChooseCard(fury);

		// Battle Fury is played on Balin, its only Dwarf.
		assertInZone(Zone.DISCARD, fury);
		assertEquals(10, scn.GetStrength(balin));
		// The Orcs skirmishing Bilbo, of two cultures; not the Spider beside them, nor the Orc assigned to
		// Gandalf whose skirmish is still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, sneak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spider, runner));
		scn.FreepsChooseCard(aggressor);

		assertEquals(4, scn.GetStrength(aggressor));
		assertEquals(5, scn.GetStrength(sneak));
		assertEquals(5, scn.GetStrength(runner));
		// No doubt and no look at the deck.
		assertEquals(0, scn.GetBurdens());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Bilbo wins; in the next skirmish, a phase of its own, the Aggressor is back at 6.
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DwarvenSkirmishAbilityInAnotherSkirmishWhileBilboWaitsIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var fury = scn.GetFreepsCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(fury);
		assertInZone(Zone.HAND, fury);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveMinionsToTable(aggressor, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, aggressor}, new PhysicalCardImpl[]{gandalf, runner});
		// Gandalf's skirmish first, while Bilbo is still assigned and waiting.
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsActionAvailable(gandalf, "[dwarven]"));
		scn.FreepsUseCardAction(gandalf, "[dwarven]");

		// Battle Fury is played and the ability carried out with no Orc skirmishing Bilbo: the Orc Gandalf is
		// fighting and the Orc waiting for Bilbo are neither of them made weaker.
		assertInZone(Zone.DISCARD, fury);
		assertEquals(10, scn.GetStrength(balin));
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DwarvenSkirmishAbilityIsNotOfferedWithNoDwarvenSkirmishEventInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();
		var lore = scn.GetFreepsCard("lore");
		var take = scn.GetFreepsCard("take");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(lore, take);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, aggressor);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf, "[dwarven]"));
		// The other ability, paid with the [gandalf] skirmish event in hand, is offered.
		assertTrue(scn.FreepsActionAvailable(gandalf, "[gandalf]"));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyByDarkSorceryHisReconcileDrawIsNotOfferedAndHisSkirmishAbilitiesStillAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var take = scn.GetFreepsCard("take");
		var fury = scn.GetFreepsCard("fury");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(take, fury, scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"));

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveMinionsToTable(runner);
		scn.MoveShadowCardsToSupportArea("sorcery");

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		// Dark Sorcery leaves his Skirmish abilities alone.
		assertTrue(scn.FreepsActionAvailable(gandalf, "[gandalf]"));
		assertTrue(scn.FreepsActionAvailable(gandalf, "[dwarven]"));

		// Bilbo loses to the Runner and lives; no other skirmish, so the regroup phase follows.
		scn.PassSkirmishActions();
		scn.PassRegroupActions();
		scn.FreepsChooseToStay();
		assertEquals(4, scn.GetFreepsHandCount());
		scn.FreepsDeclineReconciliation();

		// He has reconciled to 8, and his draw is not offered: the turn goes to the other player.
		assertEquals(8, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
