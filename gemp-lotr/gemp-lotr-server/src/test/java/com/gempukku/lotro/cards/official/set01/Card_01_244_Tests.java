package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_244_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defense", "1_244");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc who is not [sauron] ([moria]), no text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; his text answers only his own play

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not the Ring-bearer, strength 8
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally, a character who is no companion

					// Dwarf Guard: plain cards for the Free Peoples hand
					put("dwarf1", "1_7");
					put("dwarf2", "1_7");
					put("dwarf3", "1_7");
					put("dwarf4", "1_7");
					put("dwarf5", "1_7");
					put("dwarf6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defense", "1_244");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the bearer, a companion who is not the Ring-bearer
					put("savage", "1_151");     // Uruk Savage: the minion the bearer is assigned to
					put("runner", "1_178");     // Goblin Runner: a minion for the Ring-bearer, who does not bear the condition

					// Two cards for the Free Peoples hand, one short of the three a discard would take
					put("gimli", "1_13");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesperateDefenseoftheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Desperate Defense of the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert a [sauron] Orc. Plays on a companion.<br>Each time bearer is assigned to a skirmish, the Free Peoples player chooses to either discard 3 cards from hand or add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("defense");

		assertEquals("Desperate Defense of the Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFewerThanThreeCardsInHandBearerAssignedToASkirmishAddsABurdenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var defense = scn.GetShadowCard("defense");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveCardsToHand(gimli, sam);
		assertInHand(gimli, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();

		assertAttachedTo(defense, aragorn);
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());

		// The bearer and the Ring-bearer are assigned in the one answer.  Three cards cannot be discarded from
		// a hand of two, so there is nothing to choose: one burden, for the bearer's assignment and not for
		// the other, and the game has gone on to the skirmishes.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, savage }, new PhysicalCardImpl[] { frodo, runner });
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(gimli, sam);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ToPlayAShadowPlayerExertsASauronOrcAndPutsItOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defense = scn.GetShadowCard("defense");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(defense);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, guard, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.ShadowPlayAvailable(defense));

		scn.ShadowPlayCard(defense);
		// The bearer: either companion, the Ring-bearer or not; not the ally, nor a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grimir, soldier, chieftain, guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// The cost: either [sauron] Orc; not the [sauron] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertAttachedTo(defense, aragorn);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defense = scn.GetShadowCard("defense");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defense);
		assertInHand(defense);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard, troop);
		// Vitality 2: one wound exhausts the only [sauron] Orc.  The Uruk-hai and the [moria] Orc could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(defense));
		assertInHand(defense);
	}

	@Test
	public void EachTimeBearerIsAssignedTheFreePeoplesPlayerDiscardsThreeCardsOrAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defense = scn.GetShadowCard("defense");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf1 = scn.GetFreepsCard("dwarf1");
		var dwarf2 = scn.GetFreepsCard("dwarf2");
		var dwarf3 = scn.GetFreepsCard("dwarf3");
		var dwarf4 = scn.GetFreepsCard("dwarf4");
		var dwarf5 = scn.GetFreepsCard("dwarf5");
		var dwarf6 = scn.GetFreepsCard("dwarf6");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveCardsToHand(dwarf1, dwarf2, dwarf3, dwarf4, dwarf5, dwarf6);
		assertAttachedTo(defense, aragorn);
		assertInHand(dwarf1, dwarf2, dwarf3, dwarf4, dwarf5, dwarf6);
		assertEquals(6, scn.GetFreepsHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetFreepsHandCount());
		scn.FreepsAssignToMinions(aragorn, soldier);

		// The Free Peoples player chooses, and both are open to him.
		assertTrue(scn.FreepsChoiceAvailable("Discard 3 cards from hand"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Discard 3 cards from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(dwarf1, dwarf2, dwarf3, dwarf4, dwarf5, dwarf6));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(dwarf1, dwarf2, dwarf3);

		assertInDiscard(dwarf1, dwarf2, dwarf3);
		assertInHand(dwarf4, dwarf5, dwarf6);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		// Aragorn (8) beats the Soldier (7); then the fellowship moves again and a second assignment phase comes.
		scn.FreepsResolveSkirmish(aragorn);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(runner);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		scn.FreepsAssignToMinions(aragorn, runner);

		assertTrue(scn.FreepsChoiceAvailable("Discard 3 cards from hand"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(dwarf4, dwarf5, dwarf6);
		// The Soldier, who lived through the first skirmish, is left over for the Shadow player to assign.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void AnAssignmentOfAnotherCompanionAsksNothingAndTheShadowPlayersAssignmentOfBearerAsks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf1 = scn.GetFreepsCard("dwarf1");
		var dwarf2 = scn.GetFreepsCard("dwarf2");
		var dwarf3 = scn.GetFreepsCard("dwarf3");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveCardsToHand(dwarf1, dwarf2, dwarf3);
		assertAttachedTo(defense, aragorn);
		assertInHand(dwarf1, dwarf2, dwarf3);
		assertEquals(3, scn.GetFreepsHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		// Frodo, who does not bear it, is assigned; Aragorn is left for the Shadow player.
		scn.FreepsAssignToMinions(frodo, runner);
		assertFalse(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.ShadowDecisionAvailable("assign"));
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());

		scn.ShadowAssignToMinions(aragorn, troop);
		// A hand of exactly three: both are open to him.
		assertTrue(scn.FreepsChoiceAvailable("Discard 3 cards from hand"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
