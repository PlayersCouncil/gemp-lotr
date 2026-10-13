package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: what it says when it is asked to answer or to read something that is not there.  An
 * answer for a player who is not being asked anything, an action of a card that is not on offer, and a reading of
 * a parameter the pending decision does not have each fail the test with a message that names the player, what was
 * attempted and what each player is in fact being asked, and leave whatever is pending as it was.  So does a choice
 * of cards given where the game is not asking for cards, or is asking for them the other way, and so does
 * declining to choose where the game is not asking for cards or will not take none, and so does the catch-all
 * FreepsChoose(text) at a choice of actions, where it used to take whichever action had the words, and so does
 * accepting an optional trigger where no choice of actions is pending, where it used to answer "0".  Also the one
 * reading that is no failure: the maximum of a number the game sets no upper bound on.
 */
public class RigFailureTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a maneuver ability only
					put("gandalf", "1_364");        // Gandalf, The Grey Wizard: a Wizard, for Gathering Wind
					put("gimli", "1_13");           // Gimli, Son of Gloin: companion, cost 2
					// Gathering Wind.  "Maneuver: Exert your Wizard to choose a number.  Make each minion with that
					// twilight cost strength -2 until the regroup phase."  The number has no upper bound.
					put("wind", "10_16");
					// Barliman Butterbur.  "Fellowship: Exert Barliman Butterbur to take a [gandalf] event into hand
					// from your discard pile."  An ally at home at site 1; with two events there, a choice among
					// cards the game shows.
					put("barliman", "1_70");
					put("sleep", "1_84");           // Sleep, Caradhras: a [gandalf] Fellowship event
					put("intimidate", "1_76");      // Intimidate: a [gandalf] Response event
					// Ottar, Man of Laketown.  "Fellowship: Exert Ottar to discard up to 3 cards from hand and
					// draw an equal number of cards."  A choice among cards where they lie, of which none need
					// be chosen.
					put("ottar", "1_80");
					// Fireworks.  "Fellowship: Spot Gandalf to shuffle up to 2 [gandalf] or up to 2 [shire] cards
					// from your discard pile into your draw deck."  A choice among cards the game shows, of
					// which none need be chosen.
					put("fireworks", "3_32");

					put("runner", "1_178");         // Goblin Runner: cost 1, strength 5
					put("troop", "1_177");          // Goblin Patrol Troop: cost 6, strength 13, no text
					put("slayer1", "3_93");         // Morgul Slayer: a [sauron] minion; a regroup ability only
					put("slayer2", "3_93");
					// Forces of Mordor.  "Shadow: Spot X [sauron] minions to add (X)."  X is bounded by the minions.
					put("forces", "1_248");
					// Goblin Scimitar.  "Bearer must be a [moria] Orc.  When you play this possession, you may
					// draw a card."  An optional trigger.
					put("scimitar", "1_180");
				}},
				// Site 1 is Bag End: "Fellowship: Exert a Hobbit to play The Gaffer from your draw deck."  With
				// Frodo there it is an action on offer in every fellowship phase.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Fails unless the rig refuses the call, saying each of the given things.
	private static void assertRefused(Runnable call, String... said) {
		String refusal = null;
		try {
			call.run();
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	@Test
	public void AnAnswerForAPlayerWhoIsNotBeingAskedAnythingIsRefusedSayingWhatEachPlayerIsBeingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		// In the fellowship phase the Shadow player is not being asked anything.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [Use Bag End].";
		assertRefused(() -> scn.ShadowAssignToMinions(aragorn, runner),
				"Shadow Player was to answer \"" + aragorn.getCardId() + " " + runner.getCardId() + "\", but is not being asked anything.",
				freepsAsked);
		assertRefused(() -> scn.ShadowChooseCard(aragorn),
				"Shadow Player was to choose Aragorn, Ranger of the North, but that player is not being asked anything.", freepsAsked);
		assertRefused(scn::ShadowChooseYes,
				"Shadow Player was to choose \"Yes\", but that player is not being asked anything.", freepsAsked);
		assertRefused(scn::ShadowDeclineOptionalTrigger,
				"Shadow Player was to pass or decline, but is not being asked anything.", freepsAsked);

		// Nothing has moved.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The plain passes are not answers of that kind: with nothing pending they do nothing and say nothing.
		scn.ShadowPass();
		scn.ShadowPass("Play Shadow action or Pass");
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second assignment by the Free Peoples player, who gave all of his in one answer: it is the Shadow
		// player who is being asked now, and the refusal says so.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertRefused(() -> scn.FreepsAssignToMinions(gandalf, troop),
				"Free Peoples Player was to answer \"" + gandalf.getCardId() + " " + troop.getCardId() + "\", but is not being asked anything.",
				"Shadow Player is being asked \"Assign minions to companions or allies at home\".");
		assertFalse(scn.IsCharAssigned(gandalf));

		// What the Shadow player is being asked is still there to be answered.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		scn.ShadowAssignToMinions(gandalf, troop);
		assertTrue(scn.IsCharAssignedAgainst(gandalf, troop));
	}

	@Test
	public void ACardThatIsNotOnOfferIsRefusedByNameWithWhatIsOnOfferAndTheChoiceStaysPending() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var wind = scn.GetFreepsCard("wind");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gimli, wind);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		// Gimli can be played and Bag End used; a maneuver event cannot be played, and Aragorn has nothing to use.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertFalse(scn.FreepsPlayAvailable(wind));
		assertFalse(scn.FreepsActionAvailable(aragorn));

		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [";
		assertRefused(() -> scn.FreepsPlayCard(wind),
				"Gathering Wind is not offering Free Peoples Player a play.", freepsAsked, "Use Bag End", "Play Gimli",
				"Shadow Player is not being asked anything.");
		assertRefused(() -> scn.FreepsUseCardAction(aragorn),
				"Aragorn, Ranger of the North is not offering Free Peoples Player an action.", freepsAsked, "Use Bag End");
		assertRefused(() -> scn.FreepsTransferCard(aragorn),
				"Aragorn, Ranger of the North is not offering Free Peoples Player a transfer.", freepsAsked);
		// The other player's card, while that player is not being asked anything at all.
		assertRefused(() -> scn.ShadowPlayCard(runner),
				"Goblin Runner is not offering Shadow Player a play.", "Shadow Player is not being asked anything.", freepsAsked);

		// The choice the Free Peoples player was facing is still pending, and is answered like any other.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertInZone(Zone.HAND, wind);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ReadingAParameterTheDecisionDoesNotHaveIsRefusedSayingWhatIsBeingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var wind = scn.GetFreepsCard("wind");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// A choice of actions has no bounds and shows no cards.
		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [Use Bag End].";
		assertRefused(scn::FreepsGetChoiceMin,
				"The 'min' of Free Peoples Player's decision was to be read, but the decision in front of that player has no 'min'.",
				freepsAsked, "Shadow Player is not being asked anything.");
		assertRefused(scn::FreepsGetChoiceMax, "has no 'max'.", freepsAsked);
		assertRefused(scn::FreepsGetSelectableCount, "has no 'selectable'.", freepsAsked);
		// And a player who is not being asked anything has no decision to read.
		assertRefused(scn::ShadowGetChoiceMin,
				"The 'min' of Shadow Player's decision was to be read, but that player is not being asked anything.", freepsAsked);
		assertRefused(scn::ShadowGetSelectableCount, "that player is not being asked anything.", freepsAsked);

		// The list of actions is a reading that has an answer either way: those on offer, or none.
		assertEquals(1, scn.FreepsGetAvailableActions().size());
		assertTrue(scn.ShadowGetAvailableActions().isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A choice of a number is no choice of actions: none are listed, and a card is not played into it.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertTrue(scn.FreepsGetAvailableActions().isEmpty());
		assertFalse(scn.FreepsAnyActionsAvailable());
		assertFalse(scn.FreepsActionAvailable("Use"));
		assertRefused(scn::FreepsGetSelectableCount, "has no 'selectable'.", "Free Peoples Player is being asked \"Choose a minion cost\".");
		assertRefused(() -> scn.FreepsPlayCard(gimli),
				"Gimli, ", " is not offering Free Peoples Player a play.", "Free Peoples Player is being asked \"Choose a minion cost\".");
		assertRefused(scn::FreepsChooseYes,
				"Free Peoples Player was to choose \"Yes\", but the decision in front of that player has no 'results'.");

		// The number is still being asked for.
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertEquals(0, scn.FreepsGetChoiceMin());
	}

	@Test
	public void ACardChosenWhereTheGameIsNotAskingForCardsIsRefusedSayingWhatIsAskedAndNothingIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wind = scn.GetFreepsCard("wind");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		// A choice of actions.  A card's id is a number, and a number here is an action's place in the list; and
		// the list has blueprints of its own, which a card chosen by blueprint matches none of, so that the answer
		// is an empty one: a pass.  Neither is given.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		String notCards = ", but the decision in front of that player is not a choice of cards.";
		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [Use Bag End].";
		assertRefused(() -> scn.FreepsChooseCard(aragorn),
				"Free Peoples Player was to choose Aragorn, Ranger of the North" + notCards, freepsAsked,
				"Shadow Player is not being asked anything.");
		assertRefused(() -> scn.FreepsChooseCards(aragorn, gandalf),
				"Free Peoples Player was to choose Aragorn, Ranger of the North, Gandalf, The Grey Wizard" + notCards, freepsAsked);
		assertRefused(() -> scn.FreepsChooseCards(), "Free Peoples Player was to choose no card" + notCards, freepsAsked);
		assertRefused(() -> scn.ChooseCards(VirtualTableScenario.P1, aragorn),
				"Free Peoples Player was to choose Aragorn, Ranger of the North" + notCards, freepsAsked);
		assertRefused(() -> scn.FreepsChooseCardIDFromSelection(aragorn),
				"Free Peoples Player was to choose Aragorn, Ranger of the North" + notCards, freepsAsked);
		assertRefused(() -> scn.FreepsChooseCardBPFromSelection(aragorn),
				"Free Peoples Player was to choose Aragorn, Ranger of the North from a selection of cards" + notCards, freepsAsked);
		assertRefused(() -> scn.FreepsChooseCardBPFromSelection("1_89"),
				"Free Peoples Player was to choose 1_89 from a selection of cards" + notCards, freepsAsked);

		// The fellowship phase has not been passed, and its actions are still on offer.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// A choice of a number, reached past a choice the game made by itself: Gandalf is the only Wizard there
		// is to exert, so no card was asked for.  His id is not taken for the number.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertRefused(() -> scn.FreepsChooseCard(gandalf),
				"Free Peoples Player was to choose Gandalf, The Grey Wizard" + notCards,
				"Free Peoples Player is being asked \"Choose a minion cost\".", "Shadow Player is not being asked anything.");
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsChoose("6");
		assertEquals(11, scn.GetStrength(troop));

		// An assignment, where a lone id would assign nobody to that companion and end the Free Peoples player's
		// assigning.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertRefused(() -> scn.FreepsChooseCard(aragorn),
				"Free Peoples Player was to choose Aragorn, Ranger of the North" + notCards,
				"Free Peoples Player is being asked \"Assign minions to companions or allies at home\".");
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void EachKindOfCardChoiceTakesACardChosenAndRefusesTheAnswerThatFitsTheOtherKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var sleep = scn.GetFreepsCard("sleep");
		var intimidate = scn.GetFreepsCard("intimidate");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(sleep, intimidate);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		// Cards the game shows, from the discard pile: answered by their places in the showing, not by card id.
		scn.FreepsUseCardAction(barliman);
		assertTrue(scn.FreepsDecisionAvailable("Choose card"));
		assertEquals(2, scn.FreepsGetSelectableCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, intimidate));
		String shown = ", but that player is choosing among cards that are shown, which are not answered by card id";
		assertRefused(() -> scn.ChooseCards(VirtualTableScenario.P1, sleep),
				"Free Peoples Player was to choose Sleep, Caradhras" + shown, "Free Peoples Player is being asked \"Choose card");
		assertRefused(() -> scn.FreepsChooseCardIDFromSelection(sleep), "Free Peoples Player was to choose Sleep, Caradhras" + shown);
		assertInZone(Zone.DISCARD, sleep);
		assertEquals(2, scn.FreepsGetSelectableCount());

		scn.FreepsChooseCard(sleep);
		assertInZone(Zone.HAND, sleep);
		assertInZone(Zone.DISCARD, intimidate);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Cards where they lie, in play: answered by card id, not by blueprint.  Which skirmish to resolve is
		// such a choice, and is asked with two to choose between.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, runner },
				new PhysicalCardImpl[] { gimli, troop });
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertRefused(() -> scn.FreepsChooseCardBPFromSelection(aragorn),
				"Free Peoples Player was to choose Aragorn, Ranger of the North from a selection of cards, but that player is"
						+ " choosing among cards where they lie, which are not answered by blueprint",
				"Free Peoples Player is being asked \"Choose next skirmish to resolve\".");
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertFalse(scn.IsCharSkirmishing(aragorn));

		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningToChooseWhereTheGameIsNotAskingForCardsIsRefusedSayingWhatIsAskedAndNothingIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wind = scn.GetFreepsCard("wind");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(wind);
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// A choice of actions, where the empty answer that chooses no card is a pass.  It is not given.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		String notCards = " was to decline choosing, but the decision in front of that player is not a choice of cards.";
		String freepsAsked = "Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [Use Bag End].";
		assertRefused(scn::FreepsDeclineChoosing, "Free Peoples Player" + notCards, freepsAsked,
				"Shadow Player is not being asked anything.");
		// And a player who is not being asked anything has nothing to decline.
		assertRefused(scn::ShadowDeclineChoosing,
				"Shadow Player was to decline choosing, but that player is not being asked anything.", freepsAsked);

		// The fellowship phase has not been passed.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// An optional trigger, where the same answer declines it.  The Goblin Runner is the only [moria] Orc, so
		// the scimitar's bearer is not asked for and its "you may draw a card" is the next thing asked.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		int hand = scn.GetShadowHandCount();
		assertRefused(scn::ShadowDeclineChoosing, "Shadow Player" + notCards,
				"Shadow Player is being asked \"Optional responses\", offering [",
				"Free Peoples Player is not being asked anything.");

		// The trigger has not been declined: it is still there to be taken, and draws its card.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(hand, scn.GetShadowHandCount());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A choice of a number, which the game would not take the empty answer for either: now it is said why.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertRefused(scn::FreepsDeclineChoosing, "Free Peoples Player" + notCards,
				"Free Peoples Player is being asked \"Choose a minion cost\".", "Shadow Player is not being asked anything.");
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		scn.FreepsChoose("6");
		assertEquals(11, scn.GetStrength(troop));

		// An assignment, where the empty answer assigns nobody and ends the Free Peoples player's assigning.
		scn.SkipToAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertRefused(scn::FreepsDeclineChoosing, "Free Peoples Player" + notCards,
				"Free Peoples Player is being asked \"Assign minions to companions or allies at home\".");
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsAssignToMinions(aragorn, runner);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void AcceptingAnOptionalTriggerWhereNoActionIsOnOfferIsRefusedSayingWhatIsAskedAndNothingIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wind = scn.GetFreepsCard("wind");
		var ottar = scn.GetFreepsCard("ottar");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToHand(wind, gimli);
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		String notActions = " was to accept an optional trigger, but the decision in front of that player is not a choice of actions.";

		// A player who is not being asked anything has nothing to accept.
		assertRefused(scn::ShadowAcceptOptionalTrigger,
				"Shadow Player was to accept an optional trigger, but that player is not being asked anything.",
				"Free Peoples Player is being asked \"Play Fellowship action or Pass\"");

		// Cards where they lie, of which none need be chosen: "0" is no card id the game offers, and would have
		// been taken for whatever it meant there.  Nothing is chosen and the choice stays.
		scn.FreepsUseCardAction(ottar);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertRefused(scn::FreepsAcceptOptionalTrigger, "Free Peoples Player" + notActions,
				"Free Peoples Player is being asked \"Choose cards from hand to discard\".",
				"Shadow Player is not being asked anything.");
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertInZone(Zone.HAND, wind, gimli);
		scn.FreepsDeclineChoosing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The scimitar's "you may draw a card", an optional trigger: accepted, it draws.  The Goblin Runner is the
		// only [moria] Orc, so no bearer is asked for.
		scn.FreepsPass();
		scn.ShadowPlayCard(scimitar);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		int hand = scn.GetShadowHandCount();
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A number, where "0" is a number: refused, and the question stays.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertRefused(scn::FreepsAcceptOptionalTrigger, "Free Peoples Player" + notActions,
				"Free Peoples Player is being asked \"Choose a minion cost\".");
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		scn.FreepsChoose("6");
		assertEquals(11, scn.GetStrength(troop));
	}

	@Test
	public void DecliningToChooseChoosesNoCardWhereNoneNeedBeChosenAndIsRefusedWhereOneMustBe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wind = scn.GetFreepsCard("wind");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar = scn.GetFreepsCard("ottar");
		var fireworks = scn.GetFreepsCard("fireworks");
		var sleep = scn.GetFreepsCard("sleep");
		var intimidate = scn.GetFreepsCard("intimidate");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveAlliesToTable(barliman, ottar);
		scn.MoveCardsToHand(wind, fireworks);
		scn.MoveCardsToDiscard(sleep, intimidate);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);

		// Cards where they lie, of which none need be chosen: "discard up to 3 cards from hand".  No card is
		// discarded and none drawn; Ottar's exertion, the cost, is paid.
		scn.FreepsUseCardAction(ottar);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards from hand to discard"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(ottar));
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, wind);
		assertInZone(Zone.HAND, fireworks);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Cards the game shows, of which none need be chosen: "shuffle up to 2 [gandalf] ... cards from your
		// discard pile into your draw deck".  Both stay in the discard pile.
		scn.FreepsPlayCard(fireworks);
		scn.FreepsChoose("GANDALF");
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, intimidate));
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DISCARD, sleep);
		assertInZone(Zone.DISCARD, intimidate);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Cards the game shows, of which one must be chosen: "take a [gandalf] event into hand from your discard
		// pile".  The game would not take an answer of none; the refusal now says so, and what is being asked.
		scn.FreepsUseCardAction(barliman);
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertEquals(1, scn.FreepsGetChoiceMin());
		String oneAtLeast = "Free Peoples Player was to decline choosing, but at least 1 card must be chosen there.";
		assertRefused(scn::FreepsDeclineChoosing, oneAtLeast,
				"Free Peoples Player is being asked \"Choose card from discard\".", "Shadow Player is not being asked anything.");
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertInZone(Zone.DISCARD, sleep);
		scn.FreepsChooseCard(sleep);
		assertInZone(Zone.HAND, sleep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Cards where they lie, of which one must be chosen: which skirmish to resolve.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, runner },
				new PhysicalCardImpl[] { gandalf, troop });
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertRefused(scn::FreepsDeclineChoosing, oneAtLeast,
				"Free Peoples Player is being asked \"Choose next skirmish to resolve\".");
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheMaximumOfANumberWithNoUpperBoundReadsAsUncappedAndThatOfABoundedNumberAsItsBound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var wind = scn.GetFreepsCard("wind");
		var troop = scn.GetShadowCard("troop");
		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");
		var forces = scn.GetShadowCard("forces");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wind);
		scn.MoveCardsToHand(forces);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, slayer1, slayer2);
		scn.FreepsPass();

		// "Spot X [sauron] minions": the two Morgul Slayers bound the number.
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(forces);
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertEquals(twilight + 2, scn.GetTwilight());

		// "Choose a number": the game names a least number and no greatest.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(wind);
		assertTrue(scn.FreepsDecisionAvailable("Choose a minion cost"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(Integer.MAX_VALUE, scn.FreepsGetChoiceMax());

		// The number is asked and taken as before: the minion costing 6 is the one weakened.
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsChoose("6");
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(slayer1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheCatchAllChooseTakesNoActionAtAChoiceOfActionsAndStillAnswersAMultipleChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(gimli, runner);

		scn.StartGame();

		// Player 1 could play Gimli.  Words that are in that action's wording, given to the catch-all, used to
		// play him: an answer written for a choice the game never asked did something else in silence.
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertRefused(() -> scn.FreepsChoose("Gimli"),
				"Free Peoples Player was to choose \"Gimli\", but no multiple choice is in front of that player",
				"an action is not taken this way",
				"Free Peoples Player is being asked \"Play Fellowship action or Pass\", offering [");
		assertInZone(Zone.HAND, gimli);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The same for the second player.
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(runner));
		assertRefused(() -> scn.ShadowChoose("Runner"),
				"Shadow Player was to choose \"Runner\", but no multiple choice is in front of that player",
				"Shadow Player is being asked \"Play Shadow action or Pass\", offering [");
		assertInZone(Zone.HAND, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A multiple choice is answered by its words as before: "Do you want to make another move?", No.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsChoose("No");
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}
}
