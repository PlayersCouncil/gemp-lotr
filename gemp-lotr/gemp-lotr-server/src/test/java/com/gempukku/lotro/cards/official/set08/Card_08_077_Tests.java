package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_077_Tests
{

	// Every test is in the Shadow phase at site 2 (Ettenmoors, Shadow number 2), so a minion of a higher site
	// number pays the roaming penalty of 2 as it is played.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("squealer", "8_77");
					put("squealer2", "8_77");

					// Úlairë Lemenya: [wraith] Nazgûl, cost 4, site 3; lowers companions who bear a [wraith] condition
					put("lemenya", "1_232");
					// Úlairë Nelya: [wraith] Nazgûl, cost 5, site 2; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, cost 2, site 4; stronger only at 2 wounds on the Ring-bearer
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, cost 3, site 4; archery +2 only at 3 burdens
					put("creeper2", "8_75");
					put("steed", "1_208");      // Black Steed: [wraith] possession (a mount), cost 1, on a Nazgûl
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, site 6; his text answers a weapon played on him

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: companion; stronger against a Nazgûl only
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion; a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: companion; an archery ability used only on purpose
					put("guard", "5_83");       // Household Guard: companion; valiant and nothing else
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSquealerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Morgul Squealer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Shadow:</b> Discard this minion to play a [wraith] minion from your discard pile.
		 * <b>Shadow:</b> Spot 6 companions and discard this minion to play up to 2 [wraith] minions from your draw
		 * deck; their twilight costs are each -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("squealer");

		assertEquals("Morgul Squealer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardsItselfToPlayAWraithMinionFromItsOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squealer = scn.GetShadowCard("squealer");
		var squealer2 = scn.GetShadowCard("squealer2");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var chieftain = scn.GetShadowCard("chieftain");
		// A choice from a pile is read by blueprint, so the other player's [wraith] minion is a Creeper, a
		// blueprint the Shadow player's pile does not hold.
		var freepsCreeper = scn.GetFreepsCard("creeper");
		scn.MoveCardsToDiscard(lemenya, hound, steed, chieftain, freepsCreeper);

		scn.StartGame();
		// Laid before the move: the Shadow player's actions are worked out as his phase begins. Nelya is a Nazgûl
		// in play, so the Black Steed in the discard pile could be played.
		scn.MoveMinionsToTable(squealer, squealer2, nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(squealer, "discard pile"));

		scn.ShadowUseCardAction(squealer, "discard pile");
		// The cost: this Squealer is discarded, and the other stays in play.
		assertInZone(Zone.DISCARD, squealer);
		assertInZone(Zone.SHADOW_CHARACTERS, squealer2);

		// A Nazgûl and a [wraith] Orc; not a [wraith] possession, a minion of another culture, the [wraith] minion
		// in the other player's discard pile, nor the Squealer that has just gone there.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, chieftain, freepsCreeper, squealer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertInZone(Zone.DISCARD, lemenya, steed, chieftain, freepsCreeper, squealer);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardPileAbilityIsNotOfferedWithNoWraithMinionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squealer = scn.GetShadowCard("squealer");
		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var chieftain = scn.GetShadowCard("chieftain");
		var freepsHound = scn.GetFreepsCard("hound");
		scn.MoveCardsToDiscard(steed, chieftain, freepsHound);

		scn.StartGame();
		scn.MoveMinionsToTable(squealer, nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [wraith] possession that could be played on Nelya, a minion of another culture, and a [wraith] minion
		// in the other player's discard pile.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(squealer, "discard pile"));
		assertInZone(Zone.SHADOW_CHARACTERS, squealer);
	}

	@Test
	public void DiscardPileAbilityIsNotOfferedWhenTheTwilightPoolIsOneShortOfTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squealer = scn.GetShadowCard("squealer");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToDiscard(nelya);

		scn.StartGame();
		scn.MoveMinionsToTable(squealer);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 + 2 for the site + 1 for Frodo; Nelya costs 5, and site 2 is hers, so no roaming penalty.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(squealer, "discard pile"));
		assertInZone(Zone.SHADOW_CHARACTERS, squealer);
	}

	@Test
	public void WithSixCompanionsDiscardsItselfToPlayTwoWraithMinionsFromTheDrawDeckEachOneCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squealer = scn.GetShadowCard("squealer");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var creeper = scn.GetShadowCard("creeper");
		var creeper2 = scn.GetShadowCard("creeper2");
		var steed = scn.GetShadowCard("steed");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable("aragorn", "arwen", "boromir", "legolas", "guard");

		scn.StartGame();
		scn.MoveMinionsToTable(squealer);
		scn.MoveCardsToTopOfDeck(steed, chieftain, creeper, hound, lemenya);
		scn.MoveCardsToHand(creeper2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Frodo and five more: six companions.
		int pool = scn.GetTwilight();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(squealer, "draw deck"));

		scn.ShadowUseCardAction(squealer, "draw deck");
		assertInZone(Zone.DISCARD, squealer);

		scn.ShadowDismissRevealedCards();
		// A Nazgûl and two [wraith] Orcs; not a [wraith] possession or a minion of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound, creeper));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, chieftain));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		// 4 - 1 + 2 roaming
		assertEquals(pool - 5, scn.GetTwilight());

		scn.ShadowDismissRevealedCards();
		// The Black Steed could now be played on Lemenya; it is still no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, creeper));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, chieftain));
		scn.ShadowChooseCardBPFromSelection(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		// 2 - 1 + 2 roaming
		assertEquals(pool - 8, scn.GetTwilight());

		// Two and no more: the second Creeper is still in the deck.
		assertInZone(Zone.DECK, creeper, steed, chieftain);
		assertTrue(scn.GetShadowDeckShuffleCount() > shuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [wraith] minion played from hand afterwards pays its full cost: 3 + 2 roaming.
		scn.ShadowPlayCard(creeper2);
		assertEquals(pool - 13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawDeckPlaysMayBothBeDeclinedAndTheDeckIsStillShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squealer = scn.GetShadowCard("squealer");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable("aragorn", "arwen", "boromir", "legolas", "guard");

		scn.StartGame();
		scn.MoveMinionsToTable(squealer);
		scn.MoveCardsToTopOfDeck(hound, lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(squealer, "draw deck"));

		scn.ShadowUseCardAction(squealer, "draw deck");
		assertInZone(Zone.DISCARD, squealer);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		scn.ShadowDeclineChoosing();
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DECK, lemenya, hound);
		assertEquals(pool, scn.GetTwilight());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawDeckAbilityIsNotOfferedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var squealer = scn.GetShadowCard("squealer");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable("aragorn", "arwen", "boromir", "legolas");
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("bounder"));

		scn.StartGame();
		scn.MoveMinionsToTable(squealer);
		scn.MoveCardsToTopOfDeck(hound, lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Frodo and four more are five companions; the Bounder is a character who is no companion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(squealer, "draw deck"));
		assertInZone(Zone.SHADOW_CHARACTERS, squealer);
	}
}
