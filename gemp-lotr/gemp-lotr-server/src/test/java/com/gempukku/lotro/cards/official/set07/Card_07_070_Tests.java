package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("serving", "7_70");

					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] Sméagol; to play, add a burden; a skirmish ability only
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Sméagol; an assignment ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is no Sméagol; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man ally, who is no companion; a fellowship ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion, laid in the other player's discard pile only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ServingthePreciousStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Serving the Precious
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Discard Sméagol to play a companion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("serving");

		assertEquals("Serving the Precious", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsSmeagolToPlayACompanionOfThePlayersChoiceFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var serving = scn.GetFreepsCard("serving");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var boromir = scn.GetShadowCard("boromir");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(serving);
		scn.MoveCardsToDiscard(aragorn, gandalf, barliman, boromir);
		assertInHand(serving);
		assertInDiscard(aragorn, gandalf, barliman, boromir);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(serving));
		scn.FreepsPlayCard(serving);

		// The cost: Sméagol, the only one there is, to the discard pile.
		assertInDiscard(smeagol);

		// Companions of two cultures and races; not an ally, nor a companion in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, boromir));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInDiscard(serving, smeagol, gandalf, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheSmeagolDiscardedForTheCostIsNotTheCompanionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var serving = scn.GetFreepsCard("serving");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(serving);
		scn.MoveCardsToDiscard(aragorn, gandalf);
		assertInHand(serving);
		assertInDiscard(aragorn, gandalf);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		scn.FreepsPlayCard(serving);
		assertInDiscard(smeagol);

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInDiscard(smeagol, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolPaysTheCostToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var serving = scn.GetFreepsCard("serving");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(serving);
		scn.MoveCardsToDiscard(aragorn, gandalf);
		assertInHand(serving);
		assertInDiscard(aragorn, gandalf);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(serving));
		scn.FreepsPlayCard(serving);

		assertInDiscard(smeagol);
		scn.FreepsChooseCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInDiscard(serving, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutSmeagol() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var serving = scn.GetFreepsCard("serving");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(serving);
		scn.MoveCardsToDiscard(aragorn);
		assertInHand(serving);
		assertInDiscard(aragorn);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(serving));
	}

	@Test
	public void CannotBePlayedWithNoCompanionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var serving = scn.GetFreepsCard("serving");
		var smeagol = scn.GetFreepsCard("smeagol");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(serving);
		// An ally, who is no companion, is all the pile holds.
		scn.MoveCardsToDiscard(barliman);
		assertInHand(serving);
		assertInDiscard(barliman);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(serving));
	}
}
