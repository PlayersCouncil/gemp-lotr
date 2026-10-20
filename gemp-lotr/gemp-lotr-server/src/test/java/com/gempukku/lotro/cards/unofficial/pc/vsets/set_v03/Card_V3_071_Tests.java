package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

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

public class Card_V3_071_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hour", "103_71");
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: [ringwraith] Nazgul, strength 9, vitality 2, not fierce; weakens only bearers of [ringwraith] conditions
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: [ringwraith] Nazgul, strength 9, vitality 3, not fierce; a Shadow ability never used here
					put("nertea", "1_234");     // Ulaire Nertea, Messenger of Dol Guldur: [ringwraith] Nazgul, strength 9, vitality 2; text only when played
					put("rider", "12_161");     // Black Rider: [ringwraith] Nazgul, cost 5
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: the one Nazgul of another culture, [raider]
					put("cur", "7_189");        // Morgul Cur: [ringwraith] Orc, strength 8, +2 while a Nazgul can be spotted, vitality 2
					put("power", "1_211");      // Drawn to Its Power: [ringwraith] condition; does nothing from a hand
					put("filler1", "1_177");    // Goblin Patrol Troop: [moria] Orc with no text; hand and deck filler
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
					put("filler7", "1_177");
					put("filler8", "1_177");
					put("filler9", "1_177");
					put("filler10", "1_177");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; not overwhelmed unless tripled
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; overwhelmed by any Nazgul
					put("bounder", "1_286");    // Bounder: Hobbit ally, home site 2, strength 2, vitality 2; a skirmish ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThisisMyHourStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: This is My Hour
		 * Unique: false
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If your Nazgul wins a skirmish, exert it to reconcile your hand.  You may discard a Nazgul
		 * 	from hand to wound a companion it was skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hour");

		assertEquals("This is My Hour", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NazgulWinningASkirmishMayExertToReconcileTheShadowPlayersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var nertea = scn.GetShadowCard("nertea");
		var cur = scn.GetShadowCard("cur");
		var filler1 = scn.GetShadowCard("filler1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// The only minion in hand is of the right culture and the wrong race.
		scn.MoveCardsToHand(hour, cur, filler1);
		scn.MoveCardsToFreepsHand("filler1", "filler2");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, otsea, nertea);
		// One hindered card of each player's: a reconcile restores the cards of the player who reconciles.
		scn.HinderCard(nertea, boromir);
		// What the reconcile will draw: no Nazgul among them.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"), scn.GetShadowCard("filler4"),
				scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"), scn.GetShadowCard("filler7"), scn.GetShadowCard("filler8"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Lemenya, strength 9, beats Aragorn, strength 8.
		scn.PassSkirmishActions();

		int deck = scn.GetShadowDeckCount();
		// The event answers the win before Aragorn's wound for losing (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.IsHindered(nertea));
		assertTrue(scn.IsHindered(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hour));

		scn.ShadowChooseOptionalTrigger(hour);

		// The Nazgul who won exerts; the Nazgul standing by does not.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		// The reconcile: the player's own hindered card is restored, then one card may be discarded.
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertFalse(scn.IsHindered(nertea));
		assertTrue(scn.IsHindered(boromir));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(cur, filler1));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(filler1);

		assertInDiscard(filler1);
		// 1 card held + 7 drawn
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(deck - 7, scn.GetShadowDeckCount());
		assertInHand(cur);
		// It is the hand of the player who played the event.
		assertEquals(2, scn.GetFreepsHandCount());
		// With no Nazgul in hand there is nothing to ask and nobody is wounded.
		assertFalse(scn.ShadowDecisionAvailable("discard a Nazgul"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(hour);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ReconcilingFromAboveEightCardsDiscardsDownToEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour, filler1, filler2, scn.GetShadowCard("filler3"), scn.GetShadowCard("filler4"),
				scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"), scn.GetShadowCard("filler7"),
				scn.GetShadowCard("filler8"), scn.GetShadowCard("filler9"), scn.GetShadowCard("filler10"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		int deck = scn.GetShadowDeckCount();
		assertEquals(11, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowChooseOptionalTrigger(hour);

		// 10 cards held once the event has left the hand: the one a player may discard and the one more
		// that brings the hand to 8 are asked for together.
		assertEquals(10, scn.GetShadowHandCount());
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(filler1, filler2);

		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertInDiscard(filler1, filler2, hour);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenANazgulLosesNorWhenAMinionThatIsNoNazgulWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var otsea = scn.GetShadowCard("otsea");
		var cur = scn.GetShadowCard("cur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(hour);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, cur);
		// Aragorn at strength 11 beats Otsea, strength 9.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, otsea}, new PhysicalCardImpl[]{boromir, cur});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		int twilight = scn.GetTwilight();
		// The Nazgul lost: he is wounded, still able to exert, and nothing is offered.
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(boromir);
		// Morgul Cur, strength 8 + 2 beside a Nazgul, beats Boromir, strength 7.
		scn.PassSkirmishActions();

		// A [ringwraith] minion won with a Nazgul in play who could exert, but the winner is no Nazgul.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInHand(hour);
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheNazgulWhoWonIsExhaustedThoughAnotherNazgulCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(hour);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, otsea);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(lemenya, 1);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"),
				scn.GetShadowCard("filler4"), scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"),
				scn.GetShadowCard("filler7"), scn.GetShadowCard("filler8"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, otsea});
		scn.FreepsResolveSkirmish(aragorn);
		// Lemenya, strength 9, beats Aragorn, strength 8.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(boromir);
		// Otsea, strength 9, beats Boromir, strength 7.
		scn.PassSkirmishActions();

		// The same event on the same table is offered for a Nazgul who can exert, and he is the one who does.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hour));
		scn.ShadowChooseOptionalTrigger(hour);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		scn.ShadowDeclineReconciliation();
		assertEquals(8, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoNazgulWinningOneSkirmishTheOneTheEventIsPlayedForExertsAndNoOtherMinionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var nertea = scn.GetShadowCard("nertea");
		var cur = scn.GetShadowCard("cur");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, otsea, nertea, cur);
		// Aragorn at strength 15 loses to 9 + 9 + 10 without being overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 7));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"),
				scn.GetShadowCard("filler4"), scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"),
				scn.GetShadowCard("filler7"), scn.GetShadowCard("filler8"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		// Nertea is left out of the skirmish.
		scn.ShadowAssignToMinions(aragorn, otsea, cur);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The event answers the win before Aragorn's wound for losing (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		// The event is offered once for each Nazgul who won, and each offer names its Nazgul.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hour, "Lemenya"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hour, "Ots"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hour, "Nert"));

		scn.ShadowChooseOptionalTrigger(hour, "Lemenya");

		// One exertion, on the Nazgul the event was played for: not on the other Nazgul who won, not on the
		// Orc who won beside them, and not on the Nazgul who was not in the skirmish.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(cur));
		assertEquals(0, scn.GetWoundsOn(nertea));

		scn.ShadowDeclineReconciliation();
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(hour);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AfterTheReconcileANazgulMayBeDiscardedFromHandToWoundTheCompanionItWasSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var rider = scn.GetShadowCard("rider");
		var khamul = scn.GetShadowCard("khamul");
		var cur = scn.GetShadowCard("cur");
		var power = scn.GetShadowCard("power");
		var filler1 = scn.GetShadowCard("filler1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// In hand: a [ringwraith] Nazgul, a [ringwraith] minion that is no Nazgul, a [ringwraith] card that is no minion.
		scn.MoveCardsToHand(hour, rider, cur, power);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, otsea);
		// The reconcile draws a second Nazgul, of another culture, and four cards of no interest.
		scn.MoveCardsToTopOfDeck(khamul, filler1, scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"), scn.GetShadowCard("filler4"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, otsea});
		scn.FreepsResolveSkirmish(aragorn);
		// Lemenya, strength 9, beats Aragorn, strength 8. Boromir's skirmish with Otsea has not begun.
		scn.PassSkirmishActions();

		scn.ShadowChooseOptionalTrigger(hour);
		scn.ShadowDeclineReconciliation();

		// 3 cards held + 5 drawn. The reconcile comes first, so the Nazgul it drew is there to be discarded.
		assertEquals(8, scn.GetShadowHandCount());
		assertInHand(khamul);
		assertTrue(scn.ShadowDecisionAvailable("discard a Nazgul"));
		// Aragorn's wound for losing is still to come (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		scn.ShadowChooseYes();

		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cur, power, filler1));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);

		assertInDiscard(khamul);
		assertInHand(rider);
		assertEquals(7, scn.GetShadowHandCount());
		// One wound, on the companion Lemenya was skirmishing, and then his wound for losing; none on the companion
		// assigned to Otsea.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertInDiscard(hour);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
	}

	@Test
	public void DecliningTheDiscardLeavesTheHandAndTheCompanionAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var khamul = scn.GetShadowCard("khamul");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour, rider);

		scn.StartGame();
		// The Nazgul who wins is of the [raider] culture.
		scn.MoveMinionsToTable(khamul);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"),
				scn.GetShadowCard("filler4"), scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"),
				scn.GetShadowCard("filler7"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, khamul);
		scn.FreepsResolveSkirmish(aragorn);
		// Khamul, strength 12, beats Aragorn, strength 8.
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(khamul));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hour));
		scn.ShadowChooseOptionalTrigger(hour);
		assertEquals(1, scn.GetWoundsOn(khamul));
		scn.ShadowDeclineReconciliation();

		assertTrue(scn.ShadowDecisionAvailable("discard a Nazgul"));
		assertEquals(8, scn.GetShadowHandCount());
		// Aragorn's wound for losing is still to come (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowChooseNo();

		assertInHand(rider);
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Khamul is fierce: the fierce assignment phase comes next.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheCompanionAboutToBeOverwhelmedIsStillThereToWoundAndIsKilledAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(hour, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"),
				scn.GetShadowCard("filler4"), scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"),
				scn.GetShadowCard("filler7"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, lemenya);
		scn.FreepsResolveSkirmish(guard);
		// Lemenya, strength 9, overwhelms the Dwarf Guard, strength 4.
		scn.PassSkirmishActions();

		// The event answers the win before the overwhelm (engine pass 54; until then the Guard was dead by now,
		// and the discard was offered with nobody left to wound).
		assertInZone(Zone.FREE_CHARACTERS, guard);

		scn.ShadowChooseOptionalTrigger(hour);
		scn.ShadowDeclineReconciliation();

		// The Guard is the companion Lemenya was skirmishing.
		assertTrue(scn.ShadowDecisionAvailable("discard a Nazgul"));
		assertInHand(rider);

		// The one Nazgul in hand is taken without a choice.
		scn.ShadowChooseYes();

		assertInDiscard(rider);
		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The Guard took the wound, and then was overwhelmed.
		assertInZone(Zone.DEAD, guard);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionThatWonBesideTheNazgulIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var cur = scn.GetShadowCard("cur");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, cur);
		// Aragorn at strength 11 loses to 9 + 10 without being overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"),
				scn.GetShadowCard("filler4"), scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"),
				scn.GetShadowCard("filler7"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowAssignToMinions(aragorn, cur);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The event answers the win before Aragorn's wound for losing (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(cur));

		scn.ShadowChooseOptionalTrigger(hour);
		scn.ShadowDeclineReconciliation();
		scn.ShadowChooseYes();

		assertInDiscard(rider);
		// The companion Lemenya was skirmishing is wounded, and then takes his wound for losing; the Orc who fought
		// beside Lemenya is no companion.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(cur));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyTheNazgulBeatIsNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var lemenya = scn.GetShadowCard("lemenya");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(hour, rider);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		// The Bounder at strength 8 loses to Lemenya, strength 9, without being overwhelmed. Site 2 is his home.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 6));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"),
				scn.GetShadowCard("filler4"), scn.GetShadowCard("filler5"), scn.GetShadowCard("filler6"),
				scn.GetShadowCard("filler7"));
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, lemenya);
		scn.FreepsResolveSkirmish(bounder);
		scn.PassSkirmishActions();

		// The event answers the win before the Bounder's wound for losing (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.ShadowChooseOptionalTrigger(hour);
		scn.ShadowDeclineReconciliation();

		// The cost can be paid, so the offer is made; an ally is no companion, so it wounds nobody.
		assertTrue(scn.ShadowDecisionAvailable("discard a Nazgul"));
		scn.ShadowChooseYes();

		assertInDiscard(rider);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
