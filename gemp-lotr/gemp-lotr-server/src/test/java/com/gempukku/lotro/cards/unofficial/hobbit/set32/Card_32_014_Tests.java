package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_014_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nenya", "32_14");
					// Galadriel, Elven Lady: [elven] Elf ally, Wise, 3/3; a start-of-turn heal of a wounded Gandalf and
					// a skirmish ability, not used
					put("galadriel", "32_13");
					// Galadriel, Bearer of Wisdom: the Galadriel who is a companion, 3/3; resistance text only
					put("galcomp", "9_14");
					// Galadriel, Lady of the Golden Wood: a Decipher Galadriel who is an ally, 3/3; a start-of-turn heal
					// of a wounded Elf (laid after it) and a fellowship ability, not used
					put("golden", "3_17");
					// Elrond, Elven Lord: [elven] Elf ally, Wise, 8/4; a start-of-turn heal of a wounded Wise ally (none
					// is wounded where he is laid) and a regroup ability, not used
					put("elrond", "30_21");
					// Radagast, The Brown: [gandalf] Wizard ally, Wise; a fellowship ability that wants a Gandalf in
					// the discard pile
					put("radagast", "31_17");
					// Thranduil, Elven King: [elven] Elf ally, not Wise; a maneuver ability that wants Thorin or a
					// [dwarven] artifact
					put("thranduil", "31_12");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, Wise; a fellowship ability, a discard bar
					// that needs 2 [dwarven] followers, and a discount on Thorin
					put("gandalf", "30_26");

					// For the draw deck
					put("takeup", "30_31");     // Take up arms. Fight!: [gandalf] skirmish event, spell
					put("gathering", "32_22");  // Gathering of the Three Rings: [gandalf] maneuver event, spell
					put("courage", "30_30");    // He gives me courage: [gandalf] maneuver event, no spell
					put("chill", "1_134");      // Saruman's Chill: [isengard] condition, spell
					put("glamdring", "30_29");  // Glamdring: [gandalf] artifact
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Nenya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Game Text: Bearer must be Galadriel.<br>At the start of each of your turns, you may heal a Wise ally.<br><b>Fellowship:</b> Exert Galadriel and add (2) to take a [gandalf] spell from your draw deck into your hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nenya");

		assertEquals("Nenya", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void NenyaPlaysOnTheGaladrielWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(galadriel, elrond);
		scn.MoveCardsToHand(nenya);
		assertInHand(nenya);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(nenya));
		scn.FreepsPlayCard(nenya);

		// Galadriel is the one bearer, taken unasked; not Elrond, another Wise [elven] Elf ally.
		assertAttachedTo(nenya, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NenyaPlaysOnTheGaladrielWhoIsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galcomp = scn.GetFreepsCard("galcomp");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(galcomp);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(nenya);
		assertInHand(nenya);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(nenya));
		scn.FreepsPlayCard(nenya);

		assertAttachedTo(nenya, galcomp);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NenyaPlaysOnADecipherGaladrielAllyWhoThenPaysTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var golden = scn.GetFreepsCard("golden");
		var elrond = scn.GetFreepsCard("elrond");
		var takeup = scn.GetFreepsCard("takeup");
		var courage = scn.GetFreepsCard("courage");

		scn.StartGame();
		scn.MoveAlliesToTable(golden, elrond);
		scn.MoveCardsToHand(nenya);
		scn.SetFreepsDrawDeck(takeup, courage);
		scn.FreepsRefreshDecision();
		assertInHand(nenya);
		assertInZone(Zone.DECK, takeup, courage);

		assertTrue(scn.FreepsPlayAvailable(nenya));
		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, golden);

		assertEquals(0, scn.GetWoundsOn(golden));
		assertEquals(0, scn.GetWoundsOn(elrond));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsActionAvailable(nenya));
		scn.FreepsUseCardAction(nenya);

		assertEquals(1, scn.GetWoundsOn(golden));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(twilight + 2, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(takeup);
		assertInHand(takeup);
		assertInZone(Zone.DECK, courage);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NenyaCannotBePlayedWithNoGaladriel() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(nenya);
		assertInHand(nenya);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nenya));
		assertInHand(nenya);
	}

	@Test
	public void AtTheStartOfHerOwnersTurnNenyaMayHealAWiseAllyOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(galadriel, radagast, thranduil);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(galadriel, nenya);
		assertAttachedTo(nenya, galadriel);
		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(radagast, 1);
		scn.AddWoundsToChar(thranduil, 1);
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		// Galadriel's own offer to heal Gandalf stands beside Nenya's.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nenya));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		scn.FreepsChooseOptionalTrigger(nenya);

		// Wise allies of two cultures and races, her bearer among them; not an ally who is not Wise, nor a Wise
		// companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thranduil, gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(radagast);

		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Galadriel's own offer is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWiseAllyWoundedNenyaOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(galadriel, radagast, thranduil);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(galadriel, nenya);
		assertAttachedTo(nenya, galadriel);
		// Wounded, but neither is a Wise ally.
		scn.AddWoundsToChar(thranduil, 1);
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.StartGame();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(nenya));
		// Galadriel's own offer, for the wounded Gandalf, is all there is; declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(thranduil));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningNenyasOfferHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(galadriel, radagast);
		scn.AttachCardsTo(galadriel, nenya);
		assertAttachedTo(nenya, galadriel);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nenya));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NenyasOfferComesAgainAtHerOwnersNextTurnAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(galadriel, radagast);
		scn.AttachCardsTo(galadriel, nenya);
		assertAttachedTo(nenya, galadriel);
		scn.AddWoundsToChar(radagast, 1);
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nenya));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Turn 2, the other player's: nothing is offered to Nenya's owner.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(nenya));
		assertEquals(1, scn.GetWoundsOn(radagast));

		// Turn 3, her owner's: offered again, and taken. Radagast is the one Wise ally wounded, healed unasked.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nenya));
		scn.FreepsChooseOptionalTrigger(nenya);
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
	}

	@Test
	public void FellowshipAbilityExertsGaladrielAndAddsTwoToTakeAGandalfSpellFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var elrond = scn.GetFreepsCard("elrond");
		var takeup = scn.GetFreepsCard("takeup");
		var gathering = scn.GetFreepsCard("gathering");
		var courage = scn.GetFreepsCard("courage");
		var chill = scn.GetFreepsCard("chill");
		var glamdring = scn.GetFreepsCard("glamdring");

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel, elrond);
		scn.AttachCardsTo(galadriel, nenya);
		scn.SetFreepsDrawDeck(takeup, gathering, courage, chill, glamdring);
		scn.FreepsRefreshDecision();
		assertAttachedTo(nenya, galadriel);
		assertInZone(Zone.DECK, takeup, gathering, courage, chill, glamdring);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetTwilight());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(nenya));
		scn.MarkGameLog();
		scn.FreepsUseCardAction(nenya);

		// Galadriel, her bearer, exerts; Elrond does not.
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetTwilight());

		scn.FreepsDismissRevealedCards();
		// [gandalf] spells of two phases; not a [gandalf] event that is no spell, a spell of another culture, nor a
		// [gandalf] card that is no event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(takeup, gathering));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(courage, chill, glamdring));
		scn.FreepsChooseCardBPFromSelection(takeup);

		assertInHand(takeup);
		assertInZone(Zone.DECK, gathering, courage, chill, glamdring);
		// The card taken is named in the game log, and one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(takeup));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(gathering));
		// The deck looked through is shuffled, once; the other player's is not.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheGaladrielWhoIsACompanionPaysTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galcomp = scn.GetFreepsCard("galcomp");
		var takeup = scn.GetFreepsCard("takeup");
		var courage = scn.GetFreepsCard("courage");

		scn.StartGame();
		scn.MoveCompanionsToTable(galcomp);
		scn.AttachCardsTo(galcomp, nenya);
		scn.SetFreepsDrawDeck(takeup, courage);
		scn.FreepsRefreshDecision();
		assertAttachedTo(nenya, galcomp);
		assertInZone(Zone.DECK, takeup, courage);
		assertEquals(0, scn.GetWoundsOn(galcomp));
		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.FreepsActionAvailable(nenya));
		scn.FreepsUseCardAction(nenya);

		assertEquals(1, scn.GetWoundsOn(galcomp));
		assertEquals(2, scn.GetTwilight());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(takeup);
		assertInHand(takeup);
		assertInZone(Zone.DECK, courage);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfSpellInTheDrawDeckTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var courage = scn.GetFreepsCard("courage");
		var chill = scn.GetFreepsCard("chill");
		var glamdring = scn.GetFreepsCard("glamdring");

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel);
		scn.AttachCardsTo(galadriel, nenya);
		scn.SetFreepsDrawDeck(courage, chill, glamdring);
		scn.FreepsRefreshDecision();
		assertAttachedTo(nenya, galadriel);
		assertInZone(Zone.DECK, courage, chill, glamdring);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetTwilight());
		int handCount = scn.GetFreepsHandCount();

		// A [gandalf] event that is no spell, a spell of another culture and a [gandalf] artifact: nothing to take.
		// A Hobbit-set take from the draw deck with nothing to take is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418): Galadriel does not
		// exert, nor is (2) added, for a look that finds nothing.
		assertFalse(scn.FreepsActionAvailable(nenya));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, courage, chill, glamdring);
		assertEquals(handCount, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileGaladrielIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var elrond = scn.GetFreepsCard("elrond");
		var takeup = scn.GetFreepsCard("takeup");

		scn.StartGame();
		scn.MoveAlliesToTable(galadriel, elrond);
		scn.AttachCardsTo(galadriel, nenya);
		scn.SetFreepsDrawDeck(takeup);
		// Vitality 3, +1 for Nenya, with 3 wounds: she cannot exert. Elrond, another [elven] Elf ally, could.
		scn.AddWoundsToChar(galadriel, 3);
		scn.FreepsRefreshDecision();
		assertAttachedTo(nenya, galadriel);
		assertInZone(Zone.DECK, takeup);
		assertEquals(3, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(elrond));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(nenya));
		assertEquals(3, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertInZone(Zone.DECK, takeup);
	}
}
