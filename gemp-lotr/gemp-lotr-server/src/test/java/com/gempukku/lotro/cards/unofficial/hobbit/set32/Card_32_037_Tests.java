package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_037_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  A doubt is the HDG's burden.  The Shadow ability is taken in the Shadow phase of turn 1 at The Troll
	// Hoard 30_50 (site 2), where each minion of a higher site number roams (+2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("danger", "32_37");
					// [sauron] cards: a character, an item and a card that is neither.
					put("veteran", "32_43");     // Orkish Veteran: [sauron] Orc, cost 2, site 4, strength 7; archer at site 9 only, a Regroup ability, not used
					put("bow", "32_41");         // Orkish Bow: [sauron] possession, cost 1; bearer must be a [sauron] Orc
					put("pursuer", "32_42");     // Orkish Pursuer: [sauron] Orc, the Bow's bearer; a skirmish-win trigger and a Regroup ability, not used
					put("jail", "32_39");        // Jail: [sauron] condition, cost 2; a Regroup ability, not used
					put("jail2", "32_39");
					put("sentinel", "33_49");    // Orc Sentinel: [sauron] Orc, cost 1, strength 5; a Shadow ability, not used
					// Cards of other cultures.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc; an Assignment ability, not used
					put("river", "31_50");       // Enchanted River: [spider] condition
					// A [sauron] card for the other player's own discard pile, of a blueprint the Shadow player's piles do not hold.
					put("malevolent", "33_47");  // Malevolent Orc: [sauron] Orc
					// For the Skirmish ability.
					put("sauron", "32_45");      // Sauron, The Necromancer: [sauron] Maia, vitality 4; [gundabad] Orcs +1 (none here), an Assignment ability, not used
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("balin", "30_2");        // Balin, Brother of Dwalin: strength 7; a Fellowship ability, not used
					put("dwalin", "30_9");       // Dwalin, Brother of Balin: strength 7; a Maneuver ability, not used
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DangerWrappedinShadowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Danger Wrapped in Shadows
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Shadow:</b> Remove (2) to play a [sauron] card from your draw deck or discard pile (limit
		 * once per phase).<br><b>Skirmish:</b> Remove a doubt or exert Sauron to make an Orc strength +1 (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("danger");

		assertEquals("Danger Wrapped in Shadows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowRemoves2ToPlayASauronCardFromTheDiscardPileOncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var veteran = scn.GetShadowCard("veteran");
		var bow = scn.GetShadowCard("bow");
		var pursuer = scn.GetShadowCard("pursuer");
		var jail = scn.GetShadowCard("jail");
		var sentinel = scn.GetShadowCard("sentinel");
		var aggressor = scn.GetShadowCard("aggressor");
		var river = scn.GetShadowCard("river");
		var freepsMalevolent = scn.GetFreepsCard("malevolent");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		// A bearer for the Bow.
		scn.MoveMinionsToTable(pursuer);
		scn.SetShadowDrawDeck(sentinel);
		scn.MoveCardsToDiscard(veteran, bow, jail, aggressor, river, freepsMalevolent);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertInZone(Zone.DECK, sentinel);
		assertInZone(Zone.DISCARD, veteran, bow, jail, aggressor, river, freepsMalevolent);

		assertTrue(scn.ShadowActionAvailable(danger));
		scn.ShadowUseCardAction(danger);
		assertEquals(8, scn.GetTwilight());
		scn.ShadowChoose("discard pile");
		// A [sauron] character, possession and condition; not cards of other cultures, and not the other player's
		// [sauron] card in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, bow, jail));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aggressor, river, freepsMalevolent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(veteran);

		assertInZone(Zone.SHADOW_CHARACTERS, veteran, pursuer);
		assertInZone(Zone.DISCARD, bow, jail, aggressor, river, freepsMalevolent);
		// 8 - (2 + 2 roaming)
		assertEquals(4, scn.GetTwilight());

		// Once in the phase: (2) and the Jail's 2 are there, and it is not offered again.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(danger));
	}

	@Test
	public void PlaysASauronCardFromTheDrawDeckAndShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var veteran = scn.GetShadowCard("veteran");
		var jail = scn.GetShadowCard("jail");
		var sentinel = scn.GetShadowCard("sentinel");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(jail, sentinel, aggressor);   // lint-ok: jail2, the twin, is set aside by this cheat and is in no pile
		// A [sauron] card in the discard pile, so that the pile is chosen.
		scn.MoveCardsToDiscard(veteran);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.DECK, jail, sentinel, aggressor);
		assertInZone(Zone.DISCARD, veteran);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(danger);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(jail, sentinel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aggressor));
		scn.ShadowChooseCardBPFromSelection(jail);

		assertInZone(Zone.SUPPORT, jail, danger);
		assertInZone(Zone.DECK, sentinel, aggressor);
		assertInZone(Zone.DISCARD, veteran);
		// 10 - 2 - 2
		assertEquals(6, scn.GetTwilight());
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedAgainInASecondShadowPhaseOfTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var jail = scn.GetShadowCard("jail");
		var jail2 = scn.GetShadowCard("jail2");
		var sentinel = scn.GetShadowCard("sentinel");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(sentinel);
		scn.MoveCardsToDiscard(jail, jail2);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.DECK, sentinel);
		assertInZone(Zone.DISCARD, jail, jail2);

		scn.ShadowUseCardAction(danger);
		scn.ShadowChoose("discard pile");
		scn.ShadowChooseCardBPFromSelection(jail);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(danger));

		// No minion is in play, so the turn goes on to the regroup phase, and the fellowship moves again.
		scn.ShadowPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// A [sauron] card in each pile again, so that the pile is chosen.
		scn.SetShadowDrawDeck(sentinel);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DECK, sentinel);
		assertInZone(Zone.DISCARD, jail2);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(danger));
		scn.ShadowUseCardAction(danger);
		scn.ShadowChoose("discard pile");
		assertInZone(Zone.SUPPORT, danger, jail2);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedWithExactlyTheTwoAndTheCardsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var veteran = scn.GetShadowCard("veteran");
		var sentinel = scn.GetShadowCard("sentinel");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(sentinel);
		scn.MoveCardsToDiscard(veteran);
		// 2 removed, then 2 + 2 roaming for the Orc.
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.DECK, sentinel);
		assertInZone(Zone.DISCARD, veteran);

		assertTrue(scn.ShadowActionAvailable(danger));
		scn.ShadowUseCardAction(danger);
		scn.ShadowChoose("discard pile");

		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithASauronCardOnlyInTheDiscardPileItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(aggressor);
		scn.MoveCardsToDiscard(veteran);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.DECK, aggressor);
		assertInZone(Zone.DISCARD, veteran);

		scn.ShadowUseCardAction(danger);
		// No choice of pile, the draw deck holding no [sauron] card, and the one card there is played unasked.
		assertFalse(scn.ShadowChoiceAvailable("draw deck"));
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertInZone(Zone.DECK, aggressor);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithLessThanTwoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var jail = scn.GetShadowCard("jail");
		var sentinel = scn.GetShadowCard("sentinel");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(sentinel);
		scn.MoveCardsToDiscard(jail);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DECK, sentinel);
		assertInZone(Zone.DISCARD, jail);
		assertEquals(1, scn.GetTwilight());

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(danger));
	}

	@Test
	public void IsNotOfferedWithNoSauronCardInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var aggressor = scn.GetShadowCard("aggressor");
		var river = scn.GetShadowCard("river");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(aggressor);
		scn.MoveCardsToDiscard(river);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.DECK, aggressor);
		assertInZone(Zone.DISCARD, river);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(danger));
	}

	@Test
	public void IsNotOfferedWithOneTwilightTooFewForTheOnlySauronCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(danger);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, danger);
		scn.FreepsPass();
		scn.SetShadowDrawDeck(aggressor);
		scn.MoveCardsToDiscard(veteran);
		// 2 removed leaves 3 against the Orc's 2 + 2 roaming.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.DECK, aggressor);
		assertInZone(Zone.DISCARD, veteran);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(danger));
	}

	@Test
	public void SkirmishRemovesADoubtOrExertsSauronToMakeAnOrcStrongerUpToPlus3EachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var sauron = scn.GetShadowCard("sauron");
		var veteran = scn.GetShadowCard("veteran");
		var sentinel = scn.GetShadowCard("sentinel");
		var troop = scn.GetShadowCard("troop");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCardsToSupportArea(danger);
		scn.MoveCompanionsToTable(balin, dwalin);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, veteran, sentinel, troop);
		scn.AddBurdens(3);
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.SHADOW_CHARACTERS, sauron, veteran, sentinel, troop);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sauron));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, veteran}, new PhysicalCardImpl[]{dwalin, sentinel});
		// Sauron and the Troop are left over.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		// Both costs can be paid: the player is asked which.
		assertTrue(scn.ShadowActionAvailable(danger));
		scn.ShadowUseCardAction(danger);
		assertEquals(List.of("Remove a doubt", "Exert Sauron"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove a doubt");
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sauron));
		// Orcs of two cultures, in this skirmish, in another to come and in none; not Sauron, who is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, sentinel, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron));
		scn.ShadowChooseCard(veteran);
		assertEquals(8, scn.GetStrength(veteran));

		scn.FreepsPass();
		scn.ShadowUseCardAction(danger);
		scn.ShadowChoose("Exert Sauron");
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(sauron));
		scn.ShadowChooseCard(troop);
		assertEquals(14, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowUseCardAction(danger);
		scn.ShadowChoose("Remove a doubt");
		scn.ShadowChooseCard(veteran);
		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(veteran));

		// The card has given +3 this skirmish: offered, paid for, and nothing more.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(danger));
		scn.ShadowUseCardAction(danger);
		scn.ShadowChoose("Remove a doubt");
		scn.ShadowChooseCard(sentinel);
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(sentinel));
		assertEquals(9, scn.GetStrength(veteran));
		assertEquals(14, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonuses are gone, and the card gives again.  With no doubt
		// left, Sauron is exerted unasked.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(7, scn.GetStrength(veteran));
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPass();
		scn.ShadowUseCardAction(danger);
		assertEquals(2, scn.GetWoundsOn(sauron));
		scn.ShadowChooseCard(sentinel);
		assertEquals(6, scn.GetStrength(sentinel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoSauronInPlayRemovesADoubtUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var veteran = scn.GetShadowCard("veteran");
		var troop = scn.GetShadowCard("troop");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToSupportArea(danger);
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, troop);
		scn.AddBurdens(1);
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran, troop);
		assertEquals(1, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		scn.ShadowUseCardAction(danger);
		assertEquals(0, scn.GetBurdens());
		scn.ShadowChooseCard(veteran);
		assertEquals(8, scn.GetStrength(veteran));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoDoubtAndSauronExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var danger = scn.GetShadowCard("danger");
		var sauron = scn.GetShadowCard("sauron");
		var veteran = scn.GetShadowCard("veteran");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToSupportArea(danger);
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		// Another [sauron] minion able to exert stands beside him.
		scn.MoveMinionsToTable(sauron, veteran);
		scn.AddWoundsToChar(sauron, 3);
		assertInZone(Zone.SUPPORT, danger);
		assertInZone(Zone.SHADOW_CHARACTERS, sauron, veteran);
		assertEquals(3, scn.GetWoundsOn(sauron));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, veteran);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(danger));
		assertEquals(0, scn.GetWoundsOn(veteran));
	}
}
