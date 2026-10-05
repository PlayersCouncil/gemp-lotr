package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("bilbo", "101_50");

					// The Ring-bearer, Frodo, Son of Drogo, is a [shire] companion with the Frodo signet.
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion, Ring-bound, with the Aragorn signet
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion with the Frodo signet, (2); an Elf to exert for the Phial
					put("guard", "1_7");        // Dwarf Guard: a companion with no signet

					put("coat", "13_153");      // Mithril-coat, Dwarf-mail: [shire] artifact, (2); bearer must be a Ring-bound Hobbit
					put("sting", "1_313");      // Sting: [shire] possession, (1); bearer must be Frodo
					put("phial", "3_24");       // Phial of Galadriel: [elven] possession, (0); to play, exert an Elf; bearer must be the Ring-bearer
					put("sword", "1_299");      // Hobbit Sword: [shire] hand weapon, (1); bearer must be a Hobbit; no other text
					put("pipe", "1_285");       // Bilbo's Pipe: [shire] pipe, (1); bearer must be a Hobbit
					put("pan", "3_108");        // Frying Pan: [shire] hand weapon, (0); bearer must be a Hobbit

					put("tale", "1_317");       // There and Back Again: [shire] condition; bearer must be a Hobbit companion
					put("toby", "1_305");       // Old Toby: [shire] possession that plays to the support area
					put("armor", "1_8");        // Dwarven Armor: possession, (0); bearer must be a Dwarf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Bilbo, Of Bag End
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: To play, spot 2 companions with the Frodo signet.
		 * 	Fellowship: If the fellowship is at a sanctuary, exert Bilbo twice to play 2 items from your draw deck
		 * 	on [shire] companions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bilbo");

		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Of Bag End", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForTwoTwilightWithTwoFrodoSignetCompanionsToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var legolas = scn.GetFreepsCard("legolas");
		// With Frodo, two companions with the Frodo signet: one [shire], one [elven].
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(bilbo);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.HAND, bilbo);
		assertTrue(scn.FreepsPlayAvailable(bilbo));

		scn.FreepsPlayCard(bilbo);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("F-0040: the definition has no requirement to play; Bilbo is playable with one Frodo-signet companion in play")
	public void CannotBePlayedUntilASecondCompanionWithTheFrodoSignetIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		// Frodo has the Frodo signet.  Beside him, a companion with the Aragorn signet and one with none.
		scn.MoveCompanionsToTable(sam, guard);
		scn.MoveCardsToHand(bilbo, legolas);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(bilbo));

		// The second companion with the Frodo signet, and not a [shire] one.
		scn.FreepsPlayCard(legolas);

		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsPlayAvailable(bilbo));
		scn.FreepsPlayCard(bilbo);

		assertInZone(Zone.SUPPORT, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsOfferedOnlyAtASanctuaryAndPutsItsItemsOnTheShireCompanionsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var pipe = scn.GetFreepsCard("pipe");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		// Bilbo is unwounded throughout, and could exert twice at every site.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsActionAvailable(bilbo));

		scn.SkipToSite(2);
		assertFalse(scn.FreepsActionAvailable(bilbo));

		// Rivendell Valley, a sanctuary, and Bilbo's home.
		scn.SkipToSite(3);
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.SkipToSite(4);
		assertFalse(scn.FreepsActionAvailable(bilbo));

		scn.SkipToSite(5);
		assertFalse(scn.FreepsActionAvailable(bilbo));

		// Galadriel's Glade, a sanctuary that is not his home.
		scn.SkipToSite(6);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		// The turns played on the way have drawn the deck into hand.
		scn.MoveCardsToTopOfDeck(sword, pipe, coat);
		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(bilbo);
		assertEquals(2, scn.GetWoundsOn(bilbo));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, pipe, coat));
		scn.FreepsChooseCardBPFromSelection(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, coat));
		scn.FreepsChooseCardBPFromSelection(pipe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(sword, frodo);
		assertNotAttachedTo(sword, sam);
		assertAttachedTo(pipe, sam);
		assertNotAttachedTo(pipe, frodo);
		// 1 for the Sword + 1 for the Pipe
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtASanctuaryExertsBilboTwiceToPlayTwoChosenItemsFromTheDrawDeckAndReshufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var guard = scn.GetFreepsCard("guard");
		var coat = scn.GetFreepsCard("coat");
		var sting = scn.GetFreepsCard("sting");
		var phial = scn.GetFreepsCard("phial");
		var sword = scn.GetFreepsCard("sword");
		var pipe = scn.GetFreepsCard("pipe");
		var pan = scn.GetFreepsCard("pan");
		var tale = scn.GetFreepsCard("tale");
		var toby = scn.GetFreepsCard("toby");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(sam, legolas);
		// Items that are not in the draw deck.
		scn.MoveCardsToHand(pan);
		scn.MoveCardsToDiscard(pipe);
		// Site 1 is no sanctuary; every site is made one, which saves playing two turns to reach site 3.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.SANCTUARY));

		scn.StartGame();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertInZone(Zone.DECK, coat, sting, phial, sword, tale, toby, armor, guard);
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);

		assertEquals(2, scn.GetWoundsOn(bilbo));
		scn.FreepsDismissRevealedCards();
		// An artifact and three possessions, three [shire] and one [elven].
		assertTrue(scn.FreepsHasCardChoiceAvailable(coat, sting, phial, sword));
		// Not the condition a Hobbit companion bears, the possession for the support area, the possession
		// only a Dwarf bears, nor the companion; and not the item in hand or the one in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale, toby, armor, guard, pan, pipe));
		scn.FreepsChooseCardBPFromSelection(sting);

		// Sting's bearer must be Frodo, so nobody is asked who bears it.
		assertAttachedTo(sting, frodo);
		assertEquals(1, scn.GetTwilight());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(coat, phial, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sting, tale, toby, armor, guard, pan, pipe));
		scn.FreepsChooseCardBPFromSelection(phial);

		// The Phial's own cost is paid: Legolas is the only Elf, and exerts.  It costs (0).
		assertAttachedTo(phial, frodo);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, coat, sword, tale, toby, armor, guard);
		assertInZone(Zone.HAND, pan);
		assertInDiscard(pipe);
		// A deck that has been looked through is reshuffled; the other player's is left alone.
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// Vitality 3 with 2 wounds: Bilbo cannot exert again.
		assertFalse(scn.FreepsActionAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachItemGoesOnlyOnAShireCompanionAndBothMayGoOnTheSameOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var pipe = scn.GetFreepsCard("pipe");
		var pan = scn.GetFreepsCard("pan");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(sam, legolas);
		scn.MoveCardsToHand(pan);
		// A Hobbit companion who is not [shire].  Bilbo is a [shire] Hobbit who is not a companion.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, legolas, null, Race.HOBBIT));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.SANCTUARY));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(bilbo);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, pipe));
		scn.FreepsChooseCardBPFromSelection(sword);

		// The Sword's bearer must be a Hobbit, and all four are.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(sword, sam);
		assertNotAttachedTo(sword, frodo);

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe));
		scn.FreepsChooseCardBPFromSelection(pipe);

		// So must the Pipe's, and Sam is offered again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(pipe, sam);
		assertNotAttachedTo(pipe, frodo);
		// 1 for the Sword + 1 for the Pipe
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Played from hand, an item whose bearer must be a Hobbit may go on Bilbo or on Legolas.  Sam has a
		// hand weapon already.
		scn.FreepsPlayCard(pan);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bilbo, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCard(legolas);

		assertAttachedTo(pan, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceBilboHasAWoundAndCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var pan = scn.GetFreepsCard("pan");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(pan);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.SANCTUARY));

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.AddWoundsToChar(bilbo, 1);
		// The game works out what is on offer when it asks, so something is played to make it ask again.
		scn.FreepsPlayCard(pan);
		scn.FreepsChooseCard(frodo);

		// Vitality 3 with 1 wound: Bilbo could exert once.
		assertAttachedTo(pan, frodo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.IsExhausted(bilbo));
		assertFalse(scn.FreepsActionAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneItemInTheDrawDeckThatOneIsPlayedAndNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var tale = scn.GetFreepsCard("tale");
		var toby = scn.GetFreepsCard("toby");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(sam);
		// Every item a [shire] companion could bear but the Hobbit Sword.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("coat"), scn.GetFreepsCard("sting"), scn.GetFreepsCard("phial"),
				scn.GetFreepsCard("pipe"), scn.GetFreepsCard("pan"));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.SANCTUARY));

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertInZone(Zone.DECK, sword, tale, toby, armor);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword));
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(sword, sam);
		// The deck is looked through for the second item, and nothing in it can be chosen.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale, toby, armor));
		assertEquals(0, scn.FreepsGetChoiceMax());
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetTwilight());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, tale, toby, armor);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoItemInTheDrawDeckBilboStillExertsTwiceAndTheDeckIsLookedThroughAndReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var sam = scn.GetFreepsCard("sam");
		var tale = scn.GetFreepsCard("tale");
		var toby = scn.GetFreepsCard("toby");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(sam);
		// Every item a [shire] companion could bear.  A condition one could bear, a possession for the support
		// area and a possession for a Dwarf are still in the deck.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("coat"), scn.GetFreepsCard("sting"), scn.GetFreepsCard("phial"),
				scn.GetFreepsCard("pipe"), scn.GetFreepsCard("pan"), scn.GetFreepsCard("sword"));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.SANCTUARY));

		scn.StartGame();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();
		assertInZone(Zone.DECK, tale, toby, armor);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		// There is no penalty for looking in a draw deck for a card that is not there.
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);

		assertEquals(2, scn.GetWoundsOn(bilbo));
		// The deck is looked through once for each item, and nothing in it can be chosen.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale, toby, armor));
		assertEquals(0, scn.FreepsGetChoiceMax());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale, toby, armor));
		assertEquals(0, scn.FreepsGetChoiceMax());
		int shufflesBeforeTheLastLookEnds = scn.GetFreepsDeckShuffleCount();
		scn.FreepsDismissRevealedCards();

		assertEquals(0, scn.GetTwilight());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, tale, toby, armor);
		// The deck is reshuffled once the last look through it is over, with nothing having left it.
		assertTrue(scn.GetFreepsDeckShuffleCount() > shufflesBeforeTheLastLookEnds);
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
