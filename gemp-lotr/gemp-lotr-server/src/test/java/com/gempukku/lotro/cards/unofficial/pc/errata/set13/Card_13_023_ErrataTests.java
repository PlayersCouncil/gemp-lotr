package com.gempukku.lotro.cards.unofficial.pc.errata.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_023_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elf", "63_23");
					put("elf2", "63_23");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8, vitality 4; a maneuver ability never used here

					put("defiance", "1_37");    // Defiance: [elven] Skirmish event, cost 1, "Make an Elf strength +2 (or +4 if skirmishing a Nazgul)."
					put("border", "1_32");      // Border Defenses: [elven] Skirmish event, cost 1, "Make an Elf strength +2 (or +4 if skirmishing an archer)."
					put("sentinels", "2_37");   // Sentinels of Numenor: [gondor] Skirmish event, cost 0, "Make a [gondor] companion strength +2 (or +4 if in a fierce skirmish)."
					put("lightfoot", "1_52");   // Lightfootedness: [elven] Archery event, cost 0, "Spot an Elf companion to make the minion archery total -1."
					put("seeitdone", "2_34");   // Gondor Will See It Done: Response event, cost 0, "If a companion is about to take a wound, exert a [gondor] companion to prevent that wound."

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("soldier", "1_271");    // Orc Soldier: strength 7, vitality 2; a skirmish ability never used here
					put("drums", "1_168");      // Drums in the Deep: Shadow Skirmish event, cost 0, "Make a [moria] Orc strength +2 (or +4 if skirmishing a Dwarf)."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShroudedElfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Shrouded Elf
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * 	<b>Response:</b> If you play an event during a skirmish involving this companion, exert this
		 * 	companion to place that event on top of your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elf");

		assertEquals("Shrouded Elf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedFromHandForTwoAsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		scn.MoveCardsToHand(elf);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elf));
		scn.FreepsPlayCard(elf);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, elf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEventPlayedInTheElfsSkirmishGoesToTheDiscardPileUnlessTheElfExertsToPlaceItOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defiance = scn.GetFreepsCard("defiance");
		var sentinels = scn.GetFreepsCard("sentinels");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf, aragorn);
		scn.MoveCardsToHand(defiance, sentinels);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(elf));
		assertEquals(6, scn.GetStrength(elf));

		// An [elven] event on the Elf himself. The response is offered and declined.
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(elf));
		assertEquals(8, scn.GetStrength(elf));
		assertInDiscard(defiance);
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.ShadowPass();
		// A [gondor] event on Aragorn, who is in no skirmish. The response is offered again and taken.
		assertEquals(8, scn.GetStrength(aragorn));
		scn.MarkGameLog();
		scn.FreepsPlayCard(sentinels);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elf));

		// A player may respond more than once to the same play: the Elf could exert again, for nothing more.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(elf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DECK, sentinels);
		assertSame(sentinels, scn.GetFreepsTopOfDeck());
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		// It went there from its play, not by way of the hand.
		assertTrue(scn.GameLogSinceMarkSays("on top of their deck"));
		assertFalse(scn.GameLogSinceMarkSays("back into hand"));
		// The event still does what it says.
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TwoEventsAnsweredInOneSkirmishLieOnTheDrawDeckTheLaterOneOnTop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var defiance = scn.GetFreepsCard("defiance");
		var border = scn.GetFreepsCard("border");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(defiance, border);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(defiance);
		scn.FreepsAcceptOptionalTrigger();
		// Offered once more for the same play, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elf));
		assertSame(defiance, scn.GetFreepsTopOfDeck());
		assertEquals(deck + 1, scn.GetFreepsDeckCount());

		scn.ShadowPass();
		scn.FreepsPlayCard(border);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsAcceptOptionalTrigger();
		// Vitality 3 with 2 wounds: the Elf cannot exert again, so nothing more is offered.
		assertEquals(2, scn.GetWoundsOn(elf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The later event lies on top of the earlier one, and both took effect.
		assertSame(border, scn.GetFreepsTopOfDeck());
		assertSame(defiance, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(10, scn.GetStrength(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AResponseEventPlayedInTheElfsSkirmishMayBePlacedOnTopOfTheDrawDeckToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var seeitdone = scn.GetFreepsCard("seeitdone");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(elf, aragorn);
		scn.MoveCardsToHand(seeitdone);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, soldier);
		scn.FreepsResolveSkirmish(elf);
		scn.PassCurrentPhaseActions();

		// The Elf (6) loses to the Orc Soldier (7) and is about to take a wound: the response event is played,
		// for an exertion of Aragorn.
		assertEquals(0, scn.GetWoundsOn(elf));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));
		scn.FreepsPlayCard(seeitdone);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// That is an event played during a skirmish involving the Elf.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsDeclineOptionalTrigger();

		// 1 exertion; the wound itself was prevented by the event, which is on top of the draw deck.
		assertEquals(1, scn.GetWoundsOn(elf));
		assertInZone(Zone.DECK, seeitdone);
		assertSame(seeitdone, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheElfExertsToAnswerAndAnExhaustedElfIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var defiance = scn.GetFreepsCard("defiance");
		var border = scn.GetFreepsCard("border");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(defiance, border);

		scn.StartGame();
		scn.AddWoundsToChar(elf, 1);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(1, scn.GetWoundsOn(elf));
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsAcceptOptionalTrigger();

		// One exertion. Vitality 3 with 2 wounds: he cannot exert again, and nothing more is offered.
		assertEquals(2, scn.GetWoundsOn(elf));
		assertTrue(scn.IsExhausted(elf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		// The next event is not answered at all, and goes to the discard pile.
		scn.FreepsPlayCard(border);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(elf));
		assertInDiscard(border);
		assertEquals(10, scn.GetStrength(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnEventInAnotherPhaseOrInAnotherCompanionsSkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lightfoot = scn.GetFreepsCard("lightfoot");
		var defiance = scn.GetFreepsCard("defiance");
		var border = scn.GetFreepsCard("border");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(elf, aragorn);
		scn.MoveCardsToHand(lightfoot, defiance, border);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.ARCHERY);

		// An event in the archery phase: no skirmish at all.
		assertTrue(scn.FreepsPlayAvailable(lightfoot));
		scn.FreepsPlayCard(lightfoot);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(lightfoot);
		assertEquals(0, scn.GetWoundsOn(elf));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elf, savage}, new PhysicalCardImpl[]{aragorn, runner});
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's skirmish. The Elf is assigned to one that has not begun, and the event is played on him.
		assertTrue(scn.IsCharAssigned(elf));
		assertFalse(scn.IsCharSkirmishing(elf));
		scn.FreepsPlayCard(defiance);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(8, scn.GetStrength(elf));
		assertInDiscard(defiance);
		assertEquals(0, scn.GetWoundsOn(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn (8) kills the Goblin Runner (5, vitality 1).
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(elf);

		// The Elf's own skirmish: now an event is answered if the player wishes. He does not.
		assertTrue(scn.IsCharSkirmishing(elf));
		scn.FreepsPlayCard(border);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(elf));
		assertInDiscard(border);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOtherPlayersEventInTheElfsSkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var runner = scn.GetShadowCard("runner");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, runner);
		scn.FreepsResolveSkirmish(elf);

		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.ShadowPlayAvailable(drums));
		scn.ShadowPlayCard(drums);

		// The Shadow player's event took effect and went to his discard pile; neither player is offered anything.
		assertEquals(7, scn.GetStrength(runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(drums);
		assertEquals(0, scn.GetWoundsOn(elf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OfTwoShroudedElvesOnlyTheOneInTheSkirmishMayAnswer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var elf2 = scn.GetFreepsCard("elf2");
		var defiance = scn.GetFreepsCard("defiance");
		var border = scn.GetFreepsCard("border");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(elf, elf2);
		scn.MoveCardsToHand(defiance, border);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elf, savage}, new PhysicalCardImpl[]{elf2, runner});
		scn.FreepsResolveSkirmish(elf);

		// The responses to the play come before the event's Elf is chosen. The copy assigned to a skirmish
		// that has not begun offers nothing.
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elf2));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elf));
		assertEquals(0, scn.GetWoundsOn(elf2));
		// Once more for the same play, and again from the skirmishing copy alone.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elf2));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, elf2));
		scn.FreepsChooseCard(elf);

		// The first Elf (8) beats the Uruk Savage (5).
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(elf2);

		// In the second Elf's skirmish it is the other way round.
		scn.FreepsPlayCard(border);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elf2));
		assertEquals(1, scn.GetWoundsOn(elf));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsChooseCard(elf2);
		assertEquals(8, scn.GetStrength(elf2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
