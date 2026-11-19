package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mirror", "15_22");
					put("elf", "1_53");          // Lorien Elf: an Elf companion with no keyword (cheated in past "To play, spot an Elf")
					put("celeborn", "1_34");     // Celeborn, Lord of Lorien: an Elf ally; a Fellowship heal used only on purpose
					put("legolas", "1_51");      // Legolas, Prince of Mirkwood: an Elf companion with no requirement to play
					put("uruviel", "15_26");     // Uruviel, Woodland Maid: an [elven] follower, which has no race; its texts act only on a transfer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mirror", "15_22");
					// Elrond, Lord of Rivendell: an Elf ally of vitality 4; "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					put("legolas", "1_50");         // Legolas, Greenleaf: an Elf companion; his ability is an archery one
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a companion who is no Elf
					// Morgul on the March: "Each time the Free Peoples player draws a card (except during the
					// regroup phase), add a threat."  To play, spot a Nazgûl: it is cheated in.
					put("march", "7_195");

					// The draw deck.  Free Peoples cards:
					put("sword", "1_299");          // Hobbit Sword
					put("axe", "1_3");              // Axe Strike
					put("blow", "1_5");             // Cleaving Blow
					// and Shadow cards:
					put("runner", "1_178");         // Goblin Runner
					put("scimitar", "1_180");       // Goblin Scimitar
					put("troop", "1_177");          // Goblin Patrol Troop
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOtherDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mirror", "15_22");
					put("legolas", "1_50");         // Legolas, Greenleaf: an Elf companion who can pay the Mirror's exertion
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: the [gondor] companion Drawing His Eye spots
					// Barliman Butterbur: "Fellowship: Exert Barliman Butterbur to take a [gandalf] event into hand from
					// your discard pile": a card taken into hand that is not drawn.
					put("barliman", "1_70");
					put("wizard", "1_78");          // Mysterious Wizard: a [gandalf] event for the discard pile
					// Drawing His Eye: reveals a Shadow card at random for each [gondor] companion spotted, discards a
					// revealed Shadow card, "then its owner draws a card": the Shadow player's draw.
					put("eye", "10_29");
					put("runner", "1_178");         // Goblin Runner: the one card in the Shadow hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMirrorofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: The Mirror of Galadriel, Dangerous Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot 3 Elves.<br>Each time you are about to draw a card, you may exert an Elf to look at the top three cards of your draw deck instead. Take a Free Peoples card into your hand and place the other cards on the bottom of your draw deck.
		*/

		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");

		assertEquals("The Mirror of Galadriel", mirror.getBlueprint().getTitle());
		assertEquals("Dangerous Guide", mirror.getBlueprint().getSubtitle());
		assertTrue(mirror.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, mirror.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, mirror.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, mirror.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(mirror, Keyword.SUPPORT_AREA));
		assertNull(mirror.getBlueprint().getPossessionClasses());
		assertEquals(2, mirror.getBlueprint().getTwilightCost());
	}

	@Test
	public void AboutToDrawACardItsOwnerMayExertAnElfToLookAtTheTopThreeAndTakeAFreePeoplesCardInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var sword = scn.GetFreepsCard("sword");
		var axe = scn.GetFreepsCard("axe");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		// The draw deck from the top: Goblin Runner, Hobbit Sword, Axe Strike, Cleaving Blow, and three more.
		scn.MoveCardsToTopOfDeck(blow, axe, sword, runner);
		assertInZone(Zone.DECK, blow, axe, sword, runner);
		scn.FreepsRefreshDecision();
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		// Before the card is drawn, the Mirror asks.
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mirror));
		scn.FreepsAcceptOptionalTrigger();

		// "Exert an Elf": the ally and the companion, and not the Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(elrond));

		// The top three are looked at, and no card beneath them: the two Free Peoples cards may be taken,
		// and the Shadow card is only shown.  Cleaving Blow, the fourth card, is a Free Peoples card and
		// would be one to take if it were among them.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, blow));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetFreepsHandCount());

		// The lower of the two is taken: the third card of the deck, not the one that was about to be drawn.
		scn.FreepsChooseCardBPFromSelection(axe);
		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());

		// The other two go beneath the deck in the order their owner gives: the Sword, and the Runner under it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, runner));
		scn.FreepsChooseCardBPFromSelection(sword);
		assertEquals(6, scn.GetFreepsDeckCount());
		assertSame(runner, scn.GetFreepsBottomOfDeck());
		assertSame(sword, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(blow, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// A draw replaced by the look moves no card from the top of the deck into hand.  The card the Mirror then
	// takes "did come from the top of your draw deck" (Ketura on Race Text 94_28, whose words are the Mirror's:
	// about to draw, look at the top cards instead, take one into hand, the rest beneath), so it is the card
	// drawn for a card that answers a draw.
	@Test
	public void TheCardTakenInPlaceOfTheDrawIsTheCardDrawnAndTheOneThatWasAboutToBeDrawnIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var sword = scn.GetFreepsCard("sword");
		var axe = scn.GetFreepsCard("axe");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(elrond);
		// Three companions with the Ring-bearer: room for three threats.
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("march"));

		scn.StartGame();
		// The draw deck from the top: Goblin Runner, Hobbit Sword, Axe Strike, Cleaving Blow.
		scn.MoveCardsToTopOfDeck(blow, axe, sword, runner);
		assertInZone(Zone.DECK, blow, axe, sword, runner);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(elrond);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);
		// The three are being looked at, and nothing has been drawn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, axe));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseCardBPFromSelection(axe);
		scn.FreepsChooseCardBPFromSelection(sword);
		// Goblin Runner, the card that was about to be drawn, lies beneath the deck.  Axe Strike is the one
		// card in hand, and Morgul on the March answers it once.
		assertSame(runner, scn.GetFreepsBottomOfDeck());
		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The control: the Mirror declined, the top card is drawn, and that is one threat as well.
		scn.FreepsUseCardAction(elrond);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mirror));
		scn.FreepsDeclineOptionalTrigger();
		assertInHand(blow);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ALookThatFindsNoFreePeoplesCardTakesNothingAndNothingIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var scimitar = scn.GetFreepsCard("scimitar");
		var troop = scn.GetFreepsCard("troop");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("march"));

		scn.StartGame();
		// The draw deck from the top: three Shadow cards, then Cleaving Blow.
		scn.MoveCardsToTopOfDeck(blow, troop, scimitar, runner);
		assertInZone(Zone.DECK, blow, troop, scimitar, runner);
		scn.FreepsRefreshDecision();
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetThreats());

		scn.FreepsUseCardAction(elrond);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));

		// The three are shown and none of them may be taken.  Cleaving Blow, the fourth card, is a Free
		// Peoples card: were it looked at too, four would be shown and one could be taken.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(blow));
		assertEquals(0, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();

		// All three go beneath the deck, in the order their owner gives.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, scimitar, troop));
		scn.FreepsChooseCardBPFromSelection(runner);
		scn.FreepsChooseCardBPFromSelection(scimitar);
		assertSame(troop, scn.GetFreepsBottomOfDeck());
		assertSame(scimitar, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(runner, scn.GetFromBottomOfFreepsDeck(3));
		assertSame(blow, scn.GetFreepsTopOfDeck());
		// The draw was replaced and no card came of the look: nothing is in hand, the deck is whole, and
		// Morgul on the March has nothing to answer.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The control, on the same table: a card that is drawn is answered.
		scn.FreepsUseCardAction(elrond);
		scn.FreepsDeclineOptionalTrigger();
		assertInHand(blow);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// "Look at" and "place the other cards on the bottom": nothing is revealed, so the other player learns which
	// card was taken (it had to be a Free Peoples card, and the log line is what shows that: Q-0025) and not
	// which cards went beneath the deck (Q-0176).  The log used to name those too (F-0378).
	@Test
	public void TheCardsPlacedBeneathTheDrawDeckAreNotNamedInTheGameLog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		var sword = scn.GetFreepsCard("sword");
		var axe = scn.GetFreepsCard("axe");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		// The draw deck from the top: Goblin Runner, Hobbit Sword, Axe Strike, Cleaving Blow.
		scn.MoveCardsToTopOfDeck(blow, axe, sword, runner);
		assertInZone(Zone.DECK, blow, axe, sword, runner);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(elrond);
		scn.MarkGameLog();
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(legolas);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseCardBPFromSelection(axe);
		scn.FreepsChooseCardBPFromSelection(sword);

		assertInHand(axe);
		assertSame(runner, scn.GetFreepsBottomOfDeck());
		assertSame(sword, scn.GetFromBottomOfFreepsDeck(2));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(axe));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(sword));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedOnceThreeElvesCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var elf = scn.GetFreepsCard("elf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var legolas = scn.GetFreepsCard("legolas");
		var uruviel = scn.GetFreepsCard("uruviel");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(celeborn);
		// Uruviel is an [elven] follower: no race, so no Elf.
		scn.MoveCardsToSupportArea(uruviel);
		scn.MoveCardsToHand(mirror, legolas);
		assertInHand(mirror, legolas);
		assertInZone(Zone.SUPPORT, uruviel);

		scn.StartGame();

		// Two Elves, a companion and an ally.
		assertFalse(scn.FreepsPlayAvailable(mirror));

		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsPlayAvailable(mirror));
		scn.FreepsPlayCard(mirror);
		assertInZone(Zone.SUPPORT, mirror);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryElfExhaustedTheMirrorIsNotOfferedAndTheCardIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(legolas, aragorn);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(runner);
		assertInZone(Zone.DECK, runner);
		// Legolas (vitality 3) exhausted; Elrond (vitality 4) will be, once he has paid for his draw.
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(elrond, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertSame(runner, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(elrond);
		assertEquals(3, scn.GetWoundsOn(elrond));
		// Aragorn could exert, but he is no Elf.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mirror));
		assertInHand(runner);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardTakenFromTheDiscardPileAndTheShadowPlayersDrawAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherDrawScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var wizard = scn.GetFreepsCard("wizard");
		var eye = scn.GetFreepsCard("eye");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToDiscard(wizard);
		assertInDiscard(wizard);

		scn.StartGame();
		scn.SetFreepsHand(eye);
		scn.SetShadowHand(runner);
		scn.FreepsRefreshDecision();
		assertInHand(eye, runner);
		assertEquals(0, scn.GetWoundsOn(legolas));
		int shadowDeck = scn.GetShadowDeckCount();

		// A card taken into hand from the discard pile is no draw.
		scn.FreepsUseCardAction(barliman);
		assertInHand(wizard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mirror));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The Shadow player's draw is not "you".
		scn.FreepsPlayCard(eye);
		scn.FreepsChoose("1");
		scn.FreepsDismissRevealedCards();
		assertInDiscard(runner, eye);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mirror));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
