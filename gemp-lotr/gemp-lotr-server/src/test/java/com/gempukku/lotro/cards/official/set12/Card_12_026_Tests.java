package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("discoveries", "12_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExamineScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("discoveries", "12_26");
					// Gandalf, Friend of the Shirefolk: the [gandalf] Wizard, and the one who could exert for toil
					put("gandalf", "1_72");
					// Gimli, Skilled Defender: with the Ring-bearer, the two other companions; his text answers
					// a skirmish he wins
					put("gimli", "5_7");
					put("strike", "1_3");       // Axe Strike, Battle Fury and Cleaving Blow: the top three cards of the draw deck
					put("fury", "1_4");
					put("blow", "1_5");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAllyWizardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("discoveries", "12_26");
					// Saruman, The White (a Hobbit-set card): the one [gandalf] Wizard who is an ally and no
					// companion.  His texts are about Gandalf, a Wise ally to discard, and a skirmish.
					put("saruman", "32_24");
					put("gimli", "5_7");        // Gimli, Skilled Defender: with the Ring-bearer, the two companions
					put("strike", "1_3");       // Axe Strike, Battle Fury and Cleaving Blow: the top three cards of the draw deck
					put("fury", "1_4");
					put("blow", "1_5");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFourCompanionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("discoveries", "12_26");
					// Gandalf, Friend of the Shirefolk: the [gandalf] Wizard; a strength bonus by races is his whole text
					put("gandalf", "1_72");
					// Ent Avenger: a [gandalf] companion who is no Wizard; a strength bonus beside 3 Ents only
					put("avenger", "6_27");
					// Gimli, Skilled Defender: a [dwarven] Dwarf companion; his text answers a skirmish he wins
					put("gimli", "5_7");
					// Barliman Butterbur: a [gandalf] Man ally, no companion; a fellowship ability used only on purpose
					put("barliman", "1_70");
					put("strike", "1_3");       // Axe Strike, Battle Fury, Cleaving Blow and Glamdring: the top four cards of the draw deck
					put("fury", "1_4");
					put("blow", "1_5");
					put("glamdring", "1_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DiscoveriesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Discoveries
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 7
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>. <b>Toil 3</b>. <helper>(For each [gandalf] character you exert when playing this, its twilight cost is -3.)</helper><br>Spot a [gandalf] Wizard and X other companions to examine the top X cards of your draw deck. Replace those cards in any order.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("discoveries");

		assertEquals("Discoveries", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(7, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExaminesAsManyTopCardsAsOtherCompanionsSpottedAndPutsThemBackInTheOrderChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExamineScenario();

		var discoveries = scn.GetFreepsCard("discoveries");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToHand(discoveries);

		scn.StartGame();
		// From the top: Axe Strike, Battle Fury, Cleaving Blow.
		scn.MoveCardsToTopOfDeck(blow, fury, strike);
		scn.FreepsRefreshDecision();

		assertEquals(strike, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(fury, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(blow, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(0, scn.GetTwilight());

		// Toil 3: Gandalf could exert for it, and does not.
		scn.FreepsPlayCard(discoveries);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
		scn.FreepsDeclineChoosing();
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The game asks how many companions are spotted, the Wizard among them: Gandalf, Gimli and the
		// Ring-bearer are three, and X is the two others.
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");

		// The top two cards are shown to their owner alone, and the third is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, fury));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(blow));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// The card chosen first goes back first, and the other on top of it.
		scn.FreepsChooseCardBPFromSelection(strike);
		assertEquals(fury, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(strike, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(blow, scn.GetFromTopOfFreepsDeck(3));
		assertInZone(Zone.DISCARD, discoveries);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// "Spot a [gandalf] Wizard and X other companions": X may be none (CR 4.2 "spot": "you may choose to spot 2
	// Elves, 1 Elf, or none"), and then no card is examined.  The game's question counts the Wizard too and
	// starts at 0; answered 0, it used to make the number of cards to examine one less than nothing and throw
	// (F-0374).
	@Test
	public void SpottingNobodyExaminesNoCardAndTheGameGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExamineScenario();

		var discoveries = scn.GetFreepsCard("discoveries");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToHand(discoveries);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(blow, fury, strike);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(discoveries);
		scn.FreepsDeclineChoosing();
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChoose("0");

		// Nothing is shown and nothing moves; the event is played and done with.
		assertEquals(strike, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(fury, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(blow, scn.GetFromTopOfFreepsDeck(3));
		assertInZone(Zone.DISCARD, discoveries);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// "A [gandalf] Wizard and X other companions": where the Wizard spotted is no companion, every companion is
	// another one.  Only a Hobbit-set card shows it (every Decipher [gandalf] Wizard is a companion), on a table
	// of the Anything Goes format.  The game used to examine one card fewer than the companions spotted here
	// too (F-0374).
	@Test
	public void WithTheWizardAnAllyEveryCompanionSpottedIsAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyWizardScenario();

		var discoveries = scn.GetFreepsCard("discoveries");
		var saruman = scn.GetFreepsCard("saruman");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToHand(discoveries);

		scn.StartGame();
		// From the top: Axe Strike, Battle Fury, Cleaving Blow.
		scn.MoveCardsToTopOfDeck(blow, fury, strike);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(discoveries));

		// Toil 3: Saruman could exert for it, and does not.
		scn.FreepsPlayCard(discoveries);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman));
		scn.FreepsDeclineChoosing();
		assertEquals(7, scn.GetTwilight());

		// Gimli and the Ring-bearer are the two companions, both of them other than the Wizard: X is 2.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, fury));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(blow));

		scn.FreepsChooseCardBPFromSelection(strike);
		assertEquals(fury, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(strike, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(blow, scn.GetFromTopOfFreepsDeck(3));
		assertInZone(Zone.DISCARD, discoveries);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
	@Test
	public void CannotBePlayedWithoutAGandalfWizardThoughAGandalfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFourCompanionsScenario();

		var discoveries = scn.GetFreepsCard("discoveries");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(avenger, gimli);
		scn.MoveCardsToHand(discoveries);

		scn.StartGame();

		// The Ent Avenger is a [gandalf] companion, and no Wizard.
		assertInHand(discoveries);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(discoveries));

		// With Gandalf on the table it is.
		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(discoveries));
	}

	@Test
	public void WithThreeOtherCompanionsSpottedExaminesTheTopThreeAndAnAllyIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFourCompanionsScenario();

		var discoveries = scn.GetFreepsCard("discoveries");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var blow = scn.GetFreepsCard("blow");
		var glamdring = scn.GetFreepsCard("glamdring");
		// Barliman's home is site 1: he is in play and at the fellowship's site.
		scn.MoveCompanionsToTable(gandalf, avenger, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(discoveries);

		scn.StartGame();
		// From the top: Axe Strike, Battle Fury, Cleaving Blow, Glamdring.
		scn.MoveCardsToTopOfDeck(glamdring, blow, fury, strike);
		scn.FreepsRefreshDecision();

		assertEquals(strike, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(fury, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(blow, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(glamdring, scn.GetFromTopOfFreepsDeck(4));
		var deckSize = scn.GetFreepsDeckCount();
		var shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(discoveries);
		// Toil 3: nobody exerts for it.
		scn.FreepsDeclineChoosing();

		// Gandalf, the Ent Avenger, Gimli and the Ring-bearer are the companions; Barliman is an ally and is not
		// counted.  Spotting all four, X is the three other than the Wizard.
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("4");

		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, fury, blow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// Cleaving Blow goes back first, then Axe Strike, and Battle Fury last, on top.
		scn.FreepsChooseCardBPFromSelection(blow);
		scn.FreepsChooseCardBPFromSelection(strike);
		assertEquals(fury, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(strike, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(blow, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(glamdring, scn.GetFromTopOfFreepsDeck(4));
		// Nothing left the deck and nothing shuffled it: an examine of the top cards is no search.
		assertEquals(deckSize, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.DISCARD, discoveries);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
