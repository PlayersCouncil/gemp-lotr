package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thorin", "17_6");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability only

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("sneak", "1_181");        // Goblin Sneak: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("pursuer", "4_190");      // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its texts are about unbound Hobbits
					put("troop", "1_177");        // Goblin Patrol Troop: no text; here only ever a card in the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThorinIIIStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Thorin III, Stonehelm
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Damage +1.
		 * Each time a minion loses a skirmish involving Thorin III, its owner discards the top card from his or her
		 * draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thorin");

		assertEquals("Thorin III", card.getBlueprint().getTitle());
		assertEquals("Stonehelm", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void AMinionLosingASkirmishToThorinMakesItsOwnerDiscardTheTopCardOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(thorin, aragorn);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var top = scn.GetShadowCard("runner");
		var next = scn.GetShadowCard("soldier");
		scn.MoveMinionsToTable(savage, savage2);
		// The Shadow player's own Runner is the top card of his draw deck, his Soldier the one beneath it.
		scn.MoveCardsToTopOfDeck(next, top);
		assertInZone(Zone.DECK, top, next);
		assertEquals(top, scn.GetShadowTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, savage}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(thorin);
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(top, scn.GetShadowTopOfDeck());

		// 6 against 5: the Savage loses to Thorin.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Thorin III");
		// The Savage's owner discards the top card of his own draw deck: one card, and nobody else any.
		assertInZone(Zone.DISCARD, top);
		assertInZone(Zone.DECK, next);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// Two wounds for Thorin's damage +1, and the Savage lives.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// A minion losing a skirmish that does not involve Thorin, while he is in play: nothing is discarded.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInZone(Zone.DECK, next);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsDiscardedWhenThorinLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();

		var soldier = scn.GetShadowCard("soldier");
		scn.MoveMinionsToTable(soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, soldier);
		scn.FreepsResolveSkirmish(thorin);
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetShadowDiscardCount());

		// 6 against 7: the Soldier wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionKilledByTheWoundForLosingHasStillLostAndItsOwnerDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		var top = scn.GetShadowCard("soldier");
		var next = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToTopOfDeck(next, top);
		assertInZone(Zone.DECK, top, next);
		assertEquals(top, scn.GetShadowTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, runner);
		scn.FreepsResolveSkirmish(thorin);
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());

		// 6 against 5.  The wound for losing is carried out first and kills the Runner, of vitality 1.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, runner, top);
		assertInZone(Zone.DECK, next);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		// the Runner and the card from the deck
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionThatLosesToThorinInTheSkirmishAndTheFierceSkirmishDiscardsEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();

		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, pursuer);
		scn.FreepsResolveSkirmish(thorin);
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());

		// 6 against 5: the Pursuer loses and takes 2 wounds of its 3 vitality.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		// The fierce skirmish: the Pursuer loses to Thorin a second time, and is killed by it.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(thorin, pursuer);
		scn.FreepsResolveSkirmish(thorin);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, pursuer);
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		// two cards from the deck and the Pursuer
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoMinionsLosingOneSkirmishToThorinEachMakeTheirOwnerDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);
		// A strength that only arranges the board: Thorin is to beat two minions of 5 together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thorin, null, 5));

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");
		var top = scn.GetShadowCard("soldier");
		var next = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(runner, sneak);
		scn.MoveCardsToTopOfDeck(next, top);
		assertInZone(Zone.DECK, top, next);
		assertEquals(top, scn.GetShadowTopOfDeck());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, runner);
		scn.ShadowAssignToMinions(thorin, sneak);
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(11, scn.GetStrength(thorin));
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowDiscardCount());

		// 11 against 5 + 5: each minion has lost a skirmish involving Thorin, and each sets the text off.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, runner, sneak, top, next);
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
