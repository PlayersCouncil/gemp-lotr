package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_9");
					put("axe2", "1_9");
					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no damage bonus
					// Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1.
					// "Skirmish: Exert Gimli to make him strength +2."
					put("gimli", "1_13");
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, home site 3, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man
					// Preparations: [dwarven] condition in the support area.  "Skirmish: Wound a minion skirmishing a
					// Dwarf once for each [dwarven] card stacked here. Discard this condition."  Its regroup ability
					// is how a card comes to be stacked there.
					put("preparations", "7_12");
					put("handaxe", "2_10");       // Hand Axe: a [dwarven] possession; here only ever the card stacked on Preparations

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3
					put("savage2", "1_151");
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("sneak", "1_181");        // Goblin Sneak: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("wallcrawler", "1_184");  // Goblin Wallcrawler: [moria] Orc, strength 6, vitality 1; stronger only underground
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarven Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a Dwarf.
		 * Each time a player's minion loses a skirmish to bearer, that player discards the top card from his or her
		 * draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");
		assertEquals("Dwarven Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CanOnlyBePlayedOnADwarfCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(axe, gimli, grimir);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Aragorn is a companion and no Dwarf: beside him and Frodo there is nobody to bear the Axe.
		assertFalse(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(grimir);

		// 2 for Gimli + 1 for Grimir
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(axe);
		// A Dwarf companion and a Dwarf ally; not Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(axe, grimir);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionLosingASkirmishToBearerMakesItsOwnerDiscardTheTopCardOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var otherAxe = scn.GetFreepsCard("axe2");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var top = scn.GetShadowCard("runner");
		var next = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(guard, axe);
		// A second Axe, on a Dwarf who fights nobody this turn: it has nothing to say about the Guard's win.
		scn.AttachCardsTo(grimir, otherAxe);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		// The Shadow player's own Runner is the top card of his draw deck, his Soldier the one beneath it.
		scn.MoveCardsToTopOfDeck(next, top);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(guard);

		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(top, scn.GetShadowTopOfDeck());

		scn.PassSkirmishActions();

		// The wound for losing and the Axe's discard come at the one moment; the discard is carried out first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Dwarven Axe");
		assertEquals(1, scn.GetWoundsOn(savage));
		// The Savage's owner discards the top card of his own draw deck: one card, and nobody else any.
		assertInZone(Zone.DISCARD, top);
		assertInZone(Zone.DECK, next);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli bears no Axe: the second Savage loses to him, takes 2 wounds for his damage +1, and nothing
		// more is discarded.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertInZone(Zone.DECK, next);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsDiscardedWhenBearerLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, soldier);
		scn.FreepsResolveSkirmish(guard);

		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(7, scn.GetStrength(soldier));

		// 6 against 7: the Guard loses to the Soldier.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AxeAffectsMaxOneMinionDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");
		var top = scn.GetShadowCard("soldier");
		var next = scn.GetShadowCard("wallcrawler");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, sneak);
		scn.MoveCardsToTopOfDeck(next, top);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowAssignToMinions(gimli, sneak);
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsUseCardAction(gimli);
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPass();
		// 6 + 2 for the Axe + 2 and 2 for his own ability, against 5 + 5.
		assertEquals(12, scn.GetStrength(gimli));
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.FreepsPass();

		// Comprehensive Rules 4.2, Section Three, DWARVEN AXE: "This card can trigger only once for each Shadow
		// player with a minion in that skirmish, regardless of how many minions that player had."
		// Both minions lost at the one moment, so the wounds for losing and a trigger for each minion are put to
		// the Free Peoples player to be ordered.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(3, scn.FreepsGetActionCount());
		assertInZone(Zone.DECK, top, next);

		// The first trigger carried out takes the Axe's one use for this skirmish.  The second is dropped: it is
		// not put to the player again, and nothing more is discarded.
		scn.FreepsResolveActionOrder("Dwarven Axe");
		assertInZone(Zone.DISCARD, top, runner, sneak);
		assertInZone(Zone.DECK, next);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		// 2 dead minions, and one card discarded from the deck
		assertEquals(3, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	protected VirtualTableScenario GetFierceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_9");
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no damage bonus
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: only ever a card in the draw deck here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The once is once a skirmish, not once a turn: a minion that loses to bearer in the skirmish and again in the
	// fierce skirmish has lost two skirmishes, and its owner discards each time.
	@Test
	public void AMinionThatLosesToBearerInTwoSkirmishesOfOneTurnDiscardsEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, axe);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.MINION, null, Keyword.FIERCE));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		// The Guard, 4 + 2, beats the Savage, 5.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		// The fierce skirmish: the Savage loses to the Guard a second time.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Council ruling LR 2, which this card's bug report led to (https://github.com/PlayersCouncil/gemp-lotr/issues/571):
	// "A character removed from his or her skirmish is a losing character, unless there are still one or more
	// characters on each side of that skirmish, in which case the removed character is neither a losing nor a
	// winning character."
	@Test
	public void AxeDoesNotTriggerOnMinionKilledBeforeSkirmishResolves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var preparations = scn.GetFreepsCard("preparations");
		var handaxe = scn.GetFreepsCard("handaxe");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");
		var wallcrawler = scn.GetShadowCard("wallcrawler");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, handaxe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, sneak, wallcrawler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowAssignToMinions(gimli, sneak, wallcrawler);
		scn.FreepsResolveSkirmish(gimli);

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());

		// One [dwarven] card is stacked on Preparations: one wound, which kills the Wallcrawler in the middle of
		// the skirmish.  The Runner and the Sneak fight on, so the Wallcrawler neither won nor lost.
		scn.FreepsUseCardAction(preparations);
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, sneak, wallcrawler));
		scn.FreepsChooseCard(wallcrawler);
		assertInZone(Zone.DISCARD, wallcrawler);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 6 + 2 against 5 + 5: Gimli loses, and no minion has lost a skirmish to him.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		// the Wallcrawler alone
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLastMinionKilledDuringTheSkirmishHasLostToBearerAndItsOwnerDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var preparations = scn.GetFreepsCard("preparations");
		var handaxe = scn.GetFreepsCard("handaxe");
		var runner = scn.GetShadowCard("runner");
		var top = scn.GetShadowCard("soldier");
		var next = scn.GetShadowCard("sneak");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, handaxe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToTopOfDeck(next, top);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.DECK, top, next);

		// Gimli's only opponent dies before strengths are compared: the skirmish is over and the Runner lost it.
		scn.FreepsUseCardAction(preparations);
		assertInZone(Zone.DISCARD, runner);
		// The skirmish's own resolution and the Axe's discard come at the one moment.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsActionAvailable("Dwarven Axe"));
		assertInZone(Zone.DECK, top);
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DISCARD, top);
		assertInZone(Zone.DECK, next);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		// the Runner, and the card from the top of the deck
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	protected VirtualTableScenario GetKilledMinionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_9");
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1, so the wound for losing kills it
					put("troop", "1_177");      // Goblin Patrol Troop: no text; only ever a card in the draw deck here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The oldest of the old `at` tests (dwarvenAxeDoesNotFreeze, 2011): the minion that lost dies of the very loss
	// the Axe answers.  Here the wound is carried out first, so the minion is gone before the Axe's trigger is.
	@Test
	public void TheDiscardIsStillMadeWhenTheWoundForLosingIsCarriedOutFirstAndKillsTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledMinionScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		var top = scn.GetShadowTopOfDeck();
		int deck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(deck >= 2);

		scn.PassSkirmishActions();

		// The wound for losing and the Axe's trigger come at the one moment, and the Free Peoples player is asked
		// which to carry out first.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		assertEquals(2, scn.FreepsGetAvailableActions().size());
		assertTrue(scn.FreepsActionAvailable("Dwarven Axe"));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.DECK, top);

		scn.FreepsResolveRuleFirst();

		// The Runner is dead, and its owner still discards: the top card of his own draw deck and no other.
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.DISCARD, top);
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// The same loss with no card left in the minion's owner's draw deck, which is the table the 2011 test stood on:
	// there is nothing to discard, and the game goes on.
	@Test
	public void WithNoCardInItsOwnersDrawDeckTheLosingMinionsOwnerDiscardsNothingAndTheGameGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledMinionScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToDiscard(new ArrayList<>(scn.GetShadowDrawDeck()).toArray(new PhysicalCardImpl[0]));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		int discard = scn.GetShadowDiscardCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(freepsDeck > 0);

		scn.PassSkirmishActions();

		// The Axe's trigger is carried out first, and finds no card: nothing is discarded from either deck.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("Dwarven Axe");
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());

		// The wound for losing follows without a question, and the Runner is the one card added to the pile.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(discard + 1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
