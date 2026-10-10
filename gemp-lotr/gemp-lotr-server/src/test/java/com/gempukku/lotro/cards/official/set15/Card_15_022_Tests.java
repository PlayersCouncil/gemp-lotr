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
					put("card", "15_22");
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

		var card = scn.GetFreepsCard("card");

		assertEquals("The Mirror of Galadriel", card.getBlueprint().getTitle());
		assertEquals("Dangerous Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
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
}
