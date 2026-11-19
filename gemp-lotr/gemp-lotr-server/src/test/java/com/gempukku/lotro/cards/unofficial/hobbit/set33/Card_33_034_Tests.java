package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_034_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Elvenking is
	// played in the Shadow phase of turn 1 at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elvenking", "33_34");

					// [mirkwood] conditions of several titles.
					put("secretive", "33_33");  // Secretive People: cost 1; Skirmish and Regroup abilities not used
					put("wargold", "32_26");    // War for Gold: unique, cost 2; a move trigger for allies (none here) and a Maneuver ability
					put("grudge", "33_29");     // Ancient Grudge: unique, cost 2; a modifier at 3 [mirkwood] conditions, a Skirmish ability not used
					put("grudge2", "33_29");
					put("feuds", "31_29");      // Ancestral Feuds: unique; laid only in the other player's discard pile
					// Conditions of other cultures.
					put("jail", "32_39");       // Jail: a [sauron] condition; a Regroup ability not used
					put("hatred", "30_34");     // Hatred Rekindled: a [gundabad] condition; a Shadow ability not used
					put("rock", "4_50");        // Here Is Good Rock: a [dwarven] condition of the Free Peoples player; a Skirmish ability not used

					// [gundabad] cards that are no condition.
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, strength 5; a Maneuver ability not used
					put("warg", "31_27");       // Warg: [gundabad] possession on Yazneg; a Regroup ability not used

					// The Free Peoples player's discards.
					put("balin", "30_2");       // Balin, Brother of Dwalin: Dwarf companion, who exerts for Ancestral Knowledge
					// Ancestral Knowledge: [dwarven] Maneuver event, exert a Dwarf character to discard a condition (or 2
					// conditions if you can spot 4 Shadow conditions)
					put("knowledge", "30_1");
					// Nori, Of the Blue Mountains: Maneuver: exert Nori twice and discard a [dwarven] follower to discard
					// a possession
					put("nori", "30_15");
					put("bifur", "30_5");       // Bifur, Inarticulate: a [dwarven] follower on Nori, paid for his ability
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: a [gandalf] Wizard companion, strength 7
					// Introspection: [gandalf] Fellowship event; spot a [gandalf] Wizard to have the opponent discard one of
					// his own conditions
					put("introspection", "12_29");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheElvenkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: The Elvenking
		 * Unique: True
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, you may play a [mirkwood] condition from your draw deck or discard pile.
		 * 	Response: If [gundabad] and/or [mirkwood] conditions are about to be discarded by an opponent, discard this condition to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elvenking");

		// The picture misprints the title "The Elvenkng"; the definition keeps "The Elvenking" (Ketura, F-0738).
		assertEquals("The Elvenking", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedItMayPlayAMirkwoodConditionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var wargold = scn.GetShadowCard("wargold");
		var grudge = scn.GetShadowCard("grudge");
		var jail = scn.GetShadowCard("jail");
		var hatred = scn.GetShadowCard("hatred");
		var freepsFeuds = scn.GetFreepsCard("feuds");
		scn.MoveCardsToHand(elvenking);

		scn.StartGame();
		assertInHand(elvenking);
		scn.FreepsPass();
		// A [mirkwood] condition in each pile, so that the pile is chosen.
		scn.SetShadowDrawDeck(grudge);
		scn.MoveCardsToDiscard(secretive, wargold, jail, hatred, freepsFeuds);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(elvenking);
		assertInZone(Zone.DECK, grudge);
		assertInZone(Zone.DISCARD, secretive, wargold, jail, hatred, freepsFeuds);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(elvenking);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(elvenking));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");
		// [mirkwood] conditions of two titles; not conditions of other cultures, and not the other player's
		// [mirkwood] condition in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(secretive, wargold));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail, hatred, freepsFeuds));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(secretive);

		assertInZone(Zone.SUPPORT, elvenking, secretive);
		assertInZone(Zone.DISCARD, wargold, jail, hatred, freepsFeuds);
		assertInZone(Zone.DECK, grudge);
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedItMayPlayAMirkwoodConditionFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var grudge = scn.GetShadowCard("grudge");
		var jail = scn.GetShadowCard("jail");
		scn.MoveCardsToHand(elvenking);

		scn.StartGame();
		assertInHand(elvenking);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(grudge, jail);   // lint-ok: grudge2, the twin, is set aside by this cheat and is in no pile
		scn.MoveCardsToDiscard(secretive);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(elvenking);
		assertInZone(Zone.DECK, grudge, jail);
		assertInZone(Zone.DISCARD, secretive);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(elvenking);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(grudge));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail));
		scn.ShadowChooseCardBPFromSelection(grudge);

		assertInZone(Zone.SUPPORT, elvenking, grudge);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, secretive);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMirkwoodConditionOnlyInTheDiscardPileItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var jail = scn.GetShadowCard("jail");
		scn.MoveCardsToHand(elvenking);

		scn.StartGame();
		assertInHand(elvenking);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(jail);
		scn.MoveCardsToDiscard(secretive);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(elvenking);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, secretive);

		scn.ShadowPlayCard(elvenking);
		scn.ShadowAcceptOptionalTrigger();
		// No choice of pile, the draw deck holding no [mirkwood] condition, and the one there is played unasked.
		assertFalse(scn.ShadowChoiceAvailable("draw deck"));
		assertInZone(Zone.SUPPORT, elvenking, secretive);
		assertInZone(Zone.DECK, jail);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var grudge = scn.GetShadowCard("grudge");
		scn.MoveCardsToHand(elvenking);

		scn.StartGame();
		assertInHand(elvenking);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(grudge);
		scn.MoveCardsToDiscard(secretive);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(elvenking);
		assertInZone(Zone.DECK, grudge);
		assertInZone(Zone.DISCARD, secretive);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(elvenking);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(elvenking));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, elvenking);
		assertInZone(Zone.DECK, grudge);
		assertInZone(Zone.DISCARD, secretive);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMirkwoodConditionPlayedAfterItIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var wargold = scn.GetShadowCard("wargold");
		scn.MoveCardsToSupportArea(elvenking);
		scn.MoveCardsToHand(secretive);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking);
		assertInHand(secretive);
		scn.FreepsPass();
		scn.MoveCardsToDiscard(wargold);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, elvenking);
		assertInHand(secretive);
		assertInZone(Zone.DISCARD, wargold);

		scn.ShadowPlayCard(secretive);
		assertInZone(Zone.SUPPORT, secretive);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, wargold);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoMirkwoodConditionInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var jail = scn.GetShadowCard("jail");
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCardsToHand(elvenking);

		scn.StartGame();
		assertInHand(elvenking);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(jail);
		scn.MoveCardsToDiscard(hatred);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(elvenking);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, hatred);

		scn.ShadowPlayCard(elvenking);
		assertInZone(Zone.SUPPORT, elvenking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoMirkwoodConditionCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var grudge = scn.GetShadowCard("grudge");
		var grudge2 = scn.GetShadowCard("grudge2");
		var jail = scn.GetShadowCard("jail");
		scn.MoveCardsToHand(elvenking);
		scn.MoveCardsToSupportArea(grudge);

		scn.StartGame();
		assertInHand(elvenking);
		assertInZone(Zone.SUPPORT, grudge);
		scn.FreepsPass();
		// Ancient Grudge is in play, so its copy in the discard pile cannot be played; the pool pays for the Elvenking
		// and leaves nothing for Secretive People's (1).
		scn.SetShadowDrawDeck(jail);
		scn.MoveCardsToDiscard(secretive, grudge2);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInHand(elvenking);
		assertInZone(Zone.SUPPORT, grudge);
		assertInZone(Zone.DECK, jail);
		assertInZone(Zone.DISCARD, secretive, grudge2);

		scn.ShadowPlayCard(elvenking);
		assertInZone(Zone.SUPPORT, elvenking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, secretive, grudge2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseDiscardsItselfToSaveAGundabadConditionTheFreePeoplesPlayerDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var hatred = scn.GetShadowCard("hatred");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		var knowledge = scn.GetFreepsCard("knowledge");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(elvenking, hatred);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, hatred);
		assertInHand(knowledge);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(knowledge);
		scn.FreepsChooseCard(hatred);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(elvenking));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(elvenking, knowledge);
		assertInZone(Zone.SUPPORT, hatred);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DeclinedTheMirkwoodConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		var knowledge = scn.GetFreepsCard("knowledge");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(elvenking, secretive);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, secretive);
		assertInHand(knowledge);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(knowledge);
		scn.FreepsChooseCard(secretive);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(elvenking));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(secretive, knowledge);
		assertInZone(Zone.SUPPORT, elvenking);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheWholeDiscardIsSavedAShadowConditionOfAnotherCultureIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var jail = scn.GetShadowCard("jail");
		var hatred = scn.GetShadowCard("hatred");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		var knowledge = scn.GetFreepsCard("knowledge");
		scn.MoveCompanionsToTable(balin);
		// Four Shadow conditions: Ancestral Knowledge discards two.
		scn.MoveCardsToSupportArea(elvenking, secretive, jail, hatred);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, secretive, jail, hatred);
		assertInHand(knowledge);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(knowledge);
		scn.FreepsChooseCards(secretive, jail);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(elvenking));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(elvenking, knowledge);
		// One discard of two cards, prevented whole: the [sauron] Jail with the [mirkwood] condition.
		assertInZone(Zone.SUPPORT, secretive, jail, hatred);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheWholeDiscardIsSavedTheFreePeoplesPlayersOwnConditionIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var jail = scn.GetShadowCard("jail");
		var hatred = scn.GetShadowCard("hatred");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		var rock = scn.GetFreepsCard("rock");
		var knowledge = scn.GetFreepsCard("knowledge");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(elvenking, secretive, jail, hatred);
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, secretive, jail, hatred, rock);
		assertInHand(knowledge);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(knowledge);
		scn.FreepsChooseCards(hatred, rock);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(elvenking));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(elvenking, knowledge);
		// The Free Peoples player's own [dwarven] condition is saved with the [gundabad] one.
		assertInZone(Zone.SUPPORT, hatred, rock, secretive, jail);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedForAConditionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var jail = scn.GetShadowCard("jail");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		var knowledge = scn.GetFreepsCard("knowledge");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(elvenking, jail);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, jail);
		assertInHand(knowledge);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(knowledge);
		scn.FreepsChooseCard(jail);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(jail, knowledge);
		assertInZone(Zone.SUPPORT, elvenking);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheElvenkingAloneIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var jail = scn.GetShadowCard("jail");
		var yazneg = scn.GetShadowCard("yazneg");
		var balin = scn.GetFreepsCard("balin");
		var knowledge = scn.GetFreepsCard("knowledge");
		scn.MoveCompanionsToTable(balin);
		// The Jail ([sauron]) is laid so that the Free Peoples player has a choice of condition.
		scn.MoveCardsToSupportArea(elvenking, jail);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, jail);
		assertInHand(knowledge);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Elvenking is itself a [mirkwood] condition; as built ("another"), its own discard alone is not answered
		// (Q-0413).
		scn.FreepsPlayCard(knowledge);
		scn.FreepsChooseCard(elvenking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(elvenking, knowledge);
		assertInZone(Zone.SUPPORT, jail);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedForAGundabadPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var yazneg = scn.GetShadowCard("yazneg");
		var warg = scn.GetShadowCard("warg");
		var nori = scn.GetFreepsCard("nori");
		var bifur = scn.GetFreepsCard("bifur");
		scn.MoveCompanionsToTable(nori);
		scn.MoveCardsToSupportArea(elvenking);

		scn.StartGame();
		scn.AttachCardsTo(nori, bifur);
		scn.MoveMinionsToTable(yazneg);
		scn.AttachCardsTo(yazneg, warg);
		assertInZone(Zone.SUPPORT, elvenking);
		assertAttachedTo(bifur, nori);
		assertAttachedTo(warg, yazneg);
		scn.SkipToPhase(Phase.MANEUVER);
		// Bifur's Aid is offered at the start of the maneuver phase.
		scn.FreepsDeclineOptionalTrigger();

		scn.FreepsUseCardAction(nori);
		// Bifur is paid and the Warg, the one possession, is discarded.
		assertInDiscard(bifur, warg);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, elvenking);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenItsOwnerDiscardsHisMirkwoodCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var wargold = scn.GetShadowCard("wargold");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToSupportArea(elvenking, wargold);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, wargold);
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// War for Gold discards itself to pay for its own ability.
		scn.ShadowUseCardAction(wargold);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(wargold);
		assertInZone(Zone.SUPPORT, elvenking);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenAFreePeoplesEventMakesTheShadowPlayerDiscardHisOwnCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elvenking = scn.GetShadowCard("elvenking");
		var secretive = scn.GetShadowCard("secretive");
		var gandalf = scn.GetFreepsCard("gandalf");
		var introspection = scn.GetFreepsCard("introspection");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(elvenking, secretive);
		scn.MoveCardsToHand(introspection);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, elvenking, secretive);
		assertInHand(introspection);

		// Away from a battleground: the Shadow player chooses which of his own conditions to discard.
		scn.FreepsPlayCard(introspection);
		scn.ShadowChooseCard(secretive);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(secretive, introspection);
		assertInZone(Zone.SUPPORT, elvenking);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
